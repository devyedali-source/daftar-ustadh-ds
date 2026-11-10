package com.elyedali.daftarustadh.data

import org.json.JSONArray
import org.json.JSONObject

/** ملاحظة تُكتب تلقائيًا حين يكون المعدل ≥ min (حتى الدرجة التالية). */
data class RemarkLevel(val min: Double, val textAr: String, val textFr: String)

/**
 * ملاحظات سجل الأستاذ حسب معدل المادة. يعدّلها الأستاذ من «إعدادات سجل الأستاذ».
 * تُحفظ في جدول الإعدادات الداخلية (app_meta) بالمفتاح teacher_remarks.
 */
object RemarkScale {
    const val KEY = "teacher_remarks"

    val DEFAULT = listOf(
        RemarkLevel(0.0, "ضعيف جدا", "Très faible"),
        RemarkLevel(5.0, "ضعيف", "Faible"),
        RemarkLevel(8.0, "غير كاف", "Insuffisant"),
        RemarkLevel(10.0, "مقبول", "Passable"),
        RemarkLevel(12.0, "حسن", "Assez bien"),
        RemarkLevel(14.0, "جيد", "Bien"),
        RemarkLevel(16.0, "جيد جدا", "Très bien"),
        RemarkLevel(18.0, "ممتاز", "Excellent")
    )

    /** المواد التي تُدرَّس بالفرنسية (أو بلغة أجنبية): ملاحظتها بالفرنسية فقط؛ وغيرها بالعربية فقط. */
    private val FRENCH_TAUGHT = setOf("MATS", "PC", "SN", "FR", "ANG", "TI")
    fun isFrenchTaught(subjectCode: String) = subjectCode in FRENCH_TAUGHT
    /** نص ملاحظة المادة بلغة تدريسها. */
    fun textFor(level: RemarkLevel, subjectCode: String): String = if (isFrenchTaught(subjectCode)) level.textFr else level.textAr

    fun remarkFor(avg: Double?, scale: List<RemarkLevel>): RemarkLevel? =
        if (avg == null) null else scale.sortedBy { it.min }.lastOrNull { avg >= it.min }

    fun encode(scale: List<RemarkLevel>): String = JSONArray().apply {
        scale.sortedBy { it.min }.forEach { put(JSONObject().put("min", it.min).put("ar", it.textAr).put("fr", it.textFr)) }
    }.toString()

    fun decode(text: String?): List<RemarkLevel> {
        if (text.isNullOrBlank()) return DEFAULT
        return try {
            val a = JSONArray(text)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                RemarkLevel(o.getDouble("min"), o.getString("ar"), o.getString("fr"))
            }.sortedBy { it.min }.ifEmpty { DEFAULT }
        } catch (e: Exception) {
            DEFAULT
        }
    }
}
