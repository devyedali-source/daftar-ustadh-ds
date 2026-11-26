# Série d'exercices de fin de chapitre — Physique 7C.
# k = "book" : exercice du manuel (src = page et numéro) ; "extra" : exercice du même type ; "int" : situation d'intégration.
SERIES = {}

SERIES["Physique – Chapitre I : Relation fondamentale de la dynamique"] = [
 {"k": "book", "src": "p. 20, ex. 1", "q": "Une automobile de 5 m roulant à 90 km/h double un camion de 10 m roulant à 72 km/h. Le dépassement commence quand l’avant de l’auto est à 20 m de l’arrière du camion et finit quand l’arrière de l’auto est à 30 m de l’avant du camion. 1) Durée du dépassement. 2) Montrer que l’auto parcourt 325 m.",
  "a": "1) Vitesse relative 25 − 20 = 5 m/s ; distance relative 20 + 10 + 5 + 30 = 65 m ⇒ t = 13 s.\n2) d = 25 × 13 = 325 m."},
 {"k": "book", "src": "p. 20, ex. 2", "q": "Deux autos A et B roulent à 72 km/h, A derrière B à 25 m. À t = 0, A accélère (a = 1 m/s²) et se rabat devant B quand elle a 30 m d’avance. 1) Équations horaires. 2) Distance parcourue par B pendant la manœuvre.",
  "a": "1) Origine en A : xA = 0,5t² + 20t ; xB = 25 + 20t.\n2) xA − xB = 30 ⇒ 0,5t² = 55 ⇒ t ≈ 10,5 s ; B parcourt 20 × 10,5 ≈ 210 m."},
 {"k": "book", "src": "p. 20, ex. 3", "q": "Deux voitures se suivent à 108 km/h, à la distance d. M₁ freine à t = 0 (6 m/s²) ; M₂ freine 1 s plus tard (5 m/s²). 1) Condition sur d pour éviter le choc. 2) Si d = 30 m : instant et vitesses au choc. 3) Si d = 55 m : distance entre les voitures à l’arrêt.",
  "a": "1) M₁ s’arrête en 75 m ; M₂ parcourt 30 m + 90 m = 120 m. L’écart diminue constamment : il faut d > 45 m.\n2) Pour t ≥ 1 s l’écart vaut d − 0,5t² − 5t + 2,5 ; avec d = 30 m il s’annule pour t ≈ 4,49 s : v₁ ≈ 3,1 m/s, v₂ ≈ 12,6 m/s.\n3) D = 55 − 45 = 10 m."},
 {"k": "book", "src": "p. 21, ex. 5", "q": "Un voyageur à 25 m de la portière court à 24 km/h quand le train démarre (a = 1,2 m/s²). Peut-il rattraper le train ? Sinon, à quelle distance minimale arrive-t-il ?",
  "a": "Écart e(t) = 25 + 0,6t² − 6,67t, minimal pour t = 6,67/1,2 ≈ 5,6 s : e ≈ 6,5 m > 0. Il ne rattrape pas le train et en reste au mieux à 6,5 m."},
 {"k": "book", "src": "p. 21, ex. 6", "q": "Une voiture à 144 km/h voit un obstacle à 130 m ; elle freine et passe à 108 km/h en 1 s. 1) Décélération. 2) Distance d’arrêt par rapport à l’obstacle. 3) Si le conducteur réagit 1,5 s après l’apparition de l’obstacle ?",
  "a": "1) a = (30 − 40)/1 = −10 m/s².\n2) Distance de freinage 40²/20 = 80 m : elle s’arrête à 50 m de l’obstacle.\n3) 1,5 × 40 = 60 m puis 80 m : 140 m > 130 m, elle heurte l’obstacle à v = √(1600 − 2 × 10 × 70) ≈ 14 m/s."},
 {"k": "extra", "src": "", "q": "Un solide de 2 kg est tiré sur un sol horizontal par une force horizontale de 10 N ; les frottements valent 4 N. Accélération ? Vitesse après 5 s s’il part du repos ?",
  "a": "RFD : ma = F − f ⇒ a = 3 m/s² ; v = 15 m/s."},
 {"k": "int", "src": "", "q": "Un ascenseur de 800 kg monte : il accélère à 1 m/s² pendant 2 s, monte à vitesse constante, puis freine à 1 m/s² pendant 2 s (g = 10 N/kg).\n1) Tension du câble dans chaque phase.\n2) Une personne de 60 kg est sur une balance : que lit-elle pendant chaque phase ?",
  "a": "1) T = m(g + a) = 8800 N ; T = mg = 8000 N ; T = m(g − a) = 7200 N.\n2) Elle lit l’équivalent de 66 kg, 60 kg puis 54 kg (réaction R = m(g ± a))."},
]

