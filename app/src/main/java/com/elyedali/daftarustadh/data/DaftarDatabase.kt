package com.elyedali.daftarustadh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * قاعدة البيانات المحلية الخاصة بدفتر الأستاذ DS وحده (ملف daftar_ustadh_ds.db).
 * لا علاقة لها بقاعدة دفتر المعلم.
 * قاعدة دائمة: لا يُستعمل الحذف عند الترقية (fallbackToDestructiveMigration)؛
 * كل تغيير في الجداول يكون بترقية (Migration) تحفظ بيانات المستخدم.
 */
@Database(
    entities = [AppMetaEntity::class, InstitutionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DaftarDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun institutionDao(): InstitutionDao

    companion object {
        @Volatile private var instance: DaftarDatabase? = null

        /** 1 → 2: إضافة جدول المؤسسة. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `institution` (" +
                        "`id` TEXT NOT NULL, `wilaya` TEXT NOT NULL, `schoolName` TEXT NOT NULL, " +
                        "`schoolListId` TEXT, `stage` TEXT NOT NULL, `schoolYear` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        fun get(context: Context): DaftarDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaftarDatabase::class.java,
                    "daftar_ustadh_ds.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
