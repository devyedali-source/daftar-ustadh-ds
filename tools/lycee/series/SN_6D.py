# Série d'exercices de fin de chapitre — Sciences naturelles 6D.
SERIES = {'Chapitre I : Cycles de développement et cycles chromosomiques': [{'k': 'book',
                                                                    'src': 'p. 56, ex. 3',
                                                                    'q': 'Un brin d’ADN a la séquence 3’ ACC GAC TAT ATA TAT CCG CAC TAC TTC GAC ACT 5’. 1) Séquence de l’autre brin. 2) Enzyme qui '
                                                                         'fabrique l’ARNm et où elle se fixe. 3) Séquence de l’ARNm si ce brin est transcrit.',
                                                                    'a': '1) 5’ TGG CTG ATA TAT ATA GGC GTG ATG AAG CTG TGA 3’ (A–T, C–G).\n'
                                                                         '2) L’ARN polymérase, qui se fixe au promoteur, au début du gène, et lit le brin transcrit dans le sens 3’ → 5’.\n'
                                                                         '3) 5’ UGG CUG AUA UAU AUA GGC GUG AUG AAG CUG UGA 3’ (même séquence que le brin non transcrit, avec U à la place de T). À '
                                                                         'partir du codon AUG : Met – Lys – Leu – stop (UGA).'},
                                                                   {'k': 'book',
                                                                    'src': 'p. 57, ex. 4 (1 à 5)',
                                                                    'q': '1) La méiose produit : 2 cellules haploïdes / 4 diploïdes / 4 haploïdes / 2 diploïdes ? 2) La transcription utilise : l’ARN '
                                                                         'polymérase sur le brin codant / l’ARN polymérase sur le brin non codant / l’ADN polymérase ? 3) La réplication de l’ADN a '
                                                                         'lieu grâce à : l’ARN polymérase / l’ADN polymérase durant l’interphase ? 4) L’épissage alternatif produit : un seul ARNm / '
                                                                         'de multiples ARNm / … ? 5) La mitose : source de diversité / donne 4 cellules / conserve le caryotype / produit les gamètes '
                                                                         '?',
                                                                    'a': '1) 4 cellules haploïdes à partir d’une cellule diploïde. 2) L’ARN polymérase qui lit le brin non codant (brin transcrit). 3) '
                                                                         'L’ADN polymérase, pendant la phase S de l’interphase. 4) De multiples ARNm à partir d’un même pré-ARNm. 5) Elle conserve '
                                                                         'toutes les caractéristiques du caryotype.'},
                                                                   {'k': 'book',
                                                                    'src': 'p. 57, ex. 4 (6 à 14)',
                                                                    'q': '6) Composition d’un chromosome métaphasique ? 8) Combien de nucléofilaments contient-il ? 9) L’ADN est-il le seul '
                                                                         'constituant du chromosome ? 10) Qu’est-ce qu’un gène ? 11) Caractéristiques de l’interphase ? 12) Évolution des chromosomes '
                                                                         'au cours du cycle ? 13) Résultat d’un cycle cellulaire ? 14) Nombre de chromosomes humains ?',
                                                                    'a': '6) Deux chromatides réunies par le centromère. 8) Deux. 9) Non : l’ADN est le support de l’information génétique et l’un des '
                                                                         'constituants (avec des protéines). 10) Une succession de nucléotides codant un polypeptide. 11) Trois étapes (G1, S, G2), '
                                                                         'avec synthèses moléculaires. 12) Ils se condensent avant la mitose et alternent entre une et deux chromatides. 13) Deux '
                                                                         'cellules dont les chromosomes ont un nucléofilament. 14) 23 paires (46).'},
                                                                   {'k': 'book',
                                                                    'src': 'p. 55-56, ex. 2',
                                                                    'q': 'On cultive des cellules somatiques et on mesure la quantité d’ADN par cellule ; à un temps t, on ajoute de la colchicine. '
                                                                         'Que représentent la phase où la quantité d’ADN double, et celle où elle est divisée par deux ? Quelle est l’action de la '
                                                                         'colchicine ?',
                                                                    'a': 'Le doublement correspond à la réplication (phase S) ; la division par deux à la mitose (anaphase-télophase). La colchicine '
                                                                         'empêche la formation du fuseau : les chromosomes ne se séparent pas, la quantité d’ADN reste doublée (et peut doubler encore '
                                                                         'au cycle suivant, polyploïdie).'},
                                                                   {'k': 'extra',
                                                                    'src': '',
                                                                    'q': 'Une cellule de 2n = 8 chromosomes entre en méiose. Combien de chromosomes et de chromatides en prophase I, à la fin de la '
                                                                         'méiose I et à la fin de la méiose II ? Combien de combinaisons différentes de gamètes le brassage interchromosomique '
                                                                         'permet-il ?',
                                                                    'a': 'Prophase I : 8 chromosomes, 16 chromatides. Fin de méiose I : n = 4 chromosomes à 2 chromatides. Fin de méiose II : 4 '
                                                                         'chromosomes à 1 chromatide. Combinaisons : 2ⁿ = 2⁴ = 16 (sans compter les crossing-over).'},
                                                                   {'k': 'extra',
                                                                    'src': '',
                                                                    'q': 'Expliquer comment la méiose et la fécondation assurent à la fois la stabilité du caryotype et la diversité des individus.',
                                                                    'a': 'La méiose divise par deux le nombre de chromosomes (gamètes à n) et la fécondation le rétablit (cellule-œuf à 2n) : le '
                                                                         'caryotype est stable. Le brassage inter- et intrachromosomique de la méiose et la rencontre au hasard des gamètes créent des '
                                                                         'combinaisons d’allèles nouvelles.'},
                                                                   {'k': 'int',
                                                                    'src': '',
                                                                    'q': 'Un enfant atteint de trisomie 21 possède 47 chromosomes.\n'
                                                                         '1) Quelle anomalie présente son caryotype ?\n'
                                                                         '2) Expliquer, à l’aide d’un schéma de la méiose, comment un gamète peut contenir deux chromosomes 21.',
                                                                    'a': '1) Trois chromosomes 21 au lieu de deux.\n'
                                                                         '2) Lors de l’anaphase I (non-disjonction des homologues) ou de l’anaphase II (non-séparation des chromatides), les deux '
                                                                         'chromosomes 21 partent vers le même pôle : un gamète reçoit deux chromosomes 21 ; fécondé par un gamète normal, il donne une '
                                                                         'cellule-œuf à 47 chromosomes.'}],
 'Chapitre II : Régulation de la glycémie et diabète': [{'k': 'book',
                                                         'src': 'p. 91, ex. 1 (1 à 6)',
                                                         'q': 'Choisir la (les) bonne(s) réponse(s) : 1) Le foie : seul effecteur / libère seulement du glucose / possède les enzymes d’hydrolyse du '
                                                              'glycogène / est sensible au glucagon ? 2) La glycémie : taux de glucose plasmatique / diminuée chez le diabétique / diminue après un '
                                                              'exercice / diminue après un repas ? 3) Une glycosurie est ? 4) La néoglucogenèse est ? 5) L’insuline : non protéique / hyperglycémiante '
                                                              '/ sécrétée après un repas riche en glucides / sécrétée par le pancréas exocrine ? 6) Valeur de référence de la glycémie ?',
                                                         'a': '1) C et D. 2) A et C. 3) La présence de glucose dans les urines. 4) La synthèse de glucose à partir de substrats non glucidiques. 5) C '
                                                              '(elle est sécrétée par les cellules β des îlots de Langerhans, pancréas endocrine). 6) Environ 0,9 g/L, variable régulée car le corps a '
                                                              'besoin de glucose en permanence.'},
                                                        {'k': 'book',
                                                         'src': 'p. 94, ex. 1 (7 à 12)',
                                                         'q': '7) Le foie stocke/déstocke-t-il le glucose ? 8) L’insuline est-elle hypo- ou hyperglycémiante ? 9) Le pancréas : rôle des îlots de '
                                                              'Langerhans ? 10) Effets du glucagon ? 11) Éléments nécessaires à la régulation ? 12) Effets d’une hyperglycémie chez un individu sain ?',
                                                         'a': '7) Oui : il stocke le glucose sous forme de glycogène (et de triglycérides) et le libère par glycogénolyse. 8) Hypoglycémiante. 9) '
                                                              'Partie exocrine digestive majoritaire et îlots de Langerhans qui captent les variations de glycémie et sécrètent insuline et glucagon. '
                                                              '10) Il favorise la glycogénolyse hépatique et s’oppose à l’insuline. 11) Des capteurs (cellules des îlots), des messagers hormonaux '
                                                              '(insuline, glucagon) et des effecteurs (foie, muscles, tissu adipeux). 12) Libération d’insuline par les cellules β, entrée du glucose '
                                                              'dans les cellules musculaires, hépatiques et adipeuses, stockage en glycogène et triglycérides.'},
                                                        {'k': 'extra',
                                                         'src': '',
                                                         'q': 'Chez un chien, l’ablation du pancréas provoque une hyperglycémie et une glycosurie ; la greffe d’un pancréas sous la peau (sans '
                                                              'connexion au tube digestif) les fait disparaître. Conclure.',
                                                         'a': 'Le pancréas est indispensable à la régulation de la glycémie ; il agit par voie sanguine (hormones), et non par son suc digestif : '
                                                              'c’est sa fonction endocrine (insuline) qui est en jeu.'},
                                                        {'k': 'extra',
                                                         'src': '',
                                                         'q': 'Pourquoi le glucose apparaît-il dans l’urine au-delà d’une glycémie d’environ 1,8 g/L ?',
                                                         'a': 'Le glucose filtré par le rein est normalement réabsorbé en totalité ; au-delà du seuil rénal (≈ 1,8 g/L) les transporteurs sont saturés '
                                                              ': le glucose non réabsorbé passe dans l’urine (glycosurie).'},
                                                        {'k': 'extra',
                                                         'src': '',
                                                         'q': 'Distinguer diabète de type 1 et diabète de type 2 (cause, âge d’apparition, traitement).',
                                                         'a': 'Type 1 : destruction auto-immune des cellules β, absence d’insuline, souvent chez le jeune ; traitement par injections d’insuline. Type '
                                                              '2 : résistance des cellules à l’insuline (souvent avec surpoids et sédentarité), chez l’adulte ; traitement par régime, activité '
                                                              'physique, médicaments, parfois insuline.'},
                                                        {'k': 'int',
                                                         'src': '',
                                                         'q': 'Un homme de 50 ans en surpoids a une glycémie à jeun de 1,4 g/L à deux reprises et se plaint d’une soif intense.\n'
                                                              '1) Quel diagnostic ? Justifier.\n'
                                                              '2) Pourquoi a-t-il soif ?\n'
                                                              '3) Proposer des conseils d’hygiène de vie.',
                                                         'a': '1) Un diabète (glycémie à jeun ≥ 1,26 g/L à deux reprises), probablement de type 2.\n'
                                                              '2) La glycosurie entraîne une perte d’eau importante dans l’urine (polyurie), d’où la soif.\n'
                                                              '3) Réduire les sucres rapides et les graisses, perdre du poids, pratiquer une activité physique régulière, suivre le traitement et '
                                                              'contrôler régulièrement sa glycémie.'}],
 'Chapitre III : Réponses immunitaires non spécifiques': [{'k': 'book',
                                                           'src': 'p. 129, ex. 1 (1 à 7)',
                                                           'q': '1) Combien de symptômes accompagnent une réaction inflammatoire ? 2) Les cellules de l’immunité sont ? 4) L’immunité innée repose sur '
                                                                'des mécanismes : héréditaires / non conservés / tardifs / spécifiques des bactéries ? 5) La phagocytose a lieu lors de l’immunité '
                                                                'innée, adaptative ou les deux ? 6) Le thymus contient ? 7) Un antigène est ?',
                                                           'a': '1) 4 : rougeur, chaleur, gonflement (œdème), douleur. 2) Les globules blancs (leucocytes). 4) Génétiquement héréditaires. 5) Lors des '
                                                                'deux. 6) Des lymphocytes T (LT CD4, CD8). 7) Une molécule étrangère qui provoque une réaction du système immunitaire.'},
                                                          {'k': 'book',
                                                           'src': 'p. 132, ex. 1 (8 à 13)',
                                                           'q': '8) Leucocytes sécréteurs d’anticorps ? 9) Un anticorps se lie à ? 10) Composition d’une molécule d’anticorps ? 11) Zones variables ? '
                                                                '12) Un LT8 se différencie en ? 13) L’immunité innée est assurée par ?',
                                                           'a': '8) Les plasmocytes. 9) À un antigène spécifique ; il neutralise les antigènes dans le milieu extracellulaire. 10) Deux chaînes '
                                                                'légères et deux chaînes lourdes (forme en Y). 11) Les chaînes H et L possèdent des zones variables (site de fixation de l’antigène) '
                                                                'et des zones constantes. 12) En lymphocyte T cytotoxique. 13) Les cellules phagocytaires, mobilisées très rapidement.'},
                                                          {'k': 'extra',
                                                           'src': '',
                                                           'q': 'Décrire les étapes de la phagocytose.',
                                                           'a': 'Adhésion du phagocyte au microbe, ingestion par émission de pseudopodes (vésicule de phagocytose), digestion par les enzymes des '
                                                                'lysosomes, rejet des déchets.'},
                                                          {'k': 'extra',
                                                           'src': '',
                                                           'q': 'Pourquoi une greffe de peau entre deux personnes non apparentées est-elle rejetée, alors qu’une autogreffe ne l’est pas ?',
                                                           'a': 'Les cellules portent des marqueurs du soi (CMH/HLA) propres à chaque individu ; ceux du donneur sont reconnus comme non-soi et '
                                                                'déclenchent une réaction immunitaire. L’autogreffe porte les mêmes marqueurs.'},
                                                          {'k': 'int',
                                                           'src': '',
                                                           'q': 'Un élève se blesse avec un clou rouillé. Quelques heures plus tard, la plaie est rouge, chaude, gonflée et douloureuse, puis du pus '
                                                                'apparaît.\n'
                                                                '1) Nommer et expliquer cette réaction.\n'
                                                                '2) Que contient le pus ?\n'
                                                                '3) Pourquoi le médecin vérifie-t-il qu’il est vacciné contre le tétanos ?',
                                                           'a': '1) Réaction inflammatoire : dilatation des vaisseaux (rougeur, chaleur), sortie de plasma (gonflement), excitation des terminaisons '
                                                                'nerveuses (douleur), afflux de phagocytes.\n'
                                                                '2) Des phagocytes morts, des microbes et des débris cellulaires.\n'
                                                                '3) La bactérie du tétanos peut entrer par une plaie souillée ; seule une immunité adaptative (vaccin, mémoire) protège efficacement '
                                                                'contre sa toxine.'}],
 'Chapitre IV : Les protides': [{'k': 'book',
                                 'src': 'p. 171, ex. 1 (1 à 8)',
                                 'q': '1) Les êtres vivants se caractérisent par ? 2) Proportion de carbone dans le vivant ? 3) Éléments chimiques principaux ? 4) Atome le plus abondant en masse '
                                      'dans une cellule ? 5) Molécules les plus abondantes ? 6) Une liaison peptidique unit ? 7) Deux sortes de substances constituant un organisme ? 8) Pourcentage '
                                      'd’eau du corps ?',
                                 'a': '1) Leur matière carbonée et leur richesse en eau. 2) Plus importante que dans le monde inerte. 3) C, H, O (et N). 4) L’oxygène. 5) L’eau. 6) Deux acides '
                                      'aminés. 7) Minérales et organiques. 8) Environ 60 à 65 %.'},
                                {'k': 'book',
                                 'src': 'p. 175, ex. 1 (10 à 14)',
                                 'q': '10) Le glucose est un ? 11) Le saccharose est un ? 12) La cellulose est un ? 13) Rôle des glucides ? 14) De quoi sont constitués les protides ?',
                                 'a': '10) Un ose (sucre simple). 11) Un dioside (glucose + fructose). 12) Un polyoside (sucre complexe). 13) Source d’énergie, rôle de structure (cellulose) et de '
                                      'réserve (amidon, glycogène). 14) D’acides aminés.'},
                                {'k': 'extra',
                                 'src': '',
                                 'q': 'Proposer des tests simples pour identifier dans un aliment : l’amidon, un sucre réducteur, les protéines et les lipides.',
                                 'a': 'Amidon : eau iodée (bleu-violet). Sucre réducteur : liqueur de Fehling à chaud (précipité rouge brique). Protéines : réaction du biuret (violet) ou '
                                      'xanthoprotéique (jaune). Lipides : tache translucide sur papier, ou Soudan III (rouge).'},
                                {'k': 'extra',
                                 'src': '',
                                 'q': 'Écrire la réaction de formation d’un dipeptide à partir de deux acides aminés R₁–CH(NH₂)–COOH et R₂–CH(NH₂)–COOH.',
                                 'a': 'R₁–CH(NH₂)–COOH + H₂N–CH(R₂)–COOH → R₁–CH(NH₂)–CO–NH–CH(R₂)–COOH + H₂O (liaison peptidique –CO–NH–).'},
                                {'k': 'int',
                                 'src': '',
                                 'q': 'Sur l’étiquette d’un biscuit on lit, pour 100 g : glucides 65 g (dont sucres 30 g), lipides 20 g, protéines 7 g, sel 0,5 g.\n'
                                      '1) Calculer l’énergie apportée par 100 g (4, 9 et 4 kcal/g).\n'
                                      '2) Ce biscuit est-il un aliment équilibré ? Justifier.',
                                 'a': '1) 65 × 4 + 20 × 9 + 7 × 4 = 260 + 180 + 28 = 468 kcal.\n'
                                      '2) Non : il est riche en sucres et en graisses et pauvre en protéines et en fibres ; c’est un aliment à consommer avec modération, en complément d’autres '
                                      'aliments.'}],
 'Chapitre V : Phénomènes thermiques de la contraction musculaire': [{'k': 'book',
                                                                      'src': 'p. 218',
                                                                      'q': 'Pasteur cultive de la levure dans du glucose avec de moins en moins de dioxygène : milieu riche en O₂ : traces d’éthanol, '
                                                                           'rendement 250 mg de levure/g de glucose ; milieu pauvre : peu d’éthanol, 40 ; sans O₂ : beaucoup d’éthanol, 5,7. Conclure.',
                                                                      'a': 'En présence d’O₂, la levure respire : dégradation complète du glucose, beaucoup d’énergie, forte multiplication. Sans O₂, '
                                                                           'elle fermente (glucose → éthanol + CO₂) : dégradation incomplète, peu d’énergie, faible croissance. La respiration est '
                                                                           'bien plus rentable (36-38 ATP contre 2 ATP par glucose).'},
                                                                     {'k': 'book',
                                                                      'src': 'p. 220',
                                                                      'q': 'On stimule un muscle : 1) non traité : le glycogène baisse (1,08 → 0,8 g/kg) et l’acide lactique augmente ; 2) glycolyse '
                                                                           'bloquée : la phosphocréatine baisse (15-17 → 3-4) ; 3) glycolyse et phosphocréatine bloquées : l’ATP s’annule. a) Source '
                                                                           'd’énergie dans chaque cas. b) Pourquoi l’ATP est-il la forme d’énergie directement utilisable ?',
                                                                      'a': 'a) 1) Glycolyse anaérobie (fermentation lactique) qui régénère l’ATP ; 2) phosphocréatine (PC + ADP → C + ATP) ; 3) seule '
                                                                           'la réserve d’ATP est consommée.\n'
                                                                           'b) Dans tous les cas la contraction consomme de l’ATP (son hydrolyse par la myosine fournit l’énergie) ; les autres voies '
                                                                           'ne servent qu’à le régénérer, sa quantité restant stable tant qu’elles fonctionnent.'},
                                                                     {'k': 'book',
                                                                      'src': 'p. 216',
                                                                      'q': 'a) Les fibres rapides ont un réticulum endoplasmique très développé. b) L’injection de Ca²⁺ dans l’hyaloplasme provoque la '
                                                                           'contraction. c) Le calcium radioactif est dans le réticulum des fibres relâchées, dans l’hyaloplasme des fibres '
                                                                           'contractées. Interpréter et préciser le rôle du calcium.',
                                                                      'a': 'Le réticulum stocke les ions Ca²⁺ ; lors de l’excitation il les libère dans l’hyaloplasme, ce qui déclenche la contraction '
                                                                           '; leur repompage (actif, avec ATP) provoque le relâchement. Plus le réticulum est développé, plus ces échanges sont '
                                                                           'rapides. Le Ca²⁺ démasque les sites de fixation de la myosine sur l’actine.'},
                                                                     {'k': 'book',
                                                                      'src': 'p. 218',
                                                                      'q': 'On cultive de la levure dans un liquide sucré, flacon A ouvert, flacon B fermé ; on compte les cellules. Quel flacon donne '
                                                                           'la plus forte multiplication ? Pourquoi ?',
                                                                      'a': 'Le flacon ouvert A (dioxygène disponible) : la levure respire et tire beaucoup plus d’énergie du glucose, elle se '
                                                                           'multiplie davantage. Dans B, l’O₂ s’épuise, la levure fermente et se multiplie peu.'},
                                                                     {'k': 'extra',
                                                                      'src': '',
                                                                      'q': 'Écrire les équations bilans de la respiration, de la fermentation alcoolique et de la fermentation lactique, avec le '
                                                                           'nombre d’ATP produits par molécule de glucose.',
                                                                      'a': 'Respiration : C₆H₁₂O₆ + 6O₂ → 6CO₂ + 6H₂O (36 à 38 ATP). Fermentation alcoolique : C₆H₁₂O₆ → 2C₂H₅OH + 2CO₂ (2 ATP). '
                                                                           'Fermentation lactique : C₆H₁₂O₆ → 2CH₃–CHOH–COOH (2 ATP).'},
                                                                     {'k': 'extra',
                                                                      'src': '',
                                                                      'q': 'Distinguer chaleur initiale et chaleur retardée lors de la contraction musculaire.',
                                                                      'a': 'La chaleur initiale est dégagée pendant la contraction (hydrolyse de l’ATP, réactions anaérobies rapides) ; la chaleur '
                                                                           'retardée apparaît après, pendant la récupération, et nécessite du dioxygène (respiration qui reconstitue les réserves).'},
                                                                     {'k': 'int',
                                                                      'src': '',
                                                                      'q': 'Un coureur de 100 m et un marathonien n’utilisent pas les mêmes voies de production d’ATP.\n'
                                                                           '1) Quelles voies dominent chez chacun ?\n'
                                                                           '2) Pourquoi le sprinteur est-il essoufflé après la course et pourquoi ses muscles peuvent-ils être douloureux ?',
                                                                      'a': '1) Sprint : réserves d’ATP, phosphocréatine et fermentation lactique (voies anaérobies rapides). Marathon : respiration '
                                                                           'aérobie (glucose et lipides).\n'
                                                                           '2) Il rembourse la « dette d’oxygène » (respiration qui élimine l’acide lactique et reconstitue les réserves) ; '
                                                                           'l’accumulation d’acide lactique contribue aux douleurs et à la fatigue.'}]}
