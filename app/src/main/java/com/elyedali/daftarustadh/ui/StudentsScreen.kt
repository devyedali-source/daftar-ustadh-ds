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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.data.Gender
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.sp
import java.util.UUID

/** طلاب القسم: لائحة، إضافة، تعديل، حذف بالرمز 9999، واستيراد من Excel. */
@Composable
fun StudentsScreen(
    schoolClass: ClassEntity,
    students: List<StudentEntity>,
    onSave: (StudentEntity) -> Unit,
    onDelete: (StudentEntity) -> Unit,
    onImport: () -> Unit,
    onShowSubjects: () -> Unit,
    onBack: () -> Unit,
    /** null في «أقسامي» عند الأستاذ: اللائحة للقراءة فقط، لأن مرجعها من أنشأ القسم. */
    onShare: ((com.elyedali.daftarustadh.transfer.ClassFileType) -> Unit)? = null,
    readOnly: Boolean = false,
    subtitle: String? = null,
    onOpenResults: (() -> Unit)? = null,
    showHeader: Boolean = true
) {
    var shareMenu by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudentEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<StudentEntity?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showHeader) ScreenHeader(
                schoolClass.name, onBack,
                listOfNotNull(subtitle, stringResource(R.string.students_count, students.size))
            ) { HeaderAction(stringResource(R.string.subjects), onShowSubjects) }
            if (!readOnly) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { adding = true }, modifier = Modifier.weight(1f)) { Text("+  " + stringResource(R.string.add_student)) }
                    OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.import_excel)) }
                }
            }
            if (onOpenResults != null) {
                Button(onClick = onOpenResults, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.class_results)) }
            }
            if (onShare != null && students.isNotEmpty()) {
                Column {
                    OutlinedButton(onClick = { shareMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.share_class))
                    }
                    androidx.compose.material3.DropdownMenu(expanded = shareMenu, onDismissRequest = { shareMenu = false }) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Column {
                                    Text(stringResource(R.string.share_teacher_file))
                                    Text(stringResource(R.string.share_teacher_file_hint), style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = { shareMenu = false; onShare(com.elyedali.daftarustadh.transfer.ClassFileType.TEACHER) }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Column {
                                    Text(stringResource(R.string.share_full_file))
                                    Text(stringResource(R.string.share_full_file_hint), style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = { shareMenu = false; onShare(com.elyedali.daftarustadh.transfer.ClassFileType.FULL) }
                        )
                    }
                }
            }
            if (students.isEmpty()) Text(stringResource(R.string.no_students))
            LazyColumn {
                items(students, key = { it.id }) { s ->
                    Row(
                        Modifier.fillMaxWidth().clickable(enabled = !readOnly) { editing = s }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.rollNumber.toString(), Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                        Column(Modifier.weight(1f)) {
                            if (s.nameAr.isNotBlank()) Text(s.nameAr)
                            if (s.nameFr.isNotBlank()) Text(s.nameFr, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (s.gender.isNotBlank()) Text(genderLabel(s.gender), style = MaterialTheme.typography.labelSmall,
                                color = if (s.gender == Gender.GIRL) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                            if (s.rim.isNotBlank()) Text(s.rim, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (adding || editing != null) {
        StudentEditDialog(
            classId = schoolClass.id,
            existing = editing,
            others = students.filter { it.id != editing?.id },
            onSave = { onSave(it); adding = false; editing = null },
            onDelete = editing?.let { e -> { toDelete = e; editing = null } },
            onDismiss = { adding = false; editing = null }
        )
    }
    toDelete?.let { s ->
        SensitiveConfirmDialog(
            title = stringResource(R.string.delete_student_title),
            message = listOf(s.nameAr, s.nameFr).filter { it.isNotBlank() }.joinToString(" — "),
            onConfirm = { onDelete(s); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun StudentEditDialog(
    classId: String,
    existing: StudentEntity?,
    others: List<StudentEntity>,
    onSave: (StudentEntity) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // معرّف الطالب ثابت من الآن: تُحفظ صورته باسمه حتى قبل الحفظ
    val studentId = remember { existing?.id ?: UUID.randomUUID().toString() }
    var photoVersion by remember { mutableStateOf(0) }
    val photo = remember(photoVersion) { com.elyedali.daftarustadh.data.StudentPhotos.load(context, studentId) }
    fun onPicked(uri: android.net.Uri?) {
        if (uri != null && com.elyedali.daftarustadh.data.StudentPhotos.save(context, studentId, uri)) photoVersion++
    }
    val pickPhoto = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()) { onPicked(it) }
    var cameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val takePhoto = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { ok -> if (ok) onPicked(cameraUri) }
    val suggestedRoll = (others.maxOfOrNull { it.rollNumber } ?: 0) + 1
    var roll by remember { mutableStateOf((existing?.rollNumber ?: suggestedRoll).toString()) }
    var nameAr by remember { mutableStateOf(existing?.nameAr ?: "") }
    var nameFr by remember { mutableStateOf(existing?.nameFr ?: "") }
    var rim by remember { mutableStateOf(existing?.rim ?: "") }
    var nationalId by remember { mutableStateOf(existing?.nationalId ?: "") }
    var phone1 by remember { mutableStateOf(existing?.guardianPhone1 ?: "") }
    var phone2 by remember { mutableStateOf(existing?.guardianPhone2 ?: "") }
    var gender by remember { mutableStateOf(existing?.gender ?: "") }

    val rollNumber = roll.toIntOrNull()
    val rollTaken = rollNumber != null && others.any { it.rollNumber == rollNumber }
    val canSave = rollNumber != null && rollNumber > 0 && !rollTaken && (nameAr.isNotBlank() || nameFr.isNotBlank())
    val digits = KeyboardOptions(keyboardType = KeyboardType.Number)
    val phone = KeyboardOptions(keyboardType = KeyboardType.Phone)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing == null) R.string.add_student else R.string.edit_student)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // صورة الطالب (لبطاقة الطالب)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.foundation.layout.Box(
                        Modifier.size(64.dp, 80.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo != null) androidx.compose.foundation.Image(photo.asImageBitmap(), null,
                            Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                        else Text("👤", fontSize = 28.sp)
                    }
                    Column {
                        TextButton(onClick = {
                            val dir = java.io.File(context.cacheDir, "photos").apply { mkdirs() }
                            val f = java.io.File(dir, "student-${System.currentTimeMillis()}.jpg")
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".files", f)
                            cameraUri = uri
                            try { takePhoto.launch(uri) } catch (e: Exception) { pickPhoto.launch("image/*") }
                        }) { Text("📷 " + stringResource(R.string.photo_take)) }
                        TextButton(onClick = { pickPhoto.launch("image/*") }) { Text("🖼️ " + stringResource(R.string.photo_pick)) }
                        if (photo != null) TextButton(onClick = {
                            com.elyedali.daftarustadh.data.StudentPhotos.delete(context, studentId); photoVersion++
                        }) { Text(stringResource(R.string.photo_delete), color = MaterialTheme.colorScheme.error) }
                    }
                }
                OutlinedTextField(roll, { roll = it.filter(Char::isDigit).take(3) }, label = { Text(stringResource(R.string.roll_number)) },
                    singleLine = true, keyboardOptions = digits, isError = rollTaken,
                    supportingText = { if (rollTaken) Text(stringResource(R.string.roll_taken)) },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(nameAr, { nameAr = it }, label = { Text(stringResource(R.string.name_ar)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(nameFr, { nameFr = it }, label = { Text(stringResource(R.string.name_fr)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.gender))
                    androidx.compose.material3.FilterChip(selected = gender == Gender.BOY, onClick = { gender = if (gender == Gender.BOY) "" else Gender.BOY },
                        label = { Text(stringResource(R.string.boy)) })
                    androidx.compose.material3.FilterChip(selected = gender == Gender.GIRL, onClick = { gender = if (gender == Gender.GIRL) "" else Gender.GIRL },
                        label = { Text(stringResource(R.string.girl)) })
                }
                OutlinedTextField(rim, { rim = it.trim() }, label = { Text(stringResource(R.string.rim)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(nationalId, { nationalId = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.national_id)) }, singleLine = true, keyboardOptions = digits, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone1, { phone1 = it.filter { c -> c.isDigit() || c == '+' || c == ' ' } }, label = { Text(stringResource(R.string.guardian_phone_1)) }, singleLine = true, keyboardOptions = phone, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone2, { phone2 = it.filter { c -> c.isDigit() || c == '+' || c == ' ' } }, label = { Text(stringResource(R.string.guardian_phone_2)) }, singleLine = true, keyboardOptions = phone, modifier = Modifier.fillMaxWidth())
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(stringResource(R.string.delete_student_title), color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = canSave, onClick = {
                onSave(
                    StudentEntity(
                        id = studentId,
                        classId = classId,
                        rollNumber = rollNumber!!,
                        nameAr = nameAr.trim(),
                        nameFr = nameFr.trim(),
                        rim = rim.trim(),
                        nationalId = nationalId.trim(),
                        guardianPhone1 = phone1.trim(),
                        guardianPhone2 = phone2.trim(),
                        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                        gender = gender
                    )
                )
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
fun genderLabel(g: String): String = when (g) {
    Gender.BOY -> stringResource(R.string.boy)
    Gender.GIRL -> stringResource(R.string.girl)
    else -> ""
}
