package com.elyedali.daftarustadh.data

/**
 * تفعيل الأستاذ للسنة الدراسية: التخطيط السنوي والتحاضير للفصلين الثاني والثالث (الفصل الأول مجاني).
 * التفعيل الواحد يشمل 3 مواد في 4 مستويات يختارها الأستاذ، لحسابه وللسنة الدراسية الواحدة،
 * ويسري على كل أقسامه من هذه المستويات: ما وصل من الإدارة وما أنشأه بنفسه في العمل الحر.
 * رصد النتائج مجاني دائمًا للأستاذ.
 */
object TeacherActivation {
    const val KEY = "teacher_activated_years"        // السنوات المفعّلة (تضعها الإدارة/الخادم)
    const val REQUESTED_KEY = "teacher_activation_requested"
    const val CODE_KEY = "teacher_install_code"
    const val PRICE = 190
    /** التفعيلات السارية: سطر لكل تفعيل «السنة;المواد;المستويات» (تضعها الاشتراكات من الخادم). */
    const val PACKS_KEY = "teacher_activation_packs"
    const val MAX_SUBJECTS = 3
    const val MAX_LEVELS = 4

    /** تفعيل واحد: مواد ومستويات لسنة دراسية. بلا مواد ولا مستويات = كل المواد والمستويات (تفعيل قديم). */
    data class Pack(val year: String, val subjects: Set<String>, val levels: Set<String>) {
        val all: Boolean get() = subjects.isEmpty() && levels.isEmpty()
        fun covers(level: String, subject: String) = all || (level in levels && subject in subjects)
        fun coversLevel(level: String) = all || level in levels
    }

    /** صيغة التفعيل على الخادم: «MATS,PC,SN|1AS,2AS,3AS,5C». */
    fun packString(subjects: Collection<String>, levels: Collection<String>): String =
        subjects.distinct().sorted().joinToString(",") + "|" + levels.distinct().sorted().joinToString(",")

    fun parsePack(year: String, s: String): Pack {
        val parts = s.split('|')
        fun set(i: Int) = parts.getOrNull(i).orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        return Pack(year, set(0), set(1))
    }

    fun encodePacks(list: Collection<Pack>): String =
        list.map { "${it.year};${packString(it.subjects, it.levels)}" }.distinct().sorted().joinToString("\n")

    fun decodePacks(text: String?): List<Pack> = text.orEmpty().lines().filter { it.contains(';') }.map { line ->
        parsePack(line.substringBefore(';'), line.substringAfter(';'))
    }

    /** تخطيط هذه المادة في هذا المستوى مفعّل لهذه السنة؟ */
    fun unlocked(packs: List<Pack>, year: String, level: String, subject: String) =
        packs.any { it.year == year && it.covers(level, subject) }

    /** المواد المفعّلة في هذا المستوى لهذه السنة (null = كل المواد). */
    fun subjectsFor(packs: List<Pack>, year: String, level: String): Set<String>? {
        val mine = packs.filter { it.year == year && it.coversLevel(level) }
        if (mine.any { it.all }) return null
        return mine.flatMap { it.subjects }.toSet()
    }

    fun decode(text: String?): Set<String> = ClassActivation.decode(text)
    fun encode(years: Set<String>): String = ClassActivation.encode(years)

    /** التخطيط والتحاضير: الفصل الأول مجاني. */
    fun planningNeedsActivation(term: Int) = term >= 2

    /** كود قصير لهذا الجهاز يُرسل في طلب التفعيل. */
    fun newCode(): String = java.util.UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
}
