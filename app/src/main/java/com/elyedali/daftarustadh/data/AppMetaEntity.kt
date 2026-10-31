package com.elyedali.daftarustadh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** جدول إعدادات داخلية بسيط (مفتاح/قيمة). تُضاف جداول المؤسسة والأقسام والطلاب في الخطوات القادمة. */
@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String
)
