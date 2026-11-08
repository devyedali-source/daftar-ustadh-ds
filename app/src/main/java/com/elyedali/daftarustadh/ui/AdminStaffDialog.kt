package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.exchange.ExchangeAd
import org.json.JSONArray
import org.json.JSONObject

/** عضو في طاقم الإدارة: مدير الدروس، المدير العام، المراقب العام، أو عامل في السكرتيريا. */
data class StaffMember(val name: String, val role: String, val phone: String)

object AdminStaff {
    const val KEY = "admin_staff"
    val ROLES = listOf("director_studies", "director_general", "supervisor", "secretariat")

    fun roleLabel(role: String): Int = when (role) {
        "director_studies" -> R.string.staff_role_studies
        "director_general" -> R.string.staff_role_general
        "supervisor" -> R.string.staff_role_supervisor
        else -> R.string.staff_role_secretariat
    }

    fun decode(text: String?): List<StaffMember> = try {
        val a = JSONArray(text ?: "[]")
        (0 until a.length()).map { i -> a.getJSONObject(i).let { StaffMember(it.optString("name"), it.optString("role"), it.optString("phone")) } }
    } catch (e: Exception) { emptyList() }

    fun encode(list: List<StaffMember>): String =
        JSONArray(list.map { JSONObject().put("name", it.name).put("role", it.role).put("phone", it.phone) }).toString()
}

/**
 * نافذة «مشاركة ملف الإدارة الكامل»: قائمة طاقم الإدارة بأرقام واتساب، والإرسال المباشر لأي واحد منهم،
 * مع إضافة عضو أو تعديله أو حذفه، أو المشاركة بطريقة أخرى.
 */
@Composable
fun AdminStaffShareDialog(
    staff: List<StaffMember>,
    onSave: (List<StaffMember>) -> Unit,
    onSendTo: (StaffMember) -> Unit,
    onShareOther: () -> Unit,
    onDismiss: () -> Unit
) {
    var editing by remember { mutableStateOf<Int?>(null) }   // -1 = عضو جديد
    if (editing != null) {
        val idx = editing!!
        val old = staff.getOrNull(idx)
        var name by remember(idx) { mutableStateOf(old?.name.orEmpty()) }
        var role by remember(idx) { mutableStateOf(old?.role ?: AdminStaff.ROLES.first()) }
        var phone by remember(idx) { mutableStateOf(old?.phone?.let { ExchangeAd.localPhone(it) }.orEmpty()) }
        val normalized = ExchangeAd.normalizePhone(phone)
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(stringResource(if (old == null) R.string.staff_add else R.string.staff_edit)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.staff_name)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.staff_role), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    AdminStaff.ROLES.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            pair.forEach { r ->
                                FilterChip(selected = role == r, onClick = { role = r },
                                    label = { Text(stringResource(AdminStaff.roleLabel(r)), fontSize = 11.sp) })
                            }
                        }
                    }
                    OutlinedTextField(phone, { phone = it }, label = { Text(stringResource(R.string.staff_phone)) },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phone.isNotBlank() && normalized == null, modifier = Modifier.fillMaxWidth())
                    ContactPhoneButton { phone = it }
                    if (phone.isNotBlank() && normalized == null)
                        Text(stringResource(R.string.staff_phone_bad), color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank() && normalized != null, onClick = {
                    val m = StaffMember(name.trim(), role, normalized!!)
                    onSave(if (old == null) staff + m else staff.mapIndexed { i, s -> if (i == idx) m else s })
                    editing = null
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                Row {
                    if (old != null) TextButton(onClick = { onSave(staff.filterIndexed { i, _ -> i != idx }); editing = null }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) }
                }
            }
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.af_share)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.staff_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.heightIn(max = 340.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(staff) { i, m ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, fontWeight = FontWeight.Bold)
                                    Text(stringResource(AdminStaff.roleLabel(m.role)), fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("✏️ " + stringResource(R.string.staff_edit), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp).clickable { editing = i })
                                }
                                Button(onClick = { onSendTo(m) }, shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                                    Text(stringResource(R.string.staff_send), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = { editing = -1 }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("➕ " + stringResource(R.string.staff_add))
                }
                OutlinedButton(onClick = onShareOther, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("📤 " + stringResource(R.string.staff_share_other))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
