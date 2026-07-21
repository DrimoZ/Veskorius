package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Veskorius.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // horizontalBlock (et non simpleBlock) : le bloc porte une propriete
        // FACING a 4 valeurs, chacune a besoin de sa variante de blockstate.
        // Le modele reste un cube uniforme tant que la texture est un placeholder
        // (Phase 6) — l'orientation existe cote etat, pas encore cote rendu.
        horizontalBlock(ModBlocks.RESONANCE_STABILIZER.get(), cubeAll(ModBlocks.RESONANCE_STABILIZER.get()));
        horizontalBlock(ModBlocks.COMPONENT_ASSEMBLER.get(), cubeAll(ModBlocks.COMPONENT_ASSEMBLER.get()));
        horizontalBlock(ModBlocks.FLUX_PURIFIER.get(), cubeAll(ModBlocks.FLUX_PURIFIER.get()));
        horizontalBlock(ModBlocks.RESONANCE_WHETSTONE.get(), cubeAll(ModBlocks.RESONANCE_WHETSTONE.get()));
        horizontalBlock(ModBlocks.FIELD_EMITTER.get(), cubeAll(ModBlocks.FIELD_EMITTER.get()));

        // Blocs naturels : cubes uniformes simples.
        simpleBlockWithItem(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get(),
            cubeAll(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get()));
        simpleBlockWithItem(ModBlocks.RESONANCE_VEINED_STONE.get(),
            cubeAll(ModBlocks.RESONANCE_VEINED_STONE.get()));
    }
}
