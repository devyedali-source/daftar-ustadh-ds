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
    onBack: () -> Unit
) {
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    var choosing by remember { mutableStateOf(false) }
    val mine = classSubjects.filter { it.code in mySubjectCodes }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScreenHeader(
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
                        val subjectGrades = grades.filter { it.subjectCode == sub.code }
                        val filled = subjectGrades.size
                        val total = studentCount * GradeSlot.ALL.size
                        Card(Modifier.fillMaxWidth().clickable { onOpenSubject(sub.code) }) {
                            Column(Modifier.padding(14.dp)) {
                                Text(if (isFr) sub.nameFr else sub.nameAr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.subject_progress, sub.coefficient, filled, total), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                OutlinedButton(onClick = { choosing = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.choose_my_subjects)) }
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
