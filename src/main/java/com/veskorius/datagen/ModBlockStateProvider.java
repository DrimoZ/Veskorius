package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractMachineBlock;
import com.veskorius.block.FieldEmitterBlock;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.ResonanceVeinedStoneBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    /**
     * Décalage de rotation des modèles orientés. {@code FACING} vaut la direction vers
     * laquelle la façade regarde ; la face avant du modèle est dessinée au nord, d'où
     * le +180 (même convention que {@code horizontalBlock} de NeoForge).
     */
    private static final int FACING_OFFSET = 180;

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Veskorius.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // --- Machines actives -------------------------------------------------
        // Chaque machine est un bloc ORIENTÉ à deux états : sa face avant porte son
        // emblème, et cet emblème s'allume quand la machine avance un cycle.
        //
        // Les deux étaient invisibles avant : le modèle était un `cube_all` (même
        // texture sur les six faces, donc aucune orientation lisible) et les variantes
        // lit=true / lit=false pointaient le MÊME modèle — la propriété LIT ne pilotait
        // que le niveau de lumière. Une machine à l'arrêt et une machine en marche
        // étaient strictement identiques de face, ce que 12-UX promet pourtant comme
        // « le seul retour "pas d'énergie" lisible sans ouvrir le GUI ».
        machine(ModBlocks.RESONANCE_STABILIZER.get(), "resonance_stabilizer", FRACTURED);
        machine(ModBlocks.COMPONENT_ASSEMBLER.get(), "component_assembler", FRACTURED);
        machine(ModBlocks.RESONANCE_WHETSTONE.get(), "resonance_whetstone", FRACTURED);
        machine(ModBlocks.CRYSTAL_CRUSHER.get(), "crystal_crusher", FRACTURED);
        machine(ModBlocks.FLUX_PURIFIER.get(), "flux_purifier", ATTUNED);
        machine(ModBlocks.CRYSTAL_ROOST.get(), "crystal_roost", ATTUNED);
        machine(ModBlocks.DAMPING_ARRAY.get(), "damping_array", VESKORIAN);

        // --- Émetteurs de champ ----------------------------------------------
        // Même traitement, sur la propriété LIT ajoutée à FieldEmitterBlock : un
        // émetteur alimenté se distingue désormais d'un émetteur à sec au premier
        // coup d'œil, sans attendre une bouffée de particules.
        emitter(ModBlocks.FIELD_EMITTER.get(), "field_emitter", ATTUNED);
        emitter(ModBlocks.TUNABLE_FIELD_EMITTER.get(), "tunable_field_emitter", ATTUNED);

        // --- Châssis nus ------------------------------------------------------
        // Le bloc de base, posable tel quel. C'est littéralement le boîtier que
        // portent les machines de son palier : côtés et dessus sont les MÊMES fichiers.
        chassis(ModBlocks.FRACTURED_CHASSIS.get(), FRACTURED);
        chassis(ModBlocks.ATTUNED_CHASSIS.get(), ATTUNED);
        chassis(ModBlocks.VESKORIAN_CHASSIS.get(), VESKORIAN);

        // --- Blocs naturels ---------------------------------------------------
        simpleBlockWithItem(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get(),
            cubeAll(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get()));

        // La pierre veinée a un état SPORED : même roche, nodules en plus.
        var plainVeined = cubeAll(ModBlocks.RESONANCE_VEINED_STONE.get());
        var sporedVeined = models().cubeAll("resonance_veined_stone_spored",
            modLoc("block/resonance_veined_stone_spored"));
        getVariantBuilder(ModBlocks.RESONANCE_VEINED_STONE.get()).forAllStates(state ->
            ConfiguredModel.builder().modelFile(
                state.getValue(ResonanceVeinedStoneBlock.SPORED) ? sporedVeined : plainVeined).build());
        itemModels().withExistingParent("resonance_veined_stone", modLoc("block/resonance_veined_stone"));

        // Le dépôt de flux n'a pas d'objet : juste la blockstate + le modèle.
        simpleBlock(ModBlocks.RAW_FLUX_DEPOSIT.get(), cubeAll(ModBlocks.RAW_FLUX_DEPOSIT.get()));

        // Console d'attunement : son écran est sur les QUATRE côtés. Elle n'a pas de
        // propriété FACING (elle est posée par la génération, jamais par un joueur) —
        // la reconnaître ne doit donc pas dépendre de l'angle sous lequel on l'aborde.
        simpleBlock(ModBlocks.ATTUNEMENT_CONSOLE.get(), models().cube("attunement_console",
            modLoc("block/" + FRACTURED + "_top"), modLoc("block/" + FRACTURED + "_top"),
            modLoc("block/attunement_console_front"), modLoc("block/attunement_console_front"),
            modLoc("block/attunement_console_front"), modLoc("block/attunement_console_front"))
            .texture("particle", modLoc("block/attunement_console_front")));
    }

    // --- Châssis de palier ---------------------------------------------------
    // Le nom du châssis suffit à retrouver ses deux textures : une machine déclare
    // son palier, pas ses fichiers. Ajouter une machine T2 ne demande donc aucune
    // texture de boîtier — c'est tout l'intérêt du système.

    private static final String FRACTURED = "fractured_chassis";
    private static final String ATTUNED = "attuned_chassis";
    private static final String VESKORIAN = "veskorian_chassis";

    private void chassis(Block block, String chassisName) {
        simpleBlockWithItem(block, models().cubeBottomTop(chassisName,
            modLoc("block/" + chassisName + "_side"),
            modLoc("block/" + chassisName + "_top"),
            modLoc("block/" + chassisName + "_top")));
    }

    // --- Fabriques de modèles ------------------------------------------------

    private void machine(Block block, String name, String chassisName) {
        litOriented(block, name, chassisName,
            AbstractMachineBlock.FACING, AbstractMachineBlock.LIT);
    }

    private void emitter(Block block, String name, String chassisName) {
        litOriented(block, name, chassisName, FieldEmitterBlock.FACING, FieldEmitterBlock.LIT);
    }

    /**
     * Deux modèles orientables — façade éteinte et façade allumée — et une variante par
     * couple (orientation, état). Le modèle « éteint » garde le nom nu du bloc : c'est
     * lui que réutilisent les modèles d'objet, qui n'ont donc pas à changer.
     */
    private void litOriented(Block block, String name, String chassisName,
                             DirectionProperty facing, BooleanProperty lit) {
        ResourceLocation side = modLoc("block/" + chassisName + "_side");
        ResourceLocation top = modLoc("block/" + chassisName + "_top");
        ModelFile off = models().orientable(name, side, modLoc("block/" + name + "_front"), top);
        ModelFile on = models().orientable(name + "_on", side, modLoc("block/" + name + "_front_on"), top);
        getVariantBuilder(block).forAllStates((BlockState state) -> ConfiguredModel.builder()
            .modelFile(state.getValue(lit) ? on : off)
            .rotationY(((int) state.getValue(facing).toYRot() + FACING_OFFSET) % 360)
            .build());
    }
}
