package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.TimetableTeachers
import org.json.JSONArray
import org.json.JSONObject

/** معلومات الأستاذ (وضع الأستاذ): تُسجَّل مرة واحدة ويمكن تعديلها. */
data class TeacherProfile(val name: String, val phone: String, val specialties: List<String>) {
    fun encode(): String = JSONObject().put("name", name).put("phone", phone)
        .put("specialties", JSONArray(specialties)).toString()

    companion object {
        const val KEY = "teacher_profile"

        fun decode(text: String?): TeacherProfile? {
            if (text.isNullOrBlank()) return null
            return runCatching {
                val o = JSONObject(text)
                val s = o.optJSONArray("specialties") ?: JSONArray()
                TeacherProfile(o.getString("name"), o.optString("phone"), (0 until s.length()).map { s.getString(it) })
            }.getOrNull()?.takeIf { it.name.isNotBlank() }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherProfileScreen(
    existing: TeacherProfile?,
    specialties: List<SpecialtyOption>,
    onSave: (TeacherProfile) -> Unit,
    onCancel: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(existing?.phone.orEmpty()) }
    var chosen by androidx.compose.runtime.remember { mutableStateOf(existing?.specialties.orEmpty()) }
    val max = TimetableTeachers.MAX_SUBJECTS
    val canSave = name.isNotBlank() && chosen.isNotEmpty() && chosen.size <= max

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.tp_title), onMenu = null, onBack = onCancel)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(stringResource(R.string.tp_intro), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.tp_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone, { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    label = { Text(stringResource(R.string.tp_phone)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.tp_specialties), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(stringResource(R.string.max_subjects, chosen.size, max), fontSize = 12.sp,
                    color = if (chosen.size >= max) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    specialties.forEach { s ->
                        FilterChip(
                            selected = s.code in chosen,
                            enabled = s.code in chosen || chosen.size < max,
                            onClick = { chosen = if (s.code in chosen) chosen - s.code else chosen + s.code },
                            label = { Text(s.label, fontSize = 12.sp) }
                        )
                    }
                }
                Button(
                    enabled = canSave,
                    onClick = { onSave(TeacherProfile(name.trim(), phone.trim(), specialties.map { it.code }.filter { it in chosen })) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) }
            }
        }
    }
}
