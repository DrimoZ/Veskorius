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
}
