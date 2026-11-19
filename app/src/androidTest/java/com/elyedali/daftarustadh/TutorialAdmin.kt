package com.elyedali.daftarustadh

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elyedali.daftarustadh.data.*
import com.elyedali.daftarustadh.pdf.ReportCardPdf
import com.elyedali.daftarustadh.report.KashfSettings
import com.elyedali.daftarustadh.report.ReportCalc
import com.elyedali.daftarustadh.timetable.TimetableGenerator
import com.elyedali.daftarustadh.ui.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

/**
 * لقطات فيديو شرح «إدارة DS»: الشاشات الحقيقية نفسها ببيانات مثال — الدخول، المؤسسة، الأساتذة، الجدول الزمني
 * (التوليد أو الإدخال اليدوي)، إرسال ملفات الأساتذة، رفع ملفات الفصل أو الإدخال اليدوي، الملاحظات العامة، والكشوف.
 */
@RunWith(AndroidJUnit4::class)
class TutorialAdmin {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val ctx get() = rule.activity
    private val out by lazy { File(ctx.filesDir, "store").apply { mkdirs() } }
    private val year = "2026-2027"
    private val school = TutorialLang.s("إعدادية النجاح", "Collège En-Najah")
    private val classes by lazy { listOf("1AS" to 1, "1AS" to 2, "2AS" to 1, "3AS" to 1)
        .mapIndexed { i, (l, n) -> ClassEntity("k$i", "main", year, l, n, i.toLong()) } }
    private val subjectsByClass by lazy { classes.associate { it.id to CurriculumRepository.level(ctx, year, it.levelCode)!!.subjects } }
    private fun label(code: String) = subjectsByClass.values.flatten().firstOrNull { it.code == code }?.let { SubjectNames.display(it, TutorialLang.fr) } ?: code
    private val teacherNames = if (TutorialLang.fr) listOf("Mohamed Ahmed", "Mariem Salem", "Sidi Mohamed", "Fatimetou Zahra", "Ahmed Aly", "Aichetou Yahya", "Cheikh Mohamed",
        "Khadijetou Sidi", "Yahya Abdallahi", "Zeinebou Mohamed", "Lehssen Ahmed", "Oumkelthoum") else listOf("محمد أحمد", "مريم سالم", "سيدي محمد", "فاطمة الزهراء", "أحمد عالي", "عائشة يحيى", "الشيخ محمد",
        "خديجة سيدي", "يحيى عبد الله", "زينب محمد", "الحسن أحمد", "أم كلثوم")
    private val allCodes by lazy { subjectsByClass.values.flatten().map { it.code }.distinct() }
    private val teachers by lazy { allCodes.mapIndexed { i, code ->
        TimetableTeacher(teacherNames[i % teacherNames.size], listOf(code), "2224${1000 + i * 37}", maxHours = 24) } }
    private val assignments by lazy { classes.flatMap { c -> subjectsByClass[c.id]!!.map { s ->
        TimetableAssignmentEntity(c.id, s.code, teachers.first { s.code in it.subjects }.name) } } }
    private val names = listOf("آمنة أحمد" to "Amna Ahmed", "إبراهيم محمد" to "Ibrahim Mohamed", "أم كلثوم سيدي" to "Oumkelthoum Sidi",
        "الحسن عبد الله" to "Lehssen Abdallahi", "خديجة محمد الأمين" to "Khadijetou Mohamed Lemine", "زينب أحمد سالم" to "Zeinebou Ahmed Salem",
        "سيدي محمد" to "Sidi Mohamed", "عائشة يحيى" to "Aichetou Yahya", "محمد المختار" to "Mohamed El Moctar", "مريم الشيخ" to "Mariem Cheikh")
    private val cls get() = classes[0]
    private val subjects get() = subjectsByClass[cls.id]!!
    private val students by lazy { names.mapIndexed { i, (ar, fr) ->
        StudentEntity("s$i", cls.id, i + 1, ar, fr, "2201${i}45", "", "", "", 0L, if (i % 2 == 0) "F" else "M") } }
    private fun grades(missing: Boolean): List<GradeEntity> {
        val rnd = Random(9)
        return students.flatMap { s -> subjects.flatMapIndexed { k, sub ->
            listOf(GradeSlot.T1, GradeSlot.E1).mapNotNull { slot ->
                if (missing && k in setOf(2, 5)) null else GradeEntity(s.id, cls.id, sub.code, slot, rnd.nextInt(30, 78) / 4.0, 0L)
            } } }
    }
    private fun records(missing: Boolean) = subjects.mapIndexedNotNull { k, s ->
        if (missing && k in setOf(2, 5)) null else SubjectRecordEntity(cls.id, s.code, assignments.first { it.classId == cls.id && it.subjectCode == s.code }.teacherName, TestsMode.AVERAGE, 0L) }

