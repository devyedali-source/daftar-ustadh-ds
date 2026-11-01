package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    /** أقسام وضع الإدارة في سنة دراسية. */
    @Query("SELECT * FROM school_class WHERE managed = 1 AND schoolYear = :schoolYear ORDER BY levelCode, number")
    fun observeManaged(schoolYear: String): Flow<List<ClassEntity>>

    /** أقسام الأستاذ («أقسامي») من كل المؤسسات. */
    @Query("SELECT * FROM school_class WHERE teaching = 1 ORDER BY schoolYear DESC, levelCode, number")
    fun observeTeaching(): Flow<List<ClassEntity>>

    /** كل الأقسام (الإدارة والأستاذ) من كل السنوات — للصفحة الرئيسية. */
    @Query("SELECT * FROM school_class WHERE managed = 1 OR teaching = 1 ORDER BY schoolYear DESC, levelCode, number")
    fun observeAll(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM school_class WHERE id = :id")
    fun observeById(id: String): Flow<ClassEntity?>

    @Query("SELECT * FROM school_class WHERE id = :id")
    suspend fun getById(id: String): ClassEntity?

    @Query("SELECT * FROM school_class WHERE institutionId = :institutionId AND schoolYear = :schoolYear AND levelCode = :levelCode AND number = :number LIMIT 1")
    suspend fun findSlot(institutionId: String, schoolYear: String, levelCode: String, number: Int): ClassEntity?

    /** يرجع -1 إن كان القسم موجودًا من قبل (نفس المؤسسة والسنة والمستوى والرقم). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(c: ClassEntity): Long

    @Update
    suspend fun update(c: ClassEntity)

    @Delete
    suspend fun delete(c: ClassEntity)
}
