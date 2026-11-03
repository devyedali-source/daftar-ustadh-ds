package com.elyedali.daftarustadh

/**
 * التطبيقان: «إدارة DS» للإدارة و«دفتر الأستاذ DS» للأستاذ، من مشروع واحد (BuildConfig.ROLE).
 */
object AppRole {
    const val ADMIN_APP_NAME = "إدارة DS"
    const val TEACHER_APP_NAME = "دفتر الأستاذ DS"

    /** رابط تحميل تطبيق الأستاذ: يُرسل مع ملف الأستاذ. يُكتب هنا عند رفع التطبيق (Play Store أو غيره). */
    const val TEACHER_APP_LINK = ""

    val isAdmin: Boolean get() = BuildConfig.ROLE == "admin"
}
