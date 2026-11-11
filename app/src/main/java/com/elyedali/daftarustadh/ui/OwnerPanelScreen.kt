package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.elyedali.daftarustadh.auth.DsSubscriptions
import com.elyedali.daftarustadh.auth.OwnerAdmin
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * لوحة التحكم (حساب صاحب التطبيق فقط)، كما في دفتر المعلم:
 * عدد مستخدمي التطبيقين، التفعيلات حسب السنة الدراسية، التبادلات المفعّلة وغير المفعّلة،
 * وقبول طلبات التفعيل أو رفضها وتفعيل منشورات التبادل.
 */
@Composable
fun OwnerPanelScreen(
    /** طلب وصل برابط التفعيل من واتساب: يُفتح مباشرة. */
    focusRequestId: String?,
    /** إعلان تبادل وصل برابط التفعيل: يُفتح في تبويب التبادلات. */
    focusExchangeId: String? = null,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(if (focusRequestId != null) 1 else if (focusExchangeId != null) 2 else 0) }
    var reload by remember { mutableIntStateOf(0) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            ScreenHeader("👑 " + stringResource(R.string.op_title), onBack)
            TabRow(selectedTabIndex = tab) {
                listOf(R.string.op_tab_stats, R.string.op_tab_requests, R.string.op_tab_exchanges).forEachIndexed { i, res ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(res), fontSize = 13.sp) })
                }
            }
            when (tab) {
                0 -> StatsTab(reload) { reload++ }
                1 -> RequestsTab(focusRequestId, reload) { reload++ }
                else -> ExchangesTab(focusExchangeId, reload) { reload++ }
            }
        }
    }
}

@Composable
private fun Loading() = Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable
private fun ErrorBox(onRetry: () -> Unit) = Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("⚠️ " + stringResource(R.string.op_error), color = MaterialTheme.colorScheme.error)
    OutlinedButton(onClick = onRetry) { Text("🔄 " + stringResource(R.string.op_refresh)) }
}

