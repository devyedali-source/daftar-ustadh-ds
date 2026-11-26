# Série d'exercices de fin de chapitre — Chimie 6C/6D.
# k = "book" : exercice du manuel (src = page et numéro) ; "extra" : exercice du même type ; "int" : situation d'intégration.
SERIES = {}

SERIES["Chimie – Chapitre I : Ressources organiques naturelles et analyse élémentaire"] = [
 {"k": "book", "src": "p. 14, exercice résolu", "q": "La combustion complète de 3,6 g d’un composé CₓHᵧO_z fournit 8,7 g de CO₂ et 3,7 g d’eau ; la densité de sa vapeur par rapport à l’air est 2,48.\na) Composition centésimale massique. b) Masse molaire. c) Formule brute.",
  "a": "a) m(C) = 8,7 × 12/44 ≈ 2,4 g ⇒ %C ≈ 66,7 % ; m(H) = 3,7 × 2/18 ≈ 0,4 g ⇒ %H ≈ 11,1 % ; %O ≈ 22,2 %.\nb) M = 29d = 29 × 2,48 ≈ 72 g/mol.\nc) 12x/66,7 = y/11,1 = 16z/22,2 = 72/100 ⇒ x = 4, y = 8, z = 1 : C₄H₈O."},
 {"k": "book", "src": "p. 15, application", "q": "Calculer les pourcentages massiques de carbone, d’hydrogène et d’oxygène de l’éthanol C₂H₆O.",
  "a": "M = 46 g/mol. %C = 24/46 ≈ 52,2 % ; %H = 6/46 ≈ 13,0 % ; %O = 16/46 ≈ 34,8 %."},
 {"k": "book", "src": "p. 17, ex. 1", "q": "Classer en substances naturelles et synthétiques : plastique, bois, fruits, médicaments, air, colorants, essence, cuivre, verre, propane, peinture, sang, eau.",
  "a": "Naturelles : bois, fruits, air, cuivre, sang, eau (et le propane du gaz naturel). Synthétiques (fabriquées par l’homme) : plastique, médicaments, colorants, essence (issue du raffinage), verre, peinture. (Certains, comme les colorants ou les médicaments, peuvent aussi être d’origine naturelle.)"},
 {"k": "book", "src": "p. 17, ex. 2", "q": "1) Peut-on utiliser le pétrole brut tel quel ? 2) Principe de la distillation fractionnée. 3) Les dérivés du pétrole sont-ils naturels ou synthétiques ? 4) Citer deux matériaux synthétisés à partir de ces dérivés.",
  "a": "1) Non : c’est un mélange de nombreux hydrocarbures qu’il faut séparer (raffinage).\n2) On chauffe le pétrole ; les constituants se séparent selon leur température d’ébullition et se condensent à différentes hauteurs de la colonne.\n3) Les fractions obtenues par distillation sont des produits naturels séparés ; les produits obtenus ensuite par réactions chimiques sont synthétiques.\n4) Matières plastiques (polyéthylène, PVC), fibres textiles (nylon), caoutchouc synthétique…"},
 {"k": "extra", "src": "", "q": "Un hydrocarbure contient 85,7 % de carbone en masse ; sa masse molaire est 56 g/mol. Déterminer sa formule brute.",
  "a": "x = 0,857 × 56/12 = 4 ; y = 0,143 × 56/1 = 8 : C₄H₈."},
 {"k": "extra", "src": "", "q": "Comment met-on en évidence la présence de carbone et d’hydrogène dans un composé organique ?",
  "a": "On le brûle : l’eau formée (hydrogène) se condense en buée et colore en bleu le sulfate de cuivre anhydre ; le dioxyde de carbone (carbone) trouble l’eau de chaux."},
 {"k": "int", "src": "", "q": "Un laboratoire analyse un produit vendu comme « vinaigre pur ». Le composé actif contient 40,0 % de C, 6,7 % de H et 53,3 % de O ; sa masse molaire est 60 g/mol.\n1) Déterminer sa formule brute.\n2) Le produit pur est l’acide éthanoïque CH₃COOH : le résultat est-il cohérent ?",
  "a": "1) x = 0,40 × 60/12 = 2 ; y = 0,067 × 60 = 4 ; z = 0,533 × 60/16 = 2 : C₂H₄O₂.\n2) CH₃COOH a pour formule brute C₂H₄O₂ : le résultat est cohérent."},
]

