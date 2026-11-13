"""Banque TI — 3AS."""
B = {
"Introduction à PowerPoint et création d'une présentation": [
 ("PowerPoint", "À quoi sert PowerPoint ? Cite deux situations d'utilisation.", "Logiciel de présentation assistée par ordinateur (PréAO) ; exposé en classe, présentation d'un projet."),
 ("Nouvelle présentation", "Décris les étapes pour créer une nouvelle présentation avec un thème.", "Ouvrir PowerPoint → Nouvelle présentation → onglet Création → choisir un thème."),
 ("Interface", "Cite trois zones de l'interface de PowerPoint.", "Volet des miniatures, diapositive en cours, zone de commentaires, ruban."),
],
"Ouverture, fermeture et enregistrement d'une présentation": [
 ("Extension", "Quelle est l'extension d'un fichier PowerPoint ? Comment l'enregistrer en PDF ?", ".pptx ; Fichier → Enregistrer sous → type PDF."),
 ("Ouvrir", "Comment ouvrir une présentation enregistrée sur une clé USB ?", "Fichier → Ouvrir → Parcourir → sélectionner la clé USB → choisir le fichier → Ouvrir."),
 ("Fermer sans perdre", "Que se passe-t-il si on ferme une présentation non enregistrée ?", "PowerPoint demande d'enregistrer ; si on refuse, les modifications sont perdues."),
],
"Insertion et gestion des diapositives": [
 ("Diapositive", "Comment ajouter, dupliquer et supprimer une diapositive ?", "Accueil → Nouvelle diapositive ; clic droit → Dupliquer ; sélectionner → touche Suppr."),
 ("Disposition", "Qu'est-ce qu'une disposition ? Donne deux exemples.", "L'organisation des zones d'une diapositive ; Titre seul, Titre et contenu, Deux contenus."),
 ("Ordre", "Comment changer l'ordre des diapositives ?", "Glisser-déposer les miniatures dans le volet de gauche ou utiliser la trieuse de diapositives."),
],
"Insertion de texte et mise en forme": [
 ("Zone de texte", "Comment ajouter une zone de texte et changer sa couleur ?", "Insertion → Zone de texte → dessiner la zone → taper le texte → Accueil → couleur de police."),
 ("Règles", "Cite trois règles pour un texte lisible sur une diapositive.", "Peu de texte (mots-clés), police assez grande (≥ 24), bon contraste couleur texte/fond."),
 ("Puces", "À quoi servent les puces ? Comment les ajouter ?", "À présenter les idées en liste ; Accueil → Paragraphe → Puces."),
],
"Insertion d'images et objets graphiques": [
 ("Image et taille", "Comment redimensionner une image sans la déformer dans PowerPoint ?", "Tirer une poignée d'angle (en gardant les proportions)."),
 ("Insertion", "Cite trois objets qu'on peut insérer dans une diapositive.", "Image, forme, SmartArt, graphique, tableau, vidéo."),
 ("SmartArt", "Qu'est-ce qu'un SmartArt ? Donne un usage.", "Un diagramme prêt à l'emploi ; représenter les étapes d'un processus ou une hiérarchie."),
],
"Déplacement et transition des diapositives": [
 ("Appliquer une transition", "Comment appliquer la même transition à toutes les diapositives ?", "Onglet Transitions → choisir l'effet → Appliquer partout."),
 ("Transition / animation", "Différence entre transition et animation ?", "Transition : effet entre deux diapositives ; animation : effet sur un objet à l'intérieur d'une diapositive."),
 ("Conseil", "Pourquoi éviter trop d'animations ?", "Elles distraient le public et font perdre du temps."),
],
"Exposition et finalisation de la présentation": [
 ("Diaporama", "Comment lancer le diaporama depuis le début ? Depuis la diapositive actuelle ?", "Touche F5 ; Maj + F5."),
 ("Préparer l'exposé", "Cite trois conseils pour présenter son exposé devant la classe.", "Répéter avant, regarder le public, ne pas lire les diapositives, respecter le temps."),
],
"Naviguer sur Internet en toute sécurité et avec éthique": [
 ("Données personnelles", "Cite trois données personnelles à ne jamais publier.", "Adresse, numéro de téléphone, mots de passe, photos privées, numéro de carte."),
 ("Wi-Fi public", "Quels risques en utilisant un Wi-Fi public ?", "Interception des données ; éviter de se connecter à des comptes sensibles."),
 ("Éthique", "Un ami publie une photo humiliante d'un camarade. Que fais-tu ?", "Je lui demande de la retirer, je ne la partage pas et je préviens un adulte si besoin."),
],
"Introduction à la notion d'algorithme": [
 ("Algorithme", "Qu'est-ce qu'un algorithme ? Donne un exemple de la vie courante.", "Suite finie et ordonnée d'instructions pour résoudre un problème ; une recette de cuisine."),
 ("Structure", "Cite les trois parties d'un algorithme.", "Déclaration (variables), début (instructions), fin."),
 ("Ordre", "Écris l'algorithme pour préparer le thé en 4 étapes.", "Début ; faire bouillir l'eau ; mettre le thé et le sucre ; laisser infuser puis servir ; Fin."),
],
"Introduction à la notion de variables": [
 ("Variable", "Qu'est-ce qu'une variable ? Cite deux types.", "Une case mémoire nommée qui contient une valeur ; entier, réel, chaîne de caractères, booléen."),
 ("Affectation", "Après les instructions a ← 5 ; b ← a + 3 ; a ← b × 2, que valent a et b ?", "b = 8 ; a = 16."),
 ("Échange", "Écris les instructions pour échanger les valeurs de x et y.", "temp ← x ; x ← y ; y ← temp."),
],
"Introduction à la notion de conditions": [
 ("Condition", "Écris une structure Si…Alors…Sinon qui affiche « Admis » si note ≥ 10.", "Si note ≥ 10 Alors Afficher(« Admis ») Sinon Afficher(« Ajourné ») FinSi"),
 ("Pair / impair", "Écris la condition pour savoir si un nombre n est pair.", "Si n mod 2 = 0 Alors pair Sinon impair."),
],
"Écriture d'un algorithme simple": [
 ("Surface", "Écris un algorithme qui lit la longueur et la largeur d'un rectangle et affiche sa surface.", "Variables L, l, S : réels ; Début Lire(L, l) ; S ← L × l ; Afficher(S) ; Fin."),
 ("Moyenne", "Écris un algorithme qui calcule la moyenne de trois notes.", "Lire(a, b, c) ; m ← (a + b + c) / 3 ; Afficher(m)."),
],
"Test d'un algorithme simple": [
 ("Tracer", "Algorithme : x ← 4 ; y ← x × x ; Si y > 10 Alors Afficher(\"grand\") Sinon Afficher(\"petit\"). Qu'affiche-t-il ?", "y = 16 > 10 → « grand »."),
 ("Erreur", "Trouve l'erreur : Lire(a) ; b ← a / 0 ; Afficher(b).", "Division par zéro : impossible."),
],
"Introduction aux boucles": [
 ("Boucle Pour", "Écris une boucle qui affiche les nombres de 1 à 10.", "Pour i de 1 à 10 Faire Afficher(i) FinPour"),
 ("Tant que", "Différence entre boucle Pour et boucle Tant que ?", "Pour : nombre de répétitions connu ; Tant que : répète tant qu'une condition est vraie."),
],
"Utilisation des boucles dans un algorithme": [
 ("Somme", "Écris un algorithme qui calcule la somme 1 + 2 + … + 100.", "s ← 0 ; Pour i de 1 à 100 Faire s ← s + i FinPour ; Afficher(s) → 5050."),
 ("Table", "Écris un algorithme qui affiche la table de multiplication de 7.", "Pour i de 1 à 10 Faire Afficher(7 × i) FinPour."),
],
"Reconnaissance de la tâche faite par un algorithme": [
 ("Que fait-il ?", "p ← 1 ; Pour i de 1 à 5 Faire p ← p × i FinPour ; Afficher(p). Que fait cet algorithme et qu'affiche-t-il ?", "Il calcule 5! (factorielle) ; affiche 120."),
 ("Que fait-il ? (2)", "Lire(a, b) ; Si a > b Alors Afficher(a) Sinon Afficher(b). Que fait cet algorithme ?", "Il affiche le plus grand des deux nombres."),
],
"Création et test d'un algorithme final": [
 ("Projet", "Écris un algorithme qui lit 5 notes, calcule la moyenne et affiche « Admis » ou « Ajourné ».", "s ← 0 ; Pour i de 1 à 5 Faire Lire(n) ; s ← s + n FinPour ; m ← s / 5 ; Si m ≥ 10 Alors Afficher(« Admis ») Sinon Afficher(« Ajourné »)."),
],
"Introduction aux réseaux informatiques": [
 ("Réseau", "Cite deux avantages d'un réseau informatique.", "Partage de fichiers et d'imprimantes, accès à Internet partagé, communication."),
],
"Composantes d'un réseau simple": [
 ("Composants", "Cite quatre composants d'un réseau local.", "Ordinateurs, câbles réseau, switch, routeur/box, carte réseau, point d'accès Wi-Fi."),
 ("Rôle", "Rôle du routeur et du switch ?", "Routeur : relie le réseau local à Internet ; switch : relie les appareils du réseau local entre eux."),
],
"Fonctionnement et types de réseaux": [
 ("Types", "Classe du plus petit au plus grand : WAN, LAN, PAN, MAN.", "PAN < LAN < MAN < WAN."),
 ("Adresse IP", "Qu'est-ce qu'une adresse IP ?", "Un numéro qui identifie un appareil sur un réseau."),
],
"Utilisation d'un réseau local simple": [
 ("Partage", "Comment partager un dossier sur un réseau local ?", "Clic droit sur le dossier → Propriétés → Partage → choisir les utilisateurs et les droits."),
],
"Introduction à l'e-mail": [
 ("Webmail", "Qu'est-ce qu'un webmail ? Donne deux exemples.", "Une messagerie utilisée via un navigateur ; Gmail, Outlook.com, Yahoo Mail."),
],
"Rédaction et envoi d'un e-mail": [
 ("Rédaction", "Cite les parties d'un e-mail formel.", "Objet clair, formule d'appel, message, formule de politesse, signature."),
 ("Cci", "À quoi sert le champ Cci ?", "Envoyer une copie cachée : les autres destinataires ne voient pas cette adresse."),
],
"Ajouter une pièce jointe": [
 ("Pièce jointe", "Comment joindre un fichier à un e-mail ? Quelle limite faut-il respecter ?", "Cliquer sur le trombone → choisir le fichier → Ouvrir ; respecter la taille maximale (souvent 25 Mo) ou utiliser un lien de partage."),
],
"Réception et gestion des e-mails": [
 ("Gestion", "Cite trois actions possibles sur un e-mail reçu.", "Répondre, transférer, archiver, supprimer, marquer comme spam."),
 ("Répondre / transférer", "Différence entre « Répondre » et « Transférer » ?", "Répondre : à l'expéditeur ; transférer : envoyer le message reçu à une autre personne."),
],
"Notion de cahier des charges": [
 ("Cahier des charges", "Qu'est-ce qu'un cahier des charges ?", "Document qui décrit les besoins, les fonctions et les contraintes d'un objet à concevoir."),
 ("Contenu", "Cite quatre éléments d'un cahier des charges.", "Le besoin, les fonctions de service, les contraintes (coût, délai, sécurité, environnement), les critères d'appréciation."),
 ("Bête à cornes", "Applique l'outil « bête à cornes » à un ventilateur.", "À qui rend-il service ? À l'utilisateur ; sur quoi agit-il ? L'air ; dans quel but ? Rafraîchir la pièce."),
 ("Contraintes", "Cite trois contraintes pour concevoir une lampe solaire d'étude.", "Coût faible, autonomie de plusieurs heures, résistance à la chaleur et à la poussière, sécurité."),
],
"Environnement et fonctionnement de l'objet technique": [
 ("Milieu extérieur", "Cite les éléments du milieu extérieur d'un vélo.", "L'utilisateur, la route, l'air (vent), la pluie, les autres usagers."),
 ("Fonctionnement", "Décris le fonctionnement d'un vélo de la pédale à la roue.", "Les pédales font tourner le plateau, la chaîne transmet le mouvement au pignon qui entraîne la roue arrière."),
 ("Interactions", "Comment la poussière du Sahara influence-t-elle la conception d'un ordinateur ?", "Il faut des filtres, une bonne ventilation et une protection contre la poussière."),
 ("Fonction principale", "Donne la fonction principale d'un climatiseur et une fonction contrainte.", "Principale : refroidir l'air de la pièce ; contrainte : consommer peu d'énergie / faire peu de bruit."),
],
"Estimation du coût et limitations matérielles": [
 ("Coût", "Une lampe nécessite : panneau 2 500 MRU, batterie 1 800 MRU, LED 300 MRU, boîtier 400 MRU. Calcule le coût des matériaux.", "2 500 + 1 800 + 300 + 400 = 5 000 MRU."),
 ("Prix de vente", "Si la main-d'œuvre coûte 1 000 MRU et la marge 20 % du coût total, quel est le prix de vente ?", "Coût total = 6 000 ; marge = 1 200 ; prix = 7 200 MRU."),
 ("Limitations", "Donne deux limitations matérielles possibles lors de la fabrication d'un objet.", "Matériaux indisponibles localement, outils inadaptés, budget limité."),
 ("Réduction", "Propose deux moyens de réduire le coût d'un objet.", "Utiliser des matériaux locaux, simplifier la conception, produire en série."),
],
"Caractéristiques économiques des matériaux": [
 ("Critères", "Cite trois critères économiques dans le choix d'un matériau.", "Prix, disponibilité, facilité de mise en œuvre, possibilité de recyclage."),
 ("Comparaison", "Pourquoi utilise-t-on souvent le plastique plutôt que l'aluminium pour les seaux ?", "Le plastique est moins cher, léger et facile à mouler."),
 ("Recyclage", "Pourquoi le recyclage des matériaux réduit-il les coûts ?", "Il économise les matières premières et l'énergie de fabrication."),
 ("Local", "Cite deux matériaux disponibles localement en Mauritanie utilisés dans la construction.", "Sable, pierre, argile (banco), coquillages, bois local."),
],
"Étude de cas : élaboration d'un cahier des charges": [
 ("Cas", "Rédige un cahier des charges simplifié pour un porte-cartable.", "Besoin : ranger les cartables ; fonctions : supporter 10 cartables ; contraintes : coût < 2 000 MRU, solide, sans angles coupants."),
 ("Critères", "Pour un chargeur solaire de téléphone, donne un critère et son niveau.", "Puissance : 5 W ; temps de charge : < 4 h ; poids : < 500 g."),
],
"Cycle de vie d'un système technique": [
 ("Étapes", "Cite les étapes du cycle de vie d'un objet technique.", "Extraction des matières premières, fabrication, distribution, utilisation, fin de vie (recyclage ou élimination)."),
 ("Impact", "À quelle étape un téléphone pollue-t-il le plus ? Justifie.", "Souvent la fabrication (extraction des métaux, énergie) et la fin de vie s'il n'est pas recyclé."),
 ("Fin de vie", "Que faire d'un vieux téléphone ?", "Le réparer, le donner, ou le déposer dans un point de collecte pour recyclage."),
],
"Conversion des énergies": [
 ("Conversion", "Donne la conversion d'énergie dans : un panneau solaire, un moteur électrique, une lampe.", "Lumineuse → électrique ; électrique → mécanique ; électrique → lumineuse (+ thermique)."),
 ("Rendement", "Un moteur reçoit 100 J et fournit 75 J de travail utile. Calcule son rendement.", "η = 75 / 100 = 75 %."),
],
"Nature de l'énergie de fonctionnement": [
 ("Nature", "Quelle énergie fait fonctionner : un réveil à piles, une moto, une éolienne ?", "Chimique (piles) → électrique ; chimique (carburant) ; éolienne (cinétique du vent)."),
],
"Fonction de synchronisation et fonction de signalisation": [
 ("Signalisation", "Donne deux exemples de fonction de signalisation.", "Le voyant d'un chargeur, le clignotant d'une voiture, la sonnerie d'un réveil."),
 ("Synchronisation", "Qu'est-ce que la synchronisation ? Donne un exemple.", "Coordonner des actions dans le temps ; les feux tricolores d'un carrefour."),
],
"Fonction de transmission de mouvements": [
 ("Transmission", "Cite trois systèmes de transmission de mouvement.", "Engrenages, poulies et courroie, pignons et chaîne."),
 ("Rapport", "Un pignon moteur de 10 dents entraîne une roue de 30 dents. La roue tourne-t-elle plus vite ou moins vite ?", "Moins vite : 3 fois plus lentement (rapport 10/30 = 1/3)."),
],
"Fonction de commande à distance": [
 ("Télécommande", "Comment fonctionne une télécommande de téléviseur ?", "Elle envoie un signal infrarouge codé reçu par le capteur du téléviseur."),
 ("Exemples", "Cite trois objets commandés à distance.", "Téléviseur, portail automatique, drone, climatiseur."),
],
"Publicité : définition, constituants, fonctions et méthodes": [
 ("Publicité", "Définis la publicité et cite deux de ses fonctions.", "Message destiné à faire connaître et vendre un produit ; informer, persuader, fidéliser."),
 ("Constituants", "Cite les constituants d'une affiche publicitaire.", "Image, slogan, nom du produit/logo, texte informatif, coordonnées."),
 ("Critique", "Pourquoi faut-il être critique face à la publicité ?", "Elle peut exagérer les qualités et pousser à des achats inutiles."),
],
"Technologies d'un secteur et contraintes environnementales": [
 ("Pêche", "Cite une technologie utilisée dans la pêche et une contrainte environnementale.", "GPS / sonar, chambres froides ; surpêche, pollution marine, respect des repos biologiques."),
 ("Mines", "Quelles contraintes environnementales pour une mine ?", "Poussière, consommation d'eau, gestion des déchets, réhabilitation du site."),
],
"Énergies renouvelables : types et stockage": [
 ("Stockage", "Pourquoi faut-il stocker l'énergie solaire ? Comment ?", "Car le Soleil n'est pas disponible la nuit ; avec des batteries."),
 ("Types", "Cite quatre énergies renouvelables.", "Solaire, éolienne, hydraulique, biomasse, géothermique."),
],
}
