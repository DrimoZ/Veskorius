package com.veskorius.datagen;

import static com.veskorius.datagen.Masonry.AIR;
import static com.veskorius.datagen.Masonry.BLOOM;
import static com.veskorius.datagen.Masonry.BRICK;
import static com.veskorius.datagen.Masonry.BULKHEAD;
import static com.veskorius.datagen.Masonry.CHISELED;
import static com.veskorius.datagen.Masonry.COPPER;
import static com.veskorius.datagen.Masonry.CRACKED;
import static com.veskorius.datagen.Masonry.GLASS;
import static com.veskorius.datagen.Masonry.GRATE;
import static com.veskorius.datagen.Masonry.PAVING;
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
 * Génère les <b>pièces de structure</b> (NBT) — 08-Structures.md pour le contenu,
 * 17-Dungeons.md pour la forme, {@link Masonry} pour les gestes.
 *
 * <p><b>Troisième passe (2026-08-07) : le monumental.</b> Les deux premières avaient
 * corrigé la boîte creuse puis le plafond plat ; il restait le défaut le plus difficile à
 * nommer — <i>« des micro-salles hyper-chargées en blocs pas utiles »</i>. Deux règles en
 * sortent, et elles gouvernent tout ce fichier :
 *
 * <ol>
 *   <li><b>Une civilisation se lit à ses proportions et à ses ordres, jamais à son
 *       mobilier.</b> Une salle de 27 mètres à double colonnade et huit mètres sous voûte
 *       dit « ils étaient nombreux et ils bâtissaient » ; la même salle réduite à 9
 *       mètres et remplie de tonneaux, d'établis et de pots de fleurs dit « un ordinateur
 *       a rempli une case ». Donc : <b>de la hauteur et du vide plutôt que des objets</b>,
 *       et au plus deux ou trois meubles par salle.</li>
 *   <li><b>Rien ne flotte.</b> Lampes et conduits <b>remplacent</b> le bloc de mur, ils ne
 *       s'y accolent pas ; le centre d'une salle s'éclaire au lustre, accroché à sa clé de
 *       voûte. C'était le défaut le plus visible et le plus bête : des rangées de blocs en
 *       lévitation le long de chaque paroi.</li>
 * </ol>
 *
 * <p>Retirés au passage, et pour de bon : <b>le gravier</b> (bloc à gravité — il
 * s'effondre au premier chargement de chunk et emporte le dessin) et <b>les sources
 * d'eau</b> (décoratives sur le papier, une inondation dès qu'un bloc voisin manque).
 *
 * <p><b>L'invariant de contenu</b> (17-Dungeons.md §3) : le chemin critique — console,
 * sas, émetteur ancien, coffre d'amorçage, coffre-réserve — vit <b>dans la pièce de
 * départ</b>. Le pool ne sert qu'aux ailes facultatives.
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

    public static final String SIGMA_LABORATORY = "sigma_laboratory";
    public static final String GUARD_POST = "guard_post";
    public static final String DRILL_SHAFT = "drill_shaft";

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
        pieces.put(SIGMA_LABORATORY, sigmaLaboratory());
        pieces.put(GUARD_POST, guardPost());
        pieces.put(DRILL_SHAFT, drillShaft());
        pieces.put(RUIN_MARKER, ruinMarker());
        pieces.put(RUIN_MARKER_PILLAR, ruinMarkerPillar());
        pieces.put(SUNKEN_CHAMBER, sunkenChamber());
        return CompletableFuture.allOf(pieces.entrySet().stream()
            .map(e -> write(cache, e.getKey(), e.getValue()))
            .toArray(CompletableFuture[]::new));
    }

    private static final String STABLE_CRYSTAL = "veskorius:stable_resonance_crystal";

    // =========================================================================
    // AVANT-POSTE — 39×31×39
    // =========================================================================
    //
    //   Deux niveaux, reliés par une VIS unique. Chaque salle est creusée à part ; entre
    //   elles il reste de la roche. Le circuit est une ARBORESCENCE, pas un couloir :
    //
    //        vestibule ──┬── vis ──▼── salle des machines ──[SAS]── rotonde (console)
    //                    └── grande nef  (le lore, le butin — branche facultative)
    //
    //   PLAN, niveau haut (y18-25)          PLAN, niveau bas (y3-10)
    //   z 0        12       24      38      z 0        12       24      38
    // x ┌──────────────────────────────┐  x ┌──────────────────────────────┐
    // 0 │ ╭───╮   ┌──────────┐         │  0 │ ╭───╮                        │
    //   │ │VIS│═══│ VESTIBULE│         │    │ │VIS│                        │
    // 8 │ ╰─╥─╯   └────╥─────┘         │  8 │ ╰─╥─╯                        │
    //   │   ║          ║               │    │   ╚══╗                       │
    //16 │ ┌─╨──────────╨──────────────┐│ 16 │ ┌────╨─────────┐  ╭────────╮ │
    //   │ │   G R A N D E   N E F     ││    │ │ SALLE DES    │B │ ROTONDE│ │
    //30 │ └───────────────────────────┘│ 30 │ └ MACHINES ────┘  ╰────────╯ │
    // 38└──────────────────────────────┘  38└──────────────────────────────┘

    private static final int OUT_SIZE = 39;
    private static final int OUT_H = 31;
    /** Niveau bas : sol foulé à y=3, huit blocs sous voûte. */
    private static final int LOW = 3;
    /** Niveau haut : sol foulé à y=18, huit blocs sous voûte. */
    private static final int HIGH = 18;
    private static final int VIS_X = 6;
    private static final int VIS_Z = 6;

    private static CompoundTag outpost() {
        TemplateBuilder b = new TemplateBuilder(OUT_SIZE, OUT_H, OUT_SIZE);

        // La VIS d'abord : c'est la colonne vertébrale. Elle n'a qu'UNE entrée par
        // niveau — deux paliers au même étage sur une vis, c'est une chute garantie du
        // côté qui n'a pas la marche.
        Masonry.spiralStair(b, VIS_X, VIS_Z, HIGH - 1, LOW - 1, Direction.EAST);
        Masonry.conduitDrop(b, VIS_X - 1, LOW, VIS_Z - 1, HIGH - 2);

        outpostVestibule(b);
        outpostNave(b);
        outpostMachineHall(b);
        outpostRotunda(b);
        return b.build();
    }

    /**
     * <b>Vestibule</b> — l'entrée, et le seul endroit où le monde pénètre la ruine. Sa
     * voûte s'est ouverte : le bouchon de roche qui la remplace est ce qu'une grotte peut
     * croiser, donc ce par quoi on tombe dessus en explorant.
     *
     * <p>Volontairement <b>vide</b>. Une salle d'accueil encombrée de tonneaux ne dit rien ;
     * une salle haute, nue, dont un pan de voûte gît au sol, dit qu'on entre quelque part.
     */
    private static void outpostVestibule(TemplateBuilder b) {
        Masonry.chamber(b, 14, HIGH, 2, 24, HIGH + 7, 12, Masonry.Style.noble());
        Masonry.gallery(b, 10, HIGH, 6, 13, 6);
        Masonry.gallery(b, 19, HIGH, 13, 19, 15);

        Masonry.collapse(b, 19, 7, 4, HIGH + 8, HIGH, 0x5EED1);
        Masonry.wallBreach(b, 25, HIGH, 3, 5, true, -1, 0x5EED2);
        Masonry.silt(b, 16, HIGH - 1, 11, 2);

        // Alcôve de Custode, VIDE. Quelqu'un veillait ici, et n'y est plus.
        alcove(b, 13, HIGH, 9, false, false);
        Masonry.sconce(b, 13, HIGH + 3, 4, Direction.Axis.Z);
        Masonry.sconce(b, 25, HIGH + 3, 9, Direction.Axis.Z);
        Masonry.conduitRun(b, 25, HIGH + 5, 3, 25, 11);
        b.set(25, HIGH + 5, 7, CRACKED);

        wingConnector(b, 19, HIGH, 1, Direction.NORTH);
    }

    /**
     * <b>Grande nef</b> — 31 mètres de long, deux colonnades, huit mètres sous voûte. C'est
     * la pièce qui doit faire dire « ils étaient une civilisation », et elle le fait par sa
     * <b>section</b>, pas par son contenu : trois meubles en tout.
     *
     * <p>Un tiers de la voûte manque, et sa matière est là, en cône, sous le trou. On
     * contourne le tas pour traverser : c'est lui qui dessine le cheminement, pas une
     * cloison.
     */
    private static void outpostNave(TemplateBuilder b) {
        Masonry.chamber(b, 4, HIGH, 16, 34, HIGH + 7, 30, Masonry.Style.noble());

        // Les deux colonnades : elles découpent la nef en vaisseau central et bas-côtés.
        // C'est la répétition verticale qui donne la hauteur, pas la hauteur elle-même.
        for (int z : new int[] {20, 26}) {
            for (int x = 7; x <= 31; x += 4) {
                for (int y = HIGH; y <= HIGH + 6; y++) {
                    b.set(x, y, z, Masonry.column(Direction.Axis.Y));
                }
                b.set(x, HIGH, z, CHISELED);
                b.set(x, HIGH + 6, z, CHISELED);
                b.set(x, HIGH + 7, z, CHISELED);
                if (x + 4 <= 31) {
                    b.set(x + 1, HIGH + 7, z, Masonry.stair(Direction.EAST, true));
                    b.set(x + 3, HIGH + 7, z, Masonry.stair(Direction.WEST, true));
                }
            }
        }
        // Bas-côtés surélevés d'un gradin : le vaisseau central se creuse sans qu'on ait
        // rien à descendre.
        Masonry.terrace(b, 5, HIGH, 17, 33, 19, PAVING);
        Masonry.terrace(b, 5, HIGH, 27, 33, 29, PAVING);

        Masonry.collapse(b, 14, 24, 5, HIGH + 8, HIGH, 0x5EED3);
        Masonry.wallBreach(b, 3, HIGH, 22, 6, true, 1, 0x5EED4);

        // LE JOURNAL : quatre coffres DANS L'ORDRE, sous une arcade aveugle du mur nord.
        // On lit une descente en longeant la nef, pas une anecdote tirée au sort.
        Masonry.arcade(b, 35, HIGH, 18, 28, 1);
        ResourceLocation[] log = {
            CodexEntries.OUTPOST_LOG_1, CodexEntries.OUTPOST_LOG_2,
            CodexEntries.OUTPOST_LOG_3, CodexEntries.OUTPOST_LOG_4,
        };
        for (int i = 0; i < log.length; i++) {
            b.fragmentChest(10 + i * 6, HIGH, 16, log[i], Direction.SOUTH);
            b.set(10 + i * 6, HIGH + 1, 15, CHISELED);
        }
        b.lootChest(6, HIGH, 23, ModWorldGen.OUTPOST_LOOT, Direction.EAST);
        custode(b, 19.5, HIGH, 23.5);

        Masonry.chandelier(b, 11, HIGH + 7, 23, 2);
        Masonry.chandelier(b, 19, HIGH + 7, 23, 2);
        Masonry.chandelier(b, 27, HIGH + 7, 23, 2);
        Masonry.sconce(b, 3, HIGH + 3, 25, Direction.Axis.Z);
        Masonry.conduitRun(b, 3, HIGH + 5, 17, 3, 29);
    }

    /**
     * <b>Salle des machines</b> — la seule pièce encore équipée. L'émetteur ancien y trône
     * sur une estrade à gradins, à sec ; le sas est dans le mur est ; et le coffre-réserve,
     * au contenu <b>fixe</b>, garantit le cristal qui ouvre tout.
     */
    private static void outpostMachineHall(TemplateBuilder b) {
        Masonry.chamber(b, 4, LOW, 14, 23, LOW + 7, 30, Masonry.Style.noble());
        Masonry.gallery(b, VIS_X, LOW, 10, VIS_X, 13);
        Masonry.colonnade(b, 9, LOW, 17, 27, LOW + 7);
        Masonry.colonnade(b, 18, LOW, 17, 27, LOW + 7);

        // Estrade à deux gradins. Le relief désigne la pièce maîtresse sans une lampe de
        // plus — et il porte l'émetteur au niveau du regard.
        Masonry.terrace(b, 11, LOW - 1, 20, 16, 25, PAVING);
        Masonry.terrace(b, 12, LOW, 21, 15, 24, CHISELED);
        b.set(13, LOW + 1, 22, ModBlocks.ANCIENT_EMITTER.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        b.set(14, LOW + 1, 22, Blocks.SMITHING_TABLE.defaultBlockState());

        b.itemChest(16, LOW + 1, 22, STABLE_CRYSTAL, 1, Direction.WEST);
        b.lootChest(6, LOW, 16, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);

        // Deux Custodes rangés en alcôve : ils se lèvent quand la salle se réveille.
        alcove(b, 3, LOW, 19, true, true);
        alcove(b, 3, LOW, 27, true, true);

        Masonry.conduitRun(b, 3, LOW + 5, 15, 3, 29);
        Masonry.conduitRun(b, 24, LOW + 5, 15, 24, 29);
        Masonry.sconce(b, 3, LOW + 3, 23, Direction.Axis.Z);
        Masonry.sconce(b, 24, LOW + 3, 17, Direction.Axis.Z);
        Masonry.chandelier(b, 13, LOW + 8, 17, 2);
        Masonry.chandelier(b, 13, LOW + 8, 28, 2);

        // Neuf siècles de champ qui dérive laissent des traces (02-Lore.md, Âge 4).
        b.box(20, LOW, 28, 22, LOW, 29, BLOOM);
        b.set(21, LOW + 1, 29, BLOOM);
        Masonry.silt(b, 8, LOW - 1, 28, 2);
        Masonry.wallBreach(b, 12, LOW, 31, 5, false, -1, 0x5EED5);

        // LE SAS, dans le mur est : un bloc de large, deux de haut, indestructible, avec
        // une baie de part et d'autre — un verrou qui ne montre pas ce qu'il garde n'est
        // qu'un mur.
        b.box(24, LOW, 21, 24, LOW + 3, 23, BRICK);
        b.set(24, LOW, 22, BULKHEAD);
        b.set(24, LOW + 1, 22, BULKHEAD);
        b.set(24, LOW + 1, 21, GLASS);
        b.set(24, LOW + 1, 23, GLASS);
        b.set(24, LOW + 2, 22, CHISELED);
        b.set(24, LOW + 3, 22, GRATE);
    }

    /**
     * <b>Rotonde de la console</b> — octogonale, à colonnes engagées, coiffée d'une
     * coupole. Une salle dont le plan n'est pas rectangulaire est lue comme importante
     * avant qu'on ait rien écrit dedans : c'est le moyen le moins cher de dire « ceci est
     * la pièce maîtresse », et le seul qui ne dépende pas de l'éclairage.
     */
    private static void outpostRotunda(TemplateBuilder b) {
        Masonry.rotunda(b, 31, LOW, 22, 5, 8);
        b.box(25, LOW, 21, 25, LOW + 2, 23, AIR);

        Masonry.terrace(b, 29, LOW, 20, 33, 24, PAVING);
        b.set(31, LOW + 1, 22, ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState());
        Masonry.chandelier(b, 31, LOW + 12, 22, 3);
        for (int[] c : new int[][] {{28, 19}, {34, 19}, {28, 25}, {34, 25}}) {
            Masonry.sconce(b, c[0], LOW + 3, c[1], Direction.Axis.Z);
        }
        Masonry.conduitDrop(b, 31, LOW, 17, LOW + 6);
        Masonry.conduitDrop(b, 31, LOW, 27, LOW + 6);
    }

    // =========================================================================
    // Ailes facultatives — 15×11×15
    // =========================================================================

    private static final int WING_W = 15;
    private static final int WING_H = 11;
    private static final int WING_D = 15;

    private static CompoundTag wingStore() {
        TemplateBuilder b = wingShell(Masonry.Style.common());
        Masonry.arcade(b, 12, 2, 4, 10, 1);
        b.lootChest(7, 2, 3, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);
        b.set(12, 2, 5, Blocks.BARREL.defaultBlockState());
        b.set(12, 2, 9, Blocks.BARREL.defaultBlockState());
        Masonry.chandelier(b, 7, 9, 7, 2);
        return b.build();
    }

    private static CompoundTag wingQuarters() {
        TemplateBuilder b = wingShell(Masonry.Style.common());
        for (int z = 4; z <= 10; z += 3) {
            cot(b, 2, 2, z);
        }
        b.lootChest(12, 2, 7, ModWorldGen.OUTPOST_LOOT, Direction.WEST);
        custode(b, 7.5, 2, 7.5);
        Masonry.chandelier(b, 7, 9, 7, 2);
        Masonry.sconce(b, 7, 5, 1, Direction.Axis.X);
        return b.build();
    }

    /** Galerie effondrée : une impasse à creuser. Le tas raconte d'où il est tombé. */
    private static CompoundTag wingCollapsed() {
        TemplateBuilder b = wingShell(Masonry.Style.plain());
        Masonry.collapse(b, 8, 8, 5, 7, 2, 0x5EED6);
        Masonry.wallBreach(b, 14, 2, 4, 6, true, -1, 0x5EED7);
        b.box(2, 2, 11, 4, 2, 12, BLOOM);
        Masonry.silt(b, 4, 1, 4, 2);
        return b.build();
    }

    private static TemplateBuilder wingShell(Masonry.Style style) {
        TemplateBuilder b = new TemplateBuilder(WING_W, WING_H, WING_D);
        Masonry.chamber(b, 2, 2, 2, WING_W - 3, 6, WING_D - 3, style);
        Masonry.conduitRun(b, 1, 5, 3, 1, 11);
        wingConnector(b, 1, 2, 7, Direction.WEST);
        wingConnector(b, WING_W - 2, 2, 7, Direction.EAST);
        return b;
    }

    // =========================================================================
    // HAMEAU — 25×17×25 : une nef, pas un lotissement
    // =========================================================================
    //
    // « Une maison isolée raconte un survivant, un hameau raconte un peuple. » La première
    // version prenait ça au pied de la lettre : quatre cabanes autour d'une placette, donc
    // un lotissement. Un peuple qui creuse ne juxtapose pas des maisons — il taille UNE
    // halle et loge dans ses parois. D'où : une nef à colonnade, des logis en alcôves dans
    // les bas-côtés, un escalier d'apparat pour y descendre.

    private static final int HAM_W = 25;
    private static final int HAM_H = 17;
    private static final int HAM_D = 25;
    private static final int HOUSE_W = 15;
    private static final int HOUSE_H = 12;
    private static final int HOUSE_D = 15;

    private static CompoundTag hamlet() {
        TemplateBuilder b = new TemplateBuilder(HAM_W, HAM_H, HAM_D);
        Masonry.chamber(b, 3, 4, 3, 21, 11, 21, Masonry.Style.noble());

        // Deux colonnades, un vaisseau central creusé d'un gradin : la halle.
        Masonry.colonnade(b, 8, 4, 5, 19, 11);
        Masonry.colonnade(b, 16, 4, 5, 19, 11);
        b.box(9, 3, 5, 15, 3, 19, PAVING);
        for (int z = 5; z <= 19; z++) {
            b.set(8, 3, z, Masonry.pavingStair(Direction.WEST, false));
            b.set(16, 3, z, Masonry.pavingStair(Direction.EAST, false));
        }

        // L'escalier d'apparat : on descend DANS la halle, on n'y tombe pas.
        Masonry.grandStair(b, 11, 13, 4, 8, 4);

        // Le foyer commun : un âtre de cuivre au centre du vaisseau, et rien d'autre.
        // Un lieu commun se reconnaît à ce qu'il ne contient aucune récompense.
        b.box(11, 3, 11, 13, 3, 13, COPPER);
        b.set(12, 4, 12, Blocks.CAMPFIRE.defaultBlockState()
            .setValue(BlockStateProperties.LIT, Boolean.FALSE));
        Masonry.chandelier(b, 12, 12, 8, 2);
        Masonry.chandelier(b, 12, 12, 16, 2);
        Masonry.conduitRun(b, 2, 8, 4, 2, 20);
        Masonry.conduitRun(b, 22, 8, 4, 22, 20);
        Masonry.sconce(b, 2, 6, 12, Direction.Axis.Z);
        Masonry.sconce(b, 22, 6, 12, Direction.Axis.Z);

        // Les logis sont TAILLÉS DANS LES BAS-CÔTÉS, pas accolés dehors : c'est ça, un
        // habitat creusé. Le pool n'ajoute que les annexes qui débordent de la halle.
        Masonry.arcade(b, 3, 4, 5, 19, 2);
        Masonry.arcade(b, 21, 4, 5, 19, 2);
        for (int z : new int[] {5, 9, 13, 17}) {
            cot(b, 3, 4, z);
            cot(b, 21, 4, z);
        }
        b.lootChest(3, 4, 18, ModWorldGen.MODEST_DWELLING_LOOT, Direction.EAST);
        b.lootChest(21, 4, 6, ModWorldGen.MODEST_DWELLING_LOOT, Direction.WEST);

        Masonry.collapse(b, 18, 18, 4, 12, 4, 0x5EEDE);

        houseConnector(b, 2, 4, 6, Direction.WEST);
        houseConnector(b, HAM_W - 3, 4, 18, Direction.EAST);
        houseConnector(b, 12, 4, 2, Direction.NORTH);
        houseConnector(b, 6, 4, HAM_D - 3, Direction.SOUTH);
        return b.build();
    }

    /** Annexe d'habitation : une salle voûtée avec sa niche de couchage. */
    private static CompoundTag hamletDwelling() {
        TemplateBuilder b = houseShell(Masonry.Style.common(), 6);
        Masonry.arcade(b, 12, 2, 4, 10, 1);
        cot(b, 12, 2, 5);
        cot(b, 12, 2, 9);
        b.set(2, 2, 3, Blocks.FURNACE.defaultBlockState());
        b.lootChest(7, 2, 11, ModWorldGen.MODEST_DWELLING_LOOT, Direction.NORTH);
        Masonry.chandelier(b, 7, 9, 7, 2);
        return b.build();
    }

    /** Atelier : une halle à quatre colonnes libres. Le logis « riche » du hameau. */
    private static CompoundTag hamletWorkshop() {
        TemplateBuilder b = houseShell(Masonry.Style.noble(), 7);
        for (int[] c : new int[][] {{5, 5}, {9, 5}, {5, 9}, {9, 9}}) {
            for (int y = 2; y <= 8; y++) {
                b.set(c[0], y, c[1], Masonry.column(Direction.Axis.Y));
            }
            b.set(c[0], 2, c[1], CHISELED);
            b.set(c[0], 8, c[1], CHISELED);
        }
        b.set(7, 2, 11, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.lootChest(2, 2, 11, ModWorldGen.MODEST_DWELLING_LOOT, Direction.EAST);
        Masonry.chandelier(b, 7, 10, 7, 2);
        return b.build();
    }

    /**
     * Citerne : une salle qui n'est pas un logis. Un hameau fait uniquement de maisons est
     * un lotissement ; il lui faut un ouvrage collectif pour ressembler à un village.
     * Elle est <b>à sec</b> — c'est une ruine, et une source d'eau décorative devient une
     * inondation dès qu'un bloc voisin manque.
     */
    private static CompoundTag hamletCistern() {
        TemplateBuilder b = houseShell(Masonry.Style.noble(), 7);
        b.box(3, 1, 3, 11, 3, 11, AIR);
        b.box(3, 0, 3, 11, 0, 11, Masonry.TILE);
        for (int i = 3; i <= 11; i++) {
            b.set(i, 1, 3, Masonry.stair(Direction.SOUTH, false));
            b.set(i, 1, 11, Masonry.stair(Direction.NORTH, false));
            b.set(3, 1, i, Masonry.stair(Direction.EAST, false));
            b.set(11, 1, i, Masonry.stair(Direction.WEST, false));
        }
        Masonry.silt(b, 7, 0, 7, 3);
        b.box(7, 2, 3, 7, 8, 3, GRATE);
        Masonry.chandelier(b, 7, 10, 7, 3);
        return b.build();
    }

    /** Annexe effondrée : une impasse qu'il faut creuser. Elle casse la régularité. */
    private static CompoundTag hamletCollapsed() {
        TemplateBuilder b = houseShell(Masonry.Style.plain(), 5);
        Masonry.collapse(b, 7, 7, 5, 6, 2, 0x5EED8);
        Masonry.wallBreach(b, 1, 2, 5, 5, true, 1, 0x5EED9);
        b.box(11, 2, 10, 12, 2, 11, BLOOM);
        Masonry.silt(b, 3, 1, 3, 2);
        return b.build();
    }

    private static TemplateBuilder houseShell(Masonry.Style style, int height) {
        TemplateBuilder b = new TemplateBuilder(HOUSE_W, HOUSE_H, HOUSE_D);
        Masonry.chamber(b, 2, 2, 2, HOUSE_W - 3, height, HOUSE_D - 3, style);
        houseConnector(b, 1, 2, 7, Direction.WEST);
        houseConnector(b, HOUSE_W - 2, 2, 7, Direction.EAST);
        return b;
    }

    // =========================================================================
    // POSTE DE GARDE — la tour inversée (08-Structures.md, 17-Dungeons.md §5.3)
    // =========================================================================
    //
    // Une garnison qui DESCEND. C'est l'image la plus marquante pour le moins cher, et
    // elle porte le sens du lieu : les Custodes gardaient ce qui était en dessous.
    //
    // Deux choix font toute la salle, et aucun ne coûte un bloc de plus :
    //
    //   1. L'escalier est OUVERT SUR LE VIDE. Le combat devient positionnel — on recule,
    //      on contourne, on tombe — au lieu d'être un échange de coups dans un couloir.
    //   2. LA LUMIÈRE AUGMENTE EN DESCENDANT, l'inverse de l'intuition. Ce n'est pas un
    //      effet : ça DIT où le réseau a survécu, et donc pourquoi les gardiens du bas
    //      sont encore debout quand les alcôves du haut sont vides.
    //
    //   y30 ┌───────┐  meurtrière — l'entrée, au ras d'une grotte
    //   y24 │  ░ ░  │  alcôves VIDES               ← noir
    //   y17 │  ░█░  │  corps de garde (1 Custode)  ← noir
    //   y10 │  ▓█▓  │  poste bas (2 Custodes)      ← ça s'allume
    //   y 3 │ █████ │  ARSENAL (butin T2)          ← plein jour
    //       └───────┘

    private static final int POST_SIZE = 27;
    private static final int POST_H = 32;
    private static final int POST_C = 13;
    /** Marche haute et fond de cage. 27 → 4 = exactement les 24 cases d'un tour de vis. */
    private static final int POST_TOP = 27;
    private static final int POST_BOTTOM = 4;

    private static CompoundTag guardPost() {
        TemplateBuilder b = new TemplateBuilder(POST_SIZE, POST_H, POST_SIZE);

        // L'ARSENAL EN PREMIER, la vis par-dessus : la cage traverse la salle et y plante
        // son noyau de maçonnerie. C'est l'ordre qui compte — l'inverse ferait creuser
        // l'arsenal À TRAVERS l'escalier, effaçant ses huit dernières marches et
        // transformant l'arrivée en chute de sept blocs.
        guardArsenal(b);
        Masonry.spiralStair(b, POST_C, POST_C, POST_TOP, POST_BOTTOM, Direction.NORTH);

        // Le puits central : on évide le noyau, si bien que l'escalier tourne autour d'un
        // VIDE de vingt-trois blocs et non d'une colonne pleine. C'est ce vide qui rend le
        // combat positionnel — on recule, on contourne, on tombe.
        b.box(POST_C - 1, POST_BOTTOM + 1, POST_C - 1, POST_C + 1, POST_TOP - 1, POST_C + 1, AIR);
        for (int y = POST_BOTTOM; y <= POST_TOP + 1; y++) {
            for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
                b.set(POST_C + c[0], y, POST_C + c[1], Masonry.column(Direction.Axis.Y));
            }
        }
        // La gueule de la tour : la cage s'ouvre en grand sur l'arsenal. On y débouche à
        // découvert, avec deux gardes déjà en poste — le seul endroit du mod où le joueur
        // est vu avant de voir.
        b.box(POST_C - 3, POST_BOTTOM + 1, POST_C - 4, POST_C + 3, POST_BOTTOM + 2, POST_C - 4, AIR);
        for (int x = POST_C - 3; x <= POST_C + 3; x++) {
            b.set(x, POST_BOTTOM + 3, POST_C - 4, x % 3 == 1 ? GRATE : CHISELED);
        }

        // L'entrée : une meurtrière au niveau de la marche haute, plein nord.
        Masonry.gallery(b, POST_C, POST_TOP + 1, POST_C - 8, POST_C, POST_C - 4);
        b.set(POST_C, POST_TOP + 3, POST_C - 8, GRATE);

        // Les paliers. Chacun s'ouvre EXACTEMENT sur la marche de sa hauteur — calculée,
        // jamais estimée : une ouverture « à peu près au bon endroit » débouche à côté de
        // la marche, c'est-à-dire dans le vide du puits.
        guardLanding(b, 22, false, false);
        guardLanding(b, 16, true, true);
        return b.build();
    }

    /**
     * Un palier : une chambre creusée à l'écart du puits, reliée par une galerie qui part
     * de la marche de cette hauteur. Les alcôves du fond sont <b>vides</b> en haut et
     * occupées en bas — une rangée de niches désertes dit « ils étaient plus nombreux »
     * sans un mot, et prépare la rencontre d'en dessous.
     */
    private static void guardLanding(TemplateBuilder b, int y, boolean manned, boolean lit) {
        int[] e = Masonry.spiralExit(POST_C, POST_C, POST_TOP, Direction.NORTH, y);
        int dx = e[2];
        int dz = e[3];
        int nearX = POST_C + dx * 5;
        int nearZ = POST_C + dz * 5;
        int farX = POST_C + dx * 11;
        int farZ = POST_C + dz * 11;

        // La chambre : cinq de profondeur, sept de large, perpendiculaire à la sortie.
        Masonry.chamber(b,
            Math.min(nearX + dx, farX) - (dx == 0 ? 3 : 0), y, Math.min(nearZ + dz, farZ) - (dz == 0 ? 3 : 0),
            Math.max(nearX + dx, farX) + (dx == 0 ? 3 : 0), y + 4, Math.max(nearZ + dz, farZ) + (dz == 0 ? 3 : 0),
            Masonry.Style.common());
        // La galerie part du mur de cage (distance 4) et finit dans le mur de la chambre.
        if (dx != 0) {
            Masonry.gallery(b, Math.min(POST_C + dx * 4, nearX), y, e[1],
                Math.max(POST_C + dx * 4, nearX), e[1]);
        } else {
            Masonry.gallery(b, e[0], y, Math.min(POST_C + dz * 4, nearZ),
                e[0], Math.max(POST_C + dz * 4, nearZ));
        }

        for (int k = -2; k <= 2; k += 2) {
            alcove(b, farX + dx + (dx == 0 ? k : 0), y, farZ + dz + (dz == 0 ? k : 0),
                dx == 0, manned && k == 0);
        }
        if (manned) {
            b.lootChest(farX - dx, y, farZ - dz, ModWorldGen.OUTPOST_LOOT, Direction.NORTH);
        }
        if (lit) {
            Masonry.chandelier(b, (nearX + farX) / 2, y + 5, (nearZ + farZ) / 2, 1);
        }
    }

    /** L'arsenal : la seule salle éclairée, et la seule qui récompense. */
    private static void guardArsenal(TemplateBuilder b) {
        Masonry.chamber(b, 3, POST_BOTTOM + 1, 3, 23, POST_BOTTOM + 7, 23, Masonry.Style.noble());
        Masonry.colonnade(b, 7, POST_BOTTOM + 1, 7, 19, POST_BOTTOM + 7);
        Masonry.colonnade(b, 19, POST_BOTTOM + 1, 7, 19, POST_BOTTOM + 7);

        b.lootChest(5, POST_BOTTOM + 1, 5, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);
        b.lootChest(21, POST_BOTTOM + 1, 21, ModWorldGen.OUTPOST_LOOT, Direction.NORTH);
        custode(b, 7.5, POST_BOTTOM + 1, 20.5);
        custode(b, 20.5, POST_BOTTOM + 1, 7.5);

        Masonry.conduitRun(b, 2, POST_BOTTOM + 4, 4, 2, 22);
        Masonry.conduitRun(b, 24, POST_BOTTOM + 4, 4, 24, 22);
        Masonry.sconce(b, 2, POST_BOTTOM + 3, 13, Direction.Axis.Z);
        Masonry.sconce(b, 24, POST_BOTTOM + 3, 13, Direction.Axis.Z);
        Masonry.chandelier(b, 7, POST_BOTTOM + 8, 7, 2);
        Masonry.chandelier(b, 19, POST_BOTTOM + 8, 19, 2);
        Masonry.wallBreach(b, 14, POST_BOTTOM + 1, 24, 5, false, -1, 0x5EEE1);
        Masonry.silt(b, 6, POST_BOTTOM, 20, 2);
    }

    // =========================================================================
    // SIGMA LABORATORY — la roue (08-Structures.md, 17-Dungeons.md §5.4)
    // =========================================================================
    //
    //   Le seul VRAI puzzle du mod, et il tient en une phrase : réparer deux relais avant
    //   que le premier ne retombe. Il n'a fallu inventer aucune serrure à deux clés — les
    //   relais sont posés EN CHAÎNE (le premier dans la portée de l'émetteur encore
    //   vivant, le second dans la portée du premier seulement), si bien que la
    //   simultanéité devient une contrainte de TRAJET : quatre-vingt-dix secondes pour
    //   faire le tour de la roue. Voir DamagedRelayBlockEntity.
    //
    //        z 0        12        24       36
    //   x=0  ┌────────────────────────────────┐
    //        │        ┌─ SERRES ─┐            │  RELAIS A (dans le champ de
    //     6  │        │  [ A ]   │               l'émetteur encore alimenté)
    //        │        └────╥─────┘            │
    //    14  │  ┌────┐  ╔══╩══╗  ┌─────────┐  │  le HALL est scellé par un SAS
    //        │  │DISS│══╣HALL ╠══╡DORTOIRS │  │  qui n'ouvre que dans le champ
    //        │  │ ☠  │  ║[SAS]║  │         │  │  du RELAIS B
    //    24  │  └────┘  ╚══╦══╝  └─────────┘  │
    //        │        ┌────╨─────┐            │
    //    32  │        │ POMPES   │            │  RELAIS B (portée de A seulement)
    //        │        │  [ B ]   │            │
    //   x=38 └────────────────────────────────┘

    private static final int SIG_SIZE = 39;
    private static final int SIG_H = 22;
    /** Sol foulé. Un seul niveau : la roue doit se lire d'un coup, c'est son intérêt. */
    private static final int SIG_Y = 3;
    private static final int SIG_C = 19;

    private static CompoundTag sigmaLaboratory() {
        TemplateBuilder b = new TemplateBuilder(SIG_SIZE, SIG_H, SIG_SIZE);

        // Les quatre ailes d'abord, le cœur ensuite : le sanctuaire s'inscrit DANS le
        // déambulatoire, et le déambulatoire mord sur les murs des ailes. Écrit dans
        // l'autre sens, chaque salle reboucherait la précédente.
        sigmaGreenhouses(b);
        sigmaPumps(b);
        sigmaDissonance(b);
        sigmaQuarters(b);
        sigmaAmbulatory(b);
        sigmaHall(b);
        return b.build();
    }

    /**
     * <b>Le déambulatoire.</b> Un couloir annulaire qui contourne le sanctuaire et relie
     * les quatre ailes entre elles.
     *
     * <p>C'est lui qui résout le problème que pose toute salle centrale scellée : comment
     * circuler si le centre est fermé ? Le réflexe serait de percer le sanctuaire — c'est
     * exactement ce qu'il ne faut pas faire, sinon il n'est plus scellé. <b>Le relais B est
     * ici</b>, sur la branche ouest, devant la chambre de dissonance : il faut approcher la
     * salle dangereuse pour l'atteindre.
     */
    private static void sigmaAmbulatory(TemplateBuilder b) {
        // Noyau à 6 et non 7 : à 7, l'anneau se réduisait à des cases reliées en
        // DIAGONALE aux angles de l'octogone — un couloir qui a l'air continu et qu'on ne
        // peut pas parcourir. Le déambulatoire doit rester franchissable en pas cardinaux
        // sur tout son tour, sinon il ne relie rien.
        Masonry.ambulatory(b, SIG_C, SIG_Y, SIG_C, 6, 10, 5);
        // Les quatre percées vers les ailes.
        b.box(SIG_C - 1, SIG_Y, 7, SIG_C + 1, SIG_Y + 2, 9, AIR);
        b.box(SIG_C - 1, SIG_Y, 29, SIG_C + 1, SIG_Y + 2, 31, AIR);
        b.box(7, SIG_Y, SIG_C - 1, 9, SIG_Y + 2, SIG_C + 1, AIR);
        b.box(29, SIG_Y, SIG_C - 1, 31, SIG_Y + 2, SIG_C + 1, AIR);

        b.set(9, SIG_Y, SIG_C, ModBlocks.DAMAGED_RELAY.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        Masonry.sconce(b, SIG_C, SIG_Y + 2, 9, Direction.Axis.X);
        Masonry.sconce(b, SIG_C, SIG_Y + 2, 29, Direction.Axis.X);
    }

    /**
     * <b>Le hall scellé.</b> Rotonde à coupole, console T3 au centre, un seul sas — au
     * <b>sud</b>, à l'opposé du relais A. Ce n'est pas de la mise en scène : c'est ce qui
     * force la chaîne. Le relais A, seul, ne porte pas jusqu'ici.
     */
    private static void sigmaHall(TemplateBuilder b) {
        Masonry.rotunda(b, SIG_C, SIG_Y, SIG_C, 5, 8);
        Masonry.terrace(b, SIG_C - 2, SIG_Y, SIG_C - 2, SIG_C + 2, SIG_C + 2, PAVING);
        b.set(SIG_C, SIG_Y + 1, SIG_C, ModBlocks.SIGMA_CONSOLE.get().defaultBlockState());
        Masonry.chandelier(b, SIG_C, SIG_Y + 13, SIG_C, 3);

        // LE SAS : dans le mur sud du sanctuaire, un bloc de large, deux de haut. Les
        // trois autres orientations sont murées et vitrées — on voit la console pendant
        // tout le donjon sans pouvoir l'atteindre, et c'est ce qui donne son enjeu au
        // trajet.
        b.box(SIG_C - 1, SIG_Y, SIG_C + 6, SIG_C + 1, SIG_Y + 3, SIG_C + 6, BRICK);
        b.set(SIG_C, SIG_Y, SIG_C + 6, BULKHEAD);
        b.set(SIG_C, SIG_Y + 1, SIG_C + 6, BULKHEAD);
        b.set(SIG_C - 1, SIG_Y + 1, SIG_C + 6, GLASS);
        b.set(SIG_C + 1, SIG_Y + 1, SIG_C + 6, GLASS);
        b.set(SIG_C, SIG_Y + 2, SIG_C + 6, GRATE);
        for (int[] c : new int[][] {{SIG_C, SIG_C - 6}, {SIG_C - 6, SIG_C}, {SIG_C + 6, SIG_C}}) {
            b.box(c[0], SIG_Y, c[1], c[0], SIG_Y + 3, c[1], BRICK);
            b.set(c[0], SIG_Y + 1, c[1], GLASS);
            b.set(c[0], SIG_Y + 2, c[1], GRATE);
        }
    }

    /**
     * <b>Aile nord — les serres.</b> Verrières crevées, cultures mortes : la seule salle du
     * Sigma qui montre à quoi ils <i>tenaient</i> plutôt qu'à quoi ils travaillaient. C'est
     * aussi l'entrée (la voûte de verre s'est effondrée) et le point de départ du puzzle :
     * l'<b>émetteur encore alimenté</b> et le <b>relais A</b> y sont.
     */
    private static void sigmaGreenhouses(TemplateBuilder b) {
        Masonry.chamber(b, 10, SIG_Y, 2, 28, SIG_Y + 6, 8, Masonry.Style.noble());
        Masonry.colonnade(b, 14, SIG_Y, 3, 7, SIG_Y + 6);
        Masonry.colonnade(b, 24, SIG_Y, 3, 7, SIG_Y + 6);
        b.box(11, SIG_Y + 7, 3, 27, SIG_Y + 7, 7, GLASS);
        Masonry.collapse(b, 14, 5, 3, SIG_Y + 7, SIG_Y, 0x5EEF1);

        fuelledEmitter(b, 22, SIG_Y, 4);
        b.set(19, SIG_Y, 4, ModBlocks.DAMAGED_RELAY.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        for (int x = 12; x <= 26; x += 4) {
            b.set(x, SIG_Y, 7, Blocks.COMPOSTER.defaultBlockState());
        }
        Masonry.sconce(b, 9, SIG_Y + 3, 5, Direction.Axis.Z);
    }

    /** <b>Aile sud — la salle des pompes.</b> Deux Custodes Lourds en poste, et du butin. */
    private static void sigmaPumps(TemplateBuilder b) {
        Masonry.chamber(b, 10, SIG_Y, 30, 28, SIG_Y + 6, 36, Masonry.Style.noble());
        Masonry.colonnade(b, 14, SIG_Y, 31, 35, SIG_Y + 6);
        Masonry.colonnade(b, 24, SIG_Y, 31, 35, SIG_Y + 6);
        b.box(17, SIG_Y, 33, 21, SIG_Y + 1, 35, COPPER);
        b.set(19, SIG_Y + 2, 34, GRATE);
        b.lootChest(12, SIG_Y, 31, ModWorldGen.OUTPOST_LOOT, Direction.SOUTH);
        custode(b, 13.5, SIG_Y, 34.5);
        custode(b, 25.5, SIG_Y, 34.5);
        Masonry.chandelier(b, 19, SIG_Y + 7, 32, 2);
        Masonry.wallBreach(b, 15, SIG_Y, 37, 6, false, -1, 0x5EEF2);
    }

    /**
     * <b>Aile ouest — la chambre de dissonance.</b> Un émetteur qu'on n'a jamais coupé,
     * neuf siècles de dérive. Le meilleur butin facultatif est dedans, et on y entre en
     * <b>gérant une mécanique du mod</b> : poser un Damping Array (il agit à 16 blocs, la
     * décharge en porte 6 — `06-Energy.md` garantit donc qu'on peut toujours nettoyer à
     * distance sûre), couper l'émetteur, ou courir.
     */
    private static void sigmaDissonance(TemplateBuilder b) {
        Masonry.chamber(b, 2, SIG_Y, 12, 8, SIG_Y + 5, 26, Masonry.Style.common());
        fuelledEmitter(b, 5, SIG_Y, 19);
        b.box(4, SIG_Y - 1, 18, 6, SIG_Y - 1, 20, CHISELED);
        b.box(3, SIG_Y, 13, 4, SIG_Y, 14, BLOOM);
        b.box(6, SIG_Y, 24, 7, SIG_Y, 25, BLOOM);
        b.set(3, SIG_Y + 1, 13, BLOOM);
        b.lootChest(7, SIG_Y, 13, ModWorldGen.OUTPOST_LOOT, Direction.WEST);
        b.lootChest(3, SIG_Y, 25, ModWorldGen.OUTPOST_LOOT, Direction.EAST);
        Masonry.sconce(b, 1, SIG_Y + 2, 19, Direction.Axis.Z);
    }

    /** <b>Aile est — dortoirs et infirmerie.</b> Le lore, et rien de technique. */
    private static void sigmaQuarters(TemplateBuilder b) {
        Masonry.chamber(b, 30, SIG_Y, 12, 36, SIG_Y + 5, 26, Masonry.Style.common());
        Masonry.arcade(b, 36, SIG_Y, 13, 25, 1);
        for (int z = 14; z <= 24; z += 4) {
            cot(b, 31, SIG_Y, z);
        }
        b.lootChest(35, SIG_Y, 14, ModWorldGen.MODEST_DWELLING_LOOT, Direction.WEST);
        b.set(35, SIG_Y, 24, Blocks.LECTERN.defaultBlockState());
        custode(b, 33.5, SIG_Y, 19.5);
        Masonry.chandelier(b, 33, SIG_Y + 6, 19, 2);
        Masonry.collapse(b, 33, 24, 3, SIG_Y + 6, SIG_Y, 0x5EEF3);
    }

    /**
     * Pose un émetteur ancien <b>déjà alimenté</b> : on lui écrit sa réserve pleine
     * directement dans le NBT de sa block entity.
     *
     * <p>C'est ce qui distingue le Sigma de l'Avant-poste, et ce n'est pas un détail : à
     * l'Avant-poste tout est mort et le joueur rallume ; ici <b>quelque chose tourne
     * encore</b> — 08-Structures.md l'annonce (« quelques machines encore alimentées seules
     * dans le noir ») — et c'est ce qui rend le puzzle des relais amorçable sans que le
     * joueur ait à apporter quoi que ce soit.
     *
     * <p>La <b>réserve</b> plutôt qu'un cristal dans le slot : un entier, pas un format
     * d'inventaire à reproduire à la main dans du NBT écrit par nous. Et rien ne la
     * consomme tant qu'aucune machine n'y puise — un relais rediffuse, il ne prélève pas —
     * donc l'émetteur tourne encore dans neuf siècles, ce qui est précisément le propos.
     */
    private static void fuelledEmitter(TemplateBuilder b, int x, int y, int z) {
        CompoundTag be = new CompoundTag();
        be.putString("id", "veskorius:field_emitter");
        be.putInt("reserve", 4000);
        b.set(x, y, z, ModBlocks.ANCIENT_EMITTER.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(com.veskorius.block.FieldEmitterBlock.LIT, Boolean.TRUE), be);
    }


    // =========================================================================
    // PUITS DE FORAGE — la structure qui manquait au T1
    // =========================================================================
    //
    // Le T1 n'avait AUCUNE structure à lui : le joueur commençait dans un monde où le
    // veskorien n'existait qu'en gros morceaux hors de sa portée. Le puits de forage est
    // sa première ruine : elle ne débloque rien, elle ENSEIGNE — « descendre, c'est
    // trouver du cristal » — et elle le fait en montrant un chantier qui a fait
    // exactement ça, jusqu'à ce qu'il s'effondre.

    private static final int DRILL_SIZE = 15;
    private static final int DRILL_H = 28;

    private static CompoundTag drillShaft() {
        TemplateBuilder b = new TemplateBuilder(DRILL_SIZE, DRILL_H, DRILL_SIZE);

        // Le fût du puits : sept blocs de côté, chemisé, sur vingt de haut.
        b.box(3, 4, 3, 11, DRILL_H - 1, 11, BRICK);
        b.box(4, 4, 4, 10, DRILL_H - 2, 10, AIR);
        for (int y = 4; y < DRILL_H - 1; y++) {
            for (int[] c : new int[][] {{4, 4}, {10, 4}, {4, 10}, {10, 10}}) {
                b.set(c[0], y, c[1], Masonry.column(Direction.Axis.Y));
            }
        }
        Masonry.conduitDrop(b, 3, 5, 7, DRILL_H - 3);

        // Les plateformes : décalées, incomplètes, à trois blocs les unes des autres. On
        // descend de proche en proche — pas d'escalier, c'est un chantier abandonné, pas
        // un bâtiment. La chute est le danger, et elle est franchissable.
        for (int i = 0; i < 6; i++) {
            int y = DRILL_H - 5 - i * 3;
            boolean east = i % 2 == 0;
            b.box(east ? 5 : 8, y, 5, east ? 7 : 10, y, 10, east ? Masonry.TILE : PAVING);
            b.set(east ? 7 : 8, y + 1, i % 4 < 2 ? 5 : 10, Masonry.COPPER);
        }

        // Au fond : le foreur brisé, et ce qu'il cherchait.
        b.box(4, 3, 4, 10, 3, 10, Masonry.ROCK);
        b.box(5, 4, 5, 9, 4, 9, RUBBLE);
        b.set(7, 4, 7, ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get().defaultBlockState());
        for (int[] c : new int[][] {{5, 7}, {9, 7}, {7, 5}, {7, 9}}) {
            b.set(c[0], 4, c[1], ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get().defaultBlockState());
            b.set(c[0], 3, c[1], ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState());
        }
        b.box(6, 5, 6, 8, 6, 8, Masonry.COPPER);
        b.set(7, 7, 7, CRACKED);
        b.lootChest(9, 5, 9, ModWorldGen.MODEST_DWELLING_LOOT, Direction.WEST);
        Masonry.sconce(b, 3, 6, 7, Direction.Axis.Z);

        // La gueule du puits, effondrée : c'est par là qu'une grotte le croise.
        // Le bouchon d'éboulis se pose AU-DESSUS de la plateforme haute, pas dessus :
        // un cône de rayon 3 déposé au niveau du premier palier l'ensevelit entièrement et
        // le puits devient inaccessible par le haut — donc inaccessible tout court.
        Masonry.collapse(b, 7, 7, 3, DRILL_H - 1, DRILL_H - 2, 0x5EEE2);
        return b.build();
    }

    // =========================================================================
    // PETITES RUINES — la texture de fond du monde
    // =========================================================================
    //
    // Le monde n'avait que deux ruines, toutes deux grandes. Une civilisation effondrée
    // ne laisse pas deux bâtiments : elle laisse surtout des miettes. Leur rôle n'est pas
    // de récompenser, c'est de faire qu'on croise du veskorien tout le temps — et qu'une
    // VRAIE structure se distingue par contraste au lieu d'apparaître de nulle part.

    /** Bout de galerie enseveli : une travée de voûte et son tas. Aucune récompense. */
    private static CompoundTag ruinMarker() {
        TemplateBuilder b = new TemplateBuilder(11, 9, 11);
        Masonry.chamber(b, 2, 2, 2, 8, 5, 8, Masonry.Style.common());
        Masonry.collapse(b, 5, 5, 4, 6, 2, 0x5EEDA);
        Masonry.wallBreach(b, 1, 2, 3, 5, true, 1, 0x5EEDB);
        Masonry.conduitRun(b, 9, 4, 3, 9, 7);
        return b.build();
    }

    /** Borne de conduit : un fût brisé sur sa base dallée. Le plus petit signe veskorien. */
    private static CompoundTag ruinMarkerPillar() {
        TemplateBuilder b = new TemplateBuilder(7, 9, 7);
        b.box(0, 0, 0, 6, 0, 6, RUBBLE);
        b.box(1, 0, 1, 5, 0, 5, Masonry.TILE);
        b.box(2, 1, 2, 4, 1, 4, PAVING);
        b.set(3, 1, 3, CHISELED);
        for (int y = 2; y <= 5; y++) {
            b.set(3, y, 3, Masonry.column(Direction.Axis.Y));
        }
        b.set(3, 4, 3, Masonry.conduit(Direction.Axis.Y));
        b.set(3, 6, 3, CRACKED);
        for (int[] c : new int[][] {{2, 3}, {4, 3}, {3, 2}, {3, 4}}) {
            b.set(c[0], 1, c[1], SLAB);
        }
        b.set(1, 1, 5, RUBBLE);
        b.set(5, 1, 2, RUBBLE);
        return b.build();
    }

    /**
     * Chambre engloutie : une salle voûtée à demi comblée, un coffre. C'est le format « on
     * a trouvé quelque chose » sans être un donjon — le palier manquant entre la borne et
     * l'Avant-poste.
     */
    private static CompoundTag sunkenChamber() {
        TemplateBuilder b = new TemplateBuilder(17, 13, 17);
        Masonry.chamber(b, 3, 3, 3, 13, 8, 13, Masonry.Style.noble());
        Masonry.colonnade(b, 6, 3, 5, 11, 8);
        Masonry.colonnade(b, 10, 3, 5, 11, 8);
        Masonry.collapse(b, 6, 6, 5, 9, 3, 0x5EEDC);
        Masonry.wallBreach(b, 14, 3, 8, 5, true, -1, 0x5EEDD);
        Masonry.silt(b, 11, 2, 11, 2);
        b.lootChest(12, 3, 5, ModWorldGen.MODEST_DWELLING_LOOT, Direction.WEST);
        b.box(12, 3, 12, 13, 3, 13, BLOOM);
        Masonry.conduitRun(b, 2, 6, 4, 2, 12);
        Masonry.chandelier(b, 8, 9, 10, 2);
        return b.build();
    }

    // =========================================================================
    // Bouchons de fin de branche
    // =========================================================================

    /**
     * Bouchon : un mur d'un bloc d'épaisseur qui referme une branche non poursuivie. Sans
     * lui, une structure sur deux se termine par un trou béant sur la roche, là où le
     * jigsaw a atteint sa profondeur maximale — d'où sa déclaration en {@code fallback} de
     * tous les pools. Son connecteur a pour {@code final_state} de la <b>brique</b> : un
     * bouchon qui se remplace par de l'air ne bouche rien.
     */
    private static CompoundTag cap(int height, int depth) {
        TemplateBuilder b = new TemplateBuilder(1, height, depth);
        b.box(0, 0, 0, 0, height - 1, depth - 1, BRICK);
        b.jigsaw(0, 2, depth / 2, Direction.WEST, "veskorius:corridor", "veskorius:corridor",
            net.minecraft.data.worldgen.Pools.EMPTY, BRICK, true);
        return b.build();
    }

    // --- Mobilier (rare, par principe) -------------------------------------------

    /**
     * Couchette veskorienne : une dalle et un dossier gravé. (Les lits vanilla occupent
     * deux blocs, et une moitié posée seule est <b>retirée</b> par la mise à jour de
     * voisinage — ils disparaissaient sans un mot.)
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
        b.set(x + dx, y, z + dz, Masonry.column(Direction.Axis.Y));
        b.set(x - dx, y, z - dz, Masonry.column(Direction.Axis.Y));
        b.set(x + dx, y + 1, z + dz, Masonry.column(Direction.Axis.Y));
        b.set(x - dx, y + 1, z - dz, Masonry.column(Direction.Axis.Y));
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
        b.set(x, y + 2, z, AIR);
    }

    private static void houseConnector(TemplateBuilder b, int x, int y, int z, Direction front) {
        b.jigsaw(x, y, z, front, "veskorius:corridor", "veskorius:corridor",
            ModStructures.HAMLET_HOUSE_POOL, AIR, true);
        b.set(x, y + 1, z, AIR);
        b.set(x, y + 2, z, AIR);
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
