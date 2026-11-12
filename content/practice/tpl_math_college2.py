"""قوالب رياضيات 2AS و3AS — أرقام مختلفة وحلول محسوبة."""
import math
from mathfr import fmt, fr, rnd, PI, D, F
from tpl_math_college import T, LET, art, NOMS

def lin(a, b, v="x"):
    """كتابة a·x + b بشكل سليم."""
    a, b = D(a), D(b)
    t = "" if a == 0 else (("" if a == 1 else "−" if a == -1 else fmt(a)) + v)
    if b == 0: return t or "0"
    if not t: return fmt(b)
    return t + (" + " + fmt(b) if b > 0 else " − " + fmt(-b))

def sgn(fx):  # كسر بإشارة
    return ("−" if fx < 0 else "") + fr(abs(fx))

def sg(x):  # عدد نسبي بين قوسين إن كان سالبًا
    return f"({fmt(x)})" if x < 0 else fmt(x)

# ======================= 2AS =======================
def relatifs_ops(r):
    a, b, c, d = [r.choice([-1, 1]) * r.randint(2, 25) for _ in range(4)]
    e, f = r.choice([-1, 1]) * r.randint(2, 12), r.choice([-1, 1]) * r.randint(2, 9)
    g = r.choice([-1, 1]) * r.randint(2, 9); h = g * r.choice([-1, 1]) * r.randint(2, 9)
    return T("Opérations sur les entiers relatifs",
      f"Calculer :\nA = {sg(a)} + {sg(b)}\nB = {sg(c)} − {sg(d)}\nC = {sg(e)} × {sg(f)}\nD = {sg(h)} ÷ {sg(g)}\nE = {sg(a)} − {sg(b)} + {sg(c)}",
      f"A = {fmt(a+b)}\nB = {sg(c)} + {sg(-d)} = {fmt(c-d)}\nC = {fmt(e*f)}\nD = {fmt(h//g)}\nE = {fmt(a-b+c)}")

def relatifs_axe(r):
    xs = sorted(r.sample(range(-9, 10), 4)); L = r.choice(["ABCD", "EFGH", "MNPQ"])
    A, B, C, Dd = L; a, b, c, d = xs; r.shuffle(xs)
    return T("Repérage sur un axe",
      f"Sur un axe gradué, on place les points {A}({fmt(a)}), {B}({fmt(b)}), {C}({fmt(c)}) et {Dd}({fmt(d)}).\n1) Ranger leurs abscisses dans l'ordre croissant.\n2) Calculer les distances {A}{B}, {A}{Dd} et {B}{C}.\n3) Calculer l'abscisse du milieu I de [{A}{Dd}].\n4) Donner l'abscisse du symétrique de {B} par rapport à {C}.",
      f"1) {fmt(a)} < {fmt(b)} < {fmt(c)} < {fmt(d)}\n2) {A}{B} = {fmt(b)} − {sg(a)} = {b-a} ; {A}{Dd} = {d-a} ; {B}{C} = {c-b}\n3) x(I) = ({fmt(a)} + {sg(d)}) ÷ 2 = {fmt(D(a+d)/2)}\n4) 2 × {sg(c)} − {sg(b)} = {fmt(2*c-b)}")

def rationnels(r):
    a, b = r.randint(1, 7), r.choice([3, 4, 5, 6, 7, 9]); c, d = r.randint(1, 7), r.choice([2, 3, 5, 8])
    s1 = F(a, b) * F(-c, d); e, f = r.randint(1, 5), r.choice([3, 4, 5, 7])
    g, h = r.randint(1, 5), r.choice([2, 6, 9])
    while math.gcd(e, f) != 1: e += 1
    s2 = F(e, f) / F(g, h); p, q = r.randint(1, 5), r.choice([4, 6]); s3 = F(-p, q) + F(1, 3)
    return T("Nombres rationnels",
      f"Calculer et donner le résultat sous forme irréductible :\nA = {a}/{b} × (−{c}/{d})\nB = {e}/{f} ÷ {g}/{h}\nC = −{p}/{q} + 1/3",
      f"A = −{a*c}/{b*d}" + (f" = {sgn(s1)}" if F(a*c, b*d).denominator != b*d else "") + f"\nB = {e}/{f} × {h}/{g} = {e*h}/{f*g}" + (f" = {fr(s2)}" if F(e*h, f*g).denominator != f*g else "") + f"\nC = −{p*3}/{q*3} + {q}/{q*3} = {sgn(s3)}")

