package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R

/**
 * القائمة الجانبية كما في دفتر المعلم: رأس ملوّن بالشعار واسم التطبيق والمؤسسة،
 * ثم: الصفحة الرئيسية والأقسام، التبادلات، التشريع، كتبي؛ وفي الأسفل المزامنة وتسجيل الخروج.
 */
@Composable
fun AppDrawer(
    schoolName: String?,
    schoolYear: String?,
    current: String,
    lastSync: String?,
    onItem: (String) -> Unit
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        modifier = Modifier.width(310.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(
                            // شعار الجمهورية الإسلامية الموريتانية
                            painter = painterResource(R.drawable.img_mauritania_seal), contentDescription = null,
                            modifier = Modifier.size(58.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column {
                            Text(stringResource(R.string.app_name) + " 🏫", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text(schoolName ?: stringResource(R.string.drawer_no_school), fontWeight = FontWeight.Bold, fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (schoolYear != null) Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(stringResource(R.string.home_year_label, schoolYear), Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.drawer_menu), fontWeight = FontWeight.Bold, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            Item("🏫", R.string.drawer_home, current == "HOME") { onItem("HOME") }
            Item("🤝", R.string.menu_exchanges, current == "EXCHANGES") { onItem("EXCHANGES") }
            Item("⚖️", R.string.menu_legislation, current == "LEGISLATION") { onItem("LEGISLATION") }
            Item("📚", R.string.menu_books, false) { onItem("BOOKS") }

            Spacer(Modifier.weight(1f))
            HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

            // المزامنة (Google Drive) كما في دفتر المعلم
            Surface(
                color = Color(0xFFF1F8E9), shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("☁️ " + stringResource(R.string.drawer_sync_title), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                    Text(stringResource(R.string.drawer_last_sync, lastSync ?: stringResource(R.string.drawer_never_synced)),
                        fontSize = 10.5.sp, color = Color(0xFF33691E))
                    Button(
                        onClick = { onItem("SYNC") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White)
                    ) { Text("🔄 " + stringResource(R.string.drawer_sync), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
            }

            Row(Modifier.padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌐", fontSize = 16.sp)
                FilterChip(selected = !isFr, onClick = { onItem("LANG_AR") }, label = { Text("العربية") })
                FilterChip(selected = isFr, onClick = { onItem("LANG_FR") }, label = { Text("Français") })
            }

            OutlinedButton(
                onClick = { onItem("LOGOUT") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                border = BorderStroke(1.dp, Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)
            ) { Text("🚪 " + stringResource(R.string.drawer_logout), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            Text(stringResource(R.string.drawer_footer), fontSize = 10.sp, color = Color.Gray,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Item(icon: String, label: Int, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(stringResource(label), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        icon = { Text(icon, fontSize = 18.sp) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp)
    )
}
