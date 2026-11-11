package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.Institutions

/**
 * «مؤسساتي» (تطبيق الإدارة): مؤسسات الحساب، لكل واحدة أقسامها وتلاميذها وأساتذتها وجدولها واشتراكها،
 * والانتقال بينها، أو إضافة مؤسسة جديدة.
 */
@Composable
fun InstitutionsDialog(
    entries: List<Institutions.Entry>,
    current: String,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmNew by remember { mutableStateOf(false) }
    if (confirmNew) {
        AlertDialog(
            onDismissRequest = { confirmNew = false },
            title = { Text(stringResource(R.string.inst_new)) },
            text = { Text(stringResource(R.string.inst_new_hint)) },
            confirmButton = { TextButton(onClick = { confirmNew = false; onCreate() }) { Text(stringResource(R.string.inst_create)) } },
            dismissButton = { TextButton(onClick = { confirmNew = false }) { Text(stringResource(R.string.cancel)) } }
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏢 " + stringResource(R.string.inst_my)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.inst_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(entries, key = { it.key }) { e ->
                        val isCur = e.key == current
                        Card(
                            Modifier.fillMaxWidth().clickable { onOpen(e.key) },
                            border = if (isCur) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            colors = CardDefaults.cardColors(containerColor = if (isCur) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text((if (isCur) "✅ " else "🏫 ") + e.name.ifBlank { stringResource(R.string.inst_unnamed) },
                                    fontWeight = FontWeight.Bold)
                                if (e.wilaya.isNotBlank()) Text(e.wilaya, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (isCur) Text(stringResource(R.string.inst_current), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                OutlinedButton(onClick = { confirmNew = true }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("➕ " + stringResource(R.string.inst_new))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
