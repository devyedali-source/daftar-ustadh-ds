package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.elyedali.daftarustadh.data.RemarkLevel
import com.elyedali.daftarustadh.data.RemarkScale
import com.elyedali.daftarustadh.pdf.PdfCommon.H
import com.elyedali.daftarustadh.pdf.PdfCommon.M
import com.elyedali.daftarustadh.pdf.PdfCommon.W
import com.elyedali.daftarustadh.pdf.PdfCommon.num
import com.elyedali.daftarustadh.report.StudentReport
import java.io.ByteArrayOutputStream

/**
 * كشوف الدرجات على هيئة الكشف الرسمي، كشف واحد في كل صفحة A4 طولية.
 * term = 1: الاختبارات (للاطلاع) ثم امتحان الفصل الأول، وهو المعدل.
 * term = 2: الاختبارات وامتحان الفصل الأول (للاطلاع)، والمعدل = امتحان الفصل الثاني وحده.
 * الاختبارات كلها في خانة واحدة (12.5/ 13/ 15.7)، ولا تدخل في الحساب إلا في الكشف السنوي حيث يظهر معدلها فقط.
 * term = 3: الكشف السنوي: الامتحان الأول ×1، الثاني ×2، الثالث ×3، معدل الاختبارات ×3،
 * معدل المادة، الضارب، المجموع — والقيم تُكتب مضروبة كما في الكشف الرسمي.
 */
object ReportCardPdf {
    private val BLACK = 0xFF000000.toInt()
    private val GREY = 0xFFEDEDED.toInt()

    data class Info(
        val schoolYear: String,
        val schoolName: String,
        val className: String,
        val passThreshold: Double,
        val subjectRemarks: List<RemarkLevel>,
        val generalRemarks: List<RemarkLevel>,
        val date: String,
        val seal: Bitmap?
    )

    fun build(term: Int, reports: List<StudentReport>, info: Info): ByteArray =
        PdfCommon.render { drawInto(it, 1, term, reports, info) }

