package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstitutionDao {
    @Query("SELECT * FROM institution ORDER BY createdAt LIMIT 1")
    fun observe(): Flow<InstitutionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(institution: InstitutionEntity)
}
