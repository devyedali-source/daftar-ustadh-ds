package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R

/** ما تختاره الإدارة لإرساله إلى الوراقة. */
data class PrintChoice(val records: Boolean, val timetables: Boolean, val absence: Boolean, val textbook: Boolean, val textbookPages: Int,
                       val cards: Boolean = false)

/** وثيقة في صفحة الوراقة: هل هي جاهزة، وما الذي ينقصها. */
data class PrintItem(val ready: Boolean, val note: String)

/**
 * مشاركة الوثائق مع الوراقة لسحبها (الإدارة): ملفات PDF جاهزة للطباعة،
 * ملف لكل وثيقة يجمع كل الأقسام أو كل الأساتذة.
 */
@Composable
fun PrintShopScreen(
    title: String,
    intro: String,
    records: PrintItem?,
    timetables: PrintItem?,
    absence: PrintItem?,
    textbook: PrintItem?,
    working: Boolean,
    onShare: (PrintChoice) -> Unit,
    onOpenTimetable: () -> Unit,
    onBack: () -> Unit
) {
    // كل صفحة تعرض وثائقها فقط: دفاتر الإدارة (الغياب والنصوص)، أو سجلات الأساتذة، أو جداولهم
    var rec by rememberSaveable { mutableStateOf(records?.ready == true) }
    var tt by rememberSaveable { mutableStateOf(timetables?.ready == true) }
    var abs by rememberSaveable { mutableStateOf(absence?.ready == true) }
    var tb by rememberSaveable { mutableStateOf(textbook?.ready == true) }
    var pages by rememberSaveable { mutableIntStateOf(20) }
    val recOn = rec && records?.ready == true
    val ttOn = tt && timetables?.ready == true
    val absOn = abs && absence?.ready == true
    val tbOn = tb && textbook?.ready == true
    val any = recOn || ttOn || absOn || tbOn

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = title, onMenu = null, onBack = onBack)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(intro, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                records?.let { Option("📒", stringResource(R.string.ps_records), it, rec) { v -> rec = v } }
                timetables?.let { Option("📅", stringResource(R.string.ps_timetables), it, tt) { v -> tt = v } }
                if ((records != null && !records.ready) || (timetables != null && !timetables.ready)) OutlinedButton(onClick = onOpenTimetable, modifier = Modifier.fillMaxWidth()) {
                    Text("📅 " + stringResource(R.string.sr_open_timetable))
                }
                absence?.let { Option("🗓️", stringResource(R.string.ps_absence), it, abs) { v -> abs = v } }
                textbook?.let { Option("📖", stringResource(R.string.ps_textbook), it, tb) { v -> tb = v } }
                if (textbook != null && tbOn) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(stringResource(R.string.ps_textbook_pages), fontSize = 13.sp, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { if (pages > 5) pages -= 5 }, enabled = pages > 5) { Text("−") }
                    Text("$pages", fontWeight = FontWeight.Black, fontSize = 17.sp)
                    OutlinedButton(onClick = { if (pages < 200) pages += 5 }, enabled = pages < 200) { Text("+") }
                }
                Button(
                    onClick = { onShare(PrintChoice(recOn, ttOn, absOn, tbOn, pages)) },
                    enabled = any && !working,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    if (working) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Text("  " + stringResource(R.string.ps_preparing))
                    } else Text("🖨️ " + stringResource(R.string.ps_share), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Option(icon: String, title: String, item: PrintItem, checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(enabled = item.ready) { onChange(!checked) }, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked && item.ready, onCheckedChange = { onChange(it) }, enabled = item.ready)
            Column(Modifier.weight(1f)) {
                Text("$icon $title", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(item.note, fontSize = 12.sp,
                    color = if (item.ready) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
            }
        }
    }
}
