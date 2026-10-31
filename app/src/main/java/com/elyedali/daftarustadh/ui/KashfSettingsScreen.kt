package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.GradeMath
import com.elyedali.daftarustadh.report.KashfSettings

/** إعدادات الكشوف عند الإدارة: معدل النجاح لكل مستوى (وشعبة)، والملاحظة العامة. */
@Composable
fun KashfSettingsScreen(
    levels: List<String>,
    thresholds: Map<String, Double>,
    onSave: (Map<String, Double>) -> Unit,
    onEditGeneralRemarks: () -> Unit,
    onBack: () -> Unit
) {
    val texts = remember(thresholds, levels) {
        mutableStateMapOf<String, String>().apply {
            levels.forEach { put(it, GradeMath.format(thresholds[it] ?: KashfSettings.DEFAULT_THRESHOLD)) }
        }
    }
    fun valid(t: String?) = GradeMath.parse(t ?: "")?.let { it in KashfSettings.MIN_THRESHOLD..KashfSettings.MAX_THRESHOLD } == true
    val allValid = levels.all { valid(texts[it]) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                Text(stringResource(R.string.kashf_settings), style = MaterialTheme.typography.titleLarge)
            }
            OutlinedButton(onClick = onEditGeneralRemarks, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.general_remarks))
            }
            Text(stringResource(R.string.pass_thresholds), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.pass_thresholds_help), style = MaterialTheme.typography.bodySmall)
            Button(enabled = allValid, onClick = {
                onSave(levels.associateWith { GradeMath.parse(texts[it]!!)!! })
            }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(levels, key = { it }) { code ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(code, style = MaterialTheme.typography.titleMedium)
                            Text(levelDescription(code), style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedTextField(
                            value = texts[code] ?: "",
                            onValueChange = { texts[code] = it.take(5) },
                            singleLine = true,
                            isError = !valid(texts[code]),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }
            }
        }
    }
}
