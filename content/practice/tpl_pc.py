"""بنوك تمارين الفيزياء والكيمياء (2AS و3AS) بالفرنسية — عناصر مكتوبة وقوالب رقمية بحلول محسوبة."""
from mathfr import fmt, rnd, D
def T(title, text, sol): return {"title": title, "text": text, "solution": sol}

# ================= 2AS =================
MELANGES = [
 T("Mélange homogène ou hétérogène ?", "Classer les mélanges suivants en homogènes et hétérogènes :\neau + sel dissous ; eau + sable ; eau + huile ; eau + sucre dissous ; jus d'orange avec pulpe ; air filtré.",
   "Homogènes : eau salée, eau sucrée, air filtré (on ne distingue pas les constituants à l'œil nu).\nHétérogènes : eau + sable, eau + huile, jus d'orange avec pulpe."),
 T("Définir un mélange", "1) Qu'est-ce qu'un mélange ?\n2) Donner deux exemples de mélanges de la vie quotidienne.\n3) Le lait est-il un corps pur ? Justifier.",
   "1) Un mélange est constitué d'au moins deux constituants différents.\n2) Le thé sucré, l'eau boueuse, l'air…\n3) Non : le lait contient de l'eau, des matières grasses, des protéines… c'est un mélange."),
 T("La décantation", "On laisse reposer de l'eau boueuse dans un verre pendant une heure.\n1) Qu'observe-t-on ?\n2) Comment s'appelle cette méthode de séparation ?\n3) Le liquide obtenu est-il parfaitement limpide ? Que faut-il faire ensuite ?",
   "1) Les particules solides tombent au fond du verre ; un liquide plus clair se trouve au-dessus.\n2) La décantation.\n3) Non, il peut rester de fines particules : on réalise une filtration."),
 T("La filtration", "1) Citer le matériel nécessaire pour réaliser une filtration.\n2) Comment appelle-t-on le liquide recueilli après filtration ?\n3) La filtration permet-elle de séparer le sel de l'eau salée ? Justifier.",
   "1) Un entonnoir, un papier filtre, un support et un bécher (ou erlenmeyer).\n2) Le filtrat.\n3) Non : le sel est dissous, il traverse le filtre avec l'eau (mélange homogène)."),
 T("La distillation", "1) Quel est le but de la distillation ?\n2) Nommer les deux changements d'état qui se produisent lors d'une distillation.\n3) Comment s'appelle le liquide recueilli ?\n4) Comment obtenir de l'eau pure à partir d'eau de mer ?",
   "1) Séparer les constituants d'un mélange homogène liquide (ou obtenir un liquide pur).\n2) La vaporisation (ébullition) puis la liquéfaction (condensation).\n3) Le distillat.\n4) Par distillation de l'eau de mer : la vapeur d'eau se condense, le sel reste dans le ballon."),
 T("Choisir une méthode de séparation", "Indiquer la méthode qui permet de séparer :\na) l'huile et l'eau ;\nb) le sable et l'eau ;\nc) l'eau et le sel dissous ;\nd) les feuilles de thé et le thé.",
   "a) Décantation (ampoule à décanter) ;\nb) décantation puis filtration ;\nc) distillation (ou évaporation pour récupérer le sel) ;\nd) filtration."),
 T("Ordre des étapes", "Pour obtenir une eau limpide à partir d'une eau boueuse, un élève propose : filtration, puis décantation.\n1) L'ordre est-il judicieux ? Justifier.\n2) L'eau obtenue est-elle potable ? Pourquoi ?",
   "1) Non : on décante d'abord (pour ne pas boucher le filtre), puis on filtre.\n2) Pas forcément : elle peut contenir des substances dissoutes et des microbes invisibles."),
]
MELANGES += [
 T("Évaporation et cristallisation", "On chauffe doucement de l'eau salée dans une coupelle jusqu'à disparition de l'eau.\n1) Qu'obtient-on au fond de la coupelle ?\n2) Comment s'appelle cette méthode ?\n3) Comment produit-on le sel dans les salines ?",
   "1) Des cristaux de sel.\n2) L'évaporation (cristallisation).\n3) L'eau de mer s'évapore au soleil dans des bassins et le sel se dépose."),
 T("Mélange de liquides", "On verse de l'alcool dans l'eau, puis de l'huile dans l'eau.\n1) Dans quel cas obtient-on un mélange homogène ?\n2) Comment dit-on de deux liquides qui se mélangent ? qui ne se mélangent pas ?",
   "1) Eau + alcool.\n2) Miscibles ; non miscibles."),
 T("Schéma d'une distillation", "Légender un schéma de montage de distillation avec les mots : ballon, chauffe-ballon, thermomètre, réfrigérant, entrée d'eau froide, distillat.\nPourquoi l'eau froide entre-t-elle par le bas du réfrigérant ?",
   "Le ballon est chauffé ; le thermomètre est en haut du ballon ; la vapeur passe dans le réfrigérant refroidi par l'eau froide ; le distillat est recueilli dans un erlenmeyer.\nL'eau froide entre par le bas pour que le réfrigérant soit entièrement rempli d'eau et refroidisse mieux."),
]
AIR = [
 T("Composition de l'air", "1) Donner les deux principaux constituants de l'air et leurs proportions approximatives.\n2) Citer deux autres gaz présents dans l'air en faible quantité.\n3) Dans une salle de 200 m³, quel volume de dioxygène y a-t-il environ ?",
   "1) Le diazote (environ 78 %, soit 4/5) et le dioxygène (environ 21 %, soit 1/5).\n2) Le dioxyde de carbone, la vapeur d'eau, l'argon…\n3) 200 × 21 ÷ 100 = 42 m³ (ou 200 ÷ 5 = 40 m³ environ)."),
 T("L'air, un mélange", "1) L'air est-il un corps pur ou un mélange ? Justifier.\n2) Quel constituant de l'air est indispensable à la respiration et aux combustions ?\n3) Quel gaz est rejeté lors de la respiration ? Comment le mettre en évidence ?",
   "1) Un mélange : il est formé de plusieurs gaz.\n2) Le dioxygène.\n3) Le dioxyde de carbone ; il trouble l'eau de chaux."),
 T("L'air a une masse", "Un ballon vide pèse 520 g. Après l'avoir gonflé avec de l'air, il pèse 523 g.\n1) Quelle est la masse de l'air introduit ?\n2) Que peut-on en conclure ?",
   "1) 523 − 520 = 3 g\n2) L'air, comme tout gaz, a une masse."),
 T("Compressibilité de l'air", "On bouche l'extrémité d'une seringue remplie d'air et on appuie sur le piston.\n1) Que se passe-t-il ?\n2) Quelle propriété des gaz cette expérience met-elle en évidence ?\n3) Peut-on faire la même chose avec de l'eau ?",
   "1) Le piston s'enfonce : le volume de l'air diminue.\n2) Les gaz sont compressibles.\n3) Non, les liquides sont pratiquement incompressibles."),
]
AIR += [
 T("La combustion consomme le dioxygène", "On renverse un bocal sur une bougie allumée posée dans une assiette d'eau.\n1) Qu'observe-t-on après quelques secondes ?\n2) Quel gaz a été consommé ?\n3) Pourquoi l'eau monte-t-elle dans le bocal ?",
   "1) La bougie s'éteint et l'eau monte un peu dans le bocal.\n2) Le dioxygène.\n3) Une partie du gaz a disparu (consommée par la combustion), l'eau prend sa place."),
 T("La pollution de l'air", "1) Citer deux sources de pollution de l'air.\n2) Citer deux conséquences de cette pollution.\n3) Proposer deux gestes pour réduire la pollution de l'air.",
   "1) Les gaz d'échappement des voitures, les fumées des usines, la combustion des déchets.\n2) Maladies respiratoires, effet de serre et réchauffement climatique.\n3) Planter des arbres, ne pas brûler les déchets, utiliser les transports en commun."),
]
def air_vol(r):
    V = r.choice([50, 80, 120, 150, 250, 300])
    return T("Volumes de diazote et de dioxygène", f"Une pièce contient {V} m³ d'air.\nCalculer le volume de diazote (78 %) et de dioxygène (21 %) qu'elle contient.",
             f"Diazote : {V} × 78 ÷ 100 = {fmt(D(V*78)/100)} m³\nDioxygène : {V} × 21 ÷ 100 = {fmt(D(V*21)/100)} m³")
