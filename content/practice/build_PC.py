import random
from gen_common import stations, draw, draw_fill, exam_props, save
import tpl_pc as P

def build(level, bank, nxt=None):
    series, exams = [], []; used = set(); seen_lessons = []
    for st in stations("PC", level):
        seen_lessons += st["lessons"]
        items = P.items_for(bank, st["lessons"])
        assert items, (level, st["week"], st["lessons"])
        title = {"eval": "Série d'exercices — " + st["label"], "final": "Révision générale de l'année"}.get(st["kind"])
        if st["kind"] in ("eval", "final"):
            seed = sum(map(ord, st["week"]))
            its = draw_fill(items, P.items_for(bank, seen_lessons), 20, seed, used)
            series.append({"week": st["week"], "term": st["term"], "kind": st["kind"], "title": title, "covers": st["lessons"][:8], "items": its})
        else:
            # trois exercices par sujet, pris dans des thèmes différents si possible
            third = max(1, len(items) // 3)
            pools = [items[:third] or items, items[third:2*third] or items, items[2*third:] or items]
            ordn = {1: "1er", 2: "2e", 3: "3e"}[st["term"]]
            exams.append({"week": st["week"], "term": st["term"], "kind": "exam", "title": f"Examen du {ordn} trimestre", "covers": st["lessons"][:8],
                          "items": exam_props(pools, 20, 500 + st["term"], f"Proposition {{}} — Examen du {ordn} trimestre")})
    save("PC", level, "fr", series, exams)

build("2AS", P.BANK_2AS)
build("3AS", P.BANK_3AS)
