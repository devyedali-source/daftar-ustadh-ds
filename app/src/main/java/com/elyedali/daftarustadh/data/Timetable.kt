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

    @Query("SELECT * FROM timetable_assignment")
    abstract suspend fun listAssignments(): List<TimetableAssignmentEntity>

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

    /** بداية حصتي آخر النهار المحفوظة (الحصة الخامسة). */
    val eveningStart: Int get() = times.getOrNull(4)?.first ?: DEFAULT_EVENING

    /** عدد حصص يوم معيّن (الجمعة أقصر). */
    fun periodsOn(day: Int): Int = (dayPeriods[day] ?: periods).coerceIn(0, periods)

    val slotCount: Int get() = days.sumOf { periodsOn(it) }

    fun start(period: Int): Int = times.getOrNull(period)?.first ?: 0

    fun end(period: Int): Int = times.getOrNull(period)?.second ?: 0

    fun label(period: Int): String = "${hhmm(start(period))}-${hhmm(end(period))}"

    /** الحصص التي يمكن أن تبدأ بها حصة من ساعتين: هي والتي تليها بلا استراحة بينهما. */
    fun doubleStarts(): Set<Int> = (0 until periods - 1).filter { end(it) == start(it + 1) }.toSet()

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

        /** أيام الدراسة ثابتة: الاثنين إلى السبت. */
        val FIXED_DAYS = listOf(0, 1, 2, 3, 4, 5)

        /** بداية حصتي آخر النهار: 12:15 (الأغلب) أو مساءً حسب اختيار المؤسسة (مثل 16:00 أو 17:00). */
        const val DEFAULT_EVENING = 12 * 60 + 15

        /**
         * التوقيت ثابت: 08:00–14:00 من الاثنين إلى السبت، والجمعة حتى 12:00 (أربع حصص).
         * الشيء الوحيد الذي تختاره المؤسسة: وقت حصتي آخر النهار (بين 12 و14، أو مساءً).
         */
        fun fixed(eveningStart: Int = DEFAULT_EVENING): TimetableSettings {
            val evening = if (eveningStart == DEFAULT_EVENING) DEFAULT_TIMES.drop(4)
                else listOf(eveningStart to eveningStart + 60, eveningStart + 60 to eveningStart + 120)
            return TimetableSettings(days = FIXED_DAYS, times = DEFAULT_TIMES.take(4) + evening, dayPeriods = mapOf(4 to 4))
        }

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

/**
 * أستاذ في المؤسسة (مُدخل من الإدارة): اسمه، ومواده الأصلية (تخصصه: مادة أو مادتان)،
 * والمواد التي يمكن أن يدرّسها عند الحاجة، ونصابه (ساعاته في الأسبوع)، ورقم واتسابه.
 */
data class TimetableTeacher(
    val name: String,
    val subjects: List<String>,
    val phone: String = "",
    val extra: List<String> = emptyList(),
    val maxHours: Int = TimetableTeachers.DEFAULT_MAX_HOURS
) {
    /** كل ما يمكن أن يُسند إليه: مواده الأصلية ثم الإضافية. */
    val all: List<String> get() = subjects + extra.filter { it !in subjects }
}

object TimetableTeachers {
    const val KEY = "timetable_teachers"

    /** لا يدرّس الأستاذ أكثر من 5 مواد. */
    const val MAX_SUBJECTS = 5

    /** المواد الأصلية (التخصص): مادة أو مادتان على الأكثر. */
    const val MAX_MAIN = 2

    /** النصاب الافتراضي للأستاذ (ساعات في الأسبوع)، تعدّله الإدارة لكل أستاذ. */
    const val DEFAULT_MAX_HOURS = 18

    fun encode(list: List<TimetableTeacher>): String = JSONArray().apply {
        list.forEach { t ->
            put(JSONObject().put("name", t.name).put("subjects", JSONArray(t.subjects)).put("phone", t.phone)
                .put("extra", JSONArray(t.extra)).put("maxHours", t.maxHours))
        }
    }.toString()

