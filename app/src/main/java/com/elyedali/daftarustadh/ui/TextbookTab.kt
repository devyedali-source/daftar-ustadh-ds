package com.elyedali.daftarustadh.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.SignatureCodec
import com.elyedali.daftarustadh.data.TextbookEntryEntity
import java.util.Calendar
import java.util.UUID

private val InkBlue = Color(0xFF1F3A93)

private fun tbDisplayDate(iso: String): String {
    val p = iso.split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
}

private fun tbIsoDate(year: Int, month0: Int, day: Int) = "%04d-%02d-%02d".format(java.util.Locale.ROOT, year, month0 + 1, day)

private fun tbToday(): String = Calendar.getInstance().let {
    tbIsoDate(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH))
}

private fun minutesOf(hhmm: String): Int? {
    val p = hhmm.split(":")
    val h = p.getOrNull(0)?.toIntOrNull() ?: return null
    val m = p.getOrNull(1)?.toIntOrNull() ?: return null
    return h * 60 + m
}

private fun hhmm(minutes: Int) = "%02d:%02d".format(java.util.Locale.ROOT, minutes / 60, minutes % 60)

@Composable
private fun durationLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> stringResource(R.string.tb_duration_hm, h, m)
        h > 0 -> stringResource(R.string.tb_duration_h, h)
        else -> stringResource(R.string.tb_duration_m, m)
    }
}

/**
 * دفتر النصوص — للإدارة فقط، داخل القسم.
 * كل سطر: التاريخ، التوقيت، المادة، المجال، الموضوع، المدة، وتوقيع الأستاذ بالإصبع.
 */
