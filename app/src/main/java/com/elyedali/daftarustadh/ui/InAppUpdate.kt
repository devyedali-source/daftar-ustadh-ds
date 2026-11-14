package com.elyedali.daftarustadh.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * تنبيه التحديث من متجر Google Play (كما في دفتر المعلم):
 * - يفحص المتجر مرة واحدة في كل تشغيل للتطبيق.
 * - يعرض «يتوفر إصدار جديد» مع «تحديث الآن» أو «لاحقًا» (تحديث مرن: يكمل المستخدم عمله أثناء التنزيل).
 * - بعد التنزيل يعرض «تثبيت الآن».
 * - بلا إنترنت أو بلا متجر (نسخة مثبتة من ملف APK) لا يظهر شيء ولا يعطّل المستخدم.
 */
object InAppUpdateManager {
    private const val TAG = "InAppUpdate"
    const val REQUEST_CODE = 8901

    private var manager: AppUpdateManager? = null
    private var cachedInfo: AppUpdateInfo? = null
    private var checked = false

    var showPrompt by mutableStateOf(false)
        private set
    var downloaded by mutableStateOf(false)
        private set

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) { downloaded = true; showPrompt = false }
    }

    private fun manager(context: Context): AppUpdateManager =
        manager ?: AppUpdateManagerFactory.create(context.applicationContext).also { manager = it; it.registerListener(listener) }

    fun checkOncePerSession(context: Context) {
        if (checked) return
        checked = true
        try {
            manager(context).appUpdateInfo.addOnSuccessListener { info ->
                cachedInfo = info
                when {
                    info.installStatus() == InstallStatus.DOWNLOADED -> downloaded = true
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> showPrompt = true
                }
            }.addOnFailureListener { e -> Log.d(TAG, "update check skipped: ${e.message}") }
        } catch (e: Exception) { Log.d(TAG, "update check error: ${e.message}") }
    }

    fun startUpdate(activity: Activity) {
        showPrompt = false
        val info = cachedInfo
        if (info != null && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            try {
                manager(activity).startUpdateFlowForResult(info, activity, AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(), REQUEST_CODE)
                return
            } catch (e: Exception) { Log.e(TAG, "update flow failed", e) }
        }
        openPlayStore(activity)
    }

    fun dismiss() { showPrompt = false }

    fun completeUpdate() { try { manager?.completeUpdate() } catch (e: Exception) { Log.e(TAG, "complete failed", e) } }

    /** عند الرجوع إلى التطبيق: تحديث نُزّل في الخلفية ينتظر التثبيت. */
    fun onResume() {
        try { manager?.appUpdateInfo?.addOnSuccessListener { if (it.installStatus() == InstallStatus.DOWNLOADED) downloaded = true } }
        catch (e: Exception) { Log.d(TAG, "resume check error: ${e.message}") }
    }

    fun openPlayStore(context: Context) {
        val pkg = context.packageName
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        catch (e: Exception) {
            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            catch (e2: Exception) { Log.d(TAG, "no store: ${e2.message}") }
        }
    }
}

/** نوافذ التحديث: «يتوفر إصدار جديد» ثم «تم تنزيل التحديث». */
@Composable
fun InAppUpdateDialogs(activity: Activity) {
    if (InAppUpdateManager.showPrompt) AlertDialog(
        onDismissRequest = { InAppUpdateManager.dismiss() },
        title = { Text("🔄 " + stringResource(R.string.upd_title), fontWeight = FontWeight.ExtraBold) },
        text = { Text(stringResource(R.string.upd_text, stringResource(R.string.app_name))) },
        confirmButton = { Button(onClick = { InAppUpdateManager.startUpdate(activity) }) { Text(stringResource(R.string.upd_now)) } },
        dismissButton = { TextButton(onClick = { InAppUpdateManager.dismiss() }) { Text(stringResource(R.string.upd_later)) } }
    )
    else if (InAppUpdateManager.downloaded) AlertDialog(
        onDismissRequest = { },
        title = { Text("✅ " + stringResource(R.string.upd_downloaded), fontWeight = FontWeight.ExtraBold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(stringResource(R.string.upd_downloaded_text)) } },
        confirmButton = { Button(onClick = { InAppUpdateManager.completeUpdate() }) { Text(stringResource(R.string.upd_install)) } }
    )
}
