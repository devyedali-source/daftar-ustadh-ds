# Mathématiques 6ème D — IPN. Le programme de ces chapitres est commun avec la 6ème C :
# on reprend les leçons de MATS_6C correspondantes (mêmes notions) avec les pages du manuel de 6ème D.
import copy, importlib.util, os

BOOK = "MATS_6D"

_spec = importlib.util.spec_from_file_location("MATS_6C_src", os.path.join(os.path.dirname(os.path.abspath(__file__)), "MATS_6C.py"))
_c = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_c)


def R(key, title, pages, add=None, drop=()):
    src = next(l for l in _c.LESSONS if key in l["title"])
    l = copy.deepcopy(src)
    l["title"] = title
    l["match"] = [title.split(" : ", 1)[-1]]
    l["pages"] = pages
    board = []
    for s in l["board"]:
        if any(d in s["h"] for d in drop):
            continue
        s["h"] = s["h"].replace(" du manuel", "")
        board.append(s)
    l["board"] = board + (add or [])
    return l


def S(h, lines=None, table=None):
    d = {"h": h, "lines": lines or []}
    if table:
        d["table"] = table
    return d


LESSONS = [
R("Polynômes — degré", "Chapitre I : Polynômes — degré, égalité, division, racines", [9, 15]),
R("Fractions rationnelles et éléments simples", "Chapitre I : Fractions rationnelles et éléments simples", [15, 18]),
R("Équations et inéquations du premier et du second degré", "Chapitre II : Équations et inéquations du premier et du second degré", [19, 30]),
R("Systèmes linéaires", "Chapitre II : Systèmes linéaires (2 et 3 inconnues) et inéquations", [30, 38]),
R("Matrices, déterminants", "Chapitre II : Matrices, déterminants et méthode de Cramer", [38, 42]),
R("Exercices (équations, systèmes, matrices)", "Chapitre II : Exercices (équations, systèmes, matrices)", [43, 44]),
R("Suites — généralités", "Chapitre III : Suites — généralités, arithmétiques et géométriques", [45, 55]),
R("Raisonnement par récurrence et limites de suites", "Chapitre III : Limites de suites, récurrence et suites adjacentes", [55, 63],
  add=[S("Suites adjacentes", ["(un) croissante, (vn) décroissante et lim (vn − un) = 0 : les deux suites sont adjacentes et convergent vers la même limite."])]),
R("Généralités sur les fonctions et symétries", "Chapitre IV : Généralités sur les fonctions et symétries", [64, 68]),
R("Limites", "Chapitre IV : Limites", [68, 80]),
R("Continuité et théorème des valeurs intermédiaires", "Chapitre IV : Continuité et théorème des valeurs intermédiaires", [80, 86]),
R("Dérivabilité en un point", "Chapitre V : Dérivabilité en un point, tangente et demi-tangentes", [87, 93]),
R("Dérivées, variations et applications", "Chapitre V : Dérivées, variations et propriétés des fonctions dérivables", [93, 106]),
R("Étude et représentation de fonctions", "Chapitre VI : Étude et représentation de fonctions", [107, 130]),
R("Cercle trigonométrique et angles associés", "Chapitre VII : Cercle trigonométrique et angles associés", [131, 136]),
R("Angles orientés de vecteurs", "Chapitre VII : Angles orientés", [136, 140]),
R("Formules trigonométriques et équations", "Chapitre VII : Formules trigonométriques, équations et inéquations", [140, 146]),
R("Dénombrement", "Chapitre VIII : Dénombrement", [147, 160]),
R("Chapitre 15 : Probabilités", "Chapitre IX : Probabilités", [161, 168]),
R("Échantillonnage et intervalle de fluctuation", "Chapitre X : Statistique — échantillonnage", [169, 171],
  add=[S("Intervalle de confiance", ["Si la proportion p est inconnue, à partir de la fréquence f observée sur un échantillon de taille n, l’intervalle [f − 1/√n ; f + 1/√n] est un intervalle de confiance de p au niveau 95 %.", "Exemple : n = 100, f = 0,5 ⇒ [0,4 ; 0,6]."])]),
]
