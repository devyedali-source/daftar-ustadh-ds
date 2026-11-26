package com.elyedali.daftarustadh.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.BookPack
import com.elyedali.daftarustadh.data.BookLesson
import com.elyedali.daftarustadh.data.LessonBank
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * درس من التخطيط السنوي (تطبيق الأستاذ): تحضير قصير، وزر «محتوى الكتاب» (صفحات الدرس من الكتاب المدرسي)،
 * وزر «محتوى السبورة».
 */
@Composable
fun LessonDialog(
    lesson: BookLesson,
    subjectLabel: String,
    week: String,
    bookAsset: String,
    bookTitle: String,
    /** رمز المادة: الإنجليزية (ANG) تُعرض عناوينها بالإنجليزية، ومواد الفرنسية بالفرنسية. */
    subjectCode: String? = null,
    onDismiss: () -> Unit
) {
    var view by rememberSaveable { mutableStateOf(0) }   // 0 التحضير، 1 الكتاب، 2 السبورة
    // دروس الفرنسية والإنجليزية: كل العناوين بلغة الدرس (قد لا يفهم الأستاذ العربية)، والصفحة من اليسار إلى اليمين
    val labels = lessonLabels(lesson, subjectCode)
    val ltr = labels != null
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides
            if (ltr) androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.platform.LocalLayoutDirection.current
    ) {
    val L = labels ?: LessonLabels(
        stringResource(R.string.lesson_prep), stringResource(R.string.lesson_book), stringResource(R.string.lesson_board),
        stringResource(R.string.lesson_pages, lesson.pageFrom, lesson.pageTo), stringResource(R.string.lesson_objectives),
        stringResource(R.string.lesson_intro), stringResource(R.string.lesson_summary), stringResource(R.string.lesson_points),
        stringResource(R.string.lesson_application), stringResource(R.string.lesson_reallife),
        stringResource(R.string.lesson_series), stringResource(R.string.lesson_show_solution), stringResource(R.string.lesson_hide_solution),
        stringResource(R.string.lesson_series_book), stringResource(R.string.lesson_series_extra), stringResource(R.string.lesson_series_int))
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, securePolicy = ScreenSecurity.dialogPolicy)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // لا يختفي أسفل المحتوى (مثل زر «إظهار الإجابة») تحت أزرار الهاتف
            Column(Modifier.fillMaxSize().systemBarsPadding()) {
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { if (view != 0) view = 0 else onDismiss() }) {
                        Text(if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Ltr) "←" else "→", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(lesson.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp, maxLines = 2)
                        Text(if (ltr) week else "$subjectLabel — $week", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 1)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ViewButton("📝 " + L.prep, view == 0, Modifier.weight(1f)) { view = 0 }
                    ViewButton("📖 " + L.book, view == 1, Modifier.weight(1f)) { view = 1 }
                    ViewButton("🧑‍🏫 " + L.board, view == 2, Modifier.weight(1f)) { view = 2 }
                    // سلسلة تمارين آخر الفصل (المواد العلمية في الثانوية)
                    if (lesson.series.isNotEmpty()) ViewButton("📚 " + L.series, view == 3, Modifier.weight(1f)) { view = 3 }
                }
                Box(Modifier.weight(1f)) {
                    when (view) {
                        1 -> BookPages(bookAsset, lesson.pageFrom, lesson.pageTo, bookTitle)
                        // دروس المواد التي تُدرَّس بالفرنسية أو الإنجليزية: السبورة تُكتب من اليسار إلى اليمين
                        2 -> BoardView(lesson, bookAsset)
                        3 -> SeriesView(lesson, bookAsset, L)
                        else -> PrepView(lesson, L)
                    }
                }
            }
        }
    }
    }
}

/** عناوين صفحة الدرس. */
private data class LessonLabels(
    val prep: String, val book: String, val board: String, val pages: String,
    val objectives: String, val intro: String, val summary: String, val points: String, val application: String,
    val reallife: String = "",
    val series: String = "Série", val showSol: String = "Afficher la solution", val hideSol: String = "Masquer la solution",
    val kBook: String = "Exercice du manuel", val kExtra: String = "Exercice complémentaire", val kInt: String = "Situation d’intégration"
)