def litteral(r):
    k, a, b = r.randint(2, 9), r.randint(1, 9), r.randint(1, 9); m, n = r.randint(2, 7), r.randint(2, 7)
    x0 = r.choice([-3, -2, 2, 3, 4]); p = r.randint(2, 6)
    vA = k * x0 + k * a
    return T("Calcul littéral",
      f"1) Développer : A = {k}(x + {a}) ; B = −{m}(2x − {b}).\n2) Réduire : C = {m}x + {n} − {k}x + {p}x − {a}.\n3) Factoriser : E = {k}x + {k*a} ; F = {m}y² − {m*n}y.\n4) Calculer A pour x = {fmt(x0)}.",
      f"1) A = {k}x + {k*a} ; B = −{2*m}x + {m*b}\n2) C = ({m} − {k} + {p})x + {n} − {a} = {lin(m-k+p, n-a)}\n3) E = {k}(x + {a}) ; F = {m}y(y − {n})\n4) A = {k} × {sg(x0)} + {k*a} = {fmt(vA)}")

def angles2(r):
    a = r.randint(20, 70); b = r.randint(100, 160)
    c = r.randint(35, 145)
    return T("Angles",
      f"1) Calculer le complémentaire d'un angle de {a}° et le supplémentaire d'un angle de {b}°.\n2) Deux droites sécantes forment un angle de {c}°. Calculer les trois autres angles formés.\n3) Deux droites parallèles sont coupées par une sécante. L'un des angles formés mesure {c}°. Quelle est la mesure de l'angle alterne-interne correspondant ? Et celle de son angle adjacent supplémentaire ?",
      f"1) 90° − {a}° = {90-a}° ; 180° − {b}° = {180-b}°\n2) L'angle opposé par le sommet mesure {c}° ; les deux autres mesurent 180° − {c}° = {180-c}°.\n3) Les angles alternes-internes ont la même mesure : {c}° ; l'angle adjacent supplémentaire : {180-c}°.")

def polygones(r):
    a, b, c = r.randint(60, 110), r.randint(70, 120), r.randint(60, 100); d = 360 - a - b - c
    while d <= 20 or d >= 180: c = r.randint(60, 100); d = 360 - a - b - c
    n = r.choice([5, 6, 8, 10, 12]); ang = D(180 * (n - 2)) / n; s = r.randint(3, 9)
    A, B, C, Dd = r.choice(["ABCD", "EFGH", "MNPQ"])
    return T("Polygones",
      f"1) Dans le quadrilatère {A}{B}{C}{Dd}, ∠{A} = {a}°, ∠{B} = {b}° et ∠{C} = {c}°. Calculer ∠{Dd}.\n2) Un polygone régulier a {n} côtés de {s} cm. Calculer son périmètre et la mesure de chacun de ses angles (somme des angles = (n − 2) × 180°).\n3) Citer deux propriétés des diagonales d'un losange.",
      f"1) La somme des angles d'un quadrilatère est 360° : ∠{Dd} = 360° − ({a} + {b} + {c})° = {d}°\n2) Périmètre : {n} × {s} = {n*s} cm ; angle : ({n} − 2) × 180° ÷ {n} = {fmt(ang)}°\n3) Les diagonales d'un losange se coupent en leur milieu et sont perpendiculaires.")

