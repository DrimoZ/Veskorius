package com.veskorius.client;

import com.veskorius.codex.CodexCategory;
import com.veskorius.codex.CodexEntry;
import com.veskorius.codex.CodexRegistry;
import com.veskorius.codex.CodexUnlock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/**
 * Cache client de l'état de déblocage du Codex (15-Codex-Guidebook.md), alimenté par
 * {@code CodexSyncPayload}. Un seul joueur local, donc un simple ensemble statique
 * suffit. Lu par le {@code CodexScreen}. Les entrées {@code ALWAYS} comptent débloquées
 * d'office (jamais envoyées sur le réseau).
 */
public final class ClientCodexData {

    private static Set<ResourceLocation> unlocked = Set.of();

    private ClientCodexData() {
    }

    public static void apply(List<ResourceLocation> ids) {
        unlocked = new LinkedHashSet<>(ids);
    }

    public static boolean isUnlocked(CodexEntry entry) {
        return entry.unlock().type() == CodexUnlock.Type.ALWAYS || unlocked.contains(entry.id());
    }

    public static int unlockedCount(CodexCategory category) {
        int count = 0;
        for (CodexEntry entry : CodexRegistry.byCategory(category)) {
            if (isUnlocked(entry)) {
                count++;
            }
        }
        return count;
    }

    /** Total débloqué toutes catégories confondues (pour un compteur global). */
    public static int totalUnlocked() {
        int count = 0;
        for (CodexEntry entry : CodexRegistry.all()) {
            if (isUnlocked(entry)) {
                count++;
            }
        }
        return count;
    }
}
