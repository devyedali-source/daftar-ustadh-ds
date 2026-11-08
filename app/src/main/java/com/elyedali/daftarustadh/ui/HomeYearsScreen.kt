package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.ClassGenderCount
import com.elyedali.daftarustadh.data.Gender
import com.elyedali.daftarustadh.data.SchoolYear

/** شريط علوي كما في دفتر المعلم: لون أساسي، زر القائمة أو الرجوع، شعار التطبيق، والعنوان. */
@Composable
fun AppTopBar(title: String, onMenu: (() -> Unit)?, onBack: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
        }
        if (onMenu != null) IconButton(onClick = onMenu, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White)
        }
        Image(
            // شعار الجمهورية الإسلامية الموريتانية
            painter = painterResource(R.drawable.img_mauritania_seal), contentDescription = null,
            modifier = Modifier.padding(horizontal = 4.dp).size(34.dp),
            contentScale = ContentScale.Fit
        )
        Text(title, Modifier.weight(1f).padding(horizontal = 6.dp), color = Color.White, fontWeight = FontWeight.Black,
            fontSize = 15.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * الصفحة الرئيسية كما في دفتر المعلم:
 * 1) اختيار السنة الدراسية (بطاقة لكل سنة وعدد أقسامها)، 2) أقسام السنة المختارة،
 * مع زر «إنشاء قسم» العائم (الإدارة) وزر «فتح ملف قسم» (الأستاذ).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeYearsScreen(
    classes: List<ClassEntity>,            // أقسام الإدارة وأقسام الأستاذ من كل السنوات
    currentInstitutionYear: String?,
    selectedYear: String?,
    mode: String?,                         // null = لم يُختر بعد، "admin" = وضع الإدارة، "teacher" = وضع الأستاذ
    onSelectMode: (String?) -> Unit,
    institutionName: String?,
    teacherName: String?,
    onTool: (String) -> Unit,
    schoolNames: Map<String, String>,
    ownInstitutionId: String?,
    activated: Set<String>,
    genderCounts: List<ClassGenderCount>,
    onSelectYear: (String?) -> Unit,
    onOpenClass: (ClassEntity) -> Unit,
    onDeleteClass: (ClassEntity) -> Unit,
    onCreateClass: () -> Unit,
    onOpenFile: () -> Unit,
    /** الأستاذ: يُنشئ أقسامًا فقط في مؤسسة عمله الفردي. */
    canCreateClass: Boolean = true,
    onMenu: () -> Unit,
    /** الأستاذ: تقدم رصد النتائج لكل فصل، ورفعها للإدارة بعد اكتمالها. */
    termProgress: List<TeacherTermProgress> = emptyList(),
    onUploadTerm: (Int) -> Unit = {}
) {
    val current = SchoolYear.current()
    val years = remember(classes, currentInstitutionYear) {
        (classes.map { it.schoolYear } + listOfNotNull(currentInstitutionYear)).distinct().sortedDescending()
    }
    var toDelete by remember { mutableStateOf<ClassEntity?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                val modeLabel = when (mode) {
                    MODE_ADMIN -> stringResource(R.string.mode_admin)
                    MODE_TEACHER -> stringResource(R.string.mode_teacher)
                    else -> null
                }
                AppTopBar(
                    title = when {
                        selectedYear == null -> stringResource(R.string.home_choose_year_title)
                        modeLabel == null -> stringResource(R.string.home_year_title, selectedYear)
                        else -> "$modeLabel — ⁦$selectedYear⁩"
                    },
                    onMenu = onMenu,
                    onBack = when {
                        selectedYear == null -> null
                        mode != null -> ({ onSelectMode(null) })
                        else -> ({ onSelectYear(null) })
                    }
                )
                if (selectedYear == null) {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Text(stringResource(R.string.home_choose_year_prompt), fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        if (years.isEmpty()) item {
                            EmptyCard(stringResource(R.string.home_no_years), stringResource(R.string.home_no_years_desc))
                        }
                        items(years, key = { it }) { year ->
                            val count = classes.count { it.schoolYear == year }
                            Card(
                                Modifier.fillMaxWidth().clickable { onSelectYear(year) }, shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(46.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text("📚", fontSize = 22.sp) }
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.home_year_label, "⁦$year⁩"), Modifier.weight(1f, fill = false),
                                                fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            if (year == current) {
                                                Spacer(Modifier.size(8.dp))
                                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                                    Text("● " + stringResource(R.string.home_year_ongoing), Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                                }
                                            }
                                        }
                                        Text(stringResource(R.string.home_year_classes, count.toString()), fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                } else if (mode == null) {
                    // اختيار وضع العمل: الإدارة أو الأستاذ
                    val inYear = classes.filter { it.schoolYear == selectedYear }
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Text(stringResource(R.string.mode_choose), fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        item {
                            ModeCard("🏫", stringResource(R.string.mode_admin), stringResource(R.string.mode_admin_desc),
                                inYear.count { it.managed }, Color(0xFFE3F2FD), Color(0xFF1565C0)) { onSelectMode(MODE_ADMIN) }
                        }
                        item {
                            ModeCard("👨‍🏫", stringResource(R.string.mode_teacher), stringResource(R.string.mode_teacher_desc),
                                inYear.count { it.teaching }, Color(0xFFF3E5F5), Color(0xFF6A1B9A)) { onSelectMode(MODE_TEACHER) }
                        }
                        item { ModesGuide() }
                    }
                } else {
                    val yearClasses = classes.filter { it.schoolYear == selectedYear && (if (mode == MODE_ADMIN) it.managed else it.teaching) }
                        .sortedWith(compareBy({ it.levelCode }, { it.number }))
                    val ids = yearClasses.map { it.id }.toSet()
                    val counts = genderCounts.filter { it.classId in ids }
                    val total = counts.sumOf { it.count }
                    val boys = counts.filter { it.gender == Gender.BOY }.sumOf { it.count }
                    val girls = counts.filter { it.gender == Gender.GIRL }.sumOf { it.count }
                    // بطاقة صاحب الوضع (الأستاذ أو المؤسسة) وأدواته
                    val ownerCard: @Composable () -> Unit = {
                            // صاحب الوضع وأدواته: المؤسسة وأدوات الإدارة، أو الأستاذ وأدواته
                            // كل زر إما أداة مباشرة، وإما مجموعة تفتح قائمة بأدواتها
                            val tools: List<Pair<String, List<Pair<String, String>>>> = if (mode == MODE_ADMIN) listOf(
                                ("📊 " + stringResource(R.string.statistics)) to listOf("STATS" to ""),
                                ("⚙️ " + stringResource(R.string.home_group_settings)) to listOf(
                                    "INSTITUTION" to ("🏛️ " + stringResource(R.string.drawer_institution)),
                                    "ACTIVATION" to ("⭐ " + stringResource(R.string.act_title))),
                                ("🤝 " + stringResource(R.string.home_group_share)) to listOf(
                                    "SHARE_RECORDS" to ("📤 " + stringResource(R.string.sr_title)),
                                    "PRINT_RECORDS" to ("📒 " + stringResource(R.string.ps_records_title))),
                                ("📥 " + stringResource(R.string.bu_button)) to listOf("UPLOAD_MANY" to ""),
                                ("📅 " + stringResource(R.string.home_group_timetables)) to listOf(
                                    "TIMETABLE" to ("📅 " + stringResource(R.string.tt_title)),
                                    "PRINT_TIMETABLES" to ("🗓️ " + stringResource(R.string.ps_timetables_title)))
                            ) else listOf(
                                ("👤 " + stringResource(R.string.tp_title)) to listOf("TEACHER_PROFILE" to ""),
                                ("✏️ " + stringResource(R.string.remarks_settings)) to listOf("REMARKS" to "")
                            )
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text((if (mode == MODE_ADMIN) "🏫 " + (institutionName ?: "") else "👨‍🏫 " + (teacherName ?: "")),
                                        fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 2)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        tools.forEach { (label, items) ->
                                            if (items.size == 1) {
                                                AssistChip(onClick = { onTool(items[0].first) }, label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                                            } else Box {
                                                var open by remember { mutableStateOf(false) }
                                                AssistChip(onClick = { open = true }, label = { Text("$label ▾", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                                                androidx.compose.material3.DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                                    items.forEach { (key, itemLabel) ->
                                                        androidx.compose.material3.DropdownMenuItem(
                                                            text = { Text(itemLabel, fontWeight = FontWeight.Bold) },
                                                            onClick = { open = false; onTool(key) })
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                    }
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // وضع الأستاذ: معلوماته أعلى الإحصائيات
                        if (mode == MODE_TEACHER) item { ownerCard() }
                        item {
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    MiniStat("🏫", yearClasses.size, stringResource(R.string.stat_classes))
                                    MiniStat("👥", total, stringResource(R.string.stat_students))
                                    // الأستاذ: مكان الأولاد والبنات تقدم رصد النتائج أسفل البطاقة
                                    if (mode != MODE_TEACHER) {
                                        MiniStat("👦", boys, stringResource(R.string.stat_boys))
                                        MiniStat("👧", girls, stringResource(R.string.stat_girls))
                                    }
                                }
                            }
                        }
                        if (mode == MODE_TEACHER && termProgress.isNotEmpty()) item { TermProgressCard(termProgress, onUploadTerm) }
                        if (mode != MODE_TEACHER) item { ownerCard() }
                        // خطوات العمل: ماذا تفعل الإدارة، وماذا يفعل الأستاذ
                        item {
                            if (mode == MODE_ADMIN) StepsGuide(R.string.steps_admin_title, listOf(
                                R.string.steps_admin_1, R.string.steps_admin_2, R.string.steps_admin_3,
                                R.string.steps_admin_4, R.string.steps_admin_5, R.string.steps_admin_6), yearClasses.isEmpty())
                            else StepsGuide(R.string.steps_teacher_title, listOf(
                                R.string.steps_teacher_1, R.string.steps_teacher_2, R.string.steps_teacher_3,
                                R.string.steps_teacher_4, R.string.steps_teacher_5), yearClasses.isEmpty())
                        }
                        item {
                            Text(stringResource(R.string.home_year_classes_title, selectedYear, yearClasses.size.toString()),
                                fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        if (yearClasses.isEmpty()) item {
                            EmptyCard(stringResource(R.string.home_no_classes_year), stringResource(R.string.home_no_classes_year_desc))
                        }
                        items(yearClasses, key = { it.id }) { c ->
                            val students = counts.filter { it.classId == c.id }.sumOf { it.count }
                            Card(
                                Modifier.fillMaxWidth().clickable { onOpenClass(c) }, shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text(c.levelCode, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(c.name, fontWeight = FontWeight.Black, fontSize = 17.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (mode == MODE_ADMIN) {
                                                val on = c.id in activated
                                                Badge(
                                                    stringResource(if (on) R.string.act_status_active else R.string.act_status_inactive),
                                                    if (on) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                                    if (on) Color(0xFF2E7D32) else Color(0xFFE65100)
                                                )
                                            }
                                        }
                                        Text(
                                            listOfNotNull(levelDescription(c.levelCode),
                                                schoolNames[c.institutionId]?.takeIf { mode == MODE_TEACHER },
                                                stringResource(R.string.home_students_n, students.toString())).joinToString("  |  "),
                                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2
                                        )
                                    }
                                    IconButton(onClick = { toDelete = c }) {
                                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFC62828))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // الأزرار العائمة: إنشاء قسم في وضع الإدارة، وفتح ملف قسم في وضع الأستاذ
            if (selectedYear == null || mode != null) Column(
                Modifier.align(Alignment.BottomEnd).padding(16.dp),
                horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (mode != MODE_ADMIN) SmallFloatingActionButton(onClick = onOpenFile, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  " + stringResource(R.string.open_class_file), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (mode != MODE_TEACHER || canCreateClass) ExtendedFloatingActionButton(
                    onClick = onCreateClass,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.create_class), fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White
                )
            }
        }
    }
    toDelete?.let { c ->
        SensitiveConfirmDialog(
            title = stringResource(R.string.delete_class_title, c.name),
            message = stringResource(R.string.delete_class_text),
            onConfirm = { onDeleteClass(c); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

/** شرح الوضعين: لمن كل وضع، وكيف يتنقل من يجمع بينهما. */
@Composable
private fun ModesGuide() {
    var open by remember { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth().clickable { open = !open }, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💡 " + stringResource(R.string.guide_title) + if (open) "  ▲" else "  ▼",
                fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF8D6E00))
            if (open) {
                listOf(
                    R.string.guide_admin_title to R.string.guide_admin,
                    R.string.guide_teacher_title to R.string.guide_teacher,
                    R.string.guide_both_title to R.string.guide_both,
                    R.string.guide_one_title to R.string.guide_one,
                    R.string.guide_move_title to R.string.guide_move
                ).forEach { (t, b) ->
                    Text(stringResource(t), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(stringResource(b), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(stringResource(R.string.guide_short), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** بطاقة «خطوات العمل» قابلة للطي: مفتوحة ما دام لا يوجد قسم، ثم مطوية. */
@Composable
private fun StepsGuide(title: Int, steps: List<Int>, startOpen: Boolean) {
    var open by remember { mutableStateOf(startOpen) }
    Card(
        Modifier.fillMaxWidth().clickable { open = !open }, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("📘 " + stringResource(title) + if (open) "  ▲" else "  ▼", fontWeight = FontWeight.Black, fontSize = 15.sp,
                color = Color(0xFF1B5E20))
            if (open) steps.forEachIndexed { i, res ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("${i + 1}.", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32), modifier = Modifier.padding(end = 6.dp))
                    Text(stringResource(res), fontSize = 13.sp)
                }
            }
        }
    }
}

const val MODE_ADMIN = "admin"
const val MODE_TEACHER = "teacher"

/** وضع هذا التطبيق: «إدارة DS» = وضع الإدارة، «دفتر الأستاذ DS» = وضع الأستاذ. */
val APP_MODE: String get() = if (com.elyedali.daftarustadh.AppRole.isAdmin) MODE_ADMIN else MODE_TEACHER

@Composable
private fun ModeCard(icon: String, title: String, desc: String, count: Int, bg: Color, fg: Color, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) { Text(icon, fontSize = 28.sp) }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = fg)
                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.home_year_classes, count.toString()), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = fg)
        }
    }
}

@Composable
private fun MiniStat(icon: String, value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text("$value", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Badge(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(text, Modifier.padding(horizontal = 7.dp, vertical = 2.dp), color = fg, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyCard(title: String, desc: String) {
    Card(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📭", fontSize = 38.sp)
            Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(desc, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
        }
    }
}


/** تقدم رصد نتائج فصل عند الأستاذ: الأقسام المكتملة من أقسامه. */
data class TeacherTermProgress(val term: Int, val completed: List<String>, val total: Int) {
    val isDone: Boolean get() = total > 0 && completed.size >= total
}

@Composable
private fun TermProgressCard(progress: List<TeacherTermProgress>, onUpload: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📊 " + stringResource(R.string.tprog_title), fontWeight = FontWeight.Black, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary)
            if (progress.first().total == 0)
                Text(stringResource(R.string.tprog_no_subjects), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else progress.forEach { p ->
                val done = p.isDone
                val color = if (done) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.tprog_term, p.term), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.tprog_count, p.completed.size, p.total) + if (done) "  ✓" else "",
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
                    }
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { p.completed.size.toFloat() / p.total },
                        modifier = Modifier.fillMaxWidth().height(6.dp), color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant)
                    if (p.completed.isNotEmpty() && !done)
                        Text(stringResource(R.string.tprog_done_list, p.completed.joinToString("، ")), fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (done) Button(onClick = { onUpload(p.term) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White)) {
                        Text("📤 " + stringResource(R.string.tprog_upload, p.term), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
