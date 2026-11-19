package com.elyedali.daftarustadh

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elyedali.daftarustadh.data.*
import com.elyedali.daftarustadh.ui.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

/**
 * لقطات فيديو شرح «دفتر الأستاذ DS»: الشاشات الحقيقية نفسها (نفس الكود ومحتوى الدروس) ببيانات مثال.
 * من الدخول إلى التخطيط والتحاضير والسبورة والتمارين و«تحريراتي»، ثم ملف الإدارة ورصد النتائج وإرسالها.
 */
@RunWith(AndroidJUnit4::class)
class TutorialTeacher {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val ctx get() = rule.activity
    private val out by lazy { File(ctx.filesDir, "store").apply { mkdirs() } }
    private val year = "2026-2027"
    private val school = "إعدادية النجاح"
    private val c1 by lazy { ClassEntity("c1", "inst1", year, "1AS", 1, 0L) }
    private val c2 by lazy { ClassEntity("c2", "inst1", year, "2AS", 2, 1L) }
    private val subj1 by lazy { CurriculumRepository.level(ctx, year, "1AS")!!.subjects }
    private val names = listOf("آمنة أحمد" to "Amna Ahmed", "إبراهيم محمد" to "Ibrahim Mohamed", "أم كلثوم سيدي" to "Oumkelthoum Sidi",
        "الحسن عبد الله" to "Lehssen Abdallahi", "خديجة محمد الأمين" to "Khadijetou Mohamed Lemine", "زينب أحمد سالم" to "Zeinebou Ahmed Salem",
        "سيدي محمد" to "Sidi Mohamed", "عائشة يحيى" to "Aichetou Yahya", "محمد المختار" to "Mohamed El Moctar", "مريم الشيخ" to "Mariem Cheikh")
    private val students by lazy { names.mapIndexed { i, (ar, fr) ->
        StudentEntity("s$i", "c1", i + 1, ar, fr, "2201${i}45", "", "", "", 0L, if (i % 2 == 0) "F" else "M") } }
    private val stamp = "الأستاذ: محمد أحمد\nأستاذ مادتي الرياضيات والفيزياء\n$school"

    @Before fun setup() {
        ScreenSecurity.allowForStoreShots = true
        ctx.getSharedPreferences("planning_order", Context.MODE_PRIVATE).edit()
            .putStringSet("fav_subjects", setOf("MATS", "SN")).putStringSet("fav_levels", setOf("1AS", "2AS")).apply()
        ctx.getSharedPreferences("book_packs", Context.MODE_PRIVATE).edit()
            .putStringSet("subjects", setOf("MATS", "SN")).putStringSet("levels", setOf("1AS", "2AS")).apply()
    }

