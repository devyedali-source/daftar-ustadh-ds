package com.elyedali.daftarustadh.ui

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.AbsenceEntity
import com.elyedali.daftarustadh.data.AbsenceSessionEntity
import com.elyedali.daftarustadh.data.AbsenceSource
import com.elyedali.daftarustadh.data.StudentEntity
import java.util.Calendar

/** مادة تظهر في دفتر الغياب. */
data class AbsenceSubject(val code: String, val label: String)

private val PresentGreen = Color(0xFF2E7D32)
private val AbsentRed = Color(0xFFC62828)
const val ABSENCE_MAX_PERIOD = 8

/** التاريخ محفوظ بصيغة yyyy-MM-dd ويُعرض dd/MM/yyyy. */
private fun displayDate(iso: String): String {
    val p = iso.split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
}

private fun isoDate(year: Int, month0: Int, day: Int) = "%04d-%02d-%02d".format(java.util.Locale.ROOT, year, month0 + 1, day)

private fun todayIso(): String = Calendar.getInstance().let {
    isoDate(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH))
}

/**
 * دفتر الغياب داخل القسم.
 * - الأستاذ: موادّه فقط (subjects = موادّه في هذا القسم)، ويرسل غياب مادته إلى الإدارة.
 * - الإدارة: جميع مواد القسم، تسجّل بنفسها وترفع ملفات غياب الأساتذة.
 */