SERIES["Physique – Chapitre II : Particule chargée dans un champ magnétique — force de Lorentz"] = [
 {"k": "book", "src": "p. 72, ex. 1", "q": "Un solide de 400 g est lancé à 4 m/s vers le haut d’un plan incliné de 30° (AB = 0,7 m ; g = 10 m/s²). 1) Sans frottement : accélération, vitesse en B, durée. 2) En réalité vB = 2 m/s : frottement et réaction du plan. 3) Équation de la trajectoire après B et sommet.",
  "a": "1) a = −g sin 30° = −5 m/s² ; vB² = 16 − 7 = 9 ⇒ vB = 3 m/s ; t = 0,2 s.\n2) a = (4 − 16)/1,4 ≈ −8,57 m/s² ⇒ f = m(8,57 − 5) ≈ 1,43 N ; R = mg cos 30° ≈ 3,46 N.\n3) Départ à 2 m/s incliné de 30° : y = −1,67x² + 0,577x (repère en B) ; sommet x ≈ 0,17 m, y = 0,05 m."},
 {"k": "book", "src": "p. 72, ex. 2", "q": "Un solide de 2 g est lancé de O à v₀ = 2 m/s sous l’angle β (g = 10 m/s²). 1) Hauteur maximale pour β = 90° et sommet M′ pour β = 45°. 2) β = 0 et le solide porte q : avec q = 2 × 10⁻⁶ C le mouvement est rectiligne uniforme : champ E⃗ ? Avec q = 6 × 10⁻⁶ C : trajectoire ; passe-t-elle par M′ ?",
  "a": "1) YM = v₀²/(2g) = 0,2 m ; pour 45° : YM′ = 0,1 m et M′(0,2 m ; 0,1 m).\n2) qE = mg ⇒ E = 10⁴ V/m vertical vers le haut. Avec 6 × 10⁻⁶ C : force nette 0,04 N vers le haut, a = 20 m/s² : x = 2t, y = 10t² ⇒ y = 2,5x² ; pour x = 0,2 : y = 0,1 : elle passe par M′."},
 {"k": "book", "src": "p. 88, ex. 31", "q": "Satellite géostationnaire (R = 6400 km ; G = 6,67 × 10⁻¹¹ SI ; g₀ = 9,8 m/s²). 1) Expression de sa vitesse. 2) Plan de l’orbite, altitude, vitesse angulaire et linéaire. 3) Masse de la Terre.",
  "a": "1) v = √(g₀R²/r) = √(GM/r).\n2) Plan équatorial ; T = 86 164 s ⇒ r = (g₀R²T²/4π²)^(1/3) ≈ 42 260 km, altitude ≈ 35 900 km ; ω ≈ 7,29 × 10⁻⁵ rad/s ; v ≈ 3,08 km/s.\n3) M = g₀R²/G ≈ 6,0 × 10²⁴ kg."},
 {"k": "book", "src": "p. 89, ex. 32", "q": "Un satellite de 1020 kg est en orbite circulaire à h = 400 km (g₀ = 9,8 m/s² ; R = 6400 km). Vitesse, période et énergie cinétique.",
  "a": "v = √(g₀R²/(R + h)) ≈ 7,68 km/s ; T = 2π(R + h)/v ≈ 5560 s (≈ 1 h 33 min) ; Ec ≈ 3,0 × 10¹⁰ J."},
 {"k": "book", "src": "p. 90, ex. 34", "q": "1) Un satellite tourne à r = 42,3 × 10³ km autour de la Terre (M = 5,98 × 10²⁴ kg) : période ? 2) La Terre décrit en 365,25 jours une orbite de rayon 1,496 × 10⁸ km : masse du Soleil.",
  "a": "1) T = 2π√(r³/GM) ≈ 24 h : satellite géostationnaire s’il est dans le plan de l’équateur.\n2) M = 4π²r³/(GT²) ≈ 2,0 × 10³⁰ kg."},
 {"k": "extra", "src": "", "q": "Un proton (m = 1,67 × 10⁻²⁷ kg) entre à 2 × 10⁶ m/s perpendiculairement à un champ magnétique de 0,1 T. Rayon de sa trajectoire et période.",
  "a": "R = mv/(eB) = 1,67 × 10⁻²⁷ × 2 × 10⁶/(1,6 × 10⁻²⁰) ≈ 0,21 m ; T = 2πm/(eB) ≈ 6,6 × 10⁻⁷ s."},
 {"k": "extra", "src": "", "q": "Spectrographe de masse : des ions ³⁹K⁺ et ⁴¹K⁺, accélérés sous la même tension U, entrent dans un champ B. Montrer que R = √(2mU/(qB²)) et calculer le rapport des rayons.",
  "a": "½mv² = qU et R = mv/(qB) ⇒ R = √(2mU/q)/B. R₄₁/R₃₉ = √(41/39) ≈ 1,025."},
 {"k": "int", "src": "", "q": "Un téléviseur à tube dévie ses électrons (accélérés sous 20 kV) avec un champ magnétique. Pour que le faisceau tourne sur un arc de rayon 20 cm, quel champ faut-il ? (mₑ = 9,1 × 10⁻³¹ kg)",
  "a": "v = √(2eU/m) ≈ 8,4 × 10⁷ m/s ; B = mv/(eR) ≈ 2,4 × 10⁻³ T."},
]

