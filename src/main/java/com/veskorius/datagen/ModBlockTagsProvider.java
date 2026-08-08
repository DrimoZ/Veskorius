package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.tag.ModTags;
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
        // TROIS BLOCS MANQUAIENT ICI, ET ILS DISPARAISSAIENT AU MINAGE.
        //
        // requiresCorrectToolForDrops() sans appartenance à un tag mineable/* signifie
        // qu'AUCUN outil n'est jamais correct : la table de butin ne se déclenche donc
        // jamais, quel que soit ce qu'on tient en main. L'Advanced Assembler s'évaporait
        // quand on le récupérait, et le Bloc d'Alliage — un bloc de CONSTRUCTION, produit
        // du Structural Synthesizer — ne revenait pas du mur qu'on venait de bâtir.
        //
        // Rien ne le signalait : le bloc se pose, se casse, et le joueur croit à une
        // maladresse. C'est `gradlew audit` qui l'a trouvé, en croisant la propriété du
        // bloc avec les tags générés.
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(ModBlocks.ADVANCED_ASSEMBLER.get())
            .add(ModBlocks.VESKORIAN_ALLOY_BLOCK.get())
            .add(ModBlocks.SYNTHESIS_RESIDUE_BLOCK.get())
            .add(ModBlocks.FRACTURED_CHASSIS.get())
            .add(ModBlocks.ATTUNED_CHASSIS.get())
            .add(ModBlocks.VESKORIAN_CHASSIS.get())
            .add(ModBlocks.RESONANCE_STABILIZER.get())
            .add(ModBlocks.COMPONENT_ASSEMBLER.get())
            .add(ModBlocks.RESONANCE_WHETSTONE.get())
            .add(ModBlocks.FLUX_PURIFIER.get())
            .add(ModBlocks.FIELD_EMITTER.get())
            .add(ModBlocks.TUNABLE_FIELD_EMITTER.get())
            .add(ModBlocks.CRYSTAL_CRUSHER.get())
            .add(ModBlocks.CRYSTAL_ROOST.get())
            .add(ModBlocks.DAMPING_ARRAY.get())
            .add(ModBlocks.VESKORIAN_ALLOY_FORGE.get())
            .add(ModBlocks.RESONANCE_RELAY.get())
            .add(ModBlocks.FLUX_COMPRESSOR.get())
            .add(ModBlocks.RECLAIMER.get())
            .add(ModBlocks.DEEP_SYNTHESIS_CHAMBER.get())
            .add(ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get())
            .add(ModBlocks.CONVERGENCE_CORE.get())
            .add(ModBlocks.RIFT_ANCHOR.get())
            .add(ModBlocks.RIFT_CORE_EXTRACTOR.get())
            .add(ModBlocks.RIFT_WARD_EMITTER.get())
            .add(ModBlocks.DEFORMED_STONE.get())
            .add(ModBlocks.RESONANCE_NETWORK_HUB.get())
            .add(ModBlocks.HARMONIC_AMPLIFIER.get())
            .add(ModBlocks.STRUCTURAL_SYNTHESIZER.get())
            .add(ModBlocks.DEEP_CRYSTAL_DRILLER.get())
            .add(ModBlocks.SLAG_VENT.get())
            .add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())
            .add(ModBlocks.RESONANCE_VEINED_STONE.get())
            .add(ModBlocks.RAW_FLUX_DEPOSIT.get())
            .add(ModBlocks.ATTUNEMENT_CONSOLE.get())
            .add(ModBlocks.ANCIENT_CONDUIT_STONE.get())
            .add(ARCHITECTURE);
        // LA PIERRE À CONDUITS N'EST MINABLE QUE PAR L'ALLIAGE.
        //
        // Un palier d'outil se dit en négatif : on inscrit le tag comme INCORRECT pour les
        // six paliers vanilla, et on l'omet du tag veskorien. Ni le diamant ni la netherite
        // n'en tirent alors quoi que ce soit, et l'alliage seul y arrive — sans une ligne
        // de code, uniquement par la donnée. C'est le mécanisme exact de needs_diamond_tool.
        tag(ModTags.Blocks.NEEDS_VESKORIAN_TOOL)
            .add(ModBlocks.ANCIENT_CONDUIT_STONE.get());
        for (net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> incorrect :
                java.util.List.of(BlockTags.INCORRECT_FOR_WOODEN_TOOL,
                    BlockTags.INCORRECT_FOR_STONE_TOOL, BlockTags.INCORRECT_FOR_IRON_TOOL,
                    BlockTags.INCORRECT_FOR_GOLD_TOOL, BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                    BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
            tag(incorrect).addTag(ModTags.Blocks.NEEDS_VESKORIAN_TOOL);
        }
        // Le tag veskorien existe et reste VIDE : l'alliage ne rate rien. Il doit tout de
        // même être généré, sinon le palier référence un tag absent et chaque bloc devient
        // incassable à la pioche d'alliage.
        tag(ModTags.Blocks.INCORRECT_FOR_VESKORIAN_TOOL);

        // Du sable se creuse à la pelle. Il ne se perd pas sans (il n'exige pas le bon
        // outil), mais le creuser à la main prend vingt fois plus longtemps.
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .add(ModBlocks.RESONANCE_SAND.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
            .add(ModBlocks.FRACTURED_CHASSIS.get())
            .add(ModBlocks.ATTUNED_CHASSIS.get())
            .add(ModBlocks.VESKORIAN_CHASSIS.get())
            .add(ModBlocks.RESONANCE_STABILIZER.get())
            .add(ModBlocks.COMPONENT_ASSEMBLER.get())
            .add(ModBlocks.RESONANCE_WHETSTONE.get())
            .add(ModBlocks.FLUX_PURIFIER.get())
            .add(ModBlocks.FIELD_EMITTER.get())
            .add(ModBlocks.TUNABLE_FIELD_EMITTER.get())
            .add(ModBlocks.CRYSTAL_CRUSHER.get())
            .add(ModBlocks.CRYSTAL_ROOST.get())
            .add(ModBlocks.DAMPING_ARRAY.get())
            .add(ModBlocks.VESKORIAN_ALLOY_FORGE.get())
            .add(ModBlocks.RESONANCE_RELAY.get())
            .add(ModBlocks.FLUX_COMPRESSOR.get())
            .add(ModBlocks.RECLAIMER.get())
            .add(ModBlocks.DEEP_SYNTHESIS_CHAMBER.get())
            .add(ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get())
            .add(ModBlocks.CONVERGENCE_CORE.get())
            .add(ModBlocks.RIFT_ANCHOR.get())
            .add(ModBlocks.RIFT_CORE_EXTRACTOR.get())
            .add(ModBlocks.RIFT_WARD_EMITTER.get())
            .add(ModBlocks.DEFORMED_STONE.get())
            .add(ModBlocks.RESONANCE_NETWORK_HUB.get())
            .add(ModBlocks.HARMONIC_AMPLIFIER.get())
            .add(ModBlocks.STRUCTURAL_SYNTHESIZER.get())
            .add(ModBlocks.DEEP_CRYSTAL_DRILLER.get())
            .add(ModBlocks.SLAG_VENT.get())
            .add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())
            .add(ModBlocks.RESONANCE_VEINED_STONE.get())
            .add(ModBlocks.VESKORIAN_ALLOY_BLOCK.get())
            .add(ARCHITECTURE);

        // Blocs que le pourrissage des pièces de structure a le DROIT de manger
        // (17-Dungeons.md §2.4). C'est une liste blanche, pas une liste noire, et c'est
        // le point important : BlockRotProcessor ne retire que ce qui est ici. Une
        // console, un coffre, un sas ou un émetteur ancien n'y sera jamais, donc aucun
        // ajout futur de pièce ne pourra rendre un Avant-poste infranchissable en
        // effaçant statistiquement son chemin critique. Le même bug a déjà été trouvé
        // deux fois sur le loot d'amorçage ; ici il est impossible par construction.
        tag(ModTags.Blocks.STRUCTURE_ROTTABLE)
            .add(ARCHITECTURE)
            .add(ModBlocks.RESONANCE_VEINED_STONE.get());
    }

    /**
     * La maçonnerie de donjon, citée trois fois ci-dessus. Extraite pour que l'ajout
     * d'un bloc d'architecture ne demande qu'une ligne, et surtout pour qu'il ne puisse
     * pas atterrir dans deux tags sur trois.
     */
    private static final net.minecraft.world.level.block.Block[] ARCHITECTURE = {
        ModBlocks.VEINED_STONE_BRICKS.get(),
        ModBlocks.CRACKED_VEINED_STONE_BRICKS.get(),
        ModBlocks.CHISELED_VEINED_STONE.get(),
        ModBlocks.VEINED_STONE_BRICK_STAIRS.get(),
        ModBlocks.VEINED_STONE_BRICK_SLAB.get(),
        ModBlocks.VEINED_STONE_BRICK_WALL.get(),
        ModBlocks.RESONANCE_LAMP.get(),
        ModBlocks.CONDUIT_LINE.get(),
        ModBlocks.VEINED_STONE_COLUMN.get(),
    };
}
