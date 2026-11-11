package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** رأس الشاشة الموحّد: شريط ملوّن بزر رجوع وعنوان وسطور فرعية وأزرار جانبية. */
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)?,
    subtitles: List<String> = emptyList(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(primary, primary.copy(alpha = 0.82f))))
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        } else Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            subtitles.filter { it.isNotBlank() }.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** زر نصي أبيض داخل رأس الشاشة. */
@Composable
fun HeaderAction(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.labelLarge
    )
}

/** بطاقة إجراء رئيسية: أيقونة دائرية، عنوان، ووصف قصير. */
@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    description: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    container: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = tint) }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** خانة رقم إحصائي: قيمة كبيرة وتحتها وصفها. */
@Composable
fun StatTile(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f))) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** عنوان قسم داخل الشاشة. */
@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
}

/**
 * زر «من جهات الاتصال»: يفتح قائمة أرقام الهاتف ويرجع الرقم المختار بصيغة محلية (8 أرقام إن أمكن).
 * لا يحتاج إذن قراءة جهات الاتصال: النظام يعطي التطبيق الرقم المختار فقط.
 */
@Composable
fun ContactPhoneButton(onPicked: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val uri = res.data?.data ?: return@rememberLauncherForActivityResult
        val number = try {
            context.contentResolver.query(uri,
                arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (e: Exception) { null } ?: return@rememberLauncherForActivityResult
        val normalized = com.elyedali.daftarustadh.exchange.ExchangeAd.normalizePhone(number)
        onPicked(normalized?.let { com.elyedali.daftarustadh.exchange.ExchangeAd.localPhone(it) }
            ?: number.filter { it.isDigit() || it == '+' || it == ' ' })
    }
    androidx.compose.material3.OutlinedButton(
        onClick = {
            val intent = android.content.Intent(android.content.Intent.ACTION_PICK,
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            try { launcher.launch(intent) } catch (e: Exception) {
                android.widget.Toast.makeText(context, context.getString(com.elyedali.daftarustadh.R.string.contact_pick_unavailable),
                    android.widget.Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("📇 " + androidx.compose.ui.res.stringResource(com.elyedali.daftarustadh.R.string.contact_pick)) }
}

/** فاصل القوائم حسب لغة التطبيق: «، » بالعربية و«, » بالفرنسية. */
fun listSep(): String {
    val lang = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().get(0)?.language
        ?: java.util.Locale.getDefault().language
    return if (lang == "fr") ", " else "، "
}
