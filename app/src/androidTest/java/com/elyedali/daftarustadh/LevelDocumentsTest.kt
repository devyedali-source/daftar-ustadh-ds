package com.elyedali.daftarustadh

import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elyedali.daftarustadh.data.*
import com.elyedali.daftarustadh.pdf.DocumentPdf
import com.elyedali.daftarustadh.ui.DaftarTheme
import com.elyedali.daftarustadh.ui.LevelDocuments
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * «تحريراتي» حسب المستوى: ترقية القاعدة (الوثائق القديمة تنتقل إلى مستوى قسمها دون ضياع)،
 * والصفحة (القائمة، العرض بالتاريخ والطابع)، وملف PDF بالطابع.
 */
@RunWith(AndroidJUnit4::class)
class LevelDocumentsTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val ctx get() = rule.activity
    private val out by lazy { File(ctx.filesDir, "store").apply { mkdirs() } }

    private fun save(name: String, bmp: Bitmap) =
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }

    @Test fun migrationKeepsOldDocumentsUnderTheirLevel() = runBlocking {
        val name = Institutions.dbName(Institutions.MAIN)
        ctx.deleteDatabase(name)
        // قاعدة بالإصدار الحالي، فيها قسم 2AS
        DaftarDatabase.get(ctx).let { db ->
            db.classDao().insert(ClassEntity("c1", "main", "2026-2027", "2AS", 1, 0L))
            db.close()
        }
        DaftarDatabase.switchTo(ctx, Institutions.MAIN)
        // إرجاع جدول الوثائق إلى شكل الإصدار 12 (قبل المستوى) بوثيقة كُتبت داخل القسم
        SQLiteDatabase.openDatabase(ctx.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { w ->
            w.execSQL("DROP TABLE `document`")
            w.execSQL("CREATE TABLE IF NOT EXISTS `document` (`id` TEXT NOT NULL, `classId` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                "`title` TEXT NOT NULL, `body` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            w.execSQL("CREATE INDEX IF NOT EXISTS `index_document_classId` ON `document` (`classId`)")
            w.execSQL("INSERT INTO `document` VALUES ('d-old', 'c1', 'exam', 'امتحان قديم', 'نص', 1700000000000)")
            w.version = 12
        }
        // فتح القاعدة: الترقية 12 → 13 ثم تحقق Room من الجداول
        val db = DaftarDatabase.get(ctx)
        val docs = db.documentDao().observeByLevel("2AS").first()
        assertEquals(listOf("d-old"), docs.map { it.id })
        assertEquals("", docs.single().subjectCode)
        // وثيقة جديدة لمستوى، وحذف القسم لا يحذف تحريرات الأستاذ
        db.documentDao().save(DocumentEntity("d-new", "", DocType.EXAM, "Devoir 1", "Exercice 1 ...", 1800000000000, levelCode = "2AS", subjectCode = "MATS"))
        db.documentDao().deleteByClass("c1")
        assertEquals(setOf("d-old", "d-new"), db.documentDao().observeByLevel("2AS").first().map { it.id }.toSet())
        assertTrue(db.documentDao().observeByLevel("1AS").first().isEmpty())
    }

    @Test fun screenAndPdfWithStamp() {
        val stamp = "الأستاذ: محمد أحمد\nأستاذ مادة الرياضيات والفيزياء\nإعدادية النجاح"
        val docs = listOf(
            DocumentEntity("a", "", DocType.EXAM, "اختبار الفصل الأول", "التمرين الأول:\nأحسب ما يلي ...\n\nالتمرين الثاني:\nحل المعادلة ...", 1800000000000, levelCode = "1AS", subjectCode = "MATS"),
            DocumentEntity("b", "", DocType.REQUEST, "طلب رخصة", "السيد المدير ...", 1790000000000, levelCode = "1AS"))
        val stampState = mutableStateOf(stamp)
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                DaftarTheme {
                    LevelDocuments("1AS", listOf("MATS" to "Mathématiques", "PC" to "Physique-Chimie"), docs, stampState.value,
                        onSaveStamp = { stampState.value = it }, onSave = {}, onDelete = {}, onExport = {})
                }
            }
        }
        rule.waitForIdle()
        save("docs-1-list", rule.onRoot().captureToImage().asAndroidBitmap())
        rule.onNodeWithText("اختبار الفصل الأول").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("آخر تعديل", substring = true).assertExists()
        save("docs-2-view", rule.onRoot().captureToImage().asAndroidBitmap())

        // ملف PDF: المستوى، مادة الامتحان، النص، وأسطر الطابع
        val bytes = DocumentPdf.build(DocumentPdf.Info("المستوى: 1AS", "Niveau : 1AS", "المادة: الرياضيات — Matière : Mathématiques",
            docs[0].title, docs[0].body, stamp.lines()))
        val f = File(out, "doc.pdf").apply { writeBytes(bytes) }
        ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            val r = PdfRenderer(pfd)
            val p = r.openPage(0)
            val bmp = Bitmap.createBitmap(1080, (1080f * p.height / p.width).toInt(), Bitmap.Config.ARGB_8888)
            bmp.eraseColor(Color.WHITE)
            p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            p.close(); r.close()
            save("docs-3-pdf", bmp)
        }
    }
}
