package com.veskorius.datagen;

import com.veskorius.item.CodexEntries;
import com.veskorius.item.ModDataComponents;
import com.veskorius.item.ModItems;
import com.veskorius.worldgen.ModWorldGen;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Butin des coffres de ruines (08-Structures.md). L'Habitation Modeste donne du
 * quotidien + un fragment de lore ; l'Avant-poste, des matériaux d'amorçage T2 (le
 * blueprint vient de la console, pas du coffre).
 */
public class ModChestLootProvider implements LootTableSubProvider {

    public ModChestLootProvider(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        // Habitation Modeste : rations + matériaux T1, et 1 fragment de vie quotidienne.
        output.accept(key(ModWorldGen.MODEST_DWELLING_LOOT), LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(2.0f, 4.0f))
                .add(LootItem.lootTableItem(ModItems.FOSSILIZED_RATION.get())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f))))
                .add(LootItem.lootTableItem(Items.COBBLESTONE)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0f, 8.0f))))
                .add(LootItem.lootTableItem(Items.COPPER_INGOT)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))))
            // Un fragment de lore, tiré parmi les trois (sinon deux des trois pages de
            // Codex resteraient injouables en survie). Le « hint/workshop » pointe vers
            // la console de l'Avant-poste : sa place est bien dans une habitation.
            .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(fragment(CodexEntries.DAILY_LIFE_LAMPS))
                .add(fragment(CodexEntries.DAILY_LIFE_RATION))
                .add(fragment(CodexEntries.DAILY_LIFE_MARKET))
                .add(fragment(CodexEntries.DAILY_LIFE_CHILDREN))
                .add(fragment(CodexEntries.DAILY_LIFE_FESTIVAL))
                .add(fragment(CodexEntries.HINT_WORKSHOP))));

        // Avant-poste : matériaux pour fabriquer le premier Field Emitter après avoir
        // réveillé la console (fer, redstone, or). Pas de blueprint ici (console).
        output.accept(key(ModWorldGen.OUTPOST_LOOT), LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(2.0f, 4.0f))
                .add(LootItem.lootTableItem(Items.IRON_INGOT)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f))))
                .add(LootItem.lootTableItem(Items.REDSTONE)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f))))
                .add(LootItem.lootTableItem(Items.GOLD_INGOT)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))))
            // Inscription laissée par le Custode qui garde le site.
            .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(fragment(CodexEntries.CUSTODE_WATCH))));
    }

    /** Un {@code codex_fragment} portant l'entrée de lore donnée. */
    private static LootPoolSingletonContainer.Builder<?> fragment(ResourceLocation entry) {
        return LootItem.lootTableItem(ModItems.CODEX_FRAGMENT.get())
            .apply(SetComponentsFunction.setComponent(ModDataComponents.CODEX_ENTRY.get(), entry));
    }

    private static ResourceKey<LootTable> key(ResourceLocation loc) {
        return ResourceKey.create(Registries.LOOT_TABLE, loc);
    }
}
