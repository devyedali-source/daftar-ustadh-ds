# Série d'exercices de fin de chapitre — Mathématiques 7D.
# k = "book" : exercice du manuel (src = page et numéro) ; "extra" : exercice du même type ; "int" : situation d'intégration.
SERIES = {}

SERIES["Chapitre 1 : Nombres complexes — forme exponentielle et géométrie"] = [
 {"k": "book", "src": "p. 20, ex. 4", "q": "Résoudre dans ℂ : z² = −5 + 12i.",
  "a": "z = x + iy : x² − y² = −5, 2xy = 12, x² + y² = 13 ⇒ x² = 4, y² = 9 avec xy > 0 : z = 2 + 3i ou z = −2 − 3i."},
 {"k": "book", "src": "p. 20, ex. 5", "q": "P(z) = z³ + 9iz² + 2(6i − 11)z − 3(4i + 12) (Correction : le manuel écrit « − 3(4i − 12) » ; la solution du manuel utilise bien − 12i − 36).\n1) Montrer que P(z) = 0 a une solution réelle z₁. 2) Déterminer Q tel que P(z) = Q(z)(z − z₁). 3) Montrer que Q(z) = 0 a une solution imaginaire pure z₂. 4) Résoudre P(z) = 0. 5) Montrer que les points A, B, C d’affixes z₁, z₂, z₃ sont alignés.",
  "a": "1) P(z) = z³ − 22z − 36 + i(9z² + 12z − 12). Pour x réel : x³ − 22x − 36 = 0 et 3x² + 4x − 4 = 0 ; la seconde donne x = 2/3 ou −2, seul −2 vérifie la première : z₁ = −2.\n2) Q(z) = z² + (9i − 2)z − 6(i + 3).\n3) Δ = −5 − 12i = (2 − 3i)² : racines −3i et 2 − 6i ; z₂ = −3i.\n4) S = {−2 ; −3i ; 2 − 6i}.\n5) z₂ − z₁ = 2 − 3i et z₃ − z₁ = 4 − 6i = 2(z₂ − z₁) : AC⃗ = 2AB⃗, les points sont alignés."},
 {"k": "extra", "src": "", "q": "Écrire sous forme exponentielle z = 1 + i et w = √3 − i, puis calculer z × w et z/w sous forme exponentielle.",
  "a": "z = √2 e^(iπ/4), w = 2e^(−iπ/6). zw = 2√2 e^(iπ/12) ; z/w = (√2/2) e^(i5π/12)."},
 {"k": "extra", "src": "", "q": "Calculer (1 + i√3)⁶.",
  "a": "1 + i√3 = 2e^(iπ/3) : (1 + i√3)⁶ = 64e^(2iπ) = 64."},
 {"k": "extra", "src": "", "q": "A(1 + i), B(3 + 2i), C(2 + 4i). Calculer (c − a)/(b − a) et en déduire la nature de ABC.",
  "a": "(1 + 3i)/(2 + i) = (1 + 3i)(2 − i)/5 = (5 + 5i)/5 = 1 + i = √2 e^(iπ/4). Donc AC = √2 AB et (AB⃗ ; AC⃗) = π/4. Vérification : AB = |2 + i| = √5, BC = |−1 + 2i| = √5, AC = √10 et AB² + BC² = AC² : ABC est rectangle isocèle en B."},
 {"k": "extra", "src": "", "q": "Ensemble des points M d’affixe z tels que |z − 1 − i| = 2.",
  "a": "C’est le cercle de centre Ω(1 + i) et de rayon 2."},
 {"k": "int", "src": "", "q": "Sur un plan (repère complexe, unité 100 m), trois antennes sont en A(0), B(4) et C(2 + 2i√3).\n1) Montrer que ABC est équilatéral.\n2) On veut un relais au point Ω à égale distance des trois antennes. Donner son affixe et la distance aux antennes.",
  "a": "1) AB = 4, AC = |2 + 2i√3| = 4, BC = |−2 + 2i√3| = 4.\n2) Ω est le centre de gravité : ω = (0 + 4 + 2 + 2i√3)/3 = 2 + (2√3/3)i. Distance |ω| = √(4 + 4/3) = 4/√3 ≈ 2,31, soit environ 231 m."},
]

