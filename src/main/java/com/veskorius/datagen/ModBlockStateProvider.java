package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractMachineBlock;
import com.veskorius.block.FieldEmitterBlock;
import com.veskorius.block.FieldSensitiveBlock;
import com.veskorius.block.ConnectedFrame;
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
        machine(ModBlocks.RESONANCE_STABILIZER.get(), "resonance_stabilizer", FRACTURED, PLINTH);
        machine(ModBlocks.COMPONENT_ASSEMBLER.get(), "component_assembler", FRACTURED, PRESS);
        machine(ModBlocks.RESONANCE_WHETSTONE.get(), "resonance_whetstone", FRACTURED, WHEEL);
        machine(ModBlocks.CRYSTAL_CRUSHER.get(), "crystal_crusher", FRACTURED, JAWS);
        machine(ModBlocks.FLUX_PURIFIER.get(), "flux_purifier", ATTUNED, TANK);
        machine(ModBlocks.CRYSTAL_ROOST.get(), "crystal_roost", ATTUNED, NEST);
        machine(ModBlocks.DAMPING_ARRAY.get(), "damping_array", VESKORIAN, SLATS);
        machine(ModBlocks.VESKORIAN_ALLOY_FORGE.get(), "veskorian_alloy_forge", VESKORIAN, PRESS);
        machine(ModBlocks.FLUX_COMPRESSOR.get(), "flux_compressor", VESKORIAN, RAM);
        machine(ModBlocks.RECLAIMER.get(), "reclaimer", VESKORIAN, FUNNEL);
        machine(ModBlocks.ADVANCED_ASSEMBLER.get(), "advanced_assembler", VESKORIAN, PRESS);
        machine(ModBlocks.DEEP_SYNTHESIS_CHAMBER.get(), "deep_synthesis_chamber", VESKORIAN, TANK);
        machine(ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get(), "automated_extraction_array", VESKORIAN, GANTRY);
        machine(ModBlocks.STRUCTURAL_SYNTHESIZER.get(), "structural_synthesizer", VESKORIAN, MOLD);
        machine(ModBlocks.DEEP_CRYSTAL_DRILLER.get(), "deep_crystal_driller", VESKORIAN, DERRICK);

        // --- Émetteurs de champ ----------------------------------------------
        // Même traitement, sur la propriété LIT ajoutée à FieldEmitterBlock : un
        // émetteur alimenté se distingue désormais d'un émetteur à sec au premier
        // coup d'œil, sans attendre une bouffée de particules.
        emitter(ModBlocks.FIELD_EMITTER.get(), "field_emitter", ATTUNED);
        emitter(ModBlocks.TUNABLE_FIELD_EMITTER.get(), "tunable_field_emitter", ATTUNED);

        // Le relais est un émetteur, mais il ne doit PAS ressembler à un émetteur : posé au
        // milieu d'un trajet, on doit savoir de loin s'il faut le ravitailler (émetteur) ou
        // seulement remonter sa chaîne (relais). D'où un MÂT là où l'émetteur est une tour —
        // même famille, silhouette opposée : mince et haut contre large et tassé.
        relay(ModBlocks.RESONANCE_RELAY.get(), "resonance_relay", VESKORIAN);
        // L'évent n'est pas une machine à cycle : même famille de propriétés, silhouette
        // de CHEMINÉE — on doit lire « ça évacue » et non « ça fabrique ».
        vent(ModBlocks.SLAG_VENT.get(), "slag_vent", VESKORIAN);
        // L'amplificateur reprend le MÂT du relais : c'est la même famille d'appareil, et
        // le joueur doit lire « réseau » avant de lire « lequel ». Sa façade les sépare.
        relay(ModBlocks.HARMONIC_AMPLIFIER.get(), "harmonic_amplifier", VESKORIAN);
        // Le Hub n'émet rien et ne transporte rien : il ARBITRE. Silhouette de borne
        // trapue, ni tour ni mât — on ne doit pas le chercher des yeux comme une source.
        vent(ModBlocks.RESONANCE_NETWORK_HUB.get(), "resonance_network_hub", VESKORIAN);
        // Le Core est le seul bloc du mod qui soit un MONUMENT : socle large, corps
        // massif, couronne. Il doit se voir de loin au milieu de son anneau — c'est la
        // pièce autour de laquelle on bâtit, pas une machine qu'on range contre un mur.
        core(ModBlocks.CONVERGENCE_CORE.get(), "convergence_core", VESKORIAN);
        // L'Ancre : silhouette de PIEU, la seule du mod. On la plante en bordure, elle
        // doit se lire comme quelque chose qu'on enfonce, pas qu'on pose.
        vent(ModBlocks.RIFT_ANCHOR.get(), "rift_anchor", VESKORIAN);
        machine(ModBlocks.RIFT_CORE_EXTRACTOR.get(), "rift_core_extractor", VESKORIAN, DERRICK);
        // Le Ward est une TOUR d'émission : il rayonne une protection, exactement comme un
        // émetteur rayonne un champ. Même geste, même silhouette.
        emitter(ModBlocks.RIFT_WARD_EMITTER.get(), "rift_ward_emitter", VESKORIAN);

        // --- Châssis nus ------------------------------------------------------
        // Le bloc de base, posable tel quel. C'est littéralement le boîtier que
        // portent les machines de son palier : côtés et dessus sont les MÊMES fichiers.
        connectedChassis(ModBlocks.FRACTURED_CHASSIS.get(), FRACTURED);
        connectedChassis(ModBlocks.ATTUNED_CHASSIS.get(), ATTUNED);
        connectedChassis(ModBlocks.VESKORIAN_CHASSIS.get(), VESKORIAN);

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
        simpleBlock(ModBlocks.SIGMA_CONSOLE.get(), sigmaConsoleModel());
        simpleBlock(ModBlocks.ARCHIVE_CONSOLE.get(), consoleModel("archive_console", VESKORIAN));
        simpleBlock(ModBlocks.ARCHIVE_PEDESTAL.get(), pedestalModel());
        // Relais endommagé : la silhouette de l'émetteur, sur le châssis T3. Le joueur
        // doit reconnaître un appareil de réseau avant de savoir ce qu'il fait.
        emitter(ModBlocks.DAMAGED_RELAY.get(), "damaged_relay", VESKORIAN);

        // --- Architecture de donjon (17-Dungeons.md §4) -----------------------
        architecture();
    }

    /**
     * La maçonnerie veskorienne et ses trois blocs réactifs au champ.
     *
     * <p>Regroupé dans sa propre méthode parce que ces blocs ne suivent pas la grammaire
     * des machines (pas de façade orientée, pas de châssis) : ils suivent celle des blocs
     * de construction vanilla, et c'est délibéré — un mur doit se poser et s'assembler
     * comme le joueur s'y attend, sinon on ne bâtit pas avec.
     */
    private void architecture() {
        simpleBlockWithItem(ModBlocks.DEFORMED_STONE.get(),
            cubeAll(ModBlocks.DEFORMED_STONE.get()));
        simpleBlock(ModBlocks.RIFT_CORE.get(), cubeAll(ModBlocks.RIFT_CORE.get()));
        simpleBlockWithItem(ModBlocks.VESKORIAN_ALLOY_BLOCK.get(),
            cubeAll(ModBlocks.VESKORIAN_ALLOY_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.SYNTHESIS_RESIDUE_BLOCK.get(),
            cubeAll(ModBlocks.SYNTHESIS_RESIDUE_BLOCK.get()));
        bloomBush();
        simpleBlockWithItem(ModBlocks.RESONANCE_SAND.get(),
            cubeAll(ModBlocks.RESONANCE_SAND.get()));
        // TRANSLUCENT et non cutout : la texture a des pixels semi-transparents sur ses
        // reflets. En cutout ils seraient arrondis à « opaque ou rien », et la vitre
        // perdrait exactement ce qui la fait lire comme une vitre.
        connectedGlass(ModBlocks.RESONANCE_GLASS.get(), "resonance_glass");
        // Le cratère n'a PAS d'objet : il n'existe que pendant un orage et se ramasse
        // en fragment. Lui donner un item le rendrait posable, donc stockable — et le
        // « rien ne s'accumule » de l'événement tomberait.
        simpleBlock(ModBlocks.METEORIC_CRATER.get(),
            models().cubeAll("meteoric_crater", modLoc("block/meteoric_crater"))
                .renderType("cutout"));
        simpleBlockWithItem(ModBlocks.ANCIENT_CONDUIT_STONE.get(),
            cubeAll(ModBlocks.ANCIENT_CONDUIT_STONE.get()));
        connectedGlass(ModBlocks.LUMINOUS_RESONANCE_GLASS.get(), "luminous_resonance_glass");
        simpleBlockWithItem(ModBlocks.VEINED_STONE_BRICKS.get(),
            cubeAll(ModBlocks.VEINED_STONE_BRICKS.get()));
        simpleBlockWithItem(ModBlocks.CRACKED_VEINED_STONE_BRICKS.get(),
            cubeAll(ModBlocks.CRACKED_VEINED_STONE_BRICKS.get()));
        simpleBlockWithItem(ModBlocks.CHISELED_VEINED_STONE.get(),
            cubeAll(ModBlocks.CHISELED_VEINED_STONE.get()));

        ResourceLocation brick = modLoc("block/veined_stone_bricks");
        stairsBlock(ModBlocks.VEINED_STONE_BRICK_STAIRS.get(), brick);
        slabBlock(ModBlocks.VEINED_STONE_BRICK_SLAB.get(), brick, brick);
        wallBlock(ModBlocks.VEINED_STONE_BRICK_WALL.get(), brick);
        // Les escaliers/dalles/murs génèrent plusieurs modèles ; l'objet doit pointer
        // celui qui les représente en main, sinon il apparaît en cube plein.
        itemModels().withExistingParent("veined_stone_brick_stairs", modLoc("block/veined_stone_brick_stairs"));
        itemModels().withExistingParent("veined_stone_brick_slab", modLoc("block/veined_stone_brick_slab"));
        itemModels().wallInventory("veined_stone_brick_wall", brick);

        // Colonne cannelée : un bloc à axe, comme une bûche. C'est elle qui fait les
        // colonnades, donc les monuments.
        axisBlock(ModBlocks.VEINED_STONE_COLUMN.get(),
            modLoc("block/veined_stone_column"), modLoc("block/veined_stone_column_top"));
        itemModels().withExistingParent("veined_stone_column", modLoc("block/veined_stone_column"));

        // Lampe et conduit : deux états sur POWERED, comme les machines sur LIT. C'est
        // le même contrat de lecture (une façade éteinte, une façade allumée) appliqué
        // aux murs — c'est ce qui fait qu'un donjon alimenté se lit d'un coup d'œil.
        poweredCube(ModBlocks.RESONANCE_LAMP.get(), "resonance_lamp", null);
        poweredConduit();

        simpleBlockWithItem(ModBlocks.DISSONANCE_BLOOM.get(),
            models().cubeAll("dissonance_bloom", modLoc("block/dissonance_bloom"))
                .renderType("cutout"));

        bulkhead();
        // L'émetteur ancien réutilise la silhouette de tour de l'émetteur T2 : c'est la
        // même machine, il doit avoir la même allure (17-Dungeons.md §5.1).
        emitter(ModBlocks.ANCIENT_EMITTER.get(), "ancient_emitter", FRACTURED);
    }

    /**
     * Cube à deux états d'alimentation. {@code topTexture} non nul monte le dessus et le
     * dessous sur une autre texture : le conduit est une <b>assise de maçonnerie</b>
     * traversée par une gouttière, pas un cube de conduit — vu de dessus, il doit
     * disparaître dans le mur.
     */
    private void poweredCube(Block block, String name, String topTexture) {
        ModelFile off = poweredModel(name, name, topTexture);
        ModelFile on = poweredModel(name + "_on", name + "_on", topTexture);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
            .modelFile(state.getValue(FieldSensitiveBlock.POWERED) ? on : off).build());
        itemModels().withExistingParent(name, modLoc("block/" + name));
    }

    /**
     * Conduit : deux états d'alimentation × trois axes. L'axe n'est pas du décor — un
     * conduit est un <b>tracé</b>, et sans lui une descente verticale affichait une
     * gouttière horizontale à chaque bloc, si bien que le tuyau avait l'air haché en
     * travers tous les mètres.
     */
    private void poweredConduit() {
        ModelFile off = models().cubeColumn("conduit_line",
            modLoc("block/conduit_line"), modLoc("block/conduit_line_end"));
        ModelFile on = models().cubeColumn("conduit_line_on",
            modLoc("block/conduit_line_on"), modLoc("block/conduit_line_end_on"));
        getVariantBuilder(ModBlocks.CONDUIT_LINE.get()).forAllStates(state -> {
            ConfiguredModel.Builder<?> model = ConfiguredModel.builder()
                .modelFile(state.getValue(FieldSensitiveBlock.POWERED) ? on : off);
            return switch (state.getValue(com.veskorius.block.ConduitLineBlock.AXIS)) {
                case X -> model.rotationX(90).rotationY(90).build();
                case Z -> model.rotationX(90).build();
                default -> model.build();
            };
        });
        itemModels().withExistingParent("conduit_line", modLoc("block/conduit_line"));
    }

    private ModelFile poweredModel(String name, String texture, String topTexture) {
        return topTexture == null
            ? models().cubeAll(name, modLoc("block/" + texture))
            : models().cubeBottomTop(name, modLoc("block/" + texture),
                modLoc("block/" + topTexture), modLoc("block/" + topTexture));
    }

    /**
     * Sas : plaque pleine fermée, seuil de 2 px ouvert. Les deux modèles portent la même
     * texture de base et se distinguent par leur <b>volume</b> — c'est le mouvement de la
     * plaque qu'on doit lire, pas un changement de couleur.
     */
    private void bulkhead() {
        ModelFile closed = models().cubeAll("resonance_bulkhead", modLoc("block/resonance_bulkhead"));
        ModelFile open = models().getBuilder("resonance_bulkhead_open")
            .parent(BLOCK_ROOT)
            .texture("all", modLoc("block/resonance_bulkhead_open"))
            .texture("particle", modLoc("block/resonance_bulkhead_open"))
            .element().from(0, 0, 0).to(16, 2, 16)
            .allFaces((dir, face) -> face.texture("#all")).end();
        getVariantBuilder(ModBlocks.RESONANCE_BULKHEAD.get()).forAllStates(state ->
            ConfiguredModel.builder()
                .modelFile(state.getValue(FieldSensitiveBlock.POWERED) ? open : closed).build());
    }

    // --- Châssis de palier ---------------------------------------------------
    // Le nom du châssis suffit à retrouver ses deux textures : une machine déclare
    // son palier, pas ses fichiers. Ajouter une machine T2 ne demande donc aucune
    // texture de boîtier — c'est tout l'intérêt du système.

    private static final String FRACTURED = "fractured_chassis";
    private static final String ATTUNED = "attuned_chassis";
    private static final String VESKORIAN = "veskorian_chassis";

    /**
     * Le buisson de floraison : une CROIX par stade de croissance.
     *
     * <p>La croix plutôt qu'un cube, parce que c'est la forme que Minecraft a apprise à
     * ses joueurs pour « ceci est une plante, ça se traverse ». En cutout, sinon le
     * fond transparent de la texture s'affiche en noir.
     */
    private void bloomBush() {
        var block = ModBlocks.RESONANCE_BLOOM_BUSH.get();
        getVariantBuilder(block).forAllStates(state -> {
            int age = state.getValue(
                com.veskorius.block.ResonanceBloomBushBlock.AGE);
            return ConfiguredModel.builder()
                .modelFile(models().cross("resonance_bloom_bush_stage" + age,
                    modLoc("block/resonance_bloom_bush_stage" + age)).renderType("cutout"))
                .build();
        });
    }

    /**
     * <b>Verre connecté</b> : un cube sans bordure, plus une bague de cadre par côté où
     * le verre s'arrête.
     *
     * <p><b>Multipart plutôt que soixante-quatre modèles.</b> Six côtés valent 2⁶ états ;
     * les écrire un par un aurait donné soixante-quatre fichiers à maintenir pour une
     * règle qui tient en une phrase. Le multipart compose : le cube toujours, chaque
     * bague seulement quand son côté est ouvert.
     *
     * <p><b>Une bague est un carré de quatre barres</b>, posé au bord du bloc, sur les
     * quatre faces perpendiculaires à sa direction. C'est ce qui fait qu'un mur de verre
     * n'est encadré qu'à son pourtour : à l'intérieur, chaque côté touche du verre, donc
     * aucune bague n'est posée.
     */
    /**
     * <b>Un bloc a cadre connecte.</b> On ecrit ici les MORCEAUX — un fond, douze baguettes
     * d'arete, vingt-quatre quarts de cadre — et rien qui dise quand les poser.
     *
     * <p><b>Le blockstate ne pointe que sur le fond.</b> C'est {@code ConnectedFrameModel}
     * qui assemble le cadre a la construction du chunk, en lisant le voisinage reel. Un
     * multipart ne pouvait pas : l'arete concave et le coin rentrant demandent de connaitre
     * les DIAGONALES, soit dix-huit booleens, soit 262 144 etats par bloc au lieu de 64.
     *
     * <p><b>Une seule methode pour le verre et pour les chassis.</b> Les deux ont eu leur
     * propre version, donc le meme raisonnement ecrit deux fois — et une seule moitie
     * corrigee a chaque defaut trouve. Ce qui les distingue tient en trois parametres : la
     * texture du cadre, son epaisseur, et la couche de rendu.
     *
     * <p>Les noms des morceaux viennent de {@link ConnectedFrame} : la datagen les ecrit, le
     * modele les reclame, et un desaccord d'un caractere rendrait le bloc invisible sans la
     * moindre erreur.
     *
     * @param frameTexture la texture des baguettes
     * @param thickness    leur epaisseur en pixels
     * @param renderType   {@code null} pour la couche solide par defaut
     */
    private void connectedFrame(Block block, String name, ModelFile base,
                                String frameTexture, float thickness, String renderType) {
        simpleBlock(block, base);
        ConnectedFrame.forEachEdge((a, b) ->
            framePiece(ConnectedFrame.barName(name, a, b), frameTexture, renderType,
                barBox(a, b, thickness)));
        ConnectedFrame.forEachFaceCorner((f, p, q) ->
            framePiece(ConnectedFrame.cornerName(name, f, p, q), frameTexture, renderType,
                cornerBox(f, p, q, thickness)));
    }

    /**
     * <b>La baguette d'une arete</b> : {@code thickness} pixels sur les deux axes des faces
     * {@code a} et {@code b}, toute la longueur sur le troisieme.
     *
     * <p><b>Le debord est minuscule et decale par axe long</b> (0,02 a 0,04 px). Il ne sert
     * qu'a decoller la baguette du fond : deux surfaces au meme plan se disputent la
     * profondeur et clignotent. Le decalage, lui, evite que deux baguettes se retrouvent
     * coplanaires dans un angle, la ou trois se rejoignent.
     *
     * <p>Une version debordait d'un demi-pixel « pour le relief » : a la jonction de deux
     * blocs non connectes, les deux cages se chevauchaient alors sur un pixel entier et un
     * mur entier scintillait.
     */
    private static float[][] barBox(Direction a, Direction b, float t) {
        int longAxis = 3 - a.getAxis().ordinal() - b.getAxis().ordinal();
        float over = 0.02f + 0.01f * longAxis;
        float[] from = {-over, -over, -over};
        float[] to = {16 + over, 16 + over, 16 + over};
        for (Direction d : new Direction[] {a, b}) {
            int axis = d.getAxis().ordinal();
            boolean high = d.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            from[axis] = high ? 16 - t : -over;
            to[axis] = high ? 16 + over : t;
        }
        return new float[][] {from, to};
    }

    /**
     * <b>Le quart de cadre d'un coin rentrant</b> : un carre pose sur la face {@code f}, dans
     * l'angle entre {@code p} et {@code q}.
     *
     * <p>C'est la piece qui manquait, et le defaut se voyait tres bien : dans une disposition
     * en L, le bloc de l'angle touche ses deux voisins, ne dessine donc aucune bordure — et
     * son coin restait nu au milieu du cadre. Aucune baguette ne pouvait le couvrir, puisqu'une
     * baguette n'existe que la ou le voisin manque.
     *
     * <p>Il ne deborde QUE du cote de {@code f}. Vers {@code p} et {@code q} il y a un voisin
     * — c'est la condition meme de sa presence — donc rien a decoller.
     */
    private static float[][] cornerBox(Direction f, Direction p, Direction q, float t) {
        final float over = 0.02f;
        float[] from = {0, 0, 0};
        float[] to = {16, 16, 16};
        for (Direction d : new Direction[] {p, q}) {
            int axis = d.getAxis().ordinal();
            boolean high = d.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            from[axis] = high ? 16 - t : 0;
            to[axis] = high ? 16 : t;
        }
        int fa = f.getAxis().ordinal();
        boolean fh = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        from[fa] = fh ? 16 - t : -over;
        to[fa] = fh ? 16 + over : t;
        return new float[][] {from, to};
    }

    /**
     * Un morceau de cadre : une boite, une texture, et des UV BORNEES A LA TUILE.
     *
     * <p>Le bornage est le noeud du probleme. L'UV automatique de Minecraft se deduit des
     * coordonnees ; une geometrie qui deborde du cube sort donc aussi de sa tuile et va lire
     * la texture VOISINE dans l'atlas — un lisere clair sur chaque bord. Or les morceaux
     * DOIVENT deborder un peu, sinon ils sont coplanaires avec le fond et clignotent. Les
     * deux contraintes ne se concilient qu'en dissociant la geometrie de l'UV.
     *
     * <p>Reprendre la texture du bloc lui-meme, et non un metal a part, fait le reste : l'UV
     * fait tomber chaque morceau exactement sur la bordure qu'il represente, si bien qu'un
     * bloc isole redessine le bloc en main.
     */
    private ModelFile framePiece(String name, String texture, String renderType, float[][] box) {
        final float[] from = box[0];
        final float[] to = box[1];
        var builder = models().getBuilder(name)
            .parent(models().getExistingFile(mcLoc("block/block")))
            .texture("frame", modLoc("block/" + texture))
            .texture("particle", modLoc("block/" + texture));
        if (renderType != null) {
            builder = builder.renderType(renderType);
        }
        return builder
            .element().from(from[0], from[1], from[2]).to(to[0], to[1], to[2])
            .allFaces((face, f) -> {
                float[] uv = faceUvs(face, from, to);
                f.texture("#frame").uvs(uv[0], uv[1], uv[2], uv[3]);
            }).end();
    }

    /**
     * L'UV que Minecraft deduirait tout seul de la boite, pour une face donnee — recalculee
     * pour pouvoir la BORNER a la tuile.
     */
    private static float[] faceUvs(Direction face, float[] f, float[] t) {
        float[] uv = switch (face) {
            case DOWN -> new float[] {f[0], 16 - t[2], t[0], 16 - f[2]};
            case UP -> new float[] {f[0], f[2], t[0], t[2]};
            case NORTH -> new float[] {16 - t[0], 16 - t[1], 16 - f[0], 16 - f[1]};
            case SOUTH -> new float[] {f[0], 16 - t[1], t[0], 16 - f[1]};
            case WEST -> new float[] {f[2], 16 - t[1], t[2], 16 - f[1]};
            case EAST -> new float[] {16 - t[2], 16 - t[1], 16 - f[2], 16 - f[1]};
        };
        for (int i = 0; i < uv.length; i++) {
            uv[i] = Math.max(0f, Math.min(16f, uv[i]));
        }
        return uv;
    }

    /**
     * Verre connecte : une vitre lisse, et un cadre d'un pixel sur les seules aretes qui
     * bordent le vide.
     *
     * <p>Le cadre est en CUTOUT et non en translucide : un cadre est opaque, et le sortir du
     * tri des surfaces transparentes supprime une cause de scintillement.
     */
    private void connectedGlass(Block block, String name) {
        ModelFile pane = models().cubeAll(name + "_pane", modLoc("block/" + name + "_pane"))
            .renderType("translucent");
        connectedFrame(block, name, pane, name + "_frame", 1.0f, "cutout");
        // L'objet montre le verre ENCADRE, pas la plaque nue : dans un inventaire, un carre
        // de reflets sans bord ne se lit pas comme du verre.
        itemModels().cubeAll(name, modLoc("block/" + name));
    }

    /**
     * Caisson connecte : une plaque, et un cadre de trois pixels.
     *
     * <p>Trois pixels, et ce chiffre est PARTAGE avec le generateur de textures
     * (tools/block-textures/marble.js, CASING_WIDTH) : le cadre peint tient tout entier dans
     * les trois premiers pixels du bord. Un morceau plus mince n'en reproduirait qu'une
     * partie, et le bloc pose cesserait de ressembler au bloc en main.
     *
     * <p>La plaque est un cube PLEIN, de 0 a 16. L'avoir reculee de 0,05 px pour la departir
     * des baguettes ouvrait une fente de 0,1 px entre deux caissons, que rien ne fermait
     * puisque les faces partagees sont cullees : on voyait le ciel a travers le mur.
     */
    private void connectedChassis(Block block, String name) {
        ModelFile plate = models().cubeAll(name + "_plate", modLoc("block/" + name + "_plate"));
        connectedFrame(block, name, plate, name + "_side", 3.0f, null);
        // L'objet montre le caisson COMPLET, cadre peint : dans un inventaire, une plaque nue
        // ne se distingue d'aucune autre plaque nue.
        itemModels().cubeBottomTop(name, modLoc("block/" + name + "_side"),
            modLoc("block/" + name + "_top"), modLoc("block/" + name + "_top"));
    }
    // --- Fabriques de modèles ------------------------------------------------

    private void machine(Block block, String name, String chassisName, Shape shape) {
        ModelFile off = shaped(name, chassisName, name + "_front", shape);
        ModelFile on = shaped(name + "_on", chassisName, name + "_front_on", shape);
        oriented(block, name, off, on, AbstractMachineBlock.FACING, AbstractMachineBlock.LIT);
    }

    /**
     * L'émetteur n'est pas un cube : c'est une <b>tour à trois étages</b> — socle,
     * coffret, tête d'émission. Il mérite sa propre silhouette parce que c'est le bloc
     * qu'on cherche des yeux dans une base : c'est lui qui alimente tout le reste, et
     * on doit le repérer de loin sans lire une seule texture. Tout tient dans le cube
     * (rien ne dépasse), donc on peut empiler ou construire autour sans surprise.
     */
    private void emitter(Block block, String name, String chassisName) {
        ModelFile off = shaped(name, chassisName, name + "_front", TOWER);
        ModelFile on = shaped(name + "_on", chassisName, name + "_front_on", TOWER);
        oriented(block, name, off, on, FieldEmitterBlock.FACING, FieldEmitterBlock.LIT);
    }

    /** Même fabrique, silhouette de monument. */
    private void core(Block block, String name, String chassisName) {
        ModelFile off = shaped(name, chassisName, name + "_front", MONUMENT);
        ModelFile on = shaped(name + "_on", chassisName, name + "_front_on", MONUMENT);
        oriented(block, name, off, on, FieldEmitterBlock.FACING, FieldEmitterBlock.LIT);
    }

    /** Même fabrique, silhouette de cheminée. */
    private void vent(Block block, String name, String chassisName) {
        ModelFile off = shaped(name, chassisName, name + "_front", STACK);
        ModelFile on = shaped(name + "_on", chassisName, name + "_front_on", STACK);
        oriented(block, name, off, on, FieldEmitterBlock.FACING, FieldEmitterBlock.LIT);
    }

    /** Même fabrique, silhouette de mât. Voir la note à l'appel. */
    private void relay(Block block, String name, String chassisName) {
        ModelFile off = shaped(name, chassisName, name + "_front", MAST);
        ModelFile on = shaped(name + "_on", chassisName, name + "_front_on", MAST);
        oriented(block, name, off, on, FieldEmitterBlock.FACING, FieldEmitterBlock.LIT);
    }



    /**
     * Deux modèles orientables — façade éteinte et façade allumée — et une variante par
     * couple (orientation, état). Le modèle « éteint » garde le nom nu du bloc : c'est
     * lui que réutilisent les modèles d'objet, qui n'ont donc pas à changer.
     */
    /**
     * Une variante par couple (orientation, état allumé) — <b>et le modèle d'objet</b>.
     *
     * <p>Ce dernier était jusqu'ici recopié à la main dans {@code ModItemModelProvider},
     * une ligne par machine. Le piège est que rien ne signale l'oubli : la machine se pose,
     * se texture et fonctionne parfaitement, et seul son <b>objet</b> apparaît en cube
     * violet — dans l'inventaire et dans la main, jamais dans le monde. La Veskorian Alloy
     * Forge est partie ainsi. Le modèle d'objet est donc produit ici, par la méthode qui ne
     * peut pas ne pas être appelée : ajouter une machine sans son objet est désormais
     * impossible plutôt que seulement déconseillé.
     */
    private void oriented(Block block, String name, ModelFile off, ModelFile on,
                          DirectionProperty facing, BooleanProperty lit) {
        getVariantBuilder(block).forAllStates((BlockState state) -> ConfiguredModel.builder()
            .modelFile(state.getValue(lit) ? on : off)
            .rotationY(((int) state.getValue(facing).toYRot() + FACING_OFFSET) % 360)
            .build());
        // Sauf pour les blocs de structure (émetteur ancien, relais endommagé), qui n'ont
        // volontairement aucun objet : leur en générer un serait un fichier orphelin.
        if (block.asItem() != net.minecraft.world.item.Items.AIR) {
            itemModels().withExistingParent(name, modLoc("block/" + name));
        }
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
     * Silhouettes de machine — <b>une forme par machine</b>, pas neuf cubes.
     *
     * <p>Une texture ne change jamais la silhouette. Or c'est elle qu'on reconnaît de
     * loin dans une base, avant même de distinguer une façade : neuf cubes identiques
     * texturés différemment restent neuf cubes. Chaque machine reçoit donc une
     * géométrie propre — socle, étage, creux traversant, colonne — et plusieurs ont un
     * <b>vrai vide</b> plutôt qu'un vide peint.
     *
     * <p>Deux conséquences à ne jamais oublier pour un bloc non plein, toutes deux
     * invisibles en regardant le monde : il faut {@code noOcclusion()} sur le bloc
     * (sinon Minecraft culle les faces voisines en les croyant cachées et on voit à
     * travers le monde par les creux), et une {@code VoxelShape} adaptée (sinon la
     * boîte de collision par défaut plante un mur invisible dans le vide).
     */
    @FunctionalInterface
    private interface Shape {
        void build(ModBlockStateProvider p, net.neoforged.neoforge.client.model.generators.BlockModelBuilder b);
    }

    /** Le socle plein + un étage en retrait : la forme « appareil posé ». */
    private static final Shape PLINTH = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 3, 16, "#side", "#top", "#side", false);
        p.cube(b, 2, 3, 2, 14, 13, 14, "#side", "#top", "#front", false);
        p.cube(b, 5, 13, 5, 11, 16, 11, "#front", "#top", "#front", false);
    };

    /** Presse à étages : massive, tassée vers le bas. */
    private static final Shape PRESS = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 12, 16, "#side", "#top", "#front", false);
        p.cube(b, 1, 12, 1, 15, 16, 15, "#side", "#top", "#side", false);
    };

    /** Roue sur son bâti : une lame verticale, lisible de profil comme de face. */
    private static final Shape WHEEL = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 5, 16, "#side", "#top", "#side", false);
        p.cube(b, 6, 5, 2, 10, 15, 14, "#front", "#side", "#front", false);
        p.cube(b, 1, 5, 5, 6, 9, 11, "#side", "#side", "#side", false);
        p.cube(b, 10, 5, 5, 15, 9, 11, "#side", "#side", "#side", false);
    };

    /** Deux mâchoires séparées par un VIDE traversant : la forme la plus reconnaissable du lot. */
    private static final Shape JAWS = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 7, 16, "#side", "#top", "#front", false);
        p.cube(b, 0, 10, 0, 16, 16, 16, "#side", "#top", "#front", false);
        // Quatre montants qui tiennent la mâchoire haute — le reste est ouvert.
        for (int[] c : new int[][] {{0, 0}, {13, 0}, {0, 13}, {13, 13}}) {
            p.cube(b, c[0], 7, c[1], c[0] + 3, 10, c[1] + 3, "#side", "#side", "#side", false);
        }
    };

    /** Cuve : socle, colonne étroite, chapeau. La seule silhouette élancée. */
    private static final Shape TANK = (p, b) -> {
        p.cube(b, 1, 0, 1, 15, 3, 15, "#side", "#top", "#side", false);
        p.cube(b, 3, 3, 3, 13, 13, 13, "#front", "#top", "#front", false);
        p.cube(b, 1, 13, 1, 15, 16, 15, "#side", "#top", "#side", false);
    };

    /** Caisse à nichoir : l'ouverture est RÉELLEMENT évidée, pas peinte. */
    private static final Shape NEST = (p, b) -> {
        p.cube(b, 0, 0, 0, 3, 16, 16, "#side", "#top", "#front", false);   // paroi gauche
        p.cube(b, 13, 0, 0, 16, 16, 16, "#side", "#top", "#front", false); // paroi droite
        p.cube(b, 3, 0, 0, 13, 3, 16, "#side", "#top", "#front", false);   // plancher
        p.cube(b, 3, 13, 0, 13, 16, 16, "#side", "#top", "#front", false); // linteau
        p.cube(b, 3, 3, 9, 13, 13, 16, "#side", "#side", "#side", false);  // fond de la niche
    };

    /** Grille : des lames espacées, donc des trous traversants. */
    private static final Shape SLATS = (p, b) -> {
        p.cube(b, 0, 0, 0, 2, 16, 16, "#side", "#side", "#front", false);
        p.cube(b, 14, 0, 0, 16, 16, 16, "#side", "#side", "#front", false);
        for (int y = 0; y < 16; y += 4) {
            p.cube(b, 2, y, 2, 14, y + 2, 14, "#front", "#top", "#front", false);
        }
    };

    /** Tour d'émission : socle large, coffret, tête, nœud. */
    private static final Shape TOWER = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 3, 16, "#side", "#top", "#side", false);
        p.cube(b, 1, 3, 1, 15, 12, 15, "#side", "#top", "#front", false);
        p.cube(b, 3, 12, 3, 13, 15, 13, "#side", "#top", "#side", false);
        p.cube(b, 6, 15, 6, 10, 16, 10, "#top", "#top", "#top", false);
    };

    /**
     * Mât relais : semelle étroite, fût mince, deux bras en croix, tête. C'est la seule
     * silhouette du mod qui laisse voir le décor <b>à travers son milieu</b> sur toute la
     * largeur du bloc — un appareil qu'on plante en terrain découvert et qu'on doit repérer
     * de très loin, à contre-jour, en ne lisant que sa découpe.
     */
    private static final Shape MAST = (p, b) -> {
        p.cube(b, 3, 0, 3, 13, 2, 13, "#side", "#top", "#side", false);   // semelle
        p.cube(b, 6, 2, 6, 10, 11, 10, "#front", "#top", "#front", false); // fût
        p.bar(b, 1, 8, 7, 15, 9, 9);                                       // bras est-ouest
        p.bar(b, 7, 10, 1, 9, 11, 15);                                     // bras nord-sud
        p.cube(b, 5, 11, 5, 11, 14, 11, "#side", "#top", "#front", false); // tête
        p.cube(b, 7, 14, 7, 9, 16, 9, "#top", "#top", "#top", false);      // pointe
    };

    /**
     * Presse à mouton : une enclume massive et un bélier suspendu, séparés par un VIDE.
     * C'est le vide qui dit « ça écrase » — une presse pleine serait un cube de plus.
     */
    private static final Shape RAM = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 5, 16, "#side", "#top", "#front", false);   // enclume
        p.cube(b, 1, 5, 1, 4, 16, 15, "#side", "#top", "#side", false);    // montant gauche
        p.cube(b, 12, 5, 1, 15, 16, 15, "#side", "#top", "#side", false);  // montant droit
        p.cube(b, 4, 9, 4, 12, 14, 12, "#front", "#top", "#front", false); // bélier suspendu
        p.cube(b, 0, 14, 0, 16, 16, 16, "#side", "#top", "#side", false);  // sommier
    };

    /**
     * Trémie : une gueule carrée qui se rétrécit vers un bac. La silhouette se lit de haut
     * en bas et non par symétrie — le Reclaimer ne transforme pas, il <b>trie</b>. Ce qui
     * entre par le large est informe, ce qui ressort par l'étroit est compté.
     *
     * <p>Deux étages, pas trois : un entonnoir trop détaillé redevient un cube à distance,
     * et c'est à distance qu'on doit reconnaître la machine dans un atelier.
     */
    private static final Shape FUNNEL = (p, b) -> {
        p.cube(b, 0, 11, 0, 16, 16, 16, "#side", "#top", "#front", false);   // gueule
        p.cube(b, 3, 6, 3, 13, 11, 13, "#front", "#top", "#front", false);   // col
        p.cube(b, 1, 0, 1, 15, 6, 15, "#side", "#top", "#front", false);     // bac
    };

    /**
     * Moule ouvert : un bac large et bas, aux bords relevés, vide au centre. La seule
     * silhouette du lot qui soit plus large que haute — on doit lire « on coule dedans ».
     */
    private static final Shape MOLD = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 6, 16, "#side", "#top", "#front", false);   // socle
        p.cube(b, 0, 6, 0, 16, 11, 3, "#side", "#top", "#front", false);   // bord nord
        p.cube(b, 0, 6, 13, 16, 11, 16, "#side", "#top", "#front", false); // bord sud
        p.cube(b, 0, 6, 3, 3, 11, 13, "#side", "#top", "#side", false);    // bord ouest
        p.cube(b, 13, 6, 3, 16, 11, 13, "#side", "#top", "#side", false);  // bord est
        p.cube(b, 3, 6, 3, 13, 7, 13, "#front", "#front", "#front", false); // fond du bac
    };

    /**
     * Derrick : un bâti à quatre pieds, une tête, et un TREPAN QUI DESCEND SOUS LE BLOC.
     * C'est la seule silhouette du mod qui déborde volontairement vers le bas — elle dit
     * où la machine travaille, et une foreuse qui ne montre pas son outil ne se lit pas.
     */
    private static final Shape DERRICK = (p, b) -> {
        for (int[] c : new int[][] {{1, 1}, {12, 1}, {1, 12}, {12, 12}}) {
            p.cube(b, c[0], 2, c[1], c[0] + 3, 12, c[1] + 3, "#side", "#top", "#side", false);
        }
        p.cube(b, 0, 12, 0, 16, 16, 16, "#side", "#top", "#front", false); // tête
        p.cube(b, 6, 2, 6, 10, 12, 10, "#front", "#top", "#front", false); // arbre
        p.cube(b, 7, 0, 7, 9, 2, 9, "#front", "#front", "#front", false);  // trépan
    };

    /**
     * Cheminée : un fût qui se rétrécit vers le haut, ouvert au sommet. Elle ne ressemble
     * à aucune machine du mod parce qu'elle n'en est pas une — elle ne produit rien.
     */
    private static final Shape STACK = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 4, 16, "#side", "#top", "#front", false);
        p.cube(b, 2, 4, 2, 14, 10, 14, "#side", "#top", "#front", false);
        p.cube(b, 4, 10, 4, 12, 14, 12, "#side", "#top", "#side", false);
        // Couronne évidée : quatre parois, rien au milieu — la fumée sort par là.
        p.cube(b, 4, 14, 4, 12, 16, 5, "#side", "#side", "#side", false);
        p.cube(b, 4, 14, 11, 12, 16, 12, "#side", "#side", "#side", false);
        p.cube(b, 4, 14, 5, 5, 16, 11, "#side", "#side", "#side", false);
        p.cube(b, 11, 14, 5, 12, 16, 11, "#side", "#side", "#side", false);
    };

    /**
     * Portique : deux pieds, une poutre en haut, et TOUT LE MILIEU OUVERT. C'est la seule
     * silhouette du lot dont le vide traverse de part en part sur toute la hauteur — un
     * appareil sous lequel on passe, pas un appareil dans lequel on met quelque chose. Il
     * commande d'autres machines ; il ne doit pas ressembler à un four.
     */
    private static final Shape GANTRY = (p, b) -> {
        p.cube(b, 0, 0, 0, 4, 16, 16, "#side", "#top", "#front", false);   // pied gauche
        p.cube(b, 12, 0, 0, 16, 16, 16, "#side", "#top", "#front", false); // pied droit
        p.cube(b, 4, 12, 0, 12, 16, 16, "#side", "#top", "#front", false); // poutre
        p.bar(b, 4, 10, 7, 12, 12, 9);                                     // rail de translation
    };

    /**
     * Monument : socle débordant, corps massif, couronne en retrait. Aucune autre
     * silhouette du mod n'occupe autant de volume — c'est délibéré, c'est le seul bloc
     * autour duquel le joueur construit une figure de onze blocs de côté.
     */
    private static final Shape MONUMENT = (p, b) -> {
        p.cube(b, 0, 0, 0, 16, 4, 16, "#side", "#top", "#side", false);
        p.cube(b, 2, 4, 2, 14, 12, 14, "#side", "#top", "#front", false);
        p.cube(b, 4, 12, 4, 12, 14, 12, "#side", "#top", "#side", false);
        p.cube(b, 6, 14, 6, 10, 16, 10, "#front", "#top", "#front", false);
    };

    /** Assemble un modèle à partir d'une silhouette et d'un jeu de textures. */
    private ModelFile shaped(String name, String chassisName, String frontTexture, Shape shape) {
        ResourceLocation side = modLoc("block/" + chassisName + "_side");
        ResourceLocation top = modLoc("block/" + chassisName + "_top");
        var b = models().getBuilder(name)
            .parent(BLOCK_ROOT)
            .texture("particle", modLoc("block/" + frontTexture))
            .texture("side", side)
            .texture("top", top)
            .texture("front", modLoc("block/" + frontTexture));
        shape.build(this, b);
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
        return consoleModel("attunement_console", FRACTURED);
    }

    /**
     * Socle d'archive : base, fût, tablette. Une silhouette de <b>pupitre de
     * consultation</b> et non de machine — c'est un meuble de bibliothèque, et il doit se
     * distinguer au premier regard des consoles qui, elles, donnent quelque chose.
     */
    private ModelFile pedestalModel() {
        var b = models().getBuilder("archive_pedestal")
            .parent(BLOCK_ROOT)
            .texture("particle", modLoc("block/chiseled_veined_stone"))
            .texture("side", modLoc("block/" + VESKORIAN + "_side"))
            .texture("top", modLoc("block/chiseled_veined_stone"));
        cube(b, 1, 0, 1, 15, 4, 15, "#side", "#top", "#side", false);
        cube(b, 3, 4, 3, 13, 12, 13, "#side", "#side", "#side", false);
        cube(b, 1, 12, 1, 15, 14, 15, "#side", "#top", "#side", false);
        return b;
    }

    /**
     * La console du Sigma : <b>même pupitre, châssis de haute époque</b>. Deux blocs, une
     * seule silhouette — le geste qui ouvre un palier doit se reconnaître d'un coup d'œil
     * au T3 comme au T2, seule la matière dit qu'on a changé d'âge.
     */
    private ModelFile sigmaConsoleModel() {
        return consoleModel("sigma_console", VESKORIAN);
    }

    private ModelFile consoleModel(String name, String chassisName) {
        ResourceLocation shell = modLoc("block/" + chassisName + "_side");
        ResourceLocation top = modLoc("block/" + chassisName + "_top");
        ResourceLocation screen = modLoc("block/" + name + "_front");
        var b = models().getBuilder(name)
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
