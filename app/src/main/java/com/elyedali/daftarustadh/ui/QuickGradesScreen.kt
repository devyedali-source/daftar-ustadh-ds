package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.CurriculumSubject
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeMath
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.data.StudentEntity

/** سطر مفهوم من النص: رقم النداء والنقطة (أو خطأ). */
data class RollGrade(val line: String, val roll: Int?, val grade: Double?)

/**
 * يقرأ «رقم النداء والنقطة» من كل سطر: «3 12.5» أو «3: 12,5» أو «3 - 12» وبالأرقام العربية.
 * أول عدد صحيح في السطر = رقم النداء، وآخر عدد = النقطة.
 */
fun parseRollGrades(text: String): List<RollGrade> =
    text.map { c -> if (c in '٠'..'٩') '0' + (c - '٠') else c }.joinToString("")
        .replace('٫', '.')
        .lines().map { it.trim() }.filter { it.isNotEmpty() }
        .map { line ->
            val nums = Regex("""\d+(?:[.,]\d+)?""").findAll(line).map { it.value.replace(',', '.') }.toList()
            if (nums.size < 2) RollGrade(line, null, null)
            else RollGrade(line, nums.first().toDoubleOrNull()?.takeIf { it % 1.0 == 0.0 }?.toInt(), GradeMath.parse(nums.last()))
        }

/**
 * إدخال النتائج بأرقام النداء (الإدارة): للمؤسسات التي لا ترسل الأسماء للأساتذة،
 * فيعيد الأستاذ ورقة فيها أرقام النداء والنقاط. تكتبها الإدارة سطرًا سطرًا، أو تلتقط صورة الورقة
 * فيقرأ التطبيق الأرقام منها، ثم تراجعها قبل الحفظ.
 */
@Composable
fun QuickGradesScreen(
    className: String,
    subjects: List<CurriculumSubject>,
    subjectLabel: (CurriculumSubject) -> String,
    students: List<StudentEntity>,
    grades: List<GradeEntity>,
    initialSubject: String?,
    ocrText: String?,
    ocrBusy: Boolean,
    onPhoto: () -> Unit,
    onCamera: () -> Unit,
    onOcrConsumed: () -> Unit,
    onSave: (subjectCode: String, slot: String, values: Map<String, Double>) -> Unit,
    onBack: () -> Unit
) {
    var subject by rememberSaveable { mutableStateOf(initialSubject ?: subjects.firstOrNull()?.code) }
    var term by rememberSaveable { mutableIntStateOf(1) }
    var exam by rememberSaveable { mutableStateOf(true) }
    var text by rememberSaveable { mutableStateOf("") }
    var saved by rememberSaveable { mutableStateOf<Int?>(null) }
    // نص الصورة يُضاف إلى الخانة للمراجعة
    androidx.compose.runtime.LaunchedEffect(ocrText) {
        if (ocrText != null) { text = ocrText; saved = null; onOcrConsumed() }
    }
    val slot = if (exam) GradeSlot.exam(term) else GradeSlot.test(term)
    val byRoll = students.associateBy { it.rollNumber }
    val parsed = parseRollGrades(text)
    val badLines = parsed.filter { it.roll == null || it.grade == null || it.roll !in byRoll }
    val dupRolls = parsed.mapNotNull { it.roll }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    val good = parsed.filter { it.roll != null && it.grade != null && it.roll in byRoll && it.roll !in dupRolls }
    val current = grades.filter { it.subjectCode == subject && it.slot == slot }.associateBy { it.studentId }
    val missing = students.filter { st -> good.none { it.roll == st.rollNumber } && current[st.id] == null }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.qg_title) + " — " + className, onMenu = null, onBack = onBack)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text(stringResource(R.string.qg_intro), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects, key = { it.code }) { s ->
                            FilterChip(selected = subject == s.code, onClick = { subject = s.code; saved = null }, label = { Text(subjectLabel(s)) })
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        (1..3).forEach { t ->
                            FilterChip(selected = term == t, onClick = { term = t; saved = null }, label = { Text(stringResource(R.string.qg_term, t)) })
                        }
                        Text("|", color = MaterialTheme.colorScheme.outline)
                        FilterChip(selected = !exam, onClick = { exam = false; saved = null }, label = { Text(stringResource(R.string.test_short)) })
                        FilterChip(selected = exam, onClick = { exam = true; saved = null }, label = { Text(stringResource(R.string.exam_short)) })
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onCamera, enabled = !ocrBusy, modifier = Modifier.weight(1f)) { Text("📷 " + stringResource(R.string.qg_camera)) }
                        OutlinedButton(onClick = onPhoto, enabled = !ocrBusy, modifier = Modifier.weight(1f)) { Text("🖼️ " + stringResource(R.string.qg_gallery)) }
                    }
                    if (ocrBusy) Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("  " + stringResource(R.string.qg_reading), fontSize = 13.sp)
                    }
                }
                item {
                    OutlinedTextField(
                        text, { text = it; saved = null },
                        label = { Text(stringResource(R.string.qg_lines)) },
                        placeholder = { Text("1  12.5\n2  14\n3  9.75") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp)
                    )
                    Text(stringResource(R.string.qg_lines_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // إن قُرئت الورقة بالعكس (النقطة قبل الرقم): يُقلب أول وآخر عدد في كل سطر
                    if (text.isNotBlank()) androidx.compose.material3.TextButton(onClick = {
                        text = text.lines().joinToString("\n") { line ->
                            val nums = Regex("""\d+(?:[.,]\d+)?""").findAll(line).map { it.value }.toList()
                            if (nums.size >= 2) "${nums.last()}  ${nums.first()}" else line
                        }
                        saved = null
                    }) { Text("⇄ " + stringResource(R.string.qg_swap), fontSize = 12.sp) }
                }
                if (badLines.isNotEmpty()) item {
                    Text(stringResource(R.string.qg_bad, badLines.joinToString(" | ") { it.line }), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (dupRolls.isNotEmpty()) item {
                    Text(stringResource(R.string.qg_dup, dupRolls.sorted().joinToString("، ")), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (good.isNotEmpty()) item {
                    Text(stringResource(R.string.qg_preview, good.size, students.size), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                items(good.sortedBy { it.roll }, key = { "g-" + it.roll }) { rg ->
                    val st = byRoll.getValue(rg.roll!!)
                    val old = current[st.id]?.value
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${st.rollNumber}", fontWeight = FontWeight.Black, modifier = Modifier.width(34.dp))
                        Text(st.nameAr.ifBlank { st.nameFr }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                        if (old != null && old != rg.grade) Text(GradeMath.format(old) + " ← ", fontSize = 12.sp, color = Color(0xFFE65100))
                        Text(GradeMath.format(rg.grade), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (good.isNotEmpty() && missing.isNotEmpty()) item {
                    Text(stringResource(R.string.qg_missing, missing.joinToString("، ") { "${it.rollNumber}" }), fontSize = 12.sp,
                        color = Color(0xFFE65100))
                }
                item {
                    Button(
                        onClick = {
                            subject?.let { s ->
                                onSave(s, slot, good.associate { byRoll.getValue(it.roll!!).id to it.grade!! })
                                saved = good.size
                                text = ""
                            }
                        },
                        enabled = subject != null && good.isNotEmpty() && badLines.isEmpty() && dupRolls.isEmpty(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    ) { Text("💾 " + stringResource(R.string.qg_save, good.size), fontWeight = FontWeight.Bold) }
                }
                saved?.let { n -> item { Text("✅ " + stringResource(R.string.qg_saved, n), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) } }
            }
        }
    }
}
