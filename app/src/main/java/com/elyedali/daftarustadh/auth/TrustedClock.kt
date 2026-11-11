package com.elyedali.daftarustadh.auth

import android.content.Context
import android.os.SystemClock
import java.net.HttpURLConnection
import java.net.URL

/**
 * ساعة موثوقة لحساب مدة الاشتراكات، لا يغيّرها تعديل تاريخ الهاتف، وتعمل بلا إنترنت:
 * تأخذ أكبر قيمة من: ساعة الهاتف، وأكبر وقت رآه التطبيق (لا يرجع إلى الوراء)،
 * ووقت الإنترنت عند آخر اتصال + المدة المنقضية من عدّاد الهاتف الداخلي.
 */
object TrustedClock {
    private const val PREFS = "ds_trusted_clock"
    private const val K_MAX = "max_seen"
    private const val K_SERVER = "server_ms"
    private const val K_ELAPSED = "server_elapsed"
    private const val STEP = 10L * 60L * 1000L

    private val lock = Any()
    @Volatile private var ctx: Context? = null
    @Volatile private var loaded = false
    @Volatile private var maxSeen = 0L
    @Volatile private var persisted = 0L
    @Volatile private var serverMs = 0L
    @Volatile private var serverElapsed = 0L

    fun init(context: Context) { if (ctx == null) ctx = context.applicationContext; load() }

    private fun load() {
        if (loaded) return
        val c = ctx ?: return
        synchronized(lock) {
            if (loaded) return
            val p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            maxSeen = p.getLong(K_MAX, 0L); persisted = maxSeen
            serverMs = p.getLong(K_SERVER, 0L); serverElapsed = p.getLong(K_ELAPSED, 0L)
            loaded = true
        }
    }

    private fun persist() {
        val c = ctx ?: return
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(K_MAX, maxSeen).putLong(K_SERVER, serverMs).putLong(K_ELAPSED, serverElapsed).apply()
        persisted = maxSeen
    }

    private fun estimate(): Long {
        if (serverMs <= 0L || serverElapsed <= 0L) return 0L
        val now = SystemClock.elapsedRealtime()
        if (now < serverElapsed) return 0L   // أُعيد تشغيل الهاتف
        return serverMs + (now - serverElapsed)
    }

    /** الوقت الموثوق بالمللي ثانية. */
    fun now(): Long {
        val wall = System.currentTimeMillis()
        load()
        if (!loaded) return wall
        var t = maxOf(wall, maxSeen, estimate())
        if (t > maxSeen) synchronized(lock) {
            if (t > maxSeen) { maxSeen = t; if (t - persisted >= STEP) persist() }
        }
        return t
    }

    /** وقت الإنترنت هو المرجع النهائي عند الاتصال (يصحح ساعة ضُبطت خطأً إلى المستقبل). */
    fun onServerTime(ms: Long) {
        if (ms <= 0L) return
        load()
        synchronized(lock) { serverMs = ms; serverElapsed = SystemClock.elapsedRealtime(); maxSeen = ms; persist() }
    }

    /** يقرأ الوقت من ترويسة Date لخادم Google. يُستدعى من خيط خلفي. */
    fun syncWithServer(): Boolean {
        for (address in listOf("https://www.google.com/generate_204", "https://firestore.googleapis.com/")) {
            var conn: HttpURLConnection? = null
            try {
                conn = URL(address).openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"; conn.connectTimeout = 6000; conn.readTimeout = 6000
                conn.instanceFollowRedirects = false; conn.useCaches = false
                conn.connect()
                val d = conn.getHeaderFieldDate("Date", 0L)
                if (d > 0L) { onServerTime(d); return true }
            } catch (e: Exception) {
            } finally { try { conn?.disconnect() } catch (e: Exception) { } }
        }
        return false
    }
}
