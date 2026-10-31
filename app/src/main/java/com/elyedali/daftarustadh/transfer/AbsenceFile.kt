package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.AbsenceSessionEntity
import com.elyedali.daftarustadh.data.AbsenceSource
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.DaftarDatabase
import org.json.JSONArray
import org.json.JSONObject

/** حصة في ملف الغياب: التاريخ ورقم الحصة والطلاب الغائبون (بأرقامهم الخفية). */
data class AbsenceFileSession(val date: String, val period: Int, val absentIds: List<String>)

/**
 * سجل غياب مادة يرسله الأستاذ إلى الإدارة (ملف ‎.dsq)، بنفس بصمة المؤسسة والقسم
 * التي في سجل النتائج، فيُتحقق منها عند الرفع.
 */
data class AbsenceFile(
    val institutionId: String,
    val wilaya: String,
    val schoolName: String,
    val schoolListId: String?,
    val classId: String,
    val schoolYear: String,
    val levelCode: String,
    val number: Int,
    val subjectCode: String,
    val subjectNameAr: String,
    val subjectNameFr: String,
    val teacherName: String,
    val sessions: List<AbsenceFileSession>
) {
    val className: String get() = "$levelCode$number"

    fun toJson(format: String, type: String): JSONObject = JSONObject()
        .put("format", format).put("version", 1).put("type", type)
        .put("exportedAt", System.currentTimeMillis())
        .put("institution", JSONObject()
            .put("id", institutionId).put("wilaya", wilaya).put("schoolName", schoolName)
            .put("schoolListId", schoolListId ?: JSONObject.NULL))
        .put("class", JSONObject()
            .put("id", classId).put("schoolYear", schoolYear).put("levelCode", levelCode).put("number", number))
        .put("subject", JSONObject().put("code", subjectCode).put("nameAr", subjectNameAr).put("nameFr", subjectNameFr))
        .put("teacherName", teacherName)
        .put("sessions", JSONArray().apply {
            sessions.forEach { s ->
                put(JSONObject().put("date", s.date).put("period", s.period).put("absent", JSONArray(s.absentIds)))
            }
        })

    /** اسم كامل بالعربية والفرنسية حتى يعرف المتلقي محتواه من الواتساب. */
    fun fileName(): String {
        val raw = "غياب ${subjectNameAr} - القسم $className - الأستاذ $teacherName - " +
            "Absences ${subjectNameFr} - $className - $schoolYear"
        return raw.replace(Regex("""[\\/:*?"<>|\r\n\t]"""), " ").replace(Regex("\\s+"), " ").trim().take(150) + ".dsq"
    }

    companion object {
        fun fromJson(json: JSONObject): AbsenceFile {
            try {
                val i = json.getJSONObject("institution")
                val c = json.getJSONObject("class")
                val sub = json.getJSONObject("subject")
                val arr = json.getJSONArray("sessions")
                val sessions = (0 until arr.length()).map { k ->
                    val s = arr.getJSONObject(k)
                    val a = s.getJSONArray("absent")
                    val date = s.getString("date")
                    require(Regex("""\d{4}-\d{2}-\d{2}""").matches(date))
                    AbsenceFileSession(date, s.getInt("period"), (0 until a.length()).map { a.getString(it) })
                }
                return AbsenceFile(
                    institutionId = i.getString("id"),
                    wilaya = i.getString("wilaya"),
                    schoolName = i.getString("schoolName"),
                    schoolListId = if (i.isNull("schoolListId")) null else i.getString("schoolListId"),
                    classId = c.getString("id"),
                    schoolYear = c.getString("schoolYear"),
                    levelCode = c.getString("levelCode"),
                    number = c.getInt("number"),
                    subjectCode = sub.getString("code"),
                    subjectNameAr = sub.optString("nameAr"),
                    subjectNameFr = sub.optString("nameFr"),
                    teacherName = json.optString("teacherName"),
                    sessions = sessions
                )
            } catch (e: Exception) {
                throw InvalidClassFileException()
            }
        }
    }
}

/** نتيجة فحص ملف غياب قبل رفعه عند الإدارة. */
sealed class AbsenceCheck {
    data class WrongClass(val file: AbsenceFile) : AbsenceCheck()
    data class NotManaged(val file: AbsenceFile) : AbsenceCheck()
    data class Ready(
        val file: AbsenceFile,
        val schoolClass: ClassEntity,
        val newSessions: Int,       // حصص لم تكن عندك
        val replacedSessions: Int,  // حصص موجودة عندك وستحلّ محلها نسخة الأستاذ
        val unknownStudents: Int    // غائبون في الملف وليسوا في القسم عندك (يُتجاهلون)
    ) : AbsenceCheck()
}

class AbsenceImporter(private val db: DaftarDatabase) {

    suspend fun check(file: AbsenceFile): AbsenceCheck {
        val c = db.classDao().getById(file.classId)
        if (c == null || c.institutionId != file.institutionId || c.schoolYear != file.schoolYear ||
            c.levelCode != file.levelCode || c.number != file.number
        ) return AbsenceCheck.WrongClass(file)
        val owner = db.institutionDao().getById(c.institutionId)
        if (owner == null || owner.wilaya != file.wilaya ||
            (owner.schoolListId != null && file.schoolListId != null && owner.schoolListId != file.schoolListId)
        ) return AbsenceCheck.WrongClass(file)
        if (!c.managed) return AbsenceCheck.NotManaged(file)

        val existing = db.absenceDao().listSessions(c.id, file.subjectCode).map { it.id }.toSet()
        val ids = db.studentDao().listByClass(c.id).map { it.id }.toSet()
        val fileIds = file.sessions.map { AbsenceSessionEntity.idOf(c.id, file.subjectCode, it.date, it.period) }
        return AbsenceCheck.Ready(
            file = file,
            schoolClass = c,
            newSessions = fileIds.count { it !in existing },
            replacedSessions = fileIds.count { it in existing },
            unknownStudents = file.sessions.flatMap { it.absentIds }.toSet().count { it !in ids }
        )
    }

    /** كل حصة في الملف تحلّ محلّ نفس الحصة عندك؛ الحصص الأخرى لا تُمسّ. */
    suspend fun apply(ready: AbsenceCheck.Ready) {
        val c = ready.schoolClass
        val file = ready.file
        val ids = db.studentDao().listByClass(c.id).map { it.id }.toSet()
        val now = System.currentTimeMillis()
        file.sessions.forEach { s ->
            db.absenceDao().saveSession(
                AbsenceSessionEntity(
                    id = AbsenceSessionEntity.idOf(c.id, file.subjectCode, s.date, s.period),
                    classId = c.id,
                    subjectCode = file.subjectCode,
                    date = s.date,
                    period = s.period,
                    source = AbsenceSource.IMPORT,
                    recordedBy = file.teacherName,
                    updatedAt = now
                ),
                s.absentIds.filter { it in ids }
            )
        }
    }
}