SERIES["Chimie – Chapitre II : Les alcanes"] = [
 {"k": "book", "src": "p. 32, ex. 6", "q": "Une bouteille contient 6 kg de gaz C₄H₁₀ (Vm = 24 L/mol).\n1) Famille. 2) Nom et les deux formules développées. 3) Équation de combustion complète. 4) Masse molaire. 5) Nombre de moles. 6) Volume de dioxygène nécessaire.",
  "a": "1) Les alcanes.\n2) Butane : CH₃–CH₂–CH₂–CH₃ et méthylpropane CH₃–CH(CH₃)–CH₃.\n3) 2C₄H₁₀ + 13O₂ → 8CO₂ + 10H₂O.\n4) M = 58 g/mol.\n5) n = 6000/58 ≈ 103 mol.\n6) n(O₂) = 6,5 × 103 ≈ 672 mol ⇒ V ≈ 16,1 m³."},
 {"k": "book", "src": "p. 32, ex. 7", "q": "Un alcane a pour formule C₅H₁₂. 1) Formule développée et nom. 2) Masse molaire. 3) Équation de combustion équilibrée. 4) Masse de dioxygène pour 1 mol d’alcane.",
  "a": "1) CH₃–CH₂–CH₂–CH₂–CH₃ : pentane (isomères : méthylbutane, diméthylpropane).\n2) M = 72 g/mol.\n3) C₅H₁₂ + 8O₂ → 5CO₂ + 6H₂O.\n4) 8 mol, soit 256 g."},
 {"k": "book", "src": "p. 32, ex. 8", "q": "On récupère 2100 L de méthane par jour (Vm = 24 L/mol). 1) Nombre de moles. 2) Équation de combustion. 3) Moles et volume de dioxygène nécessaires.",
  "a": "1) n = 2100/24 = 87,5 mol.\n2) CH₄ + 2O₂ → CO₂ + 2H₂O.\n3) 175 mol, soit 4200 L."},
 {"k": "book", "src": "p. 32, ex. 9", "q": "Le butane est un alcane à 4 atomes de carbone : formule brute, développée et semi-développée. Formule brute d’un alcane à n atomes de carbone.",
  "a": "C₄H₁₀ ; CH₃–CH₂–CH₂–CH₃. Formule générale CₙH₂ₙ₊₂."},
 {"k": "extra", "src": "", "q": "Nommer : a) CH₃–CH(CH₃)–CH₂–CH₃ ; b) CH₃–C(CH₃)₂–CH₂–CH(CH₃)–CH₃. Écrire la formule du 3-éthyl-2-méthylhexane.",
  "a": "a) 2-méthylbutane. b) 2,2,4-triméthylpentane. 3-éthyl-2-méthylhexane : CH₃–CH(CH₃)–CH(C₂H₅)–CH₂–CH₂–CH₃."},
 {"k": "extra", "src": "", "q": "Écrire les équations de monochloration du méthane à la lumière. Combien de dérivés monochlorés donne le propane ?",
  "a": "CH₄ + Cl₂ → CH₃Cl + HCl (substitution, à la lumière ; on peut aller jusqu’à CCl₄). Le propane donne deux dérivés monochlorés : 1-chloropropane et 2-chloropropane."},
 {"k": "int", "src": "", "q": "Une famille utilise une bouteille de 12,5 kg de butane en un mois.\n1) Quelle masse de CO₂ rejette-t-elle ?\n2) Une voiture rejette environ 120 g de CO₂ par km. À combien de kilomètres en voiture cela correspond-il ?",
  "a": "1) n(C₄H₁₀) = 12 500/58 ≈ 216 mol ⇒ n(CO₂) = 4 × 216 ≈ 862 mol ⇒ m ≈ 37,9 kg.\n2) 37 900/120 ≈ 316 km."},
]

