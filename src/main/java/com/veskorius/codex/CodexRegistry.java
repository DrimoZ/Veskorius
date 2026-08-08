package com.veskorius.codex;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.CodexEntries;
import com.veskorius.item.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Catalogue des entrées du Codex de Résonance (15-Codex-Guidebook.md). Défini en
 * code (v1) : les deux côtés client/serveur le partagent, donc aucune synchronisation
 * réseau du catalogue n'est nécessaire — seul l'état de déblocage voyage, porté par
 * l'objet (voir {@code CodexUnlocks}). Le texte de chaque entrée reste de la donnée
 * (fichiers de langue).
 *
 * L'ordre d'ajout est l'ordre d'affichage dans une catégorie. Le contenu v1 couvre
 * toute la boucle T1-T2 ; les tiers suivants ajouteront leurs entrées ici.
 */
public final class CodexRegistry {

    private static final List<CodexEntry> ENTRIES = new ArrayList<>();

    /**
     * Palier de chaque entrée, par chemin. Une table plutôt qu'un argument de plus sur
     * chaque ligne d'{@code add} : les soixante appels resteraient lisibles, mais on
     * relirait soixante fois le même chiffre au lieu de voir la progression d'un bloc.
     * Le lore n'y figure pas — il n'appartient à aucun palier, et c'est voulu.
     */
    private static final java.util.Map<String, Integer> TIERS = new java.util.HashMap<>();

    static {
        TIERS.put("intro/welcome", 0);
        TIERS.put("intro/using_codex", 0);
        TIERS.put("intro/getting_started", 0);
        TIERS.put("crystals/raw", 1);
        TIERS.put("crystals/stable", 1);
        TIERS.put("crystals/dust", 1);
        TIERS.put("crystals/pockets", 1);
        TIERS.put("crystals/refined", 2);
        TIERS.put("crystals/alloy", 3);
        TIERS.put("crystals/slag", 3);
        TIERS.put("crystals/hyper", 4);
        TIERS.put("crystals/essence", 5);
        TIERS.put("fields/osc", 2);
        TIERS.put("fields/emitter", 2);
        TIERS.put("fields/storage_cell", 2);
        TIERS.put("fields/locator", 2);
        TIERS.put("fields/lattice", 4);
        TIERS.put("fields/calibration", 4);
        TIERS.put("machines/stabilizer", 1);
        TIERS.put("machines/assembler", 1);
        TIERS.put("machines/whetstone", 1);
        TIERS.put("machines/crusher", 1);
        TIERS.put("machines/control", 1);
        TIERS.put("machines/purifier", 2);
        TIERS.put("machines/roost", 2);
        TIERS.put("machines/tuner", 2);
        TIERS.put("machines/catalyst_core", 2);
        TIERS.put("machines/forge", 3);
        TIERS.put("machines/relay", 3);
        TIERS.put("machines/synthesizer", 3);
        TIERS.put("machines/driller", 3);
        TIERS.put("machines/slag_vent", 3);
        TIERS.put("machines/compressor", 3);
        TIERS.put("machines/chamber", 4);
        TIERS.put("machines/amplifier", 4);
        TIERS.put("machines/hub", 4);
        TIERS.put("machines/extraction_array", 4);
        TIERS.put("machines/convergence_core", 4);
        TIERS.put("machines/rift_anchor", 5);
        TIERS.put("machines/rift_extractor", 5);
        TIERS.put("machines/rift_ward", 5);
        TIERS.put("world/veined_stone", 1);
        TIERS.put("world/flux_deposit", 1);
        TIERS.put("world/outpost", 2);
        TIERS.put("world/rift", 5);
        TIERS.put("fauna/strider", 1);
        TIERS.put("fauna/custode", 2);
        TIERS.put("fauna/archiviste", 4);
        TIERS.put("fauna/guardian", 5);
        TIERS.put("gear/tools", 3);
        TIERS.put("gear/armor", 3);
        TIERS.put("gear/ward_plate", 5);
        TIERS.put("progression/tier1", 1);
        TIERS.put("progression/tier2", 2);
        TIERS.put("progression/tier3", 3);
        TIERS.put("progression/tier4", 4);
        TIERS.put("progression/tier5", 5);
    }

