package com.elyedali.daftarustadh

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
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
import com.elyedali.daftarustadh.data.GradeMath
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.elyedali.daftarustadh.ui.AbsenceTab
import com.elyedali.daftarustadh.ui.AbsenceSubject
import com.elyedali.daftarustadh.ui.TextbookTab
import com.elyedali.daftarustadh.ui.HomeYearsScreen
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
/** العربية بأرقام لاتينية. */
private const val AR_LATN = "ar-u-nu-latn"
/** بصمة الجدول الزمني المعتمد. */
private const val TT_APPROVED_KEY = "timetable_approved"

private enum class Screen { HOME, INSTITUTION, CLASSES, TEACHING, TEACHER_CLASS, STUDENTS, IMPORT, GRADES, REMARKS, RESULTS, KASHF_SETTINGS, GENERAL_REMARKS, STATS,
    ALL_CLASSES, ADMIN_CLASS, TEACHER_HUB, LEGISLATION, EXCHANGES, BOOKS, ACTIVATION, TIMETABLE, MY_TIMETABLE, TEACHER_PROFILE, SHARE_RECORDS, PRINT_SHOP, ABSENCE_CENTER }

/** نتيجة فتح ملف قسم، تُعرض في نافذة. */
/** فتح ملف PDF بزر «فتح ملف»: ملفات PDF للطباعة فقط، والتطبيق يفتح ملفات ‎.dsq. */
private class PdfNotImportableException : Exception()

private sealed class FileResult {
    data object Invalid : FileResult()
    data class Done(val outcome: ImportOutcome) : FileResult()
    data class Message(val text: String) : FileResult()
}

class MainActivity : AppCompatActivity() {

    /** شعار الجمهورية في رأسية الوثائق: يُحمَّل مرة واحدة. */
    private val sealBitmap: android.graphics.Bitmap? by lazy {
        android.graphics.BitmapFactory.decodeResource(resources, R.drawable.img_mauritania_seal)
    }

