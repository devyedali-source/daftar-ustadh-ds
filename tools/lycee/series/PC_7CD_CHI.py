# Série d'exercices de fin de chapitre — Chimie 7C/7D.
# k = "book" : exercice du manuel (src = page et numéro) ; "extra" : exercice du même type ; "int" : situation d'intégration.
SERIES = {}

SERIES["Chimie – Chapitre I : Les alcools — oxydation et déshydratation"] = [
 {"k": "book", "src": "p. 35, ex. 2", "q": "Une substance contient en masse 60 % de C, 26,7 % de O et 13,3 % de H ; la densité de sa vapeur par rapport à l’air est 2,07. Elle décolore le permanganate acide en donnant un produit qui précipite avec la DNPH mais pas avec la liqueur de Fehling. Identifier la substance et le produit.",
  "a": "M = 29 × 2,07 ≈ 60 g/mol ; x = 0,60 × 60/12 = 3, y = 0,133 × 60 = 8, z = 0,267 × 60/16 = 1 : C₃H₈O. Le produit d’oxydation est une cétone (DNPH +, Fehling −) : l’alcool est secondaire, c’est le propan-2-ol CH₃–CHOH–CH₃, oxydé en propanone CH₃–CO–CH₃."},
 {"k": "book", "src": "p. 35, ex. 3", "q": "Deux alcools A₁ et A₂ de même formule brute se déshydratent en C₃H₆. 6 g du mélange sont oxydés par un oxydant en excès ; la masse totale des produits obtenus est 6,2 g. 1) Alcène, A₁ et A₂. 2) Produits d’oxydation. 3) Composition molaire du mélange.",
  "a": "1) Propène CH₂=CH–CH₃ ; A₁ = propan-1-ol, A₂ = propan-2-ol (C₃H₈O, M = 60 g/mol).\n2) Propan-1-ol → propanal → acide propanoïque (le BBT jaunit) ; propan-2-ol → propanone (DNPH +, Fehling −).\n3) n total = 0,1 mol ; 74x + 58(0,1 − x) = 6,2 ⇒ x = 0,025 mol : 25 % de propan-1-ol et 75 % de propan-2-ol."},
 {"k": "book", "src": "p. 36, ex. 4", "q": "Un alcool a pour masse molaire 74 g/mol. Son oxydation ménagée donne un produit qui réagit avec la 2,4-DNPH mais ne change pas l’aspect de la liqueur de Fehling. 1) Formule brute et alcools possibles. 2) Conclure sur la classe et le nom de l’alcool.",
  "a": "1) CₙH₂ₙ₊₂O : 14n + 18 = 74 ⇒ n = 4, C₄H₁₀O : butan-1-ol, butan-2-ol, 2-méthylpropan-1-ol, 2-méthylpropan-2-ol.\n2) DNPH positif (précipité jaune-orangé) : aldéhyde ou cétone ; Fehling négatif : c’est une cétone. L’alcool est secondaire : butan-2-ol (produit : butanone). On peut confirmer avec le réactif de Schiff (reste incolore)."},
 {"k": "book", "src": "p. 36, ex. 5", "q": "A : CH₃–CHOH–CH₂–CH₃. 1) Nom et classe. 2) Réaction avec l’acide éthanoïque : équation, caractéristiques, nom du produit. 3) Carbone asymétrique. 4) Produit d’oxydation ménagée.",
  "a": "1) Butan-2-ol, alcool secondaire.\n2) CH₃COOH + C₄H₉OH ⇌ CH₃COO–CH(CH₃)–C₂H₅ + H₂O : estérification lente, limitée, athermique ; éthanoate de 1-méthylpropyle.\n3) Le carbone 2 (lié à H, OH, CH₃ et C₂H₅) : deux énantiomères images l’un de l’autre dans un miroir.\n4) Butanone CH₃–CO–CH₂–CH₃."},
 {"k": "book", "src": "p. 37, ex. 8", "q": "Un alcool A réagit avec un excès de sodium ; le sel obtenu contient 23,96 % de sodium. 1) Équation (formules générales). 2) Formule brute de A. 3) A est optiquement actif : nom.",
  "a": "1) 2R–OH + 2Na → 2R–ONa + H₂.\n2) M(RONa) = 23/0,2396 ≈ 96 g/mol ⇒ M(R) = 96 − 39 = 57 : C₄H₉ ; A = C₄H₁₀O.\n3) Le seul alcool C₄H₁₀O chiral est le butan-2-ol."},
 {"k": "extra", "src": "", "q": "Écrire l’équation de la déshydratation intramoléculaire de l’éthanol (vers 170 °C, H₂SO₄) et celle de la déshydratation intermoléculaire (vers 140 °C).",
  "a": "CH₃–CH₂OH → CH₂=CH₂ + H₂O (éthylène) ; 2CH₃–CH₂OH → CH₃–CH₂–O–CH₂–CH₃ + H₂O (éthoxyéthane)."},
 {"k": "int", "src": "", "q": "Un contrôle d’alcoolémie : l’éthanol de l’haleine est oxydé par le dichromate (orange) en acide éthanoïque, et le dichromate devient Cr³⁺ (vert).\n1) Écrire les demi-équations et l’équation bilan (couples Cr₂O₇²⁻/Cr³⁺ et CH₃COOH/C₂H₅OH).\n2) Pourquoi la couleur du test indique-t-elle la présence d’alcool ?",
  "a": "1) Cr₂O₇²⁻ + 14H⁺ + 6e⁻ → 2Cr³⁺ + 7H₂O ; C₂H₅OH + H₂O → CH₃COOH + 4H⁺ + 4e⁻. Bilan : 2Cr₂O₇²⁻ + 3C₂H₅OH + 16H⁺ → 4Cr³⁺ + 3CH₃COOH + 11H₂O.\n2) Plus il y a d’éthanol, plus de dichromate orange est transformé en ions Cr³⁺ verts."},
]

