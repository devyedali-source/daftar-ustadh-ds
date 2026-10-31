package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** وثيقة يكتبها الأستاذ في قسم (امتحان، طلب، درس...) ويسحبها PDF بالرأسية وطابعه. */
@Entity(tableName = "document", indices = [Index(value = ["classId"])])
data class DocumentEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val type: String,       // exam | request | lesson | other
    val title: String,
    val body: String,
    val updatedAt: Long
)

object DocType {
    const val EXAM = "exam"
    const val REQUEST = "request"
    const val LESSON = "lesson"
    const val OTHER = "other"
    val ALL = listOf(EXAM, REQUEST, LESSON, OTHER)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM document WHERE classId = :classId ORDER BY updatedAt DESC")
    fun observeByClass(classId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(d: DocumentEntity)

    @Delete
    suspend fun delete(d: DocumentEntity)

    @Query("DELETE FROM document WHERE classId = :classId")
    suspend fun deleteByClass(classId: String)
}
