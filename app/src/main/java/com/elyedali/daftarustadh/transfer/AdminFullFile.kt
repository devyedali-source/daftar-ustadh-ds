package com.elyedali.daftarustadh.transfer

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.elyedali.daftarustadh.data.DaftarDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

/**
 * «ملف الإدارة الكامل»: كل معلومات تطبيق الإدارة في ملف واحد — المؤسسة والأقسام والطلاب والنتائج
 * وسجلات المواد والغياب والأساتذة والجداول الزمنية وإعدادات الكشوف.
 * يُرسل من تطبيق إدارة إلى تطبيق إدارة آخر (مدير الدروس ← المدير العام ← المراقب العام، أو عامل يساعد في الرصد).
 * لا يحمل التفعيل: كل تطبيق إدارة يُفعَّل وحده.
 */
class AdminFullFile(val json: JSONObject) {
    val schoolName: String get() = json.optString("schoolName")
    val exportedAt: Long get() = json.optLong("exportedAt")
    private fun rows(t: String): JSONArray = json.optJSONObject("tables")?.optJSONArray(t) ?: JSONArray()
    val classCount: Int get() = rows("school_class").length()
    val studentCount: Int get() = rows("student").length()

    /** نتيجة الإدراج. */
    sealed class Outcome {
        data class Done(val classes: Int, val students: Int, val grades: Int, val merged: List<String>) : Outcome()
        /** الملف لمؤسسة أخرى غير مؤسستك. */
        data class OtherInstitution(val schoolName: String) : Outcome()
    }

    companion object {
        const val TYPE = "admin_full"
        private const val VERSION = 1

        /** معلومات التطبيق التي تنتقل مع الملف (لا التفعيل ولا ما يخص هذا الهاتف وحده). */
        private val META_KEYS = listOf(
            "timetable_settings", "timetable_teachers", "timetable_approved", "timetable_manual",
            "pass_thresholds", "general_remarks", "teacher_remarks"
        )
        private const val META_PREFIX = "record_exported|"

        private val CLASS_TABLES = listOf("student", "grade", "subject_record", "timetable_assignment",
            "timetable_slot", "absence_session", "absence")

        private fun norm(t: String?) = t.orEmpty().trim().replace(Regex("\\s+"), " ").lowercase()

        private fun cursorRows(c: Cursor): JSONArray {
            val out = JSONArray()
            c.use {
                while (it.moveToNext()) {
                    val o = JSONObject()
                    for (i in 0 until it.columnCount) {
                        val name = it.getColumnName(i)
                        when (it.getType(i)) {
                            Cursor.FIELD_TYPE_NULL -> o.put(name, JSONObject.NULL)
                            Cursor.FIELD_TYPE_INTEGER -> o.put(name, it.getLong(i))
                            Cursor.FIELD_TYPE_FLOAT -> o.put(name, it.getDouble(i))
                            else -> o.put(name, it.getString(i))
                        }
                    }
                    out.put(o)
                }
            }
            return out
        }

        /** يبني الملف من قاعدة بيانات تطبيق الإدارة. */
        fun export(db: DaftarDatabase, schoolName: String, format: String): ByteArray {
            val r = db.openHelper.readableDatabase
            val tables = JSONObject()
            tables.put("institution", cursorRows(r.query("SELECT * FROM institution WHERE isOwn = 1")))
            val ownClasses = "SELECT id FROM school_class WHERE institutionId IN (SELECT id FROM institution WHERE isOwn = 1)"
            tables.put("school_class", cursorRows(r.query("SELECT * FROM school_class WHERE id IN ($ownClasses)")))
            CLASS_TABLES.forEach { t -> tables.put(t, cursorRows(r.query("SELECT * FROM $t WHERE classId IN ($ownClasses)"))) }
            val meta = JSONObject()
            r.query("SELECT `key`, value FROM app_meta").use { c ->
                while (c.moveToNext()) {
                    val k = c.getString(0)
                    if (k in META_KEYS || k.startsWith(META_PREFIX)) meta.put(k, c.getString(1))
                }
            }
            val json = JSONObject()
                .put("format", format).put("type", TYPE).put("version", VERSION)
                .put("schoolName", schoolName).put("exportedAt", System.currentTimeMillis())
                .put("tables", tables).put("meta", meta)
            val out = ByteArrayOutputStream()
            GZIPOutputStream(out).use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
            return out.toByteArray()
        }

        /** اسم الملف بالعربية والفرنسية حتى يعرفه المتلقي من واتساب. */
        fun fileName(schoolName: String): String {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH.mm", java.util.Locale.US).format(java.util.Date())
            val raw = "ملف الإدارة الكامل - Fichier complet de l'administration - $schoolName - $date"
            return raw.replace(Regex("""[\\/:*?"<>|\r\n\t]"""), " ").replace(Regex("\\s+"), " ").trim().take(150) + ".dsq"
        }
    }

