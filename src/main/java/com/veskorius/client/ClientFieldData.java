package com.veskorius.client;

import com.veskorius.network.FieldHudPayload;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * Cache client de la dernière lecture de champ reçue (12-UX, « HUD de champ »), alimenté
 * par {@code FieldHudPayload}. Un seul joueur local : un champ statique suffit, comme
 * pour {@link ClientCodexData}.
 *
 * <p>La lecture <b>périme</b> : le serveur n'envoie rien quand le joueur sort d'un champ
 * ou range son Locator, donc c'est l'absence de nouvelle lecture qui efface le HUD. La
 * péremption vaut plusieurs intervalles d'envoi pour qu'un simple hoquet réseau ne fasse
 * pas clignoter l'affichage.
 */
public final class ClientFieldData {

    /** Ticks sans nouvelle lecture au bout desquels le HUD s'efface. */
    private static final int STALE_AFTER_TICKS = 40;

    @Nullable
    private static FieldHudPayload last;
    private static long lastAt;

    private ClientFieldData() {
    }

    public static void apply(FieldHudPayload payload) {
        last = payload;
        lastAt = now();
    }

    /** Lecture courante, ou {@code null} s'il n'y en a pas (ou plus). */
    @Nullable
    public static FieldHudPayload current() {
        if (last == null) {
            return null;
        }
        long age = now() - lastAt;
        // age < 0 : le temps du monde a reculé (changement de monde) — la lecture ne
        // vient pas d'ici, on l'efface aussi.
        if (age < 0 || age > STALE_AFTER_TICKS) {
            last = null;
            return null;
        }
        return last;
    }

    /** Effacement immédiat (changement de monde/déconnexion). */
    public static void clear() {
        last = null;
    }

    private static long now() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }
}
