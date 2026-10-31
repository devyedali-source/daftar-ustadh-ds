package com.elyedali.daftarustadh

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.ui.DaftarTheme
import com.elyedali.daftarustadh.ui.HomeScreen
import com.elyedali.daftarustadh.ui.InstitutionScreen
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // العربية هي اللغة الافتراضية عند أول تشغيل
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
        }
        val dao = DaftarDatabase.get(this).institutionDao()
        setContent {
            DaftarTheme {
                val scope = rememberCoroutineScope()
                val institution by dao.observe().collectAsState(initial = null)
                var editing by rememberSaveable { mutableStateOf(false) }

                if (editing) {
                    BackHandler { editing = false }
                    InstitutionScreen(
                        existing = institution,
                        onSave = { scope.launch { dao.save(it); editing = false } },
                        onCancel = { editing = false }
                    )
                } else {
                    HomeScreen(
                        institution = institution,
                        onRegisterInstitution = { editing = true },
                        onLanguageSelected = { tag ->
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }
                    )
                }
            }
        }
    }
}