    fun decode(text: String?): List<TimetableTeacher> {
        if (text.isNullOrBlank()) return emptyList()
        return runCatching {
            val a = JSONArray(text)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                val s = o.getJSONArray("subjects")
                val e = o.optJSONArray("extra")
                TimetableTeacher(o.getString("name"), (0 until s.length()).map { s.getString(it) }, o.optString("phone"),
                    e?.let { a2 -> (0 until a2.length()).map { a2.getString(it) } }.orEmpty(),
                    o.optInt("maxHours", DEFAULT_MAX_HOURS).takeIf { it > 0 } ?: DEFAULT_MAX_HOURS)
            }.filter { it.name.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    /**
     * يوزّع أقسام كل مادة على أساتذتها:
     * 1) أصحاب المادة الأصلية أولًا، الأقل ساعات منهم، ما دام في نصابه متسع (مع تفضيل من يدرّس نفس المستوى).
     * 2) ما فاض عن نصابهم يُسند لمن يمكنه تدريس المادة (مادة إضافية) وله وقت فراغ.
     * 3) إن لم يتسع أحد: صاحب المادة الأصلية الأقل ساعات، ثم صاحب المادة الإضافية.
     * @param classSubjects لكل قسم: (معرّف القسم، المستوى، [(رمز المادة، ساعاتها)])
     */
    fun distribute(
        teachers: List<TimetableTeacher>,
        classSubjects: List<Triple<String, String, List<Pair<String, Int>>>>
    ): List<TimetableAssignmentEntity> {
        val load = HashMap<String, Int>()
        val levels = HashMap<String, MutableSet<String>>()  // أستاذ -> مستويات (مادة|مستوى)
        val jobs = classSubjects.flatMap { (cid, level, subs) -> subs.map { (code, h) -> Triple(cid, level, code to h) } }
            .filter { (_, _, sh) -> teachers.any { sh.first in it.all } }
            .sortedWith(compareByDescending<Triple<String, String, Pair<String, Int>>> { it.third.second }.thenBy { it.second }.thenBy { it.first })
        val out = ArrayList<TimetableAssignmentEntity>()
        fun best(list: List<TimetableTeacher>, code: String, level: String) = list.minWithOrNull(
            compareBy<TimetableTeacher> { load[it.name] ?: 0 }
                .thenBy { if (levels[it.name]?.contains("$code|$level") == true) 0 else 1 }
                .thenBy { it.name })
        // أصحاب المواد الأصلية يأخذون حصصهم أولًا، ثم يُوزَّع الفائض على أصحاب المواد الإضافية
        val pending = ArrayList<Triple<String, String, Pair<String, Int>>>()
        jobs.forEach { job ->
            val (cid, level, sh) = job
            val (code, hours) = sh
            val pick = best(teachers.filter { code in it.subjects && (load[it.name] ?: 0) + hours <= it.maxHours }, code, level)
            if (pick == null) { pending += job; return@forEach }
            load[pick.name] = (load[pick.name] ?: 0) + hours
            levels.getOrPut(pick.name) { HashSet() }.add("$code|$level")
            out += TimetableAssignmentEntity(cid, code, pick.name)
        }
        pending.forEach { (cid, level, sh) ->
            val (code, hours) = sh
            val fits = { t: TimetableTeacher -> (load[t.name] ?: 0) + hours <= t.maxHours }
            val pick = best(teachers.filter { code !in it.subjects && code in it.extra && fits(it) }, code, level)
                ?: best(teachers.filter { code in it.subjects }, code, level)
                ?: best(teachers.filter { code in it.extra }, code, level)
                ?: return@forEach
            load[pick.name] = (load[pick.name] ?: 0) + hours
            levels.getOrPut(pick.name) { HashSet() }.add("$code|$level")
            out += TimetableAssignmentEntity(cid, code, pick.name)
        }
        return out
    }
}
