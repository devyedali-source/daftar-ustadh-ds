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
        val hasGrades: Boolean,
        /** طالب السجل ← طالب القسم عندك (بالرقم الخفي، وإلا برقم النداء). */
        val mapping: Map<String, String> = emptyMap(),
        /** عدد الطلاب الذين طوبقوا برقم النداء لأن رقمهم الخفي تغيّر (طالب أُعيد تسجيله). */
        val byRoll: Int = 0,
        /** القسم نفسه عرفناه باسمه (المؤسسة والسنة والقسم) لأن رمزه تغيّر (أُعيد إنشاؤه أو أُعيد تثبيت التطبيق). */
        val classByName: Boolean = false,
        /** السجل أقدم من سجل هذه المادة الذي رُفع من قبل (أُرسل قبله): رفعه يُرجع نقاطًا قديمة. */
        val older: Boolean = false,
        /** آخر فصل في السجل (الفصل الذي يخصه). */
        val term: Int = 1,
        /** سجل من الوضع الحر: عدد التلاميذ مطابق، فطوبقوا حسب ترتيبهم في القائمة دون أرقام النداء أو الأسماء. */
        val byOrder: Boolean = false
    ) : RecordCheck()
}

class RecordImporter(private val db: DaftarDatabase) {

    private fun norm(t: String) = t.trim().replace(Regex("\\s+"), " ").lowercase()

    suspend fun check(file: RecordFile): RecordCheck {
        val byId = db.classDao().getById(file.classId)
        // التحقق: بصمة القسم تتضمن المؤسسة والسنة والمستوى والرقم؛ ويُتحقق منها صراحةً أيضًا
        val idOk = byId != null && byId.institutionId == file.institutionId && byId.schoolYear == file.schoolYear &&
            byId.levelCode == file.levelCode && byId.number == file.number
        var classByName = false
        val c: ClassEntity = if (idOk) byId!! else {
            // رمز القسم لا يوافق (أُعيد إنشاء القسم أو أُعيد تثبيت تطبيق الإدارة بعد إرسال الملفات للأساتذة):
            // يُعرف القسم باسمه في مؤسستك: نفس اسم المؤسسة والسنة والمستوى والرقم، ثم يُطابق الطلاب بأرقام النداء
            val own = db.institutionDao().getOwn() ?: return RecordCheck.WrongClass(file)
            // المؤسسة نفسها: نفس رمزها في لائحة المدارس، أو نفس الاسم والولاية (سجل الوضع الحر)
            val sameList = own.schoolListId != null && own.schoolListId == file.schoolListId
            val sameName = norm(own.schoolName) == norm(file.schoolName) && norm(own.wilaya) == norm(file.wilaya)
            if (!sameList && !sameName) return RecordCheck.WrongClass(file)
            val slot = db.classDao().findSlot(own.id, file.schoolYear, file.levelCode, file.number)
                ?: return RecordCheck.WrongClass(file)
            classByName = true
            slot
        }
        if (!classByName) {
            val owner = db.institutionDao().getById(c.institutionId)
            if (owner == null ||
                (owner.schoolListId != null && file.schoolListId != null && owner.schoolListId != file.schoolListId)
            ) return RecordCheck.WrongClass(file)
        }
        if (!c.managed) return RecordCheck.NotManaged(file)

        val students = db.studentDao().listByClass(c.id)
        val ids = students.map { it.id }.toSet()
        // المطابقة: بالرقم الخفي أولًا؛ ومن بقي يُطابق برقم النداء (ثابت طوال السنة) إن كان ذلك الرقم لم يُطابق بعد
        val mapping = HashMap<String, String>()
        file.students.filter { it.id in ids }.forEach { mapping[it.id] = it.id }
        val usedTargets = mapping.values.toMutableSet()
        val byRollNumber = students.filter { it.id !in usedTargets }.associateBy { it.rollNumber }
        var byRoll = 0
        file.students.filter { it.id !in mapping && it.roll > 0 }.forEach { fs ->
            val target = byRollNumber[fs.roll]
            if (target != null && target.id !in usedTargets) { mapping[fs.id] = target.id; usedTargets += target.id; byRoll++ }
        }
        // الوضع الحر (القسم عُرف باسمه): إن كان عدد التلاميذ مطابقًا يُطابقون حسب ترتيبهم في القائمة،
        // ولا يهم اختلاف أرقام النداء أو الأسماء
        var byOrder = false
        // لا تُستعمل إلا إذا لم يُطابق أي تلميذ برقمه الخفي ولا برقم النداء: المطابقة الجزئية برقم النداء أدق،
        // والمطابقة بالترتيب فوقها قد تُزيح نقاط التلاميذ إن اختلف ترتيب القائمتين
        if (classByName && file.students.isNotEmpty() && file.students.size == students.size && mapping.isEmpty()) {
            mapping.clear(); usedTargets.clear(); byRoll = 0
            file.students.sortedBy { it.roll }.zip(students.sortedBy { it.rollNumber }).forEach { (fs, st) ->
                mapping[fs.id] = st.id; usedTargets += st.id
            }
            byOrder = true
        }
        val lastSent = db.appMetaDao().get(exportedKey(c.id, file.subjectCode))?.toLongOrNull() ?: 0L
        return RecordCheck.Ready(
            older = file.exportedAt > 0 && file.exportedAt < lastSent,
            term = termOf(file),
            file = file,
            schoolClass = c,
            matched = mapping.size,
            unknownStudents = file.students.filter { it.id !in mapping }.map { "${it.roll}. ${it.name}" },
            missingStudents = students.filter { it.id !in usedTargets }.sortedBy { it.rollNumber }
                .map { "${it.rollNumber}. ${it.nameAr.ifBlank { it.nameFr }}" },
            alreadyUploaded = db.gradeDao().getRecord(c.id, file.subjectCode),
            hasGrades = db.gradeDao().countGrades(c.id, file.subjectCode) > 0,
            mapping = mapping,
            byRoll = byRoll,
            classByName = classByName,
            byOrder = byOrder
        )
    }

