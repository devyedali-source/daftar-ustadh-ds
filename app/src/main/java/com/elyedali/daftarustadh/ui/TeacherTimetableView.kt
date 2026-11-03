package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.TeacherTimetableEntity

/**
 * جدول الأستاذ كما وصله من إدارات المؤسسات التي يدرّس فيها:
 * كل الأيام مرتبة بالتوقيت، مع تصفية حسب المؤسسة، وإبراز القسم الحالي إن وُجد.
 * تُكشف الحصص المتداخلة بين مؤسستين (نفس اليوم والتوقيت).
 */
@Composable
fun TeacherTimetableView(
    entries: List<TeacherTimetableEntity>,
    highlightClass: String?,
    highlightInstitution: String?,
    onOpenFile: () -> Unit,
    showHeader: Boolean,
    onBack: () -> Unit,
    /** محتوى يُضاف قبل الجدول وبعده (صفحة اختيار المؤسسة في تطبيق الأستاذ). */
    before: LazyListScope.() -> Unit = {},
    after: LazyListScope.() -> Unit = {},
    showOpenFile: Boolean = true
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    val days = dayLabels()
    val schools = entries.map { it.institutionId to it.schoolName }.distinct()
    var filter by rememberSaveable { mutableStateOf(highlightInstitution?.takeIf { id -> schools.any { it.first == id } }) }
    val shown = entries.filter { filter == null || it.institutionId == filter }
    // تداخل: حصتان من مؤسستين مختلفتين في نفس اليوم ونفس التوقيت
    val clashes = entries.groupBy { it.day }.flatMap { (_, list) ->
        list.filter { a -> list.any { b -> b.institutionId != a.institutionId && a.startTime < b.endTime && b.startTime < a.endTime } }
    }.map { it.id }.toSet()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = if (showHeader) 12.dp else 0.dp, vertical = if (showHeader) 10.dp else 0.dp)) {
            if (showHeader) ScreenHeader(stringResource(R.string.tt_my_timetable), onBack)
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
                before()
                if (showOpenFile) item {
                    OutlinedButton(onClick = onOpenFile, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("📥 " + stringResource(R.string.tt_open_file))
                    }
                }
                if (entries.isEmpty()) {
                    item { Text(stringResource(R.string.tt_teacher_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    after()
                    return@LazyColumn
                }
                if (schools.size > 1) item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text(stringResource(R.string.tt_all_schools)) }) }
                        items(schools, key = { it.first }) { (id, name) ->
                            FilterChip(selected = filter == id, onClick = { filter = id }, label = { Text(name) })
                        }
                    }
                }
                item {
                    Text(stringResource(R.string.tt_week_hours, shown.size), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                if (clashes.isNotEmpty()) item {
                    Text("⚠️ " + stringResource(R.string.tt_clash), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                val byDay = shown.groupBy { it.day }.toSortedMap()
                byDay.forEach { (d, list) ->
                    item(key = "day-$d") {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(days[d], fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                list.sortedBy { it.startTime }.forEach { e ->
                                    val hl = e.className == highlightClass && (highlightInstitution == null || e.institutionId == highlightInstitution)
                                    val bg = when {
                                        e.id in clashes -> MaterialTheme.colorScheme.errorContainer
                                        hl -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> Color.Transparent
                                    }
                                    Row(Modifier.fillMaxWidth().background(bg, RoundedCornerShape(8.dp)).padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text("⁦${e.startTime}-${e.endTime}⁩", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text("${e.className} — ${if (isFr) e.subjectNameFr else e.subjectNameAr}", fontWeight = FontWeight.SemiBold)
                                            if (filter == null && schools.size > 1) Text(e.schoolName, fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                after()
            }
        }
    }
}
