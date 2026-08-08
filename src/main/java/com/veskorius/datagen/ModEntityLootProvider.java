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

        // Le Lourd lache la meme chose, en plus grande quantite : 4 a 7 fragments.
        //
        // Le fragment est un substitut 1:1 du lingot de fer dans les recettes Veskorius,
        // donc son role est de recompenser un style de jeu combat. Un garde deux fois plus
        // resistant qui rendrait autant qu un garde ordinaire ne recompenserait rien — il
        // taxerait. La quantite est la seule variable qui compte ici : lui inventer un
        // objet propre aurait ajoute une matiere au mod pour dire « celui-la etait plus dur ».
        add(ModEntities.CUSTODE_LOURD.get(), LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(4.0f, 7.0f))
                .add(LootItem.lootTableItem(ModItems.CUSTODE_ALLOY_FRAGMENT.get())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 1.0f))))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        // Les deux Custodes ont une table ; le Fileur (sans drop) n'est pas listé, et
        // l'Archiviste non plus : sa récompense est le coffre de sa salle, pas son corps.
        return Stream.of(ModEntities.CUSTODE.get(), ModEntities.CUSTODE_LOURD.get());
    }
}
