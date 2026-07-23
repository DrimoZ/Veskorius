package com.veskorius.energy;

/**
 * Bande harmonique d'un champ ou d'une machine (06-Energy.md, « Harmoniques &
 * Dissonance »).
 *
 * <p>Volontairement <b>peu nombreuses et identifiées par une couleur</b> : la lecture
 * se fait à l'œil (coupole de l'émetteur / glow de la machine), jamais par un chiffre.
 * Même couleur = accordé ; couleurs différentes = désaccordé (la machine tourne quand
 * même, mais coûte plus cher et génère de la dissonance).
 *
 * <p>Une machine <b>sans bande</b> ({@code null} côté code) est <b>universelle</b> :
 * elle accepte n'importe quel champ et ne se désaccorde jamais. C'est le cas de tout
 * le T1 — la boucle de départ ne gagne aucune complexité.
 *
 * <p>L'ordinal est persisté en NBT : ne pas réordonner sans migration.
 */
public enum HarmonicBand {

    /** Bande par défaut, celle du Field Emitter T2. Violet. */
    FUNDAMENTAL(0xFF9B59D0),
    /** Cyan. Débloquée avec l'Émetteur Accordable. */
    MEDIAN(0xFF35C0C8),
    /** Ambre. Débloquée avec l'Émetteur Accordable. */
    HIGH(0xFFD8922A);

    private static final HarmonicBand[] VALUES = values();

    private final int color;

    HarmonicBand(int color) {
        this.color = color;
    }

    public static HarmonicBand byIndex(int index) {
        return VALUES[Math.floorMod(index, VALUES.length)];
    }

    /**
     * Bande suivante, bornée au nombre de bandes actives en configuration : un modpack
     * qui n'en veut que 2 ne peut pas en sélectionner une troisième.
     */
    public HarmonicBand next(int activeBandCount) {
        int count = Math.clamp(activeBandCount, 1, VALUES.length);
        return byIndex((ordinal() + 1) % count);
    }

    /** Couleur ARGB, utilisée par la coupole et le glow (12-UX : la couleur EST l'interface). */
    public int color() {
        return color;
    }

    /** Clé de langue : {@code gui.veskorius.band.<name>}. */
    public String labelKey() {
        return "gui.veskorius.band." + name().toLowerCase();
    }
}
