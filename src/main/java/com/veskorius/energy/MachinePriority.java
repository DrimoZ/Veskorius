package com.veskorius.energy;

import net.minecraft.network.chat.Component;

/**
 * Priorité d'une machine face à la <b>pénurie</b> d'un champ (06-Energy.md, Resonance
 * Network Hub).
 *
 * <p>Sans Hub posé, elle ne sert à rien : toutes les machines se servent dans l'ordre où
 * elles tiquent, et une base sous-alimentée hoquette au hasard. C'est délibérément
 * frustrant — c'est ce qui donne au Hub une raison d'exister au T4.
 *
 * <p>Trois niveaux, pas dix. Un ordre de tri fin obligerait à ouvrir un écran pour chaque
 * machine et à s'en souvenir ; trois classes se décident au premier coup d'œil et se
 * réglent au Resonance Tuner comme tout le reste. L'ordinal est persisté : ne pas
 * réordonner.
 */
public enum MachinePriority {

    /** Délestée en premier. Pour ce qui peut attendre : broyeurs, presses, décor. */
    LOW("priority_low"),

    /** Défaut. */
    NORMAL("priority_normal"),

    /** Servie jusqu'au bout. Pour ce dont l'arrêt coûte cher : synthèse, ancrage. */
    HIGH("priority_high");

    private static final MachinePriority[] VALUES = values();

    private final String key;

    MachinePriority(String key) {
        this.key = key;
    }

    public static MachinePriority byIndex(int index) {
        return VALUES[Math.floorMod(index, VALUES.length)];
    }

    public MachinePriority next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public Component label() {
        return Component.translatable("gui.veskorius." + key);
    }
}
