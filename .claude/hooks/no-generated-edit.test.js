// Table de vérité de no-generated-edit.js.   node .claude/hooks/no-generated-edit.test.js
//
// Elle existe parce que la première version du hook a bloqué son propre commit
// d'installation, puis sa propre réécriture : le message citait le chemin protégé, et la
// détection se contentait de « chemin quelque part » + « chevron quelque part ». Les faux
// positifs d'un garde-fou coûtent plus cher que son absence — on le désactive, et il ne
// protège plus rien. D'où les cas « lecture » et « commit qui cite le chemin », qui sont
// la moitié du test.
//
// Les cas vivent dans un fichier plutôt que sur la ligne de commande : écrits en argument
// de `node -e`, ils déclencheraient le hook qu'ils testent.
const { execFileSync } = require('child_process');
const path = require('path');

const HOOK = path.join(__dirname, 'no-generated-edit.js');
const G = 'src/generated/resources';
const BS = String.fromCharCode(92);

const cas = [
  // [code attendu, description, charge utile PreToolUse]
  [2, 'Edit dans le genere', { tool_name: 'Edit', tool_input: { file_path: `x/${G}/data/a.json` } }],
  [2, 'Edit chemin Windows', { tool_name: 'Edit', tool_input: { file_path: `C:${BS}p${BS}src${BS}generated${BS}resources${BS}a.json` } }],
  [0, 'Edit dans le source Java', { tool_name: 'Edit', tool_input: { file_path: 'src/main/java/com/veskorius/block/ModBlocks.java' } }],
  [2, 'sed -i', { tool_name: 'Bash', tool_input: { command: `sed -i s/2/1/ ${G}/data/a.json` } }],
  [2, 'perl -0pi', { tool_name: 'Bash', tool_input: { command: `perl -0pi -e 's/a/b/' ${G}/data/a.json` } }],
  [2, 'redirection', { tool_name: 'Bash', tool_input: { command: `echo '{}' > ${G}/data/a.json` } }],
  [2, 'rm', { tool_name: 'Bash', tool_input: { command: `rm -f ${G}/data/a.json` } }],
  [2, 'Set-Content', { tool_name: 'PowerShell', tool_input: { command: `Set-Content ${G}/data/a.json '{}'` } }],
  [0, 'lecture cat', { tool_name: 'Bash', tool_input: { command: `cat ${G}/data/a.json` } }],
  [0, 'lecture grep vers ailleurs', { tool_name: 'Bash', tool_input: { command: `grep -r foo ${G} > /tmp/out.txt` } }],
  [0, 'jq lu puis pipe', { tool_name: 'Bash', tool_input: { command: `cat ${G}/a.json | jq .result` } }],
  [0, 'commit qui cite le chemin', { tool_name: 'Bash', tool_input: { command: `git commit -m "refuse les ecritures dans ${G}, rm compris <noreply@anthropic.com>"` } }],
  [0, 'runData', { tool_name: 'Bash', tool_input: { command: './gradlew runData --offline' } }],
  [0, 'ecriture ailleurs', { tool_name: 'Bash', tool_input: { command: 'echo x > src/main/resources/a.json' } }],
];

let echecs = 0;
for (const [attendu, nom, payload] of cas) {
  let code = 0;
  try {
    execFileSync('node', [HOOK], { input: JSON.stringify(payload), stdio: ['pipe', 'pipe', 'pipe'] });
  } catch (e) {
    code = e.status;
  }
  const ok = code === attendu;
  if (!ok) echecs++;
  console.log(`${ok ? 'ok   ' : 'ECHEC'} ${nom} (attendu ${attendu}, obtenu ${code})`);
}
console.log(echecs === 0 ? `\n${cas.length}/${cas.length} cas passent.` : `\n${echecs} echec(s).`);
process.exit(echecs === 0 ? 0 : 1);
