package com.veskorius.datagen;

import static com.veskorius.datagen.Masonry.AIR;
import static com.veskorius.datagen.Masonry.BLOOM;
import static com.veskorius.datagen.Masonry.BRICK;
import static com.veskorius.datagen.Masonry.BULKHEAD;
import static com.veskorius.datagen.Masonry.CHISELED;
import static com.veskorius.datagen.Masonry.CONDUIT;
import static com.veskorius.datagen.Masonry.CRACKED;
import static com.veskorius.datagen.Masonry.DEBRIS;
import static com.veskorius.datagen.Masonry.GLASS;
import static com.veskorius.datagen.Masonry.LAMP;
import static com.veskorius.datagen.Masonry.ROCK;
import static com.veskorius.datagen.Masonry.RUBBLE;
import static com.veskorius.datagen.Masonry.SLAB;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Génère les <b>pièces de structure</b> (NBT) des donjons veskoriens — 08-Structures.md
 * pour le contenu, 17-Dungeons.md pour la forme, {@link Masonry} pour les gestes.
 *
 * <p><b>La refonte du 2026-08-07 (seconde passe).</b> La première tentative de « vrai
 * donjon » a échoué au test qui compte : ça se voyait que c'était un ordinateur qui avait
 * posé des cubes. Le diagnostic, en trois points, gouverne tout ce fichier :
 *
 * <ol>
 *   <li><b>On construisait des boîtes au lieu de creuser une masse.</b> Chaque pièce était
 *       une coquille rectangulaire pleine d'air, cloisonnée en salles rectangulaires — donc
 *       un plan d'appartement, avec une silhouette de pavé. Maintenant chaque salle est
 *       <b>évidée séparément</b> et il reste de la <b>roche du monde entre elles</b> ; on
 *       circule par des <b>galeries</b>, jamais par une porte percée dans une cloison.</li>
 *   <li><b>Tout était plat et à angle droit.</b> Plafonds plats, hauteurs identiques,
 *       quatre angles droits par salle. Maintenant : <b>voûtes en berceau</b>, angles
 *       coupés, bandeaux, pilastres, une <b>rotonde octogonale à coupole</b>, et un
 *       <b>escalier en vis</b> à la place des rampes droites.</li>
 *   <li><b>La ruine était du bruit.</b> Maintenant elle est <b>causale</b> : la voûte
 *       s'ouvre et la matière manquante gît en cône exactement dessous, les murs crevés
 *       versent leurs pierres vers l'intérieur, l'eau s'accumule au point bas.</li>
 * </ol>
 *
 * <p><b>L'invariant qui gouverne le contenu</b> (17-Dungeons.md §3) : le chemin critique —
 * console, sas, émetteur ancien, coffre d'amorçage, coffre-réserve — vit <b>dans la pièce
 * de départ</b>. Le pool ne sert qu'aux ailes facultatives.
 */
public class ModStructurePieceProvider implements DataProvider {

    public static final String OUTPOST = "outpost";
    public static final String OUTPOST_WING_STORE = "outpost_wing_store";
    public static final String OUTPOST_WING_QUARTERS = "outpost_wing_quarters";
    public static final String OUTPOST_WING_COLLAPSED = "outpost_wing_collapsed";
    public static final String OUTPOST_CAP = "outpost_cap";

    public static final String HAMLET = "hamlet";
    public static final String HAMLET_DWELLING = "hamlet_dwelling";
    public static final String HAMLET_WORKSHOP = "hamlet_workshop";
    public static final String HAMLET_CISTERN = "hamlet_cistern";
    public static final String HAMLET_COLLAPSED = "hamlet_collapsed";
    public static final String HAMLET_CAP = "hamlet_cap";

