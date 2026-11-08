package com.elyedali.daftarustadh.ocr

import android.content.Context
import android.net.Uri

/** تطبيق الأستاذ لا يقرأ النتائج من الصور (ميزة الإدارة)، فلا تُضمَّن مكتبة القراءة فيه. */
object RollOcr {
    @Suppress("UNUSED_PARAMETER")
    suspend fun read(context: Context, uri: Uri): String = ""
}
