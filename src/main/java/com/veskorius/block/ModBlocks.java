package com.veskorius.block;

import com.veskorius.Veskorius;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    /** Machine #1 (05-Machines.md). Bloc actif : block entity + GUI. */
    public static final DeferredBlock<ResonanceStabilizerBlock> RESONANCE_STABILIZER =
        BLOCKS.registerBlock("resonance_stabilizer",
            ResonanceStabilizerBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops());

    /** Machine #2 (05-Machines.md). Bloc actif : block entity + GUI, consomme des Osc. */
    public static final DeferredBlock<ComponentAssemblerBlock> COMPONENT_ASSEMBLER =
        BLOCKS.registerBlock("component_assembler",
            ComponentAssemblerBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());

    /** Machine #3 (05-Machines.md). Bloc actif : block entity + GUI. */
    public static final DeferredBlock<ResonanceWhetstoneBlock> RESONANCE_WHETSTONE =
        BLOCKS.registerBlock("resonance_whetstone",
            ResonanceWhetstoneBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.5f, 6.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());

    /**
     * Pierre veinée qui enrobe les poches de cristal (07-World-Generation.md).
     * Bloc décoratif + « tell » visuel : le voir, c'est savoir qu'une poche est
     * proche. Ne rayonne pas — on le reconnaît à sa texture, pas à sa lumière.
     */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> RESONANCE_VEINED_STONE =
        BLOCKS.registerSimpleBlock("resonance_veined_stone",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(1.5f, 6.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops());

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
                .requiresCorrectToolForDrops());

    /** Machine #4 (05-Machines.md). Bloc passif : fournit un champ de Résonance. */
    public static final DeferredBlock<FieldEmitterBlock> FIELD_EMITTER =
        BLOCKS.registerBlock("field_emitter",
            FieldEmitterBlock::new,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(3.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 4));
}
