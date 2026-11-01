package com.elyedali.daftarustadh.ui

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.CurriculumSubject
import com.elyedali.daftarustadh.data.TimetableAssignmentEntity
import com.elyedali.daftarustadh.data.TimetableSettings
import com.elyedali.daftarustadh.data.TimetableSlotEntity
import com.elyedali.daftarustadh.data.TimetableTeacher

/** أسماء الأيام: 0 = الاثنين ... 6 = الأحد. */
@Composable
fun dayLabels(): List<String> = listOf(
    stringResource(R.string.day_0), stringResource(R.string.day_1), stringResource(R.string.day_2),
    stringResource(R.string.day_3), stringResource(R.string.day_4), stringResource(R.string.day_5),
    stringResource(R.string.day_6)
)

private val SubjectColors = listOf(
    0xFFE3F2FD, 0xFFE8F5E9, 0xFFFFF3E0, 0xFFF3E5F5, 0xFFE0F7FA, 0xFFFFFDE7, 0xFFFCE4EC,
    0xFFEDE7F6, 0xFFE0F2F1, 0xFFFBE9E7, 0xFFF1F8E9, 0xFFECEFF1, 0xFFFFF8E1
).map { Color(it) }

private fun colorFor(key: String) = SubjectColors[Math.floorMod(key.hashCode(), SubjectColors.size)]

/**
 * الجدول الزمني للمؤسسة (الإدارة):
 * الإعدادات والتوليد، أساتذة المواد، الجدول حسب القسم (تبديل حصتين وتعديل يدوي)، والجدول حسب الأستاذ (إرساله له).
 */
@Composable
fun TimetableScreen(
    classes: List<ClassEntity>,
    subjectsByClass: Map<String, List<CurriculumSubject>>,
    subjectLabel: (String) -> String,
    settings: TimetableSettings,
    assignments: List<TimetableAssignmentEntity>,
    teachers: List<TimetableTeacher>,
    slots: List<TimetableSlotEntity>,
    generating: Boolean,
    lastResult: String?,
    initialClassId: String?,
    onSaveSettings: (TimetableSettings) -> Unit,
    onAssign: (classId: String, subjectCode: String, teacher: String) -> Unit,
    onSaveTeachers: (List<TimetableTeacher>) -> Unit,
    onDistribute: () -> Unit,
    onGenerate: () -> Unit,
    onSwap: (classId: String, a: Pair<Int, Int>, b: Pair<Int, Int>) -> String?,
    onEditCell: (classId: String, day: Int, period: Int, subjectCode: String?) -> Unit,
    onShareClass: (ClassEntity) -> Unit,
    onShareTeacherPdf: (String) -> Unit,
    onSendTeacherFile: (String) -> Unit,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(if (initialClassId != null && slots.any { it.classId == initialClassId }) 2 else 0) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
            ScreenHeader(stringResource(R.string.tt_title), onBack)
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp, containerColor = MaterialTheme.colorScheme.background) {
                listOf(R.string.tt_tab_generate, R.string.tt_tab_teachers, R.string.tt_tab_by_class, R.string.tt_tab_by_teacher)
                    .forEachIndexed { i, res -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(res)) }) }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    0 -> GenerateTab(classes, subjectsByClass, settings, assignments, slots, generating, lastResult, onSaveSettings, onGenerate)
                    1 -> TeachersTab(classes, subjectsByClass, subjectLabel, assignments, teachers, onAssign, onSaveTeachers, onDistribute)
                    2 -> ByClassTab(classes, subjectsByClass, subjectLabel, settings, assignments, slots, initialClassId, onSwap, onEditCell, onShareClass)
                    else -> ByTeacherTab(classes, subjectLabel, settings, slots, onShareTeacherPdf, onSendTeacherFile)
                }
            }
        }
    }
}

