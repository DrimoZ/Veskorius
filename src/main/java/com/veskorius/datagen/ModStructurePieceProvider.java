package com.veskorius.datagen;

import com.google.common.hash.Hashing;
import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.CodexEntries;
import com.veskorius.worldgen.ModStructures;
import com.veskorius.worldgen.ModWorldGen;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Génère les <b>pièces de structure</b> (NBT) des donjons veskoriens
 * (08-Structures.md pour le contenu, 17-Dungeons.md pour la forme).
 *
 * <p><b>Pourquoi le NBT est produit par code plutôt qu'édité dans un structure block :</b>
 * comme le reste du projet, aucune ressource n'est écrite à la main — les pièces dérivent
 * du code, donc restent alignées sur les registres (bloc de mur, console, sas) sans
 * risque de désynchronisation. Le format produit est exactement celui que
 * {@code StructureTemplate.load} relit.
 *
 * <p><b>Ce qui a changé le 2026-08-07 (17-Dungeons.md).</b> Les pièces étaient des salles
 * uniques et plates, identiques d'une ruine à l'autre, bâties en deepslate vanilla, et
 * sans un seul bloc jigsaw — la structure « en jigsaw » n'en était donc pas une. Elles
 * sont maintenant :
 * <ul>
 *   <li><b>en volume</b> : l'Avant-poste a trois paliers autour d'un puits traversant ;</li>
 *   <li><b>en maçonnerie veskorienne</b> (briques de pierre veinée, conduits, lampes),
 *       pas en pierre vanilla ;</li>
 *   <li><b>réellement assemblées</b> : chaque pièce porte des connecteurs, et les ailes
 *       facultatives se tirent d'un pool ;</li>
 *   <li><b>usées à la pose</b> par des processors (voir {@code ModProcessorLists}), donc
 *       jamais deux fois pareilles.</li>
 * </ul>
 *
 * <p><b>L'invariant qui gouverne tout ce fichier</b> (17-Dungeons.md §3) : le chemin
 * critique — console, sas, émetteur ancien, coffre d'amorçage, coffre-réserve — vit
 * <b>dans la pièce de départ</b>. Le pool ne sert qu'aux ailes facultatives. Un chemin
 * critique tiré au sort, c'est une progression suspendue à un dé ; le mod s'est déjà fait
 * prendre deux fois par cette classe de bug.
 */
public class ModStructurePieceProvider implements DataProvider {

    // Pièces de l'Avant-poste
    public static final String OUTPOST = "outpost";
    public static final String OUTPOST_WING_STORE = "outpost_wing_store";
    public static final String OUTPOST_WING_QUARTERS = "outpost_wing_quarters";
    public static final String OUTPOST_WING_COLLAPSED = "outpost_wing_collapsed";
    public static final String OUTPOST_CAP = "outpost_cap";

    // Pièces du Hameau (ex-« Habitation Modeste »)
    public static final String HAMLET = "hamlet";
    public static final String HAMLET_DWELLING = "hamlet_dwelling";
    public static final String HAMLET_WORKSHOP = "hamlet_workshop";
    public static final String HAMLET_COLLAPSED = "hamlet_collapsed";
    public static final String HAMLET_CAP = "hamlet_cap";

    private final PackOutput.PathProvider pathProvider;

