"""قوالب تمارين رياضيات الإعدادية (1AS) — كل قالب يعطي (عنوان، نص، حل) بأرقام مختلفة وحل محسوب."""
from mathfr import fmt, fr, rnd, PI, D, F

def art(a): return "le" if a == 1 else "les"

def T(title, text, sol): return {"title": title, "text": text, "solution": sol}
LET = ["ABC", "EFG", "MNP", "RST", "IJK", "DEF", "PQR", "UVW"]

# ———— Entiers naturels ————
def calc_entiers(r):
    a, b, c = r.randint(120, 900), r.randint(20, 99), r.randint(10, 80)
    m, n, k = r.choice([25, 50, 125]), r.choice([4, 8, 2]), r.randint(11, 39)
    q = r.randint(105, 480); dv = r.choice([4, 6, 8, 9, 12])
    return T("Calculs sur les entiers naturels",
      f"Calculer :\na = {fmt(a)} + {b} − {c}\nb = {m} × {n} × {k}\nc = {fmt(q*dv)} ÷ {dv}\nd = {fmt(1000*r.randint(1,9))} − {fmt(a)}",
      None) | {"solution": None, "_v": (a, b, c, m, n, k, q, dv)}

def _calc_entiers(r):
    a, b, c = r.randint(120, 900), r.randint(20, 99), r.randint(10, 80)
    m, n, k = r.choice([(25, 4), (50, 2), (125, 8)]) + (r.randint(11, 39),)
    q = r.randint(105, 480); dv = r.choice([4, 6, 8, 9, 12]); t = 1000 * r.randint(2, 9)
    return T("Calculs sur les entiers naturels",
      f"Calculer :\na = {fmt(a)} + {b} − {c}\nb = {m} × {n} × {k}\nc = {fmt(q*dv)} ÷ {dv}\nd = {fmt(t)} − {fmt(a)}",
      f"a = {fmt(a+b)} − {c} = {fmt(a+b-c)}\nb = {m*n} × {k} = {fmt(m*n*k)}\nc = {fmt(q)}\nd = {fmt(t-a)}")
calc_entiers = _calc_entiers

def priorites(r):
    p, q, s = r.randint(5, 30), r.randint(2, 9), r.randint(2, 9)
    u = r.choice([2, 3, 4, 5, 6]); t = u * r.randint(2, 9); v = r.randint(1, 9); w0 = r.randint(40, 90)
    x, y = r.randint(8, 15), r.randint(1, 7); w = r.randint(3, 9); z, k = r.randint(2, 5), r.randint(2, 4)
    A = p + q * s; B = (p + q) * s; C = w0 - t // u + v; Dv = w * (x - y) - z * k
    return T("Priorités opératoires",
      f"Calculer en respectant les priorités :\nA = {p} + {q} × {s}\nB = ({p} + {q}) × {s}\nC = {w0} − {t} ÷ {u} + {v}\nD = {w} × ({x} − {y}) − {z} × {k}",
      f"A = {p} + {q*s} = {A}\nB = {p+q} × {s} = {B}\nC = {w0} − {t//u} + {v} = {C}\nD = {w} × {x-y} − {z*k} = {w*(x-y)} − {z*k} = {Dv}")

def division(r):
    b = r.randint(7, 45); a = r.randint(250, 2500)
    b2 = r.randint(11, 30); a2 = r.randint(300, 1500)
    return T("Division euclidienne",
      f"1) Effectuer la division euclidienne de {fmt(a)} par {b} et écrire l'égalité correspondante.\n2) Même question pour {fmt(a2)} et {b2}.\n3) Quel est le plus petit nombre à ajouter à {fmt(a)} pour obtenir un multiple de {b} ?",
      f"1) {fmt(a)} = {b} × {a//b} + {a%b} (reste {a%b} < {b})\n2) {fmt(a2)} = {b2} × {a2//b2} + {a2%b2}\n3) " +
      (f"{fmt(a)} est déjà un multiple de {b} : on ajoute 0." if a % b == 0 else f"{b} − {a%b} = {b - a%b} (car {fmt(a + b - a%b)} = {b} × {a//b + 1})"))

def divisibilite(r):
    ns = sorted(set(r.choice([r.randint(100, 999), 3 * r.randint(40, 330), 5 * r.randint(30, 200), 9 * r.randint(20, 111), 2 * r.randint(50, 500)]) for _ in range(9)))[:5]
    ds = lambda n: sum(map(int, str(n)))
    by = lambda k: [n for n in ns if n % k == 0]
    j = lambda l: " ; ".join(fmt(x) for x in l) if l else "aucun"
    return T("Critères de divisibilité",
      f"On donne les nombres : {j(ns)}.\nParmi ces nombres, lesquels sont divisibles :\na) par 2 ? b) par 3 ? c) par 5 ? d) par 9 ?\nJustifier à l'aide des critères de divisibilité.",
      f"Sommes des chiffres : " + " ; ".join(f"{fmt(n)} → {ds(n)}" for n in ns) +
      f"\na) Par 2 (unités paires) : {j(by(2))}\nb) Par 3 (somme multiple de 3) : {j(by(3))}\nc) Par 5 (unités 0 ou 5) : {j(by(5))}\nd) Par 9 (somme multiple de 9) : {j(by(9))}")

