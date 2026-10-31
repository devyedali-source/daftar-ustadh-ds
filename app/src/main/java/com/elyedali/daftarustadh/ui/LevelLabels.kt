package com.elyedali.daftarustadh.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elyedali.daftarustadh.R

/** وصف المستوى: 1AS → «السنة الأولى إعدادية»، 5C → «السنة الخامسة — شعبة الرياضيات». */
@Composable
fun levelDescription(code: String): String {
    val year = code.takeWhile { it.isDigit() }.toIntOrNull() ?: return code
    val yearRes = when (year) {
        1 -> R.string.year_1; 2 -> R.string.year_2; 3 -> R.string.year_3; 4 -> R.string.year_4
        5 -> R.string.year_5; 6 -> R.string.year_6; else -> R.string.year_7
    }
    if (code.endsWith("AS")) return stringResource(yearRes) + " " + stringResource(R.string.college_suffix)
    val branchRes = when (code.last()) {
        'C' -> R.string.branch_c; 'D' -> R.string.branch_d; 'A' -> R.string.branch_a; else -> R.string.branch_o
    }
    return stringResource(yearRes) + " — " + stringResource(branchRes)
}
