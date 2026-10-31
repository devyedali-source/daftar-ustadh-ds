package com.elyedali.daftarustadh.importer

import com.elyedali.daftarustadh.data.StudentEntity
import java.util.UUID

/** الخانة التي يُربط بها عمود من أعمدة الملف. */
enum class StudentField { IGNORE, ROLL, NAME_AR, NAME_FR, RIM, NATIONAL_ID, PHONE1, PHONE2 }

data class ImportColumn(val index: Int, val header: String?, val samples: List<String>)

/** نتيجة التعرّف التلقائي على الملف: هل في أوله سطر عناوين، وما الخانة المقترحة لكل عمود. */
data class ImportGuess(
    val headerRowIndex: Int?,       // null = لا يوجد سطر عناوين
    val columns: List<ImportColumn>,
    val mapping: List<StudentField>
)

data class ImportPreview(
    val students: List<StudentEntity>,
    val renumbered: Int,            // طلاب أُعطوا رقم نداء جديد (فارغ أو مكرّر)
    val skippedDuplicates: Int,     // طلاب موجودون من قبل في القسم
    val skippedEmpty: Int           // أسطر بلا اسم
)

object StudentImport {

    private fun norm(s: String): String = s.lowercase()
        .replace(Regex("[أإآ]"), "ا").replace('ة', 'ه').replace('ى', 'ي')
        .replace(Regex("[éèê]"), "e")
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

    private fun hasArabic(s: String) = s.any { it in '؀'..'ۿ' }
    private fun hasLatin(s: String) = s.any { it in 'a'..'z' || it in 'A'..'Z' }

    /** خانة مقترحة من عنوان العمود، أو null إن لم يُفهم العنوان. */
    private fun fieldFromHeader(header: String): StudentField? {
        val h = norm(header)
        if (h.isEmpty()) return null
        fun any(vararg k: String) = k.any { h == it || h.contains(it) }
        return when {
            any("الرقم الوطني", "رقم وطني", "nni", "national") -> StudentField.NATIONAL_ID
            any("الرقم المدرسي", "رقم مدرسي", "رقم التسجيل", "matricule", "rim") -> StudentField.RIM
            any("رقم النداء", "النداء", "ordre", "n ordre") || h == "n" || h == "no" || h == "num" || h == "رقم" || h == "ر ت" -> StudentField.ROLL
            any("هاتف", "تلفون", "tel", "telephone", "phone", "الوكيل", "tuteur", "parent") -> StudentField.PHONE1
            any("بالفرنسيه", "بالفرنسية", "francais", "latin") -> StudentField.NAME_FR
            any("بالعربيه", "بالعربية", "arabe") -> StudentField.NAME_AR
            any("الاسم", "اسم", "nom", "prenom", "name", "eleve", "التلميذ", "الطالب") -> StudentField.NAME_AR // يُحسم بالمحتوى
            else -> null
        }
    }

    /** خانة مقترحة من محتوى العمود فقط. */
    private fun fieldFromContent(values: List<String>): StudentField {
        val v = values.filter { it.isNotBlank() }
        if (v.isEmpty()) return StudentField.IGNORE
        val ar = v.count { hasArabic(it) }
        val la = v.count { hasLatin(it) && !hasArabic(it) }
        if (ar * 2 >= v.size) return StudentField.NAME_AR
        if (la * 2 >= v.size) return StudentField.NAME_FR
        val digits = v.map { it.filter(Char::isDigit) }
        if (digits.all { it.isNotEmpty() && it.length <= 3 }) return StudentField.ROLL
        if (digits.all { it.length == 10 }) return StudentField.NATIONAL_ID
        if (digits.all { it.length == 8 && it[0] in "234" }) return StudentField.PHONE1
        if (digits.all { it.isNotEmpty() }) return StudentField.RIM
        return StudentField.IGNORE
    }

