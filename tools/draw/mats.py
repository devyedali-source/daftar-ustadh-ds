from common import *

def m1_comparer():
    f, ax = fig(7, 1.6, (-0.8, 11), (-0.8, 0.9))
    numline(ax, 0, 10)
    pt(ax, (3, 0), 'A', color=BLUE); pt(ax, (7, 0), 'B', color=RED)
    ax.text(5, 0.6, '3 < 7 : le point A est à gauche du point B', ha='center', fontsize=12)
    save(f, 'MATS_1AS_comparer')

def m1_droites():
    f, ax = fig(7, 2.6, (-0.5, 9.5), (-1.6, 1.4))
    y = 0.8; seg(ax, (0.5, y), (4, y)); pt(ax, (0.5, y), 'A', (0, 0.3)); pt(ax, (2.5, y), 'B', (0, 0.3))
    arrow(ax, (3.6, y), (4.3, y)); ax.text(6.5, y, 'demi-droite [AB)', va='center', color=BLUE)
    y = -0.8; seg(ax, (0.2, y), (4, y)); arrow(ax, (3.6, y), (4.3, y)); arrow(ax, (0.6, y), (-0.1, y))
    pt(ax, (1.2, y), 'A', (0, 0.3)); pt(ax, (2.8, y), 'B', (0, 0.3)); ax.text(6.5, y, 'droite (AB)', va='center', color=BLUE)
    save(f, 'MATS_1AS_demidroite')

def m1_encadrer():
    f, ax = fig(7, 1.8, (2.6, 4.5), (-0.8, 0.9))
    seg(ax, (2.8, 0), (4.2, 0)); arrow(ax, (4.15, 0), (4.35, 0))
    for k in range(11):
        v = 3 + k / 10; h = 0.14 if k in (0, 5, 10) else 0.07
        seg(ax, (v, -h), (v, h), lw=1.3 if h > 0.1 else 0.9)
    for v, t in [(3, '3'), (4, '4')]: ax.text(v, -0.38, t, ha='center')
    pt(ax, (3.5, 0), None, color=RED); ax.text(3.5, -0.38, '3,5', ha='center', color=RED)
    ax.text(3.5, 0.6, '3 < 3,5 < 4   (encadrement à l’unité près)', ha='center', fontsize=12)
    save(f, 'MATS_1AS_encadrer')

def frac_rect(ax, x, y, w, h, n, k, color):
    for i in range(n):
        ax.add_patch(Rectangle((x + i * w / n, y), w / n, h, fc=color if i < k else 'white', ec='black', lw=1.4))

def m1_fractions_egales():
    f, ax = fig(7, 1.8, (-0.2, 10.4), (-0.9, 1.3))
    frac_rect(ax, 0, 0, 4, 1, 2, 1, '#90CAF9'); ax.text(2, -0.5, r'$\frac{1}{2}$', ha='center', fontsize=17)
    ax.text(5.1, 0.5, '=', ha='center', va='center', fontsize=20)
    frac_rect(ax, 6.2, 0, 4, 1, 4, 2, '#90CAF9'); ax.text(8.2, -0.5, r'$\frac{2}{4} = \frac{1\times 2}{2\times 2}$', ha='center', fontsize=15)
    save(f, 'MATS_1AS_fractions_egales')

def m1_aire_triangle():
    f, ax = fig(5, 3, (-0.5, 6.5), (-0.9, 3.6))
    A, B, C, H = (2, 3), (0, 0), (6, 0), (2, 0)
    ax.add_patch(Polygon([A, B, C], closed=True, fc='#FFF3E0', ec='black', lw=1.8))
    seg(ax, A, H, RED, 1.6, '--'); right_angle(ax, H, (1, 0), (0, 1), 0.25, RED)
    pt(ax, A, 'A', (0, 0.3)); pt(ax, B, 'B', (-0.3, -0.1)); pt(ax, C, 'C', (0.3, -0.1)); pt(ax, H, 'H', (0, -0.35), RED)
    ax.text(2.25, 1.4, 'hauteur', color=RED, fontsize=11); ax.text(4, -0.6, 'base', ha='center', color=BLUE)
    ax.text(3, -1.25, 'Aire = (BC × AH) / 2', ha='center', fontsize=13)
    save(f, 'MATS_1AS_aire_triangle')