    private fun exportedKey(classId: String, subject: String) = "record_exported|$classId|$subject"

    /** آخر فصل وصلت امتحاناته في السجل. */
    fun termOf(file: RecordFile): Int =
        (3 downTo 1).firstOrNull { t -> file.students.any { it.grades.containsKey(com.elyedali.daftarustadh.data.GradeSlot.exam(t)) } } ?: 1

    /**
     * يرفع نتائج المادة. السجل يخص فصله فقط:
     * - سجل الفصل الأول: امتحان الفصل الأول.
     * - سجل الفصل الثاني: امتحان الفصل الثاني.
     * - السجل الأخير: امتحان الفصل الثالث والاختبارات.
     * خانات الفصول السابقة في السجل لا تُكتب إلا إذا كانت فارغة عند الإدارة، فلا يُمسّ ما صحّحته الإدارة
     * (مثل الغائب بتبرير من 0 إلى 10) حين يصل سجل الفصل الموالي.
     */
    suspend fun apply(ready: RecordCheck.Ready) {
        val gradeDao = db.gradeDao()
        val c = ready.schoolClass
        val file = ready.file
        val ids = db.studentDao().listByClass(c.id).map { it.id }.toSet()
        val now = System.currentTimeMillis()
        // طالب السجل ← طالب القسم (المطابقة التي عُرضت قبل الرفع)
        val target: (String) -> String? = { fid -> (ready.mapping[fid] ?: fid).takeIf { it in ids } }
        val term = termOf(file)
        val S = com.elyedali.daftarustadh.data.GradeSlot
        // خانات هذا الفصل التي يحلّ فيها السجل محلّ السابق: امتحانه واختباراته (كلها في الفصل الأخير)
        fun owns(slot: String) = slot == S.exam(term) ||
            (S.isTest(slot) && (term >= 3 || S.testTerm(slot) == term))
        val existing = gradeDao.listSubject(c.id, file.subjectCode).groupBy { it.studentId }
            .mapValues { e -> e.value.associateBy { it.slot } }
        // نقطة صحّحتها الإدارة بيدها بعد رفع السجل السابق (مثل الغائب بتبرير من 0 إلى 10):
        // لا يمحوها سجل يصل بعد ذلك من الأستاذ، حتى لو أعاد إرسال سجل الفصل نفسه
        // (السجل السابق من أستاذ؛ أما الإدخال اليدوي في الإدارة فيحلّ محله سجل الأستاذ حين يصل)
        val prevReceived = gradeDao.getRecord(c.id, file.subjectCode)?.takeIf { it.teacherName.isNotBlank() }?.receivedAt
        fun editedByAdmin(g: GradeEntity?) = g != null && prevReceived != null && g.updatedAt > prevReceived
        file.students.forEach { s ->
            val sid = target(s.id) ?: return@forEach
            val mine = existing[sid].orEmpty()
            // خانات هذا الفصل: تحلّ محلّ السابق (وتُفرَّغ إن لم تكن في السجل)
            (mine.keys + S.exam(term)).filter { owns(it) && it !in s.grades && !editedByAdmin(mine[it]) }
                .forEach { slot -> gradeDao.clear(sid, file.subjectCode, slot) }
            s.grades.forEach { (slot, v) ->
                if ((owns(slot) && !editedByAdmin(mine[slot])) || mine[slot] == null)
                    gradeDao.save(GradeEntity(sid, c.id, file.subjectCode, slot, v, now, s.marks[slot]))
            }
        }
        if (file.exportedAt > 0) {
            val key = exportedKey(c.id, file.subjectCode)
            val prev = db.appMetaDao().get(key)?.toLongOrNull() ?: 0L
            if (file.exportedAt > prev) db.appMetaDao().put(com.elyedali.daftarustadh.data.AppMetaEntity(key, file.exportedAt.toString()))
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
