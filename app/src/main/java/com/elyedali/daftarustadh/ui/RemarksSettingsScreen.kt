package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.GradeMath
import com.elyedali.daftarustadh.data.RemarkLevel
import com.elyedali.daftarustadh.data.RemarkScale

private class EditableLevel(min: String, ar: String, fr: String) {
    var min = androidx.compose.runtime.mutableStateOf(min)
    var ar = androidx.compose.runtime.mutableStateOf(ar)
    var fr = androidx.compose.runtime.mutableStateOf(fr)
}

/** إعدادات سجل الأستاذ: ملاحظات المادة حسب معدلها. */
@Composable
fun RemarksSettingsScreen(
    scale: List<RemarkLevel>,
    /** null = لا تُعرض خانة اسم الأستاذ (ملاحظات الإدارة العامة). */
    teacherName: String?,
    onSave: (List<RemarkLevel>, String) -> Unit,
    title: String? = null,
    defaults: List<RemarkLevel> = RemarkScale.DEFAULT,
    onBack: () -> Unit
) {
    val rows = remember(scale) {
        mutableStateListOf<EditableLevel>().apply {
            scale.sortedBy { it.min }.forEach { add(EditableLevel(GradeMath.format(it.min), it.textAr, it.textFr)) }
        }
    }
    val name = remember(teacherName) { androidx.compose.runtime.mutableStateOf(teacherName.orEmpty()) }
    val parsed = rows.map { r -> GradeMath.parse(r.min.value) }
    val valid = rows.isNotEmpty() && parsed.all { it != null } && parsed.contains(0.0) &&
        parsed.toSet().size == parsed.size && rows.all { it.ar.value.isNotBlank() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                Text(title ?: stringResource(R.string.remarks_settings), style = MaterialTheme.typography.titleLarge)
            }
            if (teacherName != null) OutlinedTextField(
                name.value, { name.value = it },
                label = { Text(stringResource(R.string.teacher_name)) },
                supportingText = { Text(stringResource(R.string.teacher_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.remarks_help), style = MaterialTheme.typography.bodySmall)
            if (!valid) Text(stringResource(R.string.remarks_invalid), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = valid, onClick = {
                    onSave(rows.map { RemarkLevel(GradeMath.parse(it.min.value)!!, it.ar.value.trim(), it.fr.value.trim().ifBlank { it.ar.value.trim() }) }, name.value.trim())
                }) { Text(stringResource(R.string.save)) }
                OutlinedButton(onClick = { rows.add(EditableLevel("", "", "")) }) { Text("+  " + stringResource(R.string.add_level)) }
                TextButton(onClick = {
                    rows.clear(); defaults.forEach { rows.add(EditableLevel(GradeMath.format(it.min), it.textAr, it.textFr)) }
                }) { Text(stringResource(R.string.restore_default)) }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(rows) { i, r ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                r.min.value, { r.min.value = it.take(5) },
                                label = { Text(stringResource(R.string.from_average)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(90.dp)
                            )
                            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                                OutlinedTextField(r.ar.value, { r.ar.value = it }, label = { Text(stringResource(R.string.remark_ar)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(r.fr.value, { r.fr.value = it }, label = { Text(stringResource(R.string.remark_fr)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            }
                            TextButton(onClick = { rows.removeAt(i) }) { Text("✕") }
                        }
                    }
                }
            }
        }
    }
}
