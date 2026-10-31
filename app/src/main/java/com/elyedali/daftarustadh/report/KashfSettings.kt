package com.elyedali.daftarustadh.report

import com.elyedali.daftarustadh.data.RemarkLevel
import org.json.JSONObject

/**
 * إعدادات الكشوف عند الإدارة:
 * - معدل النجاح لكل مستوى (وفي الثانوية لكل مستوى مع شعبته: 5C، 6D...) بين 8 و10.
 * - الملاحظة العامة حسب المعدل العام (تظهر في «ملاحظات المدير»).
 */
object KashfSettings {
    const val THRESHOLDS_KEY = "pass_thresholds"
    const val GENERAL_REMARKS_KEY = "general_remarks"
    const val DEFAULT_THRESHOLD = 10.0
    const val MIN_THRESHOLD = 8.0
    const val MAX_THRESHOLD = 10.0

    val DEFAULT_GENERAL_REMARKS = listOf(
        RemarkLevel(0.0, "نتائج ضعيفة، عليه مضاعفة الجهد", "Résultats faibles, doit redoubler d'efforts"),
        RemarkLevel(8.0, "نتائج غير كافية، يمكنه التحسن", "Résultats insuffisants, peut mieux faire"),
        RemarkLevel(10.0, "نتائج مقبولة", "Résultats passables"),
        RemarkLevel(12.0, "نتائج حسنة", "Assez bons résultats"),
        RemarkLevel(14.0, "نتائج جيدة، واصل", "Bons résultats, continuez"),
        RemarkLevel(16.0, "نتائج جيدة جدا", "Très bons résultats"),
        RemarkLevel(18.0, "نتائج ممتازة، تهانينا", "Excellents résultats, félicitations")
    )

    fun decodeThresholds(text: String?): Map<String, Double> = try {
        val o = JSONObject(text ?: "{}")
        o.keys().asSequence().associateWith { o.getDouble(it) }
    } catch (e: Exception) { emptyMap() }

    fun encodeThresholds(map: Map<String, Double>): String =
        JSONObject().apply { map.forEach { (k, v) -> put(k, v) } }.toString()
}
