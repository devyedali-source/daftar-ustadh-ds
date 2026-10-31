package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * حصة سُجّل فيها الغياب: قسم + مادة + تاريخ + رقم الحصة.
 * المعرّف ثابت (classId|subject|date|period) حتى تحلّ الحصة نفسها محلّ نسختها
 * سواء سجّلها الأستاذ وأرسلها، أو سجّلتها الإدارة بنفسها.
 */
@Entity(tableName = "absence_session", indices = [Index("classId")])
data class AbsenceSessionEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val subjectCode: String,
    val date: String,        // yyyy-MM-dd
    val period: Int,         // رقم الحصة في اليوم (1..8)
    val source: String,      // teacher | admin | import
    val recordedBy: String,  // اسم الأستاذ أو المسجّل
    val updatedAt: Long
) {
    companion object {
        fun idOf(classId: String, subjectCode: String, date: String, period: Int) = "$classId|$subjectCode|$date|$period"
    }
}

object AbsenceSource {
    const val TEACHER = "teacher"
    const val ADMIN = "admin"
    const val IMPORT = "import"
}

/** طالب غائب في حصة. الحاضرون لا يُسجَّلون. */
@Entity(
    tableName = "absence",
    primaryKeys = ["sessionId", "studentId"],
    indices = [Index("classId"), Index("studentId")]
)
data class AbsenceEntity(
    val sessionId: String,
    val studentId: String,
    val classId: String
)

@Dao
abstract class AbsenceDao {
    @Query("SELECT * FROM absence_session WHERE classId = :classId ORDER BY date DESC, period DESC")
    abstract fun observeSessions(classId: String): Flow<List<AbsenceSessionEntity>>

    @Query("SELECT * FROM absence WHERE classId = :classId")
    abstract fun observeAbsences(classId: String): Flow<List<AbsenceEntity>>

    @Query("SELECT * FROM absence_session WHERE classId = :classId AND subjectCode = :subjectCode ORDER BY date, period")
    abstract suspend fun listSessions(classId: String, subjectCode: String): List<AbsenceSessionEntity>

    @Query("SELECT * FROM absence WHERE sessionId = :sessionId")
    abstract suspend fun listAbsences(sessionId: String): List<AbsenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveSessionRow(s: AbsenceSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveAbsences(list: List<AbsenceEntity>)

    @Query("DELETE FROM absence WHERE sessionId = :sessionId")
    abstract suspend fun clearSession(sessionId: String)

    @Query("DELETE FROM absence_session WHERE id = :sessionId")
    abstract suspend fun deleteSessionRow(sessionId: String)

    /** يحفظ الحصة وقائمة غائبيها (تحلّ محلّ ما سبق لنفس الحصة). */
    @Transaction
    open suspend fun saveSession(s: AbsenceSessionEntity, absentStudentIds: Collection<String>) {
        saveSessionRow(s)
        clearSession(s.id)
        saveAbsences(absentStudentIds.map { AbsenceEntity(s.id, it, s.classId) })
    }

    /** تغيير التاريخ أو الحصة أو المادة يغيّر المعرّف: تُحذف النسخة القديمة. */
    @Transaction
    open suspend fun replaceSession(oldId: String?, s: AbsenceSessionEntity, absentStudentIds: Collection<String>) {
        if (oldId != null && oldId != s.id) { clearSession(oldId); deleteSessionRow(oldId) }
        saveSession(s, absentStudentIds)
    }

    @Transaction
    open suspend fun deleteSession(sessionId: String) {
        clearSession(sessionId)
        deleteSessionRow(sessionId)
    }

    @Query("DELETE FROM absence WHERE studentId = :studentId")
    abstract suspend fun deleteByStudent(studentId: String)

    @Query("DELETE FROM absence WHERE classId = :classId")
    abstract suspend fun deleteAbsencesByClass(classId: String)

    @Query("DELETE FROM absence_session WHERE classId = :classId")
    abstract suspend fun deleteSessionsByClass(classId: String)
}