SERIES["Chapitre 2 : Limites, continuité et dérivation"] = [
 {"k": "book", "src": "p. 31, ex. 7", "q": "f(x) = tan x sur ]−π/2 ; π/2[. a) Montrer que f est une bijection de ]−π/2 ; π/2[ sur ℝ. b) Expliciter la dérivée de f⁻¹.",
  "a": "a) f′(x) = 1 + tan²x > 0 : f est continue et strictement croissante ; limites −∞ en −π/2 et +∞ en π/2 : bijection sur ℝ.\nb) (f⁻¹)′(x) = 1/f′(f⁻¹(x)) = 1/(1 + x²)."},
 {"k": "book", "src": "p. 31, ex. 8", "q": "Une compagnie vend un billet 580 MRU. À partir de 200 passagers, chaque groupe de 5 passagers supplémentaires entraîne une remise de 10 MRU sur tous les billets. L’avion a 300 places. Quel nombre de passagers donne la recette maximale ?",
  "a": "Avec x groupes de 5 au-delà de 200 (0 ≤ x ≤ 20) : f(x) = (200 + 5x)(580 − 10x) = −50x² + 900x + 116 000 ; f′(x) = −100x + 900 = 0 ⇔ x = 9 : 245 passagers, billet à 490 MRU, recette 120 050 MRU. On peut encore ajouter 4 passagers sans déclencher la remise suivante : 249 passagers et une recette de 490 × 249 = 122 010 MRU."},
 {"k": "extra", "src": "", "q": "Calculer : a) lim (x → +∞) (√(x² + 3x) − x) ; b) lim (x → 0) (1 − cos x)/x² ; c) lim (x → 1) (x³ − 1)/(x − 1).",
  "a": "a) 3x/(√(x² + 3x) + x) → 3/2. b) (1 − cos x)/x² = 2sin²(x/2)/x² → 1/2. c) x² + x + 1 → 3."},
 {"k": "extra", "src": "", "q": "f(x) = x² sin(1/x) pour x ≠ 0 et f(0) = 0. Étudier la continuité et la dérivabilité de f en 0.",
  "a": "|f(x)| ≤ x² → 0 : f continue en 0. (f(x) − f(0))/x = x sin(1/x), et |x sin(1/x)| ≤ |x| → 0 : f dérivable en 0 et f′(0) = 0."},
 {"k": "extra", "src": "", "q": "Montrer que l’équation x³ + 2x − 5 = 0 admet une unique solution α sur ℝ et que 1,3 < α < 1,4.",
  "a": "f(x) = x³ + 2x − 5 : f′(x) = 3x² + 2 > 0, f strictement croissante de −∞ à +∞ : solution unique. f(1,3) = −0,203 < 0 et f(1,4) = 0,544 > 0."},
 {"k": "extra", "src": "", "q": "Donner l’approximation affine de √(1 + h) pour h proche de 0 et en déduire une valeur approchée de √1,02.",
  "a": "√(1 + h) ≈ 1 + h/2 (tangente en 0). √1,02 ≈ 1,01."},
 {"k": "int", "src": "", "q": "Un réservoir cylindrique fermé de volume 2π m³ doit être fabriqué avec le moins de tôle possible. On note r le rayon (en m).\n1) Montrer que l’aire totale est S(r) = 2πr² + 4π/r.\n2) Trouver r pour que S soit minimale et donner la hauteur correspondante.",
  "a": "1) h = 2π/(πr²) = 2/r² ; S = 2πr² + 2πrh = 2πr² + 4π/r.\n2) S′(r) = 4πr − 4π/r² = 4π(r³ − 1)/r² : minimum pour r = 1 m, h = 2 m."},
]

