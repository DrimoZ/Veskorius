// Formes de base, définies en SPANS EXPLICITES (y, x0, x1 inclus).
//
// Pourquoi pas un rastériseur : sur une grille 16x16 le centre tombe ENTRE les
// pixels 7 et 8. Un cercle tracé par formule centré sur 8 est décalé d'un demi-
// pixel, ce qui se voit immédiatement — la forme paraît bancale sans qu'on sache
// dire pourquoi. Les tables ci-dessous sont symétriques par construction : chaque
// span vérifie x0 + x1 = 15.
//
// Toutes respectent une MARGE D'1 PIXEL : rien ne touche le bord du bloc, sinon la
// forme paraît rognée et le bloc « pas cadré ».

/** Cercle plein, 12 px de diamètre, centré. */
const DISC12 = [
  [2, 6, 9], [3, 4, 11], [4, 3, 12], [5, 2, 13], [6, 2, 13], [7, 2, 13],
  [8, 2, 13], [9, 2, 13], [10, 2, 13], [11, 3, 12], [12, 4, 11], [13, 6, 9],
];

/** Cercle plein, 8 px. */
const DISC8 = [
  [4, 6, 9], [5, 5, 10], [6, 4, 11], [7, 4, 11],
  [8, 4, 11], [9, 4, 11], [10, 5, 10], [11, 6, 9],
];

/** Cercle plein, 4 px. */
const DISC4 = [[6, 6, 9], [7, 5, 10], [8, 5, 10], [9, 6, 9]];

/** Losange (cristal), 12 large x 13 haut, centré. */
const DIAMOND = [
  [1, 7, 8], [2, 6, 9], [3, 5, 10], [4, 4, 11], [5, 3, 12], [6, 2, 13], [7, 2, 13],
  [8, 3, 12], [9, 4, 11], [10, 5, 10], [11, 6, 9], [12, 7, 8],
];

/** Losange intérieur, 6 large. */
const DIAMOND_IN = [
  [4, 7, 8], [5, 6, 9], [6, 5, 10], [7, 5, 10], [8, 6, 9], [9, 7, 8],
];

/** Cœur du losange, 2 large. */
const DIAMOND_CORE = [[6, 7, 8], [7, 7, 8], [8, 7, 8]];

/** Applique une table de spans. */
function fill(c, spans, col) {
  for (const [y, x0, x1] of spans) {
    for (let x = x0; x <= x1; x++) c.set(x, y, col);
  }
  return c;
}

/** Contour d'une table de spans : le pixel juste à l'extérieur de chaque extrémité. */
function outline(c, spans, col) {
  const byY = new Map(spans.map(([y, a, b]) => [y, [a, b]]));
  for (const [y, x0, x1] of spans) {
    c.set(x0 - 1, y, col);
    c.set(x1 + 1, y, col);
    const above = byY.get(y - 1);
    const below = byY.get(y + 1);
    for (let x = x0; x <= x1; x++) {
      if (!above || x < above[0] || x > above[1]) c.set(x, y - 1, col);
      if (!below || x < below[0] || x > below[1]) c.set(x, y + 1, col);
    }
  }
  return c;
}

/** Anneau : la couronne d'un disque (le disque moins un disque plus petit). */
function ring(c, outerSpans, innerSpans, col) {
  const inner = new Map(innerSpans.map(([y, a, b]) => [y, [a, b]]));
  for (const [y, x0, x1] of outerSpans) {
    const i = inner.get(y);
    for (let x = x0; x <= x1; x++) {
      if (!i || x < i[0] || x > i[1]) c.set(x, y, col);
    }
  }
  return c;
}

module.exports = { DISC12, DISC8, DISC4, DIAMOND, DIAMOND_IN, DIAMOND_CORE, fill, outline, ring };