def projection(r):
    A, B = r.choice(["AB", "MN", "EF"]); d = r.choice(["d", "D", "Δ"])
    return T("Projection orthogonale",
      f"Tracer une droite ({d}) et un segment [{A}{B}] non parallèle à ({d}).\n1) Construire les projetés orthogonaux {A}' et {B}' de {A} et {B} sur ({d}).\n2) Que peut-on dire des droites ({A}{A}') et ({B}{B}') ? Justifier.\n3) Si [{A}{B}] est perpendiculaire à ({d}), que peut-on dire de {A}' et {B}' ?",
      f"1) On trace les perpendiculaires à ({d}) passant par {A} et {B} ; elles coupent ({d}) en {A}' et {B}'.\n2) ({A}{A}') et ({B}{B}') sont perpendiculaires à ({d}) : elles sont parallèles.\n3) {A}' et {B}' sont confondus.")

def droites_cercles(r):
    rr = r.randint(3, 8); ds = [D(rr) - 1 - D(r.randint(0, 9))/10, D(rr), D(rr) + 1 + D(r.randint(0, 9))/10]
    r.shuffle(ds); O = "O"
    def pos(dd): return "sécante au cercle (deux points communs)" if dd < rr else ("tangente au cercle (un point commun)" if dd == rr else "extérieure au cercle (aucun point commun)")
    return T("Droites et cercles",
      f"Un cercle (C) a pour centre {O} et pour rayon {rr} cm. Les distances du point {O} aux droites (d1), (d2) et (d3) sont respectivement {fmt(ds[0])} cm, {fmt(ds[1])} cm et {fmt(ds[2])} cm.\n1) Préciser la position de chaque droite par rapport au cercle.\n2) La droite tangente en T est-elle perpendiculaire au rayon [{O}T] ?",
      "1) " + " ; ".join(f"(d{i+1}) : {pos(x)}" for i, x in enumerate(ds)) + f"\n2) Oui : la tangente en T est perpendiculaire au rayon [{O}T].")

def symetrie_centrale(r):
    A, B, C = r.choice(LET); l1, l2 = r.randint(3, 8), r.randint(3, 8); ang = r.randint(30, 120)
    return T("Symétrie centrale",
      f"{A}{B}{C} est un triangle tel que {A}{B} = {l1} cm, {A}{C} = {l2} cm et ∠{B}{A}{C} = {ang}°. O est le milieu de [{B}{C}].\n1) Construire {A}', le symétrique de {A} par rapport à O.\n2) Quelle est la nature du quadrilatère {A}{B}{A}'{C} ? Justifier.\n3) Donner {A}'{C}, {A}'{B} et ∠{B}{A}'{C}.",
      f"2) O est le milieu de [{B}{C}] et de [{A}{A}'] : les diagonales se coupent en leur milieu, {A}{B}{A}'{C} est un parallélogramme.\n3) La symétrie centrale conserve les longueurs et les angles : {A}'{C} = {A}{B} = {l1} cm ; {A}'{B} = {A}{C} = {l2} cm ; ∠{B}{A}'{C} = {ang}°.")

def vitesse(r):
    v = r.choice([40, 50, 60, 70, 80, 90]); t = r.choice([D('1.5'), D('2'), D('2.5'), D('3'), D('0.75')])
    d2 = r.choice([120, 150, 180, 210, 240]); t2 = r.choice([2, 3, 4])
    villes = r.choice([("Nouakchott", "Rosso"), ("Nouakchott", "Boutilimit"), ("Kiffa", "Aïoun"), ("Atar", "Akjoujt")])
    return T("Proportionnalité : vitesse moyenne",
      f"1) Un car roule à la vitesse moyenne de {v} km/h pendant {fmt(t)} h. Quelle distance parcourt-il ?\n2) La distance entre {villes[0]} et {villes[1]} est {d2} km ; le trajet dure {t2} h. Calculer la vitesse moyenne.\n3) Combien de temps faut-il pour parcourir {v*2} km à {v} km/h ?",
      f"1) d = {v} × {fmt(t)} = {fmt(v*t)} km\n2) v = {d2} ÷ {t2} = {fmt(D(d2)/t2)} km/h\n3) t = {v*2} ÷ {v} = 2 h")

