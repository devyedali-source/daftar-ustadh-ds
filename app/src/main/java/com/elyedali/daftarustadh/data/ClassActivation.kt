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

    // تفعيل الإدارة: للفصل الأخير فقط (الكشف السنوي)، حسب عدد الأقسام
    const val PRICE_UP_TO_3 = 700
    const val PRICE_UP_TO_6 = 900
    const val PRICE_MORE = 1100

    /** سعر تفعيل عدد من الأقسام: 1–3 أقسام 700، 4–6 أقسام 900، أكثر من 6 أقسام 1100. */
    fun price(count: Int): Int = when {
        count <= 0 -> 0
        count <= 3 -> PRICE_UP_TO_3
        count <= 6 -> PRICE_UP_TO_6
        else -> PRICE_MORE
    }

    fun decode(text: String?): Set<String> =
        if (text.isNullOrBlank()) emptySet()
        else runCatching { JSONArray(text).let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } }.getOrDefault(emptySet())

    fun encode(ids: Set<String>): String = JSONArray(ids.sorted()).toString()

    /** الكشوف التي تحتاج تفعيلًا: الفصل الأخير فقط (الكشف السنوي). الفصلان الأول والثاني مجانيان. */
    fun needsActivation(term: Int) = term >= 3

    /** كود قصير للقسم يُرسل في طلب التفعيل. */
    fun code(classId: String): String = classId.replace("-", "").take(8).uppercase()
}