def m1_parallelogrammes():
    f, ax = fig(8, 2.6, (-0.3, 12.3), (-1.1, 2.6))
    R = [(0, 0), (3, 0), (3, 2), (0, 2)]
    ax.add_patch(Polygon(R, closed=True, fc='#E3F2FD', ec='black', lw=1.6))
    for v, d1, d2 in [((0, 0), (1, 0), (0, 1)), ((3, 0), (-1, 0), (0, 1)), ((3, 2), (-1, 0), (0, -1)), ((0, 2), (1, 0), (0, -1))]: right_angle(ax, v, d1, d2, 0.22)
    ax.text(1.5, -0.6, 'Rectangle', ha='center', color=BLUE)
    L = [(5.6, 1), (6.9, 0), (8.2, 1), (6.9, 2)]
    ax.add_patch(Polygon(L, closed=True, fc='#E8F5E9', ec='black', lw=1.6))
    for i in range(4): tick_code(ax, L[i], L[(i + 1) % 4], 1)
    ax.text(6.9, -0.6, 'Losange', ha='center', color=GREEN)
    S = [(9.6, 0), (11.6, 0), (11.6, 2), (9.6, 2)]
    ax.add_patch(Polygon(S, closed=True, fc='#FFF3E0', ec='black', lw=1.6))
    for i in range(4): tick_code(ax, S[i], S[(i + 1) % 4], 1)
    right_angle(ax, (9.6, 0), (1, 0), (0, 1), 0.22)
    ax.text(10.6, -0.6, 'Carré', ha='center', color=ORANGE)
    save(f, 'MATS_1AS_parallelogrammes')

def m1_pourcentage():
    f, ax = fig(4.2, 4.2, (-0.3, 10.3), (-1.6, 10.3))
    for i in range(10):
        for j in range(10):
            k = j * 10 + i
            ax.add_patch(Rectangle((i, 9 - j), 1, 1, fc='#EF9A9A' if k < 40 else 'white', ec=GRAY, lw=0.8))
    ax.add_patch(Rectangle((0, 0), 10, 10, fill=False, ec='black', lw=1.6))
    ax.text(5, -0.9, '40 carreaux sur 100 : 40 %', ha='center', fontsize=13)
    save(f, 'MATS_1AS_pourcentage')

def m1_cavaliere():
    f, ax = fig(4.5, 4.4, (-0.5, 5.2), (-0.8, 5.0))
    a = 3; dx, dy = 1.2, 1.2
    F = [(0, 0), (a, 0), (a, a), (0, a)]
    ax.add_patch(Polygon(F, closed=True, fc='#E3F2FD', ec='black', lw=1.8))
    B = [(x + dx, y + dy) for x, y in F]
    for i in (1, 2, 3): seg(ax, F[i], B[i])
    seg(ax, B[1], B[2]); seg(ax, B[2], B[3])
    seg(ax, F[0], B[0], ls='--', lw=1.4); seg(ax, B[0], B[1], ls='--', lw=1.4); seg(ax, B[0], B[3], ls='--', lw=1.4)
    ax.text(1.5, -0.45, 'face avant en vraie grandeur', ha='center', fontsize=10, color=BLUE)
    ax.text(3.9, 0.35, 'fuyante', fontsize=10, color=RED, rotation=45)
    ax.text(2.6, 4.65, 'arêtes cachées en pointillés', ha='center', fontsize=10, color=GRAY)
    save(f, 'MATS_1AS_cube_cavaliere')

