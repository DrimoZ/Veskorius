package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.tag.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, Veskorius.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Les blocs sont declares requiresCorrectToolForDrops : sans ces deux
        // tags, ils ne se dropent avec aucun outil.
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(ModBlocks.FRACTURED_CHASSIS.get())
            .add(ModBlocks.ATTUNED_CHASSIS.get())
            .add(ModBlocks.VESKORIAN_CHASSIS.get())
            .add(ModBlocks.RESONANCE_STABILIZER.get())
            .add(ModBlocks.COMPONENT_ASSEMBLER.get())
            .add(ModBlocks.RESONANCE_WHETSTONE.get())
            .add(ModBlocks.FLUX_PURIFIER.get())
            .add(ModBlocks.FIELD_EMITTER.get())
            .add(ModBlocks.TUNABLE_FIELD_EMITTER.get())
            .add(ModBlocks.CRYSTAL_CRUSHER.get())
            .add(ModBlocks.CRYSTAL_ROOST.get())
            .add(ModBlocks.DAMPING_ARRAY.get())
            .add(ModBlocks.VESKORIAN_ALLOY_FORGE.get())
            .add(ModBlocks.RESONANCE_RELAY.get())
            .add(ModBlocks.FLUX_COMPRESSOR.get())
            .add(ModBlocks.DEEP_SYNTHESIS_CHAMBER.get())
            .add(ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get())
            .add(ModBlocks.CONVERGENCE_CORE.get())
            .add(ModBlocks.RIFT_ANCHOR.get())
            .add(ModBlocks.DEFORMED_STONE.get())
            .add(ModBlocks.RESONANCE_NETWORK_HUB.get())
            .add(ModBlocks.HARMONIC_AMPLIFIER.get())
            .add(ModBlocks.STRUCTURAL_SYNTHESIZER.get())
            .add(ModBlocks.DEEP_CRYSTAL_DRILLER.get())
            .add(ModBlocks.SLAG_VENT.get())
            .add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())
            .add(ModBlocks.RESONANCE_VEINED_STONE.get())
            .add(ModBlocks.RAW_FLUX_DEPOSIT.get())
            .add(ModBlocks.ATTUNEMENT_CONSOLE.get())
            .add(ARCHITECTURE);
        tag(BlockTags.NEEDS_STONE_TOOL)
            .add(ModBlocks.FRACTURED_CHASSIS.get())
            .add(ModBlocks.ATTUNED_CHASSIS.get())
            .add(ModBlocks.VESKORIAN_CHASSIS.get())
            .add(ModBlocks.RESONANCE_STABILIZER.get())
            .add(ModBlocks.COMPONENT_ASSEMBLER.get())
            .add(ModBlocks.RESONANCE_WHETSTONE.get())
            .add(ModBlocks.FLUX_PURIFIER.get())
            .add(ModBlocks.FIELD_EMITTER.get())
            .add(ModBlocks.TUNABLE_FIELD_EMITTER.get())
            .add(ModBlocks.CRYSTAL_CRUSHER.get())
            .add(ModBlocks.CRYSTAL_ROOST.get())
            .add(ModBlocks.DAMPING_ARRAY.get())
            .add(ModBlocks.VESKORIAN_ALLOY_FORGE.get())
            .add(ModBlocks.RESONANCE_RELAY.get())
            .add(ModBlocks.FLUX_COMPRESSOR.get())
            .add(ModBlocks.DEEP_SYNTHESIS_CHAMBER.get())
            .add(ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get())
            .add(ModBlocks.CONVERGENCE_CORE.get())
            .add(ModBlocks.RIFT_ANCHOR.get())
            .add(ModBlocks.DEFORMED_STONE.get())
            .add(ModBlocks.RESONANCE_NETWORK_HUB.get())
            .add(ModBlocks.HARMONIC_AMPLIFIER.get())
            .add(ModBlocks.STRUCTURAL_SYNTHESIZER.get())
            .add(ModBlocks.DEEP_CRYSTAL_DRILLER.get())
            .add(ModBlocks.SLAG_VENT.get())
            .add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())
            .add(ModBlocks.RESONANCE_VEINED_STONE.get())
            .add(ModBlocks.VESKORIAN_ALLOY_BLOCK.get())
            .add(ARCHITECTURE);

        // Blocs que le pourrissage des pièces de structure a le DROIT de manger
        // (17-Dungeons.md §2.4). C'est une liste blanche, pas une liste noire, et c'est
        // le point important : BlockRotProcessor ne retire que ce qui est ici. Une
        // console, un coffre, un sas ou un émetteur ancien n'y sera jamais, donc aucun
        // ajout futur de pièce ne pourra rendre un Avant-poste infranchissable en
        // effaçant statistiquement son chemin critique. Le même bug a déjà été trouvé
        // deux fois sur le loot d'amorçage ; ici il est impossible par construction.
        tag(ModTags.Blocks.STRUCTURE_ROTTABLE)
            .add(ARCHITECTURE)
            .add(ModBlocks.RESONANCE_VEINED_STONE.get());
    }

    /**
     * La maçonnerie de donjon, citée trois fois ci-dessus. Extraite pour que l'ajout
     * d'un bloc d'architecture ne demande qu'une ligne, et surtout pour qu'il ne puisse
     * pas atterrir dans deux tags sur trois.
     */
    private static final net.minecraft.world.level.block.Block[] ARCHITECTURE = {
        ModBlocks.VEINED_STONE_BRICKS.get(),
        ModBlocks.CRACKED_VEINED_STONE_BRICKS.get(),
        ModBlocks.CHISELED_VEINED_STONE.get(),
        ModBlocks.VEINED_STONE_BRICK_STAIRS.get(),
        ModBlocks.VEINED_STONE_BRICK_SLAB.get(),
        ModBlocks.VEINED_STONE_BRICK_WALL.get(),
        ModBlocks.RESONANCE_LAMP.get(),
        ModBlocks.CONDUIT_LINE.get(),
        ModBlocks.VEINED_STONE_COLUMN.get(),
    };
}
