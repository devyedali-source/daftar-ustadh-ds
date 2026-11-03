package com.elyedali.daftarustadh.pdf

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.elyedali.daftarustadh.data.Gender
import com.elyedali.daftarustadh.data.StudentEntity

/**
 * لائحة طلاب القسم داخل ملف القسم (صفحات PDF تُقرأ في واتساب وتُطبع):
 * - ملف الأساتذة: رقم النداء والاسم بالعربية والفرنسية.
 * - الملف الكامل: ومعها الجنس والرقم المدرسي والرقم الوطني وهاتف الوكيل.
 * الجدول من اليمين إلى اليسار، وعنوان القسم والمؤسسة في أعلى كل صفحة.
 */
object StudentListPdf {
    private const val W = 595f
    private const val H = 842f
    private const val M = 28f
    private const val ROW = 24f
    private const val TITLE = 46f
    private const val HEAD = 30f
    private val BLACK = 0xFF000000.toInt()

    data class Info(
        val title: String,      // مثل: لائحة طلاب القسم 1AS1 — Liste des élèves
        val subtitle: String,   // المؤسسة والسنة
        val full: Boolean       // الملف الكامل: كل المعلومات
    )

    private class Col(val head: String, val w: Float, val align: Layout.Alignment, val value: (StudentEntity) -> String)

    /** يضيف لائحة الطلاب إلى مستند قائم، ويرجع رقم الصفحة التالية. */
    fun drawInto(doc: PdfDocument, startNo: Int, students: List<StudentEntity>, info: Info): Int {
        val sorted = students.sortedBy { it.rollNumber }
        val center = Layout.Alignment.ALIGN_CENTER
        val normal = Layout.Alignment.ALIGN_NORMAL
        val fixed = ArrayList<Col>()
        fixed += Col("رقم النداء\nN°", 40f, center) { if (it.rollNumber > 0) it.rollNumber.toString() else "" }
        val extra = if (info.full) listOf(
            Col("الجنس\nSexe", 40f, center) { s -> when (s.gender) { Gender.BOY -> "ولد"; Gender.GIRL -> "بنت"; else -> "" } },
            Col("الرقم المدرسي\nMatricule", 72f, center) { s -> if (s.rim.isBlank()) "" else "\u2066${s.rim}\u2069" },
            Col("الرقم الوطني\nNNI", 76f, center) { s -> if (s.nationalId.isBlank()) "" else "\u2066${s.nationalId}\u2069" },
            Col("هاتف الوكيل\nTél. tuteur", 80f, center) { s ->
                listOf(s.guardianPhone1, s.guardianPhone2).filter { it.isNotBlank() }.joinToString("\n") { "\u2066$it\u2069" }
            }
        ) else emptyList()
        val nameW = (W - 2 * M) - fixed.sumOf { it.w.toDouble() }.toFloat() - extra.sumOf { it.w.toDouble() }.toFloat()
        val cols = fixed + Col("الاسم — Nom", nameW, normal) { s ->
            listOf(s.nameAr, s.nameFr).filter { it.isNotBlank() }.joinToString("\n")
        } + extra

        val perPage = ((H - 2 * M - TITLE - HEAD - 16f) / ROW).toInt()
        val pages = sorted.chunked(perPage).ifEmpty { listOf(emptyList()) }
        val line = Paint().apply { color = BLACK; strokeWidth = 0.7f; style = Paint.Style.STROKE }
        val fill = Paint().apply { color = 0xFFEDEDED.toInt(); style = Paint.Style.FILL }

        pages.forEachIndexed { pi, rows ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), startNo + pi).create())
            val c = page.canvas
            PdfText.draw(c, info.title, M, M, W - 2 * M, 22f, 12.5f, bold = true, maxLines = 1)
            PdfText.draw(c, info.subtitle, M, M + 22f, W - 2 * M, 18f, 9f, maxLines = 1)
            val top = M + TITLE
            // حدود الأعمدة من اليمين
            val edges = ArrayList<Float>()
            var x = W - M
            edges += x
            cols.forEach { x -= it.w; edges += x }
            c.drawRect(M, top, W - M, top + HEAD, fill)
            cols.forEachIndexed { i, col ->
                PdfText.draw(c, col.head, edges[i + 1] + 1f, top, col.w - 2f, HEAD, 7.5f, bold = true)
            }
            var y = top + HEAD
            rows.forEach { s ->
                cols.forEachIndexed { i, col ->
                    val pad = if (col.align == normal) 4f else 1f
                    PdfText.draw(c, col.value(s), edges[i + 1] + pad, y, col.w - 2 * pad, ROW, 8.5f, col.align,
                        bold = i == 0, maxLines = 2)
                }
                y += ROW
            }
            c.drawRect(M, top, W - M, y, line)
            edges.drop(1).dropLast(1).forEach { ex -> c.drawLine(ex, top, ex, y, line) }
            var ry = top + HEAD
            c.drawLine(M, ry, W - M, ry, line)
            repeat(rows.size) { ry += ROW; c.drawLine(M, ry, W - M, ry, line) }
            val footer = "عدد الطلاب: \u2066${sorted.size}\u2069 — Effectif : ${sorted.size}" +
                if (pages.size > 1) "    \u2066${pi + 1}/${pages.size}\u2069" else ""
            PdfText.draw(c, footer, M, H - M - 6f, W - 2 * M, 14f, 8.5f, maxLines = 1)
            doc.finishPage(page)
        }
        return startNo + pages.size
    }
}
