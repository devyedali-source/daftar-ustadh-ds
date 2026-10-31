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
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.transfer.ClassFile
import com.elyedali.daftarustadh.transfer.ClassFileCodec
import com.elyedali.daftarustadh.transfer.ClassFileImporter
import com.elyedali.daftarustadh.transfer.ClassFileType
import com.elyedali.daftarustadh.transfer.ImportOutcome
import com.elyedali.daftarustadh.transfer.ShareHelper
import com.elyedali.daftarustadh.ui.ClassSubjectsDialog
import com.elyedali.daftarustadh.ui.ClassesScreen
import com.elyedali.daftarustadh.ui.DaftarTheme
import com.elyedali.daftarustadh.ui.HomeScreen
import com.elyedali.daftarustadh.ui.ImportStudentsScreen
import com.elyedali.daftarustadh.ui.InstitutionScreen
import com.elyedali.daftarustadh.ui.StudentsScreen
import com.elyedali.daftarustadh.ui.TeachingClassesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private enum class Screen { HOME, INSTITUTION, CLASSES, TEACHING, STUDENTS, IMPORT }

/** نتيجة فتح ملف قسم، تُعرض في نافذة. */
private sealed class FileResult {
    data object Invalid : FileResult()
    data class Done(val outcome: ImportOutcome) : FileResult()
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
        val importer = ClassFileImporter(db)

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

                // فتح ملف قسم: من الواتساب أو من زر «فتح ملف قسم»
                fun processFile(uri: Uri) {
                    scope.launch {
                        val result = try {
                            val bytes = withContext(Dispatchers.IO) {
                                contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            } ?: throw IllegalStateException()
                            val file = ClassFileCodec.decode(bytes)
                            FileResult.Done(importer.save(file))
                        } catch (e: Exception) {
                            FileResult.Invalid
                        }
                        fileResult = result
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
                    Screen.STUDENTS -> BackHandler { screen = if (fromTeaching) Screen.TEACHING else Screen.CLASSES }
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
                        onOpen = { c -> classId = c.id; fromTeaching = true; screen = Screen.STUDENTS },
                        onBack = { screen = Screen.HOME }
                    )

                    screen == Screen.STUDENTS && currentClass != null -> {
                        val c = currentClass!!
                        val readOnly = fromTeaching
                        StudentsScreen(
                            schoolClass = c,
                            students = students,
                            onSave = { s -> scope.launch { studentDao.save(s) } },
                            onDelete = { s -> scope.launch { studentDao.delete(s) } },
                            onImport = { screen = Screen.IMPORT },
                            onShowSubjects = { showSubjects = true },
                            onBack = { screen = if (fromTeaching) Screen.TEACHING else Screen.CLASSES },
                            onShare = if (readOnly) null else { type -> share(c, type) },
                            readOnly = readOnly,
                            subtitle = schoolNames[c.institutionId]
                        )
                        if (showSubjects) ClassSubjectsDialog(c) { showSubjects = false }
                    }

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
                        onLanguageSelected = { tag ->
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }
                    )
                }

                fileResult?.let { r ->
                    val message = when (r) {
                        FileResult.Invalid -> stringResource(R.string.file_invalid)
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
