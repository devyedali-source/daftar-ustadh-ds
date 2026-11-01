package com.elyedali.daftarustadh.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

/** أستاذ المادة في القسم (تعيين الإدارة) — أساس توليد الجدول الزمني. */
@Entity(tableName = "timetable_assignment", primaryKeys = ["classId", "subjectCode"])
data class TimetableAssignmentEntity(
    val classId: String,
    val subjectCode: String,
    val teacherName: String
)

/** حصة في جدول القسم: اليوم (0 = الاثنين ... 6 = الأحد) ورقم الحصة (0 ...). */
@Entity(tableName = "timetable_slot", primaryKeys = ["classId", "day", "period"], indices = [Index("classId")])
data class TimetableSlotEntity(
    val classId: String,
    val day: Int,
    val period: Int,
    val subjectCode: String,
    val teacherName: String
)

/**
 * جدول الأستاذ كما وصله من إدارة مؤسسة (قد يدرّس في عدة مؤسسات).
 * كل ملف جديد من نفس المؤسسة والسنة يحلّ محلّ القديم.
 */
@Entity(tableName = "teacher_timetable", indices = [Index("institutionId")])
data class TeacherTimetableEntity(
    @androidx.room.PrimaryKey val id: String,  // institutionId|schoolYear|day|period
    val institutionId: String,
    val schoolName: String,
    val schoolYear: String,
    val teacherName: String,
    val day: Int,
    val period: Int,
    val startTime: String,
    val endTime: String,
    val className: String,
    val subjectCode: String,
    val subjectNameAr: String,
    val subjectNameFr: String,
    val receivedAt: Long
)

@Dao
abstract class TimetableDao {
    @Query("SELECT * FROM timetable_assignment")
    abstract fun observeAssignments(): Flow<List<TimetableAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveAssignment(a: TimetableAssignmentEntity)

    @Query("DELETE FROM timetable_assignment WHERE classId = :classId AND subjectCode = :subjectCode")
    abstract suspend fun deleteAssignment(classId: String, subjectCode: String)

    @Query("SELECT * FROM timetable_slot")
    abstract fun observeSlots(): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveSlots(list: List<TimetableSlotEntity>)

    @Query("DELETE FROM timetable_slot WHERE classId = :classId AND day = :day AND period = :period")
    abstract suspend fun deleteSlot(classId: String, day: Int, period: Int)

    @Query("DELETE FROM timetable_slot WHERE classId IN (:classIds)")
    abstract suspend fun deleteSlotsOf(classIds: List<String>)

    /** يستبدل جداول هذه الأقسام بجدول جديد مولَّد. */
    @Transaction
    open suspend fun replaceSlots(classIds: List<String>, slots: List<TimetableSlotEntity>) {
        deleteSlotsOf(classIds)
        saveSlots(slots)
    }

    @Query("DELETE FROM timetable_assignment WHERE classId = :classId")
    abstract suspend fun deleteAssignmentsByClass(classId: String)

    @Query("DELETE FROM timetable_slot WHERE classId = :classId")
    abstract suspend fun deleteSlotsByClass(classId: String)

    @Query("SELECT * FROM teacher_timetable ORDER BY schoolName, day, period")
    abstract fun observeTeacherTimetable(): Flow<List<TeacherTimetableEntity>>

    @Query("DELETE FROM teacher_timetable WHERE institutionId = :institutionId AND schoolYear = :schoolYear")
    abstract suspend fun clearTeacherTimetable(institutionId: String, schoolYear: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveTeacherTimetable(list: List<TeacherTimetableEntity>)

    @Transaction
    open suspend fun replaceTeacherTimetable(institutionId: String, schoolYear: String, list: List<TeacherTimetableEntity>) {
        clearTeacherTimetable(institutionId, schoolYear)
        saveTeacherTimetable(list)
    }
}

/**
 * إعدادات الجدول: أيام الدراسة، توقيت كل حصة (بداية ونهاية)، وأيام أقصر (الجمعة).
 * التوقيت التلقائي: 8-9، 9-10، استراحة 10:00-10:15، 10:15-11:05، 11:05-12:00،
 * استراحة 12:00-12:15، 12:15-13:05، 13:05-14:00؛ والجمعة تنتهي 12:00.
 * تُحفظ في app_meta.
 */
data class TimetableSettings(
    val days: List<Int> = listOf(0, 1, 2, 3, 4, 5),      // الاثنين ... السبت (السبت يوم دراسة في الإعدادية والثانوية)
    val times: List<Pair<Int, Int>> = DEFAULT_TIMES,     // (بداية، نهاية) بالدقائق لكل حصة
    val dayPeriods: Map<Int, Int> = mapOf(4 to 4)        // أيام أقصر: الجمعة تنتهي 12:00 (4 حصص)
) {
    val periods: Int get() = times.size

    /** عدد حصص يوم معيّن (الجمعة أقصر). */
    fun periodsOn(day: Int): Int = (dayPeriods[day] ?: periods).coerceIn(0, periods)

    val slotCount: Int get() = days.sumOf { periodsOn(it) }

    fun start(period: Int): Int = times.getOrNull(period)?.first ?: 0

    fun end(period: Int): Int = times.getOrNull(period)?.second ?: 0

    fun label(period: Int): String = "${hhmm(start(period))}-${hhmm(end(period))}"

    /** الاستراحات: الفراغ بين نهاية حصة وبداية التي تليها. */
    fun breakBefore(period: Int): Int = if (period <= 0) 0 else (start(period) - end(period - 1)).coerceAtLeast(0)

    fun encode(): String = JSONObject()
        .put("days", JSONArray(days))
        .put("times", JSONArray().apply { times.forEach { (a, b) -> put(JSONArray().put(a).put(b)) } })
        .put("dayPeriods", JSONObject().apply { dayPeriods.forEach { (d, n) -> put(d.toString(), n) } })
        .toString()

    companion object {
        const val KEY = "timetable_settings"

        private fun t(h: Int, m: Int) = h * 60 + m
        val DEFAULT_TIMES = listOf(
            t(8, 0) to t(9, 0), t(9, 0) to t(10, 0),
            t(10, 15) to t(11, 5), t(11, 5) to t(12, 0),
            t(12, 15) to t(13, 5), t(13, 5) to t(14, 0)
        )

        fun hhmm(m: Int) = "%02d:%02d".format(java.util.Locale.ROOT, (m / 60) % 24, m % 60)

        fun decode(text: String?): TimetableSettings {
            if (text.isNullOrBlank()) return TimetableSettings()
            return runCatching {
                val o = JSONObject(text)
                val d = o.getJSONArray("days")
                val ta = o.optJSONArray("times")
                val times = if (ta == null) DEFAULT_TIMES else (0 until ta.length()).map { k ->
                    val p = ta.getJSONArray(k); p.getInt(0) to p.getInt(1)
                }.filter { it.second > it.first }.ifEmpty { DEFAULT_TIMES }
                TimetableSettings(
                    days = (0 until d.length()).map { d.getInt(it) }.filter { it in 0..6 }.distinct().sorted().ifEmpty { listOf(0, 1, 2, 3, 4, 5) },
                    times = times.take(12),
                    dayPeriods = o.optJSONObject("dayPeriods")?.let { dp ->
                        dp.keys().asSequence().mapNotNull { k -> k.toIntOrNull()?.let { it to dp.optInt(k) } }
                            .filter { it.first in 0..6 }.toMap()
                    } ?: mapOf(4 to 4)
                )
            }.getOrDefault(TimetableSettings())
        }
    }
}
