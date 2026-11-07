package com.elyedali.daftarustadh.timetable

import kotlin.math.exp
import kotlin.random.Random

/**
 * مولّد الجدول الزمني «المنصف».
 *
 * الحصة غالبًا ساعتان وأحيانًا ساعة: ساعات كل مادة تُقسَّم إلى حصص من ساعتين متتاليتين
 * (لا تقطعهما استراحة)، وما بقي (ساعة فردية) حصة من ساعة.
 *
 * قيود لا تُخرق: لا يكون للقسم حصتان في وقت واحد، ولا يكون الأستاذ في قسمين في وقت واحد،
 * ولا توضع حصة في وقت مقفل (مثل الجمعة بعد 12:00)، ولا تقطع الاستراحة حصة الساعتين.
 * ما يُحسَّن: ساعات الأستاذ متتالية في اليوم، حصص القسم متصلة من أول اليوم،
 * المادة لا تتجاوز ساعتين في اليوم، وتتوزع حصصها على أيام الأسبوع، دون إرهاق الأستاذ في يوم واحد.
 */
object TimetableGenerator {

    /** مادة في قسم: عدد ساعاتها الأسبوعية وأستاذها (فارغ = بلا أستاذ معيّن). */
    data class Lesson(val classId: String, val subjectCode: String, val teacher: String, val hours: Int)

    data class Placed(val classId: String, val day: Int, val period: Int, val subjectCode: String, val teacher: String)

    data class Result(
        val placed: List<Placed>,
        val unplaced: List<Lesson>,          // ساعات لم يجد لها المولّد مكانًا (مع عددها في hours)
        val teacherGaps: Int,                // مجموع الفراغات في أيام الأساتذة
        val overloadedClasses: List<String>  // أقسام ساعاتها أكثر من حصص الأسبوع
    )

    private class LessonUnit(val classIdx: Int, val subject: String, val teacherIdx: Int, val teacher: String, val size: Int)

    /**
     * @param periodsPerDay عدد حصص كل يوم (الجمعة أقصر)؛ null = كل الأيام كاملة.
     * @param doubleStarts أرقام الحصص التي يمكن أن تبدأ بها حصة من ساعتين (هي والتي تليها بلا استراحة بينهما)؛
     *                     null = أي حصتين متتاليتين.
     */
    private fun teacherLoad0(units: List<LessonUnit>, t: Int): Int = units.filter { it.teacherIdx == t }.sumOf { it.size }

