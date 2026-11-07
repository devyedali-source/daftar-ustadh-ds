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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.AnnualPlans

/**
 * التخطيط السنوي لقسم إعدادي (تطبيق الأستاذ): لكل مادة يدرّسها، أسبوعًا بأسبوع على السنة كاملة،
 * مع أسابيع الاختبارات والعطل (العطلة بعد اختبار الفصل).
 */
/** مادة بلا تخطيط سنوي: التربية البدنية. */
private const val NO_PLAN = "EPS"

@Composable
fun PlanningTab(
    levelCode: String,
    mySubjects: List<Pair<String, String>>,
    /** غير مفعّل: الفصل الأول مجاني، والفصلان الثاني والثالث مقفلان. */
    locked: Boolean = false,
    onActivate: () -> Unit = {}
) {
    val context = LocalContext.current
    val withPlan = remember(levelCode) { AnnualPlans.subjectsWithPlan(context, levelCode) }
    val available = mySubjects.filter { it.first in withPlan }
    // التربية البدنية ليس لها تخطيط سنوي: لا تُعدّ ناقصة
    val missing = mySubjects.filter { it.first !in withPlan && it.first != NO_PLAN }
    val onlyNoPlan = available.isEmpty() && mySubjects.isNotEmpty() && mySubjects.all { it.first == NO_PLAN }
    var chosen by rememberSaveable { mutableStateOf(available.firstOrNull()?.first) }
    val subject = chosen?.takeIf { c -> available.any { it.first == c } } ?: available.firstOrNull()?.first
    val rows = remember(subject, levelCode) { subject?.let { AnnualPlans.rows(context, it, levelCode) }.orEmpty() }
    val termNames = listOf("", stringResource(R.string.term_1), stringResource(R.string.term_2), stringResource(R.string.term_3))
    val holiday = stringResource(R.string.plan_holiday)
    // دروس الكتاب المرتبطة بعناوين التخطيط (محتوى الكتاب، السبورة، التحضير)
    val lessonSet = remember(subject, levelCode) { subject?.let { com.elyedali.daftarustadh.data.LessonBank.load(context, it, levelCode) } }
    var opened by remember { mutableStateOf<Pair<com.elyedali.daftarustadh.data.BookLesson, String>?>(null) }
    opened?.let { (l, week) ->
        LessonDialog(l, available.firstOrNull { it.first == subject }?.second.orEmpty(), week,
            lessonSet?.bookOf(l).orEmpty(), lessonSet?.bookTitleOf(l).orEmpty()) { opened = null }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            Text("🗓️ " + stringResource(R.string.plan_title), fontWeight = FontWeight.Black, fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary)
        }
        if (available.size > 1) item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(available, key = { it.first }) { (code, label) ->
                    FilterChip(selected = code == subject, onClick = { chosen = code }, label = { Text(label) })
                }
            }
        }
        if (missing.isNotEmpty()) item {
            Text(stringResource(R.string.plan_missing, missing.joinToString("، ") { it.second }), fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (subject == null) {
            item { Text(stringResource(if (onlyNoPlan) R.string.plan_no_eps else R.string.plan_none), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            return@LazyColumn
        }
        item {
            Text(available.first { it.first == subject }.second + " — " + levelCode, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            val src = AnnualPlans.source(context, subject)
            if (src.isNotBlank()) Text("📄 " + src, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (lessonSet != null) Text("📖 " + stringResource(R.string.plan_lessons_hint), fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        // غير مفعّل: يُعرض الفصل الأول فقط، وتحل محل الفصلين الثاني والثالث بطاقة التفعيل
        val shownRows = if (locked) rows.filter { !com.elyedali.daftarustadh.data.TeacherActivation.planningNeedsActivation(it.term) } else rows
        itemsIndexed(shownRows) { i, r ->
            val prev = shownRows.getOrNull(i - 1)
            // كم مرة ورد كل عنوان قبل هذا الأسبوع (لربط العناوين المكررة بدروس مختلفة)
            val seen = shownRows.take(i).flatMap { it.items }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // عنوان الفصل عند بدايته
                if (r.term in 1..3 && r.kind != "holiday" && (prev == null || prev.term != r.term))
                    Text(termNames[r.term], fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary).padding(horizontal = 10.dp, vertical = 6.dp))
                // عنوان الوحدة عند تغيّرها
                if (r.unit.isNotBlank() && r.unit != prev?.unit)
                    Text(r.unit, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp))
                val (bg, fg) = when (r.kind) {
                    "exam", "final" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                    "holiday" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                    "eval", "diag", "start", "activity" -> Color(0xFFFFF8E1) to Color(0xFFE65100)
                    else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg).padding(10.dp),
                    verticalAlignment = Alignment.Top) {
                    Text(if (r.kind == "holiday" && r.label.isBlank()) "🌴" else r.label, Modifier.width(78.dp),
                        fontWeight = FontWeight.Bold, fontSize = 12.sp, color = fg, textAlign = TextAlign.Start)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (r.kind == "holiday" && r.items.isEmpty()) Text(holiday, fontWeight = FontWeight.Bold, color = fg, fontSize = 13.sp)
                        r.items.forEach { item ->
                            val l = if (r.kind == "lesson") lessonSet?.find(item, seen.count { it == item }) else null
                            if (l != null) Text("📖 $item", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                                    .clickable { opened = l to r.label }.padding(vertical = 2.dp))
                            else Text("• $item", fontSize = 13.sp, color = fg,
                                fontWeight = if (r.kind == "lesson") FontWeight.Normal else FontWeight.Bold)
                        }
                    }
                }
            }
        }
        if (locked && rows.any { com.elyedali.daftarustadh.data.TeacherActivation.planningNeedsActivation(it.term) }) item {
            androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔒 " + stringResource(R.string.tact_planning_locked), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.tact_planning_locked_hint, com.elyedali.daftarustadh.data.TeacherActivation.PRICE), fontSize = 12.sp)
                    androidx.compose.material3.Button(onClick = onActivate, modifier = Modifier.fillMaxWidth()) {
                        Text("⭐ " + stringResource(R.string.tact_title))
                    }
                }
            }
        }
    }
}
