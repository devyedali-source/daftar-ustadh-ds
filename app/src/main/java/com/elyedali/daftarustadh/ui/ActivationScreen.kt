package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassActivation
import com.elyedali.daftarustadh.data.ClassEntity

private val Green = Color(0xFF2E7D32)
private val Orange = Color(0xFFE65100)

/**
 * تفعيل الأقسام (الإدارة): الفصل الأول مجاني، والتفعيل يفتح الفصلين الثاني والثالث.
 * يختار المسؤول الأقسام فيُحسب السعر، ثم يرسل الطلب عبر واتساب الإدارة.
 */
@Composable
fun ActivationScreen(
    classes: List<ClassEntity>,
    activated: Set<String>,
    requested: Set<String>,
    onRequest: (List<ClassEntity>, Int) -> Unit,
    onBack: () -> Unit
) {
    val pending = classes.filter { it.id !in activated }
    val selected = remember(pending.map { it.id }) { mutableStateListOf<String>().apply { addAll(pending.map { it.id }) } }
    val total = ClassActivation.price(selected.size)

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ScreenHeader(stringResource(R.string.act_title), onBack)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp)) {
                item {
                    Card(
                        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        border = BorderStroke(1.dp, Color(0xFFFFB300))
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.act_explain), fontSize = 13.sp)
                            Text("🏷️ " + stringResource(R.string.act_prices_title), fontWeight = FontWeight.Bold, color = Orange)
                            Text(stringResource(R.string.act_price_1, ClassActivation.PRICE_1), fontSize = 13.sp)
                            Text(stringResource(R.string.act_price_3, ClassActivation.PRICE_3), fontSize = 13.sp)
                            Text(stringResource(R.string.act_price_5, ClassActivation.PRICE_5), fontSize = 13.sp)
                            Text(stringResource(R.string.act_price_7, ClassActivation.PRICE_7_PLUS), fontSize = 13.sp)
                            Text(stringResource(R.string.act_price_other), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (classes.isEmpty()) item {
                    Text(stringResource(R.string.act_no_classes), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(classes, key = { it.id }) { c ->
                    val isActive = c.id in activated
                    val isRequested = !isActive && c.id in requested
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable(enabled = !isActive) { if (c.id in selected) selected.remove(c.id) else selected.add(c.id) }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isActive || c.id in selected, enabled = !isActive,
                            onCheckedChange = { if (it) selected.add(c.id) else selected.remove(c.id) }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(c.name, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.act_code, ClassActivation.code(c.id)), fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val (res, color) = when {
                            isActive -> R.string.act_status_active to Green
                            isRequested -> R.string.act_status_requested to Orange
                            else -> R.string.act_status_inactive to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                            Text(stringResource(res), Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = color,
                                fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.act_total, selected.size, total), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Button(
                        onClick = { onRequest(classes.filter { it.id in selected }, total) },
                        enabled = selected.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) { Text("💬 " + stringResource(R.string.act_send), fontWeight = FontWeight.Bold, color = Color.White) }
                    Text(stringResource(R.string.act_after_send), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
