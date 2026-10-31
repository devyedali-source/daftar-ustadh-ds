package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.InstitutionEntity
import com.elyedali.daftarustadh.data.StudentEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * ملفات الأقسام المتبادلة عبر الواتساب أو البلوتوث (امتداد ‎.dsq، مضغوطة).
 * - TEACHER: ملف القسم للأساتذة — رقم النداء والاسمان فقط (+ الرقم الخفي لكل طالب).
 * - FULL: ملف معلومات القسم الكامل — كل معلومات الطلاب التي تحتاجها الكشوف.
 * يحمل الملف بصمة المؤسسة وبصمة القسم كما هما، فتتطابق السجلات عند كل من يفتحه.
 */
enum class ClassFileType(val code: String) { TEACHER("class_teacher"), FULL("class_full") }

data class ClassFile(
    val type: ClassFileType,
    val institution: InstitutionEntity,
    val schoolClass: ClassEntity,
    val students: List<StudentEntity>
)

class InvalidClassFileException : Exception()

object ClassFileCodec {
    private const val FORMAT = "daftar-ustadh-ds"
    private const val VERSION = 1

    fun encode(file: ClassFile): ByteArray {
        val i = file.institution
        val c = file.schoolClass
        val json = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("type", file.type.code)
            .put("exportedAt", System.currentTimeMillis())
            .put("institution", JSONObject()
                .put("id", i.id).put("wilaya", i.wilaya).put("schoolName", i.schoolName)
                .put("schoolListId", i.schoolListId ?: JSONObject.NULL)
                .put("stage", i.stage).put("schoolYear", i.schoolYear))
            .put("class", JSONObject()
                .put("id", c.id).put("schoolYear", c.schoolYear)
                .put("levelCode", c.levelCode).put("number", c.number))
            .put("students", JSONArray().apply {
                file.students.sortedBy { it.rollNumber }.forEach { s ->
                    val o = JSONObject().put("id", s.id).put("roll", s.rollNumber)
                        .put("nameAr", s.nameAr).put("nameFr", s.nameFr)
                    if (file.type == ClassFileType.FULL) {
                        o.put("rim", s.rim).put("nationalId", s.nationalId)
                            .put("phone1", s.guardianPhone1).put("phone2", s.guardianPhone2)
                    }
                    put(o)
                }
            })
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
        return out.toByteArray()
    }

    fun decode(bytes: ByteArray): ClassFile {
        try {
            val text = GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes() }.toString(Charsets.UTF_8)
            val json = JSONObject(text)
            if (json.optString("format") != FORMAT) throw InvalidClassFileException()
            val type = ClassFileType.entries.firstOrNull { it.code == json.getString("type") }
                ?: throw InvalidClassFileException()
            val io = json.getJSONObject("institution")
            val co = json.getJSONObject("class")
            val now = System.currentTimeMillis()
            val institution = InstitutionEntity(
                id = io.getString("id"),
                wilaya = io.getString("wilaya"),
                schoolName = io.getString("schoolName"),
                schoolListId = if (io.isNull("schoolListId")) null else io.getString("schoolListId"),
                stage = io.getString("stage"),
                schoolYear = io.getString("schoolYear"),
                createdAt = now,
                isOwn = false
            )
            val schoolClass = ClassEntity(
                id = co.getString("id"),
                institutionId = institution.id,
                schoolYear = co.getString("schoolYear"),
                levelCode = co.getString("levelCode"),
                number = co.getInt("number"),
                createdAt = now,
                managed = type == ClassFileType.FULL,
                teaching = type == ClassFileType.TEACHER
            )
            val arr = json.getJSONArray("students")
            val students = (0 until arr.length()).map { k ->
                val s = arr.getJSONObject(k)
                StudentEntity(
                    id = s.getString("id"),
                    classId = schoolClass.id,
                    rollNumber = s.getInt("roll"),
                    nameAr = s.optString("nameAr"),
                    nameFr = s.optString("nameFr"),
                    rim = s.optString("rim"),
                    nationalId = s.optString("nationalId"),
                    guardianPhone1 = s.optString("phone1"),
                    guardianPhone2 = s.optString("phone2"),
                    createdAt = now
                )
            }
            return ClassFile(type, institution, schoolClass, students)
        } catch (e: InvalidClassFileException) {
            throw e
        } catch (e: Exception) {
            throw InvalidClassFileException()
        }
    }

    /**
     * اسم ملف كامل بالعربية والفرنسية حتى يفهمه المتلقي في الواتساب، مثل:
     * «ملف القسم 1AS1 للأساتذة - Fichier de la classe 1AS1 pour les enseignants - إعدادية البنات - 2026-2027.dsq»
     */
    fun fileName(file: ClassFile): String {
        val c = "${file.schoolClass.levelCode}${file.schoolClass.number}"
        val title = when (file.type) {
            ClassFileType.TEACHER -> "ملف القسم $c للأساتذة - Fichier de la classe $c pour les enseignants"
            ClassFileType.FULL -> "ملف معلومات القسم $c الكامل - Fichier complet de la classe $c"
        }
        val raw = "$title - ${file.institution.schoolName} - ${file.schoolClass.schoolYear}"
        // حذف الحروف الممنوعة في أسماء الملفات، وتقصير الاسم الطويل جدًا
        val safe = raw.replace(Regex("""[\\/:*?"<>|\r\n\t]"""), " ").replace(Regex("\\s+"), " ").trim().take(150)
        return "$safe.dsq"
    }
}
