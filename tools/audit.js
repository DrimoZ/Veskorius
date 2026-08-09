// Audit de cohérence REGISTRE ↔ RESSOURCES ↔ DOSSIER.
//
// Ce fichier existe parce que la même famille de panne s'est produite quatre fois, et
// qu'aucune n'était visible en relecture : un objet enregistré sans modèle s'affiche en
// cube violet, un bloc sans traduction montre sa clé, une entrée absente de l'index de
// registres n'existe pour personne. Rien ne plante. Le mod se charge, les tests passent,
// et le défaut se découvre en jeu — ou pas du tout.
//
// Les GameTest ne peuvent pas couvrir ça : ils tournent dans le jar et ne voient ni
// src/generated, ni veskorius-design. D'où un audit hors du jeu, branché sur `gradlew
// audit`, qui échoue au lieu de prévenir.
//
//   node tools/audit.js
//
// Sortie : une ligne par problème, code de sortie 1 s'il y en a. Aucun bruit sinon.

const fs = require('fs');
const path = require('path');

const ASSETS = 'src/generated/resources/assets/veskorius/';
const DATA = 'src/generated/resources/data/veskorius/';
const problems = [];

const read = p => fs.readFileSync(p, 'utf8');
const exists = p => fs.existsSync(p);
const problem = m => problems.push(m);

// --- Ce que le code enregistre -------------------------------------------
const blocksSrc = read('src/main/java/com/veskorius/block/ModBlocks.java');
const itemsSrc = read('src/main/java/com/veskorius/item/ModItems.java');

const blocks = [...blocksSrc.matchAll(/register(?:SimpleBlock|Block)\("([a-z0-9_]+)"/g)].map(m => m[1]);
const plainItems = [...itemsSrc.matchAll(/(?:register(?:SimpleItem|Item)|armor)\("([a-z0-9_]+)"/g)].map(m => m[1]);
const blockItems = [...itemsSrc.matchAll(/registerSimpleBlockItem\(ModBlocks\.([A-Z0-9_]+)\)/g)]
  .map(m => m[1].toLowerCase());

// Les blocs déclarés sans table de butin : le provider ne les réclamera pas, et c'est
// voulu (indestructibles, ou détruits par la génération).
const noLoot = new Set();
for (const m of blocksSrc.matchAll(/register(?:SimpleBlock|Block)\("([a-z0-9_]+)"[\s\S]{0,900}?\)\);/g)) {
  if (m[0].includes('noLootTable()')) noLoot.add(m[1]);
}

// --- 1. Ressources générées ----------------------------------------------
for (const b of blocks) {
  if (!exists(ASSETS + 'blockstates/' + b + '.json')) {
    problem('bloc sans blockstate : ' + b + ' (il s\'affichera en cube violet)');
  }
  if (!noLoot.has(b) && !exists(DATA + 'loot_table/blocks/' + b + '.json')) {
    problem('bloc sans table de butin : ' + b + ' (il disparaîtra au minage)');
  }
}
for (const i of plainItems.concat(blockItems)) {
  if (!exists(ASSETS + 'models/item/' + i + '.json')) {
    problem('objet sans modèle : ' + i + ' (il s\'affichera en cube violet)');
  }
}

// --- 1 bis. Outil correct exigé, mais aucun outil déclaré ----------------
//
// `requiresCorrectToolForDrops()` sans appartenance à un tag `mineable/*` veut dire
// qu'AUCUN outil n'est jamais correct : le bloc ne se récupère JAMAIS, quoi qu'on tienne
// en main. C'est arrivé à trois blocs — dont un bloc de CONSTRUCTION qu'on ne récupérait
// pas du mur qu'on venait de bâtir, et une machine T3 qui s'évaporait quand on la
// reprenait. Rien ne plante ; le joueur croit à une maladresse et recommence.
const needsTool = new Set();
for (const m of blocksSrc.matchAll(/register(?:SimpleBlock|Block)\("([a-z0-9_]+)"[\s\S]{0,900}?\)\);/g)) {
  if (m[0].includes('requiresCorrectToolForDrops()')) needsTool.add(m[1]);
}
const mineable = new Set();
for (const tool of ['pickaxe', 'axe', 'shovel', 'hoe']) {
  const f = 'src/generated/resources/data/minecraft/tags/block/mineable/' + tool + '.json';
  if (exists(f)) for (const v of JSON.parse(read(f)).values) mineable.add(String(v));
}
for (const b of needsTool) {
  if (!mineable.has('veskorius:' + b)) {
    problem('exige le bon outil sans figurer dans aucun tag mineable/* : ' + b
      + ' (il disparaîtra au minage, quel que soit l\'outil)');
  }
}