SERIES["Chapitre 3 : Étude des fonctions"] = [
 {"k": "book", "src": "p. 44, ex. 3", "q": "Étudier la fonction f(x) = x√((x − 1)/(x + 1)).",
  "a": "D = ]−∞ ; −1[ ∪ [1 ; +∞[. Limites : −∞ en −∞, +∞ en +∞, −∞ en (−1)⁻. f(x)/x → 1 et f(x) − x → −1 : la droite y = x − 1 est asymptote oblique ; x = −1 est asymptote verticale. f′(x) = (x² + x − 1)/((x + 1)²√((x − 1)/(x + 1))) : f croît sur ]−∞ ; (−1 − √5)/2], décroît sur [(−1 − √5)/2 ; −1[, croît sur ]1 ; +∞[. Maximum local f((−1 − √5)/2) ≈ −3,33. En 1, tangente verticale (point d’arrêt (1 ; 0))."},
 {"k": "extra", "src": "", "q": "f(x) = (x² + x + 1)/(x + 1). Écrire f(x) = ax + b + c/(x + 1), puis donner les asymptotes et les variations.",
  "a": "f(x) = x + 1/(x + 1) : a = 1, b = 0, c = 1. Asymptotes x = −1 et y = x. f′(x) = 1 − 1/(x + 1)² = x(x + 2)/(x + 1)² : croissante sur ]−∞ ; −2], décroissante sur [−2 ; −1[ et sur ]−1 ; 0], croissante sur [0 ; +∞[ ; f(−2) = −3 (max local), f(0) = 1 (min local)."},
 {"k": "extra", "src": "", "q": "Montrer que le point I(−1 ; −1) est centre de symétrie de la courbe de f(x) = x + 1/(x + 1).",
  "a": "f(−2 − x) + f(x) = −2 − x − 1/(x + 1) + x + 1/(x + 1) = −2 = 2 × (−1)."},
 {"k": "extra", "src": "", "q": "Étudier la convexité de f(x) = x³ − 3x et donner son point d’inflexion.",
  "a": "f″(x) = 6x : concave sur ]−∞ ; 0], convexe sur [0 ; +∞[ ; point d’inflexion O(0 ; 0)."},
 {"k": "extra", "src": "", "q": "f(x) = √(x² + 1). Montrer que les droites y = x et y = −x sont asymptotes à la courbe en +∞ et en −∞.",
  "a": "En +∞ : f(x) − x = 1/(√(x² + 1) + x) → 0. En −∞ : f(x) + x = 1/(√(x² + 1) − x) → 0."},
 {"k": "int", "src": "", "q": "Une entreprise estime que son bénéfice (en millions d’UM) pour une dépense publicitaire x (en millions, x ≥ 0) est B(x) = 10x/(x² + 4).\n1) Étudier les variations de B.\n2) Quelle dépense rend le bénéfice maximal ? Quel est ce bénéfice ?\n3) Que devient le bénéfice si la dépense devient très grande ?",
  "a": "1) B′(x) = 10(4 − x²)/(x² + 4)² : croissante sur [0 ; 2], décroissante sur [2 ; +∞[.\n2) x = 2 millions : B(2) = 20/8 = 2,5 millions.\n3) B(x) → 0 : trop de publicité n’est pas rentable."},
]

