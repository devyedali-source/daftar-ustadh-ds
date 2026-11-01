package com.elyedali.daftarustadh.transfer

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    /** يحفظ الملف مؤقتًا ثم يفتح نافذة المشاركة (واتساب، بلوتوث، المشاركة القريبة...). */
    fun share(context: Context, bytes: ByteArray, fileName: String, chooserTitle: String, mimeType: String = "application/octet-stream") {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, fileName).apply { writeBytes(bytes) }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /**
     * يرسل الملف مباشرة إلى محادثة رقم معيّن في واتساب (أو واتساب الأعمال).
     * إن لم يكن واتساب مثبتًا تُفتح نافذة المشاركة العادية. يرجع true إن فُتح واتساب.
     * @param phone الرقم الدولي بدون + (مثل 22236000000)
     */
    fun shareToWhatsApp(context: Context, bytes: ByteArray, fileName: String, phone: String, caption: String, chooserTitle: String): Boolean {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, fileName).apply { writeBytes(bytes) }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                setPackage(pkg)
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                putExtra("jid", "$phone@s.whatsapp.net")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(send)
                return true
            } catch (e: android.content.ActivityNotFoundException) {
            }
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return false
    }
}
