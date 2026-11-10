package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeMath
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.data.TimetableAssignmentEntity

/** حالة سجل مادة في قسم لفصل معيّن. */
private enum class RecState { DONE, PARTIAL, MISSING }

private data class RecItem(val c: ClassEntity, val code: String, val state: RecState, val done: Int, val total: Int)

/**
 * متابعة سجلات الأساتذة (الإدارة): لكل فصل، أي الأساتذة أرسلوا نتائج كل أقسامهم وموادهم كاملة، ومن بقي.
 * القسم الذي اكتملت كل مواده يمكن إنتاج كشوفه مباشرة من هنا.
 */
@Composable
fun RecordsFollowScreen(
    classes: List<ClassEntity>,
    /** مواد كل قسم (رموزها) حسب منهاج مستواه. */
    subjectsOf: (ClassEntity) -> List<String>,
    subjectLabel: (String) -> String,
    assignments: List<TimetableAssignmentEntity>,
    students: List<StudentEntity>,
    grades: List<GradeEntity>,
    onUploadMany: () -> Unit,
    onGenerate: (ClassEntity, Int) -> Unit,
    onBack: () -> Unit
) {
    var term by rememberSaveable { mutableIntStateOf(1) }
    val studentsBy = students.groupBy { it.classId }
    val gradesBy = grades.groupBy { Triple(it.classId, it.subjectCode, it.studentId) }
        .mapValues { e -> e.value.associate { it.slot to it.value } }
    val teacherOf = assignments.associate { (it.classId to it.subjectCode) to it.teacherName }

    val items = classes.sortedBy { it.name }.flatMap { c ->
        val st = studentsBy[c.id].orEmpty()
        if (st.isEmpty()) emptyList() else subjectsOf(c).map { code ->
            val gs = st.map { gradesBy[Triple(c.id, code, it.id)].orEmpty() }
            val done = gs.count { GradeMath.isComplete(term, it) }
            val any = gs.any { g -> g[GradeSlot.exam(term)] != null || GradeSlot.testsOf(term, g).isNotEmpty() }
            RecItem(c, code, when { done == st.size -> RecState.DONE; any -> RecState.PARTIAL; else -> RecState.MISSING }, done, st.size)
        }
    }
    val noTeacher = stringResource(R.string.rf_no_teacher)
    val byTeacher = items.groupBy { teacherOf[it.c.id to it.code]?.takeIf { n -> n.isNotBlank() } ?: noTeacher }
        .toList().sortedWith(compareBy({ it.first == noTeacher }, { it.first }))
    val teachersDone = byTeacher.count { (name, list) -> name != noTeacher && list.all { it.state == RecState.DONE } }
    val teachersTotal = byTeacher.count { it.first != noTeacher }
    val readyClasses = classes.filter { c -> items.filter { it.c.id == c.id }.let { l -> l.isNotEmpty() && l.all { it.state == RecState.DONE } } }
        .sortedBy { it.name }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            ScreenHeader(stringResource(R.string.rf_title), onBack)
            TabRow(selectedTabIndex = term - 1) {
                listOf(R.string.term_1, R.string.term_2, R.string.term_3).forEachIndexed { i, res ->
                    Tab(selected = term == i + 1, onClick = { term = i + 1 }, text = { Text(stringResource(res)) })
                }
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Button(onClick = onUploadMany, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("📥 " + stringResource(R.string.bu_button), fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.rf_teachers_done, teachersDone, teachersTotal), fontWeight = FontWeight.Black, fontSize = 15.sp)
                            Text(stringResource(R.string.rf_classes_ready, readyClasses.size, classes.size), fontSize = 13.sp)
                            if (teachersTotal == 0) Text(stringResource(R.string.rf_no_assignments), fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                // الأقسام الجاهزة: إنتاج كشوفها
                if (readyClasses.isNotEmpty()) item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✅ " + stringResource(R.string.rf_ready_title), fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            readyClasses.forEach { c ->
                                Button(onClick = { onGenerate(c, term) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White)) {
                                    Text("📄 " + stringResource(R.string.rf_generate, c.name), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                items(byTeacher, key = { it.first }) { (name, list) ->
                    val complete = list.all { it.state == RecState.DONE }
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text((if (complete) "✅ " else "⏳ ") + name, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                Text(stringResource(R.string.rf_count, list.count { it.state == RecState.DONE }, list.size), fontSize = 12.sp,
                                    color = if (complete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                            list.forEach { it ->
                                val (mark, color, extra) = when (it.state) {
                                    RecState.DONE -> Triple("✅", Color(0xFF2E7D32), stringResource(R.string.rf_state_done))
                                    RecState.PARTIAL -> Triple("🟠", Color(0xFFE65100), stringResource(R.string.rf_state_partial, it.done, it.total))
                                    RecState.MISSING -> Triple("❌", MaterialTheme.colorScheme.error, stringResource(R.string.rf_state_missing))
                                }
                                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("$mark ${it.c.name} — ${subjectLabel(it.code)}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Text(extra, fontSize = 11.sp, color = color)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
