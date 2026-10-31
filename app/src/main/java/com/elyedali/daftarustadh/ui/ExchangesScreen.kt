package com.elyedali.daftarustadh.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.exchange.ExchangeAd
import com.elyedali.daftarustadh.exchange.ExchangeRepository
import com.elyedali.daftarustadh.exchange.ExchangeRole
import kotlinx.coroutines.launch
import java.util.UUID

/** مادة يمكن اختيارها تخصصًا. */
data class SpecialtyOption(val code: String, val label: String)

@Composable
fun exchangeRoleLabel(role: String): String = stringResource(
    when (role) {
        ExchangeRole.STUDIES_DIRECTOR -> R.string.ex_role_director
        ExchangeRole.GENERAL_SUPERVISOR -> R.string.ex_role_supervisor
        else -> R.string.ex_role_teacher
    }
)

/**
 * تبادلات الأساتذة: تبويب «الإعلانات» (إعلانات الآخرين، مع زر تواصل يفتح واتساب دون إظهار الرقم)
 * وتبويب «إعلاني» (الولاية، المقاطعة، الصفة، التخصصات، مقاطعي أم لا، الولايات المطلوبة، رقم الواتساب).
 */
@Composable
fun ExchangesScreen(
    title: String,
    wilayas: List<String>,
    specialties: List<SpecialtyOption>,
    myAd: ExchangeAd?,
    ownerId: String,
    repository: ExchangeRepository,
    onSaveMine: suspend (ExchangeAd?) -> Unit,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(if (myAd == null) 1 else 0) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ScreenHeader(title, onBack)
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.ex_tab_ads)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.ex_tab_mine)) })
            }
            if (!repository.isConnected) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text(stringResource(R.string.ex_server_pending), Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            when (tab) {
                0 -> AdsTab(specialties, myAd, repository, onEditMine = { tab = 1 })
                else -> MyAdTab(wilayas, specialties, myAd, ownerId, repository, onSaveMine)
            }
        }
    }
}

