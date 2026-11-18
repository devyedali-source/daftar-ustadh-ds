package com.elyedali.daftarustadh

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elyedali.daftarustadh.data.BookPack
import com.elyedali.daftarustadh.ui.BooksScreen
import com.elyedali.daftarustadh.ui.DaftarTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * تنزيل الكتب حسب اختيار الأستاذ، بنظام المتجر نفسه في وضع الاختبار المحلي (bundletool --local-testing):
 * لا يُنزَّل إلا كتب المواد والمستويات المختارة، وكتاب مادة أخرى ينزل وحده عند طلبه، ويُحذف عند الحذف.
 */
@RunWith(AndroidJUnit4::class)
class BookPacksTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val ctx get() = rule.activity

    private fun waitFor(what: String, timeoutMs: Long = 240_000, ok: () -> Boolean) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) { if (ok()) return; Thread.sleep(500) }
        throw AssertionError("انتهت المهلة: $what — الحالات: ${BookPack.states.value}")
    }

    /** صفحة «كتبي على الهاتف» الحقيقية (تُعرض مرة واحدة وتتابع الحالات وحدها). */
    private fun showScreen() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                DaftarTheme { BooksScreen(firstTime = false, isFr = false, onDone = {}, onMenu = {}) }
            }
        }
    }

    private fun shot(name: String) {
        // النزول إلى «حالة كتبي» حتى تظهر الكتب وحالاتها في اللقطة
        rule.waitForIdle()
        try { rule.onNode(hasScrollAction()).performScrollToNode(hasText("حالة كتبي")) } catch (e: Throwable) { }
        rule.waitForIdle(); Thread.sleep(800); rule.waitForIdle()
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap()
        val out = File(ctx.filesDir, "store").apply { mkdirs() }
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun downloadsOnlyChosenBooks() {
        val packs = BookPack.packs(ctx)
        assertTrue("فهرس الكتب فارغ", packs.size >= 28)
        rule.runOnUiThread { BookPack.start(ctx) }
        waitFor("حالة أولية") { BookPack.states.value.size == packs.size }
        assertTrue("كتاب منزّل قبل أي اختيار: ${BookPack.states.value}",
            BookPack.states.value.values.none { it is BookPack.Status.Ready })

        // أستاذ رياضيات وفيزياء في 2AS: كتابان فقط
        rule.runOnUiThread { BookPack.choose(ctx, setOf("MATS", "PC"), setOf("2AS")) }
        showScreen()
        waitFor("تنزيل كتابي الرياضيات والفيزياء") {
            BookPack.states.value["b_mats_2as"] is BookPack.Status.Ready && BookPack.states.value["b_pc_2as"] is BookPack.Status.Ready
        }
        for (path in listOf("books/MATS_2AS.pdf", "books/PC_2AS.pdf")) {
            val f = BookPack.file(ctx, path)
            assertNotNull("لم يوجد $path", f)
            assertEquals("حجم $path", packs.first { path in it.files }.bytes, f!!.length())
        }
        val others = BookPack.states.value.filterKeys { it != "b_mats_2as" && it != "b_pc_2as" }
        assertTrue("نزل كتاب غير مختار: $others", others.values.none { it is BookPack.Status.Ready })
        assertNull(BookPack.file(ctx, "books/IR_2AS.pdf"))
        shot("books-1-chosen")

        // درس من مادة أخرى: كتابها وحده (التاريخ والجغرافيا ملفان في حزمة واحدة)
        rule.runOnUiThread { BookPack.download(ctx, BookPack.packOf("books/HG_1AS_GEO.pdf")) }
        waitFor("تنزيل كتاب التاريخ والجغرافيا") { BookPack.states.value["b_hg_1as"] is BookPack.Status.Ready }
        assertNotNull(BookPack.file(ctx, "books/HG_1AS_GEO.pdf"))
        assertNotNull(BookPack.file(ctx, "books/HG_1AS_HIS.pdf"))
        shot("books-2-extra")

        // الحذف من الهاتف
        rule.runOnUiThread { BookPack.remove(ctx, "b_hg_1as") }
        waitFor("حذف الكتاب", 60_000) { BookPack.file(ctx, "books/HG_1AS_GEO.pdf") == null }
        assertTrue(BookPack.states.value["b_hg_1as"] is BookPack.Status.NotDownloaded)
        assertTrue("b_hg_1as" !in BookPack.selected(ctx))
        shot("books-3-removed")
    }
}
