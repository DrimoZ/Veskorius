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
    REDSTONE("tuner_redstone"),

    /**
     * Fait défiler la <b>bande harmonique</b> de la machine (06-Energy.md). Sans effet
     * sur les machines universelles (tout le T1) : elles n'ont pas de bande.
     */
    ATTUNE("tuner_attune"),

    /**
     * Remet à neuf la <b>calibration</b> d'un Harmonic Amplifier, contre 1 Resonance
     * Component (05-Machines.md, « Calibration »).
     *
     * <p>Ce mode n'est pas un confort : sans lui, la dérive de l'amplificateur serait une
     * dégradation à sens unique, ce qui est pire que pas de dérive du tout — une mécanique
     * qui ne fait que retirer, sans geste pour y répondre, n'est pas un système, c'est une
     * pénalité. C'est aussi le <b>même geste</b> que pour la dissonance d'un cran en
     * dessous : ça se dérègle à l'usage, on ré-accorde au Tuner. Le joueur n'apprend qu'un
     * concept pour tout le mod.
     */
    CALIBRATE("tuner_calibrate"),

    /**
     * Fait défiler la <b>priorité</b> d'une machine face à la pénurie (06-Energy.md).
     *
     * <p>Sans Resonance Network Hub posé, ce réglage n'a aucun effet — et c'est bien ainsi :
     * le joueur peut le découvrir et le régler avant d'avoir le Hub, sans que ça change
     * quoi que ce soit, puis comprendre d'un coup à quoi il servait le jour où il en pose
     * un. Un réglage qui n'apparaîtrait qu'avec sa machine serait un réglage qu'on ne
     * trouve jamais.
     */
    PRIORITY("tuner_priority");

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
