package com.elyedali.daftarustadh.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * قسم في المؤسسة، مثل 1AS1 أو 5C2.
 * المواد وضواربها لا تُنسخ هنا: تُؤخذ من مواد سنته الدراسية ومستواه، فتبقى ثابتة لأقسام السنوات الماضية.
 */
@Entity(
    tableName = "school_class",
    indices = [Index(value = ["institutionId", "schoolYear", "levelCode", "number"], unique = true)]
)
data class ClassEntity(
    @PrimaryKey val id: String,
    val institutionId: String,
    val schoolYear: String,
    val levelCode: String,   // 1AS ... 4AS، 5A 5C 5D 5O ... 7O
    val number: Int,         // 1، 2، 3 ...
    val createdAt: Long,
    /** يظهر في وضع الإدارة (أنشأه المستخدم أو وصله ملف معلوماته الكامل). */
    @ColumnInfo(defaultValue = "1") val managed: Boolean = true,
    /** يظهر في «أقسامي» عند الأستاذ (وصله ملف القسم للأساتذة). */
    @ColumnInfo(defaultValue = "0") val teaching: Boolean = false
) {
    val name: String get() = "$levelCode$number"
}
