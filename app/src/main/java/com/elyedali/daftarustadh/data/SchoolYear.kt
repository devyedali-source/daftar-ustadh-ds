package com.elyedali.daftarustadh.data

import java.util.Calendar

object SchoolYear {
    /** السنة الدراسية الجارية: تبدأ في أغسطس. مثال: من أغسطس 2026 إلى يوليو 2027 = "2026-2027". */
    fun current(): String {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val start = if (c.get(Calendar.MONTH) >= Calendar.AUGUST) y else y - 1
        return "$start-${start + 1}"
    }

    /** السنوات المعروضة للاختيار: السابقة والجارية والقادمة. */
    fun choices(): List<String> {
        val start = current().substringBefore('-').toInt()
        return listOf(start - 1, start, start + 1).map { "$it-${it + 1}" }
    }
}