def m2_addition():
    f, ax = fig(7.5, 2.2, (-6.8, 5.8), (-0.8, 1.9))
    numline(ax, -6, 5)
    arrow(ax, (0, 0.45), (3, 0.45), BLUE); ax.text(1.5, 0.75, '+3', ha='center', color=BLUE)
    arrow(ax, (3, 1.05), (-2, 1.05), RED); ax.text(0.5, 1.35, '−5', ha='center', color=RED)
    pt(ax, (-2, 0), None, color=RED)
    ax.text(-4.6, 1.45, '(+3) + (−5) = −2', ha='center', fontsize=13)
    save(f, 'MATS_2AS_addition_relatifs')

def m2_identite():
    f, ax = fig(4.4, 4.4, (-0.8, 5.4), (-0.8, 5.4))
    a, b = 3, 1.6
    ax.add_patch(Rectangle((0, b), a, a, fc='#90CAF9', ec='black', lw=1.5)); ax.text(a / 2, b + a / 2, r'$a^2$', ha='center', va='center', fontsize=17)
    ax.add_patch(Rectangle((a, b), b, a, fc='#C5E1A5', ec='black', lw=1.5)); ax.text(a + b / 2, b + a / 2, r'$ab$', ha='center', va='center', fontsize=15)
    ax.add_patch(Rectangle((0, 0), a, b, fc='#C5E1A5', ec='black', lw=1.5)); ax.text(a / 2, b / 2, r'$ab$', ha='center', va='center', fontsize=15)
    ax.add_patch(Rectangle((a, 0), b, b, fc='#FFCC80', ec='black', lw=1.5)); ax.text(a + b / 2, b / 2, r'$b^2$', ha='center', va='center', fontsize=15)
    ax.text(a / 2, a + b + 0.3, 'a', ha='center'); ax.text(a + b / 2, a + b + 0.3, 'b', ha='center')
    ax.text(-0.35, b + a / 2, 'a', va='center'); ax.text(-0.35, b / 2, 'b', va='center')
    ax.text((a + b) / 2, -0.5, r'$(a+b)^2 = a^2 + 2ab + b^2$', ha='center', fontsize=14)
    save(f, 'MATS_identite_remarquable')

def m2_axe():
    f, ax = fig(7.5, 1.9, (-4.8, 5.8), (-0.9, 1.1))
    numline(ax, -4, 5)
    pt(ax, (0, 0), 'O', (0, 0.32)); pt(ax, (1, 0), 'I', (0, 0.32))
    pt(ax, (-3, 0), 'M', (0, 0.32), RED); pt(ax, (4, 0), 'N', (0, 0.32), BLUE)
    ax.text(0.5, 0.8, 'MN = x$_N$ − x$_M$ = 4 − (−3) = 7', ha='center', fontsize=12)
    save(f, 'MATS_2AS_axe')

def m2_vecteurs():
    f, ax = fig(8, 3, (-0.4, 11), (-0.8, 3.3))
    A, B, C = (0, 0), (2.5, 2.4), (4.6, 0.4)
    arrow(ax, A, B, BLUE); arrow(ax, B, C, RED); arrow(ax, A, C, GREEN, 2.2)
    for P, n, o in [(A, 'A', (-0.25, -0.2)), (B, 'B', (0, 0.3)), (C, 'C', (0.3, -0.1))]: pt(ax, P, n, o, size=3)
    ax.text(2.3, -0.55, 'AB⃗ + BC⃗ = AC⃗  (Chasles)', ha='center', fontsize=12)
    A2, B2, D2 = (6.3, 0), (9.3, 0), (7.3, 2.2); C2 = (B2[0] + D2[0] - A2[0], D2[1])
    seg(ax, B2, C2, GRAY, 1.2, '--'); seg(ax, D2, C2, GRAY, 1.2, '--')
    arrow(ax, A2, B2, BLUE); arrow(ax, A2, D2, RED); arrow(ax, A2, C2, GREEN, 2.2)
    for P, n, o in [(A2, 'A', (-0.25, -0.2)), (B2, 'B', (0.25, -0.2)), (D2, 'D', (-0.2, 0.3)), (C2, 'C', (0.25, 0.25))]: pt(ax, P, n, o, size=3)
    ax.text(8.5, -0.55, 'AB⃗ + AD⃗ = AC⃗  (parallélogramme)', ha='center', fontsize=12)
    save(f, 'MATS_vecteurs_somme')