# ———— Décimaux ————
def dec_ecriture(r):
    a, b, c = r.randint(1, 9), r.randint(0, 9), r.randint(0, 9)
    d1, d2, d3 = r.randint(1, 9), r.randint(0, 9), r.randint(1, 9)
    x = D(f"{a}{b}{c}.{d1}{d2}{d3}")
    u, cen = r.randint(2, 40), r.randint(1, 9)
    mil = r.randint(101, 989)
    return T("Écriture des nombres décimaux",
      f"1) Décomposer {fmt(x)} selon la valeur de ses chiffres.\n2) Écrire en chiffres :\na) {u} unités et {cen} centièmes ;\nb) {mil} millièmes.\n3) Donner la partie entière et la partie décimale de {fmt(x)}.",
      f"1) {fmt(x)} = {a} × 100 + {b} × 10 + {c} + {d1}/10 + {d2}/100 + {d3}/1 000\n2) a) {fmt(D(u) + D(cen)/100)}  b) {fmt(D(mil)/1000)}\n3) Partie entière : {a*100+b*10+c} ; partie décimale : {fmt(x - (a*100+b*10+c))}")

def ranger(r):
    k = r.randint(2, 30)
    a, b = r.randint(1, 9), r.randint(1, 9)
    vals = {D(f"{k}.{a}"), D(f"{k}.0{a}"), D(f"{k}.{a}0{b}"), D(f"{k}.0{a}{b}"), D(f"{k-1}.99")}
    vals = sorted(vals)
    sh = vals[:]; r.shuffle(sh)
    return T("Comparer et ranger des décimaux",
      f"Ranger dans l'ordre croissant :\n" + " ; ".join(fmt(x) for x in sh) + f"\nPuis encadrer {fmt(vals[2])} par deux entiers consécutifs.",
      " < ".join(fmt(x) for x in vals) + f"\n{int(vals[2])} < {fmt(vals[2])} < {int(vals[2])+1}")

def arrondi(r):
    x = D(r.randint(10000, 99999)) / 1000
    return T("Valeurs approchées",
      f"Soit le nombre {fmt(x)}.\n1) Donner son arrondi à l'unité, au dixième et au centième.\n2) Donner sa troncature au dixième.\n3) L'encadrer par deux décimaux consécutifs ayant un chiffre après la virgule.",
      f"1) Unité : {fmt(rnd(x,0))} ; dixième : {fmt(rnd(x,1))} ; centième : {fmt(rnd(x,2))}\n2) Troncature au dixième : {fmt(D(int(x*10))/10)}\n3) {fmt(D(int(x*10))/10)} < {fmt(x)} < {fmt(D(int(x*10)+1)/10)}")

def ops_dec(r):
    a, b = D(r.randint(101, 999)) / 10, D(r.randint(101, 999)) / 100
    c = r.randint(20, 90); d = D(r.randint(101, 999)) / 100
    e, f = D(r.randint(11, 49)) / 10, D(r.randint(11, 49)) / 10
    g = D(r.randint(101, 999)) / 100; p = r.choice([10, 100, 1000]); h = D(r.randint(1001, 9999)) / 10; q = r.choice([10, 100])
    return T("Opérations sur les décimaux",
      f"Calculer :\na) {fmt(a)} + {fmt(b)}\nb) {c} − {fmt(d)}\nc) {fmt(e)} × {fmt(f)}\nd) {fmt(g)} × {fmt(p)}\ne) {fmt(h)} ÷ {q}",
      f"a) {fmt(a+b)}  b) {fmt(c-d)}  c) {fmt(e*f)}  d) {fmt(g*p)}  e) {fmt(h/q)}")

NOMS = ["Aminata", "Mohamed", "Fatimetou", "Sidi", "Mariem", "Ahmed", "Khadijetou", "Oumar", "Zeinebou", "Cheikh", "Aïcha", "Moussa"]
def achat(r):
    n = r.choice(NOMS); k = r.randint(2, 6); p = D(r.randint(150, 450)) / 10; s = D(r.randint(50, 250)) / 10
    tot = k * p + s; bill = 100 if tot < 100 else (200 if tot < 200 else 500)
    return T("Problème : achats",
      f"{n} achète {k} cahiers à {fmt(p)} MRU l'un et une règle à {fmt(s)} MRU. Il paie avec un billet de {bill} MRU.\n1) Combien coûtent les cahiers ?\n2) Quel est le montant total ?\n3) Combien lui rend-on ?",
      f"1) {k} × {fmt(p)} = {fmt(k*p)} MRU\n2) {fmt(k*p)} + {fmt(s)} = {fmt(tot)} MRU\n3) {bill} − {fmt(tot)} = {fmt(bill - tot)} MRU")