@Composable
fun TextbookTab(
    classId: String,
    subjects: List<AbsenceSubject>,
    entries: List<TextbookEntryEntity>,
    onSave: (TextbookEntryEntity) -> Unit,
    onDelete: (TextbookEntryEntity) -> Unit
) {
    val labels = remember(subjects) { subjects.associate { it.code to it.label } }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }

    val editing = entries.firstOrNull { it.id == editingId }
    if (creating || editing != null) {
        BackHandler { creating = false; editingId = null }
        TextbookEditor(
            classId = classId,
            subjects = subjects,
            existing = editing,
            defaultSubject = filter ?: subjects.firstOrNull()?.code.orEmpty(),
            onSave = { onSave(it); creating = false; editingId = null },
            onDelete = { onDelete(it); creating = false; editingId = null },
            onCancel = { creating = false; editingId = null }
        )
        return
    }

    val shown = entries.filter { filter == null || it.subjectCode == filter }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(selected = filter == null, onClick = { filter = null },
                        label = { Text(stringResource(R.string.abs_all_subjects)) })
                }
                items(subjects, key = { it.code }) { s ->
                    FilterChip(selected = filter == s.code, onClick = { filter = s.code }, label = { Text(s.label) })
                }
            }
        }
        item {
            Button(onClick = { creating = true }, enabled = subjects.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("➕ " + stringResource(R.string.tb_new), fontWeight = FontWeight.Bold)
            }
        }
        item {
            Text(stringResource(R.string.tb_count, shown.size), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        if (shown.isEmpty()) item {
            Text(stringResource(R.string.tb_empty), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        }
        items(shown, key = { it.id }) { e ->
            Card(
                Modifier.fillMaxWidth().clickable { editingId = e.id },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("📅 ⁦${tbDisplayDate(e.date)}⁩", fontWeight = FontWeight.Bold)
                        Text("🕘 ⁦${e.startTime} - ${e.endTime}⁩", color = MaterialTheme.colorScheme.secondary)
                        Text(durationLabel(e.durationMinutes), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(labels[e.subjectCode] ?: e.subjectCode, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (e.domain.isNotBlank()) Text(stringResource(R.string.tb_domain_line, e.domain), fontSize = 13.sp)
                    Text(stringResource(R.string.tb_topic_line, e.topic), fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.tb_signature) + ":", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (e.signature.isBlank()) Text(stringResource(R.string.tb_not_signed), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        else SignaturePreview(e.signature, Modifier.width(110.dp).height(44.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SignaturePreview(encoded: String, modifier: Modifier) {
    val strokes = remember(encoded) { SignatureCodec.decode(encoded) }
    Canvas(modifier) {
        strokes.forEach { s ->
            val path = Path()
            s.forEachIndexed { i, (x, y) ->
                if (i == 0) path.moveTo(x * size.width, y * size.height) else path.lineTo(x * size.width, y * size.height)
            }
            drawPath(path, InkBlue, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

@Composable
private fun TextbookEditor(
    classId: String,
    subjects: List<AbsenceSubject>,
    existing: TextbookEntryEntity?,
    defaultSubject: String,
    onSave: (TextbookEntryEntity) -> Unit,
    onDelete: (TextbookEntryEntity) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var subject by rememberSaveable { mutableStateOf(existing?.subjectCode ?: defaultSubject) }
    var date by rememberSaveable { mutableStateOf(existing?.date ?: tbToday()) }
    var start by rememberSaveable { mutableStateOf(existing?.startTime ?: "08:00") }
    var end by rememberSaveable { mutableStateOf(existing?.endTime ?: "09:00") }
    var domain by rememberSaveable { mutableStateOf(existing?.domain ?: "") }
    var topic by rememberSaveable { mutableStateOf(existing?.topic ?: "") }
    val strokes = remember { mutableStateListOf<List<Pair<Float, Float>>>().apply { addAll(SignatureCodec.decode(existing?.signature.orEmpty())) } }
    var current by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var confirmDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    val duration = run {
        val a = minutesOf(start); val b = minutesOf(end)
        if (a != null && b != null && b > a) b - a else null
    }

    fun pickTime(value: String, set: (String) -> Unit) {
        val m = minutesOf(value) ?: 480
        TimePickerDialog(context, { _, h, mm -> set(hhmm(h * 60 + mm)) }, m / 60, m % 60, true).show()
    }

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)) {
        item {
            Text(stringResource(if (existing == null) R.string.tb_new else R.string.tb_edit),
                fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
        }
        item {
            OutlinedButton(
                onClick = {
                    val p = date.split("-").mapNotNull { it.toIntOrNull() }
                    val cal = Calendar.getInstance()
                    val (y, mo, d) = if (p.size == 3) Triple(p[0], p[1] - 1, p[2])
                        else Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    DatePickerDialog(context, { _, yy, mm, dd -> date = tbIsoDate(yy, mm, dd) }, y, mo, d).show()
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            ) { Text("📅 " + stringResource(R.string.abs_date) + ": ⁦${tbDisplayDate(date)}⁩") }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.tb_time), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { pickTime(start) { start = it } }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.tb_from) + " ⁦$start⁩")
                    }
                    OutlinedButton(onClick = { pickTime(end) { end = it } }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.tb_to) + " ⁦$end⁩")
                    }
                }
                Text(
                    stringResource(R.string.tb_duration) + ": " + (duration?.let { durationLabel(it) } ?: stringResource(R.string.tb_bad_time)),
                    fontWeight = FontWeight.Bold,
                    color = if (duration == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.abs_subject), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects, key = { it.code }) { s ->
                        FilterChip(selected = subject == s.code, onClick = { subject = s.code }, label = { Text(s.label) })
                    }
                }
            }
        }
        item {
            OutlinedTextField(domain, { domain = it }, label = { Text(stringResource(R.string.tb_domain)) },
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(topic, { topic = it }, label = { Text(stringResource(R.string.tb_topic)) },
                minLines = 2, modifier = Modifier.fillMaxWidth())
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.tb_signature_teacher), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { strokes.clear(); current = emptyList() }) { Text(stringResource(R.string.tb_clear)) }
                }
                Canvas(
                    Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            fun norm(o: Offset) = (o.x / size.width).coerceIn(0f, 1f) to (o.y / size.height).coerceIn(0f, 1f)
                            // نستهلك اللمسة من أولها حتى لا تتحرك الصفحة أثناء التوقيع
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                down.consume()
                                current = listOf(norm(down.position))
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    change.consume()
                                    current = current + norm(change.position)
                                }
                                if (current.size > 1) strokes.add(current)
                                current = emptyList()
                            }
                        }
                ) {
                    (strokes + listOf(current)).forEach { s ->
                        if (s.size > 1) {
                            val path = Path()
                            s.forEachIndexed { i, (x, y) ->
                                if (i == 0) path.moveTo(x * size.width, y * size.height) else path.lineTo(x * size.width, y * size.height)
                            }
                            drawPath(path, InkBlue, style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
                Text(stringResource(R.string.tb_signature_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start)
            }
        }
        error?.let { item { Text(stringResource(it), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } }
        item {
            Button(
                onClick = {
                    error = when {
                        subject.isBlank() -> R.string.tb_err_subject
                        duration == null -> R.string.tb_bad_time
                        topic.isBlank() -> R.string.tb_err_topic
                        else -> null
                    }
                    if (error == null && duration != null) onSave(
                        TextbookEntryEntity(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            classId = classId, date = date, startTime = start, endTime = end,
                            subjectCode = subject, domain = domain.trim(), topic = topic.trim(),
                            durationMinutes = duration,
                            signature = SignatureCodec.encode(strokes.toList()),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
            ) { Text("💾 " + stringResource(R.string.save), fontWeight = FontWeight.Bold) }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                if (existing != null) Button(
                    onClick = { confirmDelete = true }, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) { Text(stringResource(R.string.tb_delete), color = Color.White) }
            }
        }
    }
    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            text = { Text(stringResource(R.string.tb_delete_confirm)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(existing) }) { Text(stringResource(R.string.tb_delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
