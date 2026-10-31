package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.CurriculumSubject
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeMath
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.data.RemarkLevel
import com.elyedali.daftarustadh.data.RemarkScale
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.data.TestsMode

/**
 * إدراج نتائج مادة في قسم: لكل فصل خانتا الاختبار والامتحان، ثم معدل المادة وملاحظته.
 * كل خانة تُحفظ فور كتابة نقطة صحيحة (0 إلى 20)، والخانة الفارغة تُمسح.
 */
@Composable
fun GradesScreen(
    schoolClass: ClassEntity,
    subject: CurriculumSubject,
    students: List<StudentEntity>,
    grades: List<GradeEntity>,
    testsMode: String,
    scale: List<RemarkLevel>,
    onSetGrade: (studentId: String, slot: String, value: Double?) -> Unit,
    onTestsMode: (String) -> Unit,
    onBack: () -> Unit
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    var term by rememberSaveable { mutableIntStateOf(1) }
    val byStudent: Map<String, Map<String, Double>> = remember(grades) {
        grades.groupBy { it.studentId }.mapValues { e -> e.value.associate { it.slot to it.value } }
    }
    val complete = students.count { s ->
        val g = byStudent[s.id].orEmpty()
        g[GradeSlot.test(term)] != null && g[GradeSlot.exam(term)] != null
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                Column(Modifier.weight(1f)) {
                    Text(if (isFr) subject.nameFr else subject.nameAr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${schoolClass.name} — ${stringResource(R.string.coefficient)} ${subject.coefficient}", style = MaterialTheme.typography.bodySmall)
                }
            }
            TabRow(selectedTabIndex = term - 1) {
                listOf(R.string.term_1, R.string.term_2, R.string.term_3).forEachIndexed { i, res ->
                    Tab(selected = term == i + 1, onClick = { term = i + 1 }, text = { Text(stringResource(res)) })
                }
            }
            Text(
                stringResource(R.string.grades_complete, complete, students.size),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                stringResource(
                    when (term) { 1 -> R.string.rule_term_1; 2 -> R.string.rule_term_2; else -> R.string.rule_term_3 }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (term == 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    FilterChip(
                        selected = testsMode == TestsMode.AVERAGE,
                        onClick = { onTestsMode(TestsMode.AVERAGE) },
                        label = { Text(stringResource(R.string.tests_mode_average)) }
                    )
                    FilterChip(
                        selected = testsMode == TestsMode.BEST,
                        onClick = { onTestsMode(TestsMode.BEST) },
                        label = { Text(stringResource(R.string.tests_mode_best)) }
                    )
                }
            }
            // رأس الجدول
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.student), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.test_short), Modifier.width(68.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.exam_short), Modifier.width(68.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.average_short), Modifier.width(48.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
            LazyColumn {
                items(students, key = { it.id }) { s ->
                    val g = byStudent[s.id].orEmpty()
                    val avg = GradeMath.subjectAverage(term, g, testsMode)
                    val remark = RemarkScale.remarkFor(avg, scale)
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${s.rollNumber}. " + (if (isFr && s.nameFr.isNotBlank()) s.nameFr else s.nameAr.ifBlank { s.nameFr }), maxLines = 2)
                            if (remark != null) {
                                Text(if (isFr) remark.textFr else remark.textAr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        GradeField(s.id, GradeSlot.test(term), g[GradeSlot.test(term)], onSetGrade, Modifier.width(68.dp))
                        GradeField(s.id, GradeSlot.exam(term), g[GradeSlot.exam(term)], onSetGrade, Modifier.width(68.dp))
                        Text(GradeMath.format(avg), Modifier.width(48.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun GradeField(
    studentId: String,
    slot: String,
    value: Double?,
    onSetGrade: (String, String, Double?) -> Unit,
    modifier: Modifier
) {
    var text by remember(studentId, slot) { mutableStateOf(GradeMath.format(value)) }
    // إن تغيّرت القيمة من مكان آخر (رفع سجل مثلًا) تُحدَّث الخانة
    LaunchedEffect(value) {
        if (GradeMath.parse(text) != value) text = GradeMath.format(value)
    }
    val invalid = text.isNotBlank() && GradeMath.parse(text) == null
    OutlinedTextField(
        value = text,
        onValueChange = { v ->
            text = v.take(5)
            if (text.isBlank()) onSetGrade(studentId, slot, null)
            else GradeMath.parse(text)?.let { onSetGrade(studentId, slot, it) }
        },
        singleLine = true,
        isError = invalid,
        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        modifier = modifier.padding(horizontal = 2.dp)
    )
}
