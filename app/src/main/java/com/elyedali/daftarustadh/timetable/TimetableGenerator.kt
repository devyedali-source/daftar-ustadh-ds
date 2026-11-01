package com.elyedali.daftarustadh.timetable

import kotlin.math.exp
import kotlin.random.Random

/**
 * مولّد الجدول الزمني «المنصف».
 *
 * قيود لا تُخرق: لا يكون للقسم حصتان في وقت واحد، ولا يكون الأستاذ في قسمين في وقت واحد.
 * ما يُحسَّن (كلما قلّ كان أفضل):
 *  - ساعات كل أستاذ متتالية في اليوم (بلا فراغات بين حصصه)،
 *  - حصص القسم متصلة من أول اليوم (بلا فراغات في وسط اليوم)،
 *  - المادة الواحدة لا تتجاوز ساعتين في اليوم، وساعتاها في نفس اليوم متتاليتان،
 *  - توزيع ساعات المادة على أيام مختلفة،
 *  - توزيع عبء الأستاذ على أيامه دون إرهاق يوم واحد.
 */
object TimetableGenerator {

    /** مادة في قسم: عدد ساعاتها الأسبوعية وأستاذها (فارغ = بلا أستاذ معيّن). */
    data class Lesson(val classId: String, val subjectCode: String, val teacher: String, val hours: Int)

    data class Placed(val classId: String, val day: Int, val period: Int, val subjectCode: String, val teacher: String)

    data class Result(
        val placed: List<Placed>,
        val unplaced: List<Lesson>,      // ساعات لم يجد لها المولّد مكانًا (مع عددها في hours)
        val teacherGaps: Int,            // مجموع الفراغات في أيام الأساتذة
        val overloadedClasses: List<String> // أقسام ساعاتها أكثر من حصص الأسبوع
    )

    private class LessonUnit(val classIdx: Int, val subject: String, val teacherIdx: Int, val teacher: String)

