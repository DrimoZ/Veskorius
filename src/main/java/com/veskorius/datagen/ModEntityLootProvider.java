package com.veskorius.datagen;

import com.veskorius.entity.ModEntities;
import com.veskorius.item.ModItems;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Butin des entités (09-Entities.md). Le Custode lâche 2-4 Custode Alloy Fragment
 * (récompense du combat). Le Fileur ne lâche rien (volontaire).
 */
public class ModEntityLootProvider extends EntityLootSubProvider {

    public ModEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        add(ModEntities.CUSTODE.get(), LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(2.0f, 4.0f))
                .add(LootItem.lootTableItem(ModItems.CUSTODE_ALLOY_FRAGMENT.get())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 1.0f))))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        // Seul le Custode a une table ; le Fileur (sans drop) n'est pas listé.
        return Stream.of(ModEntities.CUSTODE.get());
    }
}