SERIES["Chimie – Chapitre II : Estérification, hydrolyse et saponification"] = [
 {"k": "book", "src": "p. 51, ex. 1", "q": "On porte à 100 °C 7,2 g d’eau et 40,8 g d’ester CH₃–COO–CH(CH₃)–CH₃ (Correction : le manuel l’appelle « éthanoate de méthyle » ; c’est l’éthanoate de 1-méthyléthyle). 1) Quantités initiales. 2) Équation et produits. 3) À l’équilibre il reste 24,48 g d’ester : composition, constante K, rendement. 4) Effet d’un ajout d’eau.",
  "a": "1) M(ester) = 102 g/mol : 0,40 mol ; eau 0,40 mol.\n2) CH₃COOCH(CH₃)₂ + H₂O ⇌ CH₃COOH + (CH₃)₂CHOH : acide éthanoïque et propan-2-ol (hydrolyse). La température accélère la réaction mais ne déplace pas l’équilibre (réaction athermique).\n3) Ester restant 0,24 mol : 0,24 mol d’eau, 0,16 mol d’acide et 0,16 mol d’alcool. K = 0,16²/0,24² ≈ 0,44 ; rendement de l’hydrolyse 40 %.\n4) L’équilibre se déplace dans le sens de l’hydrolyse."},
 {"k": "book", "src": "p. 52, ex. 2", "q": "A et B (C, H, O ; chaînes saturées) ont la même masse molaire et des formules différentes. L’oxydation ménagée de A donne C (DNPH +, Fehling −). A et B donnent un ester D de masse molaire 130 g/mol. Identifier A, B, C, D et l’alcène A′ dont l’hydratation donne A.",
  "a": "C est une cétone donc A est un alcool secondaire ; B est un acide carboxylique. D : CₙH₂ₙO₂, 14n + 32 = 130 ⇒ n = 7. Même masse molaire : alcool C₄H₁₀O (74) et acide C₃H₆O₂ (74). A = butan-2-ol ; B = acide propanoïque ; C = butanone ; D = CH₃–CH₂–COO–CH(CH₃)–CH₂–CH₃ (propanoate de 1-méthylpropyle). A′ : but-1-ène ou but-2-ène."},
 {"k": "book", "src": "p. 52, ex. 3 (4)", "q": "1 mol de chlorure de propanoyle réagit avec 1 mol d’un alcool R–OH. 1) Équation. 2) La réaction est-elle limitée ? 3) On obtient 102 g d’ester : formules et noms de l’ester et de l’alcool.",
  "a": "1) CH₃CH₂COCl + R–OH → CH₃CH₂COOR + HCl.\n2) Non : avec un chlorure d’acyle la réaction est rapide et totale.\n3) M(ester) = 102 g/mol : C₅H₁₀O₂ ⇒ R = C₂H₅ : propanoate d’éthyle CH₃CH₂COOCH₂CH₃ ; l’alcool est l’éthanol."},
 {"k": "book", "src": "p. 182, ex. 1", "q": "On chauffe 2,3 g d’acide méthanoïque et 1,6 g de méthanol ; à l’équilibre il reste 0,76 g d’acide. 1) Équation et caractéristiques. 2) Rendement. 3) Synthèse avec l’anhydride méthanoïque : équation, masse maximale d’ester et masse d’anhydride nécessaire.",
  "a": "1) HCOOH + CH₃OH ⇌ HCOOCH₃ + H₂O : lente, limitée, athermique.\n2) 0,050 mol de chaque réactif ; acide restant 0,0165 mol ⇒ ester 0,0335 mol : rendement ≈ 67 %.\n3) (HCO)₂O + CH₃OH → HCOOCH₃ + HCOOH, rapide et totale. Ester maximal 0,050 mol = 3,0 g ; anhydride 0,050 mol × 74 = 3,7 g."},
 {"k": "extra", "src": "", "q": "On saponifie 8,8 g d’éthanoate d’éthyle par un excès de soude. Équation, nom des produits et masse d’éthanoate de sodium obtenue.",
  "a": "CH₃COOC₂H₅ + Na⁺ + HO⁻ → CH₃COO⁻ + Na⁺ + C₂H₅OH (totale). n = 8,8/88 = 0,10 mol ⇒ 0,10 mol d’éthanoate de sodium (82 g/mol) : 8,2 g."},
 {"k": "int", "src": "", "q": "Un savonnier traite 890 kg de trioléine (triester du glycérol et de l’acide oléique, M = 884 g/mol) par la soude.\n1) Écrire l’équation générale de la saponification d’un triglycéride.\n2) Quelle masse de savon (oléate de sodium, M = 304 g/mol) obtient-on au maximum ?",
  "a": "1) Triglycéride + 3(Na⁺ + HO⁻) → glycérol + 3(RCOO⁻ + Na⁺).\n2) n ≈ 890 000/884 ≈ 1007 mol ⇒ 3021 mol de savon ≈ 918 kg."},
]