def stats2(r):
    vals = sorted(r.sample(range(8, 19), 5)); eff = [r.randint(2, 9) for _ in vals]; n = sum(eff)
    m = D(sum(v * e for v, e in zip(vals, eff))) / n
    fr_ = [rnd(D(e * 100) / n, 1) for e in eff]
    return T("Statistique",
      f"Le tableau donne l'âge (en années) des membres d'un club :\nÂge : " + " | ".join(map(str, vals)) + "\nEffectif : " + " | ".join(map(str, eff)) +
      "\n1) Quel est l'effectif total ?\n2) Calculer les fréquences en pourcentage (arrondies au dixième).\n3) Calculer l'âge moyen (arrondi au dixième).\n4) Quel est le mode ?",
      f"1) N = {n}\n2) " + " ; ".join(f"{v} ans : {fmt(f)} %" for v, f in zip(vals, fr_)) +
      f"\n3) (" + " + ".join(f"{v}×{e}" for v, e in zip(vals, eff)) + f") ÷ {n} ≈ {fmt(rnd(m,1))} ans\n4) " +
      " et ".join(f"{v} ans" for v, e in zip(vals, eff) if e == max(eff)))

def cylindre_prisme(r):
    rr, h = r.randint(2, 9), r.randint(4, 15); V = PI * rr * rr * h; Al = 2 * PI * rr * h
    a, b, H = r.randint(3, 9), r.randint(3, 9), r.randint(5, 14); Vp = D(a * b) / 2 * H
    return T("Cylindre et prisme droit",
      f"1) Un cylindre a pour rayon {rr} cm et pour hauteur {h} cm (π ≈ 3,14). Calculer son aire latérale et son volume.\n2) Un prisme droit a pour base un triangle rectangle dont les côtés de l'angle droit mesurent {a} cm et {b} cm ; sa hauteur est {H} cm. Calculer l'aire de la base et le volume du prisme.",
      f"1) Aire latérale : 2 × 3,14 × {rr} × {h} = {fmt(Al)} cm² ; volume : 3,14 × {rr}² × {h} = {fmt(V)} cm³\n2) Base : {a} × {b} ÷ 2 = {fmt(D(a*b)/2)} cm² ; volume : {fmt(D(a*b)/2)} × {H} = {fmt(Vp)} cm³")

def sit_citerne(r):
    rr = D(r.randint(5, 12)) / 10; h = D(r.randint(10, 25)) / 10; V = PI * rr * rr * h
    L = V * 1000; prix = r.choice([5, 10, 15])
    return T("Situation d'intégration : la citerne",
      f"Une citerne cylindrique a un rayon de {fmt(rr)} m et une hauteur de {fmt(h)} m (π ≈ 3,14).\n1) Calculer son volume en m³ puis en litres.\n2) L'eau est vendue {prix} MRU les 100 litres. Quel est le prix de l'eau d'une citerne pleine ?\n3) On a déjà utilisé 30 % de l'eau. Combien de litres restent ?",
      f"1) V = 3,14 × {fmt(rr)}² × {fmt(h)} = {fmt(V)} m³ = {fmt(L)} L\n2) {fmt(L)} ÷ 100 × {prix} = {fmt(L/100*prix)} MRU\n3) {fmt(L)} × 70 ÷ 100 = {fmt(L*70/100)} L")

def sit_voyage(r):
    v = r.choice([60, 75, 80, 90]); d = v * r.choice([3, 4, 5]); dep = r.choice([6, 7, 8]); conso = r.choice([8, 9, 10, 12]); px = r.choice([50, 55, 60])
    h = d // v; L = D(d * conso) / 100
    return T("Situation d'intégration : le voyage",
      f"Une famille part en voiture à {dep} h pour un trajet de {d} km à la vitesse moyenne de {v} km/h.\n1) Combien de temps dure le trajet ? À quelle heure arrive-t-elle ?\n2) La voiture consomme {conso} L aux 100 km. Combien de litres faut-il ?\n3) Le litre de carburant coûte {px} MRU. Calculer la dépense.",
      f"1) {d} ÷ {v} = {h} h ; arrivée à {dep + h} h\n2) {d} × {conso} ÷ 100 = {fmt(L)} L\n3) {fmt(L)} × {px} = {fmt(L*px)} MRU")

