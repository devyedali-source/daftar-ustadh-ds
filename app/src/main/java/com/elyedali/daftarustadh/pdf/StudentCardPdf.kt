package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout

/**
 * بطاقات الطلاب للطباعة: عشر بطاقات في كل صفحة A4 (عمودان × خمسة صفوف)، تُقصّ بعد الطباعة.
 * في كل بطاقة: المؤسسة والسنة، الصورة، الاسم بالعربية والفرنسية، الرقم المدرسي، القسم ورقم النداء.
 */
object StudentCardPdf {
    private const val W = 595f
    private const val H = 842f
    private const val M = 28f
    private const val GAP = 12f
    private const val CW = (W - 2 * M - GAP) / 2f
    private const val CH = 146f
    private const val ROWS = 5
    private val INK = 0xFF2E1A6E.toInt()

    data class Card(
        val nameAr: String, val nameFr: String, val rim: String,
        val className: String, val roll: Int,
        val photo: () -> Bitmap?   // تُحمَّل عند رسم صفحتها فقط، ثم تُحرَّر (مئات الصور لا تتسع في الذاكرة معًا)
    )

    data class Info(val schoolName: String, val schoolYear: String, val seal: Bitmap?)

    fun build(cards: List<Card>, info: Info): ByteArray = PdfCommon.render { drawInto(it, 1, cards, info) }

    /** يضيف البطاقات إلى مستند قائم، ويرجع رقم الصفحة التالية. */
    fun drawInto(doc: PdfDocument, startNo: Int, cards: List<Card>, info: Info): Int {
        val perPage = ROWS * 2
        val pages = cards.chunked(perPage).ifEmpty { listOf(emptyList()) }
        pages.forEachIndexed { pi, list ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), startNo + pi).create())
            val loaded = ArrayList<Bitmap>()
            list.forEachIndexed { i, card ->
                val row = i / 2
                val col = i % 2
                // الترتيب من اليمين: البطاقة الأولى في العمود الأيمن
                val left = if (col == 0) W - M - CW else M
                val top = M + row * (CH + GAP)
                val photo = card.photo()
                if (photo != null) loaded += photo
                drawCard(page.canvas, left, top, card, photo, info)
            }
            doc.finishPage(page)
            loaded.forEach { it.recycle() }
        }
        return startNo + pages.size
    }

    private fun drawCard(c: Canvas, left: Float, top: Float, card: Card, photo: Bitmap?, info: Info) {
        val box = RectF(left, top, left + CW, top + CH)
        // خط القص المتقطع حول البطاقة
        c.drawRoundRect(box, 10f, 10f, Paint().apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL })
        c.drawRoundRect(box, 10f, 10f, Paint().apply { color = INK; style = Paint.Style.STROKE; strokeWidth = 1.2f; isAntiAlias = true })
        // الشريط العلوي: المؤسسة والسنة
        val band = RectF(left, top, left + CW, top + 30f)
        c.save(); c.clipRect(band)
        c.drawRoundRect(RectF(left, top, left + CW, top + 40f), 10f, 10f, Paint().apply { color = 0xFFEDE7F6.toInt(); style = Paint.Style.FILL })
        c.restore()
        info.seal?.let { c.drawBitmap(it, null, RectF(left + 6f, top + 3f, left + 30f, top + 27f), Paint(Paint.FILTER_BITMAP_FLAG)) }
        PdfText.draw(c, info.schoolName, left + 32f, top + 1f, CW - 38f, 16f, 8.5f, bold = true, color = INK, maxLines = 1)
        PdfText.draw(c, "بطاقة طالب — Carte d'élève — ⁦${info.schoolYear}⁩", left + 32f, top + 15f, CW - 38f, 14f, 7f, color = INK, maxLines = 1)
        // الصورة يمينًا
        val ph = RectF(left + CW - 8f - 64f, top + 38f, left + CW - 8f, top + 38f + 80f)
        if (photo != null) {
            // قصّ الصورة لتملأ الإطار دون تشويه
            val r = ph.width() / ph.height()
            val src = if (photo.width.toFloat() / photo.height > r) {
                val w = (photo.height * r).toInt(); Rect((photo.width - w) / 2, 0, (photo.width + w) / 2, photo.height)
            } else {
                val h = (photo.width / r).toInt(); Rect(0, (photo.height - h) / 2, photo.width, (photo.height + h) / 2)
            }
            c.drawBitmap(photo, src, ph, Paint(Paint.FILTER_BITMAP_FLAG))
        } else {
            c.drawRect(ph, Paint().apply { color = 0xFFF3F3F3.toInt(); style = Paint.Style.FILL })
            PdfText.draw(c, "صورة\nPhoto", ph.left, ph.top, ph.width(), ph.height(), 7.5f, color = 0xFF9E9E9E.toInt())
        }
        c.drawRect(ph, Paint().apply { color = INK; style = Paint.Style.STROKE; strokeWidth = 0.8f })
        // المعلومات يسار الصورة (بمحاذاة اليمين)
        val tx = left + 8f
        val tw = ph.left - 6f - tx
        val right = Layout.Alignment.ALIGN_NORMAL
        var y = top + 36f
        PdfText.draw(c, card.nameAr, tx, y, tw, 18f, 10.5f, bold = true, align = right, maxLines = 1); y += 18f
        if (card.nameFr.isNotBlank()) { PdfText.draw(c, card.nameFr, tx, y, tw, 15f, 8.5f, align = Layout.Alignment.ALIGN_CENTER, maxLines = 1); y += 16f }
        PdfText.draw(c, "الرقم المدرسي: ⁦${card.rim.ifBlank { "—" }}⁩", tx, y, tw, 15f, 8.5f, align = right, maxLines = 1); y += 16f
        PdfText.draw(c, "القسم: ⁦${card.className}⁩", tx, y, tw, 15f, 8.5f, align = right, maxLines = 1); y += 16f
        PdfText.draw(c, "رقم النداء: ⁦${card.roll}⁩", tx, y, tw, 15f, 9f, bold = true, align = right, maxLines = 1)
        PdfText.draw(c, "Matricule : ${card.rim.ifBlank { "—" }} — Classe : ${card.className} — N° ${card.roll}", left + 6f, top + CH - 18f,
            CW - 12f, 14f, 6.5f, color = 0xFF616161.toInt(), maxLines = 1)
    }
}