SERIES["Chimie – Chapitre III : Les amides"] = [
 {"k": "book", "src": "p. 62, ex. 1", "q": "On veut préparer le propanamide (A) à partir du propan-1-ol (B). 1) Formules et familles. 2) Oxydation de B en C, réaction de C avec l’ammoniac (produit D), puis déshydratation de D. 3) Utilisation d’un dérivé E de C (obtenu par PCl₅ ou SOCl₂).",
  "a": "1) A : CH₃CH₂CONH₂ (amide) ; B : CH₃CH₂CH₂OH (alcool primaire).\n2) C : acide propanoïque CH₃CH₂COOH. C + NH₃ → CH₃CH₂COO⁻NH₄⁺ (D, propanoate d’ammonium) : réaction acide-base. Par chauffage : CH₃CH₂COONH₄ → CH₃CH₂CONH₂ + H₂O.\n3) E : chlorure de propanoyle CH₃CH₂COCl ; CH₃CH₂COCl + 2NH₃ → CH₃CH₂CONH₂ + NH₄Cl (rapide et totale)."},
 {"k": "book", "src": "p. 63, ex. 2 (4)", "q": "L’acide propanoïque B réagit sur une amine secondaire E pour donner F contenant 14,16 % d’oxygène. Fonction et formule de F.",
  "a": "F est un amide N,N-disubstitué de formule CₙH₂ₙ₊₁NO : M = 16/0,1416 ≈ 113 g/mol ⇒ 14n + 31 = 113 ⇒ n ≈ 6 (C₆H₁₃NO). Avec l’acide propanoïque (3 C), l’amine contient 3 C, par exemple la N-méthyléthanamine : F = CH₃CH₂CON(CH₃)C₂H₅ (N-éthyl-N-méthylpropanamide)."},
 {"k": "extra", "src": "", "q": "Nommer : a) CH₃CONH₂ ; b) CH₃CH₂CONHCH₃ ; c) HCON(CH₃)₂.",
  "a": "a) Éthanamide. b) N-méthylpropanamide. c) N,N-diméthylméthanamide."},
 {"k": "extra", "src": "", "q": "Écrire la réaction de l’anhydride éthanoïque avec la méthylamine. Quel est l’intérêt d’utiliser l’anhydride plutôt que l’acide ?",
  "a": "(CH₃CO)₂O + 2CH₃NH₂ → CH₃CONHCH₃ + CH₃COO⁻ + CH₃NH₃⁺. La réaction est rapide et totale, alors qu’avec l’acide on forme d’abord un sel qu’il faut chauffer longtemps."},
 {"k": "int", "src": "", "q": "Le paracétamol est un amide obtenu à partir du 4-aminophénol (M = 109 g/mol) et de l’anhydride éthanoïque.\n1) Quel groupe du 4-aminophénol réagit ?\n2) Avec 10,9 g de 4-aminophénol et un rendement de 80 %, quelle masse de paracétamol (M = 151 g/mol) obtient-on ?",
  "a": "1) Le groupe amino –NH₂, qui devient –NH–CO–CH₃.\n2) n = 0,100 mol ⇒ 0,080 mol de paracétamol ≈ 12,1 g."},
]