    fun guess(rows: List<List<String>>): ImportGuess {
        val width = rows.maxOf { it.size }
        // سطر العناوين: أول سطر (من أول 10) فيه عنوانان مفهومان على الأقل
        val headerRow = rows.take(10).indexOfFirst { r -> r.count { fieldFromHeader(it) != null } >= 2 }
            .takeIf { it >= 0 }
        val dataRows = rows.drop((headerRow ?: -1) + 1)
        val columns = (0 until width).map { c ->
            ImportColumn(
                index = c,
                header = headerRow?.let { rows[it].getOrNull(c) }?.takeIf { it.isNotBlank() },
                samples = dataRows.map { it.getOrNull(c) ?: "" }.filter { it.isNotBlank() }.take(3)
            )
        }
        val mapping = columns.map { col ->
            val values = dataRows.take(30).map { it.getOrNull(col.index) ?: "" }
            val byHeader = col.header?.let { fieldFromHeader(it) }
            when {
                byHeader == StudentField.NAME_AR || byHeader == StudentField.NAME_FR -> {
                    // عنوان «الاسم/Nom»: اللغة تُحسم بمحتوى العمود
                    when (fieldFromContent(values)) {
                        StudentField.NAME_FR -> StudentField.NAME_FR
                        StudentField.NAME_AR -> StudentField.NAME_AR
                        else -> byHeader
                    }
                }
                byHeader != null -> byHeader
                else -> fieldFromContent(values)
            }
        }.toMutableList()
        // لا تتكرّر الخانة: الهاتف الثاني يصبح هاتف الوكيل 2، وأي تكرار آخر يُترك فارغًا
        val used = HashSet<StudentField>()
        for (i in mapping.indices) {
            var f = mapping[i]
            if (f == StudentField.PHONE1 && StudentField.PHONE1 in used) f = StudentField.PHONE2
            if (f != StudentField.IGNORE && f in used) f = StudentField.IGNORE
            mapping[i] = f
            used.add(f)
        }
        return ImportGuess(headerRow, columns, mapping)
    }

    fun preview(
        rows: List<List<String>>,
        headerRowIndex: Int?,
        mapping: List<StudentField>,
        classId: String,
        existing: List<StudentEntity>
    ): ImportPreview {
        fun col(f: StudentField) = mapping.indexOf(f).takeIf { it >= 0 }
        val cRoll = col(StudentField.ROLL); val cAr = col(StudentField.NAME_AR); val cFr = col(StudentField.NAME_FR)
        val cRim = col(StudentField.RIM); val cNat = col(StudentField.NATIONAL_ID)
        val cP1 = col(StudentField.PHONE1); val cP2 = col(StudentField.PHONE2)
        fun cell(r: List<String>, c: Int?) = c?.let { r.getOrNull(it) }?.trim() ?: ""

        val usedRolls = existing.map { it.rollNumber }.toHashSet()
        val existingRims = existing.map { it.rim }.filter { it.isNotBlank() }.toHashSet()
        val existingNames = existing.map { norm(it.nameAr) + "|" + norm(it.nameFr) }.toHashSet()
        var nextRoll = (existing.maxOfOrNull { it.rollNumber } ?: 0) + 1
        var renumbered = 0; var dup = 0; var empty = 0
        val now = System.currentTimeMillis()
        val out = ArrayList<StudentEntity>()

        for (r in rows.drop((headerRowIndex ?: -1) + 1)) {
            val ar = cell(r, cAr); val fr = cell(r, cFr)
            if (ar.isBlank() && fr.isBlank()) { empty++; continue }
            val rim = cell(r, cRim)
            val nameKey = norm(ar) + "|" + norm(fr)
            if ((rim.isNotBlank() && rim in existingRims) || nameKey in existingNames) { dup++; continue }
            var roll = cell(r, cRoll).filter(Char::isDigit).toIntOrNull()
            if (roll == null || roll <= 0 || roll in usedRolls) {
                while (nextRoll in usedRolls) nextRoll++
                roll = nextRoll
                renumbered++
            }
            usedRolls.add(roll)
            if (rim.isNotBlank()) existingRims.add(rim)
            existingNames.add(nameKey)
            out.add(
                StudentEntity(
                    id = UUID.randomUUID().toString(),
                    classId = classId,
                    rollNumber = roll,
                    nameAr = ar,
                    nameFr = fr,
                    rim = rim,
                    nationalId = cell(r, cNat),
                    guardianPhone1 = cell(r, cP1),
                    guardianPhone2 = cell(r, cP2),
                    createdAt = now
                )
            )
        }
        return ImportPreview(out.sortedBy { it.rollNumber }, renumbered, dup, empty)
    }
}
