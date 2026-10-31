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
    val updatedAt: Long
)

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
    fun test(term: Int) = "T$term"
    fun exam(term: Int) = "E$term"
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
