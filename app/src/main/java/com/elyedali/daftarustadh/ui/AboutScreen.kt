package com.elyedali.daftarustadh.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.AppRole
import com.elyedali.daftarustadh.BuildConfig
import com.elyedali.daftarustadh.R

/** صفحة «حول التطبيق»: قناة واتساب، التواصل مع المطور، والتطوير المستمر للمحتوى. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val noApp = stringResource(R.string.about_no_app)
    fun open(uri: String) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
        catch (e: Exception) { Toast.makeText(context, noApp, Toast.LENGTH_LONG).show() }
    }
    val appName = stringResource(R.string.app_name)
    val mailSubject = stringResource(R.string.about_mail_subject, appName)
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            ScreenHeader(stringResource(R.string.menu_about), onBack)
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // الشعار: الأيقونة متكيّفة (لا تُعرض مباشرة)، فتُرسم واجهتها على لون خلفيتها
                Image(painterResource(R.mipmap.ic_launcher_foreground), contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(RoundedCornerShape(22.dp))
                        .background(androidx.compose.ui.res.colorResource(R.color.ic_launcher_background)))
                Text(appName, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
                Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(if (AppRole.isAdmin) R.string.about_desc_admin else R.string.about_desc_teacher),
                    fontSize = 14.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)

                // قناة الواتساب
                AboutCard("📢 " + stringResource(R.string.about_channel_title), Color(0xFFE8F5E9)) {
                    Text(stringResource(R.string.about_channel_text), fontSize = 13.sp, lineHeight = 20.sp)
                    Button(onClick = { open(CHANNEL_URL) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White)) {
                        Text(stringResource(R.string.about_channel_button), fontWeight = FontWeight.Bold)
                    }
                }

                // التواصل مع المطور
                AboutCard("💡 " + stringResource(R.string.about_dev_title), Color(0xFFE3F2FD)) {
                    Text(stringResource(R.string.about_dev_text), fontSize = 13.sp, lineHeight = 20.sp)
                    Text(DEV_EMAIL, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = { open("mailto:$DEV_EMAIL?subject=" + Uri.encode(mailSubject)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("✉️ " + stringResource(R.string.about_dev_button), fontWeight = FontWeight.Bold)
                    }
                }

                // تطوير المحتوى مستمر
                AboutCard("🚀 " + stringResource(R.string.about_updates_title), Color(0xFFFFF8E1)) {
                    Text(stringResource(R.string.about_updates_text), fontSize = 13.sp, lineHeight = 20.sp)
                    listOf(R.string.about_upd_preps, R.string.about_upd_board, R.string.about_upd_exercises, R.string.about_upd_refs)
                        .forEach { Text("• " + stringResource(it), fontSize = 13.sp, lineHeight = 20.sp) }
                }

                Text(stringResource(R.string.drawer_footer), fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun AboutCard(title: String, bg: Color, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF263238))
            content()
        }
    }
}

private const val CHANNEL_URL = "https://whatsapp.com/channel/0029VbDrE3OKQuJJrvrBrI0d"
private const val DEV_EMAIL = "devyedali@gmail.com"
