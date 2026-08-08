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
        dropSelf(ModBlocks.FRACTURED_CHASSIS.get());
        dropSelf(ModBlocks.ATTUNED_CHASSIS.get());
        dropSelf(ModBlocks.VESKORIAN_CHASSIS.get());
        dropSelf(ModBlocks.RESONANCE_STABILIZER.get());
        dropSelf(ModBlocks.COMPONENT_ASSEMBLER.get());
        dropSelf(ModBlocks.RESONANCE_WHETSTONE.get());
        dropSelf(ModBlocks.FLUX_PURIFIER.get());
        dropSelf(ModBlocks.FIELD_EMITTER.get());
        dropSelf(ModBlocks.TUNABLE_FIELD_EMITTER.get());
        dropSelf(ModBlocks.CRYSTAL_CRUSHER.get());
        dropSelf(ModBlocks.CRYSTAL_ROOST.get());
        dropSelf(ModBlocks.DAMPING_ARRAY.get());
        dropSelf(ModBlocks.VESKORIAN_ALLOY_FORGE.get());
        dropSelf(ModBlocks.RESONANCE_RELAY.get());
        dropSelf(ModBlocks.FLUX_COMPRESSOR.get());
        dropSelf(ModBlocks.RECLAIMER.get());
        dropSelf(ModBlocks.ADVANCED_ASSEMBLER.get());
        dropSelf(ModBlocks.DEEP_SYNTHESIS_CHAMBER.get());
        dropSelf(ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get());
        dropSelf(ModBlocks.CONVERGENCE_CORE.get());
        dropSelf(ModBlocks.RIFT_ANCHOR.get());
        dropSelf(ModBlocks.RIFT_CORE_EXTRACTOR.get());
        dropSelf(ModBlocks.RIFT_WARD_EMITTER.get());
        dropSelf(ModBlocks.DEFORMED_STONE.get());
        dropSelf(ModBlocks.RESONANCE_NETWORK_HUB.get());
        dropSelf(ModBlocks.HARMONIC_AMPLIFIER.get());
        dropSelf(ModBlocks.STRUCTURAL_SYNTHESIZER.get());
        dropSelf(ModBlocks.DEEP_CRYSTAL_DRILLER.get());
        dropSelf(ModBlocks.SLAG_VENT.get());

        // La poche de cristal lâche du Raw Resonance Crystal (Fortune s'applique,
        // Silk Touch récupère le bloc lui-même) — comportement de minerai standard.
        add(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get(),
            block -> createOreDrop(block, ModItems.RAW_RESONANCE_CRYSTAL.get()));
        // La pierre veinée se drop elle-même (bloc de construction).
        dropSelf(ModBlocks.RESONANCE_VEINED_STONE.get());

        // Le dépôt de flux miné ne donne RIEN (la croûte se détruit) — il faut le
        // brosser pour obtenir le flux (comportement vanilla des blocs suspects).
        add(ModBlocks.RAW_FLUX_DEPOSIT.get(), LootTable.lootTable());
        // La console d'attunement minée ne donne RIEN (« machine morte non
        // récupérable » — 08-Structures.md) : que des gravats, aucun objet.
        add(ModBlocks.ATTUNEMENT_CONSOLE.get(), LootTable.lootTable());
        add(ModBlocks.SIGMA_CONSOLE.get(), LootTable.lootTable());

        // --- Architecture de donjon (17-Dungeons.md §4) -----------------------
        // Tout se récupère : ce qu'on trouve en ruine doit pouvoir être rebâti.
        dropSelf(ModBlocks.VEINED_STONE_BRICKS.get());
        dropSelf(ModBlocks.CRACKED_VEINED_STONE_BRICKS.get());
        dropSelf(ModBlocks.CHISELED_VEINED_STONE.get());
        dropSelf(ModBlocks.VEINED_STONE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.VEINED_STONE_BRICK_WALL.get());
        dropSelf(ModBlocks.RESONANCE_LAMP.get());
        dropSelf(ModBlocks.CONDUIT_LINE.get());
        dropSelf(ModBlocks.VEINED_STONE_COLUMN.get());
        dropSelf(ModBlocks.VESKORIAN_ALLOY_BLOCK.get());
        dropSelf(ModBlocks.SYNTHESIS_RESIDUE_BLOCK.get());
        dropSelf(ModBlocks.RESONANCE_SAND.get());
        // Casser le buisson rend la GRAINE, jamais le buisson. On ne déplace pas une
        // plante, on la resème — et surtout la graine ne doit pas pouvoir se perdre :
        // elle ne s'obtient qu'en fouillant une Archive, et pas à tous les coups.
        add(ModBlocks.RESONANCE_BLOOM_BUSH.get(),
            block -> createSingleItemTable(ModItems.ANCIENT_SEED.get()));
        // Le verre se récupère SANS Toucher de Soie, contrairement au verre vanilla.
        // La doctrine de ce fichier — « tout se récupère : ce qu'on trouve en ruine
        // doit pouvoir être rebâti » — vaut d'autant plus pour le seul matériau du mod
        // qui n'existe que pour bâtir. Le casser par erreur ne doit pas coûter un
        // Cristal Stable.
        dropSelf(ModBlocks.RESONANCE_GLASS.get());
        dropSelf(ModBlocks.LUMINOUS_RESONANCE_GLASS.get());
        dropSelf(ModBlocks.DISSONANCE_BLOOM.get());
        // Une dalle double rend DEUX dalles : sans cette table dédiée, la moitié du
        // bloc disparaît au minage.
        add(ModBlocks.VEINED_STONE_BRICK_SLAB.get(),
            block -> createSlabItemTable(ModBlocks.VEINED_STONE_BRICK_SLAB.get()));

        // Le sas et l'émetteur ancien sont déclarés noLootTable() (ModBlocks) : ils
        // sont indestructibles, donc il n'y a rien à décrire ici. Le provider ne les
        // réclamera pas — c'est précisément ce que noLootTable() lui dit.
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