AIR.append(air_vol)
CORPS = [
 T("Corps purs simples et composés", "Classer : dioxygène O₂ ; eau H₂O ; fer Fe ; dioxyde de carbone CO₂ ; diazote N₂ ; chlorure de sodium NaCl.\nen corps purs simples et corps purs composés. Justifier.",
   "Simples (un seul type d'atome) : O₂, Fe, N₂.\nComposés (plusieurs types d'atomes) : H₂O, CO₂, NaCl."),
 T("Corps pur ou mélange ?", "Dire si chaque substance est un corps pur ou un mélange : eau distillée ; eau du robinet ; air ; fer ; jus de citron ; sel de cuisine (pur).",
   "Corps purs : eau distillée, fer, sel de cuisine pur.\nMélanges : eau du robinet, air, jus de citron."),
 T("Identifier un corps pur", "1) Quelle est la température d'ébullition de l'eau pure sous la pression normale ?\n2) Une eau commence à bouillir à 102 °C. Est-elle pure ? Justifier.",
   "1) 100 °C.\n2) Non : un corps pur a une température d'ébullition fixe ; un écart indique la présence d'autres substances (par exemple du sel)."),
]
ATOMES = [
 T("Noms et symboles", "Donner le symbole des atomes suivants : hydrogène, oxygène, carbone, azote, chlore, sodium, fer, cuivre, argent, or.",
   "H ; O ; C ; N ; Cl ; Na ; Fe ; Cu ; Ag ; Au."),
 T("Des symboles aux noms", "Donner le nom de l'atome correspondant à chaque symbole : Na ; Cl ; Cu ; Ag ; Au ; Fe ; N.",
   "Sodium ; chlore ; cuivre ; argent ; or ; fer ; azote."),
 T("Règle d'écriture des symboles", "1) Quelle règle utilise-t-on pour écrire le symbole d'un atome ?\n2) Corriger les écritures fausses : co ; FE ; cL ; Ag ; NA.",
   "1) La première lettre est une majuscule ; la deuxième (s'il y en a une) est une minuscule.\n2) Co (ou C et O selon le cas) ; Fe ; Cl ; Ag (correct) ; Na."),
]
MOLECULES = [
 T("Formules de molécules", "Donner la formule des molécules suivantes et le nombre d'atomes de chacune : dihydrogène, dioxygène, diazote, dichlore, eau, dioxyde de carbone.",
   "H₂ (2 atomes) ; O₂ (2) ; N₂ (2) ; Cl₂ (2) ; H₂O (3 : 2 H et 1 O) ; CO₂ (3 : 1 C et 2 O)."),
 T("Lire une formule", "Pour chaque formule, indiquer la nature et le nombre d'atomes : H₂O ; CO₂ ; CH₄ ; NH₃.",
   "H₂O : 2 hydrogène, 1 oxygène.\nCO₂ : 1 carbone, 2 oxygène.\nCH₄ : 1 carbone, 4 hydrogène.\nNH₃ : 1 azote, 3 hydrogène."),
 T("Atome ou molécule ?", "1) Quelle différence y a-t-il entre un atome et une molécule ?\n2) Classer : O ; O₂ ; Fe ; H₂O ; Cu ; N₂.",
   "1) Une molécule est un assemblage d'atomes liés entre eux.\n2) Atomes : O, Fe, Cu. Molécules : O₂, H₂O, N₂."),
]
def molec_count(r):
    n = r.randint(2, 9); f = r.choice([("H₂O", {"H": 2, "O": 1}), ("CO₂", {"C": 1, "O": 2}), ("CH₄", {"C": 1, "H": 4}), ("NH₃", {"N": 1, "H": 3})])
    tot = ", ".join(f"{n*v} atomes de {k}" for k, v in f[1].items())
    return T("Compter des atomes", f"Combien d'atomes de chaque sorte y a-t-il dans {n} molécules de {f[0]} ?", f"{n} × {f[0]} : {tot}.")
