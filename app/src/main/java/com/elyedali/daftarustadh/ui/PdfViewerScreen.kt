package com.elyedali.daftarustadh.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elyedali.daftarustadh.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** يفتح ملف PDF مضمّنًا في التطبيق (بدون إنترنت) عبر PdfRenderer. */
private class AssetPdf(context: Context, assetPath: String) {
    val file: File = File(context.cacheDir, "pdf/" + assetPath.substringAfterLast('/')).also { f ->
        if (!f.exists()) {
            f.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input -> f.outputStream().use { input.copyTo(it) } }
        }
    }
    private val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(fd)
    private val lock = Mutex()
    val pageCount: Int get() = renderer.pageCount

    suspend fun aspect(index: Int): Float = lock.withLock {
        renderer.openPage(index).use { it.width.toFloat() / it.height }
    }

    suspend fun render(index: Int, width: Int): Bitmap = withContext(Dispatchers.IO) {
        lock.withLock {
            renderer.openPage(index).use { page ->
                val h = (width.toFloat() * page.height / page.width).toInt()
                val bmp = Bitmap.createBitmap(width, h, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(AColor.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            }
        }
    }

    fun close() { renderer.close(); fd.close() }
}

/** قارئ PDF: الصفحات تحت بعضها، وبلمسة على صفحة تُفتح مكبّرة قابلة للتقريب بإصبعين. */
@Composable
fun PdfViewerScreen(
    title: String,
    assetPath: String,
    onShare: (File) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pdf = remember(assetPath) { AssetPdf(context, assetPath) }
    DisposableEffect(pdf) { onDispose { pdf.close() } }
    var zoomPage by remember { mutableStateOf<Int?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ScreenHeader(title, onBack, listOf(stringResource(R.string.pdf_pages, pdf.pageCount))) {
                HeaderAction(stringResource(R.string.share)) { onShare(pdf.file) }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items((0 until pdf.pageCount).toList(), key = { it }) { i ->
                    PdfPage(pdf, i, 1100, Modifier.fillMaxWidth().clickable { zoomPage = i })
                }
            }
        }
    }
    zoomPage?.let { i ->
        Dialog(onDismissRequest = { zoomPage = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            Box(
                Modifier.fillMaxSize().background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale == 1f) Offset.Zero else offset + pan
                        }
                    }
                    .clickable { if (scale == 1f) zoomPage = null },
                contentAlignment = Alignment.Center
            ) {
                PdfPage(pdf, i, 2000, Modifier.fillMaxWidth().graphicsLayer {
                    scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y
                })
            }
        }
    }
}

@Composable
private fun PdfPage(pdf: AssetPdf, index: Int, width: Int, modifier: Modifier) {
    val aspect by produceState(0.707f, index) { value = pdf.aspect(index) }
    val bitmap by produceState<Bitmap?>(null, index, width) { value = pdf.render(index, width) }
    Card(modifier) {
        val b = bitmap
        if (b == null) {
            Box(Modifier.fillMaxWidth().aspectRatio(aspect), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Image(b.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().aspectRatio(aspect))
        }
    }
}