    fun generate(
        lessons: List<Lesson>,
        dayCount: Int,
        periods: Int,
        seed: Long = System.currentTimeMillis(),
        iterations: Int = 150_000,
        periodsPerDay: List<Int>? = null,
        doubleStarts: Set<Int>? = null
    ): Result {
        val rnd = Random(seed)
        val perDay = IntArray(dayCount) { d -> (periodsPerDay?.getOrNull(d) ?: periods).coerceIn(0, periods) }
        val starts2 = doubleStarts ?: (0 until periods - 1).toSet()
        val classIds = lessons.map { it.classId }.distinct()
        val teachers = lessons.map { it.teacher }.filter { it.isNotBlank() }.distinct()
        val classIndex = classIds.withIndex().associate { it.value to it.index }
        val teacherIndex = teachers.withIndex().associate { it.value to it.index }
        val slots = dayCount * periods
        val openSlots = perDay.sum()
        val overloaded = classIds.filter { c -> lessons.filter { it.classId == c }.sumOf { it.hours } > openSlots }

        // الحصص: ساعتان ساعتان، والساعة الفردية حصة من ساعة
        val units = ArrayList<LessonUnit>()
        lessons.forEach { l ->
            val c = classIndex.getValue(l.classId)
            val t = teacherIndex[l.teacher] ?: -1
            repeat(l.hours / 2) { units += LessonUnit(c, l.subjectCode, t, l.teacher, 2) }
            if (l.hours % 2 == 1) units += LessonUnit(c, l.subjectCode, t, l.teacher, 1)
        }
        val nC = classIds.size
        val nT = teachers.size
        val classAt = Array(nC) { IntArray(slots) { -1 } }
        val teacherAt = Array(nT) { IntArray(slots) { -1 } }
        val pos = IntArray(units.size) { -1 }

        /** هل يصح أن تبدأ الحصة u في الخانة s (بغض النظر عن الشغل)؟ */
        fun validStart(u: Int, s: Int): Boolean {
            val d = s / periods; val p = s % periods
            val size = units[u].size
            if (p + size > perDay[d]) return false
            return size == 1 || p in starts2
        }

        fun free(u: Int, s: Int): Boolean {
            if (!validStart(u, s)) return false
            val unit = units[u]
            for (k in 0 until unit.size) {
                if (classAt[unit.classIdx][s + k] != -1) return false
                if (unit.teacherIdx >= 0 && teacherAt[unit.teacherIdx][s + k] != -1) return false
            }
            return true
        }

        fun place(u: Int, s: Int) {
            val unit = units[u]
            for (k in 0 until unit.size) {
                classAt[unit.classIdx][s + k] = u
                if (unit.teacherIdx >= 0) teacherAt[unit.teacherIdx][s + k] = u
            }
            pos[u] = s
        }

        fun remove(u: Int) {
            val s = pos[u]; if (s < 0) return
            val unit = units[u]
            for (k in 0 until unit.size) {
                classAt[unit.classIdx][s + k] = -1
                if (unit.teacherIdx >= 0) teacherAt[unit.teacherIdx][s + k] = -1
            }
            pos[u] = -1
        }

        fun classDayCost(c: Int, d: Int): Double {
            var cost = 0.0
            var first = -1; var last = -1; var count = 0
            val hoursBySubject = HashMap<String, Int>()
            val unitsBySubject = HashMap<String, MutableSet<Int>>()
            for (p in 0 until periods) {
                val u = classAt[c][d * periods + p]
                if (u >= 0) {
                    if (first < 0) first = p
                    last = p; count++
                    val sub = units[u].subject
                    hoursBySubject[sub] = (hoursBySubject[sub] ?: 0) + 1
                    unitsBySubject.getOrPut(sub) { HashSet() }.add(u)
                }
            }
            if (count > 0) {
                cost += 4.0 * (last - first + 1 - count)  // فراغات في وسط اليوم
                cost += 0.6 * first                        // يبدأ متأخرًا
            }
            hoursBySubject.forEach { (sub, h) ->
                if (h > 2) cost += 6.0 * (h - 2)                          // أكثر من ساعتين للمادة في اليوم
                if ((unitsBySubject[sub]?.size ?: 0) > 1) cost += 3.0    // حصتان منفصلتان لنفس المادة في يوم واحد
            }
            return cost
        }

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
            if (count == 1) cost += 0.8
            return cost
        }

        // حصص المادة الواحدة في القسم تتوزع على أيام مختلفة
        fun subjectSpreadCost(c: Int): Double {
            val days = HashMap<String, MutableSet<Int>>()
            val count = HashMap<String, Int>()
            val seen = HashSet<Int>()
            for (s in 0 until slots) {
                val u = classAt[c][s]
                if (u >= 0 && seen.add(u)) {
                    days.getOrPut(units[u].subject) { HashSet() }.add(s / periods)
                    count[units[u].subject] = (count[units[u].subject] ?: 0) + 1
                }
            }
            var cost = 0.0
            val openDays = perDay.count { it > 0 }
            count.forEach { (sub, n) ->
                val ideal = minOf(openDays, n)
                val used = days[sub]?.size ?: 0
                if (used < ideal) cost += 1.5 * (ideal - used)
            }
            return cost
        }

        // أيام عمل الأستاذ: تُجمع حصصه في أقل عدد من الأيام المتتالية، فتبقى له أيام فارغة كاملة
        // (يعمل مثلًا بداية الأسبوع، أو نهايته، فيقدر على العمل في مؤسسة أخرى بقية الأيام).
        // أساتذة المادة الواحدة يتقاسمون الأسبوع: واحد يميل إلى أوله، والآخر إلى آخره.
        val openDayList = (0 until dayCount).filter { perDay[it] > 0 }
        val neededDays = IntArray(nT) { t -> ((teacherLoad0(units, t) + 4) / 5).coerceIn(1, openDayList.size.coerceAtLeast(1)) }
        val leanLate = BooleanArray(nT)
        units.filter { it.teacherIdx >= 0 }.groupBy { it.subject }.forEach { (_, us) ->
            us.map { it.teacher }.distinct().sorted().forEachIndexed { i, name -> teacherIndex[name]?.let { if (i % 2 == 1) leanLate[it] = true } }
        }
        fun teacherWeekCost(t: Int): Double {
            val worked = openDayList.filter { d -> (0 until periods).any { p -> teacherAt[t][d * periods + p] >= 0 } }
            if (worked.isEmpty()) return 0.0
            val pos0 = worked.map { openDayList.indexOf(it) }
            var cost = 3.0 * (worked.size - neededDays[t]).coerceAtLeast(0)    // يوم عمل زائد
            cost += 1.0 * (pos0.last() - pos0.first() + 1 - worked.size)       // يوم فارغ في وسط أيام عمله
            val last = openDayList.size - 1
            cost += 0.15 * pos0.sumOf { if (leanLate[t]) last - it else it }  // الميل إلى أول الأسبوع أو آخره
            return cost
        }

