package com.elyedali.daftarustadh.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument

/**
 * دفتر الغياب (سجل النداء) للسحب والملء باليد — A4 عرضية، ثنائي اللغة.
 * لكل أسبوع من السنة الدراسية صفحة (أو أكثر إن كثر الطلاب):
 * رقم النداء والاسم، ثم لكل يوم دراسة ست خانات (حصة لكل ساعة)، ثم مجموع الغيابات.
 */
object AbsencePdf {
    private const val W = 842f
    private const val H = 595f
    private const val M = 24f
    private const val HEADER = 82f
    private const val TITLE = 40f
    private const val DAY_HEAD = 22f   // اسم اليوم وتاريخه
    private const val HOUR_HEAD = 12f  // أرقام الحصص 1..6
    private const val HEAD = DAY_HEAD + HOUR_HEAD
    const val HOURS = 6
    private const val ROW = 15f
    private const val ROLL_W = 22f
    private const val NAME_W = 150f
    private const val TOTAL_W = 34f
    private val BLACK = 0xFF000000.toInt()

    /** يوم دراسة: رقمه في الشهر واسمه المختصر. */
    data class Day(val number: Int, val label: String)

    data class Month(val titleAr: String, val titleFr: String, val days: List<Day>)

    data class Student(val roll: Int, val nameAr: String, val nameFr: String)

    data class Info(
        val title: String,          // «دفتر الغياب — Registre d'appel»
        val lineAr: String,
        val lineFr: String,
        val shortLine: String,      // سطر مختصر أعلى الصفحات التالية (القسم والسنة)
        val months: List<Month>,
        val students: List<Student>,
        val rollHeader: String,
        val nameHeader: String,
        val totalHeader: String,
        val page: (Int, Int) -> String,
        val seal: Bitmap?
    )

    fun build(info: Info): ByteArray = PdfCommon.render { drawInto(it, 1, info) }

    /** يضيف دفتر غياب القسم إلى مستند قائم، ويرجع رقم الصفحة التالية. */
    fun drawInto(doc: PdfDocument, startNo: Int, info: Info): Int {
        val students = info.students.sortedBy { it.roll }.ifEmpty { (1..35).map { Student(it, "", "") } }
        val firstCap = ((H - M - 16f - (M + HEADER + TITLE + 18f) - HEAD) / ROW).toInt()
        val otherCap = ((H - M - 16f - (M + 18f + 18f) - HEAD) / ROW).toInt()
        // صفحات كل شهر
        val plan = ArrayList<Triple<Month, List<Student>, Boolean>>()   // (الشهر، طلاب الصفحة، أول صفحة في الدفتر؟)
        info.months.forEachIndexed { mi, m ->
            var rest = students
            var first = mi == 0
            while (rest.isNotEmpty()) {
                val cap = if (first) firstCap else otherCap
                plan += Triple(m, rest.take(cap), first)
                rest = rest.drop(cap); first = false
            }
        }
        val total = plan.size
        plan.forEachIndexed { i, (m, rows, first) ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), startNo + i).create())
            val c = page.canvas
            var y = M
            if (first) {
                PdfCommon.drawOfficialHeader(c, info.seal, W)
                y += HEADER
                PdfText.draw(c, info.title, M, y, W - 2 * M, 18f, 13f, bold = true, maxLines = 1)
                PdfText.draw(c, info.lineAr, M, y + 18f, W - 2 * M, 11f, 8.5f, maxLines = 1)
                PdfText.draw(c, info.lineFr, M, y + 29f, W - 2 * M, 11f, 8.5f, maxLines = 1)
                y += TITLE
            } else {
                PdfText.draw(c, info.shortLine, M, y, W - 2 * M, 16f, 9f, bold = true, maxLines = 1)
                y += 18f
            }
            PdfText.draw(c, "${m.titleAr} — ${m.titleFr}", M, y, W - 2 * M, 18f, 11f, bold = true, maxLines = 1)
            y += 18f
            drawTable(c, y, m, rows, info)
            PdfText.draw(c, info.page(i + 1, total), M, H - M + 2f, W - 2 * M, 14f, 8f)
            doc.finishPage(page)
        }
        return startNo + total
    }

    private fun drawTable(c: Canvas, top: Float, m: Month, rows: List<Student>, info: Info) {
        val line = Paint().apply { color = BLACK; strokeWidth = 0.6f; style = Paint.Style.STROKE }
        val fill = Paint().apply { color = 0xFFEDEDED.toInt(); style = Paint.Style.FILL }
        val right = W - M
        val thick = Paint().apply { color = BLACK; strokeWidth = 1.1f; style = Paint.Style.STROKE }
        val days = m.days.size.coerceAtLeast(1)
        val dayW = (W - 2 * M - ROLL_W - NAME_W - TOTAL_W) / days
        val hourW = dayW / HOURS
        // حدود الأعمدة من اليمين: رقم النداء، الاسم، الأيام، المجموع
        val edges = ArrayList<Float>()
        var x = right
        edges += x; x -= ROLL_W; edges += x; x -= NAME_W; edges += x
        repeat(days) { x -= dayW; edges += x }
        x -= TOTAL_W; edges += x
        edges[edges.lastIndex] = M
        val bottom = top + HEAD + rows.size * ROW
        c.drawRect(M, top, right, top + HEAD, fill)
        PdfText.draw(c, info.rollHeader, edges[1], top, ROLL_W, HEAD, 6.5f, bold = true)
        PdfText.draw(c, info.nameHeader, edges[2], top, NAME_W, HEAD, 7.5f, bold = true)
        m.days.forEachIndexed { i, d ->
            val dx = edges[3 + i]
            PdfText.draw(c, "${d.label} ${d.number}", dx, top, dayW, DAY_HEAD, 7.5f, bold = true, maxLines = 1)
            // الحصص من اليمين: 1 ثم 2 … 6
            for (h in 0 until HOURS) {
                PdfText.draw(c, "${h + 1}", dx + dayW - (h + 1) * hourW, top + DAY_HEAD, hourW, HOUR_HEAD, 6.5f, bold = true)
            }
        }
        PdfText.draw(c, info.totalHeader, M, top, TOTAL_W, HEAD, 6.5f, bold = true)
        var y = top + HEAD
        rows.forEach { s ->
            PdfText.draw(c, if (s.roll > 0) s.roll.toString() else "", edges[1], y, ROLL_W, ROW, 7.5f)
            val name = listOf(s.nameAr, s.nameFr).filter { it.isNotBlank() }.joinToString(" — ")
            PdfText.draw(c, name, edges[2] + 2f, y, NAME_W - 4f, ROW, 7f, align = android.text.Layout.Alignment.ALIGN_NORMAL, maxLines = 1)
            y += ROW
        }
        c.drawRect(M, top, right, bottom, thick)
        edges.drop(1).dropLast(1).forEach { ex -> c.drawLine(ex, top, ex, bottom, thick) }
        // خطوط الحصص داخل كل يوم
        m.days.indices.forEach { i ->
            val dx = edges[3 + i]
            c.drawLine(dx, top + DAY_HEAD, dx + dayW, top + DAY_HEAD, line)
            for (h in 1 until HOURS) { val hx = dx + h * hourW; c.drawLine(hx, top + DAY_HEAD, hx, bottom, line) }
        }
        var ry = top + HEAD
        c.drawLine(M, ry, right, ry, thick)
        repeat(rows.size) { ry += ROW; c.drawLine(M, ry, right, ry, line) }
    }
}
