package com.elyedali.daftarustadh.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.SecureFlagPolicy

/**
 * منع لقطات الشاشة وتسجيلها (FLAG_SECURE) في التخطيط السنوي والتحاضير وسلاسل التمارين، كما في دفتر المعلم.
 * يُستثنى حساب مدير الأقسام (صاحب التطبيق) وحده.
 */
object ScreenSecurity {
    fun captureAllowed(): Boolean = try { com.elyedali.daftarustadh.auth.DsAuth.isOwner } catch (e: Exception) { false }

    /** للنوافذ المنبثقة (لها نافذة خاصة لا ترث منع الشاشة الرئيسية). */
    val dialogPolicy: SecureFlagPolicy get() = if (captureAllowed()) SecureFlagPolicy.Inherit else SecureFlagPolicy.SecureOn
}

/** يمنع التقاط الشاشة وتسجيلها ما دامت هذه الصفحة ظاهرة. */
@Composable
fun SecureScreen() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        // السياق قد يكون مغلّفًا (اللغة/السمة): نبحث عن النشاط نفسه
        var c: android.content.Context? = context
        while (c is android.content.ContextWrapper && c !is android.app.Activity) c = c.baseContext
        val window = (c as? android.app.Activity)?.window
        val on = !ScreenSecurity.captureAllowed()
        if (on) window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { if (on) window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE) }
    }
}
