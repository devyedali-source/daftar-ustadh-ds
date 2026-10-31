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

    @Query("DELETE FROM teacher_subject WHERE classId = :classId AND subjectCode = :subjectCode")
    suspend fun removeTeacherSubject(classId: String, subjectCode: String)

    @Query("DELETE FROM teacher_subject WHERE classId = :classId")
    suspend fun deleteTeacherSubjectsByClass(classId: String)
}
