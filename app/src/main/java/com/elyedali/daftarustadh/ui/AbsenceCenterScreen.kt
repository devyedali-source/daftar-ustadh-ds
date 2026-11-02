package com.elyedali.daftarustadh.ui

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.AbsenceEntity
import com.elyedali.daftarustadh.data.AbsenceSessionEntity
import com.elyedali.daftarustadh.data.AbsenceSource
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.data.TimetableSettings
import com.elyedali.daftarustadh.data.TimetableSlotEntity
import java.util.Calendar

private val Red = Color(0xFFC62828)
private val Green = Color(0xFF2E7D32)

private fun iso(y: Int, m0: Int, d: Int) = "%04d-%02d-%02d".format(java.util.Locale.ROOT, y, m0 + 1, d)
private fun today(): String = Calendar.getInstance().let { iso(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH)) }
private fun show(isoDate: String): String = isoDate.split("-").let { if (it.size == 3) "${it[2]}/${it[1]}/${it[0]}" else isoDate }

/** يوم الأسبوع للتاريخ: 0 = الاثنين ... 6 = الأحد (كما في الجدول الزمني). */
private fun weekday(isoDate: String): Int {
    val p = isoDate.split("-").mapNotNull { it.toIntOrNull() }
    if (p.size != 3) return 0
    val c = Calendar.getInstance().apply { clear(); set(p[0], p[1] - 1, p[2]) }
    return (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
}

/** يقرأ أرقام النداء من ورقة الأستاذ: «3 7 12» أو «3، 7، 12» وبالأرقام العربية أيضًا. */
fun parseRollNumbers(text: String): List<Int> =
    text.map { c -> if (c in '٠'..'٩') '0' + (c - '٠') else c }.joinToString("")
        .split(Regex("[^0-9]+")).mapNotNull { it.toIntOrNull() }.distinct()

@Composable
private fun DateButton(label: String, value: String, modifier: Modifier = Modifier, onPick: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            val p = value.split("-").mapNotNull { it.toIntOrNull() }
            val cal = Calendar.getInstance()
            val (y, m, d) = if (p.size == 3) Triple(p[0], p[1] - 1, p[2])
                else Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            DatePickerDialog(context, { _, yy, mm, dd -> onPick(iso(yy, mm, dd)) }, y, m, d).show()
        },
        modifier = modifier, shape = RoundedCornerShape(12.dp)
    ) { Text("📅 $label: ⁦${show(value)}⁩", fontSize = 13.sp) }
}

/**
 * مركز الغياب (الإدارة):
 * 1) تسجيل ورقة الأستاذ: القسم والتاريخ والحصة، ثم أرقام النداء للغائبين.
 * 2) الأكثر تغيبًا: ترتيب الطلاب حسب ساعات الغياب بين تاريخين، لكل الأقسام أو لقسم.
 */
@Composable
fun AbsenceCenterScreen(
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    sessions: List<AbsenceSessionEntity>,
    absences: List<AbsenceEntity>,
    settings: TimetableSettings,
    slots: List<TimetableSlotEntity>,
    subjectsOf: (ClassEntity) -> List<AbsenceSubject>,
    subjectLabel: (String) -> String,
    yearStart: String,
    onSave: (AbsenceSessionEntity, Set<String>) -> Unit,
    onBack: () -> Unit,
    schoolName: String = ""
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.ac_title), onMenu = null, onBack = onBack)
            Row(Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp)) {
                listOf(R.string.ac_tab_entry, R.string.ac_tab_top).forEachIndexed { i, res ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (tab == i) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { tab = i }.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(res), fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal,
                            color = if (tab == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (classes.isEmpty()) {
                Text(stringResource(R.string.ps_no_classes), Modifier.padding(24.dp), color = MaterialTheme.colorScheme.error)
                return@Column
            }
            if (tab == 0) EntryTab(classes, students, sessions, absences, settings, slots, subjectsOf, subjectLabel, onSave, schoolName)
            else TopTab(classes, students, sessions, absences, yearStart)
        }
    }
}

