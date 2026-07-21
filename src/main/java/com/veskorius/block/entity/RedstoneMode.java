package com.veskorius.block.entity;

import net.minecraft.network.chat.Component;

/**
 * Mode de contrôle redstone d'une machine, dans l'esprit des machines Thermal :
 * la redstone peut être ignorée, exiger un signal, ou exiger l'absence de signal.
 *
 * L'ordre des valeurs sert d'index de synchronisation (ContainerData) et d'ordre
 * de défilement du bouton — ne pas réordonner sans adapter la persistance.
 */
public enum RedstoneMode {

    /** La redstone n'a aucun effet ; seul l'interrupteur manuel compte. */
    IGNORED("redstone_ignored"),

    /** La machine ne tourne que si elle reçoit un signal redstone. */
    REQUIRES_SIGNAL("redstone_requires_signal"),

    /** La machine ne tourne que si elle ne reçoit PAS de signal. */
    REQUIRES_NO_SIGNAL("redstone_requires_no_signal");

    private static final RedstoneMode[] VALUES = values();

    private final String translationSuffix;

    RedstoneMode(String translationSuffix) {
        this.translationSuffix = translationSuffix;
    }

    /** Mode suivant, en boucle — pour le clic sur le bouton. */
    public RedstoneMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static RedstoneMode byIndex(int index) {
        return VALUES[Math.floorMod(index, VALUES.length)];
    }

    /** Vrai si la machine a le droit de tourner, selon la présence d'un signal. */
    public boolean allowsRunning(boolean powered) {
        return switch (this) {
            case IGNORED -> true;
            case REQUIRES_SIGNAL -> powered;
            case REQUIRES_NO_SIGNAL -> !powered;
        };
    }

    public Component label() {
        return Component.translatable("gui.veskorius." + translationSuffix);
    }
}
