package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.CurriculumSubject
import com.elyedali.daftarustadh.data.InstitutionEntity
import com.elyedali.daftarustadh.data.StudentEntity
import java.io.ByteArrayOutputStream

/**
 * سجل الأستاذ للسحب والملء اليدوي (A4 طولية):
 * رأسية رسمية + اسم المادة والقسم، ثم جدول الطلاب بخانات اختبار وامتحان الفصول الثلاثة فارغة،
 * وفي آخر صفحة «توقيع الأستاذ» وتحته طابع باسمه.
 */
object TeacherRecordPdf {
    private const val W = 595f
    private const val H = 842f
    private const val M = 28f
    private const val ROW = 22f
    private const val HEADER = 82f
    private const val TITLE = 62f
    private const val TABLE_HEAD = 32f
    private const val FOOTER = 110f
    private val BLACK = 0xFF000000.toInt()
    private val INK = 0xFF1F3A93.toInt()   // لون الطابع: حبر أزرق

    data class Labels(
        val headerAr: List<String> = OfficialHeader.AR,   // أسطر الرأسية بالعربية (يمين)
        val headerFr: List<String> = OfficialHeader.FR,   // أسطر الرأسية بالفرنسية (يسار)
        val title: String,
        val subtitle: String,
        val schoolLine: String,          // الولاية والمؤسسة
        val rollHeader: String,
        val nameHeader: String,
        val gradeHeaders: List<String>, // 6 عناوين: اختبار ف1، امتحان ف1، ...
        val signature: String,
        val stampLines: List<String>,
        val page: (Int, Int) -> String,
        val seal: Bitmap? = null        // شعار الجمهورية في وسط الرأسية
    )