def deux_offres(r):
    q = r.randint(3, 8); p1 = D(r.randint(80, 140)); p2 = p1 + r.randint(5, 20); rem = r.randint(10, 60) * 5
    t1, t2 = q * p1 + rem, q * p2
    best = "le magasin A" if t1 < t2 else ("le magasin B" if t2 < t1 else "les deux magasins (même prix)")
    return T("Problème : comparer deux offres",
      f"On veut acheter {q} chemises.\nMagasin A : {fmt(p1)} MRU la chemise, plus {rem} MRU de livraison.\nMagasin B : {fmt(p2)} MRU la chemise, livraison gratuite.\nQuel magasin est le plus avantageux ? Justifier.",
      f"A : {q} × {fmt(p1)} + {rem} = {fmt(t1)} MRU\nB : {q} × {fmt(p2)} = {fmt(t2)} MRU\nLe plus avantageux est {best}.")

# ———— Droites et segments ————
def segments(r):
    L = r.choice(LET); A, B, C = L
    x = D(r.randint(4, 14)) / 2 + 1; y = D(r.randint(4, 14)) / 2 + 1
    ac = x + y; am = ac / 2; mb = abs(am - x)
    pos = f"M est entre {A} et {B}" if am < x else (f"M est entre {B} et {C}" if am > x else f"M et {B} sont confondus")
    return T("Points alignés et milieu",
      f"Les points {A}, {B} et {C} sont alignés dans cet ordre avec {A}{B} = {fmt(x)} cm et {B}{C} = {fmt(y)} cm.\n1) Faire une figure.\n2) Calculer {A}{C}.\n3) M est le milieu de [{A}{C}]. Calculer {A}M, puis la distance M{B}.",
      f"2) {A}{C} = {A}{B} + {B}{C} = {fmt(x)} + {fmt(y)} = {fmt(ac)} cm\n3) {A}M = {fmt(ac)} ÷ 2 = {fmt(am)} cm ; " + (f"M{B} = |{fmt(am)} − {fmt(x)}| = {fmt(mb)} cm ({pos})" if mb else f"M{B} = 0 cm ({pos})"))

def droites(r):
    v = r.randint(0, 2); d, a, b = r.choice([("d", "d1", "d2"), ("D", "D1", "D2"), ("L", "L1", "L2")])
    P = r.choice("AMPK")
    if v == 0:
        return T("Droites perpendiculaires et parallèles",
          f"Tracer une droite ({d}) et un point {P} qui n'appartient pas à ({d}).\n1) Tracer la droite ({a}) passant par {P} et perpendiculaire à ({d}).\n2) Tracer la droite ({b}) passant par {P} et parallèle à ({d}).\n3) Que peut-on dire des droites ({a}) et ({b}) ? Justifier.",
          f"1) et 2) Construction à l'équerre et à la règle.\n3) ({b}) // ({d}) et ({a}) ⊥ ({d}) ; si deux droites sont parallèles, toute droite perpendiculaire à l'une est perpendiculaire à l'autre : ({a}) ⊥ ({b}).")
    if v == 1:
        return T("Deux droites perpendiculaires à une même droite",
          f"Tracer une droite ({d}). Placer deux points {P} et Q hors de ({d}).\n1) Tracer ({a}) passant par {P} et perpendiculaire à ({d}).\n2) Tracer ({b}) passant par Q et perpendiculaire à ({d}).\n3) Quelle est la position relative de ({a}) et ({b}) ? Justifier.",
          f"3) ({a}) et ({b}) sont perpendiculaires à la même droite ({d}) : elles sont parallèles (ou confondues si {P}, Q sont sur la même perpendiculaire).")
    return T("Deux droites parallèles à une même droite",
      f"Tracer une droite ({d}). Placer deux points {P} et Q hors de ({d}), de part et d'autre de ({d}).\n1) Tracer ({a}) passant par {P} et parallèle à ({d}).\n2) Tracer ({b}) passant par Q et parallèle à ({d}).\n3) Que peut-on dire de ({a}) et ({b}) ? Justifier.",
      f"3) Deux droites parallèles à une même droite sont parallèles entre elles : ({a}) // ({b}).")

def mediatrice(r):
    A, B = r.choice(["AB", "EF", "MN", "RS"]); L = D(r.randint(10, 18)) / 2 + 2; m = D(r.randint(9, 15)) / 2 + 2
    while m <= L / 2: m += 1
    return T("Médiatrice d'un segment",
      f"1) Tracer un segment [{A}{B}] de longueur {fmt(L)} cm et construire sa médiatrice (Δ). Elle coupe [{A}{B}] en I.\n2) Calculer {A}I.\n3) M est un point de (Δ) tel que M{A} = {fmt(m)} cm. Calculer M{B}. Justifier.\n4) Quelle est la nature du triangle M{A}{B} ?",
      f"2) I est le milieu de [{A}{B}] : {A}I = {fmt(L)} ÷ 2 = {fmt(L/2)} cm\n3) Tout point de la médiatrice est équidistant des extrémités : M{B} = M{A} = {fmt(m)} cm\n4) M{A} = M{B} : le triangle M{A}{B} est isocèle en M.")

