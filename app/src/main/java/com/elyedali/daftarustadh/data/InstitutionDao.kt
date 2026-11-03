package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstitutionDao {
    /** المؤسسة التي سجّلها المستخدم بنفسه (وضع الإدارة). */
    @Query("SELECT * FROM institution WHERE isOwn = 1 ORDER BY createdAt LIMIT 1")
    fun observe(): Flow<InstitutionEntity?>

    /** كل المؤسسات: مؤسسته، والمؤسسات التي وصلته أقسامها في ملفات. */
    @Query("SELECT * FROM institution")
    fun observeAll(): Flow<List<InstitutionEntity>>

    @Query("SELECT * FROM institution WHERE isOwn = 1 ORDER BY createdAt LIMIT 1")
    suspend fun getOwn(): InstitutionEntity?

    @Query("SELECT COUNT(*) FROM institution WHERE isOwn = 1")
    suspend fun ownCount(): Int

    @Query("SELECT * FROM institution WHERE id = :id")
    suspend fun getById(id: String): InstitutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(institution: InstitutionEntity)
}
