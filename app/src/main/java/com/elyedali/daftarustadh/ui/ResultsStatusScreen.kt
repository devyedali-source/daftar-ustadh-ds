package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.CurriculumSubject
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeMath
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.data.SubjectRecordEntity

/** نتائج القسم عند الإدارة: شريط المواد المكتملة والمتبقية لكل فصل، ورفع سجلات الأساتذة. */
@Composable
fun ResultsStatusScreen(
    schoolClass: ClassEntity,
    subjects: List<CurriculumSubject>,
    students: List<StudentEntity>,
    grades: List<GradeEntity>,
    records: List<SubjectRecordEntity>,
    onUploadRecord: () -> Unit,
    onQuickEntry: (() -> Unit)? = null,
    onOpenSubject: (String) -> Unit,
    onGenerate: (term: Int) -> Unit,
    onOpenSettings: () -> Unit,
    showHeader: Boolean = true,
    showGenerate: Boolean = true,
    onBack: () -> Unit
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    var term by rememberSaveable { mutableIntStateOf(1) }
    val bySubject = remember(grades) {
        grades.groupBy { it.subjectCode }.mapValues { e ->
            e.value.groupBy { it.studentId }.mapValues { s -> s.value.associate { it.slot to it.value } }
        }
    }
    fun missing(code: String): Int = students.count { st ->
        !GradeMath.isComplete(term, bySubject[code]?.get(st.id).orEmpty())
    }
    val completeCount = subjects.count { students.isNotEmpty() && missing(it.code) == 0 }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showHeader) ScreenHeader(stringResource(R.string.class_results_title, schoolClass.name), onBack, listOf(stringResource(R.string.students_count, students.size)))
            TabRow(selectedTabIndex = term - 1) {
                listOf(R.string.term_1, R.string.term_2, R.string.term_3).forEachIndexed { i, res ->
                    Tab(selected = term == i + 1, onClick = { term = i + 1 }, text = { Text(stringResource(res)) })
                }
            }
            Text(stringResource(R.string.subjects_complete, completeCount, subjects.size), fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { if (subjects.isEmpty()) 0f else completeCount.toFloat() / subjects.size },
                modifier = Modifier.fillMaxWidth()
            )
            if (subjects.isNotEmpty() && completeCount == subjects.size) {
                Text(stringResource(R.string.all_subjects_complete), color = MaterialTheme.colorScheme.primary)
                if (showGenerate) Button(onClick = { onGenerate(term) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (term == 3) R.string.generate_annual else R.string.generate_term))
                }
            }
            if (showGenerate) TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.kashf_settings)) }
            Button(onClick = onUploadRecord, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.upload_record)) }
            if (onQuickEntry != null) androidx.compose.material3.OutlinedButton(onClick = onQuickEntry, modifier = Modifier.fillMaxWidth()) {
                Text("⚡ " + stringResource(R.string.qg_button))
            }
            Text(stringResource(R.string.upload_record_hint), style = MaterialTheme.typography.bodySmall)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(subjects, key = { it.code }) { sub ->
                    val miss = missing(sub.code)
                    val done = students.isNotEmpty() && miss == 0
                    val record = records.firstOrNull { it.subjectCode == sub.code }
                    val hasAny = bySubject[sub.code].orEmpty().isNotEmpty()
                    Card(
                        Modifier.fillMaxWidth().clickable { onOpenSubject(sub.code) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (done) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(if (isFr) sub.nameFr else sub.nameAr, fontWeight = FontWeight.Bold)
                                Text(
                                    when {
                                        record != null && record.teacherName.isNotBlank() -> stringResource(R.string.source_teacher, record.teacherName)
                                        hasAny -> stringResource(R.string.source_manual)
                                        else -> stringResource(R.string.source_none)
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                if (done) "✓" else stringResource(R.string.missing_students, miss),
                                color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
