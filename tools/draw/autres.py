from common import *

def flow(name, items, w=4.6, h=0.75, gap=0.45, width=6):
    """مخطط سير عمودي: items = [(نص, شكل)] ؛ الشكل: pill (بداية/نهاية)، io (إدخال/إخراج)، rect (معالجة)."""
    n = len(items); H = n * h + (n - 1) * gap
    f, ax = fig(width * 0.6, (H + 0.6) * 0.62, (-w / 2 - 0.3, w / 2 + 0.3), (-H - 0.3, 0.3))
    y = -h / 2
    for i, (t, s) in enumerate(items):
        if s == 'io':
            ax.add_patch(Polygon([(-w / 2 + 0.25, y - h / 2), (w / 2, y - h / 2), (w / 2 - 0.25, y + h / 2), (-w / 2, y + h / 2)], closed=True, fc='#FFF8E1', ec=ORANGE, lw=1.6))
            ax.text(0, y, t, ha='center', va='center', fontsize=11)
        elif s == 'pill': box(ax, (0, y), w * 0.55, h, t, fc='#E8F5E9', ec=GREEN, fs=11, shape='pill')
        else: box(ax, (0, y), w, h, t, fs=11, shape='square')
        if i < n - 1: arrow(ax, (0, y - h / 2), (0, y - h / 2 - gap))
        y -= h + gap
    save(f, name)

def ti_the():
    flow('TI_3AS_algo_the', [('Début', 'pill'), ('1. Faire chauffer l’eau', 'rect'), ('2. Mettre le thé dans la tasse', 'rect'),
         ('3. Verser l’eau chaude', 'rect'), ('4. Attendre', 'rect'), ('5. Ajouter du sucre', 'rect'), ('Fin', 'pill')], w=6.2)

def ti_somme():
    flow('TI_3AS_algo_structure', [('Début', 'pill'), ('Lire A', 'io'), ('Lire B', 'io'), ('S ← A + B', 'rect'), ('Afficher S', 'io'), ('Fin', 'pill')])

def ti_double():
    flow('TI_3AS_algo_double', [('Début', 'pill'), ('Écrire(« Donner un nombre »)', 'io'), ('Lire(N)', 'io'), ('D ← N * 2', 'rect'),
         ('Écrire(« Le double est : », D)', 'io'), ('Fin', 'pill')], w=5.8)

def ti_variable():
    f, ax = fig(6.5, 2.4, (-0.3, 10.3), (-1.4, 2.4))
    for x, nom, typ, val in [(0, 'nom', 'chaîne', '« Yahya »'), (5.3, 'age', 'entier', '18')]:
        ax.add_patch(FancyBboxPatch((x, 0), 4.6, 1.3, boxstyle='round,pad=0.02,rounding_size=0.15', fc='#E3F2FD', ec=BLUE, lw=1.6))
        ax.text(x + 2.3, 0.65, val, ha='center', va='center', fontsize=15, color=RED)
        ax.text(x + 2.3, 1.75, nom, ha='center', fontsize=13, fontweight='bold')
        ax.text(x + 2.3, -0.4, 'type : ' + typ, ha='center', fontsize=11, color=GRAY)
    ax.text(5, -1.1, 'Une variable : un nom, un type et une valeur (case de la mémoire)', ha='center', fontsize=10.5)
    save(f, 'TI_3AS_variable')

def decision(name, cond, oui, non, avant=None):
    f, ax = fig(6, 4.4 if avant else 3.6, (-4.6, 4.6), (-4.6, 1.6 if avant else 0.6))
    y = 0
    if avant:
        box(ax, (0, 1), 3, 0.7, avant, fc='#FFF8E1', ec=ORANGE, fs=11, shape='square'); arrow(ax, (0, 0.65), (0, 0.05)); y = -0.6
    box(ax, (0, y), 4, 1.3, cond, fc='#FCE4EC', ec=RED, fs=11, shape='diamond')
    seg(ax, (-2, y), (-3, y)); arrow(ax, (-3, y), (-3, y - 1.4)); ax.text(-2.5, y + 0.2, 'Vrai', ha='center', color=GREEN, fontsize=11)
    seg(ax, (2, y), (3, y)); arrow(ax, (3, y), (3, y - 1.4)); ax.text(2.5, y + 0.2, 'Faux', ha='center', color=RED, fontsize=11)
    box(ax, (-3, y - 1.8), 2.9, 0.8, oui, fc='#E8F5E9', ec=GREEN, fs=10.5, shape='square')
    box(ax, (3, y - 1.8), 2.9, 0.8, non, fc='#FFEBEE', ec=RED, fs=10.5, shape='square')
    seg(ax, (-3, y - 2.2), (-3, y - 2.9)); seg(ax, (3, y - 2.2), (3, y - 2.9)); seg(ax, (-3, y - 2.9), (3, y - 2.9))
    arrow(ax, (0, y - 2.9), (0, y - 3.4)); ax.text(0, y - 3.7, 'FinSi', ha='center', fontsize=11)
    save(f, name)

