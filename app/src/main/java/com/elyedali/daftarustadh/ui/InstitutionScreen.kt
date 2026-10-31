package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.InstitutionEntity
import com.elyedali.daftarustadh.data.SchoolDirectory
import com.elyedali.daftarustadh.data.SchoolYear
import com.elyedali.daftarustadh.data.Stage
import com.elyedali.daftarustadh.data.Wilaya
import java.util.UUID

/** شاشة تسجيل المؤسسة أو تعديلها (واجهة الإدارة). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionScreen(
    existing: InstitutionEntity?,
    onSave: (InstitutionEntity) -> Unit,
    onCancel: (() -> Unit)?
) {
    val context = LocalContext.current
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    val wilayas = remember { SchoolDirectory.wilayas(context) }

    var wilayaName by rememberSaveable { mutableStateOf(existing?.wilaya ?: "") }
    var schoolName by rememberSaveable { mutableStateOf(existing?.schoolName ?: "") }
    var schoolListId by rememberSaveable { mutableStateOf(existing?.schoolListId) }
    var manualEntry by rememberSaveable { mutableStateOf(existing != null && existing.schoolListId == null) }
    var stage by rememberSaveable { mutableStateOf(existing?.stage ?: "") }
    var schoolYear by rememberSaveable { mutableStateOf(existing?.schoolYear ?: SchoolYear.current()) }
    var showSchoolPicker by remember { mutableStateOf(false) }
    var wilayaMenu by remember { mutableStateOf(false) }
    var yearMenu by remember { mutableStateOf(false) }

    val wilaya = wilayas.firstOrNull { it.name == wilayaName }
    fun label(w: Wilaya) = if (isFr) w.nameFr else w.name
    val canSave = wilaya != null && schoolName.isNotBlank() && stage.isNotEmpty()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(R.string.institution_title), style = MaterialTheme.typography.headlineSmall)

            // الولاية
            ExposedDropdownMenuBox(expanded = wilayaMenu, onExpandedChange = { wilayaMenu = it }) {
                OutlinedTextField(
                    value = wilaya?.let { label(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.wilaya)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wilayaMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = wilayaMenu, onDismissRequest = { wilayaMenu = false }) {
                    wilayas.forEach { w ->
                        DropdownMenuItem(text = { Text(label(w)) }, onClick = {
                            if (w.name != wilayaName) {
                                wilayaName = w.name
                                if (!manualEntry) { schoolName = ""; schoolListId = null }
                            }
                            wilayaMenu = false
                        })
                    }
                }
            }

            // المؤسسة
            if (!manualEntry) {
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.school)) },
                    placeholder = { Text(stringResource(R.string.choose_school)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().clickable(enabled = wilaya != null) { showSchoolPicker = true }
                )
                if (wilaya == null) {
                    Text(stringResource(R.string.choose_wilaya_first), style = MaterialTheme.typography.bodySmall)
                }
            } else {
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it; schoolListId = null },
                    label = { Text(stringResource(R.string.school_manual)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { manualEntry = false; schoolName = ""; schoolListId = null }) {
                    Text(stringResource(R.string.back_to_list))
                }
            }

            // نوع المؤسسة
            Text(stringResource(R.string.stage), style = MaterialTheme.typography.titleSmall)
            listOf(
                Stage.COLLEGE to R.string.stage_college,
                Stage.LYCEE to R.string.stage_lycee,
                Stage.BOTH to R.string.stage_both
            ).forEach { (value, res) ->
                Row(
                    Modifier.fillMaxWidth().selectable(selected = stage == value, onClick = { stage = value }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = stage == value, onClick = { stage = value })
                    Text(stringResource(res))
                }
            }

            // السنة الدراسية
            ExposedDropdownMenuBox(expanded = yearMenu, onExpandedChange = { yearMenu = it }) {
                OutlinedTextField(
                    value = schoolYear,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.school_year)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = yearMenu, onDismissRequest = { yearMenu = false }) {
                    SchoolYear.choices().forEach { y ->
                        DropdownMenuItem(text = { Text(y) }, onClick = { schoolYear = y; yearMenu = false })
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = canSave,
                    onClick = {
                        onSave(
                            InstitutionEntity(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                wilaya = wilayaName,
                                schoolName = schoolName.trim(),
                                schoolListId = if (manualEntry) null else schoolListId,
                                stage = stage,
                                schoolYear = schoolYear,
                                createdAt = existing?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    }
                ) { Text(stringResource(R.string.save)) }
                if (onCancel != null) {
                    OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
                }
            }
        }
    }

    if (showSchoolPicker && wilaya != null) {
        SchoolPickerDialog(
            wilaya = wilaya,
            onPick = { s -> schoolName = s.name; schoolListId = s.id; showSchoolPicker = false },
            onManual = { manualEntry = true; schoolName = ""; schoolListId = null; showSchoolPicker = false },
            onDismiss = { showSchoolPicker = false }
        )
    }
}

@Composable
private fun SchoolPickerDialog(
    wilaya: Wilaya,
    onPick: (com.elyedali.daftarustadh.data.ListedSchool) -> Unit,
    onManual: () -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val q = SchoolDirectory.normalize(query)
    val filtered = remember(q, wilaya) {
        if (q.isEmpty()) wilaya.schools
        else wilaya.schools.filter { SchoolDirectory.normalize(it.name).contains(q) }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.search_school)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = onManual) { Text(stringResource(R.string.school_not_found)) }
                HorizontalDivider()
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    items(filtered, key = { it.id }) { s ->
                        Text(
                            s.name,
                            Modifier.fillMaxWidth().clickable { onPick(s) }.padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
