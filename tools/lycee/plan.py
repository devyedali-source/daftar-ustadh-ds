#!/usr/bin/env python3
"""تخطيط الثانوية السنوي: لا يوجد تخطيط رسمي بالأسابيع، فتُعرض عناوين دروس الكتاب المدرسي بترتيبها مقسمة على الفصول الثلاثة (بلا أسابيع).

- الرزنامة نفسها في كل المواد (كما في الإعدادية): أسبوع تشخيصي، دروس، اختبار وعطلة في نهاية الفصلين الأول والثاني،
  مراجعة عامة ثم اختبار في نهاية الفصل الثالث.
- عدد أسابيع كل درس يتناسب مع عدد صفحاته في الكتاب (درس واحد على الأقل)، ودرس طويل يمتد على أسابيع متتالية.
- مادة بكتابين في الشعبة (الفيزياء/الكيمياء، التاريخ/الجغرافيا): يُوزَّع كل كتاب على الأسابيع نفسها، فيكون في الأسبوع درس من كل كتاب.
- الكتاب المشترك بين شعب: نفس التوزيع لكل شعبة (كل شعبة تُتمّ كتابها في السنة؛ الساعات تحدد عدد الحصص لا ترتيب الدروس).
الاستعمال: python3 tools/lycee/plan.py   (يقرأ كل ملفات content ويكتب planning.json)
"""
import json, os
from build import BOOKS, load, HERE, ROOT

PLANNING = os.path.join(ROOT, "app", "src", "teacher", "assets", "planning", "planning.json")

