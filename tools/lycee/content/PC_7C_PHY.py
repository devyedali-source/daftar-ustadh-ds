# Physique-Chimie 7e C — partie Physique (IPN). Contenu tiré du manuel uniquement (« Essentiel » et exemples).
# PC_7D_PHY reprend les chapitres communs avec les pages de son propre manuel.
BOOK = "PC_7C_PHY"


def S(h, lines=None, table=None):
    d = {"h": h, "lines": lines or []}
    if table:
        d["table"] = table
    return d


def L(title, pages, objectives, intro, summary, points, app, board, apps=()):
    return {"title": title, "match": [title.split(" : ", 1)[-1]], "pages": pages, "objectives": objectives,
            "intro": intro, "summary": summary, "points": points,
            "application": {"q": app[0], "a": app[1]},
            "applications": [{"q": q, "a": a} for q, a in apps], "board": board}


def lessons(P, N):
    """P : pages par clé ; N : numéros de chapitre par clé (« I », « II »…), propres à chaque manuel."""
    out = []

    def add(key, *a, **k):
        if key in P:
            t = a[0].replace("{n}", N[key])
            out.append(L(t, P[key], *a[1:], **k))

    add("cin", "Physique – Chapitre {n} : Cinématique du point — vitesse et accélération",
        ["Écrire les vecteurs position, vitesse et accélération.", "Utiliser la base de Frenet.", "Reconnaître MRU, MRUV et mouvement accéléré ou retardé."],
        "Comment décrire précisément le mouvement d’une balle ?",
        "Dans un repère (O ; i⃗, j⃗, k⃗) lié au référentiel, le vecteur position est OM⃗ = x i⃗ + y j⃗ + z k⃗ ; x(t), y(t), z(t) sont les équations horaires ; l’équation y = f(x) est celle de la trajectoire. Vitesse moyenne : v⃗m = ΔOM⃗/Δt ; vitesse instantanée v⃗ = dOM⃗/dt (tangente à la trajectoire, dans le sens du mouvement, v = √(vx² + vy² + vz²)). Accélération : a⃗ = dv⃗/dt ; dans la base de Frenet a⃗ = aₜ t⃗ + aₙ n⃗ avec aₜ = dv/dt et aₙ = v²/r. Le mouvement est accéléré si a⃗·v⃗ > 0, retardé si a⃗·v⃗ < 0. MRU : a = 0, x = vt + x0. MRUV : a constante, v = at + v0, x = ½at² + v0t + x0.",
        ["OM⃗ = x i⃗ + y j⃗ + z k⃗.", "v⃗ = dOM⃗/dt ; a⃗ = dv⃗/dt.", "Frenet : aₜ = dv/dt ; aₙ = v²/r.", "a⃗·v⃗ > 0 accéléré ; < 0 retardé.", "MRUV : x = ½at² + v0t + x0."],
        ("x = 2t et y = 4t² − 1 : trajectoire et vitesse à t = 1 s ?", "y = x² − 1 (parabole) ; vx = 2, vy = 8t ⇒ v(1) = √68 ≈ 8,2 m/s."),
        [S("Mouvements rectilignes", table=[["Mouvement", "a", "v(t)", "x(t)"], ["MRU", "0", "v0", "v0t + x0"], ["MRUV", "constante", "at + v0", "½at² + v0t + x0"]]),
         S("Relation indépendante du temps (MRUV)", ["v² − v0² = 2a(x − x0)."])],
        apps=[("Un mobile a a = 2 m/s², v0 = 0 : distance après 5 s ?", "x = ½ × 2 × 25 = 25 m."), ("Mouvement circulaire uniforme : aₜ ? aₙ ?", "aₜ = 0 ; aₙ = v²/r (centripète).")])
    add("rfd", "Physique – Chapitre {n} : Relation fondamentale de la dynamique",
        ["Distinguer forces intérieures et extérieures.", "Énoncer le principe de l’inertie et la RFD.", "Appliquer la méthode de résolution."],
        "Pourquoi un passager est-il projeté vers l’avant quand le bus freine ?",
        "Un système est un ensemble de points matériels en interaction, déformable ou indéformable. Une force intérieure est exercée par une partie du système sur une autre ; une force extérieure par un agent extérieur. Principe de l’inertie : si ΣF⃗ext = 0⃗, le centre d’inertie est au repos ou en mouvement rectiligne uniforme. Relation fondamentale de la dynamique (dans un repère galiléen) : ΣF⃗ext = m a⃗. Méthode : préciser le système, faire l’inventaire des forces, choisir le repère, faire un schéma, appliquer la RFD et projeter sur les axes.",
        ["Forces intérieures / extérieures.", "Inertie : ΣF⃗ = 0⃗ ⇒ repos ou MRU.", "RFD : ΣF⃗ext = m a⃗.", "Repère galiléen.", "Système → forces → repère → schéma → projection."],
        ("Une caisse de 10 kg est tirée horizontalement par 30 N, sans frottement : accélération ?", "a = F/m = 3 m/s²."),
        [S("Méthode de résolution", ["1) Système ; 2) bilan des forces ; 3) repère galiléen ; 4) schéma ; 5) ΣF⃗ext = m a⃗ ; 6) projections."])],
        apps=[("Un corps en chute libre : accélération ?", "a⃗ = g⃗."), ("Si la résultante est nulle, le mobile peut-il bouger ?", "Oui, en mouvement rectiligne uniforme.")])
    add("proj", "Physique – Chapitre {n} : Mouvement d’un projectile dans le champ de pesanteur",
        ["Établir les équations horaires et l’équation de la trajectoire.", "Calculer la flèche et la portée."],
        "Sous quel angle faut-il lancer un ballon pour aller le plus loin ?",
        "Un solide de masse m lancé de O avec une vitesse V⃗0 faisant l’angle α avec l’horizontale n’est soumis qu’à son poids : la RFD donne a⃗ = g⃗. Sur Ox : ax = 0, Vx = V0 cos α, x = V0 cos α · t (MRU). Sur Oy : ay = −g, Vy = −gt + V0 sin α, y = −½gt² + V0 sin α · t (MRUV). Équation de la trajectoire (parabole) : y = −g x²/(2V0² cos² α) + x tan α. Au sommet S, Vy = 0 : l’ordonnée yS est la flèche, yS = V0² sin² α/(2g). La portée est l’abscisse du point d’ordonnée nulle : xP = V0² sin 2α/g, maximale pour α = 45°.",
        ["a⃗ = g⃗.", "x = V0 cos α · t ; y = −½gt² + V0 sin α · t.", "y = −g x²/(2V0² cos² α) + x tan α.", "Flèche : V0² sin² α/(2g).", "Portée : V0² sin 2α/g."],
        ("V0 = 20 m/s, α = 45°, g = 10 : portée et flèche ?", "Portée = 400/10 = 40 m ; flèche = 400 × 0,5/20 = 10 m."),
        [S("Conditions initiales", table=[["Grandeur", "Sur Ox", "Sur Oy"], ["Accélération", "0", "−g"], ["Vitesse", "V0 cos α", "−gt + V0 sin α"], ["Position", "V0 cos α · t", "−½gt² + V0 sin α · t"]])],
        apps=[("Pour quel angle la portée est-elle maximale ?", "45°."), ("Au sommet, que vaut Vy ?", "0.")])
    add("plan", "Physique – Chapitre {n} : Solide sur un plan incliné et sur une piste circulaire",
        ["Appliquer la RFD à un solide sur un plan incliné avec frottement.", "Combiner théorème de l’énergie cinétique et RFD sur une piste circulaire."],
        "Une voiture qui descend une pente sans moteur accélère-t-elle toujours ?",
        "Sur un plan incliné d’angle α avec frottement f : P⃗ + f⃗ + R⃗n = m a⃗ ; sur l’axe du mouvement : mg sin α − f = ma, donc a = g sin α − f/m est constante : le mouvement est rectiligne uniformément varié ; sur la normale : Rn = mg cos α ; la réaction totale vaut R = √(Rn² + f²). Sur une piste circulaire lisse de rayon r, le théorème de l’énergie cinétique entre A et M donne VM² = VA² + 2gr sin θ (la réaction ne travaille pas, W(P) = mgr sin θ). La RFD projetée sur la normale centripète donne R = mg sin θ + mVM²/r, soit R = 3mg sin θ + mVA²/r. Le solide quitte la piste lorsque R s’annule.",
        ["Plan incliné : a = g sin α − f/m.", "Rn = mg cos α.", "Piste : VM² = VA² + 2gr sin θ.", "R = mg sin θ + mVM²/r.", "Décollement : R = 0."],
        ("Plan à 30°, sans frottement : accélération ?", "a = g sin 30° = 5 m/s² (g = 10)."),
        [S("Plan incliné", ["Ox : mg sin α − f = ma ; Oy : Rn − mg cos α = 0.", "R = √((mg cos α)² + (mg sin α − ma)²)."])],
        apps=[("Avec f = 2 N, m = 1 kg, α = 30° : a ?", "a = 5 − 2 = 3 m/s²."), ("Pourquoi la réaction d’une piste lisse ne travaille-t-elle pas ?", "Elle est perpendiculaire au déplacement.")])
    add("champE", "Physique – Chapitre {n} : Particule chargée dans un champ électrique uniforme",
        ["Étudier l’accélération d’une particule dans un champ uniforme.", "Établir la trajectoire dans un champ déviateur ; calculer la déviation."],
        "Comment fonctionne un oscilloscope cathodique ?",
        "Champ accélérateur : une particule de charge q > 0 et de masse m, entrant sans vitesse entre deux plaques A et C distantes de d0 avec U0 = VA − VC, a une accélération constante a = qU0/(m d0) : mouvement RUV ; à la sortie, V = √(2qU0/m). Champ déviateur : entre deux plaques horizontales P1 et P2 de longueur l, distantes de d, sous la tension U, la particule entrant en O avec V⃗0 horizontale subit a⃗ = qE⃗/m (verticale, a = qU/(md)). Sur Ox : x = V0t (MRU) ; sur Oy : y = (qU/(2md)) t². Trajectoire parabolique : y = qU x²/(2md V0²) pour 0 ≤ x ≤ l. Après les plaques, la particule suit un mouvement rectiligne uniforme ; la déviation sur l’écran se calcule par tan α = dy/dx en x = l.",
        ["Accélération : a = qU0/(md0).", "Vitesse de sortie : √(2qU0/m).", "Déviation : y = qU x²/(2mdV0²).", "Hors des plaques : MRU.", "tan α = qUl/(mdV0²)."],
        ("Électron accéléré sous 1 000 V (e = 1,6·10⁻¹⁹ C, m = 9,1·10⁻³¹ kg) : vitesse ?", "V = √(2 × 1,6·10⁻¹⁹ × 1000/9,1·10⁻³¹) ≈ 1,9·10⁷ m/s."),
        [S("Champ déviateur", table=[["Axe", "Accélération", "Vitesse", "Position"], ["Ox", "0", "V0", "V0t"], ["Oy", "qU/(md)", "(qU/(md))t", "(qU/(2md))t²"]])],
        apps=[("Le poids de l’électron est-il pris en compte ?", "Non, il est négligeable devant la force électrique."), ("Que devient la trajectoire si U double ?", "La déviation y double.")])
    add("sat", "Physique – Chapitre {n} : Satellites et gravitation",
        ["Exprimer g à une altitude h.", "Établir la vitesse et la période d’un satellite en orbite circulaire ; troisième loi de Kepler.", "Définir le satellite géostationnaire ; l’énergie mécanique d’un satellite."],
        "Pourquoi un satellite ne tombe-t-il pas sur la Terre ?",
        "Loi de la gravitation : F = G m M/r². À l’altitude h : g = GM/(R + h)² ; au sol g0 = GM/R², donc g = g0R²/r² (r = R + h). Pour un satellite en orbite circulaire, la RFD projetée sur la tangente donne aₜ = 0 : mouvement uniforme ; sur la normale : GmM/r² = mV²/r, d’où V = √(GM/r) = √(g0R²/r) et ω = √(GM/r³). Période T = 2π/ω = 2πr/V. Troisième loi de Kepler : r³/T² = GM/(4π²) = constante pour tous les satellites d’une même planète. Un satellite géostationnaire est fixe dans le référentiel terrestre : orbite dans le plan équatorial, même sens de rotation que la Terre, période T0 = 24 h = 86 400 s. Énergies : Ec = GmM/(2r) ; Ep = −GmM/r + K ; Em = −GmM/(2r) + K.",
        ["g = g0R²/(R + h)².", "V = √(GM/r).", "T = 2πr/V.", "Kepler : r³/T² = GM/(4π²).", "Géostationnaire : équatorial, T = 24 h."],
        ("Vitesse d’un satellite à r = 7 000 km (g0 = 9,8 ; R = 6 400 km) ?", "V = √(9,8 × (6,4·10⁶)²/7·10⁶) ≈ 7,6·10³ m/s."),
        [S("Satellite géostationnaire", ["Plan d’orbite : équateur ; sens : celui de la Terre ; T0 = 86 400 s.", "Kepler donne r ≈ 42 000 km (altitude ≈ 36 000 km)."])],
        apps=[("Le mouvement d’un satellite en orbite circulaire est-il uniforme ?", "Oui : aₜ = 0."), ("Si r est multiplié par 4, que devient T ?", "T est multiplié par 8 (T² ∝ r³).")])
    add("lor", "Physique – Chapitre {n} : Particule chargée dans un champ magnétique — force de Lorentz",
        ["Connaître la force de Lorentz F⃗ = qV⃗ ∧ B⃗.", "Montrer que le mouvement est circulaire uniforme ; calculer le rayon.", "Applications : déflexion, spectrographe de masse, cyclotron."],
        "Comment sépare-t-on les isotopes d’un élément ?",
        "Une particule de charge q et de vitesse V⃗ dans un champ magnétique B⃗ subit la force de Lorentz F⃗ = qV⃗ ∧ B⃗ : appliquée à la particule, perpendiculaire au plan (V⃗, B⃗), de sens tel que le trièdre (qV⃗, B⃗, F⃗) soit direct, d’intensité F = |q|VB sin(V⃗, B⃗). Si V⃗0 est perpendiculaire à un champ uniforme B⃗, l’accélération est perpendiculaire à B⃗ (mouvement plan) et à V⃗ (aₜ = 0 : mouvement uniforme) ; aₙ = |q|V0B/m = V0²/r constant : la trajectoire est un cercle de rayon r = mV0/(|q|B). Déviation angulaire : sin α = l/r (l largeur de la zone de champ) ; déviation linéaire sur l’écran O’I ≈ D tan α. Spectrographe de masse : des isotopes de même charge et même vitesse ont des rayons proportionnels à leurs masses.",
        ["F⃗ = qV⃗ ∧ B⃗ ; F = |q|VB sin θ.", "Mouvement plan, circulaire uniforme.", "r = mV0/(|q|B).", "sin α = l/r.", "Spectrographe : r ∝ m."],
        ("Proton (m = 1,67·10⁻²⁷ kg) à 10⁶ m/s dans B = 0,1 T : rayon ?", "r = 1,67·10⁻²⁷ × 10⁶/(1,6·10⁻¹⁹ × 0,1) ≈ 0,10 m."),
        [S("Caractéristiques de la force de Lorentz", table=[["Élément", "Caractéristique"], ["Point d’application", "la particule"], ["Direction", "perpendiculaire à V⃗ et à B⃗"], ["Sens", "trièdre (qV⃗, B⃗, F⃗) direct"], ["Intensité", "|q|VB sin(V⃗, B⃗)"]])],
        apps=[("La force de Lorentz travaille-t-elle ?", "Non : elle est perpendiculaire à la vitesse, l’énergie cinétique reste constante."), ("Que devient r si B double ?", "Il est divisé par 2.")])
    add("osc", "Physique – Chapitre {n} : Pendule élastique (oscillateur harmonique libre)",
        ["Établir l’équation différentielle x’’ + ω0²x = 0.", "Exprimer période, fréquence et relation indépendante du temps.", "Étudier l’énergie d’un pendule élastique."],
        "Pourquoi la période d’un ressort ne dépend-elle pas de l’amplitude ?",
        "Un mouvement est rectiligne sinusoïdal si x = Xm cos(ωt + φ) : v = −ωXm sin(ωt + φ), a = −ω²x, d’où l’équation différentielle x’’ + ω²x = 0. Période T = 2π/ω ; fréquence N = 1/T. Relation indépendante du temps : v² = ω²(Xm² − x²). Pendule élastique (masse m, raideur k) : à l’équilibre (vertical) mg = kΔl0 ; la RFD donne x’’ + (k/m)x = 0 : ω0 = √(k/m), T0 = 2π√(m/k), indépendante de l’amplitude. Énergie : Ec = ½mv², Ep = ½kx² (en prenant l’équilibre comme référence) ; Em = ½kXm² = ½mVmax² constante (système conservatif).",
        ["x = Xm cos(ωt + φ).", "x’’ + ω0²x = 0 ; ω0 = √(k/m).", "T0 = 2π√(m/k).", "v² = ω²(Xm² − x²).", "Em = ½kXm² constante."],
        ("m = 0,1 kg, k = 10 N/m : période ?", "T0 = 2π√(0,01) ≈ 0,63 s."),
        [S("Énergies", table=[["Énergie", "Expression"], ["Cinétique", "½mv²"], ["Potentielle élastique", "½kx²"], ["Mécanique", "½kXm² = ½mVmax²"]])],
        apps=[("Vitesse maximale si Xm = 5 cm et ω0 = 10 rad/s ?", "Vmax = 0,5 m/s."), ("Si m est multipliée par 4, T0 ?", "T0 double.")])
    add("lap", "Physique – Chapitre {n} : Force de Laplace",
        ["Connaître les caractéristiques de la force de Laplace.", "Utiliser la règle de la main droite.", "Appliquer : rails de Laplace, haut-parleur, moteur."],
        "Comment un courant peut-il mettre une tige en mouvement ?",
        "Un conducteur de longueur l parcouru par un courant I et plongé dans un champ magnétique B⃗ subit la force de Laplace F⃗ = I l⃗ ∧ B⃗ : appliquée au milieu de la portion placée dans le champ, perpendiculaire au plan formé par le conducteur et B⃗, de sens tel que le trièdre (I l⃗, B⃗, F⃗) soit direct, d’intensité F = I l B |sin(l⃗, B⃗)|. Règle de la main droite : le courant sort par le bout des doigts, la paume est tournée vers le sens de B⃗, le pouce indique le sens de F⃗. Applications : rails de Laplace, haut-parleur électrodynamique, moteur à courant continu.",
        ["F⃗ = I l⃗ ∧ B⃗.", "F = I l B sin θ.", "Règle de la main droite.", "Rails, haut-parleur, moteur."],
        ("I = 5 A, l = 0,1 m, B = 0,2 T, conducteur perpendiculaire à B⃗ : F ?", "F = 5 × 0,1 × 0,2 = 0,1 N."),
        [S("Caractéristiques", table=[["Élément", "Caractéristique"], ["Point d’application", "milieu de la portion dans le champ"], ["Direction", "perpendiculaire au conducteur et à B⃗"], ["Sens", "trièdre (Il⃗, B⃗, F⃗) direct"], ["Intensité", "I l B sin(l⃗, B⃗)"]])],
        apps=[("Que vaut F si le conducteur est parallèle à B⃗ ?", "0."), ("Comment inverser le sens de F ?", "Inverser le courant ou le champ.")])
    add("ind", "Physique – Chapitre {n} : Induction électromagnétique",
        ["Calculer le flux magnétique à travers un circuit.", "Calculer la f.é.m. induite (loi de Faraday) ; appliquer la loi de Lenz.", "Déterminer le sens du courant induit."],
        "Comment un alternateur produit-il de l’électricité ?",
        "À une surface S on associe le vecteur surface S⃗ = S n⃗ (n⃗ normal, orienté par la règle de la main droite selon le sens positif choisi sur le contour). Le flux de B⃗ à travers S : Φ = B⃗·S⃗ = BS cos(B⃗, n⃗) en weber (Wb) ; pour N spires Φ = NBS cos θ. Toute variation du flux à travers un circuit y fait apparaître une force électromotrice induite : moyenne em = −ΔΦ/Δt, instantanée e = −dΦ/dt (loi de Faraday). Si le circuit est fermé (résistance R), il est parcouru par un courant induit i = e/R. Loi de Lenz : le courant induit a un sens tel qu’il s’oppose, par ses effets, à la cause qui lui donne naissance.",
        ["Φ = NBS cos θ (Wb).", "e = −dΦ/dt.", "i = e/R.", "Lenz : opposition à la cause.", "Alternateur, transformateur."],
        ("Bobine de 100 spires de 20 cm², B passant de 0 à 0,5 T en 0,1 s (axe parallèle à B⃗) : f.é.m. moyenne ?", "ΔΦ = 100 × 2·10⁻³ × 0,5 = 0,1 Wb ; |e| = 1 V."),
        [S("Règle d’orientation", ["Main droite : la paume vers le centre de la surface, les doigts dans le sens positif du contour, le pouce donne n⃗."]),
         S("Loi de Lenz (exemple)", ["Un aimant approche d’une bobine par son pôle nord : la face de la bobine en regard devient une face nord et repousse l’aimant."])],
        apps=[("Unité du flux magnétique ?", "Le weber (Wb)."), ("Pourquoi le signe « − » dans e = −dΦ/dt ?", "Il traduit la loi de Lenz.")])
    add("rl", "Physique – Chapitre {n} : Auto-induction et circuit RL",
        ["Définir l’inductance et la f.é.m. d’auto-induction.", "Établir et résoudre l’équation de l’établissement et de la rupture du courant.", "Calculer la constante de temps et l’énergie magnétique."],
        "Pourquoi une étincelle apparaît-elle quand on ouvre un circuit contenant une bobine ?",
        "Une bobine (solénoïde) de N spires, de longueur l et de rayon R a pour inductance L = μ0πN²R²/l (henry) ; son flux propre est Φp = Li ; sa f.é.m. d’auto-induction e = −L di/dt ; la tension à ses bornes u = ri + L di/dt. Énergie magnétique emmagasinée : Em = ½Li². Établissement du courant dans un dipôle RL soumis à un échelon E (RT résistance totale) : di/dt + (RT/L)i = E/L ; solution i = IP(1 − e^(−t/τ)) avec IP = E/RT et τ = L/RT. Après τ, i atteint 63 % de IP. Rupture : di/dt + (RT/L)i = 0, i = IP e^(−t/τ) : après τ, i vaut 37 % de sa valeur initiale. La bobine s’oppose aux variations du courant.",
        ["L = μ0πN²R²/l.", "e = −L di/dt ; u = ri + L di/dt.", "Em = ½Li².", "i = IP(1 − e^(−t/τ)) ; τ = L/RT.", "Après τ : 63 % (établissement), 37 % (rupture)."],
        ("L = 0,1 H, RT = 10 Ω, E = 6 V : τ et IP ?", "τ = 0,01 s ; IP = 0,6 A."),
        [S("Établissement et rupture", table=[["Phase", "Équation", "Solution"], ["Établissement", "di/dt + (RT/L)i = E/L", "IP(1 − e^(−t/τ))"], ["Rupture", "di/dt + (RT/L)i = 0", "IP e^(−t/τ)"]])],
        apps=[("Énergie stockée dans L = 0,5 H parcourue par 2 A ?", "Em = ½ × 0,5 × 4 = 1 J."), ("En régime permanent, que vaut L di/dt ?", "0 : la bobine se comporte comme un résistor de résistance r.")])
    add("rc", "Physique – Chapitre {n} : Condensateur et circuit RC",
        ["Connaître Q = CU, la capacité d’un condensateur plan et l’énergie stockée.", "Associer des condensateurs.", "Étudier la charge et la décharge d’un circuit RC ; constante de temps."],
        "Comment le flash d’un appareil photo stocke-t-il son énergie ?",
        "Capacité : C = Q/U (farad) ; condensateur plan : C = εS/d. Énergie emmagasinée : E = Q²/(2C) = ½CU² = ½QU. En série : 1/Céq = Σ 1/Ci ; en parallèle : Céq = Σ Ci. Charge d’un circuit RC sous un échelon E : uR + uC = E, d’où duC/dt + uC/(RC) = E/(RC), solution uC = E(1 − e^(−t/τ)) avec τ = RC ; i = C duC/dt = (E/R)e^(−t/τ). Décharge : uC = E e^(−t/τ). Après τ, uC atteint 63 % de E (charge) ou 37 % de sa valeur initiale (décharge) ; τ s’obtient aussi par la tangente à l’origine ; au bout de 5τ, la charge est pratiquement terminée.",
        ["C = Q/U ; plan : C = εS/d.", "E = ½CU².", "Série : 1/Céq = Σ1/Ci ; parallèle : Céq = ΣCi.", "uC = E(1 − e^(−t/τ)) ; τ = RC.", "Après τ : 63 % ; après 5τ : chargé."],
        ("R = 10 kΩ, C = 100 µF : τ ?", "τ = 10⁴ × 10⁻⁴ = 1 s."),
        [S("Charge et décharge", table=[["Phase", "uC(t)", "i(t)"], ["Charge", "E(1 − e^(−t/τ))", "(E/R)e^(−t/τ)"], ["Décharge", "E e^(−t/τ)", "−(E/R)e^(−t/τ)"]])],
        apps=[("Énergie d’un condensateur de 10 µF chargé sous 100 V ?", "E = ½ × 10⁻⁵ × 10⁴ = 0,05 J."), ("Deux condensateurs de 6 µF en série ?", "Céq = 3 µF.")])
    add("lc", "Physique – Chapitre {n} : Oscillations électriques libres (LC et RLC)",
        ["Établir l’équation différentielle d’un circuit LC.", "Exprimer la période propre et l’énergie totale.", "Décrire l’amortissement dans un circuit RLC libre."],
        "Comment un circuit électrique peut-il osciller comme un ressort ?",
        "Lors de la décharge d’un condensateur dans une bobine idéale (circuit LC libre), la loi des mailles q/C + L di/dt = 0 donne d²q/dt² + q/(LC) = 0 : oscillations sinusoïdales libres de la charge q = Qm cos(ω0t + φ), avec ω0 = 1/√(LC) et la période propre T0 = 2π√(LC). L’énergie totale E = q²/(2C) + ½Li² = Qm²/(2C) = ½LIm² est constante : le système est conservatif (échange entre énergie électrique et magnétique). Dans un circuit RLC libre, q/C + L di/dt + Ri = 0 donne d²q/dt² + (R/L)dq/dt + q/(LC) = 0 : le terme (R/L)dq/dt traduit l’amortissement ; en régime pseudo-périodique, la pseudo-période est voisine de T0 ; si l’amortissement est fort, le régime est apériodique. L’énergie diminue par effet Joule.",
        ["d²q/dt² + q/(LC) = 0.", "T0 = 2π√(LC).", "E = Qm²/(2C) = ½LIm² (LC).", "RLC : amortissement (effet Joule).", "Pseudo-périodique / apériodique."],
        ("L = 0,1 H, C = 10 µF : période propre ?", "T0 = 2π√(10⁻⁶) ≈ 6,3 ms."),
        [S("Analogie mécanique", table=[["Électrique", "Mécanique"], ["q", "x"], ["L", "m"], ["1/C", "k"], ["R", "frottement"]])],
        apps=[("Énergie d’un circuit LC avec C = 1 µF et Qm = 10⁻⁵ C ?", "E = 10⁻¹⁰/(2·10⁻⁶) = 5·10⁻⁵ J."), ("Pourquoi les oscillations d’un RLC s’amortissent-elles ?", "La résistance dissipe l’énergie par effet Joule.")])
    add("ac", "Physique – Chapitre {n} : Oscillations électriques forcées — circuit RLC en régime sinusoïdal",
        ["Définir valeurs efficaces et impédance.", "Calculer l’impédance et le déphasage d’un RLC série.", "Étudier la résonance d’intensité, la bande passante et le facteur de qualité ; la puissance moyenne."],
        "Comment une radio sélectionne-t-elle une seule station ?",
        "Pour i = Im cos(ωt + φi) : Ieff = Im/√2 ; Ueff = Um/√2. L’impédance d’un dipôle est Z = Um/Im = U/I. Un circuit RLC série alimenté par u = Um cos(ωt + φu) est parcouru par un courant sinusoïdal de même pulsation ; Z = √(R² + (Lω − 1/(Cω))²) ; le déphasage φ de u par rapport à i vérifie cos φ = R/Z et tan φ = (Lω − 1/(Cω))/R. Résonance d’intensité pour Lω = 1/(Cω), soit ω0 = 1/√(LC) : l’impédance est minimale (Z = R), le circuit se comporte comme un résistor, l’intensité efficace est maximale (I0 = U/R), u et i sont en phase. Bande passante : Δω = ω2 − ω1 = R/L (avec ω1ω2 = ω0²) ; facteur de qualité Q = ω0/Δω = Lω0/R = (1/R)√(L/C). Puissance moyenne : P = UI cos φ = RI².",
        ["Ieff = Im/√2.", "Z = √(R² + (Lω − 1/(Cω))²).", "tan φ = (Lω − 1/(Cω))/R.", "Résonance : ω0 = 1/√(LC), Z = R.", "Q = Lω0/R ; P = UI cos φ."],
        ("R = 10 Ω, L = 0,1 H, C = 10 µF : pulsation de résonance ?", "ω0 = 1/√(10⁻⁶) = 1000 rad/s."),
        [S("À la résonance", ["Z = R ; I = U/R maximale ; φ = 0 ; cos φ = 1.", "Bande passante Δω = R/L ; Q = ω0/Δω."])],
        apps=[("Valeur efficace de u = 311 cos(100πt) ?", "≈ 220 V."), ("Facteur de qualité pour R = 10 Ω, L = 0,1 H, ω0 = 1000 rad/s ?", "Q = 100/10 = 10.")])
    add("prop", "Physique – Chapitre {n} : Propagation d’un mouvement vibratoire (milieu unidimensionnel)",
        ["Utiliser le stroboscope.", "Distinguer ondes transversales et longitudinales ; célérité, longueur d’onde.", "Établir l’équation d’un point du milieu ; comparer les vibrations de deux points."],
        "Pourquoi une vague avance-t-elle alors que l’eau ne fait que monter et descendre ?",
        "Stroboscope de fréquence Ne éclairant un phénomène de fréquence N : immobilité apparente si N = kNe ; k positions immobiles si Ne = kN ; mouvement ralenti si N et Ne sont voisines (direct si N légèrement supérieure à Ne, inverse sinon). Un ébranlement est transversal si la vibration est perpendiculaire à la propagation, longitudinal si elle est parallèle. Le long d’une corde tendue (tension F, masse linéique μ), la célérité est V = √(F/μ). La longueur d’onde λ = VT est la distance parcourue pendant une période. Si la source vibre selon yS = a cos(ωt + φS), un point M à la distance x reçoit la vibration avec un retard θ = x/V : yM = a cos(ωt + φS − 2πx/λ). Deux points distants de kλ vibrent en phase, de (2k + 1)λ/2 en opposition de phase.",
        ["Stroboscope : immobilité si N = kNe.", "Transversale / longitudinale.", "V = √(F/μ) ; λ = VT.", "yM = a cos(ωt + φS − 2πx/λ).", "En phase : kλ ; opposition : (2k + 1)λ/2."],
        ("Corde : N = 50 Hz, V = 10 m/s : longueur d’onde ?", "λ = V/N = 0,2 m."),
        [S("Comparaison de deux points", table=[["Distance", "État vibratoire"], ["d = kλ", "en phase"], ["d = (2k + 1)λ/2", "en opposition de phase"], ["d = (2k + 1)λ/4", "en quadrature"]])],
        apps=[("Fréquence N = 100 Hz, stroboscope Ne = 50 Hz : aspect ?", "Immobilité apparente (N = 2Ne)."), ("Le son est-il une onde transversale ?", "Non, longitudinale.")])
    add("interm", "Physique – Chapitre {n} : Interférences mécaniques à la surface d’un liquide",
        ["Établir l’élongation d’un point recevant deux ondes cohérentes.", "Déterminer les points d’amplitude maximale et nulle (franges)."],
        "Que se passe-t-il quand deux pointes frappent en même temps la surface de l’eau ?",
        "Une source S vibrant selon yS = a cos(ωt + φS) donne en M, à la distance d, yM = a cos(ωt − 2πd/λ + φS). Deux sources S1 et S2 cohérentes (en phase) donnent en M (distances d1 et d2) : yM = 2a cos(π(d2 − d1)/λ) cos(ωt − π(d1 + d2)/λ + φS). Si d2 − d1 = kλ, M vibre avec une amplitude maximale 2a ; si d2 − d1 = (2k + 1)λ/2, M est immobile (amplitude nulle). Les points d’amplitude maximale (ou nulle) se trouvent sur des hyperboles de foyers S1 et S2 : les franges d’interférence. Entre S1 et S2, deux franges consécutives de même nature sont distantes de λ/2.",
        ["Sources cohérentes.", "Amplitude 2a|cos(π(d2 − d1)/λ)|.", "Maximum : d2 − d1 = kλ.", "Immobile : d2 − d1 = (2k + 1)λ/2.", "Franges hyperboliques ; distance λ/2 sur S1S2."],
        ("λ = 1 cm, d1 = 3 cm, d2 = 5 cm : état de M ?", "d2 − d1 = 2λ : amplitude maximale."),
        [S("Résultats", table=[["d2 − d1", "Amplitude"], ["kλ", "maximale (2a)"], ["(2k + 1)λ/2", "nulle"]])],
        apps=[("Distance entre deux franges d’amplitude maximale consécutives sur S1S2 si λ = 8 mm ?", "4 mm."), ("Pourquoi les sources doivent-elles être cohérentes ?", "Pour garder un déphasage constant et obtenir des franges stables.")])
    add("intl", "Physique – Chapitre {n} : Interférences lumineuses (fentes de Young)",
        ["Relier fréquence, longueur d’onde et énergie d’un photon.", "Calculer la différence de marche et la position des franges.", "Calculer l’interfrange ; étudier la coïncidence de deux radiations."],
        "La lumière ajoutée à la lumière peut-elle donner l’obscurité ?",
        "La fréquence d’une radiation est ν = 1/T = c/λ ; un photon porte l’énergie E = hν = hc/λ (h = 6,62·10⁻³⁴ J·s). Dans le dispositif de Young (fentes distantes de a, écran à la distance D), la différence de marche au point M d’abscisse x est δ = d2 − d1 = ax/D. M est sur une frange brillante si δ = kλ, soit x = kλD/a ; sur une frange sombre si δ = (2k’ + 1)λ/2, soit x = (2k’ + 1)λD/(2a). L’interfrange (distance entre deux franges consécutives de même nature) est i = λD/a. L’ordre d’interférence p = x/i est entier pour une frange brillante, demi-entier pour une frange sombre. Avec deux radiations λ1 et λ2, les franges brillantes coïncident lorsque k1λ1 = k2λ2 ; la première coïncidence correspond aux plus petites valeurs de k1 et k2.",
        ["E = hν = hc/λ.", "δ = ax/D.", "Brillante : x = kλD/a ; sombre : x = (2k + 1)λD/(2a).", "i = λD/a.", "Coïncidence : k1λ1 = k2λ2."],
        ("λ = 600 nm, a = 1 mm, D = 2 m : interfrange ?", "i = 6·10⁻⁷ × 2/10⁻³ = 1,2 mm."),
        [S("Franges", table=[["Frange", "Différence de marche", "Position"], ["Brillante", "kλ", "kλD/a"], ["Sombre", "(2k + 1)λ/2", "(2k + 1)λD/(2a)"]])],
        apps=[("Énergie d’un photon de 500 nm ?", "E = 6,62·10⁻³⁴ × 3·10⁸/5·10⁻⁷ ≈ 4·10⁻¹⁹ J."), ("λ1 = 400 nm, λ2 = 600 nm : première coïncidence ?", "3λ1 = 2λ2 : k1 = 3, k2 = 2.")])
    add("niv", "Physique – Chapitre {n} : Niveaux d’énergie de l’atome d’hydrogène",
        ["Utiliser En = −13,6/n² eV.", "Interpréter excitation, désexcitation, ionisation.", "Calculer les longueurs d’onde des séries de Lyman, Balmer…"],
        "Pourquoi le spectre de l’hydrogène est-il formé de raies ?",
        "L’énergie de l’atome d’hydrogène est quantifiée : En = −13,6/n² eV (n = 1, 2, 3…), avec 1 eV = 1,6·10⁻¹⁹ J. n = 1 : état fondamental (E1 = −13,6 eV) ; n > 1 : états excités ; n → ∞ : état ionisé (E∞ = 0). L’excitation est une transition vers un niveau supérieur par absorption d’un photon ; la désexcitation, vers un niveau inférieur, s’accompagne de l’émission d’un photon : hν = En − Ep. L’ionisation libère l’électron. Formule de Ritz : 1/λ = RH(1/p² − 1/n²) avec RH = 1,09·10⁷ m⁻¹. Série de Lyman : retour au niveau p = 1 (ultraviolet) ; série de Balmer : retour à p = 2 (visible en partie) ; Paschen : p = 3.",
        ["En = −13,6/n² eV.", "Fondamental n = 1 ; ionisé E = 0.", "hν = En − Ep.", "1/λ = RH(1/p² − 1/n²).", "Lyman p = 1 ; Balmer p = 2."],
        ("Énergie d’ionisation de l’atome d’hydrogène à l’état fondamental ?", "13,6 eV."),
        [S("Séries spectrales", table=[["Série", "Niveau final p", "Domaine"], ["Lyman", "1", "ultraviolet"], ["Balmer", "2", "visible (et proche UV)"], ["Paschen", "3", "infrarouge"]]),
         S("Exemple", ["Transition 3 → 2 : ΔE = −1,51 − (−3,4) = 1,89 eV ⇒ λ = hc/ΔE ≈ 656 nm (raie rouge)."])],
        apps=[("E2 et E3 ?", "E2 = −3,4 eV ; E3 ≈ −1,51 eV."), ("Un photon de 10,2 eV peut-il être absorbé à l’état fondamental ?", "Oui : E2 − E1 = 10,2 eV.")])
    add("photo", "Physique – Chapitre {n} : Effet photoélectrique",
        ["Définir l’effet photoélectrique, la fréquence seuil et le travail d’extraction.", "Utiliser la relation d’Einstein et le potentiel d’arrêt.", "Calculer la puissance lumineuse, le courant de saturation et le rendement quantique."],
        "Pourquoi une lumière rouge intense ne libère-t-elle aucun électron d’un métal alors qu’une faible lumière ultraviolette le fait ?",
        "L’effet photoélectrique est l’émission d’électrons par un métal exposé à un rayonnement électromagnétique convenable. La fréquence seuil ν0 est la fréquence minimale capable d’arracher des électrons (λ0 = c/ν0 longueur d’onde seuil) ; l’énergie d’extraction est W0 = hν0. Relation d’Einstein : hν = W0 + Ec max. L’énergie cinétique maximale se mesure par le potentiel d’arrêt U0 : ½mVmax² = eU0, d’où U0 = (h/e)(ν − ν0). La puissance lumineuse reçue est P = n h ν (n photons par seconde) ; le rendement quantique r = n’/n (n’ photons efficaces) ; le courant de saturation IS = n’e = P e r/(hν).",
        ["Seuil : ν ≥ ν0.", "W0 = hν0.", "hν = W0 + Ec max.", "eU0 = Ec max.", "IS = Per/(hν)."],
        ("Métal de seuil λ0 = 500 nm éclairé par λ = 400 nm : Ec max ?", "hc(1/λ − 1/λ0) = 6,62·10⁻³⁴ × 3·10⁸ × (2,5·10⁶ − 2·10⁶) ≈ 1·10⁻¹⁹ J (≈ 0,62 eV)."),
        [S("Relations", table=[["Grandeur", "Expression"], ["Travail d’extraction", "W0 = hν0 = hc/λ0"], ["Einstein", "hν = W0 + Ec max"], ["Potentiel d’arrêt", "U0 = (h/e)(ν − ν0)"], ["Courant de saturation", "IS = Per/(hν)"]])],
        apps=[("L’effet dépend-il de l’intensité lumineuse pour se produire ?", "Non, il dépend de la fréquence (ν ≥ ν0) ; l’intensité agit sur le nombre d’électrons."), ("Ec max = 1 eV : potentiel d’arrêt ?", "U0 = 1 V.")])
    add("nuc", "Physique – Chapitre {n} : Réactions nucléaires",
        ["Calculer le défaut de masse et l’énergie de liaison par nucléon.", "Écrire les désintégrations α, β⁻, β⁺, γ.", "Distinguer fission et fusion ; calculer l’énergie libérée."],
        "D’où vient l’énergie fantastique du Soleil ?",
        "Le défaut de masse d’un noyau ᴬ_Z X est Δm = Zmp + (A − Z)mn − m(noyau) > 0. Relation d’Einstein : E = mc². L’énergie de liaison El = Δm c² ; l’énergie de liaison par nucléon El/A mesure la stabilité. Désintégrations : α : ᴬ_Z X → ᴬ⁻⁴_(Z−2) Y + ⁴₂He ; β⁻ : ᴬ_Z X → ᴬ_(Z+1) Y + ⁰₋₁e (un neutron devient proton) ; β⁺ : ᴬ_Z X → ᴬ_(Z−1) Y + ⁰₁e (un proton devient neutron) ; γ : Y* → Y + γ. Fission : un noyau lourd fissile bombardé par un neutron se scinde en deux noyaux plus légers et libère k neutrons (réaction en chaîne). Fusion : deux petits noyaux s’unissent pour former un noyau plus lourd (²₁H + ³₁H → ⁴₂He + ¹₀n). L’énergie libérée vaut E = (Σm réactifs − Σm produits)c².",
        ["Δm = Zmp + (A − Z)mn − m.", "El = Δm c² ; El/A.", "α, β⁻, β⁺, γ.", "Fission (noyaux lourds), fusion (légers).", "E = Δm c²."],
        ("Δm = 0,03 u : énergie de liaison (1 u ↔ 931,5 MeV) ?", "El ≈ 27,9 MeV."),
        [S("Exemples de fusion", ["²₁H + ³₁H → ⁴₂He + ¹₀n.", "²₁H + ²₁H → ³₁H + ¹₁p."]),
         S("Fission", ["ᴬX + ¹₀n → ᴬ¹X1 + ᴬ²X2 + k ¹₀n : les neutrons émis entretiennent la réaction en chaîne."])],
        apps=[("Quel est le noyau le plus stable : El/A = 8,8 MeV ou 7,6 MeV ?", "Celui de 8,8 MeV/nucléon."), ("Équation de la désintégration β⁻ du ⁶⁰₂₇Co ?", "⁶⁰₂₇Co → ⁶⁰₂₈Ni + ⁰₋₁e.")])
    return out


_P = {"cin": [5, 13], "rfd": [14, 22], "proj": [23, 28], "plan": [29, 36], "champE": [37, 46], "sat": [47, 59],
      "lor": [60, 94], "osc": [95, 113], "lap": [114, 125], "ind": [126, 141], "rl": [142, 164], "rc": [165, 183],
      "lc": [184, 197], "ac": [198, 223], "prop": [224, 243], "interm": [244, 255], "intl": [256, 271],
      "niv": [272, 283], "photo": [284, 298], "nuc": [299, 320]}
_N = {"cin": "I", "rfd": "I", "proj": "II", "plan": "II", "champE": "II", "sat": "II", "lor": "II", "osc": "III",
      "lap": "IV", "ind": "V", "rl": "VI", "rc": "VII", "lc": "VIII", "ac": "IX", "prop": "X", "interm": "XI",
      "intl": "XII", "niv": "XIII", "photo": "XIV", "nuc": "XV"}
LESSONS = lessons(_P, _N)
