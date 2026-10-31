package com.elyedali.daftarustadh.data

import android.content.Context
import com.elyedali.daftarustadh.BuildConfig
import kotlin.random.Random

/**
 * بيانات تجريبية لتجربة التطبيق: مؤسسة، أقسام، طلاب، نتائج، وسجلات أساتذة.
 * كل معرّفاتها تبدأ بـ «demo-» فيمكن حذفها لاحقًا دون مسّ بيانات حقيقية.
 *
 * القاعدة الآن (قبل تسجيل الدخول): تُضاف مرة واحدة في نسخ التجربة (debug) إذا كانت القاعدة فارغة.
 * القاعدة بعد إضافة تسجيل الدخول: تُضاف فقط لحساب صاحب التطبيق [OWNER_EMAIL].
 */
object DemoData {
    const val OWNER_EMAIL = "elyedalimoctar@gmail.com"
    private const val SEEDED_KEY = "demo_seeded"
    private const val YEAR = "2026-2027"

    fun allowedFor(email: String?): Boolean =
        if (email != null) email.equals(OWNER_EMAIL, ignoreCase = true) else BuildConfig.DEBUG

    suspend fun seedIfNeeded(context: Context, db: DaftarDatabase, email: String? = null) {
        if (!allowedFor(email)) return
        val meta = db.appMetaDao()
        if (meta.get(SEEDED_KEY) != null) return
        if (db.institutionDao().getById("demo-inst") != null) return
        // لا تُضاف فوق بيانات حقيقية
        if (db.classDao().getById("demo-1as1") == null && hasRealData(db)) { meta.put(AppMetaEntity(SEEDED_KEY, "skipped")); return }
        seed(context, db)
        meta.put(AppMetaEntity(SEEDED_KEY, System.currentTimeMillis().toString()))
    }

    private suspend fun hasRealData(db: DaftarDatabase): Boolean =
        db.institutionDao().ownCount() > 0

    private val NAMES = listOf(
        "أحمد محمد سالم" to "Ahmed Mohamed Salem", "فاطمة عبد الله أحمد" to "Fatimetou Abdallahi Ahmed",
        "محمد الأمين سيدي" to "Mohamed Lemine Sidi", "مريم الشيخ محمد" to "Mariem Cheikh Mohamed",
        "عبد الرحمن أحمدو" to "Abderrahmane Ahmedou", "خديجة محمد المختار" to "Khadijetou Mohamed Moctar",
        "سيدي محمد عبد الله" to "Sidi Mohamed Abdallahi", "عائشة أحمد بابه" to "Aicha Ahmed Babe",
        "محمد فال الشيخ" to "Mohamed Vall Cheikh", "زينب محمد الأمين" to "Zeinebou Mohamed Lemine",
        "الشيخ أحمد محمود" to "Cheikh Ahmed Mahmoud", "آمنة سيدي محمد" to "Amna Sidi Mohamed",
        "عبد الله محمد يحي" to "Abdallahi Mohamed Yahya", "مريم أحمد سالم" to "Mariem Ahmed Salem",
        "يحي عبد الرحمن" to "Yahya Abderrahmane", "فاطمة الزهراء محمد" to "Fatimetou Zahra Mohamed",
        "محمد المصطفى أحمد" to "Mohamed Moustapha Ahmed", "السالكة محمد" to "Salka Mohamed",
        "أحمد سالم الطالب" to "Ahmed Salem Taleb", "حواء عبد الله" to "Hawa Abdallahi",
        "محمد محمود أحمد" to "Mohamed Mahmoud Ahmed", "النانه سيدي" to "Nana Sidi",
        "عمر أحمد عبد الله" to "Oumar Ahmed Abdallahi", "لاله محمد" to "Lalla Mohamed",
        "إبراهيم الشيخ" to "Ibrahima Cheikh", "مريم سيدي أحمد" to "Mariem Sidi Ahmed",
        "الحسن محمد" to "El Hacen Mohamed", "توت أحمد" to "Toutou Ahmed",
        "عثمان محمد" to "Ousmane Mohamed", "أم كلثوم أحمد" to "Oumou Kelthoum Ahmed"
    )

    private val GIRLS = setOf(
        "آمنة سيدي محمد",
        "أم كلثوم أحمد",
        "السالكة محمد",
        "النانه سيدي",
        "توت أحمد",
        "حواء عبد الله",
        "خديجة محمد المختار",
        "زينب محمد الأمين",
        "عائشة أحمد بابه",
        "فاطمة الزهراء محمد",
        "فاطمة عبد الله أحمد",
        "لاله محمد",
        "مريم أحمد سالم",
        "مريم الشيخ محمد",
        "مريم سيدي أحمد"
    )