# ———— Fractions ————
def frac_simpl(r):
    out = []; sol = []
    for _ in range(3):
        a, b = r.choice([(3, 4), (2, 3), (5, 7), (4, 9), (3, 5), (7, 8), (5, 6), (2, 5)]); k = r.randint(3, 12)
        out.append(f"{a*k}/{b*k}"); sol.append(f"{a*k}/{b*k} = {a}/{b}")
    a, b = r.choice([(2, 3), (3, 4), (5, 6)]); k = r.randint(2, 6)
    return T("Simplifier des fractions",
      f"1) Simplifier : " + " ; ".join(out) + f".\n2) Compléter : {a}/{b} = …/{b*k}  ;  {a}/{b} = {a*k+a}/…",
      "1) " + " ; ".join(sol) + f"\n2) {a}/{b} = {a*k}/{b*k} ; {a}/{b} = {a*(k+1)}/{b*(k+1)}")

def frac_comp(r):
    d = r.randint(5, 13); a, b = sorted(r.sample(range(1, d), 2))
    n = r.randint(2, 7); d1, d2 = sorted(r.sample(range(n + 1, n + 9), 2))
    p = r.randint(5, 12); q = r.randint(2, p - 1)
    m = r.choice([2, 3, 4]); dd = r.choice([3, 4, 5]); x = r.randint(1, dd - 1); y = x * m + r.choice([-1, 1])
    c4 = "<" if F(x, dd) < F(y, dd * m) else ">"
    return T("Comparer des fractions",
      f"Comparer :\na) {b}/{d} et {a}/{d}\nb) {n}/{d1} et {n}/{d2}\nc) {p}/{q} et 1\nd) {x}/{dd} et {y}/{dd*m}",
      f"a) {b}/{d} > {a}/{d} (même dénominateur)\nb) {n}/{d1} > {n}/{d2} (même numérateur : le plus petit dénominateur donne la plus grande fraction)\nc) {p}/{q} > 1 (numérateur > dénominateur)\nd) {x}/{dd} = {x*m}/{dd*m} donc {x}/{dd} {c4} {y}/{dd*m}")

def frac_add(r):
    d = r.randint(5, 11); a, b = r.randint(1, d - 2), 1; b = r.randint(1, d - a - 1)
    d2 = r.choice([10, 12, 8, 9]); c, e = sorted(r.sample(range(1, d2), 2))[::-1]
    p = r.choice([2, 3, 4]); q = r.choice([2, 3]); D1 = p * q; u = r.randint(1, p - 1) if p > 1 else 1; v = r.randint(1, D1 - 1)
    s3 = F(u, p) + F(v, D1)
    g = r.choice([4, 6, 8]); h = g // 2; w = r.randint(h + 1, g - 1) if h + 1 < g else g - 1; z = 1
    s4 = F(w, g) - F(z, h)
    return T("Addition et soustraction de fractions",
      f"Calculer et simplifier si possible :\na) {a}/{d} + {b}/{d}\nb) {c}/{d2} − {e}/{d2}\nc) {u}/{p} + {v}/{D1}\nd) {w}/{g} − {z}/{h}",
      f"a) {a+b}/{d}" + (f" = {fr(F(a+b,d))}" if F(a+b, d).denominator != d else "") +
      f"\nb) {c-e}/{d2}" + (f" = {fr(F(c-e,d2))}" if F(c-e, d2).denominator != d2 else "") +
      f"\nc) {u*q}/{D1} + {v}/{D1} = {u*q+v}/{D1}" + (f" = {fr(s3)}" if s3.denominator != D1 else "") +
      f"\nd) {w}/{g} − {2*z}/{g} = {w-2*z}/{g}" + (f" = {fr(s4)}" if s4.denominator != g else ""))

def frac_qte(r):
    a, b = r.choice([(3, 4), (2, 5), (3, 8), (5, 6), (2, 3), (4, 7)]); n = b * r.randint(5, 15)
    a2, b2 = r.choice([(1, 4), (2, 5), (3, 10), (1, 3)]); m = b2 * r.randint(20, 90) * 10
    a3, b3 = r.choice([(3, 8), (2, 5), (4, 9), (5, 12)]); c = b3 * r.randint(4, 7)
    return T("Fraction d'une quantité",
      f"1) Calculer {art(a)} {a}/{b} de {n}.\n2) Calculer {art(a2)} {a2}/{b2} de {fmt(m)} MRU.\n3) Dans une classe de {c} élèves, {art(a3)} {a3}/{b3} sont des garçons. Combien y a-t-il de garçons ? de filles ?",
      f"1) {n} ÷ {b} × {a} = {n//b*a}\n2) {fmt(m)} ÷ {b2} × {a2} = {fmt(m//b2*a2)} MRU\n3) Garçons : {c} ÷ {b3} × {a3} = {c//b3*a3} ; filles : {c} − {c//b3*a3} = {c - c//b3*a3}")