    private fun show(content: @Composable () -> Unit) {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                DaftarTheme { Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { content() } }
            }
        }
        settle()
    }
    private fun settle() { rule.waitForIdle(); Thread.sleep(700); rule.waitForIdle() }
    private fun save(name: String, dialog: Boolean = false) {
        settle()
        val bmp = (if (dialog) rule.onNode(isDialog()) else rule.onRoot()).captureToImage().asAndroidBitmap()
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun click(text: String) { rule.onAllNodesWithText(text, substring = true).onFirst().performClick(); settle() }

    private fun grades(partial: Boolean): List<GradeEntity> {
        val rnd = Random(5)
        return students.flatMap { s -> listOf("MATS", "SN").flatMap { code ->
            listOf(GradeSlot.T1, GradeSlot.E1).mapNotNull { slot ->
                if (partial && code == "SN" && slot == GradeSlot.E1 && s.rollNumber > 6) null
                else GradeEntity(s.id, "c1", code, slot, rnd.nextInt(30, 78) / 4.0, 0L)
            } } }
    }

    @Test fun t01_login() { show { LoginScreen(onSignedIn = {}) }; save("t01-login") }

    @Test fun t02_years() {
        show {
            HomeYearsScreen(classes = emptyList(), currentInstitutionYear = null, selectedYear = null, mode = "teacher", onSelectMode = {},
                institutionName = null, teacherName = null, onTool = {}, schoolNames = emptyMap(), ownInstitutionId = null,
                activated = emptySet(), genderCounts = emptyList(), onSelectYear = {}, onOpenClass = {}, onDeleteClass = {},
                onCreateClass = {}, onOpenFile = {}, onMenu = {})
        }
        save("t02-years")
    }

    @Test fun t03_books() { show { BooksScreen(firstTime = true, isFr = false, onDone = {}, onMenu = null) }; save("t03-books") }

    @Test fun t04_hub() { show { TeacherYearHub(year, onPlanning = {}, onSchools = {}, onBack = {}, onMenu = {}) }; save("t04-hub") }

    private val docs = listOf(
        DocumentEntity("a", "", DocType.EXAM, "اختبار الفصل الأول", "التمرين الأول:\nأحسب ما يلي ...\n\nالتمرين الثاني:\nحل المعادلة ...",
            1795000000000, levelCode = "1AS", subjectCode = "MATS"),
        DocumentEntity("b", "", DocType.LESSON, "ملخص درس الكسور", "الكسر هو ...", 1794000000000, levelCode = "1AS"))

    private fun planning() = show {
        PlanningBrowserScreen(year = year, isFr = false, lockedFor = { _, _ -> false }, onActivate = {}, onBack = {}, onMenu = {},
            documents = { lv, subs -> LevelDocuments(lv, subs, docs.filter { it.levelCode == lv }, stamp, {}, {}, {}, {}) })
    }

    @Test fun t05_planning() {
        planning(); save("t05a-stages")
        click(ctx.getString(R.string.tact_stage_college)); save("t05b-levels")
        click("1AS"); save("t05c-weeks")
    }

    @Test fun t06_documents() {
        planning(); click(ctx.getString(R.string.tact_stage_college)); click("1AS")
        click(ctx.getString(R.string.doc_tab_mine)); save("t06a-docs")
        click("اختبار الفصل الأول"); save("t06b-doc-view")
    }

    @Test fun t07_new_document() {
        planning(); click(ctx.getString(R.string.tact_stage_college)); click("1AS")
        click(ctx.getString(R.string.doc_tab_mine)); click(ctx.getString(R.string.doc_new)); save("t07-doc-new")
    }

    @Test fun t08_prep_board() {
        val set = LessonBank.load(ctx, "MATS", "1AS")!!
        val l = set.lessons[2]
        show { LessonDialog(l, "Mathématiques", "Novembre S2", set.bookOf(l), set.bookTitleOf(l), "MATS") {} }
        save("t08a-prep", dialog = true)
        rule.onNodeWithText("🧑‍🏫", substring = true).performClick()
        save("t08b-board", dialog = true)
    }

    @Test fun t09_practice() {
        val set = PracticeBank.load(ctx, "MATS", "1AS")!!
        show { PracticeDialog(set.blocks.first(), set.lang, "Mathématiques", set.word) {} }
        save("t09-practice", dialog = true)
    }

    @Test fun t10_schools_empty() {
        show { TeacherSchoolsScreen(year, emptyList(), emptyList(), onPick = {}, onOpenFile = {}, onBack = {}, onMenu = {}) }
        save("t10-schools-empty")
    }

    @Test fun t11_file_received() {
        show {
            AlertDialog(onDismissRequest = {}, text = { Text(ctx.getString(R.string.sr_received, school, "1AS1، 2AS2", 4, 12)) },
                confirmButton = { TextButton(onClick = {}) { Text(ctx.getString(R.string.ok)) } })
        }
        save("t11-received", dialog = true)
    }

    private val timetable by lazy {
        listOf(0 to 0, 0 to 1, 1 to 2, 1 to 3, 2 to 0, 2 to 1, 3 to 4, 3 to 5, 4 to 0, 4 to 1, 5 to 2, 5 to 3).mapIndexed { i, (d, p) ->
            val cls = if (i % 2 == 0) "1AS1" else "2AS2"; val code = if (i % 4 < 2) "MATS" else "SN"
            val t = TimetableSettings().times[p]
            TeacherTimetableEntity("inst1|$year|$d|$p", "inst1", school, year, "محمد أحمد", d, p,
                "%02d:%02d".format(t.first / 60, t.first % 60), "%02d:%02d".format(t.second / 60, t.second % 60),
                cls, code, if (code == "MATS") "الرياضيات" else "الفيزياء والكيمياء",
                if (code == "MATS") "Mathématiques" else "Physique-Chimie", 0L)
        }
    }

    @Test fun t12_schools() {
        show { TeacherSchoolsScreen(year, listOf(TeacherSchool("inst1", school, listOf("1AS1", "2AS2"))), timetable,
            onPick = {}, onOpenFile = {}, onBack = {}, onMenu = {}) }
        save("t12-schools")
    }

    private fun classesHome(done: Boolean) = show {
        HomeYearsScreen(classes = listOf(c1, c2), currentInstitutionYear = year, selectedYear = year, mode = "teacher", onSelectMode = {},
            institutionName = school, teacherName = "محمد أحمد", onTool = {}, schoolNames = mapOf("inst1" to school), ownInstitutionId = null,
            activated = emptySet(), genderCounts = listOf(ClassGenderCount("c1", "F", 18), ClassGenderCount("c1", "M", 16),
                ClassGenderCount("c2", "F", 17), ClassGenderCount("c2", "M", 15)),
            onSelectYear = {}, onOpenClass = {}, onDeleteClass = {}, onCreateClass = {}, onOpenFile = {}, canCreateClass = false, onMenu = {},
            termProgress = listOf(TeacherTermProgress(1, if (done) listOf("1AS1 الرياضيات", "1AS1 الفيزياء", "2AS2 الرياضيات", "2AS2 الفيزياء")
                else listOf("1AS1 الرياضيات", "2AS2 الرياضيات"), 4)))
    }

    @Test fun t13_classes() { classesHome(false); save("t13-classes") }

    @Test fun t14_class() {
        show { TeacherClassScreen(c1, school, subj1, setOf("MATS", "SN"), grades(true), students.size, onSetMySubjects = {},
            onOpenSubject = {}, onShowStudents = {}, onBack = {}, subjectsFromAdmin = true) }
        save("t14-class")
    }

    @Test fun t15_grades() {
        show { GradesScreen(schoolClass = c1, subject = subj1.first { it.code == "SN" }, students = students,
            grades = grades(true).filter { it.subjectCode == "SN" && (it.slot != GradeSlot.E1 || it.studentId.removePrefix("s").toInt() < 6) },
            testsMode = TestsMode.AVERAGE, scale = RemarkScale.DEFAULT, onSetGrade = { _, _, _, _ -> }, onTestsMode = {}, onPrintRecord = {}, onBack = {}) }
        save("t15-grades")
    }

    @Test fun t16_upload() { classesHome(true); save("t16-upload") }
}
