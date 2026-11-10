package com.elyedali.daftarustadh.report

import com.elyedali.daftarustadh.data.CurriculumSubject
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeMath
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.data.SubjectRecordEntity
import com.elyedali.daftarustadh.data.TestsMode

/** سطر مادة في كشف طالب. */
data class SubjectLine(
    val subject: CurriculumSubject,
    val e1: Double?, val e2: Double?, val e3: Double?,
    val testsAvg: Double?,
    /** كل الاختبارات المرفوعة حتى هذا الفصل في خانة واحدة (12.5/ 13/ 15.7): تُعرض في كشفي الفصلين الأول والثاني ولا تدخل في الحساب. */
    val testsShown: String = "",
    val average: Double?,          // معدل المادة لهذا الكشف
    val total: Double?,            // المعدل × الضارب
    val teacherName: String
)

data class StudentReport(
    val student: StudentEntity,
    val lines: List<SubjectLine>,
    val total: Double,
    val coefficients: Int,
    val generalAverage: Double,
    val rank: Int,
    val classSize: Int
)

/**
 * حساب كشوف قسم لفصل معيّن (1، 2) أو للكشف السنوي (3):
 * - معدل المادة حسب قواعد GradeMath.
 * - المعدل العام = مجموع (معدل المادة × ضاربها) ÷ مجموع الضوارب.
 * - الرتبة داخل القسم: المتساويان يأخذان نفس الرتبة ويُقفز الرقم التالي (1، 2، 2، 4).
 */
object ReportCalc {

    /** ترتيب المواد كما في كشف الدرجات الرسمي. */
    private val OFFICIAL_ORDER = listOf("IR", "AR", "HG", "PH", "IC", "FR", "ANG", "MATS", "SN", "PC", "TI", "LEG", "EPS")

    fun orderSubjects(subjects: List<CurriculumSubject>): List<CurriculumSubject> =
        subjects.sortedBy { OFFICIAL_ORDER.indexOf(it.code).let { i -> if (i < 0) 99 else i } }

    /** الطلاب الذين تنقصهم نتائج لهذا الكشف، مع المواد الناقصة لكل واحد. */
    fun missing(
        term: Int, subjects: List<CurriculumSubject>, students: List<StudentEntity>, grades: List<GradeEntity>
    ): Map<StudentEntity, List<CurriculumSubject>> {
        val g = index(grades)
        return students.sortedBy { it.rollNumber }.associateWith { s ->
            subjects.filter { !GradeMath.isComplete(term, g[s.id]?.get(it.code).orEmpty()) }
        }.filterValues { it.isNotEmpty() }
    }

    fun build(
        term: Int,
        subjects: List<CurriculumSubject>,
        students: List<StudentEntity>,
        grades: List<GradeEntity>,
        records: List<SubjectRecordEntity>,
        teachingModes: Map<String, String>
    ): List<StudentReport> {
        val ordered = orderSubjects(subjects)
        val g = index(grades)
        val coefSum = ordered.sumOf { it.coefficient }
        val partial = students.map { s ->
            val lines = ordered.map { sub ->
                val m = g[s.id]?.get(sub.code).orEmpty()
                val rec = records.firstOrNull { it.subjectCode == sub.code }
                val mode = rec?.testsMode ?: teachingModes[sub.code] ?: TestsMode.AVERAGE
                val avg = GradeMath.subjectAverage(term, m, mode)
                SubjectLine(
                    subject = sub,
                    e1 = m[GradeSlot.E1], e2 = m[GradeSlot.E2], e3 = m[GradeSlot.E3],
                    testsAvg = GradeMath.testsAverage(m, mode),
                    // كل الاختبارات المرفوعة حتى هذا الفصل (في ف2: اختبارات ف1 وف2)، شكلية لا تدخل في الحساب
                    testsShown = if (term <= 2) (1..term).flatMap { t -> GradeSlot.testsOf(t, m).map { it.second } }
                        .joinToString("/ ") { GradeMath.format(it) } else "",
                    average = avg,
                    total = avg?.let { GradeMath.round2(it * sub.coefficient) },
                    teacherName = rec?.teacherName.orEmpty()
                )
            }
            val total = GradeMath.round2(lines.sumOf { it.total ?: 0.0 })
            Triple(s, lines, total)
        }
        // المعدل العام مقرّبًا لرقمين كما يُطبع، والرتبة عليه هو نفسه: المتساويان في الكشف متساويان في الرتبة
        val averages = partial.map { (s, _, total) -> s.id to (if (coefSum == 0) 0.0 else GradeMath.round2(total / coefSum)) }.toMap()
        val rounded = averages
        return partial.map { (s, lines, total) ->
            val mine = rounded.getValue(s.id)
            StudentReport(
                student = s,
                lines = lines,
                total = total,
                coefficients = coefSum,
                generalAverage = averages.getValue(s.id),
                rank = 1 + rounded.values.count { it > mine },
                classSize = students.size
            )
        }.sortedBy { it.student.rollNumber }
    }

    private fun index(grades: List<GradeEntity>): Map<String, Map<String, Map<String, Double>>> =
        grades.groupBy { it.studentId }.mapValues { e ->
            e.value.groupBy { it.subjectCode }.mapValues { s -> s.value.associate { it.slot to it.value } }
        }
}
