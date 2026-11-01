package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.data.TeacherSubjectEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * حزمة سجلات الأستاذ: ترسلها الإدارة لكل أستاذ عبر واتساب.
 * تحمل كل أقسامه (لوائح الطلاب) مع المواد التي يدرّسها في كل قسم،
 * فيجد عند فتحها سجلات النتائج جاهزة للرصد.
 */
data class TeacherBundle(
    val teacherName: String,
    val schoolName: String,
    val schoolYear: String,
    val classes: List<Pair<ClassFile, List<String>>>   // (ملف القسم للأساتذة، رموز المواد)
) {
    fun toJson(format: String, type: String): JSONObject = JSONObject()
        .put("format", format).put("version", 1).put("type", type)
        .put("exportedAt", System.currentTimeMillis())
        .put("teacherName", teacherName)
        .put("schoolName", schoolName)
        .put("schoolYear", schoolYear)
        .put("classes", JSONArray().apply {
            classes.forEach { (file, subjects) ->
                put(JSONObject().put("file", ClassFileCodec.toJson(file)).put("subjects", JSONArray(subjects)))
            }
        })

    /** اسم الملف بالعربية والفرنسية ليفهمه الأستاذ في واتساب. */
    fun fileName(): String {
        val safe = "$teacherName - $schoolName - $schoolYear".replace(Regex("[\\\\/:*?\"<>|]"), " ")
        return "سجلات النتائج - Registres de notes - $safe.dsq"
    }

    companion object {
        fun fromJson(json: JSONObject): TeacherBundle {
            val arr = json.getJSONArray("classes")
            val classes = (0 until arr.length()).map { k ->
                val o = arr.getJSONObject(k)
                val s = o.getJSONArray("subjects")
                ClassFileCodec.fromJson(o.getJSONObject("file")) to (0 until s.length()).map { s.getString(it) }
            }
            if (classes.isEmpty()) throw InvalidClassFileException()
            return TeacherBundle(json.optString("teacherName"), json.optString("schoolName"), json.optString("schoolYear"), classes)
        }
    }
}

/** نتيجة فتح الحزمة: الأقسام المضافة، والأقسام المرفوضة لأنها موجودة من مصدر آخر. */
data class BundleOutcome(val saved: List<String>, val conflicts: List<String>, val subjects: Int)

class TeacherBundleImporter(private val db: DaftarDatabase) {
    private val classImporter = ClassFileImporter(db)

    suspend fun save(bundle: TeacherBundle): BundleOutcome {
        val saved = ArrayList<String>()
        val conflicts = ArrayList<String>()
        var subjects = 0
        bundle.classes.forEach { (file, codes) ->
            when (val r = classImporter.save(file)) {
                is ImportOutcome.Success -> {
                    saved += r.className
                    codes.forEach { db.gradeDao().addTeacherSubject(TeacherSubjectEntity(file.schoolClass.id, it)); subjects++ }
                }
                is ImportOutcome.ConflictingSource -> conflicts += r.className
            }
        }
        return BundleOutcome(saved, conflicts, subjects)
    }
}
