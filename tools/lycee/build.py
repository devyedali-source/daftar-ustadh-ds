#!/usr/bin/env python3
"""يبني ملفات دروس الثانوية من tools/lycee/content/<الكتاب>.py

كل ملف محتوى يعرّف BOOK (اسم الكتاب في الحزم، مثل MATS_5C) و LESSONS (قائمة دروس بصيغة ملفات الإعدادية).
يُكتب الملف lessons/<المادة>_<الشعبة>.json لكل شعبة يخدمها الكتاب (الكتاب المشترك = نفس الدروس).
الاستعمال: python3 tools/lycee/build.py [أسماء الكتب...]   (بلا وسائط: كل الكتب)
"""
import importlib.util, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
OUT = os.path.join(ROOT, "app", "src", "teacher", "assets", "lessons")
BOOKS = {os.path.basename(b["asset"])[:-4]: b for b in json.load(open(os.path.join(HERE, "books.json"), encoding="utf-8"))}


def load(name):
    spec = importlib.util.spec_from_file_location(name, os.path.join(HERE, "content", name + ".py"))
    m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m)
    return m


def check(book, lessons, pages):
    titles = set()
    for l in lessons:
        for k in ("title", "match", "pages", "objectives", "intro", "summary", "points", "application", "board"):
            assert k in l, f"{book}: {l.get('title')} بلا {k}"
        a, b = l["pages"]
        assert 1 <= a <= b <= pages, f"{book}: صفحات خاطئة {l['title']}"
        for s in l["board"]:
            f = s.get("fig")
            if f:
                r = f["rect"]
                assert 1 <= f["page"] <= pages and 0 <= r[0] < r[2] <= 1 and 0 <= r[1] < r[3] <= 1, f"{book}: رسم خاطئ {l['title']}"
        assert l["title"] not in titles, f"{book}: عنوان مكرر {l['title']}"
        titles.add(l["title"])


def load_series(name, lessons, pages):
    """سلسلة تمارين آخر الفصل: tools/lycee/series/<الكتاب>.py يعرّف SERIES = {عنوان الدرس: [تمارين]}.
    كل تمرين: k ("book" من الكتاب، "extra" على نمطه، "int" وضعية إدماجية)، src، q، a، و fig اختياري."""
    p = os.path.join(HERE, "series", name + ".py")
    if not os.path.exists(p):
        return lessons
    spec = importlib.util.spec_from_file_location("series_" + name, p)
    m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m)
    titles = {l["title"] for l in lessons}
    for t, items in m.SERIES.items():
        assert t in titles, f"{name}: سلسلة لدرس غير موجود {t}"
        for e in items:
            assert e.get("k") in ("book", "extra", "int") and e.get("q") and e.get("a"), f"{name}: تمرين ناقص في {t}"
            f = e.get("fig")
            if f:
                r = f["rect"]
                assert 1 <= f["page"] <= pages and 0 <= r[0] < r[2] <= 1 and 0 <= r[1] < r[3] <= 1, f"{name}: رسم خاطئ في سلسلة {t}"
    return [dict(l, series=m.SERIES[l["title"]]) if l["title"] in m.SERIES else l for l in lessons]


BOARD_HEADS = {
    "fr": ("Situation de départ", "Cours", "À retenir", "Exemples", "Exemple", "Réponse", "Exercice d’application", "Solution"),
    "ar": ("وضعية الانطلاق", "الدرس", "أستخلص", "أمثلة", "مثال", "الجواب", "تمرين تطبيقي", "الحل"),
    "en": ("Warm-up", "Lesson", "Key points", "Examples", "Example", "Answer", "Practice", "Answer"),
}


def board_lang(subject, lesson):
    if subject == "ANG":
        return "en"
    return "ar" if any("\u0600" <= c <= "\u06FF" for c in lesson["title"]) else "fr"