MOLECULES.append(molec_count)
CIRCUIT = [
 T("Les dipôles et leurs symboles", "Dessiner le symbole normalisé de : une pile, une lampe, un interrupteur ouvert, un interrupteur fermé, un moteur, une DEL.",
   "Pile : deux traits parallèles inégaux ; lampe : cercle avec une croix ; interrupteur ouvert : trait levé ; fermé : trait continu ; moteur : cercle avec M ; DEL : triangle-trait avec deux flèches sortantes."),
 T("Circuit fermé ou ouvert", "1) À quelle condition une lampe brille-t-elle dans un circuit ?\n2) Une lampe ne brille pas alors que le circuit est fermé. Donner deux causes possibles.",
   "1) Le circuit doit être fermé : une boucle conductrice ininterrompue reliant les bornes du générateur.\n2) Lampe grillée, pile usée, mauvais contact, fil coupé."),
 T("Série ou parallèle ?", "Deux lampes L1 et L2 sont branchées avec une pile.\n1) Dans un premier montage, si L1 grille, L2 s'éteint. Comment sont-elles branchées ?\n2) Dans un second montage, si L1 grille, L2 continue de briller. Comment sont-elles branchées ?\n3) Quel montage utilise-t-on dans une maison ? Pourquoi ?",
   "1) En série (une seule boucle).\n2) En dérivation (parallèle).\n3) En dérivation : chaque appareil fonctionne indépendamment des autres."),
 T("Court-circuit", "1) Qu'est-ce qu'un court-circuit ?\n2) Pourquoi est-il dangereux ?\n3) Quel dispositif protège une installation contre les courts-circuits ?",
   "1) Les bornes d'un dipôle (ou du générateur) sont reliées par un fil sans dipôle : le courant ne passe plus par le dipôle.\n2) Le courant devient très intense : échauffement des fils, risque d'incendie, pile détériorée.\n3) Un fusible ou un disjoncteur."),
]
def intensite_serie(r):
    I = D(r.randint(12, 95)) / 100
    return T("Loi d'unicité de l'intensité", f"Un circuit série comporte une pile, une lampe et un moteur. Un ampèremètre placé avant la lampe indique {fmt(I)} A.\n1) Quelle est l'intensité du courant qui traverse le moteur ? Justifier.\n2) Convertir cette intensité en mA.\n3) Comment branche-t-on un ampèremètre ?",
             f"1) {fmt(I)} A : dans un circuit série, l'intensité est la même en tout point (loi d'unicité).\n2) {fmt(I)} A = {fmt(I*1000)} mA\n3) En série, le courant entrant par la borne mA (ou A) et sortant par la borne COM.")
def noeuds(r):
    I1, I2 = D(r.randint(10, 60)) / 100, D(r.randint(10, 60)) / 100
    I3 = D(r.randint(10, 40)) / 100; It = I1 + I2 + I3
    return T("Loi des nœuds", f"Trois lampes sont montées en dérivation aux bornes d'un générateur. Elles sont traversées par I1 = {fmt(I1)} A, I2 = {fmt(I2)} A et I3 = {fmt(I3)} A.\n1) Énoncer la loi des nœuds.\n2) Calculer l'intensité I du courant dans la branche principale.\n3) On retire la lampe 3. Quelle est la nouvelle intensité principale ?",
             f"1) La somme des intensités des courants qui arrivent à un nœud est égale à la somme des intensités qui en repartent.\n2) I = {fmt(I1)} + {fmt(I2)} + {fmt(I3)} = {fmt(It)} A\n3) I = {fmt(I1)} + {fmt(I2)} = {fmt(I1+I2)} A")
