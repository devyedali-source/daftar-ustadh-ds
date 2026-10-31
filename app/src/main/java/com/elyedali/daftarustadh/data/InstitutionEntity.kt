package com.elyedali.daftarustadh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * المؤسسة التي تسجّلها الإدارة.
 * id هو «بصمة المؤسسة»: يُنشأ مرة واحدة عند التسجيل ولا يتغيّر، وينتقل مع ملفات الأقسام.
 * schoolListId موجود فقط إذا اختيرت المؤسسة من اللائحة، ويكون فارغًا إذا كُتب اسمها يدويًا.
 */
@Entity(tableName = "institution")
data class InstitutionEntity(
    @PrimaryKey val id: String,
    val wilaya: String,
    val schoolName: String,
    val schoolListId: String?,
    val stage: String,      // college | lycee | both
    val schoolYear: String,
    val createdAt: Long
)

object Stage {
    const val COLLEGE = "college"
    const val LYCEE = "lycee"
    const val BOTH = "both"
}
