package com.elyedali.daftarustadh.transfer

import com.elyedali.daftarustadh.data.DaftarDatabase

sealed class ImportOutcome {
    data class Success(val className: String, val type: ClassFileType, val added: Int, val updated: Int) : ImportOutcome()
    /** نفس القسم (نفس المؤسسة والسنة والمستوى والرقم) موجود عندك من مصدر آخر. */
    data class ConflictingSource(val className: String) : ImportOutcome()
}

/**
 * يحفظ ملف قسم وصل من زميل أو من الإدارة.
 * - لا يحذف أي طالب: الطالب الموجود يُحدَّث، والجديد يُضاف.
 * - ملف الأساتذة لا يمحو الرقم المدرسي ولا الرقم الوطني ولا الهواتف إن كانت موجودة عندك.
 * - أول ملف يصل لقسم هو مرجعه: إن وصل نفس القسم ببصمة مختلفة يُرفض.
 */
class ClassFileImporter(private val db: DaftarDatabase) {

    suspend fun save(file: ClassFile): ImportOutcome {
        val institutionDao = db.institutionDao()
        val classDao = db.classDao()
        val studentDao = db.studentDao()
        val incoming = file.schoolClass

        // مؤسسة القسم: تُضاف إن لم تكن موجودة، ولا تُمسّ مؤسسة المستخدم نفسه
        val known = institutionDao.getById(file.institution.id)
        if (known == null) institutionDao.save(file.institution)
        else if (!known.isOwn) institutionDao.save(file.institution.copy(createdAt = known.createdAt))

        val existing = classDao.getById(incoming.id)
        if (existing == null) {
            val slot = classDao.findSlot(incoming.institutionId, incoming.schoolYear, incoming.levelCode, incoming.number)
            if (slot != null) return ImportOutcome.ConflictingSource(incoming.name)
            classDao.insert(incoming)
        } else {
            classDao.update(
                existing.copy(
                    managed = existing.managed || incoming.managed,
                    teaching = existing.teaching || incoming.teaching
                )
            )
        }

        var added = 0
        var updated = 0
        for (s in file.students) {
            val old = studentDao.getById(s.id)
            if (old == null) {
                studentDao.save(s); added++
            } else {
                val merged = if (file.type == ClassFileType.FULL) {
                    s.copy(createdAt = old.createdAt)
                } else {
                    old.copy(rollNumber = s.rollNumber, nameAr = s.nameAr, nameFr = s.nameFr)
                }
                if (merged != old) { studentDao.save(merged); updated++ }
            }
        }
        return ImportOutcome.Success(incoming.name, file.type, added, updated)
    }
}