SERIES["Chimie – Chapitre IV : Évolution quantitative d’un système chimique"] = [
 {"k": "book", "src": "p. 103, ex. 1", "q": "Décomposition de l’eau oxygénée : 2H₂O₂ → 2H₂O + O₂ ; à t = 0 on a 0,06 mol de H₂O₂ dans 1 L. On mesure le volume V de dioxygène formé. 1) n(O₂) en fonction de V et Vm. 2) Quantité de H₂O₂ disparue et concentration c restante.",
  "a": "1) n(O₂) = V/Vm.\n2) n(H₂O₂) disparu = 2V/Vm ; c = 0,06 − 2V/Vm (mol/L, volume 1 L)."},
 {"k": "book", "src": "p. 104, ex. 4 (1, 2)", "q": "On mélange 50 mL de peroxodisulfate (5 × 10⁻² mol/L) et 50 mL d’iodure de potassium (16 × 10⁻² mol/L). Équation, tableau d’avancement, réactif limitant et concentration finale en diiode.",
  "a": "S₂O₈²⁻ + 2I⁻ → 2SO₄²⁻ + I₂. n(S₂O₈²⁻) = 2,5 × 10⁻³ mol ; n(I⁻) = 8,0 × 10⁻³ mol (xmax = 4 × 10⁻³). S₂O₈²⁻ est limitant : xmax = 2,5 × 10⁻³ mol ; [I₂]final = 2,5 × 10⁻³/0,100 = 2,5 × 10⁻² mol/L."},
 {"k": "extra", "src": "", "q": "Définir l’avancement volumique y. Pour la réaction précédente, exprimer [I⁻] en fonction de [I⁻]₀ et de y.",
  "a": "y = x/V (mol/L). [I⁻] = [I⁻]₀ − 2y."},
 {"k": "extra", "src": "", "q": "On dose le diiode formé par le thiosulfate : I₂ + 2S₂O₃²⁻ → 2I⁻ + S₄O₆²⁻. Un prélèvement de 10 mL nécessite 12 mL de thiosulfate à 0,02 mol/L. Concentration du diiode ?",
  "a": "n(I₂) = ½ × 0,02 × 0,012 = 1,2 × 10⁻⁴ mol ⇒ [I₂] = 1,2 × 10⁻² mol/L."},
 {"k": "int", "src": "", "q": "Un comprimé effervescent contient 1,7 g d’hydrogénocarbonate de sodium (M = 84 g/mol) qui réagit avec un excès d’acide : HCO₃⁻ + H⁺ → CO₂ + H₂O (Vm = 24 L/mol).\n1) Quel volume de gaz se dégage au maximum ?\n2) On mesure 300 mL au bout de 30 s : quel est l’avancement à cet instant ?",
  "a": "1) n = 1,7/84 ≈ 0,020 mol ⇒ V ≈ 0,49 L.\n2) x = 0,300/24 = 0,0125 mol (environ 62 % de l’avancement maximal)."},
]