    private fun columns(w: SupportSQLiteDatabase, table: String): Set<String> {
        val out = HashSet<String>()
        w.query("PRAGMA table_info($table)").use { c -> val i = c.getColumnIndex("name"); while (c.moveToNext()) out += c.getString(i) }
        return out
    }

    private fun values(o: JSONObject, cols: Set<String>): ContentValues {
        val v = ContentValues()
        o.keys().forEach { k ->
            if (k !in cols) return@forEach
            when (val x = o.get(k)) {
                JSONObject.NULL -> v.putNull(k)
                is Int -> v.put(k, x.toLong())
                is Long -> v.put(k, x)
                is Double -> v.put(k, x)
                is Boolean -> v.put(k, if (x) 1L else 0L)
                else -> v.put(k, x.toString())
            }
        }
        return v
    }

    private fun one(w: SupportSQLiteDatabase, sql: String, vararg args: Any?): String? =
        w.query(sql, args).use { c -> if (c.moveToFirst()) c.getString(0) else null }

    /**
     * يدرج الملف: يضيف الأقسام الجديدة ويحدّث الموجودة؛ النقطة الأحدث هي التي تبقى، فلا تُمحى نتيجة رُصدت عندك بعد الملف.
     * الجداول الزمنية والأساتذة وإعدادات الكشوف تأخذ ما في الملف.
     */
    fun apply(db: DaftarDatabase): Outcome {
        var outcome: Outcome? = null
        db.runInTransaction(Runnable {
            val w = db.openHelper.writableDatabase
            val instRows = rows("institution")
            val fi = if (instRows.length() > 0) instRows.getJSONObject(0) else null
            // المؤسسة: إن لم تكن عندك مؤسسة تصبح مؤسسة الملف مؤسستك؛ وإن كانت لك مؤسسة فيجب أن تكون نفسها
            var target: String? = null
            w.query("SELECT id, schoolName, wilaya, schoolListId FROM institution WHERE isOwn = 1 LIMIT 1").use { c ->
                if (c.moveToFirst()) {
                    val ownId = c.getString(0)
                    if (fi == null) { target = ownId; return@use }
                    val sameId = ownId == fi.optString("id")
                    val sameList = !c.isNull(3) && c.getString(3) == fi.optString("schoolListId", "")
                    val sameName = norm(c.getString(1)) == norm(fi.optString("schoolName")) && norm(c.getString(2)) == norm(fi.optString("wilaya"))
                    if (sameId) {
                        w.insert("institution", SQLiteDatabase.CONFLICT_REPLACE, values(fi, columns(w, "institution")).apply { put("isOwn", 1L) })
                        target = ownId
                    } else if (sameList || sameName) target = ownId
                }
            }
            if (target == null) {
                if (fi == null) { outcome = Outcome.Done(0, 0, 0, emptyList()); return@Runnable }
                val own = one(w, "SELECT id FROM institution WHERE isOwn = 1 LIMIT 1")
                if (own != null) { outcome = Outcome.OtherInstitution(fi.optString("schoolName")); return@Runnable }
                w.insert("institution", SQLiteDatabase.CONFLICT_REPLACE, values(fi, columns(w, "institution")).apply { put("isOwn", 1L) })
                target = fi.optString("id")
            }

            // الأقسام: نفس القسم (المؤسسة والسنة والمستوى والرقم) أنشئ عندك برمز آخر → يُدمج فيه
            val classCols = columns(w, "school_class")
            val classMap = HashMap<String, String>()
            val merged = ArrayList<String>()
            val cls = rows("school_class")
            for (i in 0 until cls.length()) {
                val c = cls.getJSONObject(i)
                val id = c.getString("id")
                c.put("institutionId", target)
                val exists = one(w, "SELECT id FROM school_class WHERE id = ?", id) != null
                if (exists) { w.insert("school_class", SQLiteDatabase.CONFLICT_REPLACE, values(c, classCols)); classMap[id] = id; continue }
                val slot = one(w, "SELECT id FROM school_class WHERE institutionId = ? AND schoolYear = ? AND levelCode = ? AND number = ?",
                    target, c.optString("schoolYear"), c.optString("levelCode"), c.optLong("number"))
                if (slot != null) { classMap[id] = slot; merged += c.optString("levelCode") + c.optLong("number") }
                else { w.insert("school_class", SQLiteDatabase.CONFLICT_REPLACE, values(c, classCols)); classMap[id] = id }
            }

            // الطلاب: في القسم المدموج يُطابق الطالب برقم النداء
            val stCols = columns(w, "student")
            val stMap = HashMap<String, String>()
            val sts = rows("student")
            for (i in 0 until sts.length()) {
                val s = sts.getJSONObject(i)
                val oldId = s.getString("id")
                val oldClass = s.getString("classId")
                val newClass = classMap[oldClass] ?: continue
                var newId = oldId
                if (newClass != oldClass) {
                    newId = one(w, "SELECT id FROM student WHERE classId = ? AND rollNumber = ?", newClass, s.optLong("rollNumber")) ?: oldId
                }
                s.put("id", newId); s.put("classId", newClass)
                w.insert("student", SQLiteDatabase.CONFLICT_REPLACE, values(s, stCols))
                stMap[oldId] = newId
            }

            // النقاط: الأحدث هو الذي يبقى
            val gCols = columns(w, "grade")
            var gradeCount = 0
            val gs = rows("grade")
            for (i in 0 until gs.length()) {
                val g = gs.getJSONObject(i)
                val sid = stMap[g.getString("studentId")] ?: continue
                val cid = classMap[g.getString("classId")] ?: continue
                g.put("studentId", sid); g.put("classId", cid)
                val mine = one(w, "SELECT updatedAt FROM grade WHERE studentId = ? AND subjectCode = ? AND slot = ?",
                    sid, g.optString("subjectCode"), g.optString("slot"))?.toLongOrNull()
                if (mine == null || g.optLong("updatedAt") >= mine) {
                    w.insert("grade", SQLiteDatabase.CONFLICT_REPLACE, values(g, gCols)); gradeCount++
                }
            }

            // سجلات المواد (اسم الأستاذ في الكشف): الأحدث يبقى
            val rCols = columns(w, "subject_record")
            val rs = rows("subject_record")
            for (i in 0 until rs.length()) {
                val r = rs.getJSONObject(i)
                val cid = classMap[r.getString("classId")] ?: continue
                r.put("classId", cid)
                val mine = one(w, "SELECT receivedAt FROM subject_record WHERE classId = ? AND subjectCode = ?", cid, r.optString("subjectCode"))?.toLongOrNull()
                if (mine == null || r.optLong("receivedAt") >= mine) w.insert("subject_record", SQLiteDatabase.CONFLICT_REPLACE, values(r, rCols))
            }

            // الغياب
            val sesCols = columns(w, "absence_session")
            val ses = rows("absence_session")
            for (i in 0 until ses.length()) {
                val s = ses.getJSONObject(i)
                val cid = classMap[s.getString("classId")] ?: continue
                s.put("classId", cid)
                w.insert("absence_session", SQLiteDatabase.CONFLICT_REPLACE, values(s, sesCols))
            }
            val abCols = columns(w, "absence")
            val abs = rows("absence")
            for (i in 0 until abs.length()) {
                val a = abs.getJSONObject(i)
                val cid = classMap[a.getString("classId")] ?: continue
                val sid = stMap[a.getString("studentId")] ?: continue
                a.put("classId", cid); a.put("studentId", sid)
                w.insert("absence", SQLiteDatabase.CONFLICT_REPLACE, values(a, abCols))
            }

            // الجداول الزمنية: جدول كل قسم في الملف يحلّ محلّ جدوله عندك
            classMap.values.toSet().forEach { cid ->
                w.delete("timetable_slot", "classId = ?", arrayOf(cid))
                w.delete("timetable_assignment", "classId = ?", arrayOf(cid))
            }
            listOf("timetable_assignment", "timetable_slot").forEach { t ->
                val cols = columns(w, t)
                val tr = rows(t)
                for (i in 0 until tr.length()) {
                    val o = tr.getJSONObject(i)
                    val cid = classMap[o.getString("classId")] ?: continue
                    o.put("classId", cid)
                    w.insert(t, SQLiteDatabase.CONFLICT_REPLACE, values(o, cols))
                }
            }

            // الأساتذة وإعدادات الجدول والكشوف
            val meta = json.optJSONObject("meta")
            meta?.keys()?.forEach { k ->
                if (k in META_KEYS || k.startsWith(META_PREFIX)) {
                    w.insert("app_meta", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply { put("key", k); put("value", meta.optString(k)) })
                }
            }
            outcome = Outcome.Done(classMap.size, stMap.size, gradeCount, merged)
        })
        return outcome ?: Outcome.Done(0, 0, 0, emptyList())
    }
}
