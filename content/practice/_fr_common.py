"""Items communs FR (remédiation, activités parascolaires, projet)."""
REMED = {
"1AS": [
 ("Le présent", "Conjugue au présent : je (être) en classe ; nous (avoir) un livre ; ils (aller) à l'école ; tu (finir) ton devoir.", "je suis ; nous avons ; ils vont ; tu finis."),
 ("Articles", "Complète avec un, une, des, le, la, les : J'ai ___ stylo. ___ soleil brille. Elle achète ___ oranges.", "un ; Le ; des."),
 ("Pluriel", "Mets au pluriel : un cheval, un enfant, un bijou, un journal.", "des chevaux, des enfants, des bijoux, des journaux."),
 ("Ponctuation", "Ponctue : où habites tu je habite à rosso", "Où habites-tu ? J'habite à Rosso."),
 ("Féminin", "Mets au féminin : un élève sérieux, un ami gentil, un voisin heureux.", "une élève sérieuse, une amie gentille, une voisine heureuse."),
 ("Phrase", "Remets en ordre : école / va / à / mon / l' / frère / matin / le", "Mon frère va à l'école le matin."),
],
"2AS": [
 ("Présent et passé composé", "Conjugue : je (finir) au présent ; nous (manger) au passé composé ; elles (partir) au passé composé.", "je finis ; nous avons mangé ; elles sont parties."),
 ("Accord de l'adjectif", "Accorde : des fleurs (beau) ; une maison (blanc) ; des élèves (attentif).", "belles ; blanche ; attentifs/attentives."),
 ("Homophones", "Complète avec a / à, et / est : Il ___ un livre ___ la main. Le ciel ___ bleu ___ clair.", "a – à ; est – et."),
 ("Types de phrases", "Transforme en phrase interrogative puis négative : Tu aimes le football.", "Aimes-tu le football ? / Tu n'aimes pas le football."),
 ("Pronoms", "Remplace le groupe souligné par un pronom : Je vois mes amis. Il parle à sa mère.", "Je les vois. Il lui parle."),
 ("Imparfait", "Conjugue à l'imparfait : nous (jouer), il (faire), vous (être).", "nous jouions, il faisait, vous étiez."),
],
"3AS": [
 ("Temps du récit", "Conjugue au passé simple : il (prendre), ils (venir), elle (marcher).", "il prit, ils vinrent, elle marcha."),
 ("Imparfait / passé simple", "Complète : Il (pleuvoir) quand soudain la porte (s'ouvrir).", "pleuvait ; s'ouvrit."),
 ("Accord du participe", "Accorde : Les filles sont (arrivé). Les lettres que j'ai (écrit).", "arrivées ; écrites."),
 ("Subordonnée", "Relie avec une conjonction : Il est resté à la maison. Il était malade.", "Il est resté à la maison parce qu'il était malade."),
 ("Discours indirect", "Mets au discours indirect : Il dit : « Je viendrai demain. »", "Il dit qu'il viendra le lendemain / demain."),
 ("Connecteurs", "Complète avec d'abord, ensuite, enfin, mais : ___ j'ai révisé, ___ j'ai fait mes exercices, ___ je me suis reposé.", "D'abord ; ensuite ; enfin."),
],
}
PARA = [
 ("Affiche", "Pour la journée de l'arbre, rédige une affiche (titre, slogan, date, lieu, deux consignes).", "Titre : Plantons ensemble ! ; slogan : « Un arbre, une vie » ; date, lieu ; consignes : apporter un plant, arroser chaque semaine."),
 ("Compte rendu", "Rédige en 6 lignes le compte rendu d'une sortie scolaire.", "Date et lieu, participants, déroulement, ce que vous avez appris, appréciation."),
 ("Invitation", "Rédige une invitation à la fête de fin d'année de l'école.", "Destinataire, objet, date, heure, lieu, programme, formule de politesse, signature."),
]
PROJET = [
 ("Projet de classe", "Présente en 6 lignes le projet de ta classe : titre, objectif, étapes, rôle de chacun.", "Titre clair ; objectif ; 3 étapes (préparation, réalisation, présentation) ; répartition des tâches ; résultat attendu."),
 ("Exposé", "Prépare le plan d'un exposé sur un métier de ton choix.", "Introduction ; présentation du métier ; formation ; avantages et inconvénients ; conclusion."),
]
def common(level, B):
    B["Activités de remédiation"] = REMED[level]
    B["Activité parascolaire"] = PARA
    B["Projet"] = PROJET; B["Présentation du projet"] = PROJET
    return B