SERIES["Chimie – Chapitre III : Les alcynes"] = [
 {"k": "book", "src": "p. 53, ex. 1 (1-1)", "q": "La combustion complète de 40 mL d’un mélange de propane et de propène a nécessité 192,5 mL de dioxygène. Composition volumique du mélange ?",
  "a": "C₃H₈ + 5O₂ → 3CO₂ + 4H₂O et C₃H₆ + 9/2 O₂ → 3CO₂ + 3H₂O. Avec x mL de propane : 5x + 4,5(40 − x) = 192,5 ⇒ x = 25 mL. 62,5 % de propane et 37,5 % de propène (réponse A)."},
 {"k": "book", "src": "p. 53, ex. 1 (1-2)", "q": "1,68 g d’un alcène X fixe 2,4 g de dibrome. Identifier X parmi C₄H₈, C₈H₁₆, C₅H₁₀. (Correction : le manuel écrit « 168 g », ce qui est impossible.)",
  "a": "n(Br₂) = 2,4/160 = 0,015 mol = n(X) ⇒ M = 1,68/0,015 = 112 g/mol = 14n ⇒ n = 8 : C₈H₁₆."},
 {"k": "book", "src": "p. 53, ex. 2", "q": "Compléter : a) CH₃–CH=CH₂ + Br₂ ; b) CH₃–CH₂–CH=CH₂ + H₂O ; c) n CH₂=CHCl ; d) CH₃–CH=CH–CH₃ + H₂ (platine) ; e) HC≡CH + H₂O (Hg²⁺).",
  "a": "a) CH₃–CHBr–CH₂Br (1,2-dibromopropane).\nb) butan-2-ol (majoritaire) et butan-1-ol.\nc) –(CH₂–CHCl)ₙ– : polychlorure de vinyle (PVC).\nd) CH₃–CH₂–CH₂–CH₃ (butane).\ne) CH₂=CHOH (énol instable) → CH₃–CHO (éthanal)."},
 {"k": "book", "src": "p. 53, ex. 3", "q": "Écrire les formules de : a) 2,6-diméthylhept-3-yne ; b) 5-méthylhex-2-ène ; c) 2-méthyl-4-éthylhex-2-ène ; d) cyclohexène.",
  "a": "a) CH₃–CH(CH₃)–C≡C–CH₂–CH(CH₃)–CH₃.\nb) CH₃–CH=CH–CH₂–CH(CH₃)–CH₃.\nc) (CH₃)₂C=CH–CH(C₂H₅)–CH₂–CH₃.\nd) Cycle de 6 carbones comportant une double liaison (C₆H₁₀)."},
 {"k": "book", "src": "p. 53, ex. 4", "q": "On réalise l’addition d’eau sur le butène C₄H₈. a) Combien d’isomères du butène à chaîne non ramifiée ? b) Équations des réactions possibles ; combien de butanols différents obtient-on ?",
  "a": "a) Deux : but-1-ène et but-2-ène (le but-2-ène existant en Z et E).\nb) but-1-ène + H₂O → butan-1-ol ou butan-2-ol ; but-2-ène + H₂O → butan-2-ol. On obtient deux butanols."},
 {"k": "extra", "src": "", "q": "L’acétylène est préparé par action de l’eau sur le carbure de calcium : CaC₂ + 2H₂O → C₂H₂ + Ca(OH)₂. Quel volume d’acétylène (Vm = 24 L/mol) obtient-on avec 32 g de carbure ? Écrire l’équation de sa combustion complète.",
  "a": "n(CaC₂) = 32/64 = 0,50 mol ⇒ V(C₂H₂) = 12 L. 2C₂H₂ + 5O₂ → 4CO₂ + 2H₂O."},
 {"k": "int", "src": "", "q": "Un soudeur utilise un chalumeau oxyacétylénique. Pour 1 heure de travail il consomme 500 L d’acétylène (Vm = 24 L/mol).\n1) Quel volume de dioxygène faut-il prévoir ?\n2) Pourquoi la flamme oxyacétylénique est-elle très chaude ?",
  "a": "1) 2C₂H₂ + 5O₂ → … : V(O₂) = 2,5 × 500 = 1250 L.\n2) La combustion de l’acétylène dans le dioxygène pur libère beaucoup d’énergie (la flamme dépasse 3000 °C), ce qui permet de fondre les métaux."},
]

