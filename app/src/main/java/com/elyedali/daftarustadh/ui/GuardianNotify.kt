package com.elyedali.daftarustadh.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.exchange.ExchangeAd

/** رقم واتساب الوكيل: الهاتف الأول، وإلا الثاني (بصيغة 222xxxxxxxx). */
fun guardianWhatsApp(s: StudentEntity): String? =
    ExchangeAd.normalizePhone(s.guardianPhone1) ?: ExchangeAd.normalizePhone(s.guardianPhone2)

/**
 * إبلاغ وكلاء الغائبين بعد تسجيل الغياب: لكل غائب زر يفتح واتساب وكيله برسالة جاهزة.
 * @param when_ وصف الحصة: التاريخ ورقم الحصة والمادة
 */
/** رسالة الغياب للوكيل بالعربية ثم بالفرنسية دائمًا، أيًّا كانت لغة الواجهة. */
fun guardianMessage(st: StudentEntity, className: String, schoolName: String, date: String, period: Int, subjectAr: String, subjectFr: String,
                    time: String = ""): String {
    val t = if (time.isBlank()) "" else " \u2066$time\u2069"
    val nameAr = st.nameAr.ifBlank { st.nameFr }
    val nameFr = st.nameFr.ifBlank { st.nameAr }
    return "السلام عليكم ورحمة الله،\n" +
        "نُعلمكم أن الطالب(ة) $nameAr من القسم \u2066$className\u2069 تغيّب(ت) يوم \u2066$date\u2069 في الحصة $period$t ($subjectAr).\n" +
        "إدارة $schoolName\n\n" +
        "Bonjour,\n" +
        "Nous vous informons que l'élève $nameFr de la classe $className a été absent(e) le $date, séance $period${if (time.isBlank()) "" else " ($time)"} — $subjectFr.\n" +
        "L'administration de $schoolName"
}

@Composable
fun GuardianNotifyDialog(
    absent: List<StudentEntity>,
    className: String,
    schoolName: String,
    whenText: String,
    date: String,
    period: Int,
    subjectAr: String,
    subjectFr: String,
    time: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var sent by remember { mutableStateOf(setOf<String>()) }
    val noWhatsApp = stringResource(R.string.ex_no_whatsapp)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.gn_title, absent.size)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { Text(whenText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(absent.sortedBy { it.rollNumber }, key = { it.id }) { st ->
                    val phone = guardianWhatsApp(st)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("${st.rollNumber}. " + st.nameAr.ifBlank { st.nameFr }, fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                            if (phone == null) Text(stringResource(R.string.gn_no_phone), fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                        if (phone != null) Button(
                            onClick = {
                                val msg = guardianMessage(st, className, schoolName, date, period, subjectAr, subjectFr, time)
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=" + Uri.encode(msg))))
                                    sent = sent + st.id
                                } catch (e: Exception) {
                                    Toast.makeText(context, noWhatsApp, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (st.id in sent) Color(0xFF9E9E9E) else Color(0xFF25D366))
                        ) { Text(if (st.id in sent) "✅" else "🟢 " + stringResource(R.string.gn_send), fontSize = 12.sp) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) } }
    )
}