@Composable
private fun EntryTab(
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    sessions: List<AbsenceSessionEntity>,
    absences: List<AbsenceEntity>,
    settings: TimetableSettings,
    slots: List<TimetableSlotEntity>,
    subjectsOf: (ClassEntity) -> List<AbsenceSubject>,
    subjectLabel: (String) -> String,
    onSave: (AbsenceSessionEntity, Set<String>) -> Unit,
    schoolName: String
) {
    val whenLabel = stringResource(R.string.gn_when)
    var classId by rememberSaveable { mutableStateOf(classes.first().id) }
    val cls = classes.firstOrNull { it.id == classId } ?: classes.first()
    var date by rememberSaveable { mutableStateOf(today()) }
    var period by rememberSaveable { mutableIntStateOf(0) }          // 0 = لم تُختر
    var chosenSubject by rememberSaveable { mutableStateOf<String?>(null) }
    var numbers by rememberSaveable { mutableStateOf("") }
    var saved by remember { mutableStateOf<String?>(null) }
    // غائبو آخر حصة حُفظت ووصفها: لإبلاغ وكلائهم
    var lastAbsent by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    var lastWhen by remember { mutableStateOf("") }
    var lastClass by remember { mutableStateOf("") }
    var notify by remember { mutableStateOf(false) }

    val day = weekday(date)
    val periodsToday = if (day in settings.days) settings.periodsOn(day) else 0
    val slot = slots.firstOrNull { it.classId == cls.id && it.day == day && it.period == period - 1 }
    val subjects = subjectsOf(cls)
    val subject = slot?.subjectCode ?: chosenSubject
    val sessionId = subject?.let { AbsenceSessionEntity.idOf(cls.id, it, date, period) }
    val existing = sessions.firstOrNull { it.id == sessionId }
    val classStudents = students.filter { it.classId == cls.id }
    val byRoll = classStudents.associateBy { it.rollNumber }
    val rolls = parseRollNumbers(numbers)
    val unknown = rolls.filter { it !in byRoll }
    val absentStudents = rolls.mapNotNull { byRoll[it] }

    // عند فتح حصة مسجّلة من قبل: تظهر أرقام غائبيها لتعديلها
    LaunchedEffect(sessionId) {
        if (existing != null) {
            val ids = absences.filter { it.sessionId == existing.id }.map { it.studentId }.toSet()
            numbers = classStudents.filter { it.id in ids }.map { it.rollNumber }.sorted().joinToString(" ")
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(stringResource(R.string.ac_entry_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.ac_class), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classes, key = { it.id }) { c ->
                        FilterChip(selected = c.id == cls.id, onClick = { classId = c.id; numbers = ""; saved = null }, label = { Text(c.name) })
                    }
                }
            }
        }
        item {
            DateButton(stringResource(R.string.abs_date), date, Modifier.fillMaxWidth()) { date = it; period = 0; numbers = ""; saved = null }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.ac_period), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (periodsToday == 0) {
                    Text(stringResource(R.string.ac_no_school_day), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                } else LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..periodsToday).toList()) { p ->
                        FilterChip(selected = period == p, onClick = { period = p; chosenSubject = null; numbers = ""; saved = null },
                            label = { Text("$p  ⁦${settings.label(p - 1)}⁩", fontSize = 12.sp) })
                    }
                }
            }
        }
        if (period > 0) item { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (slot != null) {
                Text("📘 " + subjectLabel(slot.subjectCode) + if (slot.teacherName.isNotBlank()) " — 👨‍🏫 ${slot.teacherName}" else "",
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            } else {
                Text(stringResource(R.string.ac_pick_subject), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects, key = { it.code }) { s ->
                        FilterChip(selected = chosenSubject == s.code, onClick = { chosenSubject = s.code }, label = { Text(s.label) })
                    }
                }
            }
        } }
        if (period > 0 && subject != null) {
            if (existing != null) item {
                Text("✏️ " + stringResource(R.string.ac_existing), fontSize = 12.sp, color = Color(0xFFE65100))
            }
            item { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    numbers, { numbers = it; saved = null },
                    label = { Text(stringResource(R.string.ac_numbers)) },
                    placeholder = { Text("3 7 12") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = unknown.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (unknown.isNotEmpty()) Text(stringResource(R.string.ac_unknown, unknown.joinToString("، ")),
                    color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            } }
            item {
                Text(
                    if (absentStudents.isEmpty()) stringResource(R.string.ac_none_absent)
                    else stringResource(R.string.abs_absent_count, absentStudents.size, classStudents.size),
                    fontWeight = FontWeight.Bold, color = if (absentStudents.isEmpty()) Green else Red
                )
            }
            items(absentStudents, key = { "a-" + it.id }) { st ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Red.copy(alpha = 0.07f))
                    .padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${st.rollNumber}", fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                    Text(st.nameAr.ifBlank { st.nameFr }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(
                            AbsenceSessionEntity(
                                id = sessionId!!, classId = cls.id, subjectCode = subject, date = date, period = period,
                                source = AbsenceSource.ADMIN, recordedBy = slot?.teacherName.orEmpty(),
                                updatedAt = System.currentTimeMillis()
                            ),
                            absentStudents.map { it.id }.toSet()
                        )
                        saved = "${cls.name} — ${show(date)} — $period"
                        lastAbsent = absentStudents
                        lastClass = cls.name
                        lastWhen = whenLabel.format("\u2066${show(date)}\u2069", period, subjectLabel(subject))
                        numbers = ""
                        // الحصة التالية مباشرة لتسجيل الورقة الموالية
                        if (period < periodsToday) { period += 1; chosenSubject = null }
                    },
                    enabled = unknown.isEmpty() && classStudents.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                ) { Text("💾 " + stringResource(R.string.ac_save), fontWeight = FontWeight.Bold) }
            }
        }
        saved?.let { s -> item { Text("✅ " + stringResource(R.string.ac_saved, s), color = Green, fontWeight = FontWeight.Bold) } }
        if (saved != null && lastAbsent.isNotEmpty()) item {
            OutlinedButton(onClick = { notify = true }, modifier = Modifier.fillMaxWidth()) {
                Text("📩 " + stringResource(R.string.gn_button, lastAbsent.size), fontWeight = FontWeight.Bold)
            }
        }
    }
    if (notify) GuardianNotifyDialog(lastAbsent, lastClass, schoolName, lastWhen) { notify = false }
}

