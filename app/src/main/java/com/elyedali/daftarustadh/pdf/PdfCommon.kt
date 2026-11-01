package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import java.math.BigDecimal
import java.math.RoundingMode

object PdfCommon {
    /** ينشئ مستند PDF ويكتب فيه ما يرسمه block، ثم يرجع بايتات الملف. */
    fun render(block: (PdfDocument) -> Unit): ByteArray {
        val doc = PdfDocument()
        try {
            block(doc)
            val out = java.io.ByteArrayOutputStream()
            doc.writeTo(out)
            return out.toByteArray()
        } finally {
            doc.close()
        }
    }

    /** يكتب المستند مباشرة في ملف (للمستندات الكبيرة جدًا: كل سجلات المؤسسة). */
    fun renderToFile(file: java.io.File, block: (PdfDocument) -> Unit) {
        val doc = PdfDocument()
        try {
            block(doc)
            file.outputStream().use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
    }

    const val W = 595f
    const val H = 842f
    const val M = 28f
    const val HEADER = 82f

    /** الرأسية الرسمية: العربية يمينًا، الفرنسية يسارًا، والشعار في الوسط. */
    fun drawOfficialHeader(c: Canvas, seal: Bitmap?, pageW: Float = W) {
        val sealSize = 72f
        val col = (pageW - 2 * M - sealSize - 16f) / 2f
        val lineH = 15f
        OfficialHeader.AR.forEachIndexed { i, t ->
            PdfText.draw(c, t, pageW - M - col, M + i * lineH, col, lineH, 9.5f, Layout.Alignment.ALIGN_CENTER, bold = i == 0, maxLines = 1)
        }
        OfficialHeader.FR.forEachIndexed { i, t ->
            PdfText.draw(c, t, M, M + i * lineH, col, lineH, 9f, Layout.Alignment.ALIGN_CENTER, bold = i == 0, maxLines = 1)
        }
        seal?.let { bmp ->
            val left = (pageW - sealSize) / 2f
            c.drawBitmap(bmp, null, RectF(left, M, left + sealSize, M + sealSize), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
    }

    /** رقم بفاصلة عشرية كما في الكشوف الرسمية: 14,11 — وفارغ إن لم يوجد. */
    fun num(v: Double?): String =
        if (v == null) "" else BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')
}
