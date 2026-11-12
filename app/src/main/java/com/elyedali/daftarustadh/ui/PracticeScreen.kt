package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elyedali.daftarustadh.data.PracticeBlock

/** عناوين الشاشة بلغة المادة نفسها (الأستاذ قد لا يفهم لغة أخرى). */
private data class PracticeLabels(val exercise: String, val proposal: String, val show: String, val hide: String, val covers: String)

private fun labelsFor(lang: String) = when (lang) {
    "fr" -> PracticeLabels("Exercice", "Proposition", "Afficher la solution", "Masquer la solution", "Révision de")
    "en" -> PracticeLabels("Exercise", "Proposal", "Show the answer", "Hide the answer", "Review of")
    else -> PracticeLabels("التمرين", "المقترح", "إظهار الحل", "إخفاء الحل", "مراجعة")
}

/** سلسلة تمارين أسبوع الإدماج/التقويم/العلاج، أو مقترحات أسبوع الامتحان، مع الحلول. */
@Composable
fun PracticeDialog(block: PracticeBlock, lang: String, subjectLabel: String, onDismiss: () -> Unit) {
    val L = labelsFor(lang)
    val shown = remember { mutableStateMapOf<Int, Boolean>() }
    val isExam = block.kind == "exam"
    CompositionLocalProvider(LocalLayoutDirection provides if (lang == "ar") LocalLayoutDirection.current else LayoutDirection.Ltr) {
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().background(if (isExam) Color(0xFFC62828) else Color(0xFFE65100))
                        .padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(block.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text("$subjectLabel — ${block.week} · ${block.items.size}", color = Color.White.copy(alpha = .85f), fontSize = 12.sp)
                        }
                        Text(if (LocalLayoutDirection.current == LayoutDirection.Ltr) "✕" else "✕", color = Color.White, fontSize = 22.sp,
                            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onDismiss).padding(8.dp))
                    }
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (block.covers.isNotEmpty()) item {
                            Text("${L.covers} : " + block.covers.joinToString(" · "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        itemsIndexed(block.items) { i, it ->
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (isExam) it.title else "${L.exercise} ${i + 1} — ${it.title}",
                                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isExam) Color(0xFFC62828) else Color(0xFFB23A1F))
                                Text(it.text, fontSize = 14.5.sp, lineHeight = 23.sp)
                                if (it.solution.isNotBlank()) {
                                    val open = shown[i] == true
                                    Text((if (open) "▲ " + L.hide else "▼ " + L.show), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { shown[i] = !open }.padding(vertical = 4.dp))
                                    if (open) Text(it.solution, fontSize = 14.sp, lineHeight = 22.sp,
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFEEF7EE)).padding(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
