package com.veskorius.item;

import com.veskorius.Veskorius;
import net.minecraft.resources.ResourceLocation;

/**
 * Entrées de Codex du mod (08-Structures.md). Pur lore. Un datapack/une extension
 * peut en ajouter : il suffit d'un {@code codex_fragment} portant un nouvel id et des
 * clés de langue {@code codex.<ns>.<path>.title/.text} (voir {@link CodexFragmentItem}).
 */
public final class CodexEntries {

    private CodexEntries() {
    }

    /** Vie quotidienne du Peuple du réseau (Habitation Modeste). */
    public static final ResourceLocation DAILY_LIFE_LAMPS = id("daily_life/lamps");
    public static final ResourceLocation DAILY_LIFE_RATION = id("daily_life/ration");
    public static final ResourceLocation DAILY_LIFE_MARKET = id("daily_life/market");
    public static final ResourceLocation DAILY_LIFE_CHILDREN = id("daily_life/children");
    public static final ResourceLocation DAILY_LIFE_FESTIVAL = id("daily_life/festival");
    /** Inscription laissée par un Custode (Avant-poste). */
    public static final ResourceLocation CUSTODE_WATCH = id("custode/watch");
    /** Indice pointant vers la console de l'Avant-poste. */
    public static final ResourceLocation HINT_WORKSHOP = id("hint/workshop");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