    /** ملف قسم فُتح من الواتساب أو من مدير الملفات، ينتظر المعالجة. */
    private val incomingFile = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // العربية هي اللغة الافتراضية عند أول تشغيل، بأرقام عادية (1 2 3) لا (١ ٢ ٣) في كل التطبيق
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty || locales.toLanguageTags() == "ar") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(AR_LATN))
        }
        if (savedInstanceState == null) handleIntent(intent)

        val db = DaftarDatabase.get(this)
        val institutionDao = db.institutionDao()
        val classDao = db.classDao()
        val studentDao = db.studentDao()
        val gradeDao = db.gradeDao()
        val documentDao = db.documentDao()
        val absenceDao = db.absenceDao()
        val timetableDao = db.timetableDao()
        val absenceImporter = com.elyedali.daftarustadh.transfer.AbsenceImporter(db)
        val metaDao = db.appMetaDao()
        val importer = ClassFileImporter(db)
        // بيانات تجريبية (نسخ التجربة فقط الآن؛ وبعد تسجيل الدخول لحساب صاحب التطبيق فقط)
        lifecycleScope.launch(Dispatchers.IO) { com.elyedali.daftarustadh.data.DemoData.seedIfNeeded(this@MainActivity, db) }
        val recordImporter = RecordImporter(db)
        val bundleImporter = com.elyedali.daftarustadh.transfer.TeacherBundleImporter(db)

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
                val isFrUi = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "fr"

                val teachingClasses by remember { classDao.observeTeaching() }.collectAsState(initial = emptyList())
                val allClasses by remember { classDao.observeAll() }.collectAsState(initial = emptyList())
                val homeGenderCounts by remember { studentDao.observeGenderCounts() }.collectAsState(initial = emptyList())
                var selectedYear by rememberSaveable { mutableStateOf<String?>(null) }
                var homeMode by rememberSaveable { mutableStateOf<String?>(null) }  // admin / teacher
                /** سنة عمل الإدارة: السنة المختارة في وضع الإدارة، وإلا سنة المؤسسة. كل أدوات الإدارة تعمل عليها. */
                val adminYear: String? = selectedYear?.takeIf { homeMode == com.elyedali.daftarustadh.ui.MODE_ADMIN } ?: inst?.schoolYear
                val managedFlow = remember(inst?.id, adminYear) {
                    if (inst == null || adminYear == null) flowOf(emptyList()) else classDao.observeManaged(adminYear)
                }
                val managedClasses by managedFlow.collectAsState(initial = emptyList())
                var showCreateClass by remember { mutableStateOf(false) }
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
                var absenceCheck by remember { mutableStateOf<com.elyedali.daftarustadh.transfer.AbsenceCheck.Ready?>(null) }
                val absenceSessionsFlow = remember(classId) { classId?.let { absenceDao.observeSessions(it) } ?: flowOf(emptyList()) }
                val absenceSessions by absenceSessionsFlow.collectAsState(initial = emptyList())
                val absencesFlow = remember(classId) { classId?.let { absenceDao.observeAbsences(it) } ?: flowOf(emptyList()) }
                val absences by absencesFlow.collectAsState(initial = emptyList())
                // مركز الغياب: كل أقسام الإدارة
                val managedIds = managedClasses.map { it.id }
                val allStudents by remember(managedIds) { studentDao.observeByClasses(managedIds) }.collectAsState(initial = emptyList())
                val allAbsSessions by remember(managedIds) { absenceDao.observeSessionsOf(managedIds) }.collectAsState(initial = emptyList())
                val allAbsences by remember(managedIds) { absenceDao.observeAbsencesOf(managedIds) }.collectAsState(initial = emptyList())
                val recordsFlow = remember(classId) { classId?.let { gradeDao.observeRecords(it) } ?: flowOf(emptyList()) }
                val records by recordsFlow.collectAsState(initial = emptyList())
                val classGradesFlow = remember(classId) { classId?.let { gradeDao.observeClass(it) } ?: flowOf(emptyList()) }
                val classGrades by classGradesFlow.collectAsState(initial = emptyList())
                val mySubjectsFlow = remember(classId) { classId?.let { gradeDao.observeTeacherSubjects(it) } ?: flowOf(emptyList()) }
                val mySubjects by mySubjectsFlow.collectAsState(initial = emptyList())
                val remarksText by remember { metaDao.observe(RemarkScale.KEY) }.collectAsState(initial = null)
                val remarkScale = remember(remarksText) { RemarkScale.decode(remarksText) }
                val teacherName by remember { metaDao.observe(TEACHER_NAME_KEY) }.collectAsState(initial = null)
                val teacherProfileText by remember { metaDao.observe(com.elyedali.daftarustadh.ui.TeacherProfile.KEY) }.collectAsState(initial = null)
                val teacherProfile = com.elyedali.daftarustadh.ui.TeacherProfile.decode(teacherProfileText)
                val activatedText by remember { metaDao.observe(com.elyedali.daftarustadh.data.ClassActivation.KEY) }.collectAsState(initial = null)
                val activatedClasses = remember(activatedText) { com.elyedali.daftarustadh.data.ClassActivation.decode(activatedText) }
                val requestedText by remember { metaDao.observe(com.elyedali.daftarustadh.data.ClassActivation.REQUESTED_KEY) }.collectAsState(initial = null)
                val requestedClasses = remember(requestedText) { com.elyedali.daftarustadh.data.ClassActivation.decode(requestedText) }
                var activationBack by rememberSaveable { mutableStateOf(Screen.HOME) }
                val ttSettingsText by remember { metaDao.observe(com.elyedali.daftarustadh.data.TimetableSettings.KEY) }.collectAsState(initial = null)
                val ttSettings = remember(ttSettingsText) { com.elyedali.daftarustadh.data.TimetableSettings.decode(ttSettingsText) }
                val ttAssignments by remember { timetableDao.observeAssignments() }.collectAsState(initial = emptyList())
                val ttTeachersText by remember { metaDao.observe(com.elyedali.daftarustadh.data.TimetableTeachers.KEY) }.collectAsState(initial = null)
                val ttTeachers = remember(ttTeachersText) { com.elyedali.daftarustadh.data.TimetableTeachers.decode(ttTeachersText) }
                val ttSlots by remember { timetableDao.observeSlots() }.collectAsState(initial = emptyList())
                val ttApprovedSig by remember { metaDao.observe(TT_APPROVED_KEY) }.collectAsState(initial = null)
                val myTimetable by remember { timetableDao.observeTeacherTimetable() }.collectAsState(initial = emptyList())
                var ttGenerating by remember { mutableStateOf(false) }
                var ttResult by remember { mutableStateOf<String?>(null) }
                var ttInitialClass by rememberSaveable { mutableStateOf<String?>(null) }
                var ttInitialTab by remember { mutableStateOf<Int?>(null) }
                var recordsSent by remember { mutableStateOf(setOf<String>()) }
                var printWorking by remember { mutableStateOf(false) }
                var ttBack by rememberSaveable { mutableStateOf(Screen.HOME) }
                var askNameFor by remember { mutableStateOf<String?>(null) }
                val thresholdsText by remember { metaDao.observe(KashfSettings.THRESHOLDS_KEY) }.collectAsState(initial = null)
                val thresholds = remember(thresholdsText) { KashfSettings.decodeThresholds(thresholdsText) }
                val generalText by remember { metaDao.observe(KashfSettings.GENERAL_REMARKS_KEY) }.collectAsState(initial = null)
                val generalRemarks = remember(generalText) {
                    if (generalText.isNullOrBlank()) KashfSettings.DEFAULT_GENERAL_REMARKS else RemarkScale.decode(generalText)
                }

                /** حذف قسم وكل ما يتعلق به (بعد إدخال 9999). */
                fun deleteClass(c: ClassEntity) {
                    scope.launch {
                        gradeDao.deleteByClass(c.id)
                        gradeDao.deleteRecordsByClass(c.id)
                        documentDao.deleteByClass(c.id)
                        gradeDao.deleteTeacherSubjectsByClass(c.id)
                        studentDao.deleteByClass(c.id)
                        absenceDao.deleteAbsencesByClass(c.id)
                        absenceDao.deleteSessionsByClass(c.id)
                        timetableDao.deleteAssignmentsByClass(c.id)
                        timetableDao.deleteSlotsByClass(c.id)
                        classDao.delete(c)
                    }
                }

                /** إنتاج كشوف الدرجات لقسم: ملف PDF واحد، كشف في كل صفحة، يُرسل للوراقة. */
                fun generateKashf(c: ClassEntity, term: Int) {
                    // الفصل الأول مجاني؛ كشف الفصل الثاني والسنوي يحتاجان تفعيل القسم
                    if (com.elyedali.daftarustadh.data.ClassActivation.needsActivation(term) && c.id !in activatedClasses) {
                        activationBack = screen; screen = Screen.ACTIVATION
                        return
                    }
                    // لا كشف قبل اكتمال النتائج (حماية إضافية لما يعرضه التبويب)
                    val subjectsNow = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                    if (students.isEmpty() || com.elyedali.daftarustadh.report.ReportCalc.missing(term, subjectsNow, students, classGrades).isNotEmpty()) {
                        android.widget.Toast.makeText(this@MainActivity, getString(R.string.kashf_blocked), android.widget.Toast.LENGTH_LONG).show()
                        return
                    }
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
                            seal = sealBitmap
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

                var docSubjectChoice by remember { mutableStateOf<Pair<DocumentEntity, String>?>(null) }

                /**
                 * سحب وثيقة الأستاذ PDF: بلا رأسية ولا شعار، فقط المؤسسة والسنة والقسم (والمادة في الاختبار)،
                 * والتوقيع وطابعه أسفل الصفحة. إن كان للأستاذ عدة مواد في القسم يختار مادة الاختبار أولًا.
                 */
                fun exportDocument(d: DocumentEntity, name: String, examSubject: String? = null) {
                    val mineCodes = mySubjects.map { it.subjectCode }
                    if (d.type == com.elyedali.daftarustadh.data.DocType.EXAM && examSubject == null && mineCodes.size > 1) {
                        docSubjectChoice = d to name
                        return
                    }
                    scope.launch {
                        val c = classDao.getById(d.classId) ?: return@launch
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val all = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                        val subjects = all.filter { s -> s.code in mineCodes }
                        val examSub = if (d.type == com.elyedali.daftarustadh.data.DocType.EXAM)
                            all.firstOrNull { it.code == (examSubject ?: mineCodes.singleOrNull()) } else null
                        val stamp = listOf(name) +
                            (if (subjects.isEmpty()) emptyList() else listOf(
                                "أستاذ مادة: " + subjects.joinToString("، ") { it.nameAr },
                                "Professeur de : " + subjects.joinToString(", ") { it.nameFr }
                            ))
                        val info = DocumentPdf.Info(
                            infoLineAr = "المؤسسة: ${owner.schoolName} — السنة الدراسية: \u2066${c.schoolYear}\u2069 — القسم: \u2066${c.name}\u2069",
                            infoLineFr = "Établissement : ${owner.schoolName} — Année scolaire : ${c.schoolYear} — Classe : ${c.name}",
                            subjectLine = examSub?.let { "المادة: ${it.nameAr} — Matière : ${it.nameFr}" },
                            title = d.title,
                            body = d.body,
                            stampLines = stamp
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

                /** إرسال غياب مادة الأستاذ في القسم إلى الإدارة (ملف ‎.dsq عبر الواتساب). */
                fun sendAbsence(c: ClassEntity, code: String, name: String) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val subject = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects?.firstOrNull { it.code == code } ?: return@launch
                        val sessions = absenceDao.listSessions(c.id, code).map { s ->
                            com.elyedali.daftarustadh.transfer.AbsenceFileSession(
                                s.date, s.period, absenceDao.listAbsences(s.id).map { it.studentId }
                            )
                        }
                        val file = com.elyedali.daftarustadh.transfer.AbsenceFile(
                            institutionId = owner.id, wilaya = owner.wilaya, schoolName = owner.schoolName,
                            schoolListId = owner.schoolListId, classId = c.id, schoolYear = c.schoolYear,
                            levelCode = c.levelCode, number = c.number, subjectCode = code,
                            subjectNameAr = subject.nameAr, subjectNameFr = subject.nameFr,
                            teacherName = name, sessions = sessions
                        )
                        val bytes = withContext(Dispatchers.Default) { TransferCodec.encodeAbsence(file) }
                        ShareHelper.share(this@MainActivity, bytes, file.fileName(), getString(R.string.abs_share_title))
                    }
                }
                var askNameAbsence by remember { mutableStateOf(false) }

                fun subjectName(code: String): String {
                    val all = CurriculumRepository.availableYears(this@MainActivity).lastOrNull()?.let { y ->
                        CurriculumRepository.levels(this@MainActivity, y).flatMap { it.subjects }
                    }.orEmpty()
                    val s = all.firstOrNull { it.code == code } ?: return code
                    return if (isFrUi) s.nameFr else s.nameAr
                }

                /** توزيع أقسام كل مادة على أساتذتها بالعدل (حسب قائمة الأساتذة ومواد كل واحد). */
                suspend fun distributeTeachers() {
                    val classSubjects = managedClasses.map { c ->
                        Triple(c.id, c.levelCode, CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects.orEmpty().map { it.code to it.weeklyHours })
                    }
                    val result = com.elyedali.daftarustadh.data.TimetableTeachers.distribute(ttTeachers, classSubjects)
                    val covered = ttTeachers.flatMap { it.subjects }.toSet()
                    classSubjects.forEach { (cid, _, subs) ->
                        subs.filter { it.first in covered }.forEach { (code, _) -> timetableDao.deleteAssignment(cid, code) }
                    }
                    result.forEach { timetableDao.saveAssignment(it) }
                    // الحصص الموجودة تأخذ الأستاذ الجديد
                    val byKey = result.associateBy { it.classId to it.subjectCode }
                    timetableDao.saveSlots(ttSlots.mapNotNull { sl -> byKey[sl.classId to sl.subjectCode]?.let { sl.copy(teacherName = it.teacherName) } })
                }

                /** حفظ قائمة الأساتذة؛ تغيير اسم أستاذ يُنقل إلى تعييناته وجدوله، وحذفه يحذف تعييناته. */
                fun saveTeachers(newList: List<com.elyedali.daftarustadh.data.TimetableTeacher>) {
                    val old = ttTeachers
                    scope.launch {
                        metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.data.TimetableTeachers.KEY,
                            com.elyedali.daftarustadh.data.TimetableTeachers.encode(newList)))
                        val removed = old.map { it.name } - newList.map { it.name }.toSet()
                        val added = newList.map { it.name } - old.map { it.name }.toSet()
                        if (old.size == newList.size && removed.size == 1 && added.size == 1) {
                            val from = removed.first(); val to = added.first()
                            ttAssignments.filter { it.teacherName == from }.forEach { timetableDao.saveAssignment(it.copy(teacherName = to)) }
                            timetableDao.saveSlots(ttSlots.filter { it.teacherName == from }.map { it.copy(teacherName = to) })
                        } else {
                            ttAssignments.filter { it.teacherName in removed }.forEach { timetableDao.deleteAssignment(it.classId, it.subjectCode) }
                            timetableDao.saveSlots(ttSlots.filter { it.teacherName in removed }.map { it.copy(teacherName = "") })
                        }
                    }
                }

                /** توليد الجدول الزمني لكل أقسام الإدارة في السنة الحالية. */
                fun generateTimetable() {
                    val classesNow = managedClasses
                    if (classesNow.isEmpty()) return
                    ttGenerating = true
                    scope.launch {
                        // أول توليد: إن لم توزَّع الأقسام بعد على الأساتذة، تُوزَّع تلقائيًا
                        if (ttAssignments.none { a -> classesNow.any { it.id == a.classId } } && ttTeachers.isNotEmpty()) distributeTeachers()
                        val assignmentsNow = timetableDao.listAssignments()
                        val settings = ttSettings
                        val lessons = classesNow.flatMap { c ->
                            CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty().map { sub ->
                                com.elyedali.daftarustadh.timetable.TimetableGenerator.Lesson(
                                    c.id, sub.code,
                                    assignmentsNow.firstOrNull { it.classId == c.id && it.subjectCode == sub.code }?.teacherName.orEmpty(),
                                    sub.weeklyHours
                                )
                            }
                        }
                        val result = withContext(Dispatchers.Default) {
                            com.elyedali.daftarustadh.timetable.TimetableGenerator.generate(lessons, settings.days.size, settings.periods,
                                periodsPerDay = settings.days.map { settings.periodsOn(it) },
                                doubleStarts = settings.doubleStarts())
                        }
                        val newSlots = result.placed.map {
                            com.elyedali.daftarustadh.data.TimetableSlotEntity(it.classId, settings.days[it.day], it.period, it.subjectCode, it.teacher)
                        }
                        timetableDao.replaceSlots(classesNow.map { it.id }, newSlots)
                        val names = classesNow.associate { it.id to it.name }
                        ttResult = buildString {
                            append(getString(R.string.tt_result_ok, newSlots.size, result.teacherGaps))
                            if (result.unplaced.isNotEmpty()) {
                                append('\n').append(getString(R.string.tt_result_unplaced,
                                    result.unplaced.joinToString("، ") { "${names[it.classId]} ${subjectName(it.subjectCode)} (${it.hours})" }))
                            }
                        }
                        ttGenerating = false
                    }
                }

                fun swapSlots(classId: String, a: Pair<Int, Int>, b: Pair<Int, Int>): String? {
                    val placed = ttSlots.map { com.elyedali.daftarustadh.timetable.TimetableGenerator.Placed(it.classId, it.day, it.period, it.subjectCode, it.teacherName) }
                    com.elyedali.daftarustadh.timetable.TimetableGenerator.canSwap(placed, classId, a, b)?.let { return it }
                    val sa = ttSlots.firstOrNull { it.classId == classId && it.day == a.first && it.period == a.second }
                    val sb = ttSlots.firstOrNull { it.classId == classId && it.day == b.first && it.period == b.second }
                    scope.launch {
                        timetableDao.deleteSlot(classId, a.first, a.second)
                        timetableDao.deleteSlot(classId, b.first, b.second)
                        timetableDao.saveSlots(listOfNotNull(
                            sa?.copy(day = b.first, period = b.second),
                            sb?.copy(day = a.first, period = a.second)
                        ))
                    }
                    return null
                }

                fun timetableInfo(title: String, lineAr: String, lineFr: String, cells: (Int, Int) -> String): com.elyedali.daftarustadh.pdf.TimetablePdf.Info {
                    val dayAr = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")
                    val dayFr = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
                    val st = ttSettings
                    return (
                        com.elyedali.daftarustadh.pdf.TimetablePdf.Info(
                            title = title, lineAr = lineAr, lineFr = lineFr,
                            dayLabels = st.days.map { "${dayAr[it]}\n${dayFr[it]}" },
                            periodLabels = (0 until st.periods).map { "${it + 1}\n${st.label(it)}" },
                            cells = st.days.map { d -> (0 until st.periods).map { p -> cells(d, p) } },
                            footer = "دفتر الأستاذ DS",
                            seal = sealBitmap
                        )
                    )
                }

                fun timetablePdf(title: String, lineAr: String, lineFr: String, cells: (Int, Int) -> String): ByteArray =
                    com.elyedali.daftarustadh.pdf.TimetablePdf.build(timetableInfo(title, lineAr, lineFr, cells))

                fun subjectArFr(code: String): Pair<String, String> {
                    val all = CurriculumRepository.availableYears(this@MainActivity).lastOrNull()?.let { y ->
                        CurriculumRepository.levels(this@MainActivity, y).flatMap { it.subjects }
                    }.orEmpty()
                    val s = all.firstOrNull { it.code == code }
                    return (s?.nameAr ?: code) to (s?.nameFr ?: code)
                }

                fun shareClassTimetable(c: ClassEntity) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val at = ttSlots.filter { it.classId == c.id }.associateBy { it.day to it.period }
                        val bytes = withContext(Dispatchers.Default) {
                            timetablePdf(
                                "جدول القسم \u2066${c.name}\u2069 — Emploi du temps de la classe ${c.name}",
                                "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — السنة الدراسية: \u2066${c.schoolYear}\u2069",
                                "Établissement : ${owner.schoolName} — Année scolaire : ${c.schoolYear}"
                            ) { d, p -> at[d to p]?.let { s -> val (ar, fr) = subjectArFr(s.subjectCode); "$ar\n$fr" + if (s.teacherName.isNotBlank()) "\n${s.teacherName}" else "" }.orEmpty() }
                        }
                        val name = "جدول القسم ${c.name} - Emploi du temps ${c.name} - ${owner.schoolName} - ${c.schoolYear}.pdf"
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, name, getString(R.string.tt_title), "application/pdf")
                    }
                }

                fun shareTeacherTimetablePdf(teacher: String) {
                    scope.launch {
                        val owner = inst ?: return@launch
                        val names = managedClasses.associate { it.id to it.name }
                        val mine = ttSlots.filter { it.teacherName == teacher }.groupBy { it.day to it.period }
                        val bytes = withContext(Dispatchers.Default) {
                            timetablePdf(
                                "جدول الأستاذ: $teacher — Emploi du temps du professeur",
                                "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — السنة الدراسية: \u2066${adminYear ?: owner.schoolYear}\u2069",
                                "Établissement : ${owner.schoolName} — Année scolaire : ${adminYear ?: owner.schoolYear}"
                            ) { d, p -> mine[d to p]?.let { l -> val (ar, fr) = subjectArFr(l.first().subjectCode); l.joinToString(" / ") { names[it.classId].orEmpty() } + "\n$ar\n$fr" }.orEmpty() }
                        }
                        val name = "جدول الأستاذ $teacher - Emploi du temps - ${owner.schoolName} - ${adminYear ?: owner.schoolYear}.pdf"
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, name, getString(R.string.tt_title), "application/pdf")
                    }
                }

                // ===== حالة الجدول الزمني: اكتماله واعتماده =====
                val ttManagedSlots = ttSlots.filter { s -> managedClasses.any { it.id == s.classId } }
                /** بصمة الجدول: أي تعديل بعد الاعتماد يُلغي الاعتماد تلقائيًا. */
                val ttSignature = ttManagedSlots.map { "${it.classId}|${it.day}|${it.period}|${it.subjectCode}|${it.teacherName}" }
                    .sorted().joinToString(";").hashCode().toString() + ":" + ttManagedSlots.size
                /** ما ينقص الجدول ليُعتمد (فارغة = مكتمل). */
                val ttProblems: List<String> = run {
                    val out = ArrayList<String>()
                    if (managedClasses.isEmpty()) return@run out
                    if (ttManagedSlots.isEmpty()) { out += getString(R.string.tt_pb_not_generated); return@run out }
                    managedClasses.sortedWith(compareBy({ it.levelCode }, { it.number })).forEach { c ->
                        val subs = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                        val mine = ttManagedSlots.filter { it.classId == c.id }
                        subs.forEach { sub ->
                            val placed = mine.count { it.subjectCode == sub.code }
                            if (placed < sub.weeklyHours) out += getString(R.string.tt_pb_missing_hours, c.name, subjectName(sub.code), sub.weeklyHours - placed)
                        }
                        mine.filter { it.teacherName.isBlank() }.map { it.subjectCode }.distinct().forEach { code ->
                            out += getString(R.string.tt_pb_no_teacher, c.name, subjectName(code))
                        }
                    }
                    // أستاذ في قسمين في نفس الحصة
                    ttManagedSlots.filter { it.teacherName.isNotBlank() }.groupBy { Triple(it.teacherName, it.day, it.period) }
                        .filter { it.value.size > 1 }.keys.forEach { (t, d, p) ->
                            out += getString(R.string.tt_pb_clash, t, getString(listOf(R.string.day_0, R.string.day_1, R.string.day_2, R.string.day_3, R.string.day_4, R.string.day_5, R.string.day_6)[d.coerceIn(0, 6)]), p + 1)
                        }
                    out
                }
                val ttApproved = ttProblems.isEmpty() && ttManagedSlots.isNotEmpty() && ttApprovedSig == ttSignature
                /** أقسام الإدارة التي لم يُسجَّل فيها أي طالب بعد. */
                val classesWithoutStudents = managedClasses.filter { c -> homeGenderCounts.none { it.classId == c.id && it.count > 0 } }
                    .sortedWith(compareBy({ it.levelCode }, { it.number }))

                /** جدول الأستاذ كما يصله (ملف ‎.dsq أو داخل حزمة سجلاته). */
                fun buildTeacherTimetable(teacher: String): com.elyedali.daftarustadh.transfer.TimetableFile? {
                    val owner = inst ?: return null
                    val names = managedClasses.associate { it.id to it.name }
                    val st = ttSettings
                    return com.elyedali.daftarustadh.transfer.TimetableFile(
                        institutionId = owner.id, schoolName = owner.schoolName, wilaya = owner.wilaya,
                        schoolYear = adminYear ?: owner.schoolYear, teacherName = teacher,
                        entries = ttSlots.filter { it.teacherName == teacher && it.classId in names }.map { s ->
                            val (ar, fr) = subjectArFr(s.subjectCode)
                            com.elyedali.daftarustadh.transfer.TimetableFileEntry(
                                day = s.day, period = s.period,
                                startTime = com.elyedali.daftarustadh.data.TimetableSettings.hhmm(st.start(s.period)),
                                endTime = com.elyedali.daftarustadh.data.TimetableSettings.hhmm(st.end(s.period)),
                                className = names[s.classId].orEmpty(), subjectCode = s.subjectCode,
                                subjectNameAr = ar, subjectNameFr = fr
                            )
                        }
                    )
                }

                fun sendTeacherTimetable(teacher: String) {
                    if (!ttApproved) {
                        android.widget.Toast.makeText(this@MainActivity, getString(R.string.tt_approve_first), android.widget.Toast.LENGTH_LONG).show()
                        return
                    }
                    val file = buildTeacherTimetable(teacher) ?: return
                    scope.launch {
                        val bytes = withContext(Dispatchers.Default) { TransferCodec.encodeTimetable(file) }
                        ShareHelper.share(this@MainActivity, bytes, file.fileName(), getString(R.string.tt_send_teacher))
                    }
                }

                /** سحب دفتر النصوص فارغًا (PDF) لملئه باليد — للإدارة. */
                fun textbookInfo(c: ClassEntity, owner: com.elyedali.daftarustadh.data.InstitutionEntity): com.elyedali.daftarustadh.pdf.TextbookPdf.Info {
                        val wilayaFr = SchoolDirectory.wilaya(this@MainActivity, owner.wilaya)?.nameFr ?: owner.wilaya
                        return com.elyedali.daftarustadh.pdf.TextbookPdf.Info(
                            title = "دفتر النصوص — Cahier de textes",
                            lineAr = "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — القسم: \u2066${c.name}\u2069 — السنة الدراسية: \u2066${c.schoolYear}\u2069",
                            lineFr = "Établissement : ${owner.schoolName} — Wilaya : $wilayaFr — Classe : ${c.name} — Année scolaire : ${c.schoolYear}",
                            shortLine = "دفتر النصوص — Cahier de textes — \u2066${c.name}\u2069 — \u2066${c.schoolYear}\u2069",
                            headers = listOf(
                                "التاريخ\nDate", "التوقيت\nHoraire", "المادة\nMatière", "المجال\nDomaine",
                                "الموضوع\nSujet", "المدة\nDurée", "التوقيع\nSignature"
                            ),
                            page = { p, n -> "صفحة $p من $n — Page $p / $n" },
                            seal = sealBitmap
                        )
                }

                fun printTextbook(c: ClassEntity, pages: Int) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val info = textbookInfo(c, owner)
                        val bytes = withContext(Dispatchers.Default) { com.elyedali.daftarustadh.pdf.TextbookPdf.build(pages, info) }
                        val name = "دفتر النصوص - Cahier de textes - ${c.name} - ${owner.schoolName} - ${c.schoolYear}.pdf"
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, name, getString(R.string.tb_print), "application/pdf")
                    }
                }


                /** سحب سجل الأستاذ لمادة: PDF فارغ الخانات، يُرسل أو يُطبع من نافذة المشاركة. */
                /** عناوين سجل نتائج مادة: ثنائي اللغة دائمًا (عربي وفرنسي)، مثل الكشوف الرسمية، أيًّا كانت لغة الواجهة. */
                fun recordLabels(c: ClassEntity, owner: com.elyedali.daftarustadh.data.InstitutionEntity,
                                 subject: com.elyedali.daftarustadh.data.CurriculumSubject, name: String): TeacherRecordPdf.Labels {
                        val wilaya = SchoolDirectory.wilaya(this@MainActivity, owner.wilaya)
                        return TeacherRecordPdf.Labels(
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
                            seal = sealBitmap
                        )
                }

                fun printRecord(c: ClassEntity, code: String, name: String, blank: Boolean = false) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val subject = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects?.firstOrNull { it.code == code } ?: return@launch
                        val list = if (blank) TeacherRecordPdf.blankRows(70) else studentDao.listByClass(c.id)
                        val labels = recordLabels(c, owner, subject, name)
                        val bytes = withContext(Dispatchers.Default) { TeacherRecordPdf.build(list, labels) }
                        val fileName = getString(R.string.rec_file_name, subject.nameAr, c.name, subject.nameFr, c.schoolYear)
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, fileName, getString(R.string.share_record), "application/pdf")
                    }
                }

                // فتح ملف قسم: من الواتساب أو من زر «فتح ملف قسم»
                fun processFile(uri: Uri) {
                    scope.launch {
                        var importedYear: String? = null
                        val result: FileResult? = try {
                            val bytes = withContext(Dispatchers.IO) {
                                contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            } ?: throw IllegalStateException()
                            // ملفات التطبيق تصل في صورة PDF تحمل البيانات؛ أما PDF عادي (للطباعة) فلا يُفتح في التطبيق
                            val data = if (com.elyedali.daftarustadh.transfer.DsqPdf.isPdf(bytes))
                                com.elyedali.daftarustadh.transfer.DsqPdf.extract(bytes) ?: throw PdfNotImportableException()
                            else bytes
                            when (val any = TransferCodec.decodeAny(data)) {
                                is RecordFile -> when (val chk = recordImporter.check(any)) {
                                    is RecordCheck.Ready -> { recordCheck = chk; null }
                                    is RecordCheck.WrongClass -> FileResult.Message(
                                        getString(R.string.record_wrong_class, chk.file.schoolName, chk.file.className, chk.file.wilaya, chk.file.schoolYear)
                                    )
                                    is RecordCheck.NotManaged -> FileResult.Message(getString(R.string.record_not_managed, chk.file.className))
                                }
                                is com.elyedali.daftarustadh.transfer.AbsenceFile -> when (val chk = absenceImporter.check(any)) {
                                    is com.elyedali.daftarustadh.transfer.AbsenceCheck.Ready -> { absenceCheck = chk; null }
                                    is com.elyedali.daftarustadh.transfer.AbsenceCheck.WrongClass -> FileResult.Message(
                                        getString(R.string.record_wrong_class, chk.file.schoolName, chk.file.className, chk.file.wilaya, chk.file.schoolYear)
                                    )
                                    is com.elyedali.daftarustadh.transfer.AbsenceCheck.NotManaged -> FileResult.Message(getString(R.string.record_not_managed, chk.file.className))
                                }
                                is com.elyedali.daftarustadh.transfer.TimetableFile -> {
                                    timetableDao.replaceTeacherTimetable(any.institutionId, any.schoolYear, any.toEntities(System.currentTimeMillis()))
                                    screen = Screen.MY_TIMETABLE
                                    FileResult.Message(getString(R.string.tt_received, any.schoolName, any.entries.size))
                                }
                                is com.elyedali.daftarustadh.transfer.TeacherBundle -> {
                                    val out = bundleImporter.save(any)
                                    // اسم الأستاذ كما سجّلته الإدارة، إن لم يكن مسجّلًا عنده
                                    if (teacherName.isNullOrBlank() && any.teacherName.isNotBlank())
                                        metaDao.put(AppMetaEntity(TEACHER_NAME_KEY, any.teacherName))
                                    if (out.saved.isNotEmpty()) {
                                        selectedYear = any.classes.first().first.schoolClass.schoolYear
                                        homeMode = com.elyedali.daftarustadh.ui.MODE_TEACHER
                                        screen = Screen.HOME
                                    }
                                    FileResult.Message(
                                        getString(R.string.sr_received, any.schoolName, out.saved.joinToString("، "), out.subjects, out.periods) +
                                            if (out.conflicts.isEmpty()) "" else "\n\n" + getString(R.string.sr_received_conflicts, out.conflicts.joinToString("، "))
                                    )
                                }
                                is ClassFile -> { importedYear = any.schoolClass.schoolYear; FileResult.Done(importer.save(any)) }
                                else -> FileResult.Invalid
                            }
                        } catch (e: PdfNotImportableException) {
                            FileResult.Message(getString(R.string.file_is_pdf))
                        } catch (e: Exception) {
                            FileResult.Invalid
                        }
                        if (result != null) fileResult = result
                        if (result is FileResult.Done && result.outcome is ImportOutcome.Success) {
                            // يفتح الصفحة الرئيسية على سنة القسم ووضعه (الأستاذ أو الإدارة)
                            val ok = result.outcome as ImportOutcome.Success
                            importedYear?.let { selectedYear = it }
                            homeMode = if (ok.type == ClassFileType.TEACHER) com.elyedali.daftarustadh.ui.MODE_TEACHER
                                else com.elyedali.daftarustadh.ui.MODE_ADMIN
                            screen = Screen.HOME
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

                /** يرسل للأستاذ عبر واتساب حزمة فيها كل أقسامه ولوائح طلابها والمواد التي يدرّسها في كل قسم. */
                fun sendTeacherRecords(t: com.elyedali.daftarustadh.data.TimetableTeacher) {
                    val owner = inst ?: return
                    val mine = ttAssignments.filter { it.teacherName == t.name }
                    val classes = managedClasses.filter { c -> mine.any { it.classId == c.id } }
                        .sortedWith(compareBy({ it.levelCode }, { it.number }))
                    if (classes.isEmpty() || t.phone.isBlank()) return
                    if (!ttApproved || classesWithoutStudents.isNotEmpty()) return
                    val timetable = buildTeacherTimetable(t.name)
                    scope.launch {
                        val bundle = com.elyedali.daftarustadh.transfer.TeacherBundle(
                            teacherName = t.name, schoolName = owner.schoolName, schoolYear = adminYear ?: owner.schoolYear,
                            classes = classes.map { c ->
                                ClassFile(ClassFileType.TEACHER, owner, c, studentDao.listByClass(c.id)) to
                                    mine.filter { it.classId == c.id }.map { it.subjectCode }
                            },
                            timetable = timetable
                        )
                        val bytes = withContext(Dispatchers.Default) { TransferCodec.encodeBundle(bundle) }
                        val caption = getString(R.string.sr_caption, t.name, owner.schoolName,
                            classes.joinToString("، ") { c -> c.name + " (" + mine.filter { it.classId == c.id }.joinToString("، ") { subjectName(it.subjectCode) } + ")" })
                        ShareHelper.shareToWhatsApp(this@MainActivity, bytes, bundle.fileName(), t.phone, caption, chooserTitle)
                        recordsSent = recordsSent + t.name
                    }
                }

                /** أشهر السنة الدراسية (أكتوبر ← يونيو) وأيام الدراسة في كل شهر، حسب أيام الجدول الزمني. */
                fun schoolMonths(schoolYear: String): List<com.elyedali.daftarustadh.pdf.AbsencePdf.Month> {
                    val y1 = schoolYear.take(4).toIntOrNull() ?: return emptyList()
                    val ar = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
                    val fr = listOf("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre")
                    val short = listOf("إثن", "ثلا", "أرب", "خمي", "جمع", "سبت", "أحد")   // 0 = الاثنين
                    val schoolDays = ttSettings.days.toSet()
                    return (listOf(y1 to 9, y1 to 10, y1 to 11) + (0..5).map { (y1 + 1) to it }).map { (y, m) ->
                        val cal = java.util.Calendar.getInstance().apply { clear(); set(y, m, 1) }
                        val n = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        val days = (1..n).mapNotNull { d ->
                            cal.set(java.util.Calendar.DAY_OF_MONTH, d)
                            val idx = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7   // الاثنين = 0
                            if (idx in schoolDays) com.elyedali.daftarustadh.pdf.AbsencePdf.Day(d, short[idx]) else null
                        }
                        com.elyedali.daftarustadh.pdf.AbsencePdf.Month("شهر ${ar[m]} ⁦$y⁩", "Mois de ${fr[m]} $y", days)
                    }
                }

                /**
                 * الوراقة: يجهّز ملفات PDF للطباعة (ملف لكل وثيقة يجمع كل الأقسام أو كل الأساتذة)
                 * ثم يشاركها دفعة واحدة (واتساب أو غيره).
                 */
                fun shareWithPrintShop(choice: com.elyedali.daftarustadh.ui.PrintChoice) {
                    val owner = inst ?: return
                    val classes = managedClasses.sortedWith(compareBy({ it.levelCode }, { it.number }))
                    if (classes.isEmpty()) return
                    printWorking = true
                    scope.launch {
                        try {
                            val seal = sealBitmap
                            val dir = ShareHelper.freshShareDir(this@MainActivity)
                            val tag = "${owner.schoolName} - ${adminYear ?: owner.schoolYear}".replace(Regex("[\\\\/:*?\"<>|]"), " ")
                            val students = classes.associate { it.id to studentDao.listByClass(it.id) }
                            val files = ArrayList<java.io.File>()
                            withContext(Dispatchers.Default) {
                                if (choice.records) {
                                    val f = java.io.File(dir, "سجلات نتائج الأساتذة - Registres de notes - $tag.pdf")
                                    com.elyedali.daftarustadh.pdf.PdfCommon.renderToFile(f) { doc ->
                                        var no = 1
                                        classes.forEach { c ->
                                            CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty().forEach { sub ->
                                                val teacher = ttAssignments.firstOrNull { it.classId == c.id && it.subjectCode == sub.code }?.teacherName.orEmpty()
                                                no = TeacherRecordPdf.drawInto(doc, no, students[c.id].orEmpty(),
                                                    recordLabels(c, owner, sub, teacher).copy(seal = seal))
                                            }
                                        }
                                    }
                                    files += f
                                }
                                if (choice.timetables) {
                                    val names = classes.associate { it.id to it.name }
                                    val f = java.io.File(dir, "جداول الأساتذة - Emplois du temps des professeurs - $tag.pdf")
                                    com.elyedali.daftarustadh.pdf.PdfCommon.renderToFile(f) { doc ->
                                        var no = 1
                                        ttManagedSlots.map { it.teacherName }.filter { it.isNotBlank() }.distinct().sorted().forEach { t ->
                                            val mine = ttManagedSlots.filter { it.teacherName == t }.groupBy { it.day to it.period }
                                            val info = timetableInfo(
                                                "جدول الأستاذ: $t — Emploi du temps du professeur",
                                                "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — السنة الدراسية: ⁦${adminYear ?: owner.schoolYear}⁩",
                                                "Établissement : ${owner.schoolName} — Année scolaire : ${adminYear ?: owner.schoolYear}"
                                            ) { d, p -> mine[d to p]?.let { l -> val (ar, fr) = subjectArFr(l.first().subjectCode); l.joinToString(" / ") { names[it.classId].orEmpty() } + "\n$ar\n$fr" }.orEmpty() }
                                            no = com.elyedali.daftarustadh.pdf.TimetablePdf.drawInto(doc, no, info.copy(seal = seal))
                                        }
                                    }
                                    files += f
                                }
                                if (choice.absence) {
                                    val months = schoolMonths(adminYear ?: owner.schoolYear)
                                    val wilayaFr = SchoolDirectory.wilaya(this@MainActivity, owner.wilaya)?.nameFr ?: owner.wilaya
                                    val f = java.io.File(dir, "دفتر الغياب - Registre d'appel - $tag.pdf")
                                    com.elyedali.daftarustadh.pdf.PdfCommon.renderToFile(f) { doc ->
                                        var no = 1
                                        classes.forEach { c ->
                                            no = com.elyedali.daftarustadh.pdf.AbsencePdf.drawInto(doc, no, com.elyedali.daftarustadh.pdf.AbsencePdf.Info(
                                                title = "دفتر الغياب — Registre d'appel",
                                                lineAr = "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — القسم: ⁦${c.name}⁩ — السنة الدراسية: ⁦${c.schoolYear}⁩",
                                                lineFr = "Établissement : ${owner.schoolName} — Wilaya : $wilayaFr — Classe : ${c.name} — Année scolaire : ${c.schoolYear}",
                                                shortLine = "دفتر الغياب — Registre d'appel — ⁦${c.name}⁩ — ⁦${c.schoolYear}⁩",
                                                months = months,
                                                students = students[c.id].orEmpty().map { s -> com.elyedali.daftarustadh.pdf.AbsencePdf.Student(s.rollNumber, s.nameAr, s.nameFr) },
                                                rollHeader = "ر.ن\nN°",
                                                nameHeader = "الاسم الكامل — Nom et Prénom",
                                                totalHeader = "المجموع\nTotal",
                                                page = { p, n -> "⁦${c.name}⁩ — صفحة $p من $n — Page $p / $n" },
                                                seal = seal
                                            ))
                                        }
                                    }
                                    files += f
                                }
                                if (choice.textbook) {
                                    val f = java.io.File(dir, "دفتر النصوص - Cahier de textes - $tag.pdf")
                                    com.elyedali.daftarustadh.pdf.PdfCommon.renderToFile(f) { doc ->
                                        var no = 1
                                        classes.forEach { c ->
                                            no = com.elyedali.daftarustadh.pdf.TextbookPdf.drawInto(doc, no, choice.textbookPages, textbookInfo(c, owner).copy(seal = seal))
                                        }
                                    }
                                    files += f
                                }
                            }
                            if (files.isNotEmpty()) ShareHelper.shareFiles(this@MainActivity, files, "application/pdf",
                                getString(R.string.ps_title), getString(R.string.ps_message, owner.schoolName))
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(this@MainActivity, getString(R.string.ps_error), android.widget.Toast.LENGTH_LONG).show()
                        } finally {
                            printWorking = false
                        }
                    }
                }

                when (screen) {
                    Screen.HOME -> if (homeMode != null) BackHandler { homeMode = null } else if (selectedYear != null) BackHandler { selectedYear = null }
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
                    Screen.ACTIVATION -> BackHandler { screen = activationBack }
                    Screen.TIMETABLE -> BackHandler { screen = ttBack }
                    Screen.MY_TIMETABLE -> BackHandler { screen = Screen.HOME }
                    Screen.INSTITUTION -> BackHandler { if (inst == null) homeMode = null; screen = Screen.HOME }
                    Screen.TEACHER_PROFILE -> BackHandler { if (teacherProfile == null) homeMode = null; screen = Screen.HOME }
                    else -> BackHandler { screen = Screen.HOME }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = screen == Screen.HOME || drawerState.isOpen,
                    drawerContent = {
                        com.elyedali.daftarustadh.ui.AppDrawer(
                            schoolName = inst?.schoolName,
                            schoolYear = inst?.schoolYear,
                            current = screen.name,
                            lastSync = null,
                            onItem = { key ->
                                scope.launch { drawerState.close() }
                                when (key) {
                                    "HOME" -> { selectedYear = null; homeMode = null; screen = Screen.HOME }
                                    "SYNC" -> android.widget.Toast.makeText(this@MainActivity, getString(R.string.drawer_sync_later), android.widget.Toast.LENGTH_LONG).show()
                                    "LOGOUT" -> android.widget.Toast.makeText(this@MainActivity, getString(R.string.drawer_logout_later), android.widget.Toast.LENGTH_LONG).show()
                                    "TIMETABLE" -> { ttInitialClass = null; ttBack = Screen.HOME; screen = Screen.TIMETABLE }
                                    "MY_TIMETABLE" -> screen = Screen.MY_TIMETABLE
                                    "EXCHANGES" -> screen = Screen.EXCHANGES
                                    "LEGISLATION" -> screen = Screen.LEGISLATION
                                    "BOOKS" -> openKoutoubi()
                                    "STATS" -> screen = Screen.STATS
                                    "ACTIVATION" -> { activationBack = Screen.HOME; screen = Screen.ACTIVATION }
                                    "INSTITUTION" -> screen = Screen.INSTITUTION
                                    "REMARKS" -> screen = Screen.REMARKS
                                    "OPEN_FILE" -> openFile()
                                    "LANG_AR" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(AR_LATN))
                                    "LANG_FR" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fr"))
                                }
                            }
                        )
                    }
                ) {
                when {
                    screen == Screen.INSTITUTION -> InstitutionScreen(
                        existing = inst,
                        onSave = { scope.launch { institutionDao.save(it); screen = Screen.HOME } },
                        onCancel = { if (inst == null) homeMode = null; screen = Screen.HOME }
                    )

                    screen == Screen.ABSENCE_CENTER -> com.elyedali.daftarustadh.ui.AbsenceCenterScreen(
                        classes = managedClasses.sortedWith(compareBy({ it.levelCode }, { it.number })),
                        students = allStudents,
                        sessions = allAbsSessions,
                        absences = allAbsences,
                        settings = ttSettings,
                        slots = ttManagedSlots,
                        subjectsOf = { c ->
                            CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                                .map { AbsenceSubject(it.code, subjectName(it.code)) }
                        },
                        subjectLabel = { subjectName(it) },
                        yearStart = ((adminYear ?: inst?.schoolYear)?.take(4) ?: "2026") + "-10-01",
                        onSave = { session, ids -> scope.launch { absenceDao.saveSession(session, ids) } },
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.PRINT_SHOP -> {
                        val noStudents = classesWithoutStudents.joinToString("، ") { it.name }
                        val n = managedClasses.size
                        com.elyedali.daftarustadh.ui.PrintShopScreen(
                            records = com.elyedali.daftarustadh.ui.PrintItem(
                                ready = n > 0 && ttApproved && classesWithoutStudents.isEmpty(),
                                note = when {
                                    n == 0 -> getString(R.string.ps_no_classes)
                                    !ttApproved -> getString(R.string.sr_block_timetable)
                                    classesWithoutStudents.isNotEmpty() -> getString(R.string.sr_block_students, noStudents)
                                    else -> getString(R.string.ps_records_note, n)
                                }),
                            timetables = com.elyedali.daftarustadh.ui.PrintItem(
                                ready = n > 0 && ttApproved,
                                note = if (ttApproved) getString(R.string.ps_timetables_note, ttManagedSlots.map { it.teacherName }.filter { it.isNotBlank() }.distinct().size)
                                    else getString(R.string.sr_block_timetable)),
                            absence = com.elyedali.daftarustadh.ui.PrintItem(
                                ready = n > 0 && classesWithoutStudents.isEmpty(),
                                note = when {
                                    n == 0 -> getString(R.string.ps_no_classes)
                                    classesWithoutStudents.isNotEmpty() -> getString(R.string.sr_block_students, noStudents)
                                    else -> getString(R.string.ps_absence_note, n)
                                }),
                            textbook = com.elyedali.daftarustadh.ui.PrintItem(
                                ready = n > 0,
                                note = if (n == 0) getString(R.string.ps_no_classes) else getString(R.string.ps_textbook_note, n)),
                            working = printWorking,
                            onShare = { shareWithPrintShop(it) },
                            onOpenTimetable = { ttInitialClass = null; ttInitialTab = 0; ttBack = Screen.PRINT_SHOP; screen = Screen.TIMETABLE },
                            onBack = { screen = Screen.HOME }
                        )
                    }

                    screen == Screen.SHARE_RECORDS -> com.elyedali.daftarustadh.ui.ShareRecordsScreen(
                        teachers = ttTeachers,
                        classes = managedClasses,
                        assignments = ttAssignments,
                        subjectLabel = { subjectName(it) },
                        sent = recordsSent,
                        blockers = buildList {
                            if (!ttApproved) add(getString(R.string.sr_block_timetable))
                            if (classesWithoutStudents.isNotEmpty())
                                add(getString(R.string.sr_block_students, classesWithoutStudents.joinToString("، ") { it.name }))
                        },
                        onOpenTimetable = { ttInitialClass = null; ttInitialTab = 0; ttBack = Screen.SHARE_RECORDS; screen = Screen.TIMETABLE },
                        onSend = { sendTeacherRecords(it) },
                        onSavePhone = { t, phone -> saveTeachers(ttTeachers.map { if (it.name == t.name) it.copy(phone = phone) else it }) },
                        onManageTeachers = { ttInitialClass = null; ttInitialTab = 1; ttBack = Screen.SHARE_RECORDS; screen = Screen.TIMETABLE },
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.TEACHER_PROFILE -> {
                        val isFrP = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "fr"
                        val options = remember(isFrP) {
                            val year = CurriculumRepository.availableYears(this@MainActivity).lastOrNull()
                            (if (year == null) emptyList() else CurriculumRepository.levels(this@MainActivity, year).flatMap { it.subjects }.distinctBy { it.code })
                                .map { com.elyedali.daftarustadh.ui.SpecialtyOption(it.code, if (isFrP) it.nameFr else it.nameAr) }
                        }
                        com.elyedali.daftarustadh.ui.TeacherProfileScreen(
                            existing = teacherProfile ?: teacherName?.takeIf { it.isNotBlank() }?.let { com.elyedali.daftarustadh.ui.TeacherProfile(it, "", emptyList()) },
                            specialties = options,
                            onSave = { pr ->
                                scope.launch {
                                    metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.ui.TeacherProfile.KEY, pr.encode()))
                                    metaDao.put(AppMetaEntity(TEACHER_NAME_KEY, pr.name))
                                    screen = Screen.HOME
                                }
                            },
                            onCancel = { if (teacherProfile == null) homeMode = null; screen = Screen.HOME }
                        )
                    }

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
                                absenceDao.deleteAbsencesByClass(c.id)
                                absenceDao.deleteSessionsByClass(c.id)
                                timetableDao.deleteAssignmentsByClass(c.id)
                                timetableDao.deleteSlotsByClass(c.id)
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

                    screen == Screen.TIMETABLE -> com.elyedali.daftarustadh.ui.TimetableScreen(
                        classes = managedClasses.sortedWith(compareBy({ it.levelCode }, { it.number })),
                        subjectsByClass = managedClasses.associate { c ->
                            c.id to CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                        },
                        subjectLabel = { subjectName(it) },
                        settings = ttSettings,
                        assignments = ttAssignments,
                        teachers = ttTeachers,
                        slots = ttSlots.filter { s -> managedClasses.any { it.id == s.classId } },
                        generating = ttGenerating,
                        lastResult = ttResult,
                        initialClassId = ttInitialClass,
                        onSaveSettings = { st -> scope.launch { metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.data.TimetableSettings.KEY, st.encode())) } },
                        onAssign = { cid, code, teacher ->
                            scope.launch {
                                if (teacher.isBlank()) timetableDao.deleteAssignment(cid, code)
                                else timetableDao.saveAssignment(com.elyedali.daftarustadh.data.TimetableAssignmentEntity(cid, code, teacher))
                                // الحصص الموجودة لهذه المادة تأخذ الأستاذ الجديد
                                timetableDao.saveSlots(ttSlots.filter { it.classId == cid && it.subjectCode == code }.map { it.copy(teacherName = teacher) })
                            }
                        },
                        onSaveTeachers = { saveTeachers(it) },
                        onDistribute = { scope.launch { distributeTeachers() } },
                        onGenerate = { generateTimetable() },
                        onSwap = { cid, a, b -> swapSlots(cid, a, b) },
                        onEditCell = { cid, d, p, code ->
                            scope.launch {
                                if (code == null) timetableDao.deleteSlot(cid, d, p)
                                else {
                                    val teacher = ttAssignments.firstOrNull { it.classId == cid && it.subjectCode == code }?.teacherName.orEmpty()
                                    timetableDao.saveSlots(listOf(com.elyedali.daftarustadh.data.TimetableSlotEntity(cid, d, p, code, teacher)))
                                }
                            }
                        },
                        onShareClass = { shareClassTimetable(it) },
                        onShareTeacherPdf = { shareTeacherTimetablePdf(it) },
                        onSendTeacherFile = { sendTeacherTimetable(it) },
                        onBack = { screen = ttBack },
                        initialTab = ttInitialTab,
                        problems = ttProblems,
                        approved = ttApproved,
                        onApprove = { yes ->
                            scope.launch { metaDao.put(AppMetaEntity(TT_APPROVED_KEY, if (yes) ttSignature else "")) }
                        }
                    )

                    screen == Screen.MY_TIMETABLE -> com.elyedali.daftarustadh.ui.TeacherTimetableView(
                        entries = myTimetable,
                        highlightClass = null,
                        highlightInstitution = null,
                        onOpenFile = openFile,
                        showHeader = true,
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.ACTIVATION -> com.elyedali.daftarustadh.ui.ActivationScreen(
                        classes = managedClasses.sortedBy { it.name },
                        activated = activatedClasses,
                        requested = requestedClasses,
                        onRequest = { chosen, total ->
                            val owner = inst
                            val msg = buildString {
                                append(getString(R.string.act_msg_intro)).append('\n')
                                if (owner != null) {
                                    append(getString(R.string.act_msg_school, owner.schoolName)).append('\n')
                                    append(getString(R.string.act_msg_wilaya, owner.wilaya)).append('\n')
                                    append(getString(R.string.act_msg_year, owner.schoolYear)).append('\n')
                                }
                                append(getString(R.string.act_msg_classes, chosen.size)).append('\n')
                                chosen.forEach { append("• ${it.name} — ${com.elyedali.daftarustadh.data.ClassActivation.code(it.id)}\n") }
                                append(getString(R.string.act_msg_total, total))
                            }
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${com.elyedali.daftarustadh.data.ClassActivation.ADMIN_PHONE}&text=" + Uri.encode(msg))
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, uri))
                                scope.launch {
                                    metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.data.ClassActivation.REQUESTED_KEY,
                                        com.elyedali.daftarustadh.data.ClassActivation.encode(requestedClasses + chosen.map { it.id })))
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(this@MainActivity, getString(R.string.ex_no_whatsapp), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        onBack = { screen = activationBack }
                    )

                    screen == Screen.EXCHANGES -> {
                        val myPostsText by remember { metaDao.observe(EXCHANGE_POSTS_KEY) }.collectAsState(initial = null)
                        val myPosts = remember(myPostsText) { com.elyedali.daftarustadh.exchange.ExchangeAd.listFromJson(myPostsText) }
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
                            myPosts = myPosts,
                            ownerId = owner,
                            repository = com.elyedali.daftarustadh.exchange.NotConnectedExchangeRepository,
                            onSaveMyPosts = { list -> metaDao.put(AppMetaEntity(EXCHANGE_POSTS_KEY, com.elyedali.daftarustadh.exchange.ExchangeAd.listToJson(list))) },
                            onBack = { screen = Screen.HOME }
                        )
                    }

                    screen == Screen.ADMIN_CLASS && currentClass != null -> {
                        val c = currentClass!!
                        val subjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                        ClassHub(
                            title = c.name,
                            subtitles = listOfNotNull(stringResource(R.string.mode_admin), c.schoolYear),
                            tabs = listOf(stringResource(R.string.tab_students), stringResource(R.string.tab_results),
                                stringResource(R.string.tab_absence), stringResource(R.string.tab_kashf), stringResource(R.string.tab_class_timetable)),
                            icons = listOf(androidx.compose.material.icons.Icons.Filled.Person, androidx.compose.material.icons.Icons.Filled.Edit,
                                androidx.compose.material.icons.Icons.Filled.CheckCircle, androidx.compose.material.icons.Icons.Filled.List,
                                androidx.compose.material.icons.Icons.Filled.DateRange),
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
                                        onDelete = { s -> scope.launch { gradeDao.deleteByStudent(s.id); absenceDao.deleteByStudent(s.id); studentDao.delete(s) } },
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
                                2 -> AbsenceTab(
                                    classId = c.id,
                                    students = students,
                                    subjects = subjects.map { AbsenceSubject(it.code, if (isFrUi) it.nameFr else it.nameAr) },
                                    sessions = absenceSessions,
                                    absences = absences,
                                    isAdmin = true,
                                    onSave = { old, session, ids -> scope.launch { absenceDao.replaceSession(old, session, ids) } },
                                    onDelete = { id -> scope.launch { absenceDao.deleteSession(id) } },
                                    onSend = null,
                                    onUpload = null,
                                    onChooseSubjects = null
                                )
                                3 -> {
                                    // «الكشوف»: كشوف الدرجات + دفتر النصوص (سحب اللوائح والكشوف كما في دفتر المعلم)
                                    var printTab by rememberSaveable { mutableIntStateOf(0) }
                                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                                        androidx.compose.foundation.layout.Row(
                                            Modifier.fillMaxWidth().padding(top = 10.dp),
                                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                                        ) {
                                            androidx.compose.material3.FilterChip(selected = printTab == 0, onClick = { printTab = 0 },
                                                label = { Text(stringResource(R.string.kashf_reports)) })
                                            androidx.compose.material3.FilterChip(selected = printTab == 1, onClick = { printTab = 1 },
                                                label = { Text(stringResource(R.string.tab_textbook)) })
                                        }
                                        if (printTab == 0) {
                                            val missing = (1..3).associateWith { t -> ReportCalc.missing(t, subjects, students, classGrades).size }
                                            KashfTab(
                                                missingByTerm = if (students.isEmpty()) mapOf(1 to -1, 2 to -1, 3 to -1).mapValues { 1 } else missing,
                                                onGenerate = { term -> generateKashf(c, term) },
                                                onOpenSettings = { screen = Screen.KASHF_SETTINGS },
                                                locked = c.id !in activatedClasses,
                                                onActivate = { activationBack = Screen.ADMIN_CLASS; screen = Screen.ACTIVATION }
                                            )
                                        } else TextbookTab(onPrint = { pages -> printTextbook(c, pages) })
                                    }
                                }
                                else -> {
                                    val at = ttSlots.filter { it.classId == c.id }.associateBy { it.day to it.period }
                                    androidx.compose.foundation.layout.Column(
                                        Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(top = 10.dp),
                                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (at.isEmpty()) Text(stringResource(R.string.tt_no_timetable))
                                        else com.elyedali.daftarustadh.ui.TimetableGrid(
                                            settings = ttSettings,
                                            cell = { d, p -> at[d to p]?.let { subjectName(it.subjectCode) to it.teacherName } },
                                            colorKey = { d, p -> at[d to p]?.subjectCode }
                                        )
                                        androidx.compose.material3.Button(onClick = {
                                            ttInitialClass = c.id; ttInitialTab = null; ttBack = Screen.ADMIN_CLASS; screen = Screen.TIMETABLE
                                        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tt_open_institution)) }
                                        if (at.isNotEmpty()) androidx.compose.material3.OutlinedButton(onClick = { shareClassTimetable(c) },
                                            modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tt_share_class, c.name)) }
                                    }
                                }
                            }
                        }
                    }

                    screen == Screen.TEACHER_HUB && currentClass != null -> {
                        val c = currentClass!!
                        ClassHub(
                            title = c.name,
                            subtitles = listOfNotNull(stringResource(R.string.mode_teacher), schoolNames[c.institutionId], c.schoolYear),
                            tabs = listOf(stringResource(R.string.tab_students), stringResource(R.string.tab_results),
                                stringResource(R.string.tab_teacher_timetable), stringResource(R.string.tab_editor)),
                            icons = listOf(androidx.compose.material.icons.Icons.Filled.Person, androidx.compose.material.icons.Icons.Filled.Edit,
                                androidx.compose.material.icons.Icons.Filled.DateRange,
                                androidx.compose.material.icons.Icons.Filled.Create),
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
                                2 -> com.elyedali.daftarustadh.ui.TeacherTimetableView(
                                    entries = myTimetable,
                                    highlightClass = c.name,
                                    highlightInstitution = c.institutionId,
                                    onOpenFile = openFile,
                                    showHeader = false,
                                    onBack = { screen = hubBack }
                                )
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
                            onDelete = { s -> scope.launch { gradeDao.deleteByStudent(s.id); absenceDao.deleteByStudent(s.id); studentDao.delete(s) } },
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
                            onSetGrade = { studentId, slot, value, mark ->
                                scope.launch {
                                    if (value == null) gradeDao.clear(studentId, code, slot)
                                    else gradeDao.save(GradeEntity(studentId, c.id, code, slot, value, System.currentTimeMillis(), mark))
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

                    // إعدادات سجل الأستاذ (وضع الأستاذ): ملاحظات المواد حسب المعدل؛ اسمه في «معلوماتي»
                    screen == Screen.REMARKS -> RemarksSettingsScreen(
                        scale = remarkScale,
                        teacherName = null,
                        onSave = { list, _ ->
                            scope.launch {
                                metaDao.put(AppMetaEntity(RemarkScale.KEY, RemarkScale.encode(list)))
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

                    else -> HomeYearsScreen(
                        classes = allClasses,
                        currentInstitutionYear = inst?.schoolYear,
                        selectedYear = selectedYear,
                        mode = homeMode,
                        onSelectMode = { m ->
                            homeMode = m
                            // أول دخول: الإدارة تسجّل معلومات المؤسسة، والأستاذ يسجّل معلوماته (مرة واحدة)
                            if (m == com.elyedali.daftarustadh.ui.MODE_ADMIN && inst == null) screen = Screen.INSTITUTION
                            if (m == com.elyedali.daftarustadh.ui.MODE_TEACHER && teacherProfile == null) screen = Screen.TEACHER_PROFILE
                        },
                        institutionName = inst?.schoolName,
                        teacherName = teacherProfile?.name ?: teacherName,
                        onTool = { key ->
                            when (key) {
                                "INSTITUTION" -> screen = Screen.INSTITUTION
                                "TIMETABLE" -> { ttInitialClass = null; ttInitialTab = null; ttBack = Screen.HOME; screen = Screen.TIMETABLE }
                                "SHARE_RECORDS" -> screen = Screen.SHARE_RECORDS
                                "PRINT_SHOP" -> screen = Screen.PRINT_SHOP
                                "ABSENCE_CENTER" -> screen = Screen.ABSENCE_CENTER
                                "STATS" -> screen = Screen.STATS
                                "ACTIVATION" -> { activationBack = Screen.HOME; screen = Screen.ACTIVATION }
                                "REMARKS" -> screen = Screen.REMARKS
                                "TEACHER_PROFILE" -> screen = Screen.TEACHER_PROFILE
                                "MY_TIMETABLE" -> screen = Screen.MY_TIMETABLE
                            }
                        },
                        schoolNames = schoolNames,
                        ownInstitutionId = inst?.id,
                        activated = activatedClasses,
                        genderCounts = homeGenderCounts,
                        onSelectYear = { selectedYear = it; homeMode = null },
                        onOpenClass = { c ->
                            classId = c.id; hubTab = 0; hubBack = Screen.HOME
                            if (homeMode == com.elyedali.daftarustadh.ui.MODE_ADMIN) { fromTeaching = false; screen = Screen.ADMIN_CLASS }
                            else { fromTeaching = true; screen = Screen.TEACHER_HUB }
                        },
                        onDeleteClass = { deleteClass(it) },
                        onCreateClass = { if (inst == null) screen = Screen.INSTITUTION else showCreateClass = true },
                        onOpenFile = openFile,
                        onMenu = { scope.launch { drawerState.open() } }
                    )
                }

                }

                if (showCreateClass && inst != null) {
                    val year = selectedYear ?: inst.schoolYear
                    com.elyedali.daftarustadh.ui.CreateClassDialog(
                        institution = inst,
                        schoolYear = year,
                        existing = allClasses,
                        onCreate = { level, number, onResult ->
                            scope.launch {
                                val rowId = classDao.insert(
                                    ClassEntity(
                                        id = UUID.randomUUID().toString(),
                                        institutionId = inst.id,
                                        schoolYear = year,
                                        levelCode = level,
                                        number = number,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                onResult(rowId != -1L)
                                if (rowId != -1L) { selectedYear = year; homeMode = com.elyedali.daftarustadh.ui.MODE_ADMIN }
                            }
                        },
                        onDismiss = { showCreateClass = false }
                    )
                }

                askNameFor?.let { code ->
                    var typed by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { askNameFor = null; askNameSends = false; askNameAbsence = false },
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
                                    askNameAbsence -> { askNameAbsence = false; currentClass?.let { sendAbsence(it, code, n) } }
                                    doc != null -> { askNameDoc = null; exportDocument(doc, n) }
                                    askNameSends -> { askNameSends = false; sendConfirm = code }
                                    else -> currentClass?.let { printRecord(it, code, n, askNameBlank) }
                                }
                            }) { Text(stringResource(R.string.save)) }
                        },
                        dismissButton = { TextButton(onClick = { askNameFor = null }) { Text(stringResource(R.string.cancel)) } }
                    )
                }

                docSubjectChoice?.let { (d, n) ->
                    val c = currentClass
                    val opts = c?.let { cc -> CurriculumRepository.level(this@MainActivity, cc.schoolYear, cc.levelCode)?.subjects.orEmpty() }
                        .orEmpty().filter { s -> mySubjects.any { it.subjectCode == s.code } }
                    AlertDialog(
                        onDismissRequest = { docSubjectChoice = null },
                        title = { Text(stringResource(R.string.doc_pick_subject)) },
                        text = {
                            androidx.compose.foundation.layout.Column {
                                opts.forEach { sub ->
                                    TextButton(onClick = { docSubjectChoice = null; exportDocument(d, n, sub.code) }) {
                                        Text(if (isFrUi) sub.nameFr else sub.nameAr)
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = { TextButton(onClick = { docSubjectChoice = null }) { Text(stringResource(R.string.cancel)) } }
                    )
                }

                sendConfirm?.let { code ->
                    val c = currentClass
                    val n = teacherName
                    // لا يُرسل السجل إلا مكتملًا: آخر فصل بدأ رصده يجب أن يكتمل لكل الطلاب (وكذا الفصول قبله)
                    val byStudent = classGrades.filter { it.subjectCode == code }.groupBy { it.studentId }
                        .mapValues { e -> e.value.associate { it.slot to it.value } }
                    val started = (3 downTo 1).firstOrNull { t ->
                        byStudent.values.any { it[GradeSlot.test(t)] != null || it[GradeSlot.exam(t)] != null }
                    } ?: 0
                    val incomplete = if (started == 0) students else students.filter { st ->
                        (1..started).any { t -> !GradeMath.isComplete(t, byStudent[st.id].orEmpty()) }
                    }
                    if (c == null || n.isNullOrBlank()) {
                        LaunchedEffect(code) { sendConfirm = null }
                    } else if (students.isNotEmpty() && incomplete.isEmpty()) {
                        LaunchedEffect(code) { sendConfirm = null; sendRecord(c, code, n) }
                    } else {
                        AlertDialog(
                            onDismissRequest = { sendConfirm = null },
                            title = { Text(stringResource(R.string.send_blocked_title)) },
                            text = {
                                Text(
                                    if (started == 0) stringResource(R.string.send_blocked_empty)
                                    else stringResource(R.string.send_blocked_list, started, incomplete.size,
                                        incomplete.sortedBy { it.rollNumber }.take(15).joinToString("، ") { "${it.rollNumber}. ${it.nameAr.ifBlank { it.nameFr }}" } +
                                            if (incomplete.size > 15) " …" else "")
                                )
                            },
                            confirmButton = { TextButton(onClick = { sendConfirm = null }) { Text(stringResource(R.string.ok)) } }
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
                                // الأستاذ المعيّن لهذه المادة في هذا القسم (الجدول الزمني) يختلف عن صاحب السجل؟
                                val assigned = ttAssignments.firstOrNull { it.classId == chk.schoolClass.id && it.subjectCode == chk.file.subjectCode }?.teacherName
                                if (!assigned.isNullOrBlank() && assigned.trim() != chk.file.teacherName.trim())
                                    Text(stringResource(R.string.record_other_teacher, assigned), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
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

                absenceCheck?.let { chk ->
                    val isFr = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "fr"
                    val subjectName = if (isFr) chk.file.subjectNameFr else chk.file.subjectNameAr
                    AlertDialog(
                        onDismissRequest = { absenceCheck = null },
                        title = { Text(stringResource(R.string.abs_import_title, subjectName, chk.file.className)) },
                        text = {
                            androidx.compose.foundation.layout.Column {
                                Text(stringResource(R.string.record_teacher, chk.file.teacherName))
                                Text(stringResource(R.string.abs_import_counts, chk.newSessions, chk.replacedSessions))
                                if (chk.unknownStudents > 0)
                                    Text(stringResource(R.string.abs_import_unknown, chk.unknownStudents), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                            }
                        },
                        confirmButton = {
                            TextButton(enabled = chk.file.sessions.isNotEmpty(), onClick = {
                                absenceCheck = null
                                scope.launch {
                                    absenceImporter.apply(chk)
                                    classId = chk.schoolClass.id
                                    fromTeaching = false
                                    hubTab = 2
                                    screen = Screen.ADMIN_CLASS
                                    fileResult = FileResult.Message(getString(R.string.abs_import_done, subjectName, chk.file.className))
                                }
                            }) { Text(stringResource(R.string.record_upload)) }
                        },
                        dismissButton = { TextButton(onClick = { absenceCheck = null }) { Text(stringResource(R.string.cancel)) } }
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
        const val EXCHANGE_POSTS_KEY = "exchange_my_posts"
        const val EXCHANGE_OWNER_KEY = "exchange_owner_id"
    }
}