SERIES["Chapitre 4 : Fonctions logarithme népérien et exponentielle"] = [
 {"k": "book", "src": "p. 55, ex. 1", "q": "Résoudre dans ℝ : 1) ln(3 + x) = ln 3 + ln x ; 2) ln(3x) = 3ln x ; 3) ln x + ln(x − 2) = ln(x + 10) ; 4) ln(x² − 2x) = ln(x + 10).",
  "a": "1) x > 0 et 3 + x = 3x : x = 3/2.\n2) x > 0 et 3x = x³ : x = √3.\n3) x > 2 et x² − 2x = x + 10 ⇔ x² − 3x − 10 = 0 : x = 5.\n4) x² − 2x > 0, x + 10 > 0 et x² − 3x − 10 = 0 : x = 5 ou x = −2 (les deux conviennent)."},
 {"k": "book", "src": "p. 55, ex. 2", "q": "Résoudre dans ℝ : ln²x − ln x − 6 > 0.",
  "a": "X = ln x : (X − 3)(X + 2) > 0 ⇔ X < −2 ou X > 3. S = ]0 ; e⁻²[ ∪ ]e³ ; +∞[."},
 {"k": "book", "src": "p. 55, ex. 7", "q": "Sₙ = ln 2 + ln(3/2) + ln(4/3) + … + ln((n + 1)/n). Calculer S₁, S₂, S₃ et S₂₀₁₄.",
  "a": "Sₙ = ln(2 × 3/2 × … × (n + 1)/n) = ln(n + 1). S₁ = ln 2, S₂ = ln 3, S₃ = ln 4, S₂₀₁₄ = ln 2015."},
 {"k": "book", "src": "p. 55, ex. 8", "q": "f(x) = ln(1 + eˣ). 1) Limites. 2) Variations et signe. 3) Montrer que f(x) = x + f(−x) ; en déduire une asymptote Δ en +∞ et la position de la courbe.",
  "a": "1) 0 en −∞ (asymptote y = 0), +∞ en +∞.\n2) f′(x) = eˣ/(1 + eˣ) > 0 : croissante ; 1 + eˣ > 1 donc f > 0.\n3) ln(1 + eˣ) = ln(eˣ(1 + e⁻ˣ)) = x + ln(1 + e⁻ˣ). f(x) − x = ln(1 + e⁻ˣ) → 0 et > 0 : Δ : y = x est asymptote, la courbe est au-dessus."},
 {"k": "book", "src": "p. 55, ex. 10", "q": "1) x est un entier non nul à n chiffres. Justifier que n − 1 ≤ log x < n. 2) En déduire le nombre de chiffres de 2²⁰¹⁴.",
  "a": "1) 10ⁿ⁻¹ ≤ x < 10ⁿ, et log est croissante.\n2) log(2²⁰¹⁴) = 2014 × log 2 ≈ 606,27 : 2²⁰¹⁴ a 607 chiffres."},
 {"k": "book", "src": "p. 55, ex. 11 et 12", "q": "1) Résoudre e^(2x) + eˣ − 2 = 0. 2) Montrer que f(x) = (1 − eˣ)/(1 + eˣ) est impaire.",
  "a": "1) X = eˣ > 0 : X² + X − 2 = 0 ⇔ X = 1 (X = −2 impossible) : x = 0.\n2) f(−x) = (1 − e⁻ˣ)/(1 + e⁻ˣ) ; en multipliant par eˣ : (eˣ − 1)/(eˣ + 1) = −f(x)."},
 {"k": "int", "src": "", "q": "Une population de bactéries double toutes les 3 heures : N(t) = N₀ × 2^(t/3) (t en heures). Au départ N₀ = 1000.\n1) Écrire N(t) sous la forme N₀e^(kt) et donner k.\n2) Combien y a-t-il de bactéries au bout de 10 h ?\n3) Au bout de combien de temps dépasse-t-on 1 million de bactéries ?",
  "a": "1) 2^(t/3) = e^(t ln 2/3) : k = ln 2/3 ≈ 0,231.\n2) N(10) = 1000 × 2^(10/3) ≈ 10 079.\n3) 2^(t/3) > 1000 ⇔ t > 3 ln 1000/ln 2 ≈ 29,9 h."},
]