SERIES["Chimie – Chapitre V : Vitesses de réaction et temps de demi-réaction"] = [
 {"k": "book", "src": "p. 104, ex. 3", "q": "Oxydation des ions iodure par S₂O₈²⁻ : on mélange 50 mL de S₂O₈²⁻ (C₁) et 50 mL de KI (0,1 mol/L). 1) [I⁻]₀ dans le mélange. 2) Montrer que la vitesse volumique s’écrit v = −½ d[I⁻]/dt. 3) Dosage par le thiosulfate : montrer que [I₂] = C·Véq/(2V₀).",
  "a": "1) [I⁻]₀ = 0,1 × 50/100 = 0,05 mol/L ; [S₂O₈²⁻]₀ = C₁/2.\n2) v = dy/dt et [I⁻] = [I⁻]₀ − 2y ⇒ v = −½ d[I⁻]/dt.\n3) À l’équivalence n(I₂) = ½ n(S₂O₃²⁻) = C·Véq/2 ⇒ [I₂] = C·Véq/(2V₀)."},
 {"k": "extra", "src": "", "q": "Définir le temps de demi-réaction t½. Une réaction a pour avancement final 2,5 × 10⁻³ mol ; à quelle valeur de x correspond t½ ?",
  "a": "t½ est la durée au bout de laquelle l’avancement atteint la moitié de sa valeur finale : x(t½) = 1,25 × 10⁻³ mol."},
 {"k": "extra", "src": "", "q": "Comment détermine-t-on graphiquement la vitesse volumique à un instant t à partir de la courbe y = f(t) ? Comment évolue-t-elle au cours du temps et pourquoi ?",
  "a": "C’est le coefficient directeur de la tangente à la courbe au point d’abscisse t. Elle diminue au cours du temps car la concentration des réactifs diminue."},
 {"k": "extra", "src": "", "q": "La concentration de diiode passe de 0 à 8 × 10⁻³ mol/L en 4 min puis à 1,2 × 10⁻² mol/L en 8 min. Calculer les vitesses moyennes sur [0 ; 4] et [4 ; 8] min. Conclusion ?",
  "a": "2 × 10⁻³ puis 1 × 10⁻³ mol·L⁻¹·min⁻¹ : la réaction ralentit."},
 {"k": "int", "src": "", "q": "Le lait se conserve 3 jours à 4 °C mais aigrit en 12 heures à 30 °C.\n1) Quel facteur cinétique intervient ?\n2) Pourquoi conserve-t-on les aliments au réfrigérateur ?",
  "a": "1) La température : la vitesse des réactions (ici les transformations dues aux bactéries) augmente avec la température.\n2) Le froid ralentit les réactions de dégradation."},
]

SERIES["Chimie – Chapitre VI : Facteurs cinétiques et catalyse"] = [
 {"k": "book", "src": "p. 103, ex. 2", "q": "On mélange de l’eau oxygénée et des ions dichromate en milieu acide (réaction totale). 1) Demi-équations et équation bilan (Cr₂O₇²⁻/Cr³⁺ ; O₂/H₂O₂). 2) Définir un catalyseur. 3) Comment reconnaître, sur la courbe n(H₂O₂) = f(t), l’expérience réalisée avec catalyseur ?",
  "a": "1) Cr₂O₇²⁻ + 14H⁺ + 6e⁻ → 2Cr³⁺ + 7H₂O ; H₂O₂ → O₂ + 2H⁺ + 2e⁻. Bilan : Cr₂O₇²⁻ + 3H₂O₂ + 8H⁺ → 2Cr³⁺ + 3O₂ + 7H₂O.\n2) Une substance qui accélère une réaction sans être consommée et sans modifier l’état final.\n3) La courbe décroît plus vite mais atteint la même valeur finale."},
 {"k": "book", "src": "p. 104, ex. 4", "q": "Dans le mélange peroxodisulfate/iodure, on prélève 10 mL qu’on verse dans de l’eau glacée avant de doser le diiode. Pourquoi ? Quel rôle joue l’empois d’amidon ?",
  "a": "L’eau glacée refroidit et dilue : la réaction est pratiquement stoppée (trempe), ce qui permet de doser la quantité de diiode présente à la date du prélèvement. L’empois d’amidon colore en bleu nuit le diiode : sa disparition repère l’équivalence."},
 {"k": "extra", "src": "", "q": "Citer trois facteurs cinétiques et donner un exemple de chacun dans la vie courante.",
  "a": "Température (cuisson plus rapide en autocuiseur), concentration des réactifs (un détartrant concentré agit plus vite), catalyseur (pot catalytique, enzymes de la digestion). On peut aussi citer la surface de contact (sucre en poudre qui se dissout plus vite)."},
 {"k": "extra", "src": "", "q": "Distinguer catalyse homogène, hétérogène et enzymatique, avec un exemple pour chacune.",
  "a": "Homogène : catalyseur et réactifs dans la même phase (ions Fe²⁺ pour l’oxydation des iodures par S₂O₈²⁻). Hétérogène : phases différentes (platine du pot catalytique). Enzymatique : catalyseur biologique (catalase qui décompose l’eau oxygénée)."},
 {"k": "int", "src": "", "q": "L’eau oxygénée d’une pharmacie se décompose lentement : 2H₂O₂ → 2H₂O + O₂. Sur une plaie, elle mousse immédiatement.\n1) Quel est le rôle du sang dans cette observation ?\n2) Pourquoi conserve-t-on l’eau oxygénée dans un flacon opaque, au frais ?",
  "a": "1) Le sang contient une enzyme, la catalase, qui catalyse la décomposition : le dioxygène se dégage très vite.\n2) La lumière et la chaleur accélèrent la décomposition."},
]

