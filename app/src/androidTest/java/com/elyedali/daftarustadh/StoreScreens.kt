package com.elyedali.daftarustadh

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.elyedali.daftarustadh.data.*
import com.elyedali.daftarustadh.pdf.ReportCardPdf
import com.elyedali.daftarustadh.report.KashfSettings
import com.elyedali.daftarustadh.report.ReportCalc
import com.elyedali.daftarustadh.ui.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

/** لقطات متجر «إدارة DS» من الشاشات الحقيقية نفسها (نفس الكود)، ببيانات مثال. */
@RunWith(AndroidJUnit4::class)
class StoreScreens {
    @get:Rule val rule = createComposeRule()
    private val ctx get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val out by lazy { File(ctx.filesDir, "store").apply { mkdirs() } }

    private val year = "2026-2027" // السنة الدراسية الحالية
    private val subjects by lazy { CurriculumRepository.level(ctx, year, "3AS")!!.subjects }
    private val cls by lazy { ClassEntity("c1", "main", year, "3AS", 2, 0L) }
    private val names = listOf("آمنة أحمد" to "Amna Ahmed", "إبراهيم محمد" to "Ibrahim Mohamed", "أم كلثوم سيدي" to "Oumkelthoum Sidi",
        "الحسن عبد الله" to "Lehssen Abdallahi", "خديجة محمد الأمين" to "Khadijetou Mohamed Lemine", "زينب أحمد سالم" to "Zeinebou Ahmed Salem",
        "سيدي محمد" to "Sidi Mohamed", "عائشة يحيى" to "Aichetou Yahya", "محمد المختار" to "Mohamed El Moctar", "مريم الشيخ" to "Mariem Cheikh",
        "فاطمة سالم" to "Fatimetou Salem", "أحمد عبد الرحمن" to "Ahmed Abderrahmane")
    private val students by lazy { names.mapIndexed { i, (ar, fr) ->
        StudentEntity("s$i", "c1", i + 1, ar, fr, "2201${i}45", "", "", "", 0L, if (i % 2 == 0) "F" else "M") } }
    private val teachers = listOf("أحمد سالم", "مريم محمد", "سيدي أحمد", "فاطمة الزهراء", "محمد الأمين", "عائشة أحمد", "خديجة سيدي", "يحيى عبد الله", "الشيخ ولد محمد", "زينب عالي")

    /** كل المواد كاملة في الفصل الأول، إلا مادتين ناقصتين (لإظهار «ما ينقص»). */
    private fun grades(full: Boolean): List<GradeEntity> {
        val rnd = Random(11)
        return students.flatMap { s -> subjects.flatMapIndexed { k, sub ->
            GradeSlot.ALL.mapNotNull { slot ->
                if (!full && (k == 3 && s.rollNumber > 9 || k == 6) && slot.endsWith("1")) null
                else GradeEntity(s.id, "c1", sub.code, slot, rnd.nextInt(32, 76) / 4.0, 0L)
            } } }
    }
    private val records by lazy { subjects.mapIndexed { k, s -> SubjectRecordEntity("c1", s.code, teachers[k % teachers.size], TestsMode.AVERAGE, 0L) }
        .filterIndexed { k, _ -> k != 6 } }

    private fun shoot(name: String, content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { DaftarTheme { content() } } }
        rule.waitForIdle(); Thread.sleep(600); rule.waitForIdle()
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap()
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun s1_institution() = shoot("1-institution") {
        val classes = listOf("1AS" to 1, "1AS" to 2, "1AS" to 3, "2AS" to 1, "2AS" to 2, "3AS" to 1, "3AS" to 2, "4AS" to 1, "4AS" to 2)
            .mapIndexed { i, (l, n) -> ClassEntity("k$i", "main", year, l, n, i.toLong()) }
        HomeYearsScreen(classes = classes, currentInstitutionYear = year, selectedYear = year, mode = "admin", onSelectMode = {},
            institutionName = "إعدادية النجاح", teacherName = null, onTool = {}, schoolNames = emptyMap(), ownInstitutionId = "main",
            activated = setOf(year), genderCounts = classes.flatMap { listOf(ClassGenderCount(it.id, "F", 19), ClassGenderCount(it.id, "M", 17)) },
            onSelectYear = {}, onOpenClass = {}, onDeleteClass = {}, onCreateClass = {}, onOpenFile = {}, onMenu = {},
            adminInstitutions = listOf(Institutions.Entry("main", "إعدادية النجاح", "نواكشوط الغربية")), activeInstKey = "main", instChosen = true,
            teachersCount = 22, timetableState = 2, classesWithoutStudents = 0)
    }

    @Test fun s2_results() = shoot("2-results") {
        ResultsStatusScreen(schoolClass = cls, subjects = subjects, students = students, grades = grades(false), records = records,
            onQuickEntry = {}, teacherOf = { code -> records.firstOrNull { it.subjectCode == code }?.let { it.teacherName to "22200000" } },
            onRemind = { _, _ -> }, onOpenSubject = {}, onGenerate = {}, onOpenSettings = {}, onBack = {})
    }

    @Test fun s3_grades() = shoot("3-grades") {
        GradesScreen(schoolClass = cls, subject = subjects.first { it.code == "MATS" }, students = students, grades = grades(true).filter { it.subjectCode == "MATS" },
            testsMode = TestsMode.AVERAGE, scale = RemarkScale.DEFAULT, onSetGrade = { _, _, _, _ -> }, onTestsMode = {}, onPrintRecord = {}, onBack = {})
    }

    @Test fun s4_kashf() {
        val reports = ReportCalc.build(1, subjects, students, grades(true), emptyList(), emptyMap())
        val seal = BitmapFactory.decodeResource(ctx.resources, R.drawable.img_mauritania_seal)
        val info = ReportCardPdf.Info(schoolYear = year, schoolName = "إعدادية النجاح", className = "3AS2",
            passThreshold = KashfSettings.DEFAULT_THRESHOLD, subjectRemarks = { RemarkScale.DEFAULT },
            generalRemarks = KashfSettings.DEFAULT_GENERAL_REMARKS, date = "15/01/2027", seal = seal)
        val pdf = File(out, "kashf.pdf").apply { writeBytes(ReportCardPdf.build(1, reports, info)) }
        PdfRenderer(ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)).use { r ->
            r.openPage(0).use { p ->
                val w = 1080; val h = (w * p.height.toFloat() / p.width).toInt()
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
                p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                File(out, "4-kashf.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        }
    }
}
