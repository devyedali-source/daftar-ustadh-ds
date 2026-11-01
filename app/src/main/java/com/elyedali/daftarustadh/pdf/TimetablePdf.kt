package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

/**
 * جدول زمني (قسم أو أستاذ) — A4 عرضية، ثنائي اللغة، بالرأسية الرسمية.
 * الأيام صفوف (من الأعلى)، والحصص أعمدة من اليمين إلى اليسار.
 */
object TimetablePdf {
    private const val W = 842f
    private const val H = 595f
    private const val M = 28f
    private const val HEADER = 82f
    private val BLACK = 0xFF000000.toInt()

    data class Info(
        val title: String,
        val lineAr: String,
        val lineFr: String,
        val dayLabels: List<String>,       // تسمية كل صف (يوم)
        val periodLabels: List<String>,    // تسمية كل عمود (حصة + توقيت)
        val cells: List<List<String>>,     // [يوم][حصة] نص الخانة (فارغ = لا حصة)
        val footer: String,
        val seal: Bitmap?
    )

    fun build(info: Info): ByteArray = PdfCommon.render { drawInto(it, 1, info) }

    /** يرسم الجدول في صفحة جديدة من مستند قائم، ويرجع رقم الصفحة التالية. */
    fun drawInto(doc: PdfDocument, pageNo: Int, info: Info): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), pageNo).create())
        val c = page.canvas
        PdfCommon.drawOfficialHeader(c, info.seal, W)
        var y = M + HEADER
        PdfText.draw(c, info.title, M, y, W - 2 * M, 20f, 13f, bold = true, maxLines = 1)
        PdfText.draw(c, info.lineAr, M, y + 20f, W - 2 * M, 12f, 8.5f, maxLines = 1)
        PdfText.draw(c, info.lineFr, M, y + 32f, W - 2 * M, 12f, 8.5f, maxLines = 1)
        y += 50f

        val line = Paint().apply { color = BLACK; strokeWidth = 0.7f; style = Paint.Style.STROKE }
        val fill = Paint().apply { color = 0xFFEDEDED.toInt(); style = Paint.Style.FILL }
        val dayW = 70f
        val headH = 30f
        val cols = info.periodLabels.size.coerceAtLeast(1)
        val colW = (W - 2 * M - dayW) / cols
        val rows = info.dayLabels.size.coerceAtLeast(1)
        val rowH = ((H - M - 20f - y - headH) / rows).coerceAtMost(62f)
        val right = W - M
        val bottom = y + headH + rows * rowH

        c.drawRect(M, y, right, y + headH, fill)
        c.drawRect(right - dayW, y, right, bottom, fill)
        info.periodLabels.forEachIndexed { i, label ->
            val x = right - dayW - (i + 1) * colW
            PdfText.draw(c, label, x + 1f, y, colW - 2f, headH, 7.5f, bold = true)
        }
        info.dayLabels.forEachIndexed { d, label ->
            val top = y + headH + d * rowH
            PdfText.draw(c, label, right - dayW + 2f, top, dayW - 4f, rowH, 9f, bold = true)
            info.periodLabels.indices.forEach { p ->
                val text = info.cells.getOrNull(d)?.getOrNull(p).orEmpty()
                if (text.isNotEmpty()) {
                    val x = right - dayW - (p + 1) * colW
                    PdfText.draw(c, text, x + 2f, top, colW - 4f, rowH, 8f, maxLines = 4)
                }
            }
        }
        c.drawRect(M, y, right, bottom, line)
        c.drawLine(M, y + headH, right, y + headH, line)
        for (d in 1 until rows) c.drawLine(M, y + headH + d * rowH, right, y + headH + d * rowH, line)
        c.drawLine(right - dayW, y, right - dayW, bottom, line)
        for (p in 1 until cols) { val x = right - dayW - p * colW; c.drawLine(x, y, x, bottom, line) }
        PdfText.draw(c, info.footer, M, bottom + 4f, W - 2 * M, 14f, 8f, maxLines = 1)
        doc.finishPage(page)
        return pageNo + 1
    }
}
