package com.veskorius.block;

import com.veskorius.Veskorius;
import java.util.function.ToIntFunction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Blocs du mod. Voir veskorius-design/13-Registry-Index.md pour la liste des 23
 * machines prevues et leur statut.
 *
 * NB : c'est bien {@code DeferredRegister.createBlocks} — voir la note dans
 * {@code ModItems} pour la raison.
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Veskorius.MOD_ID);

    /**
     * Glow d'une machine active : rayonne un peu quand elle avance un cycle
     * ({@link AbstractMachineBlock#LIT}), noir à l'arrêt. Retour visuel « en marche »
     * commun aux machines actives — voir {@code AbstractMachineBlockEntity.setLit}.
     */
    private static final ToIntFunction<BlockState> MACHINE_GLOW =
        state -> state.getValue(AbstractMachineBlock.LIT) ? 7 : 0;

    /**
     * Glow d'un émetteur. Contrairement aux machines, il ne s'éteint pas complètement
     * à vide : un émetteur reste un objet manifestement sous tension, même à sec. La
     * différence de luminosité double le changement de façade — deux signaux pour la
     * même information, lisibles de loin comme de près.
     */
    private static final ToIntFunction<BlockState> EMITTER_GLOW =
        state -> state.getValue(FieldEmitterBlock.LIT) ? 9 : 2;

    /**
     * Châssis de palier — la <b>brique commune</b> des machines (05-Machines.md,
     * « Châssis par palier »). Trois raisons d'exister, dans cet ordre :
     *
     * <ol>
     *   <li><b>Craft</b> : chaque machine se fabrique désormais « châssis de son palier +
     *       ce qui la distingue ». La grammaire de fabrication devient lisible : on ne
     *       réapprend pas une recette entière par machine, on apprend un boîtier et une
     *       pièce.</li>
     *   <li><b>Lecture</b> : le châssis porte les textures de flanc et de dessus de
     *       toutes les machines de son palier. Le palier d'une machine se lit donc
     *       <b>sur le bloc</b>, à distance, sans GUI ni tooltip.</li>
     *   <li><b>Lore</b> : le T1 est de la ruine récupérée (pierre, cuivre patiné,
     *       gravures interrompues), le T2 du restauré qui conduit vraiment. La
     *       progression se voit dans la matière.</li>
     * </ol>
     *
     * Ils restent des blocs décoratifs à part entière — poser un mur de châssis est un
     * usage légitime.
     */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> FRACTURED_CHASSIS =
        BLOCKS.registerSimpleBlock("fractured_chassis",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());

    public static final DeferredBlock<net.minecraft.world.level.block.Block> ATTUNED_CHASSIS =
        BLOCKS.registerSimpleBlock("attuned_chassis",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());

    public static final DeferredBlock<net.minecraft.world.level.block.Block> VESKORIAN_CHASSIS =
        BLOCKS.registerSimpleBlock("veskorian_chassis",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(4.0f, 8.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());

    /** Machine #1 (05-Machines.md). Bloc actif : block entity + GUI. */
    public static final DeferredBlock<ResonanceStabilizerBlock> RESONANCE_STABILIZER =
        BLOCKS.registerBlock("resonance_stabilizer",
            ResonanceStabilizerBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops()
                // Silhouette propre (socle, creux, colonne) : plus un cube plein.
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #2 (05-Machines.md). Bloc actif : block entity + GUI, consomme des Osc. */
    public static final DeferredBlock<ComponentAssemblerBlock> COMPONENT_ASSEMBLER =
        BLOCKS.registerBlock("component_assembler",
            ComponentAssemblerBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                // Silhouette propre (socle, creux, colonne) : plus un cube plein.
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #3 (05-Machines.md). Bloc actif : block entity + GUI. */
    public static final DeferredBlock<ResonanceWhetstoneBlock> RESONANCE_WHETSTONE =
        BLOCKS.registerBlock("resonance_whetstone",
            ResonanceWhetstoneBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.5f, 6.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                // Silhouette propre (socle, creux, colonne) : plus un cube plein.
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /**
     * Pierre veinée qui enrobe les poches de cristal (07-World-Generation.md).
     * Bloc décoratif + « tell » visuel : le voir, c'est savoir qu'une poche est
     * proche. Ne rayonne pas — on le reconnaît à sa texture, pas à sa lumière.
     */
    public static final DeferredBlock<ResonanceVeinedStoneBlock> RESONANCE_VEINED_STONE =
        BLOCKS.registerBlock("resonance_veined_stone",
            ResonanceVeinedStoneBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(1.5f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .randomTicks()
                .requiresCorrectToolForDrops());

    /**
     * Croûte de flux brossable sur les parois des poches (07-World-Generation.md).
     * Bloc brossable façon sable/gravier suspect : le brosser révèle du Raw Flux
     * Deposit. Le MINER ne donne rien (la croûte se détruit) — voir le loot ; c'est
     * le comportement vanilla des blocs suspects, et ça honore la « collecte
     * silencieuse par observation » du design.
     *
     * Le bloc n'a pas d'objet (généré en monde uniquement, jamais posé). Il est
     * rattaché au type de block entity vanilla BRUSHABLE_BLOCK par
     * {@link com.veskorius.block.entity.ModBrushableBlocks}.
     */
    public static final DeferredBlock<BrushableBlock> RAW_FLUX_DEPOSIT =
        BLOCKS.registerBlock("raw_flux_deposit",
            props -> new BrushableBlock(net.minecraft.world.level.block.Blocks.STONE,
                SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, props),
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_PURPLE)
                .strength(0.6f)
                .sound(SoundType.GRAVEL)
                .requiresCorrectToolForDrops()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));

    /**
     * Bloc de poche de cristal brut (07-World-Generation.md). Se génère en petites
     * poches Y -20 à 0 et lâche du Raw Resonance Crystal. Légère luminosité : le
     * cristal instable « rayonne » et se repère une fois la paroi ouverte.
     */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> RESONANCE_CRYSTAL_CLUSTER =
        BLOCKS.registerSimpleBlock("resonance_crystal_cluster",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0f, 3.0f)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 5));

    /** Machine #5 (05-Machines.md). Bloc actif : block entity + GUI, surchauffe. */
    public static final DeferredBlock<FluxPurifierBlock> FLUX_PURIFIER =
        BLOCKS.registerBlock("flux_purifier",
            FluxPurifierBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                // Silhouette propre (socle, creux, colonne) : plus un cube plein.
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /**
     * Machine #22 (05-Machines.md). Bloc actif : block entity + GUI. Voie T1
     * alternative au Stabilizer (1 Raw Crystal → 3 Resonance Dust, 10 s, autonome).
     */
    public static final DeferredBlock<CrystalCrusherBlock> CRYSTAL_CRUSHER =
        BLOCKS.registerBlock("crystal_crusher",
            CrystalCrusherBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                // Silhouette propre (socle, creux, colonne) : plus un cube plein.
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /**
     * Console d'attunement de l'Avant-poste (08-Structures.md). Générée uniquement en
     * structure, sans objet (non récupérable). Clic droit sur place → blueprint T2.
     */
    public static final DeferredBlock<AttunementConsoleBlock> ATTUNEMENT_CONSOLE =
        BLOCKS.registerBlock("attunement_console",
            AttunementConsoleBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                // Pupitre incliné, donc PAS un cube plein : sans noOcclusion, Minecraft
                // culle les faces des blocs voisins en supposant qu'elles sont cachées,
                // et on voit à travers le monde par-dessus le socle.
                .noOcclusion()
                .lightLevel(state -> 3));

    /**
     * Machine #8 (05-Machines.md). Production passive de cristal brut : 2 Quartz →
     * 1 Raw Crystal (600 s), à condition qu'un Fileur de Cristal soit à proximité.
     */
    public static final DeferredBlock<CrystalRoostBlock> CRYSTAL_ROOST =
        BLOCKS.registerBlock("crystal_roost",
            CrystalRoostBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.5f, 3.0f)
                .sound(SoundType.WOOD)
                .requiresCorrectToolForDrops()
                // Silhouette propre (socle, creux, colonne) : plus un cube plein.
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /**
     * Damping Array (06-Energy.md) : absorbe la dissonance d'un champ, en consommant un
     * agent de damping et en cristallisant le déchet. Autonome (0 Osc) — voir la BE.
     */
    public static final DeferredBlock<DampingArrayBlock> DAMPING_ARRAY =
        BLOCKS.registerBlock("damping_array",
            DampingArrayBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                // Silhouette propre (socle, creux, colonne) : plus un cube plein.
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #4 (05-Machines.md). Bloc passif : fournit un champ de Résonance. */
    public static final DeferredBlock<FieldEmitterBlock> FIELD_EMITTER =
        BLOCKS.registerBlock("field_emitter",
            FieldEmitterBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                // Tour à étages, pas un cube : voir la note sur la console.
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    /**
     * Émetteur Accordable (06-Energy.md) : Field Emitter dont la **bande harmonique se
     * choisit** au Resonance Tuner. C'est lui qui introduit le choix de fréquence — donc
     * la possibilité de router l'énergie par bande, sans câbles.
     */
    public static final DeferredBlock<TunableFieldEmitterBlock> TUNABLE_FIELD_EMITTER =
        BLOCKS.registerBlock("tunable_field_emitter",
            TunableFieldEmitterBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    // =========================================================================
    // Architecture de donjon (17-Dungeons.md §4)
    // =========================================================================
    //
    // Ces blocs existent d'abord pour une raison mesurée : les pièces de structure
    // n'avaient que HUIT blockstates à leur disposition, dont un seul du mod. Une
    // civilisation de la Résonance bâtissait donc en deepslate poli vanilla, et
    // aucune ruine n'était reconnaissable comme veskorienne de l'intérieur. On ne
    // fait pas d'architecture sans vocabulaire ; le voici.
    //
    // Ils sont aussi tous posables par le joueur (sauf le sas et l'émetteur ancien,
    // qui sont du mobilier de structure) : ce qu'on trouve en ruine, on doit pouvoir
    // le rebâtir — c'est la vision du mod appliquée aux murs.

    /** Propriétés communes de la maçonnerie veskorienne : la même pierre, appareillée. */
    private static BlockBehaviour.Properties masonry() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.DEEPSLATE)
            .strength(2.0f, 6.0f)
            .sound(SoundType.DEEPSLATE)
            .requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<net.minecraft.world.level.block.Block> VEINED_STONE_BRICKS =
        BLOCKS.registerSimpleBlock("veined_stone_bricks", masonry());

    public static final DeferredBlock<net.minecraft.world.level.block.Block> CRACKED_VEINED_STONE_BRICKS =
        BLOCKS.registerSimpleBlock("cracked_veined_stone_bricks", masonry());

    /**
     * Le seul bloc « écrit » du vocabulaire. Sert de borne : une salle qui en porte est
     * une salle qui comptait pour eux. À n'employer qu'aux seuils et aux salles
     * maîtresses — s'il est partout, il ne signale plus rien.
     */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> CHISELED_VEINED_STONE =
        BLOCKS.registerSimpleBlock("chiseled_veined_stone", masonry());

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> VEINED_STONE_BRICK_STAIRS =
        BLOCKS.registerBlock("veined_stone_brick_stairs",
            props -> new net.minecraft.world.level.block.StairBlock(
                VEINED_STONE_BRICKS.get().defaultBlockState(), props),
            masonry());

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> VEINED_STONE_BRICK_SLAB =
        BLOCKS.registerBlock("veined_stone_brick_slab",
            net.minecraft.world.level.block.SlabBlock::new, masonry());

    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> VEINED_STONE_BRICK_WALL =
        BLOCKS.registerBlock("veined_stone_brick_wall",
            net.minecraft.world.level.block.WallBlock::new, masonry().forceSolidOn());

    /**
     * <b>Lampe de Résonance</b> — l'éclairage veskorien, et le retour visuel de la règle
     * R2 (17-Dungeons.md). Elle ne brûle rien : elle s'allume quand un champ actif la
     * couvre, et s'éteint sinon. Éteinte, elle éclaire tout de même faiblement (3) —
     * une ruine noire est une ruine qu'on ne visite pas, et le contraste avec l'état
     * allumé (14) reste largement lisible.
     *
     * <p>Elle remplace les lanternes qui décoraient les pièces jusqu'ici : une
     * civilisation de la Résonance ne s'éclaire pas à la bougie (pilier 1).
     */
    public static final DeferredBlock<FieldSensitiveBlock> RESONANCE_LAMP =
        BLOCKS.registerBlock("resonance_lamp",
            FieldSensitiveBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(1.5f, 6.0f)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(FieldSensitiveBlock.POWERED) ? 14 : 3));

    /**
     * <b>Conduit</b> — le fil d'Ariane du donjon (règle R2). Une gouttière prise dans la
     * maçonnerie, éteinte sur une branche morte, vivante sur une branche alimentée. Le
     * joueur suit la lumière, sans carte ni marqueur.
     */
    public static final DeferredBlock<ConduitLineBlock> CONDUIT_LINE =
        BLOCKS.registerBlock("conduit_line",
            ConduitLineBlock::new,
            masonry().lightLevel(state -> state.getValue(FieldSensitiveBlock.POWERED) ? 7 : 0));

    /**
     * <b>Colonne cannelée.</b> Le bloc qui fait les colonnades, donc celui qui fait les
     * monuments : c'est la <b>verticalité répétée</b> qui donne l'impression de hauteur,
     * bien plus que la hauteur réelle. Sans lui, une grande salle reste une grande boîte.
     */
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> VEINED_STONE_COLUMN =
        BLOCKS.registerBlock("veined_stone_column",
            net.minecraft.world.level.block.RotatedPillarBlock::new, masonry());

    /**
     * <b>Efflorescence de dissonance</b> (règle R3). Sans collision : on la traverse, et
     * la traverser blesse. Voir {@link DissonanceBloomBlock}.
     */
    public static final DeferredBlock<DissonanceBloomBlock> DISSONANCE_BLOOM =
        BLOCKS.registerBlock("dissonance_bloom",
            DissonanceBloomBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(0.4f)
                .sound(SoundType.SCULK)
                .noCollission()
                .noOcclusion());

    /**
     * <b>Sas de Résonance</b> — le verrou des donjons (règle R1). Indestructible par
     * conception : voir {@link ResonanceBulkheadBlock}. Généré uniquement en structure,
     * donc sans objet ni loot table.
     */
    public static final DeferredBlock<ResonanceBulkheadBlock> RESONANCE_BULKHEAD =
        BLOCKS.registerBlock("resonance_bulkhead",
            ResonanceBulkheadBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(-1.0f, 3600000.0f)
                .sound(SoundType.NETHERITE_BLOCK)
                .noLootTable()
                .noOcclusion()
                .lightLevel(state -> state.getValue(FieldSensitiveBlock.POWERED) ? 6 : 0));

    /**
     * <b>Console du Sigma Laboratory</b> : même geste, palier au-dessus. Elle rend le
     * blueprint T3 (03-Progression.md) et se génère derrière le sas que les deux relais
     * ouvrent — la récompense du seul vrai puzzle du mod.
     */
    /**
     * <b>Socle d'archive</b> : la serrure de la salle de lecture (voir
     * {@link ArchivePedestalBlock}). Généré en structure, sans objet — c'est un meuble de
     * lieu, pas du mobilier qu'on emporte.
     */
    /**
     * <b>Bloc d'alliage veskorien</b> (Structural Synthesizer, T3). Matériau de
     * construction du palier et brique des machines T5 — à la fois décoratif et
     * structurel, comme les châssis.
     */
    /**
     * Machine #10 (05-Machines.md). <b>Porte d'entrée du T3</b> : c'est elle qui produit
     * l'alliage dont dépend tout le palier — et la scorie dont il faut s'occuper.
     */
    public static final DeferredBlock<VeskorianAlloyForgeBlock> VESKORIAN_ALLOY_FORGE =
        BLOCKS.registerBlock("veskorian_alloy_forge",
            VeskorianAlloyForgeBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /**
     * Machine #9 (05-Machines.md). <b>La portée du réseau, et son seul coût continu.</b>
     * Même famille visuelle que les émetteurs (FACING + LIT), parce que c'en est un —
     * simplement un qui ne brûle rien et ne fait que répéter ce qu'il reçoit.
     */
    public static final DeferredBlock<ResonanceRelayBlock> RESONANCE_RELAY =
        BLOCKS.registerBlock("resonance_relay",
            ResonanceRelayBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                // Mât ajouré : sans ça, on voit à travers le monde par ses vides.
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    /**
    /**
     * <b>Pierre déformée</b> : la coquille d'une Faille, et son <b>seul signe
     * avant-coureur</b>. Une Faille est invisible au Locator ; la reconnaître est une
     * compétence du joueur, pas une capacité d'objet (07-World-Generation.md).
     */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> DEFORMED_STONE =
        BLOCKS.registerSimpleBlock("deformed_stone",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(2.0f, 8.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());

    /**
     * <b>Noyau de Faille</b>. Indestructible et sans objet : la ressource finale du mod
     * dépend de lui, et c'est ce qui la rend finie.
     */
    public static final DeferredBlock<RiftCoreBlock> RIFT_CORE =
        BLOCKS.registerBlock("rift_core",
            RiftCoreBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(-1.0f, 3600000.0f)
                .sound(SoundType.AMETHYST)
                .noLootTable()
                .noOcclusion()
                .lightLevel(state -> 10));

    /** Machine #19 : stabilise une Faille tant qu'elle est alimentée. 20 Osc/tick. */
    public static final DeferredBlock<RiftAnchorBlock> RIFT_ANCHOR =
        BLOCKS.registerBlock("rift_anchor",
            RiftAnchorBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(5.0f, 12.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    /**
     * Machine #18 : le seul multi-bloc du mod. Posé seul, il est inerte — il lui faut huit
     * relais ou amplificateurs à 5 blocs, tous en vue directe.
     */
    public static final DeferredBlock<ConvergenceCoreBlock> CONVERGENCE_CORE =
        BLOCKS.registerBlock("convergence_core",
            ConvergenceCoreBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(6.0f, 12.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    /** Machine #16 : commande les foreuses — elle ramasse, et elle les fait tourner double. */
    public static final DeferredBlock<AutomatedExtractionArrayBlock> AUTOMATED_EXTRACTION_ARRAY =
        BLOCKS.registerBlock("automated_extraction_array",
            AutomatedExtractionArrayBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(4.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #17 : passif. Décide qui s'arrête quand le champ ne suffit plus. */
    public static final DeferredBlock<ResonanceNetworkHubBlock> RESONANCE_NETWORK_HUB =
        BLOCKS.registerBlock("resonance_network_hub",
            ResonanceNetworkHubBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    /** Machine #15 : la seule source de Hyper Refined Crystal, donc la clé du T4. */
    public static final DeferredBlock<DeepSynthesisChamberBlock> DEEP_SYNTHESIS_CHAMBER =
        BLOCKS.registerBlock("deep_synthesis_chamber",
            DeepSynthesisChamberBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(4.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #14 : double la portée reçue, trois au maximum en chaîne. */
    public static final DeferredBlock<HarmonicAmplifierBlock> HARMONIC_AMPLIFIER =
        BLOCKS.registerBlock("harmonic_amplifier",
            HarmonicAmplifierBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(4.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    /** Machine #23 : condense quatre cristaux raffinés en un Flux Concentré. */
    public static final DeferredBlock<FluxCompressorBlock> FLUX_COMPRESSOR =
        BLOCKS.registerBlock("flux_compressor",
            FluxCompressorBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #11 : ce qui rend le palier bâtissable — le lingot devient un bloc. */
    public static final DeferredBlock<StructuralSynthesizerBlock> STRUCTURAL_SYNTHESIZER =
        BLOCKS.registerBlock("structural_synthesizer",
            StructuralSynthesizerBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #12 : la seule machine du mod qui modifie le monde. */
    public static final DeferredBlock<DeepCrystalDrillerBlock> DEEP_CRYSTAL_DRILLER =
        BLOCKS.registerBlock("deep_crystal_driller",
            DeepCrystalDrillerBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(4.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(MACHINE_GLOW));

    /** Machine #13 : passif. N'existe que parce que la Forge se bloque. */
    public static final DeferredBlock<SlagVentBlock> SLAG_VENT =
        BLOCKS.registerBlock("slag_vent",
            SlagVentBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(3.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));

    public static final DeferredBlock<net.minecraft.world.level.block.Block> VESKORIAN_ALLOY_BLOCK =
        BLOCKS.registerSimpleBlock("veskorian_alloy_block",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .strength(5.0f, 8.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());

    public static final DeferredBlock<ArchivePedestalBlock> ARCHIVE_PEDESTAL =
        BLOCKS.registerBlock("archive_pedestal",
            ArchivePedestalBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(-1.0f, 3600000.0f)
                .sound(SoundType.DEEPSLATE)
                .noLootTable()
                .noOcclusion()
                .lightLevel(state -> 2));

    /**
     * <b>Console de l'Archive Regionale</b> — elle rend le blueprint <b>T4</b>.
     *
     * <p>Elle manquait, et son absence etait un mur : la salle de lecture posait la console
     * du Sigma, qui rend le T3. L'Archive ne debloquait donc rien de plus que le donjon
     * precedent, et le T4 n'avait <b>aucune porte d'entree dans tout le mod</b> — un palier
     * entier decrit au dossier et inatteignable en jeu. Le commentaire de la piece annoncait
     * pourtant « la console rend le blueprint T4 » : la prose etait juste, le bloc pose ne
     * l'etait pas.
     */
    public static final DeferredBlock<AttunementConsoleBlock> ARCHIVE_CONSOLE =
        BLOCKS.registerBlock("archive_console",
            props -> new AttunementConsoleBlock(props, 4),
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .noLootTable()
                .lightLevel(state -> 4));

    public static final DeferredBlock<AttunementConsoleBlock> SIGMA_CONSOLE =
        BLOCKS.registerBlock("sigma_console",
            props -> new AttunementConsoleBlock(props, 3),
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .lightLevel(state -> 4));

    /**
     * <b>Relais endommagé</b> : le puzzle du Sigma. Réparé au Resonance Component, il
     * rediffuse la Résonance 90 secondes — voir {@link DamagedRelayBlock}. Indestructible
     * et sans objet : récupérable, il donnerait un relais T3 gratuit au milieu du donjon
     * dont c'est précisément la récompense.
     */
    public static final DeferredBlock<DamagedRelayBlock> DAMAGED_RELAY =
        BLOCKS.registerBlock("damaged_relay",
            DamagedRelayBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(-1.0f, 3600000.0f)
                .sound(SoundType.METAL)
                .noLootTable()
                .noOcclusion()
                .lightLevel(state -> state.getValue(DamagedRelayBlock.LIT) ? 11 : 2));

    /**
     * <b>Émetteur ancien</b> — l'émetteur de champ que les Veskoriens avaient laissé en
     * place, à sec. C'est <b>littéralement un {@link FieldEmitterBlock}</b> : même bloc,
     * même block entity, même carburant. Ce choix est le cœur du geste de l'Avant-poste
     * (17-Dungeons.md §5.1) :
     *
     * <ul>
     *   <li>le joueur <b>voit fonctionner</b>, avant de savoir le fabriquer, la machine
     *       exacte qu'il craftera au T2 — le pilier 2 (« la connaissance est spatiale »)
     *       appliqué à une machine et non à un texte ;</li>
     *   <li>le champ qu'il produit est un <b>vrai</b> champ : il ouvre le sas, allume les
     *       conduits et alimenterait une machine posée à côté. Aucun cas particulier de
     *       code, donc aucun risque que la « fausse » version diverge de la vraie.</li>
     * </ul>
     *
     * <p>Non minable et sans loot : sinon on le rapporterait chez soi et l'Avant-poste
     * offrirait gratuitement une machine T2 avant même le blueprint.
     */
    public static final DeferredBlock<FieldEmitterBlock> ANCIENT_EMITTER =
        BLOCKS.registerBlock("ancient_emitter",
            FieldEmitterBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0f, 3600000.0f)
                .sound(SoundType.METAL)
                .noLootTable()
                .noOcclusion()
                .lightLevel(EMITTER_GLOW));
}
