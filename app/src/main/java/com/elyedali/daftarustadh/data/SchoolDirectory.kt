package com.elyedali.daftarustadh.data

import android.content.Context
import org.json.JSONObject
import java.security.MessageDigest

data class ListedSchool(val id: String, val name: String)

data class Wilaya(val name: String, val nameFr: String, val schools: List<ListedSchool>)

/**
 * لائحة الولايات ومؤسساتها (الإعداديات والثانويات) من ملف assets/schools/colleges_2026.json.
 * لكل مؤسسة في اللائحة معرّف ثابت مشتق من الولاية واسم المؤسسة، فيكون نفسه في كل الهواتف،
 * ويُستعمل في التحقق من الملفات حين تكون المؤسسة مختارة من اللائحة.
 */
object SchoolDirectory {
    private const val FILE = "schools/colleges_2026.json"
    @Volatile private var cache: List<Wilaya>? = null

    fun wilayas(context: Context): List<Wilaya> = cache ?: synchronized(this) {
        cache ?: load(context).also { cache = it }
    }

    fun wilaya(context: Context, name: String): Wilaya? = wilayas(context).firstOrNull { it.name == name }

    private fun load(context: Context): List<Wilaya> {
        val root = JSONObject(context.assets.open(FILE).bufferedReader(Charsets.UTF_8).use { it.readText() })
        val arr = root.getJSONArray("wilayas")
        return (0 until arr.length()).map { i ->
            val w = arr.getJSONObject(i)
            val name = w.getString("name")
            val s = w.getJSONArray("schools")
            Wilaya(
                name = name,
                nameFr = w.getString("name_fr"),
                schools = (0 until s.length()).map { j ->
                    val school = s.getString(j)
                    ListedSchool(id = stableId(name, school), name = school)
                }
            )
        }
    }

    private fun stableId(wilaya: String, school: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest("$wilaya|$school".toByteArray(Charsets.UTF_8))
        return "L" + bytes.take(8).joinToString("") { "%02x".format(it) }
    }

    /** للبحث: يوحّد الهمزات والتاء المربوطة والياء، فيجد المستخدم المؤسسة مهما كتبها. */
    fun normalize(text: String): String = text
        .replace(Regex("[أإآ]"), "ا")
        .replace('ة', 'ه')
        .replace('ى', 'ي')
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase()
}