def noeud_inconnue(r):
    I = D(r.randint(60, 150)) / 100; I1 = D(r.randint(15, int(I*100) - 10)) / 100
    return T("Trouver une intensité inconnue", f"Dans un montage en dérivation à deux branches, l'ampèremètre de la branche principale indique {fmt(I)} A et celui de la branche 1 indique {fmt(I1)} A.\nCalculer l'intensité dans la branche 2. En mA ?",
             f"I2 = {fmt(I)} − {fmt(I1)} = {fmt(I-I1)} A = {fmt((I-I1)*1000)} mA")
CIRCUIT_NUM = [intensite_serie, noeuds, noeud_inconnue]
def tension_add(r):
    U = r.choice([D(6), D(9), D(12), D('4.5')]); U1 = D(r.randint(10, int(U * 10) - 10)) / 10
    return T("Additivité des tensions", f"Une lampe et un moteur sont branchés en série aux bornes d'un générateur de tension {fmt(U)} V. La tension aux bornes de la lampe est {fmt(U1)} V.\n1) Énoncer la loi d'additivité des tensions.\n2) Calculer la tension aux bornes du moteur.\n3) Comment branche-t-on un voltmètre ?",
             f"1) Dans un circuit série, la tension aux bornes du générateur est égale à la somme des tensions aux bornes des autres dipôles.\n2) U_M = {fmt(U)} − {fmt(U1)} = {fmt(U-U1)} V\n3) En dérivation, aux bornes du dipôle (borne V et borne COM).")
def tension_unicite(r):
    U = r.choice([D('4.5'), D(6), D(9), D(12)])
    return T("Unicité de la tension en dérivation", f"Deux lampes sont montées en dérivation aux bornes d'une pile de {fmt(U)} V.\n1) Quelle est la tension aux bornes de chaque lampe ? Justifier.\n2) On ajoute une troisième lampe en dérivation. La tension change-t-elle ?",
             f"1) {fmt(U)} V pour chacune : les dipôles en dérivation ont la même tension à leurs bornes.\n2) Non, elle reste égale à {fmt(U)} V (si la pile est idéale).")
def tension_conv(r):
    a, b = D(r.randint(150, 9500)) / 1000, r.randint(2, 900)
    return T("Mesure et conversion de tensions", f"1) Convertir {fmt(a)} V en mV et {b} mV en V.\n2) Quel calibre choisir pour mesurer une tension d'environ 4,5 V avec un voltmètre dont les calibres sont 200 mV, 2 V, 20 V, 200 V ?",
             f"1) {fmt(a)} V = {fmt(a*1000)} mV ; {b} mV = {fmt(D(b)/1000)} V\n2) 20 V (le plus petit calibre supérieur à la valeur mesurée).")
TENSION = [tension_add, tension_unicite, tension_conv]
def nominale(r):
    Un = r.choice([D(6), D('3.5'), D(12), D('2.5')]); U = Un + r.choice([D(-2), D(-1), D(0), D(2), D(3)])
    st = "fonctionnement normal" if U == Un else ("surtension : la lampe brille trop et risque de griller" if U > Un else "sous-tension : la lampe brille faiblement")
    return T("Tension nominale", f"Sur le culot d'une lampe on lit « {fmt(Un)} V ».\n1) Que représente cette indication ?\n2) On l'alimente sous {fmt(U)} V. Que se passe-t-il ?",
             f"1) C'est sa tension nominale : la tension pour laquelle elle fonctionne normalement.\n2) {fmt(U)} V {'=' if U == Un else '>' if U > Un else '<'} {fmt(Un)} V : {st}.")
FORCES = [
 T("Effets d'une force", "Pour chaque situation, préciser l'effet de la force (statique : déformation ; dynamique : mise en mouvement ou modification du mouvement) :\na) un joueur frappe un ballon ;\nb) on écrase une boule de pâte ;\nc) le frein ralentit un vélo ;\nd) un ressort s'allonge sous une charge.",
   "a) Dynamique (mise en mouvement) ;\nb) statique (déformation) ;\nc) dynamique (modification du mouvement) ;\nd) statique (déformation)."),
 T("Forces de contact et à distance", "Classer en forces de contact et forces à distance : la force du vent sur une voile ; l'attraction d'un aimant sur un clou ; le poids d'une pomme ; la poussée d'une main sur une porte ; la force d'un fil qui retient une lampe.",
   "Contact : vent sur la voile, main sur la porte, fil sur la lampe.\nÀ distance : aimant sur le clou, poids de la pomme."),
 T("Caractéristiques d'une force", "Citer les quatre caractéristiques d'une force et l'unité de son intensité. Avec quel appareil mesure-t-on cette intensité ?",
   "Point d'application, direction (droite d'action), sens, intensité. Unité : le newton (N). Appareil : le dynamomètre."),
]
def force_repr(r):
    F = r.choice([2, 3, 4, 5, 6, 8, 10, 12, 15, 20]); e = r.choice([1, 2, 5]); l = D(F) / e
    sens = r.choice(["vers le haut", "vers la droite", "vers le bas"])
    return T("Représenter une force", f"Une force d'intensité {F} N, verticale ou horizontale, dirigée {sens}, s'exerce au point A.\nÉchelle : 1 cm pour {e} N.\n1) Quelle longueur doit avoir la flèche ?\n2) Décrire la représentation.",
             f"1) {F} ÷ {e} = {fmt(l)} cm\n2) Une flèche partant de A, dirigée {sens}, de longueur {fmt(l)} cm.")
