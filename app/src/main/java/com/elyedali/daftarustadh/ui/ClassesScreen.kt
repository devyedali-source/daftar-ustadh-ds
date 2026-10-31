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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.CurriculumLevel
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.InstitutionEntity
import com.elyedali.daftarustadh.data.Stage

/** شاشة الأقسام عند الإدارة: لائحة أقسام المؤسسة في سنتها الدراسية، وإنشاء قسم، وعرض مواده. */
@Composable
fun ClassesScreen(
    institution: InstitutionEntity,
    classes: List<ClassEntity>,
    schoolNames: Map<String, String>,
    onCreate: (levelCode: String, number: Int, onResult: (Boolean) -> Unit) -> Unit,
    onOpen: (ClassEntity) -> Unit,
    onDelete: (ClassEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showCreate by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<ClassEntity?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.classes_title), style = MaterialTheme.typography.titleLarge)
                    Text("${institution.schoolName} — ${institution.schoolYear}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = { showCreate = true }, modifier = Modifier.fillMaxWidth()) {
                Text("+  " + stringResource(R.string.create_class))
            }
            if (classes.isEmpty()) {
                Text(stringResource(R.string.no_classes), style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(classes, key = { it.id }) { c ->
                    val level = CurriculumRepository.level(context, c.schoolYear, c.levelCode)
                    Card(Modifier.fillMaxWidth().clickable { onOpen(c) }) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(levelDescription(c.levelCode), style = MaterialTheme.typography.bodySmall)
                                if (c.institutionId != institution.id) {
                                    Text(schoolNames[c.institutionId] ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                if (level != null) {
                                    Text(
                                        stringResource(R.string.subjects_summary, level.subjects.size, level.totalCoefficient),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            TextButton(onClick = { toDelete = c }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateClassDialog(
            institution = institution,
            existing = classes,
            onCreate = onCreate,
            onDismiss = { showCreate = false }
        )
    }
    toDelete?.let { c ->
        SensitiveConfirmDialog(
            title = stringResource(R.string.delete_class_title, c.name),
            message = stringResource(R.string.delete_class_text),
            onConfirm = { onDelete(c); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateClassDialog(
    institution: InstitutionEntity,
    existing: List<ClassEntity>,
    onCreate: (String, Int, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val levels: List<CurriculumLevel> = remember(institution) {
        val stages = when (institution.stage) {
            Stage.COLLEGE -> listOf(Stage.COLLEGE)
            Stage.LYCEE -> listOf(Stage.LYCEE)
            else -> listOf(Stage.COLLEGE, Stage.LYCEE)
        }
        stages.flatMap { CurriculumRepository.creatableLevels(context, institution.schoolYear, it) }
    }
    var levelCode by remember { mutableStateOf<String?>(null) }
    var numberText by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val duplicateMsg = stringResource(R.string.class_exists)

    fun nextNumber(code: String): Int =
        (existing.filter { it.levelCode == code && it.institutionId == institution.id }.maxOfOrNull { it.number } ?: 0) + 1

    val number = numberText.toIntOrNull()
    val canCreate = levelCode != null && number != null && number in 1..99

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_class)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = menu, onExpandedChange = { menu = it }) {
                    OutlinedTextField(
                        value = levelCode?.let { "$it — ${levelDescription(it)}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.level)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        levels.forEach { l ->
                            DropdownMenuItem(
                                text = { Text("${l.code} — ${levelDescription(l.code)}") },
                                onClick = {
                                    levelCode = l.code
                                    numberText = nextNumber(l.code).toString()
                                    error = null
                                    menu = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { v -> numberText = v.filter { it.isDigit() }.take(2); error = null },
                    label = { Text(stringResource(R.string.class_number)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (levelCode != null && number != null) {
                    Text(
                        stringResource(R.string.class_name_preview, "$levelCode$number"),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = canCreate, onClick = {
                onCreate(levelCode!!, number!!) { ok -> if (ok) onDismiss() else error = duplicateMsg }
            }) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
fun ClassSubjectsDialog(c: ClassEntity, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    val level = remember(c) { CurriculumRepository.level(context, c.schoolYear, c.levelCode) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${c.name} — ${levelDescription(c.levelCode)}") },
        text = {
            Column {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(stringResource(R.string.subject), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.coefficient), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider()
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(level?.subjects ?: emptyList(), key = { it.code }) { s ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(if (isFr) s.nameFr else s.nameAr, Modifier.weight(1f))
                            Text(s.coefficient.toString())
                        }
                    }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(stringResource(R.string.total), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text((level?.totalCoefficient ?: 0).toString(), fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}