SERIES["Physique – Chapitre III : Pendule élastique (oscillateur harmonique libre)"] = [
 {"k": "book", "src": "p. 105, ex. 1", "q": "x = A cos ωt, y = A sin ωt avec A = 10 cm et ω = 10 rad/s. Vitesse, accélération, trajectoire.",
  "a": "v = Aω = 1 m/s constante ; a = Aω² = 10 m/s², dirigée vers O (centripète) ; trajectoire : cercle de centre O et de rayon A."},
 {"k": "book", "src": "p. 105, ex. 4", "q": "Un mouvement rectiligne sinusoïdal a une accélération maximale de 800 m/s² et une vitesse maximale de 4 m/s. Fréquence et amplitude.",
  "a": "ω²Xm = 800 et ωXm = 4 ⇒ ω = 200 rad/s, Xm = 2 cm, N ≈ 31,8 Hz."},
 {"k": "book", "src": "p. 105, ex. 5", "q": "x = 2 × 10⁻² cos(40πt + φ). Nature du mouvement, amplitude, pulsation, période, fréquence, longueur du segment, vitesse maximale.",
  "a": "Rectiligne sinusoïdal ; Xm = 2 cm ; ω = 40π rad/s ; T = 0,05 s ; f = 20 Hz ; segment de 4 cm ; vmax = Xmω ≈ 2,5 m/s."},
 {"k": "extra", "src": "", "q": "Un solide de 200 g est accroché à un ressort de raideur 20 N/m sur un plan horizontal sans frottement ; on l’écarte de 4 cm et on le lâche. Équation différentielle, période, équation horaire et vitesse maximale.",
  "a": "mẍ + kx = 0 ; ω₀ = √(k/m) = 10 rad/s ; T₀ ≈ 0,63 s ; x = 0,04 cos(10t) ; vmax = 0,4 m/s."},
 {"k": "extra", "src": "", "q": "Pour l’oscillateur précédent, calculer l’énergie mécanique et montrer qu’elle se conserve.",
  "a": "E = ½kx² + ½mv² = ½kXm² = 0,016 J ; dE/dt = v(kx + mẍ) = 0."},
 {"k": "int", "src": "", "q": "La suspension d’une voiture (masse supportée par une roue : 300 kg) se comporte comme un ressort de raideur 3 × 10⁴ N/m.\n1) Période des oscillations libres.\n2) La voiture passe sur des dos d’âne espacés de 5 m : à quelle vitesse les oscillations risquent-elles d’être amplifiées ? Rôle des amortisseurs ?",
  "a": "1) T = 2π√(m/k) ≈ 0,63 s.\n2) Quand la durée entre deux bosses vaut T : v = 5/0,63 ≈ 8 m/s (29 km/h) ; les amortisseurs dissipent l’énergie et évitent la résonance."},
]

SERIES["Physique – Chapitre IV : Force de Laplace"] = [
 {"k": "book", "src": "p. 120, ex. 1", "q": "Une tige de cuivre de 20 g est suspendue à deux ressorts (k = 5 N/m chacun, l₀ = 15 cm) ; un courant de 5 A la traverse ; le champ B = 0,6 T agit sur 8 cm (g = 10 m/s²). 1) Longueur des ressorts sans courant. 2) Force de Laplace et longueur si B est parallèle à la tige, perpendiculaire « rentrant » ou « sortant ». 3) Valeur de B pour que la force compense le poids.",
  "a": "1) Allongement mg/(2k) = 2 cm : 17 cm.\n2) F = IℓB = 0,24 N. B parallèle à la tige : F = 0, 17 cm. Force vers le bas : (0,2 + 0,24)/10 = 4,4 cm ⇒ 19,4 cm ; vers le haut : −0,4 cm ⇒ 14,6 cm.\n3) IℓB = mg ⇒ B = 0,5 T ; ressorts à leur longueur à vide, 15 cm."},
 {"k": "book", "src": "p. 120, ex. 2", "q": "Une tige de 10 cm et 10 g roule sans frottement sur deux rails horizontaux ; I = 0,5 A ; B = 0,8 T vertical. 1) Force. 2) Nature et équation du mouvement. 3) Angle d’inclinaison des rails pour l’équilibre.",
  "a": "1) F = IℓB = 0,04 N, horizontale, perpendiculaire à la tige.\n2) a = F/m = 4 m/s² : x = 2t² (m.r.u.a.).\n3) mg sin α = F ⇒ sin α = 0,4 ⇒ α ≈ 23,6°."},
 {"k": "extra", "src": "", "q": "Un cadre de 50 spires (4 cm × 3 cm) parcouru par 2 A est placé dans un champ de 0,5 T, son plan parallèle au champ. Moment du couple de Laplace.",
  "a": "Γ = NIBS = 50 × 2 × 0,5 × 1,2 × 10⁻³ = 0,06 N·m."},
 {"k": "extra", "src": "", "q": "Expliquer le principe du haut-parleur électrodynamique.",
  "a": "La bobine solidaire de la membrane, parcourue par un courant variable, est placée dans le champ radial d’un aimant : la force de Laplace variable fait vibrer la membrane, qui émet un son."},
 {"k": "int", "src": "", "q": "Les rails de Laplace servent à étudier un « canon électromagnétique ». Une tige de 50 g et de 20 cm est parcourue par 50 A dans un champ de 1 T, sur 1 m de rails.\n1) Force et accélération.\n2) Vitesse de sortie (sans frottement).",
  "a": "1) F = 50 × 0,2 × 1 = 10 N ; a = 200 m/s².\n2) v = √(2ad) = 20 m/s."},
]

