"""سلاسل ومقترحات الرياضيات 2AS و3AS."""
import json, random
import tpl_math_college2 as u
from build_MATS_1AS import unique

ORD = {1: "1er", 2: "2e", 3: "3e"}
def exams_for(TERM):
    out = []
    for term, c in TERM.items():
        props, seen, k = [], set(), 0
        while len(props) < 20:
            r = random.Random(7000 * term + k + sum(map(ord, c["week"]))); k += 1
            parts = [r.choice(p)(random.Random(r.randint(0, 10**9))) for p in (c["A"], c["B"], c["G"], c["S"])]
            key = "|".join(p["text"] for p in parts)
            if key in seen: continue
            seen.add(key); n = len(props) + 1
            props.append({"title": f"Proposition {n} — Examen du {ORD[term]} trimestre",
                          "text": "\n\n".join(f"Exercice {i+1} : {p['title']}\n{p['text']}" for i, p in enumerate(parts)),
                          "solution": "\n\n".join(f"Exercice {i+1}\n{p['solution']}" for i, p in enumerate(parts))})
        out.append({"week": c["week"], "term": term, "kind": "exam", "title": f"Examen du {ORD[term]} trimestre", "covers": c["covers"], "items": props})
    return out

def save(level, series, exams):
    for s in series: assert len(s["items"]) == 20
    json.dump({"subject": "MATS", "level": level, "lang": "fr", "series": series, "exams": exams},
              open(f"../../app/src/teacher/assets/practice/MATS_{level}.json", "w"), ensure_ascii=False, indent=1)
    print(level, [len(s["items"]) for s in series], [len(e["items"]) for e in exams])

# ———— 2AS ————
N2 = [u.relatifs_ops, u.relatifs_axe, u.rationnels, u.litteral, u.vitesse, u.stats2]
G2 = [u.angles2, u.polygones, u.projection, u.droites_cercles, u.symetrie_centrale, u.cylindre_prisme]
S2 = [u.sit_citerne, u.sit_voyage, u.sit_expression]
used = set()
series2 = [
 {"week": "Mai S3", "term": 3, "kind": "eval", "title": "Révision : activités numériques", "covers": ["Entiers relatifs", "Décimaux", "Nombres rationnels", "Calcul littéral", "Proportionnalité", "Statistique"],
  "items": unique(N2, 18, 201, used) + unique([u.sit_expression, u.sit_voyage], 2, 202, used)},
 {"week": "Mai S4", "term": 3, "kind": "eval", "title": "Révision : activités géométriques", "covers": ["Angles", "Polygones", "Projection orthogonale", "Droites et cercles", "Transformations", "Cylindre et prisme droit"],
  "items": unique(G2, 18, 203, used) + unique([u.sit_citerne], 2, 204, used)},
 {"week": "Juin S1", "term": 3, "kind": "eval", "title": "Révision générale (1)", "covers": ["Programme de l'année"],
  "items": unique(N2, 9, 205, used) + unique(G2, 8, 206, used) + unique(S2, 3, 207, used)},
 {"week": "Juin S2", "term": 3, "kind": "eval", "title": "Révision générale (2)", "covers": ["Programme de l'année"],
  "items": unique(N2[::-1], 9, 208, used) + unique(G2[::-1], 8, 209, used) + unique(S2[::-1], 3, 210, used)},
]
T2 = {
 1: dict(week="Décembre S3", A=[u.relatifs_ops, u.relatifs_axe], B=[u.rationnels, u.litteral], G=[u.relatifs_axe, u.rationnels], S=[u.sit_expression],
         covers=["Entiers relatifs", "Décimaux", "Nombres rationnels", "Calcul littéral", "Repérage sur un axe"]),
 2: dict(week="Mars S3", A=[u.angles2, u.polygones], B=[u.droites_cercles, u.projection], G=[u.symetrie_centrale], S=[u.litteral, u.relatifs_ops],
         covers=["Angles", "Polygones", "Projection orthogonale", "Droites et cercles", "Transformations"]),
 3: dict(week="Juin S3", A=[u.vitesse, u.stats2], B=N2[:4], G=[u.cylindre_prisme] + G2[:5], S=S2,
         covers=["Proportionnalité", "Statistique", "Cylindre et prisme droit", "Révision de l'année"]),
}
save("2AS", series2, exams_for(T2))

# ———— 3AS ————
N3 = [u.pgcd, u.premiers, u.racines, u.puissances, u.identites, u.equations, u.fct_lineaire, u.stats3]
G3 = [u.angle_inscrit, u.triangle_cercle, u.vecteurs, u.pythagore, u.trigo, u.translation, u.boule]
S3 = [u.sit_rampe, u.sit_forfait]
used = set()
series3 = [{"week": "Mai S4", "term": 3, "kind": "eval", "title": "Révision générale", "covers": ["Programme de l'année"],
            "items": unique(N3, 9, 301, used) + unique(G3, 8, 302, used) + unique(S3, 3, 303, used)}]
T3 = {
 1: dict(week="Décembre S3", A=[u.pgcd, u.premiers], B=[u.racines, u.puissances], G=[u.identites, u.equations, u.angle_inscrit], S=[u.equations, u.identites],
         covers=["Arithmétique", "Nombres réels", "Calcul littéral", "Équations et inéquations", "Angles"]),
 2: dict(week="Mars S3", A=[u.triangle_cercle, u.vecteurs], B=[u.pythagore], G=[u.trigo, u.translation], S=[u.sit_rampe],
         covers=["Triangle et cercle", "Vecteurs et repères", "Théorème de Pythagore", "Trigonométrie", "Transformations"]),
 3: dict(week="Juin S1", A=[u.fct_lineaire, u.stats3], B=N3[:6], G=[u.boule] + G3[:5], S=S3,
         covers=["Fonction linéaire", "Statistique", "Boule et sphère", "Révision de l'année"]),
}
save("3AS", series3, exams_for(T3))
