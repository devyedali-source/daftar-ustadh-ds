package com.elyedali.daftarustadh.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.exchange.ExchangeAd
import com.elyedali.daftarustadh.exchange.ExchangeRepository
import com.elyedali.daftarustadh.exchange.ExchangeRole
import com.elyedali.daftarustadh.exchange.OfferType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** مادة يمكن اختيارها تخصصًا. */
data class SpecialtyOption(val code: String, val label: String)

private val WhatsAppGreen = Color(0xFF25D366)
private val DangerRed = Color(0xFFD32F2F)

@Composable
fun exchangeRoleLabel(role: String): String = stringResource(
    when (role) {
        ExchangeRole.STUDIES_DIRECTOR -> R.string.ex_role_director
        ExchangeRole.GENERAL_SUPERVISOR -> R.string.ex_role_supervisor
        else -> R.string.ex_role_teacher
    }
)

@Composable
fun offerTypeLabel(type: String): String = stringResource(
    when (type) {
        OfferType.SELL -> R.string.ex_offer_sell
        OfferType.BUY -> R.string.ex_offer_buy
        else -> R.string.ex_offer_friendly
    }
)

private fun offerTypeIcon(type: String) = when (type) {
    OfferType.SELL -> "🏷️"
    OfferType.BUY -> "💰"
    else -> "🤝"
}

private enum class Direction { ALL, LEAVING, ENTERING }

/**
 * تبادلات الأساتذة — ثلاثة تبويبات: «العروض» (مع التصفية)، «استمارة الإعلان»، «منشوراتي».
 * لا يظهر في المنشور اسم ولا رقم؛ زر «تواصل» يفتح واتساب صاحب المنشور برسالة جاهزة.
 */
@Composable
fun ExchangesScreen(
    title: String,
    wilayas: List<String>,
    specialties: List<SpecialtyOption>,
    myPosts: List<ExchangeAd>,
    ownerId: String,
    repository: ExchangeRepository,
    onSaveMyPosts: suspend (List<ExchangeAd>) -> Unit,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = tab != 0) { tab = 0 }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val labels = remember(specialties) { specialties.associate { it.code to it.label } }
    val publishedMsg = stringResource(R.string.ex_published)
    val savedLocalMsg = stringResource(R.string.ex_saved_local)

    var activationFor by remember { mutableStateOf<ExchangeAd?>(null) }

    // يحفظ المنشور على الهاتف ثم يحاول إرساله إلى الخادم.
    val savePost: (ExchangeAd) -> Unit = { ad ->
        scope.launch {
            val others = myPosts.filter { it.id != ad.id }
            onSaveMyPosts(listOf(ad) + others)
            val ok = runCatching { repository.publish(ad) }.getOrDefault(false)
            if (ok) onSaveMyPosts(listOf(ad.copy(isSynced = true)) + others)
            Toast.makeText(context, if (ok) publishedMsg else savedLocalMsg, Toast.LENGTH_LONG).show()
        }
    }
    val deletePost: (ExchangeAd) -> Unit = { ad ->
        scope.launch {
            runCatching { repository.delete(ad.id) }
            onSaveMyPosts(myPosts.filter { it.id != ad.id })
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ScreenHeader(title, onBack)
            SegmentedTabs(
                listOf(
                    stringResource(R.string.ex_tab_offers),
                    stringResource(R.string.ex_tab_form),
                    stringResource(R.string.ex_tab_mine)
                ),
                tab
            ) { tab = it }
            if (!repository.isConnected) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ex_server_pending), Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            when (tab) {
                0 -> OffersTab(wilayas, specialties, labels, myPosts, repository, onDelete = deletePost)
                1 -> ExchangeForm(wilayas, specialties, existing = null, ownerId = ownerId,
                    defaults = myPosts.firstOrNull()) { ad -> savePost(ad); tab = 2; activationFor = ad }
                else -> MyPostsTab(wilayas, specialties, labels, myPosts, ownerId, onSave = savePost, onDelete = deletePost)
            }
        }
    }
    activationFor?.let { ad -> ActivationDialog(ad) { activationFor = null } }
}

