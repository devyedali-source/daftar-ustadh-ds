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
import com.elyedali.daftarustadh.data.AppMetaEntity
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.RemarkScale
import com.elyedali.daftarustadh.data.TeacherSubjectEntity
import com.elyedali.daftarustadh.data.SubjectRecordEntity
import com.elyedali.daftarustadh.data.GradeSlot
import androidx.compose.ui.Modifier
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

private enum class Screen { HOME, INSTITUTION, CLASSES, TEACHING, TEACHER_CLASS, STUDENTS, IMPORT, GRADES, REMARKS, RESULTS }

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
        val metaDao = db.appMetaDao()
        val importer = ClassFileImporter(db)
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
                var gradesBack by rememberSaveable { mutableStateOf(Screen.TEACHER_CLASS) }
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
                var askNameSends by remember { mutableStateOf(false) }
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
                fun printRecord(c: ClassEntity, code: String, name: String) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val subject = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects?.firstOrNull { it.code == code } ?: return@launch
                        val list = studentDao.listByClass(c.id)
                        val isFr = resources.configuration.locales[0].language == "fr"
                        val subjectName = if (isFr) subject.nameFr else subject.nameAr
                        val wilaya = SchoolDirectory.wilaya(this@MainActivity, owner.wilaya)
                        val labels = TeacherRecordPdf.Labels(
                            title = getString(R.string.rec_title, subjectName),
                            subtitle = getString(R.string.rec_subtitle, c.name, c.schoolYear, subject.coefficient),
                            schoolLine = getString(R.string.rec_school_line, if (isFr && wilaya != null) wilaya.nameFr else owner.wilaya, owner.schoolName),
                            rollHeader = getString(R.string.rec_roll),
                            nameHeader = getString(R.string.rec_name),
                            gradeHeaders = (1..3).flatMap { t -> listOf(getString(R.string.rec_test, t), getString(R.string.rec_exam, t)) },
                            signature = getString(R.string.rec_signature),
                            stampLines = listOf(name, getString(R.string.rec_stamp_subject, subjectName)),
                            page = { p, n -> getString(R.string.rec_page, p, n) },
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
                    Screen.IMPORT -> BackHandler { screen = Screen.STUDENTS }
                    else -> BackHandler { screen = Screen.HOME }
                }

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
                        onOpen = { c -> classId = c.id; fromTeaching = false; screen = Screen.STUDENTS },
                        onDelete = { c ->
                            scope.launch {
                                gradeDao.deleteByClass(c.id)
                                gradeDao.deleteRecordsByClass(c.id)
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
                        onOpen = { c -> classId = c.id; fromTeaching = true; screen = Screen.TEACHER_CLASS },
                        onBack = { screen = Screen.HOME }
                    )

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
                            onPrintRecord = {
                                val n = teacherName
                                if (n.isNullOrBlank()) askNameFor = code else printRecord(c, code, n)
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
                            onBack = { screen = Screen.STUDENTS }
                        )
                    }

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
                        onConfirm = { list -> scope.launch { studentDao.saveAll(list); screen = Screen.STUDENTS } },
                        onBack = { screen = Screen.STUDENTS }
                    )

                    else -> HomeScreen(
                        institution = inst,
                        onRegisterInstitution = { screen = Screen.INSTITUTION },
                        onOpenClasses = { screen = Screen.CLASSES },
                        onOpenTeaching = { screen = Screen.TEACHING },
                        onOpenFile = openFile,
                        onOpenRemarks = { screen = Screen.REMARKS },
                        onLanguageSelected = { tag ->
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }
                    )
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
                                if (askNameSends) { askNameSends = false; sendConfirm = code }
                                else currentClass?.let { printRecord(it, code, n) }
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
                                    screen = Screen.RESULTS
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
}
