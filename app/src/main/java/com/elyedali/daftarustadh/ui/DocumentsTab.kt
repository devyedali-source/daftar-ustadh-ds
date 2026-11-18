package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val dateFmt get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)

/**
 * «تحريراتي» لمستوى (في البرنامج السنوي): يكتب الأستاذ امتحانًا أو طلبًا أو درسًا، ويعرضه ويعدّله ويحذفه
 * ويسحبه PDF بطابعه. الطابع (اسمه، مادته، مؤسسته…) يكتبه مرة ويعدّله متى شاء، ويظهر أسفل كل تحرير.
 */
@Composable
fun LevelDocuments(
    level: String,
    levelSubjects: List<Pair<String, String>>,   // (الرمز، الاسم) لمادة الامتحان
    documents: List<DocumentEntity>,
    stamp: String,
    onSaveStamp: (String) -> Unit,
    onSave: (DocumentEntity) -> Unit,
    onDelete: (DocumentEntity) -> Unit,
    onExport: (DocumentEntity) -> Unit
) {
    var viewingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<DocumentEntity?>(null) }
    var toDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var editStamp by remember { mutableStateOf(false) }
    // بلا طابع لا يُسحب تحرير: يُطلب الطابع أولًا ثم يُسحب
    var exportAfterStamp by remember { mutableStateOf<DocumentEntity?>(null) }
    fun export(d: DocumentEntity) { if (stamp.isBlank()) { exportAfterStamp = d; editStamp = true } else onExport(d) }
    fun subjectName(code: String) = levelSubjects.firstOrNull { it.first == code }?.second

    if (editStamp) StampDialog(stamp, onSave = { s ->
        onSaveStamp(s); editStamp = false
        exportAfterStamp?.let { d -> if (s.isNotBlank()) onExport(d) }; exportAfterStamp = null
    }, onDismiss = { editStamp = false; exportAfterStamp = null })
    toDelete?.let { d ->
        SensitiveConfirmDialog(stringResource(R.string.doc_delete), d.title.ifBlank { docTypeLabel(d.type) },
            onConfirm = { onDelete(d); toDelete = null; if (viewingId == d.id) viewingId = null }, onDismiss = { toDelete = null })
    }

    editing?.let { cur ->
        DocumentEditor(cur, levelSubjects, stamp, onSave = { onSave(it); viewingId = it.id; editing = null },
            onExport = { onSave(it); export(it) }, onEditStamp = { editStamp = true }, onClose = { editing = null })
        return
    }
    documents.firstOrNull { it.id == viewingId }?.let { d ->
        DocumentView(d, subjectName(d.subjectCode), stamp,
            onEdit = { editing = d }, onExport = { export(d) }, onDelete = { toDelete = d },
            onEditStamp = { editStamp = true }, onClose = { viewingId = null })
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("✍️ " + stringResource(R.string.doc_my_title, level), fontWeight = FontWeight.Black, fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary)
        }
        item {
            ActionCard(Icons.Filled.Create, stringResource(R.string.doc_new), stringResource(R.string.doc_new_desc),
                tint = MaterialTheme.colorScheme.secondary) {
                editing = DocumentEntity(UUID.randomUUID().toString(), "", DocType.EXAM, "", "", System.currentTimeMillis(), levelCode = level)
            }
        }
        if (documents.isEmpty()) item { Text(stringResource(R.string.doc_none), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(documents, key = { it.id }) { d ->
            Card(Modifier.fillMaxWidth().clickable { viewingId = d.id }) {
                Row(Modifier.padding(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(d.title.ifBlank { docTypeLabel(d.type) }, style = MaterialTheme.typography.titleMedium)
                        Text(listOfNotNull(docTypeLabel(d.type), subjectName(d.subjectCode), dateFmt.format(Date(d.updatedAt))).joinToString(" — "),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { export(d) }) { Text(stringResource(R.string.doc_pdf)) }
                }
            }
        }
        item { StampCard(stamp) { editStamp = true } }
    }
}

/** الطابع كما يظهر أسفل التحريرات وفي الـPDF. */
@Composable
private fun StampCard(stamp: String, onEdit: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("🔖 " + stringResource(R.string.doc_stamp_title), fontWeight = FontWeight.Bold)
        if (stamp.isBlank()) Text(stringResource(R.string.doc_stamp_empty), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else StampBox(stamp)
        OutlinedButton(onClick = onEdit) { Text(stringResource(if (stamp.isBlank()) R.string.doc_stamp_write else R.string.doc_stamp_edit)) }
    }
}

@Composable
private fun StampBox(stamp: String) {
    val ink = androidx.compose.ui.graphics.Color(0xFF1F3A93)
    Column(Modifier.border(1.5.dp, ink, RoundedCornerShape(8.dp)).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        stamp.lines().filter { it.isNotBlank() }.forEach { Text(it.trim(), color = ink, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
    }
}

@Composable
private fun StampDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("🔖 " + stringResource(R.string.doc_stamp_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.doc_stamp_hint), fontSize = 13.sp)
                OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                    placeholder = { Text(stringResource(R.string.doc_stamp_placeholder)) })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

/** عرض تحرير: العنوان والنص والتاريخ والطابع، مع التعديل والسحب والحذف. */
@Composable
private fun DocumentView(d: DocumentEntity, subject: String?, stamp: String, onEdit: () -> Unit, onExport: () -> Unit,
                         onDelete: () -> Unit, onEditStamp: () -> Unit, onClose: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onClose)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = onClose, contentPadding = PaddingValues(0.dp)) { Text("→ " + stringResource(R.string.doc_back_list)) }
        Text(listOfNotNull(docTypeLabel(d.type), subject).joinToString(" — ") + "\n" +
            stringResource(R.string.doc_updated, dateFmt.format(Date(d.updatedAt))),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (d.title.isNotBlank()) Text(d.title, fontWeight = FontWeight.Black, fontSize = 17.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
                Text(d.body, fontSize = 14.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.doc_signature), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (stamp.isBlank()) TextButton(onClick = onEditStamp, contentPadding = PaddingValues(0.dp)) { Text("🔖 " + stringResource(R.string.doc_stamp_write)) }
                else StampBox(stamp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onEdit) { Text("✏️ " + stringResource(R.string.edit)) }
            OutlinedButton(onClick = onExport) { Text("📄 " + stringResource(R.string.doc_pdf)) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentEditor(
    doc: DocumentEntity,
    levelSubjects: List<Pair<String, String>>,
    stamp: String,
    onSave: (DocumentEntity) -> Unit,
    onExport: (DocumentEntity) -> Unit,
    onEditStamp: () -> Unit,
    onClose: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onClose)
    var type by remember { mutableStateOf(doc.type) }
    var subject by remember { mutableStateOf(doc.subjectCode) }
    var title by remember { mutableStateOf(doc.title) }
    var body by remember { mutableStateOf(doc.body) }
    fun current() = doc.copy(type = type, title = title.trim(), body = body, updatedAt = System.currentTimeMillis(),
        subjectCode = if (type == DocType.EXAM) subject else "")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DocType.ALL.forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(docTypeLabel(t)) }) }
        }
        if (type == DocType.EXAM && levelSubjects.isNotEmpty()) {
            Text(stringResource(R.string.doc_exam_subject), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                levelSubjects.forEach { (code, name) ->
                    FilterChip(selected = subject == code, onClick = { subject = if (subject == code) "" else code }, label = { Text(name) })
                }
            }
        }
        OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.doc_title)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(body, { body = it }, label = { Text(stringResource(R.string.doc_body)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp))
        StampCard(stamp, onEditStamp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSave(current()) }, enabled = body.isNotBlank() || title.isNotBlank()) { Text(stringResource(R.string.save)) }
            OutlinedButton(enabled = body.isNotBlank() || title.isNotBlank(), onClick = { onExport(current()) }) { Text(stringResource(R.string.doc_pdf)) }
            TextButton(onClick = onClose) { Text(stringResource(R.string.cancel)) }
        }
    }
}
