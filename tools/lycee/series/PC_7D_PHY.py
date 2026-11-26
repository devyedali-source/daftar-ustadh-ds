# Série d'exercices de fin de chapitre — Physique 7D (mêmes exercices que le manuel 7C lorsqu'ils y figurent, pages du manuel 7D).
SERIES = {'Physique – Chapitre I : Relation fondamentale de la dynamique': [{'k': 'book',
                                                                    'src': 'p. 22',
                                                                    'q': 'Une automobile de 5 m roulant à 90 km/h double un camion de 10 m roulant à 72 km/h. Le dépassement commence quand l’avant de '
                                                                         'l’auto est à 20 m de l’arrière du camion et finit quand l’arrière de l’auto est à 30 m de l’avant du camion. 1) Durée du '
                                                                         'dépassement. 2) Montrer que l’auto parcourt 325 m.',
                                                                    'a': '1) Vitesse relative 25 − 20 = 5 m/s ; distance relative 20 + 10 + 5 + 30 = 65 m ⇒ t = 13 s.\n2) d = 25 × 13 = 325 m.'},
                                                                   {'k': 'book',
                                                                    'src': 'p. 22',
                                                                    'q': 'Deux autos A et B roulent à 72 km/h, A derrière B à 25 m. À t = 0, A accélère (a = 1 m/s²) et se rabat devant B quand elle a '
                                                                         '30 m d’avance. 1) Équations horaires. 2) Distance parcourue par B pendant la manœuvre.',
                                                                    'a': '1) Origine en A : xA = 0,5t² + 20t ; xB = 25 + 20t.\n'
                                                                         '2) xA − xB = 30 ⇒ 0,5t² = 55 ⇒ t ≈ 10,5 s ; B parcourt 20 × 10,5 ≈ 210 m.'},
                                                                   {'k': 'book',
                                                                    'src': 'p. 22',
                                                                    'q': 'Deux voitures se suivent à 108 km/h, à la distance d. M₁ freine à t = 0 (6 m/s²) ; M₂ freine 1 s plus tard (5 m/s²). 1) '
                                                                         'Condition sur d pour éviter le choc. 2) Si d = 30 m : instant et vitesses au choc. 3) Si d = 55 m : distance entre les '
                                                                         'voitures à l’arrêt.',
                                                                    'a': '1) M₁ s’arrête en 75 m ; M₂ parcourt 30 m + 90 m = 120 m. L’écart diminue constamment : il faut d > 45 m.\n'
                                                                         '2) Pour t ≥ 1 s l’écart vaut d − 0,5t² − 5t + 2,5 ; avec d = 30 m il s’annule pour t ≈ 4,49 s : v₁ ≈ 3,1 m/s, v₂ ≈ 12,6 '
                                                                         'm/s.\n'
                                                                         '3) D = 55 − 45 = 10 m.'},
                                                                   {'k': 'book',
                                                                    'src': 'p. 23',
                                                                    'q': 'Un voyageur à 25 m de la portière court à 24 km/h quand le train démarre (a = 1,2 m/s²). Peut-il rattraper le train ? Sinon, '
                                                                         'à quelle distance minimale arrive-t-il ?',
                                                                    'a': 'Écart e(t) = 25 + 0,6t² − 6,67t, minimal pour t = 6,67/1,2 ≈ 5,6 s : e ≈ 6,5 m > 0. Il ne rattrape pas le train et en reste '
                                                                         'au mieux à 6,5 m.'},
                                                                   {'k': 'book',
                                                                    'src': 'p. 23',
                                                                    'q': 'Une voiture à 144 km/h voit un obstacle à 130 m ; elle freine et passe à 108 km/h en 1 s. 1) Décélération. 2) Distance '
                                                                         'd’arrêt par rapport à l’obstacle. 3) Si le conducteur réagit 1,5 s après l’apparition de l’obstacle ?',
                                                                    'a': '1) a = (30 − 40)/1 = −10 m/s².\n'
                                                                         '2) Distance de freinage 40²/20 = 80 m : elle s’arrête à 50 m de l’obstacle.\n'
                                                                         '3) 1,5 × 40 = 60 m puis 80 m : 140 m > 130 m, elle heurte l’obstacle à v = √(1600 − 2 × 10 × 70) ≈ 14 m/s.'},
                                                                   {'k': 'extra',
                                                                    'src': '',
                                                                    'q': 'Un solide de 2 kg est tiré sur un sol horizontal par une force horizontale de 10 N ; les frottements valent 4 N. '
                                                                         'Accélération ? Vitesse après 5 s s’il part du repos ?',
                                                                    'a': 'RFD : ma = F − f ⇒ a = 3 m/s² ; v = 15 m/s.'},
                                                                   {'k': 'int',
                                                                    'src': '',
                                                                    'q': 'Un ascenseur de 800 kg monte : il accélère à 1 m/s² pendant 2 s, monte à vitesse constante, puis freine à 1 m/s² pendant 2 s '
                                                                         '(g = 10 N/kg).\n'
                                                                         '1) Tension du câble dans chaque phase.\n'
                                                                         '2) Une personne de 60 kg est sur une balance : que lit-elle pendant chaque phase ?',
                                                                    'a': '1) T = m(g + a) = 8800 N ; T = mg = 8000 N ; T = m(g − a) = 7200 N.\n'
                                                                         '2) Elle lit l’équivalent de 66 kg, 60 kg puis 54 kg (réaction R = m(g ± a)).'}],
 'Physique – Chapitre II : Particule chargée dans un champ magnétique — force de Lorentz': [{'k': 'book',
                                                                                             'src': 'p. 74',
                                                                                             'q': 'Un solide de 400 g est lancé à 4 m/s vers le haut d’un plan incliné de 30° (AB = 0,7 m ; g = 10 '
                                                                                                  'm/s²). 1) Sans frottement : accélération, vitesse en B, durée. 2) En réalité vB = 2 m/s : '
                                                                                                  'frottement et réaction du plan. 3) Équation de la trajectoire après B et sommet.',
                                                                                             'a': '1) a = −g sin 30° = −5 m/s² ; vB² = 16 − 7 = 9 ⇒ vB = 3 m/s ; t = 0,2 s.\n'
                                                                                                  '2) a = (4 − 16)/1,4 ≈ −8,57 m/s² ⇒ f = m(8,57 − 5) ≈ 1,43 N ; R = mg cos 30° ≈ 3,46 N.\n'
                                                                                                  '3) Départ à 2 m/s incliné de 30° : y = −1,67x² + 0,577x (repère en B) ; sommet x ≈ 0,17 m, y = 0,05 '
                                                                                                  'm.'},
                                                                                            {'k': 'extra',
                                                                                             'src': '',
                                                                                             'q': 'Un solide de 2 g est lancé de O à v₀ = 2 m/s sous l’angle β (g = 10 m/s²). 1) Hauteur maximale pour '
                                                                                                  'β = 90° et sommet M′ pour β = 45°. 2) β = 0 et le solide porte q : avec q = 2 × 10⁻⁶ C le mouvement '
                                                                                                  'est rectiligne uniforme : champ E⃗ ? Avec q = 6 × 10⁻⁶ C : trajectoire ; passe-t-elle par M′ ?',
                                                                                             'a': '1) YM = v₀²/(2g) = 0,2 m ; pour 45° : YM′ = 0,1 m et M′(0,2 m ; 0,1 m).\n'
                                                                                                  '2) qE = mg ⇒ E = 10⁴ V/m vertical vers le haut. Avec 6 × 10⁻⁶ C : force nette 0,04 N vers le haut, '
                                                                                                  'a = 20 m/s² : x = 2t, y = 10t² ⇒ y = 2,5x² ; pour x = 0,2 : y = 0,1 : elle passe par M′.'},
                                                                                            {'k': 'extra',
                                                                                             'src': '',
                                                                                             'q': 'Satellite géostationnaire (R = 6400 km ; G = 6,67 × 10⁻¹¹ SI ; g₀ = 9,8 m/s²). 1) Expression de sa '
                                                                                                  'vitesse. 2) Plan de l’orbite, altitude, vitesse angulaire et linéaire. 3) Masse de la Terre.',
                                                                                             'a': '1) v = √(g₀R²/r) = √(GM/r).\n'
                                                                                                  '2) Plan équatorial ; T = 86 164 s ⇒ r = (g₀R²T²/4π²)^(1/3) ≈ 42 260 km, altitude ≈ 35 900 km ; ω ≈ '
                                                                                                  '7,29 × 10⁻⁵ rad/s ; v ≈ 3,08 km/s.\n'
                                                                                                  '3) M = g₀R²/G ≈ 6,0 × 10²⁴ kg.'},
                                                                                            {'k': 'book',
                                                                                             'src': 'p. 62',
                                                                                             'q': 'Un satellite de 1020 kg est en orbite circulaire à h = 400 km (g₀ = 9,8 m/s² ; R = 6400 km). '
                                                                                                  'Vitesse, période et énergie cinétique.',
                                                                                             'a': 'v = √(g₀R²/(R + h)) ≈ 7,68 km/s ; T = 2π(R + h)/v ≈ 5560 s (≈ 1 h 33 min) ; Ec ≈ 3,0 × 10¹⁰ J.'},
                                                                                            {'k': 'book',
                                                                                             'src': 'p. 83',
                                                                                             'q': '1) Un satellite tourne à r = 42,3 × 10³ km autour de la Terre (M = 5,98 × 10²⁴ kg) : période ? 2) '
                                                                                                  'La Terre décrit en 365,25 jours une orbite de rayon 1,496 × 10⁸ km : masse du Soleil.',
                                                                                             'a': '1) T = 2π√(r³/GM) ≈ 24 h : satellite géostationnaire s’il est dans le plan de l’équateur.\n'
                                                                                                  '2) M = 4π²r³/(GT²) ≈ 2,0 × 10³⁰ kg.'},
                                                                                            {'k': 'extra',
                                                                                             'src': '',
                                                                                             'q': 'Un proton (m = 1,67 × 10⁻²⁷ kg) entre à 2 × 10⁶ m/s perpendiculairement à un champ magnétique de '
                                                                                                  '0,1 T. Rayon de sa trajectoire et période.',
                                                                                             'a': 'R = mv/(eB) = 1,67 × 10⁻²⁷ × 2 × 10⁶/(1,6 × 10⁻²⁰) ≈ 0,21 m ; T = 2πm/(eB) ≈ 6,6 × 10⁻⁷ s.'},
                                                                                            {'k': 'extra',
                                                                                             'src': '',
                                                                                             'q': 'Spectrographe de masse : des ions ³⁹K⁺ et ⁴¹K⁺, accélérés sous la même tension U, entrent dans un '
                                                                                                  'champ B. Montrer que R = √(2mU/(qB²)) et calculer le rapport des rayons.',
                                                                                             'a': '½mv² = qU et R = mv/(qB) ⇒ R = √(2mU/q)/B. R₄₁/R₃₉ = √(41/39) ≈ 1,025.'},
                                                                                            {'k': 'int',
                                                                                             'src': '',
                                                                                             'q': 'Un téléviseur à tube dévie ses électrons (accélérés sous 20 kV) avec un champ magnétique. Pour que '
                                                                                                  'le faisceau tourne sur un arc de rayon 20 cm, quel champ faut-il ? (mₑ = 9,1 × 10⁻³¹ kg)',
                                                                                             'a': 'v = √(2eU/m) ≈ 8,4 × 10⁷ m/s ; B = mv/(eR) ≈ 2,4 × 10⁻³ T.'}],
 'Physique – Chapitre III : Pendule élastique (oscillateur harmonique libre)': [{'k': 'book',
                                                                                 'src': 'p. 98',
                                                                                 'q': 'x = A cos ωt, y = A sin ωt avec A = 10 cm et ω = 10 rad/s. Vitesse, accélération, trajectoire.',
                                                                                 'a': 'v = Aω = 1 m/s constante ; a = Aω² = 10 m/s², dirigée vers O (centripète) ; trajectoire : cercle de centre O et '
                                                                                      'de rayon A.'},
                                                                                {'k': 'book',
                                                                                 'src': 'p. 98',
                                                                                 'q': 'Un mouvement rectiligne sinusoïdal a une accélération maximale de 800 m/s² et une vitesse maximale de 4 m/s. '
                                                                                      'Fréquence et amplitude.',
                                                                                 'a': 'ω²Xm = 800 et ωXm = 4 ⇒ ω = 200 rad/s, Xm = 2 cm, N ≈ 31,8 Hz.'},
                                                                                {'k': 'book',
                                                                                 'src': 'p. 98',
                                                                                 'q': 'x = 2 × 10⁻² cos(40πt + φ). Nature du mouvement, amplitude, pulsation, période, fréquence, longueur du segment, '
                                                                                      'vitesse maximale.',
                                                                                 'a': 'Rectiligne sinusoïdal ; Xm = 2 cm ; ω = 40π rad/s ; T = 0,05 s ; f = 20 Hz ; segment de 4 cm ; vmax = Xmω ≈ 2,5 '
                                                                                      'm/s.'},
                                                                                {'k': 'extra',
                                                                                 'src': '',
                                                                                 'q': 'Un solide de 200 g est accroché à un ressort de raideur 20 N/m sur un plan horizontal sans frottement ; on '
                                                                                      'l’écarte de 4 cm et on le lâche. Équation différentielle, période, équation horaire et vitesse maximale.',
                                                                                 'a': 'mẍ + kx = 0 ; ω₀ = √(k/m) = 10 rad/s ; T₀ ≈ 0,63 s ; x = 0,04 cos(10t) ; vmax = 0,4 m/s.'},
                                                                                {'k': 'extra',
                                                                                 'src': '',
                                                                                 'q': 'Pour l’oscillateur précédent, calculer l’énergie mécanique et montrer qu’elle se conserve.',
                                                                                 'a': 'E = ½kx² + ½mv² = ½kXm² = 0,016 J ; dE/dt = v(kx + mẍ) = 0.'},
                                                                                {'k': 'int',
                                                                                 'src': '',
                                                                                 'q': 'La suspension d’une voiture (masse supportée par une roue : 300 kg) se comporte comme un ressort de raideur 3 × '
                                                                                      '10⁴ N/m.\n'
                                                                                      '1) Période des oscillations libres.\n'
                                                                                      '2) La voiture passe sur des dos d’âne espacés de 5 m : à quelle vitesse les oscillations risquent-elles d’être '
                                                                                      'amplifiées ? Rôle des amortisseurs ?',
                                                                                 'a': '1) T = 2π√(m/k) ≈ 0,63 s.\n'
                                                                                      '2) Quand la durée entre deux bosses vaut T : v = 5/0,63 ≈ 8 m/s (29 km/h) ; les amortisseurs dissipent '
                                                                                      'l’énergie et évitent la résonance.'}],
 'Physique – Chapitre IV : Force de Laplace': [{'k': 'book',
                                                'src': 'p. 111',
                                                'q': 'Une tige de cuivre de 20 g est suspendue à deux ressorts (k = 5 N/m chacun, l₀ = 15 cm) ; un courant de 5 A la traverse ; le champ B = 0,6 T '
                                                     'agit sur 8 cm (g = 10 m/s²). 1) Longueur des ressorts sans courant. 2) Force de Laplace et longueur si B est parallèle à la tige, '
                                                     'perpendiculaire « rentrant » ou « sortant ». 3) Valeur de B pour que la force compense le poids.',
                                                'a': '1) Allongement mg/(2k) = 2 cm : 17 cm.\n'
                                                     '2) F = IℓB = 0,24 N. B parallèle à la tige : F = 0, 17 cm. Force vers le bas : (0,2 + 0,24)/10 = 4,4 cm ⇒ 19,4 cm ; vers le haut : −0,4 cm ⇒ '
                                                     '14,6 cm.\n'
                                                     '3) IℓB = mg ⇒ B = 0,5 T ; ressorts à leur longueur à vide, 15 cm.'},
                                               {'k': 'book',
                                                'src': 'p. 111',
                                                'q': 'Une tige de 10 cm et 10 g roule sans frottement sur deux rails horizontaux ; I = 0,5 A ; B = 0,8 T vertical. 1) Force. 2) Nature et équation du '
                                                     'mouvement. 3) Angle d’inclinaison des rails pour l’équilibre.',
                                                'a': '1) F = IℓB = 0,04 N, horizontale, perpendiculaire à la tige.\n'
                                                     '2) a = F/m = 4 m/s² : x = 2t² (m.r.u.a.).\n'
                                                     '3) mg sin α = F ⇒ sin α = 0,4 ⇒ α ≈ 23,6°.'},
                                               {'k': 'extra',
                                                'src': '',
                                                'q': 'Un cadre de 50 spires (4 cm × 3 cm) parcouru par 2 A est placé dans un champ de 0,5 T, son plan parallèle au champ. Moment du couple de Laplace.',
                                                'a': 'Γ = NIBS = 50 × 2 × 0,5 × 1,2 × 10⁻³ = 0,06 N·m.'},
                                               {'k': 'extra',
                                                'src': '',
                                                'q': 'Expliquer le principe du haut-parleur électrodynamique.',
                                                'a': 'La bobine solidaire de la membrane, parcourue par un courant variable, est placée dans le champ radial d’un aimant : la force de Laplace '
                                                     'variable fait vibrer la membrane, qui émet un son.'},
                                               {'k': 'int',
                                                'src': '',
                                                'q': 'Les rails de Laplace servent à étudier un « canon électromagnétique ». Une tige de 50 g et de 20 cm est parcourue par 50 A dans un champ de 1 T, '
                                                     'sur 1 m de rails.\n'
                                                     '1) Force et accélération.\n'
                                                     '2) Vitesse de sortie (sans frottement).',
                                                'a': '1) F = 50 × 0,2 × 1 = 10 N ; a = 200 m/s².\n2) v = √(2ad) = 20 m/s.'}],
 'Physique – Chapitre V : Induction électromagnétique': [{'k': 'book',
                                                          'src': 'p. 126',
                                                          'q': 'Bobine inductrice : E = 6 V, rhéostat 1 Ω, bobine de 20 cm, r = 2 Ω, 100 spires. Bobine induite (à l’intérieur) : 1000 spires de '
                                                               'section 2 cm², R = 3 Ω. 1) Courant inducteur. 2) Champ dans l’inductrice. 3) Flux dans la bobine induite. 4) f.é.m. moyenne et courant '
                                                               'induit si le courant inducteur s’annule en 50 ms.',
                                                          'a': '1) I = 6/3 = 2 A.\n'
                                                               '2) B = μ₀NI/ℓ ≈ 1,26 × 10⁻³ T.\n'
                                                               '3) Φ = N₂BS₂ ≈ 2,5 × 10⁻⁴ Wb.\n'
                                                               '4) e = ΔΦ/Δt ≈ 5,0 × 10⁻³ V ; i = e/R ≈ 1,7 mA, dans un sens qui crée un champ de même sens que le champ inducteur qui disparaît (loi '
                                                               'de Lenz).'},
                                                         {'k': 'extra',
                                                          'src': '',
                                                          'q': 'Une tige de 20 cm glisse à 2 m/s sur des rails dans un champ vertical de 0,5 T ; le circuit a une résistance de 0,4 Ω. f.é.m., courant '
                                                               'induit et force de Laplace.',
                                                          'a': 'e = Bℓv = 0,2 V ; i = 0,5 A ; F = iℓB = 0,05 N, opposée au déplacement (Lenz).'},
                                                         {'k': 'extra',
                                                          'src': '',
                                                          'q': 'Un cadre de 100 spires et de 50 cm² tourne à 50 tr/s dans un champ de 0,1 T. Expression de la f.é.m. et valeur maximale.',
                                                          'a': 'Φ = NBS cos ωt ⇒ e = NBSω sin ωt ; ω = 100π rad/s : Emax = 100 × 0,1 × 5 × 10⁻³ × 314 ≈ 15,7 V.'},
                                                         {'k': 'extra',
                                                          'src': '',
                                                          'q': 'Énoncer la loi de Lenz et l’appliquer : on approche le pôle nord d’un aimant d’une bobine fermée ; quelle face de la bobine devient '
                                                               'une face nord ?',
                                                          'a': 'Le courant induit s’oppose par ses effets à la cause qui lui donne naissance : la face tournée vers l’aimant devient une face nord, '
                                                               'qui repousse l’aimant.'},
                                                         {'k': 'int',
                                                          'src': '',
                                                          'q': 'Une dynamo de vélo comporte un aimant tournant devant une bobine.\n'
                                                               '1) Pourquoi la lampe brille-t-elle davantage quand on roule vite ?\n'
                                                               '2) Pourquoi sent-on une résistance au pédalage quand la lampe est allumée ?',
                                                          'a': '1) La f.é.m. induite est proportionnelle à la vitesse de variation du flux, donc à la vitesse de rotation.\n'
                                                               '2) Le courant induit crée des forces qui s’opposent à la rotation (Lenz) : l’énergie électrique vient du travail du cycliste.'}],
 'Physique – Chapitre VI : Propagation d’un mouvement vibratoire (milieu unidimensionnel)': [{'k': 'book',
                                                                                              'src': 'p. 147',
                                                                                              'q': 'Un disque portant une tache blanche tourne à 3600 tr/min. Observation au stroboscope pour Ne = 61, '
                                                                                                   '58, 180, 120, 30 et 20 Hz. Quelle est la vitesse d’un disque pour lequel 50 Hz est la plus grande '
                                                                                                   'fréquence donnant une tache unique immobile ?',
                                                                                              'a': 'N = 60 Hz. 61 Hz : tache tournant lentement en sens inverse (1 tr/s) ; 58 Hz : lentement dans le '
                                                                                                   'sens réel (2 tr/s) ; 180 Hz : 3 taches immobiles ; 120 Hz : 2 taches immobiles ; 30 et 20 Hz : une '
                                                                                                   'tache immobile. Deuxième cas : N = 50 Hz = 3000 tr/min.'},
                                                                                             {'k': 'book',
                                                                                              'src': 'p. 147',
                                                                                              'q': 'Un stroboscope mécanique à 4 trous tourne à 10 tr/s. 1) Fréquence et période des éclairs. 2) '
                                                                                                   'Vitesses du disque D pour que le secteur paraisse immobile. 3) D tourne à 41 tr/s : observation.',
                                                                                              'a': '1) Ne = 40 Hz ; Te = 25 ms.\n'
                                                                                                   '2) N = 40k tr/s (k entier).\n'
                                                                                                   '3) Mouvement apparent ralenti dans le sens réel, à 1 tr/s.'},
                                                                                             {'k': 'book',
                                                                                              'src': 'p. 148',
                                                                                              'q': 'Une corde vibre à 65 Hz ; le premier point en opposition de phase avec la source est à 30 cm. '
                                                                                                   'Célérité ; points vibrant en phase avec M.',
                                                                                              'a': 'λ/2 = 30 cm ⇒ λ = 60 cm ; c = λf = 39 m/s ; points d’abscisses 30 + 60k cm.'},
                                                                                             {'k': 'book',
                                                                                              'src': 'p. 148',
                                                                                              'q': 'Une source vibre à 50 Hz ; la célérité est 10 m/s. Longueur d’onde ; comparer M (à 20 cm) et S.',
                                                                                              'a': 'λ = 0,2 m ; SM = λ : M vibre en phase avec S (avec un retard d’une période).'},
                                                                                             {'k': 'extra',
                                                                                              'src': '',
                                                                                              'q': 'Écrire l’élongation d’un point M situé à la distance x de la source si yS = a sin(ωt).',
                                                                                              'a': 'yM(t) = a sin(ωt − 2πx/λ) pour t ≥ x/c (avant, M est au repos).'},
                                                                                             {'k': 'int',
                                                                                              'src': '',
                                                                                              'q': 'Lors d’un orage, on entend le tonnerre 6 s après avoir vu l’éclair (son : 340 m/s).\n'
                                                                                                   '1) À quelle distance est l’orage ?\n'
                                                                                                   '2) Un son de 680 Hz a quelle longueur d’onde dans l’air ?',
                                                                                              'a': '1) d = 340 × 6 ≈ 2 km (la lumière arrive quasi instantanément).\n2) λ = 340/680 = 0,5 m.'}],
 'Physique – Chapitre VII : Interférences mécaniques à la surface d’un liquide': [{'k': 'book',
                                                                                   'src': 'p. 148',
                                                                                   'q': 'Ondes circulaires de longueur d’onde 1 cm ; la plus grande fréquence d’éclairs donnant l’immobilité apparente '
                                                                                        'est 15 Hz. 1) Fréquence du vibreur, célérité. 2) Aspect à 30 Hz et à 16 Hz.',
                                                                                   'a': '1) f = 15 Hz ; c = λf = 0,15 m/s.\n'
                                                                                        '2) 30 Hz : immobilité apparente, mais on voit deux fois plus de rides (crêtes et creux alternent). 16 Hz : '
                                                                                        'mouvement apparent lent vers la source, à 1 Hz, soit une célérité apparente de 1 cm/s.'},
                                                                                  {'k': 'book',
                                                                                   'src': 'p. 158',
                                                                                   'q': 'Deux pointes S₁ et S₂ distantes de 5 cm vibrent à 20 Hz (amplitude 4 mm ; c = 0,36 m/s). État vibratoire de '
                                                                                        'M₁ (d₁ = 10 ; d₂ = 11,8 cm), M₂ (14,7 ; 16,5 cm), M₃ (8,1 ; 5,4 cm).',
                                                                                   'a': 'λ = 1,8 cm. M₁ et M₂ : δ = 1,8 cm = λ : amplitude maximale (8 mm), sur la même frange. M₃ : δ = 2,7 cm = 1,5λ '
                                                                                        ': point immobile.'},
                                                                                  {'k': 'extra',
                                                                                   'src': '',
                                                                                   'q': 'Combien de franges d’amplitude maximale coupent le segment S₁S₂ = 5 cm si λ = 1,8 cm ?',
                                                                                   'a': 'Points tels que |δ| = kλ < 5 cm ⇒ k = −2 à 2 : 5 franges.'},
                                                                                  {'k': 'extra',
                                                                                   'src': '',
                                                                                   'q': 'Pourquoi faut-il que les deux sources soient synchrones et cohérentes pour observer des interférences stables '
                                                                                        '?',
                                                                                   'a': 'Il faut un déphasage constant entre elles pour que les points de vibration maximale et nulle restent fixes.'},
                                                                                  {'k': 'int',
                                                                                   'src': '',
                                                                                   'q': 'Dans une piscine, deux bouées agitées ensemble créent des zones d’eau calme. Les bouées sont à 4 m l’une de '
                                                                                        'l’autre et la longueur d’onde est 1 m.\n'
                                                                                        '1) Expliquer la présence de zones calmes.\n'
                                                                                        '2) À quelle distance de la première bouée, sur le segment qui les relie, trouve-t-on la première zone calme ?',
                                                                                   'a': '1) Interférences destructives : les ondes arrivent en opposition de phase.\n'
                                                                                        '2) δ = |d₁ − d₂| = (2k + 1)λ/2 ; avec d₁ + d₂ = 4 : d₁ = 0,25 m, 0,75 m… La première est à 0,25 m.'}],
 'Physique – Chapitre VIII : Interférences lumineuses (fentes de Young)': [{'k': 'book',
                                                                            'src': 'p. 174',
                                                                            'q': 'Un laser éclaire deux fentes distantes de a = 0,5 mm ; écran à D = 2 m. 6 franges brillantes occupent 12,7 mm. '
                                                                                 'Interfrange, longueur d’onde et fréquence.',
                                                                            'a': '6 franges = 5 interfranges : i = 2,54 mm ; λ = ai/D = 635 nm ; f = c/λ ≈ 4,7 × 10¹⁴ Hz. La différence de marche est '
                                                                                 'δ = ax/D ; frange centrale brillante (δ = 0) ; pour espacer les franges, il faut rapprocher les fentes.'},
                                                                           {'k': 'book',
                                                                            'src': 'p. 174',
                                                                            'q': 'Deux trous de Young distants de 500 μm, laser de 633 nm, écran à 4 m. Nature de la frange en O et interfrange.',
                                                                            'a': 'En O, δ = 0 : frange brillante. i = λD/a ≈ 5,1 mm.'},
                                                                           {'k': 'extra',
                                                                            'src': '',
                                                                            'q': 'Avec les données précédentes, à quelle distance de O se trouve la 3e frange sombre ?',
                                                                            'a': 'Franges sombres : x = (k + ½)i ; la 3e (k = 2) est à 2,5 × 5,06 ≈ 12,7 mm.'},
                                                                           {'k': 'extra',
                                                                            'src': '',
                                                                            'q': 'Qu’observe-t-on si l’on remplace le laser par une lumière blanche ?',
                                                                            'a': 'Une frange centrale blanche, entourée de quelques franges irisées (chaque couleur a son interfrange), puis un blanc '
                                                                                 'd’ordre supérieur.'},
                                                                           {'k': 'int',
                                                                            'src': '',
                                                                            'q': 'Pour mesurer l’écartement a de deux fentes inconnues, on utilise un laser de 650 nm et un écran à 1,5 m ; on mesure '
                                                                                 '10 interfranges sur 19,5 mm.\n'
                                                                                 'Calculer a.',
                                                                            'a': 'i = 1,95 mm ; a = λD/i = 650 × 10⁻⁹ × 1,5/1,95 × 10⁻³ = 0,5 mm.'}],
 'Physique – Chapitre IX : Niveaux d’énergie de l’atome d’hydrogène': [{'k': 'book',
                                                                        'src': 'p. 181',
                                                                        'q': 'L’énergie d’ionisation de l’hydrogène est 13,6 eV. Longueur d’onde maximale permettant de l’ioniser ?',
                                                                        'a': 'λ = hc/E = 6,62 × 10⁻³⁴ × 3 × 10⁸/(13,6 × 1,6 × 10⁻¹⁹) ≈ 91 nm (ultraviolet).'},
                                                                       {'k': 'book',
                                                                        'src': 'p. 181',
                                                                        'q': 'En = −13,6/n² eV. 1) Énergie minimale pour passer de l’état fondamental à un état excité et longueur d’onde associée. 2) '
                                                                             'Plus grande longueur d’onde émise lors d’une transition vers n = 2.',
                                                                        'a': '1) E₂ − E₁ = 10,2 eV ; λ ≈ 122 nm.\n2) Transition 3 → 2 : 13,6(1/4 − 1/9) ≈ 1,89 eV ; λ ≈ 657 nm (raie rouge Hα).'},
                                                                       {'k': 'extra',
                                                                        'src': '',
                                                                        'q': 'Un atome d’hydrogène à l’état fondamental reçoit un photon de 12,75 eV. Est-il absorbé ? Sur quel niveau arrive l’atome '
                                                                             '?',
                                                                        'a': 'E₄ − E₁ = 13,6(1 − 1/16) = 12,75 eV : le photon est absorbé et l’atome passe au niveau n = 4.'},
                                                                       {'k': 'extra',
                                                                        'src': '',
                                                                        'q': 'Un photon de 15 eV frappe un atome d’hydrogène dans l’état fondamental. Que se passe-t-il ?',
                                                                        'a': 'L’atome est ionisé ; l’électron emporte une énergie cinétique de 15 − 13,6 = 1,4 eV.'},
                                                                       {'k': 'int',
                                                                        'src': '',
                                                                        'q': 'Les lampes à vapeur d’hydrogène émettent un spectre de raies.\n'
                                                                             '1) Pourquoi ce spectre est-il discontinu ?\n'
                                                                             '2) Les raies visibles (série de Balmer) correspondent aux retours vers n = 2. Calculer la longueur d’onde de la raie 4 → '
                                                                             '2.',
                                                                        'a': '1) Les niveaux d’énergie sont quantifiés : seules certaines énergies de photons sont possibles.\n'
                                                                             '2) ΔE = 13,6(1/4 − 1/16) = 2,55 eV ⇒ λ ≈ 487 nm (bleu-vert).'}],
 'Physique – Chapitre X : Effet photoélectrique': [{'k': 'book',
                                                    'src': 'p. 202',
                                                    'q': 'Une cellule reçoit une radiation de 0,483 μm ; le courant s’annule pour une tension d’arrêt de 0,87 V. Travail d’extraction, fréquence seuil '
                                                         'et longueur d’onde seuil.',
                                                    'a': 'W₀ = hc/λ − eU₀ ≈ 4,11 × 10⁻¹⁹ − 1,39 × 10⁻¹⁹ ≈ 2,72 × 10⁻¹⁹ J ≈ 1,70 eV ; ν₀ = W₀/h ≈ 4,1 × 10¹⁴ Hz ; λ₀ ≈ 730 nm.'},
                                                   {'k': 'book',
                                                    'src': 'p. 202',
                                                    'q': 'Cathode au césium (ν₀ = 4,6 × 10¹⁴ Hz) éclairée par 430 nm, puissance 1 mW. 1) W₀, Ec max, potentiel d’arrêt. 2) Vitesse d’impact sur '
                                                         'l’anode sous 10 V. 3) Courant de saturation si le rendement quantique est 0,03.',
                                                    'a': '1) W₀ = hν₀ ≈ 3,05 × 10⁻¹⁹ J ≈ 1,90 eV ; E(photon) ≈ 2,89 eV ⇒ Ec max ≈ 0,98 eV ; U₀ ≈ 0,98 V.\n'
                                                         '2) Ec = 0,98 + 10 eV ⇒ v ≈ 2,0 × 10⁶ m/s.\n'
                                                         '3) N(photons) = P/E ≈ 2,2 × 10¹⁵ /s ; Is = 0,03 × N × e ≈ 1,0 × 10⁻⁵ A.'},
                                                   {'k': 'book',
                                                    'src': 'p. 202',
                                                    'q': 'Chaque photon transporte 2,75 eV ; le travail d’extraction vaut 2,25 eV. Longueur d’onde, vitesse d’expulsion ; que faut-il changer pour '
                                                         'l’augmenter ?',
                                                    'a': 'λ = hc/E ≈ 451 nm ; Ec = 0,5 eV ⇒ v ≈ 4,2 × 10⁵ m/s. Il faut diminuer la longueur d’onde : la puissance lumineuse ne change que le nombre '
                                                         'd’électrons émis.'},
                                                   {'k': 'extra',
                                                    'src': '',
                                                    'q': 'Pourquoi l’effet photoélectrique est-il une preuve de l’aspect corpusculaire de la lumière ?',
                                                    'a': 'L’émission dépend de la fréquence (seuil) et non de l’intensité : chaque photon d’énergie hν arrache un électron ; une onde classique ne '
                                                         'l’explique pas.'},
                                                   {'k': 'int',
                                                    'src': '',
                                                    'q': 'Une porte automatique utilise une cellule au potassium (W₀ = 2,25 eV).\n'
                                                         '1) Fonctionne-t-elle avec une lumière rouge de 700 nm ? avec une lumière bleue de 450 nm ?\n'
                                                         '2) Pourquoi l’utilise-t-on souvent avec de l’infrarouge et des métaux à faible W₀ ?',
                                                    'a': '1) 700 nm : 1,77 eV < 2,25 eV : non. 450 nm : 2,76 eV : oui.\n'
                                                         '2) Pour détecter un rayonnement invisible, il faut une cathode à très faible travail d’extraction (comme le césium) ou un capteur '
                                                         'semi-conducteur.'}],
 'Physique – Chapitre XI : Réactions nucléaires': [{'k': 'book',
                                                    'src': 'p. 221',
                                                    'q': 'Le polonium 210 (Z = 84) émet une particule α et donne du plomb. 1) Équation. 2) Énergie libérée (m(Po) = 210,0482 u ; m(Pb) = 206,0385 u ; '
                                                         'm(α) = 4,0039 u ; 1 u = 1,67 × 10⁻²⁷ kg). 3) Masse restante de 20 g après 414 jours (T = 136 jours).',
                                                    'a': '1) ²¹⁰₈₄Po → ²⁰⁶₈₂Pb + ⁴₂He.\n2) Δm = 0,0058 u ⇒ E = Δm c² ≈ 8,7 × 10⁻¹³ J ≈ 5,4 MeV.\n3) m = 20 × 2^(−414/136) ≈ 2,4 g.'},
                                                   {'k': 'book',
                                                    'src': 'p. 221',
                                                    'q': 'Le bismuth 210 (Z = 83) donne du polonium 210 : type de désintégration et équation.',
                                                    'a': 'Z augmente de 1 et A ne change pas : désintégration β⁻. ²¹⁰₈₃Bi → ²¹⁰₈₄Po + ⁰₋₁e (+ antineutrino).'},
                                                   {'k': 'book',
                                                    'src': 'p. 221',
                                                    'q': 'L’uranium 238 (Z = 92) donne du radium 226 (Z = 88) après x désintégrations α et y désintégrations β⁻. Trouver x et y.',
                                                    'a': 'Nombre de masse : 238 = 226 + 4x ⇒ x = 3. Charge : 92 = 88 + 2x − y ⇒ y = 2.'},
                                                   {'k': 'extra',
                                                    'src': '',
                                                    'q': 'Écrire la loi de décroissance radioactive et relier la constante radioactive λ à la demi-vie T. Calculer λ pour le polonium (T = 136 j).',
                                                    'a': 'N = N₀e^(−λt) ; λ = ln 2/T ≈ 5,1 × 10⁻³ j⁻¹ ≈ 5,9 × 10⁻⁸ s⁻¹.'},
                                                   {'k': 'extra',
                                                    'src': '',
                                                    'q': 'Fusion : ²₁H + ³₁H → ⁴₂He + ¹₀n. Masses : 2,01355 ; 3,01550 ; 4,00150 ; 1,00866 u (1 u = 931,5 MeV/c²). Énergie libérée ?',
                                                    'a': 'Δm = 5,02905 − 5,01016 = 0,01889 u ⇒ E ≈ 17,6 MeV.'},
                                                   {'k': 'int',
                                                    'src': '',
                                                    'q': 'Pour dater un bois ancien, on mesure son activité en carbone 14 (T = 5730 ans) : elle vaut 25 % de celle d’un bois vivant.\n'
                                                         '1) Quel est l’âge du bois ?\n'
                                                         '2) Pourquoi ne peut-on pas dater ainsi un objet de 500 000 ans ?',
                                                    'a': '1) 25 % = (1/2)² : âge ≈ 2 × 5730 = 11 460 ans.\n2) Après près de 90 demi-vies, il ne reste pratiquement plus de carbone 14 mesurable.'}]}
