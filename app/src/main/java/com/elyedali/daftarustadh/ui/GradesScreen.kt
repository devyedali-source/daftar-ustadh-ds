package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.onFocusChanged
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
import com.elyedali.daftarustadh.data.GradeMark
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
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GradesScreen(
    schoolClass: ClassEntity,
    subject: CurriculumSubject,
    students: List<StudentEntity>,
    grades: List<GradeEntity>,
    testsMode: String,
    scale: List<RemarkLevel>,
    /** mark: علامة الغياب (GradeMark) أو null لنقطة عادية. */
    onSetGrade: (studentId: String, slot: String, value: Double?, mark: String?) -> Unit,
    onTestsMode: (String) -> Unit,
    /** true = سجل فارغ بـ70 خانة بلا أسماء؛ false = سجل بأسماء طلاب القسم وخانات فارغة. */
    onPrintRecord: (blank: Boolean) -> Unit,
    /** null عند الإدارة: إرسال السجل خاص بالأستاذ. */
    onSendRecord: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    var term by rememberSaveable { mutableIntStateOf(1) }
    val byStudent: Map<String, Map<String, Double>> = remember(grades) {
        grades.groupBy { it.studentId }.mapValues { e -> e.value.associate { it.slot to it.value } }
    }
    val marksBy: Map<String, Map<String, String>> = remember(grades) {
        grades.filter { it.mark != null }.groupBy { it.studentId }.mapValues { e -> e.value.associate { it.slot to it.mark!! } }
    }
    // نفس قاعدة الاكتمال المعتمدة في الكشوف وفي إرسال السجل
    val complete = students.count { s -> GradeMath.isComplete(term, byStudent[s.id].orEmpty()) }


    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            ScreenHeader(
                if (isFr) subject.nameFr else subject.nameAr, onBack,
                listOf("${schoolClass.name} — ${stringResource(R.string.coefficient)} ${subject.coefficient}")
            ) {
                var printMenu by remember { mutableStateOf(false) }
                androidx.compose.foundation.layout.Box {
                    HeaderAction(stringResource(R.string.print_record)) { printMenu = true }
                    androidx.compose.material3.DropdownMenu(expanded = printMenu, onDismissRequest = { printMenu = false }) {
                        androidx.compose.material3.DropdownMenuItem(text = { Text(stringResource(R.string.print_with_names)) },
                            onClick = { printMenu = false; onPrintRecord(false) })
                        androidx.compose.material3.DropdownMenuItem(text = { Text(stringResource(R.string.print_blank_70)) },
                            onClick = { printMenu = false; onPrintRecord(true) })
                    }
                }
            }
            if (onSendRecord != null) {
                androidx.compose.material3.Button(onClick = onSendRecord, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.send_record))
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
                        val mk = marksBy[s.id].orEmpty()
                        GradeField(s.id, GradeSlot.test(term), g[GradeSlot.test(term)], mk[GradeSlot.test(term)], onSetGrade, Modifier.width(68.dp))
                        GradeField(s.id, GradeSlot.exam(term), g[GradeSlot.exam(term)], mk[GradeSlot.exam(term)], onSetGrade, Modifier.width(68.dp))
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
    mark: String?,
    onSetGrade: (String, String, Double?, String?) -> Unit,
    modifier: Modifier
) {
    // الغياب يُعرض «غ.م» (بتبرير = 10) أو «غ» (بدون تبرير = 0)
    val markLabel = when (mark) {
        GradeMark.JUSTIFIED -> stringResource(R.string.gm_short_justified)
        GradeMark.UNJUSTIFIED -> stringResource(R.string.gm_short_unjustified)
        else -> null
    }
    // الخانة تعرض دائمًا النقطة المحفوظة؛ ولا تُعرض الكتابة الجارية إلا أثناء الكتابة فيها
    var focused by remember(studentId, slot) { mutableStateOf(false) }
    var text by remember(studentId, slot) { mutableStateOf(GradeMath.format(value)) }
    val shown = if (focused) text else markLabel ?: GradeMath.format(value)
    val invalid = focused && text.isNotBlank() && GradeMath.parse(text) == null
    OutlinedTextField(
        value = shown,
        onValueChange = { v ->
            text = v.take(5)
            if (text.isBlank()) onSetGrade(studentId, slot, null, null)
            else GradeMath.parse(text)?.let { onSetGrade(studentId, slot, it, null) }
        },
        singleLine = true,
        isError = invalid,
        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        modifier = modifier.padding(horizontal = 2.dp).onFocusChanged { f ->
            if (f.isFocused && !focused) text = if (mark != null) "" else GradeMath.format(value)
            focused = f.isFocused
        }
    )
}