/** يفتح واتساب الإدارة برسالة طلب تفعيل الإعلان (كما في دفتر المعلم). */
fun sendExchangeActivation(context: android.content.Context, ad: ExchangeAd, message: String) {
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=${com.elyedali.daftarustadh.exchange.ExchangeActivation.ADMIN_PHONE}&text=" + Uri.encode(message))
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.ex_no_whatsapp), Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun activationMessage(ad: ExchangeAd): String = stringResource(R.string.ex_act_message, ad.code) + "\n" +
    stringResource(R.string.act_msg_link, com.elyedali.daftarustadh.auth.DsSubscriptions.exchangeLink(ad.id))

/** بعد نشر الإعلان: السعر، وملاحظة أنه لا يظهر للعامة إلا بعد التفعيل، وزر واتساب الإدارة. */
@Composable
private fun ActivationDialog(ad: ExchangeAd, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val message = activationMessage(ad)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📢", fontSize = 24.sp)
                Text(stringResource(R.string.ex_act_title), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1B5E20))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = BorderStroke(1.dp, Color(0xFFFFB300)), shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🏷️ " + stringResource(R.string.ex_act_price, com.elyedali.daftarustadh.exchange.ExchangeActivation.PRICE_MRU),
                            fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFFE65100))
                        Text(stringResource(R.string.ex_act_note), fontSize = 12.sp, color = Color(0xFF5D4037))
                    }
                }
                Text(stringResource(R.string.ex_act_press, ad.code), fontSize = 13.sp, color = Color.DarkGray)
            }
        },
        confirmButton = {
            Button(
                onClick = { sendExchangeActivation(context, ad, message); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen), shape = RoundedCornerShape(12.dp)
            ) { Text("💬 " + stringResource(R.string.ex_act_whatsapp), fontWeight = FontWeight.Bold, color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ex_close), color = Color.Gray) } },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun SegmentedTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(primary).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        labels.forEachIndexed { i, label ->
            val isSelected = i == selected
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 12.sp, maxLines = 1,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    color = if (isSelected) primary else Color.White)
            }
        }
    }
}

/* ---------------- العروض ---------------- */