    fun generate(
        lessons: List<Lesson>,
        dayCount: Int,
        periods: Int,
        seed: Long = System.currentTimeMillis(),
        iterations: Int = 120_000
    ): Result {
        val rnd = Random(seed)
        val classIds = lessons.map { it.classId }.distinct()
        val teachers = lessons.map { it.teacher }.filter { it.isNotBlank() }.distinct()
        val classIndex = classIds.withIndex().associate { it.value to it.index }
        val teacherIndex = teachers.withIndex().associate { it.value to it.index }
        val slots = dayCount * periods
        val overloaded = classIds.filter { c -> lessons.filter { it.classId == c }.sumOf { it.hours } > slots }

        // وحدات الساعة
        val units = ArrayList<LessonUnit>()
        lessons.forEach { l ->
            repeat(l.hours) { units += LessonUnit(classIndex.getValue(l.classId), l.subjectCode, teacherIndex[l.teacher] ?: -1, l.teacher) }
        }
        val nC = classIds.size
        val nT = teachers.size
        // شغل: القسم × الخانة -> رقم الوحدة أو -1 ؛ الأستاذ × الخانة -> رقم الوحدة أو -1
        val classAt = Array(nC) { IntArray(slots) { -1 } }
        val teacherAt = Array(nT) { IntArray(slots) { -1 } }
        val pos = IntArray(units.size) { -1 }

        fun free(u: Int, s: Int): Boolean {
            val unit = units[u]
            if (classAt[unit.classIdx][s] != -1) return false
            return unit.teacherIdx < 0 || teacherAt[unit.teacherIdx][s] == -1
        }

        fun place(u: Int, s: Int) {
            val unit = units[u]
            classAt[unit.classIdx][s] = u
            if (unit.teacherIdx >= 0) teacherAt[unit.teacherIdx][s] = u
            pos[u] = s
        }

        fun remove(u: Int) {
            val s = pos[u]; if (s < 0) return
            val unit = units[u]
            classAt[unit.classIdx][s] = -1
            if (unit.teacherIdx >= 0) teacherAt[unit.teacherIdx][s] = -1
            pos[u] = -1
        }

        // تكلفة يوم قسم: فراغات وسط اليوم، تأخير البداية، ومواد مكرّرة أكثر من ساعتين أو غير متتالية
        fun classDayCost(c: Int, d: Int): Double {
            var cost = 0.0
            var first = -1; var last = -1; var count = 0
            val bySubject = HashMap<String, MutableList<Int>>()
            for (p in 0 until periods) {
                val u = classAt[c][d * periods + p]
                if (u >= 0) {
                    if (first < 0) first = p
                    last = p; count++
                    bySubject.getOrPut(units[u].subject) { ArrayList() }.add(p)
                }
            }
            if (count > 0) {
                cost += 4.0 * (last - first + 1 - count)  // فراغات في وسط اليوم
                cost += 0.6 * first                        // يبدأ متأخرًا
            }
            bySubject.values.forEach { ps ->
                if (ps.size > 2) cost += 6.0 * (ps.size - 2)
                if (ps.size >= 2) {
                    // الساعات في نفس اليوم يجب أن تكون متتالية
                    val span = ps.max() - ps.min() + 1
                    cost += 2.5 * (span - ps.size)
                }
            }
            return cost
        }

        // تكلفة يوم أستاذ: فراغات بين حصصه، وعبء ثقيل في يوم واحد
        fun teacherDayCost(t: Int, d: Int): Double {
            var first = -1; var last = -1; var count = 0
            for (p in 0 until periods) {
                if (teacherAt[t][d * periods + p] >= 0) {
                    if (first < 0) first = p
                    last = p; count++
                }
            }
            if (count == 0) return 0.0
            var cost = 3.0 * (last - first + 1 - count)
            if (count > 5) cost += 2.0 * (count - 5)
            if (count == 1) cost += 0.8  // حصة يتيمة في يوم كامل
            return cost
        }

        // توزيع ساعات المادة على الأيام: أيام بلا حصة لمادة كثيرة الساعات
        fun subjectSpreadCost(c: Int): Double {
            val perSubjectDays = HashMap<String, MutableSet<Int>>()
            val perSubjectCount = HashMap<String, Int>()
            for (s in 0 until slots) {
                val u = classAt[c][s]
                if (u >= 0) {
                    perSubjectDays.getOrPut(units[u].subject) { HashSet() }.add(s / periods)
                    perSubjectCount[units[u].subject] = (perSubjectCount[units[u].subject] ?: 0) + 1
                }
            }
            var cost = 0.0
            perSubjectCount.forEach { (sub, n) ->
                val ideal = minOf(dayCount, (n + 1) / 2)
                val used = perSubjectDays[sub]?.size ?: 0
                if (used < ideal) cost += 1.5 * (ideal - used)
            }
            return cost
        }

        fun dayCostAround(u: Int, s: Int): Double {
            val unit = units[u]; val d = s / periods
            var c = classDayCost(unit.classIdx, d) + subjectSpreadCost(unit.classIdx)
            if (unit.teacherIdx >= 0) c += teacherDayCost(unit.teacherIdx, d)
            return c
        }

        // 1) توزيع أولي جشع: الأساتذة الأكثر عبئًا والمواد الأكثر ساعات أولًا
        val teacherLoad = IntArray(nT)
        units.forEach { if (it.teacherIdx >= 0) teacherLoad[it.teacherIdx]++ }
        val tieBreak = IntArray(units.size) { rnd.nextInt() }
        val order = units.indices.sortedWith(
            compareByDescending<Int> { if (units[it].teacherIdx >= 0) teacherLoad[units[it].teacherIdx] else 0 }
                .thenBy { tieBreak[it] }
        )
        val unplacedUnits = ArrayList<Int>()
        for (u in order) {
            var best = -1; var bestCost = Double.MAX_VALUE
            for (s in 0 until slots) {
                if (!free(u, s)) continue
                place(u, s)
                val cost = dayCostAround(u, s) + rnd.nextDouble() * 0.3
                remove(u)
                if (cost < bestCost) { bestCost = cost; best = s }
            }
            if (best >= 0) place(u, best) else unplacedUnits += u
        }

        // 2) تحسين محلي (تلدين محاكى): نقل حصة إلى خانة فارغة، أو تبديل حصتين في نفس القسم
        fun totalCost(): Double {
            var c = 0.0
            for (ci in 0 until nC) { for (d in 0 until dayCount) c += classDayCost(ci, d); c += subjectSpreadCost(ci) }
            for (t in 0 until nT) for (d in 0 until dayCount) c += teacherDayCost(t, d)
            return c
        }

        fun localCost(cls: Int, ts: List<Int>, days: Set<Int>): Double {
            var c = subjectSpreadCost(cls)
            days.forEach { d ->
                c += classDayCost(cls, d)
                ts.forEach { t -> if (t >= 0) c += teacherDayCost(t, d) }
            }
            return c
        }

        var temp = 2.0
        val cooling = Math.pow(0.01 / temp, 1.0 / iterations.coerceAtLeast(1))
        val placedUnits = units.indices.filter { pos[it] >= 0 }
        if (placedUnits.isNotEmpty()) repeat(iterations) {
            val u = placedUnits[rnd.nextInt(placedUnits.size)]
            val unit = units[u]
            val from = pos[u]
            val to = rnd.nextInt(slots)
            if (to == from) { temp *= cooling; return@repeat }
            val other = classAt[unit.classIdx][to]
            if (other < 0) {
                // نقل إلى خانة فارغة في القسم
                if (unit.teacherIdx >= 0 && teacherAt[unit.teacherIdx][to] != -1) { temp *= cooling; return@repeat }
                val days = setOf(from / periods, to / periods)
                val before = localCost(unit.classIdx, listOf(unit.teacherIdx), days)
                remove(u); place(u, to)
                val after = localCost(unit.classIdx, listOf(unit.teacherIdx), days)
                val delta = after - before
                if (delta > 0 && rnd.nextDouble() >= exp(-delta / temp)) { remove(u); place(u, from) }
            } else {
                // تبديل حصتين من نفس القسم
                val o = units[other]
                if (o.subject == unit.subject && o.teacherIdx == unit.teacherIdx) { temp *= cooling; return@repeat }
                remove(u); remove(other)
                val ok = (unit.teacherIdx < 0 || teacherAt[unit.teacherIdx][to] == -1) &&
                    (o.teacherIdx < 0 || teacherAt[o.teacherIdx][from] == -1)
                if (!ok) { place(u, from); place(other, to); temp *= cooling; return@repeat }
                val days = setOf(from / periods, to / periods)
                val ts = listOf(unit.teacherIdx, o.teacherIdx)
                place(u, from); place(other, to)
                val before = localCost(unit.classIdx, ts, days)
                remove(u); remove(other)
                place(u, to); place(other, from)
                val after = localCost(unit.classIdx, ts, days)
                val delta = after - before
                if (delta > 0 && rnd.nextDouble() >= exp(-delta / temp)) {
                    remove(u); remove(other); place(u, from); place(other, to)
                }
            }
            temp *= cooling
        }

        // محاولة أخيرة لوضع ما لم يُوضع
        val stillUnplaced = ArrayList<Int>()
        unplacedUnits.forEach { u ->
            val s = (0 until slots).firstOrNull { free(u, it) }
            if (s != null) place(u, s) else stillUnplaced += u
        }

        var gaps = 0
        for (t in 0 until nT) for (d in 0 until dayCount) {
            var first = -1; var last = -1; var count = 0
            for (p in 0 until periods) if (teacherAt[t][d * periods + p] >= 0) { if (first < 0) first = p; last = p; count++ }
            if (count > 0) gaps += last - first + 1 - count
        }

        val placed = units.indices.filter { pos[it] >= 0 }.map { u ->
            val unit = units[u]
            Placed(classIds[unit.classIdx], pos[u] / periods, pos[u] % periods, unit.subject, unit.teacher)
        }
        val unplaced = stillUnplaced.groupBy { units[it].classIdx to units[it].subject }.map { (k, list) ->
            Lesson(classIds[k.first], k.second, units[list.first()].teacher, list.size)
        }
        @Suppress("UNUSED_VARIABLE") val finalCost = totalCost()
        return Result(placed, unplaced, gaps, overloaded)
    }

    /** يتحقق من إمكانية تبديل حصتين في قسم (الأستاذ غير مشغول في الوقت الجديد بقسم آخر). */
    fun canSwap(
        slots: List<Placed>,
        classId: String,
        a: Pair<Int, Int>,
        b: Pair<Int, Int>
    ): String? {
        val sa = slots.firstOrNull { it.classId == classId && it.day == a.first && it.period == a.second }
        val sb = slots.firstOrNull { it.classId == classId && it.day == b.first && it.period == b.second }
        fun busy(teacher: String, day: Int, period: Int) = teacher.isNotBlank() &&
            slots.any { it.classId != classId && it.teacher == teacher && it.day == day && it.period == period }
        if (sa != null && busy(sa.teacher, b.first, b.second)) return sa.teacher
        if (sb != null && busy(sb.teacher, a.first, a.second)) return sb.teacher
        return null
    }
}
