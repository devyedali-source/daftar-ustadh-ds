package com.elyedali.daftarustadh

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.elyedali.daftarustadh.data.AppMetaEntity
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.RemarkScale
import com.elyedali.daftarustadh.data.TeacherSubjectEntity
import com.elyedali.daftarustadh.data.SubjectRecordEntity
import com.elyedali.daftarustadh.data.GradeSlot
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import com.elyedali.daftarustadh.data.TestsMode
import com.elyedali.daftarustadh.data.SchoolDirectory
import com.elyedali.daftarustadh.pdf.TeacherRecordPdf
import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.transfer.ClassFile
import com.elyedali.daftarustadh.transfer.ClassFileCodec
import com.elyedali.daftarustadh.transfer.ClassFileImporter
import com.elyedali.daftarustadh.transfer.ClassFileType
import com.elyedali.daftarustadh.transfer.ImportOutcome
import com.elyedali.daftarustadh.transfer.ShareHelper
import com.elyedali.daftarustadh.transfer.RecordCheck
import com.elyedali.daftarustadh.transfer.RecordFile
import com.elyedali.daftarustadh.transfer.RecordImporter
import com.elyedali.daftarustadh.transfer.TransferCodec
import com.elyedali.daftarustadh.ui.ResultsStatusScreen
import com.elyedali.daftarustadh.ui.StatsScreen
import com.elyedali.daftarustadh.ui.AllClassesScreen
import com.elyedali.daftarustadh.ui.ClassHub
import com.elyedali.daftarustadh.ui.ComingSoon
import com.elyedali.daftarustadh.ui.DocumentsTab
import com.elyedali.daftarustadh.ui.KashfTab
import com.elyedali.daftarustadh.data.DocumentEntity
import com.elyedali.daftarustadh.pdf.DocumentPdf
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableIntStateOf
import com.elyedali.daftarustadh.ui.KashfSettingsScreen
import com.elyedali.daftarustadh.report.KashfSettings
import com.elyedali.daftarustadh.report.ReportCalc
import com.elyedali.daftarustadh.pdf.ReportCardPdf
import com.elyedali.daftarustadh.data.Stage
import com.elyedali.daftarustadh.ui.ClassSubjectsDialog
import com.elyedali.daftarustadh.ui.ClassesScreen
import com.elyedali.daftarustadh.ui.DaftarTheme
import com.elyedali.daftarustadh.ui.GradesScreen
import com.elyedali.daftarustadh.ui.HomeScreen
import com.elyedali.daftarustadh.ui.ImportStudentsScreen
import com.elyedali.daftarustadh.ui.InstitutionScreen
import com.elyedali.daftarustadh.ui.RemarksSettingsScreen
import com.elyedali.daftarustadh.ui.StudentsScreen
import com.elyedali.daftarustadh.ui.TeacherClassScreen
import com.elyedali.daftarustadh.ui.TeachingClassesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TEACHER_NAME_KEY = "teacher_name"

private enum class Screen { HOME, INSTITUTION, CLASSES, TEACHING, TEACHER_CLASS, STUDENTS, IMPORT, GRADES, REMARKS, RESULTS, KASHF_SETTINGS, GENERAL_REMARKS, STATS,
    ALL_CLASSES, ADMIN_CLASS, TEACHER_HUB, LEGISLATION, EXCHANGES, BOOKS }

/** نتيجة فتح ملف قسم، تُعرض في نافذة. */
private sealed class FileResult {
    data object Invalid : FileResult()
    data class Done(val outcome: ImportOutcome) : FileResult()
    data class Message(val text: String) : FileResult()
}

class MainActivity : AppCompatActivity() {

