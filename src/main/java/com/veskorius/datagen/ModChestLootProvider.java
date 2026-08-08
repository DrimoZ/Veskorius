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
            // POOLS D'AMORÇAGE GARANTIS — brisent un verrou de progression : la recette du
            // Field Emitter exige 4 Resonance Component + 2 Gold, or les Component ne
            // s'obtiennent qu'au Component Assembler… qui a besoin d'un champ pour tourner,
            // champ que seul le Field Emitter fournit (dépendance circulaire). L'Avant-poste
            // fournit donc DE QUOI FABRIQUER EXACTEMENT UN Field Emitter (4 Component + 2 Gold,
            // garantis), après quoi ce premier champ alimente l'Assembler et la boucle
            // s'auto-entretient. C'est l'« amorçage T2 » que 08-Structures.md promet.
            //
            // UN POOL PAR OBJET, et c'est essentiel : un pool à 1 roll tire UNE entrée parmi
            // les siennes. Regrouper les deux dans un seul pool donnait « 4 Component OU
            // 2 Gold » à pile ou face — la moitié des Avant-postes ne débloquaient donc rien
            // et le verrou circulaire tenait toujours. Deux pools = deux tirages certains.
            .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(LootItem.lootTableItem(ModItems.RESONANCE_COMPONENT.get())
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0f)))))
            .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(LootItem.lootTableItem(Items.GOLD_INGOT)
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0f)))))
            // Matériaux d'appoint variés (quantités aléatoires, en plus de l'amorçage).
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

        // Archive Régionale : l'amorçage du T4, et le chiffre 3 n'est pas décoratif.
        //
        // Le Hyper Refined Crystal ne sort que de la Deep Synthesis Chamber, qui est
        // elle-même une machine T4 dont la construction en CONSOMME un (il devient son
        // catalyseur permanent). Sans stock initial, le palier est un cercle fermé.
        // L'Archive en donne exactement 3 : deux partent dans le Harmonic Lattice du
        // premier Amplificateur, le troisième dans la Chambre. Le joueur ne peut donc pas
        // avoir les deux tout de suite et doit choisir — « mon premier Amplificateur, ou
        // ma production perenne ? ». Avec 2 il resterait bloqué sur un seul Amplificateur
        // sans jamais pouvoir en refaire ; avec 4 il n'y aurait plus de choix du tout.
        // Le raisonnement complet est dans 05-Machines.md, « Bootstrap du T4 » : ce
        // commentaire existe pour qu'on ne « rééquilibre » pas ce 3 par inadvertance.
        //
        // POOL DÉDIÉ ET GARANTI, comme l'amorçage de l'Avant-poste : mélangé aux autres
        // entrées, il serait tiré une fois sur N et la moitié des Archives ne débloqueraient
        // rien. Ce piège s'est déjà refermé deux fois sur ce fichier.
        output.accept(key(ModWorldGen.ARCHIVE_LOOT), LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(LootItem.lootTableItem(ModItems.HYPER_REFINED_CRYSTAL.get())
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0f)))))
            // De quoi tenir la suite : le Lattice réclame 4 lingots conducteurs, et on ne
            // fond pas de l'or à l'endroit où on vient de résoudre une énigme.
            .withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(2.0f, 3.0f))
                .add(LootItem.lootTableItem(Items.GOLD_INGOT)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f))))
                .add(LootItem.lootTableItem(ModItems.REFINED_RESONANCE_CRYSTAL.get())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f))))
                .add(LootItem.lootTableItem(Items.DIAMOND)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))));
        archiveDeep(output);
    }

    private void archiveDeep(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        // La salle profonde : 2 Hyper Refined de plus, garantis.
        //
        // Ce chiffre n'est pas un bonus de confort, c'est le retrait d'un choix. L'Archive
        // en donne trois, ce qui force à trancher entre le premier Amplificateur et la
        // Chambre de Synthèse ; ces deux-là permettent d'avoir les deux. C'est la bonne
        // forme pour une récompense OPTIONNELLE — elle n'accélère rien, elle adoucit une
        // décision, et qui l'ignore ne perd rien d'essentiel.
        //
        // Deux et pas trois : à trois, la salle de lecture cesserait d'imposer quoi que ce
        // soit et le « Bootstrap du T4 » de 05-Machines.md n'aurait plus d'objet.
        output.accept(key(ModWorldGen.ARCHIVE_DEEP_LOOT), LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(LootItem.lootTableItem(ModItems.HYPER_REFINED_CRYSTAL.get())
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0f)))))
            .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(LootItem.lootTableItem(ModItems.VESKORIAN_ALLOY_INGOT.get())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0f, 6.0f))))
                .add(LootItem.lootTableItem(ModItems.REFINED_RESONANCE_CRYSTAL.get())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0f, 8.0f))))));
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
