package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.TeacherTimetableEntity

/** مؤسسة يدرّس فيها الأستاذ في السنة المختارة. */
data class TeacherSchool(val id: String, val name: String, val classes: List<String>, val self: Boolean = false)

/**
 * تطبيق الأستاذ بعد اختيار السنة: جدوله الزمني الشامل لكل المؤسسات التي وصلته ملفاتها،
 * ثم اختيار المؤسسة لفتح أقسامها.
 */
@Composable
fun TeacherSchoolsScreen(
    year: String,
    schools: List<TeacherSchool>,
    timetable: List<TeacherTimetableEntity>,
    onPick: (String) -> Unit,
    onOpenFile: () -> Unit,
    onAddSelfSchool: () -> Unit = {},
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    var showTimetable by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.mode_teacher) + " — " + year, onMenu = onMenu, onBack = onBack)
            Box(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                TeacherTimetableView(
                    entries = timetable,
                    highlightClass = null,
                    highlightInstitution = null,
                    onOpenFile = onOpenFile,
                    showHeader = false,
                    onBack = onBack,
                    showOpenFile = false,
                    collapsed = !showTimetable,
                    before = {
                        // «جدولي الزمني»: عنوان يُضغط فيظهر الجدول تحته أو يُطوى
                        item {
                            Card(Modifier.fillMaxWidth().padding(top = 6.dp).clickable { showTimetable = !showTimetable },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("🗓️ " + stringResource(R.string.ts_my_timetable), fontWeight = FontWeight.Black, fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary)
                                        Text(stringResource(R.string.tt_week_hours, timetable.size), fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(if (showTimetable) "▴" else "▾", fontSize = 20.sp, fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    },
                    after = {
                        item {
                            Text("🏫 " + stringResource(R.string.ts_pick_school), fontWeight = FontWeight.Black, fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp))
                        }
                        if (schools.isEmpty()) item {
                            Text(stringResource(R.string.ts_no_school), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                        schools.forEach { sc ->
                            item(key = "school-" + sc.id) {
                                Card(
                                    Modifier.fillMaxWidth().clickable { onPick(sc.id) }, shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(46.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center) { Text("🏫", fontSize = 20.sp) }
                                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                            Text(sc.name, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            // مصدر المؤسسة: عمله الفردي أو ملف من إدارتها
                                            Text(stringResource(if (sc.self) R.string.ts_self_work else R.string.ts_from_admin), fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (sc.self) Color(0xFFB26A00) else Color(0xFF2E7D32))
                                            Text(if (sc.classes.isEmpty()) stringResource(R.string.ts_no_classes_yet)
                                                else stringResource(R.string.ts_classes, sc.classes.joinToString("، ")), fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Button(onClick = onOpenFile, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Text("📥 " + stringResource(R.string.ts_open_file), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        item {
                            androidx.compose.material3.OutlinedButton(onClick = onAddSelfSchool, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Text("➕ " + stringResource(R.string.ts_add_self_school), fontWeight = FontWeight.Bold)
                            }
                        }
                        item { Text(stringResource(R.string.ts_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        item { Text(stringResource(R.string.ts_self_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                )
            }
        }
    }
}
