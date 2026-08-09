# Publier une version

Ce fichier est une procédure, pas de la doc joueur. Il décrit ce qu'il faut faire à la main —
et **je ne le fais pas à ta place** pour les étapes qui demandent tes identifiants.

## 1. Le jar

```bash
./gradlew build
```

Sort `build/libs/veskorius-<version>.jar`. Deux vérifications avant de l'envoyer où que ce
soit :

```bash
unzip -p build/libs/veskorius-0.1.0.jar META-INF/neoforge.mods.toml
```
→ version, `versionRange` de NeoForge, description.

```bash
unzip -l build/libs/veskorius-0.1.0.jar | grep -icE "jei|jade|construction"
```
→ ne doit remonter que nos propres classes `com/veskorius/compat/jei/`. JEI, Jade et les
Construction Wands sont déclarés en `localRuntime` : ils servent au développement et ne partent
jamais dans le jar.

Pour tester dans un vrai environnement : profil NeoForge 1.21.1 dans le launcher, le jar dans
`mods/`, rien d'autre.

## 2. CurseForge

**À faire par toi.** Publier engage ton compte, et je ne manipule ni ton mot de passe ni ton
jeton d'API.

1. https://legacy.curseforge.com/minecraft/mc-mods → *Create a Project*.
2. Catégorie : *Technology*. Licence : celle de `gradle.properties`
   (`mod_license`, actuellement `All Rights Reserved`).
3. Description du projet : copier le contenu de [`curseforge.md`](curseforge.md). La page
   accepte le Markdown ; elle est bilingue anglais / français, l'anglais d'abord.
4. *Upload file* → `build/libs/veskorius-0.1.0.jar`
   - **Release type : Alpha**
   - Game version : 1.21.1
   - Modloader : NeoForge
   - Changelog : ce que change cette version. Pour la première, la liste des cinq paliers suffit.
5. Une image de projet (logo) est demandée pour la validation.

La validation manuelle de CurseForge prend en général quelques heures.

### Ce qu'il reste à décider avant de publier

- `mod_authors` est **vide** dans `gradle.properties` — le jar ne déclare aucun auteur.
- `mod_description` est sans accents (« technologie ancienne a restaurer ») : c'est le texte
  affiché dans la liste des mods en jeu.
- Aucun `logoFile` n'est déclaré dans le `neoforge.mods.toml`.

## 3. Le wiki GitHub

Les pages sont dans [`wiki/`](wiki/). Le wiki GitHub est un dépôt git séparé — il faut donc
d'abord que le dépôt ait un remote (`git remote -v` n'en renvoie aucun aujourd'hui), et que le
wiki ait été initialisé une fois depuis l'onglet *Wiki* de GitHub.

Ensuite :

```bash
git clone https://github.com/<compte>/<depot>.wiki.git /tmp/veskorius-wiki
cp wiki/*.md /tmp/veskorius-wiki/
cd /tmp/veskorius-wiki && git add -A && git commit -m "wiki: pages joueur" && git push
```

`Home.md` est la page d'accueil, `_Sidebar.md` la navigation latérale — ces deux noms sont
imposés par GitHub. Les liens entre pages utilisent le nom du fichier sans `.md`.

Le wiki est en **anglais** : il s'adresse au même public que la page CurseForge. Le guide
joueur français reste dans `veskorius-design/guide-joueur/`.

## 4. Cloner sous Windows

Le plus long chemin du dépôt fait 148 caractères — un nom de recette composé par la datagen
vanilla. Sous la limite MAX_PATH, `git clone` n'échoue pas franchement : il réussit **en
partie**, en laissant des fichiers manquants et un `error: Filename too long` dans la sortie.
Constaté en clonant dans un dossier temporaire profond.

```bash
git config --global core.longpaths true
```
