package com.elyedali.daftarustadh.data

import android.content.Context
import org.json.JSONObject

/** مادة دراسية بضاربها وساعاتها الأسبوعية في مستوى معيّن. */
data class CurriculumSubject(
    val code: String,
    val nameAr: String,
    val nameFr: String,
    val coefficient: Int,
    val weeklyHours: Int
)

/** المواد التي تُدرَّس بالفرنسية: يظهر اسمها بالفرنسية دائمًا، مهما كانت لغة التطبيق. */
object SubjectNames {
    val TAUGHT_IN_FRENCH = setOf("FR", "MATS", "PC", "SN", "TI")
    fun display(s: CurriculumSubject, frUi: Boolean): String = if (frUi || s.code in TAUGHT_IN_FRENCH) s.nameFr else s.nameAr
}

/** مستوى دراسي (مثل 1AS أو 5C) ومواده. */
data class CurriculumLevel(
    val code: String,
    val stage: String,          // "college" إعدادية | "lycee" ثانوية
    val system: String,         // "new" النظام الجديد | "old" النظام القديم
    val lastSchoolYear: String?, // آخر سنة دراسية يُنشأ فيها هذا المستوى (مثل 4AS)
    val subjects: List<CurriculumSubject>
) {
    val totalCoefficient: Int get() = subjects.sumOf { it.coefficient }
}

/**
 * مواد كل سنة دراسية محفوظة في ملف خاص بها داخل التطبيق:
 * assets/curriculum/curriculum_2026_2027.json ، ثم curriculum_2027_2028.json ... إلخ.
 * القسم يأخذ مواد سنته الدراسية؛ وإن لم يوجد ملف لسنته يأخذ آخر سنة سابقة متوفرة.
 * هكذا لا تتغيّر مواد أقسام السنوات الماضية ولا كشوفها حين تُضاف سنة جديدة.
 */
object CurriculumRepository {
    private const val DIR = "curriculum"
    private val cache = mutableMapOf<String, List<CurriculumLevel>>()

    fun availableYears(context: Context): List<String> =
        (context.assets.list(DIR) ?: emptyArray())
            .mapNotNull { Regex("""curriculum_(\d{4})_(\d{4})\.json""").matchEntire(it) }
            .map { "${it.groupValues[1]}-${it.groupValues[2]}" }
            .sorted()

    /** السنة التي تُؤخذ منها المواد لسنة دراسية معيّنة. */
    fun resolveYear(context: Context, schoolYear: String): String? {
        val years = availableYears(context)
        return years.lastOrNull { it <= schoolYear } ?: years.firstOrNull()
    }

    fun levels(context: Context, schoolYear: String): List<CurriculumLevel> {
        val year = resolveYear(context, schoolYear) ?: return emptyList()
        return cache.getOrPut(year) { load(context, year) }
    }

    /** المستويات التي يمكن إنشاء قسم منها في سنة دراسية معيّنة ولمرحلة معيّنة. */
    fun creatableLevels(context: Context, schoolYear: String, stage: String): List<CurriculumLevel> =
        levels(context, schoolYear).filter { lvl ->
            lvl.stage == stage && (lvl.lastSchoolYear == null || schoolYear <= lvl.lastSchoolYear)
        }

    fun level(context: Context, schoolYear: String, code: String): CurriculumLevel? =
        levels(context, schoolYear).firstOrNull { it.code == code }

    private fun load(context: Context, year: String): List<CurriculumLevel> {
        val file = "$DIR/curriculum_${year.replace('-', '_')}.json"
        val root = JSONObject(context.assets.open(file).bufferedReader(Charsets.UTF_8).use { it.readText() })
        val arr = root.getJSONArray("levels")
        return (0 until arr.length()).map { i ->
            val l = arr.getJSONObject(i)
            val subs = l.getJSONArray("subjects")
            CurriculumLevel(
                code = l.getString("code"),
                stage = l.getString("stage"),
                system = l.getString("system"),
                lastSchoolYear = if (l.has("last_school_year")) l.getString("last_school_year") else null,
                subjects = (0 until subs.length()).map { j ->
                    val s = subs.getJSONObject(j)
                    CurriculumSubject(
                        code = s.getString("code"),
                        nameAr = s.getString("name_ar"),
                        nameFr = s.getString("name_fr"),
                        coefficient = s.getInt("coefficient"),
                        weeklyHours = s.getInt("weekly_hours")
                    )
                }
            )
        }
    }
}
