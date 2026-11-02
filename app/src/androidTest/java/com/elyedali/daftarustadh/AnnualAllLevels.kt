package com.elyedali.daftarustadh

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.data.RemarkScale
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.pdf.ReportCardPdf
import com.elyedali.daftarustadh.report.KashfSettings
import com.elyedali.daftarustadh.report.ReportCalc
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

/** مثال الكشف السنوي لكل المستويات والشعب (طالب واحد لكل مستوى)، بنفس كود التطبيق. */
@RunWith(AndroidJUnit4::class)
class AnnualAllLevels {
    @Test
    fun generate() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val year = CurriculumRepository.availableYears(ctx).last()
        val seal = BitmapFactory.decodeResource(ctx.resources, R.drawable.img_mauritania_seal)
        val out = File(ctx.filesDir, "samples/annual").apply { mkdirs() }
        val rnd = Random(21)
        CurriculumRepository.levels(ctx, year).forEachIndexed { idx, lvl ->
            val cls = "${lvl.code}1"
            val students = listOf(
                StudentEntity("a", cls, 1, "فاطمة محمد سالم", "Fatimetou Mohamed Salem", "1234567890", "", "", "", 0L, "F"),
                StudentEntity("b", cls, 2, "أحمد عبد الله", "Ahmed Abdallahi", "1234567891", "", "", "", 0L, "M")
            )
            val grades = students.flatMap { s ->
                lvl.subjects.flatMap { sub -> GradeSlot.ALL.map { slot -> GradeEntity(s.id, cls, sub.code, slot, rnd.nextInt(24, 76) / 4.0, 0L) } }
            }
            val reports = ReportCalc.build(3, lvl.subjects, students, grades, emptyList(), emptyMap()).take(1)
            val info = ReportCardPdf.Info(
                schoolYear = year, schoolName = if (lvl.stage == "lycee") "ثانوية البنات" else "إعدادية البنات", className = cls,
                passThreshold = KashfSettings.DEFAULT_THRESHOLD, subjectRemarks = RemarkScale.DEFAULT,
                generalRemarks = KashfSettings.DEFAULT_GENERAL_REMARKS, date = "02/11/2026", seal = seal
            )
            File(out, "%02d-%s.pdf".format(idx + 1, lvl.code)).writeBytes(ReportCardPdf.build(3, reports, info))
        }
    }
}