def ti_boucle(name, init, cond, corps, incr):
    f, ax = fig(5, 4.8, (-4, 4.4), (-6.3, 1.0))
    y = 0.3
    if init: box(ax, (0, y), 2.8, 0.7, init, fs=11, shape='square'); arrow(ax, (0, y - 0.35), (0, y - 0.95)); y -= 1.6
    box(ax, (0, y), 3.8, 1.3, cond, fc='#FCE4EC', ec=RED, fs=11, shape='diamond')
    arrow(ax, (0, y - 0.65), (0, y - 1.25)); ax.text(0.2, y - 0.95, 'Vrai', color=GREEN, fontsize=10.5)
    box(ax, (0, y - 1.6), 3.2, 0.7, corps, fc='#E8F5E9', ec=GREEN, fs=11, shape='square')
    arrow(ax, (0, y - 1.95), (0, y - 2.45)); box(ax, (0, y - 2.8), 2.8, 0.7, incr, fs=11, shape='square')
    seg(ax, (-1.4, y - 2.8), (-3.2, y - 2.8)); seg(ax, (-3.2, y - 2.8), (-3.2, y)); arrow(ax, (-3.2, y), (-1.9, y))
    ax.text(-3.5, y - 1.4, 'on répète', rotation=90, va='center', ha='center', color=BLUE, fontsize=10)
    seg(ax, (1.9, y), (3.4, y)); arrow(ax, (3.4, y), (3.4, y - 3.8)); ax.text(2.6, y + 0.2, 'Faux', ha='center', color=RED, fontsize=10.5)
    ax.text(3.4, y - 4.15, 'Fin de la boucle', ha='center', fontsize=10.5)
    save(f, name)

def ti_structures():
    f, ax = fig(10, 4, (-0.2, 15.2), (-3.6, 1.2))
    ax.text(2.3, 0.8, 'Séquence', ha='center', fontweight='bold', color=BLUE)
    for i, t in enumerate(['instruction 1', 'instruction 2', 'instruction 3']):
        box(ax, (2.3, -0.1 - i * 1.15), 3.4, 0.6, t, fs=10, shape='square')
        if i < 2: arrow(ax, (2.3, -0.4 - i * 1.15), (2.3, -0.85 - i * 1.15))
    ax.text(7.5, 0.8, 'Sélection', ha='center', fontweight='bold', color=RED)
    box(ax, (7.5, -0.4), 3, 1.1, 'condition ?', fc='#FCE4EC', ec=RED, fs=10, shape='diamond')
    arrow(ax, (6, -0.4), (6, -2.1)); arrow(ax, (9, -0.4), (9, -2.1)); seg(ax, (6, -0.4), (6.0, -0.4))
    ax.text(5.7, -1.2, 'Vrai', rotation=90, fontsize=9, color=GREEN, va='center'); ax.text(9.25, -1.2, 'Faux', rotation=90, fontsize=9, color=RED, va='center')
    box(ax, (6, -2.5), 2, 0.6, 'Alors', fc='#E8F5E9', ec=GREEN, fs=10, shape='square'); box(ax, (9, -2.5), 2, 0.6, 'Sinon', fc='#FFEBEE', ec=RED, fs=10, shape='square')
    ax.text(12.8, 0.8, 'Répétition', ha='center', fontweight='bold', color=GREEN)
    box(ax, (12.8, -0.4), 3, 1.1, 'condition ?', fc='#FCE4EC', ec=RED, fs=10, shape='diamond')
    arrow(ax, (12.8, -0.95), (12.8, -1.6)); box(ax, (12.8, -1.95), 2.6, 0.6, 'instructions', fc='#E8F5E9', ec=GREEN, fs=10, shape='square')
    seg(ax, (11.5, -1.95), (10.9, -1.95)); seg(ax, (10.9, -1.95), (10.9, -0.4)); arrow(ax, (10.9, -0.4), (11.3, -0.4))
    seg(ax, (14.3, -0.4), (14.8, -0.4)); arrow(ax, (14.8, -0.4), (14.8, -3.0)); ax.text(14.85, -3.3, 'sortie', fontsize=9, ha='center')
    save(f, 'TI_3AS_trois_structures')

