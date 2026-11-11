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
import androidx.compose.ui.unit.sp
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
/** الجدول يُدخل يدويًا (أُعدّ خارج التطبيق): لا توزيع تلقائي للأساتذة على الأقسام. */
private const val TT_MANUAL_KEY = "timetable_manual"

private enum class Screen { HOME, INSTITUTION, CLASSES, TEACHING, TEACHER_CLASS, STUDENTS, IMPORT, GRADES, REMARKS, RESULTS, KASHF_SETTINGS, GENERAL_REMARKS, STATS,
    ALL_CLASSES, ADMIN_CLASS, TEACHER_HUB, LEGISLATION, EXCHANGES, BOOKS, ACTIVATION, TIMETABLE, MY_TIMETABLE, TEACHER_PROFILE, SHARE_RECORDS, PRINT_SHOP, ABSENCE_CENTER, QUICK_GRADES, ABOUT, RECORDS_FOLLOW }

/** نتيجة فتح ملف قسم، تُعرض في نافذة. */
/** فتح ملف PDF بزر «فتح ملف»: ملفات PDF للطباعة فقط، والتطبيق يفتح ملفات ‎.dsq. */
private class PdfNotImportableException : Exception()
/** ملف يخص التطبيق الآخر (الإدارة أو الأستاذ). */
private class WrongAppException(val app: String) : Exception()

/**
 * نفس الأستاذ رغم اختلاف بسيط في كتابة الاسم (المسافات، «الأستاذ»، الهمزات، اسم زائد أو ناقص):
 * الإدارة تعرف رقم واتسابه ومن أرسل الملف، فلا يُعدّ الاختلاف البسيط أستاذًا آخر.
 */
private fun sameTeacher(a: String, b: String): Boolean {
    fun n(t: String) = t.lowercase().replace(Regex("[أإآ]"), "ا").replace('ة', 'ه').replace('ى', 'ي')
        .replace(Regex("^\\s*(الاستاذ|الاستاذه|استاذ|prof\\.?|m\\.|mme)\\s+"), "")
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
    val x = n(a); val y = n(b)
    if (x.isEmpty() || y.isEmpty() || x == y) return true
    val wx = x.split(" ").toSet(); val wy = y.split(" ").toSet()
    // كل كلمات الاسم الأقصر موجودة في الأطول (مثل «محمد أحمد» و«محمد أحمد سالم»)
    return wx.containsAll(wy) || wy.containsAll(wx)
}

