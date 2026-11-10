package com.elyedali.daftarustadh.data

import androidx.room.Entity
import androidx.room.Index

/**
 * نقطة واحدة لطالب في مادة: اختبار أو امتحان فصل.
 * الخانة الفارغة لا يُحفظ لها سطر؛ القيمة من 0 إلى 20.
 */
@Entity(
    tableName = "grade",
    primaryKeys = ["studentId", "subjectCode", "slot"],
    indices = [Index(value = ["classId", "subjectCode"])]
)
data class GradeEntity(
    val studentId: String,
    val classId: String,
    val subjectCode: String,
    val slot: String,       // T1 E1 T2 E2 T3 E3
    val value: Double,
    val updatedAt: Long,
    val mark: String? = null   // غياب: AJ = غائب بتبرير (10)، AN = غائب بدون تبرير (0)؛ null = نقطة عادية
)

/** الغياب عن اختبار أو امتحان: بتبرير يأخذ 10، وبدون تبرير يأخذ 0. */
object GradeMark {
    const val JUSTIFIED = "AJ"
    const val UNJUSTIFIED = "AN"
    fun valueOf(mark: String): Double = if (mark == JUSTIFIED) 10.0 else 0.0
    fun isValid(mark: String?) = mark == JUSTIFIED || mark == UNJUSTIFIED
}

/** المواد التي يدرّسها الأستاذ في قسم، وطريقة حساب معدل الاختبارات في الفصل الثالث. */
@Entity(tableName = "teacher_subject", primaryKeys = ["classId", "subjectCode"])
data class TeacherSubjectEntity(
    val classId: String,
    val subjectCode: String,
    val testsMode: String = TestsMode.AVERAGE
)

object TestsMode {
    const val AVERAGE = "average"   // معدل الاختبارات المدرجة
    const val BEST = "best"         // أفضل نتيجة اختبار
}

object GradeSlot {
    const val T1 = "T1"; const val E1 = "E1"
    const val T2 = "T2"; const val E2 = "E2"
    const val T3 = "T3"; const val E3 = "E3"
    val ALL = listOf(T1, E1, T2, E2, T3, E3)
    /** الاختبار رقم n في الفصل: الأول «T1» (كما كان)، والإضافية «T1.2» و«T1.3»... */
    fun test(term: Int, n: Int = 1) = if (n <= 1) "T$term" else "T$term.$n"
    fun exam(term: Int) = "E$term"

    private val TEST_RE = Regex("""T([123])(?:\.(\d+))?""")
    fun isTest(slot: String) = TEST_RE.matches(slot)
    /** الفصل الذي ينتمي إليه الاختبار، أو null إن لم تكن الخانة اختبارًا. */
    fun testTerm(slot: String): Int? = TEST_RE.matchEntire(slot)?.groupValues?.get(1)?.toIntOrNull()
    fun testIndex(slot: String): Int = TEST_RE.matchEntire(slot)?.groupValues?.get(2)?.toIntOrNull() ?: 1
    /** خانة معروفة: امتحان أو اختبار (بما فيه الاختبارات الإضافية). */
    fun isKnown(slot: String) = slot in ALL || isTest(slot)
    /** اختبارات فصل واحد مرتبة: (الخانة، النقطة). */
    fun testsOf(term: Int, grades: Map<String, Double>): List<Pair<String, Double>> =
        grades.filterKeys { testTerm(it) == term }.toList().sortedBy { testIndex(it.first) }
    /** كل الاختبارات المرتبة من الفصل الأول إلى الثالث. */
    fun allTests(grades: Map<String, Double>): List<Double> = (1..3).flatMap { t -> testsOf(t, grades).map { it.second } }
    /** ترتيب الخانات للعرض: اختبارات ف1 ثم امتحانه، ثم ف2... */
    fun sortKey(slot: String): Int {
        val t = testTerm(slot)
        return if (t != null) t * 100 + testIndex(slot) else (slot.drop(1).toIntOrNull() ?: 9) * 100 + 99
    }
}

/**
 * سجل مادة رُفع عند الإدارة (أو أُدخل يدويًا): اسم الأستاذ الذي يظهر في الكشف،
 * وطريقة حساب معدل الاختبارات التي اختارها، وتاريخ الرفع.
 * teacherName فارغ = إدخال يدوي من الإدارة.
 */
@Entity(tableName = "subject_record", primaryKeys = ["classId", "subjectCode"])
data class SubjectRecordEntity(
    val classId: String,
    val subjectCode: String,
    val teacherName: String,
    val testsMode: String,
    val receivedAt: Long
)
