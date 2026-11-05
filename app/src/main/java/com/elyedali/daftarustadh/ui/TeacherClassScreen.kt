package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.elyedali.daftarustadh.data.GradeSlot

/** قسم عند الأستاذ: يختار مواده فيه، ثم يفتح كل مادة لإدراج نتائجها. */
@Composable
fun TeacherClassScreen(
    schoolClass: ClassEntity,
    schoolName: String?,
    classSubjects: List<CurriculumSubject>,
    mySubjectCodes: Set<String>,
    grades: List<GradeEntity>,
    studentCount: Int,
    onSetMySubjects: (Set<String>) -> Unit,
    onOpenSubject: (String) -> Unit,
    onShowStudents: () -> Unit,
    onBack: () -> Unit,
    showHeader: Boolean = true,
    /** الإدارة أسندت للأستاذ مواده في هذا القسم (حزمة الأستاذ): لا يظهر زر «اختيار موادي». */
    subjectsFromAdmin: Boolean = false
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    var choosing by remember { mutableStateOf(false) }
    val mine = classSubjects.filter { it.code in mySubjectCodes }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (showHeader) ScreenHeader(
                schoolClass.name, onBack,
                listOfNotNull(schoolName, stringResource(R.string.students_count, studentCount))
            ) { HeaderAction(stringResource(R.string.students_list), onShowStudents) }
            Text(stringResource(R.string.my_subjects_here), style = MaterialTheme.typography.titleMedium)
            if (mine.isEmpty()) {
                Text(stringResource(R.string.choose_my_subjects_hint))
                Button(onClick = { choosing = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.choose_my_subjects)) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(mine, key = { it.code }) { sub ->
                        // مؤشر الاكتمال لكل فصل بنفس قاعدة الكشوف وإرسال السجل
                        val byStudent = grades.filter { it.subjectCode == sub.code }.groupBy { it.studentId }
                            .mapValues { e -> e.value.associate { it.slot to it.value } }
                        val doneByTerm = (1..3).map { t -> byStudent.values.count { com.elyedali.daftarustadh.data.GradeMath.isComplete(t, it) } }
                        Card(Modifier.fillMaxWidth().clickable { onOpenSubject(sub.code) }) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(if (isFr) sub.nameFr else sub.nameAr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.coefficient) + " ${sub.coefficient}", style = MaterialTheme.typography.bodySmall)
                                doneByTerm.forEachIndexed { i, n ->
                                    val ok = studentCount > 0 && n >= studentCount
                                    Text(
                                        stringResource(R.string.term_progress, i + 1, n, studentCount) + if (ok) "  ✓" else "",
                                        style = MaterialTheme.typography.bodySmall, fontWeight = if (ok) FontWeight.Bold else FontWeight.Normal,
                                        color = if (ok) androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                if (!subjectsFromAdmin)
                    OutlinedButton(onClick = { choosing = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.choose_my_subjects)) }
                else Text(stringResource(R.string.subjects_from_admin), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (choosing) {
        var selected by remember { mutableStateOf(mySubjectCodes) }
        AlertDialog(
            onDismissRequest = { choosing = false },
            title = { Text(stringResource(R.string.choose_my_subjects)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(classSubjects, key = { it.code }) { sub ->
                        val on = sub.code in selected
                        Row(
                            Modifier.fillMaxWidth().clickable { selected = if (on) selected - sub.code else selected + sub.code },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = on, onCheckedChange = { selected = if (on) selected - sub.code else selected + sub.code })
                            Text(if (isFr) sub.nameFr else sub.nameAr)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onSetMySubjects(selected); choosing = false }) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = { choosing = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
