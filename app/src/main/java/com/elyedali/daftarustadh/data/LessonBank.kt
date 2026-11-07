package com.elyedali.daftarustadh.data

import android.content.Context
import org.json.JSONObject
import java.io.File

/** رسم من الكتاب يُقتطع من صفحته: rect = (يسار، أعلى، يمين، أسفل) كنسب من عرض الصفحة وطولها. */
data class BoardFigure(val page: Int, val rect: List<Float>, val caption: String)

/** قسم من محتوى السبورة: عنوان وأسطر، وجدول (الصف الأول عناوين الأعمدة) أو رسم من الكتاب إن وُجدا. */
data class BoardSection(
    val heading: String,
    val lines: List<String>,
    val table: List<List<String>> = emptyList(),
    val figure: BoardFigure? = null
)

/**
 * درس من الكتاب المدرسي مرتبط بعنوان في التخطيط السنوي:
 * صفحاته في الكتاب، أهدافه، تحضير قصير (مقدمة، ملخص، أهم النقاط، تطبيق)، ومحتوى السبورة.
 */
data class BookLesson(
    val title: String,
    val match: List<String>,
    val pageFrom: Int,
    val pageTo: Int,
    val objectives: List<String>,
    val intro: String,
    val summary: String,
    val points: List<String>,
    val applicationQ: String,
    val applicationA: String,
    val board: List<BoardSection>,
    /** كتاب الدرس إن كان للمادة أكثر من كتاب (مثل التاريخ والجغرافيا). */
    val book: String = "",
    val bookTitle: String = ""
)

/** دروس مادة لمستوى: الملف assets/lessons/{المادة}_{المستوى}.json والكتاب في assets/books. */
data class LessonSet(val book: String, val bookTitle: String, val lessons: List<BookLesson>) {
    /** occurrence: ترتيب تكرار العنوان نفسه في التخطيط (مثل «مفاهيم جغرافية» ثلاث مرات) — الدروس المتطابقة تؤخذ بترتيبها. */
    fun find(planTitle: String, occurrence: Int = 0): BookLesson? {
        val c = lessons.filter { planTitle in it.match }
        return c.getOrNull(occurrence) ?: c.firstOrNull()
    }
    fun bookOf(l: BookLesson): String = l.book.ifBlank { book }
    fun bookTitleOf(l: BookLesson): String = l.bookTitle.ifBlank { bookTitle }
}

object LessonBank {
    private val cache = HashMap<String, LessonSet?>()

    fun load(context: Context, subject: String, level: String): LessonSet? = synchronized(cache) {
        val key = "${subject}_$level"
        cache.getOrPut(key) {
            try {
                val o = JSONObject(context.assets.open("lessons/$key.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
                fun strs(a: org.json.JSONArray?) = (0 until (a?.length() ?: 0)).map { a!!.getString(it) }
                val ls = o.getJSONArray("lessons")
                LessonSet(o.optString("book"), o.optString("bookTitle"), (0 until ls.length()).map { i ->
                    val l = ls.getJSONObject(i)
                    val pages = l.getJSONArray("pages")
                    val app = l.optJSONObject("application")
                    val b = l.optJSONArray("board")
                    BookLesson(
                        title = l.getString("title"), match = strs(l.optJSONArray("match")),
                        pageFrom = pages.getInt(0), pageTo = pages.getInt(1),
                        objectives = strs(l.optJSONArray("objectives")), intro = l.optString("intro"),
                        summary = l.optString("summary"), points = strs(l.optJSONArray("points")),
                        applicationQ = app?.optString("q").orEmpty(), applicationA = app?.optString("a").orEmpty(),
                        board = (0 until (b?.length() ?: 0)).map { j ->
                            val s = b!!.getJSONObject(j)
                            val t = s.optJSONArray("table")
                            val f = s.optJSONObject("fig")
                            BoardSection(s.optString("h"), strs(s.optJSONArray("lines")),
                                (0 until (t?.length() ?: 0)).map { r -> strs(t!!.getJSONArray(r)) },
                                f?.let { fo ->
                                    val r = fo.getJSONArray("rect")
                                    BoardFigure(fo.getInt("page"), (0 until r.length()).map { r.getDouble(it).toFloat() }, fo.optString("caption"))
                                })
                        },
                        book = l.optString("book"), bookTitle = l.optString("bookTitle")
                    )
                })
            } catch (e: Exception) { null }
        }
    }

    /** نسخة الكتاب في ملفات التطبيق (PdfRenderer يحتاج ملفًا لا ملف assets). */
    fun bookFile(context: Context, assetPath: String): File? = try {
        val out = File(context.filesDir, assetPath)
        val size = context.assets.openFd(assetPath).use { it.length }
        if (!out.exists() || (size > 0 && out.length() != size)) {
            out.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input -> out.outputStream().use { input.copyTo(it) } }
        }
        out
    } catch (e: Exception) {
        // openFd يفشل إن كان الملف مضغوطًا داخل التطبيق: ننسخه مرة واحدة فقط
        try {
            val out = File(context.filesDir, assetPath)
            if (!out.exists()) {
                out.parentFile?.mkdirs()
                context.assets.open(assetPath).use { input -> out.outputStream().use { input.copyTo(it) } }
            }
            out
        } catch (e2: Exception) { null }
    }
}
