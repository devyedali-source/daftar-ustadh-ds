package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R

/** رمز تأكيد كل حذف حساس في التطبيق (قسم، طالب، نتائج...). */
const val SENSITIVE_DELETE_CODE = "9999"

/** نافذة تأكيد الحذف الحساس: لا يُفعَّل زر الحذف إلا بعد كتابة 9999. */
@Composable
fun SensitiveConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(message)
                Text(stringResource(R.string.type_code_to_delete, SENSITIVE_DELETE_CODE), color = MaterialTheme.colorScheme.error)
                OutlinedTextField(
                    value = code,
                    onValueChange = { v -> code = v.filter { it.isDigit() }.take(4) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = code == SENSITIVE_DELETE_CODE, onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

/** رمز فتح إعدادات الجدول الزمني بعد اعتماده. */
const val TIMETABLE_EDIT_CODE = "7777"

/** نافذة تنبيه بالرمز: لا يُفعَّل زر المتابعة إلا بعد كتابة الرمز. */
@Composable
fun CodeConfirmDialog(
    title: String,
    message: String,
    requiredCode: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(message)
                Text(stringResource(R.string.type_code_to_continue, requiredCode), color = MaterialTheme.colorScheme.error)
                OutlinedTextField(
                    value = code,
                    onValueChange = { v -> code = v.filter { it.isDigit() }.take(requiredCode.length) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = code == requiredCode, onClick = onConfirm) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