@Composable
private fun TopTab(
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    sessions: List<AbsenceSessionEntity>,
    absences: List<AbsenceEntity>,
    yearStart: String
) {
    var from by rememberSaveable { mutableStateOf(yearStart) }
    var to by rememberSaveable { mutableStateOf(today()) }
    var classFilter by rememberSaveable { mutableStateOf<String?>(null) }
    val names = classes.associate { it.id to it.name }
    val inRange = sessions.filter { it.date in from..to && (classFilter == null || it.classId == classFilter) }.map { it.id }.toSet()
    val counts = absences.filter { it.sessionId in inRange }.groupingBy { it.studentId }.eachCount()
    val byId = students.associateBy { it.id }
    val ranking = counts.entries.mapNotNull { (id, n) -> byId[id]?.let { it to n } }.sortedByDescending { it.second }
    val totalHours = counts.values.sum()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateButton(stringResource(R.string.ac_from), from, Modifier.weight(1f)) { from = it }
                DateButton(stringResource(R.string.ac_to), to, Modifier.weight(1f)) { to = it }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { FilterChip(selected = classFilter == null, onClick = { classFilter = null }, label = { Text(stringResource(R.string.ac_all_classes)) }) }
                items(classes, key = { it.id }) { c ->
                    FilterChip(selected = classFilter == c.id, onClick = { classFilter = c.id }, label = { Text(c.name) })
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(stringResource(R.string.ac_summary, inRange.size, totalHours, ranking.size),
                    Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        if (from > to) item { Text(stringResource(R.string.ac_bad_range), color = MaterialTheme.colorScheme.error) }
        if (ranking.isEmpty()) item {
            Text(stringResource(R.string.ac_no_absence), color = Green, modifier = Modifier.padding(12.dp))
        }
        items(ranking.take(100), key = { "r-" + it.first.id }) { (st, n) ->
            val rank = ranking.indexOfFirst { it.first.id == st.id } + 1
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$rank", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(30.dp))
                Column(Modifier.weight(1f)) {
                    Text(st.nameAr.ifBlank { st.nameFr }, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${names[st.classId].orEmpty()} — ${stringResource(R.string.ac_roll, st.rollNumber)}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(color = Red.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(stringResource(R.string.abs_hours, n), Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
