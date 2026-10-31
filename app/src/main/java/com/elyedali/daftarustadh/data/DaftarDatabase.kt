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
    entities = [AppMetaEntity::class, InstitutionEntity::class, ClassEntity::class, StudentEntity::class,
        GradeEntity::class, TeacherSubjectEntity::class, SubjectRecordEntity::class],
    version = 8,
    exportSchema = false
)
abstract class DaftarDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun institutionDao(): InstitutionDao
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun gradeDao(): GradeDao

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

        /** 3 → 4: إضافة جدول الطلاب. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `student` (" +
                        "`id` TEXT NOT NULL, `classId` TEXT NOT NULL, `rollNumber` INTEGER NOT NULL, " +
                        "`nameAr` TEXT NOT NULL, `nameFr` TEXT NOT NULL, `rim` TEXT NOT NULL, " +
                        "`nationalId` TEXT NOT NULL, `guardianPhone1` TEXT NOT NULL, " +
                        "`guardianPhone2` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_classId` ON `student` (`classId`)")
            }
        }

        /** 4 → 5: تمييز مؤسسة المستخدم، وتمييز أقسام الإدارة وأقسام الأستاذ. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `institution` ADD COLUMN `isOwn` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `school_class` ADD COLUMN `managed` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `school_class` ADD COLUMN `teaching` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** 5 → 6: النقاط، ومواد الأستاذ في كل قسم. */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `grade` (" +
                        "`studentId` TEXT NOT NULL, `classId` TEXT NOT NULL, `subjectCode` TEXT NOT NULL, " +
                        "`slot` TEXT NOT NULL, `value` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`studentId`, `subjectCode`, `slot`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_grade_classId_subjectCode` ON `grade` (`classId`, `subjectCode`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `teacher_subject` (" +
                        "`classId` TEXT NOT NULL, `subjectCode` TEXT NOT NULL, `testsMode` TEXT NOT NULL, " +
                        "PRIMARY KEY(`classId`, `subjectCode`))"
                )
            }
        }

        /** 6 → 7: سجلات المواد المرفوعة عند الإدارة. */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `subject_record` (" +
                        "`classId` TEXT NOT NULL, `subjectCode` TEXT NOT NULL, `teacherName` TEXT NOT NULL, " +
                        "`testsMode` TEXT NOT NULL, `receivedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`classId`, `subjectCode`))"
                )
            }
        }

        /** 7 → 8: جنس الطالب (للإحصائيات). */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `student` ADD COLUMN `gender` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun get(context: Context): DaftarDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaftarDatabase::class.java,
                    "daftar_ustadh_ds.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8).build().also { instance = it }
            }
    }
}