def sit_expression(r):
    x = r.randint(3, 9); a = r.randint(2, 6)
    return T("Situation d'intégration : le terrain",
      f"Un terrain rectangulaire a une longueur de (2x + {a}) m et une largeur de x m.\n1) Exprimer son périmètre P en fonction de x, puis le réduire.\n2) Exprimer son aire A en fonction de x (développer).\n3) Calculer P et A pour x = {x}.",
      f"1) P = 2(2x + {a} + x) = 2(3x + {a}) = 6x + {2*a}\n2) A = x(2x + {a}) = 2x² + {a}x\n3) P = 6 × {x} + {2*a} = {6*x+2*a} m ; A = 2 × {x*x} + {a} × {x} = {2*x*x + a*x} m²")

# ======================= 3AS =======================
def pgcd(r):
    g = r.choice([6, 8, 12, 14, 15, 18, 21, 24]); a, b = g * r.choice([5, 7, 9, 11]), g * r.choice([4, 8, 13, 10])
    while math.gcd(a, b) != g: b += g
    steps = []; x, y = max(a, b), min(a, b)
    while y: steps.append(f"{x} = {y} × {x//y} + {x%y}"); x, y = y, x % y
    return T("Arithmétique : PGCD",
      f"1) Calculer PGCD({a} ; {b}) par l'algorithme d'Euclide.\n2) En déduire la forme irréductible de {a}/{b}.\n3) {a} et {b} sont-ils premiers entre eux ?",
      "1) " + " ; ".join(steps) + f" → PGCD = {g}\n2) {a}/{b} = {a//g}/{b//g}\n3) Non, leur PGCD est {g} ≠ 1.")

def premiers(r):
    n = r.choice([2**3 * 3 * 5, 2**2 * 3**2 * 7, 2 * 3 * 5 * 7, 2**4 * 3**2, 2 * 5**2 * 7, 3**3 * 2 * 11])
    f = []; m = n; p = 2
    while m > 1:
        while m % p == 0: f.append(p); m //= p
        p += 1
    from collections import Counter
    dec = " × ".join(f"{p}" if e == 1 else f"{p}^{e}" for p, e in sorted(Counter(f).items()))
    return T("Nombres premiers",
      f"1) Parmi les nombres 21 ; 29 ; 51 ; 53 ; 91 ; 97, lesquels sont premiers ?\n2) Décomposer {n} en produit de facteurs premiers.",
      f"1) 29 ; 53 ; 97 (21 = 3 × 7 ; 51 = 3 × 17 ; 91 = 7 × 13)\n2) {n} = {dec}")

def racines(r):
    a = r.choice([2, 3, 5, 6, 7]); k1, k2 = r.sample([2, 3, 4, 5], 2); q = r.choice([4, 9, 16, 25, 36, 49])
    return T("Nombres réels : racines carrées",
      f"Simplifier :\nA = √{k1*k1*a}\nB = √{k2*k2*a} + 2√{a}\nC = √{q} × √{q}\nD = (√{a})² + √{q}",
      f"A = √({k1*k1} × {a}) = {k1}√{a}\nB = {k2}√{a} + 2√{a} = {k2+2}√{a}\nC = {q}\nD = {a} + {int(q**.5)} = {a + int(q**.5)}")

def puissances(r):
    a = r.randint(2, 5); m, n = r.randint(2, 5), r.randint(2, 4); x = r.randint(12, 98) * 10 ** r.randint(3, 6); y = D(r.randint(12, 98)) / 10 ** r.randint(4, 6)
    def sci(v):
        v = D(v); e = v.adjusted(); return f"{fmt(v.scaleb(-e).normalize())} × 10^{fmt(e)}"
    return T("Puissances et écriture scientifique",
      f"1) Écrire sous la forme d'une seule puissance : {a}^{m} × {a}^{n} ; ({a}^{m})^{n} ; {a}^{m+n} ÷ {a}^{n}.\n2) Donner l'écriture scientifique de {fmt(x)} et de {fmt(y)}.",
      f"1) {a}^{m+n} ; {a}^{m*n} ; {a}^{m}\n2) {fmt(x)} = {sci(x)} ; {fmt(y)} = {sci(y)}")

