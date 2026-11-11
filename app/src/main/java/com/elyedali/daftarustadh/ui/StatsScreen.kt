package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.ClassGenderCount
import com.elyedali.daftarustadh.data.Gender

private data class Counts(val total: Int, val boys: Int, val girls: Int) {
    val unknown get() = total - boys - girls
}

/** إحصائيات المؤسسة: بطاقة التلاميذ (تصفية حسب المستوى أو القسم)، وبطاقة الأساتذة والمواد (تصفية حسب المادة أو الأستاذ). */
@Composable
fun StatsScreen(
    schoolName: String,
    schoolYear: String,
    classes: List<ClassEntity>,
    genderCounts: List<ClassGenderCount>,
    teachers: List<com.elyedali.daftarustadh.data.TimetableTeacher> = emptyList(),
    assignments: List<com.elyedali.daftarustadh.data.TimetableAssignmentEntity> = emptyList(),
    hoursOf: Map<Pair<String, String>, Int> = emptyMap(),
    subjectLabel: (String) -> String = { it },
    onBack: () -> Unit
) {
    fun countsFor(ids: Set<String>): Counts {
        val rows = genderCounts.filter { it.classId in ids }
        return Counts(
            total = rows.sumOf { it.count },
            boys = rows.filter { it.gender == Gender.BOY }.sumOf { it.count },
            girls = rows.filter { it.gender == Gender.GIRL }.sumOf { it.count }
        )
    }
    val byLevel = classes.groupBy { it.levelCode }.toSortedMap()
    val boyColor = MaterialTheme.colorScheme.primary
    val girlColor = MaterialTheme.colorScheme.tertiary

    // التلاميذ: بطاقة واحدة مع تصفية حسب المستوى أو حسب القسم
    var byClass by remember { mutableStateOf(false) }
    var pick by remember { mutableStateOf<String?>(null) }   // null = الكل
    // الأساتذة والمواد: بطاقة واحدة مع تصفية حسب المادة أو حسب الأستاذ
    var byTeacher by remember { mutableStateOf(false) }
    var tPick by remember { mutableStateOf<String?>(null) }

    val classIds = classes.map { it.id }.toSet()
    val className = classes.associate { it.id to it.name }
    val mineOf = teachers.associate { t -> t.name to assignments.filter { it.teacherName == t.name && it.classId in classIds } }
    val bySubject = teachers.flatMap { t -> t.subjects.map { it to t.name } }
        .groupBy({ it.first }, { it.second }).toList().sortedByDescending { it.second.size }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { ScreenHeader(stringResource(R.string.statistics), onBack, listOf(schoolName, schoolYear)) }

            item { SectionTitle(stringResource(R.string.stat_students)) }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(selected = !byClass, onClick = { byClass = false; pick = null },
                                label = { Text(stringResource(R.string.stat_by_level)) })
                            FilterChip(selected = byClass, onClick = { byClass = true; pick = null },
                                label = { Text(stringResource(R.string.stat_by_class)) })
                        }
                        val options = if (byClass) classes.map { it.id to it.name } else byLevel.keys.map { it to it }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item { FilterChip(selected = pick == null, onClick = { pick = null }, label = { Text(stringResource(R.string.stat_all)) }) }
                            items(options, key = { it.first }) { (id, label) ->
                                FilterChip(selected = pick == id, onClick = { pick = id }, label = { Text(label) })
                            }
                        }
                        val ids = when {
                            pick == null -> classIds
                            byClass -> setOf(pick!!)
                            else -> byLevel[pick].orEmpty().map { it.id }.toSet()
                        }
                        val c = countsFor(ids)
                        val sub = when {
                            pick == null -> null
                            byClass -> null
                            else -> levelDescription(pick!!)
                        }
                        if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            if (!byClass || pick == null) Num(ids.size.toString(), stringResource(R.string.stat_classes), MaterialTheme.colorScheme.secondary)
                            Num(c.total.toString(), stringResource(R.string.stat_students), MaterialTheme.colorScheme.onSurface)
                            Num(c.boys.toString(), stringResource(R.string.stat_boys), boyColor)
                            Num(c.girls.toString(), stringResource(R.string.stat_girls), girlColor)
                        }
                        if (c.unknown > 0) Text(stringResource(R.string.stat_unknown_gender, c.unknown), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (c.total > 0) GenderBar(c, boyColor, girlColor)
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.stat_teachers_title)) }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(selected = !byTeacher, onClick = { byTeacher = false; tPick = null },
                                    label = { Text(stringResource(R.string.stat_by_subject)) })
                                FilterChip(selected = byTeacher, onClick = { byTeacher = true; tPick = null },
                                    label = { Text(stringResource(R.string.stat_by_teacher)) })
                            }
                            Num(teachers.size.toString(), stringResource(R.string.stat_teachers), MaterialTheme.colorScheme.secondary)
                        }
                        if (teachers.isEmpty()) {
                            Text(stringResource(R.string.stat_no_teachers), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val options = if (byTeacher) teachers.map { it.name to it.name } else bySubject.map { it.first to subjectLabel(it.first) }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item { FilterChip(selected = tPick == null, onClick = { tPick = null }, label = { Text(stringResource(R.string.stat_all)) }) }
                                items(options, key = { it.first }) { (id, label) ->
                                    FilterChip(selected = tPick == id, onClick = { tPick = id }, label = { Text(label) })
                                }
                            }
                            if (byTeacher) {
                                val shown = teachers.filter { tPick == null || it.name == tPick }
                                    .sortedByDescending { t -> mineOf[t.name].orEmpty().sumOf { hoursOf[it.classId to it.subjectCode] ?: 0 } }
                                shown.forEach { t ->
                                    val mine = mineOf[t.name].orEmpty()
                                    val hours = mine.sumOf { hoursOf[it.classId to it.subjectCode] ?: 0 }
                                    val subs = mine.map { it.subjectCode }.distinct().ifEmpty { t.subjects }
                                    HorizontalDivider()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(t.name, style = MaterialTheme.typography.titleMedium)
                                            Text(subs.joinToString(listSep()) { subjectLabel(it) }, style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (tPick != null && mine.isNotEmpty())
                                                Text(mine.map { className[it.classId] ?: "" }.distinct().sorted().joinToString(listSep()),
                                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Num(mine.map { it.classId }.distinct().size.toString(), stringResource(R.string.stat_classes), MaterialTheme.colorScheme.secondary)
                                        Num(hours.toString(), stringResource(R.string.stat_hours_week), MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else {
                                bySubject.filter { tPick == null || it.first == tPick }.forEach { (code, names) ->
                                    HorizontalDivider()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(subjectLabel(code), style = MaterialTheme.typography.titleMedium)
                                            Text(names.sorted().joinToString(listSep()), style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Num(names.size.toString(), stringResource(R.string.stat_teachers), MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Num(v: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(Modifier.width(58.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(v, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** شريط نسبة الأولاد والبنات. */
@Composable
private fun GenderBar(c: Counts, boyColor: androidx.compose.ui.graphics.Color, girlColor: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (c.boys > 0) Box(Modifier.weight(c.boys.toFloat()).fillMaxHeight().background(boyColor))
        if (c.girls > 0) Box(Modifier.weight(c.girls.toFloat()).fillMaxHeight().background(girlColor))
        if (c.unknown > 0) Box(Modifier.weight(c.unknown.toFloat()).fillMaxHeight())
    }
}
