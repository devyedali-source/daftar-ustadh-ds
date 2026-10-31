package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity

/** «أقسامي» عند الأستاذ: الأقسام التي وصلته ملفاتها، من كل المؤسسات. */
@Composable
fun TeachingClassesScreen(
    classes: List<ClassEntity>,
    schoolNames: Map<String, String>,
    onOpenFile: () -> Unit,
    onOpen: (ClassEntity) -> Unit,
    onBack: () -> Unit
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                Text(stringResource(R.string.my_classes), style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onOpenFile, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.open_class_file)) }
            if (classes.isEmpty()) Text(stringResource(R.string.no_teaching_classes))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(classes, key = { it.id }) { c ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(c) }) {
                        Column(Modifier.padding(14.dp)) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(schoolNames[c.institutionId] ?: "", style = MaterialTheme.typography.bodySmall)
                            Text(c.schoolYear, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
