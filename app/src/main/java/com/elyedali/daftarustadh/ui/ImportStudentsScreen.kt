package com.elyedali.daftarustadh.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.importer.ImportGuess
import com.elyedali.daftarustadh.importer.ImportPreview
import com.elyedali.daftarustadh.importer.SpreadsheetException
import com.elyedali.daftarustadh.importer.SpreadsheetReader
import com.elyedali.daftarustadh.importer.StudentField
import com.elyedali.daftarustadh.importer.StudentImport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PICK_TYPES = arrayOf(
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.ms-excel",
    "text/csv",
    "text/comma-separated-values",
    "text/plain",
    "application/octet-stream"
)

/** استيراد الطلاب من Excel أو CSV: اختيار الملف ← مطابقة الأعمدة ← المعاينة ← الحفظ. */
@Composable
fun ImportStudentsScreen(
    schoolClass: ClassEntity,
    existing: List<StudentEntity>,
    onConfirm: (List<StudentEntity>) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var errorRes by remember { mutableStateOf<Int?>(null) }
    var rows by remember { mutableStateOf<List<List<String>>?>(null) }
    var guess by remember { mutableStateOf<ImportGuess?>(null) }
    val mapping = remember { mutableStateListOf<StudentField>() }
    var preview by remember { mutableStateOf<ImportPreview?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        loading = true; errorRes = null; preview = null
        scope.launch {
            try {
                val r = withContext(Dispatchers.IO) { SpreadsheetReader.read(context, uri) }
                val g = StudentImport.guess(r)
                rows = r; guess = g
                mapping.clear(); mapping.addAll(g.mapping)
            } catch (e: SpreadsheetException) {
                errorRes = when (e.reason) {
                    SpreadsheetException.Reason.OLD_XLS -> R.string.import_error_xls
                    SpreadsheetException.Reason.EMPTY -> R.string.import_error_empty
                    else -> R.string.import_error_read
                }
            } catch (e: Exception) {
                errorRes = R.string.import_error_read
            } finally {
                loading = false
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ScreenHeader(stringResource(R.string.import_title, schoolClass.name), onBack)
            when {
                loading -> CircularProgressIndicator()

                preview != null -> {
                    val p = preview!!
                    Text(stringResource(R.string.import_ready, p.students.size), fontWeight = FontWeight.Bold)
                    if (p.renumbered > 0) Text(stringResource(R.string.import_renumbered, p.renumbered))
                    if (p.skippedDuplicates > 0) Text(stringResource(R.string.import_duplicates, p.skippedDuplicates))
                    if (p.skippedEmpty > 0) Text(stringResource(R.string.import_empty_rows, p.skippedEmpty))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = p.students.isNotEmpty(), onClick = { onConfirm(p.students) }) {
                            Text(stringResource(R.string.import_confirm))
                        }
                        OutlinedButton(onClick = { preview = null }) { Text(stringResource(R.string.import_back_to_columns)) }
                    }
                    HorizontalDivider()
                    LazyColumn {
                        items(p.students, key = { it.id }) { s ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Text(s.rollNumber.toString(), Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                                Column(Modifier.weight(1f)) {
                                    if (s.nameAr.isNotBlank()) Text(s.nameAr)
                                    if (s.nameFr.isNotBlank()) Text(s.nameFr, style = MaterialTheme.typography.bodySmall)
                                    val extra = listOf(genderLabel(s.gender), s.rim, s.nationalId, s.guardianPhone1, s.guardianPhone2).filter { it.isNotBlank() }
                                    if (extra.isNotEmpty()) Text(extra.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }

                guess != null && rows != null -> {
                    val g = guess!!
                    Text(stringResource(R.string.import_columns_help))
                    val hasName = StudentField.NAME_AR in mapping || StudentField.NAME_FR in mapping
                    if (!hasName) Text(stringResource(R.string.import_need_name), color = MaterialTheme.colorScheme.error)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = hasName, onClick = {
                            preview = StudentImport.preview(rows!!, g.headerRowIndex, mapping.toList(), schoolClass.id, existing)
                        }) { Text(stringResource(R.string.import_next)) }
                        OutlinedButton(onClick = { picker.launch(PICK_TYPES) }) { Text(stringResource(R.string.import_other_file)) }
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(g.columns, key = { it.index }) { col ->
                            ColumnMappingCard(
                                title = col.header ?: stringResource(R.string.import_column_n, col.index + 1),
                                samples = col.samples,
                                field = mapping.getOrElse(col.index) { StudentField.IGNORE },
                                onChange = { f ->
                                    // لا تُربط خانة بعمودين: العمود السابق يصبح «تجاهل»
                                    if (f != StudentField.IGNORE) {
                                        for (i in mapping.indices) if (mapping[i] == f) mapping[i] = StudentField.IGNORE
                                    }
                                    mapping[col.index] = f
                                }
                            )
                        }
                    }
                }

                else -> {
                    Text(stringResource(R.string.import_intro))
                    errorRes?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    Button(onClick = { picker.launch(PICK_TYPES) }) { Text(stringResource(R.string.import_choose_file)) }
                }
            }
        }
    }
}

@Composable
private fun ColumnMappingCard(
    title: String,
    samples: List<String>,
    field: StudentField,
    onChange: (StudentField) -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                if (samples.isNotEmpty()) Text(samples.joinToString(" · "), style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Column {
                OutlinedButton(onClick = { menu = true }) { Text(stringResource(fieldLabel(field))) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    StudentField.entries.forEach { f ->
                        DropdownMenuItem(text = { Text(stringResource(fieldLabel(f))) }, onClick = { onChange(f); menu = false })
                    }
                }
            }
        }
    }
}

private fun fieldLabel(f: StudentField): Int = when (f) {
    StudentField.IGNORE -> R.string.field_ignore
    StudentField.ROLL -> R.string.roll_number
    StudentField.NAME_AR -> R.string.name_ar
    StudentField.NAME_FR -> R.string.name_fr
    StudentField.GENDER -> R.string.gender
    StudentField.RIM -> R.string.rim
    StudentField.NATIONAL_ID -> R.string.national_id
    StudentField.PHONE1 -> R.string.guardian_phone_1
    StudentField.PHONE2 -> R.string.guardian_phone_2
}
