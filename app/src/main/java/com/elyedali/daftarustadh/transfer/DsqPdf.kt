package com.elyedali.daftarustadh.transfer

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.util.Base64
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.pdf.PdfCommon
import com.elyedali.daftarustadh.pdf.PdfText

/**
 * ملفات التطبيق تُرسل في صورة PDF عادي: يظهر في واتساب «PDF» ويُقرأ ويُطبع كأي وثيقة،
 * وتُحمل بيانات التطبيق داخله بعد نهاية المستند (تتجاهلها برامج قراءة PDF)،
 * فيفتحه التطبيق بزر «فتح ملف» ويستخرج البيانات منه.
 */
object DsqPdf {
    private const val BEGIN = "\n%DAFTAR-USTADH-DS-DATA-BEGIN\n"
    private const val END = "\n%DAFTAR-USTADH-DS-DATA-END\n"
    private const val W = 595f
    private const val H = 842f
    private const val M = 28f

    /** صفحة PDF تعرّف بالملف، وتحمل البيانات بعدها. */
    fun wrap(context: Context, data: ByteArray, title: String): ByteArray {
        val seal = BitmapFactory.decodeResource(context.resources, R.drawable.img_mauritania_seal)
        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(java.util.Date())
        val pdf = PdfCommon.render { doc ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), 1).create())
            val c = page.canvas
            PdfCommon.drawOfficialHeader(c, seal, W)
            var y = M + PdfCommon.HEADER + 30f
            PdfText.draw(c, title, M, y, W - 2 * M, 70f, 15f, bold = true, maxLines = 3)
            y += 90f
            val box = RectF(M + 20f, y, W - M - 20f, y + 230f)
            c.drawRoundRect(box, 14f, 14f, Paint().apply { color = 0xFFF3EDF7.toInt(); style = Paint.Style.FILL })
            c.drawRoundRect(box, 14f, 14f, Paint().apply { color = 0xFF6750A4.toInt(); style = Paint.Style.STROKE; strokeWidth = 1.5f })
            val text = listOf(
                "📂 ملف بيانات من تطبيق «دفتر الأستاذ DS»",
                "لإضافة محتواه: افتح تطبيق دفتر الأستاذ DS ثم اضغط «فتح ملف» واختر هذا الملف،",
                "أو اضغط على الملف في واتساب واختر «دفتر الأستاذ DS».",
                "Fichier de données de l'application « دفتر الأستاذ DS »",
                "Pour l'ajouter : ouvrez l'application puis « Ouvrir un fichier » et choisissez ce fichier."
            )
            var ty = y + 16f
            text.forEachIndexed { i, line ->
                val h = if (i == 0 || i == 3) 34f else 40f
                PdfText.draw(c, line, box.left + 14f, ty, box.width() - 28f, h, if (i == 0 || i == 3) 12.5f else 10.5f,
                    bold = i == 0 || i == 3, maxLines = 2)
                ty += h + 4f
            }
            PdfText.draw(c, "⁦$date⁩ — دفتر الأستاذ DS", M, H - M - 20f, W - 2 * M, 16f, 9f)
            doc.finishPage(page)
        }
        val payload = Base64.encodeToString(data, Base64.NO_WRAP).toByteArray(Charsets.US_ASCII)
        return pdf + BEGIN.toByteArray(Charsets.US_ASCII) + payload + END.toByteArray(Charsets.US_ASCII)
    }

    fun isPdf(bytes: ByteArray): Boolean = bytes.size >= 4 && String(bytes, 0, 4, Charsets.ISO_8859_1) == "%PDF"

    /** يستخرج بيانات التطبيق من ملف PDF أرسله التطبيق، أو null إن كان PDF عاديًا. */
    fun extract(bytes: ByteArray): ByteArray? {
        val text = String(bytes, Charsets.ISO_8859_1)
        val b = text.lastIndexOf(BEGIN)
        if (b < 0) return null
        val e = text.indexOf(END, b + BEGIN.length)
        if (e < 0) return null
        return try {
            Base64.decode(text.substring(b + BEGIN.length, e), Base64.NO_WRAP)
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}