// --- 2. Langues, à parité stricte ----------------------------------------
// (Le provider français refuse déjà de générer s'il manque une clé — on vérifie ici la
// SORTIE, au cas où quelqu'un contournerait le provider.)
const en = JSON.parse(read(ASSETS + 'lang/en_us.json'));
const fr = JSON.parse(read(ASSETS + 'lang/fr_fr.json'));
for (const k of Object.keys(en)) {
  if (!fr[k]) problem('clé sans traduction française : ' + k);
}
for (const b of blocks) {
  if (!en['block.veskorius.' + b]) problem('bloc sans nom anglais : ' + b);
}
for (const i of plainItems) {
  if (!en['item.veskorius.' + i]) problem('objet sans nom anglais : ' + i);
}

// --- 3. L'index de registres du dossier ----------------------------------
// 13-Registry-Index.md a pour seule raison d'être de lister TOUT nom de registre. Il
// dérive dès qu'on ajoute quelque chose sans y penser — et un index incomplet est pire
// qu'absent, puisqu'on le consulte pour vérifier qu'un nom est libre.
const index = read('veskorius-design/13-Registry-Index.md');
for (const n of blocks.concat(plainItems)) {
  // Les familles sont parfois listées en abrégé (`_pickaxe`, `_boots`) : on accepte le
  // suffixe seul, sinon l'audit crie sur une notation volontaire.
  const short = '_' + n.split('_').slice(-1)[0];
  if (!index.includes('`' + n + '`') && !index.includes('`' + short + '`')
      && !index.includes('/ `' + short)) {
    problem('absent de 13-Registry-Index.md : ' + n);
  }
}

// --- 4. Les chiffres de l'inventaire -------------------------------------
// 18-Etat-des-lieux.md annonce des totaux. Faux, ils envoient le travail suivant dans la
// mauvaise direction — ça s'est produit, et deux fonctionnalités ont été « recodées »
// alors qu'elles existaient.
const walk = (d, a = []) => {
  for (const e of fs.readdirSync(d, { withFileTypes: true })) {
    const p = path.join(d, e.name);
    e.isDirectory() ? walk(p, a) : a.push(p);
  }
  return a;
};
const javaFiles = walk('src/main/java').filter(f => f.endsWith('.java'));
const state = read('veskorius-design/18-Etat-des-lieux.md');
const claim = (label, re, actual, tolerance = 0) => {
  const m = state.match(re);
  if (!m) { problem('18-Etat-des-lieux.md : chiffre « ' + label + ' » introuvable'); return; }
  const said = parseInt(m[1].replace(/\s/g, ''), 10);
  if (Math.abs(said - actual) > tolerance) {
    problem('18-Etat-des-lieux.md annonce ' + said + ' ' + label + ', le code en a ' + actual);
  }
};
claim('fichiers Java', /\*\*(\d+) fichiers Java/, javaFiles.length);
claim('blocs', /\*\*(\d+) blocs, \d+ items\*\*/, blocks.length);
claim('items', /\*\*\d+ blocs, (\d+) items\*\*/, plainItems.length + blockItems.length);
claim('entrées de Codex',
  /\*\*(\d+) entrées\*\*/,
  (read('src/main/java/com/veskorius/codex/CodexRegistry.java').match(/^\s+add\(/gm) || []).length);
// Le chiffre avait glissé de 9 à 14 sans que rien ne le dise : c'est exactement le genre
// d'écart qui fait réécrire un système « manquant » qui existait déjà.
claim('types de recette', /\*\*(\d+) types de recette\*\*/,
  (read('src/main/java/com/veskorius/recipe/ModRecipeTypes.java').match(/"[a-z_]+"/g) || [])
    .filter((v, i, a) => a.indexOf(v) === i).length);
claim('GameTest', /\*\*(\d+) GameTest/,
  javaFiles.filter(f => f.includes('gametest'))
    .reduce((n, f) => n + (read(f).match(/^    @GameTest/gm) || []).length, 0));

// --- Verdict --------------------------------------------------------------
if (problems.length === 0) {
  console.log('audit : rien à signaler ('
    + blocks.length + ' blocs, ' + (plainItems.length + blockItems.length) + ' items).');
  process.exit(0);
}
console.error('audit : ' + problems.length + ' problème(s)\n');
for (const p of problems) console.error('  · ' + p);
process.exit(1);