def ti_powerpoint():
    f, ax = fig(7.5, 4.6, (-0.2, 12.2), (-0.9, 7.6))
    ax.add_patch(Rectangle((0, 0), 12, 7.4, fc='#FAFAFA', ec='black', lw=1.6))
    ax.add_patch(Rectangle((0, 6.6), 12, 0.8, fc='#C62828', ec='black', lw=1.2))
    for i, t in enumerate(['Fichier', 'Accueil', 'Insertion', 'Création', 'Transitions', 'Animations', 'Diaporama']):
        ax.text(0.25 + i * 1.7, 7.0, t, color='white', fontsize=7.5, va='center')
    ax.add_patch(Rectangle((0, 5.6), 12, 1.0, fc='#ECEFF1', ec='black', lw=1))
    ax.text(6, 6.1, 'Ruban : boutons de l’onglet choisi', ha='center', va='center', fontsize=10, color=GRAY)
    ax.add_patch(Rectangle((0, 0.9), 2.4, 4.7, fc='#E3F2FD', ec='black', lw=1))
    for i in range(3): ax.add_patch(Rectangle((0.35, 4.2 - i * 1.4), 1.7, 1.0, fc='white', ec=BLUE if i == 0 else GRAY, lw=1.6 if i == 0 else 0.8))
    ax.add_patch(Rectangle((3.0, 1.6), 8.4, 3.7, fc='white', ec='black', lw=1.4))
    ax.add_patch(Rectangle((3.8, 3.6), 6.8, 1.1, fill=False, ec=GRAY, lw=1, ls='--')); ax.text(7.2, 4.15, 'Cliquez pour ajouter un titre', ha='center', va='center', fontsize=10, color=GRAY)
    ax.add_patch(Rectangle((3.8, 2.1), 6.8, 1.1, fill=False, ec=GRAY, lw=1, ls='--')); ax.text(7.2, 2.65, 'Cliquez pour ajouter un sous-titre', ha='center', va='center', fontsize=9, color=GRAY)
    ax.add_patch(Rectangle((2.4, 0.9), 9.6, 0.6, fc='#F5F5F5', ec='black', lw=0.8)); ax.text(3.0, 1.2, 'Notes', va='center', fontsize=8.5, color=GRAY)
    ax.add_patch(Rectangle((0, 0), 12, 0.9, fc='#ECEFF1', ec='black', lw=0.8)); ax.text(0.3, 0.45, 'Diapositive 1 sur 3', va='center', fontsize=8.5)
    for t, xy, xt in [('onglets', (4.5, 7.0), (4.5, 8.05)), ('volet des\ndiapositives', (1.2, 2.0), (-0.1, -0.55)), ('diapositive', (11.2, 3.0), (11.6, -0.55)), ('zones de texte', (9.9, 4.6), (9.0, 8.05))]:
        arrow(ax, xt, xy, BLUE, 1.2, ms=10); ax.text(*xt, t, ha='center', va='bottom' if xt[1] > 7 else 'top', fontsize=9.5, color=BLUE)
    save(f, 'TI_3AS_interface_powerpoint')

