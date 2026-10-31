package com.elyedali.daftarustadh.data

import org.json.JSONArray

/**
 * تفعيل أقسام الإدارة: الفصل الأول مجاني، والتفعيل يفتح الفصلين الثاني والثالث للسنة الدراسية
 * (كشف الفصل الثاني والكشف السنوي). الأستاذ مجاني دائمًا.
 *
 * الأسعار (أوقية جديدة): قسم 350، ثلاثة أقسام 1000، خمسة أقسام 1500، سبعة فما فوق 1900.
 * للأعداد الأخرى يُحسب أرخص جمع: قسمان 700، أربعة 1350، ستة 1850.
 */
object ClassActivation {
    const val KEY = "activated_classes"          // معرّفات الأقسام المفعّلة (يضعها الخادم/الإدارة)
    const val REQUESTED_KEY = "activation_requested"  // أقسام أُرسل طلب تفعيلها وتنتظر
    const val ADMIN_PHONE = "22237786585"

    const val PRICE_1 = 350
    const val PRICE_3 = 1000
    const val PRICE_5 = 1500
    const val PRICE_7_PLUS = 1900

    /** سعر تفعيل عدد من الأقسام. */
    fun price(count: Int): Int {
        if (count <= 0) return 0
        if (count >= 7) return PRICE_7_PLUS
        // أرخص جمع من باقات 1 و3 و5 (والعدد أقل من 7)
        val best = IntArray(count + 1) { Int.MAX_VALUE }
        best[0] = 0
        for (n in 1..count) {
            for ((size, cost) in listOf(1 to PRICE_1, 3 to PRICE_3, 5 to PRICE_5)) {
                if (size <= n && best[n - size] != Int.MAX_VALUE) best[n] = minOf(best[n], best[n - size] + cost)
            }
        }
        return minOf(best[count], PRICE_7_PLUS)
    }

    fun decode(text: String?): Set<String> =
        if (text.isNullOrBlank()) emptySet()
        else runCatching { JSONArray(text).let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } }.getOrDefault(emptySet())

    fun encode(ids: Set<String>): String = JSONArray(ids.sorted()).toString()

    /** الكشوف التي تحتاج تفعيلًا: كشف الفصل الثاني (2) والكشف السنوي (3). */
    fun needsActivation(term: Int) = term >= 2

    /** كود قصير للقسم يُرسل في طلب التفعيل. */
    fun code(classId: String): String = classId.replace("-", "").take(8).uppercase()
}
