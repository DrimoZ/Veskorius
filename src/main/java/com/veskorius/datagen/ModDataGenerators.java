package com.veskorius.datagen;

import com.veskorius.Veskorius;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Point d'entree du datagen : {@code ./gradlew runData} ecrit les JSON dans
 * src/generated/resources/, deja declare comme source de ressources dans
 * build.gradle.
 *
 * A partir d'ici, aucun blockstate / modele / recette / loot table / traduction
 * n'est ecrit a la main — ils sont tous derives du code, donc impossibles a
 * desynchroniser des registres.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(),
            new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(),
            new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(),
            new ModLanguageProvider(output));

        generator.addProvider(event.includeServer(),
            new ModRecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(),
            new ModStructureTemplateProvider(output));
        generator.addProvider(event.includeServer(),
            new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                    ModBlockLootProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider));

        // Les tags d'objets referencent les tags de blocs (mecanisme de copie de
        // ItemTagsProvider), d'ou le passage explicite du provider de blocs.
        BlockTagsProvider blockTags =
            new ModBlockTagsProvider(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
            new ModItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
    }
}