def m2_polygone():
    f, ax = fig(4.5, 3.6, (-2.6, 2.8), (-2.4, 2.4))
    P = [(np.cos(np.pi / 2 + k * 2 * np.pi / 5) * 1.8, np.sin(np.pi / 2 + k * 2 * np.pi / 5) * 1.8) for k in range(5)]
    ax.add_patch(Polygon(P, closed=True, fc='#E3F2FD', ec='black', lw=1.8))
    seg(ax, P[0], P[2], RED, 1.6, '--')
    for p, n in zip(P, 'ABCDE'):
        pt(ax, p, n, (p[0] * 0.15, p[1] * 0.15), size=4)
    ax.text(-1.55, 0.2, 'diagonale\n[AC]', color=RED, fontsize=11, ha='center')
    ax.text(0, -2.2, 'pentagone : 5 sommets, 5 côtés', ha='center', fontsize=12)
    save(f, 'MATS_2AS_polygone')

def m2_projection():
    f, ax = fig(7, 3, (-0.5, 9.5), (-1, 3.2))
    seg(ax, (-0.3, 0), (9.2, 0), lw=2); ax.text(9.2, 0.25, 'D', fontsize=13)
    pts = [((1, 2.4), 'A'), ((4, 1.6), 'M'), ((7.6, 0.6), 'B')]
    for (x, y), n in pts:
        seg(ax, (x, y), (x, 0), BLUE, 1.2, '--'); right_angle(ax, (x, 0), (1, 0), (0, 1), 0.2, BLUE)
        pt(ax, (x, y), n, (0, 0.3)); pt(ax, (x, 0), n + '′', (0, -0.35), BLUE)
    seg(ax, (1, 2.4), (7.6, 0.6), GRAY, 1.2)
    ax.text(4.3, -0.85, 'M milieu de [AB]  ⟹  M′ milieu de [A′B′]', ha='center', fontsize=12)
    save(f, 'MATS_2AS_projection')

def repere(ax, xr, yr, grid=True):
    if grid:
        for x in range(xr[0], xr[1] + 1): seg(ax, (x, yr[0]), (x, yr[1]), '#E0E0E0', 0.8)
        for y in range(yr[0], yr[1] + 1): seg(ax, (xr[0], y), (xr[1], y), '#E0E0E0', 0.8)
    arrow(ax, (xr[0], 0), (xr[1] + 0.4, 0)); arrow(ax, (0, yr[0]), (0, yr[1] + 0.4))
    ax.text(-0.3, -0.35, 'O', fontsize=12)

def m2_coordonnees():
    f, ax = fig(4.5, 3.6, (-1, 5.8), (-1, 4.8))
    repere(ax, (-1, 5), (-1, 4))
    ax.text(1, -0.35, 'I', ha='center'); ax.text(-0.3, 1, 'J', va='center')
    M = (3, 2); pt(ax, M, 'M(3 ; 2)', (0.2, 0.3), RED, ha='left')
    seg(ax, M, (3, 0), RED, 1.3, '--'); seg(ax, M, (0, 2), RED, 1.3, '--')
    ax.text(3, -0.4, '3', ha='center', color=RED); ax.text(-0.35, 2, '2', va='center', color=RED)
    save(f, 'MATS_2AS_coordonnees')