    fun build(students: List<StudentEntity>, labels: Labels): ByteArray {
        val sorted = students.sortedBy { it.rollNumber }
        val firstCapacity = ((H - 2 * M - HEADER - TITLE - TABLE_HEAD) / ROW).toInt()
        val otherCapacity = ((H - 2 * M - TABLE_HEAD - 20f) / ROW).toInt()
        // توزيع الطلاب على الصفحات
        val pages = ArrayList<List<StudentEntity>>()
        var rest = sorted
        var cap = firstCapacity
        while (rest.isNotEmpty() || pages.isEmpty()) {
            pages.add(rest.take(cap)); rest = rest.drop(cap); cap = otherCapacity
            if (rest.isEmpty()) break
        }
        // هل يتسع التوقيع تحت آخر جدول؟
        val lastTop = if (pages.size == 1) M + HEADER + TITLE else M + 20f
        val lastBottom = lastTop + TABLE_HEAD + pages.last().size * ROW
        val signatureOnNewPage = lastBottom + FOOTER > H - M
        val totalPages = pages.size + if (signatureOnNewPage) 1 else 0

        val doc = PdfDocument()
        pages.forEachIndexed { index, rows ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), index + 1).create())
            val c = page.canvas
            var y = M
            if (index == 0) {
                drawHeader(c, labels); y += HEADER
                PdfText.draw(c, labels.title, M, y, W - 2 * M, 22f, 12.5f, bold = true, maxLines = 1)
                PdfText.draw(c, labels.subtitle, M, y + 22f, W - 2 * M, 18f, 9f, maxLines = 1)
                PdfText.draw(c, labels.schoolLine, M, y + 40f, W - 2 * M, 18f, 9f, maxLines = 1)
                y += TITLE
            } else {
                y += 20f
            }
            val bottom = drawTable(c, y, rows, labels)
            if (index == pages.lastIndex && !signatureOnNewPage) drawSignature(c, bottom + 12f, labels)
            PdfText.draw(c, labels.page(index + 1, totalPages), M, H - M + 4f, W - 2 * M, 14f, 8f)
            doc.finishPage(page)
        }
        if (signatureOnNewPage) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), totalPages).create())
            drawSignature(page.canvas, M + 20f, labels)
            PdfText.draw(page.canvas, labels.page(totalPages, totalPages), M, H - M + 4f, W - 2 * M, 14f, 8f)
            doc.finishPage(page)
        }
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    private fun drawHeader(c: Canvas, l: Labels) = PdfCommon.drawOfficialHeader(c, l.seal)

    /** يرسم الجدول من اليمين إلى اليسار ويرجع أسفل الجدول. */
    private fun drawTable(c: Canvas, top: Float, rows: List<StudentEntity>, l: Labels): Float {
        val line = Paint().apply { color = BLACK; strokeWidth = 0.7f; style = Paint.Style.STROKE }
        val fill = Paint().apply { color = 0xFFEDEDED.toInt(); style = Paint.Style.FILL }
        val right = W - M
        val rollW = 28f
        val gradeW = 48f
        val nameW = (W - 2 * M) - rollW - 6 * gradeW
        // حدود الأعمدة من اليمين
        val edges = ArrayList<Float>()
        var x = right
        edges.add(x); x -= rollW; edges.add(x); x -= nameW; edges.add(x)
        repeat(6) { x -= gradeW; edges.add(x) }
        // رأس الجدول
        c.drawRect(M, top, right, top + TABLE_HEAD, fill)
        val heads = listOf(l.rollHeader, l.nameHeader) + l.gradeHeaders
        for (i in heads.indices) {
            PdfText.draw(c, heads[i], edges[i + 1] + 1f, top, edges[i] - edges[i + 1] - 2f, TABLE_HEAD, 7.5f, bold = true)
        }
        var y = top + TABLE_HEAD
        rows.forEach { s ->
            PdfText.draw(c, if (s.rollNumber > 0) s.rollNumber.toString() else "", edges[1], y, rollW, ROW, 9f)
            // الاسم بالعربية وتحته بالفرنسية
            val names = listOf(s.nameAr, s.nameFr).filter { it.isNotBlank() }.joinToString("\n")
            PdfText.draw(c, names, edges[2] + 3f, y, nameW - 6f, ROW, 8f, Layout.Alignment.ALIGN_NORMAL, maxLines = 2)
            y += ROW
        }
        // الخطوط
        val bottom = y
        c.drawRect(M, top, right, bottom, line)
        edges.drop(1).dropLast(1).forEach { ex -> c.drawLine(ex, top, ex, bottom, line) }
        var ry = top + TABLE_HEAD
        c.drawLine(M, ry, right, ry, line)
        repeat(rows.size) { ry += ROW; c.drawLine(M, ry, right, ry, line) }
        return bottom
    }

    /** «توقيع الأستاذ» وتحته الطابع باسمه، في الجهة اليسرى من الصفحة. */
    private fun drawSignature(c: Canvas, top: Float, l: Labels) {
        val boxW = 230f
        val left = M + 10f
        PdfText.draw(c, l.signature, left, top, boxW, 16f, 10f, bold = true, maxLines = 1)
        val stampTop = top + 22f
        val stampH = 16f * l.stampLines.size + 14f
        val ink = Paint().apply { color = INK; style = Paint.Style.STROKE; strokeWidth = 1.6f; isAntiAlias = true }
        c.drawRoundRect(RectF(left, stampTop, left + boxW, stampTop + stampH), 10f, 10f, ink)
        c.drawRoundRect(RectF(left + 3f, stampTop + 3f, left + boxW - 3f, stampTop + stampH - 3f), 8f, 8f, ink.apply { strokeWidth = 0.7f })
        l.stampLines.forEachIndexed { i, t ->
            PdfText.draw(c, t, left + 6f, stampTop + 7f + i * 16f, boxW - 12f, 16f, if (i == 0) 11f else 9f, bold = i == 0, color = INK, maxLines = 1)
        }
    }

    /** أسطر فارغة لسجل يُملأ كله باليد: أرقام من 1 إلى count بلا أسماء. */
    fun blankRows(count: Int = 70): List<StudentEntity> = (1..count).map {
        StudentEntity("blank-$it", "", it, "", "", "", "", "", "", 0L)
    }
}