SERIES["Physique – Chapitre V : Induction électromagnétique"] = [
 {"k": "book", "src": "p. 136, ex. 2", "q": "Bobine inductrice : E = 6 V, rhéostat 1 Ω, bobine de 20 cm, r = 2 Ω, 100 spires. Bobine induite (à l’intérieur) : 1000 spires de section 2 cm², R = 3 Ω. 1) Courant inducteur. 2) Champ dans l’inductrice. 3) Flux dans la bobine induite. 4) f.é.m. moyenne et courant induit si le courant inducteur s’annule en 50 ms.",
  "a": "1) I = 6/3 = 2 A.\n2) B = μ₀NI/ℓ ≈ 1,26 × 10⁻³ T.\n3) Φ = N₂BS₂ ≈ 2,5 × 10⁻⁴ Wb.\n4) e = ΔΦ/Δt ≈ 5,0 × 10⁻³ V ; i = e/R ≈ 1,7 mA, dans un sens qui crée un champ de même sens que le champ inducteur qui disparaît (loi de Lenz)."},
 {"k": "extra", "src": "", "q": "Une tige de 20 cm glisse à 2 m/s sur des rails dans un champ vertical de 0,5 T ; le circuit a une résistance de 0,4 Ω. f.é.m., courant induit et force de Laplace.",
  "a": "e = Bℓv = 0,2 V ; i = 0,5 A ; F = iℓB = 0,05 N, opposée au déplacement (Lenz)."},
 {"k": "extra", "src": "", "q": "Un cadre de 100 spires et de 50 cm² tourne à 50 tr/s dans un champ de 0,1 T. Expression de la f.é.m. et valeur maximale.",
  "a": "Φ = NBS cos ωt ⇒ e = NBSω sin ωt ; ω = 100π rad/s : Emax = 100 × 0,1 × 5 × 10⁻³ × 314 ≈ 15,7 V."},
 {"k": "extra", "src": "", "q": "Énoncer la loi de Lenz et l’appliquer : on approche le pôle nord d’un aimant d’une bobine fermée ; quelle face de la bobine devient une face nord ?",
  "a": "Le courant induit s’oppose par ses effets à la cause qui lui donne naissance : la face tournée vers l’aimant devient une face nord, qui repousse l’aimant."},
 {"k": "int", "src": "", "q": "Une dynamo de vélo comporte un aimant tournant devant une bobine.\n1) Pourquoi la lampe brille-t-elle davantage quand on roule vite ?\n2) Pourquoi sent-on une résistance au pédalage quand la lampe est allumée ?",
  "a": "1) La f.é.m. induite est proportionnelle à la vitesse de variation du flux, donc à la vitesse de rotation.\n2) Le courant induit crée des forces qui s’opposent à la rotation (Lenz) : l’énergie électrique vient du travail du cycliste."},
]

SERIES["Physique – Chapitre VI : Auto-induction et circuit RL"] = [
 {"k": "book", "src": "p. 155, ex. 1", "q": "On approche un aimant d’une bobine reliée à un microampèremètre. 1) Phénomène observé. 2) Inducteur et induit. 3) Avec un GBF triangulaire, montrer que la tension aux bornes d’une bobine idéale vaut u = L di/dt.",
  "a": "1) Induction : un courant traverse le circuit pendant le mouvement.\n2) Inducteur : l’aimant ; induit : la bobine.\n3) Le courant variable crée un flux propre variable Φ = Li ; la f.é.m. d’auto-induction est e = −L di/dt et, pour une bobine sans résistance, u = L di/dt (une tension en créneaux pour un courant triangulaire)."},
 {"k": "extra", "src": "", "q": "Une bobine (L = 0,5 H ; r = 10 Ω) est en série avec R = 40 Ω sous E = 10 V. Constante de temps, intensité en régime permanent et énergie emmagasinée.",
  "a": "τ = L/(R + r) = 0,01 s ; I = 10/50 = 0,2 A ; W = ½LI² = 0,01 J."},
 {"k": "extra", "src": "", "q": "Établir l’équation différentielle de l’établissement du courant dans un circuit RL et donner sa solution.",
  "a": "E = Ri + L di/dt ⇒ i(t) = (E/R)(1 − e^(−t/τ)) avec τ = L/R."},
 {"k": "extra", "src": "", "q": "Une bobine de 0,2 H voit son courant passer de 2 A à 0 en 1 ms. f.é.m. d’auto-induction moyenne ? Danger ?",
  "a": "|e| = LΔi/Δt = 400 V : à l’ouverture du circuit apparaissent des surtensions (étincelles), d’où les diodes de roue libre."},
 {"k": "int", "src": "", "q": "Dans une voiture, la bobine d’allumage transforme la tension de 12 V en une très haute tension pour la bougie.\n1) Pourquoi coupe-t-on brusquement le courant dans la bobine ?\n2) Avec L = 5 mH, I = 4 A coupé en 10 μs, quelle tension obtient-on au primaire ?",
  "a": "1) La variation très rapide du courant crée une forte f.é.m. d’auto-induction (amplifiée par le secondaire).\n2) e = LΔi/Δt = 5 × 10⁻³ × 4/10⁻⁵ = 2000 V."},
]