        fun localCost(cls: Int, ts: List<Int>, days: Set<Int>): Double {
            var c = subjectSpreadCost(cls)
            ts.distinct().forEach { t -> if (t >= 0) c += teacherWeekCost(t) }
            days.forEach { d ->
                c += classDayCost(cls, d)
                ts.forEach { t -> if (t >= 0) c += teacherDayCost(t, d) }
            }
            return c
        }

        // 1) توزيع أولي جشع: حصص الساعتين أولًا، ثم الأساتذة الأكثر عبئًا
        val teacherLoad = IntArray(nT)
        units.forEach { if (it.teacherIdx >= 0) teacherLoad[it.teacherIdx] += it.size }
        val tieBreak = IntArray(units.size) { rnd.nextInt() }
        val order = units.indices.sortedWith(
            compareByDescending<Int> { units[it].size }
                .thenByDescending { if (units[it].teacherIdx >= 0) teacherLoad[units[it].teacherIdx] else 0 }
                .thenBy { tieBreak[it] }
        )
        val unplacedUnits = ArrayList<Int>()
        for (u in order) {
            var best = -1; var bestCost = Double.MAX_VALUE
            val unit = units[u]
            for (s in 0 until slots) {
                if (!free(u, s)) continue
                place(u, s)
                val cost = localCost(unit.classIdx, listOf(unit.teacherIdx), setOf(s / periods)) + rnd.nextDouble() * 0.3
                remove(u)
                if (cost < bestCost) { bestCost = cost; best = s }
            }
            if (best >= 0) place(u, best) else unplacedUnits += u
        }

        // 2) تحسين محلي (تلدين محاكى): نقل حصة إلى مكان فارغ، أو تبديل حصتين بنفس المدة في نفس القسم
        var temp = 2.0
        val cooling = Math.pow(0.01 / temp, 1.0 / iterations.coerceAtLeast(1))
        val placedUnits = units.indices.filter { pos[it] >= 0 }
        if (placedUnits.isNotEmpty()) repeat(iterations) {
            val u = placedUnits[rnd.nextInt(placedUnits.size)]
            val unit = units[u]
            val from = pos[u]
            val to = rnd.nextInt(slots)
            if (to == from || !validStart(u, to)) { temp *= cooling; return@repeat }
            val owner = classAt[unit.classIdx][to]
            if (owner < 0 || owner == u) {
                // نقل إلى مكان فارغ
                remove(u)
                val canMove = free(u, to)
                place(u, from)
                if (!canMove) { temp *= cooling; return@repeat }
                val days = setOf(from / periods, to / periods)
                val before = localCost(unit.classIdx, listOf(unit.teacherIdx), days)
                remove(u); place(u, to)
                val after = localCost(unit.classIdx, listOf(unit.teacherIdx), days)
                val delta = after - before
                if (delta > 0 && rnd.nextDouble() >= exp(-delta / temp)) { remove(u); place(u, from) }
            } else {
                // تبديل مع حصة بنفس المدة تبدأ في نفس الخانة
                val o = units[owner]
                if (o.size != unit.size || pos[owner] != to || (o.subject == unit.subject && o.teacherIdx == unit.teacherIdx)) {
                    temp *= cooling; return@repeat
                }
                remove(u); remove(owner)
                var ok = free(u, to)
                if (ok) { place(u, to); ok = free(owner, from); remove(u) }
                place(u, from); place(owner, to)
                if (!ok) { temp *= cooling; return@repeat }
                val days = setOf(from / periods, to / periods)
                val ts = listOf(unit.teacherIdx, o.teacherIdx)
                val before = localCost(unit.classIdx, ts, days)
                remove(u); remove(owner)
                place(u, to); place(owner, from)
                val after = localCost(unit.classIdx, ts, days)
                val delta = after - before
                if (delta > 0 && rnd.nextDouble() >= exp(-delta / temp)) {
                    remove(u); remove(owner); place(u, from); place(owner, to)
                }
            }
            temp *= cooling
        }