def identites(r):
    a, b = r.randint(1, 9), r.randint(2, 9); c = r.randint(2, 7)
    return T("Identités remarquables",
      f"1) Développer : (x + {a})² ; (x − {b})² ; (x + {c})(x − {c}).\n2) Factoriser : x² + {2*a}x + {a*a} ; x² − {b*b} ; 9x² − {c*c}.",
      f"1) x² + {2*a}x + {a*a} ; x² − {2*b}x + {b*b} ; x² − {c*c}\n2) (x + {a})² ; (x − {b})(x + {b}) ; (3x − {c})(3x + {c})")

def equations(r):
    a, x0 = r.randint(2, 7), r.choice([-4, -3, -2, 2, 3, 4, 5]); b = r.randint(1, 15); c = a * x0 + b
    p, q = r.randint(1, 6), r.randint(1, 6); m = r.randint(2, 5); k = r.randint(1, 12)
    return T("Équations et inéquations",
      f"1) Résoudre : {a}x + {b} = {fmt(c)}.\n2) Résoudre : (x − {p})(x + {q}) = 0.\n3) Résoudre l'inéquation {m}x − {k} > {fmt(m*2 - k)} et représenter les solutions sur une droite graduée.",
      f"1) {a}x = {fmt(c)} − {b} = {fmt(c-b)} ; x = {fmt(x0)}\n2) Produit nul : x − {p} = 0 ou x + {q} = 0, donc x = {p} ou x = −{q}\n3) {m}x > {m*2} ; x > 2 (demi-droite ouverte en 2)")

def angle_inscrit(r):
    c = r.choice([60, 70, 80, 100, 110, 120, 140]); A, B, M = r.choice(["ABM", "EFP", "RST"])
    return T("Angle inscrit et angle au centre",
      f"{A}, {B} et {M} sont trois points d'un cercle de centre O. L'angle au centre ∠{A}O{B} mesure {c}°.\n1) Calculer l'angle inscrit ∠{A}{M}{B} qui intercepte le même arc.\n2) N est un autre point du même arc que {M}. Calculer ∠{A}N{B}. Justifier.",
      f"1) L'angle inscrit vaut la moitié de l'angle au centre : {c}° ÷ 2 = {c//2}°\n2) Deux angles inscrits interceptant le même arc sont égaux : ∠{A}N{B} = {c//2}°")

def triangle_cercle(r):
    hyp = r.choice([10, 13, 17, 25, 26]); A, B, C = r.choice(LET)
    return T("Triangle rectangle et cercle",
      f"{A}{B}{C} est un triangle rectangle en {A} tel que {B}{C} = {hyp} cm.\n1) Quel est le centre du cercle circonscrit à {A}{B}{C} ? Quel est son rayon ?\n2) I est le milieu de [{B}{C}]. Calculer {A}I.",
      f"1) Le cercle circonscrit à un triangle rectangle a pour centre le milieu de l'hypoténuse [{B}{C}] ; rayon {fmt(D(hyp)/2)} cm.\n2) La médiane issue de l'angle droit mesure la moitié de l'hypoténuse : {A}I = {fmt(D(hyp)/2)} cm")

def vecteurs(r):
    xa, ya, xb, yb = [r.randint(-6, 8) for _ in range(4)]
    while (xa, ya) == (xb, yb): xb += 1
    d2 = (xb - xa) ** 2 + (yb - ya) ** 2; dist = D(d2).sqrt()
    dtxt = fmt(int(dist)) if int(dist) ** 2 == d2 else f"√{d2} ≈ {fmt(rnd(dist,2))}"
    return T("Vecteurs et repères",
      f"Dans un repère orthonormé, on donne A({fmt(xa)} ; {fmt(ya)}) et B({fmt(xb)} ; {fmt(yb)}).\n1) Calculer les coordonnées du vecteur AB.\n2) Calculer les coordonnées du milieu I de [AB].\n3) Calculer la distance AB.",
      f"1) AB({fmt(xb-xa)} ; {fmt(yb-ya)})\n2) I({fmt(D(xa+xb)/2)} ; {fmt(D(ya+yb)/2)})\n3) AB = √({sg(xb-xa)}² + {sg(yb-ya)}²) = {dtxt}")