@Composable
private fun OffersTab(
    wilayas: List<String>,
    specialties: List<SpecialtyOption>,
    labels: Map<String, String>,
    myPosts: List<ExchangeAd>,
    repository: ExchangeRepository,
    onDelete: (ExchangeAd) -> Unit
) {
    var remote by remember { mutableStateOf<List<ExchangeAd>>(emptyList()) }
    LaunchedEffect(repository) { remote = runCatching { repository.list() }.getOrDefault(emptyList()) }
    var specialtyFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var typeFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var roleFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var wilayaFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var direction by rememberSaveable { mutableStateOf(Direction.ALL) }
    var pickWilaya by remember { mutableStateOf(false) }

    // منشوراتي تظهر أيضًا في العروض (كما يراها الآخرون) مع علامة «منشوري».
    val all = remember(remote, myPosts) {
        (myPosts + remote.filter { r -> myPosts.none { it.id == r.id } }).sortedByDescending { it.createdAt }
    }
    val myIds = remember(myPosts) { myPosts.map { it.id }.toSet() }
    // العروض العامة: الإعلانات المفعّلة فقط (كما في دفتر المعلم)
    val filtered = all.filter { ad ->
        ad.isApproved &&
        (specialtyFilter == null || specialtyFilter in ad.specialties) &&
            (typeFilter == null || ad.offerType == typeFilter) &&
            (roleFilter == null || ad.role == roleFilter) &&
            (wilayaFilter == null || when (direction) {
                Direction.LEAVING -> ad.wilaya == wilayaFilter
                Direction.ENTERING -> wilayaFilter in ad.desiredWilayas
                Direction.ALL -> ad.wilaya == wilayaFilter || wilayaFilter in ad.desiredWilayas
            })
    }
    val countText = when {
        wilayaFilter == null -> stringResource(R.string.ex_count_all, filtered.size)
        direction == Direction.LEAVING -> stringResource(R.string.ex_count_leaving, wilayaLabel(wilayaFilter!!), filtered.size)
        direction == Direction.ENTERING -> stringResource(R.string.ex_count_entering, wilayaLabel(wilayaFilter!!), filtered.size)
        else -> stringResource(R.string.ex_count_wilaya, wilayaLabel(wilayaFilter!!), filtered.size)
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📢", fontSize = 20.sp)
                    Text(stringResource(R.string.ex_promo), fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                }
            }
        }
        item {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔎 " + stringResource(R.string.ex_filter_title), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    ChipFilter(stringResource(R.string.ex_filter_specialty), specialties.map { it.code to it.label }, specialtyFilter) { specialtyFilter = it }
                    ChipFilter(stringResource(R.string.ex_filter_type), OfferType.ALL.map { it to offerTypeLabel(it) }, typeFilter) { typeFilter = it }
                    ChipFilter(stringResource(R.string.ex_filter_role), ExchangeRole.ALL.map { it to exchangeRoleLabel(it) }, roleFilter) { roleFilter = it }
                    OutlinedButton(onClick = { pickWilaya = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.ex_filter_wilaya) + ": " + (wilayaFilter?.let { wilayaLabel(it) } ?: stringResource(R.string.ex_all_wilayas)))
                    }
                    if (wilayaFilter != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DirectionChip(Modifier.weight(1f), "🚪 " + stringResource(R.string.ex_dir_leaving),
                                direction == Direction.LEAVING, DangerRed) {
                                direction = if (direction == Direction.LEAVING) Direction.ALL else Direction.LEAVING
                            }
                            DirectionChip(Modifier.weight(1f), "🎯 " + stringResource(R.string.ex_dir_entering),
                                direction == Direction.ENTERING, MaterialTheme.colorScheme.primary) {
                                direction = if (direction == Direction.ENTERING) Direction.ALL else Direction.ENTERING
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(countText, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp))
        }
        item {
            Surface(
                color = Color(0xFFFFF8E1), shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFE082)), modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ℹ️", fontSize = 13.sp)
                    Text(stringResource(R.string.ex_disclaimer), fontSize = 11.sp, color = Color(0xFF795548), lineHeight = 15.sp)
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth().padding(vertical = 12.dp), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🔍", fontSize = 40.sp)
                        Text(stringResource(R.string.ex_no_results), fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
                        Text(stringResource(R.string.ex_no_results_hint), fontSize = 12.sp, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { ad ->
                val mine = ad.id in myIds
                ExchangeOfferCard(ad, mine, labels, specialties, myPosts.firstOrNull(),
                    onDelete = if (mine) ({ onDelete(ad) }) else null)
            }
        }
    }

    if (pickWilaya) {
        WilayaPickerDialog(
            title = stringResource(R.string.ex_filter_wilaya),
            options = listOf<String?>(null) + wilayas,
            selected = wilayaFilter,
            labelFor = { it?.let { w -> wilayaLabel(w) } ?: stringResource(R.string.ex_all_wilayas) },
            onPick = { wilayaFilter = it; if (it == null) direction = Direction.ALL; pickWilaya = false },
            onDismiss = { pickWilaya = false }
        )
    }
}

@Composable
private fun ChipFilter(label: String, options: List<Pair<String, String>>, selected: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(selected = selected == null, onClick = { onSelect(null) },
                    label = { Text(stringResource(R.string.ex_all), fontSize = 11.sp) })
            }
            items(options, key = { it.first }) { (code, text) ->
                FilterChip(selected = selected == code, onClick = { onSelect(code) },
                    label = { Text(text, fontSize = 11.sp) })
            }
        }
    }
}

