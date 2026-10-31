package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import java.math.BigDecimal
import java.math.RoundingMode

object PdfCommon {
    const val W = 595f
    const val H = 842f
    const val M = 28f
    const val HEADER = 82f

    /** الرأسية الرسمية: العربية يمينًا، الفرنسية يسارًا، والشعار في الوسط. */
    fun drawOfficialHeader(c: Canvas, seal: Bitmap?) {
        val sealSize = 72f
        val col = (W - 2 * M - sealSize - 16f) / 2f
        val lineH = 15f
        OfficialHeader.AR.forEachIndexed { i, t ->
            PdfText.draw(c, t, W - M - col, M + i * lineH, col, lineH, 9.5f, Layout.Alignment.ALIGN_CENTER, bold = i == 0, maxLines = 1)
        }
        OfficialHeader.FR.forEachIndexed { i, t ->
            PdfText.draw(c, t, M, M + i * lineH, col, lineH, 9f, Layout.Alignment.ALIGN_CENTER, bold = i == 0, maxLines = 1)
        }
        seal?.let { bmp ->
            val left = (W - sealSize) / 2f
            c.drawBitmap(bmp, null, RectF(left, M, left + sealSize, M + sealSize), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
    }

    /** رقم بفاصلة عشرية كما في الكشوف الرسمية: 14,11 — وفارغ إن لم يوجد. */
    fun num(v: Double?): String =
        if (v == null) "" else BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')
}
