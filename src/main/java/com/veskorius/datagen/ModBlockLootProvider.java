package com.veskorius.datagen;

import com.veskorius.block.ModBlocks;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Les machines se dropent elles-memes. Leur inventaire est vide au sol
        // separement par AbstractMachineBlock.onRemove — la loot table ne gere
        // que le bloc, pas son contenu.
        dropSelf(ModBlocks.RESONANCE_STABILIZER.get());
        dropSelf(ModBlocks.COMPONENT_ASSEMBLER.get());
        dropSelf(ModBlocks.RESONANCE_WHETSTONE.get());
        dropSelf(ModBlocks.FIELD_EMITTER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // Restreint la verification de couverture aux blocs du mod : sans ca,
        // le provider exige une loot table pour tous les blocs vanilla.
        return ModBlocks.BLOCKS.getEntries().stream()
            .map(holder -> (Block) holder.value())
            .collect(Collectors.toList());
    }
}
