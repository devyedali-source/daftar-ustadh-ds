package com.elyedali.daftarustadh.data

import android.content.Context
import com.elyedali.daftarustadh.AppRole
import org.json.JSONArray
import org.json.JSONObject

/**
 * مؤسسات الحساب في تطبيق الإدارة: كل مؤسسة لها قاعدة بيانات منفصلة تمامًا (أقسام، تلاميذ، أساتذة،
 * جدول زمني، إعدادات) ونسختها الخاصة في Drive واشتراكها الخاص على الخادم. لا تختلط مؤسستان أبدًا.
 * المؤسسة الأولى «main» هي قاعدة البيانات القديمة نفسها (لا يضيع شيء مما سُجّل قبل هذه الميزة).
 */
object Institutions {
    const val MAIN = "main"
    private const val PREFS = "ds_institutions"
    private const val K_ACTIVE = "active"
    private const val K_LIST = "list"

    data class Entry(val key: String, val name: String, val wilaya: String)

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** المؤسسة المفتوحة الآن (تطبيق الأستاذ: دائمًا main). */
    fun active(c: Context): String = if (!AppRole.isAdmin) MAIN else prefs(c).getString(K_ACTIVE, MAIN) ?: MAIN

    fun dbName(key: String) = if (key == MAIN) "daftar_ustadh_ds.db" else "daftar_ustadh_ds_$key.db"

    fun list(c: Context): List<Entry> {
        val out = try {
            val a = JSONArray(prefs(c).getString(K_LIST, "[]"))
            (0 until a.length()).map { a.getJSONObject(it).let { o -> Entry(o.optString("key"), o.optString("name"), o.optString("wilaya")) } }
        } catch (e: Exception) { emptyList() }
        return if (out.none { it.key == MAIN }) listOf(Entry(MAIN, "", "")) + out else out
    }

    private fun save(c: Context, list: List<Entry>) {
        prefs(c).edit().putString(K_LIST, JSONArray(list.distinctBy { it.key }.map {
            JSONObject().put("key", it.key).put("name", it.name).put("wilaya", it.wilaya)
        }).toString()).apply()
    }

    /** يحدّث اسم المؤسسة المفتوحة في القائمة (بعد تسجيلها أو تعديلها). */
    fun rename(c: Context, key: String, name: String, wilaya: String) {
        val l = list(c)
        if (l.any { it.key == key && it.name == name && it.wilaya == wilaya }) return
        save(c, l.map { if (it.key == key) it.copy(name = name, wilaya = wilaya) else it }.let { if (it.none { e -> e.key == key }) it + Entry(key, name, wilaya) else it })
    }

    /** مؤسسة وُجدت نسختها في Drive (من هاتف آخر) ولم تكن في هذا الهاتف. */
    fun addKnown(c: Context, key: String, name: String, wilaya: String) {
        val l = list(c)
        if (l.any { it.key == key }) { if (name.isNotBlank()) rename(c, key, name, wilaya); return }
        save(c, l + Entry(key, name, wilaya))
    }

    /** مؤسسة جديدة فارغة. */
    fun create(c: Context): String {
        val key = java.util.UUID.randomUUID().toString().replace("-", "").take(12)
        save(c, list(c) + Entry(key, "", ""))
        return key
    }

    fun setActive(c: Context, key: String) { prefs(c).edit().putString(K_ACTIVE, key).commit() }

    /** حساب آخر دخل إلى الهاتف: تُحذف قواعد المؤسسات الإضافية ويرجع إلى main فقط. */
    fun resetAll(c: Context) {
        list(c).filter { it.key != MAIN }.forEach { c.applicationContext.deleteDatabase(dbName(it.key)) }
        prefs(c).edit().clear().commit()
    }
}