@Composable
private fun AdsTab(
    specialties: List<SpecialtyOption>,
    myAd: ExchangeAd?,
    repository: ExchangeRepository,
    onEditMine: () -> Unit
) {
    val context = LocalContext.current
    var ads by remember { mutableStateOf<List<ExchangeAd>?>(null) }
    var onlyMatching by rememberSaveable { mutableStateOf(myAd != null) }
    LaunchedEffect(repository) { ads = runCatching { repository.list() }.getOrDefault(emptyList()) }
    val labels = remember(specialties) { specialties.associate { it.code to it.label } }
    val message = stringResource(R.string.ex_whatsapp_message)

    if (myAd != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = onlyMatching, onCheckedChange = { onlyMatching = it })
            Text(stringResource(R.string.ex_only_matching), style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        OutlinedButton(onClick = onEditMine, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.ex_create_first))
        }
    }
    val all = ads
    val shown = when {
        all == null -> null
        onlyMatching && myAd != null -> all.filter { it.matches(myAd) }
        else -> all.filter { it.ownerId != myAd?.ownerId }
    }
    if (shown != null && shown.isEmpty()) {
        Text(stringResource(R.string.ex_no_ads), color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp))
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(shown.orEmpty(), key = { it.id }) { ad ->
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(exchangeRoleLabel(ad.role), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary)
                    Text(listOf(ad.wilaya, ad.moughataa).filter { it.isNotBlank() }.joinToString(" — "),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (ad.specialties.isNotEmpty()) InfoLine(stringResource(R.string.ex_specialties),
                        ad.specialties.joinToString("، ") { labels[it] ?: it })
                    InfoLine(stringResource(R.string.ex_moughataa_level),
                        stringResource(if (ad.isMoughataaLevel) R.string.yes else R.string.no))
                    InfoLine(stringResource(R.string.ex_desired), ad.desiredWilayas.joinToString("، "))
                    Button(
                        onClick = {
                            val uri = Uri.parse("https://wa.me/${ad.whatsapp}?text=" + Uri.encode(message))
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (e: Exception) {
                                Toast.makeText(context, R.string.no_browser, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.ex_contact)) }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyMedium)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MyAdTab(
    wilayas: List<String>,
    specialties: List<SpecialtyOption>,
    myAd: ExchangeAd?,
    ownerId: String,
    repository: ExchangeRepository,
    onSaveMine: suspend (ExchangeAd?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var role by rememberSaveable { mutableStateOf(myAd?.role ?: ExchangeRole.TEACHER) }
    var wilaya by rememberSaveable { mutableStateOf(myAd?.wilaya ?: "") }
    var moughataa by rememberSaveable { mutableStateOf(myAd?.moughataa ?: "") }
    val chosen = remember { mutableStateListOf<String>().apply { addAll(myAd?.specialties.orEmpty()) } }
    var moughataaLevel by rememberSaveable { mutableStateOf(myAd?.isMoughataaLevel ?: false) }
    val desired = remember { mutableStateListOf<String>().apply { addAll(myAd?.desiredWilayas.orEmpty()) } }
    var phone by rememberSaveable { mutableStateOf(myAd?.whatsapp ?: "") }
    var pickWilaya by remember { mutableStateOf(false) }
    var pickDesired by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    val savedMsg = stringResource(if (repository.isConnected) R.string.ex_published else R.string.ex_saved_local)
    val deletedMsg = stringResource(R.string.ex_deleted)

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.ex_privacy_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionTitle(stringResource(R.string.ex_role))
        ExchangeRole.ALL.forEach { r ->
            Row(Modifier.fillMaxWidth().clickable { role = r }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = role == r, onClick = { role = r })
                Text(exchangeRoleLabel(r))
            }
        }

        SectionTitle(stringResource(R.string.ex_wilaya))
        OutlinedButton(onClick = { pickWilaya = true }, modifier = Modifier.fillMaxWidth()) {
            Text(wilaya.ifBlank { stringResource(R.string.ex_choose_wilaya) })
        }
        OutlinedTextField(
            value = moughataa, onValueChange = { moughataa = it },
            label = { Text(stringResource(R.string.ex_moughataa)) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = moughataaLevel, onCheckedChange = { moughataaLevel = it })
            Text(stringResource(R.string.ex_moughataa_level_question))
        }

        SectionTitle(stringResource(if (role == ExchangeRole.TEACHER) R.string.ex_specialties_required else R.string.ex_specialties_optional))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            specialties.forEach { s ->
                FilterChip(
                    selected = s.code in chosen,
                    onClick = { if (s.code in chosen) chosen.remove(s.code) else chosen.add(s.code) },
                    label = { Text(s.label) }
                )
            }
        }

        SectionTitle(stringResource(R.string.ex_desired))
        OutlinedButton(onClick = { pickDesired = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (desired.isEmpty()) stringResource(R.string.ex_choose_desired) else desired.joinToString("، "))
        }

        SectionTitle(stringResource(R.string.ex_whatsapp))
        OutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = { Text(stringResource(R.string.ex_whatsapp_hint)) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                val number = ExchangeAd.normalizePhone(phone)
                error = when {
                    wilaya.isBlank() -> R.string.ex_err_wilaya
                    moughataa.isBlank() -> R.string.ex_err_moughataa
                    role == ExchangeRole.TEACHER && chosen.isEmpty() -> R.string.ex_err_specialties
                    desired.isEmpty() -> R.string.ex_err_desired
                    number == null -> R.string.ex_err_phone
                    else -> null
                }
                if (error == null && number != null) {
                    val ad = ExchangeAd(
                        id = myAd?.id ?: UUID.randomUUID().toString(),
                        ownerId = ownerId,
                        role = role,
                        wilaya = wilaya,
                        moughataa = moughataa.trim(),
                        specialties = specialties.map { it.code }.filter { it in chosen },
                        isMoughataaLevel = moughataaLevel,
                        desiredWilayas = wilayas.filter { it in desired },
                        whatsapp = number,
                        updatedAt = System.currentTimeMillis()
                    )
                    scope.launch {
                        onSaveMine(ad)
                        runCatching { repository.publish(ad) }
                        Toast.makeText(context, savedMsg, Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(if (myAd == null) R.string.ex_publish else R.string.ex_update)) }

        if (myAd != null) {
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ex_delete), color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (pickWilaya) {
        AlertDialog(
            onDismissRequest = { pickWilaya = false },
            title = { Text(stringResource(R.string.ex_wilaya)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(wilayas) { w ->
                        Row(Modifier.fillMaxWidth().clickable { wilaya = w; pickWilaya = false },
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = wilaya == w, onClick = { wilaya = w; pickWilaya = false })
                            Text(w)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickWilaya = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (pickDesired) {
        AlertDialog(
            onDismissRequest = { pickDesired = false },
            title = { Text(stringResource(R.string.ex_desired)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(wilayas) { w ->
                        val toggle = { if (w in desired) desired.remove(w) else desired.add(w) }
                        Row(Modifier.fillMaxWidth().clickable { toggle() }, verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = w in desired, onCheckedChange = { toggle() })
                            Text(w)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickDesired = false }) { Text(stringResource(R.string.ok)) } }
        )
    }
    if (confirmDelete && myAd != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.ex_delete)) },
            text = { Text(stringResource(R.string.ex_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        runCatching { repository.delete(myAd.id) }
                        onSaveMine(null)
                        Toast.makeText(context, deletedMsg, Toast.LENGTH_SHORT).show()
                    }
                }) { Text(stringResource(R.string.ex_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
