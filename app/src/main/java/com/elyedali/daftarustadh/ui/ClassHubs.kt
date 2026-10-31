package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R

/** هيكل قسم بتبويبات: رأس موحّد ثم شريط تبويبات ثم محتوى التبويب. */
@Composable
fun ClassHub(
    title: String,
    subtitles: List<String>,
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
            ScreenHeader(title, onBack, subtitles)
            ScrollableTabRow(selectedTabIndex = selected, edgePadding = 0.dp, containerColor = MaterialTheme.colorScheme.background) {
                tabs.forEachIndexed { i, t -> Tab(selected = selected == i, onClick = { onSelect(i) }, text = { Text(t) }) }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) { content() }
        }
    }
}

/** تبويب «الكشوف» عند الإدارة: حالة كل كشف (فصل أول، ثانٍ، سنوي) وزر إنتاجه. */
@Composable
fun KashfTab(
    missingByTerm: Map<Int, Int>,   // عدد الطلاب الذين تنقصهم نتائج لكل كشف
    onGenerate: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    locked: Boolean = false,          // القسم غير مفعّل: كشف الفصل الثاني والسنوي مقفلان
    onActivate: () -> Unit = {}
) {
    Column(Modifier.fillMaxSize().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(1 to R.string.term_1, 2 to R.string.term_2, 3 to R.string.kashf_annual).forEach { (term, res) ->
            val missing = missingByTerm[term] ?: 0
            val ready = missing == 0
            Card(colors = CardDefaults.cardColors(
                containerColor = if (ready) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
            )) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(res), style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (ready) stringResource(R.string.kashf_ready) else stringResource(R.string.kashf_missing, missing),
                        color = if (ready) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (locked && term >= 2) {
                        Text("🔒 " + stringResource(R.string.act_locked), color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                        Button(onClick = onActivate) { Text(stringResource(R.string.act_activate_class)) }
                    } else {
                        Button(enabled = ready, onClick = { onGenerate(term) }) {
                            Text(stringResource(if (term == 3) R.string.generate_annual else R.string.generate_term))
                        }
                    }
                }
            }
        }
        TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.kashf_settings)) }
    }
}

/** محتوى سيُبنى لاحقًا (الجدول الزمني، التشريع المدرسي...). */
@Composable
fun ComingSoon(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