def m2_inegalite():
    f, ax = fig(7.5, 2.6, (-0.4, 11), (-1, 2.6))
    A, B, C = (0, 0), (2, 2), (4.5, 0)
    ax.add_patch(Polygon([A, B, C], closed=True, fill=False, ec='black', lw=1.8))
    for P, n, o in [(A, 'A', (-0.25, -0.2)), (B, 'B', (0, 0.3)), (C, 'C', (0.25, -0.2))]: pt(ax, P, n, o, size=4)
    ax.text(2.2, -0.6, 'AC < AB + BC', ha='center', fontsize=13)
    A2, B2, C2 = (6, 0.6), (8, 0.6), (10.5, 0.6)
    seg(ax, A2, C2)
    for P, n in [(A2, 'A'), (B2, 'B'), (C2, 'C')]: pt(ax, P, n, (0, 0.32), size=4)
    ax.text(8.2, -0.6, 'B ∈ [AC] : AC = AB + BC', ha='center', fontsize=13)
    save(f, 'MATS_2AS_inegalite_triangulaire')

def m2_distance():
    f, ax = fig(6, 3, (-0.5, 8.5), (-0.9, 3.2))
    seg(ax, (-0.3, 0), (8.2, 0), lw=2); ax.text(8.2, 0.25, 'D')
    A, H, M = (3, 2.6), (3, 0), (6.4, 0)
    seg(ax, A, H, RED, 2); right_angle(ax, H, (1, 0), (0, 1), 0.22, RED)
    seg(ax, A, M, GRAY, 1.4, '--')
    pt(ax, A, 'A', (0, 0.3)); pt(ax, H, 'H', (0, -0.38), RED); pt(ax, M, 'M', (0, -0.38))
    ax.text(2.3, 1.3, 'AH', color=RED, fontsize=12); ax.text(5.1, 1.5, 'AM', color=GRAY, fontsize=12)
    ax.text(4, -0.85, 'AH < AM : AH est la distance de A à D', ha='center', fontsize=12)
    save(f, 'MATS_2AS_distance_droite')

def sym_central(name, title):
    f, ax = fig(6, 2.4, (-0.4, 8.4), (-0.9, 2.4))
    A, O = (0.5, 1.6), (4, 0.6); A2 = (2 * O[0] - A[0], 2 * O[1] - A[1])
    seg(ax, A, A2, BLUE, 1.3, '--')
    tick_code(ax, A, O, 2, color=BLUE); tick_code(ax, O, A2, 2, color=BLUE)
    pt(ax, A, 'A', (-0.1, 0.32)); pt(ax, O, 'O', (0, 0.35), RED); pt(ax, A2, 'A′', (0.1, -0.35))
    ax.text(4, -0.75, title, ha='center', fontsize=12)
    save(f, name)

def m2_translation():
    f, ax = fig(6.5, 3.2, (-0.5, 9.5), (-0.6, 4.2))
    T = [(0.5, 0.5), (2.5, 0.5), (1.2, 2.2)]; v = (4.5, 1.4)
    T2 = [(x + v[0], y + v[1]) for x, y in T]
    ax.add_patch(Polygon(T, closed=True, fc='#E3F2FD', ec='black', lw=1.6))
    ax.add_patch(Polygon(T2, closed=True, fc='#FFE0B2', ec='black', lw=1.6))
    for p, q, n in zip(T, T2, 'MNP'):
        arrow(ax, p, q, GRAY, 1.1, ls='--'); pt(ax, p, n, (-0.2, -0.25), size=3); pt(ax, q, n + '′', (0.25, -0.25), size=3)
    arrow(ax, (0.5, 3.6), (0.5 + v[0], 3.6 + v[1] * 0), RED, 2)
    ax.text(2.75, 3.95, 'vecteur AB⃗', ha='center', color=RED)
    save(f, 'MATS_2AS_translation')