FORCES.append(force_repr)
LUMIERE = [
 T("Sources de lumière", "Classer en sources primaires et sources secondaires (objets diffusants) : le Soleil ; la Lune ; une lampe allumée ; un miroir ; une bougie allumée ; un mur blanc éclairé.",
   "Primaires (produisent leur lumière) : Soleil, lampe allumée, bougie.\nSecondaires (renvoient la lumière reçue) : Lune, miroir, mur éclairé."),
 T("Propagation rectiligne", "1) Comment la lumière se propage-t-elle dans un milieu transparent et homogène ?\n2) Comment représente-t-on un rayon lumineux ?\n3) Quels sont les trois types de faisceaux lumineux ?",
   "1) En ligne droite.\n2) Par une droite munie d'une flèche indiquant le sens de propagation.\n3) Faisceaux convergent, divergent et parallèle."),
 T("Ombre propre et ombre portée", "Une balle éclairée par une lampe se trouve devant un mur.\n1) Qu'appelle-t-on ombre propre et ombre portée ?\n2) Pourquoi observe-t-on une pénombre avec une source étendue ?",
   "1) Ombre propre : partie non éclairée de l'objet ; ombre portée : zone non éclairée sur l'écran derrière l'objet.\n2) Certaines zones reçoivent la lumière d'une partie seulement de la source : elles sont partiellement éclairées."),
 T("Les éclipses", "1) Lors d'une éclipse de Soleil, quel astre se trouve entre la Terre et le Soleil ?\n2) Lors d'une éclipse de Lune, où se trouve la Terre ?\n3) Quelle propriété de la lumière ces phénomènes illustrent-ils ?",
   "1) La Lune.\n2) Entre le Soleil et la Lune : la Lune est dans l'ombre de la Terre.\n3) La propagation rectiligne de la lumière."),
]
def chambre_noire(r):
    h = D(r.randint(2, 12)); d = D(r.randint(2, 10)); dd = D(r.randint(10, 40)) / 100
    hh = h * dd / d
    return T("La chambre noire", f"Un arbre de {fmt(h)} m de haut est situé à {fmt(d)} m d'une chambre noire de profondeur {fmt(dd*100)} cm.\n1) L'image est-elle droite ou renversée ?\n2) Calculer la hauteur de l'image (h'/h = d'/d).",
             f"1) Renversée.\n2) h' = {fmt(h)} × {fmt(dd)} ÷ {fmt(d)} = {fmt(rnd(hh,3))} m = {fmt(rnd(hh*100,1))} cm")
LUMIERE.append(chambre_noire)

BANK_2AS = {
 "Mélanges": MELANGES, "Méthodes de séparation": MELANGES, "Distillation": MELANGES,
 "air": AIR, "Corps purs": CORPS, "Atome": ATOMES, "Molécule": MOLECULES,
 "circuit": CIRCUIT, "Dipôles": CIRCUIT, "Circuit série": CIRCUIT, "Intensité": CIRCUIT_NUM, "unicité": CIRCUIT_NUM + TENSION, "nœuds": CIRCUIT_NUM,
 "Tension": TENSION, "Additivité": TENSION, "Unicité de la tension": TENSION, "Tension nominale": [nominale], "Surtension": [nominale],
 "force": FORCES, "forces": FORCES, "lumière": LUMIERE, "Faisceaux": LUMIERE, "Ombres": LUMIERE, "Chambre noire": LUMIERE,
}

# ================= 3AS =================
EQS = [("C + O₂ → CO₂", "C + O₂ → CO₂ (déjà équilibrée)"), ("H₂ + O₂ → H₂O", "2 H₂ + O₂ → 2 H₂O"), ("CH₄ + O₂ → CO₂ + H₂O", "CH₄ + 2 O₂ → CO₂ + 2 H₂O"),
       ("Fe + O₂ → Fe₃O₄", "3 Fe + 2 O₂ → Fe₃O₄"), ("C + CuO → Cu + CO₂", "C + 2 CuO → 2 Cu + CO₂"), ("Na + Cl₂ → NaCl", "2 Na + Cl₂ → 2 NaCl"),
       ("Al + O₂ → Al₂O₃", "4 Al + 3 O₂ → 2 Al₂O₃"), ("C₃H₈ + O₂ → CO₂ + H₂O", "C₃H₈ + 5 O₂ → 3 CO₂ + 4 H₂O"), ("N₂ + H₂ → NH₃", "N₂ + 3 H₂ → 2 NH₃")]
def equilibrer(r):
    a, b = r.sample(EQS, 2)
    return T("Équilibrer une équation bilan", f"Équilibrer les équations suivantes :\na) {a[0]}\nb) {b[0]}\nPourquoi faut-il équilibrer une équation ?",
             f"a) {a[1]}\nb) {b[1]}\nAu cours d'une réaction, les atomes se conservent (même nombre et même nature) : on ajuste les coefficients.")
