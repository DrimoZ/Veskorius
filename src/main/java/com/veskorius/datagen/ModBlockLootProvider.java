package com.veskorius.datagen;

import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

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
        dropSelf(ModBlocks.FLUX_PURIFIER.get());
        dropSelf(ModBlocks.FIELD_EMITTER.get());

        // La poche de cristal lâche du Raw Resonance Crystal (Fortune s'applique,
        // Silk Touch récupère le bloc lui-même) — comportement de minerai standard.
        add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get(),
            block -> createOreDrop(block, ModItems.RAW_RESONANCE_CRYSTAL.get()));
        // La pierre veinée se drop elle-même (bloc de construction).
        dropSelf(ModBlocks.RESONANCE_VEINED_STONE.get());

        // Le dépôt de flux miné ne donne RIEN (la croûte se détruit) — il faut le
        // brosser pour obtenir le flux (comportement vanilla des blocs suspects).
        add(ModBlocks.RAW_FLUX_DEPOSIT.get(), LootTable.lootTable());
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
