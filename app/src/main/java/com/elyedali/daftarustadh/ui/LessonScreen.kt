package com.elyedali.daftarustadh.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    onDismiss: () -> Unit
) {
    var view by rememberSaveable { mutableStateOf(0) }   // 0 التحضير، 1 الكتاب، 2 السبورة
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { if (view != 0) view = 0 else onDismiss() }) {
                        Text("→", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(lesson.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp, maxLines = 2)
                        Text("$subjectLabel — $week", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 1)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ViewButton("📝 " + stringResource(R.string.lesson_prep), view == 0, Modifier.weight(1f)) { view = 0 }
                    ViewButton("📖 " + stringResource(R.string.lesson_book), view == 1, Modifier.weight(1f)) { view = 1 }
                    ViewButton("🧑‍🏫 " + stringResource(R.string.lesson_board), view == 2, Modifier.weight(1f)) { view = 2 }
                }
                Box(Modifier.weight(1f)) {
                    when (view) {
                        1 -> BookPages(bookAsset, lesson.pageFrom, lesson.pageTo, bookTitle)
                        2 -> BoardView(lesson)
                        else -> PrepView(lesson)
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    } else OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** التحضير القصير: الأهداف، المقدمة، الملخص، أهم النقاط، تطبيق صغير. */
@Composable
private fun PrepView(l: BookLesson) {
    var showAnswer by rememberSaveable(l.title) { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(stringResource(R.string.lesson_pages, l.pageFrom, l.pageTo), fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { PrepCard("🎯 " + stringResource(R.string.lesson_objectives), Color(0xFFE3F2FD)) { l.objectives.forEach { Text("• $it", fontSize = 14.sp) } } }
        item { PrepCard("🚪 " + stringResource(R.string.lesson_intro), Color(0xFFE8F5E9)) { Text(l.intro, fontSize = 14.sp, lineHeight = 22.sp) } }
        item { PrepCard("📄 " + stringResource(R.string.lesson_summary), Color(0xFFFFF8E1)) { Text(l.summary, fontSize = 14.sp, lineHeight = 22.sp) } }
        item { PrepCard("⭐ " + stringResource(R.string.lesson_points), Color(0xFFF3E5F5)) { l.points.forEach { Text("• $it", fontSize = 14.sp, lineHeight = 21.sp) } } }
        item {
            PrepCard("✍️ " + stringResource(R.string.lesson_application), Color(0xFFFFEBEE)) {
                Text(l.applicationQ, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
                if (l.applicationA.isNotBlank()) {
                    TextButton(onClick = { showAnswer = !showAnswer }) {
                        Text(stringResource(if (showAnswer) R.string.lesson_hide_answer else R.string.lesson_show_answer))
                    }
                    if (showAnswer) Text(l.applicationA, fontSize = 14.sp, color = Color(0xFF2E7D32), lineHeight = 22.sp)
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

/** محتوى السبورة: لوح أخضر بخط الطباشير. */
@Composable
private fun BoardView(l: BookLesson) {
    val chalk = Color(0xFFF5F5F0)
    val yellow = Color(0xFFFFE082)
    LazyColumn(Modifier.fillMaxSize().padding(8.dp).background(Color(0xFF6D4C41), RoundedCornerShape(12.dp)).padding(8.dp)
        .background(Color(0xFF1F4A36), RoundedCornerShape(6.dp)),
        contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(l.title, color = chalk, fontWeight = FontWeight.Black, fontSize = 21.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
        items(l.board) { s ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (s.heading.isNotBlank()) Text(s.heading, color = yellow, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                s.lines.forEach { line ->
                    Text(if (s.lines.size > 1) "– $line" else line, color = chalk, fontSize = 15.sp, lineHeight = 23.sp)
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
    val ready by produceState<Boolean?>(initialValue = null, asset) {
        value = withContext(Dispatchers.IO) {
            val f = LessonBank.bookFile(context, asset) ?: return@withContext false
            holder.open(f)
        }
    }
    when (ready) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        false -> Text(stringResource(R.string.lesson_book_missing), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
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

    suspend fun render(index: Int): Bitmap? = lock.withLock {
        withContext(Dispatchers.IO) {
            val r = renderer ?: return@withContext null
            if (index !in 0 until r.pageCount) return@withContext null
            try { r.openPage(index).use { page ->
                val w = 1100
                val h = (w.toFloat() * page.height / page.width).toInt()
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            } } catch (e: Exception) { null }
        }
    }

    fun close() {
        try { renderer?.close() } catch (_: Exception) {}
        try { fd?.close() } catch (_: Exception) {}
        renderer = null; fd = null
    }
}
