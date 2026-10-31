package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R

private val PAGE_CHOICES = listOf(5, 10, 20, 30, 50)

/**
 * دفتر النصوص — للإدارة فقط: يُسحب فارغًا (PDF) بالرأسية الرسمية ومعلومات القسم،
 * بأعمدة: التاريخ، التوقيت، المادة، المجال، الموضوع، المدة، التوقيع، ويُملأ باليد.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextbookTab(onPrint: (pages: Int) -> Unit) {
    var pages by rememberSaveable { mutableIntStateOf(10) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📒 " + stringResource(R.string.tab_textbook), fontWeight = FontWeight.Black, fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.tb_desc), fontSize = 13.sp)
                Text(stringResource(R.string.tb_columns), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text(stringResource(R.string.tb_pages), fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PAGE_CHOICES.forEach { n ->
                FilterChip(selected = pages == n, onClick = { pages = n }, label = { Text(stringResource(R.string.tb_pages_n, n)) })
            }
        }
        Button(onClick = { onPrint(pages) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text("🖨️ " + stringResource(R.string.tb_print), fontWeight = FontWeight.Bold)
        }
    }
}
