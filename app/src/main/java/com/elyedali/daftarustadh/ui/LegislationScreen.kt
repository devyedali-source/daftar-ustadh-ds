package com.elyedali.daftarustadh.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/* ---------- البيانات ---------- */

data class LegArticle(val title: String, val chapter: String, val text: String)
data class LegDoc(val title: String, val ref: String, val part: String, val articles: List<LegArticle>)

object LegislationRepository {
    private const val ASSET = "legislation/legislation.json"
    @Volatile private var cache: List<LegDoc>? = null

    fun load(context: Context): List<LegDoc> {
        cache?.let { return it }
        val root = JSONObject(context.assets.open(ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() })
        val docs = mutableListOf<LegDoc>()
        val parts = root.getJSONArray("parts")
        for (p in 0 until parts.length()) {
            val part = parts.getJSONObject(p)
            val partTitle = part.getString("title")
            val ds = part.getJSONArray("docs")
            for (d in 0 until ds.length()) {
                val doc = ds.getJSONObject(d)
                val arts = doc.getJSONArray("articles")
                docs += LegDoc(
                    title = doc.getString("title"),
                    ref = doc.optString("ref"),
                    part = partTitle,
                    articles = (0 until arts.length()).map { i ->
                        val a = arts.getJSONObject(i)
                        LegArticle(a.getString("title"), a.optString("chapter"), a.getString("text"))
                    }
                )
            }
        }
        return docs.also { cache = it }
    }

    /** توحيد الحروف للبحث: الهمزات والتاء المربوطة والألف المقصورة والتشكيل. */
    fun normalize(s: String): String = buildString {
        for (ch in s) {
            when (ch) {
                'أ', 'إ', 'آ', 'ٱ' -> append('ا')
                'ة' -> append('ه')
                'ى' -> append('ي')
                'ؤ' -> append('و')
                'ئ' -> append('ي')
                in 'ً'..'ْ', 'ـ' -> {}
                else -> append(ch.lowercaseChar())
            }
        }
    }
}

private data class Hit(val docIndex: Int, val articleIndex: Int)

/* ---------- الشاشة ---------- */

/**
 * التشريع المدرسي: قائمة النصوص ← مواد النص ← قراءة كل مادة وحدها (السابقة/التالية).
 * النسخة المصورة الأصلية (PDF) متاحة من زر في الأعلى.
 */