SERIES["Physique – Chapitre VII : Condensateur et circuit RC"] = [
 {"k": "book", "src": "p. 179, ex. 1", "q": "Capacité équivalente : a) 10 μF, 10 μF et 22 μF en série ; b) 10, 10, 33 et 33 pF en parallèle ; c) 100 pF et 220 pF en parallèle, en série avec 220 pF.",
  "a": "a) 1/C = 0,1 + 0,1 + 1/22 ⇒ C ≈ 4,07 μF. b) 86 pF. c) 320 pF en série avec 220 pF : ≈ 130 pF."},
 {"k": "book", "src": "p. 179, ex. 2", "q": "Un condensateur de 100 μF est chargé sous 30 V. Charge et énergie.",
  "a": "Q = CU = 3,0 × 10⁻³ C ; W = ½CU² = 0,045 J."},
 {"k": "book", "src": "p. 179, ex. 3", "q": "47 μF et 33 μF supportent chacun au plus 25 V. En série puis en parallèle : capacité équivalente, tension maximale du groupement, énergie.",
  "a": "Série : C ≈ 19,4 μF ; la charge commune est limitée par 33 μF sous 25 V (q = 825 μC), d’où Umax ≈ 42,6 V et W ≈ 17,6 mJ. Parallèle : 80 μF ; Umax = 25 V ; W = 25 mJ."},
 {"k": "extra", "src": "", "q": "Un condensateur de 10 μF se charge à travers R = 10 kΩ sous E = 12 V. Constante de temps, expression de uC(t), tension à t = τ et durée pour être chargé à 99 %.",
  "a": "τ = RC = 0,1 s ; uC = 12(1 − e^(−t/τ)) ; uC(τ) ≈ 7,6 V ; 99 % au bout de ≈ 5τ = 0,5 s (4,6τ exactement)."},
 {"k": "extra", "src": "", "q": "Le condensateur précédent, chargé sous 12 V, se décharge dans R. Expression de i(t) et énergie dissipée dans la résistance.",
  "a": "uC = 12e^(−t/τ) ; i = −(12/R)e^(−t/τ) (valeur initiale 1,2 mA). Énergie dissipée = ½CE² = 7,2 × 10⁻⁴ J."},
 {"k": "int", "src": "", "q": "Une minuterie d’escalier éteint la lumière quand la tension aux bornes d’un condensateur (C = 1000 μF), qui se décharge dans R, tombe à 37 % de sa valeur initiale.\n1) Quelle résistance faut-il pour une durée d’éclairage de 1 min ?\n2) Comment allonger la durée ?",
  "a": "1) 37 % correspond à t = τ = RC ⇒ R = 60/10⁻³ = 60 kΩ.\n2) Augmenter R ou C."},
]

SERIES["Physique – Chapitre VIII : Oscillations électriques libres (LC et RLC)"] = [
 {"k": "book", "src": "p. 193, ex. 2", "q": "Un condensateur de 2 μF chargé sous U₀ se décharge dans une bobine (L = 0,5 H) ; le courant a une intensité maximale de 0,01 A. 1) Équation différentielle. 2) Charge maximale et U₀. 3) Période propre.",
  "a": "1) d²q/dt² + q/(LC) = 0.\n2) Qm = Im/ω₀ = Im√(LC) = 10⁻⁵ C ; U₀ = Qm/C = 5 V.\n3) T₀ = 2π√(LC) ≈ 6,3 ms."},
 {"k": "book", "src": "p. 193, ex. 1 (4, 8)", "q": "Établir l’équation différentielle d’un circuit LC et la correspondance entre grandeurs mécaniques (m, k, x) et électriques (L, C, q).",
  "a": "L d²q/dt² + q/C = 0 : oscillations sinusoïdales de période 2π√(LC). Correspondance : m ↔ L ; k ↔ 1/C ; x ↔ q ; v ↔ i."},
 {"k": "extra", "src": "", "q": "Montrer que l’énergie totale d’un circuit LC idéal se conserve et l’exprimer en fonction de Qm et C.",
  "a": "E = q²/(2C) + ½Li² ; dE/dt = i(q/C + L di/dt) = 0 ; E = Qm²/(2C)."},
 {"k": "extra", "src": "", "q": "Dans un circuit RLC série libre, comment évoluent les oscillations ? Que se passe-t-il si R est très grande ?",
  "a": "L’amplitude décroît (régime pseudo-périodique) car R dissipe de l’énergie par effet Joule ; si R est grande, il n’y a plus d’oscillations (régime apériodique)."},
 {"k": "int", "src": "", "q": "Un émetteur radio utilise un circuit LC pour produire des oscillations de 1 MHz avec C = 100 pF.\n1) Quelle inductance faut-il ?\n2) Pourquoi faut-il entretenir les oscillations ?",
  "a": "1) L = 1/(4π²f²C) ≈ 2,5 × 10⁻⁴ H.\n2) Les résistances (fils, bobine) amortissent les oscillations ; un dispositif fournit l’énergie perdue."},
]

SERIES["Physique – Chapitre IX : Oscillations électriques forcées — circuit RLC en régime sinusoïdal"] = [
 {"k": "book", "src": "p. 215, ex. 1", "q": "U = 6,3 V ; f = 50 Hz ; R = 11 Ω ; L = 270 mH ; C = 45 μF en série. Impédance, intensité efficace, déphasage.",
  "a": "Lω ≈ 84,8 Ω ; 1/(Cω) ≈ 70,7 Ω ; Z = √(11² + 14,1²) ≈ 17,9 Ω ; I ≈ 0,35 A ; tan φ = 14,1/11 ⇒ φ ≈ +52° (tension en avance)."},
 {"k": "book", "src": "p. 215, ex. 2", "q": "R = 50 Ω ; bobine L = 45 mH, r = 10 Ω ; C = 10 μF ; U = 6 V ; N = 100 Hz. Impédance, intensité, tensions efficaces, déphasage.",
  "a": "Lω ≈ 28,3 Ω ; 1/(Cω) ≈ 159,2 Ω ; Z = √(60² + 130,9²) ≈ 144 Ω ; I ≈ 41,7 mA ; UR ≈ 2,1 V ; U_bobine ≈ 1,25 V ; UC ≈ 6,6 V ; φ ≈ −65° (circuit capacitif)."},
 {"k": "book", "src": "p. 215, ex. 3", "q": "R = 22 Ω ; L = 550 mH ; C = 0,80 μF ; U = 5 V à la résonance. N₀, facteur de qualité, énergie stockée, énergie consommée en 25 s, facteur de puissance.",
  "a": "N₀ = 1/(2π√(LC)) ≈ 240 Hz ; Q = Lω₀/R ≈ 37,7 ; I = U/R ≈ 0,227 A ; énergie stockée LI² ≈ 0,028 J ; W = RI²t ≈ 28,4 J ; cos φ = 1."},
 {"k": "extra", "src": "", "q": "À la résonance, montrer que UC = Q × U. Danger pour le condensateur avec les valeurs de l’exercice précédent ?",
  "a": "UC = I/(Cω₀) = (U/R)(Lω₀) = QU ≈ 188 V : le condensateur doit supporter une tension bien supérieure à celle du générateur."},
 {"k": "int", "src": "", "q": "Pour capter une station à 200 kHz, un poste radio utilise une bobine de 1 mH et un condensateur variable.\n1) Quelle capacité faut-il ?\n2) Pourquoi le circuit doit-il avoir une faible résistance ?",
  "a": "1) C = 1/(4π²f²L) ≈ 6,3 × 10⁻¹⁰ F.\n2) Pour que la résonance soit aiguë (grand facteur de qualité) et ne sélectionne que cette station."},
]

