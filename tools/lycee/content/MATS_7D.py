# Mathématiques 7e D — IPN. Plusieurs chapitres sont communs avec la 7e C : on reprend les leçons de MATS_7C
# (mêmes notions, mêmes exemples) avec les pages du manuel de 7e D, et on écrit les leçons propres au 7e D.
import copy, importlib.util, os

BOOK = "MATS_7D"

_spec = importlib.util.spec_from_file_location("MATS_7C_src", os.path.join(os.path.dirname(os.path.abspath(__file__)), "MATS_7C.py"))
_c = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_c)
S, L = _c.S, _c.L


def R(key, title, pages, add=None, drop=(), **over):
    src = next(l for l in _c.LESSONS if key in l["title"])
    l = copy.deepcopy(src)
    l["title"] = title
    l["match"] = [title.split(" : ", 1)[-1]]
    l["pages"] = pages
    l["board"] = [s for s in l["board"] if not any(d in s["h"] for d in drop)] + (add or [])
    for k, v in over.items():
        if k == "app":
            l["application"] = {"q": v[0], "a": v[1]}
        else:
            l[k] = v
    return l


LESSONS = [
R("forme algébrique, module", "Chapitre 1 : Nombres complexes — forme algébrique, module, argument, équations", [9, 15]),
L("Chapitre 1 : Nombres complexes — forme exponentielle et géométrie", [16, 22],
  ["Utiliser la notation exponentielle re^(iθ).", "Traduire alignement, parallélisme, orthogonalité et natures de triangles avec les affixes.", "Déterminer des ensembles de points."],
  "Comment prouver avec les complexes qu’un triangle est rectangle isocèle ?",
  "On note e^(iθ) = cos θ + i sin θ ; tout complexe non nul s’écrit z = re^(iθ) avec r = |z| et θ = arg z ; e^(iθ)e^(iθ’) = e^(i(θ+θ’)), 1/e^(iθ) = e^(−iθ). Configurations (A, B, C, D distincts d’affixes zA, zB, zC, zD) : (zA − zB)/(zA − zC) réel ⇔ A, B, C alignés ; (zA − zB)/(zD − zC) réel ⇔ (AB) ∥ (CD) ; imaginaire pur ⇔ (AB) ⊥ (CD) ; (zA − zB)/(zA − zC) = ki (k ≠ 0) ⇔ ABC rectangle en A ; = ±i ⇔ rectangle isocèle en A ; de module 1 ⇔ isocèle en A ; = e^(±iπ/3) ⇔ équilatéral. Pour Z = (z − b)/(z − a) : |Z| = MB/MA et arg Z = (MA⃗ ; MB⃗) [2π]. |Z| = 1 ⇔ M sur la médiatrice de [AB] ; Z réel ⇔ M sur (AB) ; Z imaginaire pur ⇔ M sur le cercle de diamètre [AB].",
  ["z = re^(iθ).", "Quotient réel : alignement ou parallélisme.", "Quotient imaginaire pur : orthogonalité.", "= ±i : triangle rectangle isocèle.", "|Z| = MB/MA ; arg Z = (MA⃗ ; MB⃗)."],
  ("A(2 + i), B(2i) et Z = (z − 2i)/(z − 2 − i). Ensemble des M tels que |Z| = 1 ?", "MB = MA : la médiatrice de [AB]."),
  [S("Configurations", table=[["Condition sur (zA − zB)/(zA − zC)", "Interprétation"], ["réel", "A, B, C alignés"], ["ki, k ≠ 0", "ABC rectangle en A"], ["±i", "rectangle isocèle en A"], ["module 1", "isocèle en A"], ["e^(±iπ/3)", "équilatéral"]]),
   S("Exemple du manuel", ["Z réel ⇔ (MA⃗ ; MB⃗) = 0 [π] ou M = B : la droite (AB) privée de A.", "Z imaginaire pur ⇔ (MA⃗ ; MB⃗) = π/2 [π] ou M = B : le cercle de diamètre [AB] privé de A."])],
  apps=[("Forme exponentielle de 1 − i ?", "√2 e^(−iπ/4)."), ("Que signifie (zA − zB)/(zA − zC) = i ?", "AB = AC et (AC⃗ ; AB⃗) = π/2 : ABC rectangle isocèle en A.")]),
R("Continuité, dérivabilité", "Chapitre 2 : Limites, continuité et dérivation", [23, 35],
  add=[S("Inégalité des accroissements finis (exemple du manuel)", ["Si m ≤ f’ ≤ M sur [a ; b], alors m(b − a) ≤ f(b) − f(a) ≤ M(b − a).", "f = sin sur [0 ; π/6] : √3/2 ≤ cos x ≤ 1 ⇒ (√3/2)x ≤ sin x ≤ x."]),
       S("Point d’inflexion (exemple)", ["f(x) = x⁴ − 6x² + 1 : f’’(x) = 12x² − 12 s’annule en ±1 en changeant de signe ⇒ I(1 ; −4) et J(−1 ; −4)."])]),
R("Généralités sur les fonctions", "Chapitre 3 : Étude des fonctions", [36, 47],
  add=[S("Plan d’étude d’une fonction", ["Ensemble de définition, ensemble d’étude (parité, périodicité), limites aux bornes, continuité, dérivabilité, sens de variation, tableau de variations, branches infinies et tangentes, représentation graphique."]),
       S("Exemple du manuel", ["f(x) = x − 1 + 4/(x − 2) : asymptotes x = 2 et y = x − 1 ; f’(x) = x(x − 4)/(x − 2)².", "Maximum local f(0) = −3 ; minimum local f(4) = 5."])]),
R("logarithme népérien et exponentielle", "Chapitre 4 : Fonctions logarithme népérien et exponentielle", [48, 58]),
L("Chapitre 5 : Suites numériques — généralités, suites arithmétiques et géométriques", [59, 62],
  ["Définir une suite (formule explicite ou récurrence) ; raisonner par récurrence.", "Étudier le sens de variation ; reconnaître une suite bornée.", "Utiliser les suites arithmétiques et géométriques et leurs sommes."],
  "On place de l’argent à intérêts composés : comment prévoir le capital dans 10 ans ?",
  "Une suite numérique est une application de ℕ (ou d’une partie de ℕ) dans ℝ ; elle est définie par une formule explicite Un = f(n) ou par une relation de récurrence U0 donné, Un+1 = f(Un). Raisonnement par récurrence : si P(0) est vraie et si P(p) ⇒ P(p + 1), alors P(n) est vraie pour tout n. (Un) est majorée s’il existe M tel que Un ≤ M, minorée s’il existe m tel que Un ≥ m, bornée si elle est les deux. Sens de variation : étudier le signe de Un+1 − Un, ou comparer Un+1/Un à 1 (termes positifs), ou étudier f si Un = f(n). Suite arithmétique de raison r : Un+1 = Un + r, Un = U0 + nr, U0 + … + Un−1 = n(U0 + Un−1)/2. Suite géométrique de raison q : Un+1 = qUn, Un = U0qⁿ, U0 + … + Un−1 = U0(1 − qⁿ)/(1 − q) (q ≠ 1). En particulier 1 + 2 + … + n = n(n + 1)/2.",
  ["Explicite ou par récurrence.", "Récurrence : initialisation + hérédité.", "Monotonie : signe de Un+1 − Un.", "Arithmétique : Un = U0 + nr.", "Géométrique : Un = U0qⁿ ; somme U0(1 − qⁿ)/(1 − q)."],
  ("U arithmétique, U0 = 2, r = 6 : U29 et S30 = U0 + … + U29 ?", "U29 = 2 + 29 × 6 = 176 ; S30 = 30 × (2 + 176)/2 = 2 670."),
  [S("Arithmétique / géométrique", table=[["", "Arithmétique", "Géométrique"], ["Récurrence", "Un+1 = Un + r", "Un+1 = qUn"], ["Explicite", "Un = U0 + nr", "Un = U0qⁿ"], ["Somme de n termes", "n(U0 + Un−1)/2", "U0(1 − qⁿ)/(1 − q)"]]),
   S("Exemple : suite bornée", ["Un = sin n / n² (n ≥ 1) : |Un| ≤ 1/n² ≤ 1 ⇒ −1 ≤ Un ≤ 1."])],
  apps=[("V géométrique de raison −3 avec V4 = 81 : V0 ?", "V0 = 81/(−3)⁴ = 1."), ("U0 = 1, Un+1 = 1 + Un² : U1 et U2 ?", "U1 = 2 ; U2 = 5.")]),
L("Chapitre 5 : Suites numériques — limites, convergence, suites adjacentes", [63, 76],
  ["Calculer la limite d’une suite (opérations, comparaison, fonction associée).", "Utiliser le théorème de la convergence monotone.", "Étudier une suite récurrente Un+1 = g(Un) ; reconnaître des suites adjacentes."],
  "La suite définie par V0 = 2 et Vn+1 = 2 + 1/Vn converge-t-elle ? Vers quoi ?",
  "Une suite est convergente si elle a une limite finie (unique) ; sinon elle diverge. Si Un = f(n) et f a une limite en +∞, la suite a la même limite (la réciproque est fausse : sin(πn) = 0). Les théorèmes sur les opérations s’appliquent comme pour les fonctions. Limites usuelles : nᵅ → +∞ (α > 0) ; aⁿ → +∞ si a > 1, → 0 si 0 < a < 1 ; ln n / n → 0 ; aⁿ/nᵅ → +∞ (a > 1). Comparaison et gendarmes : si un ≤ Un ≤ vn et un, vn → l, alors Un → l. Si (Un) → a et f continue en a, alors f(Un) → f(a). Toute suite croissante majorée (ou décroissante minorée) converge. Pour Un+1 = g(Un) avec g continue, si (Un) converge, sa limite l vérifie g(l) = l ; si cette équation n’a pas de solution, la suite diverge. Deux suites sont adjacentes si l’une croît, l’autre décroît et leur différence tend vers 0 : elles convergent vers la même limite l, avec Un ≤ l ≤ Vn.",
  ["Convergente : limite finie.", "Croissante majorée ⇒ convergente.", "Gendarmes.", "Un+1 = g(Un) : limite l avec g(l) = l.", "Adjacentes : même limite."],
  ("Limite de Un = (2n + 3)/(n + 1) ?", "Un − 2 = 1/(n + 1) → 0 : Un → 2."),
  [S("Exemples du manuel", ["Un = 1/n! : 0 < 1/n! ≤ 1/n ⇒ Un → 0.", "Wn = (2n² + n − 3)/(5n + 4) → +∞ (comme 2n/5).", "Un = (3ⁿ − 2ⁿ)/(3ⁿ + 2ⁿ) = (1 − (2/3)ⁿ)/(1 + (2/3)ⁿ) → 1.", "V0 = 2, Vn+1 = 2 + 1/Vn : Vn ≥ 2 ; g(x) = x ⇔ x² − 2x − 1 = 0 ⇒ limite 1 + √2.", "Un+1 = Un + 1/Un : g(x) = x n’a pas de solution ⇒ (Un) diverge."]),
   S("Suites adjacentes (exemple)", ["Un+1 = (Un + 2Vn)/3, Vn+1 = (Un + 4Vn)/5, U0 = 1, V0 = 2.", "Wn = Vn − Un est géométrique de raison 2/15 → 0 ; (Un) croît, (Vn) décroît : suites adjacentes.", "Tn = 3Un + 10Vn est constante (= 23) ⇒ limite commune l = 23/13."])],
  apps=[("Limite de (0,5)ⁿ ?", "0."), ("Une suite croissante et majorée par 5 converge-t-elle ?", "Oui (convergence monotone), vers une limite ≤ 5.")]),
R("Chapitre 7 : Primitives et intégrale", "Chapitre 6 : Primitives et intégrale", [77, 82]),
R("Techniques d’intégration", "Chapitre 6 : Techniques d’intégration et calcul d’aires", [83, 89],
  objectives=["Calculer une intégrale par changement de variable affine et par parties.", "Calculer l’aire entre deux courbes."],
  summary="Si F est une primitive de f, x ↦ F(λx + μ)/λ est une primitive de x ↦ f(λx + μ) (λ ≠ 0). Intégration par parties : ∫ₐᵇ f(t)g’(t)dt = [f(t)g(t)]ₐᵇ − ∫ₐᵇ f’(t)g(t)dt (f, g dérivables à dérivées continues). Aire entre deux courbes : si f ≤ g sur [a ; b], l’aire du domaine limité par les deux courbes et les droites x = a, x = b est ∫ₐᵇ (g − f)(x)dx (en unités d’aire).",
  points=["Changement de variable affine.", "Par parties : ∫fg’ = [fg] − ∫f’g.", "Aire entre courbes : ∫(g − f)."],
  board=[S("Exemples", ["J = ∫₁² x ln x dx = [x²/2 · ln x]₁² − ∫₁² x/2 dx = 2 ln 2 − 3/4.", "Aire entre y = x² − 2x + 2 et y = −x² + 6 sur [−1 ; 2] : ∫₋₁² (−2x² + 2x + 4)dx = 9 u.a."])],
  applications=[{"q": "∫₀¹ xeˣ dx ?", "a": "Par parties : [xeˣ]₀¹ − ∫₀¹ eˣ dx = e − (e − 1) = 1."}, {"q": "Aire entre y = x et y = x² sur [0 ; 1] ?", "a": "∫₀¹ (x − x²)dx = 1/2 − 1/3 = 1/6."}]),
R("Équations différentielles", "Chapitre 7 : Équations différentielles", [90, 97]),
R("Dénombrement, probabilités", "Chapitre 8 : Dénombrement, probabilités et loi binomiale", [98, 105]),
R("Lois continues", "Chapitre 8 : Lois continues, fluctuation et estimation", [106, 122]),
]