    /** ملف قسم فُتح من الواتساب أو من مدير الملفات، ينتظر المعالجة. */
    private val incomingFile = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // العربية هي اللغة الافتراضية عند أول تشغيل
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
        }
        if (savedInstanceState == null) handleIntent(intent)

        val db = DaftarDatabase.get(this)
        val institutionDao = db.institutionDao()
        val classDao = db.classDao()
        val studentDao = db.studentDao()
        val gradeDao = db.gradeDao()
        val documentDao = db.documentDao()
        val metaDao = db.appMetaDao()
        val importer = ClassFileImporter(db)
        // بيانات تجريبية (نسخ التجربة فقط الآن؛ وبعد تسجيل الدخول لحساب صاحب التطبيق فقط)
        lifecycleScope.launch(Dispatchers.IO) { com.elyedali.daftarustadh.data.DemoData.seedIfNeeded(this@MainActivity, db) }
        val recordImporter = RecordImporter(db)

        setContent {
            DaftarTheme {
                val scope = rememberCoroutineScope()
                val institution by institutionDao.observe().collectAsState(initial = null)
                val institutions by institutionDao.observeAll().collectAsState(initial = emptyList())
                val schoolNames = institutions.associate { it.id to it.schoolName }
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                var classId by rememberSaveable { mutableStateOf<String?>(null) }
                var fromTeaching by rememberSaveable { mutableStateOf(false) }
                var showSubjects by remember { mutableStateOf(false) }
                var fileResult by remember { mutableStateOf<FileResult?>(null) }
                val inst = institution

                val managedFlow = remember(inst?.schoolYear) {
                    if (inst == null) flowOf(emptyList()) else classDao.observeManaged(inst.schoolYear)
                }
                val managedClasses by managedFlow.collectAsState(initial = emptyList())
                val teachingClasses by remember { classDao.observeTeaching() }.collectAsState(initial = emptyList())
                val currentClassFlow = remember(classId) { classId?.let { classDao.observeById(it) } ?: flowOf(null) }
                val currentClass: ClassEntity? by currentClassFlow.collectAsState(initial = null)
                val studentsFlow = remember(classId) { classId?.let { studentDao.observeByClass(it) } ?: flowOf(emptyList()) }
                val students by studentsFlow.collectAsState(initial = emptyList())
                var subjectCode by rememberSaveable { mutableStateOf<String?>(null) }
                var gradesFromAdmin by rememberSaveable { mutableStateOf(false) }
                var gradesBack by rememberSaveable { mutableStateOf(Screen.TEACHER_HUB) }
                var hubTab by rememberSaveable { mutableIntStateOf(0) }
                var hubBack by rememberSaveable { mutableStateOf(Screen.ALL_CLASSES) }
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val documentsFlow = remember(classId) { classId?.let { documentDao.observeByClass(it) } ?: flowOf(emptyList()) }
                val documents by documentsFlow.collectAsState(initial = emptyList())
                var recordCheck by remember { mutableStateOf<RecordCheck.Ready?>(null) }
                val recordsFlow = remember(classId) { classId?.let { gradeDao.observeRecords(it) } ?: flowOf(emptyList()) }
                val records by recordsFlow.collectAsState(initial = emptyList())
                val classGradesFlow = remember(classId) { classId?.let { gradeDao.observeClass(it) } ?: flowOf(emptyList()) }
                val classGrades by classGradesFlow.collectAsState(initial = emptyList())
                val mySubjectsFlow = remember(classId) { classId?.let { gradeDao.observeTeacherSubjects(it) } ?: flowOf(emptyList()) }
                val mySubjects by mySubjectsFlow.collectAsState(initial = emptyList())
                val remarksText by remember { metaDao.observe(RemarkScale.KEY) }.collectAsState(initial = null)
                val remarkScale = remember(remarksText) { RemarkScale.decode(remarksText) }
                val teacherName by remember { metaDao.observe(TEACHER_NAME_KEY) }.collectAsState(initial = null)
                var askNameFor by remember { mutableStateOf<String?>(null) }
                val thresholdsText by remember { metaDao.observe(KashfSettings.THRESHOLDS_KEY) }.collectAsState(initial = null)
                val thresholds = remember(thresholdsText) { KashfSettings.decodeThresholds(thresholdsText) }
                val generalText by remember { metaDao.observe(KashfSettings.GENERAL_REMARKS_KEY) }.collectAsState(initial = null)
                val generalRemarks = remember(generalText) {
                    if (generalText.isNullOrBlank()) KashfSettings.DEFAULT_GENERAL_REMARKS else RemarkScale.decode(generalText)
                }

                /** إنتاج كشوف الدرجات لقسم: ملف PDF واحد، كشف في كل صفحة، يُرسل للوراقة. */
                fun generateKashf(c: ClassEntity, term: Int) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val subjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                        val list = studentDao.listByClass(c.id)
                        val reports = ReportCalc.build(
                            term, subjects, list, classGrades, records,
                            mySubjects.associate { it.subjectCode to it.testsMode }
                        )
                        val info = ReportCardPdf.Info(
                            schoolYear = c.schoolYear,
                            schoolName = owner.schoolName,
                            className = c.name,
                            passThreshold = thresholds[c.levelCode] ?: KashfSettings.DEFAULT_THRESHOLD,
                            subjectRemarks = remarkScale,
                            generalRemarks = generalRemarks,
                            date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date()),
                            seal = android.graphics.BitmapFactory.decodeResource(resources, R.drawable.img_mauritania_seal)
                        )
                        val bytes = withContext(Dispatchers.Default) { ReportCardPdf.build(term, reports, info) }
                        val termAr = when (term) { 1 -> "الفصل الأول"; 2 -> "الفصل الثاني"; else -> "السنوية" }
                        val termFr = when (term) { 1 -> "T1"; 2 -> "T2"; else -> "annuels" }
                        val name = getString(R.string.kashf_file_name, c.name, termAr, termFr, c.schoolYear)
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, name, getString(R.string.kashf_share), "application/pdf")
                    }
                }
                var askNameSends by remember { mutableStateOf(false) }
                var askNameBlank by remember { mutableStateOf(false) }
                var askNameDoc by remember { mutableStateOf<DocumentEntity?>(null) }

                /** سحب وثيقة الأستاذ PDF برأسية المؤسسة وطابعه. */
                fun exportDocument(d: DocumentEntity, name: String) {
                    scope.launch {
                        val c = classDao.getById(d.classId) ?: return@launch
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val subjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                            .filter { s -> mySubjects.any { it.subjectCode == s.code } }
                        val stamp = listOf(name) +
                            (if (subjects.isEmpty()) emptyList() else listOf(
                                "أستاذ مادة: " + subjects.joinToString("، ") { it.nameAr },
                                "Professeur de : " + subjects.joinToString(", ") { it.nameFr }
                            ))
                        val info = DocumentPdf.Info(
                            infoLineAr = "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — القسم: \u2066${c.name}\u2069 — السنة الدراسية: \u2066${c.schoolYear}\u2069",
                            infoLineFr = "Établissement : ${owner.schoolName} — Classe : ${c.name} — Année scolaire : ${c.schoolYear}",
                            title = d.title,
                            body = d.body,
                            stampLines = stamp,
                            seal = android.graphics.BitmapFactory.decodeResource(resources, R.drawable.img_mauritania_seal)
                        )
                        val bytes = withContext(Dispatchers.Default) { DocumentPdf.build(info) }
                        val fileName = ("${d.title.ifBlank { "Document" }} - ${c.name} - ${c.schoolYear}.pdf")
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, fileName, getString(R.string.doc_pdf), "application/pdf")
                    }
                }
                var sendConfirm by remember { mutableStateOf<String?>(null) }

                /** إرسال سجل الأستاذ لمادة: ملف ‎.dsq يحمل نتائجه وطابعه، يُرفع عند المكلّف بالقسم. */
                fun sendRecord(c: ClassEntity, code: String, name: String) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val subject = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects?.firstOrNull { it.code == code } ?: return@launch
                        val list = studentDao.listByClass(c.id)
                        val g = gradeDao.listSubject(c.id, code)
                        val mode = mySubjects.firstOrNull { it.subjectCode == code }?.testsMode ?: TestsMode.AVERAGE
                        val record = RecordFile(
                            institutionId = owner.id, wilaya = owner.wilaya, schoolName = owner.schoolName,
                            schoolListId = owner.schoolListId, classId = c.id, schoolYear = c.schoolYear,
                            levelCode = c.levelCode, number = c.number, subjectCode = code,
                            subjectNameAr = subject.nameAr, subjectNameFr = subject.nameFr,
                            teacherName = name, testsMode = mode,
                            students = TransferCodec.buildRecordStudents(list, g)
                        )
                        val bytes = withContext(Dispatchers.Default) { TransferCodec.encodeRecord(record) }
                        ShareHelper.share(this@MainActivity, bytes, TransferCodec.recordFileName(record), getString(R.string.share_record))
                    }
                }

                /** سحب سجل الأستاذ لمادة: PDF فارغ الخانات، يُرسل أو يُطبع من نافذة المشاركة. */
                fun printRecord(c: ClassEntity, code: String, name: String, blank: Boolean = false) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val subject = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects?.firstOrNull { it.code == code } ?: return@launch
                        val list = if (blank) TeacherRecordPdf.blankRows(70) else studentDao.listByClass(c.id)
                        // سجل الأستاذ ثنائي اللغة دائمًا (عربي وفرنسي)، مثل الكشوف الرسمية، أيًّا كانت لغة الواجهة
                        val wilaya = SchoolDirectory.wilaya(this@MainActivity, owner.wilaya)
                        val labels = TeacherRecordPdf.Labels(
                            title = "سجل نتائج مادة: ${subject.nameAr} — Relevé de notes : ${subject.nameFr}",
                            subtitle = "القسم: \u2066${c.name}\u2069 — السنة الدراسية: \u2066${c.schoolYear}\u2069 — الضارب: ${subject.coefficient}" +
                                " — الولاية: ${owner.wilaya} — المؤسسة: ${owner.schoolName}",
                            schoolLine = "Classe : ${c.name} — Année scolaire : ${c.schoolYear} — Coef. ${subject.coefficient}" +
                                (wilaya?.let { " — Wilaya : ${it.nameFr}" } ?: "") + " — Établissement : ${owner.schoolName}",
                            rollHeader = "ر.ن\nN°",
                            nameHeader = "الاسم الكامل\nNom et Prénom",
                            gradeHeaders = (1..3).flatMap { t ->
                                listOf("اختبار ف$t\nDevoir T$t", "امتحان ف$t\nCompo T$t")
                            },
                            signature = "توقيع الأستاذ — Signature du professeur",
                            stampLines = listOf(name, "أستاذ مادة: ${subject.nameAr}", "Professeur de : ${subject.nameFr}"),
                            page = { p, n -> "صفحة $p من $n — Page $p / $n" },
                            seal = android.graphics.BitmapFactory.decodeResource(resources, R.drawable.img_mauritania_seal)
                        )
                        val bytes = withContext(Dispatchers.Default) { TeacherRecordPdf.build(list, labels) }
                        val fileName = getString(R.string.rec_file_name, subject.nameAr, c.name, subject.nameFr, c.schoolYear)
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, fileName, getString(R.string.share_record), "application/pdf")
                    }
                }

                // فتح ملف قسم: من الواتساب أو من زر «فتح ملف قسم»
                fun processFile(uri: Uri) {
                    scope.launch {
                        val result: FileResult? = try {
                            val bytes = withContext(Dispatchers.IO) {
                                contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            } ?: throw IllegalStateException()
                            when (val any = TransferCodec.decodeAny(bytes)) {
                                is RecordFile -> when (val chk = recordImporter.check(any)) {
                                    is RecordCheck.Ready -> { recordCheck = chk; null }
                                    is RecordCheck.WrongClass -> FileResult.Message(
                                        getString(R.string.record_wrong_class, chk.file.schoolName, chk.file.className, chk.file.wilaya, chk.file.schoolYear)
                                    )
                                    is RecordCheck.NotManaged -> FileResult.Message(getString(R.string.record_not_managed, chk.file.className))
                                }
                                is ClassFile -> FileResult.Done(importer.save(any))
                                else -> FileResult.Invalid
                            }
                        } catch (e: Exception) {
                            FileResult.Invalid
                        }
                        if (result != null) fileResult = result
                        if (result is FileResult.Done && result.outcome is ImportOutcome.Success) {
                            screen = if ((result.outcome as ImportOutcome.Success).type == ClassFileType.TEACHER)
                                Screen.TEACHING else Screen.CLASSES
                        }
                    }
                }
                val pendingUri by incomingFile
                LaunchedEffect(pendingUri) {
                    pendingUri?.let { processFile(it); incomingFile.value = null }
                }
                val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri != null) processFile(uri)
                }
                val openFile = { filePicker.launch(arrayOf("*/*")) }

                val chooserTitle = stringResource(R.string.share_chooser)
                fun share(c: ClassEntity, type: ClassFileType) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val list = studentDao.listByClass(c.id)
                        val file = ClassFile(type, owner, c, list)
                        val bytes = withContext(Dispatchers.Default) { ClassFileCodec.encode(file) }
                        ShareHelper.share(this@MainActivity, bytes, ClassFileCodec.fileName(file), chooserTitle)
                    }
                }

                when (screen) {
                    Screen.HOME -> {}
                    Screen.STUDENTS -> BackHandler { screen = if (fromTeaching) Screen.TEACHER_CLASS else Screen.CLASSES }
                    Screen.TEACHER_CLASS -> BackHandler { screen = Screen.TEACHING }
                    Screen.GRADES -> BackHandler { screen = gradesBack }
                    Screen.RESULTS -> BackHandler { screen = Screen.STUDENTS }
                    Screen.STATS -> BackHandler { screen = Screen.HOME }
                    Screen.KASHF_SETTINGS -> BackHandler { screen = Screen.ADMIN_CLASS }
                    Screen.GENERAL_REMARKS -> BackHandler { screen = Screen.KASHF_SETTINGS }
                    Screen.IMPORT -> BackHandler { screen = Screen.ADMIN_CLASS }
                    Screen.ADMIN_CLASS, Screen.TEACHER_HUB -> BackHandler { screen = hubBack }
                    Screen.ALL_CLASSES, Screen.LEGISLATION, Screen.EXCHANGES, Screen.BOOKS -> BackHandler { screen = Screen.HOME }
                    else -> BackHandler { screen = Screen.HOME }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = screen == Screen.HOME || drawerState.isOpen,
                    drawerContent = {
                        ModalDrawerSheet {
                            androidx.compose.material3.Text(
                                stringResource(R.string.app_name),
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                modifier = androidx.compose.ui.Modifier.padding(20.dp)
                            )
                            listOf(
                                Screen.HOME to R.string.menu_home,
                                Screen.ALL_CLASSES to R.string.classes_title,
                                Screen.LEGISLATION to R.string.menu_legislation,
                                Screen.EXCHANGES to R.string.menu_exchanges,
                                Screen.BOOKS to R.string.menu_books
                            ).forEach { (target, res) ->
                                NavigationDrawerItem(
                                    label = { androidx.compose.material3.Text(stringResource(res)) },
                                    selected = screen == target,
                                    onClick = {
                                        if (target == Screen.BOOKS) openKoutoubi() else screen = target
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                ) {
                when {
                    screen == Screen.INSTITUTION -> InstitutionScreen(
                        existing = inst,
                        onSave = { scope.launch { institutionDao.save(it); screen = Screen.HOME } },
                        onCancel = { screen = Screen.HOME }
                    )

                    screen == Screen.CLASSES && inst != null -> ClassesScreen(
                        institution = inst,
                        classes = managedClasses,
                        schoolNames = schoolNames,
                        onCreate = { level, number, onResult ->
                            scope.launch {
                                val rowId = classDao.insert(
                                    ClassEntity(
                                        id = UUID.randomUUID().toString(),
                                        institutionId = inst.id,
                                        schoolYear = inst.schoolYear,
                                        levelCode = level,
                                        number = number,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                onResult(rowId != -1L)
                            }
                        },
                        onOpen = { c -> classId = c.id; fromTeaching = false; hubTab = 0; hubBack = Screen.CLASSES; screen = Screen.ADMIN_CLASS },
                        onDelete = { c ->
                            scope.launch {
                                gradeDao.deleteByClass(c.id)
                                gradeDao.deleteRecordsByClass(c.id)
                                documentDao.deleteByClass(c.id)
                                gradeDao.deleteTeacherSubjectsByClass(c.id)
                                studentDao.deleteByClass(c.id)
                                classDao.delete(c)
                            }
                        },
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.TEACHING -> TeachingClassesScreen(
                        classes = teachingClasses,
                        schoolNames = schoolNames,
                        onOpenFile = openFile,
                        onOpen = { c -> classId = c.id; fromTeaching = true; hubTab = 0; hubBack = Screen.TEACHING; screen = Screen.TEACHER_HUB },
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.ALL_CLASSES -> AllClassesScreen(
                        managed = managedClasses,
                        teaching = teachingClasses,
                        schoolNames = schoolNames,
                        hasInstitution = inst != null,
                        onOpenManaged = { c -> classId = c.id; fromTeaching = false; hubTab = 0; hubBack = Screen.ALL_CLASSES; screen = Screen.ADMIN_CLASS },
                        onOpenTeaching = { c -> classId = c.id; fromTeaching = true; hubTab = 0; hubBack = Screen.ALL_CLASSES; screen = Screen.TEACHER_HUB },
                        onManageClasses = { screen = Screen.CLASSES },
                        onOpenFile = openFile,
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.LEGISLATION -> com.elyedali.daftarustadh.ui.LegislationScreen(
                        title = stringResource(R.string.menu_legislation),
                        pdfAssetPath = "legislation/nusus_tashri3_madrasi.pdf",
                        onSharePdf = { file ->
                            ShareHelper.share(this@MainActivity, file.readBytes(), "نصوص مرجعية في التشريع المدرسي.pdf",
                                getString(R.string.share), "application/pdf")
                        },
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.EXCHANGES -> {
                        val myAdText by remember { metaDao.observe(EXCHANGE_AD_KEY) }.collectAsState(initial = null)
                        val myAd = remember(myAdText) {
                            myAdText?.let { runCatching { com.elyedali.daftarustadh.exchange.ExchangeAd.fromJson(org.json.JSONObject(it)) }.getOrNull() }
                        }
                        var ownerId by remember { mutableStateOf<String?>(null) }
                        LaunchedEffect(Unit) {
                            ownerId = metaDao.get(EXCHANGE_OWNER_KEY) ?: UUID.randomUUID().toString().also {
                                metaDao.put(AppMetaEntity(EXCHANGE_OWNER_KEY, it))
                            }
                        }
                        val isFr = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "fr"
                        val wilayaNames = remember { com.elyedali.daftarustadh.data.SchoolDirectory.wilayas(this@MainActivity).map { it.name } }
                        val specialtyOptions = remember(isFr) {
                            val order = listOf("IR", "AR", "HG", "PH", "IC", "FR", "ANG", "MATS", "SN", "PC", "TI", "LEG", "EPS")
                            val year = CurriculumRepository.availableYears(this@MainActivity).lastOrNull()
                            val subjects = if (year == null) emptyList()
                                else CurriculumRepository.levels(this@MainActivity, year).flatMap { it.subjects }.distinctBy { it.code }
                            subjects.sortedBy { order.indexOf(it.code).let { i -> if (i < 0) 99 else i } }
                                .map { com.elyedali.daftarustadh.ui.SpecialtyOption(it.code, if (isFr) it.nameFr else it.nameAr) }
                        }
                        val owner = ownerId
                        if (owner != null) com.elyedali.daftarustadh.ui.ExchangesScreen(
                            title = stringResource(R.string.menu_exchanges),
                            wilayas = wilayaNames,
                            specialties = specialtyOptions,
                            myAd = myAd,
                            ownerId = owner,
                            repository = com.elyedali.daftarustadh.exchange.NotConnectedExchangeRepository,
                            onSaveMine = { ad -> metaDao.put(AppMetaEntity(EXCHANGE_AD_KEY, ad?.toJson()?.toString() ?: "")) },
                            onBack = { screen = Screen.HOME }
                        )
                    }

                    screen == Screen.ADMIN_CLASS && currentClass != null -> {
                        val c = currentClass!!
                        val subjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                        ClassHub(
                            title = c.name,
                            subtitles = listOfNotNull(schoolNames[c.institutionId], c.schoolYear),
                            tabs = listOf(stringResource(R.string.tab_students), stringResource(R.string.tab_results),
                                stringResource(R.string.tab_kashf), stringResource(R.string.tab_class_timetable)),
                            selected = hubTab,
                            onSelect = { hubTab = it },
                            onBack = { screen = hubBack }
                        ) {
                            when (hubTab) {
                                0 -> {
                                    StudentsScreen(
                                        schoolClass = c,
                                        students = students,
                                        onSave = { s -> scope.launch { studentDao.save(s) } },
                                        onDelete = { s -> scope.launch { gradeDao.deleteByStudent(s.id); studentDao.delete(s) } },
                                        onImport = { screen = Screen.IMPORT },
                                        onShowSubjects = { showSubjects = true },
                                        onBack = { screen = hubBack },
                                        onShare = { type -> share(c, type) },
                                        showHeader = false
                                    )
                                    if (showSubjects) ClassSubjectsDialog(c, onPick = { code ->
                                        subjectCode = code; gradesFromAdmin = true; gradesBack = Screen.ADMIN_CLASS; showSubjects = false; screen = Screen.GRADES
                                    }) { showSubjects = false }
                                }
                                1 -> ResultsStatusScreen(
                                    schoolClass = c,
                                    subjects = subjects,
                                    students = students,
                                    grades = classGrades,
                                    records = records,
                                    onUploadRecord = openFile,
                                    onOpenSubject = { code ->
                                        subjectCode = code; gradesFromAdmin = true; gradesBack = Screen.ADMIN_CLASS; screen = Screen.GRADES
                                    },
                                    onGenerate = { term -> generateKashf(c, term) },
                                    onOpenSettings = { screen = Screen.KASHF_SETTINGS },
                                    onBack = { screen = hubBack },
                                    showHeader = false,
                                    showGenerate = false
                                )
                                2 -> {
                                    val missing = (1..3).associateWith { t -> ReportCalc.missing(t, subjects, students, classGrades).size }
                                    KashfTab(
                                        missingByTerm = if (students.isEmpty()) mapOf(1 to -1, 2 to -1, 3 to -1).mapValues { 1 } else missing,
                                        onGenerate = { term -> generateKashf(c, term) },
                                        onOpenSettings = { screen = Screen.KASHF_SETTINGS }
                                    )
                                }
                                else -> ComingSoon(stringResource(R.string.coming_soon))
                            }
                        }
                    }

                    screen == Screen.TEACHER_HUB && currentClass != null -> {
                        val c = currentClass!!
                        ClassHub(
                            title = c.name,
                            subtitles = listOfNotNull(schoolNames[c.institutionId], c.schoolYear),
                            tabs = listOf(stringResource(R.string.tab_students), stringResource(R.string.tab_results),
                                stringResource(R.string.tab_teacher_timetable), stringResource(R.string.tab_editor)),
                            selected = hubTab,
                            onSelect = { hubTab = it },
                            onBack = { screen = hubBack }
                        ) {
                            when (hubTab) {
                                0 -> StudentsScreen(
                                    schoolClass = c,
                                    students = students,
                                    onSave = {}, onDelete = {}, onImport = {},
                                    onShowSubjects = {},
                                    onBack = { screen = hubBack },
                                    readOnly = true,
                                    showHeader = false
                                )
                                1 -> TeacherClassScreen(
                                    schoolClass = c,
                                    schoolName = schoolNames[c.institutionId],
                                    classSubjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty(),
                                    mySubjectCodes = mySubjects.map { it.subjectCode }.toSet(),
                                    grades = classGrades,
                                    studentCount = students.size,
                                    onSetMySubjects = { chosen ->
                                        scope.launch {
                                            val current = mySubjects.map { it.subjectCode }.toSet()
                                            (current - chosen).forEach { gradeDao.removeTeacherSubject(c.id, it) }
                                            (chosen - current).forEach { gradeDao.saveTeacherSubject(TeacherSubjectEntity(c.id, it)) }
                                        }
                                    },
                                    onOpenSubject = { code -> subjectCode = code; gradesFromAdmin = false; gradesBack = Screen.TEACHER_HUB; screen = Screen.GRADES },
                                    onShowStudents = { hubTab = 0 },
                                    onBack = { screen = hubBack },
                                    showHeader = false
                                )
                                2 -> ComingSoon(stringResource(R.string.coming_soon))
                                else -> DocumentsTab(
                                    classId = c.id,
                                    documents = documents,
                                    onSave = { d -> scope.launch { documentDao.save(d) } },
                                    onDelete = { d -> scope.launch { documentDao.delete(d) } },
                                    onExport = { d ->
                                        val n = teacherName
                                        if (n.isNullOrBlank()) { askNameDoc = d; askNameFor = "" } else exportDocument(d, n)
                                    }
                                )
                            }
                        }
                    }

                    screen == Screen.STUDENTS && currentClass != null -> {
                        val c = currentClass!!
                        val readOnly = fromTeaching
                        StudentsScreen(
                            schoolClass = c,
                            students = students,
                            onSave = { s -> scope.launch { studentDao.save(s) } },
                            onDelete = { s -> scope.launch { gradeDao.deleteByStudent(s.id); studentDao.delete(s) } },
                            onImport = { screen = Screen.IMPORT },
                            onShowSubjects = { showSubjects = true },
                            onBack = { screen = if (fromTeaching) Screen.TEACHER_CLASS else Screen.CLASSES },
                            onShare = if (readOnly) null else { type -> share(c, type) },
                            readOnly = readOnly,
                            subtitle = schoolNames[c.institutionId],
                            onOpenResults = if (readOnly) null else { { screen = Screen.RESULTS } }
                        )
                        if (showSubjects) ClassSubjectsDialog(
                            c,
                            onPick = if (readOnly) null else { code ->
                                subjectCode = code; gradesFromAdmin = true; gradesBack = Screen.STUDENTS; showSubjects = false; screen = Screen.GRADES
                            }
                        ) { showSubjects = false }
                    }

                    screen == Screen.TEACHER_CLASS && currentClass != null -> {
                        val c = currentClass!!
                        TeacherClassScreen(
                            schoolClass = c,
                            schoolName = schoolNames[c.institutionId],
                            classSubjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty(),
                            mySubjectCodes = mySubjects.map { it.subjectCode }.toSet(),
                            grades = classGrades,
                            studentCount = students.size,
                            onSetMySubjects = { chosen ->
                                scope.launch {
                                    val current = mySubjects.map { it.subjectCode }.toSet()
                                    (current - chosen).forEach { gradeDao.removeTeacherSubject(c.id, it) }
                                    (chosen - current).forEach { gradeDao.saveTeacherSubject(TeacherSubjectEntity(c.id, it)) }
                                }
                            },
                            onOpenSubject = { code -> subjectCode = code; gradesFromAdmin = false; gradesBack = Screen.TEACHER_CLASS; screen = Screen.GRADES },
                            onShowStudents = { screen = Screen.STUDENTS },
                            onBack = { screen = Screen.TEACHING }
                        )
                    }

                    screen == Screen.GRADES && currentClass != null && subjectCode != null -> {
                        val c = currentClass!!
                        val code = subjectCode!!
                        val subject = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects?.firstOrNull { it.code == code }
                        val back = { screen = gradesBack }
                        if (subject == null) LaunchedEffect(Unit) { back() } else GradesScreen(
                            schoolClass = c,
                            subject = subject,
                            students = students,
                            grades = classGrades.filter { it.subjectCode == code },
                            testsMode = (if (gradesFromAdmin) records.firstOrNull { it.subjectCode == code }?.testsMode else null)
                                ?: mySubjects.firstOrNull { it.subjectCode == code }?.testsMode ?: TestsMode.AVERAGE,
                            scale = remarkScale,
                            onSetGrade = { studentId, slot, value ->
                                scope.launch {
                                    if (value == null) gradeDao.clear(studentId, code, slot)
                                    else gradeDao.save(GradeEntity(studentId, c.id, code, slot, value, System.currentTimeMillis()))
                                }
                            },
                            onTestsMode = { mode ->
                                scope.launch {
                                    if (gradesFromAdmin) {
                                        val old = gradeDao.getRecord(c.id, code)
                                        gradeDao.saveRecord(SubjectRecordEntity(c.id, code, old?.teacherName ?: "", mode, old?.receivedAt ?: System.currentTimeMillis()))
                                    } else gradeDao.saveTeacherSubject(TeacherSubjectEntity(c.id, code, mode))
                                }
                            },
                            onSendRecord = if (gradesFromAdmin) null else {
                                {
                                    val n = teacherName
                                    if (n.isNullOrBlank()) { askNameFor = code; askNameSends = true } else sendConfirm = code
                                }
                            },
                            onPrintRecord = { blank ->
                                val n = teacherName
                                askNameBlank = blank
                                if (n.isNullOrBlank()) askNameFor = code else printRecord(c, code, n, blank)
                            },
                            onBack = back
                        )
                    }

                    screen == Screen.RESULTS && currentClass != null -> {
                        val c = currentClass!!
                        ResultsStatusScreen(
                            schoolClass = c,
                            subjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty(),
                            students = students,
                            grades = classGrades,
                            records = records,
                            onUploadRecord = openFile,
                            onOpenSubject = { code ->
                                subjectCode = code; gradesFromAdmin = true; gradesBack = Screen.RESULTS; screen = Screen.GRADES
                            },
                            onGenerate = { term -> generateKashf(c, term) },
                            onOpenSettings = { screen = Screen.KASHF_SETTINGS },
                            onBack = { screen = Screen.STUDENTS }
                        )
                    }

                    screen == Screen.STATS && inst != null -> {
                        val genderCounts by remember { studentDao.observeGenderCounts() }.collectAsState(initial = emptyList())
                        StatsScreen(
                            schoolName = inst.schoolName,
                            schoolYear = inst.schoolYear,
                            classes = managedClasses,
                            genderCounts = genderCounts,
                            onBack = { screen = Screen.HOME }
                        )
                    }

                    screen == Screen.KASHF_SETTINGS -> {
                        val ownLevels = inst?.let { i ->
                            val stages = when (i.stage) { Stage.COLLEGE -> listOf(Stage.COLLEGE); Stage.LYCEE -> listOf(Stage.LYCEE); else -> listOf(Stage.COLLEGE, Stage.LYCEE) }
                            stages.flatMap { st -> CurriculumRepository.creatableLevels(this@MainActivity, i.schoolYear, st) }.map { it.code }
                        }.orEmpty()
                        val levels = (ownLevels + managedClasses.map { it.levelCode }).distinct()
                        KashfSettingsScreen(
                            levels = levels,
                            thresholds = thresholds,
                            onSave = { map ->
                                scope.launch {
                                    metaDao.put(AppMetaEntity(KashfSettings.THRESHOLDS_KEY, KashfSettings.encodeThresholds(thresholds + map)))
                                    screen = Screen.ADMIN_CLASS
                                }
                            },
                            onEditGeneralRemarks = { screen = Screen.GENERAL_REMARKS },
                            onBack = { screen = Screen.ADMIN_CLASS }
                        )
                    }

                    screen == Screen.GENERAL_REMARKS -> RemarksSettingsScreen(
                        scale = generalRemarks,
                        teacherName = null,
                        onSave = { list, _ ->
                            scope.launch {
                                metaDao.put(AppMetaEntity(KashfSettings.GENERAL_REMARKS_KEY, RemarkScale.encode(list)))
                                screen = Screen.KASHF_SETTINGS
                            }
                        },
                        title = stringResource(R.string.general_remarks),
                        defaults = KashfSettings.DEFAULT_GENERAL_REMARKS,
                        onBack = { screen = Screen.KASHF_SETTINGS }
                    )

                    screen == Screen.REMARKS -> RemarksSettingsScreen(
                        scale = remarkScale,
                        teacherName = teacherName.orEmpty(),
                        onSave = { list, name ->
                            scope.launch {
                                metaDao.put(AppMetaEntity(RemarkScale.KEY, RemarkScale.encode(list)))
                                metaDao.put(AppMetaEntity(TEACHER_NAME_KEY, name))
                                screen = Screen.HOME
                            }
                        },
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.IMPORT && currentClass != null -> ImportStudentsScreen(
                        schoolClass = currentClass!!,
                        existing = students,
                        onConfirm = { list -> scope.launch { studentDao.saveAll(list); screen = Screen.ADMIN_CLASS } },
                        onBack = { screen = Screen.ADMIN_CLASS }
                    )

                    else -> HomeScreen(
                        institution = inst,
                        onRegisterInstitution = { screen = Screen.INSTITUTION },
                        onOpenClasses = { screen = Screen.CLASSES },
                        onOpenTeaching = { screen = Screen.TEACHING },
                        onOpenFile = openFile,
                        onOpenRemarks = { screen = Screen.REMARKS },
                        onOpenStats = { screen = Screen.STATS },
                        onOpenMenu = { scope.launch { drawerState.open() } },
                        onLanguageSelected = { tag ->
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }
                    )
                }

                }

                askNameFor?.let { code ->
                    var typed by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { askNameFor = null; askNameSends = false },
                        title = { Text(stringResource(R.string.teacher_name)) },
                        text = {
                            androidx.compose.foundation.layout.Column {
                                Text(stringResource(R.string.teacher_name_needed))
                                OutlinedTextField(typed, { typed = it }, singleLine = true)
                            }
                        },
                        confirmButton = {
                            TextButton(enabled = typed.isNotBlank(), onClick = {
                                val n = typed.trim()
                                askNameFor = null
                                scope.launch { metaDao.put(AppMetaEntity(TEACHER_NAME_KEY, n)) }
                                val doc = askNameDoc
                                when {
                                    doc != null -> { askNameDoc = null; exportDocument(doc, n) }
                                    askNameSends -> { askNameSends = false; sendConfirm = code }
                                    else -> currentClass?.let { printRecord(it, code, n, askNameBlank) }
                                }
                            }) { Text(stringResource(R.string.save)) }
                        },
                        dismissButton = { TextButton(onClick = { askNameFor = null }) { Text(stringResource(R.string.cancel)) } }
                    )
                }

                sendConfirm?.let { code ->
                    val c = currentClass
                    val total = students.size * GradeSlot.ALL.size
                    val filled = classGrades.count { it.subjectCode == code }
                    val n = teacherName
                    if (c == null || n.isNullOrBlank()) {
                        LaunchedEffect(code) { sendConfirm = null }
                    } else if (filled >= total) {
                        LaunchedEffect(code) { sendConfirm = null; sendRecord(c, code, n) }
                    } else {
                        AlertDialog(
                            onDismissRequest = { sendConfirm = null },
                            text = { Text(stringResource(R.string.send_record_incomplete, filled, total)) },
                            confirmButton = { TextButton(onClick = { sendConfirm = null; sendRecord(c, code, n) }) { Text(stringResource(R.string.send)) } },
                            dismissButton = { TextButton(onClick = { sendConfirm = null }) { Text(stringResource(R.string.cancel)) } }
                        )
                    }
                }

                recordCheck?.let { chk ->
                    val isFr = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "fr"
                    val subjectName = if (isFr) chk.file.subjectNameFr else chk.file.subjectNameAr
                    AlertDialog(
                        onDismissRequest = { recordCheck = null },
                        title = { Text(stringResource(R.string.record_check_title, subjectName, chk.file.className)) },
                        text = {
                            androidx.compose.foundation.layout.Column(
                                Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                            ) {
                                Text(stringResource(R.string.record_teacher, chk.file.teacherName))
                                Text(stringResource(R.string.record_matched, chk.matched))
                                if (chk.unknownStudents.isNotEmpty())
                                    Text(stringResource(R.string.record_unknown, chk.unknownStudents.joinToString("، ")), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                                if (chk.missingStudents.isNotEmpty())
                                    Text(stringResource(R.string.record_missing, chk.missingStudents.joinToString("، ")), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                                if (chk.alreadyUploaded != null || chk.hasGrades)
                                    Text(stringResource(R.string.record_replace))
                            }
                        },
                        confirmButton = {
                            TextButton(enabled = chk.matched > 0, onClick = {
                                recordCheck = null
                                scope.launch {
                                    recordImporter.apply(chk)
                                    classId = chk.schoolClass.id
                                    fromTeaching = false
                                    hubTab = 1
                                    screen = Screen.ADMIN_CLASS
                                    fileResult = FileResult.Message(getString(R.string.record_done, subjectName, chk.file.className))
                                }
                            }) { Text(stringResource(R.string.record_upload)) }
                        },
                        dismissButton = { TextButton(onClick = { recordCheck = null }) { Text(stringResource(R.string.cancel)) } }
                    )
                }

                fileResult?.let { r ->
                    val message = when (r) {
                        FileResult.Invalid -> stringResource(R.string.file_invalid)
                        is FileResult.Message -> r.text
                        is FileResult.Done -> when (val o = r.outcome) {
                            is ImportOutcome.ConflictingSource -> stringResource(R.string.file_conflict, o.className)
                            is ImportOutcome.Success ->
                                stringResource(if (o.type == ClassFileType.TEACHER) R.string.file_ok_teacher else R.string.file_ok_full, o.className) +
                                    "\n" + stringResource(R.string.file_counts, o.added, o.updated)
                        }
                    }
                    AlertDialog(
                        onDismissRequest = { fileResult = null },
                        text = { Text(message) },
                        confirmButton = { TextButton(onClick = { fileResult = null }) { Text(stringResource(R.string.ok)) } }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri: Uri? = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND ->
                if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else @Suppress("DEPRECATION") intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            else -> null
        }
        if (uri != null) incomingFile.value = uri
    }

    /** «كتبي»: يفتح موقع كتبي في متصفح الهاتف خارج التطبيق. */
    private fun openKoutoubi() {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(KOUTOUBI_URL))
        try {
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(this, getString(R.string.no_browser), android.widget.Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val KOUTOUBI_URL = "https://koutoubi.mr/"
        const val EXCHANGE_AD_KEY = "exchange_my_ad"
        const val EXCHANGE_OWNER_KEY = "exchange_owner_id"
    }
}
