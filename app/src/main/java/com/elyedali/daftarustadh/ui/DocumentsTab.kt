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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.DocType
import com.elyedali.daftarustadh.data.DocumentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun docTypeLabel(t: String): String = stringResource(
    when (t) { DocType.EXAM -> R.string.doc_exam; DocType.REQUEST -> R.string.doc_request; DocType.LESSON -> R.string.doc_lesson; else -> R.string.doc_other }
)

/** «تحرير»: وثائق الأستاذ في القسم — يكتب امتحانًا أو طلبًا أو درسًا ويسحبه PDF بالرأسية وطابعه. */
@Composable
fun DocumentsTab(
    classId: String,
    documents: List<DocumentEntity>,
    onSave: (DocumentEntity) -> Unit,
    onDelete: (DocumentEntity) -> Unit,
    onExport: (DocumentEntity) -> Unit
) {
    var editing by remember { mutableStateOf<DocumentEntity?>(null) }
    var toDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    val current = editing
    if (current != null) {
        DocumentEditor(current, onSave = { onSave(it); editing = null }, onExport = { onSave(it); onExport(it) }, onClose = { editing = null })
        return
    }
    Column(Modifier.fillMaxSize().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionCard(Icons.Filled.Create, stringResource(R.string.doc_new), stringResource(R.string.doc_new_desc),
            tint = MaterialTheme.colorScheme.secondary) {
            editing = DocumentEntity(UUID.randomUUID().toString(), classId, DocType.EXAM, "", "", System.currentTimeMillis())
        }
        if (documents.isEmpty()) Text(stringResource(R.string.doc_none), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(documents, key = { it.id }) { d ->
                Card(Modifier.fillMaxWidth().clickable { editing = d }) {
                    Row(Modifier.padding(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(d.title.ifBlank { docTypeLabel(d.type) }, style = MaterialTheme.typography.titleMedium)
                            Text("${docTypeLabel(d.type)} — ${fmt.format(Date(d.updatedAt))}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onExport(d) }) { Text(stringResource(R.string.doc_pdf)) }
                        TextButton(onClick = { toDelete = d }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    toDelete?.let { d ->
        SensitiveConfirmDialog(stringResource(R.string.doc_delete), d.title, onConfirm = { onDelete(d); toDelete = null }, onDismiss = { toDelete = null })
    }
}

@Composable
private fun DocumentEditor(
    doc: DocumentEntity,
    onSave: (DocumentEntity) -> Unit,
    onExport: (DocumentEntity) -> Unit,
    onClose: () -> Unit
) {
    var type by remember { mutableStateOf(doc.type) }
    var title by remember { mutableStateOf(doc.title) }
    var body by remember { mutableStateOf(doc.body) }
    fun current() = doc.copy(type = type, title = title.trim(), body = body, updatedAt = System.currentTimeMillis())
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DocType.ALL.forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(docTypeLabel(t)) }) }
        }
        OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.doc_title)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(body, { body = it }, label = { Text(stringResource(R.string.doc_body)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSave(current()) }) { Text(stringResource(R.string.save)) }
            OutlinedButton(enabled = body.isNotBlank() || title.isNotBlank(), onClick = { onExport(current()) }) { Text(stringResource(R.string.doc_pdf)) }
            TextButton(onClick = onClose) { Text(stringResource(R.string.cancel)) }
        }
    }
}
