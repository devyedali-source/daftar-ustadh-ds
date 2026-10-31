package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.data.StudentEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/** طالب كما في سجل الأستاذ: الرقم الخفي، ورقم النداء والاسم لعرض الفروق إن وُجدت. */
data class RecordStudent(val id: String, val roll: Int, val name: String, val grades: Map<String, Double>)

/**
 * سجل نتائج مادة يرسله الأستاذ إلى المكلّف بالقسم (ملف ‎.dsq).
 * يحمل بصمة المؤسسة وبصمة القسم كما وصلتا في ملف القسم، فيُتحقق منهما عند الرفع.
 */
data class RecordFile(
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
    val testsMode: String,
    val students: List<RecordStudent>
) {
    val className: String get() = "$levelCode$number"
}

/** يقرأ أي ملف ‎.dsq: ملف قسم، أو سجل نتائج أستاذ، أو سجل غياب أستاذ. */
object TransferCodec {
    private const val FORMAT = "daftar-ustadh-ds"
    const val TYPE_RECORD = "teacher_record"
    const val TYPE_ABSENCE = "teacher_absence"

    private fun gzip(json: JSONObject): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
        return out.toByteArray()
    }

    /** يرجع ClassFile أو RecordFile أو AbsenceFile، أو يرمي InvalidClassFileException. */
    fun decodeAny(bytes: ByteArray): Any {
        val json = try {
            JSONObject(GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes() }.toString(Charsets.UTF_8))
        } catch (e: Exception) {
            throw InvalidClassFileException()
        }
        if (json.optString("format") != FORMAT) throw InvalidClassFileException()
        return when (json.optString("type")) {
            TYPE_RECORD -> decodeRecord(json)
            TYPE_ABSENCE -> AbsenceFile.fromJson(json)
            else -> ClassFileCodec.decode(bytes)
        }
    }

    fun encodeAbsence(a: AbsenceFile): ByteArray = gzip(a.toJson(FORMAT, TYPE_ABSENCE))

    fun encodeRecord(r: RecordFile): ByteArray = gzip(
        JSONObject()
            .put("format", FORMAT).put("version", 1).put("type", TYPE_RECORD)
            .put("exportedAt", System.currentTimeMillis())
            .put("institution", JSONObject()
                .put("id", r.institutionId).put("wilaya", r.wilaya).put("schoolName", r.schoolName)
                .put("schoolListId", r.schoolListId ?: JSONObject.NULL))
            .put("class", JSONObject()
                .put("id", r.classId).put("schoolYear", r.schoolYear)
                .put("levelCode", r.levelCode).put("number", r.number))
            .put("subject", JSONObject()
                .put("code", r.subjectCode).put("nameAr", r.subjectNameAr).put("nameFr", r.subjectNameFr))
            .put("teacherName", r.teacherName)
            .put("testsMode", r.testsMode)
            .put("students", JSONArray().apply {
                r.students.forEach { s ->
                    val g = JSONObject()
                    s.grades.forEach { (slot, v) -> g.put(slot, v) }
                    put(JSONObject().put("id", s.id).put("roll", s.roll).put("name", s.name).put("grades", g))
                }
            })
    )

    private fun decodeRecord(json: JSONObject): RecordFile {
        try {
            val i = json.getJSONObject("institution")
            val c = json.getJSONObject("class")
            val sub = json.getJSONObject("subject")
            val arr = json.getJSONArray("students")
            val students = (0 until arr.length()).map { k ->
                val s = arr.getJSONObject(k)
                val g = s.getJSONObject("grades")
                val grades = GradeSlot.ALL.filter { g.has(it) }.associateWith { g.getDouble(it) }
                    .filterValues { it in 0.0..20.0 }
                RecordStudent(s.getString("id"), s.optInt("roll"), s.optString("name"), grades)
            }
            return RecordFile(
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
                testsMode = json.optString("testsMode"),
                students = students
            )
        } catch (e: Exception) {
            throw InvalidClassFileException()
        }
    }

    fun buildRecordStudents(students: List<StudentEntity>, grades: List<GradeEntity>): List<RecordStudent> {
        val by = grades.groupBy { it.studentId }
        return students.sortedBy { it.rollNumber }.map { s ->
            RecordStudent(s.id, s.rollNumber, s.nameAr.ifBlank { s.nameFr }, by[s.id].orEmpty().associate { it.slot to it.value })
        }
    }

    /** اسم كامل بالعربية والفرنسية حتى يعرف المتلقي محتواه من الواتساب. */
    fun recordFileName(r: RecordFile): String {
        val raw = "سجل ${r.subjectNameAr} - القسم ${r.className} - الأستاذ ${r.teacherName} - " +
            "Relevé ${r.subjectNameFr} - ${r.className} - ${r.schoolYear}"
        return raw.replace(Regex("""[\\/:*?"<>|\r\n\t]"""), " ").replace(Regex("\\s+"), " ").trim().take(150) + ".dsq"
    }
}
