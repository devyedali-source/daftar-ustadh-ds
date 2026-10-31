package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.InstitutionEntity
import com.elyedali.daftarustadh.data.SchoolDirectory
import com.elyedali.daftarustadh.data.Stage

/** الشاشة الرئيسية: بطاقة المؤسسة، ثم قسم الأستاذ وقسم الإدارة. */
@Suppress("DEPRECATION")
@Composable
fun HomeScreen(
    institution: InstitutionEntity?,
    onRegisterInstitution: () -> Unit,
    onOpenClasses: () -> Unit,
    onOpenTeaching: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenRemarks: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenActivation: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onOpenMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val isFr = LocalConfiguration.current.locales[0].language == "fr"
    val primary = MaterialTheme.colorScheme.primary
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // الشريط العلوي: اسم التطبيق ولغة الواجهة ومعلومات المؤسسة
            Column(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge)
                    .background(Brush.linearGradient(listOf(primary, MaterialTheme.colorScheme.secondary)))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(onClick = onOpenMenu) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Filled.Menu, contentDescription = null, tint = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text(stringResource(R.string.welcome_subtitle), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                    }
                    HeaderAction(if (isFr) "العربية" else "Français") { onLanguageSelected(if (isFr) "ar" else "fr") }
                }
                if (institution != null) {
                    Spacer(Modifier.height(14.dp))
                    val w = SchoolDirectory.wilaya(context, institution.wilaya)
                    val stageRes = when (institution.stage) {
                        Stage.COLLEGE -> R.string.stage_college
                        Stage.LYCEE -> R.string.stage_lycee
                        else -> R.string.stage_both
                    }
                    Column(
                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                            .background(Color.White.copy(alpha = 0.14f)).padding(12.dp)
                    ) {
                        Text(institution.schoolName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            listOf(if (isFr && w != null) w.nameFr else institution.wilaya, stringResource(stageRes), institution.schoolYear).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            SectionTitle(stringResource(R.string.teacher_section))
            ActionCard(Icons.Filled.List, stringResource(R.string.my_classes), stringResource(R.string.home_my_classes_desc), onClick = onOpenTeaching)
            ActionCard(Icons.Filled.Share, stringResource(R.string.open_class_file), stringResource(R.string.home_open_file_desc), onClick = onOpenFile)
            ActionCard(Icons.Filled.Create, stringResource(R.string.remarks_settings), stringResource(R.string.home_remarks_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant, onClick = onOpenRemarks)

            SectionTitle(stringResource(R.string.admin_section))
            if (institution == null) {
                ActionCard(Icons.Filled.Home, stringResource(R.string.register_institution), stringResource(R.string.home_register_desc),
                    tint = MaterialTheme.colorScheme.secondary, onClick = onRegisterInstitution)
            } else {
                ActionCard(Icons.Filled.AccountBox, stringResource(R.string.classes_title), stringResource(R.string.home_classes_desc),
                    tint = MaterialTheme.colorScheme.secondary, onClick = onOpenClasses)
                ActionCard(Icons.Filled.Info, stringResource(R.string.statistics), stringResource(R.string.home_stats_desc),
                    tint = MaterialTheme.colorScheme.tertiary, onClick = onOpenStats)
                ActionCard(Icons.Filled.Star, stringResource(R.string.act_title), stringResource(R.string.home_activation_desc),
                    tint = Color(0xFFE65100), onClick = onOpenActivation)
                ActionCard(Icons.Filled.Edit, stringResource(R.string.edit_institution), null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, onClick = onRegisterInstitution)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
