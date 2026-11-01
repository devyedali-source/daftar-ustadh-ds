package com.elyedali.daftarustadh.transfer

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    /**
     * نوع خاص بملفات التطبيق (‎.dsq): بدونه يسمّيها واتساب «BIN»،
     * ومعه يظهر الملف باسمه وامتداده، ويفتح مباشرة بالتطبيق.
     */
    const val DSQ_MIME = "application/vnd.daftar-ustadh.dsq"

    /** يحفظ الملف مؤقتًا ثم يفتح نافذة المشاركة (واتساب، بلوتوث، المشاركة القريبة...). */
    fun share(context: Context, bytes: ByteArray, fileName: String, chooserTitle: String, mimeType: String = DSQ_MIME) {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, fileName).apply { writeBytes(bytes) }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            if (mimeType == DSQ_MIME) putExtra(Intent.EXTRA_TEXT, context.getString(com.elyedali.daftarustadh.R.string.dsq_caption))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** مجلد الملفات المؤقتة للمشاركة (يُفرَّغ قبل كل مشاركة). */
    fun freshShareDir(context: Context): File =
        File(context.cacheDir, "shared").apply { mkdirs(); listFiles()?.forEach { it.delete() } }

    /** يشارك عدة ملفات دفعة واحدة (مثل ملفات الوراقة). */
    fun shareFiles(context: Context, files: List<File>, mimeType: String, chooserTitle: String, text: String? = null) {
        val uris = ArrayList(files.map { FileProvider.getUriForFile(context, context.packageName + ".files", it) })
        val send = Intent(if (uris.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            if (uris.size == 1) putExtra(Intent.EXTRA_STREAM, uris[0]) else putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            if (text != null) putExtra(Intent.EXTRA_TEXT, text)
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
                type = DSQ_MIME
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
            type = DSQ_MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return false
    }
}
