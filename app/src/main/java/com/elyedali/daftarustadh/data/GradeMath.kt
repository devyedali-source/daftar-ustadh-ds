package com.elyedali.daftarustadh.data

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * قواعد حساب معدل المادة (على 20):
 * - الفصل الأول: امتحان الفصل الأول وحده.
 * - الفصل الثاني: امتحان الفصل الثاني وحده.
 * - الفصل الثالث (الأخير): (معدل الاختبارات × 3 + امتحان ف1 × 1 + امتحان ف2 × 2 + امتحان ف3 × 3) ÷ 9
 *   ومعدل الاختبارات = معدل الاختبارات المدرجة، أو أفضل نتيجة اختبار، حسب اختيار الأستاذ.
 * يُرجع null إن نقصت نقطة لازمة.
 */
object GradeMath {

    /**
     * تقريب لرقمين بعد الفاصلة (0.005 تُرفع) بحساب عشري دقيق.
     * كل معدل يُقرَّب قبل استعماله، فما يُطبع في الكشف يتطابق مع الحساب باليد.
     */
    fun round2(v: Double): Double = BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).toDouble()

    /**
     * قسمة عشرية دقيقة مقرّبة لرقمين: القسمة بالأعداد العادية (Double) تعطي مثلًا 133.7 ÷ 28 = 4.7749999…
     * فتُقرَّب خطأً إلى 4.77 بدل 4.78. هنا يُقسم المجموع كما يُقسم باليد.
     */
    fun div2(sum: BigDecimal, n: Int): Double = sum.divide(BigDecimal(n), 2, RoundingMode.HALF_UP).toDouble()

    private fun dec(v: Double): BigDecimal = BigDecimal.valueOf(v)

    fun testsAverage(grades: Map<String, Double>, mode: String): Double? {
        // كل الاختبارات المدرجة في الفصول الثلاثة، بما فيها الاختبارات الإضافية
        val tests = GradeSlot.allTests(grades)
        if (tests.isEmpty()) return null
        return if (mode == TestsMode.BEST) round2(tests.max()) else div2(tests.fold(BigDecimal.ZERO) { a, x -> a + dec(x) }, tests.size)
    }

    fun subjectAverage(term: Int, grades: Map<String, Double>, testsMode: String): Double? = when (term) {
        1 -> grades[GradeSlot.E1]?.let { round2(it) }
        2 -> grades[GradeSlot.E2]?.let { round2(it) }
        else -> {
            val t = testsAverage(grades, testsMode)
            val e1 = grades[GradeSlot.E1]; val e2 = grades[GradeSlot.E2]; val e3 = grades[GradeSlot.E3]
            if (t == null || e1 == null || e2 == null || e3 == null) null
            else div2(dec(t) * BigDecimal(3) + dec(e1) + dec(e2) * BigDecimal(2) + dec(e3) * BigDecimal(3), 9)
        }
    }

    /** يقرأ نقطة كتبها المستخدم: يقبل الفاصلة أو النقطة، والأرقام العربية، ومن 0 إلى 20. */
    fun parse(text: String): Double? {
        val t = text.trim()
            .map { c -> if (c in '٠'..'٩') ('0' + (c - '٠')) else c }
            .joinToString("").replace(',', '.').replace('٫', '.')
        if (t.isEmpty()) return null
        val v = t.toDoubleOrNull() ?: return null
        return if (v in 0.0..20.0) v else null
    }

    /** عرض النقطة بأرقام لاتينية وبرقمين بعد الفاصلة على الأكثر: 12.5، 13.75، 14. */
    fun format(v: Double?): String =
        if (v == null) "" else BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

    /** هل نتائج الطالب في المادة مكتملة لكشف هذا الفصل؟ */
    fun isComplete(term: Int, grades: Map<String, Double>): Boolean = when (term) {
        1 -> grades[GradeSlot.E1] != null
        2 -> grades[GradeSlot.E2] != null
        else -> GradeSlot.allTests(grades).isNotEmpty() &&
            grades[GradeSlot.E1] != null && grades[GradeSlot.E2] != null && grades[GradeSlot.E3] != null
    }
}
