# Mathématiques 5ème D — IPN. Le manuel 5D reprend, sous une forme allégée, les chapitres du manuel 5C (même programme).
# Les leçons reprennent le cours du manuel 5C pour les notions présentes dans le manuel 5D, avec les pages du manuel 5D ;
# les parties absentes du manuel 5D (coniques, Leibniz, équations avec paramètre, transformations, arithmétique, espace…) sont retirées.
import importlib.util as _u, os as _os, copy as _c
BOOK = "MATS_5D"
_s = _u.spec_from_file_location("_m5c", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "MATS_5C.py"))
_m = _u.module_from_spec(_s); _s.loader.exec_module(_m)

def K(src_title, title, pages, drop=(), keep_figs=False):
    src = _c.deepcopy(next(l for l in _m.LESSONS if l["title"].startswith(src_title)))
    board = []
    for b in src["board"]:
        if any(b["h"].startswith(d) for d in drop):
            continue
        if not keep_figs:
            b.pop("fig", None)
        board.append(b)
    src.update(title=title, match=[title.split(" : ", 1)[-1]], pages=pages, board=board)
    return src

LESSONS = [
K("Chapitre 1 :", "Chapitre 1 : Calcul dans ℝ", [9, 16]),
K("Chapitre 2 :", "Chapitre 2 : Polynômes", [17, 20]),
K("Chapitre 3 :", "Chapitre 3 : Calcul vectoriel", [21, 26]),
K("Chapitre 4 :", "Chapitre 4 : Géométrie analytique", [27, 34]),
K("Chapitre 5 :", "Chapitre 5 : Équations et systèmes", [35, 40], drop=("Équation avec paramètre",)),
K("Chapitre 5 (suite)", "Chapitre 5 (suite) : Inéquations", [41, 46]),
K("Chapitre 6 :", "Chapitre 6 : Barycentre", [47, 52], drop=("Fonction vectorielle de Leibniz",)),
K("Chapitre 7 :", "Chapitre 7 : Angles orientés — Trigonométrie", [53, 60]),
K("Chapitre 8 :", "Chapitre 8 : Généralités sur les fonctions", [61, 69]),
K("Chapitre 8 (suite)", "Chapitre 8 (suite) : Fonctions de référence", [70, 78]),
K("Chapitre 9 :", "Chapitre 9 : Produit scalaire", [79, 84], drop=("III. Théorème de la médiane",)),
K("Chapitre 13 :", "Chapitre 10 : Applications", [85, 88]),
K("Chapitre 14 :", "Chapitre 11 : Dénombrement", [89, 94]),
K("Chapitre 15 :", "Chapitre 12 : Statistiques", [95, 104]),
]

def _drop_items(l, key, word):
    l[key] = [x for x in l[key] if word not in x]

def _cut(l, key, start, end):
    s = l[key]; i = s.find(start)
    if i >= 0:
        j = s.find(end, i) if end else len(s)
        l[key] = (s[:i] + s[j:]).replace("  ", " ").strip()

_L = {l["title"][:12]: l for l in LESSONS}
_drop_items(_L["Chapitre 5 :"], "objectives", "paramètre")
_b = _L["Chapitre 6 :"]
_b["objectives"] = [x.replace(" et la fonction vectorielle de Leibniz", "").replace("l’homogénéité, l’associativité", "l’homogénéité et l’associativité") for x in _b["objectives"]]
_cut(_b, "summary", "Fonction de Leibniz", "Coordonnées")
_b["points"] = [x.replace("Leibniz : αMA→ + βMB→ + γMC→ = (α + β + γ)MG→ ; ", "Coordonnées : ") for x in _b["points"]]
_p = _L["Chapitre 9 :"]
_p["objectives"] = [x.replace("Appliquer le théorème de la médiane et les", "Appliquer les") for x in _p["objectives"]]
_cut(_p, "summary", "Théorème de la médiane", "Dans un triangle ABC")
for _l in LESSONS:
    for _a in [_l.get("application")] + _l.get("applications", []):
        if _a and ("Leibniz" in _a["q"] + _a["a"] or "médiane" in _a["q"] + _a["a"] and _l is _p):
            _l["applications"] = [x for x in _l.get("applications", []) if x is not _a]