SERIES["Chapitre 5 : Suites numériques — limites, convergence, suites adjacentes"] = [
 {"k": "book", "src": "p. 71, ex. 3", "q": "Étudier le sens de variation de la suite Uₙ = (3n + 5)/(n + 1).",
  "a": "Uₙ₊₁ − Uₙ = (3n + 8)/(n + 2) − (3n + 5)/(n + 1) = −2/((n + 1)(n + 2)) < 0 : la suite est strictement décroissante."},
 {"k": "book", "src": "p. 71, ex. 4", "q": "Convergence et limite de : 1) Uₙ = (1/n)cos(nπ/3) ; 2) Uₙ = (3n² + 2n + 1)/(5n − 3).",
  "a": "1) −1/n ≤ Uₙ ≤ 1/n : Uₙ → 0 (gendarmes).\n2) Uₙ ≈ 3n/5 → +∞ : la suite diverge."},
 {"k": "book", "src": "p. 71, ex. 5", "q": "Uₙ₊₁ = 2Uₙe^(−Uₙ), et on suppose que (Uₙ) converge vers l. Avec f(x) = 2xe^(−x), déterminer les valeurs possibles de l.",
  "a": "f est continue : f(Uₙ) → f(l) et Uₙ₊₁ → l, donc l = 2le^(−l) ⇔ l(1 − 2e^(−l)) = 0 ⇔ l = 0 ou l = ln 2."},
 {"k": "extra", "src": "", "q": "U₀ = 1 et Uₙ₊₁ = √(2 + Uₙ). Montrer par récurrence que 0 < Uₙ < 2 et que (Uₙ) est croissante ; en déduire qu’elle converge et trouver sa limite.",
  "a": "U₀ = 1 ∈ ]0 ; 2[ ; si 0 < Uₙ < 2 alors √2 < Uₙ₊₁ < 2. Uₙ₊₁² − Uₙ² = 2 + Uₙ − Uₙ² = (2 − Uₙ)(1 + Uₙ) > 0 : croissante. Croissante et majorée, elle converge vers l ≥ 1 avec l = √(2 + l) ⇔ l² − l − 2 = 0 ⇔ l = 2."},
 {"k": "extra", "src": "", "q": "Uₙ = 1 + 1/1! + 1/2! + … + 1/n! et Vₙ = Uₙ + 1/(n × n!). Montrer que (Uₙ) et (Vₙ) sont adjacentes.",
  "a": "Uₙ₊₁ − Uₙ = 1/(n + 1)! > 0. Vₙ₊₁ − Vₙ = 1/(n + 1)! + 1/((n + 1)(n + 1)!) − 1/(n × n!) = −1/(n(n + 1)(n + 1)!) < 0. Vₙ − Uₙ = 1/(n × n!) → 0 : elles sont adjacentes (leur limite commune est e)."},
 {"k": "int", "src": "", "q": "Un lac contient 10 000 poissons. Chaque année la population augmente de 20 %, puis on pêche 3000 poissons. uₙ est le nombre de poissons après n années.\n1) Justifier que uₙ₊₁ = 1,2uₙ − 3000.\n2) On pose vₙ = uₙ − 15 000 : montrer que (vₙ) est géométrique et exprimer uₙ.\n3) Que va-t-il se passer ? En quelle année le lac sera-t-il vide ?",
  "a": "1) Augmentation de 20 % puis retrait de 3000.\n2) vₙ₊₁ = 1,2uₙ − 18 000 = 1,2vₙ ; v₀ = −5000 : uₙ = 15 000 − 5000 × 1,2ⁿ.\n3) uₙ diminue et devient négatif : 1,2ⁿ > 3 ⇔ n > ln 3/ln 1,2 ≈ 6,03. Pendant la 7e année les poissons auront disparu : il faut réduire la pêche."},
]

SERIES["Chapitre 6 : Techniques d’intégration et calcul d’aires"] = [
 {"k": "book", "src": "p. 86, ex. 3", "q": "Calculer par parties : A = ∫₀^π x cos x dx ; B = ∫₋₁⁰ (x + 2)e^(x + 1) dx.",
  "a": "A = [x sin x]₀^π − ∫₀^π sin x dx = 0 − [−cos x]₀^π = −2.\nB = [(x + 2)e^(x + 1)]₋₁⁰ − ∫₋₁⁰ e^(x + 1) dx = (2e − 1) − (e − 1) = e."},
 {"k": "book", "src": "p. 86, ex. 4", "q": "Valeur moyenne : a) f(x) = 3x² sur [0 ; 2] ; b) f(x) = sin x sur [0 ; 2π].",
  "a": "a) m = (1/2)[x³]₀² = 4. b) m = (1/2π)[−cos x]₀^(2π) = 0."},
 {"k": "book", "src": "p. 86, ex. 6", "q": "Majorer et minorer Iₙ = ∫₀¹ xⁿ/(1 + x) dx et en déduire la limite de (Iₙ).",
  "a": "1/(2(n + 1)) ≤ Iₙ ≤ 1/(n + 1) (car 1/2 ≤ 1/(1 + x) ≤ 1) : Iₙ → 0."},
 {"k": "book", "src": "p. 86, ex. 7", "q": "Trouver une primitive de ln x sur ]0 ; +∞[.",
  "a": "Par parties : ∫₁ˣ ln t dt = x ln x − x + 1. Une primitive est F(x) = x ln x − x."},
 {"k": "extra", "src": "", "q": "Calculer l’aire du domaine compris entre la courbe de f(x) = eˣ, l’axe des abscisses et les droites x = 0 et x = ln 3.",
  "a": "A = ∫₀^(ln 3) eˣ dx = 3 − 1 = 2 u.a."},
 {"k": "extra", "src": "", "q": "Calculer ∫₁^e (ln x)/x dx.",
  "a": "(ln x)/x = u′u avec u = ln x : [ (ln x)²/2 ]₁^e = 1/2."},
 {"k": "int", "src": "", "q": "La vitesse d’une voiture qui démarre est v(t) = 20(1 − e^(−0,5t)) m/s (t en secondes).\n1) Vers quelle vitesse tend la voiture ?\n2) Calculer la distance parcourue pendant les 10 premières secondes : d = ∫₀¹⁰ v(t) dt.",
  "a": "1) v(t) → 20 m/s (72 km/h).\n2) d = 20[t + 2e^(−0,5t)]₀¹⁰ = 20(10 + 2e⁻⁵ − 2) ≈ 20 × 8,0135 ≈ 160,3 m."},
]