# ———— Proportionnalité ————
def prop_table(r):
    u = r.choice([45, 60, 70, 80, 120, 150]); prod = r.choice(["riz", "sucre", "lait en poudre", "dattes"])
    m1, m2, m3 = r.sample([2, 3, 4, 5, 6, 8, 10], 3); m4 = r.randint(11, 25)
    return T("Tableau de proportionnalité",
      f"Le prix du {prod} est proportionnel à la masse achetée. Compléter le tableau :\nMasse (kg) : {m1} | {m2} | {m3} | …\nPrix (MRU) : {fmt(u*m1)} | … | … | {fmt(u*m4)}\nQuel est le coefficient de proportionnalité ?",
      f"Prix d'un kilogramme : {fmt(u*m1)} ÷ {m1} = {u} MRU (coefficient {u})\n{m2} kg → {fmt(u*m2)} MRU ; {m3} kg → {fmt(u*m3)} MRU ; {fmt(u*m4)} ÷ {u} = {m4} kg")

def pourcent(r):
    p = r.choice([10, 15, 20, 25, 30]); x = r.choice([2000, 2400, 3000, 1600, 4500, 1200])
    q = r.choice([5, 10, 20]); y = r.choice([800, 1500, 2500, 3200])
    return T("Calculer un pourcentage",
      f"1) Un article coûte {fmt(x)} MRU. Le vendeur accorde une remise de {p} %. Calculer la remise puis le nouveau prix.\n2) Un loyer de {fmt(y)} MRU augmente de {q} %. Calculer le nouveau loyer.",
      f"1) Remise : {fmt(x)} × {p} ÷ 100 = {fmt(D(x*p)/100)} MRU ; nouveau prix : {fmt(x - D(x*p)/100)} MRU\n2) Augmentation : {fmt(y)} × {q} ÷ 100 = {fmt(D(y*q)/100)} MRU ; nouveau loyer : {fmt(y + D(y*q)/100)} MRU")

def pourcent_exp(r):
    tot = r.choice([400, 500, 600, 800, 1200]); p = r.choice([15, 20, 25, 35, 40, 45, 55, 60]); f = tot * p // 100
    t2 = r.choice([20, 25, 40, 50]); s2 = t2 * r.choice([2, 3, 4]) // 5 if t2 % 5 == 0 else t2 // 2
    return T("Exprimer un pourcentage",
      f"1) Un collège compte {fmt(tot)} élèves dont {fmt(f)} filles. Quel est le pourcentage de filles ? de garçons ?\n2) Un élève a obtenu {s2} bonnes réponses sur {t2} questions. Exprimer ce résultat en pourcentage.",
      f"1) Filles : {fmt(f)} ÷ {fmt(tot)} × 100 = {p} % ; garçons : 100 − {p} = {100-p} %\n2) {s2} ÷ {t2} × 100 = {fmt(D(s2*100)/t2)} %")

def echelle(r):
    e = r.choice([25000, 50000, 100000, 200000]); c = r.randint(3, 15); km = e * r.randint(2, 12) // 100000
    while km == 0: km = r.randint(2, 20)
    return T("Échelle d'une carte",
      f"Une carte est à l'échelle 1/{fmt(e)}.\n1) Deux villages sont distants de {c} cm sur la carte. Quelle est la distance réelle en km ?\n2) Deux villes sont distantes de {km} km. Quelle distance les sépare sur la carte ?",
      f"1) {c} × {fmt(e)} = {fmt(c*e)} cm = {fmt(D(c*e)/100000)} km\n2) {km} km = {fmt(km*100000)} cm ; {fmt(km*100000)} ÷ {fmt(e)} = {fmt(D(km*100000)/e)} cm")

# ———— Triangles, parallélogramme, cercle ————
def triangle_angles(r):
    A, B, C = r.choice(LET); a = r.randint(30, 80); b = r.randint(25, 120 - (a - 30))
    while a + b >= 175: b -= 10
    c = 180 - a - b
    a2 = r.choice([30, 40, 50, 70, 75, 80]); base = (180 - a2) // 2 if (180 - a2) % 2 == 0 else None
    if base is None: a2 = 40; base = 70
    nat = "rectangle" if 90 in (a, b, c) else ("isocèle" if len({a, b, c}) < 3 else "quelconque")
    return T("Somme des angles d'un triangle",
      f"1) Dans le triangle {A}{B}{C}, on a : {A}̂ = {a}° et {B}̂ = {b}°. Calculer {C}̂. Quelle est la nature du triangle ?\n2) Un triangle isocèle a un angle au sommet principal de {a2}°. Calculer les deux angles à la base.",
      f"1) {C}̂ = 180° − ({a}° + {b}°) = {c}° ; le triangle est {nat}.\n2) (180° − {a2}°) ÷ 2 = {base}° pour chacun des angles à la base.")

