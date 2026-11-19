package com.elyedali.daftarustadh

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale

/** لغة لقطات فيديو الشرح: «fr» = واجهة فرنسية وبيانات مثال بالفرنسية، وغير ذلك = العربية. */
object TutorialLang {
    val fr: Boolean get() = InstrumentationRegistry.getArguments().getString("lang") == "fr"
    fun s(ar: String, fr: String) = if (this.fr) fr else ar
    val direction get() = if (fr) LayoutDirection.Ltr else LayoutDirection.Rtl

    /** يطبّق لغة الواجهة على النشاط والتطبيق قبل رسم الشاشات (كما يفعل اختيار اللغة في التطبيق). */
    @Suppress("DEPRECATION")
    fun apply(vararg contexts: Context) {
        if (!fr) return
        val loc = Locale("fr")
        Locale.setDefault(loc)
        contexts.forEach { c ->
            val cfg = Configuration(c.resources.configuration).apply { setLocale(loc); setLayoutDirection(loc) }
            c.resources.updateConfiguration(cfg, c.resources.displayMetrics)
        }
    }
}