def m2_boule():
    f, ax = fig(4, 4, (-2.4, 2.4), (-2.5, 2.4))
    ax.add_patch(Circle((0, 0), 2, fc='#E3F2FD', ec='black', lw=1.8))
    ax.add_patch(Ellipse((0, 0), 4, 0.9, fill=False, ec=GRAY, lw=1.2, ls='--'))
    ax.add_patch(Ellipse((0, 1), 2 * np.sqrt(3), 0.6, fc='#FFCC80', ec=ORANGE, lw=1.4, alpha=0.9))
    pt(ax, (0, 0), 'O', (-0.2, -0.2)); seg(ax, (0, 0), (2, 0), RED, 1.6); ax.text(1, 0.2, 'r', color=RED)
    ax.text(0, 1.55, 'section : un disque', ha='center', fontsize=10, color=ORANGE)
    ax.text(0, -2.35, 'Boule B(O ; r) : OM ≤ r', ha='center', fontsize=12)
    save(f, 'MATS_2AS_boule')

def m3_ensembles():
    f, ax = fig(7, 3.8, (-0.2, 10.2), (-0.9, 5))
    names = ['ℝ', 'ℚ', '𝔻', 'ℤ', 'ℕ']; cols = ['#E3F2FD', '#E8F5E9', '#FFF8E1', '#FCE4EC', '#EDE7F6']
    ex = ['√2 ; π', '1/3 ; −2/7', '0,5 ; −3,25', '−4 ; −1', '0 ; 1 ; 2']
    for i, (n, c, e) in enumerate(zip(names, cols, ex)):
        w, h = 10 - i * 1.6, 4.8 - i * 0.85
        ax.add_patch(Ellipse((5 + i * 0.8, 2.4 - i * 0.05), w, h, fc=c, ec='black', lw=1.3))
        x = 5 + i * 0.8 - w / 2 + 0.55
        ax.text(x, 2.4 + 0.35, n, fontsize=15, fontweight='bold', va='center')
        ax.text(x, 2.4 - 0.3, e, fontsize=8.5, va='center', color=GRAY)
    ax.text(5, -0.6, 'ℕ ⊂ ℤ ⊂ 𝔻 ⊂ ℚ ⊂ ℝ', ha='center', fontsize=13)
    save(f, 'MATS_3AS_ensembles')

def m3_valeur_absolue():
    f, ax = fig(7.5, 2.2, (-5.6, 5.8), (-0.9, 1.6))
    numline(ax, -5, 5)
    pt(ax, (-3, 0), None, color=RED); pt(ax, (3, 0), None, color=RED)
    arrow(ax, (0, 0.5), (3, 0.5), BLUE, style='<|-|>'); arrow(ax, (0, 0.5), (-3, 0.5), BLUE, style='<|-|>')
    ax.text(1.5, 0.8, '3', ha='center', color=BLUE); ax.text(-1.5, 0.8, '3', ha='center', color=BLUE)
    ax.text(0, 1.35, '|3| = |−3| = 3 : distance à zéro', ha='center', fontsize=12)
    save(f, 'MATS_3AS_valeur_absolue')

def m3_systeme():
    f, ax = fig(4.8, 4.8, (-1.5, 13), (-1.5, 13))
    for x in range(0, 13, 2): seg(ax, (x, 0), (x, 12.5), '#EEEEEE', 0.8)
    for y in range(0, 13, 2): seg(ax, (0, y), (12.5, y), '#EEEEEE', 0.8)
    arrow(ax, (-0.5, 0), (12.8, 0)); arrow(ax, (0, -0.5), (0, 12.8))
    for v in range(2, 13, 2): ax.text(v, -0.7, str(v), ha='center', fontsize=9); ax.text(-0.7, v, str(v), va='center', ha='center', fontsize=9)
    xs = np.array([0, 12]); ax.plot(xs, (20 - xs) / 2, color=BLUE, lw=2)
    xs2 = np.array([5, 11.2]); ax.plot(xs2, 2 * xs2 - 10, color=RED, lw=2)
    ax.text(0.5, 10.6, 'x + 2y = 20', color=BLUE, fontsize=11); ax.text(9.4, 12.3, '2x − y = 10', color=RED, fontsize=11)
    seg(ax, (8, 6), (8, 0), GRAY, 1, '--'); seg(ax, (8, 6), (0, 6), GRAY, 1, '--')
    pt(ax, (8, 6), '(8 ; 6)', (1.6, 0.9), GREEN, 6)
    save(f, 'MATS_3AS_systeme')

