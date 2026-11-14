package com.elyedali.daftarustadh.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elyedali.daftarustadh.auth.DsAuth
import com.elyedali.daftarustadh.data.DaftarDatabase
import java.util.concurrent.TimeUnit

/**
 * مزامنة Drive الدورية في الخلفية (كل 6 ساعات، عند وجود إنترنت) حتى لو لم يُفتح التطبيق:
 * ما سُجّل يصل إلى Drive، وما سُجّل على الهاتف الآخر يصل إلى هذا الهاتف.
 */
class DriveSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (DsAuth.user == null) return Result.success()
        return try {
            val db = DaftarDatabase.get(applicationContext)
            DriveSync.init(applicationContext, db)
            when (DriveSync.sync(applicationContext, db)) {
                is DriveSync.State.Done, is DriveSync.State.NeedsConsent -> Result.success()   // الموافقة تُطلب داخل التطبيق
                else -> Result.retry()
            }
        } catch (e: Exception) { Result.retry() }
    }

    companion object {
        private const val NAME = "ds_drive_sync_periodic"
        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<DriveSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, req)
        }
        fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(NAME)
    }
}
