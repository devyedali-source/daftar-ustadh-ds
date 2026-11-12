"""بناء سلاسل الوضعيات ومقترحات الامتحانات للمواد الأدبية من بنك مفاتيحه عناوين الدروس."""
import importlib, sys
from gen_common import stations, draw_fill, exam_props, save
def T(t, x, s): return {"title": t, "text": x, "solution": s}
AR_ORD = {1: "الأول", 2: "الثاني", 3: "الثالث"}

def build(subject, level, lang="ar", word="الوضعية"):
    B = importlib.import_module(f"bank_{subject}_{level}").B
    bank = {k: [T(*it) for it in v] for k, v in B.items()}
    def items_for(lessons):
        out = []
        for l in lessons:
            for k, v in bank.items():
                if l == k or l.startswith(k) or k in l:
                    for it in v:
                        if it not in out: out.append(it)
        return out
    series, exams, seen = [], [], []
    for st in stations(subject, level):
        seen += st["lessons"]; items = items_for(st["lessons"])
        assert items, (st["week"], st["lessons"])
        if st["kind"] in ("eval", "final"):
            its = draw_fill(items, items_for(seen), 20, sum(map(ord, st["week"])), set())
            series.append({"week": st["week"], "term": st["term"], "kind": st["kind"],
                           "title": "المراجعة العامة للسنة" if st["kind"] == "final" else "وضعيات الإدماج والتقويم والعلاج",
                           "covers": st["lessons"][:10], "items": its})
        else:
            t3 = max(1, len(items) // 3); o = AR_ORD[st["term"]]
            exams.append({"week": st["week"], "term": st["term"], "kind": "exam", "title": f"امتحان الفصل {o}", "covers": st["lessons"][:10],
                          "items": exam_props([items[:t3], items[t3:2*t3], items[2*t3:]], 20, 300 + st["term"], f"المقترح {{}} — امتحان الفصل {o}", word)})
    save(subject, level, lang, series, exams)
    import json; p = f"../../app/src/teacher/assets/practice/{subject}_{level}.json"
    d = json.load(open(p)); d["word"] = word; json.dump(d, open(p, "w"), ensure_ascii=False, indent=1)

if __name__ == "__main__":
    build(sys.argv[1], sys.argv[2], *(sys.argv[3:]))