def triangle_nature(r):
    A, B, C = r.choice(LET); s = D(r.randint(6, 16)) / 2 + 1; t = s + 1
    k = r.randint(0, 2)
    return T("Construire un triangle et le reconnaître",
      f"1) Construire un triangle {A}{B}{C} tel que {A}{B} = {A}{C} = {fmt(s)} cm et {B}{C} = {fmt(t)} cm. Quelle est sa nature ?\n2) Construire un triangle équilatéral {C}{B}{A} de côté {fmt(t)} cm. Combien mesure chacun de ses angles ?\n3) Calculer le périmètre de chacun des deux triangles.",
      f"1) {A}{B} = {A}{C} : triangle isocèle en {A}.\n2) Chaque angle mesure 60°.\n3) Périmètres : {fmt(s)} + {fmt(s)} + {fmt(t)} = {fmt(2*s+t)} cm et 3 × {fmt(t)} = {fmt(3*t)} cm")

def parallelogramme(r):
    A, B, C, Dd = r.choice(["ABCD", "EFGH", "MNPQ", "RSTU"]); a = D(r.randint(8, 18)) / 2 + 1; b = D(r.randint(6, 14)) / 2 + 1
    al = r.choice([50, 55, 60, 65, 70, 75, 110, 120])
    return T("Le parallélogramme",
      f"{A}{B}{C}{Dd} est un parallélogramme tel que {A}{B} = {fmt(a)} cm, {B}{C} = {fmt(b)} cm et ∠{Dd}{A}{B} = {al}°.\n1) Calculer {C}{Dd} et {A}{Dd}. Justifier.\n2) Calculer le périmètre du parallélogramme.\n3) Calculer les angles ∠{A}{B}{C}, ∠{B}{C}{Dd} et ∠{C}{Dd}{A}.\n4) Les diagonales se coupent en O. Que représente O pour [{A}{C}] et [{B}{Dd}] ?",
      f"1) Les côtés opposés ont même longueur : {C}{Dd} = {fmt(a)} cm ; {A}{Dd} = {fmt(b)} cm\n2) 2 × ({fmt(a)} + {fmt(b)}) = {fmt(2*(a+b))} cm\n3) Deux angles consécutifs sont supplémentaires : ∠{A}{B}{C} = 180° − {al}° = {180-al}° ; angles opposés égaux : ∠{B}{C}{Dd} = {al}° ; ∠{C}{Dd}{A} = {180-al}°\n4) O est le milieu commun des deux diagonales.")

def cercle(r):
    v = r.randint(0, 2)
    if v == 0:
        rr = D(r.randint(2, 12)) if r.random() < .6 else D(r.randint(5, 25)) / 2
        return T("Cercle et disque",
          f"Un cercle a pour rayon {fmt(rr)} cm. On prend π ≈ 3,14.\n1) Calculer son diamètre.\n2) Calculer son périmètre.\n3) Calculer l'aire du disque correspondant.",
          f"1) d = 2 × {fmt(rr)} = {fmt(2*rr)} cm\n2) P = 2 × 3,14 × {fmt(rr)} = {fmt(2*PI*rr)} cm\n3) A = 3,14 × {fmt(rr)} × {fmt(rr)} = {fmt(PI*rr*rr)} cm²")
    if v == 1:
        d = D(r.randint(4, 30))
        return T("Périmètre et aire à partir du diamètre",
          f"Une roue de vélo a un diamètre de {fmt(d)} cm. On prend π ≈ 3,14.\n1) Calculer son rayon.\n2) Quelle distance parcourt la roue en un tour ?\n3) Quelle distance parcourt-elle en 100 tours (en m) ?",
          f"1) r = {fmt(d)} ÷ 2 = {fmt(d/2)} cm\n2) P = 3,14 × {fmt(d)} = {fmt(PI*d)} cm\n3) 100 × {fmt(PI*d)} = {fmt(PI*d*100)} cm = {fmt(PI*d)} m")
    rr = D(r.randint(2, 15)); P = 2 * PI * rr
    return T("Retrouver le rayon",
      f"Un cercle a un périmètre de {fmt(P)} cm (π ≈ 3,14).\n1) Calculer son rayon.\n2) Calculer l'aire du disque.",
      f"1) r = {fmt(P)} ÷ (2 × 3,14) = {fmt(rr)} cm\n2) A = 3,14 × {fmt(rr)} × {fmt(rr)} = {fmt(PI*rr*rr)} cm²")

