package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM school_class WHERE institutionId = :institutionId AND schoolYear = :schoolYear ORDER BY levelCode, number")
    fun observe(institutionId: String, schoolYear: String): Flow<List<ClassEntity>>

    /** يرجع -1 إن كان القسم موجودًا من قبل (نفس المستوى ونفس الرقم). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(c: ClassEntity): Long

    @Delete
    suspend fun delete(c: ClassEntity)
}