def sentences(text):
    """يقسم الخلاصة إلى جمل: سطر لكل فكرة على السبورة."""
    import re
    parts = re.split(r"(?<=[.!?؟])\s+(?=[A-ZÀ-ÖØ-Þ0-9«(\u0600-\u06FF])", text.strip())
    return [p.strip() for p in parts if p.strip()]


def full_board(subject, lesson):
    """سبورة الدرس كاملة ومنظمة من بدايته إلى نهايته:
    وضعية الانطلاق ← الدرس (فقرة لكل فكرة) ← أقسام السبورة الخاصة (طرائق، جداول، رسوم) ← أستخلص ← أمثلة محلولة ← تمرين تطبيقي بحله."""
    H = BOARD_HEADS[board_lang(subject, lesson)]
    out = []
    if lesson.get("intro"):
        out.append({"h": H[0], "lines": [lesson["intro"]]})
    lines = sentences(lesson.get("summary", ""))
    # سبورة الكتاب الغنية أصلًا (6 أسطر فأكثر) تُغني عن تكرار الخلاصة
    if lines and sum(len(s.get("lines", [])) for s in lesson["board"]) < 6:
        out.append({"h": H[1], "lines": lines})
    out += lesson["board"]
    if lesson.get("points"):
        out.append({"h": H[2], "lines": list(lesson["points"])})
    ex = [e for e in lesson.get("applications") or [] if e.get("q")]
    if ex:
        exl = []
        for i, e in enumerate(ex, 1):
            exl.append(f"{H[4]} {i} : {e['q']}")
            if e.get("a"):
                exl.append(f"→ {H[5]} : {e['a']}")
        out.append({"h": H[3], "lines": exl})
    app = lesson.get("application") or {}
    if app.get("q"):
        out.append({"h": H[6], "lines": [app["q"]] + ([f"→ {H[7]} : {app['a']}"] if app.get("a") else [])})
    return out


def build(name):
    m = load(name)
    b = BOOKS[m.BOOK]
    import subprocess
    pdf = os.path.join(ROOT, "bookpacks", "b_" + "_".join(m.BOOK.split("_")[:2]).lower(), "src", "main", "assets", b["asset"])
    pages = int([l for l in subprocess.run(["pdfinfo", pdf], capture_output=True, text=True).stdout.splitlines() if l.startswith("Pages")][0].split()[-1])
    check(m.BOOK, m.LESSONS, pages)
    m.LESSONS = load_series(name, m.LESSONS, pages)
    # السبورة الكاملة المنظمة (تُبنى من عناصر الدرس، وتبقى أقسام السبورة الخاصة في وسطها)
    m.LESSONS = [dict(l, board=full_board(b["subject"], l)) for l in m.LESSONS]
    data = {"subject": b["subject"], "book": b["asset"], "bookTitle": b["title"], "lessons": m.LESSONS}
    for lv in b["levels"]:
        p = os.path.join(OUT, f"{b['subject']}_{lv}.json")
        prev = json.load(open(p, encoding="utf-8")) if os.path.exists(p) else None
        d = dict(data, level=lv)
        if prev and prev.get("book") != b["asset"]:
            # مادة بكتابين في الشعبة (الفيزياء والكيمياء، التاريخ والجغرافيا): تُجمع الدروس مع كتاب كل درس
            keep = [dict(x, book=x.get("book") or prev["book"], bookTitle=x.get("bookTitle") or prev["bookTitle"])
                    for x in prev["lessons"] if (x.get("book") or prev["book"]) != b["asset"]]
            mine = [dict(x, book=b["asset"], bookTitle=b["title"]) for x in m.LESSONS]
            d = dict(data, level=lv, lessons=keep + mine)
        with open(p, "w", encoding="utf-8") as fh:
            json.dump(d, fh, ensure_ascii=False, indent=1)
        print(f"{m.BOOK} → lessons/{b['subject']}_{lv}.json ({len(d['lessons'])} درسًا)")


if __name__ == "__main__":
    names = sys.argv[1:] or sorted(f[:-3] for f in os.listdir(os.path.join(HERE, "content")) if f.endswith(".py"))
    for n in names:
        build(n)
