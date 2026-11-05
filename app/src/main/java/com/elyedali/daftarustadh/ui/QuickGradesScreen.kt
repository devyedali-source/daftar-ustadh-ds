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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
 * فيعيد الأستاذ ورقة فيها أرقام النداء والنقاط.
 * جدول مثل Excel: سطر لكل رقم نداء وخانة للنقطة، فلا يكتب المستخدم رقم النداء ولا المسافات.
 * أو تلتقط صورة الورقة فيملأ التطبيق الخانات منها، ثم تراجعها قبل الحفظ.
 */
/** قائمة منسدلة (Combobox) لخطوة اختيار. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepCombo(label: String, options: List<Pair<String, String>>, selected: String?, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == selected }?.second.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(key); open = false })
            }
        }
    }
}

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
    // الاختيار خطوة بخطوة: الفصل ← المادة ← الخانة ← الجدول
    var term by rememberSaveable { mutableStateOf<Int?>(null) }
    var subject by rememberSaveable { mutableStateOf(initialSubject?.takeIf { c -> subjects.any { it.code == c } }) }
    var exam by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var saved by rememberSaveable { mutableStateOf<Int?>(null) }
    val t = term
    val e = exam
    val slot: String? = if (t == null || e == null) null else if (e) GradeSlot.exam(t) else GradeSlot.test(t)
    val ready = slot != null && subject != null
    // ما كُتب في الجدول: رقم النداء ← النقطة كنص (يُفرَّغ عند تغيير المادة أو الخانة)
    val entries = remember(subject, slot) { mutableStateMapOf<Int, String>() }
    // آخر نص قُرئ من صورة، والأسطر التي لم تُفهم منه
    var lastOcr by rememberSaveable { mutableStateOf("") }
    var ocrBad by remember { mutableStateOf<List<String>>(emptyList()) }
    val sorted = students.sortedBy { it.rollNumber }
    val byRoll = sorted.associateBy { it.rollNumber }

    fun fillFrom(text: String) {
        val parsed = parseRollGrades(text)
        val good = parsed.filter { it.roll != null && it.grade != null && it.roll in byRoll }
        good.forEach { entries[it.roll!!] = GradeMath.format(it.grade) }
        ocrBad = parsed.filter { it !in good }.map { it.line }
        saved = null
    }
    // نص الصورة يملأ خانات الجدول للمراجعة
    LaunchedEffect(ocrText) {
        if (ocrText != null) { lastOcr = ocrText; fillFrom(ocrText); onOcrConsumed() }
    }

    val current = grades.filter { it.subjectCode == subject && it.slot == slot }.associateBy { it.studentId }
    val filled = entries.filterValues { it.isNotBlank() }
    val invalid = filled.filter { (_, v) -> GradeMath.parse(v) == null }.keys
    val values = filled.mapNotNull { (roll, v) -> val st = byRoll[roll]; val g = GradeMath.parse(v); if (st != null && g != null) st.id to g else null }.toMap()
    val missing = sorted.filter { st -> st.id !in values && current[st.id] == null }
    val focus = LocalFocusManager.current

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.qg_title) + " — " + className, onMenu = null, onBack = onBack)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { Text(stringResource(R.string.qg_intro), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                item {
                    StepCombo(stringResource(R.string.qg_pick_term),
                        listOf("1" to stringResource(R.string.term_1), "2" to stringResource(R.string.term_2), "3" to stringResource(R.string.term_3)),
                        term?.toString()) { term = it.toInt(); saved = null; ocrBad = emptyList() }
                }
                if (term != null) item {
                    StepCombo(stringResource(R.string.qg_pick_subject), subjects.map { it.code to subjectLabel(it) }, subject) {
                        subject = it; saved = null; ocrBad = emptyList()
                    }
                }
                if (term != null && subject != null) item {
                    StepCombo(stringResource(R.string.qg_pick_slot),
                        listOf("T" to stringResource(R.string.test_short), "E" to stringResource(R.string.exam_short)),
                        exam?.let { if (it) "E" else "T" }) { exam = it == "E"; saved = null; ocrBad = emptyList() }
                }
                if (!ready) item {
                    Text(stringResource(R.string.qg_pick_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (ready) item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onCamera, enabled = !ocrBusy, modifier = Modifier.weight(1f)) {
                            Text("📷 " + stringResource(R.string.qg_camera), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(onClick = onPhoto, enabled = !ocrBusy, modifier = Modifier.weight(1f)) {
                            Text("🖼️ " + stringResource(R.string.qg_gallery), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (ocrBusy) Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("  " + stringResource(R.string.qg_reading), fontSize = 13.sp)
                    }
                }
                // إن قُرئت الورقة بالعكس (النقطة قبل الرقم): يُقلب أول وآخر عدد في كل سطر ويُعاد ملء الجدول
                if (ready && lastOcr.isNotBlank()) item {
                    TextButton(onClick = {
                        lastOcr = lastOcr.lines().joinToString("\n") { line ->
                            val nums = Regex("""\d+(?:[.,]\d+)?""").findAll(line).map { it.value }.toList()
                            if (nums.size >= 2) "${nums.last()}  ${nums.first()}" else line
                        }
                        entries.clear(); fillFrom(lastOcr)
                    }) { Text("⇄ " + stringResource(R.string.qg_swap), fontSize = 12.sp) }
                }
                if (ready && ocrBad.isNotEmpty()) item {
                    Text(stringResource(R.string.qg_bad, ocrBad.joinToString(" | ")), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                // رأس الجدول
                if (ready) item {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.qg_col_roll), Modifier.width(56.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Text(stringResource(R.string.qg_col_name), Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(stringResource(R.string.qg_col_grade), Modifier.width(110.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
                if (ready && sorted.isEmpty()) item {
                    Text(stringResource(R.string.qg_no_students), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                if (ready) items(sorted, key = { it.id }) { st ->
                    val v = entries[st.rollNumber].orEmpty()
                    val bad = v.isNotBlank() && GradeMath.parse(v) == null
                    val old = current[st.id]?.value
                    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("${st.rollNumber}", Modifier.width(56.dp), fontWeight = FontWeight.Black, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text(st.nameAr.ifBlank { st.nameFr }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = v,
                            onValueChange = { t ->
                                val v = t.filter { it.isDigit() || it == '.' || it == ',' || it in '٠'..'٩' || it == '٫' }.take(5)
                                // لا تُقبل نقطة أكبر من 20
                                if (v.isBlank() || GradeMath.parse(v) != null) { entries[st.rollNumber] = v; saved = null }
                            },
                            modifier = Modifier.width(110.dp),
                            singleLine = true,
                            isError = bad,
                            // النقطة المحفوظة تظهر باهتة؛ ما تكتبه يحلّ محلها
                            placeholder = { if (old != null) Text(GradeMath.format(old), color = MaterialTheme.colorScheme.outline, fontSize = 14.sp) },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) })
                        )
                    }
                }
                if (ready) item { Text(stringResource(R.string.qg_table_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                if (invalid.isNotEmpty()) item {
                    Text(stringResource(R.string.qg_invalid, invalid.sorted().joinToString("، ")), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (values.isNotEmpty() && missing.isNotEmpty()) item {
                    Text(stringResource(R.string.qg_missing, missing.joinToString("، ") { "${it.rollNumber}" }), fontSize = 12.sp,
                        color = Color(0xFFE65100))
                }
                if (ready) item {
                    Button(
                        onClick = {
                            val sl = slot
                            subject?.let { s -> if (sl != null) {
                                onSave(s, sl, values)
                                saved = values.size
                                entries.clear(); lastOcr = ""; ocrBad = emptyList()
                                focus.clearFocus()
                            } }
                        },
                        enabled = ready && values.isNotEmpty() && invalid.isEmpty(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                    ) { Text("💾 " + stringResource(R.string.qg_save, values.size), fontWeight = FontWeight.Bold) }
                }
                saved?.let { n -> item { Text("✅ " + stringResource(R.string.qg_saved, n), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) } }
            }
        }
    }
}
