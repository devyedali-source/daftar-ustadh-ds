package com.elyedali.daftarustadh.importer

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

/** خطأ مفهوم يُعرض للمستخدم عند تعذّر قراءة الملف. */
class SpreadsheetException(val reason: Reason) : Exception(reason.name) {
    enum class Reason { OLD_XLS, UNREADABLE, EMPTY }
}

/**
 * يقرأ ملف Excel (‎.xlsx) أو CSV بدون إنترنت وبدون مكتبات ثقيلة، ويرجع الأسطر كقوائم نصوص.
 * يُقرأ أول ورقة في ملف Excel.
 */
object SpreadsheetReader {

    fun read(context: Context, uri: Uri): List<List<String>> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw SpreadsheetException(SpreadsheetException.Reason.UNREADABLE)
        if (bytes.size < 4) throw SpreadsheetException(SpreadsheetException.Reason.EMPTY)
        val rows = when {
            // ‎.xlsx ملف مضغوط يبدأ بـ PK
            bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() -> readXlsx(bytes)
            // ‎.xls القديم يبدأ بـ D0 CF 11 E0
            bytes[0] == 0xD0.toByte() && bytes[1] == 0xCF.toByte() ->
                throw SpreadsheetException(SpreadsheetException.Reason.OLD_XLS)
            else -> readCsv(bytes)
        }
        val cleaned = rows.map { r -> r.map { cleanCell(it) } }.filter { r -> r.any { it.isNotBlank() } }
        if (cleaned.isEmpty()) throw SpreadsheetException(SpreadsheetException.Reason.EMPTY)
        val width = cleaned.maxOf { it.size }
        return cleaned.map { r -> r + List(width - r.size) { "" } }
    }

    /** يزيل الفراغات ويحوّل الأرقام مثل 12345678.0 إلى 12345678. */
    private fun cleanCell(raw: String): String {
        val t = raw.replace(' ', ' ').trim()
        return if (Regex("""^-?\d+\.0+$""").matches(t)) t.substringBefore('.') else t
    }

    // ---------------- XLSX ----------------

    private fun readXlsx(bytes: ByteArray): List<List<String>> {
        val entries = HashMap<String, ByteArray>()
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    val name = e.name
                    if (name == "xl/sharedStrings.xml" || name == "xl/workbook.xml" ||
                        name == "xl/_rels/workbook.xml.rels" || name.startsWith("xl/worksheets/sheet")
                    ) entries[name] = zip.readBytes()
                }
            }
        } catch (e: Exception) {
            throw SpreadsheetException(SpreadsheetException.Reason.UNREADABLE)
        }
        val shared = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) } ?: emptyList()
        val sheetPath = firstSheetPath(entries)
        val sheet = entries[sheetPath]
            ?: entries.keys.filter { it.startsWith("xl/worksheets/sheet") }.minOrNull()?.let { entries[it] }
            ?: throw SpreadsheetException(SpreadsheetException.Reason.UNREADABLE)
        return parseSheet(sheet, shared)
    }

    private fun parser(data: ByteArray): XmlPullParser =
        Xml.newPullParser().apply { setInput(ByteArrayInputStream(data), "UTF-8") }

    private fun parseSharedStrings(data: ByteArray): List<String> {
        val out = ArrayList<String>()
        val p = parser(data)
        var current: StringBuilder? = null
        var inPhonetic = false
        var inT = false
        while (p.eventType != XmlPullParser.END_DOCUMENT) {
            when (p.eventType) {
                XmlPullParser.START_TAG -> when (p.name) {
                    "si" -> current = StringBuilder()
                    "rPh" -> inPhonetic = true
                    "t" -> inT = true
                }
                XmlPullParser.TEXT -> if (inT && !inPhonetic) current?.append(p.text)
                XmlPullParser.END_TAG -> when (p.name) {
                    "si" -> { out.add(current?.toString() ?: ""); current = null }
                    "rPh" -> inPhonetic = false
                    "t" -> inT = false
                }
            }
            p.next()
        }
        return out
    }

    /** مسار أول ورقة حسب ترتيب الأوراق في الملف. */
    private fun firstSheetPath(entries: Map<String, ByteArray>): String {
        val wb = entries["xl/workbook.xml"] ?: return "xl/worksheets/sheet1.xml"
        var firstRid: String? = null
        val p = parser(wb)
        while (p.eventType != XmlPullParser.END_DOCUMENT && firstRid == null) {
            if (p.eventType == XmlPullParser.START_TAG && p.name == "sheet") {
                firstRid = p.getAttributeValue(null, "r:id")
            }
            p.next()
        }
        val rels = entries["xl/_rels/workbook.xml.rels"] ?: return "xl/worksheets/sheet1.xml"
        val r = parser(rels)
        while (r.eventType != XmlPullParser.END_DOCUMENT) {
            if (r.eventType == XmlPullParser.START_TAG && r.name == "Relationship" &&
                r.getAttributeValue(null, "Id") == firstRid
            ) {
                val target = r.getAttributeValue(null, "Target") ?: break
                return if (target.startsWith("/")) target.removePrefix("/") else "xl/" + target.removePrefix("./")
            }
            r.next()
        }
        return "xl/worksheets/sheet1.xml"
    }

    private fun columnIndex(ref: String): Int {
        var n = 0
        for (ch in ref) {
            if (ch in 'A'..'Z') n = n * 26 + (ch - 'A' + 1) else break
        }
        return n - 1
    }

    private fun parseSheet(data: ByteArray, shared: List<String>): List<List<String>> {
        val rows = sortedMapOf<Int, MutableMap<Int, String>>()
        val p = parser(data)
        var rowIndex = -1
        var colIndex = -1
        var type: String? = null
        var value = StringBuilder()
        var inValue = false
        var inCell = false
        while (p.eventType != XmlPullParser.END_DOCUMENT) {
            when (p.eventType) {
                XmlPullParser.START_TAG -> when (p.name) {
                    "row" -> {
                        rowIndex = p.getAttributeValue(null, "r")?.toIntOrNull()?.minus(1) ?: (rowIndex + 1)
                        colIndex = -1
                    }
                    "c" -> {
                        inCell = true
                        val ref = p.getAttributeValue(null, "r")
                        colIndex = if (ref != null) columnIndex(ref) else colIndex + 1
                        type = p.getAttributeValue(null, "t")
                        value = StringBuilder()
                    }
                    "v", "t" -> if (inCell) inValue = true
                }
                XmlPullParser.TEXT -> if (inValue) value.append(p.text)
                XmlPullParser.END_TAG -> when (p.name) {
                    "v", "t" -> inValue = false
                    "c" -> {
                        inCell = false
                        val raw = value.toString()
                        val text = when (type) {
                            "s" -> raw.trim().toIntOrNull()?.let { shared.getOrNull(it) } ?: ""
                            "b" -> if (raw == "1") "TRUE" else "FALSE"
                            else -> raw
                        }
                        if (rowIndex >= 0 && colIndex >= 0 && text.isNotEmpty()) {
                            rows.getOrPut(rowIndex) { mutableMapOf() }[colIndex] = text
                        }
                    }
                }
            }
            p.next()
        }
        return rows.values.map { cells ->
            val max = cells.keys.maxOrNull() ?: -1
            (0..max).map { cells[it] ?: "" }
        }
    }

    // ---------------- CSV ----------------

    private fun readCsv(bytes: ByteArray): List<List<String>> {
        var text = bytes.toString(Charsets.UTF_8).removePrefix("﻿")
        // ملفات CSV العربية القديمة قد تكون بترميز Windows-1256
        if (text.count { it == '�' } > 3) {
            text = try { bytes.toString(Charset.forName("windows-1256")) } catch (e: Exception) { text }
        }
        val firstLine = text.lineSequence().firstOrNull { it.isNotBlank() } ?: ""
        val delimiter = listOf(';', ',', '\t').maxBy { d -> firstLine.count { it == d } }
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            when {
                quoted && ch == '"' && i + 1 < text.length && text[i + 1] == '"' -> { cell.append('"'); i++ }
                ch == '"' -> quoted = !quoted
                !quoted && ch == delimiter -> { row.add(cell.toString()); cell.clear() }
                !quoted && (ch == '\n' || ch == '\r') -> {
                    if (ch == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row.add(cell.toString()); cell.clear(); rows.add(row); row = ArrayList()
                }
                else -> cell.append(ch)
            }
            i++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) { row.add(cell.toString()); rows.add(row) }
        return rows
    }
}