SERIES["Physique – Chapitre X : Propagation d’un mouvement vibratoire (milieu unidimensionnel)"] = [
 {"k": "book", "src": "p. 240, ex. 1", "q": "Un disque portant une tache blanche tourne à 3600 tr/min. Observation au stroboscope pour Ne = 61, 58, 180, 120, 30 et 20 Hz. Quelle est la vitesse d’un disque pour lequel 50 Hz est la plus grande fréquence donnant une tache unique immobile ?",
  "a": "N = 60 Hz. 61 Hz : tache tournant lentement en sens inverse (1 tr/s) ; 58 Hz : lentement dans le sens réel (2 tr/s) ; 180 Hz : 3 taches immobiles ; 120 Hz : 2 taches immobiles ; 30 et 20 Hz : une tache immobile. Deuxième cas : N = 50 Hz = 3000 tr/min."},
 {"k": "book", "src": "p. 240, ex. 4", "q": "Un stroboscope mécanique à 4 trous tourne à 10 tr/s. 1) Fréquence et période des éclairs. 2) Vitesses du disque D pour que le secteur paraisse immobile. 3) D tourne à 41 tr/s : observation.",
  "a": "1) Ne = 40 Hz ; Te = 25 ms.\n2) N = 40k tr/s (k entier).\n3) Mouvement apparent ralenti dans le sens réel, à 1 tr/s."},
 {"k": "book", "src": "p. 251, ex. 1", "q": "Une corde vibre à 65 Hz ; le premier point en opposition de phase avec la source est à 30 cm. Célérité ; points vibrant en phase avec M.",
  "a": "λ/2 = 30 cm ⇒ λ = 60 cm ; c = λf = 39 m/s ; points d’abscisses 30 + 60k cm."},
 {"k": "book", "src": "p. 251, ex. 2", "q": "Une source vibre à 50 Hz ; la célérité est 10 m/s. Longueur d’onde ; comparer M (à 20 cm) et S.",
  "a": "λ = 0,2 m ; SM = λ : M vibre en phase avec S (avec un retard d’une période)."},
 {"k": "extra", "src": "", "q": "Écrire l’élongation d’un point M situé à la distance x de la source si yS = a sin(ωt).",
  "a": "yM(t) = a sin(ωt − 2πx/λ) pour t ≥ x/c (avant, M est au repos)."},
 {"k": "int", "src": "", "q": "Lors d’un orage, on entend le tonnerre 6 s après avoir vu l’éclair (son : 340 m/s).\n1) À quelle distance est l’orage ?\n2) Un son de 680 Hz a quelle longueur d’onde dans l’air ?",
  "a": "1) d = 340 × 6 ≈ 2 km (la lumière arrive quasi instantanément).\n2) λ = 340/680 = 0,5 m."},
]

SERIES["Physique – Chapitre XI : Interférences mécaniques à la surface d’un liquide"] = [
 {"k": "book", "src": "p. 251, ex. 3", "q": "Ondes circulaires de longueur d’onde 1 cm ; la plus grande fréquence d’éclairs donnant l’immobilité apparente est 15 Hz. 1) Fréquence du vibreur, célérité. 2) Aspect à 30 Hz et à 16 Hz.",
  "a": "1) f = 15 Hz ; c = λf = 0,15 m/s.\n2) 30 Hz : immobilité apparente, mais on voit deux fois plus de rides (crêtes et creux alternent). 16 Hz : mouvement apparent lent vers la source, à 1 Hz, soit une célérité apparente de 1 cm/s."},
 {"k": "book", "src": "p. 252, ex. 4", "q": "Deux pointes S₁ et S₂ distantes de 5 cm vibrent à 20 Hz (amplitude 4 mm ; c = 0,36 m/s). État vibratoire de M₁ (d₁ = 10 ; d₂ = 11,8 cm), M₂ (14,7 ; 16,5 cm), M₃ (8,1 ; 5,4 cm).",
  "a": "λ = 1,8 cm. M₁ et M₂ : δ = 1,8 cm = λ : amplitude maximale (8 mm), sur la même frange. M₃ : δ = 2,7 cm = 1,5λ : point immobile."},
 {"k": "extra", "src": "", "q": "Combien de franges d’amplitude maximale coupent le segment S₁S₂ = 5 cm si λ = 1,8 cm ?",
  "a": "Points tels que |δ| = kλ < 5 cm ⇒ k = −2 à 2 : 5 franges."},
 {"k": "extra", "src": "", "q": "Pourquoi faut-il que les deux sources soient synchrones et cohérentes pour observer des interférences stables ?",
  "a": "Il faut un déphasage constant entre elles pour que les points de vibration maximale et nulle restent fixes."},
 {"k": "int", "src": "", "q": "Dans une piscine, deux bouées agitées ensemble créent des zones d’eau calme. Les bouées sont à 4 m l’une de l’autre et la longueur d’onde est 1 m.\n1) Expliquer la présence de zones calmes.\n2) À quelle distance de la première bouée, sur le segment qui les relie, trouve-t-on la première zone calme ?",
  "a": "1) Interférences destructives : les ondes arrivent en opposition de phase.\n2) δ = |d₁ − d₂| = (2k + 1)λ/2 ; avec d₁ + d₂ = 4 : d₁ = 0,25 m, 0,75 m… La première est à 0,25 m."},
]

