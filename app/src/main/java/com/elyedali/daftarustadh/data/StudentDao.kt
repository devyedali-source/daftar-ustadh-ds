package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student WHERE classId = :classId ORDER BY rollNumber, nameAr")
    fun observeByClass(classId: String): Flow<List<StudentEntity>>

    /** طلاب عدة أقسام (مركز الغياب في الإدارة). */
    @Query("SELECT * FROM student WHERE classId IN (:classIds) ORDER BY rollNumber")
    fun observeByClasses(classIds: List<String>): Flow<List<StudentEntity>>

    @Query("SELECT * FROM student WHERE classId = :classId")
    suspend fun listByClass(classId: String): List<StudentEntity>

    /** عدد الطلاب لكل قسم وجنس، للإحصائيات. */
    @Query("SELECT classId, gender, COUNT(*) AS count FROM student GROUP BY classId, gender")
    fun observeGenderCounts(): Flow<List<ClassGenderCount>>

    @Query("SELECT * FROM student WHERE id = :id")
    suspend fun getById(id: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(s: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(list: List<StudentEntity>)

    @Delete
    suspend fun delete(s: StudentEntity)

    @Query("DELETE FROM student WHERE classId = :classId")
    suspend fun deleteByClass(classId: String)
}

data class ClassGenderCount(val classId: String, val gender: String, val count: Int)
