package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.elyedali.daftarustadh.pdf.PdfCommon.H
import com.elyedali.daftarustadh.pdf.PdfCommon.M
import com.elyedali.daftarustadh.pdf.PdfCommon.W
import java.io.ByteArrayOutputStream

/**
 * وثيقة الأستاذ (اختبار، طلب، درس...): بلا رأسية رسمية ولا شعار؛
 * في الأعلى معلومات المؤسسة والسنة الدراسية والقسم (والمادة في الاختبار)، ثم العنوان والنص على عدة صفحات إن طال،
 * و«توقيع الأستاذ» وطابعه في أسفل الصفحة الأخيرة.
 */
object DocumentPdf {
    private val INK = 0xFF1F3A93.toInt()

    data class Info(
        val infoLineAr: String,
        val infoLineFr: String,
        val subjectLine: String?,      // «المادة: ...» للاختبار، وإلا null
        val title: String,
        val body: String,
        val stampLines: List<String>,
        val seal: Bitmap? = null       // لم يعد مستعملًا: الوثيقة بلا شعار
    )

    fun build(i: Info): ByteArray {
        val width = (W - 2 * M).toInt()
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply { textSize = 11.5f; typeface = Typeface.DEFAULT }
        val body = StaticLayout.Builder.obtain(i.body, 0, i.body.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)   // يتبع اتجاه النص: العربي يمينًا والفرنسي يسارًا
            .setLineSpacing(3f, 1f)
            .setIncludePad(false)
            .build()
        val doc = PdfDocument()
        val headH = 16f + 16f + (if (i.subjectLine != null) 18f else 0f) + 8f
        val firstTop = M + headH + 30f
        val otherTop = M + 10f
        val pageBottom = H - M - 20f
        // الطابع في أسفل الصفحة الأخيرة: لا يتجاوز النص هذا الحد في الصفحة الأخيرة
        val stampH = 22f + 16f * i.stampLines.size + 14f
        val stampTop = pageBottom - stampH
        var line = 0
        var pageNo = 0
        while (true) {
            pageNo++
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), pageNo).create())
            val c = page.canvas
            val top = if (pageNo == 1) firstTop else otherTop
            if (pageNo == 1) {
                var y = M
                PdfText.draw(c, i.infoLineAr, M, y, W - 2 * M, 16f, 9.5f, maxLines = 1); y += 16f
                PdfText.draw(c, i.infoLineFr, M, y, W - 2 * M, 16f, 9f, maxLines = 1); y += 16f
                if (i.subjectLine != null) { PdfText.draw(c, i.subjectLine, M, y, W - 2 * M, 18f, 10.5f, bold = true, maxLines = 1); y += 18f }
                c.drawLine(M, y + 4f, W - M, y + 4f, Paint().apply { color = 0xFF000000.toInt(); strokeWidth = 0.6f })
                PdfText.draw(c, i.title, M, y + 8f, W - 2 * M, 24f, 14f, bold = true, maxLines = 2)
            }
            val startLine = line
            val startY = if (startLine < body.lineCount) body.getLineTop(startLine) else 0
            while (line < body.lineCount && top + (body.getLineBottom(line) - startY) <= pageBottom) line++
            if (line == startLine && line < body.lineCount) line++   // سطر أطول من الصفحة: يُرسم ولو قُصّ
            // إن انتهى النص هنا لكن نزل إلى مكان الطابع: نعيد الأسطر الأخيرة إلى صفحة جديدة
            if (line >= body.lineCount && line > startLine) {
                while (line > startLine + 1 && top + (body.getLineBottom(line - 1) - startY) > stampTop) line--
            }
            if (line > startLine) {
                c.save()
                c.clipRect(M, top, W - M, pageBottom)
                c.translate(M, top - startY)
                StaticLayout.Builder.obtain(i.body, body.getLineStart(startLine), body.getLineEnd(line - 1), paint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(3f, 1f).setIncludePad(false).build()
                    .let { part -> c.translate(0f, startY.toFloat()); part.draw(c) }
                c.restore()
            }
            val usedBottom = top + (if (line > startLine) body.getLineBottom(line - 1) - startY else 0).toFloat()
            val isLast = line >= body.lineCount && usedBottom <= stampTop
            if (isLast) drawStamp(c, stampTop, i)
            PdfText.draw(c, "$pageNo", M, H - M + 4f, W - 2 * M, 14f, 8f)
            doc.finishPage(page)
            if (isLast) break
        }
        val out = ByteArrayOutputStream()
        doc.writeTo(out); doc.close()
        return out.toByteArray()
    }

    private fun drawStamp(c: android.graphics.Canvas, top: Float, i: Info) {
        val boxW = 230f
        val left = M + 10f
        PdfText.draw(c, "توقيع الأستاذ — Signature du professeur", left, top, boxW, 16f, 10f, bold = true, maxLines = 1)
        val stampTop = top + 22f
        val stampH = 16f * i.stampLines.size + 14f
        val ink = Paint().apply { color = INK; style = Paint.Style.STROKE; strokeWidth = 1.6f; isAntiAlias = true }
        c.drawRoundRect(RectF(left, stampTop, left + boxW, stampTop + stampH), 10f, 10f, ink)
        c.drawRoundRect(RectF(left + 3f, stampTop + 3f, left + boxW - 3f, stampTop + stampH - 3f), 8f, 8f, ink.apply { strokeWidth = 0.7f })
        i.stampLines.forEachIndexed { k, t ->
            PdfText.draw(c, t, left + 6f, stampTop + 7f + k * 16f, boxW - 12f, 16f, if (k == 0) 11f else 9f, bold = k == 0, color = INK, maxLines = 1)
        }
    }
}