/** عناوين بلغة الدرس لدروس الفرنسية والإنجليزية، أو null لدروس العربية (تبقى بلغة التطبيق). */
private fun lessonLabels(l: BookLesson, subjectCode: String?): LessonLabels? {
    if (!isLatinLesson(l)) return null
    val pages = "${l.pageFrom} – ${l.pageTo}"
    return if (subjectCode == "ANG") LessonLabels(
        "Lesson plan", "Textbook", "Board", "Lesson pages in the textbook: $pages",
        "Objectives", "Introduction", "Summary", "Key points", "Short exercise", "Applications",
        "Exercises", "Show the answer", "Hide the answer", "Textbook exercise", "Extra exercise", "Integration task")
    else LessonLabels(
        "Préparation", "Manuel", "Tableau", "Pages de la leçon dans le manuel : $pages",
        "Objectifs", "Introduction", "Résumé", "Points essentiels", "Petite application", "Applications",
        "Série", "Afficher la solution", "Masquer la solution", "Exercice du manuel", "Exercice complémentaire", "Situation d’intégration")
}

/** الدرس مكتوب بحروف لاتينية (فرنسية أو إنجليزية): الحروف اللاتينية أكثر من العربية في عنوانه ومحتوى سبورته. */
private fun isLatinLesson(l: BookLesson): Boolean {
    val text = l.title + " " + l.board.take(3).joinToString(" ") { it.heading + " " + it.lines.take(3).joinToString(" ") }
    val arabic = text.count { it in '\u0600'..'\u06FF' }
    val latin = text.count { it in 'A'..'Z' || it in 'a'..'z' || it in '\u00C0'..'\u024F' }
    return latin > arabic
}

@Composable
private fun ViewButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, textAlign = TextAlign.Center, lineHeight = 15.sp)
    } else OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, textAlign = TextAlign.Center, lineHeight = 15.sp)
    }
}

/** سلسلة تمارين آخر الفصل: تمارين الكتاب ثم تمارين على نمطها ووضعيات إدماجية، والحل مخفي حتى يطلبه الأستاذ. */
@Composable
private fun SeriesView(l: BookLesson, bookAsset: String, L: LessonLabels) {
    val context = LocalContext.current
    val hasFig = l.series.any { it.figure != null }
    val holder = remember(bookAsset) { RendererHolder() }
    DisposableEffect(bookAsset) { onDispose { holder.close() } }
    val bookStates by BookPack.states.collectAsState()
    val bookOnPhone = bookStates[BookPack.packOf(bookAsset)] is BookPack.Status.Ready
    val ready by produceState(initialValue = false, bookAsset, hasFig, bookOnPhone) {
        if (hasFig) value = withContext(Dispatchers.IO) { LessonBank.bookFile(context, bookAsset)?.let { holder.open(it) } ?: false }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("📚 " + L.series + " — " + l.title, fontWeight = FontWeight.Black, fontSize = 16.sp) }
        items(l.series.size) { i ->
            val e = l.series[i]
            var open by rememberSaveable(l.title, i) { mutableStateOf(false) }
            val (tag, bg) = when (e.kind) {
                "int" -> L.kInt to Color(0xFFFFF3E0)
                "extra" -> L.kExtra to Color(0xFFE8F5E9)
                else -> L.kBook to Color(0xFFE3F2FD)
            }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${i + 1}. " + tag + if (e.src.isNotBlank()) " (${e.src})" else "", fontWeight = FontWeight.Black,
                        fontSize = 14.sp, color = Color(0xFF263238))
                    Text(e.q, fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF212121))
                    e.figure?.let { f ->
                        val bmp by produceState<Bitmap?>(initialValue = null, ready, f) {
                            if (ready) value = holder.renderCrop(f.page - 1, f.rect)
                        }
                        val b = bmp
                        if (b != null) Image(b.asImageBitmap(), contentDescription = f.caption,
                            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(6.dp)).padding(4.dp)
                                .aspectRatio(b.width.toFloat() / b.height), contentScale = ContentScale.FillWidth)
                        if (b == null && !ready) Text("📖 " + stringResource(R.string.bk_fig_needs_book), fontSize = 12.sp,
                            color = Color(0xFF6D4C41))
                    }
                    TextButton(onClick = { open = !open }) {
                        Text(if (open) "▲ " + L.hideSol else "▼ " + L.showSol, fontWeight = FontWeight.Bold)
                    }
                    if (open) Text("✔ " + e.a, fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF1B5E20))
                }
            }
        }
    }
}

