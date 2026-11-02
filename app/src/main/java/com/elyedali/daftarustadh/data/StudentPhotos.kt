package com.elyedali.daftarustadh.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import java.io.File

/**
 * صور الطلاب (لبطاقات الطلاب): تُحفظ داخل التطبيق باسم معرّف الطالب، مصغّرة (480 بكسل) لتبقى خفيفة.
 * لا تُنقل في ملفات الأقسام إلى الأساتذة.
 */
object StudentPhotos {
    private const val MAX = 480

    private fun dir(context: Context) = File(context.filesDir, "student_photos").apply { mkdirs() }

    fun file(context: Context, studentId: String) = File(dir(context), "$studentId.jpg")

    fun exists(context: Context, studentId: String) = file(context, studentId).exists()

    fun load(context: Context, studentId: String): Bitmap? =
        file(context, studentId).takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.absolutePath) }

    fun delete(context: Context, studentId: String) { file(context, studentId).delete() }

    /** يحفظ الصورة المختارة أو الملتقطة: تصغير، وتدوير حسب اتجاه الكاميرا. يرجع true عند النجاح. */
    fun save(context: Context, studentId: String, uri: Uri): Boolean = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > MAX * 2 || bounds.outHeight / sample > MAX * 2) sample *= 2
        val raw = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IllegalStateException()
        val rotation = context.contentResolver.openInputStream(uri)?.use {
            when (androidx.exifinterface.media.ExifInterface(it).getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL)) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val scale = minOf(1f, MAX.toFloat() / maxOf(raw.width, raw.height))
        val m = Matrix().apply { postScale(scale, scale); postRotate(rotation) }
        val out = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
        file(context, studentId).outputStream().use { out.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        true
    } catch (e: Exception) {
        false
    }
}