/* ---------------- الإعدادات والتوليد ---------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenerateTab(
    classes: List<ClassEntity>,
    subjectsByClass: Map<String, List<CurriculumSubject>>,
    settings: TimetableSettings,
    assignments: List<TimetableAssignmentEntity>,
    slots: List<TimetableSlotEntity>,
    generating: Boolean,
    lastResult: String?,
    onSave: (TimetableSettings) -> Unit,
    onGenerate: () -> Unit
) {
    val context = LocalContext.current
    val days = dayLabels()
    var confirm by remember { mutableStateOf(false) }
    val maxHours = classes.maxOfOrNull { c -> subjectsByClass[c.id].orEmpty().sumOf { it.weeklyHours } } ?: 0
    val missing = classes.sumOf { c ->
        subjectsByClass[c.id].orEmpty().count { s -> assignments.none { it.classId == c.id && it.subjectCode == s.code && it.teacherName.isNotBlank() } }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 10.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingCard(stringResource(R.string.tt_days)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0..6).forEach { d ->
                    FilterChip(selected = d in settings.days, onClick = {
                        val nd = if (d in settings.days) settings.days - d else (settings.days + d).sorted()
                        if (nd.isNotEmpty()) onSave(settings.copy(days = nd))
                    }, label = { Text(days[d]) })
                }
            }
        }
        SettingCard(stringResource(R.string.tt_times)) {
            Text(stringResource(R.string.tt_times_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            fun pick(minutes: Int, set: (Int) -> Unit) {
                TimePickerDialog(context, { _, h, m -> set(h * 60 + m) }, minutes / 60, minutes % 60, true).show()
            }
            settings.times.forEachIndexed { i, (st, en) ->
                val br = settings.breakBefore(i)
                if (br > 0) Text("☕ " + stringResource(R.string.tt_break_line,
                    TimetableSettings.hhmm(settings.end(i - 1)), TimetableSettings.hhmm(st), br),
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${i + 1}", fontWeight = FontWeight.Black, modifier = Modifier.width(22.dp))
                    OutlinedButton(onClick = {
                        pick(st) { v -> onSave(settings.copy(times = settings.times.toMutableList().also { it[i] = v to maxOf(en, v + 5) })) }
                    }) { Text("\u2066${TimetableSettings.hhmm(st)}\u2069") }
                    Text("→")
                    OutlinedButton(onClick = {
                        pick(en) { v -> if (v > st) onSave(settings.copy(times = settings.times.toMutableList().also { it[i] = st to v })) }
                    }) { Text("\u2066${TimetableSettings.hhmm(en)}\u2069") }
                    Text(stringResource(R.string.tt_minutes, en - st), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = settings.periods < 12, onClick = {
                    val last = settings.times.last()
                    val len = last.second - last.first
                    onSave(settings.copy(times = settings.times + (last.second to last.second + len)))
                }) { Text("+ " + stringResource(R.string.tt_add_period)) }
                OutlinedButton(enabled = settings.periods > 1, onClick = {
                    onSave(settings.copy(times = settings.times.dropLast(1)))
                }) { Text("− " + stringResource(R.string.tt_remove_period)) }
                TextButton(onClick = { onSave(settings.copy(times = TimetableSettings.DEFAULT_TIMES)) }) {
                    Text(stringResource(R.string.tt_default_times))
                }
            }
        }
        SettingCard(stringResource(R.string.tt_day_periods)) {
            Text(stringResource(R.string.tt_day_periods_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            settings.days.forEach { d ->
                val n = settings.periodsOn(d)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(days[d], Modifier.width(80.dp), fontWeight = FontWeight.SemiBold)
                    Stepper(n, 0, settings.periods) { v ->
                        val map = settings.dayPeriods.toMutableMap()
                        if (v == settings.periods) map.remove(d) else map[d] = v
                        onSave(settings.copy(dayPeriods = map))
                    }
                    Text("\u2066${if (n > 0) TimetableSettings.hhmm(settings.end(n - 1)) else "—"}\u2069", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.tt_capacity, settings.slotCount, maxHours), fontWeight = FontWeight.Bold,
                    color = if (maxHours > settings.slotCount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.tt_classes_count, classes.size), fontSize = 13.sp)
                if (missing > 0) Text(stringResource(R.string.tt_missing_teachers, missing), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.tt_fair_rules), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { if (slots.isNotEmpty()) confirm = true else onGenerate() },
                    enabled = !generating && classes.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    if (generating) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Text("  " + stringResource(R.string.tt_generating))
                    } else Text("⚙️ " + stringResource(R.string.tt_generate), fontWeight = FontWeight.Bold)
                }
                lastResult?.let { Text(it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        text = { Text(stringResource(R.string.tt_replace_confirm)) },
        confirmButton = { TextButton(onClick = { confirm = false; onGenerate() }) { Text(stringResource(R.string.tt_generate)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun Stepper(value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = { if (value > min) onChange(value - 1) }, enabled = value > min) { Text("−") }
        Text("$value", fontWeight = FontWeight.Black, fontSize = 18.sp)
        OutlinedButton(onClick = { if (value < max) onChange(value + 1) }, enabled = value < max) { Text("+") }
    }
}

/* ---------------- أساتذة المواد ---------------- */