REACTION = [equilibrer,
 T("Réactifs et produits", "Lors de la combustion du carbone dans le dioxygène, il se forme du dioxyde de carbone.\n1) Nommer les réactifs et le produit.\n2) Écrire le bilan en mots puis l'équation bilan.\n3) Comment mettre en évidence le produit formé ?",
   "1) Réactifs : carbone et dioxygène ; produit : dioxyde de carbone.\n2) carbone + dioxygène → dioxyde de carbone ; C + O₂ → CO₂\n3) Il trouble l'eau de chaux."),
 T("Transformation chimique ou physique ?", "Dire s'il s'agit d'une transformation chimique ou physique : la fusion de la glace ; la combustion du bois ; la rouille du fer ; la dissolution du sucre ; la cuisson d'un œuf.",
   "Physiques : fusion de la glace, dissolution du sucre.\nChimiques (nouvelles substances) : combustion du bois, rouille du fer, cuisson d'un œuf."),
 T("Conservation de la masse", "On fait réagir 12 g de carbone avec 32 g de dioxygène ; la réaction est totale.\n1) Quelle masse de dioxyde de carbone obtient-on ?\n2) Quelle loi utilise-t-on ?",
   "1) 12 + 32 = 44 g\n2) La conservation de la masse (loi de Lavoisier) : la masse des réactifs consommés est égale à la masse des produits formés."),
]
def conc(r):
    m = D(r.randint(5, 60)); V = D(r.choice([100, 200, 250, 500])) / 1000
    return T("Concentration massique", f"On dissout {fmt(m)} g de sucre dans de l'eau pour obtenir {fmt(V*1000)} mL de solution.\n1) Nommer le soluté et le solvant.\n2) Calculer la concentration massique en g/L.",
             f"1) Soluté : sucre ; solvant : eau.\n2) Cm = m ÷ V = {fmt(m)} ÷ {fmt(V)} = {fmt(m/V)} g/L")
def dilution(r):
    C1 = D(r.choice([20, 40, 50, 80, 100])); V1 = D(r.choice([10, 20, 25, 50])); k = r.choice([2, 4, 5, 10]); V2 = V1 * k
    return T("Dilution", f"On prélève {fmt(V1)} mL d'une solution de concentration {fmt(C1)} g/L et on complète avec de l'eau jusqu'à {fmt(V2)} mL.\n1) La masse de soluté change-t-elle ?\n2) Calculer la concentration de la solution diluée.\n3) Quel est le facteur de dilution ?",
             f"1) Non, la masse de soluté se conserve.\n2) C2 = C1 × V1 ÷ V2 = {fmt(C1)} × {fmt(V1)} ÷ {fmt(V2)} = {fmt(C1*V1/V2)} g/L\n3) F = V2 ÷ V1 = {k}")
SOLUTIONS = [conc, dilution,
 T("Soluté, solvant, solution", "1) Définir : soluté, solvant, solution.\n2) Qu'est-ce qu'une solution saturée ?\n3) Dans l'eau salée, identifier le soluté et le solvant.",
   "1) Soluté : substance dissoute ; solvant : liquide qui dissout ; solution : mélange homogène obtenu.\n2) Une solution qui ne peut plus dissoudre de soluté (le soluté en excès reste au fond).\n3) Soluté : sel ; solvant : eau."),
]
def ph(r):
    vals = r.sample([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13], 5)
    nat = lambda p: "acide" if p < 7 else ("neutre" if p == 7 else "basique")
    return T("Solutions acides, basiques et neutres", "On mesure le pH de cinq solutions : " + " ; ".join(f"S{i+1} : {p}" for i, p in enumerate(vals)) +
             "\n1) Classer ces solutions en acides, neutres et basiques.\n2) Quelle est la plus acide ? la plus basique ?\n3) On dilue une solution acide. Comment varie son pH ?",
             "1) " + " ; ".join(f"S{i+1} {nat(p)}" for i, p in enumerate(vals)) +
             f"\n2) Plus acide : S{vals.index(min(vals))+1} (pH {min(vals)}) ; plus basique : S{vals.index(max(vals))+1} (pH {max(vals)})\n3) Il augmente et se rapproche de 7.")
PH = [ph,
 T("Mesurer le pH", "1) Citer deux moyens de mesurer le pH d'une solution.\n2) Donner un exemple de solution acide et un exemple de solution basique de la vie courante.\n3) Pourquoi faut-il porter des lunettes et des gants pour manipuler les acides et les bases concentrés ?",
   "1) Le papier pH et le pH-mètre.\n2) Acide : jus de citron, vinaigre ; basique : eau de Javel, savon.\n3) Ils sont corrosifs : ils peuvent brûler la peau et les yeux."),
]
ETATS = [
 T("Les changements d'état", "Nommer les changements d'état suivants :\na) solide → liquide ;\nb) liquide → gaz ;\nc) gaz → liquide ;\nd) liquide → solide ;\ne) solide → gaz.",
   "a) Fusion ; b) vaporisation ; c) liquéfaction (condensation) ; d) solidification ; e) sublimation."),
 T("Propriétés des états de la matière", "Compléter :\n1) Un solide a une forme … et un volume … .\n2) Un liquide a une forme … mais un volume … .\n3) Un gaz n'a ni forme ni volume … ; il est … .\n4) La surface libre d'un liquide au repos est … .",
   "1) propre ; propre\n2) celle du récipient (pas de forme propre) ; propre\n3) propres ; compressible (et expansible)\n4) plane et horizontale"),
 T("Masse et volume lors d'un changement d'état", "On fait fondre 200 g de glace.\n1) Quelle masse d'eau liquide obtient-on ? Justifier.\n2) Le volume change-t-il ?",
   "1) 200 g : la masse se conserve lors d'un changement d'état.\n2) Oui : le volume de l'eau liquide est légèrement plus petit que celui de la glace."),
 T("Palier de température", "On chauffe de la glace à −10 °C jusqu'à obtenir de l'eau à 20 °C.\n1) À quelle température se fait la fusion ?\n2) Que devient la température pendant la fusion ?\n3) Que peut-on en conclure sur l'eau ?",
   "1) 0 °C.\n2) Elle reste constante (palier) tant que toute la glace n'a pas fondu.\n3) L'eau est un corps pur (palier de fusion à 0 °C)."),
]
def masse_vol(r):
    mat = r.choice([("fer", D(7800)), ("aluminium", D(2700)), ("cuivre", D(8900)), ("huile", D(920)), ("eau", D(1000))])
    V = D(r.randint(20, 500)) / 1000000 * 1000; Vc = D(r.randint(20, 500))
    m = mat[1] * Vc / 1000000
    return T("Masse volumique", f"Un objet en {mat[0]} a un volume de {fmt(Vc)} cm³. La masse volumique du {mat[0]} est {fmt(mat[1])} kg/m³.\n1) Convertir le volume en m³.\n2) Calculer la masse de l'objet en kg puis en g.\n3) Calculer sa densité par rapport à l'eau.",
             f"1) {fmt(Vc)} cm³ = {fmt(Vc/1000000)} m³\n2) m = ρ × V = {fmt(mat[1])} × {fmt(Vc/1000000)} = {fmt(m)} kg = {fmt(m*1000)} g\n3) d = {fmt(mat[1])} ÷ 1 000 = {fmt(mat[1]/1000)}")
