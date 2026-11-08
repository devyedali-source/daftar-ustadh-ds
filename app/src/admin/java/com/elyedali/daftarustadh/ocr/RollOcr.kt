package com.elyedali.daftarustadh.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * قراءة ورقة النتائج من صورة (على الهاتف، بلا إنترنت): يرجع سطرًا لكل صف في الورقة
 * يحتوي رقم النداء والنقطة. الأسطر غير المفهومة تُترك لتصحّحها الإدارة.
 *
 * الورقة المكتوبة باليد تكون غالبًا مائلة قليلًا في الصورة، فلا نعتمد على تطابق ارتفاع العمودين:
 * نفصل عمود أرقام النداء (يمينًا) عن عمود النقاط (يسارًا) ثم نقرن كل رقم بنقطته حسب الترتيب من الأعلى.
 */
object RollOcr {

    private data class Tok(val x: Int, val y: Int, val h: Int, val text: String)

    /** تصحيح الحروف التي تُقرأ مكان الأرقام في خط اليد (مثل l بدل 1 و O بدل 0 و 5 المعقوفة بعد الفاصلة). */
    fun normalize(raw: String): String {
        var t = raw.trim()
        // 5 بعد الفاصلة تُكتب كثيرًا بشكل معقوف فتُقرأ حرفًا (Γ أو r أو T…)، أو تضيع فتبقى الفاصلة وحدها
        t = t.replace(Regex("""([.,])\s*[ΓГTrτſ7]?$""")) { m ->
            if (m.value.length == 1 || !m.value.last().isDigit()) "${m.groupValues[1]}5" else m.value
        }
        val map = mapOf(
            'O' to '0', 'o' to '0', 'D' to '0', 'Q' to '0',
            'l' to '1', 'I' to '1', 'i' to '1', '|' to '1', '!' to '1', 'L' to '1', 'J' to '1', '/' to '1', 'Λ' to '1',
            'Z' to '2', 'z' to '2',
            'S' to '5', 's' to '5', 'Γ' to '5', 'Г' to '5',
            'G' to '6', 'b' to '6',
            'T' to '7',
            'B' to '8',
            'g' to '9', 'q' to '9'
        )
        return t.map { map[it] ?: it }.filter { it.isDigit() || it == '.' || it == ',' }.joinToString("")
            .trim('.', ',').let { if (it.count { c -> c == '.' || c == ',' } > 1) it.replace(",", "").replace(".", "") else it }
    }

    /** يقرن عمودين من الأعداد: يفصلهما بأكبر فراغ أفقي ثم يقرن بالترتيب من الأعلى. */
    private fun pair(toks: List<Tok>): String {
        if (toks.isEmpty()) return ""
        val xs = toks.map { it.x }.sorted()
        var cut = -1; var gap = 0
        for (i in 1 until xs.size) if (xs[i] - xs[i - 1] > gap) { gap = xs[i] - xs[i - 1]; cut = (xs[i] + xs[i - 1]) / 2 }
        val avgH = toks.map { it.h }.average().takeIf { !it.isNaN() } ?: 30.0
        // عمود واحد فقط (لا فراغ واضح): نرجع للطريقة القديمة حسب الصف
        if (cut < 0 || gap < avgH) return byRows(toks, avgH)
        // العمود الأيمن = أرقام النداء، والأيسر = النقاط (كما في الأوراق العربية)؛ ويمكن قلب الترتيب من الشاشة
        val right = merge(toks.filter { it.x > cut }, avgH)
        val left = merge(toks.filter { it.x <= cut }, avgH)
        val lines = ArrayList<String>()
        if (right.size == left.size) {
            right.zip(left).forEach { (r, l) -> lines += "${r.text} ${l.text}" }
        } else {
            // عدد مختلف: كل رقم نداء مع أقرب نقطة له ارتفاعًا لم تُستعمل بعد
            val used = HashSet<Int>()
            right.forEach { r ->
                val j = left.indices.filter { it !in used }.minByOrNull { kotlin.math.abs(left[it].y - r.y) }
                if (j != null && kotlin.math.abs(left[j].y - r.y) < avgH * 1.5) { used += j; lines += "${r.text} ${left[j].text}" }
                else lines += r.text
            }
            left.indices.filter { it !in used }.forEach { lines += left[it].text }
        }
        return lines.joinToString("\n")
    }

    /** يدمج القطع المتجاورة في نفس الصف داخل العمود (مثل «12,» و«5»)، مرتبة من الأعلى. */
    private fun merge(col: List<Tok>, avgH: Double): List<Tok> {
        val rows = ArrayList<MutableList<Tok>>()
        col.sortedBy { it.y }.forEach { t ->
            val row = rows.lastOrNull()
            if (row != null && kotlin.math.abs(row.first().y - t.y) < avgH * 0.5) row.add(t) else rows.add(mutableListOf(t))
        }
        return rows.mapNotNull { r ->
            val txt = normalize(r.sortedBy { it.x }.joinToString("") { it.text })
            if (txt.isEmpty()) null else Tok(r.map { it.x }.average().toInt(), r.map { it.y }.average().toInt(), r.maxOf { it.h }, txt)
        }
    }

    private fun byRows(toks: List<Tok>, avgH: Double): String {
        val rows = ArrayList<MutableList<Tok>>()
        toks.sortedBy { it.y }.forEach { w ->
            val row = rows.lastOrNull()
            if (row != null && kotlin.math.abs(row.first().y - w.y) < avgH * 0.6) row.add(w) else rows.add(mutableListOf(w))
        }
        return rows.joinToString("\n") { r -> r.sortedByDescending { it.x }.joinToString(" ") { it.text } }
    }

    suspend fun read(context: Context, uri: Uri): String = suspendCancellableCoroutine { cont ->
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            cont.resume(""); return@suspendCancellableCoroutine
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                // كل كلمة مقروءة بموضعها؛ نُبقي ما فيه رقم بعد التصحيح (العناوين مثل «الرقم» و«امتحان» تُهمل)
                val toks = result.textBlocks.flatMap { b -> b.lines.flatMap { l -> l.elements } }
                    .mapNotNull { e -> e.boundingBox?.let { box -> Tok(box.centerX(), box.centerY(), box.height(), e.text) } }
                    .filter { normalize(it.text).any { c -> c.isDigit() } || it.text.trim().let { s -> s.length <= 2 } }
                val text = try { pair(toks) } catch (e: Exception) { "" }
                recognizer.close()
                cont.resume(text)
            }
            .addOnFailureListener { recognizer.close(); cont.resume("") }
    }
}