@Composable
private fun DirectionChip(modifier: Modifier, text: String, active: Boolean, accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
            .border(if (active) 2.dp else 1.dp, if (active) accent else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        color = if (active) accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface
    ) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 10.dp), fontSize = 11.5.sp, maxLines = 1,
            textAlign = TextAlign.Center,
            fontWeight = if (active) FontWeight.Black else FontWeight.Bold,
            color = if (active) accent else MaterialTheme.colorScheme.onSurface)
    }
}

/* ---------------- بطاقة العرض ---------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExchangeOfferCard(
    ad: ExchangeAd,
    isMine: Boolean,
    labels: Map<String, String>,
    specialties: List<SpecialtyOption>,
    myDefaults: ExchangeAd?,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)?
) {
    var showContact by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val date = remember(ad.createdAt) {
        runCatching { SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.ROOT).format(Date(ad.createdAt)) }.getOrDefault("")
    }
    val (badgeBg, badgeFg) = when (ad.offerType) {
        OfferType.SELL -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        OfferType.BUY -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }
    Card(
        Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isMine && !ad.isApproved) {
                val context = LocalContext.current
                val message = activationMessage(ad)
                Card(
                    Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = BorderStroke(1.dp, Color(0xFFFFB300))
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("⏳ " + stringResource(R.string.ex_act_not_active), color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(stringResource(R.string.ex_act_banner, com.elyedali.daftarustadh.exchange.ExchangeActivation.PRICE_MRU),
                                color = Color(0xFF5D4037), fontSize = 11.sp, lineHeight = 15.sp)
                        }
                        Button(
                            onClick = { sendExchangeActivation(context, ad, message) },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text(stringResource(R.string.ex_act_button), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("👨‍🏫 " + exchangeRoleLabel(ad.role), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (ad.specialties.isNotEmpty()) Text(
                        stringResource(R.string.ex_specialty_line, ad.specialties.joinToString("، ") { labels[it] ?: it }),
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                    if (ad.isMoughataaLevel) Badge("🏛️ " + stringResource(R.string.ex_moughataa_badge), Color(0xFFFFF3E0), Color(0xFFE65100))
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Badge(offerTypeIcon(ad.offerType) + " " + offerTypeLabel(ad.offerType), badgeBg, badgeFg)
                    if (isMine) Badge(
                        if (ad.isApproved) "🌐 " + stringResource(R.string.ex_published_badge) else "⏳ " + stringResource(R.string.ex_not_published_badge),
                        if (ad.isApproved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        if (ad.isApproved) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }
            HorizontalDivider()
            if (ad.offerType != OfferType.FRIENDLY) {
                Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color(0xFFFFC107))) {
                    Text(
                        "💵 " + if (ad.isNegotiable || ad.priceAmount.isBlank()) stringResource(R.string.ex_price_negotiable)
                        else stringResource(R.string.ex_price_amount, ad.priceAmount),
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold, fontSize = 12.sp
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("📍 " + stringResource(R.string.ex_current_place), fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.ex_place_line, wilayaLabel(ad.wilaya), ad.moughataa), fontSize = 13.sp)
            }
            if (ad.desiredWilayas.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🧭 " + stringResource(R.string.ex_desired), fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ad.desiredWilayas.forEach { w -> Badge("📍 " + wilayaLabel(w), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary) }
                    }
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (date.isNotBlank()) "⏱️ $date" else "", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                if (isMine) {
                    if (onEdit != null) OutlinedButton(onClick = onEdit, shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("✏️ " + stringResource(R.string.ex_edit), fontSize = 12.sp)
                    }
                    if (onDelete != null) Button(onClick = { confirmDelete = true }, shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("🗑️ " + stringResource(R.string.ex_delete_short), fontSize = 12.sp, color = Color.White)
                    }
                } else {
                    Button(onClick = { showContact = true }, shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("💬 " + stringResource(R.string.ex_contact), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (showContact) ContactDialog(ad, labels, specialties, myDefaults) { showContact = false }
    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            text = { Text(stringResource(R.string.ex_delete_confirm)) },
            confirmButton = {
                Button(onClick = { confirmDelete = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) {
                    Text(stringResource(R.string.ex_delete_yes), color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun Badge(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = fg, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/**
 * قبل فتح واتساب يؤكد المتصل معلومات مكان عمله (التخصص، الولاية، المقاطعة) لتُرسل في الرسالة،
 * فيعرف صاحب المنشور من يتواصل معه. تُملأ تلقائيًا من أول منشور له إن وُجد.
 */
