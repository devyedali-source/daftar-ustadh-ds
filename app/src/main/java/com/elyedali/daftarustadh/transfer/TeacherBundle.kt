package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.data.GradeEntity
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
    val classes: List<Pair<ClassFile, List<String>>>,  // (ملف القسم للأساتذة، رموز المواد)
    val timetable: TimetableFile? = null               // جدول الأستاذ المعتمد
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
        .apply { timetable?.let { put("timetable", it.toJson(format, TransferCodec.TYPE_TIMETABLE)) } }

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
            val tt = json.optJSONObject("timetable")?.let { TimetableFile.fromJson(it) }
            return TeacherBundle(json.optString("teacherName"), json.optString("schoolName"), json.optString("schoolYear"), classes, tt)
        }
    }
}

/** نتيجة فتح الحزمة: الأقسام المضافة، والأقسام المرفوضة لأنها موجودة من مصدر آخر. */
data class BundleOutcome(val saved: List<String>, val conflicts: List<String>, val subjects: Int, val periods: Int)

class TeacherBundleImporter(private val db: DaftarDatabase) {
    companion object {
        fun nameKey(institutionId: String) = "bundle_teacher_name|$institutionId"
    }
    private val classImporter = ClassFileImporter(db)

    private fun norm(t: String) = t.trim().replace(Regex("\\s+"), " ").lowercase()

    /**
     * قسم وصل من قبل بنفس المؤسسة (بالاسم) والسنة والمستوى والرقم لكن برمز آخر:
     * الإدارة أعادت إنشاء القسم أو أعادت تثبيت تطبيقها. يُستبدل القديم بالجديد، وتُنقل نقاط الأستاذ
     * من طلاب القسم القديم إلى طلاب الجديد بأرقام النداء، فلا يُرفض الملف ولا يضيع ما رصده.
     */
    private suspend fun replaceOld(file: ClassFile): List<GradeEntity> {
        val incoming = file.schoolClass
        val olds = db.classDao().findTeaching(incoming.schoolYear, incoming.levelCode, incoming.number).filter { old ->
            old.id != incoming.id && (old.institutionId == incoming.institutionId ||
                db.institutionDao().getById(old.institutionId)?.let {
                    norm(it.schoolName) == norm(file.institution.schoolName) && norm(it.wilaya) == norm(file.institution.wilaya) } == true)
        }
        if (olds.isEmpty()) return emptyList()
        val newByRoll = file.students.associateBy { it.rollNumber }
        val moved = ArrayList<GradeEntity>()
        olds.forEach { old ->
            val oldStudents = db.studentDao().listByClass(old.id).associateBy { it.id }
            db.gradeDao().listClasses(listOf(old.id)).forEach { g ->
                val roll = oldStudents[g.studentId]?.rollNumber ?: return@forEach
                val to = newByRoll[roll] ?: return@forEach
                moved += g.copy(studentId = to.id, classId = incoming.id)
            }
            db.documentDao().moveClass(old.id, incoming.id)
            db.gradeDao().deleteByClass(old.id)
            db.gradeDao().deleteRecordsByClass(old.id)
            db.gradeDao().deleteTeacherSubjectsByClass(old.id)
            db.studentDao().deleteByClass(old.id)
            db.absenceDao().deleteAbsencesByClass(old.id)
            db.absenceDao().deleteSessionsByClass(old.id)
            db.classDao().delete(old)
            // جدول المؤسسة القديمة (برمزها القديم) يُحذف، فيحلّ محله جدول الملف الجديد
            if (old.institutionId != incoming.institutionId)
                db.timetableDao().clearTeacherTimetable(old.institutionId, old.schoolYear)
        }
        return moved
    }

    suspend fun save(bundle: TeacherBundle): BundleOutcome {
        val saved = ArrayList<String>()
        val conflicts = ArrayList<String>()
        var subjects = 0
        bundle.classes.forEach { (file, codes) ->
            val moved = replaceOld(file)
            when (val r = classImporter.save(file)) {
                is ImportOutcome.Success -> {
                    saved += r.className
                    codes.forEach { db.gradeDao().addTeacherSubject(TeacherSubjectEntity(file.schoolClass.id, it)); subjects++ }
                    // مادة سحبتها الإدارة منه في هذا القسم (أسندتها لأستاذ آخر): تُزال من قائمته فلا يرسل سجلها
                    db.gradeDao().listTeacherSubjects(file.schoolClass.id).filter { it.subjectCode !in codes }
                        .forEach { db.gradeDao().removeTeacherSubject(it.classId, it.subjectCode) }
                    moved.forEach { db.gradeDao().save(it) }
                }
                is ImportOutcome.ConflictingSource -> conflicts += r.className
            }
        }
        // اسم الأستاذ كما سجّلته إدارة هذه المؤسسة: يُكتب في سجلاته التي يرسلها إليها، فتتطابق مع إسنادها
        if (bundle.teacherName.isNotBlank()) bundle.classes.map { it.first.institution.id }.distinct().forEach { id ->
            db.appMetaDao().put(com.elyedali.daftarustadh.data.AppMetaEntity(nameKey(id), bundle.teacherName))
        }
        // جدول الأستاذ: يحلّ محلّ جدوله السابق من نفس المؤسسة والسنة
        val tt = bundle.timetable
        if (tt != null) db.timetableDao().replaceTeacherTimetable(tt.institutionId, tt.schoolYear, tt.toEntities(System.currentTimeMillis()))
        return BundleOutcome(saved, conflicts, subjects, tt?.entries?.size ?: 0)
    }
}