/** التحضير القصير: الأهداف، المقدمة، الملخص، أهم النقاط، تطبيق صغير. */
@Composable
private fun PrepView(l: BookLesson, L: LessonLabels) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(L.pages, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { PrepCard("🎯 " + L.objectives, Color(0xFFE3F2FD)) { l.objectives.forEach { Text("• $it", fontSize = 14.sp) } } }
        item { PrepCard("🚪 " + L.intro, Color(0xFFE8F5E9)) { Text(l.intro, fontSize = 14.sp, lineHeight = 22.sp) } }
        item { PrepCard("📄 " + L.summary, Color(0xFFFFF8E1)) { Text(l.summary, fontSize = 14.sp, lineHeight = 22.sp) } }
        item { PrepCard("⭐ " + L.points, Color(0xFFF3E5F5)) { l.points.forEach { Text("• $it", fontSize = 14.sp, lineHeight = 21.sp) } } }
        item {
            PrepCard("✍️ " + L.application, Color(0xFFFFEBEE)) {
                Text(l.applicationQ, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
            }
        }
        if (l.applications.isNotEmpty()) item {
            PrepCard("🌍 " + L.reallife, Color(0xFFE0F2F1)) {
                l.applications.forEachIndexed { i, (q, a) ->
                    Text("${i + 1}. $q", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
                    Text("✔ $a", fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFF2E7D32))
                }
            }
        }
    }
}

@Composable
private fun PrepCard(title: String, bg: Color, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF263238))
            content()
        }
    }
}

