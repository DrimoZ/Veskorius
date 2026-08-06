// Petite bibliothèque de dessin raster pour les textures de bloc (32x32).
//
// Pourquoi du procédural et plus des cartes de pixels : à 16x16 une carte de
// caractères se lit et s'édite ; à 32x32 c'est 1024 caractères par texture, illisible
// et impossible à maintenir. Surtout, la cohérence d'une SÉRIE de machines vient de
// ce que le biseau, la patine, les rivets et les gravures sont LA MÊME ROUTINE
// partout — pas de vingt dessins qui se ressemblent à peu près.

class Canvas {
  constructor(size) {
    this.size = size;
    this.px = new Uint8Array(size * size * 4);
  }

  static hex(c) {
    if (Array.isArray(c)) return c;
    const v = parseInt(c.slice(1), 16);
    return [(v >> 16) & 255, (v >> 8) & 255, v & 255, c.length > 7 ? parseInt(c.slice(7, 9), 16) : 255];
  }

  set(x, y, col, alpha = 255) {
    const s = this.size;
    if (x < 0 || y < 0 || x >= s || y >= s) return;
    const [r, g, b] = Canvas.hex(col);
    const i = (y * s + x) * 4;
    if (alpha >= 255) {
      this.px[i] = r; this.px[i + 1] = g; this.px[i + 2] = b; this.px[i + 3] = 255;
      return;
    }
    // Mélange sur ce qui est déjà là (le fond est opaque dans nos textures de bloc).
    const a = alpha / 255;
    const dst = this.px[i + 3] === 0 ? [r, g, b] : [this.px[i], this.px[i + 1], this.px[i + 2]];
    this.px[i] = Math.round(dst[0] * (1 - a) + r * a);
    this.px[i + 1] = Math.round(dst[1] * (1 - a) + g * a);
    this.px[i + 2] = Math.round(dst[2] * (1 - a) + b * a);
    this.px[i + 3] = 255;
  }

  get(x, y) {
    const i = (y * this.size + x) * 4;
    return [this.px[i], this.px[i + 1], this.px[i + 2], this.px[i + 3]];
  }

  fill(col) {
    for (let y = 0; y < this.size; y++) for (let x = 0; x < this.size; x++) this.set(x, y, col);
    return this;
  }

  rect(x, y, w, h, col, alpha) {
    for (let j = y; j < y + h; j++) for (let i = x; i < x + w; i++) this.set(i, j, col, alpha);
    return this;
  }

  /** Contour d'un rectangle (1px), sans le remplir. */
  frameRect(x, y, w, h, col, alpha) {
    for (let i = x; i < x + w; i++) { this.set(i, y, col, alpha); this.set(i, y + h - 1, col, alpha); }
    for (let j = y; j < y + h; j++) { this.set(x, j, col, alpha); this.set(x + w - 1, j, col, alpha); }
    return this;
  }

  line(x0, y0, x1, y1, col, alpha) {
    let dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
    let sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
    let err = dx - dy;
    for (;;) {
      this.set(x0, y0, col, alpha);
      if (x0 === x1 && y0 === y1) break;
      const e2 = 2 * err;
      if (e2 > -dy) { err -= dy; x0 += sx; }
      if (e2 < dx) { err += dx; y0 += sy; }
    }
    return this;
  }

  /** Disque ou anneau. `fill=false` ne pose que la couronne d'un pixel. */
  disc(cx, cy, r, col, fill = true, alpha) {
    const r2 = r * r, inner = (r - 1) * (r - 1);
    for (let y = Math.floor(cy - r); y <= Math.ceil(cy + r); y++) {
      for (let x = Math.floor(cx - r); x <= Math.ceil(cx + r); x++) {
        const d = (x - cx + 0.5) * (x - cx + 0.5) + (y - cy + 0.5) * (y - cy + 0.5);
        if (d <= r2 && (fill || d > inner)) this.set(x, y, col, alpha);
      }
    }
    return this;
  }

