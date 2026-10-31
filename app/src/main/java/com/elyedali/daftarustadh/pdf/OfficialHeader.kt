package com.elyedali.daftarustadh.pdf

/**
 * الرأسية الرسمية للوائح والكشوف (حسب كشف الدرجات الرسمي 2026): بالعربية يمينًا وبالفرنسية يسارًا.
 * لا يوجد سطر «مقاطعة» لأن التعليم الإعدادي والثانوي يتبع الولاية.
 */
object OfficialHeader {
    val AR = listOf(
        "الجمهورية الإسلامية الموريتانية",
        "شرف - إخاء - عدل",
        "وزارة التربية وإصلاح النظام التعليمي",
        "مديرية التعليم الثانوي",
        "مصلحة الشؤون المدرسية"
    )
    val FR = listOf(
        "République Islamique de Mauritanie",
        "Honneur - Fraternité - Justice",
        "Ministère de l'Éducation et de la Réforme du Système Éducatif",
        "Direction de l'Enseignement Secondaire",
        "Service des Affaires Scolaires"
    )
}