def ti_email():
    f, ax = fig(7, 4.4, (-0.2, 12.2), (-0.4, 7.6))
    ax.add_patch(Rectangle((0, 0), 12, 7.4, fc='white', ec='black', lw=1.6))
    ax.add_patch(Rectangle((0, 6.6), 12, 0.8, fc='#1565C0', ec='black', lw=1.2)); ax.text(0.3, 7.0, 'Nouveau message', color='white', va='center', fontsize=10)
    rows = [('À :', 'professeur@ecole.com'), ('Cc :', ''), ('Objet :', 'Demande d’information sur le projet')]
    for i, (k, v) in enumerate(rows):
        y = 6.1 - i * 0.7; seg(ax, (0, y - 0.35), (12, y - 0.35), '#BDBDBD', 1)
        ax.text(0.3, y, k, va='center', fontsize=10, fontweight='bold'); ax.text(1.6, y, v, va='center', fontsize=10)
    ax.text(0.3, 2.9, 'Bonjour Monsieur,\nje vous écris pour avoir plus d’informations\nsur le projet de science.\nCordialement.\n[Nom]', va='center', fontsize=10, linespacing=1.5)
    ax.add_patch(FancyBboxPatch((0.3, 0.35), 1.8, 0.65, boxstyle='round,pad=0.02,rounding_size=0.2', fc='#1565C0', ec='none')); ax.text(1.2, 0.67, 'Envoyer', color='white', ha='center', va='center', fontsize=10)
    ax.text(2.5, 0.67, '← trombone : joindre un fichier', va='center', fontsize=9.5, color=GRAY)
    for t, y in [('destinataire', 6.1), ('copie', 5.4), ('objet', 4.7)]: ax.text(11.8, y, t, ha='right', va='center', fontsize=9.5, color=RED)
    ax.text(11.8, 2.9, 'corps du message', ha='right', va='center', fontsize=9.5, color=RED)
    save(f, 'TI_3AS_email')

def ti_piece_jointe():
    flow('TI_3AS_piece_jointe', [('1. Nouveau message', 'rect'), ('2. Destinataire et objet', 'rect'), ('3. Cliquer sur le trombone (Joindre)', 'rect'),
         ('4. Choisir le fichier → Ouvrir', 'rect'), ('5. Vérifier que son nom apparaît', 'rect'), ('6. Envoyer', 'pill')], w=6.6)

def pc_molecules():
    f, ax = fig(8, 2.6, (-0.5, 15.5), (-1.6, 2.0))
    col = {'H': ('white', 0.42), 'O': ('#E53935', 0.62), 'C': ('#424242', 0.6), 'N': ('#1E88E5', 0.6)}
    def atom(x, y, e):
        c, r = col[e]; ax.add_patch(Circle((x, y), r, fc=c, ec='black', lw=1.3, zorder=3))
        ax.text(x, y, e, ha='center', va='center', fontsize=11, color='white' if e in 'CNO' else 'black', zorder=4, fontweight='bold')
    atom(0.6, 0.4, 'O'); atom(1.6, 0.4, 'O'); ax.text(1.1, -1.1, 'O₂\ndioxygène', ha='center', fontsize=10)
    atom(3.8, 0.4, 'H'); atom(4.55, 0.4, 'H'); ax.text(4.2, -1.1, 'H₂\ndihydrogène', ha='center', fontsize=10)
    atom(7.4, 0.6, 'O'); atom(6.75, 0.0, 'H'); atom(8.05, 0.0, 'H'); ax.text(7.4, -1.1, 'H₂O\neau', ha='center', fontsize=10)
    atom(10.0, 0.4, 'O'); atom(11.0, 0.4, 'C'); atom(12.0, 0.4, 'O'); ax.text(11.0, -1.1, 'CO₂\ndioxyde de carbone', ha='center', fontsize=10)
    atom(13.9, 0.4, 'N'); atom(14.9, 0.4, 'N'); ax.text(14.4, -1.1, 'N₂\ndiazote', ha='center', fontsize=10)
    ax.text(7.5, 1.65, 'molécules simples : O₂, H₂, N₂ — molécules complexes : H₂O, CO₂', ha='center', fontsize=10.5)
    save(f, 'PC_3AS_modeles_molecules')