SERIES["Chimie – Chapitre VII : Solutions acides et basiques — pH, acides et bases forts"] = [
 {"k": "book", "src": "p. 132, ex. 1", "q": "Une solution d’acide chlorhydrique a pH = 2,1. 1) Équation de dissolution de HCl. 2) Quantité d’ions H₃O⁺ dans 1 L. 3) Volume de gaz dissous (Vm = 24 L/mol).",
  "a": "1) HCl + H₂O → H₃O⁺ + Cl⁻ (totale).\n2) n = 10⁻²·¹ ≈ 7,9 × 10⁻³ mol.\n3) V ≈ 0,19 L."},
 {"k": "book", "src": "p. 132, ex. 2", "q": "Un flacon d’acide nitrique (densité 1,2) contient 76 % en masse de HNO₃. 1) Concentration C. 2) Volume à prélever pour 2 L de solution de pH 1,5.",
  "a": "1) 1 L pèse 1200 g dont 912 g de HNO₃ (63 g/mol) : C ≈ 14,5 mol/L.\n2) n = 2 × 10⁻¹·⁵ ≈ 6,3 × 10⁻² mol ⇒ V ≈ 4,4 mL."},
 {"k": "book", "src": "p. 132, ex. 3", "q": "On dissout 0,2 g de NaOH dans 200 cm³ d’eau. 1) pH. 2) Volume d’eau à ajouter à 20 mL pour obtenir pH = 11.",
  "a": "1) c = 0,005/0,2 = 0,025 mol/L ; pH = 14 + log 0,025 ≈ 12,4.\n2) Il faut diviser par 25 : volume final 500 mL, soit 480 mL d’eau."},
 {"k": "book", "src": "p. 133, ex. 5", "q": "Solution A d’acide chlorhydrique de pH 1,3. 1) Concentrations des espèces. 2) Volume de HCl gazeux (CNTP, 22,4 L/mol) pour 500 mL. 3) Volume de A et d’eau pour 1 L de solution B de pH 2.",
  "a": "1) [H₃O⁺] = [Cl⁻] ≈ 5,0 × 10⁻² mol/L ; [HO⁻] ≈ 2 × 10⁻¹³ mol/L.\n2) n = 2,5 × 10⁻² mol ⇒ V ≈ 0,56 L.\n3) n = 0,01 mol ⇒ VA = 0,2 L et 0,8 L d’eau."},
 {"k": "book", "src": "p. 133, ex. 6 (5, 6)", "q": "1) On mélange 20 mL d’acide chlorhydrique à 0,01 mol/L et 8 mL de soude à 5 × 10⁻³ mol/L : pH du mélange. 2) Quel volume de soude faut-il pour obtenir pH = 7 ?",
  "a": "1) n(H₃O⁺) = 2 × 10⁻⁴ mol ; n(HO⁻) = 4 × 10⁻⁵ mol ; il reste 1,6 × 10⁻⁴ mol dans 28 mL : [H₃O⁺] ≈ 5,7 × 10⁻³ mol/L, pH ≈ 2,2.\n2) Équivalence : Vb = 2 × 10⁻⁴/5 × 10⁻³ = 40 mL."},
 {"k": "extra", "src": "", "q": "Montrer que la dilution d’un facteur 10 d’un acide fort augmente le pH d’une unité (tant que pH < 6).",
  "a": "pH = −log c ; après dilution c′ = c/10 ⇒ pH′ = −log c + 1."},
 {"k": "int", "src": "", "q": "Un aquarium de 100 L doit avoir un pH de 7 ; l’eau est à pH 8. Le vendeur propose une solution d’acide chlorhydrique à 0,1 mol/L.\n1) Pourquoi ne peut-on pas simplement calculer le volume d’acide comme pour une solution sans tampon ?\n2) Quelle précaution prendre ?",
  "a": "1) L’eau naturelle contient des espèces (hydrogénocarbonates) qui consomment l’acide (effet tampon) : le calcul simple sous-estime la quantité nécessaire.\n2) Ajouter l’acide progressivement en contrôlant le pH, sans dépasser la valeur voulue (danger pour les poissons)."},
]