    private suspend fun seed(context: Context, db: DaftarDatabase) {
        val now = System.currentTimeMillis()
        val rnd = Random(2026)
        val wilaya = "انواكشوط 2 (الغربية)"
        val listed = SchoolDirectory.wilaya(context, wilaya)?.schools?.firstOrNull { it.name == "إعدادية البنات" }
        val own = InstitutionEntity(
            id = "demo-inst", wilaya = wilaya, schoolName = listed?.name ?: "إعدادية البنات",
            schoolListId = listed?.id, stage = Stage.BOTH, schoolYear = YEAR, createdAt = now, isOwn = true
        )
        val other = InstitutionEntity(
            id = "demo-inst-2", wilaya = wilaya, schoolName = "ثانوية تفرغ زينه",
            schoolListId = SchoolDirectory.wilaya(context, wilaya)?.schools?.firstOrNull { it.name == "ثانوية تفرغ زينه" }?.id,
            stage = Stage.LYCEE, schoolYear = YEAR, createdAt = now, isOwn = false
        )
        db.institutionDao().save(own)
        db.institutionDao().save(other)

        // قسم 1AS1: كل النتائج مكتملة → يمكن إنتاج كشوف الفصول الثلاثة
        seedClass(context, db, rnd, own, "demo-1as1", "1AS", 1, 30, completeness = 1.0, teaching = true,
            teachers = mapOf("MATS" to "محمد الأمين أحمد", "AR" to "سيدي محمد ولد أحمد", "FR" to "Mohamed Salem", "IR" to "أحمد ولد محمد"))
        // قسم 5C1: نتائج ناقصة → يظهر شريط المواد المتبقية
        seedClass(context, db, rnd, own, "demo-5c1", "5C", 1, 24, completeness = 0.6, teaching = false,
            teachers = mapOf("MATS" to "محمد الأمين أحمد", "PC" to "خديجة بنت عبد الله"))
        // قسم من مؤسسة أخرى في «أقسامي» فقط (الأستاذ يدرّس في مدرستين)
        seedClass(context, db, rnd, other, "demo-6d1", "6D", 1, 20, completeness = 0.3, teaching = true,
            managed = false, teachers = emptyMap())

        db.gradeDao().saveTeacherSubject(TeacherSubjectEntity("demo-1as1", "MATS"))
        db.gradeDao().saveTeacherSubject(TeacherSubjectEntity("demo-6d1", "MATS"))
        val meta = db.appMetaDao()
        if (meta.get("teacher_name") == null) meta.put(AppMetaEntity("teacher_name", "محمد الأمين أحمد"))
        meta.put(AppMetaEntity("pass_thresholds", """{"1AS":10,"5C":9}"""))
    }

    private suspend fun seedClass(
        context: Context, db: DaftarDatabase, rnd: Random, inst: InstitutionEntity,
        classId: String, level: String, number: Int, size: Int, completeness: Double,
        teaching: Boolean, managed: Boolean = true, teachers: Map<String, String>
    ) {
        val now = System.currentTimeMillis()
        db.classDao().insert(
            ClassEntity(classId, inst.id, YEAR, level, number, now, managed = managed, teaching = teaching)
        )
        val students = (0 until size).map { i ->
            val (ar, fr) = NAMES[(i + number * 7 + level.hashCode().and(7)) % NAMES.size]
            StudentEntity(
                id = "demo-$classId-s${i + 1}", classId = classId, rollNumber = i + 1,
                nameAr = ar, nameFr = fr,
                rim = "26" + (100000 + rnd.nextInt(899999)).toString(),
                nationalId = (1000000000L + rnd.nextLong(8999999999L)).toString(),
                guardianPhone1 = listOf("2", "3", "4")[rnd.nextInt(3)] + (1000000 + rnd.nextInt(8999999)),
                guardianPhone2 = if (rnd.nextBoolean()) "4" + (1000000 + rnd.nextInt(8999999)) else "",
                createdAt = now,
                gender = if (ar in GIRLS) Gender.GIRL else Gender.BOY
            )
        }
        db.studentDao().saveAll(students)
        val subjects = CurriculumRepository.level(context, YEAR, level)?.subjects.orEmpty()
        val subjectCount = (subjects.size * completeness).toInt().coerceAtLeast(1)
        subjects.take(subjectCount).forEach { sub ->
            students.forEach { s ->
                val base = 8 + rnd.nextInt(10)          // مستوى الطالب التقريبي
                GradeSlot.ALL.forEach { slot ->
                    val v = (base + rnd.nextInt(5) - 2).coerceIn(0, 20) + listOf(0.0, 0.25, 0.5, 0.75)[rnd.nextInt(4)]
                    db.gradeDao().save(GradeEntity(s.id, classId, sub.code, slot, v.coerceAtMost(20.0), now))
                }
            }
            teachers[sub.code]?.let { name ->
                db.gradeDao().saveRecord(SubjectRecordEntity(classId, sub.code, name, TestsMode.AVERAGE, now))
            }
        }
    }
}
