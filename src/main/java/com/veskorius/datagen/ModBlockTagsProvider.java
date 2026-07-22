package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
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
            .add(ModBlocks.RESONANCE_STABILIZER.get())
            .add(ModBlocks.COMPONENT_ASSEMBLER.get())
            .add(ModBlocks.RESONANCE_WHETSTONE.get())
            .add(ModBlocks.FLUX_PURIFIER.get())
            .add(ModBlocks.FIELD_EMITTER.get())
            .add(ModBlocks.CRYSTAL_CRUSHER.get())
            .add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())
            .add(ModBlocks.RESONANCE_VEINED_STONE.get())
            .add(ModBlocks.RAW_FLUX_DEPOSIT.get())
            .add(ModBlocks.ATTUNEMENT_CONSOLE.get());
        tag(BlockTags.NEEDS_STONE_TOOL)
            .add(ModBlocks.RESONANCE_STABILIZER.get())
            .add(ModBlocks.COMPONENT_ASSEMBLER.get())
            .add(ModBlocks.RESONANCE_WHETSTONE.get())
            .add(ModBlocks.FLUX_PURIFIER.get())
            .add(ModBlocks.FIELD_EMITTER.get())
            .add(ModBlocks.CRYSTAL_CRUSHER.get())
            .add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())
            .add(ModBlocks.RESONANCE_VEINED_STONE.get());
    }
}
