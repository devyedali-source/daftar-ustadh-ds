package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.ClassGenderCount
import com.elyedali.daftarustadh.data.Gender
import com.elyedali.daftarustadh.data.SchoolYear

/** شريط علوي كما في دفتر المعلم: لون أساسي، زر القائمة أو الرجوع، شعار التطبيق، والعنوان. */
@Composable
fun AppTopBar(title: String, onMenu: (() -> Unit)?, onBack: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
        }
        if (onMenu != null) IconButton(onClick = onMenu, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White)
        }
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground), contentDescription = null,
            modifier = Modifier.padding(horizontal = 4.dp).size(32.dp).clip(CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(title, Modifier.weight(1f).padding(horizontal = 6.dp), color = Color.White, fontWeight = FontWeight.Black,
            fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * الصفحة الرئيسية كما في دفتر المعلم:
 * 1) اختيار السنة الدراسية (بطاقة لكل سنة وعدد أقسامها)، 2) أقسام السنة المختارة،
 * مع زر «إنشاء قسم» العائم (الإدارة) وزر «فتح ملف قسم» (الأستاذ).
 */
@Composable
fun HomeYearsScreen(
    classes: List<ClassEntity>,            // أقسام الإدارة وأقسام الأستاذ من كل السنوات
    currentInstitutionYear: String?,
    selectedYear: String?,
    schoolNames: Map<String, String>,
    ownInstitutionId: String?,
    activated: Set<String>,
    genderCounts: List<ClassGenderCount>,
    onSelectYear: (String?) -> Unit,
    onOpenClass: (ClassEntity) -> Unit,
    onDeleteClass: (ClassEntity) -> Unit,
    onCreateClass: () -> Unit,
    onOpenFile: () -> Unit,
    onMenu: () -> Unit
) {
    val current = SchoolYear.current()
    val years = remember(classes, currentInstitutionYear) {
        (classes.map { it.schoolYear } + listOfNotNull(currentInstitutionYear)).distinct().sortedDescending()
    }
    var toDelete by remember { mutableStateOf<ClassEntity?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                AppTopBar(
                    title = if (selectedYear == null) stringResource(R.string.home_choose_year_title)
                    else stringResource(R.string.home_year_title, selectedYear),
                    onMenu = onMenu,
                    onBack = if (selectedYear != null) ({ onSelectYear(null) }) else null
                )
                if (selectedYear == null) {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Text(stringResource(R.string.home_choose_year_prompt), fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        if (years.isEmpty()) item {
                            EmptyCard(stringResource(R.string.home_no_years), stringResource(R.string.home_no_years_desc))
                        }
                        items(years, key = { it }) { year ->
                            val count = classes.count { it.schoolYear == year }
                            Card(
                                Modifier.fillMaxWidth().clickable { onSelectYear(year) }, shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(46.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text("📚", fontSize = 22.sp) }
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.home_year_label, "⁦$year⁩"), fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            if (year == current) {
                                                Spacer(Modifier.size(8.dp))
                                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                                    Text("● " + stringResource(R.string.home_year_ongoing), Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(stringResource(R.string.home_year_classes, count), fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                } else {
                    val yearClasses = classes.filter { it.schoolYear == selectedYear }
                        .sortedWith(compareBy({ it.levelCode }, { it.number }))
                    val ids = yearClasses.map { it.id }.toSet()
                    val counts = genderCounts.filter { it.classId in ids }
                    val total = counts.sumOf { it.count }
                    val boys = counts.filter { it.gender == Gender.BOY }.sumOf { it.count }
                    val girls = counts.filter { it.gender == Gender.GIRL }.sumOf { it.count }
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    MiniStat("🏫", yearClasses.size, stringResource(R.string.stat_classes))
                                    MiniStat("👥", total, stringResource(R.string.stat_students))
                                    MiniStat("👦", boys, stringResource(R.string.stat_boys))
                                    MiniStat("👧", girls, stringResource(R.string.stat_girls))
                                }
                            }
                        }
                        item {
                            Text(stringResource(R.string.home_year_classes_title, selectedYear, yearClasses.size),
                                fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        if (yearClasses.isEmpty()) item {
                            EmptyCard(stringResource(R.string.home_no_classes_year), stringResource(R.string.home_no_classes_year_desc))
                        }
                        items(yearClasses, key = { it.id }) { c ->
                            val students = counts.filter { it.classId == c.id }.sumOf { it.count }
                            Card(
                                Modifier.fillMaxWidth().clickable { onOpenClass(c) }, shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text(c.levelCode, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(c.name, fontWeight = FontWeight.Black, fontSize = 17.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (c.managed) Badge(stringResource(R.string.badge_admin), Color(0xFFE3F2FD), Color(0xFF1565C0))
                                            if (c.teaching) Badge(stringResource(R.string.badge_teacher), Color(0xFFF3E5F5), Color(0xFF6A1B9A))
                                            if (c.managed) {
                                                val on = c.id in activated
                                                Badge(
                                                    stringResource(if (on) R.string.act_status_active else R.string.act_status_inactive),
                                                    if (on) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                                    if (on) Color(0xFF2E7D32) else Color(0xFFE65100)
                                                )
                                            }
                                        }
                                        Text(
                                            listOfNotNull(levelDescription(c.levelCode),
                                                schoolNames[c.institutionId]?.takeIf { c.institutionId != ownInstitutionId || !c.managed },
                                                stringResource(R.string.home_students_n, students)).joinToString("  |  "),
                                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2
                                        )
                                    }
                                    IconButton(onClick = { toDelete = c }) {
                                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFC62828))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // الأزرار العائمة: إنشاء قسم (الإدارة) وفتح ملف قسم (الأستاذ)
            Column(
                Modifier.align(Alignment.BottomEnd).padding(16.dp),
                horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(onClick = onOpenFile, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  " + stringResource(R.string.open_class_file), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                ExtendedFloatingActionButton(
                    onClick = onCreateClass,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.create_class), fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White
                )
            }
        }
    }
    toDelete?.let { c ->
        SensitiveConfirmDialog(
            title = stringResource(R.string.delete_class_title, c.name),
            message = stringResource(R.string.delete_class_text),
            onConfirm = { onDeleteClass(c); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun MiniStat(icon: String, value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text("$value", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Badge(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(text, Modifier.padding(horizontal = 7.dp, vertical = 2.dp), color = fg, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyCard(title: String, desc: String) {
    Card(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📭", fontSize = 38.sp)
            Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(desc, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
        }
    }
}