SERIES["Physique – Chapitre XII : Interférences lumineuses (fentes de Young)"] = [
 {"k": "book", "src": "p. 268, ex. 1", "q": "Un laser éclaire deux fentes distantes de a = 0,5 mm ; écran à D = 2 m. 6 franges brillantes occupent 12,7 mm. Interfrange, longueur d’onde et fréquence.",
  "a": "6 franges = 5 interfranges : i = 2,54 mm ; λ = ai/D = 635 nm ; f = c/λ ≈ 4,7 × 10¹⁴ Hz. La différence de marche est δ = ax/D ; frange centrale brillante (δ = 0) ; pour espacer les franges, il faut rapprocher les fentes."},
 {"k": "book", "src": "p. 268, ex. 2", "q": "Deux trous de Young distants de 500 μm, laser de 633 nm, écran à 4 m. Nature de la frange en O et interfrange.",
  "a": "En O, δ = 0 : frange brillante. i = λD/a ≈ 5,1 mm."},
 {"k": "extra", "src": "", "q": "Avec les données précédentes, à quelle distance de O se trouve la 3e frange sombre ?",
  "a": "Franges sombres : x = (k + ½)i ; la 3e (k = 2) est à 2,5 × 5,06 ≈ 12,7 mm."},
 {"k": "extra", "src": "", "q": "Qu’observe-t-on si l’on remplace le laser par une lumière blanche ?",
  "a": "Une frange centrale blanche, entourée de quelques franges irisées (chaque couleur a son interfrange), puis un blanc d’ordre supérieur."},
 {"k": "int", "src": "", "q": "Pour mesurer l’écartement a de deux fentes inconnues, on utilise un laser de 650 nm et un écran à 1,5 m ; on mesure 10 interfranges sur 19,5 mm.\nCalculer a.",
  "a": "i = 1,95 mm ; a = λD/i = 650 × 10⁻⁹ × 1,5/1,95 × 10⁻³ = 0,5 mm."},
]

SERIES["Physique – Chapitre XIII : Niveaux d’énergie de l’atome d’hydrogène"] = [
 {"k": "book", "src": "p. 281, ex. 1", "q": "L’énergie d’ionisation de l’hydrogène est 13,6 eV. Longueur d’onde maximale permettant de l’ioniser ?",
  "a": "λ = hc/E = 6,62 × 10⁻³⁴ × 3 × 10⁸/(13,6 × 1,6 × 10⁻¹⁹) ≈ 91 nm (ultraviolet)."},
 {"k": "book", "src": "p. 281, ex. 2 et 3", "q": "En = −13,6/n² eV. 1) Énergie minimale pour passer de l’état fondamental à un état excité et longueur d’onde associée. 2) Plus grande longueur d’onde émise lors d’une transition vers n = 2.",
  "a": "1) E₂ − E₁ = 10,2 eV ; λ ≈ 122 nm.\n2) Transition 3 → 2 : 13,6(1/4 − 1/9) ≈ 1,89 eV ; λ ≈ 657 nm (raie rouge Hα)."},
 {"k": "extra", "src": "", "q": "Un atome d’hydrogène à l’état fondamental reçoit un photon de 12,75 eV. Est-il absorbé ? Sur quel niveau arrive l’atome ?",
  "a": "E₄ − E₁ = 13,6(1 − 1/16) = 12,75 eV : le photon est absorbé et l’atome passe au niveau n = 4."},
 {"k": "extra", "src": "", "q": "Un photon de 15 eV frappe un atome d’hydrogène dans l’état fondamental. Que se passe-t-il ?",
  "a": "L’atome est ionisé ; l’électron emporte une énergie cinétique de 15 − 13,6 = 1,4 eV."},
 {"k": "int", "src": "", "q": "Les lampes à vapeur d’hydrogène émettent un spectre de raies.\n1) Pourquoi ce spectre est-il discontinu ?\n2) Les raies visibles (série de Balmer) correspondent aux retours vers n = 2. Calculer la longueur d’onde de la raie 4 → 2.",
  "a": "1) Les niveaux d’énergie sont quantifiés : seules certaines énergies de photons sont possibles.\n2) ΔE = 13,6(1/4 − 1/16) = 2,55 eV ⇒ λ ≈ 487 nm (bleu-vert)."},
]

