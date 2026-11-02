package com.elyedali.daftarustadh

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.elyedali.daftarustadh.data.CurriculumRepository
import com.elyedali.daftarustadh.data.GradeEntity
import com.elyedali.daftarustadh.data.GradeSlot
import com.elyedali.daftarustadh.data.StudentEntity
import com.elyedali.daftarustadh.pdf.TeacherRecordPdf
import com.elyedali.daftarustadh.transfer.DsqPdf
import com.elyedali.daftarustadh.transfer.RecordFile
import com.elyedali.daftarustadh.transfer.TransferCodec
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

/** أمثلة سجل نتائج الأستاذ كما يرسله للإدارة بعد الفصل الأول ثم الثاني ثم الثالث (نفس كود التطبيق). */
@RunWith(AndroidJUnit4::class)
class RecordSamples {
    @Test
    fun generate() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val year = CurriculumRepository.availableYears(ctx).last()
        val subject = CurriculumRepository.level(ctx, year, "1AS")!!.subjects.first { it.code == "MATS" }
        val names = listOf(
            "فاطمة محمد سالم" to "Fatimetou Mohamed Salem", "أحمد عبد الله" to "Ahmed Abdallahi",
            "مريم الشيخ" to "Mariem Cheikh", "محمد الأمين سيدي" to "Mohamed Lemine Sidi",
            "خديجة أحمد" to "Khadijetou Ahmed", "سيدي محمد" to "Sidi Mohamed"
        )
        val students = names.mapIndexed { i, (ar, fr) -> StudentEntity("s$i", "c1", i + 1, ar, fr, "", "", "", "", 0L) }
        val rnd = Random(11)
        val all = students.flatMap { s -> GradeSlot.ALL.map { slot -> GradeEntity(s.id, "c1", "MATS", slot, rnd.nextInt(16, 76) / 4.0, 0L) } }
        val seal = BitmapFactory.decodeResource(ctx.resources, R.drawable.img_mauritania_seal)
        val out = File(ctx.filesDir, "samples").apply { mkdirs() }
        val labels = TeacherRecordPdf.Labels(
            title = "سجل نتائج مادة: ${subject.nameAr} — Relevé de notes : ${subject.nameFr}",
            subtitle = "القسم: ⁦1AS1⁩ — السنة الدراسية: ⁦$year⁩ — الضارب: ${subject.coefficient}" +
                " — الولاية: انواكشوط 2 (الغربية) — المؤسسة: إعدادية البنات",
            schoolLine = "Classe : 1AS1 — Année scolaire : $year — Coef. ${subject.coefficient} — Wilaya : Nouakchott 2 (Ouest) — Établissement : إعدادية البنات",
            rollHeader = "ر.ن\nN°", nameHeader = "الاسم الكامل\nNom et Prénom",
            gradeHeaders = (1..3).flatMap { t -> listOf("اختبار ف$t\nDevoir T$t", "امتحان ف$t\nCompo T$t") },
            signature = "توقيع الأستاذ — Signature du professeur",
            stampLines = listOf("مختار", "أستاذ مادة: ${subject.nameAr}", "Professeur de : ${subject.nameFr}"),
            page = { p, n -> "صفحة $p من $n — Page $p / $n" },
            seal = seal
        )
        listOf(1, 2, 3).forEach { term ->
            // ما يرسله الأستاذ بعد اكتمال هذا الفصل: نقاط الفصول حتى هذا الفصل
            // الفصلان الأول والثاني: الامتحانات فقط؛ الفصل الأخير: الاختبارات والامتحانات كلها
            val slots = if (term < 3) GradeSlot.ALL.take(term * 2).filter { it.startsWith("E") }.toSet() else GradeSlot.ALL.toSet()
            val g = all.filter { it.slot in slots }
            val record = RecordFile(
                institutionId = "i1", wilaya = "انواكشوط 2 (الغربية)", schoolName = "إعدادية البنات", schoolListId = null,
                classId = "c1", schoolYear = year, levelCode = "1AS", number = 1, subjectCode = "MATS",
                subjectNameAr = subject.nameAr, subjectNameFr = subject.nameFr, teacherName = "مختار", testsMode = "average",
                students = TransferCodec.buildRecordStudents(students, g)
            )
            val byStudent = g.groupBy { it.studentId }.mapValues { e -> e.value.associate { it.slot to it.value } }
            val pdf = DsqPdf.wrap(ctx, TransferCodec.encodeRecord(record), TransferCodec.recordFileName(record).removeSuffix(".dsq")) { doc, no ->
                TeacherRecordPdf.drawInto(doc, no, students, labels, byStudent)
            }
            File(out, "record-T$term.pdf").writeBytes(pdf)
        }
    }
}
