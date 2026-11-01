package com.elyedali.daftarustadh.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.TimetableAssignmentEntity
import com.elyedali.daftarustadh.data.TimetableTeacher
import com.elyedali.daftarustadh.exchange.ExchangeAd

/**
 * مشاركة سجلات الأساتذة (الإدارة): لكل أستاذ زر يرسل له عبر واتساب حزمة فيها
 * كل أقسامه ولوائح طلابها مع المواد التي يدرّسها في كل قسم.
 * الأساتذة وتوزيع الأقسام عليهم هم أنفسهم في «الجدول الزمني ← أساتذة المواد».
 */
@Composable
fun ShareRecordsScreen(
    teachers: List<TimetableTeacher>,
    classes: List<ClassEntity>,
    assignments: List<TimetableAssignmentEntity>,
    subjectLabel: (String) -> String,
    sent: Set<String>,
    onSend: (TimetableTeacher) -> Unit,
    onSavePhone: (TimetableTeacher, String) -> Unit,
    onManageTeachers: () -> Unit,
    onBack: () -> Unit
) {
    val classNames = classes.associate { it.id to it.name }
    var editing by remember { mutableStateOf<TimetableTeacher?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.sr_title), onMenu = null, onBack = onBack)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(stringResource(R.string.sr_intro), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    OutlinedButton(onClick = onManageTeachers, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("👨‍🏫 " + stringResource(R.string.sr_manage_teachers), fontWeight = FontWeight.Bold)
                    }
                }
                if (teachers.isEmpty()) item {
                    Text(stringResource(R.string.sr_no_teachers), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp))
                }
                items(teachers, key = { it.name }) { t ->
                    val mine = assignments.filter { it.teacherName == t.name && it.classId in classNames }
                    val byClass = mine.groupBy { it.classId }.entries.sortedBy { classNames[it.key] }
                    val hasPhone = t.phone.isNotBlank()
                    val done = t.name in sent
                    Card(
                        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👨‍🏫 " + t.name, fontWeight = FontWeight.Black, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                if (done) Text("✅ " + stringResource(R.string.sr_sent), fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { editing = t }, contentPadding = PaddingValues(0.dp)) {
                                Text(
                                    if (hasPhone) "🟢 " + ExchangeAd.localPhone(t.phone) + "  ✏️"
                                    else "➕ " + stringResource(R.string.sr_add_whatsapp),
                                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = if (hasPhone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }
                            if (byClass.isEmpty()) {
                                Text(stringResource(R.string.sr_no_classes), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            } else {
                                byClass.forEach { (cid, list) ->
                                    Text("• ${classNames[cid]}: " + list.joinToString("، ") { subjectLabel(it.subjectCode) },
                                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Button(
                                onClick = { onSend(t) },
                                enabled = hasPhone && byClass.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White)
                            ) {
                                Text("📤 " + stringResource(if (done) R.string.sr_send_again else R.string.sr_send, byClass.size.toString()),
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { t ->
        var phone by remember(t.name) { mutableStateOf(ExchangeAd.localPhone(t.phone)) }
        val normalized = ExchangeAd.normalizePhone(phone)
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("🟢 " + t.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(phone, { phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' } },
                        label = { Text(stringResource(R.string.tt_teacher_whatsapp)) }, placeholder = { Text("36000000") },
                        singleLine = true, isError = phone.isNotBlank() && normalized == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                    if (phone.isNotBlank() && normalized == null)
                        Text(stringResource(R.string.ex_err_phone), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = normalized != null, onClick = { onSavePhone(t, normalized!!); editing = null }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