SERIES["Chapitre 7 : Équations différentielles"] = [
 {"k": "book", "src": "p. 95, ex. 1 et 2", "q": "1) Résoudre 5y′ − 2y = 0 et 3y′ + 4y = 0. 2) Résoudre y′ − 5y = 0 avec y(2) = 7, et 0,4y′ − 1,5y = 0 avec y(0) = 1.",
  "a": "1) y = Ce^(2x/5) ; y = Ce^(−4x/3).\n2) y = 7e^(5(x − 2)) ; y′ = 3,75y : y = e^(3,75x)."},
 {"k": "book", "src": "p. 95, ex. 3", "q": "Résoudre : 1) y″ − 2y = 0 ; 2) y″ + 3y = 0 ; 3) y″ + 4y′ − 5y = 0 ; 4) y″ − 2y′ + y = 0 ; 5) y″ − 4y′ + 7y = 0.",
  "a": "1) y = C₁e^(√2x) + C₂e^(−√2x). 2) y = A cos(√3x) + B sin(√3x). 3) r = 1 ou −5 : y = C₁eˣ + C₂e^(−5x). 4) y = (C₁x + C₂)eˣ. 5) r = 2 ± i√3 : y = e^(2x)(A cos √3x + B sin √3x)."},
 {"k": "book", "src": "p. 95, ex. 4 (5 et 12)", "q": "5) y″ − y′ − 2y = 0, y(1) = 1, y′(1) = 0 ; 12) y″ − 9y = 0, y(0) = 1, y′(0) = 2.",
  "a": "5) y = C₁e^(2x) + C₂e^(−x) : C₁e² + C₂e⁻¹ = 1 et 2C₁e² − C₂e⁻¹ = 0 ⇒ C₁ = e⁻²/3, C₂ = 2e/3 : y = (1/3)e^(2(x − 1)) + (2/3)e^(−(x − 1)).\n12) y = C₁e^(3x) + C₂e^(−3x) : C₁ + C₂ = 1, 3C₁ − 3C₂ = 2 ⇒ C₁ = 5/6, C₂ = 1/6."},
 {"k": "book", "src": "p. 95, ex. 6", "q": "g″ − 2g′ + g = 0. 1) Résoudre. 2) Solution dont la courbe passe par I(0 ; 4) avec une tangente de coefficient directeur 2 en I.",
  "a": "1) g(x) = (Ax + B)eˣ.\n2) g(0) = B = 4 ; g′(0) = A + B = 2 ⇒ A = −2 : g(x) = (4 − 2x)eˣ."},
 {"k": "extra", "src": "", "q": "Résoudre y′ + 2y = 6 et trouver la solution telle que y(0) = 0.",
  "a": "y = Ce^(−2x) + 3 ; y(0) = 0 ⇒ C = −3 : y = 3(1 − e^(−2x))."},
 {"k": "int", "src": "", "q": "Un condensateur se charge selon RC u′ + u = E avec E = 12 V et RC = 2 s ; u(0) = 0.\n1) Résoudre l’équation.\n2) Au bout de combien de temps la tension atteint-elle 99 % de E ?",
  "a": "1) u′ = −u/2 + 6 : u(t) = 12(1 − e^(−t/2)).\n2) e^(−t/2) = 0,01 ⇔ t = 2 ln 100 ≈ 9,2 s."},
]

