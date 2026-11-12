package com.elyedali.daftarustadh.data

import android.content.Context
import org.json.JSONObject

/** تمرين أو وضعية أو مقترح امتحان، مع حلّه. */
data class PracticeItem(val title: String, val text: String, val solution: String)

/** سلسلة أسبوع إدماج/تقويم/علاج، أو مقترحات أسبوع امتحان، أو المراجعة العامة. */
data class PracticeBlock(val week: String, val term: Int, val kind: String, val title: String,
                         val covers: List<String>, val items: List<PracticeItem>)

data class PracticeSet(val lang: String, val blocks: List<PracticeBlock>, /** «الوضعية» بدل «التمرين» في المواد الأدبية. */ val word: String = "") {
    /** سلسلة/مقترحات أسبوع من التخطيط (بنفس عنوان الأسبوع). */
    fun forWeek(label: String): PracticeBlock? = blocks.firstOrNull { it.week == label }
}

/** سلاسل التمارين ومقترحات الامتحانات (assets/practice/{المادة}_{المستوى}.json) — تطبيق الأستاذ. */
object PracticeBank {
    private val cache = HashMap<String, PracticeSet?>()

    fun load(context: Context, subject: String, level: String): PracticeSet? = cache.getOrPut("${subject}_$level") {
        runCatching {
            val o = JSONObject(context.assets.open("practice/${subject}_$level.json").bufferedReader().use { it.readText() })
            fun blocks(key: String) = o.optJSONArray(key)?.let { a ->
                (0 until a.length()).map { i ->
                    val b = a.getJSONObject(i)
                    val its = b.getJSONArray("items"); val cv = b.optJSONArray("covers")
                    PracticeBlock(b.optString("week"), b.optInt("term"), b.optString("kind"), b.optString("title"),
                        cv?.let { c -> (0 until c.length()).map { c.getString(it) } }.orEmpty(),
                        (0 until its.length()).map { k -> its.getJSONObject(k).let { x ->
                            PracticeItem(x.optString("title"), x.optString("text"), x.optString("solution")) } })
                }
            }.orEmpty()
            PracticeSet(o.optString("lang", "ar"), blocks("series") + blocks("exams"), o.optString("word"))
        }.getOrNull()
    }
}