@Composable
fun LegislationScreen(
    title: String,
    pdfAssetPath: String,
    onSharePdf: (File) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val docs by produceState<List<LegDoc>?>(null) {
        value = withContext(Dispatchers.IO) { LegislationRepository.load(context) }
    }
    var docIndex by rememberSaveable { mutableIntStateOf(-1) }
    var articleIndex by rememberSaveable { mutableIntStateOf(-1) }
    var showPdf by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var fromSearch by rememberSaveable { mutableStateOf(false) }

    if (showPdf) {
        BackHandler { showPdf = false }
        PdfViewerScreen(title = title, assetPath = pdfAssetPath, onShare = onSharePdf, onBack = { showPdf = false })
        return
    }

    BackHandler(enabled = docIndex >= 0) {
        if (articleIndex >= 0) {
            articleIndex = -1
            if (fromSearch) { docIndex = -1; fromSearch = false }
        } else docIndex = -1
    }

    // نصوص التشريع كلها بالعربية: نعرضها من اليمين إلى اليسار حتى لو كانت الواجهة بالفرنسية.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val list = docs
                if (list == null) {
                    ScreenHeader(title, onBack)
                    Row(Modifier.fillMaxWidth().padding(32.dp), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                } else when {
                    docIndex >= 0 && articleIndex >= 0 -> ArticleReader(
                        doc = list[docIndex],
                        index = articleIndex,
                        onIndex = { articleIndex = it },
                        onBack = {
                            articleIndex = -1
                            if (fromSearch) { docIndex = -1; fromSearch = false }
                        }
                    )
                    docIndex >= 0 -> ArticleList(
                        doc = list[docIndex],
                        onOpen = { articleIndex = it; fromSearch = false },
                        onBack = { docIndex = -1 }
                    )
                    else -> DocList(
                        title = title,
                        docs = list,
                        query = query,
                        onQuery = { query = it },
                        onOpenDoc = { docIndex = it },
                        onOpenHit = { d, a -> docIndex = d; articleIndex = a; fromSearch = true },
                        onPdf = { showPdf = true },
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun DocList(
    title: String,
    docs: List<LegDoc>,
    query: String,
    onQuery: (String) -> Unit,
    onOpenDoc: (Int) -> Unit,
    onOpenHit: (Int, Int) -> Unit,
    onPdf: () -> Unit,
    onBack: () -> Unit
) {
    val total = docs.sumOf { it.articles.size }
    ScreenHeader(title, onBack, listOf(stringResource(R.string.leg_summary, docs.size, total))) {
        HeaderAction(stringResource(R.string.leg_original_pdf), onPdf)
    }
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(stringResource(R.string.leg_search_hint)) }
    )
    val q = LegislationRepository.normalize(query.trim())
    if (q.length >= 2) {
        val hits = remember(q, docs) {
            buildList {
                docs.forEachIndexed { d, doc ->
                    doc.articles.forEachIndexed { a, art ->
                        val hay = LegislationRepository.normalize(art.title + " " + art.text)
                        if (hay.contains(q)) add(Hit(d, a))
                    }
                }
            }
        }
        Text(stringResource(R.string.leg_results, hits.size), style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(hits, key = { "${it.docIndex}-${it.articleIndex}" }) { h ->
                val doc = docs[h.docIndex]
                val art = doc.articles[h.articleIndex]
                ArticleRow(art.title, doc.title, snippet(art.text, query.trim())) { onOpenHit(h.docIndex, h.articleIndex) }
            }
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        var lastPart = ""
        docs.forEachIndexed { i, doc ->
            if (doc.part != lastPart) {
                lastPart = doc.part
                item(key = "part-$i") { SectionTitle(doc.part) }
            }
            item(key = "doc-$i") {
                Card(
                    Modifier.fillMaxWidth().clickable { onOpenDoc(i) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (doc.ref.isNotBlank()) Text(doc.ref, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text(stringResource(R.string.leg_articles_count, doc.articles.size),
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleList(doc: LegDoc, onOpen: (Int) -> Unit, onBack: () -> Unit) {
    ScreenHeader(doc.title, onBack, listOf(stringResource(R.string.leg_articles_count, doc.articles.size)))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (doc.ref.isNotBlank()) item(key = "ref") {
            Text(doc.ref, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }
        var lastChapter = ""
        doc.articles.forEachIndexed { i, art ->
            if (art.chapter.isNotBlank() && art.chapter != lastChapter) {
                lastChapter = art.chapter
                item(key = "ch-$i") { SectionTitle(art.chapter) }
            }
            item(key = "a-$i") {
                ArticleRow(art.title, null, art.text.lineSequence().firstOrNull().orEmpty()) { onOpen(i) }
            }
        }
    }
}

@Composable
private fun ArticleRow(title: String, docTitle: String?, preview: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            if (docTitle != null) Text(docTitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(preview, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ColumnScope.ArticleReader(doc: LegDoc, index: Int, onIndex: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val art = doc.articles[index]
    var fontSize by rememberSaveable { mutableIntStateOf(18) }
    val scroll = rememberScrollState()
    LaunchedEffect(index) { scroll.scrollTo(0) }
    val fullText = remember(art, doc) { "${doc.title}\n${art.title}\n\n${art.text}" }
    val copiedMsg = stringResource(R.string.leg_copied)
    val shareTitle = stringResource(R.string.share)

    ScreenHeader(doc.title, onBack, listOf(stringResource(R.string.leg_position, index + 1, doc.articles.size))) {
        HeaderAction("A−") { if (fontSize > 14) fontSize -= 2 }
        HeaderAction("A+") { if (fontSize < 30) fontSize += 2 }
    }
    Card(
        Modifier.fillMaxWidth().weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (art.chapter.isNotBlank()) Text(art.chapter, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary)
            Text(art.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            SelectionContainer {
                Text(art.text, fontSize = fontSize.sp, lineHeight = (fontSize * 1.7f).sp)
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { onIndex(index - 1) }, enabled = index > 0, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.leg_prev))
        }
        OutlinedButton(onClick = {
            clipboard.setText(AnnotatedString(fullText))
            Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
        }) { Text(stringResource(R.string.leg_copy)) }
        OutlinedButton(onClick = {
            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, fullText) }
            context.startActivity(Intent.createChooser(send, shareTitle))
        }) { Text(shareTitle) }
        OutlinedButton(onClick = { onIndex(index + 1) }, enabled = index < doc.articles.size - 1, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.leg_next))
        }
    }
}

/** مقتطف قصير حول أول موضع لكلمة البحث. */
private fun snippet(text: String, query: String): String {
    val nt = LegislationRepository.normalize(text)
    val nq = LegislationRepository.normalize(query)
    val pos = nt.indexOf(nq)
    // التوحيد يحذف التشكيل فقط، لذا الموضع تقريبي؛ نأخذ نافذة واسعة بما يكفي.
    if (pos < 0) return text.lineSequence().firstOrNull().orEmpty()
    val start = (pos - 40).coerceAtLeast(0)
    val end = (pos + nq.length + 80).coerceAtMost(text.length)
    return (if (start > 0) "…" else "") + text.substring(start, end).replace('\n', ' ') + (if (end < text.length) "…" else "")
}
