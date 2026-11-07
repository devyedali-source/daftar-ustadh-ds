package com.elyedali.daftarustadh.data

/**
 * تفعيل الأستاذ (120 أوقية للسنة الدراسية) يفتح معًا:
 * - التخطيط السنوي والتحاضير للفصلين الثاني والثالث (الفصل الأول مجاني)، تابعًا كان للإدارة أو مستقلًا؛
 * - للأستاذ المستقل (عمله الفردي): رصد نتائج الفصل الأخير وحساب معدل المادة.
 * الأستاذ التابع لإدارة تعمل بالتطبيق يرصد نتائجه مجانًا.
 */
object TeacherActivation {
    const val KEY = "teacher_activated_years"        // السنوات المفعّلة (تضعها الإدارة/الخادم)
    const val REQUESTED_KEY = "teacher_activation_requested"
    const val CODE_KEY = "teacher_install_code"
    const val PRICE = 120

    fun decode(text: String?): Set<String> = ClassActivation.decode(text)
    fun encode(years: Set<String>): String = ClassActivation.encode(years)

    /** التخطيط والتحاضير: الفصل الأول مجاني. */
    fun planningNeedsActivation(term: Int) = term >= 2

    /** نتائج الأستاذ المستقل: الفصل الأخير. */
    fun gradesNeedActivation(term: Int) = term >= 3

    /** كود قصير لهذا الجهاز يُرسل في طلب التفعيل. */
    fun newCode(): String = java.util.UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
}