def m3_probleme():
    f, ax = fig(8, 1.6, (-0.2, 16.2), (-1, 1))
    steps = ['1) Choix de\nl’inconnue', '2) Mise en\néquation', '3) Résolution', '4) Vérification\net conclusion']
    for i, s in enumerate(steps):
        box(ax, (2 + i * 4, 0), 3.4, 1.5, s, fs=10.5)
        if i < 3: arrow(ax, (3.75 + i * 4, 0), (4.25 + i * 4, 0))
    save(f, 'MATS_3AS_probleme')

def m3_angle_centre():
    f, ax = fig(4, 4, (-2.6, 2.6), (-2.5, 2.7))
    ax.add_patch(Circle((0, 0), 2, fill=False, ec='black', lw=1.6))
    a1, a2 = np.radians(20), np.radians(85)
    A, B = (2 * np.cos(a1), 2 * np.sin(a1)), (2 * np.cos(a2), 2 * np.sin(a2))
    seg(ax, (0, 0), A, BLUE); seg(ax, (0, 0), B, BLUE)
    ax.add_patch(Arc((0, 0), 4, 4, theta1=20, theta2=85, color=RED, lw=4))
    ax.add_patch(Arc((0, 0), 0.9, 0.9, theta1=20, theta2=85, color=BLUE, lw=1.5))
    pt(ax, (0, 0), 'O', (-0.25, -0.2)); pt(ax, A, 'A', (0.25, 0)); pt(ax, B, 'B', (0, 0.28))
    ax.text(0.45, 0.55, 'α', color=BLUE, fontsize=13); ax.text(1.75, 1.75, 'arc AB', color=RED, fontsize=11)
    ax.text(0, -2.4, 'l = π × r × α / 180  (α en degrés)', ha='center', fontsize=11)
    save(f, 'MATS_3AS_angle_centre')

def m3_cercle_rect():
    f, ax = fig(4, 4, (-2.6, 2.6), (-2.5, 2.6))
    ax.add_patch(Circle((0, 0), 2, fill=False, ec='black', lw=1.6))
    B, C = (-2, 0), (2, 0); t = np.radians(120); A = (2 * np.cos(t), 2 * np.sin(t))
    ax.add_patch(Polygon([A, B, C], closed=True, fc='#E3F2FD', ec=BLUE, lw=1.8))
    right_angle(ax, A, np.array(B) - A, np.array(C) - A, 0.3)
    seg(ax, A, (0, 0), RED, 1.4, '--')
    pt(ax, A, 'A', (-0.15, 0.3)); pt(ax, B, 'B', (-0.3, 0)); pt(ax, C, 'C', (0.3, 0)); pt(ax, (0, 0), 'O', (0, -0.3), RED)
    ax.text(0, -2.4, '[BC] hypoténuse = diamètre ; OA = BC / 2', ha='center', fontsize=10.5)
    save(f, 'MATS_3AS_triangle_cercle')

def m3_trigo():
    f, ax = fig(5, 3.6, (-0.8, 6), (-1.2, 3.8))
    A, B, C = (0, 0), (5, 0), (0, 3)
    ax.add_patch(Polygon([A, B, C], closed=True, fc='#FFF8E1', ec='black', lw=1.8))
    right_angle(ax, A, (1, 0), (0, 1), 0.3)
    ax.add_patch(Arc(B, 1.6, 1.6, theta1=180 - np.degrees(np.arctan(3 / 5)), theta2=180, color=RED, lw=1.6))
    pt(ax, A, 'A', (-0.3, -0.25), size=3); pt(ax, B, 'B', (0.3, -0.2), size=3); pt(ax, C, 'C', (-0.3, 0.2), size=3)
    ax.text(3.75, 0.2, 'B̂', color=RED, fontsize=13)
    ax.text(2.5, -0.45, 'côté adjacent à B̂', ha='center', color=BLUE, fontsize=11)
    ax.text(-0.25, 1.5, 'côté opposé', ha='right', va='center', color=GREEN, fontsize=11)
    ax.text(2.9, 1.9, 'hypoténuse', ha='center', color=RED, fontsize=11, rotation=-31)
    save(f, 'MATS_3AS_trigonometrie')