/** نتيجة رفع ملفات النتائج دفعة واحدة. */
private data class BatchResult(
    val imported: List<String>,          // «1AS1 — الرياضيات (مختار)»
    val duplicates: Int,                 // ملفات مكررة تُجووزت
    val rejected: List<String>,          // ملفات ليست سجلات نتائج لأقسامك، أو تالفة
    val warnings: List<String>,          // طلاب في السجل وليسوا في القسم أو العكس
    val term: Int,                       // الفصل الذي فُحص اكتماله
    val missing: List<String>            // ما ينقص بعد الرفع: «1AS1: الفرنسية (ناقصة عند 3)»
)

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
    private val incomingFiles = mutableStateOf<List<Uri>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // تطبيق الأستاذ: متابعة تنزيل صور الكتب من المتجر (تظهر نسبتها في التخطيط السنوي)
        com.elyedali.daftarustadh.data.BookPack.start(this)
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
                // الدخول بحساب Google أولًا (مرة واحدة؛ يبقى الحساب محفوظًا ويعمل التطبيق بعدها بلا إنترنت)
                var signedIn by remember { mutableStateOf(com.elyedali.daftarustadh.auth.DsAuth.user) }
                if (signedIn == null) {
                    com.elyedali.daftarustadh.ui.LoginScreen(onSignedIn = { signedIn = it })
                    return@DaftarTheme
                }
                LaunchedEffect(signedIn?.uid) {
                    val me = signedIn ?: return@LaunchedEffect
                    com.elyedali.daftarustadh.auth.TrustedClock.init(this@MainActivity)
                    // الاشتراكات المحفوظة تُطبّق فورًا (بلا إنترنت)، ثم تُحدَّث من الخادم
                    com.elyedali.daftarustadh.auth.DsSubscriptions.apply(db)
                    com.elyedali.daftarustadh.auth.DsAuth.registerUser(me)
                    withContext(Dispatchers.IO) { com.elyedali.daftarustadh.auth.TrustedClock.syncWithServer() }
                    com.elyedali.daftarustadh.auth.DsSubscriptions.refresh(db)
                }
                val scope = rememberCoroutineScope()
                val institution by institutionDao.observe().collectAsState(initial = null)
                val institutions by institutionDao.observeAll().collectAsState(initial = emptyList())
                val schoolNames = institutions.associate { it.id to it.schoolName }
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                var classId by rememberSaveable { mutableStateOf<String?>(null) }
                var fromTeaching by rememberSaveable { mutableStateOf(false) }
                var showSubjects by remember { mutableStateOf(false) }
                var fileResult by remember { mutableStateOf<FileResult?>(null) }
                // ملف الإدارة الكامل وصل من تطبيق إدارة آخر: يُعرض ملخصه ويُطلب التأكيد قبل إدراجه
                var adminFilePending by remember { mutableStateOf<com.elyedali.daftarustadh.transfer.AdminFullFile?>(null) }
                // طاقم الإدارة (أرقام واتساب) لإرسال ملف الإدارة الكامل مباشرة
                var showStaffShare by remember { mutableStateOf(false) }
                val staffText by remember { metaDao.observe(com.elyedali.daftarustadh.ui.AdminStaff.KEY) }.collectAsState(initial = null)
                val inst = institution
                // تطبيق الأستاذ: مؤسسات سجّلها بنفسه (عمله الفردي، دون ملف من الإدارة)
                val selfSchoolIds = if (AppRole.isAdmin) emptySet() else institutions.filter { it.isOwn }.map { it.id }.toSet()
                var addingSelfSchool by rememberSaveable { mutableStateOf(false) }
                var importBack by rememberSaveable { mutableStateOf(Screen.ADMIN_CLASS) }
                val isFrUi = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "fr"

                val teachingClasses by remember { classDao.observeTeaching() }.collectAsState(initial = emptyList())
                val allClasses by remember { classDao.observeAll() }.collectAsState(initial = emptyList())
                val homeGenderCounts by remember { studentDao.observeGenderCounts() }.collectAsState(initial = emptyList())
                var selectedYear by rememberSaveable { mutableStateOf<String?>(null) }
                // وضع التطبيق ثابت: «إدارة DS» = الإدارة، «دفتر الأستاذ DS» = الأستاذ
                val homeMode: String = com.elyedali.daftarustadh.ui.APP_MODE
                // تطبيق الأستاذ: المؤسسة المختارة بعد السنة (قد يدرّس في أكثر من مؤسسة)
                var teacherSchool by rememberSaveable { mutableStateOf<String?>(null) }
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
                // تفعيل الأستاذ (190 أوقية للسنة): التخطيط والتحاضير للفصلين 2 و3؛ رصد النتائج مجاني دائمًا
                val teacherActText by remember { metaDao.observe(com.elyedali.daftarustadh.data.TeacherActivation.KEY) }.collectAsState(initial = null)
                val teacherActivated = remember(teacherActText) { com.elyedali.daftarustadh.data.TeacherActivation.decode(teacherActText) }
                val teacherReqText by remember { metaDao.observe(com.elyedali.daftarustadh.data.TeacherActivation.REQUESTED_KEY) }.collectAsState(initial = null)
                val teacherRequested = remember(teacherReqText) { com.elyedali.daftarustadh.data.TeacherActivation.decode(teacherReqText) }
                var teacherActivateYear by remember { mutableStateOf<String?>(null) }
                var activationBack by rememberSaveable { mutableStateOf(Screen.HOME) }
                val ttSettingsText by remember { metaDao.observe(com.elyedali.daftarustadh.data.TimetableSettings.KEY) }.collectAsState(initial = null)
                val ttSettings = remember(ttSettingsText) {
                    // التوقيت ثابت (08:00–14:00، الاثنين–السبت)؛ يُحتفظ فقط بوقت حصتي آخر النهار الذي اختارته المؤسسة
                    com.elyedali.daftarustadh.data.TimetableSettings.fixed(com.elyedali.daftarustadh.data.TimetableSettings.decode(ttSettingsText).eveningStart)
                }
                val ttAssignments by remember { timetableDao.observeAssignments() }.collectAsState(initial = emptyList())
                val ttTeachersText by remember { metaDao.observe(com.elyedali.daftarustadh.data.TimetableTeachers.KEY) }.collectAsState(initial = null)
                val ttTeachers = remember(ttTeachersText) { com.elyedali.daftarustadh.data.TimetableTeachers.decode(ttTeachersText) }
                val ttSlots by remember { timetableDao.observeSlots() }.collectAsState(initial = emptyList())
                val ttApprovedSig by remember { metaDao.observe(TT_APPROVED_KEY) }.collectAsState(initial = null)
                val ttManualText by remember { metaDao.observe(TT_MANUAL_KEY) }.collectAsState(initial = null)
                // مواد الأستاذ التي أسندتها الإدارة في حزمته، لكل قسم
                val bundleSubjectsMeta by remember { metaDao.observePrefix("bundle_subjects|") }.collectAsState(initial = emptyList())
                fun bundleSubjectsOf(classId: String): String? =
                    bundleSubjectsMeta.firstOrNull { it.key == com.elyedali.daftarustadh.transfer.TeacherBundleImporter.subjectsKey(classId) }?.value
                val ttManual = ttManualText == "1"
                val myTimetable by remember { timetableDao.observeTeacherTimetable() }.collectAsState(initial = emptyList())
                var ttGenerating by remember { mutableStateOf(false) }
                var ttResult by remember { mutableStateOf<String?>(null) }
                var ttInitialClass by rememberSaveable { mutableStateOf<String?>(null) }
                var ttInitialTab by remember { mutableStateOf<Int?>(null) }
                var gradesInitialTerm by remember { mutableStateOf(1) }
                var askTtUnlock by remember { mutableStateOf(false) }
                var generalRemarksBack by remember { mutableStateOf(Screen.KASHF_SETTINGS) }
                var recordsSent by remember { mutableStateOf(setOf<String>()) }
                var printWorking by remember { mutableStateOf(false) }
                // صفحة المشاركة مع الوراقة: BOOKS دفاتر الإدارة، RECORDS سجلات الأساتذة، TIMETABLES جداول الأساتذة
                var printKind by rememberSaveable { mutableStateOf("BOOKS") }
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
                        studentDao.listByClass(c.id).forEach { com.elyedali.daftarustadh.data.StudentPhotos.delete(this@MainActivity, it.id) }
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

                /**
                 * سجل الأستاذ لمادة في صورة PDF يحمل البيانات: (المحتوى، اسم الملف).
                 * @param uptoTerm عند رفع نتائج فصل لكل الأقسام: لا يُرسل ما بعد هذا الفصل.
                 */
                suspend fun buildRecordPdf(c: ClassEntity, code: String, name: String, uptoTerm: Int? = null): Pair<ByteArray, String>? {
                        val owner = institutionDao.getById(c.institutionId) ?: return null
                        val subject = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects?.firstOrNull { it.code == code } ?: return null
                        val list = studentDao.listByClass(c.id)
                        // كل ما رصده الأستاذ حتى هذا الفصل: الامتحانات والاختبارات (بما فيها الإضافية)،
                        // فتظهر الاختبارات في كشفي الفصلين الأول والثاني (شكليًا) وتُحسب في الكشف السنوي
                        val g = gradeDao.listSubject(c.id, code).filter { gr ->
                            val t = GradeSlot.testTerm(gr.slot) ?: gr.slot.drop(1).toIntOrNull() ?: 0
                            uptoTerm == null || t <= uptoTerm }
                        val mode = mySubjects.firstOrNull { it.subjectCode == code }?.testsMode ?: TestsMode.AVERAGE
                        val record = RecordFile(
                            institutionId = owner.id, wilaya = owner.wilaya, schoolName = owner.schoolName,
                            schoolListId = owner.schoolListId, classId = c.id, schoolYear = c.schoolYear,
                            levelCode = c.levelCode, number = c.number, subjectCode = code,
                            subjectNameAr = subject.nameAr, subjectNameFr = subject.nameFr,
                            // الاسم الذي سجّلته به إدارة هذه المؤسسة (من ملفها)، فيطابق إسنادها ولا يُرفض عندها
                            teacherName = metaDao.get(com.elyedali.daftarustadh.transfer.TeacherBundleImporter.nameKey(owner.id))
                                ?.takeIf { it.isNotBlank() } ?: name,
                            testsMode = mode,
                            students = TransferCodec.buildRecordStudents(list, g)
                        )
                        val bytes = withContext(Dispatchers.Default) { TransferCodec.encodeRecord(record) }
                        // الصفحة الثانية من الملف: سجل المادة نفسه بأسماء الطلاب ونقاطهم، يُقرأ ويُطبع في الإدارة
                        val labels = recordLabels(c, owner, subject, name)
                        val byStudent = g.groupBy { it.studentId }.mapValues { e -> e.value.associate { it.slot to it.value } }
                        return withContext(Dispatchers.Default) {
                            ShareHelper.wrapPdf(this@MainActivity, bytes, TransferCodec.recordFileName(record),
                                extra = { doc, no -> TeacherRecordPdf.drawInto(doc, no, list, labels, byStudent) })
                        }
                }

                /** إرسال سجل الأستاذ لمادة: ملف يحمل نتائجه وطابعه، يُرفع عند المكلّف بالقسم. */
                fun sendRecord(c: ClassEntity, code: String, name: String) {
                    scope.launch {
                        val (bytes, fileName) = buildRecordPdf(c, code, name) ?: return@launch
                        val file = java.io.File(ShareHelper.freshShareDir(this@MainActivity), fileName).apply { writeBytes(bytes) }
                        ShareHelper.shareFiles(this@MainActivity, listOf(file), "application/pdf", getString(R.string.share_record),
                            getString(R.string.dsq_caption))
                    }
                }

                /** رفع نتائج فصل للإدارة: سجلات كل مواد الأستاذ في كل الأقسام المكتملة، دفعة واحدة. */
                fun uploadTermRecords(term: Int, pairs: List<Pair<ClassEntity, String>>, name: String) {
                    scope.launch {
                        val dir = ShareHelper.freshShareDir(this@MainActivity)
                        val files = pairs.mapNotNull { (c, code) ->
                            buildRecordPdf(c, code, name, uptoTerm = term)?.let { (bytes, fileName) ->
                                java.io.File(dir, fileName).apply { writeBytes(bytes) }
                            }
                        }
                        if (files.isEmpty()) return@launch
                        ShareHelper.shareFiles(this@MainActivity, files, "application/pdf", getString(R.string.share_record),
                            getString(R.string.dsq_caption))
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
                /**
                 * @param onlyMissing الجدول موجود (مُدخل يدويًا أو مولَّد): لا يُغيَّر أستاذ مادة له أستاذ صالح،
                 * ويُسند فقط ما ليس له أستاذ، فلا يفسد الجدول الذي أدخلته الإدارة.
                 */
                suspend fun distributeTeachers(teachers: List<com.elyedali.daftarustadh.data.TimetableTeacher> = ttTeachers, onlyMissing: Boolean = false) {
                    val classSubjects = managedClasses.map { c ->
                        Triple(c.id, c.levelCode, CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)
                            ?.subjects.orEmpty().map { it.code to it.weeklyHours })
                    }
                    val valid = { a: com.elyedali.daftarustadh.data.TimetableAssignmentEntity ->
                        teachers.any { t -> t.name == a.teacherName && a.subjectCode in t.all } }
                    val keep = if (onlyMissing) timetableDao.listAssignments().filter(valid).associateBy { it.classId to it.subjectCode } else emptyMap()
                    val result = com.elyedali.daftarustadh.data.TimetableTeachers.distribute(teachers, classSubjects)
                        .filter { (it.classId to it.subjectCode) !in keep }
                    val covered = teachers.flatMap { it.all }.toSet()
                    classSubjects.forEach { (cid, _, subs) ->
                        subs.filter { it.first in covered && (cid to it.first) !in keep }.forEach { (code, _) -> timetableDao.deleteAssignment(cid, code) }
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
                        // توزيع تلقائي: أقسام كل مادة تُوزَّع على أساتذتها حسب الساعات المبرمجة لكل قسم
                        // (إلا في الإدخال اليدوي: الإدارة تختار أستاذ كل حصة بنفسها)
                        if (metaDao.get(TT_MANUAL_KEY) != "1")
                            distributeTeachers(newList, onlyMissing = ttSlots.any { s -> managedClasses.any { it.id == s.classId } })
                    }
                }

                /** الإدخال اليدوي: جداول كل الأقسام فارغة، وتعيينات الأساتذة تُمسح، ويُلغى الاعتماد. */
                fun startManualTimetable() {
                    val ids = managedClasses.map { it.id }
                    scope.launch {
                        metaDao.put(AppMetaEntity(TT_MANUAL_KEY, "1"))
                        metaDao.put(AppMetaEntity(TT_APPROVED_KEY, ""))
                        timetableDao.deleteSlotsOf(ids)
                        ttAssignments.filter { it.classId in ids }.forEach { timetableDao.deleteAssignment(it.classId, it.subjectCode) }
                        ttResult = null
                    }
                }

                /** توليد الجدول الزمني لكل أقسام الإدارة في السنة الحالية. */
                fun generateTimetable() {
                    val classesNow = managedClasses
                    if (classesNow.isEmpty()) return
                    ttGenerating = true
                    scope.launch {
                        // التوليد التلقائي ينهي الإدخال اليدوي
                        metaDao.put(AppMetaEntity(TT_MANUAL_KEY, ""))
                        // قبل كل توليد: توزيع تلقائي للأقسام على أساتذة كل مادة (يشمل الأقسام المضافة حديثًا)
                        if (ttTeachers.isNotEmpty()) distributeTeachers()
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

                /**
                 * تبديل حصتين في جدول قسم (أو نقل حصة إلى وقت فارغ)، بعد التأكد أن الأستاذين غير مشغولين في قسم آخر.
                 * @param keepApproval الجدول معتمد: التبديل الصحيح لا يغيّر الساعات ولا يُحدث تضاربًا، فيبقى معتمدًا.
                 */
                fun swapSlots(classId: String, a: Pair<Int, Int>, b: Pair<Int, Int>, keepApproval: Boolean = false): String? {
                    val placed = ttSlots.map { com.elyedali.daftarustadh.timetable.TimetableGenerator.Placed(it.classId, it.day, it.period, it.subjectCode, it.teacherName) }
                    com.elyedali.daftarustadh.timetable.TimetableGenerator.canSwap(placed, classId, a, b)?.let { return it }
                    val sa = ttSlots.firstOrNull { it.classId == classId && it.day == a.first && it.period == a.second }
                    val sb = ttSlots.firstOrNull { it.classId == classId && it.day == b.first && it.period == b.second }
                    scope.launch {
                        timetableDao.deleteSlot(classId, a.first, a.second)
                        timetableDao.deleteSlot(classId, b.first, b.second)
                        val moved = listOfNotNull(
                            sa?.copy(day = b.first, period = b.second),
                            sb?.copy(day = a.first, period = a.second)
                        )
                        timetableDao.saveSlots(moved)
                        if (keepApproval) {
                            // البصمة الجديدة بنفس طريقة ttSignature
                            val managed = (ttSlots.filter { it !== sa && it !== sb } + moved)
                                .filter { sl -> managedClasses.any { it.id == sl.classId } }
                            val sig = managed.map { "${it.classId}|${it.day}|${it.period}|${it.subjectCode}|${it.teacherName}" }
                                .sorted().joinToString(";").hashCode().toString() + ":" + managed.size
                            metaDao.put(AppMetaEntity(TT_APPROVED_KEY, sig))
                        }
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

                /** الجدول العام: جداول جميع الأقسام في جدول واحد (PDF). */
                fun shareAllClassesTimetable() {
                    scope.launch {
                        val owner = inst ?: return@launch
                        val year = adminYear ?: owner.schoolYear
                        val cls = managedClasses.filter { it.schoolYear == year }.sortedWith(compareBy({ it.levelCode }, { it.number }))
                        if (cls.isEmpty()) return@launch
                        val st = ttSettings
                        val dayAr = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")
                        val dayFr = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
                        val byClass = ttSlots.groupBy { it.classId }.mapValues { (_, l) -> l.associateBy { it.day to it.period } }
                        val bytes = withContext(Dispatchers.Default) {
                            com.elyedali.daftarustadh.pdf.AllClassesTimetablePdf.build(
                                com.elyedali.daftarustadh.pdf.AllClassesTimetablePdf.Info(
                                    title = "الجدول العام لجميع الأقسام — Emploi du temps général",
                                    lineAr = "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — السنة الدراسية: \u2066$year\u2069",
                                    lineFr = "Établissement : ${owner.schoolName} — Année scolaire : $year",
                                    classNames = cls.map { it.name },
                                    dayLabels = st.days.map { "${dayAr[it]}\n${dayFr[it]}" },
                                    periodLabels = st.days.map { d -> (0 until st.periodsOn(d)).map { "${it + 1}\n${st.label(it)}" } },
                                    cells = cls.map { c ->
                                        val at = byClass[c.id].orEmpty()
                                        st.days.map { d -> (0 until st.periodsOn(d)).map { p ->
                                            at[d to p]?.let { s -> subjectName(s.subjectCode) + if (s.teacherName.isNotBlank()) "\n${s.teacherName}" else "" }.orEmpty()
                                        } }
                                    },
                                    footer = "دفتر الأستاذ DS",
                                    seal = sealBitmap
                                )
                            )
                        }
                        val name = "الجدول العام - Emploi du temps général - ${owner.schoolName} - $year.pdf"
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
                    }
                    // مواد بلا أستاذ: سطر لكل مادة مع أقسامها
                    ttManagedSlots.filter { it.teacherName.isBlank() }.groupBy { it.subjectCode }.forEach { (code, l) ->
                        val names = l.map { it.classId }.distinct().mapNotNull { id -> managedClasses.firstOrNull { it.id == id } }
                            .sortedWith(compareBy({ it.levelCode }, { it.number })).joinToString("، ") { it.name }
                        out += getString(R.string.tt_pb_no_teacher, subjectName(code), names)
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

                /** صفحة جدول الأستاذ (PDF) من الجدول الزمني للمؤسسة. */
                fun teacherTimetableInfo(teacher: String, owner: com.elyedali.daftarustadh.data.InstitutionEntity): com.elyedali.daftarustadh.pdf.TimetablePdf.Info {
                    val names = managedClasses.associate { it.id to it.name }
                    val slotsMine = ttManagedSlots.filter { it.teacherName == teacher }.groupBy { it.day to it.period }
                    return timetableInfo(
                        "جدول الأستاذ: $teacher — Emploi du temps du professeur",
                        "المؤسسة: ${owner.schoolName} — السنة الدراسية: \u2066${adminYear ?: owner.schoolYear}\u2069",
                        "Établissement : ${owner.schoolName} — Année scolaire : ${adminYear ?: owner.schoolYear}"
                    ) { d, p -> slotsMine[d to p]?.let { l -> val (ar, fr) = subjectArFr(l.first().subjectCode); l.joinToString(" / ") { names[it.classId].orEmpty() } + "\n$ar\n$fr" }.orEmpty() }
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

                /** مادة السجل من مواد مستوى القسم؟ (سجل من برنامج سنة أخرى أو مادة لا يدرسها القسم يُرفض) */
                fun subjectOfLevel(c: ClassEntity, code: String): Boolean =
                    CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty().any { it.code == code }

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
                            val any = TransferCodec.decodeAny(data)
                            // كل ملف لتطبيقه: سجلات الأساتذة وغيابهم وملف القسم الكامل لتطبيق الإدارة،
                            // وحزمة الأستاذ وجدوله وملف القسم للأساتذة لتطبيق الأستاذ
                            val forAdmin = any is RecordFile || any is com.elyedali.daftarustadh.transfer.AbsenceFile ||
                                any is com.elyedali.daftarustadh.transfer.AdminFullFile ||
                                (any is ClassFile && any.type == ClassFileType.FULL)
                            val forTeacher = any is com.elyedali.daftarustadh.transfer.TeacherBundle || any is com.elyedali.daftarustadh.transfer.TimetableFile ||
                                (any is ClassFile && any.type == ClassFileType.TEACHER)
                            if (com.elyedali.daftarustadh.AppRole.isAdmin && forTeacher) throw WrongAppException(com.elyedali.daftarustadh.AppRole.TEACHER_APP_NAME)
                            if (!com.elyedali.daftarustadh.AppRole.isAdmin && forAdmin) throw WrongAppException(com.elyedali.daftarustadh.AppRole.ADMIN_APP_NAME)
                            when (any) {
                                is RecordFile -> when (val chk = recordImporter.check(any)) {
                                    is RecordCheck.Ready -> if (!subjectOfLevel(chk.schoolClass, any.subjectCode))
                                        FileResult.Message(getString(R.string.record_bad_subject, any.subjectNameAr, chk.schoolClass.name))
                                        else { recordCheck = chk; null }
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
                                    // معلومات الأستاذ تُملأ من الملف (اسمه وموادّه) إن لم يسجّلها، فلا يُطلب منه إلا رصد النتائج
                                    if (teacherProfile == null && any.teacherName.isNotBlank())
                                        metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.ui.TeacherProfile.KEY,
                                            com.elyedali.daftarustadh.ui.TeacherProfile(any.teacherName, "",
                                                any.classes.flatMap { it.second }.distinct().take(com.elyedali.daftarustadh.data.TimetableTeachers.MAX_SUBJECTS)).encode()))
                                    if (out.saved.isNotEmpty()) {
                                        selectedYear = any.classes.first().first.schoolClass.schoolYear
                                        teacherSchool = null
                                        screen = Screen.HOME
                                    }
                                    FileResult.Message(
                                        getString(R.string.sr_received, any.schoolName, out.saved.joinToString("، "), out.subjects, out.periods) +
                                            if (out.conflicts.isEmpty()) "" else "\n\n" + getString(R.string.sr_received_conflicts, out.conflicts.joinToString("، "))
                                    )
                                }
                                is ClassFile -> { importedYear = any.schoolClass.schoolYear; FileResult.Done(importer.save(any)) }
                                is com.elyedali.daftarustadh.transfer.AdminFullFile -> { adminFilePending = any; null }
                                else -> FileResult.Invalid
                            }
                        } catch (e: PdfNotImportableException) {
                            FileResult.Message(getString(R.string.file_is_pdf))
                        } catch (e: WrongAppException) {
                            FileResult.Message(getString(R.string.file_other_app, e.app))
                        } catch (e: Exception) {
                            FileResult.Invalid
                        }
                        if (result != null) fileResult = result
                        if (result is FileResult.Done && result.outcome is ImportOutcome.Success) {
                            // يفتح الصفحة الرئيسية على سنة القسم ووضعه (الأستاذ أو الإدارة)
                            val ok = result.outcome as ImportOutcome.Success
                            importedYear?.let { selectedYear = it }
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

                // ===== رفع ملفات النتائج دفعة واحدة =====
                var batchResult by remember { mutableStateOf<BatchResult?>(null) }
                var batchWorking by remember { mutableStateOf(false) }
                fun processRecordsBatch(uris: List<Uri>) {
                    if (uris.isEmpty()) return
                    batchWorking = true
                    scope.launch {
                        val rejected = ArrayList<String>()
                        val ready = ArrayList<RecordCheck.Ready>()
                        val seenBytes = HashSet<Int>()
                        var duplicates = 0
                        var older = 0
                        for (uri in uris) {
                            val name = runCatching {
                                contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                                    if (c.moveToFirst()) c.getString(0) else null
                                }
                            }.getOrNull() ?: uri.lastPathSegment.orEmpty()
                            try {
                                val bytes = withContext(Dispatchers.IO) { contentResolver.openInputStream(uri)?.use { it.readBytes() } } ?: throw IllegalStateException()
                                // نفس الملف مرتين (حُمّل من واتساب أكثر من مرة): يُتجاوز
                                if (!seenBytes.add(bytes.contentHashCode())) { duplicates++; continue }
                                val data = if (com.elyedali.daftarustadh.transfer.DsqPdf.isPdf(bytes))
                                    com.elyedali.daftarustadh.transfer.DsqPdf.extract(bytes) ?: throw PdfNotImportableException() else bytes
                                when (val any = TransferCodec.decodeAny(data)) {
                                    is RecordFile -> when (val chk = recordImporter.check(any)) {
                                        is RecordCheck.Ready -> when {
                                            !subjectOfLevel(chk.schoolClass, any.subjectCode) ->
                                                rejected += "$name — " + getString(R.string.record_bad_subject, any.subjectNameAr, chk.schoolClass.name)
                                            // سجل أقدم من الذي رُفع لهذه المادة: يُتجاوز حتى لا تعود نقاط قديمة
                                            chk.older -> { older++; }
                                            // ليس الأستاذ المسند لهذه المادة في الجدول الزمني: لا يُرفع دفعة واحدة (يُرفع وحده بعد التأكد)
                                            ttAssignments.firstOrNull { it.classId == chk.schoolClass.id && it.subjectCode == any.subjectCode }?.teacherName
                                                ?.takeIf { it.isNotBlank() && !sameTeacher(it, any.teacherName) } != null ->
                                                rejected += "$name — " + getString(R.string.bu_other_teacher, any.teacherName,
                                                    ttAssignments.first { it.classId == chk.schoolClass.id && it.subjectCode == any.subjectCode }.teacherName)
                                            else -> ready += chk
                                        }
                                        is RecordCheck.WrongClass -> rejected += "$name — " + getString(R.string.bu_wrong_class, any.className)
                                        is RecordCheck.NotManaged -> rejected += "$name — " + getString(R.string.record_not_managed, any.className)
                                    }
                                    else -> rejected += "$name — " + getString(R.string.bu_not_record)
                                }
                            } catch (e: Exception) {
                                rejected += "$name — " + getString(R.string.bu_not_record)
                            }
                        }
                        // سجلان لنفس المادة في نفس القسم: يُؤخذ الأحدث إرسالًا، ثم الأكثر نقاطًا
                        val chosen = ready.groupBy { it.schoolClass.id + "|" + it.file.subjectCode }.values.map { group ->
                            duplicates += group.size - 1
                            group.maxWith(compareBy<RecordCheck.Ready>({ it.file.exportedAt }, { r -> r.file.students.sumOf { it.grades.size } }))
                        }
                        val imported = ArrayList<String>()
                        val warnings = ArrayList<String>()
                        chosen.forEach { chk ->
                            recordImporter.apply(chk)
                            val label = "${chk.file.className} — ${if (isFrUi) chk.file.subjectNameFr else chk.file.subjectNameAr}" +
                                (if (chk.file.teacherName.isNotBlank()) " (${chk.file.teacherName})" else "")
                            imported += label
                            if (chk.unknownStudents.isNotEmpty() || chk.missingStudents.isNotEmpty())
                                warnings += label + ": " + getString(R.string.bu_students_diff, chk.unknownStudents.size, chk.missingStudents.size)
                            if (chk.byOrder)
                                warnings += label + ": " + getString(R.string.record_by_order, chk.matched)
                            else if (chk.classByName || chk.byRoll > 0)
                                warnings += label + ": " + getString(R.string.record_by_roll, chk.byRoll)
                        }
                        // الفصل الذي يُفحص: آخر فصل وصلت امتحاناته في هذه الملفات
                        val term = (3 downTo 1).firstOrNull { t ->
                            chosen.any { c -> c.file.students.any { st -> st.grades.containsKey(GradeSlot.exam(t)) } }
                        } ?: 1
                        val classes = managedClasses.sortedWith(compareBy({ it.levelCode }, { it.number }))
                        val all = gradeDao.listClasses(classes.map { it.id })
                        val missing = ArrayList<String>()
                        classes.forEach { c ->
                            val subs = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                            val sts = studentDao.listByClass(c.id)
                            val g = all.filter { it.classId == c.id }.groupBy { it.subjectCode }
                                .mapValues { e -> e.value.groupBy { it.studentId }.mapValues { s -> s.value.associate { it.slot to it.value } } }
                            val parts = subs.mapNotNull { sub ->
                                val n = sts.count { st -> !GradeMath.isComplete(term, g[sub.code]?.get(st.id).orEmpty()) }
                                if (n > 0) "${if (isFrUi) sub.nameFr else sub.nameAr} (${getString(R.string.bu_missing_n, n)})" else null
                            }
                            if (parts.isNotEmpty() && sts.isNotEmpty()) missing += "${c.name}: " + parts.joinToString("، ")
                        }
                        batchWorking = false
                        if (older > 0) warnings += getString(R.string.record_older_skipped, older)
                        batchResult = BatchResult(imported, duplicates, rejected, warnings, term, missing)
                    }
                }
                val batchPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris -> processRecordsBatch(uris) }
                val openBatch = { batchPicker.launch(arrayOf("*/*")) }
                // ملفات كثيرة وصلت معًا بـ«مشاركة» من واتساب: الإدارة ترفعها دفعة واحدة، والأستاذ يفتحها واحدًا واحدًا
                val pendingUris by incomingFiles
                LaunchedEffect(pendingUris) {
                    val list = pendingUris
                    if (list.isNotEmpty()) {
                        incomingFiles.value = emptyList()
                        if (com.elyedali.daftarustadh.AppRole.isAdmin) processRecordsBatch(list) else list.forEach { processFile(it) }
                    }
                }

                val chooserTitle = stringResource(R.string.share_chooser)
                /** عنوان لائحة طلاب القسم في ملفات القسم وحزمة الأستاذ. */
                fun studentListInfo(c: ClassEntity, owner: com.elyedali.daftarustadh.data.InstitutionEntity, full: Boolean) =
                    com.elyedali.daftarustadh.pdf.StudentListPdf.Info(
                        title = "لائحة طلاب القسم \u2066${c.name}\u2069 — Liste des élèves de la classe ${c.name}",
                        subtitle = "${owner.schoolName} — \u2066${c.schoolYear}\u2069",
                        full = full
                    )
                fun share(c: ClassEntity, type: ClassFileType) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val list = studentDao.listByClass(c.id)
                        val file = ClassFile(type, owner, c, list)
                        val bytes = withContext(Dispatchers.Default) { ClassFileCodec.encode(file) }
                        // لائحة الطلاب ظاهرة في صفحات الملف نفسه: تُقرأ في واتساب وتُطبع
                        val listInfo = studentListInfo(c, owner, type == ClassFileType.FULL)
                        ShareHelper.share(this@MainActivity, bytes, ClassFileCodec.fileName(file), chooserTitle,
                            extraPages = { doc, no -> com.elyedali.daftarustadh.pdf.StudentListPdf.drawInto(doc, no, list, listInfo) })
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
                            // رابط تحميل تطبيق الأستاذ مع الملف (حين يُحدَّد)
                            .let { cap -> if (com.elyedali.daftarustadh.AppRole.TEACHER_APP_LINK.isBlank()) cap
                                else cap + "\n\n" + getString(R.string.sr_app_link, com.elyedali.daftarustadh.AppRole.TEACHER_APP_LINK) }
                        // صفحة جدول الأستاذ داخل الملف نفسه: يراه في واتساب كوثيقة PDF
                        val ttInfo = teacherTimetableInfo(t.name, owner)
                        // بعد الجدول: لائحة طلاب كل قسم من أقسامه (رقم النداء والاسم)
                        val lists = bundle.classes.map { (f, _) -> f.students to studentListInfo(f.schoolClass, owner, false) }
                        ShareHelper.shareToWhatsApp(this@MainActivity, bytes, bundle.fileName(), t.phone, caption, chooserTitle) { doc, no ->
                            var next = com.elyedali.daftarustadh.pdf.TimetablePdf.drawInto(doc, no, ttInfo)
                            lists.forEach { (st, info) -> next = com.elyedali.daftarustadh.pdf.StudentListPdf.drawInto(doc, next, st, info) }
                            next
                        }
                        recordsSent = recordsSent + t.name
                    }
                }

                /**
                 * أسابيع السنة الدراسية (أكتوبر ← يونيو): صفحة لكل أسبوع من الاثنين إلى السبت،
                 * وفي كل يوم ست حصص.
                 */
                fun schoolMonths(schoolYear: String): List<com.elyedali.daftarustadh.pdf.AbsencePdf.Month> {
                    val y1 = schoolYear.take(4).toIntOrNull() ?: return emptyList()
                    val ar = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
                    val fr = listOf("janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre")
                    val short = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")   // 0 = الاثنين
                    val schoolDays = ttSettings.days.toSet()
                    val cal = java.util.Calendar.getInstance().apply { clear(); set(y1, 9, 1) }
                    // نبدأ من اثنين الأسبوع الذي فيه فاتح أكتوبر
                    while ((cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 != 0) cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
                    val start = java.util.Calendar.getInstance().apply { clear(); set(y1, 9, 1) }
                    val end = java.util.Calendar.getInstance().apply { clear(); set(y1 + 1, 5, 30) }
                    val weeks = ArrayList<com.elyedali.daftarustadh.pdf.AbsencePdf.Month>()
                    while (!cal.after(end)) {
                        val dates = ArrayList<Triple<Int, Int, Int>>()   // (اليوم، الشهر، السنة)
                        val days = ArrayList<com.elyedali.daftarustadh.pdf.AbsencePdf.Day>()
                        repeat(7) { idx ->
                            if (idx in schoolDays && !cal.before(start) && !cal.after(end)) {
                                val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                                days += com.elyedali.daftarustadh.pdf.AbsencePdf.Day(d, short[idx])
                                dates += Triple(d, cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.YEAR))
                            }
                            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
                        }
                        if (days.isNotEmpty()) {
                            val (d1, m1, a1) = dates.first(); val (d2, m2, a2) = dates.last()
                            val fromAr = if (m1 == m2) "$d1" else if (a1 == a2) "$d1 ${ar[m1]}" else "$d1 ${ar[m1]} $a1"
                            val fromFr = if (m1 == m2) "$d1" else if (a1 == a2) "$d1 ${fr[m1]}" else "$d1 ${fr[m1]} $a1"
                            weeks += com.elyedali.daftarustadh.pdf.AbsencePdf.Month(
                                "أسبوع من $fromAr إلى $d2 ${ar[m2]} $a2",
                                "Semaine du $fromFr au $d2 ${fr[m2]} $a2", days)
                        }
                    }
                    return weeks
                }

                /**
                 * الوراقة: يجهّز ملفات PDF للطباعة (ملف لكل وثيقة يجمع كل الأقسام أو كل الأساتذة)
                 * ثم يشاركها دفعة واحدة (واتساب أو غيره).
                 */
                /** دفتر الغياب (سجل النداء) للسحب والملء باليد: بأسماء الطلاب، أو فارغًا. */
                fun absenceRegisterInfo(c: ClassEntity, owner: com.elyedali.daftarustadh.data.InstitutionEntity,
                                        list: List<com.elyedali.daftarustadh.data.StudentEntity>, blank: Boolean): com.elyedali.daftarustadh.pdf.AbsencePdf.Info {
                    val wilayaFr = SchoolDirectory.wilaya(this@MainActivity, owner.wilaya)?.nameFr ?: owner.wilaya
                    return com.elyedali.daftarustadh.pdf.AbsencePdf.Info(
                        title = "دفتر الغياب — Registre d'appel",
                        lineAr = "المؤسسة: ${owner.schoolName} — الولاية: ${owner.wilaya} — القسم: \u2066${c.name}\u2069 — السنة الدراسية: \u2066${c.schoolYear}\u2069",
                        lineFr = "Établissement : ${owner.schoolName} — Wilaya : $wilayaFr — Classe : ${c.name} — Année scolaire : ${c.schoolYear}",
                        shortLine = "دفتر الغياب — Registre d'appel — \u2066${c.name}\u2069 — \u2066${c.schoolYear}\u2069",
                        months = schoolMonths(c.schoolYear),
                        // فارغ: أسطر بلا أسماء تُملأ باليد
                        students = if (blank) emptyList() else list.map { s -> com.elyedali.daftarustadh.pdf.AbsencePdf.Student(s.rollNumber, s.nameAr, s.nameFr) },
                        rollHeader = "ر.ن\nN°",
                        nameHeader = "الاسم الكامل — Nom et Prénom",
                        totalHeader = "المجموع\nTotal",
                        page = { p, n -> "\u2066${c.name}\u2069 — صفحة $p من $n — Page $p / $n" },
                        seal = sealBitmap
                    )
                }

                fun printAbsenceRegister(c: ClassEntity, blank: Boolean) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val list = studentDao.listByClass(c.id)
                        val info = absenceRegisterInfo(c, owner, list, blank)
                        val bytes = withContext(Dispatchers.Default) { com.elyedali.daftarustadh.pdf.AbsencePdf.build(info) }
                        val name = "دفتر الغياب - Registre d'appel - ${c.name} - ${owner.schoolName} - ${c.schoolYear}.pdf"
                            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        ShareHelper.share(this@MainActivity, bytes, name, getString(R.string.reg_absence), "application/pdf")
                    }
                }

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
                                    val f = java.io.File(dir, "دفتر الغياب - Registre d'appel - $tag.pdf")
                                    com.elyedali.daftarustadh.pdf.PdfCommon.renderToFile(f) { doc ->
                                        var no = 1
                                        classes.forEach { c ->
                                            no = com.elyedali.daftarustadh.pdf.AbsencePdf.drawInto(doc, no,
                                                absenceRegisterInfo(c, owner, students[c.id].orEmpty(), choice.absenceBlank).copy(seal = seal))
                                        }
                                    }
                                    files += f
                                }
                                if (choice.cards) {
                                    val f = java.io.File(dir, "بطاقات الطلاب - Cartes d'élèves - $tag.pdf")
                                    val info = com.elyedali.daftarustadh.pdf.StudentCardPdf.Info(owner.schoolName, adminYear ?: owner.schoolYear, seal)
                                    com.elyedali.daftarustadh.pdf.PdfCommon.renderToFile(f) { doc ->
                                        val cards = classes.flatMap { c ->
                                            students[c.id].orEmpty().sortedBy { it.rollNumber }.map { s ->
                                                com.elyedali.daftarustadh.pdf.StudentCardPdf.Card(
                                                    s.nameAr.ifBlank { s.nameFr }, if (s.nameAr.isNotBlank()) s.nameFr else "", s.rim, c.name, s.rollNumber,
                                                    { com.elyedali.daftarustadh.data.StudentPhotos.load(this@MainActivity, s.id) })
                                            }
                                        }
                                        com.elyedali.daftarustadh.pdf.StudentCardPdf.drawInto(doc, 1, cards, info)
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
                                getString(R.string.ps_share), getString(R.string.ps_message, owner.schoolName))
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(this@MainActivity, getString(R.string.ps_error), android.widget.Toast.LENGTH_LONG).show()
                        } finally {
                            printWorking = false
                        }
                    }
                }

                /** بطاقات طلاب القسم (PDF): عشر بطاقات في كل صفحة، لمشاركتها مع الوراقة. */
                fun shareClassCards(c: ClassEntity) {
                    scope.launch {
                        val owner = institutionDao.getById(c.institutionId) ?: return@launch
                        val list = studentDao.listByClass(c.id).sortedBy { it.rollNumber }
                        if (list.isEmpty()) return@launch
                        val info = com.elyedali.daftarustadh.pdf.StudentCardPdf.Info(owner.schoolName, c.schoolYear, sealBitmap)
                        val cards = list.map { s ->
                            com.elyedali.daftarustadh.pdf.StudentCardPdf.Card(
                                s.nameAr.ifBlank { s.nameFr }, if (s.nameAr.isNotBlank()) s.nameFr else "", s.rim, c.name, s.rollNumber,
                                { com.elyedali.daftarustadh.data.StudentPhotos.load(this@MainActivity, s.id) })
                        }
                        val tag = "${c.name} - ${owner.schoolName} - ${c.schoolYear}".replace(Regex("[\\\\/:*?\"<>|]"), " ")
                        val bytes = withContext(Dispatchers.Default) { com.elyedali.daftarustadh.pdf.StudentCardPdf.build(cards, info) }
                        ShareHelper.share(this@MainActivity, bytes, "بطاقات الطلاب - Cartes d'élèves - $tag.pdf", getString(R.string.cards_button), "application/pdf")
                    }
                }

                when (screen) {
                    Screen.HOME -> if (teacherSchool != null) BackHandler { teacherSchool = null } else if (selectedYear != null) BackHandler { selectedYear = null }
                    Screen.STUDENTS -> BackHandler { screen = if (fromTeaching) Screen.TEACHER_CLASS else Screen.CLASSES }
                    Screen.TEACHER_CLASS -> BackHandler { screen = Screen.TEACHING }
                    Screen.GRADES -> BackHandler { screen = gradesBack }
                    Screen.RESULTS -> BackHandler { screen = Screen.STUDENTS }
                    Screen.STATS -> BackHandler { screen = Screen.HOME }
                    Screen.KASHF_SETTINGS -> BackHandler { screen = Screen.ADMIN_CLASS }
                    Screen.GENERAL_REMARKS -> BackHandler { screen = generalRemarksBack }
                    Screen.IMPORT -> BackHandler { screen = importBack }
                    Screen.ADMIN_CLASS, Screen.TEACHER_HUB -> BackHandler { screen = hubBack }
                    Screen.ALL_CLASSES, Screen.LEGISLATION, Screen.EXCHANGES, Screen.BOOKS, Screen.ABOUT -> BackHandler { screen = Screen.HOME }
                    Screen.ACTIVATION -> BackHandler { screen = activationBack }
                    Screen.TIMETABLE -> BackHandler { screen = ttBack }
                    Screen.MY_TIMETABLE -> BackHandler { screen = Screen.HOME }
                    Screen.QUICK_GRADES -> BackHandler { hubTab = 1; screen = Screen.ADMIN_CLASS }
                    Screen.RECORDS_FOLLOW -> BackHandler { screen = Screen.HOME }
                    Screen.INSTITUTION -> BackHandler { if (addingSelfSchool) addingSelfSchool = false else if (inst == null) selectedYear = null; screen = Screen.HOME }
                    Screen.TEACHER_PROFILE -> BackHandler { if (teacherProfile == null) selectedYear = null; screen = Screen.HOME }
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
                                    "HOME" -> { selectedYear = null; teacherSchool = null; screen = Screen.HOME }
                                    "SYNC" -> android.widget.Toast.makeText(this@MainActivity, getString(R.string.drawer_sync_later), android.widget.Toast.LENGTH_LONG).show()
                                    "LOGOUT" -> { com.elyedali.daftarustadh.auth.DsAuth.signOut(this@MainActivity); signedIn = null }
                                    "TIMETABLE" -> { ttInitialClass = null; ttBack = Screen.HOME; screen = Screen.TIMETABLE }
                                    "MY_TIMETABLE" -> screen = Screen.MY_TIMETABLE
                                    "EXCHANGES" -> screen = Screen.EXCHANGES
                                    "LEGISLATION" -> screen = Screen.LEGISLATION
                                    "ABOUT" -> screen = Screen.ABOUT
                                    // مشاركة كل معلومات الإدارة مع تطبيق إدارة آخر (المدير العام، المراقب العام، عامل مساعد)
                                    "SHARE_ADMIN" -> showStaffShare = true
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
                        existing = if (addingSelfSchool) null else inst,
                        onSave = { s ->
                            scope.launch {
                                institutionDao.save(if (addingSelfSchool) s.copy(isOwn = true) else s)
                                if (addingSelfSchool) { addingSelfSchool = false; teacherSchool = s.id }
                                screen = Screen.HOME
                            }
                        },
                        onCancel = { if (addingSelfSchool) addingSelfSchool = false else if (inst == null) selectedYear = null; screen = Screen.HOME }
                    )

                    screen == Screen.QUICK_GRADES && currentClass != null -> {
                        val c = currentClass!!
                        com.elyedali.daftarustadh.ui.QuickGradesScreen(
                            className = c.name,
                            subjects = com.elyedali.daftarustadh.report.ReportCalc.orderSubjects(
                                CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()),
                            subjectLabel = { if (isFrUi) it.nameFr else it.nameAr },
                            students = students,
                            grades = classGrades,
                            initialSubject = subjectCode,
                            onSave = { code, slot, values ->
                                scope.launch {
                                    val now = System.currentTimeMillis()
                                    values.forEach { (sid, v) -> gradeDao.save(GradeEntity(sid, c.id, code, slot, v, now)) }
                                }
                            },
                            onOpenTable = { code, t ->
                                subjectCode = code; gradesFromAdmin = true; gradesInitialTerm = t
                                gradesBack = Screen.QUICK_GRADES; screen = Screen.GRADES
                            },
                            onBack = { hubTab = 1; screen = Screen.ADMIN_CLASS }
                        )
                    }

                    // متابعة سجلات الأساتذة: من أرسل نتائج كل أقسامه وموادّه في الفصل، ومن بقي
                    screen == Screen.RECORDS_FOLLOW -> {
                        val ids = managedClasses.map { it.id }
                        val fGrades by remember(ids) { gradeDao.observeClasses(ids) }.collectAsState(initial = emptyList())
                        val fStudents by remember(ids) { studentDao.observeByClasses(ids) }.collectAsState(initial = emptyList())
                        com.elyedali.daftarustadh.ui.RecordsFollowScreen(
                            classes = managedClasses,
                            subjectsOf = { c -> com.elyedali.daftarustadh.report.ReportCalc.orderSubjects(
                                CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()).map { it.code } },
                            subjectLabel = { subjectName(it) },
                            assignments = ttAssignments,
                            students = fStudents,
                            grades = fGrades,
                            onUploadMany = openBatch,
                            onGenerate = { c, t -> generateKashf(c, t) },
                            onBack = { screen = Screen.HOME }
                        )
                    }

                    screen == Screen.ABSENCE_CENTER -> com.elyedali.daftarustadh.ui.AbsenceCenterScreen(
                        classes = managedClasses.sortedWith(compareBy({ it.levelCode }, { it.number })),
                        students = allStudents,
                        sessions = allAbsSessions,
                        absences = allAbsences,
                        settings = ttSettings,
                        // الغياب مرتبط بالجدول الزمني المعتمد
                        slots = if (ttApproved) ttManagedSlots else emptyList(),
                        subjectLabel = { subjectName(it) },
                        yearStart = ((adminYear ?: inst?.schoolYear)?.take(4) ?: "2026") + "-10-01",
                        onSave = { session, ids -> scope.launch { absenceDao.saveSession(session, ids) } },
                        onBack = { screen = Screen.HOME },
                        schoolName = inst?.schoolName.orEmpty(),
                        subjectArFr = { subjectArFr(it) },
                        onOpenTimetable = { ttInitialClass = null; ttInitialTab = 0; ttBack = Screen.ABSENCE_CENTER; screen = Screen.TIMETABLE }
                    )

                    screen == Screen.PRINT_SHOP -> {
                        val noStudents = classesWithoutStudents.joinToString("، ") { it.name }
                        val n = managedClasses.size
                        com.elyedali.daftarustadh.ui.PrintShopScreen(
                            title = getString(when (printKind) { "RECORDS" -> R.string.ps_records_title; "TIMETABLES" -> R.string.ps_timetables_title; else -> R.string.ps_books_title }),
                            intro = getString(when (printKind) { "RECORDS" -> R.string.ps_intro_records; "TIMETABLES" -> R.string.ps_intro_timetables; else -> R.string.ps_intro }),
                            records = if (printKind != "RECORDS") null else com.elyedali.daftarustadh.ui.PrintItem(
                                ready = n > 0 && ttApproved && classesWithoutStudents.isEmpty(),
                                note = when {
                                    n == 0 -> getString(R.string.ps_no_classes)
                                    !ttApproved -> getString(R.string.sr_block_timetable)
                                    classesWithoutStudents.isNotEmpty() -> getString(R.string.sr_block_students, noStudents)
                                    else -> getString(R.string.ps_records_note, n)
                                }),
                            timetables = if (printKind != "TIMETABLES") null else com.elyedali.daftarustadh.ui.PrintItem(
                                ready = n > 0 && ttApproved,
                                note = if (ttApproved) getString(R.string.ps_timetables_note, ttManagedSlots.map { it.teacherName }.filter { it.isNotBlank() }.distinct().size)
                                    else getString(R.string.sr_block_timetable)),
                            // دفتر الغياب يُسحب فارغًا أو بأسماء الطلاب (القسم بلا طلاب يخرج بأسطر فارغة)
                            absence = if (printKind != "BOOKS") null else com.elyedali.daftarustadh.ui.PrintItem(
                                ready = n > 0,
                                note = if (n == 0) getString(R.string.ps_no_classes) else getString(R.string.ps_absence_note, n)),
                            textbook = if (printKind != "BOOKS") null else com.elyedali.daftarustadh.ui.PrintItem(
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
                            onCancel = { if (teacherProfile == null) selectedYear = null; screen = Screen.HOME }
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

                    // صفحة «حول التطبيق»: تُجهَّز لاحقًا
                    screen == Screen.ABOUT -> androidx.compose.material3.Surface(Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
                            com.elyedali.daftarustadh.ui.ScreenHeader(stringResource(R.string.menu_about), { screen = Screen.HOME })
                            Text(stringResource(R.string.app_name), fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                fontSize = 20.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Text(stringResource(R.string.about_soon), modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(R.string.drawer_footer), modifier = Modifier.fillMaxWidth(), fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = androidx.compose.ui.graphics.Color.Gray)
                        }
                    }

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
                        manual = ttManual,
                        onStartManual = { startManualTimetable() },
                        onSwap = { cid, a, b -> swapSlots(cid, a, b, keepApproval = ttApproved) },
                        // الإدخال اليدوي: المادة والأستاذ؛ الأستاذ المختار يصير أستاذ المادة في القسم (وكل حصصها فيه)
                        onEditCell = { cid, d, p, code, chosen ->
                            scope.launch {
                                if (code == null) timetableDao.deleteSlot(cid, d, p)
                                else {
                                    val assigned = ttAssignments.firstOrNull { it.classId == cid && it.subjectCode == code }?.teacherName.orEmpty()
                                    val teacher = chosen ?: assigned
                                    if (chosen != null && chosen != assigned) {
                                        if (chosen.isBlank()) timetableDao.deleteAssignment(cid, code)
                                        else timetableDao.saveAssignment(com.elyedali.daftarustadh.data.TimetableAssignmentEntity(cid, code, chosen))
                                        timetableDao.saveSlots(ttSlots.filter { it.classId == cid && it.subjectCode == code }.map { it.copy(teacherName = chosen) })
                                    }
                                    timetableDao.saveSlots(listOf(com.elyedali.daftarustadh.data.TimetableSlotEntity(cid, d, p, code, teacher)))
                                }
                            }
                        },
                        onShareClass = { shareClassTimetable(it) },
                        onShareAll = { shareAllClassesTimetable() },
                        onShareTeacherPdf = { shareTeacherTimetablePdf(it) },
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
                        onRequest = { chosen, total -> scope.launch {
                            val owner = inst
                            val reqCode = kotlinx.coroutines.withTimeoutOrNull(10_000) {
                                com.elyedali.daftarustadh.auth.DsSubscriptions.request(
                                    com.elyedali.daftarustadh.auth.DsSubscriptions.APP_ADMIN, owner, owner?.schoolYear.orEmpty(),
                                    chosen.map { it.id }, chosen.map { it.name }, total)
                            }
                            val msg = buildString {
                                append(getString(R.string.act_msg_intro)).append('\n')
                                if (owner != null) {
                                    append(getString(R.string.act_msg_school, owner.schoolName)).append('\n')
                                    append(getString(R.string.act_msg_wilaya, owner.wilaya)).append('\n')
                                    append(getString(R.string.act_msg_year, owner.schoolYear)).append('\n')
                                }
                                append(getString(R.string.act_msg_classes, chosen.size)).append('\n')
                                chosen.forEach { append("• ${it.name} — ${com.elyedali.daftarustadh.data.ClassActivation.code(it.id)}\n") }
                                append(getString(R.string.act_msg_total, total)).append('\n')
                                append(getString(R.string.act_msg_account, signedIn?.email.orEmpty()))
                                if (reqCode != null) append('\n').append(getString(R.string.act_msg_request, reqCode))
                            }
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${com.elyedali.daftarustadh.data.ClassActivation.ADMIN_PHONE}&text=" + Uri.encode(msg))
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, uri))
                                metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.data.ClassActivation.REQUESTED_KEY,
                                    com.elyedali.daftarustadh.data.ClassActivation.encode(requestedClasses + chosen.map { it.id })))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(this@MainActivity, getString(R.string.ex_no_whatsapp), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } },
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
                        val adminTabOrder = listOf(0, 4, 1, 3, 2)
                        ClassHub(
                            title = c.name,
                            subtitles = listOfNotNull(stringResource(R.string.mode_admin), c.schoolYear),
                            // ترتيب العرض: الطلاب، جدول القسم، النتائج، الكشوف، دفاتر الإدارة
                            // (أرقام التبويبات الداخلية كما هي: 0 الطلاب، 1 النتائج، 2 دفاتر الإدارة، 3 الكشوف، 4 جدول القسم)
                            tabs = adminTabOrder.map { listOf(stringResource(R.string.tab_students), stringResource(R.string.tab_results),
                                stringResource(R.string.ps_books_title), stringResource(R.string.tab_kashf), stringResource(R.string.tab_class_timetable))[it] },
                            icons = adminTabOrder.map { listOf(androidx.compose.material.icons.Icons.Filled.Person, androidx.compose.material.icons.Icons.Filled.Edit,
                                androidx.compose.material.icons.Icons.Filled.CheckCircle, androidx.compose.material.icons.Icons.Filled.List,
                                androidx.compose.material.icons.Icons.Filled.DateRange)[it] },
                            selected = adminTabOrder.indexOf(hubTab).coerceAtLeast(0),
                            onSelect = { hubTab = adminTabOrder[it] },
                            onBack = { screen = hubBack }
                        ) {
                            when (hubTab) {
                                0 -> {
                                    StudentsScreen(
                                        schoolClass = c,
                                        students = students,
                                        onSave = { s -> scope.launch { studentDao.save(s) } },
                                        onDelete = { s -> scope.launch { gradeDao.deleteByStudent(s.id); absenceDao.deleteByStudent(s.id); studentDao.delete(s); com.elyedali.daftarustadh.data.StudentPhotos.delete(this@MainActivity, s.id) } },
                                        onImport = { importBack = Screen.ADMIN_CLASS; screen = Screen.IMPORT },
                                        onShowSubjects = { showSubjects = true },
                                        onBack = { screen = hubBack },
                                        onShare = { type -> share(c, type) },
                                        showHeader = false,
                                        onCards = { shareClassCards(c) },
                                        onDemo = if (com.elyedali.daftarustadh.data.DemoData.allowedFor(null)) ({
                                            scope.launch { studentDao.saveAll(com.elyedali.daftarustadh.data.DemoData.sampleStudents(c.id)) }
                                        }) else null
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
                                    onUploadMany = openBatch,
                                    onQuickEntry = { subjectCode = null; screen = Screen.QUICK_GRADES },
                                    teacherOf = { code ->
                                        val name = ttAssignments.firstOrNull { it.classId == c.id && it.subjectCode == code }?.teacherName?.takeIf { it.isNotBlank() }
                                            ?: records.firstOrNull { it.subjectCode == code }?.teacherName?.takeIf { it.isNotBlank() }
                                        name?.let { n -> n to (ttTeachers.firstOrNull { it.name == n }?.phone.orEmpty()) }
                                    },
                                    onRemind = { phone, msg ->
                                        val uri = Uri.parse("https://wa.me/$phone?text=" + Uri.encode(msg))
                                        try { startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (e: Exception) {
                                            android.widget.Toast.makeText(this@MainActivity, getString(R.string.ex_no_whatsapp), android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onOpenSubject = { code ->
                                        subjectCode = code; gradesFromAdmin = true; gradesBack = Screen.ADMIN_CLASS; screen = Screen.GRADES
                                    },
                                    onGenerate = { term -> generateKashf(c, term) },
                                    onOpenSettings = { screen = Screen.KASHF_SETTINGS },
                                    onBack = { screen = hubBack },
                                    showHeader = false,
                                    showGenerate = false
                                )
                                // «دفاتر الإدارة»: دفتر الغياب + دفتر النصوص، للسحب والملء باليد
                                2 -> {
                                    var bookTab by rememberSaveable { mutableIntStateOf(0) }
                                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                                        androidx.compose.foundation.layout.Row(
                                            Modifier.fillMaxWidth().padding(top = 10.dp),
                                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                                        ) {
                                            androidx.compose.material3.FilterChip(selected = bookTab == 0, onClick = { bookTab = 0 },
                                                label = { Text(stringResource(R.string.tab_absence)) })
                                            androidx.compose.material3.FilterChip(selected = bookTab == 1, onClick = { bookTab = 1 },
                                                label = { Text(stringResource(R.string.tab_textbook)) })
                                        }
                                        if (bookTab == 0) com.elyedali.daftarustadh.ui.AbsenceRegisterTab(
                                            hasStudents = students.isNotEmpty(),
                                            onPrint = { blank -> printAbsenceRegister(c, blank) }
                                        ) else TextbookTab(onPrint = { pages -> printTextbook(c, pages) })
                                    }
                                }
                                // «الكشوف»: كشوف الدرجات
                                3 -> {
                                    val missing = (1..3).associateWith { t -> ReportCalc.missing(t, subjects, students, classGrades).size }
                                    KashfTab(
                                        missingByTerm = if (students.isEmpty()) mapOf(1 to -1, 2 to -1, 3 to -1).mapValues { 1 } else missing,
                                        onGenerate = { term -> generateKashf(c, term) },
                                        onOpenSettings = { screen = Screen.KASHF_SETTINGS },
                                        onOpenGeneralRemarks = { generalRemarksBack = screen; screen = Screen.GENERAL_REMARKS },
                                        locked = c.id !in activatedClasses,
                                        onActivate = { activationBack = Screen.ADMIN_CLASS; screen = Screen.ACTIVATION }
                                    )
                                }
                                else -> {
                                    val at = ttSlots.filter { it.classId == c.id }.associateBy { it.day to it.period }
                                    androidx.compose.foundation.layout.Column(
                                        Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(top = 10.dp),
                                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                                    ) {
                                        // هذه الصفحة تعرض فقط جدول القسم المعتمد؛ الجدول يُعدّ ويُولَّد لكل الأقسام معًا من «الجدول الزمني»
                                        // زر سحب الجدول في الأعلى فيبقى ظاهرًا دائمًا فوق الجدول
                                        if (at.isNotEmpty() && ttApproved) androidx.compose.material3.OutlinedButton(onClick = { shareClassTimetable(c) },
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()) {
                                            Text("🖨️ " + stringResource(R.string.tt_share_class, c.name),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        }
                                        if (at.isEmpty() || !ttApproved) Text(stringResource(R.string.tt_class_not_approved),
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                                        else com.elyedali.daftarustadh.ui.TimetableGrid(
                                            settings = ttSettings,
                                            cell = { d, p -> at[d to p]?.let { subjectName(it.subjectCode) to it.teacherName } },
                                            colorKey = { d, p -> at[d to p]?.subjectCode }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    screen == Screen.TEACHER_HUB && currentClass != null -> {
                        val c = currentClass!!
                        // التخطيط والتحضير للأستاذ فقط: تطبيق الإدارة لا يعرض تبويب التخطيط
                        val isCollege = !AppRole.isAdmin && CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.stage == "college"
                        ClassHub(
                            title = c.name,
                            subtitles = listOfNotNull(stringResource(R.string.mode_teacher), schoolNames[c.institutionId], c.schoolYear),
                            // أقسام الإعدادية: صفحة التخطيط السنوي
                            tabs = listOf(stringResource(R.string.tab_students), stringResource(R.string.tab_results),
                                stringResource(R.string.tab_editor)) + (if (isCollege) listOf(stringResource(R.string.plan_tab)) else emptyList()),
                            icons = listOf(androidx.compose.material.icons.Icons.Filled.Person, androidx.compose.material.icons.Icons.Filled.Edit,
                                androidx.compose.material.icons.Icons.Filled.Create) + (if (isCollege) listOf(androidx.compose.material.icons.Icons.Filled.DateRange) else emptyList()),
                            selected = hubTab,
                            onSelect = { hubTab = it },
                            onBack = { screen = hubBack }
                        ) {
                            when (hubTab) {
                                0 -> if (c.institutionId in selfSchoolIds) StudentsScreen(
                                    // عمله الفردي: يسجّل تلاميذه بنفسه
                                    schoolClass = c,
                                    students = students,
                                    onSave = { s -> scope.launch { studentDao.save(s) } },
                                    onDelete = { s -> scope.launch { gradeDao.deleteByStudent(s.id); absenceDao.deleteByStudent(s.id); studentDao.delete(s); com.elyedali.daftarustadh.data.StudentPhotos.delete(this@MainActivity, s.id) } },
                                    onImport = { importBack = Screen.TEACHER_HUB; screen = Screen.IMPORT },
                                    onShowSubjects = {},
                                    onBack = { screen = hubBack },
                                    showHeader = false
                                ) else StudentsScreen(
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
                                    subjectsFromAdmin = bundleSubjectsOf(c.id) != null,
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
                                3 -> com.elyedali.daftarustadh.ui.PlanningTab(
                                    levelCode = c.levelCode,
                                    locked = c.schoolYear !in teacherActivated,
                                    onActivate = { teacherActivateYear = c.schoolYear },
                                    // كل مواد المستوى تظهر في التخطيط، حتى لأستاذ المادة الواحدة
                                    mySubjects = CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                                        .sortedBy { s -> if (mySubjects.any { it.subjectCode == s.code }) 0 else 1 }
                                        .map { it.code to (if (isFrUi) it.nameFr else it.nameAr) }
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
                            onDelete = { s -> scope.launch { gradeDao.deleteByStudent(s.id); absenceDao.deleteByStudent(s.id); studentDao.delete(s); com.elyedali.daftarustadh.data.StudentPhotos.delete(this@MainActivity, s.id) } },
                            onImport = { importBack = Screen.ADMIN_CLASS; screen = Screen.IMPORT },
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
                            subjectsFromAdmin = bundleSubjectsOf(c.id) != null,
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
                            initialTerm = if (gradesBack == Screen.QUICK_GRADES) gradesInitialTerm else 1,
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
                                // الإدارة: اسم الأستاذ المسند للمادة في الجدول الزمني؛ الأستاذ: اسمه
                                val n = if (gradesFromAdmin) ttAssignments.firstOrNull { it.classId == c.id && it.subjectCode == code }?.teacherName
                                    ?.takeIf { it.isNotBlank() } ?: teacherName else teacherName
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
                            teachers = ttTeachers,
                            assignments = ttAssignments,
                            hoursOf = managedClasses.flatMap { c ->
                                CurriculumRepository.level(this@MainActivity, c.schoolYear, c.levelCode)?.subjects.orEmpty()
                                    .map { (c.id to it.code) to it.weeklyHours }
                            }.toMap(),
                            subjectLabel = { subjectName(it) },
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
                            onEditGeneralRemarks = { generalRemarksBack = Screen.KASHF_SETTINGS; screen = Screen.GENERAL_REMARKS },
                            onBack = { screen = Screen.ADMIN_CLASS }
                        )
                    }

                    screen == Screen.GENERAL_REMARKS -> RemarksSettingsScreen(
                        scale = generalRemarks,
                        teacherName = null,
                        onSave = { list, _ ->
                            scope.launch {
                                metaDao.put(AppMetaEntity(KashfSettings.GENERAL_REMARKS_KEY, RemarkScale.encode(list)))
                                screen = generalRemarksBack
                            }
                        },
                        title = stringResource(R.string.general_remarks),
                        defaults = KashfSettings.DEFAULT_GENERAL_REMARKS,
                        onBack = { screen = generalRemarksBack }
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
                        onConfirm = { list -> scope.launch { studentDao.saveAll(list); screen = importBack } },
                        onBack = { screen = importBack }
                    )

                    // تطبيق الأستاذ: بعد السنة، جدوله الشامل لكل المؤسسات ثم اختيار المؤسسة
                    screen == Screen.HOME && homeMode == com.elyedali.daftarustadh.ui.MODE_TEACHER && selectedYear != null && teacherSchool == null -> {
                        val year = selectedYear!!
                        val mine = allClasses.filter { it.schoolYear == year && (it.teaching || it.institutionId in selfSchoolIds) }
                        val byId = mine.groupBy { it.institutionId }
                        com.elyedali.daftarustadh.ui.TeacherSchoolsScreen(
                            year = year,
                            schools = (byId.keys + selfSchoolIds).map { id ->
                                com.elyedali.daftarustadh.ui.TeacherSchool(id, schoolNames[id].orEmpty(),
                                    byId[id].orEmpty().sortedWith(compareBy({ it.levelCode }, { it.number })).map { it.name },
                                    self = id in selfSchoolIds)
                            }.sortedWith(compareBy({ it.self }, { it.name })),
                            onAddSelfSchool = { addingSelfSchool = true; screen = Screen.INSTITUTION },
                            timetable = myTimetable.filter { it.schoolYear == year },
                            onPick = { teacherSchool = it },
                            onOpenFile = openFile,
                            onBack = { selectedYear = null },
                            onMenu = { scope.launch { drawerState.open() } }
                        )
                    }

                    else -> {
                    val homeClasses = if (homeMode == com.elyedali.daftarustadh.ui.MODE_TEACHER && teacherSchool != null) allClasses.filter { it.institutionId == teacherSchool } else allClasses
                    // الأستاذ: تقدم رصد النتائج لكل فصل في أقسامه (القسم مكتمل إذا اكتملت كل مواده لكل الطلاب)
                    val progClasses = if (homeMode == com.elyedali.daftarustadh.ui.MODE_TEACHER)
                        homeClasses.filter { it.teaching && it.schoolYear == selectedYear } else emptyList()
                    val progIds = progClasses.map { it.id }
                    val allMySubjects by remember { gradeDao.observeAllTeacherSubjects() }.collectAsState(initial = emptyList())
                    val progGrades by remember(progIds) { gradeDao.observeClasses(progIds) }.collectAsState(initial = emptyList())
                    val progStudents by remember(progIds) { studentDao.observeByClasses(progIds) }.collectAsState(initial = emptyList())
                    val progPairs = progClasses.flatMap { c -> allMySubjects.filter { it.classId == c.id }.map { c to it.subjectCode } }
                    val termProgress = if (progClasses.isEmpty()) emptyList() else {
                        val gradesBy = progGrades.groupBy { Triple(it.classId, it.subjectCode, it.studentId) }
                            .mapValues { e -> e.value.associate { it.slot to it.value } }
                        val studentsBy = progStudents.groupBy { it.classId }
                        val withSubjects = progClasses.filter { c -> progPairs.any { it.first.id == c.id } }
                        (1..3).map { t ->
                            val done = withSubjects.filter { c ->
                                val st = studentsBy[c.id].orEmpty()
                                st.isNotEmpty() && progPairs.filter { it.first.id == c.id }.all { (_, code) ->
                                    st.all { GradeMath.isComplete(t, gradesBy[Triple(c.id, code, it.id)].orEmpty()) }
                                }
                            }
                            com.elyedali.daftarustadh.ui.TeacherTermProgress(t, done.map { it.name }, withSubjects.size)
                        }
                    }
                    HomeYearsScreen(
                        classes = homeClasses,
                        currentInstitutionYear = inst?.schoolYear,
                        selectedYear = selectedYear,
                        mode = homeMode,
                        // الوضع ثابت في كل تطبيق؛ زر الرجوع في الأعلى يرجع خطوة: من الأقسام إلى المؤسسات (الأستاذ) أو إلى السنوات
                        onSelectMode = { m ->
                            if (m == null) {
                                if (homeMode == com.elyedali.daftarustadh.ui.MODE_TEACHER && teacherSchool != null) teacherSchool = null
                                else selectedYear = null
                            }
                        },
                        institutionName = inst?.schoolName,
                        teacherName = teacherProfile?.name ?: teacherName,
                        onTool = { key ->
                            when (key) {
                                "INSTITUTION" -> screen = Screen.INSTITUTION
                                "TIMETABLE" -> { ttInitialClass = null; ttInitialTab = null; ttBack = Screen.HOME; screen = Screen.TIMETABLE }
                                // إعدادات الجدول (التوليد والأساتذة): بعد الاعتماد لا تُفتح إلا بتنبيه والرمز 7777
                                "TT_SETTINGS" -> {
                                    if (ttApproved) askTtUnlock = true
                                    else { ttInitialClass = null; ttInitialTab = 0; ttBack = Screen.HOME; screen = Screen.TIMETABLE }
                                }
                                "SHARE_RECORDS" -> screen = Screen.SHARE_RECORDS
                                "PRINT_SHOP" -> { printKind = "BOOKS"; screen = Screen.PRINT_SHOP }
                                "PRINT_RECORDS" -> { printKind = "RECORDS"; screen = Screen.PRINT_SHOP }
                                "PRINT_TIMETABLES" -> { printKind = "TIMETABLES"; screen = Screen.PRINT_SHOP }
                                "UPLOAD_MANY" -> openBatch()
                                "RECORDS_FOLLOW" -> screen = Screen.RECORDS_FOLLOW
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
                        onSelectYear = {
                            selectedYear = it
                            teacherSchool = null
                            // أول دخول: الإدارة تسجّل معلومات المؤسسة، والأستاذ يسجّل معلوماته (مرة واحدة)
                            if (homeMode == com.elyedali.daftarustadh.ui.MODE_ADMIN && inst == null) screen = Screen.INSTITUTION
                            // الأستاذ لا يُطلب منه شيء: اسمه وموادّه تأتي من ملف الإدارة
                        },
                        onOpenClass = { c ->
                            classId = c.id; hubTab = 0; hubBack = Screen.HOME
                            if (homeMode == com.elyedali.daftarustadh.ui.MODE_ADMIN) { fromTeaching = false; screen = Screen.ADMIN_CLASS }
                            else { fromTeaching = true; screen = Screen.TEACHER_HUB }
                        },
                        onDeleteClass = { deleteClass(it) },
                        onCreateClass = { if (inst == null) screen = Screen.INSTITUTION else showCreateClass = true },
                        canCreateClass = homeMode != com.elyedali.daftarustadh.ui.MODE_TEACHER || teacherSchool in selfSchoolIds,
                        onOpenFile = openFile,
                        onMenu = { scope.launch { drawerState.open() } },
                        termProgress = termProgress,
                        onUploadTerm = { t ->
                            // اسم الأستاذ في السجل: الاسم الذي سجّلته به الإدارة (من ملفها) إن وُجد
                            uploadTermRecords(t, progPairs, (teacherProfile?.name ?: teacherName).orEmpty())
                        }
                    )
                    }
                }

                }

                // الإدارة: قسم في مؤسستها؛ الأستاذ: قسم في مؤسسة عمله الفردي المفتوحة (يُعدّ من أقسام تدريسه)
                val createIn = if (!AppRole.isAdmin && homeMode == com.elyedali.daftarustadh.ui.MODE_TEACHER)
                    institutions.firstOrNull { it.id == teacherSchool && it.id in selfSchoolIds } else inst
                if (showCreateClass && createIn != null) {
                    val year = selectedYear ?: createIn.schoolYear
                    val selfWork = !AppRole.isAdmin && createIn.id in selfSchoolIds
                    com.elyedali.daftarustadh.ui.CreateClassDialog(
                        institution = createIn,
                        schoolYear = year,
                        existing = allClasses,
                        onCreate = { level, number, onResult ->
                            scope.launch {
                                val rowId = classDao.insert(
                                    ClassEntity(
                                        id = UUID.randomUUID().toString(),
                                        institutionId = createIn.id,
                                        schoolYear = year,
                                        levelCode = level,
                                        number = number,
                                        createdAt = System.currentTimeMillis(),
                                        teaching = selfWork
                                    )
                                )
                                onResult(rowId != -1L)
                                if (rowId != -1L) selectedYear = year
                            }
                        },
                        onDismiss = { showCreateClass = false }
                    )
                }

                // طلب تفعيل الأستاذ عبر واتساب الإدارة
                teacherActivateYear?.let { year ->
                    AlertDialog(
                        onDismissRequest = { teacherActivateYear = null },
                        title = { Text("⭐ " + stringResource(R.string.tact_title)) },
                        text = {
                            androidx.compose.foundation.layout.Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                                Text(stringResource(R.string.tact_explain, com.elyedali.daftarustadh.data.TeacherActivation.PRICE, year))
                                Text("• " + stringResource(R.string.tact_item_planning))
                                Text(stringResource(R.string.tact_free_note), style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                if (year in teacherRequested) Text("⏳ " + stringResource(R.string.act_status_requested),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                teacherActivateYear = null
                                scope.launch {
                                    val code = metaDao.get(com.elyedali.daftarustadh.data.TeacherActivation.CODE_KEY) ?: com.elyedali.daftarustadh.data.TeacherActivation.newCode().also { metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.data.TeacherActivation.CODE_KEY, it)) }
                                    val tName = (teacherProfile?.name ?: teacherName).orEmpty()
                                    val reqCode = kotlinx.coroutines.withTimeoutOrNull(10_000) {
                                        com.elyedali.daftarustadh.auth.DsSubscriptions.request(
                                            com.elyedali.daftarustadh.auth.DsSubscriptions.APP_TEACHER, null, year,
                                            emptyList(), emptyList(), com.elyedali.daftarustadh.data.TeacherActivation.PRICE, tName)
                                    }
                                    val msg = getString(R.string.tact_msg, tName, year, code, com.elyedali.daftarustadh.data.TeacherActivation.PRICE) +
                                        "\n" + getString(R.string.act_msg_account, signedIn?.email.orEmpty()) +
                                        (reqCode?.let { "\n" + getString(R.string.act_msg_request, it) } ?: "")
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=${com.elyedali.daftarustadh.data.ClassActivation.ADMIN_PHONE}&text=" + Uri.encode(msg))
                                    try {
                                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.data.TeacherActivation.REQUESTED_KEY, com.elyedali.daftarustadh.data.TeacherActivation.encode(teacherRequested + year)))
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(this@MainActivity, getString(R.string.ex_no_whatsapp), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) { Text(stringResource(R.string.act_send)) }
                        },
                        dismissButton = { TextButton(onClick = { teacherActivateYear = null }) { Text(stringResource(R.string.cancel)) } }
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
                        byStudent.values.any { g -> GradeSlot.testsOf(t, g).isNotEmpty() || g[GradeSlot.exam(t)] != null }
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

                if (batchWorking) AlertDialog(
                    onDismissRequest = {},
                    text = { Text(stringResource(R.string.bu_working)) },
                    confirmButton = {}
                )
                batchResult?.let { r ->
                    val termName = stringResource(when (r.term) { 1 -> R.string.term_1; 2 -> R.string.term_2; else -> R.string.term_3 })
                    AlertDialog(
                        onDismissRequest = { batchResult = null },
                        title = { Text(stringResource(R.string.bu_title)) },
                        text = {
                            androidx.compose.foundation.layout.Column(
                                Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✅ " + stringResource(R.string.bu_imported, r.imported.size), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                if (r.imported.isNotEmpty()) Text(r.imported.joinToString("\n") { "• $it" }, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                if (r.duplicates > 0) Text("⏭️ " + stringResource(R.string.bu_duplicates, r.duplicates))
                                if (r.rejected.isNotEmpty()) Text("⛔ " + stringResource(R.string.bu_rejected, r.rejected.size) + "\n" +
                                    r.rejected.joinToString("\n") { "• $it" }, color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                if (r.warnings.isNotEmpty()) Text("⚠️ " + r.warnings.joinToString("\n"), color = androidx.compose.ui.graphics.Color(0xFFE65100),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                androidx.compose.material3.HorizontalDivider()
                                if (r.missing.isEmpty()) Text("🎉 " + stringResource(R.string.bu_complete, termName),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF2E7D32))
                                else {
                                    Text("❓ " + stringResource(R.string.bu_still_missing, termName), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                                    Text(r.missing.joinToString("\n") { "• $it" }, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { batchResult = null }) { Text(stringResource(R.string.ok)) } }
                    )
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
                                if (!assigned.isNullOrBlank() && !sameTeacher(assigned, chk.file.teacherName))
                                    Text(stringResource(R.string.record_other_teacher, assigned), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                                Text(stringResource(R.string.record_matched, chk.matched))
                                // القسم أُعيد إنشاؤه أو أُعيد تثبيت التطبيق بعد إرسال الملف للأستاذ: المطابقة بأرقام النداء
                                if (chk.older) Text(stringResource(R.string.record_older), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                                if (chk.byOrder) Text(stringResource(R.string.record_by_order, chk.matched), color = androidx.compose.ui.graphics.Color(0xFFE65100))
                                else if (chk.classByName) Text(stringResource(R.string.record_by_name), color = androidx.compose.ui.graphics.Color(0xFFE65100))
                                if (chk.byRoll > 0) Text(stringResource(R.string.record_by_roll, chk.byRoll), color = androidx.compose.ui.graphics.Color(0xFFE65100))
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

                if (askTtUnlock) com.elyedali.daftarustadh.ui.CodeConfirmDialog(
                    title = stringResource(R.string.tt_settings_title),
                    message = stringResource(R.string.tt_unlock_warning),
                    requiredCode = com.elyedali.daftarustadh.ui.TIMETABLE_EDIT_CODE,
                    confirmLabel = stringResource(R.string.tt_unlock_confirm),
                    onConfirm = {
                        askTtUnlock = false
                        scope.launch { metaDao.put(AppMetaEntity(TT_APPROVED_KEY, "")) }
                        ttInitialClass = null; ttInitialTab = 0; ttBack = Screen.HOME; screen = Screen.TIMETABLE
                    },
                    onDismiss = { askTtUnlock = false }
                )

                if (showStaffShare) {
                    val staff = com.elyedali.daftarustadh.ui.AdminStaff.decode(staffText)
                    val schoolName = institutions.firstOrNull { it.isOwn }?.schoolName.orEmpty()
                    suspend fun buildAdminFile() = withContext(Dispatchers.IO) {
                        com.elyedali.daftarustadh.transfer.AdminFullFile.export(db, schoolName, TransferCodec.FORMAT_NAME)
                    }
                    val fileName = com.elyedali.daftarustadh.transfer.AdminFullFile.fileName(schoolName)
                    com.elyedali.daftarustadh.ui.AdminStaffShareDialog(
                        staff = staff,
                        onSave = { list -> scope.launch { metaDao.put(AppMetaEntity(com.elyedali.daftarustadh.ui.AdminStaff.KEY,
                            com.elyedali.daftarustadh.ui.AdminStaff.encode(list))) } },
                        onSendTo = { m ->
                            showStaffShare = false
                            scope.launch {
                                val bytes = buildAdminFile()
                                ShareHelper.shareToWhatsApp(this@MainActivity, bytes, fileName, m.phone,
                                    getString(R.string.af_caption, schoolName), getString(R.string.share_chooser))
                            }
                        },
                        onShareOther = {
                            showStaffShare = false
                            scope.launch { ShareHelper.share(this@MainActivity, buildAdminFile(), fileName, getString(R.string.share_chooser)) }
                        },
                        onDismiss = { showStaffShare = false }
                    )
                }

                adminFilePending?.let { f ->
                    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(java.util.Date(f.exportedAt))
                    AlertDialog(
                        onDismissRequest = { adminFilePending = null },
                        title = { Text(stringResource(R.string.af_title)) },
                        text = { Text(stringResource(R.string.af_confirm, f.schoolName, date, f.classCount, f.studentCount)) },
                        confirmButton = {
                            TextButton(onClick = {
                                adminFilePending = null
                                scope.launch {
                                    val out = try { withContext(Dispatchers.IO) { f.apply(db) } } catch (e: Exception) { null }
                                    fileResult = when (out) {
                                        is com.elyedali.daftarustadh.transfer.AdminFullFile.Outcome.Done -> {
                                            screen = Screen.HOME
                                            FileResult.Message(getString(R.string.af_done, out.classes, out.students, out.grades) +
                                                if (out.merged.isEmpty()) "" else "\n" + getString(R.string.af_merged, out.merged.joinToString("، ")))
                                        }
                                        is com.elyedali.daftarustadh.transfer.AdminFullFile.Outcome.OtherInstitution ->
                                            FileResult.Message(getString(R.string.af_other_school, out.schoolName))
                                        null -> FileResult.Invalid
                                    }
                                }
                            }) { Text(stringResource(R.string.af_import)) }
                        },
                        dismissButton = { TextButton(onClick = { adminFilePending = null }) { Text(stringResource(R.string.cancel)) } }
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
        // عدة ملفات مُشاركة معًا من واتساب (تحديد عدة سجلات ثم «مشاركة»)
        if (intent?.action == Intent.ACTION_SEND_MULTIPLE) {
            val list: List<Uri> = (if (Build.VERSION.SDK_INT >= 33) intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else @Suppress("DEPRECATION") intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)).orEmpty()
            if (list.isNotEmpty()) incomingFiles.value = list
        }
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
