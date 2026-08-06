package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractMachineBlock;
import com.veskorius.block.FieldEmitterBlock;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.ResonanceVeinedStoneBlock;
import net.minecraft.core.Direction;
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
        simpleBlock(ModBlocks.ATTUNEMENT_CONSOLE.get(), consoleModel());
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

    /**
     * L'émetteur n'est pas un cube : c'est une <b>tour à trois étages</b> — socle,
     * coffret, tête d'émission. Il mérite sa propre silhouette parce que c'est le bloc
     * qu'on cherche des yeux dans une base : c'est lui qui alimente tout le reste, et
     * on doit le repérer de loin sans lire une seule texture. Tout tient dans le cube
     * (rien ne dépasse), donc on peut empiler ou construire autour sans surprise.
     */
    private void emitter(Block block, String name, String chassisName) {
        ModelFile off = emitterModel(name, chassisName, name + "_front");
        ModelFile on = emitterModel(name + "_on", chassisName, name + "_front_on");
        getVariantBuilder(block).forAllStates((BlockState state) -> ConfiguredModel.builder()
            .modelFile(state.getValue(FieldEmitterBlock.LIT) ? on : off)
            .rotationY(((int) state.getValue(FieldEmitterBlock.FACING).toYRot() + FACING_OFFSET) % 360)
            .build());
    }

    private ModelFile emitterModel(String name, String chassisName, String frontTexture) {
        ResourceLocation side = modLoc("block/" + chassisName + "_side");
        ResourceLocation top = modLoc("block/" + chassisName + "_top");
        var b = models().getBuilder(name)
            .parent(BLOCK_ROOT)
            .texture("particle", side)
            .texture("side", side)
            .texture("top", top)
            .texture("front", modLoc("block/" + frontTexture));

        // Socle large, coffret (c'est lui qui porte la façade), tête étroite.
        cube(b, 0, 0, 0, 16, 3, 16, "#side", "#top", "#side", false);
        cube(b, 1, 3, 1, 15, 12, 15, "#side", "#top", "#front", false);
        cube(b, 3, 12, 3, 13, 15, 13, "#side", "#top", "#side", false);
        cube(b, 6, 15, 6, 10, 16, 10, "#top", "#top", "#top", false);
        return b;
    }

    /**
     * Deux modèles orientables — façade éteinte et façade allumée — et une variante par
     * couple (orientation, état). Le modèle « éteint » garde le nom nu du bloc : c'est
     * lui que réutilisent les modèles d'objet, qui n'ont donc pas à changer.
     */
    private void litOriented(Block block, String name, String chassisName,
                             DirectionProperty facing, BooleanProperty lit) {
        ModelFile off = machineModel(name, chassisName, name + "_front");
        ModelFile on = machineModel(name + "_on", chassisName, name + "_front_on");
        getVariantBuilder(block).forAllStates((BlockState state) -> ConfiguredModel.builder()
            .modelFile(state.getValue(lit) ? on : off)
            .rotationY(((int) state.getValue(facing).toYRot() + FACING_OFFSET) % 360)
            .build());
    }

    // --- Géométrie 3D ---------------------------------------------------------

    /**
     * Racine d'un modèle écrit à la main. Hériter de {@code block/block} n'est PAS
     * cosmétique : c'est lui qui porte les transformations d'affichage (inventaire,
     * main, sol, cadre). Un modèle à éléments sans parent perd ces transformations et
     * le bloc apparaît de travers et mal dimensionné dans l'inventaire, alors qu'il est
     * parfaitement correct une fois posé — d'où un bug qu'on ne voit jamais en regardant
     * le monde.
     */
    private static final ModelFile.UncheckedModelFile BLOCK_ROOT =
        new ModelFile.UncheckedModelFile("block/block");


    /**
     * Corps d'une machine : un cube plein, plus une <b>lunette en relief</b> autour de
     * la façade.
     *
     * <p>Pourquoi du volume et pas seulement une texture : une texture de lunette
     * dessine l'ombre d'un creux, mais l'ombre ne bouge pas quand le joueur bouge. Un
     * relief réel prend la lumière du monde, se découpe sur le fond, et projette son
     * ombre — le bloc cesse d'être une image collée sur un cube. Les quatre barres
     * sortent d'un demi-pixel seulement : assez pour accrocher la lumière, assez peu
     * pour ne pas s'encastrer dans un bloc voisin.
     *
     * <p>Le relief est <b>uniquement sur la façade</b>, jamais sur les flancs : deux
     * machines côte à côte sont un cas courant en base, et des saillies latérales
     * s'interpénétreraient.
     */
    private ModelFile machineModel(String name, String chassisName, String frontTexture) {
        ResourceLocation side = modLoc("block/" + chassisName + "_side");
        ResourceLocation top = modLoc("block/" + chassisName + "_top");
        var b = models().getBuilder(name)
            .parent(BLOCK_ROOT)
            .texture("particle", side)
            .texture("side", side)
            .texture("top", top)
            .texture("front", modLoc("block/" + frontTexture));

        cube(b, 0, 0, 0, 16, 16, 16, "#side", "#top", "#front", true);

        // La lunette : quatre barres autour de la fenêtre, en saillie de 0,5.
        final float o = 2.5f, i = 13.5f, th = 1.0f, d = -0.5f;
        bar(b, o, i - th, d, i, i, 0);        // haut
        bar(b, o, o, d, i, o + th, 0);        // bas
        bar(b, o, o + th, d, o + th, i - th, 0); // gauche
        bar(b, i - th, o + th, d, i, i - th, 0); // droite
        return b;
    }

    /**
     * La console d'attunement : un pupitre à écran <b>incliné</b>, pas un cube.
     *
     * <p>C'est le bloc le plus important du jeu au moment où on le rencontre — c'est
     * lui qui ouvre le T2 — et il est posé au milieu de gravats. Un cube s'y serait
     * fondu ; un pupitre incliné se lit comme du mobilier, donc comme quelque chose
     * qu'on peut manipuler. La pente est ce qui dit « ceci s'utilise », avant même
     * que l'écran soit lisible.
     */
    private ModelFile consoleModel() {
        ResourceLocation shell = modLoc("block/" + FRACTURED + "_side");
        ResourceLocation top = modLoc("block/" + FRACTURED + "_top");
        ResourceLocation screen = modLoc("block/attunement_console_front");
        var b = models().getBuilder("attunement_console")
            .parent(BLOCK_ROOT)
            .texture("particle", screen)
            .texture("side", shell)
            .texture("top", top)
            .texture("screen", screen);

        // Socle massif.
        cube(b, 0, 0, 0, 16, 5, 16, "#side", "#top", "#side", false);
        // Écran incliné vers l'arrière : la face du haut porte les glyphes.
        var el = b.element().from(2, 5, 4).to(14, 14, 8);
        el.rotation().origin(8, 5, 8).axis(net.minecraft.core.Direction.Axis.X)
            .angle(-22.5f).rescale(true).end();
        for (Direction dir : Direction.values()) {
            el.face(dir).texture(dir == Direction.SOUTH ? "#screen" : "#side").end();
        }
        el.end();
        return b;
    }

    /** Un pavé texturé sur toutes ses faces, avec culling optionnel (corps plein). */
    private void cube(net.neoforged.neoforge.client.model.generators.BlockModelBuilder b,
                      float x1, float y1, float z1, float x2, float y2, float z2,
                      String sideTex, String topTex, String frontTex, boolean cull) {
        var el = b.element().from(x1, y1, z1).to(x2, y2, z2);
        for (Direction dir : Direction.values()) {
            String tex = switch (dir) {
                case UP, DOWN -> topTex;
                case NORTH -> frontTex;
                default -> sideTex;
            };
            var face = el.face(dir).texture(tex);
            if (cull) {
                face.cullface(dir);
            }
            face.end();
        }
        el.end();
    }

    /** Une barre de lunette : texturée en métal de châssis sur toutes ses faces. */
    private void bar(net.neoforged.neoforge.client.model.generators.BlockModelBuilder b,
                     float x1, float y1, float z1, float x2, float y2, float z2) {
        var el = b.element().from(x1, y1, z1).to(x2, y2, z2);
        for (Direction dir : Direction.values()) {
            el.face(dir).texture("#side").end();
        }
        el.end();
    }
}