SERIES["Chapitre 8 : Lois continues, fluctuation et estimation"] = [
 {"k": "book", "src": "p. 117, ex. 6", "q": "Un sac contient 15 jetons : 4 blancs, 6 noirs, 5 rouges ; on tire 3 jetons simultanément. Probabilités de : A « trois noirs » ; B « deux blancs et un rouge » ; C « trois couleurs différentes ».",
  "a": "C(15 ; 3) = 455. P(A) = C(6 ; 3)/455 = 20/455 = 4/91. P(B) = C(4 ; 2) × 5/455 = 30/455 = 6/91. P(C) = 4 × 6 × 5/455 = 120/455 = 24/91."},
 {"k": "book", "src": "p. 117, ex. 7", "q": "Une urne contient 4 boules rouges et 5 blanches ; on tire 3 boules. Probabilité qu’elles soient toutes rouges : a) successivement sans remise ; b) avec remise ; c) simultanément.",
  "a": "a) (4 × 3 × 2)/(9 × 8 × 7) = 1/21. b) (4/9)³ = 64/729. c) C(4 ; 3)/C(9 ; 3) = 4/84 = 1/21."},
 {"k": "book", "src": "p. 117, ex. 8", "q": "Dé pipé : p₂ = p₁, p₃ = 3p₁, p₄ = p₅ = 2p₁, p₆ = 2p₃. Calculer p₁ à p₆, puis la probabilité d’obtenir un chiffre pair.",
  "a": "p₆ = 6p₁ ; la somme vaut 15p₁ = 1 : p₁ = p₂ = 1/15, p₃ = 1/5, p₄ = p₅ = 2/15, p₆ = 2/5. P(pair) = (1 + 2 + 6)/15 = 3/5."},
 {"k": "book", "src": "p. 117, ex. 10", "q": "Un sac contient 8 boules blanches et 2 noires ; on en tire n simultanément. 1) Probabilité d’avoir au moins une noire. 2) Combien faut-il en prendre pour que cette probabilité dépasse 2/3 ?",
  "a": "1) P = 1 − C(8 ; n)/C(10 ; n) = 1 − (10 − n)(9 − n)/90.\n2) (10 − n)(9 − n) < 30 : n = 4 donne 30 (non) ; n = 5 donne 20. Il faut au moins 5 boules."},
 {"k": "book", "src": "p. 113, ex. 9", "q": "Dé à 3 faces blanches, 2 vertes, 1 rouge. Rouge : +2 ; vert : +1 ; blanc : on relance, puis rouge +3, vert −1, blanc 0. Loi et espérance du gain X.",
  "a": "P(2) = 1/6 ; P(1) = 1/3 ; P(3) = 1/12 ; P(−1) = 1/6 ; P(0) = 1/4. E(X) = 3/4."},
 {"k": "extra", "src": "", "q": "X suit la loi uniforme sur [0 ; 10]. Calculer P(2 ≤ X ≤ 5), P(X > 7) et E(X).",
  "a": "P(2 ≤ X ≤ 5) = 3/10 ; P(X > 7) = 3/10 ; E(X) = 5."},
 {"k": "int", "src": "", "q": "Un fournisseur affirme que 90 % de ses graines germent. Une coopérative sème 400 graines : 342 germent.\n1) Calculer la fréquence observée.\n2) Au seuil de 95 %, l’affirmation est-elle acceptable ? (I = p ± 1,96√(p(1 − p)/n))",
  "a": "1) f = 342/400 = 0,855.\n2) I = 0,9 ± 1,96 × √(0,09/400) = 0,9 ± 0,0294 = [0,871 ; 0,929]. f ∉ I : on rejette l’affirmation au risque de 5 %."},
]
