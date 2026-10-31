package com.elyedali.daftarustadh

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.ui.ClassesScreen
import com.elyedali.daftarustadh.ui.DaftarTheme
import com.elyedali.daftarustadh.ui.HomeScreen
import com.elyedali.daftarustadh.ui.InstitutionScreen
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.UUID

private enum class Screen { HOME, INSTITUTION, CLASSES }

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // العربية هي اللغة الافتراضية عند أول تشغيل
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
        }
        val db = DaftarDatabase.get(this)
        val institutionDao = db.institutionDao()
        val classDao = db.classDao()
        setContent {
            DaftarTheme {
                val scope = rememberCoroutineScope()
                val institution by institutionDao.observe().collectAsState(initial = null)
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                val inst = institution
                val classesFlow = remember(inst?.id, inst?.schoolYear) {
                    if (inst == null) flowOf(emptyList()) else classDao.observe(inst.id, inst.schoolYear)
                }
                val classes by classesFlow.collectAsState(initial = emptyList())

                if (screen != Screen.HOME) BackHandler { screen = Screen.HOME }

                when {
                    screen == Screen.INSTITUTION -> InstitutionScreen(
                        existing = inst,
                        onSave = { scope.launch { institutionDao.save(it); screen = Screen.HOME } },
                        onCancel = { screen = Screen.HOME }
                    )
                    screen == Screen.CLASSES && inst != null -> ClassesScreen(
                        institution = inst,
                        classes = classes,
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
                        onDelete = { c -> scope.launch { classDao.delete(c) } },
                        onBack = { screen = Screen.HOME }
                    )
                    else -> HomeScreen(
                        institution = inst,
                        onRegisterInstitution = { screen = Screen.INSTITUTION },
                        onOpenClasses = { screen = Screen.CLASSES },
                        onLanguageSelected = { tag ->
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }
                    )
                }
            }
        }
    }
}
