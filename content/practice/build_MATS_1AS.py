"""يبني ملف سلاسل التمارين ومقترحات الامتحانات للرياضيات 1AS."""
import json, random
import tpl_math_college as t
from MATS_1AS_S1 import S1

def unique(pool, n, seed, used=None):
    """n تمرين مختلف من قائمة قوالب (بالتناوب على القوالب)."""
    used = used if used is not None else set(); out = []; r = random.Random(seed); i = 0
    while len(out) < n:
        f = pool[i % len(pool)]; i += 1
        for _ in range(30):
            x = f(random.Random(r.randint(0, 10**9)))
            if x["text"] not in used: used.add(x["text"]); out.append(x); break
    return out

GEO = [t.segments, t.droites, t.mediatrice, t.triangle_angles, t.triangle_nature, t.parallelogramme, t.cercle, t.pave, t.cube]
YEAR_NUM = [t.calc_entiers, t.priorites, t.division, t.divisibilite, t.dec_ecriture, t.ranger, t.arrondi, t.ops_dec,
            t.frac_simpl, t.frac_comp, t.frac_add, t.frac_qte, t.prop_table, t.pourcent, t.pourcent_exp, t.echelle, t.stats, t.stats_diag]
SIT = [t.sit_commerce, t.sit_jardin, t.sit_reservoir, t.sit_bassin, t.sit_budget]

S2 = {"week": "Mai S4", "term": 3, "kind": "eval", "title": "Révision sur les activités géométriques",
      "covers": ["Droites et segments", "Triangles et parallélogramme", "Cercle et disque", "Cube et pavé droit"],
      "items": unique(GEO, 18, 11) + unique([t.sit_bassin, t.sit_reservoir], 2, 12)}
used = set(x["text"] for x in S1["items"] + S2["items"])
S3 = {"week": "Juin S1", "term": 3, "kind": "eval", "title": "Révision générale (1)",
      "covers": ["Programme de l'année"], "items": unique(YEAR_NUM, 12, 21, used) + unique(GEO, 6, 22, used) + unique(SIT, 2, 23, used)}
S4 = {"week": "Juin S2", "term": 3, "kind": "eval", "title": "Révision générale (2)",
      "covers": ["Programme de l'année"], "items": unique(YEAR_NUM[::-1], 12, 31, used) + unique(GEO[::-1], 6, 32, used) + unique(SIT[::-1], 2, 33, used)}

TERM = {
 1: dict(week="Décembre S3", A=[t.calc_entiers, t.priorites, t.division, t.divisibilite], B=[t.dec_ecriture, t.ranger, t.arrondi, t.ops_dec],
         G=[t.segments, t.droites, t.mediatrice], S=[t.achat, t.deux_offres],
         covers=["Entiers naturels", "Droites et segments", "Décimaux positifs"]),
 2: dict(week="Mars S3", A=[t.frac_simpl, t.frac_comp, t.frac_add, t.frac_qte], B=[t.prop_table, t.pourcent, t.pourcent_exp, t.echelle],
         G=[t.triangle_angles, t.triangle_nature, t.parallelogramme, t.cercle], S=[t.sit_commerce, t.sit_jardin, t.sit_bassin, t.sit_budget],
         covers=["Fractions", "Triangles et parallélogramme", "Proportionnalité et pourcentage", "Cercle et disque"]),
 3: dict(week="Juin S3", A=[t.stats, t.stats_diag], B=YEAR_NUM[:16], G=[t.pave, t.cube, t.cercle, t.parallelogramme, t.triangle_angles],
         S=[t.sit_reservoir, t.sit_commerce, t.sit_budget, t.sit_jardin, t.sit_bassin],
         covers=["Statistique", "Cube et pavé droit", "Révision de l'année"]),
}
ORD = {1: "1er", 2: "2e", 3: "3e"}
exams = []
for term, c in TERM.items():
    props = []; seen = set()
    for k in range(20):
        r = random.Random(1000 * term + k)
        parts = [r.choice(c["A"])(random.Random(r.randint(0, 10**9))), r.choice(c["B"])(random.Random(r.randint(0, 10**9))),
                 r.choice(c["G"])(random.Random(r.randint(0, 10**9))), r.choice(c["S"])(random.Random(r.randint(0, 10**9)))]
        key = "|".join(p["text"] for p in parts)
        if key in seen: continue
        seen.add(key)
        props.append({"title": f"Proposition {k+1} — Examen du {ORD[term]} trimestre",
                      "text": "\n\n".join(f"Exercice {i+1} : {p['title']}\n{p['text']}" for i, p in enumerate(parts)),
                      "solution": "\n\n".join(f"Exercice {i+1}\n{p['solution']}" for i, p in enumerate(parts))})
    assert len(props) == 20
    exams.append({"week": c["week"], "term": term, "kind": "exam", "title": f"Examen du {ORD[term]} trimestre", "covers": c["covers"], "items": props})

for s in (S1, S2, S3, S4): assert len(s["items"]) == 20, s["week"]
out = {"subject": "MATS", "level": "1AS", "lang": "fr", "series": [S1, S2, S3, S4], "exams": exams}
json.dump(out, open("../../app/src/teacher/assets/practice/MATS_1AS.json", "w"), ensure_ascii=False, indent=1)
print("series", [len(s["items"]) for s in out["series"]], "exams", [len(e["items"]) for e in exams])
