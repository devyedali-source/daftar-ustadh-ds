package com.elyedali.daftarustadh

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.report.KashfSettings
import com.elyedali.daftarustadh.data.RemarkScale
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.pdf.ReportCardPdf
import com.elyedali.daftarustadh.report.ReportCalc
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

/** يولّد أمثلة كشوف الدرجات (الفصل الأول والثاني والسنوي) بنفس كود التطبيق، للمراجعة. */
@RunWith(AndroidJUnit4::class)
class KashfSamples {
    @Test
    fun generate() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val year = CurriculumRepository.availableYears(ctx).last()
        val subjects = CurriculumRepository.level(ctx, year, "1AS")!!.subjects
        val names = listOf(
            "فاطمة محمد سالم" to "Fatimetou Mohamed Salem", "أحمد عبد الله" to "Ahmed Abdallahi",
            "مريم الشيخ" to "Mariem Cheikh", "محمد الأمين سيدي" to "Mohamed Lemine Sidi"
        )
        val students = names.mapIndexed { i, (ar, fr) ->
            StudentEntity("s$i", "c1", i + 1, ar, fr, "12345${i}678", "", "", "", 0L, if (i % 2 == 0) "F" else "M")
        }
        val rnd = Random(7)
        val grades = students.flatMap { s ->
            subjects.flatMap { sub ->
                GradeSlot.ALL.map { slot ->
                    val v = (rnd.nextInt(16, 76) / 4.0)   // 4 → 19 بخطوة 0.25
                    GradeEntity(s.id, "c1", sub.code, slot, v, 0L)
                }
            }
        }
        val seal = BitmapFactory.decodeResource(ctx.resources, R.drawable.img_mauritania_seal)
        val out = File(ctx.filesDir, "samples").apply { mkdirs() }
        listOf(1 to "kashf-T1", 2 to "kashf-T2", 3 to "kashf-annuel").forEach { (term, name) ->
            val reports = ReportCalc.build(term, subjects, students, grades, emptyList(), emptyMap())
            val info = ReportCardPdf.Info(
                schoolYear = year, schoolName = "إعدادية البنات", className = "1AS1",
                passThreshold = KashfSettings.DEFAULT_THRESHOLD,
                subjectRemarks = RemarkScale.DEFAULT, generalRemarks = KashfSettings.DEFAULT_GENERAL_REMARKS,
                date = "02/11/2026", seal = seal
            )
            File(out, "$name.pdf").writeBytes(ReportCardPdf.build(term, reports, info))
        }
    }
}
