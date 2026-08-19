// Hook PreToolUse : refuse toute écriture dans src/generated/resources/.
//
// Ces 708 JSON sont produits par `./gradlew runData`, qui vide même son .cache à chaque
// lancement pour garantir que la sortie corresponde TOUJOURS aux DataProvider Java. Une
// édition manuelle y est donc perdue au run suivant — silencieusement, et parfois
// plusieurs jours plus tard, quand on ne fait plus le lien.
//
// Ça a déjà été tenté (sed et perl sur recipe/assembling/component.json), d'où la
// couverture de Bash en plus d'Edit/Write : le raccourci se prend aussi bien en shell.
//
// LA DÉTECTION EXIGE UN VERBE D'ÉCRITURE ADJACENT AU CHEMIN, pas seulement les deux
// présents dans la même commande. La première version cherchait le chemin ET, n'importe
// où, un caractère d'une liste dont « > » faisait partie : elle a bloqué son propre
// commit d'installation, parce que le message citait le chemin et se terminait par
// « <noreply@anthropic.com> ». Puis elle a bloqué sa propre réécriture, le nouveau
// source contenant à la fois le chemin et des chevrons. Un garde-fou qui crie sur les
// commandes innocentes finit désactivé, donc inutile.
//
// Sortie 2 = l'appel est bloqué, stderr revient à Claude avec le bon geste à faire.

const CIBLE = 'src/generated/resources';

// Le chemin doit être la CIBLE d'une écriture, dans le même segment de commande —
// d'où les classes [^|;&] qui empêchent de franchir un pipe ou un enchaînement.
const ECRITURES = [
  // sed -i / perl -pi, l'option en place avant le chemin. Les chiffres comptent dans la
  // classe : `perl -0pi -e` est la forme employée dans ce dépôt, et `-[a-z]*i` la ratait.
  /\bsed\s+[^|;&]*-[a-z0-9.]*i[^|;&]*src\/generated\/resources/,
  /\bperl\s+[^|;&]*-[a-z0-9.]*i[^|;&]*src\/generated\/resources/,
  // redirection ou tee VERS le chemin
  /(?:>>?|\btee\b(?:\s+-a)?)\s*["']?[^\s"'|;&]*src\/generated\/resources/,
  // suppression / déplacement / copie / troncature visant le chemin
  /\b(?:rm|mv|cp|truncate|install)\b[^|;&]*src\/generated\/resources/,
  // équivalents PowerShell
  /\b(?:Set-Content|Add-Content|Out-File|Remove-Item|Clear-Content|Copy-Item|Move-Item)\b[^|;&]*src\/generated\/resources/i,
];

// Commandes dont l'argument libre parle souvent du dépôt, chemins compris, et qui
// n'écrivent jamais dans l'arbre de travail. Un message de commit n'est pas une écriture.
const INOFFENSIVES = /^\s*git\s+(?:commit|log|show|diff|status|blame)\b/;

let payload = {};
try {
  payload = JSON.parse(require('fs').readFileSync(0, 'utf8') || '{}');
} catch {
  process.exit(0); // charge utile illisible : ne rien bloquer sur un doute
}

const tool = payload.tool_name || '';
const input = payload.tool_input || {};
const normalise = s => String(s || '').split(String.fromCharCode(92)).join('/');

let vise = false;
if (tool === 'Edit' || tool === 'Write' || tool === 'NotebookEdit') {
  vise = normalise(input.file_path).includes(CIBLE);
} else if (tool === 'Bash' || tool === 'PowerShell') {
  const cmd = normalise(input.command);
  // Lire un JSON généré (cat, grep, jq) est le geste normal pour vérifier ce que la
  // datagen a produit : jamais bloqué.
  vise = !INOFFENSIVES.test(cmd) && ECRITURES.some(re => re.test(cmd));
}

if (!vise) process.exit(0);

console.error(
  `src/generated/resources/ est produit par la datagen : toute édition manuelle sera ` +
  `écrasée au prochain ./gradlew runData (qui vide son .cache exprès).\n\n` +
  `Le bon geste : modifier le DataProvider Java concerné dans ` +
  `src/main/java/com/veskorius/datagen/ (ModRecipeProvider, ModBlockStateProvider, ` +
  `ModBlockLootProvider, ModLanguageProvider…), puis lancer ./gradlew runData --offline.`
);
process.exit(2);
