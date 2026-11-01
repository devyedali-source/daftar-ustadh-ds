package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R

/**
 * القائمة الجانبية كما في دفتر المعلم: رأس ملوّن بالشعار واسم التطبيق والمؤسسة،
 * ثم «القائمة الجانبية للتطبيق 📌» وعناصرها، ثم أدوات الإدارة، ثم اللغة.
 */
@Composable
fun AppDrawer(
    schoolName: String?,
    schoolYear: String?,
    current: String,
    onItem: (String) -> Unit
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        modifier = Modifier.width(310.dp)
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher_foreground), contentDescription = null,
                            modifier = Modifier.size(52.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
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
            Item("📅", R.string.tt_title, current == "TIMETABLE") { onItem("TIMETABLE") }
            Item("🗓️", R.string.tt_my_timetable, current == "MY_TIMETABLE") { onItem("MY_TIMETABLE") }
            Item("🤝", R.string.menu_exchanges, current == "EXCHANGES") { onItem("EXCHANGES") }
            Item("⚖️", R.string.menu_legislation, current == "LEGISLATION") { onItem("LEGISLATION") }
            Item("📚", R.string.menu_books, false) { onItem("BOOKS") }
            HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
            Text(stringResource(R.string.admin_section), fontWeight = FontWeight.Bold, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            Item("📊", R.string.statistics, current == "STATS") { onItem("STATS") }
            Item("⭐", R.string.act_title, current == "ACTIVATION") { onItem("ACTIVATION") }
            Item("🏛️", R.string.drawer_institution, current == "INSTITUTION") { onItem("INSTITUTION") }
            Item("✏️", R.string.remarks_settings, current == "REMARKS") { onItem("REMARKS") }
            Item("📂", R.string.open_class_file, false) { onItem("OPEN_FILE") }
            HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
            Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌐", fontSize = 16.sp)
                FilterChip(selected = !isFr, onClick = { onItem("LANG_AR") }, label = { Text("العربية") })
                FilterChip(selected = isFr, onClick = { onItem("LANG_FR") }, label = { Text("Français") })
            }
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