@Composable
private fun ClassChips(classes: List<ClassEntity>, selected: String?, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(classes, key = { it.id }) { c ->
            FilterChip(selected = selected == c.id, onClick = { onSelect(c.id) }, label = { Text(c.name) })
        }
    }
}

@Composable
private fun TeachersTab(
    classes: List<ClassEntity>,
    subjectsByClass: Map<String, List<CurriculumSubject>>,
    subjectLabel: (String) -> String,
    assignments: List<TimetableAssignmentEntity>,
    teachers: List<TimetableTeacher>,
    onAssign: (String, String, String) -> Unit,
    onSaveTeachers: (List<TimetableTeacher>) -> Unit,
    onDistribute: () -> Unit
) {
    var mode by rememberSaveable { mutableIntStateOf(0) }  // 0 الأساتذة، 1 حسب القسم
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(3.dp)
        ) {
            listOf(R.string.tt_mode_teachers, R.string.tt_mode_by_class).forEachIndexed { i, res ->
                Box(
                    Modifier.weight(1f)
                        .background(if (mode == i) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { mode = i }.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(res), fontWeight = if (mode == i) FontWeight.Bold else FontWeight.Normal,
                        color = if (mode == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (mode == 0) TeacherListPane(classes, subjectsByClass, subjectLabel, assignments, teachers, onSaveTeachers, onDistribute)
        else ByClassAssignPane(classes, subjectsByClass, subjectLabel, assignments, teachers, onAssign)
    }
}

/** الأساتذة: يكتب المسؤول الاسم ويضع علامات على المواد التي يدرّسها، ثم توزَّع الأقسام بالعدل. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeacherListPane(
    classes: List<ClassEntity>,
    subjectsByClass: Map<String, List<CurriculumSubject>>,
    subjectLabel: (String) -> String,
    assignments: List<TimetableAssignmentEntity>,
    teachers: List<TimetableTeacher>,
    onSave: (List<TimetableTeacher>) -> Unit,
    onDistribute: () -> Unit
) {
    var editing by remember { mutableStateOf<Int?>(null) }   // -1 = أستاذ جديد
    var confirmDistribute by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Int?>(null) }
    val order = listOf("IR", "AR", "HG", "PH", "IC", "FR", "ANG", "MATS", "SN", "PC", "TI", "LEG", "EPS")
    // كل المواد الموجودة في أقسام المؤسسة
    val allSubjects = remember(subjectsByClass) {
        subjectsByClass.values.flatten().map { it.code }.distinct()
            .sortedBy { order.indexOf(it).let { i -> if (i < 0) 99 else i } }
    }
    val hoursOf = remember(subjectsByClass) {
        subjectsByClass.flatMap { (cid, subs) -> subs.map { (cid to it.code) to it.weeklyHours } }.toMap()
    }
    val classNames = remember(classes) { classes.associate { it.id to it.name } }
    val uncovered = allSubjects.filter { code -> teachers.none { code in it.subjects } }

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
        item { Text(stringResource(R.string.tt_teachers_list_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Button(onClick = { editing = -1 }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("➕ " + stringResource(R.string.tt_add_teacher), fontWeight = FontWeight.Bold)
            }
        }
        if (teachers.isEmpty()) item {
            Text(stringResource(R.string.tt_no_teacher_list), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp))
        }
        items(teachers.indices.toList(), key = { "t-" + teachers[it].name }) { i ->
            val t = teachers[i]
            val mine = assignments.filter { it.teacherName == t.name }
            val hours = mine.sumOf { hoursOf[it.classId to it.subjectCode] ?: 0 }
            Card(
                Modifier.fillMaxWidth().clickable { editing = i }, shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👨‍🏫 " + t.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.tt_hours_week, hours), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary)
                        TextButton(onClick = { deleting = i }) { Text("🗑️") }
                    }
                    Text(t.subjects.joinToString("، ") { subjectLabel(it) }, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    if (mine.isNotEmpty()) Text(
                        mine.groupBy { it.subjectCode }.entries.joinToString(" — ") { (code, l) ->
                            subjectLabel(code) + ": " + l.mapNotNull { classNames[it.classId] }.sorted().joinToString("، ")
                        },
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (uncovered.isNotEmpty() && teachers.isNotEmpty()) item {
            Text(stringResource(R.string.tt_uncovered, uncovered.joinToString("، ") { subjectLabel(it) }), fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error)
        }
        if (teachers.isNotEmpty()) item {
            OutlinedButton(onClick = { confirmDistribute = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("⚖️ " + stringResource(R.string.tt_distribute))
            }
        }
    }

    editing?.let { idx ->
        val existing = teachers.getOrNull(idx)
        var name by remember(idx) { mutableStateOf(existing?.name.orEmpty()) }
        var chosen by remember(idx) { mutableStateOf(existing?.subjects.orEmpty().toSet()) }
        val duplicate = teachers.withIndex().any { (j, t) -> j != idx && t.name == name.trim() }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(stringResource(if (existing == null) R.string.tt_add_teacher else R.string.tt_edit_teacher)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.tt_teacher_name)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), isError = duplicate)
                    if (duplicate) Text(stringResource(R.string.tt_teacher_exists), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    Text(stringResource(R.string.tt_teacher_subjects), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allSubjects.forEach { code ->
                            FilterChip(
                                selected = code in chosen,
                                onClick = { chosen = if (code in chosen) chosen - code else chosen + code },
                                label = { Text(subjectLabel(code)) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank() && chosen.isNotEmpty() && !duplicate, onClick = {
                    val t = TimetableTeacher(name.trim(), allSubjects.filter { it in chosen })
                    onSave(if (existing == null) teachers + t else teachers.toMutableList().also { it[idx] = t })
                    editing = null
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    deleting?.let { idx ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            text = { Text(stringResource(R.string.tt_delete_teacher, teachers.getOrNull(idx)?.name.orEmpty())) },
            confirmButton = {
                TextButton(onClick = { onSave(teachers.filterIndexed { j, _ -> j != idx }); deleting = null }) {
                    Text(stringResource(R.string.tt_delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (confirmDistribute) AlertDialog(
        onDismissRequest = { confirmDistribute = false },
        text = { Text(stringResource(R.string.tt_distribute_confirm)) },
        confirmButton = { TextButton(onClick = { confirmDistribute = false; onDistribute() }) { Text(stringResource(R.string.tt_distribute)) } },
        dismissButton = { TextButton(onClick = { confirmDistribute = false }) { Text(stringResource(R.string.cancel)) } }
    )
}

/** حسب القسم: مراجعة أستاذ كل مادة في القسم وتغييره يدويًا. */
@Composable
private fun ByClassAssignPane(
    classes: List<ClassEntity>,
    subjectsByClass: Map<String, List<CurriculumSubject>>,
    subjectLabel: (String) -> String,
    assignments: List<TimetableAssignmentEntity>,
    teachers: List<TimetableTeacher>,
    onAssign: (String, String, String) -> Unit
) {
    var classId by rememberSaveable { mutableStateOf(classes.firstOrNull()?.id) }
    var editing by remember { mutableStateOf<CurriculumSubject?>(null) }
    val cid = classId ?: return
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
        item { Text(stringResource(R.string.tt_teachers_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { ClassChips(classes, cid) { classId = it } }
        items(subjectsByClass[cid].orEmpty(), key = { it.code }) { s ->
            val teacher = assignments.firstOrNull { it.classId == cid && it.subjectCode == s.code }?.teacherName.orEmpty()
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .clickable { editing = s }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(subjectLabel(s.code), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.tt_hours_week, s.weeklyHours), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(teacher.ifBlank { stringResource(R.string.tt_choose_teacher) },
                    color = if (teacher.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
    editing?.let { s ->
        // أساتذة هذه المادة أولًا، ثم البقية
        val options = teachers.sortedBy { if (s.code in it.subjects) 0 else 1 }
        val otherLabel = stringResource(R.string.tt_other_subject)
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(subjectLabel(s.code) + " — " + classes.firstOrNull { it.id == cid }?.name.orEmpty()) },
            text = {
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    if (options.isEmpty()) item { Text(stringResource(R.string.tt_no_teacher_list)) }
                    items(options, key = { it.name }) { t ->
                        Text(
                            t.name + if (s.code in t.subjects) "" else " ($otherLabel)",
                            Modifier.fillMaxWidth().clickable { onAssign(cid, s.code, t.name); editing = null }.padding(vertical = 10.dp),
                            color = if (s.code in t.subjects) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        Text(stringResource(R.string.tt_no_teacher_for_subject), Modifier.fillMaxWidth()
                            .clickable { onAssign(cid, s.code, ""); editing = null }.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

/* ---------------- شبكة الجدول ---------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableGrid(
    settings: TimetableSettings,
    cell: (day: Int, period: Int) -> Pair<String, String>?,   // (سطر رئيسي، سطر ثانوي)
    colorKey: (day: Int, period: Int) -> String?,
    selected: Pair<Int, Int>? = null,
    onClick: ((Int, Int) -> Unit)? = null,
    onLongClick: ((Int, Int) -> Unit)? = null
) {
    val days = dayLabels()
    val cellW = 86.dp
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            Row {
                Box(Modifier.width(64.dp).padding(2.dp))
                (0 until settings.periods).forEach { p ->
                    Column(Modifier.width(cellW).padding(2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${p + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("⁦${settings.label(p)}⁩", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            settings.days.forEach { d ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(days[d], Modifier.width(64.dp).padding(2.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    (0 until settings.periods).forEach { p ->
                        val closed = p >= settings.periodsOn(d)
                        val content = if (closed) null else cell(d, p)
                        val isSel = selected == (d to p)
                        val bg = if (closed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else colorKey(d, p)?.let { colorFor(it) } ?: MaterialTheme.colorScheme.surface
                        Box(
                            Modifier.width(cellW).heightIn(min = 52.dp).padding(2.dp)
                                .background(bg, RoundedCornerShape(8.dp))
                                .border(if (isSel) 2.dp else 0.5.dp,
                                    if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp))
                                .then(
                                    if (!closed && (onClick != null || onLongClick != null)) Modifier.combinedClickable(
                                        onClick = { onClick?.invoke(d, p) },
                                        onLongClick = onLongClick?.let { { it(d, p) } }
                                    ) else Modifier
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (content != null) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(content.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                                if (content.second.isNotBlank()) Text(content.second, fontSize = 9.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- حسب القسم ---------------- */

@Composable
private fun ByClassTab(
    classes: List<ClassEntity>,
    subjectsByClass: Map<String, List<CurriculumSubject>>,
    subjectLabel: (String) -> String,
    settings: TimetableSettings,
    assignments: List<TimetableAssignmentEntity>,
    slots: List<TimetableSlotEntity>,
    initialClassId: String?,
    onSwap: (String, Pair<Int, Int>, Pair<Int, Int>) -> String?,
    onEditCell: (String, Int, Int, String?) -> Unit,
    onShare: (ClassEntity) -> Unit
) {
    val context = LocalContext.current
    var classId by rememberSaveable { mutableStateOf(initialClassId ?: classes.firstOrNull()?.id) }
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var editCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val busyMsg = stringResource(R.string.tt_swap_busy)
    val cid = classId ?: return
    val c = classes.firstOrNull { it.id == cid } ?: return
    val mine = slots.filter { it.classId == cid }
    val at = remember(mine) { mine.associateBy { it.day to it.period } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ClassChips(classes, cid) { classId = it; selected = null }
        if (mine.isEmpty()) {
            Text(stringResource(R.string.tt_no_timetable), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(stringResource(R.string.tt_swap_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TimetableGrid(
                settings = settings,
                cell = { d, p -> at[d to p]?.let { subjectLabel(it.subjectCode) to it.teacherName } },
                colorKey = { d, p -> at[d to p]?.subjectCode },
                selected = selected,
                onClick = { d, p ->
                    val s = selected
                    when {
                        s == null -> selected = d to p
                        s == (d to p) -> selected = null
                        else -> {
                            val busy = onSwap(cid, s, d to p)
                            if (busy != null) Toast.makeText(context, busyMsg.format(busy), Toast.LENGTH_LONG).show()
                            selected = null
                        }
                    }
                },
                onLongClick = { d, p -> editCell = d to p }
            )
            val hours = subjectsByClass[cid].orEmpty().sumOf { it.weeklyHours }
            Text(stringResource(R.string.tt_class_hours, mine.size, hours), fontSize = 12.sp,
                color = if (mine.size < hours) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
            Button(onClick = { onShare(c) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("🖨️ " + stringResource(R.string.tt_share_class, c.name))
            }
        }
    }
    editCell?.let { (d, p) ->
        val subjects = subjectsByClass[cid].orEmpty()
        AlertDialog(
            onDismissRequest = { editCell = null },
            title = { Text(stringResource(R.string.tt_edit_cell, dayLabels()[d], p + 1)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    item {
                        Text(stringResource(R.string.tt_empty_cell), Modifier.fillMaxWidth().clickable {
                            onEditCell(cid, d, p, null); editCell = null
                        }.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.error)
                    }
                    items(subjects, key = { it.code }) { s ->
                        val t = assignments.firstOrNull { it.classId == cid && it.subjectCode == s.code }?.teacherName.orEmpty()
                        Text(subjectLabel(s.code) + if (t.isNotBlank()) " — $t" else "",
                            Modifier.fillMaxWidth().clickable { onEditCell(cid, d, p, s.code); editCell = null }.padding(vertical = 10.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { editCell = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

/* ---------------- حسب الأستاذ ---------------- */

@Composable
private fun ByTeacherTab(
    classes: List<ClassEntity>,
    subjectLabel: (String) -> String,
    settings: TimetableSettings,
    slots: List<TimetableSlotEntity>,
    onSharePdf: (String) -> Unit,
    onSendFile: (String) -> Unit
) {
    val teachers = remember(slots) { slots.map { it.teacherName }.filter { it.isNotBlank() }.distinct().sorted() }
    var teacher by rememberSaveable { mutableStateOf<String?>(null) }
    val t = teacher?.takeIf { it in teachers } ?: teachers.firstOrNull()
    val names = remember(classes) { classes.associate { it.id to it.name } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (t == null) {
            Text(stringResource(R.string.tt_no_teachers), color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(teachers) { n -> FilterChip(selected = n == t, onClick = { teacher = n }, label = { Text(n) }) }
        }
        val mine = slots.filter { it.teacherName == t }
        val at = mine.groupBy { it.day to it.period }
        Text(stringResource(R.string.tt_teacher_hours, t, mine.size), fontWeight = FontWeight.Bold)
        TimetableGrid(
            settings = settings,
            cell = { d, p -> at[d to p]?.let { l -> l.joinToString(" / ") { names[it.classId].orEmpty() } to subjectLabel(l.first().subjectCode) } },
            colorKey = { d, p -> at[d to p]?.first()?.classId }
        )
        Button(onClick = { onSendFile(t) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("📤 " + stringResource(R.string.tt_send_teacher))
        }
        OutlinedButton(onClick = { onSharePdf(t) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("🖨️ " + stringResource(R.string.tt_teacher_pdf))
        }
    }
}