@Composable
private fun StatCard(icon: String, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)), border = BorderStroke(1.dp, color.copy(alpha = 0.35f))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(icon, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = color)
            Text(label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatsTab(reload: Int, onReload: () -> Unit) {
    var stats by remember { mutableStateOf<OwnerAdmin.Stats?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(reload) {
        failed = false; stats = null
        stats = try { OwnerAdmin.stats() } catch (e: Exception) { failed = true; null }
    }
    val s = stats
    when {
        failed -> ErrorBox(onReload)
        s == null -> Loading()
        else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("🏫", stringResource(R.string.op_admin_users), s.adminUsers.toString(), Color(0xFF1565C0), Modifier.weight(1f))
                    StatCard("👨‍🏫", stringResource(R.string.op_teacher_users), s.teacherUsers.toString(), Color(0xFF2E7D32), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("🌐", stringResource(R.string.op_ex_active), s.exchangesActive.toString(), Color(0xFF00897B), Modifier.weight(1f))
                    StatCard("⏳", stringResource(R.string.op_ex_inactive), s.exchangesInactive.toString(), Color(0xFFE65100), Modifier.weight(1f))
                    StatCard("📨", stringResource(R.string.op_pending), s.pending.toString(), Color(0xFF6A1B9A), Modifier.weight(1f))
                }
            }
            item { Text("📅 " + stringResource(R.string.op_by_year), fontWeight = FontWeight.Black, fontSize = 15.sp) }
            if (s.years.isEmpty()) item { Text(stringResource(R.string.op_none), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(s.years, key = { it.year }) { y ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(y.year.ifBlank { "—" }, fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Text("🏫 " + stringResource(R.string.op_year_admin, y.admin, y.classes), fontSize = 13.sp)
                        Text("👨‍🏫 " + stringResource(R.string.op_year_teacher, y.teacher), fontSize = 13.sp)
                        Text("💰 " + stringResource(R.string.op_year_amount, y.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { OutlinedButton(onClick = onReload, modifier = Modifier.fillMaxWidth()) { Text("🔄 " + stringResource(R.string.op_refresh)) } }
        }
    }
}

private fun date(ms: Long) = if (ms <= 0) "" else SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(ms))

@Composable
private fun RequestsTab(focusId: String?, reload: Int, onReload: () -> Unit) {
    val scope = rememberCoroutineScope()
    var status by rememberSaveable { mutableStateOf("pending") }
    var list by remember { mutableStateOf<List<OwnerAdmin.Request>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var confirm by remember { mutableStateOf<Pair<OwnerAdmin.Request, Boolean>?>(null) }
    var busy by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf<OwnerAdmin.Request?>(null) }
    LaunchedEffect(focusId) { if (focusId != null) focused = OwnerAdmin.request(focusId) }
    LaunchedEffect(reload, status) {
        failed = false; list = null
        list = try { OwnerAdmin.requests(status) } catch (e: Exception) { failed = true; null }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("pending" to R.string.op_st_pending, "approved" to R.string.op_st_approved, "rejected" to R.string.op_st_rejected).forEach { (k, res) ->
                FilterChip(selected = status == k, onClick = { status = k }, label = { Text(stringResource(res), fontSize = 12.sp) })
            }
        }
        OutlinedTextField(query, { query = it.trim().uppercase() }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            label = { Text("🔎 " + stringResource(R.string.op_search)) })
        val l = list
        when {
            failed -> ErrorBox(onReload)
            l == null -> Loading()
            else -> {
                val shown = (listOfNotNull(focused?.takeIf { f -> f.status == status && l.none { it.id == f.id } }) + l)
                    .sortedByDescending { it.id == focused?.id }
                    .filter { query.isBlank() || it.code.contains(query) || it.email.uppercase().contains(query) || it.institutionName.uppercase().contains(query) }
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (shown.isEmpty()) item { Text(stringResource(R.string.op_none), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
                    items(shown, key = { it.id }) { r ->
                        val isAdmin = r.app == DsSubscriptions.APP_ADMIN
                        val highlight = r.id == focused?.id
                        Card(Modifier.fillMaxWidth(), border = if (highlight) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text((if (isAdmin) "🏫 " else "👨‍🏫 ") + stringResource(if (isAdmin) R.string.op_app_admin else R.string.op_app_teacher),
                                        fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                    Text("#${r.code}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("👤 ${r.name.ifBlank { "—" }} • ${r.email}", fontSize = 12.5.sp)
                                if (isAdmin) Text("🏛️ ${r.institutionName} — ${r.wilaya}", fontSize = 12.5.sp)
                                Text("📅 ${r.schoolYear}", fontSize = 12.5.sp)
                                if (r.classNames.isNotEmpty()) Text("📚 " + stringResource(R.string.op_classes, r.classNames.size) + ": " + r.classNames.joinToString("، "), fontSize = 12.sp)
                                Text("💰 " + stringResource(R.string.op_year_amount, r.amount) + "   🕒 " + date(r.createdAt), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (r.status == "pending") Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    Button(onClick = { confirm = r to true }, enabled = !busy, modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("✅ " + stringResource(R.string.op_approve)) }
                                    OutlinedButton(onClick = { confirm = r to false }, enabled = !busy, modifier = Modifier.weight(1f)) {
                                        Text("✖ " + stringResource(R.string.op_reject), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    confirm?.let { (r, ok) ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(stringResource(if (ok) R.string.op_approve_q else R.string.op_reject_q)) },
            text = { Text("#${r.code} • ${r.email}\n" + (if (r.institutionName.isNotBlank()) r.institutionName + "\n" else "") + r.schoolYear +
                (if (ok) "\n\n" + stringResource(R.string.op_expires, r.schoolYear.substringAfter('-').trim()) else "")) },
            confirmButton = {
                TextButton(onClick = {
                    confirm = null; busy = true
                    scope.launch {
                        try { if (ok) OwnerAdmin.approve(r) else OwnerAdmin.reject(r) } catch (e: Exception) { failed = true }
                        busy = false; focused = null; onReload()
                    }
                }) { Text(stringResource(if (ok) R.string.op_approve else R.string.op_reject)) }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ExchangesTab(focusId: String?, reload: Int, onReload: () -> Unit) {
    val scope = rememberCoroutineScope()
    var active by rememberSaveable { mutableStateOf(false) }
    var list by remember { mutableStateOf<List<OwnerAdmin.Exchange>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(reload, active) {
        failed = false; list = null
        list = try { OwnerAdmin.exchanges(active) } catch (e: Exception) { failed = true; null }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = !active, onClick = { active = false }, label = { Text("⏳ " + stringResource(R.string.op_ex_inactive), fontSize = 12.sp) })
            FilterChip(selected = active, onClick = { active = true }, label = { Text("🌐 " + stringResource(R.string.op_ex_active), fontSize = 12.sp) })
        }
        OutlinedTextField(query, { query = it.trim().uppercase() }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            label = { Text("🔎 " + stringResource(R.string.op_search)) })
        val l = list
        when {
            failed -> ErrorBox(onReload)
            l == null -> Loading()
            else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val shown = l.filter { query.isBlank() || it.code.contains(query) || it.email.uppercase().contains(query) }
                    .sortedByDescending { it.id == focusId }
                if (shown.isEmpty()) item { Text(stringResource(R.string.op_none), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
                items(shown, key = { it.id }) { e ->
                    Card(Modifier.fillMaxWidth(), border = if (e.id == focusId) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("#${e.code}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(e.email, fontSize = 12.sp)
                                Text("📍 ${e.wilaya} • ${e.specialties.joinToString(" ")}", fontSize = 12.sp)
                            }
                            Button(onClick = { scope.launch { try { OwnerAdmin.setExchangeActive(e.id, !e.active) } catch (x: Exception) { }; onReload() } },
                                colors = ButtonDefaults.buttonColors(containerColor = if (e.active) MaterialTheme.colorScheme.error else Color(0xFF2E7D32))) {
                                Text(stringResource(if (e.active) R.string.op_ex_stop else R.string.op_ex_activate), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