@Composable
private fun ContactDialog(
    ad: ExchangeAd,
    labels: Map<String, String>,
    specialties: List<SpecialtyOption>,
    myDefaults: ExchangeAd?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var specialty by remember { mutableStateOf(myDefaults?.specialties?.firstOrNull()?.let { labels[it] } ?: "") }
    var wilaya by remember { mutableStateOf(myDefaults?.wilaya ?: "") }
    var moughataa by remember { mutableStateOf(myDefaults?.moughataa ?: "") }
    var error by remember { mutableStateOf(false) }
    val header = stringResource(R.string.ex_msg_header)
    val lSpec = stringResource(R.string.ex_msg_specialty)
    val lWilaya = stringResource(R.string.ex_wilaya)
    val lMough = stringResource(R.string.ex_moughataa)
    val noWhatsApp = stringResource(R.string.ex_no_whatsapp)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("💬 " + stringResource(R.string.ex_contact_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.ex_contact_intro), fontSize = 12.sp)
                OutlinedTextField(specialty, { specialty = it }, label = { Text(lSpec) }, singleLine = true,
                    placeholder = { Text(specialties.firstOrNull()?.label ?: "") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(wilaya, { wilaya = it }, label = { Text(lWilaya) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(moughataa, { moughataa = it }, label = { Text(lMough) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (error) Text(stringResource(R.string.ex_contact_fill), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (specialty.isBlank() || wilaya.isBlank() || moughataa.isBlank()) { error = true; return@Button }
                val text = "$header\n\n$lSpec: ${specialty.trim()}\n$lWilaya: ${wilaya.trim()}\n$lMough: ${moughataa.trim()}"
                val uri = Uri.parse("https://wa.me/${ad.whatsapp}?text=" + Uri.encode(text))
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    onDismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, noWhatsApp, Toast.LENGTH_SHORT).show()
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)) {
                Text(stringResource(R.string.ex_contact_go), color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

/* ---------------- منشوراتي ---------------- */

@Composable
private fun MyPostsTab(
    wilayas: List<String>,
    specialties: List<SpecialtyOption>,
    labels: Map<String, String>,
    myPosts: List<ExchangeAd>,
    ownerId: String,
    onSave: (ExchangeAd) -> Unit,
    onDelete: (ExchangeAd) -> Unit
) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = myPosts.firstOrNull { it.id == editingId }
    if (editing != null) {
        BackHandler { editingId = null }
        ExchangeForm(wilayas, specialties, existing = editing, ownerId = ownerId, defaults = null) { ad ->
            onSave(ad); editingId = null
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Text(stringResource(R.string.ex_my_posts_count, myPosts.size), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (myPosts.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("📭", fontSize = 40.sp)
                        Text(stringResource(R.string.ex_no_posts), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(stringResource(R.string.ex_no_posts_hint), fontSize = 12.sp, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(myPosts, key = { it.id }) { ad ->
                ExchangeOfferCard(ad, true, labels, specialties, null,
                    onEdit = if (ad.isApproved) null else ({ editingId = ad.id }), onDelete = { onDelete(ad) })
            }
        }
    }
}

/* ---------------- استمارة الإعلان ---------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExchangeForm(
    wilayas: List<String>,
    specialties: List<SpecialtyOption>,
    existing: ExchangeAd?,
    ownerId: String,
    defaults: ExchangeAd?,
    onSubmit: (ExchangeAd) -> Unit
) {
    val base = existing ?: defaults
    var offerType by rememberSaveable { mutableStateOf(existing?.offerType ?: OfferType.FRIENDLY) }
    var negotiable by rememberSaveable { mutableStateOf(existing?.isNegotiable ?: false) }
    var price by rememberSaveable { mutableStateOf(existing?.priceAmount ?: "") }
    var role by rememberSaveable { mutableStateOf(base?.role ?: ExchangeRole.TEACHER) }
    val chosen = remember { mutableStateListOf<String>().apply { addAll(base?.specialties.orEmpty()) } }
    var moughataaLevel by rememberSaveable { mutableStateOf(base?.isMoughataaLevel ?: false) }
    var wilaya by rememberSaveable { mutableStateOf(base?.wilaya ?: "") }
    var moughataa by rememberSaveable { mutableStateOf(base?.moughataa ?: "") }
    var phone by rememberSaveable { mutableStateOf(base?.whatsapp?.let { ExchangeAd.localPhone(it) } ?: "") }
    val desired = remember { mutableStateListOf<String>().apply { addAll(existing?.desiredWilayas.orEmpty()) } }
    var pickWilaya by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(if (existing == null) R.string.ex_form_title else R.string.ex_form_edit_title),
            fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.ex_privacy_note), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        FormSection(stringResource(R.string.ex_step1)) {
            OfferType.ALL.forEach { t ->
                Row(Modifier.fillMaxWidth().clickable { offerType = t }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = offerType == t, onClick = { offerType = t })
                    Text(offerTypeIcon(t) + " " + offerTypeLabel(t))
                }
            }
            if (offerType != OfferType.FRIENDLY) {
                Text("💵 " + stringResource(R.string.ex_price_title), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !negotiable, onClick = { negotiable = false }, label = { Text(stringResource(R.string.ex_price_set)) })
                    FilterChip(selected = negotiable, onClick = { negotiable = true }, label = { Text(stringResource(R.string.ex_price_private)) })
                }
                if (!negotiable) OutlinedTextField(
                    value = price, onValueChange = { v -> price = v.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.ex_price_label)) }, singleLine = true,
                    suffix = { Text(stringResource(R.string.ex_ouguiya)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        FormSection(stringResource(R.string.ex_step2)) {
            ExchangeRole.ALL.forEach { r ->
                Row(Modifier.fillMaxWidth().clickable { role = r }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == r, onClick = { role = r })
                    Text(exchangeRoleLabel(r))
                }
            }
            Text(stringResource(if (role == ExchangeRole.TEACHER) R.string.ex_specialties_required else R.string.ex_specialties_optional),
                fontSize = 12.sp, fontWeight = FontWeight.Bold)
            val maxSpec = com.elyedali.daftarustadh.data.TimetableTeachers.MAX_SUBJECTS
            Text(stringResource(R.string.max_subjects, chosen.size, maxSpec), fontSize = 12.sp,
                color = if (chosen.size >= maxSpec) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                specialties.forEach { s ->
                    FilterChip(
                        selected = s.code in chosen,
                        enabled = s.code in chosen || chosen.size < maxSpec,
                        onClick = { if (s.code in chosen) chosen.remove(s.code) else chosen.add(s.code) },
                        label = { Text(s.label, fontSize = 12.sp) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = moughataaLevel, onCheckedChange = { moughataaLevel = it })
                Column {
                    Text("🏛️ " + stringResource(R.string.ex_moughataa_badge), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.ex_moughataa_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        FormSection(stringResource(R.string.ex_step3)) {
            OutlinedButton(onClick = { pickWilaya = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.ex_wilaya) + ": " + (if (wilaya.isBlank()) stringResource(R.string.ex_choose_wilaya) else wilayaLabel(wilaya)))
            }
            // اختيار المقاطعة من مقاطعات الولاية (كما في دفتر المعلم)
            val options = com.elyedali.daftarustadh.data.Moughataas.of(wilaya)
            if (wilaya.isBlank()) {
                Text(stringResource(R.string.ex_choose_wilaya_first), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (options.isNotEmpty()) {
                Text(stringResource(R.string.ex_moughataa) + ":", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { m ->
                        FilterChip(selected = moughataa == m, onClick = { moughataa = m }, label = { Text(m) })
                    }
                }
            } else {
                OutlinedTextField(moughataa, { moughataa = it }, label = { Text(stringResource(R.string.ex_moughataa)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }

        FormSection(stringResource(R.string.ex_step4)) {
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("💬 " + stringResource(R.string.ex_whatsapp_hint)) },
                placeholder = { Text("46123456") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        FormSection(stringResource(R.string.ex_step5)) {
            Text(stringResource(R.string.ex_desired_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                wilayas.filter { it != wilaya }.forEach { w ->
                    FilterChip(selected = w in desired, onClick = { if (w in desired) desired.remove(w) else desired.add(w) },
                        label = { Text(wilayaLabel(w), fontSize = 12.sp) })
                }
            }
        }

        error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }

        Button(
            onClick = {
                val number = ExchangeAd.normalizePhone(phone)
                val targets = wilayas.filter { it in desired && it != wilaya }
                error = when {
                    offerType != OfferType.FRIENDLY && !negotiable && price.isBlank() -> R.string.ex_err_price
                    role == ExchangeRole.TEACHER && chosen.isEmpty() -> R.string.ex_err_specialties
                    chosen.size > com.elyedali.daftarustadh.data.TimetableTeachers.MAX_SUBJECTS -> R.string.ex_err_max_specialties
                    wilaya.isBlank() -> R.string.ex_err_wilaya
                    moughataa.isBlank() -> R.string.ex_err_moughataa
                    number == null -> R.string.ex_err_phone
                    targets.isEmpty() -> R.string.ex_err_desired
                    else -> null
                }
                if (error == null && number != null) onSubmit(
                    ExchangeAd(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        ownerId = ownerId,
                        offerType = offerType,
                        priceAmount = if (offerType == OfferType.FRIENDLY || negotiable) "" else price,
                        isNegotiable = offerType != OfferType.FRIENDLY && negotiable,
                        role = role,
                        specialties = specialties.map { it.code }.filter { it in chosen },
                        isMoughataaLevel = moughataaLevel,
                        wilaya = wilaya,
                        moughataa = moughataa.trim(),
                        whatsapp = number,
                        desiredWilayas = targets,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(stringResource(if (existing == null) R.string.ex_publish_now else R.string.ex_save_changes),
                fontWeight = FontWeight.Bold)
        }
    }

    if (pickWilaya) {
        WilayaPickerDialog(
            title = stringResource(R.string.ex_wilaya),
            options = wilayas,
            selected = wilaya,
            labelFor = { wilayaLabel(it) },
            onPick = { if (it != wilaya) moughataa = ""; wilaya = it; desired.remove(it); pickWilaya = false },
            onDismiss = { pickWilaya = false }
        )
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

/** اسم الولاية بلغة الواجهة: يُحفظ بالعربية ويُعرض بالفرنسية في الواجهة الفرنسية. */
@Composable
private fun wilayaLabel(name: String): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fr = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "fr"
    return if (fr) com.elyedali.daftarustadh.data.SchoolDirectory.wilaya(context, name)?.nameFr ?: name else name
}

@Composable
private fun <T> WilayaPickerDialog(
    title: String,
    options: List<T>,
    selected: T,
    labelFor: @Composable (T) -> String,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(options) { o ->
                    Row(Modifier.fillMaxWidth().clickable { onPick(o) }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = o == selected, onClick = { onPick(o) })
                        Text(labelFor(o))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