def pythagore(r):
    a, b, c = r.choice([(3, 4, 5), (6, 8, 10), (5, 12, 13), (8, 15, 17), (9, 12, 15), (7, 24, 25), (12, 16, 20)])
    A, B, C = r.choice(LET); x, y, z = r.choice([(4, 5, 6), (5, 12, 13), (6, 7, 9), (9, 40, 41)])
    rec = "rectangle" if x * x + y * y == z * z else "n'est pas rectangle"
    return T("Théorème de Pythagore",
      f"1) {A}{B}{C} est rectangle en {A} avec {A}{B} = {a} cm et {A}{C} = {b} cm. Calculer {B}{C}.\n2) Un triangle a pour côtés {x} cm, {y} cm et {z} cm. Est-il rectangle ? Justifier.\n3) Une échelle de {c} m est appuyée contre un mur ; son pied est à {a} m du mur. À quelle hauteur touche-t-elle le mur ?",
      f"1) {B}{C}² = {a}² + {b}² = {a*a+b*b} ; {B}{C} = {c} cm\n2) {z}² = {z*z} et {x}² + {y}² = {x*x+y*y} : le triangle {rec} (réciproque de Pythagore).\n3) h² = {c}² − {a}² = {c*c-a*a} ; h = {b} m")

TRIG = {30: ("0,866", "0,5", "0,577"), 40: ("0,766", "0,643", "0,839"), 45: ("0,707", "0,707", "1"), 50: ("0,643", "0,766", "1,192"), 60: ("0,5", "0,866", "1,732")}
def trigo(r):
    ang = r.choice(list(TRIG)); hyp = r.randint(6, 20); cs, sn, tg = TRIG[ang]; A, B, C = r.choice(LET)
    adj = D(hyp) * D(cs.replace(",", ".")); opp = D(hyp) * D(sn.replace(",", "."))
    return T("Trigonométrie",
      f"{A}{B}{C} est un triangle rectangle en {A} avec {B}{C} = {hyp} cm et ∠{A}{B}{C} = {ang}°.\nOn donne : cos {ang}° ≈ {cs} ; sin {ang}° ≈ {sn}.\n1) Calculer {A}{B} (arrondi au dixième).\n2) Calculer {A}{C} (arrondi au dixième).\n3) Calculer ∠{A}{C}{B}.",
      f"1) cos {ang}° = {A}{B}/{B}{C} donc {A}{B} = {hyp} × {cs} ≈ {fmt(rnd(adj,1))} cm\n2) sin {ang}° = {A}{C}/{B}{C} donc {A}{C} = {hyp} × {sn} ≈ {fmt(rnd(opp,1))} cm\n3) ∠{A}{C}{B} = 90° − {ang}° = {90-ang}°")

def translation(r):
    xa, ya, u, v = [r.randint(-5, 6) for _ in range(4)]
    return T("Transformations : translation",
      f"Dans un repère, on donne A({fmt(xa)} ; {fmt(ya)}) et le vecteur u({fmt(u)} ; {fmt(v)}).\n1) Calculer les coordonnées de A', image de A par la translation de vecteur u.\n2) Calculer les coordonnées du symétrique de A par rapport à l'origine O.",
      f"1) A'({fmt(xa)} + {sg(u)} ; {fmt(ya)} + {sg(v)}) = A'({fmt(xa+u)} ; {fmt(ya+v)})\n2) ({fmt(-xa)} ; {fmt(-ya)})")

def fct_lineaire(r):
    a = r.choice([D(2), D(3), D('1.5'), D('-2'), D('0.5'), D(4)]); x1 = r.randint(2, 9); y2 = a * r.randint(2, 12)
    return T("Fonction linéaire",
      f"f est la fonction linéaire telle que f({x1}) = {fmt(a*x1)}.\n1) Déterminer le coefficient de f.\n2) Calculer l'image de −3.\n3) Calculer l'antécédent de {fmt(y2)}.\n4) Que représente graphiquement f ?",
      f"1) a = {fmt(a*x1)} ÷ {x1} = {fmt(a)} ; f(x) = {fmt(a)}x\n2) f(−3) = {fmt(a*-3)}\n3) x = {fmt(y2)} ÷ {fmt(a)} = {fmt(y2/a)}\n4) Une droite passant par l'origine du repère.")