SERIES["Chimie – Chapitre IV : Les composés aromatiques"] = [
 {"k": "book", "src": "p. 65, ex. 1 (1, 2)", "q": "1) Le benzène se prête surtout aux réactions : d’addition, de substitution, ou à toutes les réactions ? 2) La nitration du benzène donne de l’eau et un composé de masse molaire : 123, 168 ou 120 g/mol ?",
  "a": "1) De substitution (le cycle aromatique est stable ; les additions sont difficiles).\n2) C₆H₆ + HNO₃ → C₆H₅NO₂ + H₂O ; M(C₆H₅NO₂) = 123 g/mol."},
 {"k": "book", "src": "p. 65, ex. 2", "q": "Combien de composés différents peuvent être appelés dichlorobenzène ? trichlorobenzène ? tétrachlorobenzène ?",
  "a": "Trois dans chaque cas : dichloro 1,2 ; 1,3 ; 1,4 (ortho, méta, para) ; trichloro 1,2,3 ; 1,2,4 ; 1,3,5 ; tétrachloro 1,2,3,4 ; 1,2,3,5 ; 1,2,4,5."},
 {"k": "book", "src": "p. 65, ex. 3", "q": "On réalise la bromation de 3 g de benzène (catalyseur FeBr₃, dibrome en excès) avec un rendement de 80 %. Quantité de monobromobenzène obtenue (en mol et en g) ?",
  "a": "n(C₆H₆) = 3/78 ≈ 3,85 × 10⁻² mol ; n(C₆H₅Br) = 0,80 × 3,85 × 10⁻² ≈ 3,08 × 10⁻² mol ; M = 157 g/mol ⇒ m ≈ 4,8 g."},
 {"k": "book", "src": "p. 65, ex. 4", "q": "On fait réagir 2 g de phénol avec 8 g de dibrome ; il se forme du 2,4,6-tribromophénol (réaction totale). Quantité de dérivé bromé obtenu ?",
  "a": "C₆H₅OH + 3Br₂ → C₆H₂Br₃OH + 3HBr. n(phénol) = 2/94 ≈ 2,13 × 10⁻² mol ; n(Br₂) = 8/160 = 5,0 × 10⁻² mol < 3 × 2,13 × 10⁻² : le dibrome est limitant. n = 5,0 × 10⁻²/3 ≈ 1,67 × 10⁻² mol ; M = 331 g/mol ⇒ m ≈ 5,5 g."},
 {"k": "book", "src": "p. 65, ex. 5", "q": "On fait passer 1 g de benzène vapeur avec du dihydrogène sur un catalyseur ; le produit, brûlé dans un excès de dioxygène, donne 1 g d’eau. a) Masse de CO₂ obtenue. b) Masse de benzène ayant réagi. c) Rendement.",
  "a": "C₆H₆ + 3H₂ → C₆H₁₂ puis C₆H₁₂ + 9O₂ → 6CO₂ + 6H₂O.\na) n(H₂O) = 1/18 ≈ 5,56 × 10⁻² mol = n(CO₂) ⇒ m(CO₂) ≈ 2,44 g.\nb) n(C₆H₁₂) = 9,26 × 10⁻³ mol = n(benzène ayant réagi) ⇒ 0,72 g.\nc) Rendement ≈ 72 %."},
 {"k": "extra", "src": "", "q": "Écrire l’équation de la chloration du benzène à la lumière (addition) et celle en présence de chlorure d’aluminium (substitution). Nommer les produits.",
  "a": "Addition : C₆H₆ + 3Cl₂ → C₆H₆Cl₆ (hexachlorocyclohexane). Substitution : C₆H₆ + Cl₂ → C₆H₅Cl + HCl (chlorobenzène)."},
 {"k": "int", "src": "", "q": "Le toluène (méthylbenzène) sert à fabriquer le TNT (trinitrotoluène) par trinitration.\n1) Écrire l’équation de la réaction (C₆H₅CH₃ + 3HNO₃).\n2) Quelle masse de TNT obtient-on à partir de 46 kg de toluène avec un rendement de 90 % ?",
  "a": "1) C₆H₅CH₃ + 3HNO₃ → C₆H₂(NO₂)₃CH₃ + 3H₂O.\n2) n(toluène) = 46 000/92 = 500 mol ; n(TNT) = 450 mol ; M = 227 g/mol ⇒ m ≈ 102 kg."},
]

