package com.veskorius.client;

import com.veskorius.client.screen.CodexScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Point d'entrée client du Codex : ouvre l'écran. Classe chargée uniquement côté
 * client (appelée depuis {@code ResonanceCodexItem.use} sous garde
 * {@code level.isClientSide}), donc jamais touchée sur un serveur dédié.
 */
public final class CodexClient {

    private CodexClient() {
    }

    public static void open(ItemStack codex) {
        Minecraft.getInstance().setScreen(new CodexScreen(codex));
    }
}