# ———— Statistique ————
def stats(r):
    notes = r.sample([6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17], 5); notes.sort()
    while True:
        eff = [r.randint(1, 8) for _ in notes]
        if sum(eff) in (20, 25): break
    n = sum(eff); serie = [x for x, e in zip(notes, eff) for _ in range(e)]; r.shuffle(serie)
    tot = sum(x * e for x, e in zip(notes, eff)); moy = D(tot) / n
    mode = [x for x, e in zip(notes, eff) if e == max(eff)]
    seuil = notes[2]; nb = sum(e for x, e in zip(notes, eff) if x >= seuil)
    return T("Statistique : notes d'un devoir",
      f"Voici les notes de {n} élèves :\n" + " ; ".join(map(str, serie)) +
      f"\n1) Dresser le tableau des effectifs.\n2) Calculer la fréquence de chaque note en pourcentage.\n3) Quelle est la (les) note(s) la (les) plus fréquente(s) ?\n4) Calculer la moyenne de la classe.\n5) Quel pourcentage d'élèves a obtenu au moins {seuil} ?",
      "1) " + " ; ".join(f"{x} : {e}" for x, e in zip(notes, eff)) + f" (total {n})" +
      "\n2) " + " ; ".join(f"{x} : {fmt(D(e*100)/n)} %" for x, e in zip(notes, eff)) +
      f"\n3) {' et '.join(map(str, mode))}" +
      f"\n4) (" + " + ".join(f"{x}×{e}" for x, e in zip(notes, eff)) + f") ÷ {n} = {tot} ÷ {n} = {fmt(rnd(moy,2))}" +
      f"\n5) {nb} élèves sur {n}, soit {fmt(rnd(D(nb*100)/n,1))} %")

def stats_diag(r):
    jours = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"]; v = [r.randint(2, 12) for _ in jours]
    tot = sum(v)
    return T("Lecture et représentation de données",
      f"Le nombre d'absences dans un collège pendant une semaine est :\n" + " ; ".join(f"{j} : {x}" for j, x in zip(jours, v)) +
      "\n1) Calculer le nombre total d'absences.\n2) Représenter ces données par un diagramme en bâtons.\n3) Calculer la moyenne d'absences par jour.\n4) Quel jour a le plus d'absences ? Quel pourcentage du total représente-t-il ?",
      f"1) {tot} absences\n2) Un bâton par jour, de hauteur égale au nombre d'absences.\n3) {tot} ÷ 5 = {fmt(D(tot)/5)}\n4) {jours[v.index(max(v))]} ({max(v)}) : {max(v)} ÷ {tot} × 100 ≈ {fmt(rnd(D(max(v)*100)/tot,1))} %")

# ———— Cube et pavé droit ————
def pave(r):
    L, l, h = r.randint(4, 15), r.randint(3, 10), r.randint(2, 9)
    V = L * l * h; At = 2 * (L * l + L * h + l * h)
    return T("Le pavé droit",
      f"Un pavé droit a pour dimensions : longueur {L} cm, largeur {l} cm, hauteur {h} cm.\n1) Combien a-t-il de faces, d'arêtes et de sommets ?\n2) Calculer son volume.\n3) Calculer l'aire totale de ses faces.\n4) Convertir le volume en dm³.",
      f"1) 6 faces, 12 arêtes, 8 sommets.\n2) V = {L} × {l} × {h} = {fmt(V)} cm³\n3) A = 2 × ({L}×{l} + {L}×{h} + {l}×{h}) = 2 × ({L*l} + {L*h} + {l*h}) = {fmt(At)} cm²\n4) {fmt(V)} cm³ = {fmt(D(V)/1000)} dm³")

def cube(r):
    a = r.randint(3, 12)
    return T("Le cube",
      f"Un cube a une arête de {a} cm.\n1) Calculer la longueur totale de ses arêtes.\n2) Calculer l'aire d'une face, puis l'aire totale.\n3) Calculer son volume.\n4) On double l'arête. Par combien est multiplié le volume ?",
      f"1) 12 × {a} = {12*a} cm\n2) Face : {a} × {a} = {a*a} cm² ; totale : 6 × {a*a} = {6*a*a} cm²\n3) V = {a}³ = {a**3} cm³\n4) Nouvelle arête {2*a} cm : V = {(2*a)**3} cm³, le volume est multiplié par 8.")

# ———— Situations d'intégration ————
def sit_commerce(r):
    n = r.choice([12, 16, 20, 24, 40]); pa = r.choice([1150, 1200, 1250, 1300]); tr = r.choice([400, 500, 600, 800])
    pv = pa + r.choice([100, 150, 200]); a, b = r.choice([(3, 4), (1, 2), (5, 8), (3, 8)])
    cost = n * pa + tr; sold = n * a // b; rec = n * pv; ben = rec - cost
    return T("Situation d'intégration : le commerçant",
      f"Un commerçant achète {n} sacs de riz à {fmt(pa)} MRU le sac. Le transport lui coûte {tr} MRU. Il revend chaque sac à {fmt(pv)} MRU.\n1) Calculer le coût total de l'achat.\n2) Le premier mois, il vend {art(a)} {a}/{b} des sacs. Combien de sacs a-t-il vendus ? Quelle somme a-t-il encaissée ?\n3) Il vend ensuite le reste au même prix. Calculer son bénéfice total.\n4) Exprimer ce bénéfice en pourcentage du coût total (arrondi au dixième).",
      f"1) {n} × {fmt(pa)} + {tr} = {fmt(n*pa)} + {tr} = {fmt(cost)} MRU\n2) {n} × {a}/{b} = {sold} sacs ; {sold} × {fmt(pv)} = {fmt(sold*pv)} MRU\n3) Recette : {n} × {fmt(pv)} = {fmt(rec)} MRU ; bénéfice : {fmt(rec)} − {fmt(cost)} = {fmt(ben)} MRU\n4) {fmt(ben)} ÷ {fmt(cost)} × 100 ≈ {fmt(rnd(D(ben*100)/cost,1))} %")