SERIES["Chimie – Chapitre V : Isomérie"] = [
 {"k": "book", "src": "p. 77, ex. 1 (1, 2, 3)", "q": "Formules semi-développées de : acide 2-méthylbutanoïque ; acide 3-chloropropanoïque ; 3-méthylpentanal ; 2-méthylpropanal ; hexan-3-one ; 3-méthylbutan-2-one.",
  "a": "CH₃–CH₂–CH(CH₃)–COOH ; Cl–CH₂–CH₂–COOH ; CH₃–CH₂–CH(CH₃)–CH₂–CHO ; CH₃–CH(CH₃)–CHO ; CH₃–CH₂–CO–CH₂–CH₂–CH₃ ; CH₃–CO–CH(CH₃)–CH₃."},
 {"k": "book", "src": "p. 77, ex. 1 (4, 5)", "q": "Formules semi-développées de : éthanoate de propyle ; propanoate d’éthyle ; 1-méthoxypropane ; 2-éthoxybutane.",
  "a": "CH₃–COO–CH₂–CH₂–CH₃ ; CH₃–CH₂–COO–CH₂–CH₃ ; CH₃–O–CH₂–CH₂–CH₃ ; CH₃–CH₂–O–CH(CH₃)–CH₂–CH₃."},
 {"k": "book", "src": "p. 77, ex. 2", "q": "Donner les formules et les noms des 7 isomères de formule C₄H₁₀O et les grouper par fonction.",
  "a": "Alcools : butan-1-ol, butan-2-ol, 2-méthylpropan-1-ol, 2-méthylpropan-2-ol. Éthers-oxydes : 1-méthoxypropane, 2-méthoxypropane, éthoxyéthane."},
 {"k": "book", "src": "p. 77, ex. 3", "q": "Donner les formules et les noms des 7 aldéhydes et cétones isomères de formule C₅H₁₀O.",
  "a": "Aldéhydes : pentanal, 2-méthylbutanal, 3-méthylbutanal, 2,2-diméthylpropanal. Cétones : pentan-2-one, pentan-3-one, 3-méthylbutan-2-one."},
 {"k": "book", "src": "p. 77, remarque", "q": "Écrire et nommer les deux stéréo-isomères du but-2-ène. Quelle condition faut-il pour qu’un alcène présente cette isomérie ?",
  "a": "(2Z)-but-2-ène (les deux CH₃ du même côté de la double liaison) et (2E)-but-2-ène (de part et d’autre). Chaque carbone de la double liaison doit porter deux groupes différents."},
 {"k": "extra", "src": "", "q": "Les composés C₂H₆O peuvent être l’éthanol ou le méthoxyméthane. Quel type d’isomérie ? Comment les distinguer ?",
  "a": "Isomérie de fonction (alcool / éther-oxyde). L’éthanol réagit avec le sodium (dégagement de H₂) et est liquide à 20 °C ; le méthoxyméthane est un gaz qui ne réagit pas avec le sodium."},
 {"k": "int", "src": "", "q": "Un parfumeur dispose d’un composé de formule C₄H₈O₂ à odeur fruitée, issu de la réaction d’un acide et d’un alcool.\n1) Quelle est sa fonction ?\n2) Écrire les formules et les noms des quatre esters de formule C₄H₈O₂.",
  "a": "1) Un ester.\n2) Méthanoate de propyle HCOO–CH₂–CH₂–CH₃ ; méthanoate d’isopropyle HCOO–CH(CH₃)₂ ; éthanoate d’éthyle CH₃COO–C₂H₅ ; propanoate de méthyle C₂H₅COO–CH₃."},
]

