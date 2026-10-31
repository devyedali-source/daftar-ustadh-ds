package com.elyedali.daftarustadh.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * طالب في قسم.
 * id هو «الرقم الخفي» للطالب: ثابت لا يتغيّر ولا يُعرض، وبه تتطابق سجلات الأساتذة عند الرفع
 * حتى لو تغيّر رقم النداء أو صُحّح الاسم.
 */
@Entity(tableName = "student", indices = [Index(value = ["classId"])])
data class StudentEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val rollNumber: Int,
    val nameAr: String,
    val nameFr: String,
    val rim: String,            // الرقم المدرسي
    val nationalId: String,     // الرقم الوطني
    val guardianPhone1: String, // هاتف الوكيل 1
    val guardianPhone2: String, // هاتف الوكيل 2
    val createdAt: Long
)
