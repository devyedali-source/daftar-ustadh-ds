package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument

/**
 * الجدول العام: جداول جميع الأقسام في جدول واحد — A4 طولية، بالرأسية الرسمية.
 * الأقسام أعمدة (من اليمين)، والأيام والحصص صفوف. إن كثرت الأقسام تُوزَّع على صفحات (8 أقسام في الصفحة).
 */
object AllClassesTimetablePdf {
    private const val W = 595f
    private const val H = 842f
    private const val M = 22f
    private const val HEADER = 82f
    private const val PER_PAGE = 8
    private val BLACK = 0xFF000000.toInt()

    data class Info(
        val title: String,
        val lineAr: String,
        val lineFr: String,
        val classNames: List<String>,
        val dayLabels: List<String>,          // تسمية كل يوم
        val periodLabels: List<List<String>>, // [يوم] تسميات حصصه (رقم + توقيت)
        val cells: List<List<List<String>>>,  // [قسم][يوم][حصة] نص الخانة
        val footer: String,
        val seal: Bitmap?
    )

    fun build(info: Info): ByteArray = PdfCommon.render { doc ->
        var no = 1
        info.classNames.indices.chunked(PER_PAGE).forEach { idx -> no = drawPage(doc, no, info, idx) }
    }

    private fun drawPage(doc: PdfDocument, pageNo: Int, info: Info, idx: List<Int>): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), pageNo).create())
        val c = page.canvas
        PdfCommon.drawOfficialHeader(c, info.seal, W)
        var y = M + HEADER
        PdfText.draw(c, info.title, M, y, W - 2 * M, 18f, 12f, bold = true, maxLines = 1)
        PdfText.draw(c, info.lineAr, M, y + 18f, W - 2 * M, 11f, 7.5f, maxLines = 1)
        PdfText.draw(c, info.lineFr, M, y + 29f, W - 2 * M, 11f, 7.5f, maxLines = 1)
        y += 44f

        val thin = Paint().apply { color = BLACK; strokeWidth = 0.4f; style = Paint.Style.STROKE }
        val thick = Paint().apply { color = BLACK; strokeWidth = 1.1f; style = Paint.Style.STROKE }
        val fill = Paint().apply { color = 0xFFEDEDED.toInt(); style = Paint.Style.FILL }
        val dayW = 42f
        val perW = 46f
        val headH = 20f
        val right = W - M
        val colW = (W - 2 * M - dayW - perW) / idx.size.coerceAtLeast(1)
        val rows = info.periodLabels.sumOf { it.size }.coerceAtLeast(1)
        val rowH = ((H - M - 16f - y - headH) / rows).coerceAtMost(30f)
        val bottom = y + headH + rows * rowH

        // رأس الأعمدة: اليوم، الحصة، ثم الأقسام
        c.drawRect(M, y, right, y + headH, fill)
        c.drawRect(right - dayW - perW, y, right, bottom, fill)
        idx.forEachIndexed { i, k ->
            val x = right - dayW - perW - (i + 1) * colW
            PdfText.draw(c, info.classNames[k], x + 1f, y, colW - 2f, headH, 9f, bold = true, maxLines = 1)
        }

        var top = y + headH
        info.dayLabels.forEachIndexed { d, dayLabel ->
            val periods = info.periodLabels.getOrNull(d).orEmpty()
            if (periods.isEmpty()) return@forEachIndexed
            val dayH = periods.size * rowH
            PdfText.draw(c, dayLabel, right - dayW + 1f, top, dayW - 2f, dayH, 7.5f, bold = true, maxLines = 2)
            periods.forEachIndexed { p, label ->
                val rTop = top + p * rowH
                PdfText.draw(c, label, right - dayW - perW + 1f, rTop, perW - 2f, rowH, 6f, maxLines = 2)
                idx.forEachIndexed { i, k ->
                    val text = info.cells.getOrNull(k)?.getOrNull(d)?.getOrNull(p).orEmpty()
                    if (text.isNotEmpty()) {
                        val x = right - dayW - perW - (i + 1) * colW
                        PdfText.draw(c, text, x + 1f, rTop, colW - 2f, rowH, 6.3f, maxLines = 2)
                    }
                }
                if (p > 0) c.drawLine(M, rTop, right - dayW, rTop, thin)
            }
            top += dayH
            c.drawLine(M, top, right, top, thick)
        }
        // خطوط الأعمدة والإطار
        c.drawLine(right - dayW, y, right - dayW, bottom, thick)
        c.drawLine(right - dayW - perW, y, right - dayW - perW, bottom, thick)
        for (i in 1 until idx.size) { val x = right - dayW - perW - i * colW; c.drawLine(x, y, x, bottom, thin) }
        c.drawLine(M, y + headH, right, y + headH, thick)
        c.drawRect(M, y, right, bottom, thick)
        PdfText.draw(c, info.footer, M, bottom + 3f, W - 2 * M, 12f, 7f, maxLines = 1)
        doc.finishPage(page)
        return pageNo + 1
    }
}
