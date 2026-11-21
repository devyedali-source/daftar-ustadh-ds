package com.elyedali.daftarustadh.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.BookPack
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.SchoolYear
import com.elyedali.daftarustadh.data.SubjectNames

/** حجم مقروء: 12.3 ميغا */
@Composable
fun megaBytes(bytes: Long): String = stringResource(R.string.bk_mb, String.format(java.util.Locale.US, "%.1f", bytes / 1_000_000.0))

/** اسم مادة كتاب (من المنهاج)، بلغة تدريسها كما في باقي التطبيق. */
fun bookSubjectName(context: android.content.Context, subject: String, level: String, isFr: Boolean): String {
    val years = CurriculumRepository.availableYears(context)
    val year = SchoolYear.current().let { cur -> years.lastOrNull { it <= cur } ?: years.firstOrNull() } ?: return subject
    val s = CurriculumRepository.level(context, year, level)?.subjects?.firstOrNull { it.code == subject }
        ?: CurriculumRepository.levels(context, year).flatMap { it.subjects }.firstOrNull { it.code == subject }
    return s?.let { SubjectNames.display(it, isFr) } ?: subject
}

/** المواد بترتيب المنهاج (التربية الإسلامية، العربية، الفرنسية…) لا بالترتيب الأبجدي. */
private fun bookSubjectOrder(context: android.content.Context, packs: List<BookPack.Pack>): List<String> {
    val years = CurriculumRepository.availableYears(context)
    val year = SchoolYear.current().let { cur -> years.lastOrNull { it <= cur } ?: years.firstOrNull() }
    val order = year?.let { y -> CurriculumRepository.levels(context, y).flatMap { l -> l.subjects.map { it.code } }.distinct() }.orEmpty()
    return packs.map { it.subject }.distinct().sortedBy { order.indexOf(it).let { i -> if (i < 0) Int.MAX_VALUE else i } }
}