    /** يضيف كشوف الطلاب إلى مستند قائم (كشف في كل صفحة)، ويرجع رقم الصفحة التالية. */
    fun drawInto(doc: PdfDocument, startNo: Int, term: Int, reports: List<StudentReport>, info: Info): Int {
        reports.forEachIndexed { i, r ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), startNo + i).create())
            drawCard(page.canvas, term, r, info)
            doc.finishPage(page)
        }
        return startNo + reports.size
    }

    private val line = Paint().apply { color = BLACK; strokeWidth = 0.7f; style = Paint.Style.STROKE }
    private val fill = Paint().apply { color = GREY; style = Paint.Style.FILL }

    private fun cell(c: Canvas, t: String, l: Float, top: Float, w: Float, h: Float, size: Float = 9f,
                     bold: Boolean = false, align: Layout.Alignment = Layout.Alignment.ALIGN_CENTER, lines: Int = 2) =
        PdfText.draw(c, t, l + 2f, top, w - 4f, h, size, align, bold, maxLines = lines)

    private fun drawCard(c: Canvas, term: Int, r: StudentReport, info: Info) {
        val annual = term == 3
        val half = (W - 2 * M) / 2f
        PdfCommon.drawOfficialHeader(c, info.seal)
        var y = M + PdfCommon.HEADER

        // العنوان
        val termAr = when (term) { 1 -> "الفصل الأول"; 2 -> "الفصل الثاني"; else -> "الكشف السنوي" }
        val termFr = when (term) { 1 -> "1er Trimestre"; 2 -> "2e Trimestre"; else -> "Annuel" }
        cell(c, "كشف درجات — $termAr", M + half, y, half, 22f, 13f, bold = true)
        cell(c, "Bulletin de Notes — $termFr", M, y, half, 22f, 12f, bold = true)
        y += 26f

        // معلومات الطالب
        val s = r.student
        val rowsAr = listOf(
            "السنة الدراسية: \u2066${info.schoolYear}\u2069",
            "المؤسسة: ${info.schoolName}",
            "الاسم الكامل: ${s.nameAr.ifBlank { s.nameFr }}",
            "القسم: \u2066${info.className}\u2069 — رقم النداء: ${s.rollNumber}",
            "الرقم الوطني: ${s.nationalId} — الرقم المدرسي: ${s.rim}"
        )
        val rowsFr = listOf(
            "Année Scolaire : ${info.schoolYear}",
            "Établissement : ${info.schoolName}",
            "Nom et Prénom : ${s.nameFr.ifBlank { s.nameAr }}",
            "Classe : ${info.className} — N° d'ordre : ${s.rollNumber}",
            "Numéro National : ${s.nationalId} — RIM : ${s.rim}"
        )
        rowsAr.indices.forEach { i ->
            cell(c, rowsAr[i], M + half, y, half, 16f, 9.5f, align = Layout.Alignment.ALIGN_NORMAL, lines = 1)
            cell(c, rowsFr[i], M, y, half, 16f, 9.5f, align = Layout.Alignment.ALIGN_NORMAL, lines = 1)
            y += 16f
        }
        y += 6f

        // الجدول — الأعمدة من اليمين إلى اليسار
        val right = W - M
        data class Col(val ar: String, val fr: String, val w: Float)
        val cols = if (annual) listOf(
            Col("المواد", "", 74f), Col("", "Matières", 74f),
            Col("الامتحان الأول ×1", "1° Compo ×1", 44f), Col("الامتحان الثاني ×2", "2° Compo ×2", 44f),
            Col("الامتحان الثالث ×3", "3° Compo ×3", 44f), Col("معدل الاختبارات ×3", "Moy. Interrog ×3", 48f),
            Col("معدل المادة", "Moy. Discipl", 40f), Col("الضارب", "Coeff", 32f), Col("المجموع", "Total", 42f)
        ) else if (term == 1) listOf(
            Col("المواد", "", 80f), Col("", "Matières", 80f),
            Col("الاختبارات", "Interrogations", 62f),
            Col("امتحان الفصل", "Composition", 54f), Col("الضارب", "Coeff", 36f), Col("المجموع", "Total", 50f)
        ) else listOf(
            Col("المواد", "", 72f), Col("", "Matières", 72f),
            Col("الاختبارات", "Interrogations", 66f),
            Col("امتحان الفصل الأول", "1° Compo", 48f),
            Col("امتحان الفصل الثاني", "2° Compo", 50f), Col("الضارب", "Coeff", 32f), Col("المجموع", "Total", 46f)
        )
        val obsW = (W - 2 * M) - cols.sumOf { it.w.toDouble() }.toFloat()
        val allCols = cols + Col("ملاحظات الأساتذة", "Observations et Signature des Professeurs", obsW)
        val edges = ArrayList<Float>().apply { var x = right; add(x); allCols.forEach { x -= it.w; add(x) } }
        val headH = 30f
        c.drawRect(M, y, right, y + headH, fill)
        allCols.forEachIndexed { i, col ->
            val text = listOf(col.ar, col.fr).filter { it.isNotEmpty() }.joinToString("\n")
            cell(c, text, edges[i + 1], y, col.w, headH, 7.5f, bold = true)
        }
        val tableTop = y
        y += headH
        val rowH = if (r.lines.size > 11) 21f else 24f
        r.lines.forEach { ln ->
            val values: List<String> = if (annual) listOf(
                num(ln.e1), num(ln.e2?.times(2)), num(ln.e3?.times(3)), num(ln.testsAvg?.times(3)),
                num(ln.average), ln.subject.coefficient.toString(), num(ln.total)
            ) else if (term == 1) listOf(
                ln.testsShown, num(ln.average), ln.subject.coefficient.toString(), num(ln.total)
            ) else listOf(
                ln.testsShown, num(ln.e1), num(ln.average), ln.subject.coefficient.toString(), num(ln.total)
            )
            cell(c, ln.subject.nameAr, edges[1], y, allCols[0].w, rowH, 8.5f, align = Layout.Alignment.ALIGN_NORMAL)
            cell(c, ln.subject.nameFr, edges[2], y, allCols[1].w, rowH, 8f, align = Layout.Alignment.ALIGN_NORMAL)
            values.forEachIndexed { k, v -> cell(c, v, edges[k + 3], y, allCols[k + 2].w, rowH, 9f, bold = k == values.lastIndex) }
            val remark = RemarkScale.remarkFor(ln.average, info.subjectRemarks)
            // ملاحظة المادة بلغة تدريسها وحدها (العربية أو الفرنسية)
            val obs = listOfNotNull(remark?.let { RemarkScale.textFor(it, ln.subject.code) }, ln.teacherName.takeIf { it.isNotBlank() })
                .joinToString("\n")
            cell(c, obs, edges[edges.size - 1], y, obsW, rowH, 7f)
            y += rowH
        }
        // خطوط الجدول
        c.drawRect(M, tableTop, right, y, line)
        edges.drop(1).dropLast(1).forEach { ex -> c.drawLine(ex, tableTop, ex, y, line) }
        var ry = tableTop + headH
        c.drawLine(M, ry, right, ry, line)
        repeat(r.lines.size) { ry += rowH; c.drawLine(M, ry, right, ry, line) }
        y += 14f

        // الأسفل: يسارًا المجموع والمعدل والرتبة والقرار، ويمينًا التاريخ والتوقيع وملاحظات المدير
        val blockW = 250f
        val avgAr = if (annual) "المعدل السنوي العام" else "المعدل العام"
        val avgFr = if (annual) "Moyenne Générale Annuelle" else "Moyenne Générale"
        val lefts = listOf(
            Triple("Total", num(r.total), "المجموع"),
            Triple(avgFr, num(r.generalAverage), avgAr),
            Triple("Rang", "${r.rank} / ${r.classSize}", "الرتبة")
        )
        var ly = y
        lefts.forEach { (fr, v, ar) ->
            cell(c, fr, M, ly, 95f, 20f, 9f, bold = true, align = Layout.Alignment.ALIGN_NORMAL, lines = 1)
            c.drawRect(M + 97f, ly + 1f, M + 160f, ly + 19f, line)
            cell(c, v, M + 97f, ly, 63f, 20f, 10f, bold = true, lines = 1)
            cell(c, ar, M + 162f, ly, blockW - 162f, 20f, 9f, bold = true, lines = 1)
            ly += 22f
        }
        if (annual) {
            cell(c, "DECISION — القرار", M, ly, blockW, 18f, 10f, bold = true, lines = 1)
            ly += 20f
            val passed = r.generalAverage >= info.passThreshold
            listOf(
                Triple("Admis en classe supérieure", "متجاوز إلى القسم الأعلى", passed),
                Triple("Redoublement autorisé", "مرخص له في إعادة السنة", false),
                Triple("Exclusion définitive", "مطرود نهائيا", false)
            ).forEach { (fr, ar, checked) ->
                cell(c, fr, M, ly, 110f, 16f, 8f, align = Layout.Alignment.ALIGN_NORMAL, lines = 1)
                c.drawRect(M + 112f, ly + 3f, M + 122f, ly + 13f, line)
                if (checked) cell(c, "✓", M + 110f, ly, 14f, 16f, 11f, bold = true, lines = 1)
                cell(c, ar, M + 126f, ly, blockW - 126f, 16f, 8.5f, lines = 1)
                ly += 17f
            }
        }

        val rx = W - M - blockW
        var yy = y
        cell(c, "التاريخ: \u2066${info.date}\u2069 — Date", rx, yy, blockW, 18f, 9f, lines = 1); yy += 22f
        cell(c, "توقيع وختم مدير المؤسسة\nSignature et cachet du Directeur de l'Établissement", rx, yy, blockW, 28f, 8.5f, bold = true)
        yy += 70f
        val general = RemarkScale.remarkFor(r.generalAverage, info.generalRemarks)
        cell(c, "ملاحظات المدير — Observations du Directeur", rx, yy, blockW, 16f, 8.5f, bold = true, lines = 1); yy += 18f
        if (general != null) {
            cell(c, general.textAr, rx, yy, blockW, 16f, 9.5f, lines = 1); yy += 16f
            cell(c, general.textFr, rx, yy, blockW, 16f, 9f, lines = 1)
        }
    }
}