def boule(r):
    rr = r.randint(2, 12); A = 4 * PI * rr * rr; V = D(4) / 3 * PI * rr ** 3
    return T("Boule et sphère",
      f"Une boule a un rayon de {rr} cm (π ≈ 3,14).\n1) Calculer l'aire de la sphère.\n2) Calculer le volume de la boule (arrondi à l'unité).",
      f"1) A = 4 × 3,14 × {rr}² = {fmt(A)} cm²\n2) V = 4/3 × 3,14 × {rr}³ ≈ {fmt(rnd(V,0))} cm³")

def stats3(r):
    vals = sorted(r.choices(range(5, 19), k=r.choice([9, 11, 13])))
    n = len(vals); med = vals[n // 2]; m = D(sum(vals)) / n; sh = vals[:]; r.shuffle(sh)
    return T("Statistique : moyenne et médiane",
      f"Notes d'un groupe d'élèves : " + " ; ".join(map(str, sh)) + "\n1) Ranger la série dans l'ordre croissant.\n2) Calculer la moyenne (arrondie au dixième).\n3) Déterminer la médiane.\n4) Calculer l'étendue.",
      f"1) " + " ; ".join(map(str, vals)) + f"\n2) {sum(vals)} ÷ {n} ≈ {fmt(rnd(m,1))}\n3) Effectif {n} impair : médiane = {n//2+1}e valeur = {med}\n4) {vals[-1]} − {vals[0]} = {vals[-1]-vals[0]}")

def sit_rampe(r):
    h, l = r.choice([(D('0.6'), D('8')), (D('0.5'), D('6')), (D('0.9'), D('12')), (D('0.75'), D('10'))])
    L = (h * h + l * l).sqrt()
    pente = h / l * 100
    return T("Situation d'intégration : la rampe d'accès",
      f"Pour permettre l'accès aux personnes en fauteuil roulant, on construit une rampe devant le collège. Elle doit monter de {fmt(h)} m sur une distance horizontale de {fmt(l)} m.\n1) Calculer la longueur de la rampe (arrondie au centimètre).\n2) Calculer sa pente en pourcentage (hauteur ÷ distance horizontale × 100).\n3) La pente maximale autorisée est 8 %. La rampe est-elle conforme ?",
      f"1) L² = {fmt(h)}² + {fmt(l)}² = {fmt(h*h + l*l)} ; L ≈ {fmt(rnd(L,2))} m\n2) {fmt(h)} ÷ {fmt(l)} × 100 = {fmt(rnd(pente,2))} %\n3) {'Oui' if pente <= 8 else 'Non'}, car {fmt(rnd(pente,2))} % {'≤' if pente <= 8 else '>'} 8 %.")

def sit_forfait(r):
    a = r.choice([5, 8, 10]); b = r.choice([200, 300, 400]); c = r.choice([12, 15, 20])
    x0 = D(b) / (c - a)
    return T("Situation d'intégration : choisir un forfait",
      f"Un cybercafé propose deux formules :\nFormule A : {c} MRU par heure de connexion.\nFormule B : un abonnement de {b} MRU par mois plus {a} MRU par heure.\n1) Exprimer le prix pA(x) et pB(x) pour x heures.\n2) Calculer le prix de chaque formule pour 20 heures et pour 60 heures.\n3) Résoudre pA(x) = pB(x) et conclure.",
      f"1) pA(x) = {c}x ; pB(x) = {b} + {a}x\n2) 20 h : A = {20*c} ; B = {b+20*a}. 60 h : A = {60*c} ; B = {b+60*a}\n3) {c}x = {b} + {a}x ⇔ {c-a}x = {b} ⇔ x {"=" if x0 == rnd(x0,2) else "≈"} {fmt(rnd(x0,2))} h. En dessous la formule A est moins chère, au-delà la formule B.")
