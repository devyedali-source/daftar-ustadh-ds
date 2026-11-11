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
        GradeEntity::class, TeacherSubjectEntity::class, SubjectRecordEntity::class,
        DocumentEntity::class, AbsenceSessionEntity::class, AbsenceEntity::class,
        TimetableAssignmentEntity::class, TimetableSlotEntity::class, TeacherTimetableEntity::class],
    version = 12,
    exportSchema = false
)
abstract class DaftarDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun institutionDao(): InstitutionDao
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun gradeDao(): GradeDao
    abstract fun documentDao(): DocumentDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun timetableDao(): TimetableDao

    /** مفتاح المؤسسة التي تخص هذه القاعدة (تطبيق الإدارة). */
    @Volatile var instKey: String = Institutions.MAIN

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

        /** 8 → 9: وثائق الأستاذ (تحرير). */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `document` (" +
                        "`id` TEXT NOT NULL, `classId` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`body` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_document_classId` ON `document` (`classId`)")
            }
        }

        /** 9 → 10: دفتر الغياب (الحصص والغائبون). */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `absence_session` (" +
                        "`id` TEXT NOT NULL, `classId` TEXT NOT NULL, `subjectCode` TEXT NOT NULL, `date` TEXT NOT NULL, " +
                        "`period` INTEGER NOT NULL, `source` TEXT NOT NULL, `recordedBy` TEXT NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_absence_session_classId` ON `absence_session` (`classId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `absence` (" +
                        "`sessionId` TEXT NOT NULL, `studentId` TEXT NOT NULL, `classId` TEXT NOT NULL, " +
                        "PRIMARY KEY(`sessionId`, `studentId`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_absence_classId` ON `absence` (`classId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_absence_studentId` ON `absence` (`studentId`)")
            }
        }

        /** 11 → 12: علامة الغياب في النقاط (غائب بتبرير / بدون تبرير). */
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `grade` ADD COLUMN `mark` TEXT")
            }
        }

        /** 10 → 11: الجدول الزمني (تعيين الأساتذة، حصص الأقسام، وجدول الأستاذ المستلَم). */
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `timetable_assignment` (" +
                        "`classId` TEXT NOT NULL, `subjectCode` TEXT NOT NULL, `teacherName` TEXT NOT NULL, " +
                        "PRIMARY KEY(`classId`, `subjectCode`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `timetable_slot` (" +
                        "`classId` TEXT NOT NULL, `day` INTEGER NOT NULL, `period` INTEGER NOT NULL, " +
                        "`subjectCode` TEXT NOT NULL, `teacherName` TEXT NOT NULL, PRIMARY KEY(`classId`, `day`, `period`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_timetable_slot_classId` ON `timetable_slot` (`classId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `teacher_timetable` (" +
                        "`id` TEXT NOT NULL, `institutionId` TEXT NOT NULL, `schoolName` TEXT NOT NULL, `schoolYear` TEXT NOT NULL, " +
                        "`teacherName` TEXT NOT NULL, `day` INTEGER NOT NULL, `period` INTEGER NOT NULL, `startTime` TEXT NOT NULL, " +
                        "`endTime` TEXT NOT NULL, `className` TEXT NOT NULL, `subjectCode` TEXT NOT NULL, `subjectNameAr` TEXT NOT NULL, " +
                        "`subjectNameFr` TEXT NOT NULL, `receivedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_teacher_timetable_institutionId` ON `teacher_timetable` (`institutionId`)")
            }
        }

        /** مفتاح المؤسسة التي فُتحت قاعدتها (تطبيق الإدارة: لكل مؤسسة قاعدة منفصلة). */
        @Volatile var openKey: String? = null
            private set

        /** إغلاق القاعدة الحالية قبل الانتقال إلى مؤسسة أخرى (ثم يُعاد تشغيل الشاشة). */
        fun switchTo(context: Context, key: String) = synchronized(this) {
            Institutions.setActive(context, key)
            // لا تُغلق القديمة فورًا (قد تكون شاشة ما زالت تقرأ منها)؛ تُترك ثم تُفتح قاعدة المؤسسة الأخرى
            instance = null
            openKey = null
        }

        fun get(context: Context): DaftarDatabase =
            instance ?: synchronized(this) {
                val key = Institutions.active(context)
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaftarDatabase::class.java,
                    Institutions.dbName(key)
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12).build().also { it.instKey = key; instance = it; openKey = key }
            }
    }
}