SERIES["Chimie – Chapitre VI : Classification des couples et prévision des réactions"] = [
 {"k": "book", "src": "p. 96, ex. 1", "q": "Peut-on décolorer une solution de sulfate de cuivre II en y versant de la poudre d’argent ? de la poudre de zinc ? Justifier.",
  "a": "Argent : non, Ag est un réducteur plus faible que Cu (l’ion Cu²⁺ ne peut pas oxyder Ag). Zinc : oui, Zn + Cu²⁺ → Zn²⁺ + Cu ; les ions Cu²⁺ (bleus) disparaissent."},
 {"k": "book", "src": "p. 96, ex. 2", "q": "Une goutte de chlorure de mercure II déposée sur une plaque de cuivre donne un dépôt gris. Équation et conclusion.",
  "a": "Hg²⁺ + Cu → Hg + Cu²⁺. Le cuivre est un réducteur plus fort que le mercure : Hg²⁺/Hg est placé au-dessus de Cu²⁺/Cu dans la classification."},
 {"k": "book", "src": "p. 96, ex. 3", "q": "2Na + 2H₂O → 2Na⁺ + 2HO⁻ + H₂. Est-ce une oxydoréduction ? Rôles du sodium et de l’eau ?",
  "a": "Oui : le sodium perd des électrons (Na → Na⁺ + e⁻), c’est le réducteur ; l’eau en gagne (2H₂O + 2e⁻ → H₂ + 2HO⁻), c’est l’oxydant."},
 {"k": "book", "src": "p. 96, ex. 4 et 5", "q": "1) Former les couples avec : Zn²⁺, Pb²⁺, Cr³⁺, Hg, Cu, Pb, Cr, Zn et écrire leurs demi-équations. 2) Oxydant et réducteur de : a) 2Fe³⁺ + 2I⁻ → 2Fe²⁺ + I₂ ; b) Fe²⁺ + Ce⁴⁺ → Fe³⁺ + Ce³⁺ ; c) Au³⁺ + 3Ag → Au + 3Ag⁺ ; d) 2Fe²⁺ + S₂O₈²⁻ → 2Fe³⁺ + 2SO₄²⁻.",
  "a": "1) Zn²⁺ + 2e⁻ ⇌ Zn ; Pb²⁺ + 2e⁻ ⇌ Pb ; Cr³⁺ + 3e⁻ ⇌ Cr ; Hg²⁺ + 2e⁻ ⇌ Hg et Cu²⁺ + 2e⁻ ⇌ Cu (Hg²⁺ et Cu²⁺ n’étaient pas dans la liste, il faut les ajouter pour former ces couples).\n2) a) Oxydant Fe³⁺, réducteur I⁻. b) Oxydant Ce⁴⁺, réducteur Fe²⁺. c) Oxydant Au³⁺, réducteur Ag. d) Oxydant S₂O₈²⁻, réducteur Fe²⁺."},
 {"k": "book", "src": "p. 96, ex. 6", "q": "Couples Cu²⁺/Cu, Fe²⁺/Fe, Zn²⁺/Zn, Ag⁺/Ag. Un fil de cuivre se recouvre d’argent dans le nitrate d’argent ; un fil de fer se recouvre de cuivre dans le sulfate de cuivre ; les ions Fe²⁺ réagissent avec le zinc. Classer ces couples.",
  "a": "Ag⁺ oxyde Cu ; Cu²⁺ oxyde Fe ; Fe²⁺ oxyde Zn. Pouvoir oxydant décroissant : Ag⁺ > Cu²⁺ > Fe²⁺ > Zn²⁺ ; pouvoir réducteur croissant : Ag < Cu < Fe < Zn."},
 {"k": "extra", "src": "", "q": "On plonge une lame de fer de 5,6 g dans une solution contenant 0,05 mol d’ions Cu²⁺. Quelle masse de cuivre se dépose ? Que reste-t-il de la lame ?",
  "a": "Fe + Cu²⁺ → Fe²⁺ + Cu. n(Fe) = 0,10 mol ; Cu²⁺ est limitant : 0,05 mol de Cu, soit 3,18 g ; il reste 0,05 mol de fer (2,8 g)."},
 {"k": "int", "src": "", "q": "Pour protéger la coque en acier (fer) d’un bateau, on fixe des blocs de zinc qu’il faut remplacer régulièrement.\n1) Expliquer le rôle du zinc à l’aide de la classification des couples.\n2) Pourquoi les blocs disparaissent-ils peu à peu ?",
  "a": "1) Zn est un réducteur plus fort que Fe : c’est le zinc qui s’oxyde (Zn → Zn²⁺ + 2e⁻) à la place du fer ; on parle de protection par anode sacrificielle.\n2) Le zinc est consommé : il passe en solution sous forme d’ions Zn²⁺."},
]

