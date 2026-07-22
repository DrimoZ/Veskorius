package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.worldgen.ModWorldGen;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

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

    /**
     * Registres datapack (worldgen + biome modifiers) construits par datagen.
     * Séparé car {@link DatapackBuiltinEntriesProvider} en a besoin sous forme de
     * {@link RegistrySetBuilder}.
     */
    private static final RegistrySetBuilder DATAPACK_ENTRIES = new RegistrySetBuilder()
        .add(Registries.CONFIGURED_FEATURE, ModWorldGen::bootstrapConfiguredFeatures)
        .add(Registries.PLACED_FEATURE, ModWorldGen::bootstrapPlacedFeatures)
        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGen::bootstrapBiomeModifiers);

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
            new ModAdvancementProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
            new LootTableProvider(output, Set.of(),
                List.of(
                    new LootTableProvider.SubProviderEntry(
                        ModBlockLootProvider::new, LootContextParamSets.BLOCK),
                    new LootTableProvider.SubProviderEntry(
                        ModChestLootProvider::new, LootContextParamSets.CHEST),
                    new LootTableProvider.SubProviderEntry(
                        ModEntityLootProvider::new, LootContextParamSets.ENTITY)),
                lookupProvider));

        // Les tags d'objets referencent les tags de blocs (mecanisme de copie de
        // ItemTagsProvider), d'ou le passage explicite du provider de blocs.
        BlockTagsProvider blockTags =
            new ModBlockTagsProvider(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
            new ModItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));

        // Worldgen (poches de cristal) + biome modifier, via les registres datapack.
        generator.addProvider(event.includeServer(),
            new DatapackBuiltinEntriesProvider(output, lookupProvider, DATAPACK_ENTRIES, Set.of(Veskorius.MOD_ID)));
    }
}
