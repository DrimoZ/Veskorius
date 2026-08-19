// Hook Stop : lance l'audit de cohérence en fin de tour.
//
// `tools/audit.js` est du Node pur — pas de Gradle, pas de JVM, quelques dizaines de
// millisecondes. Il attrape la famille de panne que rien d'autre ne voit : un objet
// enregistré sans modèle, un bloc sans traduction, une entrée absente de l'index de
// registres. Les GameTest ne peuvent pas la couvrir (ils tournent dans le jar, sans
// src/generated ni veskorius-design), et rien ne plante — le défaut se découvre en jeu.
//
// Sortie 2 = le tour ne se termine pas, stderr revient à Claude pour qu'il corrige.
//
// LE GARDE-FOU CONTRE LA BOUCLE EST OBLIGATOIRE. Sans `stop_hook_active`, un audit qui
// échoue pour une raison que Claude ne peut pas régler relancerait le tour sans fin. Au
// deuxième passage on laisse donc passer : le problème a déjà été signalé une fois, il
// est dans la conversation, et c'est à l'humain de trancher.

const { execFileSync } = require('child_process');
const path = require('path');

let input = '';
try {
  input = require('fs').readFileSync(0, 'utf8');
} catch {
  /* pas de stdin : on audite quand même */
}

let payload = {};
try {
  payload = JSON.parse(input || '{}');
} catch {
  /* charge utile illisible : on audite quand même */
}

if (payload.stop_hook_active) process.exit(0);

const repo = path.resolve(__dirname, '..', '..');

try {
  execFileSync('node', ['tools/audit.js'], { cwd: repo, stdio: ['ignore', 'pipe', 'pipe'] });
  process.exit(0);
} catch (e) {
  const out = `${e.stdout || ''}${e.stderr || ''}`.trim();
  // ENOENT : node introuvable, ou tools/audit.js supprimé. Ce n'est pas un défaut du
  // mod — bloquer là-dessus punirait une session qui n'a rien fait de mal.
  if (e.code === 'ENOENT') process.exit(0);
  console.error(
    `L'audit de cohérence échoue (./gradlew audit) :\n\n${out}\n\n` +
    `Corrige ces points avant de terminer. Si les ressources générées sont périmées, ` +
    `lance d'abord ./gradlew runData --offline.`
  );
  process.exit(2);
}