SERIES["Physique – Chapitre XIV : Effet photoélectrique"] = [
 {"k": "book", "src": "p. 296, ex. 1", "q": "Une cellule reçoit une radiation de 0,483 μm ; le courant s’annule pour une tension d’arrêt de 0,87 V. Travail d’extraction, fréquence seuil et longueur d’onde seuil.",
  "a": "W₀ = hc/λ − eU₀ ≈ 4,11 × 10⁻¹⁹ − 1,39 × 10⁻¹⁹ ≈ 2,72 × 10⁻¹⁹ J ≈ 1,70 eV ; ν₀ = W₀/h ≈ 4,1 × 10¹⁴ Hz ; λ₀ ≈ 730 nm."},
 {"k": "book", "src": "p. 296, ex. 2", "q": "Cathode au césium (ν₀ = 4,6 × 10¹⁴ Hz) éclairée par 430 nm, puissance 1 mW. 1) W₀, Ec max, potentiel d’arrêt. 2) Vitesse d’impact sur l’anode sous 10 V. 3) Courant de saturation si le rendement quantique est 0,03.",
  "a": "1) W₀ = hν₀ ≈ 3,05 × 10⁻¹⁹ J ≈ 1,90 eV ; E(photon) ≈ 2,89 eV ⇒ Ec max ≈ 0,98 eV ; U₀ ≈ 0,98 V.\n2) Ec = 0,98 + 10 eV ⇒ v ≈ 2,0 × 10⁶ m/s.\n3) N(photons) = P/E ≈ 2,2 × 10¹⁵ /s ; Is = 0,03 × N × e ≈ 1,0 × 10⁻⁵ A."},
 {"k": "book", "src": "p. 297, ex. 43", "q": "Chaque photon transporte 2,75 eV ; le travail d’extraction vaut 2,25 eV. Longueur d’onde, vitesse d’expulsion ; que faut-il changer pour l’augmenter ?",
  "a": "λ = hc/E ≈ 451 nm ; Ec = 0,5 eV ⇒ v ≈ 4,2 × 10⁵ m/s. Il faut diminuer la longueur d’onde : la puissance lumineuse ne change que le nombre d’électrons émis."},
 {"k": "extra", "src": "", "q": "Pourquoi l’effet photoélectrique est-il une preuve de l’aspect corpusculaire de la lumière ?",
  "a": "L’émission dépend de la fréquence (seuil) et non de l’intensité : chaque photon d’énergie hν arrache un électron ; une onde classique ne l’explique pas."},
 {"k": "int", "src": "", "q": "Une porte automatique utilise une cellule au potassium (W₀ = 2,25 eV).\n1) Fonctionne-t-elle avec une lumière rouge de 700 nm ? avec une lumière bleue de 450 nm ?\n2) Pourquoi l’utilise-t-on souvent avec de l’infrarouge et des métaux à faible W₀ ?",
  "a": "1) 700 nm : 1,77 eV < 2,25 eV : non. 450 nm : 2,76 eV : oui.\n2) Pour détecter un rayonnement invisible, il faut une cathode à très faible travail d’extraction (comme le césium) ou un capteur semi-conducteur."},
]

SERIES["Physique – Chapitre XV : Réactions nucléaires"] = [
 {"k": "book", "src": "p. 315, ex. 1", "q": "Le polonium 210 (Z = 84) émet une particule α et donne du plomb. 1) Équation. 2) Énergie libérée (m(Po) = 210,0482 u ; m(Pb) = 206,0385 u ; m(α) = 4,0039 u ; 1 u = 1,67 × 10⁻²⁷ kg). 3) Masse restante de 20 g après 414 jours (T = 136 jours).",
  "a": "1) ²¹⁰₈₄Po → ²⁰⁶₈₂Pb + ⁴₂He.\n2) Δm = 0,0058 u ⇒ E = Δm c² ≈ 8,7 × 10⁻¹³ J ≈ 5,4 MeV.\n3) m = 20 × 2^(−414/136) ≈ 2,4 g."},
 {"k": "book", "src": "p. 315, ex. 2", "q": "Le bismuth 210 (Z = 83) donne du polonium 210 : type de désintégration et équation.",
  "a": "Z augmente de 1 et A ne change pas : désintégration β⁻. ²¹⁰₈₃Bi → ²¹⁰₈₄Po + ⁰₋₁e (+ antineutrino)."},
 {"k": "book", "src": "p. 315, ex. 3", "q": "L’uranium 238 (Z = 92) donne du radium 226 (Z = 88) après x désintégrations α et y désintégrations β⁻. Trouver x et y.",
  "a": "Nombre de masse : 238 = 226 + 4x ⇒ x = 3. Charge : 92 = 88 + 2x − y ⇒ y = 2."},
 {"k": "extra", "src": "", "q": "Écrire la loi de décroissance radioactive et relier la constante radioactive λ à la demi-vie T. Calculer λ pour le polonium (T = 136 j).",
  "a": "N = N₀e^(−λt) ; λ = ln 2/T ≈ 5,1 × 10⁻³ j⁻¹ ≈ 5,9 × 10⁻⁸ s⁻¹."},
 {"k": "extra", "src": "", "q": "Fusion : ²₁H + ³₁H → ⁴₂He + ¹₀n. Masses : 2,01355 ; 3,01550 ; 4,00150 ; 1,00866 u (1 u = 931,5 MeV/c²). Énergie libérée ?",
  "a": "Δm = 5,02905 − 5,01016 = 0,01889 u ⇒ E ≈ 17,6 MeV."},
 {"k": "int", "src": "", "q": "Pour dater un bois ancien, on mesure son activité en carbone 14 (T = 5730 ans) : elle vaut 25 % de celle d’un bois vivant.\n1) Quel est l’âge du bois ?\n2) Pourquoi ne peut-on pas dater ainsi un objet de 500 000 ans ?",
  "a": "1) 25 % = (1/2)² : âge ≈ 2 × 5730 = 11 460 ans.\n2) Après près de 90 demi-vies, il ne reste pratiquement plus de carbone 14 mesurable."},
]