  /** Polygone plein (scanline), pour les formes taillées : cristaux, becs, biseaux. */
  poly(pts, col, alpha) {
    const ys = pts.map((p) => p[1]);
    for (let y = Math.floor(Math.min(...ys)); y <= Math.ceil(Math.max(...ys)); y++) {
      const xs = [];
      for (let i = 0; i < pts.length; i++) {
        const [x1, y1] = pts[i], [x2, y2] = pts[(i + 1) % pts.length];
        if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)) {
          xs.push(x1 + ((y - y1) / (y2 - y1)) * (x2 - x1));
        }
      }
      xs.sort((a, b) => a - b);
      for (let i = 0; i + 1 < xs.length; i += 2) {
        for (let x = Math.round(xs[i]); x <= Math.round(xs[i + 1]); x++) this.set(x, y, col, alpha);
      }
    }
    return this;
  }

  /** Applique un calque : fn(x,y) rend une couleur ou null. */
  each(fn) {
    for (let y = 0; y < this.size; y++) {
      for (let x = 0; x < this.size; x++) {
        const c = fn(x, y, this.get(x, y));
        if (c) this.set(x, y, c);
      }
    }
    return this;
  }
}

/** PRNG déterministe : une graine = toujours la même texture. */
function rng(seed) {
  let s = (seed >>> 0) || 1;
  return () => {
    s ^= s << 13; s >>>= 0;
    s ^= s >> 17;
    s ^= s << 5; s >>>= 0;
    return s / 0x100000000;
  };
}

// --- Primitives de matière -----------------------------------------------

/**
 * Grain de surface : quelques pixels plus clairs/plus sombres, semés au hasard.
 * Sans lui une tôle 32x32 est un aplat mort ; avec, elle a l'air d'avoir été
 * laminée puis oxydée.
 */
function grain(c, seed, light, dark, density = 0.14, region = null) {
  const rand = rng(seed);
  for (let y = 0; y < c.size; y++) {
    for (let x = 0; x < c.size; x++) {
      if (region && !region(x, y)) continue;
      const r = rand();
      if (r < density / 2) c.set(x, y, light, 90);
      else if (r < density) c.set(x, y, dark, 90);
    }
  }
  return c;
}

/**
 * Biseau : arête claire en haut/gauche, ombre en bas/droite. C'est ce qui donne
 * l'épaisseur d'une plaque — la lumière du mod vient toujours d'en haut à gauche.
 */
function bevel(c, x, y, w, h, light, dark) {
  for (let i = x; i < x + w; i++) { c.set(i, y, light); c.set(i, y + h - 1, dark); }
  for (let j = y; j < y + h; j++) { c.set(x, j, light); c.set(x + w - 1, j, dark); }
  c.set(x + w - 1, y, light);
  c.set(x, y + h - 1, dark);
  return c;
}

/** Rivet : 2x2 avec un point de lumière. Détail technologique le moins cher qui soit. */
function rivet(c, x, y, metal, hi, lo) {
  c.rect(x, y, 2, 2, metal);
  c.set(x, y, hi);
  c.set(x + 1, y + 1, lo);
  return c;
}

/**
 * Gravure : un sillon creusé dans le métal, avec un filet lumineux au fond. C'est
 * l'élément qui fait basculer une tôle de « machine » à « machine enchantée » —
 * la technologie porte la trace d'un tracé rituel. Le filet ne s'allume qu'en
 * marche : c'est lui qui raconte que la Résonance circule.
 */
function engrave(c, pts, groove, glow, alpha = 255) {
  for (let i = 0; i + 1 < pts.length; i++) {
    c.line(pts[i][0], pts[i][1], pts[i + 1][0], pts[i + 1][1], groove);
  }
  if (glow) {
    for (let i = 0; i + 1 < pts.length; i++) {
      c.line(pts[i][0], pts[i][1], pts[i + 1][0], pts[i + 1][1], glow, alpha);
    }
  }
  return c;
}

/** Patine : taches de vert-de-gris sur le cuivre, agglutinées plutôt que dispersées. */
function patina(c, seed, col, density, region = null) {
  const rand = rng(seed);
  const s = c.size;
  for (let n = 0; n < density; n++) {
    const cx = Math.floor(rand() * s), cy = Math.floor(rand() * s);
    const r = 1 + Math.floor(rand() * 2);
    for (let y = cy - r; y <= cy + r; y++) {
      for (let x = cx - r; x <= cx + r; x++) {
        if (region && !region(x, y)) continue;
        if ((x - cx) ** 2 + (y - cy) ** 2 <= r * r && rand() > 0.35) c.set(x, y, col, 120);
      }
    }
  }
  return c;
}

// --- Couches de finition --------------------------------------------------
// C'est ici que se joue la différence entre « un aplat avec du bruit » et une
// surface. Aucune de ces passes n'est visible seule ; empilées à faible opacité,
// elles donnent l'épaisseur, l'usure et la lumière. On les applique toujours dans
// cet ordre : matière → modelé → usure → lumière.

