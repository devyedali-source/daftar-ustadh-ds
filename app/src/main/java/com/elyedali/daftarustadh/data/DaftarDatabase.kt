package com.elyedali.daftarustadh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * قاعدة البيانات المحلية الخاصة بدفتر الأستاذ DS وحده (ملف daftar_ustadh_ds.db).
 * لا علاقة لها بقاعدة دفتر المعلم.
 * قاعدة دائمة: لا يُستعمل الحذف عند الترقية (fallbackToDestructiveMigration)،
 * وكل تغيير في الجداول مستقبلًا يكون بترقية تحفظ بيانات المستخدم.
 */
@Database(entities = [AppMetaEntity::class], version = 1, exportSchema = false)
abstract class DaftarDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao

    companion object {
        @Volatile private var instance: DaftarDatabase? = null

        fun get(context: Context): DaftarDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaftarDatabase::class.java,
                    "daftar_ustadh_ds.db"
                ).build().also { instance = it }
            }
    }
}
