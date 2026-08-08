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

    /**
     * Vrai une fois la première synchronisation reçue. Elle apporte <b>tout</b> ce que le
     * joueur a déjà débloqué : annoncer ce lot-là ferait trente bulles à la connexion, sur
     * des pages lues depuis longtemps. On mémorise en silence, et on n'annonce qu'ensuite.
     */
    private static boolean primed;

    public static void apply(List<ResourceLocation> ids) {
        Set<ResourceLocation> previous = unlocked;
        unlocked = new LinkedHashSet<>(ids);
        if (!primed) {
            primed = true;
            return;
        }
        for (ResourceLocation id : ids) {
            if (previous.contains(id)) {
                continue;
            }
            CodexEntry entry = CodexRegistry.get(id);
            if (entry != null) {
                net.minecraft.client.Minecraft.getInstance().getToasts()
                    .addToast(new CodexToast(entry));
            }
        }
    }

    /**
     * Remet le cache à zéro à la déconnexion. Sans ça, rejoindre un second monde
     * comparerait les nouvelles entrées à celles du monde précédent : les pages communes
     * passeraient pour déjà connues et ne s'annonceraient jamais.
     */
    public static void reset() {
        unlocked = Set.of();
        primed = false;
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