    static {
        // --- Introduction (toujours débloquées) ---
        add("intro/welcome", CodexCategory.INTRO, ModItems.RESONANCE_CODEX, CodexUnlock.always());
        add("intro/using_codex", CodexCategory.INTRO, () -> net.minecraft.world.item.Items.BOOK,
            CodexUnlock.always());
        add("intro/getting_started", CodexCategory.INTRO, ModItems.RAW_RESONANCE_CRYSTAL,
            CodexUnlock.always());

        // --- Cristaux & Raffinage ---
        add("crystals/raw", CodexCategory.CRYSTALS, ModItems.RAW_RESONANCE_CRYSTAL,
            CodexUnlock.item(ModItems.RAW_RESONANCE_CRYSTAL));
        add("crystals/stable", CodexCategory.CRYSTALS, ModItems.STABLE_RESONANCE_CRYSTAL,
            CodexUnlock.item(ModItems.STABLE_RESONANCE_CRYSTAL));
        add("crystals/refined", CodexCategory.CRYSTALS, ModItems.REFINED_RESONANCE_CRYSTAL,
            CodexUnlock.item(ModItems.REFINED_RESONANCE_CRYSTAL));
        add("crystals/dust", CodexCategory.CRYSTALS, ModItems.RESONANCE_DUST,
            CodexUnlock.item(ModItems.RESONANCE_DUST));
        add("crystals/pockets", CodexCategory.CRYSTALS, ModBlocks.RESONANCE_CRYSTAL_CLUSTER,
            CodexUnlock.item(ModItems.RAW_RESONANCE_CRYSTAL));

        // --- Champs & Énergie ---
        add("fields/osc", CodexCategory.FIELDS, ModItems.STABLE_RESONANCE_CRYSTAL,
            CodexUnlock.item(ModBlocks.FIELD_EMITTER));
        add("fields/emitter", CodexCategory.FIELDS, ModBlocks.FIELD_EMITTER,
            CodexUnlock.item(ModBlocks.FIELD_EMITTER));
        add("fields/storage_cell", CodexCategory.FIELDS, ModItems.RESONANCE_STORAGE_CELL,
            CodexUnlock.item(ModItems.RESONANCE_STORAGE_CELL));
        add("fields/locator", CodexCategory.FIELDS, ModItems.RESONANCE_LOCATOR,
            CodexUnlock.item(ModItems.RESONANCE_LOCATOR));

        // --- Machines ---
        add("machines/stabilizer", CodexCategory.MACHINES, ModBlocks.RESONANCE_STABILIZER,
            CodexUnlock.item(ModBlocks.RESONANCE_STABILIZER));
        add("machines/assembler", CodexCategory.MACHINES, ModBlocks.COMPONENT_ASSEMBLER,
            CodexUnlock.item(ModBlocks.COMPONENT_ASSEMBLER));
        add("machines/whetstone", CodexCategory.MACHINES, ModBlocks.RESONANCE_WHETSTONE,
            CodexUnlock.item(ModBlocks.RESONANCE_WHETSTONE));
        add("machines/purifier", CodexCategory.MACHINES, ModBlocks.FLUX_PURIFIER,
            CodexUnlock.item(ModBlocks.FLUX_PURIFIER));
        add("machines/crusher", CodexCategory.MACHINES, ModBlocks.CRYSTAL_CRUSHER,
            CodexUnlock.item(ModBlocks.CRYSTAL_CRUSHER));
        add("machines/roost", CodexCategory.MACHINES, ModBlocks.CRYSTAL_ROOST,
            CodexUnlock.item(ModBlocks.CRYSTAL_ROOST));
        add("machines/tuner", CodexCategory.MACHINES, ModItems.RESONANCE_TUNER,
            CodexUnlock.item(ModItems.RESONANCE_TUNER));
        add("machines/catalyst_core", CodexCategory.MACHINES, ModItems.RESONANCE_CATALYST_CORE,
            CodexUnlock.item(ModItems.RESONANCE_CATALYST_CORE));
        add("machines/control", CodexCategory.MACHINES, ModItems.RESONANCE_TUNER,
            CodexUnlock.item(ModBlocks.RESONANCE_STABILIZER));

        // --- Monde & Structures ---
        add("world/veined_stone", CodexCategory.WORLD, ModBlocks.RESONANCE_VEINED_STONE,
            CodexUnlock.item(ModBlocks.RESONANCE_VEINED_STONE));
        add("world/flux_deposit", CodexCategory.WORLD, ModItems.RAW_FLUX_DEPOSIT,
            CodexUnlock.item(ModItems.RAW_FLUX_DEPOSIT));
        add("world/outpost", CodexCategory.WORLD, ModBlocks.ATTUNEMENT_CONSOLE,
            CodexUnlock.advancement(adv("tier2_field")));

        // --- Faune ---
        add("fauna/strider", CodexCategory.FAUNA, ModItems.CRYSTAL_STRIDER_SPAWN_EGG,
            CodexUnlock.item(ModItems.RESONANCE_SPORE));
        add("fauna/custode", CodexCategory.FAUNA, ModItems.CUSTODE_SPAWN_EGG,
            CodexUnlock.item(ModItems.CUSTODE_ALLOY_FRAGMENT));
        add("fauna/archiviste", CodexCategory.FAUNA, ModItems.HYPER_REFINED_CRYSTAL,
            CodexUnlock.item(ModItems.HYPER_REFINED_CRYSTAL));

        // --- Equipement ---
        //
        // Le Codex decrivait les vingt-trois machines et pas une seule piece
        // d'equipement. Ca se voyait surtout sur la Ward Plate : elle est la SEULE
        // chose du mod qui annule les degats de phase du Gardien, et rien en jeu ne le
        // disait. Un joueur pouvait donc l'avoir dans un coffre sans savoir qu'elle
        // etait la reponse au combat qu'il perdait.
        add("gear/tools", CodexCategory.GEAR, ModItems.VESKORIAN_ALLOY_PICKAXE,
            CodexUnlock.item(ModItems.VESKORIAN_ALLOY_INGOT));
        add("gear/armor", CodexCategory.GEAR, ModItems.VESKORIAN_ALLOY_CHESTPLATE,
            CodexUnlock.item(ModItems.VESKORIAN_ALLOY_INGOT));
        add("gear/ward_plate", CodexCategory.GEAR, ModItems.RIFT_WARD_PLATE,
            CodexUnlock.item(ModItems.RIFT_ESSENCE));

        // --- Lore (réutilise le texte des fragments, débloqué à la lecture) ---
        add(CodexEntries.DAILY_LIFE_LAMPS, CodexCategory.LORE, ModItems.CODEX_FRAGMENT,
            CodexUnlock.fragment());
        add(CodexEntries.DAILY_LIFE_RATION, CodexCategory.LORE, ModItems.CODEX_FRAGMENT,
            CodexUnlock.fragment());
        add(CodexEntries.DAILY_LIFE_MARKET, CodexCategory.LORE, ModItems.CODEX_FRAGMENT,
            CodexUnlock.fragment());
        add(CodexEntries.DAILY_LIFE_CHILDREN, CodexCategory.LORE, ModItems.CODEX_FRAGMENT,
            CodexUnlock.fragment());
        add(CodexEntries.DAILY_LIFE_FESTIVAL, CodexCategory.LORE, ModItems.CODEX_FRAGMENT,
            CodexUnlock.fragment());
        add(CodexEntries.CUSTODE_WATCH, CodexCategory.LORE, ModItems.CODEX_FRAGMENT,
            CodexUnlock.fragment());
        add(CodexEntries.HINT_WORKSHOP, CodexCategory.LORE, ModItems.CODEX_FRAGMENT,
            CodexUnlock.fragment());

        // --- T3 : l'alliage, le déchet, la portée -----------------------------
        //
        // Le Codex s'arrêtait ici, au T2. Le mod, lui, va jusqu'au T5 : un joueur qui
        // finissait la partie n'avait jamais lu une ligne sur la Forge, la Faille ou le
        // Gardien. Un manuel qui couvre le tiers du jeu n'aide qu'au premier tiers.
        add("machines/forge", CodexCategory.MACHINES, ModBlocks.VESKORIAN_ALLOY_FORGE,
            CodexUnlock.item(ModBlocks.VESKORIAN_ALLOY_FORGE));
        add("machines/relay", CodexCategory.MACHINES, ModBlocks.RESONANCE_RELAY,
            CodexUnlock.item(ModBlocks.RESONANCE_RELAY));
        add("machines/synthesizer", CodexCategory.MACHINES, ModBlocks.STRUCTURAL_SYNTHESIZER,
            CodexUnlock.item(ModBlocks.STRUCTURAL_SYNTHESIZER));
        add("machines/driller", CodexCategory.MACHINES, ModBlocks.DEEP_CRYSTAL_DRILLER,
            CodexUnlock.item(ModBlocks.DEEP_CRYSTAL_DRILLER));
        add("machines/slag_vent", CodexCategory.MACHINES, ModBlocks.SLAG_VENT,
            CodexUnlock.item(ModBlocks.SLAG_VENT));
        add("machines/compressor", CodexCategory.MACHINES, ModBlocks.FLUX_COMPRESSOR,
            CodexUnlock.item(ModBlocks.FLUX_COMPRESSOR));
        add("crystals/alloy", CodexCategory.CRYSTALS, ModItems.VESKORIAN_ALLOY_INGOT,
            CodexUnlock.item(ModItems.VESKORIAN_ALLOY_INGOT));
        add("crystals/slag", CodexCategory.CRYSTALS, ModItems.FLUX_SLAG,
            CodexUnlock.item(ModItems.FLUX_SLAG));

        // --- T4 : le réseau régional ------------------------------------------
        add("machines/chamber", CodexCategory.MACHINES, ModBlocks.DEEP_SYNTHESIS_CHAMBER,
            CodexUnlock.item(ModBlocks.DEEP_SYNTHESIS_CHAMBER));
        add("machines/amplifier", CodexCategory.MACHINES, ModBlocks.HARMONIC_AMPLIFIER,
            CodexUnlock.item(ModBlocks.HARMONIC_AMPLIFIER));
        add("machines/hub", CodexCategory.MACHINES, ModBlocks.RESONANCE_NETWORK_HUB,
            CodexUnlock.item(ModBlocks.RESONANCE_NETWORK_HUB));
        add("machines/extraction_array", CodexCategory.MACHINES, ModBlocks.AUTOMATED_EXTRACTION_ARRAY,
            CodexUnlock.item(ModBlocks.AUTOMATED_EXTRACTION_ARRAY));
        add("machines/convergence_core", CodexCategory.MACHINES, ModBlocks.CONVERGENCE_CORE,
            CodexUnlock.item(ModBlocks.CONVERGENCE_CORE));
        add("crystals/hyper", CodexCategory.CRYSTALS, ModItems.HYPER_REFINED_CRYSTAL,
            CodexUnlock.item(ModItems.HYPER_REFINED_CRYSTAL));
        add("fields/lattice", CodexCategory.FIELDS, ModItems.HARMONIC_LATTICE,
            CodexUnlock.item(ModItems.HARMONIC_LATTICE));
        add("fields/calibration", CodexCategory.FIELDS, ModItems.RESONANCE_TUNER,
            CodexUnlock.item(ModBlocks.HARMONIC_AMPLIFIER));

        // --- T5 : la Faille -----------------------------------------------------
        add("world/rift", CodexCategory.WORLD, ModBlocks.DEFORMED_STONE,
            CodexUnlock.item(ModBlocks.DEFORMED_STONE));
        add("machines/rift_anchor", CodexCategory.MACHINES, ModBlocks.RIFT_ANCHOR,
            CodexUnlock.item(ModBlocks.RIFT_ANCHOR));
        add("machines/rift_extractor", CodexCategory.MACHINES, ModBlocks.RIFT_CORE_EXTRACTOR,
            CodexUnlock.item(ModBlocks.RIFT_CORE_EXTRACTOR));
        add("machines/rift_ward", CodexCategory.MACHINES, ModBlocks.RIFT_WARD_EMITTER,
            CodexUnlock.item(ModBlocks.RIFT_WARD_EMITTER));
        add("fauna/guardian", CodexCategory.FAUNA, ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT,
            CodexUnlock.item(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT));
        add("crystals/essence", CodexCategory.CRYSTALS, ModItems.RIFT_ESSENCE,
            CodexUnlock.item(ModItems.RIFT_ESSENCE));

        // --- Progression (paliers) ---
        add("progression/tier1", CodexCategory.PROGRESSION, ModItems.RAW_RESONANCE_CRYSTAL,
            CodexUnlock.advancement(adv("tier1_awakening")));
        add("progression/tier2", CodexCategory.PROGRESSION, ModItems.RESONANCE_BLUEPRINT,
            CodexUnlock.advancement(adv("tier2_field")));
        add("progression/tier3", CodexCategory.PROGRESSION, ModItems.VESKORIAN_ALLOY_INGOT,
            CodexUnlock.item(ModBlocks.VESKORIAN_CHASSIS));
        add("progression/tier4", CodexCategory.PROGRESSION, ModItems.HYPER_REFINED_CRYSTAL,
            CodexUnlock.item(ModItems.HYPER_REFINED_CRYSTAL));
        add("progression/tier5", CodexCategory.PROGRESSION, ModItems.RIFT_ESSENCE,
            CodexUnlock.item(ModBlocks.DEFORMED_STONE));
    }

