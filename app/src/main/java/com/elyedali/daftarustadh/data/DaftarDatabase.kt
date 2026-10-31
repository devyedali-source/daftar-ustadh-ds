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
    entities = [AppMetaEntity::class, InstitutionEntity::class, ClassEntity::class],
    version = 3,
    exportSchema = false
)
abstract class DaftarDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun institutionDao(): InstitutionDao
    abstract fun classDao(): ClassDao

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

        /** 2 → 3: إضافة جدول الأقسام. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `school_class` (" +
                        "`id` TEXT NOT NULL, `institutionId` TEXT NOT NULL, `schoolYear` TEXT NOT NULL, " +
                        "`levelCode` TEXT NOT NULL, `number` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_school_class_institutionId_schoolYear_levelCode_number` " +
                        "ON `school_class` (`institutionId`, `schoolYear`, `levelCode`, `number`)"
                )
            }
        }

        fun get(context: Context): DaftarDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaftarDatabase::class.java,
                    "daftar_ustadh_ds.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
    }
}
