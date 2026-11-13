"""Banque TI — 2AS."""
B = {
"Introduction aux images numériques": [
 ("Pixel", "Qu'est-ce qu'un pixel ? Que signifie une image de 1920 × 1080 ?", "Le plus petit point d'une image numérique ; l'image a 1920 pixels en largeur et 1080 en hauteur."),
 ("Formats", "Cite trois formats d'images et une caractéristique de chacun.", "JPEG : photos, compressé ; PNG : transparence, sans perte ; GIF : animations simples ; BMP : non compressé, lourd."),
 ("Calcul", "Combien de pixels contient une image de 800 × 600 ?", "800 × 600 = 480 000 pixels."),
 ("Matricielle / vectorielle", "Différence entre image matricielle et image vectorielle ?", "Matricielle : faite de pixels, perd en qualité si on l'agrandit ; vectorielle : faite de formes mathématiques, s'agrandit sans perte."),
],
"Création et enregistrement d'une image": [
 ("Paint", "Décris les étapes pour dessiner un rectangle rouge dans Paint et l'enregistrer en PNG.", "Ouvrir Paint → choisir Formes → Rectangle → couleur rouge → dessiner → Fichier → Enregistrer sous → PNG → nommer → Enregistrer."),
 ("Enregistrer sous", "Pourquoi utiliser « Enregistrer sous » plutôt que « Enregistrer » ?", "Pour choisir un nouveau nom, un emplacement ou un format sans écraser l'original."),
 ("Outils", "Cite quatre outils de Paint.", "Crayon, pinceau, gomme, pot de peinture, texte, formes, sélection."),
 ("Taille du fichier", "Une même image est enregistrée en BMP (2,4 Mo) et en JPEG (250 Ko). Explique la différence.", "Le JPEG compresse l'image donc le fichier est plus léger ; le BMP n'est pas compressé."),
],
"Téléchargement et gestion d'images": [
 ("Renommer", "Pourquoi renommer les images téléchargées (ex. IMG_2031.jpg) ?", "Pour les retrouver facilement : un nom clair comme « chinguetti_mosquee.jpg » décrit le contenu."),
 ("Télécharger", "Comment enregistrer une image trouvée sur Internet ?", "Clic droit sur l'image → Enregistrer l'image sous → choisir le dossier → Enregistrer (en respectant les droits d'auteur)."),
 ("Organiser", "Propose une méthode pour organiser 200 photos de voyage.", "Créer des dossiers par date ou par lieu, renommer les photos clairement, supprimer les doublons."),
 ("Droits", "Peut-on utiliser n'importe quelle image d'Internet dans un exposé ? Explique.", "Non, il faut respecter les droits d'auteur : utiliser des images libres de droits et citer la source."),
],
"Traitement simple d'une image : recadrage et redimensionnement": [
 ("Recadrer / redimensionner", "Quelle différence entre recadrer et redimensionner ?", "Recadrer : couper une partie de l'image ; redimensionner : changer la taille de toute l'image."),
 ("Proportions", "Une image de 1200 × 800 est redimensionnée à 50 %. Quelles sont ses nouvelles dimensions ?", "600 × 400."),
 ("Conserver les proportions", "Pourquoi cocher « Conserver les proportions » ?", "Pour éviter que l'image soit déformée (étirée ou écrasée)."),
],
"Retouches de base : luminosité, contraste et filtres": [
 ("Contraste", "Qu'est-ce que le contraste d'une image ? Que se passe-t-il si on l'augmente trop ?", "La différence entre zones claires et sombres ; trop de contraste fait perdre les détails (blancs brûlés, noirs bouchés)."),
 ("Retouches", "Une photo est trop sombre. Quel réglage utiliser ?", "Augmenter la luminosité (et éventuellement le contraste)."),
 ("Filtre", "Qu'est-ce qu'un filtre ? Donne deux exemples.", "Effet appliqué à toute l'image ; noir et blanc, sépia, flou, netteté."),
],
"Projet final : création et gestion d'une image personnalisée": [
 ("Carte de vœux", "Tu dois créer une carte de vœux numérique de 1000 × 700 pixels. Quelles étapes et quels réglages choisis-tu ?", "Nouvelle image 1000 × 700 → fond coloré → insérer une image libre redimensionnée → ajouter un texte → retoucher → enregistrer en JPEG ou PNG."),
 ("Projet", "Décris les étapes pour créer une affiche pour la journée de l'environnement.", "Choisir une image libre → recadrer → ajouter un titre → ajuster luminosité → enregistrer en PNG → ranger dans le dossier du projet."),
],
"Naviguer sur Internet de manière éducative et sécurisée": [
 ("Sécurité", "Cite quatre règles pour naviguer en sécurité.", "Ne pas donner ses informations personnelles, vérifier le cadenas https, ne pas cliquer sur les liens suspects, utiliser un antivirus."),
 ("Mot de passe", "Parmi ces mots de passe, lequel est le plus sûr ? 123456 – ahmed – Ah!2026mr#", "Ah!2026mr# : long, avec majuscules, chiffres et symboles."),
],
"Respect et éthique sur Internet": [
 ("Cyberharcèlement", "Qu'est-ce que le cyberharcèlement ? Que faire si on en est victime ?", "Harcèlement répété en ligne ; ne pas répondre, garder les preuves, bloquer, en parler à un adulte."),
],
"Système technique": [
 ("Système", "Qu'est-ce qu'un système technique ? Donne un exemple et ses éléments.", "Ensemble d'éléments qui fonctionnent ensemble pour une fonction ; vélo : cadre, roues, pédalier, chaîne, freins."),
 ("Entrées / sorties", "Pour un ventilateur, donne l'énergie d'entrée et l'effet en sortie.", "Entrée : énergie électrique ; sortie : mouvement de l'air (énergie mécanique)."),
],
"Caractéristiques physiques des matériaux": [
 ("Propriétés", "Définis : conductivité électrique, dureté, élasticité.", "Conductivité : laisser passer le courant ; dureté : résister aux rayures ; élasticité : reprendre sa forme après déformation."),
 ("Choix", "Pourquoi les fils électriques sont-ils en cuivre recouvert de plastique ?", "Le cuivre conduit bien l'électricité ; le plastique est isolant et protège."),
],
"Fonction protection et réception": [
 ("Protection", "Donne deux exemples de fonction de protection dans des objets techniques.", "Le fusible protège le circuit ; la coque protège le téléphone ; le disjoncteur protège l'installation."),
],
"Découverte d'Excel et gestion des fichiers": [
 ("Excel", "Qu'est-ce qu'un tableur ? Cite un exemple.", "Logiciel qui organise et calcule des données dans des tableaux ; Microsoft Excel, LibreOffice Calc."),
 ("Classeur", "Différence entre classeur et feuille ?", "Le classeur est le fichier ; il contient une ou plusieurs feuilles de calcul."),
],
"Feuilles et cellules : organisation du tableur": [
 ("Cellule", "Qu'est-ce que la cellule C4 ?", "La cellule à l'intersection de la colonne C et de la ligne 4."),
 ("Plage", "Que désigne la plage A1:B3 ? Combien de cellules ?", "Les cellules de A1 à B3 ; 2 colonnes × 3 lignes = 6 cellules."),
],
"Saisie et mise en forme de données": [
 ("Saisie", "Comment saisir et valider une donnée dans une cellule ?", "Cliquer sur la cellule, taper la donnée, appuyer sur Entrée."),
 ("Format", "Comment afficher un nombre avec deux décimales ?", "Sélectionner → Accueil → Nombre → augmenter les décimales (ou Format de cellule → Nombre, 2 décimales)."),
],
"Ajout de formules de base": [
 ("Formules", "A1 = 12, A2 = 8. Écris les formules de la somme, de la différence et du produit.", "=A1+A2 (20) ; =A1-A2 (4) ; =A1*A2 (96)."),
 ("Signe égal", "Par quel signe commence toute formule ?", "Le signe = ."),
 ("Moyenne", "Notes en B2, B3, B4 : 12, 15, 9. Écris la formule de la moyenne et donne le résultat.", "=(B2+B3+B4)/3 ou =MOYENNE(B2:B4) ; résultat 12."),
],
"Utilisation des fonctions courantes": [
 ("Fonctions", "Donne la fonction Excel pour : la somme de C1 à C10, le maximum, le minimum.", "=SOMME(C1:C10) ; =MAX(C1:C10) ; =MIN(C1:C10)."),
 ("SI", "Écris une formule qui affiche « Admis » si la moyenne en D2 est ≥ 10, sinon « Ajourné ».", "=SI(D2>=10;\"Admis\";\"Ajourné\")"),
],
"Mise en page et formatage avancé": [
 ("Bordures", "Comment ajouter des bordures à un tableau dans Excel ?", "Sélectionner le tableau → Accueil → Bordures → Toutes les bordures."),
 ("Mise en forme conditionnelle", "À quoi sert la mise en forme conditionnelle ? Donne un exemple.", "À colorer automatiquement les cellules selon une condition ; ex. notes < 10 en rouge."),
],
"Représentation graphique de données": [
 ("Graphiques", "Quel graphique choisir pour : l'évolution de la température sur une semaine ; la répartition des élèves par sexe ?", "Courbe (graphique en ligne) ; secteurs (camembert)."),
 ("Créer", "Décris les étapes pour créer un histogramme.", "Sélectionner les données → Insertion → Graphiques → Histogramme → ajouter titre et légende."),
],
"Préparation à l'impression": [
 ("Impression Excel", "Comment imprimer une feuille Excel sur une seule page ?", "Mise en page → Ajuster → 1 page en largeur et en hauteur, puis aperçu et impression."),
],
"Projet final : application des connaissances": [
 ("Projet", "Crée le plan d'un tableau de suivi des dépenses de la classe (colonnes, formules).", "Colonnes : Date, Objet, Montant ; total =SOMME(C2:C20) ; graphique des dépenses."),
],
"L'entreprise et les secteurs d'activités économiques": [
 ("Secteurs", "Classe dans les secteurs primaire, secondaire, tertiaire : pêche, banque, usine de poisson, élevage, école, mine de fer.", "Primaire : pêche, élevage, mine de fer (extraction) ; Secondaire : usine de poisson ; Tertiaire : banque, école."),
 ("Entreprise", "Qu'est-ce qu'une entreprise ? Cite deux types selon la taille.", "Organisation qui produit des biens ou des services ; petite (PME), moyenne, grande entreprise."),
],
"Introduction aux moteurs de recherche": [
 ("Recherche avancée", "Que fait la recherche entre guillemets « énergie solaire Mauritanie » ?", "Elle cherche l'expression exacte dans cet ordre."),
 ("Mots-clés", "Transforme la question en mots-clés : « Quelle est la date de l'indépendance de la Mauritanie ? »", "date indépendance Mauritanie."),
],
"Fiabilité des sources et dangers du web": [
 ("Fiabilité", "Cite trois indices qu'une information sur Internet est fiable.", "Auteur connu, source officielle, date récente, information confirmée par d'autres sources."),
 ("Fausses nouvelles", "Que faire avant de partager une information choquante sur WhatsApp ?", "Vérifier la source et chercher si des médias sérieux la confirment ; sinon ne pas la partager."),
],
"Téléchargement responsable et légal": [
 ("Légal", "Qu'est-ce qu'un téléchargement illégal ? Donne un exemple.", "Télécharger une œuvre protégée sans autorisation ; un film ou un logiciel payant piraté."),
],
"Sécurité et gestion des fichiers téléchargés": [
 ("Virus", "Comment un fichier téléchargé peut-il infecter l'ordinateur ? Comment se protéger ?", "Il peut contenir un virus ; utiliser un antivirus à jour, télécharger sur des sites officiels, ne pas ouvrir les fichiers .exe inconnus."),
],
"Introduction à la messagerie électronique (théorie)": [
 ("Adresse", "Décompose l'adresse ahmed.ba@gmail.com.", "ahmed.ba : identifiant ; @ : « at » (chez) ; gmail.com : nom de domaine du fournisseur."),
 ("Avantages", "Cite trois avantages du courrier électronique.", "Rapide, gratuit, envoi de pièces jointes, accessible partout."),
],
"Création et gestion d'une boîte électronique": [
 ("Création", "Quelles informations faut-il pour créer une boîte e-mail ?", "Nom, prénom, date de naissance, identifiant choisi, mot de passe solide, numéro de téléphone de récupération."),
 ("Dossiers", "Cite quatre dossiers d'une boîte e-mail.", "Boîte de réception, Envoyés, Brouillons, Spam (courrier indésirable), Corbeille."),
],
"Envoi et réception d'un courrier électronique": [
 ("Champs", "Rôle des champs : À, Cc, Objet.", "À : destinataire principal ; Cc : copie à d'autres personnes ; Objet : sujet du message."),
 ("Message", "Écris un court e-mail à ton professeur pour lui envoyer ton devoir.", "Objet : Devoir de TI – Classe 2AS ; Bonjour Monsieur, veuillez trouver ci-joint mon devoir. Cordialement, [Nom]."),
],
"Sécurité et bonnes pratiques en messagerie électronique": [
 ("Hameçonnage", "Tu reçois un e-mail qui te demande ton mot de passe pour « gagner un prix ». Que fais-tu ?", "C'est de l'hameçonnage (phishing) : je ne réponds pas, je ne clique pas sur le lien, je le supprime ou le signale."),
],
"Les critères et les leviers du développement durable": [
 ("Piliers", "Cite les trois piliers du développement durable.", "Économique, social, environnemental."),
 ("Leviers", "Propose deux actions de développement durable dans ton école.", "Économiser l'eau et l'électricité, trier les déchets, planter des arbres."),
],
"Les causes des changements climatiques": [
 ("Causes", "Cite trois causes humaines des changements climatiques.", "Combustion du pétrole et du charbon, déforestation, transport, élevage intensif, industrie."),
 ("Effet de serre", "Qu'est-ce que l'effet de serre ?", "Rétention d'une partie de la chaleur du Soleil par certains gaz de l'atmosphère (CO₂…) ; son renforcement réchauffe la Terre."),
],
"Les conséquences des changements climatiques": [
 ("Conséquences", "Cite trois conséquences des changements climatiques en Mauritanie.", "Sécheresses, avancée du désert, hausse du niveau de la mer menaçant Nouakchott, perte de pâturages."),
],
"Les types d'énergies renouvelables et leurs caractéristiques": [
 ("Renouvelables", "Cite trois énergies renouvelables et un avantage commun.", "Solaire, éolienne, hydraulique ; inépuisables et peu polluantes."),
 ("Mauritanie", "Pourquoi l'énergie solaire est-elle adaptée à la Mauritanie ?", "Fort ensoleillement toute l'année et grands espaces disponibles."),
],
}