def sit_jardin(r):
    L, l = r.randint(22, 40), r.randint(8, 20); pm = r.choice([150, 200, 250]); a, b = r.choice([(2, 5), (3, 8), (1, 4), (3, 5)])
    while (L * l) % b: L += 1
    P = 2 * (L + l); A = L * l
    return T("Situation d'intégration : le jardin de l'école",
      f"Le jardin de l'école est un rectangle de {L} m de long et {l} m de large.\n1) Calculer son périmètre et son aire.\n2) On veut l'entourer d'un grillage qui coûte {pm} MRU le mètre. Calculer la dépense.\n3) {art(a).capitalize()} {a}/{b} de la surface sont plantés de légumes. Calculer l'aire plantée.\n4) Quel pourcentage de la surface reste non planté ?",
      f"1) P = 2 × ({L} + {l}) = {P} m ; A = {L} × {l} = {A} m²\n2) {P} × {pm} = {fmt(P*pm)} MRU\n3) {A} × {a}/{b} = {A*a//b} m²\n4) {b-a}/{b} = {fmt(D((b-a)*100)/b)} %")

def sit_reservoir(r):
    L, l, h = D(r.randint(15, 30)) / 10, D(r.randint(10, 20)) / 10, D(r.randint(8, 15)) / 10
    V = L * l * h; Lit = V * 1000; p = r.choice([60, 75, 80, 90]); c = r.choice([120, 150, 200, 250])
    vol = Lit * p / 100; jours = int(vol // c)
    return T("Situation d'intégration : le réservoir d'eau",
      f"Le réservoir d'eau d'une famille a la forme d'un pavé droit de {fmt(L)} m × {fmt(l)} m × {fmt(h)} m.\n1) Calculer son volume en m³ puis en litres (1 m³ = 1 000 L).\n2) Il est rempli à {p} %. Quelle quantité d'eau contient-il ?\n3) La famille consomme {c} L par jour. Pendant combien de jours entiers peut-elle tenir sans nouvel apport ?",
      f"1) V = {fmt(L)} × {fmt(l)} × {fmt(h)} = {fmt(V)} m³ = {fmt(Lit)} L\n2) {fmt(Lit)} × {p} ÷ 100 = {fmt(vol)} L\n3) {fmt(vol)} ÷ {c} {"=" if vol % c == 0 else "≈"} {fmt(rnd(vol/c,2))} : {jours} jours entiers")

def sit_bassin(r):
    rr = D(r.randint(3, 9)); pm = r.choice([300, 400, 500]); P = 2 * PI * rr; A = PI * rr * rr
    return T("Situation d'intégration : le bassin circulaire",
      f"Une place publique possède un bassin circulaire de {fmt(rr)} m de rayon (π ≈ 3,14).\n1) Calculer la longueur du bord du bassin.\n2) On pose une bordure qui coûte {pm} MRU le mètre. Calculer le prix de la bordure.\n3) Calculer l'aire du fond du bassin.",
      f"1) 2 × 3,14 × {fmt(rr)} = {fmt(P)} m\n2) {fmt(P)} × {pm} = {fmt(P*pm)} MRU\n3) 3,14 × {fmt(rr)} × {fmt(rr)} = {fmt(A)} m²")

def sit_budget(r):
    S = r.choice([12000, 15000, 18000, 24000, 30000]); a, b = r.choice([(1, 3), (2, 5), (1, 4)]); c, d = r.choice([(1, 6), (1, 5), (1, 10)])
    p = r.choice([10, 15, 20])
    nour = S * a // b; loy = S * c // d; ep = S * p // 100; reste = S - nour - loy - ep
    return T("Situation d'intégration : le budget familial",
      f"Le revenu mensuel d'une famille est de {fmt(S)} MRU. Elle dépense {art(a)} {a}/{b} pour la nourriture, le {c}/{d} pour le loyer et épargne {p} % du revenu.\n1) Calculer chacune de ces sommes.\n2) Quelle somme reste-t-il pour les autres dépenses ?\n3) Quelle fraction du revenu cette somme représente-t-elle ?",
      f"1) Nourriture : {fmt(S)} × {a}/{b} = {fmt(nour)} MRU ; loyer : {fmt(S)} × {c}/{d} = {fmt(loy)} MRU ; épargne : {fmt(S)} × {p} ÷ 100 = {fmt(ep)} MRU\n2) {fmt(S)} − ({fmt(nour)} + {fmt(loy)} + {fmt(ep)}) = {fmt(reste)} MRU\n3) {fmt(reste)}/{fmt(S)} = {fr(F(reste, S))}")