    public ModStructurePieceProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK,
            StructureTemplateManager.STRUCTURE_RESOURCE_DIRECTORY_NAME);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<String, CompoundTag> pieces = new LinkedHashMap<>();
        pieces.put(OUTPOST, outpost());
        pieces.put(OUTPOST_WING_STORE, wingStore());
        pieces.put(OUTPOST_WING_QUARTERS, wingQuarters());
        pieces.put(OUTPOST_WING_COLLAPSED, wingCollapsed());
        pieces.put(OUTPOST_CAP, cap(WING_H, WING_D));
        pieces.put(HAMLET, hamlet());
        pieces.put(HAMLET_DWELLING, hamletDwelling());
        pieces.put(HAMLET_WORKSHOP, hamletWorkshop());
        pieces.put(HAMLET_COLLAPSED, hamletCollapsed());
        pieces.put(HAMLET_CAP, cap(HOUSE_H, HOUSE_D));
        return CompletableFuture.allOf(pieces.entrySet().stream()
            .map(e -> write(cache, e.getKey(), e.getValue()))
            .toArray(CompletableFuture[]::new));
    }

    // --- Palette ----------------------------------------------------------------
    // Une palette restreinte et constante fait la « veskorianité » d'un bâtiment mieux
    // qu'un catalogue de blocs : on doit reconnaître leur maçonnerie avant de lire un
    // panneau. Depuis 17-Dungeons.md elle est enfin FAITE DE BLOCS DU MOD — l'ancienne
    // était à 90 % du deepslate vanilla, ce qui rendait les ruines veskoriennes
    // indiscernables d'un donjon quelconque de l'intérieur.

    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState BRICK = ModBlocks.VEINED_STONE_BRICKS.get().defaultBlockState();
    private static final BlockState CRACKED = ModBlocks.CRACKED_VEINED_STONE_BRICKS.get().defaultBlockState();
    private static final BlockState CHISELED = ModBlocks.CHISELED_VEINED_STONE.get().defaultBlockState();
    private static final BlockState ROCK = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();
    private static final BlockState CONDUIT = ModBlocks.CONDUIT_LINE.get().defaultBlockState();
    private static final BlockState LAMP = ModBlocks.RESONANCE_LAMP.get().defaultBlockState();
    private static final BlockState BLOOM = ModBlocks.DISSONANCE_BLOOM.get().defaultBlockState();
    private static final BlockState BULKHEAD = ModBlocks.RESONANCE_BULKHEAD.get().defaultBlockState();
    private static final BlockState RUBBLE = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    private static final BlockState SLAB = ModBlocks.VEINED_STONE_BRICK_SLAB.get().defaultBlockState()
        .setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
    private static final BlockState GLASS = Blocks.PURPLE_STAINED_GLASS.defaultBlockState();

    /** Item du coffre-réserve : le carburant qui réveille l'émetteur ancien. */
    private static final String STABLE_CRYSTAL = "veskorius:stable_resonance_crystal";

    // =========================================================================
    // AVANT-POSTE — trois paliers autour d'un puits (17-Dungeons.md §5.1)
    // =========================================================================

    private static final int OUT_W = 21;
    private static final int OUT_D = 21;
    /** y 0 sol | 1-6 N2 | 7 sol | 8-12 N1 | 13 sol | 14-18 N0 | 19 toit. */
    private static final int OUT_H = 20;
    private static final int N2 = 1;
    private static final int N1 = 8;
    private static final int N0 = 14;

    /**
     * <b>L'Avant-poste.</b> 21×20×21, trois paliers.
     *
     * <pre>
     *   N0  y14-18  VESTIBULE    brèche au plafond (le « tell »), alcôve VIDE
     *        │      ▔▔▔▔▔▔▔▔▔    on comprend qu'on entre dans une ruine
     *        │ escalier est
     *   N1  y8-12   PALIER DE VIE  réfectoire, dortoirs, journal en 4 coffres,
     *        │      ▔▔▔▔▔▔▔▔▔▔▔▔   1 Custode, DEUX ailes facultatives (jigsaw)
     *        │      ╔═ PUITS ═╗    5×5 traversant : d'en haut on VOIT la console
     *        │ escalier ouest, bouché — il faut creuser
     *   N2  y1-6    SALLE DES MACHINES  émetteur ancien (à sec) + coffre-réserve
     *               ═══[ SAS ]═══        + coffre d'amorçage + 2 Custodes
     *               CHAMBRE DE CONSOLE  la porte du T2
     * </pre>
     *
     * <p><b>Le geste.</b> Le sas est fermé et indestructible ; la console est derrière,
     * visible par une baie. À deux pas, l'émetteur ancien est à sec. Lui donner un Stable
     * Resonance Crystal (le coffre-réserve en contient un, garanti) allume le champ : le
     * sas s'ouvre, les conduits de la salle s'allument, et les Custodes des alcôves se
     * réveillent. <b>Allumer, c'est armer le donjon</b> — et c'est le joueur qui décide.
     *
     * <p>Aucune dépendance circulaire : le Resonance Stabilizer est autonome (0 Osc,
     * aucun champ), donc un joueur T1 sait déjà faire un Stable Crystal ; et s'il arrive
     * les mains vides, le coffre-réserve lui en donne un. Deux GameTest gardent ces deux
     * garanties.
     */
    private static CompoundTag outpost() {
        TemplateBuilder b = new TemplateBuilder(OUT_W, OUT_H, OUT_D);
        shell(b, OUT_W, OUT_H, OUT_D);

        // Planchers intermédiaires, percés du puits central (5×5) : c'est lui qui fait
        // que le bâtiment se lit en une fois. Depuis le vestibule, on voit treize blocs
        // plus bas la console éteinte — on sait où on va avant de savoir comment.
        slabFloor(b, N1 - 1, 1, 1, OUT_W - 2, OUT_D - 2);
        slabFloor(b, N0 - 1, 1, 1, OUT_W - 2, OUT_D - 2);
        b.box(8, N1 - 1, 8, 12, N1 - 1, 12, AIR);
        b.box(8, N0 - 1, 8, 12, N0 - 1, 12, AIR);
        // Margelle : sans elle on tombe dans le puits sans l'avoir vu.
        ring(b, N1, 8, 12, SLAB);
        ring(b, N0, 8, 12, SLAB);

        // Les DEUX cages d'escalier sont creusées AVANT tout aménagement : elles
        // traversent les planchers, donc elles doivent précéder ce qui se pose dessus,
        // sans quoi un plancher reposé les reboucherait. Chaque salle ci-dessous est
        // meublée en dehors de leur emprise — c'est la seule contrainte de plan à tenir.
        stairwell(b, 16, 19, 10, 18, N1, N0 + 4);   // est : N0 → N1
        stairwell(b, 1, 4, 1, 9, N2, N0 - 2);       // ouest : N1 → N2

        outpostEntryLevel(b);
        outpostLivingLevel(b);
        outpostMachineLevel(b);
        return b.build();
    }

    /** N0 — le vestibule. Vide, effondré : une ruine avant toute rencontre. */
    private static void outpostEntryLevel(TemplateBuilder b) {
        // LE TELL : le plafond s'est effondré sur trois blocs de large. C'est par là
        // qu'une grotte peut croiser l'Avant-poste, donc par là qu'on le trouve en
        // explorant — /locate n'est que le second chemin, pas le seul.
        b.box(7, OUT_H - 1, 3, 9, OUT_H - 1, 5, RUBBLE);
        b.box(7, N0, 3, 9, N0 + 1, 5, RUBBLE);
        b.set(8, N0 + 2, 4, RUBBLE);

        // Alcôve de Custode, VIDE. Un avertissement : quelqu'un dormait ici, et n'y est
        // plus. La même niche, deux étages plus bas, sera occupée.
        alcove(b, 19, N0, 4, false);

        // Le conduit monte jusqu'ici mais reste MORT : la branche est coupée. C'est ce
        // contraste qui rendra lisible, plus tard, la partie qui s'allume.
        conduitRun(b, 1, N0 + 3, 2, OUT_D - 3, true);
        b.set(1, N0 + 3, 6, CRACKED);
        b.set(19, N0 + 3, 13, CRACKED);
        b.box(9, N0, 18, 11, N0, 18, CHISELED);
        lamps(b, N0 + 4, new int[][] {{6, 6}, {14, 6}, {10, 16}});

        // Descente est : N0 → N1. Une rampe franche, sans obstacle — le premier palier
        // ne doit rien demander d'autre que d'oser descendre.
        rampNorth(b, 17, 18, N0 - 1, N1 - 1, 6);
    }

    /** N1 — le palier de vie : le lore, le premier Custode, et les deux ailes. */
    private static void outpostLivingLevel(TemplateBuilder b) {
        // Réfectoire (ouest du puits) et dortoirs (est), tenus à l'écart des deux cages.
        b.set(6, N1, 3, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(7, N1, 3, Blocks.CAULDRON.defaultBlockState());
        b.set(6, N1, 6, Blocks.BARREL.defaultBlockState());
        for (int z = 3; z <= 7; z += 2) {
            cot(b, 18, N1, z);
            b.set(15, N1, z, Blocks.BOOKSHELF.defaultBlockState());
        }

        // LE JOURNAL, en quatre coffres alignés DANS L'ORDRE le long du mur sud : on lit
        // une descente en traversant la pièce, pas une anecdote tirée au sort.
        ResourceLocation[] log = {
            CodexEntries.OUTPOST_LOG_1, CodexEntries.OUTPOST_LOG_2,
            CodexEntries.OUTPOST_LOG_3, CodexEntries.OUTPOST_LOG_4,
        };
        for (int i = 0; i < log.length; i++) {
            b.fragmentChest(4 + i * 3, N1, OUT_D - 2, log[i], Direction.NORTH);
            b.set(4 + i * 3, N1 + 1, OUT_D - 2, CHISELED);
        }
        b.set(17, N1, OUT_D - 2, Blocks.LECTERN.defaultBlockState());

        b.lootChest(6, N1, 17, ModWorldGen.OUTPOST_LOOT, Direction.EAST);
        b.set(7, N1, 17, Blocks.GRINDSTONE.defaultBlockState());
        custode(b, 12.5, N1, 16.5);

        conduitRun(b, 1, N1 + 3, 2, OUT_D - 3, true);
        lamps(b, N1 + 4, new int[][] {{6, 4}, {14, 4}, {6, 16}, {14, 16}});

        // DEUX AILES FACULTATIVES. C'est la place du jigsaw, et la seule : du bonus,
        // jamais du chemin critique (17-Dungeons.md §3). Le connecteur est au niveau du
        // plancher du palier, pour qu'on entre dans l'aile de plain-pied.
        wingConnector(b, 0, N1, 15, Direction.WEST);
        wingConnector(b, OUT_W - 1, N1, 5, Direction.EAST);

        // Descente ouest : N1 → N2, BOUCHÉE. Le seul obstacle du donjon, et il ne demande
        // qu'une pioche — l'Avant-poste refuse l'énigme (08-Structures.md), la difficulté
        // y est un parcours, pas une devinette.
        rampNorth(b, 2, 8, N1 - 1, 0, 7);
        b.box(2, N1, 7, 3, N1 + 2, 8, RUBBLE);
    }

    /** N2 — la salle des machines : l'émetteur ancien, le sas, la console. */
    private static void outpostMachineLevel(TemplateBuilder b) {
        // Cloison du sas, à z=13 : elle coupe la salle des machines de la chambre de
        // console. C'est le seul mur du donjon qu'on ne peut pas contourner.
        b.box(1, N2, 13, OUT_W - 2, N2 + 5, 13, BRICK);
        b.box(9, N2, 13, 11, N2 + 1, 13, AIR);
        b.set(10, N2, 13, BULKHEAD);
        b.set(10, N2 + 1, 13, BULKHEAD);
        // Baie : on VOIT la console avant de pouvoir l'atteindre. Un verrou qui ne
        // montre pas ce qu'il garde n'est qu'un mur.
        b.set(9, N2 + 1, 13, GLASS);
        b.set(11, N2 + 1, 13, GLASS);
        b.set(9, N2, 13, CHISELED);
        b.set(11, N2, 13, CHISELED);

        // L'ÉMETTEUR ANCIEN, à sec, face au sas. Sa portée (8 blocs) couvre le sas, les
        // conduits de la salle et les deux alcôves — c'est ce qui délimite exactement ce
        // qui se réveille : la salle des machines, pas tout l'édifice.
        b.set(14, N2, 10, ModBlocks.ANCIENT_EMITTER.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
        b.box(13, N2 - 1, 9, 15, N2 - 1, 11, CHISELED);

        // Le coffre-réserve : EXACTEMENT un Stable Crystal, contenu fixe. Un joueur qui
        // arrive les mains vides doit pouvoir ouvrir le sas ; une table de loot rendrait
        // la porte du T2 aléatoire, ce que le pilier 2 interdit.
        b.itemChest(12, N2, 10, STABLE_CRYSTAL, 1, Direction.WEST);
        // Le coffre d'amorçage T2 (4 Component + 2 Gold garantis, 08-Structures.md).
        b.lootChest(3, N2, 11, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);
        b.set(4, N2, 11, Blocks.SMITHING_TABLE.defaultBlockState());
        b.set(2, N2, 12, Blocks.BARREL.defaultBlockState());

        // Deux Custodes en alcôve, de part et d'autre de la salle. Rangés, pas plantés :
        // ils se lèvent quand la salle se réveille.
        alcove(b, 1, N2, 10, true);
        alcove(b, 19, N2, 8, true);

        // Le réseau de la salle : conduits au mur, lampes au plafond. Tout est mort tant
        // que l'émetteur est à sec — c'est le retour visuel de la règle R1.
        conduitRun(b, 1, N2 + 3, 2, OUT_D - 3, true);
        lamps(b, N2 + 5, new int[][] {{6, 5}, {14, 5}, {6, 11}, {14, 11}, {10, 17}});

        // Efflorescence de dissonance dans l'angle mort : neuf siècles de champ qui
        // dérive laissent des traces (02-Lore.md, Âge 4). On la traverse en encaissant,
        // ou on fait le tour — c'est un obstacle, pas un décor.
        b.box(16, N2, 2, 18, N2, 4, BLOOM);
        b.set(17, N2 + 1, 3, BLOOM);

        // --- Chambre de console (z 14-19) : l'estrade et la porte du T2 -------
        // Estrade d'UN bloc de haut, bordée de dalles : le relief désigne la pièce
        // maîtresse sans éclairage supplémentaire, et c'est la seule salle où le sol
        // monte. La console se pose donc à y+1, au même niveau que les pieds de qui la
        // consulte — un bloc de sol occupe la case des pieds, pas celle du torse.
        b.box(7, N2, 15, 13, N2, 19, CHISELED);
        b.box(7, N2, 14, 13, N2, 14, SLAB);
        b.set(10, N2 + 1, 17, ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState());
        for (int[] c : new int[][] {{6, 15}, {14, 15}, {6, 19}, {14, 19}}) {
            for (int y = N2; y <= N2 + 4; y++) {
                b.set(c[0], y, c[1], y == N2 + 4 ? CHISELED : BRICK);
            }
        }
    }

    // --- Ailes facultatives de l'Avant-poste ------------------------------------
    // Elles n'existent que pour varier la taille et la forme d'un Avant-poste à l'autre.
    // Aucune ne contient quoi que ce soit d'obligatoire : c'est la définition même de
    // leur place dans le système (17-Dungeons.md §3).

    private static final int WING_W = 11;
    private static final int WING_H = 7;
    private static final int WING_D = 11;

    /** Réserve : des barils, un peu de butin, une salle qui respire. */
    private static CompoundTag wingStore() {
        TemplateBuilder b = wingShell();
        b.lootChest(5, 1, 2, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);
        for (int z = 2; z <= 8; z += 3) {
            b.set(2, 1, z, Blocks.BARREL.defaultBlockState());
            b.set(8, 1, z, Blocks.BARREL.defaultBlockState());
        }
        lamps(b, 5, new int[][] {{5, 5}});
        return b.build();
    }

    /** Quartiers : lits, étagères, un Custode. La récompense est le risque. */
    private static CompoundTag wingQuarters() {
        TemplateBuilder b = wingShell();
        for (int z = 2; z <= 8; z += 3) {
            cot(b, 1, 1, z);
            b.set(9, 1, z, Blocks.BOOKSHELF.defaultBlockState());
        }
        b.lootChest(5, 1, 8, ModWorldGen.OUTPOST_LOOT, Direction.NORTH);
        custode(b, 5.5, 1, 5.5);
        lamps(b, 5, new int[][] {{5, 3}, {5, 7}});
        return b.build();
    }

    /** Galerie effondrée : une impasse à creuser, et de la dissonance au fond. */
    private static CompoundTag wingCollapsed() {
        TemplateBuilder b = wingShell();
        b.box(4, 1, 4, 7, 4, 7, RUBBLE);
        b.box(2, 1, 8, 4, 1, 9, BLOOM);
        b.set(9, 1, 9, Blocks.BARREL.defaultBlockState());
        return b.build();
    }

    /**
     * Coquille d'aile, avec ses deux connecteurs : celui par lequel on entre, et celui
     * par lequel une aile <i>peut</i> en appeler une autre. C'est ce second connecteur
     * qui fait que deux Avant-postes n'ont pas la même emprise au sol.
     */
    private static TemplateBuilder wingShell() {
        TemplateBuilder b = new TemplateBuilder(WING_W, WING_H, WING_D);
        shell(b, WING_W, WING_H, WING_D);
        slabFloor(b, 0, 1, 1, WING_W - 2, WING_D - 2);
        conduitRun(b, 1, 4, 2, WING_D - 3, true);
        wingConnector(b, 0, 1, 5, Direction.WEST);
        wingConnector(b, WING_W - 1, 1, 5, Direction.EAST);
        return b;
    }

    // =========================================================================
    // HAMEAU — l'« Habitation Modeste » cesse d'être une maison seule
    // =========================================================================
    //
    // Une maison isolée ne raconte pas un peuple : elle raconte un survivant. Le hameau
    // (une galerie, un puits, trois à six logis tirés d'un pool) raconte une population
    // — ce que 02-Lore.md demande de la strate « peuple du réseau ». Et il ne coûte
    // qu'un pool : c'est exactement le bénéfice que le choix jigsaw promettait.

    private static final int HAM_W = 11;
    private static final int HAM_H = 6;
    private static final int HAM_D = 11;
    private static final int HOUSE_W = 9;
    private static final int HOUSE_H = 6;
    private static final int HOUSE_D = 11;

    /** La place commune : un puits, quatre galeries, aucun butin. */
    private static CompoundTag hamlet() {
        TemplateBuilder b = new TemplateBuilder(HAM_W, HAM_H, HAM_D);
        shell(b, HAM_W, HAM_H, HAM_D);
        slabFloor(b, 0, 1, 1, HAM_W - 2, HAM_D - 2);

        // Le puits : de l'eau, un rebord, et rien d'autre. Un lieu commun se reconnaît
        // à ce qu'il ne contient aucune récompense.
        b.box(4, 0, 4, 6, 0, 6, ROCK);
        b.set(5, 0, 5, Blocks.WATER.defaultBlockState());
        ring(b, 1, 5, 5, SLAB);
        b.set(5, 1, 5, AIR);

        b.set(2, 1, 2, Blocks.CAULDRON.defaultBlockState());
        b.set(8, 1, 8, Blocks.COMPOSTER.defaultBlockState());
        b.set(2, 1, 8, Blocks.CRAFTING_TABLE.defaultBlockState());
        lamps(b, 4, new int[][] {{2, 5}, {8, 5}});
        b.set(5, 1, 1, CHISELED);
        b.set(5, 1, HAM_D - 2, CHISELED);

        // Quatre directions : le hameau pousse là où le pool veut bien.
        houseConnector(b, 0, 1, 5, Direction.WEST);
        houseConnector(b, HAM_W - 1, 1, 5, Direction.EAST);
        houseConnector(b, 5, 1, 0, Direction.NORTH);
        houseConnector(b, 5, 1, HAM_D - 1, Direction.SOUTH);
        return b.build();
    }

    /** Logis : un lit, un âtre, un coffre. Le loot quotidien de 08-Structures.md. */
    private static CompoundTag hamletDwelling() {
        TemplateBuilder b = houseShell();
        cot(b, 1, 1, 2);
        b.set(2, 1, 2, Blocks.BOOKSHELF.defaultBlockState());
        b.set(7, 1, 2, Blocks.FURNACE.defaultBlockState());
        b.set(7, 1, 4, Blocks.CAULDRON.defaultBlockState());
        b.lootChest(4, 1, 9, ModWorldGen.MODEST_DWELLING_LOOT, Direction.NORTH);
        b.set(6, 1, 9, Blocks.FLOWER_POT.defaultBlockState());
        lamps(b, 4, new int[][] {{4, 4}});
        return b.build();
    }

    /** Atelier de famille : une halle à piliers, un établi, des réserves. */
    private static CompoundTag hamletWorkshop() {
        TemplateBuilder b = houseShell();
        for (int z = 3; z <= 7; z += 2) {
            b.box(2, 1, z, 2, 3, z, CHISELED);
            b.box(6, 1, z, 6, 3, z, CHISELED);
        }
        b.set(4, 1, 9, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(7, 1, 9, Blocks.BARREL.defaultBlockState());
        b.lootChest(1, 1, 9, ModWorldGen.MODEST_DWELLING_LOOT, Direction.EAST);
        cot(b, 1, 1, 2);
        lamps(b, 4, new int[][] {{4, 3}, {4, 7}});
        return b.build();
    }

    /**
     * Logis effondré : une impasse. Il faut creuser pour entrer, et ce qui reste dedans
     * est maigre. Sa vraie fonction est de casser la régularité — un hameau dont tous
     * les logis sont intacts n'a pas été abandonné, il a été construit hier.
     */
    private static CompoundTag hamletCollapsed() {
        TemplateBuilder b = houseShell();
        b.box(3, 1, 3, 6, 4, 6, RUBBLE);
        b.box(1, 1, 1, 2, 2, 2, RUBBLE);
        b.set(7, 1, 9, Blocks.BARREL.defaultBlockState());
        b.box(1, 1, 8, 2, 1, 9, BLOOM);
        return b.build();
    }

    private static TemplateBuilder houseShell() {
        TemplateBuilder b = new TemplateBuilder(HOUSE_W, HOUSE_H, HOUSE_D);
        shell(b, HOUSE_W, HOUSE_H, HOUSE_D);
        slabFloor(b, 0, 1, 1, HOUSE_W - 2, HOUSE_D - 2);
        houseConnector(b, 0, 1, 5, Direction.WEST);
        houseConnector(b, HOUSE_W - 1, 1, 5, Direction.EAST);
        return b;
    }

    // =========================================================================
    // Bouchons de fin de branche
    // =========================================================================

    /**
     * Bouchon : un mur d'un bloc d'épaisseur qui referme une branche non poursuivie.
     *
     * <p>Sans lui, une structure sur deux se termine par un <b>trou béant</b> sur la
     * roche, là où le jigsaw a atteint sa profondeur maximale. C'est pour ça que le pool
     * de bouchons est déclaré en {@code fallback} de tous les autres : le fallback est
     * précisément ce que le jigsaw pose quand il ne peut plus continuer.
     *
     * <p>Son connecteur a pour {@code final_state} de la <b>brique</b>, pas de l'air :
     * le bloc jigsaw est remplacé par ce qu'on lui donne, et un bouchon qui se remplace
     * par de l'air ne bouche rien.
     */
    private static CompoundTag cap(int height, int depth) {
        TemplateBuilder b = new TemplateBuilder(1, height, depth);
        b.box(0, 0, 0, 0, height - 1, depth - 1, BRICK);
        b.jigsaw(0, 1, depth / 2, Direction.WEST, "veskorius:corridor", "veskorius:corridor",
            net.minecraft.data.worldgen.Pools.EMPTY, BRICK, true);
        return b.build();
    }

    // --- Vocabulaire de construction --------------------------------------------

    /** Coquille : murs, sol et plafond de maçonnerie, intérieur creusé. */
    private static void shell(TemplateBuilder b, int w, int h, int d) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    boolean edge = x == 0 || x == w - 1 || z == 0 || z == d - 1 || y == 0 || y == h - 1;
                    b.set(x, y, z, edge ? BRICK : AIR);
                }
            }
        }
    }

    /**
     * Sol en damier brique / dalle. De l'appareillage, pas une nappe : c'est ce qui
     * distingue un bâtiment d'une salle creusée, et ça ne coûte rien.
     */
    private static void slabFloor(TemplateBuilder b, int y, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                b.set(x, y, z, (x + z) % 2 == 0 ? BRICK : CHISELED);
            }
        }
    }

    /** Margelle : le pourtour d'un puits, pour qu'on le voie avant d'y tomber. */
    private static void ring(TemplateBuilder b, int y, int min, int max, BlockState state) {
        for (int i = min - 1; i <= max + 1; i++) {
            b.set(i, y, min - 1, state);
            b.set(i, y, max + 1, state);
            b.set(min - 1, y, i, state);
            b.set(max + 1, y, i, state);
        }
    }

    /** Ligne de conduit courant le long des deux murs (ou d'un seul axe). */
    private static void conduitRun(TemplateBuilder b, int inset, int y, int from, int to, boolean bothWalls) {
        for (int i = from; i <= to; i++) {
            b.set(inset, y, i, CONDUIT);
            if (bothWalls) {
                b.set(b.width() - 1 - inset, y, i, CONDUIT);
            }
        }
    }

    /** Lampes de résonance au plafond. Éteintes tant qu'aucun champ ne les couvre. */
    private static void lamps(TemplateBuilder b, int y, int[][] spots) {
        for (int[] s : spots) {
            b.set(s[0], y, s[1], LAMP);
        }
    }

    /**
     * Alcôve de dock : une niche de deux blocs le long d'un mur latéral. Un Custode
     * <b>rangé</b> qui s'en extrait vaut dix Custodes plantés au sol — et une alcôve
     * <b>vide</b> raconte autant qu'une occupée.
     *
     * <p>La niche est formée par les jambages qu'on <b>ajoute</b> de part et d'autre,
     * jamais creusée dans la coquille : un mur d'un bloc d'épaisseur qu'on perce donne un
     * trou sur la roche, pas une alcôve.
     */
    private static void alcove(TemplateBuilder b, int x, int y, int z, boolean occupied) {
        b.box(x, y, z - 1, x, y + 2, z - 1, CHISELED);
        b.box(x, y, z + 1, x, y + 2, z + 1, CHISELED);
        b.set(x, y + 2, z, CHISELED);
        b.set(x, y, z, AIR);
        b.set(x, y + 1, z, AIR);
        if (occupied) {
            custode(b, x + 0.5, y, z + 0.5);
        }
    }

    /**
     * Cage d'escalier : le volume vide, planchers intermédiaires compris. Sans ce
     * percement, une rampe descendante se cogne au plancher de l'étage.
     */
    private static void stairwell(TemplateBuilder b, int x0, int x1, int z0, int z1, int yBottom, int yTop) {
        b.box(x0, yBottom, z0, x1, yTop, z1, AIR);
    }

    /**
     * Rampe descendante vers le nord : un bloc de descente par pas. Des blocs pleins et
     * non des escaliers — un escalier veskorien est une maçonnerie massive, et ça évite
     * qu'un processor d'usure laisse une marche flottante.
     */
    private static void rampNorth(TemplateBuilder b, int x0, int zStart, int yStart, int yEnd, int steps) {
        for (int i = 0; i <= steps; i++) {
            int y = yStart - i;
            int z = zStart - i;
            if (y < yEnd || z < 1) {
                break;
            }
            b.box(x0, y, z, x0 + 1, y, z, BRICK);
            if (y > 0) {
                // Contremarche : sans elle, une marche haute laisse un vide dessous et la
                // rampe se lit comme un escalier flottant.
                b.box(x0, y - 1, z, x0 + 1, y - 1, z, BRICK);
            }
        }
    }

    /**
     * Couchette veskorienne : une dalle de pierre et un dossier gravé.
     *
     * <p>Ce n'était d'abord qu'un {@code red_bed} vanilla — et il <b>disparaissait</b> à la
     * pose : un lit occupe deux blocs, et une moitié de lit posée seule est retirée par la
     * mise à jour de voisinage. Le défaut était invisible (la structure se générait très
     * bien, simplement sans aucun lit), et c'est le dump de géométrie qui l'a montré.
     *
     * <p>La correction est meilleure que l'original : une civilisation de la Résonance ne
     * dort pas sur de la laine teinte (pilier 1). La couchette est maintenant faite des
     * blocs du mod, donc elle survit à la pose <i>et</i> elle a l'air veskorienne.
     */
    private static void cot(TemplateBuilder b, int x, int y, int z) {
        b.set(x, y, z, SLAB);
        b.set(x, y, z + 1, SLAB);
        b.set(x, y + 1, z, CHISELED); // dossier en tête, le pied reste dégagé
    }

    /** Un Custode persistant en poste (ne despawn jamais, réactif seulement de près). */
    private static void custode(TemplateBuilder b, double x, int y, double z) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "veskorius:custode");
        tag.putBoolean("PersistenceRequired", true);
        b.entity(x, y, z, (int) x, y, (int) z, tag);
    }

    /** Connecteur d'aile d'Avant-poste : le bloc jigsaw, plus l'ouverture au-dessus. */
    private static void wingConnector(TemplateBuilder b, int x, int y, int z, Direction front) {
        b.jigsaw(x, y, z, front, "veskorius:corridor", "veskorius:corridor",
            ModStructures.OUTPOST_WING_POOL, AIR, true);
        b.set(x, y + 1, z, AIR);
    }

    /** Connecteur de logis de hameau. */
    private static void houseConnector(TemplateBuilder b, int x, int y, int z, Direction front) {
        b.jigsaw(x, y, z, front, "veskorius:corridor", "veskorius:corridor",
            ModStructures.HAMLET_HOUSE_POOL, AIR, true);
        b.set(x, y + 1, z, AIR);
    }

    // --- Écriture ---------------------------------------------------------------

    private CompletableFuture<?> write(CachedOutput cache, String name, CompoundTag tag) {
        Path target = pathProvider.file(
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name), "nbt");
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                NbtIo.writeCompressed(tag, buffer);
                byte[] bytes = buffer.toByteArray();
                cache.writeIfNeeded(target, bytes, Hashing.sha1().hashBytes(bytes));
            } catch (IOException e) {
                throw new UncheckedIOException("Écriture de la pièce de structure " + name, e);
            }
        }, Util.backgroundExecutor());
    }

    @Override
    public String getName() {
        return "Veskorius structure pieces";
    }
}