    private CodexRegistry() {
    }

    public static List<CodexEntry> all() {
        return ENTRIES;
    }

    public static List<CodexEntry> byCategory(CodexCategory category) {
        List<CodexEntry> out = new ArrayList<>();
        for (CodexEntry entry : ENTRIES) {
            if (entry.category() == category) {
                out.add(entry);
            }
        }
        return out;
    }

    /**
     * L'entrée qui suit celle-ci dans <b>sa catégorie</b>, ou {@code null} si c'est la
     * dernière.
     *
     * <p>C'est ce qui transforme le Codex d'un dictionnaire en parcours : arrivé au bas
     * d'une page, on n'a pas à se demander où aller ensuite, et surtout on découvre des
     * entrées qu'on n'aurait pas cherchées. L'ordre d'une catégorie est l'ordre
     * d'écriture du registre — il est déjà rédigé dans l'ordre où on apprend.
     */
    @Nullable
    public static CodexEntry next(CodexEntry entry) {
        List<CodexEntry> siblings = byCategory(entry.category());
        int index = siblings.indexOf(entry);
        return index >= 0 && index + 1 < siblings.size() ? siblings.get(index + 1) : null;
    }

    @Nullable
    public static CodexEntry get(ResourceLocation id) {
        for (CodexEntry entry : ENTRIES) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    private static void add(String path, CodexCategory category,
                            java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike> icon,
                            CodexUnlock unlock) {
        add(id(path), category, icon, unlock);
    }

    private static void add(ResourceLocation id, CodexCategory category,
                            java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike> icon,
                            CodexUnlock unlock) {
        ENTRIES.add(new CodexEntry(id, category, icon, unlock, TIERS.getOrDefault(id.getPath(), -1)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }

    private static ResourceLocation adv(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