MONTHS_FR = ["Octobre", "Novembre", "Décembre", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin"]
# (شهر، أسبوع، فصل، نوع)
CAL = []
for mi, m in enumerate(MONTHS_FR):
    for w in range(1, 5):
        CAL.append((m, w))
CAL = CAL[:33]                      # من Octobre S1 إلى Juin S1
KIND = {}
for i, (m, w) in enumerate(CAL):
    term = 1 if m in ("Octobre", "Novembre", "Décembre") else 2 if m in ("Janvier", "Février", "Mars") else 3
    k = "lesson"
    if (m, w) == ("Octobre", 1): k = "diag"
    elif (m, w) in (("Décembre", 3), ("Mars", 3), ("Juin", 1)): k = "exam"
    elif (m, w) in (("Décembre", 4), ("Mars", 4)): k = "holiday"
    elif (m, w) == ("Mai", 4): k = "eval"
    KIND[i] = (term, k)

AR_SUBJECTS = {"AR", "IR", "IC", "HG", "PH", "LEG"}
TXT = {
    "fr": {"diag": ["Prise de contact", "Évaluation diagnostique"], "holiday": ["Vacances"], "eval": ["Révision générale"],
           "exam": {1: ["Évaluations du 1er trimestre"], 2: ["Évaluations du 2e trimestre"], 3: ["Évaluations du 3e trimestre"]}},
    "ar": {"diag": ["استقبال الطلاب", "التقويم التشخيصي"], "holiday": ["عطلة"], "eval": ["مراجعة عامة"],
           "exam": {1: ["اختبارات الفصل الأول"], 2: ["اختبارات الفصل الثاني"], 3: ["اختبارات الفصل الثالث"]}},
}


def lang_of(subject, lessons):
    # مادة عربية أو درس بعنوان عربي (مثل الفيزياء للشعب الأصلية)
    if subject in AR_SUBJECTS:
        return "ar"
    t = lessons[0]["title"] if lessons else ""
    return "ar" if any("؀" <= c <= "ۿ" for c in t) else "fr"


def label(i, lang):
    m, w = CAL[i]
    return f"الأسبوع {i + 1}" if lang == "ar" else f"{m} S{w}"


def distribute(lessons, weeks):
    """يوزّع الدروس (بترتيبها) على قائمة أسابيع: يرجع لكل أسبوع قائمة عناوين التخطيط."""
    n, W = len(lessons), len(weeks)
    out = [[] for _ in range(W)]
    if n == 0:
        return out
    if n >= W:
        # دروس أكثر من الأسابيع: عدة دروس في الأسبوع بالتساوي
        for j, l in enumerate(lessons):
            out[min(W - 1, j * W // n)].append(l["match"][0])
        return out
    w8 = [max(1, l["pages"][1] - l["pages"][0] + 1) for l in lessons]
    tot = sum(w8)
    alloc = [1] * n
    extra = W - n
    share = [extra * x / tot for x in w8]
    base = [int(s) for s in share]
    alloc = [a + b for a, b in zip(alloc, base)]
    rest = W - sum(alloc)
    for j in sorted(range(n), key=lambda j: share[j] - base[j], reverse=True)[:rest]:
        alloc[j] += 1
    i = 0
    for l, a in zip(lessons, alloc):
        for _ in range(a):
            out[i].append(l["match"][0]); i += 1
    return out


def unit_of(lesson):
    """عنوان الوحدة/الفصل من بادئة عنوان الدرس («Unité I : …»)، إن وُجدت."""
    t = lesson["title"]
    return t.split(" : ")[0].strip() if " : " in t else ""


def rows_for(books_lessons, lang, book_titles):
    """الثانوية: عناوين الدروس مقسمة على الفصول الثلاثة (بلا أسابيع).
    يُحدَّد فصل كل درس بتوزيعه على أسابيع السنة (حسب عدد صفحاته)، ثم تُجمع عناوين كل فصل بترتيب الكتاب."""
    lesson_weeks = [i for i in range(len(CAL)) if KIND[i][1] == "lesson"]
    terms = {t: [] for t in (1, 2, 3)}
    two = len(books_lessons) > 1
    for lessons, btitle in zip(books_lessons, book_titles):
        d = distribute(lessons, lesson_weeks)
        week_of = {}
        for j, items in enumerate(d):
            for it in items:
                week_of.setdefault(it, lesson_weeks[j])
        for l in lessons:
            t = KIND[week_of.get(l["match"][0], lesson_weeks[-1])][0]
            u = unit_of(l) or (btitle if two else "")
            rows = terms[t]
            if rows and rows[-1]["k"] == "lesson" and rows[-1]["u"] == u and rows[-1]["b"] == btitle:
                if l["match"][0] not in rows[-1]["i"]:
                    rows[-1]["i"].append(l["match"][0])
            else:
                rows.append({"l": "", "w": 0, "t": t, "u": u, "k": "lesson", "i": [l["match"][0]], "b": btitle})
            # سلسلة تمارين الفصل بعد آخر دروسه (مربوطة بآخر درس في الفصل)
            if l.get("_series"):
                rows.append({"l": "", "w": 0, "t": t, "u": u, "k": "series", "i": [l["match"][0]], "b": btitle})
    out = [{"l": "", "w": 0, "t": 1, "u": "", "k": "diag", "i": TXT[lang]["diag"]}]
    for t in (1, 2, 3):
        for r in terms[t]:
            r.pop("b")
            out.append(r)
        if t == 3:
            out.append({"l": "", "w": 0, "t": 3, "u": "", "k": "eval", "i": TXT[lang]["eval"]})
        out.append({"l": "", "w": 0, "t": t, "u": "", "k": "exam", "i": TXT[lang]["exam"][t]})
        if t < 3:
            out.append({"l": "", "w": 0, "t": t, "u": "", "k": "holiday", "i": TXT[lang]["holiday"]})
    return out


def main():
    plan = json.load(open(PLANNING, encoding="utf-8"))
    by_level = {}  # (subject, level) -> [(book, lessons)]
    for f in sorted(os.listdir(os.path.join(HERE, "content"))):
        if not f.endswith(".py"):
            continue
        m = load(f[:-3])
        b = BOOKS[m.BOOK]
        # الدروس التي تُختم بها سلسلة تمارين الفصل
        sp = os.path.join(HERE, "series", f)
        with_series = set()
        if os.path.exists(sp):
            ns = {}
            exec(open(sp, encoding="utf-8").read(), ns)
            with_series = set(ns["SERIES"])
        lessons = [dict(l, _series=True) if l["title"] in with_series else l for l in m.LESSONS]
        for lv in b["levels"]:
            by_level.setdefault((b["subject"], lv), []).append((b, lessons))
    for (subj, lv), items in sorted(by_level.items()):
        items.sort(key=lambda x: x[0]["asset"])
        lang = lang_of(subj, items[0][1])
        plan["plans"].setdefault(subj, {})[lv] = rows_for([les for _, les in items], lang, [b["title"] for b, _ in items])
        titles = " ; ".join(f"« {b['title']} »" for b, _ in items)
        plan["source"][f"{subj}_{lv}"] = (
            f"عناوين دروس الكتاب المدرسي {titles} (المعهد التربوي الوطني) مقسمة على الفصول الثلاثة، بترتيبها في الكتاب"
            if lang == "ar" else
            f"Titres des leçons du manuel {titles} (IPN), répartis sur les trois trimestres, dans l’ordre des chapitres")
        print(f"{subj}_{lv}: {sum(len(r['i']) for r in plan['plans'][subj][lv] if r['k'] == 'lesson')} عنوانًا")
    with open(PLANNING, "w", encoding="utf-8") as fh:
        json.dump(plan, fh, ensure_ascii=False, indent=1)
        fh.write("\n")


if __name__ == "__main__":
    main()
