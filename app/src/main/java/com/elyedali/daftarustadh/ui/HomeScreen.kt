package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.InstitutionEntity
import com.elyedali.daftarustadh.data.SchoolDirectory
import com.elyedali.daftarustadh.data.Stage

/** الشاشة الرئيسية المؤقتة: بطاقة المؤسسة وتبديل لغة الواجهة. */
@Composable
fun HomeScreen(
    institution: InstitutionEntity?,
    onRegisterInstitution: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.welcome_subtitle), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))

            if (institution == null) {
                Button(onClick = onRegisterInstitution) { Text(stringResource(R.string.register_institution)) }
            } else {
                val w = SchoolDirectory.wilaya(context, institution.wilaya)
                val stageRes = when (institution.stage) {
                    Stage.COLLEGE -> R.string.stage_college
                    Stage.LYCEE -> R.string.stage_lycee
                    else -> R.string.stage_both
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(institution.schoolName, style = MaterialTheme.typography.titleMedium)
                        Text("${stringResource(R.string.wilaya)}: ${if (isFr && w != null) w.nameFr else institution.wilaya}")
                        Text(stringResource(stageRes))
                        Text("${stringResource(R.string.school_year)}: ${institution.schoolYear}")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRegisterInstitution) { Text(stringResource(R.string.edit_institution)) }
            }

            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onLanguageSelected("ar") }) { Text(stringResource(R.string.lang_ar)) }
                OutlinedButton(onClick = { onLanguageSelected("fr") }) { Text(stringResource(R.string.lang_fr)) }
            }
        }
    }
}
