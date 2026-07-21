package com.veskorius.item;

import net.minecraft.network.chat.Component;

/**
 * Modes du Resonance Tuner. L'outil porte un mode courant (Data Component), et un
 * clic droit sur une machine applique l'action du mode. On change de mode par
 * shift-clic droit — un seul outil pour toutes les interactions de configuration,
 * conforme à 05-Machines.md / 12-UX-and-Advancements.md.
 *
 * L'ordre sert d'index de stockage et d'ordre de défilement : ne pas réordonner
 * sans casser les Tuner déjà en jeu.
 */
public enum TunerMode {

    /** Fait pivoter la face avant de la machine de 90°. */
    ROTATE("tuner_rotate"),

    /** Bascule l'interrupteur manuel de la machine (on/off). */
    POWER("tuner_power"),

    /** Bascule le mode surchauffe (machines qui le supportent). */
    OVERHEAT("tuner_overheat"),

    /** Fait défiler le mode de contrôle redstone. */
    REDSTONE("tuner_redstone");

    private static final TunerMode[] VALUES = values();

    private final String translationSuffix;

    TunerMode(String translationSuffix) {
        this.translationSuffix = translationSuffix;
    }

    public TunerMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static TunerMode byIndex(int index) {
        return VALUES[Math.floorMod(index, VALUES.length)];
    }

    public Component label() {
        return Component.translatable("gui.veskorius." + translationSuffix);
    }
}
