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
 * يحتوي رقم النداء والنقطة، مرتبًا من الأعلى إلى الأسفل. الأسطر غير المفهومة تُترك لتصحّحها الإدارة.
 */
object RollOcr {
    suspend fun read(context: Context, uri: Uri): String = suspendCancellableCoroutine { cont ->
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            cont.resume(""); return@suspendCancellableCoroutine
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                // نجمع الكلمات حسب الصف (الارتفاع) لأن العمودين (الرقم والنقطة) قد يُقرآن كتلتين منفصلتين
                val words = result.textBlocks.flatMap { b -> b.lines.flatMap { l -> l.elements } }
                    .mapNotNull { e -> e.boundingBox?.let { box -> Triple(box.centerY(), box.left, e.text) } }
                val avgH = result.textBlocks.flatMap { it.lines }.mapNotNull { it.boundingBox?.height() }.average().takeIf { !it.isNaN() } ?: 30.0
                val rows = ArrayList<MutableList<Triple<Int, Int, String>>>()
                words.sortedBy { it.first }.forEach { w ->
                    val row = rows.lastOrNull()
                    if (row != null && kotlin.math.abs(row.first().first - w.first) < avgH * 0.6) row.add(w) else rows.add(mutableListOf(w))
                }
                // من اليمين إلى اليسار كما في الأوراق العربية (رقم النداء يمينًا)؛ ويمكن قلب الترتيب في الشاشة
                val text = rows.joinToString("\n") { r -> r.sortedByDescending { it.second }.joinToString(" ") { it.third } }
                recognizer.close()
                cont.resume(text)
            }
            .addOnFailureListener { recognizer.close(); cont.resume("") }
    }
}
