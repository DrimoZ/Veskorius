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
        // Le bloc est declare requiresCorrectToolForDrops : sans ces deux tags,
        // il ne se drope avec aucun outil.
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(ModBlocks.RESONANCE_STABILIZER.get());
        tag(BlockTags.NEEDS_STONE_TOOL)
            .add(ModBlocks.RESONANCE_STABILIZER.get());
    }
}
