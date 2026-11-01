package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.TeacherTimetableEntity
import org.json.JSONArray
import org.json.JSONObject

/** حصة في جدول الأستاذ المرسل إليه. */
data class TimetableFileEntry(
    val day: Int,
    val period: Int,
    val startTime: String,
    val endTime: String,
    val className: String,
    val subjectCode: String,
    val subjectNameAr: String,
    val subjectNameFr: String
)

/** جدول أستاذ ترسله إدارة المؤسسة إليه (ملف ‎.dsq). */
data class TimetableFile(
    val institutionId: String,
    val schoolName: String,
    val wilaya: String,
    val schoolYear: String,
    val teacherName: String,
    val entries: List<TimetableFileEntry>
) {
    fun toJson(format: String, type: String): JSONObject = JSONObject()
        .put("format", format).put("version", 1).put("type", type)
        .put("exportedAt", System.currentTimeMillis())
        .put("institution", JSONObject().put("id", institutionId).put("schoolName", schoolName).put("wilaya", wilaya))
        .put("schoolYear", schoolYear)
        .put("teacherName", teacherName)
        .put("entries", JSONArray().apply {
            entries.forEach { e ->
                put(JSONObject().put("day", e.day).put("period", e.period).put("start", e.startTime).put("end", e.endTime)
                    .put("class", e.className).put("subject", e.subjectCode)
                    .put("nameAr", e.subjectNameAr).put("nameFr", e.subjectNameFr))
            }
        })

    fun fileName(): String {
        val raw = "جدول الأستاذ $teacherName - $schoolName - Emploi du temps - $schoolYear"
        return raw.replace(Regex("""[\\/:*?"<>|\r\n\t]"""), " ").replace(Regex("\\s+"), " ").trim().take(150) + ".dsq"
    }

    fun toEntities(now: Long): List<TeacherTimetableEntity> = entries.map { e ->
        TeacherTimetableEntity(
            id = "$institutionId|$schoolYear|${e.day}|${e.period}",
            institutionId = institutionId, schoolName = schoolName, schoolYear = schoolYear, teacherName = teacherName,
            day = e.day, period = e.period, startTime = e.startTime, endTime = e.endTime, className = e.className,
            subjectCode = e.subjectCode, subjectNameAr = e.subjectNameAr, subjectNameFr = e.subjectNameFr, receivedAt = now
        )
    }

    companion object {
        fun fromJson(json: JSONObject): TimetableFile {
            try {
                val i = json.getJSONObject("institution")
                val arr = json.getJSONArray("entries")
                return TimetableFile(
                    institutionId = i.getString("id"),
                    schoolName = i.optString("schoolName"),
                    wilaya = i.optString("wilaya"),
                    schoolYear = json.getString("schoolYear"),
                    teacherName = json.optString("teacherName"),
                    entries = (0 until arr.length()).map { k ->
                        val e = arr.getJSONObject(k)
                        TimetableFileEntry(
                            day = e.getInt("day").also { require(it in 0..6) },
                            period = e.getInt("period"),
                            startTime = e.optString("start"), endTime = e.optString("end"),
                            className = e.getString("class"), subjectCode = e.optString("subject"),
                            subjectNameAr = e.optString("nameAr"), subjectNameFr = e.optString("nameFr")
                        )
                    }
                )
            } catch (e: Exception) {
                throw InvalidClassFileException()
            }
        }
    }
}