def flotte(r):
    m, V = D(r.randint(50, 900)), D(r.randint(100, 800))
    rho = m / V
    return T("Flotte ou coule ?", f"Un objet a une masse de {fmt(m)} g et un volume de {fmt(V)} cm³.\n1) Calculer sa masse volumique en g/cm³ (arrondie au centième).\n2) Calculer sa densité par rapport à l'eau.\n3) Flotte-t-il sur l'eau ?",
             f"1) ρ = {fmt(m)} ÷ {fmt(V)} ≈ {fmt(rnd(rho,2))} g/cm³\n2) d ≈ {fmt(rnd(rho,2))}\n3) {'Oui, car d < 1' if rho < 1 else ('Non, car d > 1' if rho > 1 else 'Il reste en équilibre (d = 1)')}")
MASSEVOL = [masse_vol, flotte]
def poids(r):
    m = D(r.randint(20, 900)) / 10; g = D('9.8'); gL = D('1.6')
    return T("Poids et masse", f"Un sac a une masse de {fmt(m)} kg.\n1) Calculer son poids sur la Terre (g = 9,8 N/kg).\n2) Calculer son poids sur la Lune (g = 1,6 N/kg).\n3) Sa masse change-t-elle sur la Lune ?",
             f"1) P = m × g = {fmt(m)} × 9,8 = {fmt(m*g)} N\n2) P = {fmt(m)} × 1,6 = {fmt(m*gL)} N\n3) Non, la masse ne dépend pas du lieu.")
def poids_carac(r):
    m = D(r.randint(2, 50)) / 10; P = m * 10; e = r.choice([5, 10, 20])
    return T("Caractéristiques du poids", f"Une pomme de masse {fmt(m*1000)} g est suspendue à un fil (g = 10 N/kg).\n1) Calculer son poids.\n2) Donner les caractéristiques du poids.\n3) Représenter le poids avec l'échelle 1 cm pour {e} N : quelle longueur de flèche ?",
             f"1) P = {fmt(m)} × 10 = {fmt(P)} N\n2) Point d'application : centre de gravité ; direction : verticale ; sens : vers le bas ; intensité : {fmt(P)} N.\n3) {fmt(P)} ÷ {e} = {fmt(rnd(P/e,2))} cm")
POIDS = [poids, poids_carac,
 T("Gravitation", "1) Qu'est-ce que la gravitation universelle ?\n2) De quoi dépend l'intensité de l'attraction entre deux corps ?\n3) Comment varie l'intensité de la pesanteur g quand on s'élève en altitude ?",
   "1) L'attraction mutuelle que s'exercent deux corps ayant une masse.\n2) De leurs masses et de la distance qui les sépare (elle diminue quand la distance augmente).\n3) Elle diminue."),
]
def ohm(r):
    R = r.choice([10, 22, 47, 50, 100, 220, 330]); I = D(r.randint(5, 90)) / 1000
    return T("Loi d'Ohm", f"Un conducteur ohmique de résistance R = {R} Ω est traversé par un courant d'intensité I = {fmt(I*1000)} mA.\n1) Énoncer la loi d'Ohm.\n2) Calculer la tension à ses bornes.\n3) Quelle intensité le traverse sous 6 V ?",
             f"1) U = R × I (U en V, R en Ω, I en A).\n2) U = {R} × {fmt(I)} = {fmt(R*I)} V\n3) I = 6 ÷ {R} ≈ {fmt(rnd(D(6)/R,4))} A ≈ {fmt(rnd(D(6000)/R,1))} mA")
def ohm_carac(r):
    R = r.choice([20, 40, 50, 100]); Is = [D(r.randint(1, 3) * k) / 100 for k in (1, 2, 3, 4)]
    Is = sorted(set(Is))
    return T("Caractéristique d'un dipôle ohmique", "On mesure U et I pour un conducteur ohmique :\nI (A) : " + " | ".join(fmt(i) for i in Is) + "\nU (V) : " + " | ".join(fmt(R*i) for i in Is) +
             "\n1) U et I sont-ils proportionnels ? Justifier.\n2) En déduire la résistance R.\n3) Quelle est l'allure de la caractéristique U = f(I) ?",
             f"1) Oui : U ÷ I = {R} pour chaque mesure.\n2) R = {R} Ω\n3) Une droite passant par l'origine.")
