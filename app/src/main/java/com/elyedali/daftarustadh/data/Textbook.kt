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

/**
 * سطر في دفتر النصوص (للإدارة فقط): التاريخ، التوقيت، المادة، المجال، الموضوع، المدة، التوقيع.
 * التوقيع رسم بالإصبع محفوظ كخطوط (انظر SignatureCodec).
 */
@Entity(tableName = "textbook_entry", indices = [Index("classId")])
data class TextbookEntryEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val date: String,          // yyyy-MM-dd
    val startTime: String,     // HH:mm
    val endTime: String,       // HH:mm
    val subjectCode: String,
    val domain: String,        // المجال
    val topic: String,         // الموضوع
    val durationMinutes: Int,  // المدة
    val signature: String,     // خطوط التوقيع، فارغ إن لم يوقَّع
    val updatedAt: Long
)

@Dao
interface TextbookDao {
    @Query("SELECT * FROM textbook_entry WHERE classId = :classId ORDER BY date DESC, startTime DESC")
    fun observeByClass(classId: String): Flow<List<TextbookEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(e: TextbookEntryEntity)

    @Delete
    suspend fun delete(e: TextbookEntryEntity)

    @Query("DELETE FROM textbook_entry WHERE classId = :classId")
    suspend fun deleteByClass(classId: String)
}

/**
 * يحوّل خطوط التوقيع إلى نص وبالعكس. كل نقطة بإحداثيات نسبية (0..1)
 * حتى يُرسم التوقيع بأي حجم: "x,y;x,y|x,y;..." — الخطوط مفصولة بـ |.
 */
object SignatureCodec {
    fun encode(strokes: List<List<Pair<Float, Float>>>): String =
        strokes.filter { it.size > 1 }.joinToString("|") { s ->
            s.joinToString(";") { (x, y) -> "%.3f,%.3f".format(java.util.Locale.ROOT, x, y) }
        }

    fun decode(text: String): List<List<Pair<Float, Float>>> =
        if (text.isBlank()) emptyList() else text.split("|").mapNotNull { s ->
            s.split(";").mapNotNull { p ->
                val xy = p.split(",")
                if (xy.size == 2) (xy[0].toFloatOrNull() ?: return@mapNotNull null) to (xy[1].toFloatOrNull() ?: return@mapNotNull null)
                else null
            }.takeIf { it.size > 1 }
        }
}