        // 3) إصلاح: حصة لم تجد مكانًا (لأن ساعات القسم الفارغة يكون فيها أستاذها مشغولًا)
        //    نُفرغ لها مكانًا بنقل الحصة أو الحصتين اللتين تعترضانها (في القسم أو عند الأستاذ) إلى أماكن أخرى
        fun bestFreeSlot(u: Int): Int {
            var best = -1; var bestCost = Double.MAX_VALUE
            for (s in 0 until slots) {
                if (!free(u, s)) continue
                place(u, s)
                val cost = localCost(units[u].classIdx, listOf(units[u].teacherIdx), setOf(s / periods))
                remove(u)
                if (cost < bestCost) { bestCost = cost; best = s }
            }
            return best
        }

        fun tryRepair(u: Int): Boolean {
            val unit = units[u]
            for (s in (0 until slots).filter { validStart(u, it) }.shuffled(rnd)) {
                val blockers = LinkedHashSet<Int>()
                for (k in 0 until unit.size) {
                    classAt[unit.classIdx][s + k].let { if (it >= 0) blockers += it }
                    if (unit.teacherIdx >= 0) teacherAt[unit.teacherIdx][s + k].let { if (it >= 0) blockers += it }
                }
                if (blockers.isEmpty()) { place(u, s); return true }
                if (blockers.size > 2) continue
                val old = blockers.associateWith { pos[it] }
                blockers.forEach { remove(it) }
                place(u, s)
                val moved = ArrayList<Int>()
                var ok = true
                for (b in blockers) {
                    val t = bestFreeSlot(b)
                    if (t < 0) { ok = false; break }
                    place(b, t); moved += b
                }
                if (ok) return true
                moved.forEach { remove(it) }
                remove(u)
                old.forEach { (b, p) -> place(b, p) }
            }
            return false
        }
        unplacedUnits.removeAll { tryRepair(it) }

        // محاولة أخيرة: حصة لم تجد مكانًا تُوزَّع ساعاتها منفصلة حيث أمكن
        val stillUnplaced = ArrayList<Pair<LessonUnit, Int>>()
        val extraPlaced = ArrayList<Placed>()
        unplacedUnits.forEach { u ->
            val unit = units[u]
            var left = unit.size
            for (s in 0 until slots) {
                if (left == 0) break
                val d = s / periods; val p = s % periods
                if (p >= perDay[d] || classAt[unit.classIdx][s] != -1) continue
                if (unit.teacherIdx >= 0 && teacherAt[unit.teacherIdx][s] != -1) continue
                classAt[unit.classIdx][s] = u
                if (unit.teacherIdx >= 0) teacherAt[unit.teacherIdx][s] = u
                extraPlaced += Placed(classIds[unit.classIdx], d, p, unit.subject, unit.teacher)
                left--
            }
            if (left > 0) stillUnplaced += unit to left
        }

        var gaps = 0
        for (t in 0 until nT) for (d in 0 until dayCount) {
            var first = -1; var last = -1; var count = 0
            for (p in 0 until periods) if (teacherAt[t][d * periods + p] >= 0) { if (first < 0) first = p; last = p; count++ }
            if (count > 0) gaps += last - first + 1 - count
        }

        val placed = units.indices.filter { pos[it] >= 0 }.flatMap { u ->
            val unit = units[u]
            (0 until unit.size).map { k ->
                val s = pos[u] + k
                Placed(classIds[unit.classIdx], s / periods, s % periods, unit.subject, unit.teacher)
            }
        } + extraPlaced
        val unplaced = stillUnplaced.groupBy { it.first.classIdx to it.first.subject }.map { (k, list) ->
            Lesson(classIds[k.first], k.second, list.first().first.teacher, list.sumOf { it.second })
        }
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
