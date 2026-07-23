package com.veskorius.block.entity;

/**
 * Mode d'automatisation d'objets d'une face de machine (item I/O, PAS de l'énergie —
 * l'énergie reste sans tuyaux, par champ). Chaque face d'une machine active porte un de
 * ces trois modes, configurable par le joueur :
 * <ul>
 *   <li>{@link #DISABLED} — aucune capability exposée sur cette face ;</li>
 *   <li>{@link #INPUT} — un système externe (hopper…) peut <b>insérer</b> dans les slots
 *       d'entrée (jamais dans la sortie ni l'augment) ;</li>
 *   <li>{@link #OUTPUT} — un système externe peut <b>extraire</b> du slot de sortie.</li>
 * </ul>
 * L'ordinal est persisté en NBT : ne pas réordonner sans migration.
 */
public enum SideMode {
    DISABLED,
    INPUT,
    OUTPUT;

    private static final SideMode[] VALUES = values();

    public static SideMode byIndex(int index) {
        return VALUES[Math.floorMod(index, VALUES.length)];
    }

    /** Mode suivant, pour un clic de configuration qui fait défiler les modes. */
    public SideMode next() {
        return byIndex(ordinal() + 1);
    }
}
