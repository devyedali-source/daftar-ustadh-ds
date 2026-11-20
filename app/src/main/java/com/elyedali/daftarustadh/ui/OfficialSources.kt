package com.elyedali.daftarustadh.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.elyedali.daftarustadh.R

/** روابط المصادر الرسمية للمعلومات الحكومية في التطبيق (النصوص، الضوارب، لوائح المؤسسات). */
object OfficialSources {
    const val MINISTRY = "https://www.education.gov.mr"
    const val EXAMS = "https://dec.education.gov.mr"
}

/** فتح رابط في المتصفح، مع رسالة إن لم يوجد تطبيق يفتحه. */
@Composable
fun rememberOpenLink(): (String) -> Unit {
    val context = LocalContext.current
    val noApp = stringResource(R.string.about_no_app)
    return { url ->
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        catch (e: Exception) { Toast.makeText(context, noApp, Toast.LENGTH_LONG).show() }
    }
}

/** سطر رابط قابل للضغط: الاسم ثم العنوان. */
@Composable
fun SourceLink(label: String, url: String) {
    val open = rememberOpenLink()
    Column(Modifier.fillMaxWidth().clickable { open(url) }.padding(vertical = 4.dp)) {
        Text("🔗 $label", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text(url.removePrefix("https://"), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
    }
}

/** بطاقة «المصادر الرسمية»: موقع الوزارة وموقع الامتحانات. */
@Composable
fun OfficialSourcesCard(modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("🏛️ " + stringResource(R.string.src_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.src_text), style = MaterialTheme.typography.bodySmall)
            SourceLink(stringResource(R.string.src_ministry), OfficialSources.MINISTRY)
            SourceLink(stringResource(R.string.src_exams), OfficialSources.EXAMS)
        }
    }
}
