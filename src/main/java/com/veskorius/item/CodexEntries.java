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

    /**
     * <b>Journal d'un opérateur d'attunement</b> — quatre fragments posés dans l'ordre
     * dans le cabinet d'archives de l'Avant-poste.
     *
     * <p>C'est le premier vrai arc narratif du mod, et il porte l'événement central que
     * le lore n'avait jamais raconté de l'intérieur : l'Effondrement (`02-Lore.md`).
     * Jusqu'ici les fragments étaient des instantanés de vie quotidienne, sans avant ni
     * après ; ceux-ci se suivent, et leur ordre est <b>déterministe</b> — ils sont posés
     * dans des coffres fixes de la structure, pas tirés au hasard. Un joueur qui traverse
     * les archives lit une descente, pas une anecdote.
     *
     * <p>Ils expliquent aussi, en jeu, pourquoi la console attend encore : personne n'est
     * revenu l'éteindre.
     */
    public static final ResourceLocation OUTPOST_LOG_1 = id("outpost/log_1");
    public static final ResourceLocation OUTPOST_LOG_2 = id("outpost/log_2");
    public static final ResourceLocation OUTPOST_LOG_3 = id("outpost/log_3");
    public static final ResourceLocation OUTPOST_LOG_4 = id("outpost/log_4");

    /**
     * <b>Les quatre cotes de l'Archive.</b> Dispersées dans le bâtiment, elles ne sont
     * pas seulement du lore : leur <b>ordre</b> est la clé de la salle de lecture. C'est
     * la première fois du mod qu'un fragment sert à autre chose qu'à être lu — et le
     * pilier 2 y gagne son application la plus littérale, puisqu'on ne peut résoudre
     * qu'en ayant lu.
     *
     * <p>Elles portent aussi le premier récit explicite de la sur-résonance et des Failles
     * (08-Structures.md), c'est-à-dire la cause de l'Effondrement dite par ceux qui l'ont
     * vue venir sans pouvoir l'arrêter.
     */
    public static final ResourceLocation ARCHIVE_LOG_1 = id("archive/log_1");
    public static final ResourceLocation ARCHIVE_LOG_2 = id("archive/log_2");
    public static final ResourceLocation ARCHIVE_LOG_3 = id("archive/log_3");
    public static final ResourceLocation ARCHIVE_LOG_4 = id("archive/log_4");

    /** Les quatre, dans l'ordre — l'ordre EST la serrure. */
    public static final ResourceLocation[] ARCHIVE_LOG = {
        ARCHIVE_LOG_1, ARCHIVE_LOG_2, ARCHIVE_LOG_3, ARCHIVE_LOG_4,
    };

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
