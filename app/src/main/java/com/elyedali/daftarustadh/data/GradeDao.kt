package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grade WHERE classId = :classId AND subjectCode = :subjectCode")
    fun observe(classId: String, subjectCode: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grade WHERE classId = :classId")
    fun observeClass(classId: String): Flow<List<GradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(g: GradeEntity)

    @Query("DELETE FROM grade WHERE studentId = :studentId AND subjectCode = :subjectCode AND slot = :slot")
    suspend fun clear(studentId: String, subjectCode: String, slot: String)

    @Query("DELETE FROM grade WHERE classId = :classId")
    suspend fun deleteByClass(classId: String)

    @Query("DELETE FROM grade WHERE studentId = :studentId")
    suspend fun deleteByStudent(studentId: String)

    // ---- مواد الأستاذ في القسم ----

    @Query("SELECT * FROM teacher_subject WHERE classId = :classId")
    fun observeTeacherSubjects(classId: String): Flow<List<TeacherSubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTeacherSubject(t: TeacherSubjectEntity)

    /** يضيف المادة للأستاذ دون المساس بإعداداتها إن كانت موجودة. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTeacherSubject(t: TeacherSubjectEntity)

    @Query("DELETE FROM teacher_subject WHERE classId = :classId AND subjectCode = :subjectCode")
    suspend fun removeTeacherSubject(classId: String, subjectCode: String)

    @Query("DELETE FROM teacher_subject WHERE classId = :classId")
    suspend fun deleteTeacherSubjectsByClass(classId: String)

    // ---- سجلات المواد المرفوعة عند الإدارة ----

    @Query("SELECT * FROM subject_record WHERE classId = :classId")
    fun observeRecords(classId: String): Flow<List<SubjectRecordEntity>>

    @Query("SELECT * FROM subject_record WHERE classId = :classId AND subjectCode = :subjectCode")
    suspend fun getRecord(classId: String, subjectCode: String): SubjectRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(r: SubjectRecordEntity)

    @Query("DELETE FROM subject_record WHERE classId = :classId")
    suspend fun deleteRecordsByClass(classId: String)

    @Query("SELECT COUNT(*) FROM grade WHERE classId = :classId AND subjectCode = :subjectCode")
    suspend fun countGrades(classId: String, subjectCode: String): Int

    @Query("DELETE FROM grade WHERE classId = :classId AND subjectCode = :subjectCode AND studentId = :studentId")
    suspend fun clearStudentSubject(classId: String, subjectCode: String, studentId: String)

    /** نتائج عدة أقسام (فحص ما ينقص بعد رفع ملفات النتائج دفعة واحدة). */
    @Query("SELECT * FROM grade WHERE classId IN (:classIds)")
    suspend fun listClasses(classIds: List<String>): List<GradeEntity>

    @Query("SELECT * FROM grade WHERE classId = :classId AND subjectCode = :subjectCode")
    suspend fun listSubject(classId: String, subjectCode: String): List<GradeEntity>
}