def resist_assoc(r):
    R1, R2 = r.choice([(10, 15), (20, 30), (30, 60), (40, 60), (12, 24), (100, 150)])
    Rs = R1 + R2; Rp = D(R1 * R2) / (R1 + R2)
    return T("Association de résistances", f"On dispose de R1 = {R1} Ω et R2 = {R2} Ω.\n1) Calculer la résistance équivalente lorsqu'elles sont en série.\n2) Calculer la résistance équivalente lorsqu'elles sont en dérivation.\n3) Comparer chaque résultat à R1 et R2.",
             f"1) Rs = {R1} + {R2} = {Rs} Ω\n2) 1/Rp = 1/{R1} + 1/{R2} ; Rp = {R1} × {R2} ÷ ({R1} + {R2}) = {fmt(rnd(Rp,2))} Ω\n3) Rs est plus grande que chacune ; Rp est plus petite que chacune.")
OHM = [ohm, ohm_carac, resist_assoc]
def puissance(r):
    U = r.choice([220, 230]); P = r.choice([60, 100, 1000, 1500, 2000]); t = r.choice([2, 3, 5]); prix = r.choice([59, 63])
    I = D(P) / U; E = D(P) / 1000 * t
    return T("Puissance et énergie électriques", f"Sur un appareil on lit : {U} V – {P} W. Il fonctionne {t} h par jour.\n1) Que signifient ces indications ?\n2) Calculer l'intensité du courant qui le traverse (arrondie au centième).\n3) Calculer l'énergie consommée par jour en kWh.\n4) Le kWh coûte {prix} MRU. Calculer le coût pour 30 jours.",
             f"1) Tension nominale {U} V ; puissance nominale {P} W.\n2) I = P ÷ U = {P} ÷ {U} ≈ {fmt(rnd(I,2))} A\n3) E = P × t = {fmt(D(P)/1000)} kW × {t} h = {fmt(E)} kWh\n4) {fmt(E)} × 30 × {prix} = {fmt(E*30*prix)} MRU")
PUISS = [puissance,
 T("Puissance nominale", "1) Quelle est l'unité de la puissance électrique ?\n2) Écrire la relation entre P, U et I.\n3) Deux lampes de 60 W et 100 W sont alimentées sous leur tension nominale : laquelle brille le plus ?",
   "1) Le watt (W).\n2) P = U × I.\n3) La lampe de 100 W (plus grande puissance)."),
]
def reflexion(r):
    i = r.randint(15, 75)
    return T("Réflexion de la lumière", f"Un rayon lumineux arrive sur un miroir plan avec un angle d'incidence de {i}°.\n1) Énoncer les lois de la réflexion.\n2) Quel est l'angle de réflexion ?\n3) Quel angle fait le rayon réfléchi avec la surface du miroir ?",
             f"1) Le rayon réfléchi est dans le plan d'incidence ; l'angle de réflexion est égal à l'angle d'incidence.\n2) r = {i}°\n3) 90° − {i}° = {90-i}°")
REFRAC = [reflexion,
 T("Réfraction de la lumière", "Un rayon passe de l'air dans l'eau.\n1) Qu'appelle-t-on réfraction ?\n2) Le rayon réfracté se rapproche-t-il ou s'éloigne-t-il de la normale ?\n3) Pourquoi un bâton plongé dans l'eau paraît-il brisé ?",
   "1) Le changement de direction de la lumière lorsqu'elle traverse la surface de séparation de deux milieux transparents différents.\n2) Il se rapproche de la normale (l'eau est plus réfringente que l'air).\n3) À cause de la réfraction de la lumière à la surface de l'eau."),
 T("Réfraction : de l'eau vers l'air", "Un rayon passe de l'eau vers l'air sous un angle d'incidence de 30°.\n1) L'angle de réfraction est-il plus grand ou plus petit que 30° ?\n2) Que se passe-t-il si l'angle d'incidence est très grand ?",
   "1) Plus grand : le rayon s'éloigne de la normale en passant dans l'air.\n2) Il peut ne plus sortir : c'est la réflexion totale."),
]
BANK_3AS = {
 "Réaction chimique": REACTION, "Élément chimique": REACTION, "équation bilan": REACTION, "Conservation du nombre": REACTION,
 "Soluté": SOLUTIONS, "Dissolution": SOLUTIONS, "dilution": SOLUTIONS + PH, "Concentration": SOLUTIONS, "pH": PH, "acides": PH,
 "états de la matière": ETATS, "Propriétés physiques": ETATS, "changements d'état": ETATS, "Conservation de la masse": ETATS, "Variation du volume": ETATS,
 "masse volumique": MASSEVOL, "Densité": MASSEVOL, "poids": POIDS, "Représentation vectorielle": POIDS, "gravitation": POIDS, "pesanteur": POIDS,
 "dipôle ohmique": OHM, "résistances": OHM, "Puissance": PUISS, "Énergie électrique": PUISS, "Réflexion": REFRAC, "Réfraction": REFRAC,
}

def items_for(bank, lessons):
    out = []; seen = set()
    for l in lessons:
        for k, v in bank.items():
            if k.lower() in l.lower():
                for it in v:
                    if id(it) not in seen: seen.add(id(it)); out.append(it)
    return out