SERIES["Chimie – Chapitre VII : Électrolyse"] = [
 {"k": "book", "src": "p. 114, ex. 1", "q": "Une pile associe les couples Ni²⁺/Ni et Cu²⁺/Cu ; on mesure E°(Ni²⁺/Ni) − E°(Cu²⁺/Cu) = −0,6 V. 1) Potentiel du couple Ni²⁺/Ni sachant que E°(Cu²⁺/Cu) = 0,34 V. 2) Un fil de nickel plongé dans une solution de nitrate de cuivre réagit-il ?",
  "a": "1) E°(Ni²⁺/Ni) = 0,34 − 0,6 = −0,26 V.\n2) Oui : Ni est un réducteur plus fort que Cu ; Ni + Cu²⁺ → Ni²⁺ + Cu (dépôt de cuivre sur le fil)."},
 {"k": "book", "src": "p. 114, ex. 2", "q": "Pile argent-zinc (solutions à 1 mol/L) ; E°(Ag⁺/Ag) = 0,80 V et E°(Zn²⁺/Zn) = −0,76 V. 1) Polarités. 2) F.é.m.",
  "a": "1) L’argent est le pôle +, le zinc le pôle −.\n2) E = 0,80 − (−0,76) = 1,56 V. En fonctionnement : Zn + 2Ag⁺ → Zn²⁺ + 2Ag."},
 {"k": "book", "src": "p. 114, ex. 3", "q": "Couples Cu²⁺/Cu (0,34 V) et Mg²⁺/Mg (−2,37 V). 1) Équation de la réaction spontanée. 2) F.é.m. de la pile et polarités.",
  "a": "1) Mg + Cu²⁺ → Mg²⁺ + Cu.\n2) E = 0,34 − (−2,37) = 2,71 V ; pôle + : cuivre ; pôle − : magnésium."},
 {"k": "book", "src": "p. 108, exercice résolu 2", "q": "Avec E°(Fe²⁺/Fe) = −0,44 V, E°(I₂/I⁻) = 0,54 V et E°(Fe³⁺/Fe²⁺) = 0,68 V, écrire les équations des réactions entre : 1) le fer et le diiode ; 2) les ions Fe³⁺ et les ions iodure.",
  "a": "1) I₂ + 2e⁻ → 2I⁻ et Fe → Fe²⁺ + 2e⁻ : Fe + I₂ → Fe²⁺ + 2I⁻.\n2) (Fe³⁺ + e⁻ → Fe²⁺) × 2 et 2I⁻ → I₂ + 2e⁻ : 2Fe³⁺ + 2I⁻ → 2Fe²⁺ + I₂."},
 {"k": "extra", "src": "", "q": "On électrolyse une solution de sulfate de cuivre avec une cathode en fer et une anode en cuivre, sous 0,5 A pendant 1 heure. 1) Réactions aux électrodes. 2) Masse de cuivre déposée (F = 96 500 C/mol ; M(Cu) = 63,5 g/mol).",
  "a": "1) Cathode : Cu²⁺ + 2e⁻ → Cu ; anode : Cu → Cu²⁺ + 2e⁻ (électrolyse à anode soluble).\n2) Q = 0,5 × 3600 = 1800 C ; n(e⁻) = 1800/96 500 ≈ 1,87 × 10⁻² mol ; n(Cu) ≈ 9,3 × 10⁻³ mol ⇒ m ≈ 0,59 g."},
 {"k": "extra", "src": "", "q": "Lors de l’électrolyse de l’eau (rendue conductrice), on recueille 120 mL de dihydrogène. Quel volume de dioxygène obtient-on ? Écrire les demi-équations.",
  "a": "Cathode : 2H₂O + 2e⁻ → H₂ + 2HO⁻ ; anode : 2H₂O → O₂ + 4H⁺ + 4e⁻. Bilan : 2H₂O → 2H₂ + O₂ : 60 mL de dioxygène."},
 {"k": "int", "src": "", "q": "Un bijoutier veut argenter une bague de surface 4 cm² avec une couche de 10 μm d’argent (ρ = 10,5 g/cm³ ; M(Ag) = 108 g/mol ; F = 96 500 C/mol). Il électrolyse une solution de nitrate d’argent sous 0,2 A.\n1) Où place-t-il la bague ?\n2) Quelle masse d’argent faut-il déposer ?\n3) Combien de temps dure l’opération ?",
  "a": "1) À la cathode (Ag⁺ + e⁻ → Ag).\n2) V = 4 × 10⁻³ = 4 × 10⁻³ cm³ ⇒ m = 0,042 g.\n3) n(Ag) = 3,9 × 10⁻⁴ mol = n(e⁻) ⇒ Q ≈ 37,5 C ⇒ t = Q/I ≈ 188 s (un peu plus de 3 min)."},
]
