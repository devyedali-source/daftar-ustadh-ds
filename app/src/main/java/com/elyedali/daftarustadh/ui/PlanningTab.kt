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
import androidx.compose.runtime.collectAsState
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
    /** السنة الدراسية: تحدد أي الفصول مجانية (في سنة الإطلاق الفصلان الأول والثاني). */
    schoolYear: String,
    mySubjects: List<Pair<String, String>>,
    /** المادة غير مفعّلة في هذا المستوى: الفصل الأول مجاني، والفصلان الثاني والثالث مقفلان. */
    lockedFor: (String) -> Boolean = { false },
    onActivate: () -> Unit = {}
) {
    val context = LocalContext.current
    // ممنوع التقاط الشاشة وتسجيلها في التخطيط السنوي (إلا لحساب مدير الأقسام)
    SecureScreen()
    val withPlan = remember(levelCode) { AnnualPlans.subjectsWithPlan(context, levelCode) }
    // ترتيب المواد يحدده الأستاذ بنفسه (يرفع مادة أو ينزلها) ويُحفظ على الجهاز
    val prefs = remember { context.getSharedPreferences("planning_order", android.content.Context.MODE_PRIVATE) }
    var order by remember { mutableStateOf(prefs.getString("order", "").orEmpty().split(",").filter { it.isNotBlank() }) }
    var reordering by rememberSaveable { mutableStateOf(false) }
    // «موادي»: المواد التي يدرّسها الأستاذ (⭐) تأتي أولا في كل المستويات
    var favs by remember { mutableStateOf(prefs.getStringSet("fav_subjects", emptySet()).orEmpty().toSet()) }
    fun toggleFav(code: String) {
        favs = if (code in favs) favs - code else favs + code
        prefs.edit().putStringSet("fav_subjects", favs).apply()
    }
    val available = mySubjects.filter { it.first in withPlan }.let { list ->
        list.sortedWith(compareBy({ if (it.first in favs) 0 else 1 },
            { s -> order.indexOf(s.first).let { i -> if (i < 0) Int.MAX_VALUE else i } }))
    }
    fun move(code: String, delta: Int) {
        val codes = available.map { it.first }.toMutableList()
        val i = codes.indexOf(code); val j = i + delta
        if (i < 0 || j !in codes.indices) return
        codes[i] = codes[j]; codes[j] = code
        // نحتفظ بترتيب مواد المستويات الأخرى ونضع ترتيب هذا المستوى أولا
        order = codes + order.filter { it !in codes }
        prefs.edit().putString("order", order.joinToString(",")).apply()
    }
    // التربية البدنية ليس لها تخطيط سنوي: لا تُعدّ ناقصة
    val missing = mySubjects.filter { it.first !in withPlan && it.first != NO_PLAN }
    val onlyNoPlan = available.isEmpty() && mySubjects.isNotEmpty() && mySubjects.all { it.first == NO_PLAN }
    var chosen by rememberSaveable { mutableStateOf(available.firstOrNull()?.first) }
    val subject = chosen?.takeIf { c -> available.any { it.first == c } } ?: available.firstOrNull()?.first
    val locked = subject != null && lockedFor(subject)
    val rows = remember(subject, levelCode) { subject?.let { AnnualPlans.rows(context, it, levelCode) }.orEmpty() }
    val termNames = listOf("", stringResource(R.string.term_1), stringResource(R.string.term_2), stringResource(R.string.term_3))
    val holiday = stringResource(R.string.plan_holiday)
    // دروس الكتاب المرتبطة بعناوين التخطيط (محتوى الكتاب، السبورة، التحضير)
    val lessonSet = remember(subject, levelCode) { subject?.let { com.elyedali.daftarustadh.data.LessonBank.load(context, it, levelCode) } }
    // سلاسل التمارين ومقترحات الامتحانات لأسابيع الإدماج والتقويم والامتحان
    val practice = remember(subject, levelCode) { subject?.let { com.elyedali.daftarustadh.data.PracticeBank.load(context, it, levelCode) } }
    var openedPractice by remember { mutableStateOf<com.elyedali.daftarustadh.data.PracticeBlock?>(null) }
    openedPractice?.let { b ->
        com.elyedali.daftarustadh.ui.PracticeDialog(b, practice?.lang ?: "ar",
            available.firstOrNull { it.first == subject }?.second.orEmpty(), practice?.word.orEmpty()) { openedPractice = null }
    }
    var opened by remember { mutableStateOf<Pair<com.elyedali.daftarustadh.data.BookLesson, String>?>(null) }
    opened?.let { (l, week) ->
        LessonDialog(l, available.firstOrNull { it.first == subject }?.second.orEmpty(), week,
            lessonSet?.bookOf(l).orEmpty(), lessonSet?.bookTitleOf(l).orEmpty(), subject) { opened = null }
    }

    // كتاب المادة المعروضة: إن لم يكن على الهاتف تظهر حالته أو زر تنزيله وحده (يختفي عند اكتماله)
    val books = com.elyedali.daftarustadh.data.BookPack
    val bookStates by books.states.collectAsState()
    // قد يكون للمادة أكثر من كتاب في المستوى (الفيزياء والكيمياء، التاريخ والجغرافيا): يظهر أول كتاب غير جاهز
    val coursePacks = remember(subject, levelCode) { books.packs(context).filter { it.subject == subject && levelCode in it.levels } }
    val coursePack = coursePacks.firstOrNull { bookStates[it.name] !is com.elyedali.daftarustadh.data.BookPack.Status.Ready } ?: coursePacks.firstOrNull()
    val courseState = coursePack?.let { bookStates[it.name] }
    val confirmLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()) {}
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (withPlan.isNotEmpty() && coursePack != null && courseState != null &&
            courseState !is com.elyedali.daftarustadh.data.BookPack.Status.Ready &&
            courseState !is com.elyedali.daftarustadh.data.BookPack.Status.NotInTestBuild) item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("📖 " + stringResource(R.string.bk_plan_missing) + "  (" + megaBytes(coursePack.bytes) + ")", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                BookStatusLine(courseState,
                    onDownload = { books.download(context, coursePack.name) },
                    onConfirm = { books.confirm(confirmLauncher) })
            }
        }
        item {
            Text("🗓️ " + stringResource(R.string.plan_title), fontWeight = FontWeight.Black, fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary)
        }
        if (available.size > 1) item {
            Text(if (reordering) "✔ " + stringResource(R.string.plan_reorder_done) else "⭐ " + stringResource(R.string.plan_reorder),
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { reordering = !reordering }.padding(vertical = 6.dp, horizontal = 10.dp))
        }
        if (reordering && available.size > 1) {
            item { Text(stringResource(R.string.plan_reorder_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            itemsIndexed(available, key = { _, it -> "ord_" + it.first }) { i, (code, label) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (code in favs) "★" else "☆", fontSize = 24.sp,
                        color = if (code in favs) Color(0xFFF9A825) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { toggleFav(code) }.padding(horizontal = 6.dp))
                    Text("${i + 1}. " + label, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("▲", fontSize = 18.sp, color = if (i > 0) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(enabled = i > 0) { move(code, -1) }.padding(8.dp))
                    Text("▼", fontSize = 18.sp, color = if (i < available.size - 1) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(enabled = i < available.size - 1) { move(code, 1) }.padding(8.dp))
                }
            }
            return@LazyColumn
        }
        // مستوى لا تخطيط له بعد (الرابعة إعدادية والثانوي): رسالة واحدة واضحة
        if (withPlan.isEmpty()) {
            item {
                androidx.compose.material3.Card(Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🛠️", fontSize = 40.sp)
                        Text(stringResource(R.string.plan_level_in_progress), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            return@LazyColumn
        }
        if (available.size > 1) item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(available, key = { it.first }) { (code, label) ->
                    // ★ من «موادي»، 🔒 الفصلان الثاني والثالث مقفلان
                    FilterChip(selected = code == subject, onClick = { chosen = code },
                        label = { Text((if (lockedFor(code)) "🔒 " else "") + (if (code in favs) "★ " else "") + label) })
                }
            }
        }
        if (missing.isNotEmpty()) item {
            Text(stringResource(R.string.plan_missing, missing.joinToString(listSep()) { it.second }), fontSize = 12.sp,
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
        val shownRows = if (locked) rows.filter { !com.elyedali.daftarustadh.data.TeacherActivation.planningNeedsActivation(it.term, schoolYear) } else rows
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
                        // زر السلسلة أو مقترحات الامتحان لهذا الأسبوع
                        val ps = practice
                        ps?.forWeek(r.label)?.takeIf { r.kind != "lesson" }?.let { b ->
                            val lang = ps.lang
                            val label = when {
                                b.kind == "exam" -> when (lang) { "fr" -> "📄 Propositions d'examen"; "en" -> "📄 Exam proposals"; else -> "📄 مقترحات الامتحان" }
                                else -> when (lang) { "fr" -> "📝 Série d'exercices"; "en" -> "📝 Exercise series"; else -> if (ps.word.isNotBlank()) "📝 سلسلة الوضعيات" else "📝 سلسلة التمارين" }
                            } + " (${b.items.size})"
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White,
                                modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(8.dp)).background(fg)
                                    .clickable { openedPractice = b }.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }
        if (locked && rows.any { com.elyedali.daftarustadh.data.TeacherActivation.planningNeedsActivation(it.term, schoolYear) }) item {
            androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔒 " + stringResource(R.string.tact_planning_locked), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.tact_planning_locked_hint, com.elyedali.daftarustadh.data.TeacherActivation.PRICE_STAGE), fontSize = 12.sp)
                    androidx.compose.material3.Button(onClick = onActivate, modifier = Modifier.fillMaxWidth()) {
                        Text("⭐ " + stringResource(R.string.tact_title))
                    }
                }
            }
        }
    }
}
