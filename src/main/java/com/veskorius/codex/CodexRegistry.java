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

        // --- Progression (paliers) ---
        add("progression/tier1", CodexCategory.PROGRESSION, ModItems.RAW_RESONANCE_CRYSTAL,
            CodexUnlock.advancement(adv("tier1_awakening")));
        add("progression/tier2", CodexCategory.PROGRESSION, ModItems.RESONANCE_BLUEPRINT,
            CodexUnlock.advancement(adv("tier2_field")));
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
        ENTRIES.add(new CodexEntry(id, category, icon, unlock));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }

    private static ResourceLocation adv(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