/**
 * Dégradé directionnel : la lumière vient du coin haut-gauche, donc la tôle
 * s'assombrit vers le bas-droite. Sans ça, une plaque de 32x32 reste uniformément
 * éclairée et se lit comme du carton, quel que soit le bruit posé dessus.
 */
function gradient(c, light, dark, strength = 26) {
  const s = c.size;
  c.each((x, y) => null);
  for (let y = 0; y < s; y++) {
    for (let x = 0; x < s; x++) {
      const t = (x / s + y / s) / 2; // 0 en haut-gauche, 1 en bas-droite
      const a = Math.round(Math.abs(t - 0.5) * 2 * strength);
      if (a > 0) c.set(x, y, t < 0.5 ? light : dark, a);
    }
  }
  return c;
}

/**
 * Occlusion : les pixels au bord intérieur d'un creux s'assombrissent. C'est ce qui
 * fait qu'un panneau encastré a l'air encastré plutôt que peint.
 */
function ao(c, x, y, w, h, col, depth = 3) {
  for (let d = 0; d < depth; d++) {
    const a = Math.round(70 * (1 - d / depth));
    for (let i = x + d; i < x + w - d; i++) {
      c.set(i, y + d, col, a);
      c.set(i, y + h - 1 - d, col, Math.round(a * 0.55));
    }
    for (let j = y + d; j < y + h - d; j++) {
      c.set(x + d, j, col, a);
      c.set(x + w - 1 - d, j, col, Math.round(a * 0.55));
    }
  }
  return c;
}

/**
 * Tramage : un damier entre deux valeurs, pour obtenir une transition douce sans
 * ajouter de couleur. La technique de base du pixel art à palette contrainte — elle
 * fait le travail d'un dégradé avec deux teintes.
 */
function dither(c, x, y, w, h, col, ratio = 0.5, alpha = 110) {
  for (let j = y; j < y + h; j++) {
    for (let i = x; i < x + w; i++) {
      const on = ratio >= 0.5 ? (i + j) % 2 === 0 : (i + j) % 4 === 0;
      if (on) c.set(i, j, col, alpha);
    }
  }
  return c;
}

/**
 * Rayures d'usure : de courts traits suivant la surface. Deux ou trois suffisent —
 * c'est le détail qui fait qu'un objet a servi, mais il devient du bruit si on en
 * met partout.
 */
function scratches(c, seed, light, dark, count = 5) {
  const rand = rng(seed);
  for (let n = 0; n < count; n++) {
    const x = 3 + Math.floor(rand() * (c.size - 8));
    const y = 3 + Math.floor(rand() * (c.size - 6));
    const len = 2 + Math.floor(rand() * 5);
    const horiz = rand() > 0.35;
    const col = rand() > 0.5 ? light : dark;
    for (let i = 0; i < len; i++) {
      c.set(horiz ? x + i : x, horiz ? y : y + i, col, 70);
    }
  }
  return c;
}

/**
 * Halo : la lumière déborde de ce qui l'émet. Rendu par cercles concentriques
 * d'opacité décroissante — sans lui, un cœur allumé est une pastille de couleur
 * collée sur du métal, pas une source.
 */
function bloom(c, cx, cy, r, col, strength = 70) {
  for (let ring = r; ring >= 1; ring--) {
    const a = Math.round(strength * (1 - ring / (r + 1)) ** 1.6);
    if (a > 0) c.disc(cx, cy, ring, col, false, a);
  }
  return c;
}

/** Assombrissement du pourtour : deux blocs côte à côte gardent une frontière lisible. */
function vignette(c, col, strength = 34) {
  const s = c.size;
  for (let y = 0; y < s; y++) {
    for (let x = 0; x < s; x++) {
      const d = Math.min(x, y, s - 1 - x, s - 1 - y);
      if (d < 3) c.set(x, y, col, Math.round(strength * (1 - d / 3)));
    }
  }
  return c;
}

/** Éclats spéculaires : un pixel très clair sur une arête. À doser au compte-gouttes. */
function speculars(c, pts, col) {
  for (const [x, y] of pts) c.set(x, y, col, 190);
  return c;
}

module.exports = {
  Canvas, rng, grain, bevel, rivet, engrave, patina,
  gradient, ao, dither, scratches, bloom, vignette, speculars,
};