def m3_sym_axiale():
    f, ax = fig(5, 3.4, (-0.4, 7.4), (-0.8, 4.2))
    seg(ax, (3.5, -0.3), (3.5, 4), RED, 2); ax.text(3.65, 3.8, 'Δ', color=RED, fontsize=14)
    A, A2 = (1.2, 2.3), (5.8, 2.3)
    seg(ax, A, A2, BLUE, 1.3, '--'); tick_code(ax, A, (3.5, 2.3), 1, color=BLUE); tick_code(ax, (3.5, 2.3), A2, 1, color=BLUE)
    right_angle(ax, (3.5, 2.3), (1, 0), (0, -1), 0.22, RED)
    pt(ax, A, 'A', (0, 0.32)); pt(ax, A2, 'A′', (0, 0.32))
    ax.text(3.5, -0.6, 'Δ est la médiatrice de [AA′]', ha='center', fontsize=12)
    save(f, 'MATS_3AS_symetrie_axiale')

def m3_variation():
    f, ax = fig(7, 3, (-3.3, 11.3), (-2.6, 3.2))
    for ox, a, col, t in [(0, 1, BLUE, 'a > 0 : f croissante'), (8, -1, RED, 'a < 0 : f décroissante')]:
        arrow(ax, (ox - 2.8, 0), (ox + 2.9, 0)); arrow(ax, (ox, -2.3), (ox, 2.9))
        xs = np.array([-2.4, 2.4]); ax.plot(xs + ox, a * xs * 0.8 + 0.4, color=col, lw=2.2)
        ax.text(ox, -2.55, t, ha='center', color=col, fontsize=11)
    save(f, 'MATS_3AS_sens_variation')

def m3_abs():
    f, ax = fig(5.2, 3, (-4.4, 4.4), (-0.9, 3.4))
    arrow(ax, (-3.3, 0), (3.4, 0)); arrow(ax, (0, -0.5), (0, 3.3))
    for v in (-3, -2, -1, 1, 2, 3): seg(ax, (v, -0.07), (v, 0.07)); ax.text(v, -0.35, str(v), ha='center', fontsize=9)
    ax.plot([-3, 0], [3, 0], color=RED, lw=2.2); ax.plot([0, 3], [0, 3], color=BLUE, lw=2.2)
    ax.text(-2.9, 2.3, 'y = −x', color=RED, fontsize=11, ha='right'); ax.text(2.9, 2.3, 'y = x', color=BLUE, fontsize=11, ha='left')
    ax.text(0, -0.8, 'f(x) = |x|', ha='center', fontsize=12)
    save(f, 'MATS_3AS_affine_morceaux')

ALL = [m1_comparer, m1_droites, m1_encadrer, m1_fractions_egales, m1_aire_triangle, m1_parallelogrammes, m1_pourcentage,
       m1_cavaliere, m2_addition, m2_identite, m2_axe, m2_vecteurs, m2_polygone, m2_projection, m2_coordonnees, m2_inegalite,
       m2_distance, lambda: sym_central('MATS_symetrie_centrale', 'O est le milieu de [AA′]'), m2_translation, m2_boule,
       m3_ensembles, m3_valeur_absolue, m3_systeme, m3_probleme, m3_angle_centre, m3_cercle_rect, m3_trigo, m3_sym_axiale,
       m3_variation, m3_abs]
if __name__ == '__main__':
    for fn in ALL: fn()
    print(len(ALL), 'رسما')
