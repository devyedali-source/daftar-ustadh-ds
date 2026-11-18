package com.elyedali.daftarustadh

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

/** لقطات متجر «دفتر الأستاذ DS» من الشاشات الحقيقية نفسها (نفس الكود ونفس محتوى الدروس)، ببيانات مثال للنتائج. */
@RunWith(AndroidJUnit4::class)
class TeacherStoreScreens {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val ctx get() = rule.activity
    private val out by lazy { File(ctx.filesDir, "store").apply { mkdirs() } }
    private val year = "2026-2027" // السنة الدراسية الحالية
    private val level = "1AS"
    private val subjects by lazy { CurriculumRepository.level(ctx, year, level)!!.subjects }
    private fun nameOf(code: String) = SubjectNames.display(subjects.first { it.code == code }, false)

    // منع لقطات الشاشة في التخطيط والتحاضير: يُرفع لهذا الاختبار وحده
    @Before fun allow() { ScreenSecurity.allowForStoreShots = true }

    private fun show(content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                DaftarTheme { Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { content() } }
            }
        }
        rule.waitForIdle(); Thread.sleep(800); rule.waitForIdle()
    }

    private fun save(name: String, dialog: Boolean) {
        rule.waitForIdle(); Thread.sleep(800); rule.waitForIdle()
        val node = if (dialog) rule.onNode(isDialog()) else rule.onRoot()
        val bmp = node.captureToImage().asAndroidBitmap()
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun t1_planning() {
        show {
            PlanningTab(levelCode = level, schoolYear = year,
                mySubjects = subjects.map { it.code to SubjectNames.display(it, false) })
        }
        save("1-planning", dialog = false)
    }

    @Test fun t2_prep() {
        val set = LessonBank.load(ctx, "IR", level)!!
        val l = set.lessons.first()
        show { LessonDialog(l, nameOf("IR"), "الأسبوع 3", set.bookOf(l), set.bookTitleOf(l), "IR") {} }
        save("2-prep", dialog = true)
    }

    @Test fun t3_board() {
        val set = LessonBank.load(ctx, "MATS", level)!!
        val l = set.lessons.first { it.title.startsWith("Chapitre 9") }
        show { LessonDialog(l, nameOf("MATS"), "Mai S1", set.bookOf(l), set.bookTitleOf(l), "MATS") {} }
        rule.onNodeWithText("🧑‍🏫", substring = true).performClick()
        save("3-board", dialog = true)
    }

    @Test fun t4_practice() {
        val set = PracticeBank.load(ctx, "MATS", level)!!
        show { PracticeDialog(set.blocks.first(), set.lang, nameOf("MATS"), set.word) {} }
        save("4-practice", dialog = true)
    }

    @Test fun t5_grades() {
        val names = listOf("آمنة أحمد" to "Amna Ahmed", "إبراهيم محمد" to "Brahim Mohamed", "أم كلثوم سيدي" to "Oum Kalthoum Sidi",
            "الحسن عبد الله" to "El Hacen Abdallahi", "خديجة محمد الأمين" to "Khadijetou Mohamed Lemine", "زينب أحمد سالم" to "Zeinebou Ahmed Salem",
            "سيدي محمد" to "Sidi Mohamed", "عائشة يحيى" to "Aichetou Yahya", "محمد المختار" to "Mohamed El Moctar", "مريم الشيخ" to "Mariem Cheikh")
        val students = names.mapIndexed { i, (ar, fr) ->
            StudentEntity("s$i", "c1", i + 1, ar, fr, "2201${i}45", "", "", "", 0L, if (i % 2 == 0) "F" else "M") }
        val rnd = Random(7)
        val grades = students.flatMap { s -> listOf(GradeSlot.T1, GradeSlot.E1).map { slot ->
            GradeEntity(s.id, "c1", "MATS", slot, rnd.nextInt(32, 76) / 4.0, 0L) } }
        val cls = ClassEntity("c1", "main", year, level, 1, 0L)
        show {
            GradesScreen(schoolClass = cls, subject = subjects.first { it.code == "MATS" }, students = students, grades = grades,
                testsMode = TestsMode.AVERAGE, scale = RemarkScale.DEFAULT, onSetGrade = { _, _, _, _ -> }, onTestsMode = {}, onPrintRecord = {}, onBack = {})
        }
        save("5-grades", dialog = false)
    }
}
