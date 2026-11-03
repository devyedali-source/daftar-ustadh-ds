package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R

/**
 * دفتر الغياب داخل القسم (الإدارة): للسحب فقط، يُملأ باليد.
 * بأسماء الطلاب (رقم النداء والاسم)، أو فارغًا.
 */
@Composable
fun AbsenceRegisterTab(hasStudents: Boolean, onPrint: (blank: Boolean) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🗓️ " + stringResource(R.string.reg_absence), fontWeight = FontWeight.Black, fontSize = 17.sp,
            color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.reg_absence_hint), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { onPrint(false) }, enabled = hasStudents, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text("👥 " + stringResource(R.string.reg_with_names), fontWeight = FontWeight.Bold)
        }
        if (!hasStudents) Text(stringResource(R.string.reg_no_students), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
        OutlinedButton(onClick = { onPrint(true) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text("📄 " + stringResource(R.string.reg_blank), fontWeight = FontWeight.Bold)
        }
    }
}
