package com.veskorius.item;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.entity.ModEntities;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Items de la chaine de raffinage principale (voir veskorius-design/04-Materials.md,
 * groupe 1) : Raw -> Stable -> Refined, plus le Resonance Component consomme par
 * les machines a partir du T1.
 *
 * NB : c'est bien {@code DeferredRegister.createItems} et non
 * {@code DeferredRegister.create(BuiltInRegistries.ITEM, ...)} — seule la
 * sous-classe {@link DeferredRegister.Items} expose les helpers
 * {@code registerSimpleItem} / {@code registerSimpleBlockItem}.
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Veskorius.MOD_ID);

    public static final DeferredItem<Item> RAW_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("raw_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> STABLE_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("stable_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> REFINED_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("refined_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> RESONANCE_COMPONENT =
        ITEMS.registerSimpleItem("resonance_component", new Item.Properties().stacksTo(64));

    /**
     * Poussière de résonance produite par le Crystal Crusher (1 Raw Crystal →
     * 3 Resonance Dust, voir 04-Materials.md et 05-Machines.md #22). Voie T1
     * alternative au Stabilizer (plus rapide, mais pas de Stable Crystal) ;
     * carburant de croissance et entrée de la branche alternative de l'Assembler.
     */
    public static final DeferredItem<Item> RESONANCE_DUST =
        ITEMS.registerSimpleItem("resonance_dust", new Item.Properties().stacksTo(64));

    /**
     * Flux brut obtenu en brossant un dépôt (voir 04-Materials.md). Membre du tag
     * {@code stabilizer_flux} : chemin T1 alternatif au Quartz (1:1) fondé sur
     * l'observation plutôt que le minage.
     */
    public static final DeferredItem<Item> RAW_FLUX_DEPOSIT =
        ITEMS.registerSimpleItem("raw_flux_deposit", new Item.Properties().stacksTo(64));

    /**
     * Augment transversal (05-Machines.md, 04-Materials.md groupe 4). S'insère dans
     * un slot d'augment de n'importe quelle machine active : +15% de vitesse de cycle en
     * permanence, jamais consommé. Le nombre de slots et le cumul d'un même effet sont
     * réglables en config depuis A9 ({@code machines.augment}, défaut 1 slot). Membre du tag
     * {@code veskorius:machine_augments} — l'effet est déjà porté par le socle
     * ({@code AbstractMachineBlockEntity}), cet item ne fait que remplir le tag.
     */
    public static final DeferredItem<Item> RESONANCE_CATALYST_CORE =
        ITEMS.registerSimpleItem("resonance_catalyst_core", new Item.Properties().stacksTo(64));

    /**
     * Dissonance <b>cristallisée</b> (04-Materials.md, 06-Energy.md) : ce que le Damping
     * Array extrait d'un champ pollué. Ce n'est pas un rebut anodin — c'est la substance
     * même de l'Effondrement. Ses débouchés (Reclaimer, engrais) arrivent en Phase 2.
     */
    public static final DeferredItem<Item> RESONANCE_SLUDGE =
        ITEMS.registerSimpleItem("resonance_sludge", new Item.Properties().stacksTo(64));

    /**
     * Spore de résonance (04-Materials.md, 09-Entities.md) : nourriture de
     * reproduction du Fileur de Cristal. Sa récolte est implémentée comme un état
     * {@code spored} sur le Resonance Veined Stone (pousse en faible luminosité sur
     * une face exposée, récolte au clic droit) — voir {@code ResonanceVeinedStoneBlock}.
     */
    public static final DeferredItem<Item> RESONANCE_SPORE =
        ITEMS.registerSimpleItem("resonance_spore", new Item.Properties().stacksTo(64));

    // --- Matériaux T3 (04-Materials.md, 05-Machines.md) ---------------------
    //
    // La première matière que le joueur FABRIQUE au lieu de la raffiner : la chaîne
    // T1-T2 partait du cristal et le purifiait, celle-ci part du métal et l'allie.
    // C'est le changement de nature qui marque le palier, pas le nombre d'étapes.

    /** Alliage veskorien : substrat structurel de tout le T3+ (Veskorian Alloy Forge). */
    public static final DeferredItem<Item> VESKORIAN_ALLOY_INGOT =
        ITEMS.registerSimpleItem("veskorian_alloy_ingot", new Item.Properties().stacksTo(64));

    /**
     * Variante <b>conductrice</b> : même forge, or au lieu de fer. Elle seule permet
     * le Resonance Relay — la branche du métal décide donc de ce qu'on peut bâtir, et
     * c'est un vrai choix de planification (04-Materials.md).
     */
    public static final DeferredItem<Item> VESKORIAN_CONDUCTIVE_ALLOY_INGOT =
        ITEMS.registerSimpleItem("veskorian_conductive_alloy_ingot", new Item.Properties().stacksTo(64));

    /**
     * <b>Scorie de flux</b> : le déchet de la Forge. Ce n'est pas un rebut anodin —
     * c'est chimiquement la substance qui, accumulée à l'échelle régionale, a déclenché
     * l'Effondrement (02-Lore.md). Le joueur reproduit la cause en miniature.
     */
    public static final DeferredItem<Item> FLUX_SLAG =
        ITEMS.registerSimpleItem("flux_slag", new Item.Properties().stacksTo(64));

    /** Résidu du Structural Synthesizer. Débouchés en T4 (Reclaimer, voir 16 §5). */
    public static final DeferredItem<Item> SYNTHESIS_RESIDUE =
        ITEMS.registerSimpleItem("synthesis_residue", new Item.Properties().stacksTo(64));

    /** Flux concentré : carburant du Damping Array, et brique du Convergence Core (T5). */
    public static final DeferredItem<Item> CONCENTRATED_FLUX =
        ITEMS.registerSimpleItem("concentrated_flux", new Item.Properties().stacksTo(64));

    /** Œuf d'apparition du Fileur de Cristal. */
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTAL_STRIDER_SPAWN_EGG =
        ITEMS.registerItem("crystal_strider_spawn_egg",
            props -> new DeferredSpawnEggItem(ModEntities.CRYSTAL_STRIDER, 0x6A3FA0, 0xB58AD6, props),
            new Item.Properties());

    /**
     * Fragment d'alliage lâché par le Custode (04-Materials.md, 09-Entities.md) :
     * substitut 1:1 du lingot de fer dans les recettes Veskorius (via le tag
     * {@code veskorius:iron_substitutes}) — récompense le combat plutôt que le minage.
     */
    public static final DeferredItem<Item> CUSTODE_ALLOY_FRAGMENT =
        ITEMS.registerSimpleItem("custode_alloy_fragment", new Item.Properties().stacksTo(64));

    /** Œuf d'apparition du Custode. */
    public static final DeferredItem<DeferredSpawnEggItem> CUSTODE_SPAWN_EGG =
        ITEMS.registerItem("custode_spawn_egg",
            props -> new DeferredSpawnEggItem(ModEntities.CUSTODE, 0x3A3F4A, 0x8C6A2F, props),
            new Item.Properties());

    // --- Progression : plans, fragments, loot de structure (tâche 10) --------

    /**
     * Clé de craft physique d'un tier (03-Progression.md). Requise et **rendue** dans
     * les recettes du tier. Non empilable (artefact unique qu'on garde).
     */
    public static final DeferredItem<ResonanceBlueprintItem> RESONANCE_BLUEPRINT =
        ITEMS.registerItem("resonance_blueprint",
            ResonanceBlueprintItem::new, new Item.Properties().stacksTo(1));

    /** Fragment de Codex : lore lisible, ne débloque rien (08-Structures.md). */
    public static final DeferredItem<CodexFragmentItem> CODEX_FRAGMENT =
        ITEMS.registerItem("codex_fragment",
            CodexFragmentItem::new, new Item.Properties().stacksTo(16));

    /** Nourriture ancienne « fossilisée » (flavor, Habitation Modeste). */
    public static final DeferredItem<Item> FOSSILIZED_RATION =
        ITEMS.registerSimpleItem("fossilized_ration", new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.2f).build()));

    /**
     * Le Codex de Résonance (15-Codex-Guidebook.md) : manuel en jeu qui s'écrit tout
     * seul. Non empilable (c'est un artefact qu'on garde, pas un consommable) ; son état de
     * déblocage vit sur le JOUEUR (attachment {@code codex_unlocks}), pas sur l'objet.
     * Donné à la première connexion.
     */
    public static final DeferredItem<ResonanceCodexItem> RESONANCE_CODEX =
        ITEMS.registerItem("resonance_codex",
            ResonanceCodexItem::new, new Item.Properties().stacksTo(1));

    /** Outil transversal de configuration des machines (05-Machines.md). */
    public static final DeferredItem<ResonanceTunerItem> RESONANCE_TUNER =
        ITEMS.registerItem("resonance_tuner",
            ResonanceTunerItem::new, new Item.Properties().stacksTo(1));

    /**
     * Batterie portable d'Osc (05-Machines.md #6, 06-Energy.md). Non empilable :
     * chaque cellule porte son propre état de charge. Se recharge dans un champ,
     * alimente le Resonance Locator (tâche 8).
     */
    public static final DeferredItem<ResonanceStorageCellItem> RESONANCE_STORAGE_CELL =
        ITEMS.registerItem("resonance_storage_cell",
            ResonanceStorageCellItem::new, new Item.Properties().stacksTo(1));

    /**
     * Détecteur de résonance à courte portée (05-Machines.md #7, 06-Energy.md).
     * Batterie interne, recharge par champ ou Storage Cell. Ping directionnel.
     */
    public static final DeferredItem<ResonanceLocatorItem> RESONANCE_LOCATOR =
        ITEMS.registerItem("resonance_locator",
            ResonanceLocatorItem::new, new Item.Properties().stacksTo(1));

    /**
     * Châssis de palier : la base de craft ET de texture de toutes les machines de leur
     * palier (voir {@link ModBlocks#FRACTURED_CHASSIS}).
     */
    public static final DeferredItem<BlockItem> FRACTURED_CHASSIS_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.FRACTURED_CHASSIS);

    public static final DeferredItem<BlockItem> ATTUNED_CHASSIS_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.ATTUNED_CHASSIS);

    public static final DeferredItem<BlockItem> VESKORIAN_CHASSIS_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VESKORIAN_CHASSIS);

    // BlockItems des machines (voir ModBlocks.java)
    public static final DeferredItem<BlockItem> RESONANCE_STABILIZER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_STABILIZER);

    public static final DeferredItem<BlockItem> COMPONENT_ASSEMBLER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.COMPONENT_ASSEMBLER);

    public static final DeferredItem<BlockItem> RESONANCE_WHETSTONE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_WHETSTONE);

    public static final DeferredItem<BlockItem> FLUX_PURIFIER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.FLUX_PURIFIER);

    public static final DeferredItem<BlockItem> FIELD_EMITTER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.FIELD_EMITTER);

    public static final DeferredItem<BlockItem> TUNABLE_FIELD_EMITTER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.TUNABLE_FIELD_EMITTER);

    public static final DeferredItem<BlockItem> RESONANCE_CRYSTAL_CLUSTER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_CRYSTAL_CLUSTER);

    public static final DeferredItem<BlockItem> RESONANCE_VEINED_STONE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_VEINED_STONE);

    public static final DeferredItem<BlockItem> CRYSTAL_CRUSHER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.CRYSTAL_CRUSHER);

    public static final DeferredItem<BlockItem> DAMPING_ARRAY_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.DAMPING_ARRAY);

    public static final DeferredItem<BlockItem> CRYSTAL_ROOST_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.CRYSTAL_ROOST);

    // BlockItems de l'architecture de donjon (17-Dungeons.md §4). Ce qu'on trouve en
    // ruine, on doit pouvoir le rebâtir — d'où un objet pour toute la maçonnerie, la
    // lampe et le conduit. Le SAS et l'ÉMETTEUR ANCIEN n'en ont volontairement pas :
    // ce sont des pièces de structure, pas du mobilier (voir ModBlocks).
    public static final DeferredItem<BlockItem> VEINED_STONE_BRICKS_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VEINED_STONE_BRICKS);

    public static final DeferredItem<BlockItem> CRACKED_VEINED_STONE_BRICKS_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.CRACKED_VEINED_STONE_BRICKS);

    public static final DeferredItem<BlockItem> CHISELED_VEINED_STONE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.CHISELED_VEINED_STONE);

    public static final DeferredItem<BlockItem> VEINED_STONE_BRICK_STAIRS_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VEINED_STONE_BRICK_STAIRS);

    public static final DeferredItem<BlockItem> VEINED_STONE_BRICK_SLAB_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VEINED_STONE_BRICK_SLAB);

    public static final DeferredItem<BlockItem> VEINED_STONE_BRICK_WALL_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VEINED_STONE_BRICK_WALL);

    public static final DeferredItem<BlockItem> RESONANCE_LAMP_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_LAMP);

    public static final DeferredItem<BlockItem> CONDUIT_LINE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.CONDUIT_LINE);

    public static final DeferredItem<BlockItem> DISSONANCE_BLOOM_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.DISSONANCE_BLOOM);

    public static final DeferredItem<BlockItem> VESKORIAN_ALLOY_FORGE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VESKORIAN_ALLOY_FORGE);

    public static final DeferredItem<BlockItem> RESONANCE_RELAY_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_RELAY);

    public static final DeferredItem<BlockItem> FLUX_COMPRESSOR_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.FLUX_COMPRESSOR);

    public static final DeferredItem<BlockItem> STRUCTURAL_SYNTHESIZER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.STRUCTURAL_SYNTHESIZER);

    public static final DeferredItem<BlockItem> DEEP_CRYSTAL_DRILLER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.DEEP_CRYSTAL_DRILLER);

    public static final DeferredItem<BlockItem> SLAG_VENT_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.SLAG_VENT);

    public static final DeferredItem<BlockItem> VESKORIAN_ALLOY_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VESKORIAN_ALLOY_BLOCK);

    public static final DeferredItem<BlockItem> VEINED_STONE_COLUMN_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.VEINED_STONE_COLUMN);
}