/**
 * «كتبي على الهاتف»: يختار الأستاذ موادّه (حتى 3) ومستوياته فلا يُنزَّل إلا كتبها،
 * ويرى حالة كل كتاب ويحذف ما لا يحتاجه. تظهر وحدها عند أول دخول، وتبقى في القائمة.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BooksScreen(firstTime: Boolean, isFr: Boolean, onDone: () -> Unit, onMenu: (() -> Unit)?) {
    val context = LocalContext.current
    val packs = remember { BookPack.packs(context) }
    val states by BookPack.states.collectAsState()
    var subjects by remember { mutableStateOf(BookPack.pickedSubjects(context).ifEmpty { favSubjects(context) }) }
    var levels by remember { mutableStateOf(BookPack.pickedLevels(context).ifEmpty { favLevels(context) }) }
    var selected by remember { mutableStateOf(BookPack.selected(context)) }
    var toRemove by remember { mutableStateOf<BookPack.Pack?>(null) }
    val confirmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {}

    // ترتيب المواد كما في المنهاج، والمستويات حسب المرحلة
    val subjectOrder = remember { bookSubjectOrder(context, packs) }
    val levelOrder = remember { packs.flatMap { it.levels }.distinct().sortedWith(compareBy({ it.firstOrNull { c -> c.isDigit() } }, { it })) }
    val names = remember(isFr) { subjectOrder.associateWith { s -> bookSubjectName(context, s, packs.first { it.subject == s }.level, isFr) } }
    val testBuild = states.values.any { it is BookPack.Status.NotInTestBuild }

    val wanted = packs.filter { it.subject in subjects && it.serves(levels) }
    val toDownload = wanted.filter { states[it.name] !is BookPack.Status.Ready }
    val onPhone = packs.filter { it.name in selected || states[it.name] !is BookPack.Status.NotDownloaded && states[it.name] !is BookPack.Status.NotInTestBuild && states[it.name] != null }
        .sortedWith(compareBy({ levelOrder.indexOf(it.levels.first()) }, { subjectOrder.indexOf(it.subject) }))

    toRemove?.let { p ->
        AlertDialog(onDismissRequest = { toRemove = null },
            title = { Text(stringResource(R.string.bk_remove_title)) },
            text = { Text(stringResource(R.string.bk_remove_body, names[p.subject].orEmpty() + " — " + p.levelsLabel)) },
            confirmButton = { TextButton(onClick = { BookPack.remove(context, p.name); selected = BookPack.selected(context); toRemove = null }) {
                Text(stringResource(R.string.bk_remove_yes), color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { toRemove = null }) { Text(stringResource(R.string.cancel)) } })
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            if (onMenu != null) AppTopBar(title = "📚 " + stringResource(R.string.bk_title), onMenu = onMenu, onBack = onDone)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (onMenu == null) item {
                    Text("📚 " + stringResource(R.string.bk_title), fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
                item { Text(stringResource(R.string.bk_intro, BookPack.MAX_SUBJECTS), fontSize = 13.5.sp) }
                if (testBuild) item { Note(stringResource(R.string.books_test_build)) }

                item { Text(stringResource(R.string.bk_my_subjects, subjects.size, BookPack.MAX_SUBJECTS), fontWeight = FontWeight.Bold) }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        subjectOrder.forEach { s ->
                            val on = s in subjects
                            FilterChip(selected = on, enabled = on || subjects.size < BookPack.MAX_SUBJECTS,
                                onClick = { subjects = if (on) subjects - s else subjects + s },
                                label = { Text(names[s].orEmpty()) })
                        }
                    }
                }
                item { Text(stringResource(R.string.bk_my_levels), fontWeight = FontWeight.Bold) }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        levelOrder.forEach { l ->
                            val on = l in levels
                            FilterChip(selected = on, onClick = { levels = if (on) levels - l else levels + l }, label = { Text(l) })
                        }
                    }
                }
                item {
                    Text(if (toDownload.isEmpty() && wanted.isNotEmpty()) stringResource(R.string.bk_all_ready)
                        else stringResource(R.string.bk_will_download, toDownload.size, megaBytes(toDownload.sumOf { it.bytes })),
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Button(onClick = {
                        BookPack.choose(context, subjects, levels)
                        selected = BookPack.selected(context)
                        syncFavorites(context, subjects, levels)
                        if (firstTime) onDone()
                    }, enabled = subjects.isNotEmpty() && levels.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                        Text("📥 " + stringResource(if (firstTime) R.string.bk_save_first else R.string.bk_save), fontWeight = FontWeight.Bold)
                    }
                }
                if (firstTime) item {
                    TextButton(onClick = { BookPack.markPicked(context); onDone() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.bk_later))
                    }
                }

                if (onPhone.isNotEmpty()) {
                    item { HorizontalDivider() }
                    item { Text(stringResource(R.string.bk_on_phone), fontWeight = FontWeight.Bold) }
                    items(onPhone, key = { it.name }) { p ->
                        BookRow(names[p.subject].orEmpty() + " — " + p.levelsLabel, megaBytes(p.bytes), states[p.name] ?: BookPack.Status.NotDownloaded,
                            onRetry = { BookPack.download(context, p.name) },
                            onConfirm = { BookPack.confirm(confirmLauncher) },
                            onRemove = { toRemove = p })
                    }
                }
            }
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(text, Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp), fontSize = 13.sp)
}

@Composable
private fun BookRow(title: String, size: String, st: BookPack.Status, onRetry: () -> Unit, onConfirm: () -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(size, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BookStatusLine(st, onRetry, onConfirm)
            if (st is BookPack.Status.Ready) TextButton(onClick = onRemove, contentPadding = PaddingValues(0.dp)) {
                Text("🗑 " + stringResource(R.string.bk_remove), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
    }
}

/** حالة كتاب واحد: جاهز، يُنزَّل بنسبة، ينتظر، يحتاج موافقة، أو سيُستأنف. يُستعمل هنا وفي صفحة الدرس. */
@Composable
fun BookStatusLine(st: BookPack.Status, onDownload: () -> Unit, onConfirm: () -> Unit) {
    when (st) {
        is BookPack.Status.Ready -> Text("✅ " + stringResource(R.string.bk_ready), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        is BookPack.Status.Downloading -> {
            Text("📥 " + stringResource(R.string.bk_downloading, st.percent), fontSize = 13.sp)
            LinearProgressIndicator(progress = { st.percent / 100f }, modifier = Modifier.fillMaxWidth())
        }
        is BookPack.Status.Pending -> {
            Text("⌛ " + stringResource(R.string.bk_pending), fontSize = 13.sp)
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        is BookPack.Status.NeedsConfirmation -> {
            Text("📶 " + stringResource(R.string.bk_needs_confirm), fontSize = 13.sp)
            OutlinedButton(onClick = onConfirm) { Text(stringResource(R.string.bk_confirm)) }
        }
        is BookPack.Status.Failed -> Text("⚠️ " + stringResource(R.string.bk_failed), fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
        is BookPack.Status.NotInTestBuild -> Text(stringResource(R.string.books_test_build), fontSize = 13.sp)
        is BookPack.Status.NotDownloaded -> OutlinedButton(onClick = onDownload) { Text("📥 " + stringResource(R.string.bk_download_this)) }
    }
}

private fun favSubjects(c: android.content.Context): Set<String> =
    c.getSharedPreferences("planning_order", android.content.Context.MODE_PRIVATE).getStringSet("fav_subjects", emptySet()).orEmpty()
        .take(BookPack.MAX_SUBJECTS).toSet()

private fun favLevels(c: android.content.Context): Set<String> =
    c.getSharedPreferences("planning_order", android.content.Context.MODE_PRIVATE).getStringSet("fav_levels", emptySet()).orEmpty().toSet()

/** مواد الكتب ومستوياتها تصير «موادي» و«مستوياتي» في التخطيط إن لم يكن الأستاذ اختارها هناك. */
private fun syncFavorites(c: android.content.Context, subjects: Set<String>, levels: Set<String>) {
    val p = c.getSharedPreferences("planning_order", android.content.Context.MODE_PRIVATE)
    val e = p.edit()
    if (p.getStringSet("fav_subjects", emptySet()).isNullOrEmpty()) e.putStringSet("fav_subjects", subjects)
    if (p.getStringSet("fav_levels", emptySet()).isNullOrEmpty()) e.putStringSet("fav_levels", levels)
    e.apply()
}
