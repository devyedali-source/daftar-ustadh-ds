package com.elyedali.daftarustadh.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.AnnualPlans
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.SubjectNames

/**
 * تطبيق الأستاذ بعد اختيار السنة: زران فقط.
 * ① البرنامج السنوي (خارج الأقسام، فلا يدخل الأستاذ أقسامه لتفقده فيفسد نتائجها).
 * ② مؤسساته: جدوله الزمني في كل مؤسسة وأقسامه وموادّه ورصد النتائج ورفعها للإدارة.
 */
@Composable
fun TeacherYearHub(year: String, onPlanning: () -> Unit, onSchools: () -> Unit, onBack: () -> Unit, onMenu: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.mode_teacher) + " — ⁦$year⁩", onMenu = onMenu, onBack = onBack)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    BigCard("📘", stringResource(R.string.tyh_plan_title), stringResource(R.string.tyh_plan_desc),
                        Color(0xFFE8F5E9), Color(0xFF1B5E20), onPlanning)
                }
                item {
                    BigCard("🏫", stringResource(R.string.tyh_schools_title), stringResource(R.string.tyh_schools_desc),
                        Color(0xFFE3F2FD), Color(0xFF1565C0), onSchools)
                }
            }
        }
    }
}

@Composable
private fun BigCard(icon: String, title: String, desc: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) { Text(icon, fontSize = 28.sp) }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = fg)
                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = fg)
        }
    }
}

/**
 * البرنامج السنوي: المرحلة (الإعدادية/الثانوية) ← المستوى ← المواد (التخطيط والتحاضير والكتب).
 * مستقل تمامًا عن الأقسام (قد يستعمل الأستاذ البرنامج وحده): يضع ☆ على المستويات التي تهمه فتأتي أولًا،
 * ويضع ★ على مواده فتأتي أولًا في كل المستويات («⭐ موادي أولًا»). الاختيار محفوظ على الجهاز.
 */
@Composable
fun PlanningBrowserScreen(
    year: String,
    isFr: Boolean,
    lockedFor: (level: String, subject: String) -> Boolean,
    onActivate: () -> Unit,
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    val context = LocalContext.current
    val levels = remember(year) { CurriculumRepository.levels(context, year) }
    var stage by rememberSaveable { mutableStateOf<String?>(null) }
    var level by rememberSaveable { mutableStateOf<String?>(null) }
    // «مستوياتي»: يختارها الأستاذ بنفسه بالنجمة، لا من أقسامه
    val prefs = remember { context.getSharedPreferences("planning_order", android.content.Context.MODE_PRIVATE) }
    var myLevels by remember { mutableStateOf(prefs.getStringSet("fav_levels", emptySet()).orEmpty().toSet()) }
    fun toggleLevel(code: String) {
        myLevels = if (code in myLevels) myLevels - code else myLevels + code
        prefs.edit().putStringSet("fav_levels", myLevels).apply()
    }
    BackHandler(enabled = stage != null) { if (level != null) level = null else stage = null }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                title = "📘 " + when {
                    level != null -> level!! + " — " + levelDescription(level!!)
                    stage != null -> stringResource(R.string.tyh_plan_title) + " — " + stageName(stage!!)
                    else -> stringResource(R.string.tyh_plan_title) + " — ⁦$year⁩"
                },
                onMenu = onMenu,
                onBack = { when { level != null -> level = null; stage != null -> stage = null; else -> onBack() } }
            )
            when {
                stage == null -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Text(stringResource(R.string.pb_pick_stage), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary) }
                    items(listOf("college", "lycee")) { st ->
                        val inStage = levels.filter { it.stage == st }
                        val mine = inStage.count { it.code in myLevels }
                        ChoiceCard(if (st == "college") "🎒" else "🎓", stageName(st),
                            stringResource(R.string.pb_levels_n, inStage.size) + if (mine > 0) "  ★ $mine" else "", null) { stage = st }
                    }
                }
                level == null -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { Text(stringResource(R.string.pb_pick_level), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary) }
                    // مستويات أقسام الأستاذ أولًا
                    items(levels.filter { it.stage == stage }.sortedBy { if (it.code in myLevels) 0 else 1 }, key = { it.code }) { l ->
                        val hasPlan = remember(l.code) { AnnualPlans.subjectsWithPlan(context, l.code).isNotEmpty() }
                        ChoiceCard(l.code, levelDescription(l.code),
                            stringResource(R.string.pb_subjects_n, l.subjects.count { it.code != "EPS" }),
                            if (hasPlan) null else stringResource(R.string.tact_soon),
                            star = l.code in myLevels, onStar = { toggleLevel(l.code) }) { level = l.code }
                    }
                }
                else -> Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp)) {
                    val lv = level!!
                    PlanningTab(
                        levelCode = lv,
                        mySubjects = CurriculumRepository.level(context, year, lv)?.subjects.orEmpty()
                            .map { it.code to SubjectNames.display(it, isFr) },
                        lockedFor = { code -> lockedFor(lv, code) },
                        onActivate = onActivate
                    )
                }
            }
        }
    }
}

@Composable
private fun stageName(st: String) = stringResource(if (st == "college") R.string.tact_stage_college else R.string.tact_stage_lycee)

@Composable
private fun ChoiceCard(badge: String, title: String, sub: String, note: String?,
                       star: Boolean? = null, onStar: () -> Unit = {}, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Text(badge, color = Color.White, fontWeight = FontWeight.Black, fontSize = if (badge.length > 2) 13.sp else 22.sp)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (note != null) Text(note, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
            if (star != null) androidx.compose.material3.IconButton(onClick = onStar) {
                Text(if (star) "★" else "☆", fontSize = 24.sp, color = if (star) Color(0xFFF9A825) else MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