SERIES["Chimie – Chapitre VIII : Couples acide/base, Ka et pKa"] = [
 {"k": "book", "src": "p. 153, ex. 2", "q": "Calculer Ka et pKa d’un monoacide faible AH à 0,10 mol/L dont la solution a un pH de 3,6.",
  "a": "[H₃O⁺] = [A⁻] ≈ 2,5 × 10⁻⁴ mol/L ; [AH] ≈ 0,10 mol/L ; Ka = [H₃O⁺]²/[AH] ≈ 6,3 × 10⁻⁷ ; pKa ≈ 6,2."},
 {"k": "book", "src": "p. 153, ex. 3", "q": "Une solution d’ammoniac a pH = 11,2 ; pKa(NH₄⁺/NH₃) = 9,2. Espèces présentes et concentration initiale en ammoniac.",
  "a": "[HO⁻] = 10⁻²·⁸ ≈ 1,6 × 10⁻³ mol/L = [NH₄⁺] ; [H₃O⁺] ≈ 6,3 × 10⁻¹² mol/L. [NH₃] = [NH₄⁺] × 10^(pH − pKa) = 1,6 × 10⁻³ × 100 ≈ 0,16 mol/L ; c₀ ≈ 0,16 mol/L."},
 {"k": "book", "src": "p. 153, ex. 4", "q": "Acide formique à 10⁻² mol/L : pH = 2,9 ; après dilution par 10 : pH = 3,4. 1) Acide fort ? 2) Coefficients d’ionisation α₁ et α₂. Conclusion.",
  "a": "1) Non : un acide fort aurait pH = 2 puis 3.\n2) α₁ = 10⁻²·⁹/10⁻² ≈ 0,13 ; α₂ = 10⁻³·⁴/10⁻³ ≈ 0,40. La dilution augmente l’ionisation d’un acide faible."},
 {"k": "book", "src": "p. 154, ex. 7", "q": "Acide méthanoïque à 0,1 mol/L : pH = 2,4. 1) Concentrations, Ka, pKa, α. 2) Un mélange de 50 mL de cet acide et 50 mL de méthanoate de sodium à 0,2 mol/L a pH = 4,1 : retrouver le pKa.",
  "a": "1) [H₃O⁺] = [HCOO⁻] ≈ 4,0 × 10⁻³ mol/L ; [HCOOH] ≈ 0,096 mol/L ; Ka ≈ 1,65 × 10⁻⁴ ; pKa ≈ 3,8 ; α ≈ 4 %.\n2) [HCOOH] ≈ 0,05 et [HCOO⁻] ≈ 0,1 mol/L ; pKa = pH − log([HCOO⁻]/[HCOOH]) = 4,1 − 0,3 = 3,8."},
 {"k": "book", "src": "p. 154, ex. 8", "q": "Deux acides A₁H et A₂H ont pH = 3 ; après dilution par 10 : pH = 3,5 et pH = 4. 1) Lequel est fort ? Concentration. 2) 20 cm³ de l’acide faible sont dosés par 11,5 cm³ de soude à 0,1 mol/L : concentration.",
  "a": "1) A₂H (le pH augmente d’une unité) : c = 10⁻³ mol/L.\n2) c = 0,1 × 11,5/20 ≈ 5,75 × 10⁻² mol/L."},
 {"k": "book", "src": "p. 154, ex. 6", "q": "On dissout 2,46 g d’éthanoate de sodium dans 0,3 L ; pH = 8,9. Concentration ; base forte ou faible ? Équation avec l’eau.",
  "a": "c = 2,46/82/0,3 = 0,10 mol/L. Une base forte donnerait pH = 13 : c’est une base faible. CH₃COO⁻ + H₂O ⇌ CH₃COOH + HO⁻ (couples CH₃COOH/CH₃COO⁻ et H₂O/HO⁻)."},
 {"k": "int", "src": "", "q": "Le vinaigre contient de l’acide éthanoïque (pKa = 4,8). Son pH est 2,4.\n1) Quelle espèce prédomine dans le vinaigre : CH₃COOH ou CH₃COO⁻ ?\n2) Dans l’intestin (pH 8), quelle est l’espèce prédominante ?",
  "a": "1) pH < pKa : CH₃COOH prédomine ([CH₃COO⁻]/[CH₃COOH] = 10⁻²·⁴).\n2) pH > pKa : l’ion éthanoate prédomine (rapport 10³·²)."},
]