    /** Petites ruines : la texture de fond du monde veskorien (17-Dungeons.md §6). */
    public static final String RUIN_MARKER = "ruin_marker";
    public static final String RUIN_MARKER_PILLAR = "ruin_marker_pillar";
    public static final String SUNKEN_CHAMBER = "sunken_chamber";

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
        pieces.put(HAMLET_CISTERN, hamletCistern());
        pieces.put(HAMLET_COLLAPSED, hamletCollapsed());
        pieces.put(HAMLET_CAP, cap(HOUSE_H, HOUSE_D));
        pieces.put(RUIN_MARKER, ruinMarker());
        pieces.put(RUIN_MARKER_PILLAR, ruinMarkerPillar());
        pieces.put(SUNKEN_CHAMBER, sunkenChamber());
        return CompletableFuture.allOf(pieces.entrySet().stream()
            .map(e -> write(cache, e.getKey(), e.getValue()))
            .toArray(CompletableFuture[]::new));
    }

    /** Item du coffre-réserve : le carburant qui réveille l'émetteur ancien. */
    private static final String STABLE_CRYSTAL = "veskorius:stable_resonance_crystal";

    // =========================================================================
    // AVANT-POSTE
    // =========================================================================
    //
    //   PLAN (vue de dessus, 33×33). Rien n'est aligné sur une grille : les salles
    //   sont creusées où elles ont du sens, et la roche entre elles reste de la roche.
    //
    //        z→ 0        8        16        24       32
    //   x=0  ┌───────────────────────────────────────┐
    //        │        ╭──────╮      ┌───────────┐    │  N1 (y16-20)
    //        │        │ VIS  │══════│ VESTIBULE │    │  vestibule : voûte crevée
    //     8  │        │ ⟳⟳⟳  │      │  (entrée) │    │  → c'est PAR LÀ qu'on entre
    //        │        ╰──╥───╯      └───────────┘    │
    //    16  │  ┌────────╨────────┐   ┌─────────┐    │
    //        │  │  GRANDE  SALLE  │═══│ CELLULE │→J  │  J = aile facultative
    //        │  │  voûte effondrée│   └─────────┘    │
    //    28  │  └─────────────────┘                  │
    //   x=32 └───────────────────────────────────────┘
    //
    //   x=0  ┌───────────────────────────────────────┐
    //        │        ╭──────╮                       │  N0 (y3-9), sous le précédent
    //        │        │ VIS  │                       │
    //     8  │        ╰──╥───╯                       │
    //        │  ┌────────╨────────┐ B ╭───────────╮  │  B = SAS
    //    16  │  │  SALLE DES      │═══│ ROTONDE   │  │  rotonde octogonale
    //        │  │  MACHINES       │   │ + COUPOLE │  │  → la console
    //    28  │  └─────────────────┘   ╰───────────╯  │
    //   x=32 └───────────────────────────────────────┘

    private static final int OUT_SIZE = 33;
    private static final int OUT_H = 26;
    /** Niveau bas : sol foulé à y=3. */
    private static final int LOW = 3;
    /** Niveau haut : sol foulé à y=16. */
    private static final int HIGH = 16;
    private static final int VIS_X = 12;
    private static final int VIS_Z = 8;

    private static CompoundTag outpost() {
        TemplateBuilder b = new TemplateBuilder(OUT_SIZE, OUT_H, OUT_SIZE);

        // La VIS d'abord : c'est la colonne vertébrale, tout se raccroche à elle.
        // Entrée par l'EST : c'est de ce côté que le vestibule amène. La première marche
        // doit tomber devant la galerie, sinon on en sort dans le vide.
        Masonry.spiralStair(b, VIS_X, VIS_Z, HIGH - 1, LOW - 1, Direction.EAST);
        for (int y = LOW + 1; y < HIGH; y += 3) {
            b.set(VIS_X - 1, y, VIS_Z - 1, CONDUIT);
            b.set(VIS_X + 1, y, VIS_Z + 1, CONDUIT);
        }

        outpostVestibule(b);
        outpostGreatHall(b);
        outpostCell(b);
        outpostMachineHall(b);
        outpostRotunda(b);
        return b.build();
    }

    /**
     * <b>Vestibule</b> — l'entrée, et le seul endroit où le monde pénètre la ruine. Sa
     * voûte s'est ouverte : le bouchon de gravier et de roche qui la remplace est ce qu'une
     * grotte peut croiser, donc ce par quoi on tombe dessus en explorant. Vide, humide,
     * désarmé — on doit comprendre qu'on entre dans une ruine avant de rencontrer quoi que
     * ce soit de vivant.
     */
    private static void outpostVestibule(TemplateBuilder b) {
        Masonry.chamber(b, 20, HIGH, 4, 28, HIGH + 4, 12, Masonry.Style.common());
        Masonry.gallery(b, 16, HIGH, 8, 19, 8);

        Masonry.collapse(b, 24, 8, 3, HIGH + 5, HIGH, 0x5EED1);
        Masonry.puddle(b, 22, HIGH, 11, 2);
        Masonry.dripstone(b, 26, HIGH + 5, 6, 2);
        Masonry.wallBreach(b, 29, HIGH, 5, 4, true, -1, 0x5EED2);

        // Alcôve de Custode, VIDE. Quelqu'un veillait ici, et n'y est plus.
        alcove(b, 20, HIGH, 6, true, false);
        // Conduits morts : la branche est coupée bien avant d'arriver ici.
        for (int z = 5; z <= 11; z++) {
            b.set(20, HIGH + 3, z, z == 8 ? CRACKED : CONDUIT);
        }
        b.set(24, HIGH, 4, CHISELED);
        // Une aile facultative peut s'accrocher au nord du vestibule.
        wingConnector(b, 24, HIGH, 3, Direction.NORTH);
    }

    /**
     * <b>Grande salle</b> — le morceau de bravoure, et la démonstration de la ruine
     * causale : un tiers de la voûte manque, et sa matière est là, en cône, sous le trou.
     * On tourne autour du tas pour traverser la pièce ; c'est le tas qui dessine le
     * cheminement, pas une cloison.
     */
    private static void outpostGreatHall(TemplateBuilder b) {
        Masonry.chamber(b, 4, HIGH, 16, 16, HIGH + 4, 28, Masonry.Style.noble());
        Masonry.gallery(b, VIS_X, HIGH, 12, VIS_X, 15);

        // Quatre colonnes libres : une salle de treize mètres sans support se lit comme
        // un hangar. Avec, elle se lit comme une salle.
        for (int[] c : new int[][] {{7, 19}, {13, 19}, {7, 25}, {13, 25}}) {
            for (int y = HIGH; y <= HIGH + 4; y++) {
                b.set(c[0], y, c[1], y == HIGH || y == HIGH + 4 ? CHISELED : BRICK);
            }
        }

        // L'effondrement. Rayon 4 : assez pour qu'on ne puisse pas l'ignorer, assez peu
        // pour qu'on puisse toujours faire le tour.
        Masonry.collapse(b, 10, 24, 4, HIGH + 5, HIGH, 0x5EED3);
        Masonry.wallBreach(b, 3, HIGH, 20, 5, true, 1, 0x5EED4);
        Masonry.puddle(b, 6, HIGH, 27, 2);
        Masonry.dripstone(b, 12, HIGH + 5, 18, 3);

        // LE JOURNAL, quatre coffres alignés DANS L'ORDRE le long du mur nord : on lit une
        // descente en traversant la salle, pas une anecdote tirée au sort.
        ResourceLocation[] log = {
            CodexEntries.OUTPOST_LOG_1, CodexEntries.OUTPOST_LOG_2,
            CodexEntries.OUTPOST_LOG_3, CodexEntries.OUTPOST_LOG_4,
        };
        for (int i = 0; i < log.length; i++) {
            b.fragmentChest(5 + i * 3, HIGH, 16, log[i], Direction.SOUTH);
            b.set(5 + i * 3, HIGH + 1, 16, CHISELED);
        }
        b.set(15, HIGH, 17, Blocks.LECTERN.defaultBlockState());
        b.lootChest(15, HIGH, 27, ModWorldGen.OUTPOST_LOOT, Direction.WEST);
        b.set(14, HIGH, 27, Blocks.GRINDSTONE.defaultBlockState());
        b.set(5, HIGH, 18, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(5, HIGH, 26, Blocks.CAULDRON.defaultBlockState());
        custode(b, 12.5, HIGH, 21.5);

        lamps(b, HIGH + 4, new int[][] {{6, 18}, {14, 18}, {6, 26}});
        for (int z = 17; z <= 27; z++) {
            b.set(4, HIGH + 2, z, CONDUIT);
        }
        Masonry.gallery(b, 17, HIGH, 21, 21, 21);
    }

    /** <b>Cellule</b> — un dortoir étroit, à l'écart. C'est aussi la porte d'une aile. */
    private static void outpostCell(TemplateBuilder b) {
        Masonry.chamber(b, 22, HIGH, 18, 28, HIGH + 3, 24, Masonry.Style.common());
        for (int z = 19; z <= 23; z += 2) {
            cot(b, 23, HIGH, z);
        }
        b.set(27, HIGH, 19, Blocks.BARREL.defaultBlockState());
        b.set(27, HIGH, 23, Blocks.BOOKSHELF.defaultBlockState());
        lamps(b, HIGH + 3, new int[][] {{25, 21}});
        wingConnector(b, 29, HIGH, 21, Direction.EAST);
    }

    /**
     * <b>Salle des machines</b> — voûtée haut, la seule pièce encore équipée. L'émetteur
     * ancien y trône sur un socle, à sec ; le sas est dans le mur est ; et le
     * coffre-réserve, au contenu <b>fixe</b>, garantit le cristal qui ouvre tout.
     */
    private static void outpostMachineHall(TemplateBuilder b) {
        Masonry.chamber(b, 4, LOW, 14, 18, LOW + 6, 28, Masonry.Style.noble());
        Masonry.gallery(b, VIS_X, LOW, 12, VIS_X, 13);

        // Socle de l'émetteur : trois marches concentriques. Le relief désigne la pièce
        // maîtresse sans une lampe de plus.
        b.box(9, LOW, 19, 13, LOW, 23, CHISELED);
        for (int i = 18; i <= 24; i++) {
            b.set(8, LOW, i, SLAB);
            b.set(14, LOW, i, SLAB);
            b.set(i - 10, LOW, 18, SLAB);
            b.set(i - 10, LOW, 24, SLAB);
        }
        b.set(11, LOW + 1, 21, ModBlocks.ANCIENT_EMITTER.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));

        b.itemChest(13, LOW, 21, STABLE_CRYSTAL, 1, Direction.WEST);
        b.lootChest(5, LOW, 16, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);
        b.set(6, LOW, 16, Blocks.SMITHING_TABLE.defaultBlockState());
        b.set(5, LOW, 26, Blocks.BARREL.defaultBlockState());

        // Deux Custodes rangés en alcôve : ils se lèvent quand la salle se réveille.
        alcove(b, 4, LOW, 18, true, true);
        alcove(b, 4, LOW, 25, true, true);

        // Le réseau : conduits au mur, lampes en voûte. Tout mort tant que l'émetteur
        // est à sec — c'est le retour visuel de la règle R1.
        for (int z = 15; z <= 27; z++) {
            b.set(4, LOW + 3, z, CONDUIT);
            b.set(18, LOW + 3, z, CONDUIT);
        }
        lamps(b, LOW + 6, new int[][] {{7, 17}, {15, 17}, {7, 25}, {15, 25}, {11, 21}});

        // Neuf siècles de champ qui dérive laissent des traces (02-Lore.md, Âge 4).
        b.box(16, LOW, 26, 17, LOW, 27, BLOOM);
        b.set(16, LOW + 1, 27, BLOOM);
        Masonry.puddle(b, 7, LOW, 27, 1);
        Masonry.wallBreach(b, 10, LOW, 29, 4, false, -1, 0x5EED5);

        // LE SAS, dans le mur est. Un bloc de large, deux de haut, indestructible ; et
        // une baie de part et d'autre — un verrou qui ne montre pas ce qu'il garde n'est
        // qu'un mur.
        b.box(19, LOW, 20, 19, LOW + 2, 22, BRICK);
        b.set(19, LOW, 21, BULKHEAD);
        b.set(19, LOW + 1, 21, BULKHEAD);
        b.set(19, LOW + 1, 20, GLASS);
        b.set(19, LOW + 1, 22, GLASS);
        b.set(19, LOW + 2, 21, CHISELED);
    }

    /**
     * <b>Rotonde de la console</b> — octogonale, coiffée d'une coupole, avec une lampe à
     * la clé. Une salle dont le plan n'est pas rectangulaire est lue comme importante
     * avant qu'on ait rien écrit dedans : c'est le moyen le moins cher de dire « ceci est
     * la pièce maîtresse », et le seul qui ne dépende pas de l'éclairage.
     */
    private static void outpostRotunda(TemplateBuilder b) {
        Masonry.rotunda(b, 26, LOW, 21, 5, 6);
        // Percée du sas côté rotonde (le mur commun appartient à la salle des machines).
        b.box(20, LOW, 20, 20, LOW + 2, 22, AIR);

        b.box(24, LOW, 19, 28, LOW, 23, CHISELED);
        for (int i = 18; i <= 24; i++) {
            b.set(23, LOW, i, SLAB);
            b.set(29, LOW, i, SLAB);
            b.set(i + 5, LOW, 18, SLAB);
            b.set(i + 5, LOW, 24, SLAB);
        }
        b.set(26, LOW + 1, 21, ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState());
        for (int[] c : new int[][] {{23, 18}, {29, 18}, {23, 24}, {29, 24}}) {
            for (int y = LOW; y <= LOW + 5; y++) {
                b.set(c[0], y, c[1], y == LOW + 5 ? CHISELED : BRICK);
            }
        }
        for (int z = 18; z <= 24; z += 3) {
            b.set(22, LOW + 3, z, CONDUIT);
            b.set(30, LOW + 3, z, CONDUIT);
        }
    }

    // =========================================================================
    // Ailes facultatives de l'Avant-poste
    // =========================================================================

    private static final int WING_W = 13;
    private static final int WING_H = 8;
    private static final int WING_D = 13;

    private static CompoundTag wingStore() {
        TemplateBuilder b = wingShell(Masonry.Style.common());
        b.lootChest(6, 1, 3, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);
        for (int z = 3; z <= 9; z += 3) {
            b.set(3, 1, z, Blocks.BARREL.defaultBlockState());
            b.set(9, 1, z, Blocks.BARREL.defaultBlockState());
        }
        lamps(b, 5, new int[][] {{6, 6}});
        Masonry.dripstone(b, 4, 5, 8, 2);
        return b.build();
    }

    private static CompoundTag wingQuarters() {
        TemplateBuilder b = wingShell(Masonry.Style.common());
        for (int z = 3; z <= 9; z += 3) {
            cot(b, 3, 1, z);
            b.set(9, 1, z, Blocks.BOOKSHELF.defaultBlockState());
        }
        b.lootChest(6, 1, 9, ModWorldGen.OUTPOST_LOOT, Direction.NORTH);
        custode(b, 6.5, 1, 6.5);
        lamps(b, 5, new int[][] {{6, 4}, {6, 8}});
        return b.build();
    }

    /** Galerie effondrée : une impasse à creuser. Le tas raconte d'où il est tombé. */
    private static CompoundTag wingCollapsed() {
        TemplateBuilder b = wingShell(Masonry.Style.plain());
        Masonry.collapse(b, 7, 7, 4, 5, 1, 0x5EED6);
        Masonry.wallBreach(b, 12, 1, 3, 5, true, -1, 0x5EED7);
        b.box(3, 1, 10, 5, 1, 11, BLOOM);
        b.set(10, 1, 10, Blocks.BARREL.defaultBlockState());
        Masonry.puddle(b, 4, 1, 4, 1);
        return b.build();
    }

    private static TemplateBuilder wingShell(Masonry.Style style) {
        TemplateBuilder b = new TemplateBuilder(WING_W, WING_H, WING_D);
        Masonry.chamber(b, 1, 1, 1, WING_W - 2, 4, WING_D - 2, style);
        for (int z = 2; z <= WING_D - 3; z++) {
            b.set(1, 3, z, CONDUIT);
        }
        wingConnector(b, 0, 1, 6, Direction.WEST);
        wingConnector(b, WING_W - 1, 1, 6, Direction.EAST);
        return b;
    }

    // =========================================================================
    // HAMEAU
    // =========================================================================
    //
    // Une maison isolée raconte un survivant ; un hameau raconte un peuple — ce que
    // 02-Lore.md demande de la strate « peuple du réseau ». Les logis sont VOLONTAIREMENT
    // petits et bas (4 blocs de haut) : c'est le contraste avec la voûte de l'Avant-poste
    // qui donne son échelle à l'Avant-poste. Tout mettre à la même hauteur, c'est
    // n'avoir aucune échelle du tout.

    private static final int HAM_W = 13;
    private static final int HAM_H = 8;
    private static final int HAM_D = 13;
    private static final int HOUSE_W = 11;
    private static final int HOUSE_H = 7;
    private static final int HOUSE_D = 11;

    /** La place commune : un puits, quatre départs de galerie, aucun butin. */
    private static CompoundTag hamlet() {
        TemplateBuilder b = new TemplateBuilder(HAM_W, HAM_H, HAM_D);
        Masonry.chamber(b, 1, 1, 1, 11, 4, 11, Masonry.Style.common());

        // Le puits : creusé sous le sol, cerclé de dalles. Un lieu commun se reconnaît à
        // ce qu'il ne contient aucune récompense.
        b.box(5, 0, 5, 7, 0, 7, ROCK);
        b.box(5, 0, 5, 7, 0, 7, Masonry.WATER);
        b.set(6, 0, 6, Masonry.WATER);
        for (int i = 4; i <= 8; i++) {
            b.set(i, 1, 4, SLAB);
            b.set(i, 1, 8, SLAB);
            b.set(4, 1, i, SLAB);
            b.set(8, 1, i, SLAB);
        }
        b.box(5, 1, 5, 7, 1, 7, AIR);

        b.set(3, 1, 3, Blocks.CAULDRON.defaultBlockState());
        b.set(9, 1, 9, Blocks.COMPOSTER.defaultBlockState());
        b.set(3, 1, 9, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(6, 1, 2, CHISELED);
        b.set(6, 1, 10, CHISELED);
        lamps(b, 4, new int[][] {{3, 6}, {9, 6}});
        Masonry.dripstone(b, 8, 4, 3, 2);

        houseConnector(b, 0, 1, 6, Direction.WEST);
        houseConnector(b, HAM_W - 1, 1, 6, Direction.EAST);
        houseConnector(b, 6, 1, 0, Direction.NORTH);
        houseConnector(b, 6, 1, HAM_D - 1, Direction.SOUTH);
        return b.build();
    }

    /** Logis : bas, chaud, une seule pièce. Le loot quotidien de 08-Structures.md. */
    private static CompoundTag hamletDwelling() {
        TemplateBuilder b = houseShell(Masonry.Style.plain(), 3);
        cot(b, 2, 1, 3);
        b.set(2, 1, 6, Blocks.BOOKSHELF.defaultBlockState());
        b.set(8, 1, 3, Blocks.FURNACE.defaultBlockState());
        b.set(8, 1, 5, Blocks.CAULDRON.defaultBlockState());
        b.lootChest(5, 1, 8, ModWorldGen.MODEST_DWELLING_LOOT, Direction.NORTH);
        b.set(7, 1, 8, Blocks.FLOWER_POT.defaultBlockState());
        lamps(b, 4, new int[][] {{5, 5}});
        return b.build();
    }

    /** Atelier de famille : plus haut, voûté, à piliers. Le logis « riche » du hameau. */
    private static CompoundTag hamletWorkshop() {
        TemplateBuilder b = houseShell(Masonry.Style.common(), 4);
        for (int z = 3; z <= 7; z += 2) {
            b.box(3, 1, z, 3, 3, z, CHISELED);
            b.box(7, 1, z, 7, 3, z, CHISELED);
        }
        b.set(5, 1, 8, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(8, 1, 8, Blocks.BARREL.defaultBlockState());
        b.lootChest(2, 1, 8, ModWorldGen.MODEST_DWELLING_LOOT, Direction.EAST);
        cot(b, 2, 1, 2);
        lamps(b, 5, new int[][] {{5, 3}, {5, 7}});
        return b.build();
    }

    /**
     * Citerne : une salle qui n'est pas un logis. Un hameau fait uniquement de maisons est
     * un lotissement ; il lui faut un ouvrage collectif pour ressembler à un village.
     */
    private static CompoundTag hamletCistern() {
        TemplateBuilder b = houseShell(Masonry.Style.common(), 4);
        b.box(2, 0, 2, 8, 0, 8, ROCK);
        b.box(2, 1, 2, 8, 2, 8, AIR);
        b.box(2, 0, 2, 8, 1, 8, Masonry.WATER);
        for (int i = 2; i <= 8; i++) {
            b.set(i, 2, 5, SLAB);
        }
        b.box(5, 1, 1, 5, 2, 1, AIR);
        Masonry.dripstone(b, 3, 5, 3, 3);
        Masonry.dripstone(b, 7, 5, 7, 2);
        lamps(b, 5, new int[][] {{5, 5}});
        return b.build();
    }

    /**
     * Logis effondré : une impasse qu'il faut creuser. Sa fonction est de casser la
     * régularité — un hameau dont tous les logis sont intacts n'a pas été abandonné, il
     * a été construit hier.
     */
    private static CompoundTag hamletCollapsed() {
        TemplateBuilder b = houseShell(Masonry.Style.plain(), 3);
        Masonry.collapse(b, 5, 5, 4, 4, 1, 0x5EED8);
        Masonry.wallBreach(b, 1, 1, 6, 4, true, 1, 0x5EED9);
        b.set(8, 1, 8, Blocks.BARREL.defaultBlockState());
        b.box(2, 1, 8, 3, 1, 8, BLOOM);
        Masonry.puddle(b, 8, 1, 3, 1);
        return b.build();
    }

    private static TemplateBuilder houseShell(Masonry.Style style, int height) {
        TemplateBuilder b = new TemplateBuilder(HOUSE_W, HOUSE_H, HOUSE_D);
        Masonry.chamber(b, 1, 1, 1, HOUSE_W - 2, height, HOUSE_D - 2, style);
        houseConnector(b, 0, 1, 5, Direction.WEST);
        houseConnector(b, HOUSE_W - 1, 1, 5, Direction.EAST);
        return b;
    }

    // =========================================================================
    // PETITES RUINES — la texture de fond du monde
    // =========================================================================
    //
    // Le monde n'avait que DEUX ruines, toutes deux grandes. Une civilisation effondrée
    // ne laisse pas deux bâtiments : elle laisse surtout des miettes. Ces pièces sont
    // minuscules et fréquentes ; leur rôle n'est pas de récompenser, c'est de faire
    // qu'on croise du veskorien tout le temps, et qu'une VRAIE structure se distingue
    // par contraste au lieu d'apparaître de nulle part.

    /** Bout de galerie enseveli : trois mètres de voûte et un tas. Aucune récompense. */
    private static CompoundTag ruinMarker() {
        TemplateBuilder b = new TemplateBuilder(7, 6, 7);
        Masonry.chamber(b, 1, 1, 1, 5, 3, 5, Masonry.Style.plain());
        Masonry.collapse(b, 3, 3, 3, 4, 1, 0x5EEDA);
        Masonry.wallBreach(b, 0, 1, 2, 3, true, 1, 0x5EEDB);
        b.set(5, 1, 5, CONDUIT);
        b.set(5, 2, 5, CONDUIT);
        return b.build();
    }

    /** Borne de conduit : un fût brisé, une base dallée. Le plus petit signe veskorien. */
    private static CompoundTag ruinMarkerPillar() {
        TemplateBuilder b = new TemplateBuilder(5, 7, 5);
        b.box(0, 0, 0, 4, 0, 4, RUBBLE);
        b.box(1, 0, 1, 3, 0, 3, CHISELED);
        b.box(2, 1, 2, 2, 4, 2, BRICK);
        b.set(2, 3, 2, CONDUIT);
        b.set(2, 5, 2, CRACKED);
        for (int[] c : new int[][] {{1, 2}, {3, 2}, {2, 1}, {2, 3}}) {
            b.set(c[0], 1, c[1], SLAB);
        }
        b.set(0, 1, 0, DEBRIS);
        b.set(4, 1, 3, DEBRIS);
        b.set(3, 1, 4, RUBBLE);
        return b.build();
    }

    /**
     * Chambre engloutie : une salle unique, à demi comblée, un coffre. C'est le format
     * « on a trouvé quelque chose » sans être un donjon — le palier manquant entre la
     * borne et l'Avant-poste.
     */
    private static CompoundTag sunkenChamber() {
        TemplateBuilder b = new TemplateBuilder(13, 10, 13);
        Masonry.chamber(b, 2, 2, 2, 10, 5, 10, Masonry.Style.common());
        Masonry.collapse(b, 4, 4, 4, 6, 2, 0x5EEDC);
        Masonry.wallBreach(b, 11, 2, 6, 4, true, -1, 0x5EEDD);
        Masonry.puddle(b, 8, 2, 8, 2);
        Masonry.dripstone(b, 7, 6, 4, 3);
        b.lootChest(9, 2, 3, ModWorldGen.MODEST_DWELLING_LOOT, Direction.WEST);
        b.set(3, 2, 9, Blocks.BARREL.defaultBlockState());
        b.box(9, 2, 9, 10, 2, 10, BLOOM);
        for (int z = 3; z <= 9; z++) {
            b.set(2, 4, z, z == 6 ? CRACKED : CONDUIT);
        }
        b.set(6, 2, 2, CHISELED);
        return b.build();
    }

    // =========================================================================
    // Bouchons de fin de branche
    // =========================================================================

    /**
     * Bouchon : un mur d'un bloc d'épaisseur qui referme une branche non poursuivie.
     * Sans lui, une structure sur deux se termine par un trou béant sur la roche, là où le
     * jigsaw a atteint sa profondeur maximale. D'où sa déclaration en {@code fallback} de
     * tous les pools : le fallback est précisément ce que le jigsaw pose quand il ne peut
     * plus continuer. Son connecteur a pour {@code final_state} de la <b>brique</b> — un
     * bouchon qui se remplace par de l'air ne bouche rien.
     */
    private static CompoundTag cap(int height, int depth) {
        TemplateBuilder b = new TemplateBuilder(1, height, depth);
        b.box(0, 0, 0, 0, height - 1, depth - 1, BRICK);
        b.jigsaw(0, 1, depth / 2, Direction.WEST, "veskorius:corridor", "veskorius:corridor",
            net.minecraft.data.worldgen.Pools.EMPTY, BRICK, true);
        return b.build();
    }

    // --- Mobilier ---------------------------------------------------------------

    /** Lampes de résonance en voûte. Éteintes tant qu'aucun champ ne les couvre. */
    private static void lamps(TemplateBuilder b, int y, int[][] spots) {
        for (int[] s : spots) {
            b.set(s[0], y, s[1], LAMP);
        }
    }

    /**
     * Couchette veskorienne : une dalle et un dossier gravé. (Les lits vanilla occupent
     * deux blocs, et une moitié posée seule est <b>retirée</b> par la mise à jour de
     * voisinage — ils disparaissaient sans un mot. Correction meilleure que l'original :
     * une civilisation de la Résonance ne dort pas sur de la laine teinte.)
     */
    private static void cot(TemplateBuilder b, int x, int y, int z) {
        b.set(x, y, z, SLAB);
        b.set(x, y, z + 1, SLAB);
        b.set(x, y + 1, z, CHISELED);
    }

    /**
     * Alcôve de dock : une niche de deux blocs creusée <b>dans l'épaisseur du mur</b>, avec
     * son linteau. Un Custode rangé qui s'en extrait vaut dix Custodes plantés au sol — et
     * une alcôve <b>vide</b> raconte autant qu'une occupée.
     */
    private static void alcove(TemplateBuilder b, int x, int y, int z, boolean alongZ,
                               boolean occupied) {
        int dx = alongZ ? 0 : 1;
        int dz = alongZ ? 1 : 0;
        b.set(x, y, z, AIR);
        b.set(x, y + 1, z, AIR);
        b.set(x, y + 2, z, CHISELED);
        b.set(x + dx, y, z + dz, CHISELED);
        b.set(x - dx, y, z - dz, CHISELED);
        b.set(x + dx, y + 1, z + dz, BRICK);
        b.set(x - dx, y + 1, z - dz, BRICK);
        if (occupied) {
            custode(b, x + 0.5, y, z + 0.5);
        }
    }

    /** Un Custode persistant en poste (ne despawn jamais, réactif seulement de près). */
    private static void custode(TemplateBuilder b, double x, int y, double z) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "veskorius:custode");
        tag.putBoolean("PersistenceRequired", true);
        b.entity(x, y, z, (int) x, y, (int) z, tag);
    }

    private static void wingConnector(TemplateBuilder b, int x, int y, int z, Direction front) {
        b.jigsaw(x, y, z, front, "veskorius:corridor", "veskorius:corridor",
            ModStructures.OUTPOST_WING_POOL, AIR, true);
        b.set(x, y + 1, z, AIR);
    }

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
