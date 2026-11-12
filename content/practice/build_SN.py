import importlib, sys
from gen_common import stations, draw_fill, exam_props, save
def T(t, x, s): return {"title": t, "text": x, "solution": s}

def build(level):
    B = importlib.import_module(f"bank_SN_{level}").B
    bank = {k: [T(*it) for it in v] for k, v in B.items()}
    def items_for(lessons):
        out = []
        for l in lessons:
            for k, v in bank.items():
                if l.startswith(k) or k.lower() in l.lower():
                    for it in v:
                        if it not in out: out.append(it)
        return out
    series, exams, used, seen = [], [], set(), []
    for st in stations("SN", level):
        seen += st["lessons"]; items = items_for(st["lessons"]); assert items, st
        if st["kind"] in ("eval", "final"):
            its = draw_fill(items, items_for(seen), 20, sum(map(ord, st["week"])), set())   # chaque série complète (reprises possibles entre séries)
            series.append({"week": st["week"], "term": st["term"], "kind": st["kind"],
                           "title": "Révision générale de l'année" if st["kind"] == "final" else "Série d'intégration et de remédiation",
                           "covers": st["lessons"][:8], "items": its})
        else:
            o = {1: "1er", 2: "2e", 3: "3e"}[st["term"]]; t3 = max(1, len(items) // 3)
            exams.append({"week": st["week"], "term": st["term"], "kind": "exam", "title": f"Examen du {o} trimestre", "covers": st["lessons"][:8],
                          "items": exam_props([items[:t3], items[t3:2*t3], items[2*t3:]], 20, 900 + st["term"], f"Proposition {{}} — Examen du {o} trimestre")})
    for s in series: print(s["week"], len(s["items"]))
    save("SN", level, "fr", [s for s in series], exams) if all(len(s["items"]) == 20 for s in series) else print("!! séries incomplètes")

build(sys.argv[1])
