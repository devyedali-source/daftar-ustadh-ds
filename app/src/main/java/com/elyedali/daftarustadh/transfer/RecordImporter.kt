package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.SubjectRecordEntity
import com.elyedali.daftarustadh.data.TestsMode

/** نتيجة فحص سجل أستاذ قبل رفعه عند الإدارة. */
sealed class RecordCheck {
    /** القسم غير موجود عندك: السجل من مؤسسة أو قسم أو سنة أخرى. */
    data class WrongClass(val file: RecordFile) : RecordCheck()
    /** القسم موجود عندك لكن ليس في وضع الإدارة. */
    data class NotManaged(val file: RecordFile) : RecordCheck()
    data class Ready(
        val file: RecordFile,
        val schoolClass: ClassEntity,
        val matched: Int,
        val unknownStudents: List<String>,   // في السجل وليسوا في القسم عندك
        val missingStudents: List<String>,   // في القسم عندك وليسوا في السجل
        val alreadyUploaded: SubjectRecordEntity?,
        val hasGrades: Boolean
    ) : RecordCheck()
}

class RecordImporter(private val db: DaftarDatabase) {

    suspend fun check(file: RecordFile): RecordCheck {
        val c = db.classDao().getById(file.classId)
        // التحقق: بصمة القسم تتضمن المؤسسة والسنة والمستوى والرقم؛ ويُتحقق منها صراحةً أيضًا
        if (c == null || c.institutionId != file.institutionId || c.schoolYear != file.schoolYear ||
            c.levelCode != file.levelCode || c.number != file.number
        ) return RecordCheck.WrongClass(file)
        val owner = db.institutionDao().getById(c.institutionId)
        if (owner == null || owner.wilaya != file.wilaya ||
            (owner.schoolListId != null && file.schoolListId != null && owner.schoolListId != file.schoolListId)
        ) return RecordCheck.WrongClass(file)
        if (!c.managed) return RecordCheck.NotManaged(file)

        val students = db.studentDao().listByClass(c.id)
        val ids = students.map { it.id }.toSet()
        val fileIds = file.students.map { it.id }.toSet()
        return RecordCheck.Ready(
            file = file,
            schoolClass = c,
            matched = file.students.count { it.id in ids },
            unknownStudents = file.students.filter { it.id !in ids }.map { "${it.roll}. ${it.name}" },
            missingStudents = students.filter { it.id !in fileIds }.sortedBy { it.rollNumber }
                .map { "${it.rollNumber}. ${it.nameAr.ifBlank { it.nameFr }}" },
            alreadyUploaded = db.gradeDao().getRecord(c.id, file.subjectCode),
            hasGrades = db.gradeDao().countGrades(c.id, file.subjectCode) > 0
        )
    }

    /** يرفع نتائج المادة: نتائج كل طالب في السجل تحلّ محلّ نتائجه السابقة في هذه المادة. */
    suspend fun apply(ready: RecordCheck.Ready) {
        val gradeDao = db.gradeDao()
        val c = ready.schoolClass
        val file = ready.file
        val ids = db.studentDao().listByClass(c.id).map { it.id }.toSet()
        val now = System.currentTimeMillis()
        // سجل الفصل الأول أو الثاني لا يحمل الاختبارات: تُستبدل الامتحانات فقط ولا تُمسّ بقية الخانات.
        // السجل الأخير (فيه اختبارات) يحلّ محلّ كل نتائج المادة.
        val hasTests = file.students.any { s -> s.grades.keys.any { !it.startsWith("E") } }
        file.students.filter { it.id in ids }.forEach { s ->
            if (hasTests) gradeDao.clearStudentSubject(c.id, file.subjectCode, s.id)
            else com.elyedali.daftarustadh.data.GradeSlot.ALL.filter { it.startsWith("E") }
                .forEach { slot -> gradeDao.clear(s.id, file.subjectCode, slot) }
            s.grades.forEach { (slot, v) -> gradeDao.save(GradeEntity(s.id, c.id, file.subjectCode, slot, v, now, s.marks[slot])) }
        }
        gradeDao.saveRecord(
            SubjectRecordEntity(
                classId = c.id,
                subjectCode = file.subjectCode,
                teacherName = file.teacherName,
                testsMode = file.testsMode.ifBlank { TestsMode.AVERAGE },
                receivedAt = now
            )
        )
    }
}
