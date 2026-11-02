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

    /**
     * صفحة PDF تعرّف بالملف، وتحمل البيانات بعدها.
     * @param extra صفحات إضافية تُرسم بعد صفحة التعريف (مثل جدول الأستاذ)؛ تأخذ رقم الصفحة التالية وترجع الذي بعدها. */
    fun wrap(context: Context, data: ByteArray, title: String, extra: ((PdfDocument, Int) -> Int)? = null): ByteArray {
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
            extra?.invoke(doc, 2)
        }
        val payload = BEGIN + Base64.encodeToString(data, Base64.NO_WRAP) + END
        return appendDataObject(pdf, payload)
    }

    /**
     * يضيف البيانات داخل المستند نفسه كتحديث PDF نظامي (كائن جديد + جدول مراجع + startxref في آخر الملف)،
     * لا كنص ملصق بعد نهايته: برامج القراءة الصارمة (عارض Google وواتساب) تبحث عن startxref في آخر الملف،
     * وكانت ترفض الملف حين تكون البيانات بعده.
     */
    private fun appendDataObject(pdf: ByteArray, payload: String): ByteArray {
        val text = String(pdf, Charsets.ISO_8859_1)
        val size = Regex("""/Size\s+(\d+)""").findAll(text).last().groupValues[1].toInt()
        val root = Regex("""/Root\s+(\d+\s+\d+\s+R)""").findAll(text).last().groupValues[1]
        val info = Regex("""/Info\s+(\d+\s+\d+\s+R)""").findAll(text).lastOrNull()?.groupValues?.get(1)
        val prev = Regex("""startxref\s+(\d+)""").findAll(text).last().groupValues[1]
        val sb = StringBuilder(text)
        if (!text.endsWith("\n")) sb.append('\n')
        val objOffset = sb.length
        sb.append("$size 0 obj\n<< /Length ${payload.length} >>\nstream\n").append(payload).append("\nendstream\nendobj\n")
        val xrefOffset = sb.length
        sb.append("xref\n0 1\n0000000000 65535 f \n$size 1\n")
            .append(String.format(java.util.Locale.ROOT, "%010d 00000 n \n", objOffset))
            .append("trailer\n<< /Size ${size + 1} /Root $root").append(if (info != null) " /Info $info" else "")
            .append(" /Prev $prev >>\nstartxref\n$xrefOffset\n%%EOF\n")
        return sb.toString().toByteArray(Charsets.ISO_8859_1)
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
