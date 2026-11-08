package com.elyedali.daftarustadh.data

import android.content.Context
import com.elyedali.daftarustadh.AppRole
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * صور الكتب: حزمة «books» ينزّلها المتجر تلقائيًا بعد تثبيت تطبيق الأستاذ.
 * في نسخة التجربة (APK من GitHub) تكون الكتب داخل التطبيق نفسه فتعمل مباشرة.
 */
object BookPack {
    const val PACK = "books"

    sealed class Status {
        object Ready : Status()
        data class Downloading(val percent: Int) : Status()
        object WaitingWifi : Status()
        object Pending : Status()
        object Failed : Status()
    }

    private val _status = MutableStateFlow<Status>(Status.Pending)
    val status: StateFlow<Status> = _status

    private var manager: AssetPackManager? = null
    private var bundled: Boolean? = null
    private var listening = false

    private fun isBundled(context: Context): Boolean = bundled ?: try {
        !context.assets.list(PACK).isNullOrEmpty()
    } catch (e: Exception) { false }.also { bundled = it }

    private val listener = AssetPackStateUpdateListener { s: AssetPackState ->
        if (s.name() == PACK) _status.value = toStatus(s)
    }

    private fun toStatus(s: AssetPackState): Status = when (s.status()) {
        AssetPackStatus.COMPLETED -> Status.Ready
        AssetPackStatus.DOWNLOADING, AssetPackStatus.TRANSFERRING -> {
            val total = s.totalBytesToDownload()
            Status.Downloading(if (total > 0) (s.bytesDownloaded() * 100 / total).toInt().coerceIn(0, 100) else 0)
        }
        AssetPackStatus.WAITING_FOR_WIFI, AssetPackStatus.REQUIRES_USER_CONFIRMATION -> Status.WaitingWifi
        AssetPackStatus.FAILED, AssetPackStatus.CANCELED, AssetPackStatus.NOT_INSTALLED -> Status.Failed
        else -> Status.Pending
    }

    /** يُستدعى عند فتح تطبيق الأستاذ: إن لم تكتمل الحزمة يطلب تنزيلها ويتابع التقدّم. */
    fun start(context: Context) {
        if (AppRole.isAdmin) return
        val app = context.applicationContext
        if (isBundled(app)) { _status.value = Status.Ready; return }
        try {
            val m = manager ?: AssetPackManagerFactory.getInstance(app).also { manager = it }
            if (m.getPackLocation(PACK) != null) { _status.value = Status.Ready; return }
            if (!listening) { m.registerListener(listener); listening = true }
            m.fetch(listOf(PACK)).addOnFailureListener { _status.value = Status.Failed }
        } catch (e: Exception) {
            _status.value = Status.Failed
        }
    }

    /** إعادة المحاولة بعد فشل أو انتظار شبكة. */
    fun retry(context: Context) { _status.value = Status.Pending; start(context) }

    /** ملف الكتاب الجاهز للقراءة، أو null إن لم تُنزَّل الحزمة بعد. */
    fun file(context: Context, assetPath: String): File? {
        if (isBundled(context)) return null   // يُقرأ من داخل التطبيق كما كان
        val loc = try { (manager ?: AssetPackManagerFactory.getInstance(context.applicationContext).also { manager = it })
            .getPackLocation(PACK) } catch (e: Exception) { null } ?: return null
        return loc.assetsPath()?.let { File(it, assetPath) }?.takeIf { it.exists() }
    }

    fun bundledIn(context: Context) = isBundled(context)
}