    private fun show(content: @Composable () -> Unit) {
        ScreenSecurity.allowForStoreShots = true
        TutorialLang.apply(ctx, ctx.applicationContext)
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides TutorialLang.direction) {
                DaftarTheme { Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { content() } }
            }
        }
        rule.waitForIdle(); Thread.sleep(700); rule.waitForIdle()
    }
    private fun save(name: String, dialog: Boolean = false) {
        rule.waitForIdle(); Thread.sleep(500); rule.waitForIdle()
        val bmp = (if (dialog) rule.onNode(isDialog()) else rule.onRoot()).captureToImage().asAndroidBitmap()
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun a01_login() { show { LoginScreen(onSignedIn = {}) }; save("a01-login") }

    @Test fun a02_years() {
        show {
            HomeYearsScreen(classes = emptyList(), currentInstitutionYear = null, selectedYear = null, mode = "admin", onSelectMode = {},
                institutionName = null, teacherName = null, onTool = {}, schoolNames = emptyMap(), ownInstitutionId = null,
                activated = emptySet(), genderCounts = emptyList(), onSelectYear = {}, onOpenClass = {}, onDeleteClass = {},
                onCreateClass = {}, onOpenFile = {}, onMenu = {}, instChosen = false)
        }
        save("a02-years")
    }

    @Test fun a03_institution() {
        show { InstitutionScreen(existing = InstitutionEntity("main", "انواكشوط 2 (الغربية)", school, null, Stage.COLLEGE, year, 0L),
            onSave = {}, onCancel = {}, fixedYear = year) }
        save("a03-institution")
    }

    private fun home(teachersCount: Int, tt: Int) = show {
        HomeYearsScreen(classes = classes, currentInstitutionYear = year, selectedYear = year, mode = "admin", onSelectMode = {},
            institutionName = school, teacherName = null, onTool = {}, schoolNames = emptyMap(), ownInstitutionId = "main",
            activated = setOf(year), genderCounts = classes.flatMap { listOf(ClassGenderCount(it.id, "F", 18), ClassGenderCount(it.id, "M", 16)) },
            onSelectYear = {}, onOpenClass = {}, onDeleteClass = {}, onCreateClass = {}, onOpenFile = {}, onMenu = {},
            adminInstitutions = listOf(Institutions.Entry("main", school, "انواكشوط 2 (الغربية)")), activeInstKey = "main", instChosen = true,
            teachersCount = teachersCount, timetableState = tt, classesWithoutStudents = 0)
    }

    @Test fun a04_home() { home(0, 0); save("a04-home") }

    private val settings = TimetableSettings()
    private val slots by lazy {
        val lessons = classes.flatMap { c -> subjectsByClass[c.id]!!.map { s ->
            TimetableGenerator.Lesson(c.id, s.code, assignments.first { it.classId == c.id && it.subjectCode == s.code }.teacherName, s.weeklyHours) } }
        TimetableGenerator.generate(lessons, settings.days.size, settings.periods, seed = 7, iterations = 60_000,
            periodsPerDay = settings.days.map { settings.periodsOn(it) }, doubleStarts = settings.doubleStarts())
            .placed.map { TimetableSlotEntity(it.classId, settings.days[it.day], it.period, it.subjectCode, it.teacher) }
    }

    private fun timetable(tab: Int, withSlots: Boolean, manual: Boolean = false) = show {
        TimetableScreen(classes = classes, subjectsByClass = subjectsByClass, subjectLabel = { label(it) }, settings = settings,
            assignments = assignments, teachers = teachers, slots = if (withSlots) slots else emptyList(), generating = false,
            lastResult = null, initialClassId = classes[0].id, onSaveSettings = {}, onAssign = { _, _, _ -> }, onSaveTeachers = {},
            onDistribute = {}, onGenerate = {}, onSwap = { _, _, _ -> null }, onEditCell = { _, _, _, _, _ -> }, onShareClass = {},
            onShareTeacherPdf = {}, onBack = {}, initialTab = tab, manual = manual)
    }

    @Test fun a05_teachers() { timetable(1, false); save("a05-teachers") }
    @Test fun a06_generate() { timetable(0, false); save("a06-generate") }
    @Test fun a07_grid() { timetable(2, true); save("a07-grid") }
    @Test fun a08_home_ready() { home(teachers.size, 2); save("a08-home-ready") }

    @Test fun a09_send_files() {
        show { ShareRecordsScreen(teachers = teachers, classes = classes, assignments = assignments, subjectLabel = { label(it) },
            sent = setOf(teachers[0].name, teachers[1].name), blockers = emptyList(), onOpenTimetable = {}, onSend = {},
            onSavePhone = { _, _ -> }, onManageTeachers = {}, onBack = {}) }
        save("a09-send")
    }

    @Test fun a10_follow() {
        show { RecordsFollowScreen(classes = classes, subjectsOf = { c -> subjectsByClass[c.id]!!.map { it.code } }, subjectLabel = { label(it) },
            assignments = assignments, students = students, grades = grades(true), onUploadMany = {}, onGenerate = { _, _ -> }, onBack = {}) }
        save("a10-follow")
    }

    @Test fun a11_results_missing() {
        show { ResultsStatusScreen(schoolClass = cls, subjects = subjects, students = students, grades = grades(true), records = records(true),
            onQuickEntry = {}, teacherOf = { code -> teachers.firstOrNull { code in it.subjects }?.let { it.name to it.phone } },
            onRemind = { _, _ -> }, onOpenSubject = {}, onGenerate = {}, onOpenSettings = {}, onBack = {}) }
        save("a11-results-missing")
    }

    @Test fun a12_manual() {
        val fr = subjects[2]
        show { GradesScreen(schoolClass = cls, subject = fr, students = students,
            grades = grades(false).filter { it.subjectCode == fr.code && (it.slot == GradeSlot.T1 || it.studentId.removePrefix("s").toInt() < 4) },
            testsMode = TestsMode.AVERAGE, scale = RemarkScale.DEFAULT, onSetGrade = { _, _, _, _ -> }, onTestsMode = {}, onPrintRecord = {}, onBack = {}) }
        save("a12-manual")
    }

    @Test fun a13_results_ready() {
        show { ResultsStatusScreen(schoolClass = cls, subjects = subjects, students = students, grades = grades(false), records = records(false),
            onQuickEntry = {}, teacherOf = { code -> teachers.firstOrNull { code in it.subjects }?.let { it.name to it.phone } },
            onRemind = { _, _ -> }, onOpenSubject = {}, onGenerate = {}, onOpenSettings = {}, onBack = {}) }
        save("a13-results-ready")
    }

    @Test fun a14_kashf_settings() {
        show { KashfSettingsScreen(levels = listOf("1AS", "2AS", "3AS"), thresholds = mapOf("1AS" to 10.0, "2AS" to 10.0, "3AS" to 9.0),
            onSave = {}, onEditGeneralRemarks = {}, onBack = {}) }
        save("a14-kashf-settings")
    }

    @Test fun a15_general_remarks() {
        show { RemarksSettingsScreen(scale = KashfSettings.DEFAULT_GENERAL_REMARKS, teacherName = null, onSave = { _, _ -> },
            title = ctx.getString(R.string.general_remarks), defaults = KashfSettings.DEFAULT_GENERAL_REMARKS, onBack = {}) }
        save("a15-general-remarks")
    }

    @Test fun a16_kashf() {
        val reports = ReportCalc.build(1, subjects, students, grades(false), emptyList(), emptyMap())
        val seal = BitmapFactory.decodeResource(ctx.resources, R.drawable.img_mauritania_seal)
        val info = ReportCardPdf.Info(schoolYear = year, schoolName = school, className = cls.name,
            passThreshold = KashfSettings.DEFAULT_THRESHOLD, subjectRemarks = { RemarkScale.DEFAULT },
            generalRemarks = KashfSettings.DEFAULT_GENERAL_REMARKS, date = "20/01/2027", seal = seal)
        val pdf = File(out, "kashf.pdf").apply { writeBytes(ReportCardPdf.build(1, reports, info)) }
        val pfd = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)
        val r = PdfRenderer(pfd)
        val p = r.openPage(0)
        val w = 1080; val h = (w * p.height.toFloat() / p.width).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        p.close(); r.close(); pfd.close()
        File(out, "a16-kashf.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