@Composable
fun AbsenceTab(
    classId: String,
    students: List<StudentEntity>,
    subjects: List<AbsenceSubject>,
    sessions: List<AbsenceSessionEntity>,
    absences: List<AbsenceEntity>,
    isAdmin: Boolean,
    onSave: (oldId: String?, session: AbsenceSessionEntity, absentIds: Set<String>) -> Unit,
    onDelete: (sessionId: String) -> Unit,
    onSend: ((subjectCode: String) -> Unit)?,
    onUpload: (() -> Unit)?,
    onChooseSubjects: (() -> Unit)?,
    /** للإدارة: إبلاغ وكلاء غائبي الحصة عبر واتساب. */
    className: String = "",
    schoolName: String = "",
    subjectArFr: (String) -> Pair<String, String> = { it to it }
) {
    var notifySession by remember { mutableStateOf<AbsenceSessionEntity?>(null) }
    val whenLabel = stringResource(R.string.gn_when)
    val codes = remember(subjects) { subjects.map { it.code }.toSet() }
    val labels = remember(subjects) { subjects.associate { it.code to it.label } }
    // الأستاذ لا يرى إلا حصص موادّه
    val visibleSessions = remember(sessions, codes) { sessions.filter { it.subjectCode in codes } }
    var chosenFilter by rememberSaveable { mutableStateOf<String?>(null) }
    // الأستاذ الذي له مادة واحدة في القسم: تُختار تلقائيًا
    val filter = chosenFilter ?: if (!isAdmin && subjects.size == 1) subjects.first().code else null
    var view by rememberSaveable { mutableIntStateOf(0) }  // 0 الحصص، 1 حسب الطالب
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }

    if (subjects.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("📋", fontSize = 40.sp)
            Text(stringResource(if (isAdmin) R.string.abs_no_subjects_admin else R.string.abs_no_subjects_teacher),
                textAlign = TextAlign.Center)
            if (onChooseSubjects != null) Button(onClick = onChooseSubjects) { Text(stringResource(R.string.abs_choose_subjects)) }
        }
        return
    }

    val editing = visibleSessions.firstOrNull { it.id == editingId }
    if (creating || editing != null) {
        BackHandler { creating = false; editingId = null }
        AbsenceEditor(
            classId = classId,
            students = students,
            subjects = subjects,
            existing = editing,
            existingAbsent = editing?.let { e -> absences.filter { it.sessionId == e.id }.map { it.studentId }.toSet() }.orEmpty(),
            defaultSubject = filter ?: subjects.first().code,
            allSessionIds = remember(sessions) { sessions.map { it.id }.toSet() },
            isAdmin = isAdmin,
            onSave = { old, s, ids -> onSave(old, s, ids); creating = false; editingId = null },
            onDelete = { id -> onDelete(id); creating = false; editingId = null },
            onCancel = { creating = false; editingId = null }
        )
        return
    }

    val shown = visibleSessions.filter { filter == null || it.subjectCode == filter }
    val shownIds = remember(shown) { shown.map { it.id }.toSet() }
    val absentBySession = remember(absences) { absences.groupBy { it.sessionId } }
    val names = remember(students) { students.associate { it.id to it.nameAr.ifBlank { it.nameFr } } }

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isAdmin || subjects.size > 1) item {
                    FilterChip(selected = filter == null, onClick = { chosenFilter = null },
                        label = { Text(stringResource(R.string.abs_all_subjects)) })
                }
                items(subjects, key = { it.code }) { s ->
                    FilterChip(selected = filter == s.code, onClick = { chosenFilter = s.code }, label = { Text(s.label) })
                }
            }
        }
        item {
            Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("➕ " + stringResource(R.string.abs_new), fontWeight = FontWeight.Bold)
            }
        }
        if (onSend != null) item {
            val code = filter
            OutlinedButton(
                onClick = { if (code != null) onSend(code) },
                enabled = code != null && shown.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
            ) {
                Text("📤 " + if (code == null) stringResource(R.string.abs_send_pick_subject)
                    else stringResource(R.string.abs_send, labels[code] ?: code))
            }
        }
        if (onUpload != null) item {
            OutlinedButton(onClick = onUpload, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("📥 " + stringResource(R.string.abs_upload))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp)) {
                listOf(R.string.abs_view_sessions, R.string.abs_view_students).forEachIndexed { i, res ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (view == i) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { view = i }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(res), fontWeight = if (view == i) FontWeight.Bold else FontWeight.Normal,
                            color = if (view == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (view == 0) {
            item {
                Text(stringResource(R.string.abs_sessions_count, shown.size), fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
            if (shown.isEmpty()) item {
                Text(stringResource(R.string.abs_empty), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp))
            }
            items(shown, key = { it.id }) { s ->
                val absent = absentBySession[s.id].orEmpty()
                Card(
                    Modifier.fillMaxWidth().clickable { editingId = s.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📅 ⁦${displayDate(s.date)}⁩", fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.abs_period_n, s.period), color = MaterialTheme.colorScheme.secondary)
                            Box(Modifier.weight(1f))
                            if (isAdmin) SourceBadge(s.source)
                        }
                        if (isAdmin || subjects.size > 1) Text(labels[s.subjectCode] ?: s.subjectCode,
                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            stringResource(R.string.abs_absent_count, absent.size, students.size),
                            color = if (absent.isEmpty()) PresentGreen else AbsentRed, fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                        if (absent.isNotEmpty()) Text(
                            absent.mapNotNull { names[it.studentId] }.joinToString("، "),
                            fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (s.recordedBy.isNotBlank()) Text(stringResource(R.string.abs_recorded_by, s.recordedBy),
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (isAdmin && absent.isNotEmpty()) TextButton(onClick = { notifySession = s }) {
                            Text("📩 " + stringResource(R.string.gn_button, absent.size), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val counts = absences.filter { it.sessionId in shownIds }.groupingBy { it.studentId }.eachCount()
            item {
                Text(stringResource(R.string.abs_by_student_hint, shown.size), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(students.sortedBy { it.rollNumber }, key = { "st-" + it.id }) { st ->
                val n = counts[st.id] ?: 0
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${st.rollNumber}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
                    Text(st.nameAr.ifBlank { st.nameFr }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Surface(color = if (n == 0) PresentGreen.copy(alpha = 0.12f) else AbsentRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)) {
                        Text(stringResource(R.string.abs_hours, n), Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = if (n == 0) PresentGreen else AbsentRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
    notifySession?.let { ns ->
        val ids = absences.filter { it.sessionId == ns.id }.map { it.studentId }.toSet()
        val (subAr, subFr) = subjectArFr(ns.subjectCode)
        GuardianNotifyDialog(
            absent = students.filter { it.id in ids },
            className = className,
            schoolName = schoolName,
            whenText = whenLabel.format("\u2066${displayDate(ns.date)}\u2069", ns.period, labels[ns.subjectCode] ?: ns.subjectCode),
            date = displayDate(ns.date), period = ns.period, subjectAr = subAr, subjectFr = subFr
        ) { notifySession = null }
    }
}

@Composable
private fun SourceBadge(source: String) {
    val (res, color) = when (source) {
        AbsenceSource.ADMIN -> R.string.abs_source_admin to MaterialTheme.colorScheme.tertiary
        else -> R.string.abs_source_teacher to MaterialTheme.colorScheme.secondary
    }
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
        Text(stringResource(res), Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = color, fontSize = 11.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AbsenceEditor(
    classId: String,
    students: List<StudentEntity>,
    subjects: List<AbsenceSubject>,
    existing: AbsenceSessionEntity?,
    existingAbsent: Set<String>,
    defaultSubject: String,
    allSessionIds: Set<String>,
    isAdmin: Boolean,
    onSave: (String?, AbsenceSessionEntity, Set<String>) -> Unit,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var subject by rememberSaveable { mutableStateOf(existing?.subjectCode ?: defaultSubject) }
    var date by rememberSaveable { mutableStateOf(existing?.date ?: todayIso()) }
    var period by rememberSaveable { mutableIntStateOf(existing?.period ?: 1) }
    val absent = remember { mutableStateListOf<String>().apply { addAll(existingAbsent) } }
    var confirmDelete by remember { mutableStateOf(false) }
    val newId = AbsenceSessionEntity.idOf(classId, subject, date, period)
    val overwrites = newId != existing?.id && newId in allSessionIds

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)) {
        item {
            Text(stringResource(if (existing == null) R.string.abs_new else R.string.abs_edit),
                fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
        }
        if (subjects.size > 1) item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.abs_subject), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects, key = { it.code }) { s ->
                        FilterChip(selected = subject == s.code, onClick = { subject = s.code }, label = { Text(s.label) })
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    val p = date.split("-").mapNotNull { it.toIntOrNull() }
                    val cal = Calendar.getInstance()
                    val (y, m, d) = if (p.size == 3) Triple(p[0], p[1] - 1, p[2])
                        else Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    DatePickerDialog(context, { _, yy, mm, dd -> date = isoDate(yy, mm, dd) }, y, m, d).show()
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            ) { Text("📅 " + stringResource(R.string.abs_date) + ": ⁦${displayDate(date)}⁩") }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.abs_period), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..ABSENCE_MAX_PERIOD).toList()) { p ->
                        FilterChip(selected = period == p, onClick = { period = p }, label = { Text("$p") })
                    }
                }
            }
        }
        if (overwrites) item {
            Text(stringResource(R.string.abs_overwrite_warning), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.abs_present_n, students.size - absent.size), color = PresentGreen, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.abs_absent_n, absent.size), color = AbsentRed, fontWeight = FontWeight.Bold)
                }
                Text(stringResource(R.string.abs_switch_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(students.sortedBy { it.rollNumber }, key = { it.id }) { st ->
            val isAbsent = st.id in absent
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(if (isAbsent) AbsentRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                    .clickable { if (isAbsent) absent.remove(st.id) else absent.add(st.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${st.rollNumber}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
                Text(st.nameAr.ifBlank { st.nameFr }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Switch(
                    checked = !isAbsent,
                    onCheckedChange = { present -> if (present) absent.remove(st.id) else if (!isAbsent) absent.add(st.id) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = PresentGreen, checkedThumbColor = Color.White,
                        uncheckedTrackColor = AbsentRed, uncheckedThumbColor = Color.White, uncheckedBorderColor = AbsentRed
                    )
                )
            }
        }
        item {
            Button(
                onClick = {
                    onSave(
                        existing?.id,
                        AbsenceSessionEntity(
                            id = newId, classId = classId, subjectCode = subject, date = date, period = period,
                            source = if (isAdmin) AbsenceSource.ADMIN else AbsenceSource.TEACHER,
                            recordedBy = existing?.recordedBy.orEmpty(),
                            updatedAt = System.currentTimeMillis()
                        ),
                        absent.toSet()
                    )
                },
                enabled = students.isNotEmpty() && classId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp), shape = RoundedCornerShape(14.dp)
            ) { Text("💾 " + stringResource(R.string.abs_save), fontWeight = FontWeight.Bold) }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                if (existing != null) Button(
                    onClick = { confirmDelete = true }, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                ) { Text(stringResource(R.string.abs_delete), color = Color.White) }
            }
        }
    }
    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            text = { Text(stringResource(R.string.abs_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete(existing.id) }) {
                    Text(stringResource(R.string.abs_delete), color = AbsentRed)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
