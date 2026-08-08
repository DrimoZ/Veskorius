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

    /**
     * <b>Matrice de Résonance</b> — la pièce intermédiaire du T3, et la seule dont
     * l'unique raison d'être soit d'ajouter une étape.
     *
     * <p>Le dossier la veut « requise par les machines T4 » (04-Materials.md,
     * 05-Machines.md). Sans elle, on passait des Composants de Résonance — une pièce
     * T1 — directement dans les machines du palier le plus haut : la chaîne sautait
     * deux paliers d'un coup, et le T3 n'apportait rien à ce qu'on bâtissait ensuite.
     *
     * <p>Elle se compose à l'Advanced Assembler, de Composants et d'alliage
     * <b>conducteur</b>. La branche de métal choisie à la Forge se paie donc une fois
     * de plus — c'est la troisième, après le Relais et le Treillis Harmonique.
     */
    public static final DeferredItem<Item> RESONANCE_MATRIX =
        ITEMS.registerSimpleItem("resonance_matrix", new Item.Properties().stacksTo(64));

    /** Flux concentré : carburant du Damping Array, et brique du Convergence Core (T5). */
    public static final DeferredItem<Item> CONCENTRATED_FLUX =
        ITEMS.registerSimpleItem("concentrated_flux", new Item.Properties().stacksTo(64));

    // --- Materiaux T4 (04-Materials.md) -------------------------------------

    /**
     * Quatrieme etat du cristal. <b>Il n'est pas minable</b> : la Deep Synthesis Chamber
     * est la seule source, et la Chambre elle-meme en consomme un a la construction. Les
     * trois exemplaires de l'Archive Regionale sont donc le seul stock de depart du
     * palier, et le choix « premier Amplificateur ou production perenne ? » se joue
     * dessus (05-Machines.md, « Bootstrap du T4 »).
     */
    public static final DeferredItem<Item> HYPER_REFINED_CRYSTAL =
        ITEMS.registerSimpleItem("hyper_refined_crystal", new Item.Properties().stacksTo(64));

    /**
     * <b>Treillis harmonique</b> : 4 lingots CONDUCTEURS + 2 Hyper Refined. C'est la piece
     * qui fait exister le T4 en tant que palier de reseau — Harmonic Amplifier et
     * Convergence Core en dependent tous les deux, et rien d'autre ne s'en sert. Le lingot
     * conducteur, pas le structurel : la branche de metal choisie a la Forge se paie une
     * seconde fois, un palier plus tard.
     */
    public static final DeferredItem<Item> HARMONIC_LATTICE =
        ITEMS.registerSimpleItem("harmonic_lattice", new Item.Properties().stacksTo(64));

    // --- Materiaux T5 (04-Materials.md) -------------------------------------

    /**
     * <b>Essence de Faille</b> — la <b>seule ressource volontairement finie du mod</b>.
     * Six par Faille, et la Faille est morte. Le dossier a explicitement rejete toute
     * machine de regeneration : ce qui rend la fin satisfaisante n'est pas qu'elle soit
     * riche, c'est qu'elle soit COMPTEE.
     */
    public static final DeferredItem<Item> RIFT_ESSENCE =
        ITEMS.registerSimpleItem("rift_essence", new Item.Properties().stacksTo(16));

    /**
     * <b>Alliage corrompu</b> : ce que la Faille rend d'un metal veskorien. Prime d'une
     * extraction sur sept, ou butin garanti du Gardien. Materiau du Rift-Ward Plate.
     */
    public static final DeferredItem<Item> CORRUPTED_VESKORIAN_ALLOY_INGOT =
        ITEMS.registerSimpleItem("corrupted_veskorian_alloy_ingot", new Item.Properties().stacksTo(64));

    // --- Outils et armure (04-Materials.md) ---------------------------------
    //
    // Le dossier ne nomme QUE l'épée et la pioche. Pas de hache, de pelle ni de houe :
    // ce ne sont pas des oublis à combler ici, c'est ce que le dossier prévoit. Les
    // ajouter serait décider à sa place.

    /** Dégâts du diamant, durabilité +20 % (04-Materials.md). */
    public static final DeferredItem<net.minecraft.world.item.SwordItem> VESKORIAN_ALLOY_SWORD =
        ITEMS.registerItem("veskorian_alloy_sword",
            props -> new net.minecraft.world.item.SwordItem(ModTiers.VESKORIAN_ALLOY,
                props.attributes(net.minecraft.world.item.SwordItem.createAttributes(
                    ModTiers.VESKORIAN_ALLOY, 3, -2.4f))),
            new Item.Properties());

    /**
     * Niveau netherite. Le dossier lui réserve un rôle de plus — miner
     * l'{@code ancient_conduit_stone} sans le détruire — mais ce bloc n'existe pas encore :
     * la pioche est prête, sa cible viendra.
     */
    public static final DeferredItem<net.minecraft.world.item.PickaxeItem> VESKORIAN_ALLOY_PICKAXE =
        ITEMS.registerItem("veskorian_alloy_pickaxe",
            props -> new net.minecraft.world.item.PickaxeItem(ModTiers.VESKORIAN_ALLOY,
                props.attributes(net.minecraft.world.item.PickaxeItem.createAttributes(
                    ModTiers.VESKORIAN_ALLOY, 1, -2.8f))),
            new Item.Properties());

    public static final DeferredItem<net.minecraft.world.item.ArmorItem> VESKORIAN_ALLOY_HELMET =
        armor("veskorian_alloy_helmet", net.minecraft.world.item.ArmorItem.Type.HELMET);
    public static final DeferredItem<net.minecraft.world.item.ArmorItem> VESKORIAN_ALLOY_CHESTPLATE =
        armor("veskorian_alloy_chestplate", net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<net.minecraft.world.item.ArmorItem> VESKORIAN_ALLOY_LEGGINGS =
        armor("veskorian_alloy_leggings", net.minecraft.world.item.ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<net.minecraft.world.item.ArmorItem> VESKORIAN_ALLOY_BOOTS =
        armor("veskorian_alloy_boots", net.minecraft.world.item.ArmorItem.Type.BOOTS);

    /**
     * <b>Rift-Ward Plate</b> — pièce UNIQUE, et le dossier explique pourquoi : une Faille
     * ne rend qu'environ quatre lingots corrompus en tout, si bien qu'une panoplie de
     * quatre pièces aurait exigé de vaincre trois ou quatre Gardiens pour un seul
     * équipement. Le plastron seul est calibré sur le butin garanti d'UNE Faille.
     *
     * <p>Il remplace le plastron d'alliage, il ne s'y ajoute pas : immunité totale au
     * déphasage, contre 10 % de vitesse de minage en moins tant qu'il est porté.
     */
    public static final DeferredItem<net.minecraft.world.item.ArmorItem> RIFT_WARD_PLATE =
        armor("rift_ward_plate", net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);

    private static DeferredItem<net.minecraft.world.item.ArmorItem> armor(
        String name, net.minecraft.world.item.ArmorItem.Type type) {
        return ITEMS.registerItem(name,
            props -> new net.minecraft.world.item.ArmorItem(ModTiers.ALLOY, type,
                props.durability(type.getDurability(ModTiers.ARMOR_DURABILITY))),
            new Item.Properties());
    }

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

    /** Œuf d'apparition du Custode Lourd. Acier sombre, veine sourde. */
    public static final DeferredItem<DeferredSpawnEggItem> CUSTODE_LOURD_SPAWN_EGG =
        ITEMS.registerItem("custode_lourd_spawn_egg",
            props -> new DeferredSpawnEggItem(ModEntities.CUSTODE_LOURD, 0x2E323C, 0x5C2C86, props),
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
    /**
     * <b>Graine Ancienne</b> — butin bonus de l'Archive Régionale, une fois sur cinq.
     *
     * <p>Un {@code ItemNameBlockItem} et non un item ordinaire : c'est ce qui la rend
     * PLANTABLE tout en gardant son propre nom, exactement comme les graines de blé. Sans
     * ça il aurait fallu un objet nommé « buisson de floraison » qu'on sème, ce qui dirait
     * au joueur qu'il pose un bloc au lieu de semer une graine.
     */
    public static final DeferredItem<net.minecraft.world.item.ItemNameBlockItem> ANCIENT_SEED =
        ITEMS.registerItem("ancient_seed",
            props -> new net.minecraft.world.item.ItemNameBlockItem(
                ModBlocks.RESONANCE_BLOOM_BUSH.get(), props),
            new Item.Properties());

    /**
     * <b>Floraison de Résonance</b> — se mange, ou se broie en {@code luminous_extract}.
     *
     * <p>Elle donne la VISION NOCTURNE une minute, et pas l'effet Lueur. Le dossier écrit
     * « effet Lueur ~ Vision Nocturne faible » : Lueur, dans Minecraft, dessine un contour
     * autour des entités et n'aide en rien à voir dans le noir. C'est l'intention qui a
     * été suivie — une plante qui éclaire celui qui la mange — plutôt que le nom.
     */
    public static final DeferredItem<Item> RESONANCE_BLOOM =
        ITEMS.registerSimpleItem("resonance_bloom", new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1f)
                .effect(() -> new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.NIGHT_VISION, 1200), 1.0f)
                .alwaysEdible().build()));

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

    /** Sable de Résonance : l'étape intermédiaire du verre (voir ModBlocks). */
    public static final DeferredItem<BlockItem> RESONANCE_SAND_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_SAND);

    /** Verre de Résonance : le seul bloc purement décoratif du mod. */
    public static final DeferredItem<BlockItem> RESONANCE_GLASS_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_GLASS);

    /** Résidu compressé — le seul exutoire du déchet du Synthesizer. */
    public static final DeferredItem<BlockItem> SYNTHESIS_RESIDUE_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.SYNTHESIS_RESIDUE_BLOCK);

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

    public static final DeferredItem<BlockItem> RIFT_CORE_EXTRACTOR_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RIFT_CORE_EXTRACTOR);

    public static final DeferredItem<BlockItem> RIFT_WARD_EMITTER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RIFT_WARD_EMITTER);

    public static final DeferredItem<BlockItem> DEFORMED_STONE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.DEFORMED_STONE);

    public static final DeferredItem<BlockItem> RIFT_ANCHOR_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RIFT_ANCHOR);

    public static final DeferredItem<BlockItem> CONVERGENCE_CORE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.CONVERGENCE_CORE);

    public static final DeferredItem<BlockItem> AUTOMATED_EXTRACTION_ARRAY_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.AUTOMATED_EXTRACTION_ARRAY);

    public static final DeferredItem<BlockItem> RESONANCE_NETWORK_HUB_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_NETWORK_HUB);

    public static final DeferredItem<BlockItem> DEEP_SYNTHESIS_CHAMBER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.DEEP_SYNTHESIS_CHAMBER);

    public static final DeferredItem<BlockItem> HARMONIC_AMPLIFIER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.HARMONIC_AMPLIFIER);

    public static final DeferredItem<BlockItem> FLUX_COMPRESSOR_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.FLUX_COMPRESSOR);
    public static final DeferredItem<BlockItem> RECLAIMER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RECLAIMER);

    public static final DeferredItem<BlockItem> ADVANCED_ASSEMBLER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.ADVANCED_ASSEMBLER);

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
