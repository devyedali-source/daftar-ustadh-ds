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

    @Query("SELECT * FROM student WHERE classId = :classId")
    suspend fun listByClass(classId: String): List<StudentEntity>

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