def sn_champignon():
    f, ax = fig(5, 4.6, (-3.4, 3.6), (-3.4, 3.2))
    ax.add_patch(Rectangle((-3.3, -3.3), 6.8, 2.2, fc='#D7CCC8', ec='none'))
    ax.text(2.0, -3.0, 'sol riche en matière organique', fontsize=8.5, color='#5D4037', ha='center')
    ax.add_patch(Polygon([(-0.35, -1.1), (0.35, -1.1), (0.28, 1.3), (-0.28, 1.3)], closed=True, fc='#FFF8E1', ec='black', lw=1.5))
    th = np.linspace(0, np.pi, 50); ax.fill(np.cos(th) * 2.0, 1.25 + np.sin(th) * 1.3, fc='#A1887F', ec='black', lw=1.5)
    for x in np.linspace(-1.7, 1.7, 13): seg(ax, (x, 1.25), (x * 0.25, 1.05), '#6D4C41', 0.8)
    rng = np.random.default_rng(3)
    for k in range(14):
        x0 = rng.uniform(-0.3, 0.3); pts = [(x0, -1.1)]
        for j in range(4): pts.append((pts[-1][0] + rng.uniform(-0.9, 0.9), pts[-1][1] - rng.uniform(0.2, 0.5)))
        ax.plot(*zip(*pts), color='white', lw=1.1)
    for t, xy, xt in [('chapeau', (1.2, 2.2), (2.3, 2.9)), ('lamelles', (1.2, 1.15), (2.6, 0.7)), ('pied', (0.3, 0.0), (1.7, -0.25)), ('mycélium\n(absorbe les nutriments)', (-1.0, -1.9), (-2.2, 0.4))]:
        arrow(ax, xt, xy, BLUE, 1.1, ms=9); ax.text(*xt, t, ha='center', va='bottom', fontsize=10, color=BLUE)
    save(f, 'SN_1AS_champignon')

def sn_fecondation():
    f, ax = fig(5, 5, (-3.4, 3.8), (-3.6, 3.6))
    ax.add_patch(Ellipse((0, -2.0), 2.6, 2.4, fc='#C8E6C9', ec='black', lw=1.6))
    ax.add_patch(Polygon([(-0.25, -0.85), (0.25, -0.85), (0.2, 2.4), (-0.2, 2.4)], closed=True, fc='#C8E6C9', ec='black', lw=1.5))
    ax.add_patch(Ellipse((0, 2.6), 1.3, 0.55, fc='#AED581', ec='black', lw=1.5))
    ax.add_patch(Ellipse((0, -2.2), 0.9, 1.2, fc='#FFF59D', ec='black', lw=1.3)); pt(ax, (0, -2.3), None, color=RED, size=6)
    ax.add_patch(Circle((-0.2, 2.95), 0.22, fc='#FFB300', ec='black', lw=1))
    xs = np.array([-0.2, -0.05, 0.08, -0.05, 0.05, 0.0]); ys = np.array([2.8, 1.8, 0.6, -0.6, -1.4, -1.85])
    ax.plot(xs, ys, color=ORANGE, lw=2.2)
    for t, xy, xt in [('grain de pollen', (-0.3, 3.05), (-2.2, 3.2)), ('stigmate', (0.6, 2.6), (2.4, 2.9)), ('tube pollinique', (0.06, 0.6), (2.4, 1.2)),
                      ('style', (-0.2, 1.5), (-2.3, 1.2)), ('ovaire', (-1.25, -1.6), (-2.6, -0.4)), ('ovule (oosphère)', (0.2, -2.3), (2.4, -0.9))]:
        arrow(ax, xt, xy, BLUE, 1.1, ms=9); ax.text(*xt, t, ha='center', va='bottom', fontsize=10, color=BLUE)
    ax.text(0.2, -3.5, 'anthérozoïde + oosphère → œuf', ha='center', fontsize=11)
    save(f, 'SN_2AS_fecondation')

ALL = [ti_the, ti_somme, ti_double, ti_variable,
       lambda: decision('TI_3AS_condition', 'condition', 'instruction', 'autre\ninstruction'),
       lambda: decision('TI_3AS_condition_age', 'age >= 18', 'Afficher\n« Majeur »', 'Afficher\n« Mineur »', 'Lire age'),
       lambda: decision('TI_3AS_pair_impair', 'N mod 2 = 0', 'Afficher\n« Nombre pair »', 'Afficher\n« Nombre impair »', 'Lire N'),
       lambda: ti_boucle('TI_3AS_boucle_pour', 'i ← 1', 'i ≤ 5 ?', 'Afficher « Bonjour »', 'i ← i + 1'),
       lambda: ti_boucle('TI_3AS_boucle_tantque', 'i ← 1', 'i ≤ 3 ?', 'Afficher i', 'i ← i + 1'),
       lambda: ti_boucle('TI_3AS_boucle_1_10', 'i ← 1', 'i ≤ 10 ?', 'Écrire i', 'i ← i + 1'),
       ti_structures, ti_powerpoint, ti_email, ti_piece_jointe, pc_molecules, sn_champignon, sn_fecondation]
if __name__ == '__main__':
    for fn in ALL: fn()
    print(len(ALL), 'رسما')