SERIES["Chimie – Chapitre IX : Dosages acido-basiques et solutions tampons"] = [
 {"k": "book", "src": "p. 183, ex. 2", "q": "On mélange 0,50 mol d’acide éthanoïque et 0,50 mol de propan-1-ol (ρ = 0,80 g/mL). Le contenu d’un erlenmeyer est dilué à 100 mL ; 5 mL sont titrés par la soude à 1 mol/L : Véq = 14,2 mL. 1) Volume d’alcool. 2) Constante de la réaction de titrage (pKa = 4,8). 3) Acide restant et ester formé.",
  "a": "1) V = 0,50 × 60/0,80 = 37,5 mL.\n2) K = Ka/Ke = 10⁹·² : réaction totale.\n3) 5 mL contiennent 1,42 × 10⁻² mol d’acide ⇒ 0,284 mol dans l’erlenmeyer ; ester formé 0,216 mol."},
 {"k": "book", "src": "p. 184, ex. 3", "q": "On mélange 36 g de propan-1-ol et 36 g d’acide éthanoïque (K = 4). 1) Quantités initiales. 2) Composition à l’équilibre. 3) Quantité X de propan-1-ol à ajouter pour obtenir 0,56 mol d’ester.",
  "a": "1) 0,60 mol de chaque.\n2) Mélange équimolaire avec K = 4 : 2/3 d’estérification : 0,40 mol d’ester et d’eau, 0,20 mol d’acide et d’alcool.\n3) 0,56²/(0,04 × (0,04 + X)) = 4 ⇒ X ≈ 1,92 mol."},
 {"k": "extra", "src": "", "q": "On dose 20 mL d’acide éthanoïque par la soude à 0,10 mol/L ; Véq = 16 mL. 1) Concentration de l’acide. 2) pH à la demi-équivalence. 3) Le pH à l’équivalence est-il égal, inférieur ou supérieur à 7 ? Indicateur adapté ?",
  "a": "1) c = 0,10 × 16/20 = 0,080 mol/L.\n2) pH = pKa = 4,8.\n3) Supérieur à 7 (il reste des ions éthanoate, basiques) : la phénolphtaléine convient."},
 {"k": "extra", "src": "", "q": "Qu’est-ce qu’une solution tampon ? Comment préparer 100 mL d’une solution tampon de pH 4,8 avec de l’acide éthanoïque et de l’éthanoate de sodium à 0,1 mol/L ?",
  "a": "Une solution dont le pH varie peu par ajout modéré d’acide, de base ou par dilution. Il faut [CH₃COOH] = [CH₃COO⁻] : on mélange 50 mL de chaque solution."},
 {"k": "int", "src": "", "q": "Pour contrôler un vinaigre « 6° » (6 g d’acide éthanoïque pour 100 g, ρ ≈ 1), on dilue 10 fois le vinaigre puis on dose 10 mL de la solution diluée par la soude à 0,10 mol/L ; Véq = 10 mL.\n1) Concentration de la solution diluée puis du vinaigre.\n2) Le vinaigre est-il conforme à l’étiquette ?",
  "a": "1) c = 0,10 × 10/10 = 0,10 mol/L ; vinaigre : 1,0 mol/L, soit 60 g/L.\n2) 60 g/L correspond à 6 g pour 100 mL (≈ 100 g) : il est conforme."},
]
