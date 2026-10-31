package com.elyedali.daftarustadh.pdf

import android.graphics.Canvas
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils

/** كتابة نص (عربي أو فرنسي) داخل مستطيل في صفحة PDF، مع التفاف الأسطر وتشكيل الحروف العربية. */
object PdfText {
    fun draw(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        size: Float,
        align: Layout.Alignment = Layout.Alignment.ALIGN_CENTER,
        bold: Boolean = false,
        color: Int = 0xFF000000.toInt(),
        maxLines: Int = 2
    ) {
        if (text.isEmpty() || width <= 0f) return
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
            .setAlignment(align)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setIncludePad(false)
            .build()
        canvas.save()
        val dy = ((height - layout.height) / 2f).coerceAtLeast(0f)
        canvas.translate(left, top + dy)
        layout.draw(canvas)
        canvas.restore()
    }
}
