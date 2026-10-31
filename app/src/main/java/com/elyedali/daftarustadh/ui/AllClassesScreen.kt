package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity

/** «الأقسام»: أقسام الإدارة وأقسام الأستاذ في شاشة واحدة. */
@Composable
fun AllClassesScreen(
    managed: List<ClassEntity>,
    teaching: List<ClassEntity>,
    schoolNames: Map<String, String>,
    hasInstitution: Boolean,
    onOpenManaged: (ClassEntity) -> Unit,
    onOpenTeaching: (ClassEntity) -> Unit,
    onManageClasses: () -> Unit,
    onOpenFile: () -> Unit,
    onBack: () -> Unit
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { ScreenHeader(stringResource(R.string.classes_title), onBack) }
            item { SectionTitle(stringResource(R.string.admin_section)) }
            if (hasInstitution) item {
                ActionCard(Icons.Filled.Add, stringResource(R.string.manage_classes), stringResource(R.string.manage_classes_desc),
                    tint = MaterialTheme.colorScheme.secondary, onClick = onManageClasses)
            }
            if (managed.isEmpty()) item { Text(stringResource(R.string.no_classes), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(managed, key = { "m-" + it.id }) { c ->
                ActionCard(Icons.Filled.AccountBox, c.name, listOfNotNull(levelDescription(c.levelCode), schoolNames[c.institutionId]).joinToString(" — ")) { onOpenManaged(c) }
            }
            item { SectionTitle(stringResource(R.string.teacher_section)) }
            item {
                ActionCard(Icons.Filled.Share, stringResource(R.string.open_class_file), stringResource(R.string.home_open_file_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, onClick = onOpenFile)
            }
            if (teaching.isEmpty()) item { Text(stringResource(R.string.no_teaching_classes), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(teaching, key = { "t-" + it.id }) { c ->
                ActionCard(Icons.Filled.AccountBox, c.name, listOfNotNull(schoolNames[c.institutionId], c.schoolYear).joinToString(" — "),
                    tint = MaterialTheme.colorScheme.tertiary) { onOpenTeaching(c) }
            }
        }
    }
}