/** محتوى السبورة: لوح أخضر بخط الطباشير، مع الجداول والرسوم المأخوذة من الكتاب ورسوم الشرح المعدّة للسبورة. */
@Composable
private fun BoardView(l: BookLesson, bookAsset: String) {
    val chalk = Color(0xFFF5F5F0)
    val yellow = Color(0xFFFFE082)
    val context = LocalContext.current
    val hasFig = l.board.any { it.figure != null }
    val holder = remember(bookAsset) { RendererHolder() }
    DisposableEffect(bookAsset) { onDispose { holder.close() } }
    // الكتاب قد يصل أثناء فتح الدرس (تنزيل من المتجر): تُعاد المحاولة عند تغيّر حالته
    val bookStates by com.elyedali.daftarustadh.data.BookPack.states.collectAsState()
    val bookOnPhone = bookStates[com.elyedali.daftarustadh.data.BookPack.packOf(bookAsset)] is com.elyedali.daftarustadh.data.BookPack.Status.Ready
    val ready by produceState(initialValue = false, bookAsset, hasFig, bookOnPhone) {
        if (hasFig) value = withContext(Dispatchers.IO) { LessonBank.bookFile(context, bookAsset)?.let { holder.open(it) } ?: false }
    }
    LazyColumn(Modifier.fillMaxSize().padding(8.dp).background(Color(0xFF6D4C41), RoundedCornerShape(12.dp)).padding(8.dp)
        .background(Color(0xFF1F4A36), RoundedCornerShape(6.dp)),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(l.title, color = chalk, fontWeight = FontWeight.Black, fontSize = 21.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
        items(l.board) { s ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (s.heading.isNotBlank()) Text(s.heading, color = yellow, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                s.lines.forEach { line ->
                    Text(if (s.lines.size > 1) "– $line" else line, color = chalk, fontSize = 15.sp, lineHeight = 23.sp)
                }
                if (s.table.isNotEmpty()) ChalkTable(s.table, chalk, yellow)
                s.figure?.let { f ->
                    val bmp by produceState<Bitmap?>(initialValue = null, ready, f) {
                        if (ready) value = holder.renderCrop(f.page - 1, f.rect)
                    }
                    val b = bmp
                    if (b != null) Image(b.asImageBitmap(), contentDescription = f.caption,
                        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(6.dp)).padding(4.dp)
                            .aspectRatio(b.width.toFloat() / b.height), contentScale = ContentScale.FillWidth)
                    if (b == null && !ready) Text("📖 " + stringResource(R.string.bk_fig_needs_book), color = yellow, fontSize = 13.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    if (f.caption.isNotBlank()) Text(f.caption, color = chalk.copy(alpha = 0.85f), fontSize = 13.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                s.drawing?.let { d ->
                    val bmp by produceState<Bitmap?>(initialValue = null, d.asset) {
                        value = withContext(Dispatchers.IO) {
                            try { context.assets.open(d.asset).use { android.graphics.BitmapFactory.decodeStream(it) } } catch (e: Exception) { null }
                        }
                    }
                    val b = bmp
                    if (b != null) Image(b.asImageBitmap(), contentDescription = d.caption,
                        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(6.dp)).padding(4.dp)
                            .aspectRatio(b.width.toFloat() / b.height), contentScale = ContentScale.FillWidth)
                    if (d.caption.isNotBlank()) Text("✏️ " + d.caption, color = chalk.copy(alpha = 0.85f), fontSize = 13.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** جدول بخط الطباشير: الصف الأول عناوين الأعمدة. */
@Composable
private fun ChalkTable(rows: List<List<String>>, chalk: Color, head: Color) {
    val cols = rows.maxOf { it.size }
    Column(Modifier.fillMaxWidth().border(1.dp, chalk.copy(alpha = 0.7f))) {
        rows.forEachIndexed { i, r ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                (0 until cols).forEach { c ->
                    Box(Modifier.weight(1f).fillMaxHeight().border(0.5.dp, chalk.copy(alpha = 0.5f)).padding(5.dp),
                        contentAlignment = Alignment.Center) {
                        Text(r.getOrNull(c).orEmpty(), color = if (i == 0) head else chalk,
                            fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

/** صفحات الدرس من الكتاب المدرسي (PDF داخل التطبيق)، صفحة تحت صفحة. */
@Composable
private fun BookPages(asset: String, from: Int, to: Int, bookTitle: String) {
    val context = LocalContext.current
    val holder = remember(asset) { RendererHolder() }
    DisposableEffect(asset) { onDispose { holder.close() } }
    val states by BookPack.states.collectAsState()
    val st = states[BookPack.packOf(asset)]
    val ready by produceState<Boolean?>(initialValue = null, asset, st is BookPack.Status.Ready) {
        value = withContext(Dispatchers.IO) {
            val f = LessonBank.bookFile(context, asset) ?: return@withContext false
            holder.open(f)
        }
    }
    val confirmLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()) {}
    when (ready) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        // الكتاب ليس على الهاتف: تنزيله وحده (بلا إعادة توجيه)، أو حالته إن كان يُنزَّل
        false -> Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                BookPack.bundledIn(context) || st is BookPack.Status.Ready ->
                    Text(stringResource(R.string.lesson_book_missing), color = MaterialTheme.colorScheme.error)
                st is BookPack.Status.NotInTestBuild -> Text(stringResource(R.string.books_test_build))
                else -> {
                    val pack = BookPack.packFor(context, asset)
                    Text("📖 " + stringResource(R.string.bk_lesson_missing) + (pack?.let { "  (" + megaBytes(it.bytes) + ")" } ?: ""),
                        fontWeight = FontWeight.Bold)
                    BookStatusLine(st ?: BookPack.Status.NotDownloaded,
                        onDownload = { BookPack.download(context, BookPack.packOf(asset)) },
                        onConfirm = { BookPack.confirm(confirmLauncher) })
                }
            }
        }
        else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("📖 $bookTitle", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items((from..to).toList(), key = { it }) { p ->
                val bmp by produceState<Bitmap?>(initialValue = null, p) { value = holder.render(p - 1) }
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    val b = bmp
                    if (b == null) Box(Modifier.fillMaxWidth().aspectRatio(0.707f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    else Image(b.asImageBitmap(), contentDescription = "$p", modifier = Modifier.fillMaxWidth()
                        .aspectRatio(b.width.toFloat() / b.height), contentScale = ContentScale.FillWidth)
                }
            }
        }
    }
}

/** فتح الكتاب ورسم صفحاته واحدة واحدة (PdfRenderer لا يقبل رسم صفحتين في نفس الوقت). */
private class RendererHolder {
    private var fd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    private val lock = Mutex()

    fun open(f: java.io.File): Boolean = try {
        fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(fd!!)
        true
    } catch (e: Exception) { false }

    suspend fun render(index: Int): Bitmap? = renderAt(index, 1100)

    private suspend fun renderAt(index: Int, width: Int): Bitmap? = lock.withLock {
        withContext(Dispatchers.IO) {
            val r = renderer ?: return@withContext null
            if (index !in 0 until r.pageCount) return@withContext null
            try { r.openPage(index).use { page ->
                val w = width
                val h = (w.toFloat() * page.height / page.width).toInt()
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            } } catch (e: Exception) { null }
        }
    }

    /** جزء من صفحة (رسم أو شكل) بدقة عالية: rect = (يسار، أعلى، يمين، أسفل) كنسب. */
    suspend fun renderCrop(index: Int, rect: List<Float>): Bitmap? {
        if (rect.size != 4) return null
        val full = renderAt(index, 1800) ?: return null
        val x0 = (rect[0] * full.width).toInt().coerceIn(0, full.width - 1)
        val y0 = (rect[1] * full.height).toInt().coerceIn(0, full.height - 1)
        val x1 = (rect[2] * full.width).toInt().coerceIn(x0 + 1, full.width)
        val y1 = (rect[3] * full.height).toInt().coerceIn(y0 + 1, full.height)
        return Bitmap.createBitmap(full, x0, y0, x1 - x0, y1 - y0).also { if (it !== full) full.recycle() }
    }

    fun close() {
        try { renderer?.close() } catch (_: Exception) {}
        try { fd?.close() } catch (_: Exception) {}
        renderer = null; fd = null
    }
}
