package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

/**
 * دفتر النصوص فارغًا للسحب والملء باليد (A4 عرضية)، ثنائي اللغة.
 * الأعمدة من اليمين: التاريخ، التوقيت، المادة، المجال، الموضوع، المدة، التوقيع.
 */
object TextbookPdf {
    private const val W = 842f
    private const val H = 595f
    private const val M = 28f
    private const val HEADER = 82f
    private const val TITLE = 44f
    private const val TABLE_HEAD = 30f
    private const val ROW = 26f
    private val BLACK = 0xFF000000.toInt()

    data class Info(
        val title: String,          // «دفتر النصوص — Cahier de textes»
        val lineAr: String,         // المؤسسة، الولاية، القسم، السنة
        val lineFr: String,
        val shortLine: String,      // سطر مختصر أعلى الصفحات التالية
        val headers: List<String>,  // 7 عناوين أعمدة (عربي\nفرنسي)
        val page: (Int, Int) -> String,
        val seal: Bitmap?
    )

    // نسب عرض الأعمدة بنفس ترتيب العناوين (من اليمين)
    private val WEIGHTS = listOf(1.0f, 1.1f, 1.2f, 1.6f, 3.6f, 0.8f, 1.3f)

    fun build(pages: Int, info: Info): ByteArray {
        val doc = PdfDocument()
        val count = pages.coerceIn(1, 200)
        for (p in 1..count) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), p).create())
            val c = page.canvas
            var y = M
            if (p == 1) {
                PdfCommon.drawOfficialHeader(c, info.seal, W)
                y += HEADER
                PdfText.draw(c, info.title, M, y, W - 2 * M, 20f, 13f, bold = true, maxLines = 1)
                PdfText.draw(c, info.lineAr, M, y + 20f, W - 2 * M, 12f, 8.5f, maxLines = 1)
                PdfText.draw(c, info.lineFr, M, y + 32f, W - 2 * M, 12f, 8.5f, maxLines = 1)
                y += TITLE + 6f
            } else {
                PdfText.draw(c, info.shortLine, M, y, W - 2 * M, 16f, 9f, bold = true, maxLines = 1)
                y += 22f
            }
            val rows = ((H - M - 16f - y - TABLE_HEAD) / ROW).toInt()
            drawTable(c, y, rows, info.headers)
            PdfText.draw(c, info.page(p, count), M, H - M + 2f, W - 2 * M, 14f, 8f)
            doc.finishPage(page)
        }
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    private fun drawTable(c: Canvas, top: Float, rows: Int, headers: List<String>) {
        val line = Paint().apply { color = BLACK; strokeWidth = 0.7f; style = Paint.Style.STROKE }
        val fill = Paint().apply { color = 0xFFEDEDED.toInt(); style = Paint.Style.FILL }
        val right = W - M
        val total = W - 2 * M
        val sum = WEIGHTS.sum()
        // حدود الأعمدة من اليمين إلى اليسار
        val edges = ArrayList<Float>()
        var x = right
        edges.add(x)
        WEIGHTS.forEach { w -> x -= total * w / sum; edges.add(x) }
        edges[edges.lastIndex] = M
        c.drawRect(M, top, right, top + TABLE_HEAD, fill)
        headers.forEachIndexed { i, h ->
            PdfText.draw(c, h, edges[i + 1] + 1f, top, edges[i] - edges[i + 1] - 2f, TABLE_HEAD, 8f, bold = true)
        }
        val bottom = top + TABLE_HEAD + rows * ROW
        c.drawRect(M, top, right, bottom, line)
        edges.drop(1).dropLast(1).forEach { ex -> c.drawLine(ex, top, ex, bottom, line) }
        var ry = top + TABLE_HEAD
        c.drawLine(M, ry, right, ry, line)
        repeat(rows) { ry += ROW; c.drawLine(M, ry, right, ry, line) }
    }
}
