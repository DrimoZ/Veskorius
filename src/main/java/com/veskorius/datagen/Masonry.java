package com.veskorius.datagen;

import com.veskorius.block.ModBlocks;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * <b>Vocabulaire de maçonnerie veskorienne</b> — les gestes de construction dont les
 * pièces de structure sont écrites (17-Dungeons.md §2).
 *
 * <p><b>Trois passes ont été nécessaires, et chacune a corrigé une classe d'erreur
 * différente.</b> Elles sont notées ici parce que la tentation de les refaire est réelle.
 *
 * <ol>
 *   <li><b>Des boîtes creuses.</b> On posait une coquille rectangulaire et on la
 *       cloisonnait : un plan d'appartement dans un pavé, avec de l'air là où il devrait y
 *       avoir de la roche. Corrigé par {@link #chamber} — on <b>évide une masse</b>, salle
 *       par salle, et on relie par des {@link #gallery galeries}.</li>
 *   <li><b>Tout plat, tout à angle droit.</b> Corrigé par {@link #barrelVault},
 *       {@link #dome}, les chanfreins, et par une ruine <b>causale</b>
 *       ({@link #collapse}) : le trou dans la voûte, et la matière manquante en cône
 *       exactement dessous.</li>
 *   <li><b>Petit, encombré, pas monumental.</b> C'est la passe présente. Les salles
 *       étaient microscopiques et saturées de mobilier ; une civilisation ne se lit pas à
 *       son mobilier mais à ses <b>proportions</b> et à ses <b>ordres</b>. D'où
 *       {@link #colonnade}, {@link #arcade}, {@link #frieze}, {@link #terrace},
 *       {@link #grandStair}, et une règle : <b>de la hauteur et du vide plutôt que des
 *       objets</b>.</li>
 * </ol>
 *
 * <p><b>Deux interdits, appris à la dure.</b>
 * <ul>
 *   <li><b>Aucun bloc à gravité</b> (le gravier a été retiré) : posé dans une voûte
 *       crevée, il s'effondre au premier chargement de chunk et la ruine se dégrade toute
 *       seule, jamais deux fois pareil.</li>
 *   <li><b>Aucune source d'eau</b> : une flaque décorative devient une inondation dès
 *       qu'un bloc voisin manque, et le donjon se remplit. Le lieu bas se raconte
 *       autrement — par le dépôt, pas par le liquide.</li>
 * </ul>
 *
 * <p>Tout est déterministe (graines fixes) : une pièce doit rester reproductible d'un
 * datagen à l'autre. La variété entre deux ruines du même plan vient des <i>processors</i>
 * et des pools, pas d'un tirage ici.
 */
public final class Masonry {

    // --- Palette du mod ---------------------------------------------------------

    public static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    public static final BlockState BRICK = ModBlocks.VEINED_STONE_BRICKS.get().defaultBlockState();
    public static final BlockState CRACKED = ModBlocks.CRACKED_VEINED_STONE_BRICKS.get().defaultBlockState();
    public static final BlockState CHISELED = ModBlocks.CHISELED_VEINED_STONE.get().defaultBlockState();
    public static final BlockState ROCK = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();
    public static final BlockState LAMP = ModBlocks.RESONANCE_LAMP.get().defaultBlockState();
    public static final BlockState BLOOM = ModBlocks.DISSONANCE_BLOOM.get().defaultBlockState();
    public static final BlockState BULKHEAD = ModBlocks.RESONANCE_BULKHEAD.get().defaultBlockState();
    public static final BlockState GLASS = Blocks.PURPLE_STAINED_GLASS.defaultBlockState();

    public static final BlockState SLAB = ModBlocks.VEINED_STONE_BRICK_SLAB.get().defaultBlockState()
        .setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
    public static final BlockState SLAB_TOP = ModBlocks.VEINED_STONE_BRICK_SLAB.get().defaultBlockState()
        .setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);

    /** Gravats : de la maçonnerie tombée. Jamais de bloc à gravité (voir l'en-tête). */
    public static final BlockState RUBBLE = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    public static final BlockState DEBRIS = Blocks.TUFF.defaultBlockState();

    // --- Accents vanilla --------------------------------------------------------
    // La maçonnerie du mod ne fait qu'une chose : du mur. Un monument demande des matières
    // SECONDAIRES — un sol qui n'est pas le mur, un métal, un ajour. Les prendre chez
    // vanilla plutôt que de multiplier les blocs du mod garde le registre lisible, et le
    // joueur reconnaît des matériaux qu'il sait déjà fabriquer.

    /** Sol des salles nobles : sombre, poli, sans grain — il fait ressortir les murs. */
    public static final BlockState PAVING = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    /** Sol des salles ordinaires, et bandes de dessin dans les grands sols. */
    public static final BlockState TILE = Blocks.DEEPSLATE_TILES.defaultBlockState();
    public static final BlockState PAVING_SLAB = Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState()
        .setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
    /** Le métal des Veskoriens : cuivre patiné. Bagues, ferrures, garde-corps. */
    public static final BlockState COPPER = Blocks.WEATHERED_CUT_COPPER.defaultBlockState();
    public static final BlockState COPPER_SLAB = Blocks.WEATHERED_CUT_COPPER_SLAB.defaultBlockState()
        .setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
    /** Ajour : le seul moyen de faire respirer un mur sans y percer un trou. */
    public static final BlockState GRATE = Blocks.WEATHERED_COPPER_GRATE.defaultBlockState();
    public static final BlockState CHAIN = Blocks.CHAIN.defaultBlockState();
    public static final BlockState COLUMN = ModBlocks.VEINED_STONE_COLUMN.get().defaultBlockState();

    private Masonry() {
    }

    public static BlockState stair(Direction facing, boolean top) {
        return ModBlocks.VEINED_STONE_BRICK_STAIRS.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
            .setValue(BlockStateProperties.HALF, top ? Half.TOP : Half.BOTTOM);
    }

    public static BlockState pavingStair(Direction facing, boolean top) {
        return Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
            .setValue(BlockStateProperties.HALF, top ? Half.TOP : Half.BOTTOM);
    }

    public static BlockState conduit(Direction.Axis axis) {
        return ModBlocks.CONDUIT_LINE.get().defaultBlockState()
            .setValue(com.veskorius.block.ConduitLineBlock.AXIS, axis);
    }

    public static BlockState column(Direction.Axis axis) {
        return COLUMN.setValue(BlockStateProperties.AXIS, axis);
    }

    // =========================================================================
    // Salles
    // =========================================================================

    /**
     * Style d'une salle. Un style n'est pas de la décoration : c'est ce qui distingue une
     * <b>salle noble</b> (voûtée haut, à ordre, dallée) d'un <b>couloir de service</b>
     * (bas, nu). Sans cet écart, toutes les pièces se ressemblent quelle que soit leur
     * fonction, et une grande salle n'a plus d'échelle — c'est le contraste qui fait
     * l'effet, jamais la taille absolue.
     */
    public record Style(int vaultSteps, boolean pilasters, boolean frieze,
                        int chamfer, BlockState floor) {

        /** Salle d'apparat : voûte haute, ordre complet, sol poli. */
        public static Style noble() {
            return new Style(3, true, true, 2, PAVING);
        }

        /** Salle ordinaire : voûte basse, pilastres, sol carrelé. */
        public static Style common() {
            return new Style(2, true, false, 1, TILE);
        }

        /** Service : rien. Il en faut, sinon le noble ne se voit plus. */
        public static Style plain() {
            return new Style(0, false, false, 0, BRICK);
        }
    }

    /**
     * <b>Creuse et chemise une salle.</b> Les coordonnées données sont l'<b>intérieur</b> ;
     * sol, plafond et murs sont ajoutés autour. Rien n'est écrit au-delà : la roche du
     * monde reste la roche du monde.
     */
    public static void chamber(TemplateBuilder b, int x0, int y0, int z0,
                               int x1, int y1, int z1, Style style) {
        b.box(x0 - 1, y0 - 1, z0 - 1, x1 + 1, y1 + 1, z1 + 1, BRICK);
        b.box(x0, y0, z0, x1, y1, z1, AIR);
        floor(b, x0, y0 - 1, z0, x1, z1, style.floor());

        for (int i = 1; i <= style.chamfer(); i++) {
            chamferCorners(b, x0, y0, z0, x1, y1, z1, i);
        }
        if (style.pilasters()) {
            pilasters(b, x0, y0, z0, x1, y1, z1);
        }
        if (style.frieze()) {
            // Dans le mur, comme les pilastres : un bandeau posé sur la rangée intérieure
            // serait une étagère, et rétrécirait la salle d'un bloc sur tout son pourtour.
            frieze(b, x0 - 1, y0 + (y1 - y0) / 2, z0 - 1, x1 + 1, z1 + 1);
        }
        if (style.vaultSteps() > 0) {
            barrelVault(b, x0, z0, x1, z1, y1 + 1, style.vaultSteps(), (x1 - x0) <= (z1 - z0));
        }
    }

    /**
     * Sol dessiné : un champ poli, ourlé d'une bande de carrelage. Un damier serré donne
     * du bruit ; une bordure donne un dessin — et un dessin dit « quelqu'un a tracé ça ».
     */
    public static void floor(TemplateBuilder b, int x0, int y, int z0, int x1, int z1,
                             BlockState field) {
        b.box(x0, y, z0, x1, y, z1, field);
        for (int x = x0; x <= x1; x++) {
            b.set(x, y, z0, TILE);
            b.set(x, y, z1, TILE);
        }
        for (int z = z0; z <= z1; z++) {
            b.set(x0, y, z, TILE);
            b.set(x1, y, z, TILE);
        }
    }

    /**
     * Coupe les quatre angles verticaux. Un angle droit de plus de deux blocs de haut est
     * ce qui donne à une salle son air de carton d'emballage ; le couper d'une diagonale
     * de un ou deux blocs suffit à la faire lire comme un volume taillé.
     */
    private static void chamferCorners(TemplateBuilder b, int x0, int y0, int z0,
                                       int x1, int y1, int z1, int i) {
        for (int y = y0; y <= y1; y++) {
            for (int k = 0; k < i; k++) {
                int j = i - 1 - k;
                b.set(x0 + k, y, z0 + j, BRICK);
                b.set(x1 - k, y, z0 + j, BRICK);
                b.set(x0 + k, y, z1 - j, BRICK);
                b.set(x1 - k, y, z1 - j, BRICK);
            }
        }
    }

    /**
     * Pilastres <b>engagés dans le mur</b>, tous les 4 blocs, base et chapiteau compris.
     *
     * <p><b>Engagés</b> veut dire : ils <i>remplacent</i> la maçonnerie du mur, ils ne se
     * dressent pas sur la rangée intérieure. La première version faisait l'inverse, avec
     * deux conséquences — ils mangeaient de l'espace jouable tout autour de chaque salle,
     * et surtout l'un d'eux tombait pile devant une sortie de galerie et <b>murait le
     * donjon</b>. Ils restent parfaitement lisibles à plat : c'est la texture de colonne,
     * pas le relief, qui les fait lire.
     */
    private static void pilasters(TemplateBuilder b, int x0, int y0, int z0,
                                  int x1, int y1, int z1) {
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            pilaster(b, x0 - 1, y0, z, y1);
            pilaster(b, x1 + 1, y0, z, y1);
        }
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            pilaster(b, x, y0, z0 - 1, y1);
            pilaster(b, x, y0, z1 + 1, y1);
        }
    }

    private static void pilaster(TemplateBuilder b, int x, int y0, int z, int y1) {
        for (int y = y0; y <= y1; y++) {
            b.set(x, y, z, column(Direction.Axis.Y));
        }
        b.set(x, y0, z, CHISELED);
        b.set(x, y1, z, CHISELED);
    }

    /** Frise : un bandeau de cuivre à mi-hauteur. L'œil a enfin une horizontale. */
    public static void frieze(TemplateBuilder b, int x0, int y, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            b.set(x, y, z0, x % 3 == 0 ? GRATE : COPPER);
            b.set(x, y, z1, x % 3 == 0 ? GRATE : COPPER);
        }
        for (int z = z0; z <= z1; z++) {
            b.set(x0, y, z, z % 3 == 0 ? GRATE : COPPER);
            b.set(x1, y, z, z % 3 == 0 ? GRATE : COPPER);
        }
    }

    /**
     * <b>Voûte en berceau.</b> Le plafond monte par ressauts d'escaliers retournés jusqu'à
     * une clé. C'est <i>le</i> geste qui transforme une boîte en salle — il vaut à lui seul
     * tous les ornements de mur réunis.
     */
    public static void barrelVault(TemplateBuilder b, int x0, int z0, int x1, int z1,
                                   int yStart, int steps, boolean alongZ) {
        for (int i = 0; i < steps; i++) {
            int y = yStart + i;
            b.box(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, BRICK);
            int lo = (alongZ ? x0 : z0) + i;
            int hi = (alongZ ? x1 : z1) - i;
            if (hi - lo < 2) {
                break;
            }
            if (alongZ) {
                b.box(lo + 1, y, z0, hi - 1, y, z1, AIR);
                for (int z = z0; z <= z1; z++) {
                    b.set(lo, y, z, stair(Direction.EAST, true));
                    b.set(hi, y, z, stair(Direction.WEST, true));
                }
            } else {
                b.box(x0, y, lo + 1, x1, y, hi - 1, AIR);
                for (int x = x0; x <= x1; x++) {
                    b.set(x, y, lo, stair(Direction.SOUTH, true));
                    b.set(x, y, hi, stair(Direction.NORTH, true));
                }
            }
        }
        b.box(x0 - 1, yStart + steps, z0 - 1, x1 + 1, yStart + steps, z1 + 1, BRICK);
    }

    // =========================================================================
    // Ordres — ce qui fait un monument plutôt qu'une grande pièce
    // =========================================================================

    /**
     * <b>Colonnade.</b> Deux rangs de colonnes libres qui découpent une salle en nef et
     * bas-côtés. C'est la <b>répétition verticale</b> qui donne l'impression de hauteur,
     * bien plus que la hauteur réelle — et c'est ce qui manquait le plus : nos grandes
     * salles étaient grandes et vides, donc lues comme des hangars.
     *
     * <p>Chaque colonne porte une base, un fût cannelé, un chapiteau, et une <b>imposte</b>
     * qui la relie à sa voisine : sans l'imposte, on voit des poteaux ; avec, on voit une
     * structure qui porte.
     */
    public static void colonnade(TemplateBuilder b, int x, int y0, int z0, int z1, int y1) {
        for (int z = z0; z <= z1; z += 3) {
            b.set(x, y0, z, CHISELED);
            for (int y = y0 + 1; y < y1; y++) {
                b.set(x, y, z, column(Direction.Axis.Y));
            }
            b.set(x, y1, z, CHISELED);
            // Imposte : les deux écoinçons qui relient une colonne à la suivante.
            if (z + 3 <= z1) {
                b.set(x, y1, z + 1, stair(Direction.SOUTH, true));
                b.set(x, y1, z + 2, stair(Direction.NORTH, true));
            }
        }
    }

    /**
     * <b>Arcade aveugle</b> creusée dans l'épaisseur d'un mur : une suite de niches en plein
     * cintre. Un mur nu de vingt mètres est une surface ; un mur à arcade est une façade.
     */
    public static void arcade(TemplateBuilder b, int x, int y0, int z0, int z1, int depth) {
        for (int z = z0; z + 2 <= z1; z += 4) {
            for (int dz = 0; dz <= 2; dz++) {
                for (int d = 0; d < depth; d++) {
                    b.set(x + d, y0, z + dz, AIR);
                    b.set(x + d, y0 + 1, z + dz, AIR);
                }
            }
            // Cintre : les deux naissances en escalier, la clé au milieu.
            for (int d = 0; d < depth; d++) {
                b.set(x + d, y0 + 2, z, stair(Direction.SOUTH, true));
                b.set(x + d, y0 + 2, z + 2, stair(Direction.NORTH, true));
                b.set(x + d, y0 + 2, z + 1, AIR);
                b.set(x + d, y0 + 3, z + 1, CHISELED);
            }
        }
    }

    /**
     * <b>Terrasse.</b> Un gradin d'un bloc, bordé de son nez de marche. Une salle dont le
     * sol est partout à la même hauteur est une salle sans lecture ; un seul gradin suffit
     * à dire « ici c'est autre chose ».
     */
    public static void terrace(TemplateBuilder b, int x0, int y, int z0, int x1, int z1,
                               BlockState top) {
        b.box(x0, y, z0, x1, y, z1, top);
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            b.set(x, y, z0 - 1, pavingStair(Direction.NORTH, false));
            b.set(x, y, z1 + 1, pavingStair(Direction.SOUTH, false));
        }
        for (int z = z0 - 1; z <= z1 + 1; z++) {
            b.set(x0 - 1, y, z, pavingStair(Direction.WEST, false));
            b.set(x1 + 1, y, z, pavingStair(Direction.EAST, false));
        }
    }

    /**
     * <b>Escalier d'apparat</b> : large, droit, à volées franches et rampes pleines. Sert
     * là où la vis serait mesquine — une entrée, un accès à une estrade.
     */
    public static void grandStair(TemplateBuilder b, int x0, int x1, int zTop, int yTop,
                                  int steps) {
        for (int i = 0; i < steps; i++) {
            int y = yTop - i;
            int z = zTop + i;
            b.box(x0, 0, z, x1, y, z, BRICK);
            b.box(x0, y + 1, z, x1, y + 4, z, AIR);
            b.set(x0 - 1, y + 1, z, SLAB_TOP);
            b.set(x1 + 1, y + 1, z, SLAB_TOP);
            b.set(x0 - 1, y, z, BRICK);
            b.set(x1 + 1, y, z, BRICK);
        }
    }

    // =========================================================================
    // Rotonde et coupole
    // =========================================================================

    /**
     * <b>Salle octogonale surmontée d'une coupole.</b> Réservée à ce qui compte : un plan
     * non rectangulaire est immédiatement lu comme important, sans qu'on ait besoin de
     * l'éclairer davantage ni d'y écrire quoi que ce soit.
     */
    public static void rotunda(TemplateBuilder b, int cx, int y0, int cz, int radius, int height) {
        int cut = radius / 2;
        b.box(cx - radius - 1, y0 - 1, cz - radius - 1,
            cx + radius + 1, y0 + height + radius, cz + radius + 1, BRICK);
        for (int y = y0; y < y0 + height; y++) {
            octagonLayer(b, cx, y, cz, radius, cut, AIR);
        }
        octagonLayer(b, cx, y0 - 1, cz, radius, cut, PAVING);
        octagonEdge(b, cx, y0 - 1, cz, radius, cut, TILE);

        // Colonnes engagées aux huit angles : c'est ce qui fait lire l'octogone.
        for (int y = y0; y < y0 + height; y++) {
            octagonEdge(b, cx, y, cz, radius, cut, column(Direction.Axis.Y));
        }
        octagonEdge(b, cx, y0 + height - 1, cz, radius, cut, CHISELED);

        for (int i = 0; i <= radius; i++) {
            int r = radius - i;
            int y = y0 + height + i;
            if (r < 1) {
                b.set(cx, y, cz, CHISELED);
                break;
            }
            octagonLayer(b, cx, y, cz, r, Math.max(0, r / 2), AIR);
            octagonStairs(b, cx, y, cz, r, Math.max(0, r / 2));
        }
    }

    private static void octagonLayer(TemplateBuilder b, int cx, int y, int cz,
                                     int r, int cut, BlockState state) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= r + cut) {
                    b.set(cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    /** Le contour exact d'un octogone, peint d'un état donné. */
    private static void octagonEdge(TemplateBuilder b, int cx, int y, int cz, int r, int cut,
                                    BlockState state) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.abs(dx) + Math.abs(dz) == r + cut) {
                    b.set(cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    /** Bord d'un anneau de coupole, en escaliers retournés tournés vers le centre. */
    private static void octagonStairs(TemplateBuilder b, int cx, int y, int cz, int r, int cut) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.abs(dx) + Math.abs(dz) != r + cut) {
                    continue;
                }
                Direction facing = Math.abs(dx) >= Math.abs(dz)
                    ? (dx > 0 ? Direction.WEST : Direction.EAST)
                    : (dz > 0 ? Direction.NORTH : Direction.SOUTH);
                b.set(cx + dx, y, cz + dz, stair(facing, true));
            }
        }
    }

    // =========================================================================
    // Circulation
    // =========================================================================

    /**
     * <b>Escalier en vis autour d'un noyau.</b> Le geste le plus « bâti par des gens » qui
     * existe, et le seul moyen honnête de relier deux niveaux sans une rampe droite de
     * quinze mètres — qui, elle, se lit comme un tapis roulant.
     *
     * <p>Le parcours <b>commence du côté {@code entry}</b>, et ce n'est pas un détail de
     * confort : la première marche doit tomber pile devant la galerie qui amène ici. Quand
     * elle tombait ailleurs, on sortait de la galerie <b>dans le vide de la cage</b> et on
     * chutait de dix blocs — une structure parfaitement valide, et infranchissable.
     */
    public static int spiralStair(TemplateBuilder b, int cx, int cz, int yTop, int yBottom,
                                  Direction entry) {
        final int outer = 3;
        b.box(cx - outer - 1, yBottom - 1, cz - outer - 1, cx + outer + 1, yTop + 3, cz + outer + 1, BRICK);
        b.box(cx - outer, yBottom, cz - outer, cx + outer, yTop + 2, cz + outer, AIR);
        b.box(cx - 1, yBottom - 1, cz - 1, cx + 1, yTop + 1, cz + 1, BRICK);
        // Le noyau est une colonne, pas un pilier de béton : quatre fûts aux angles.
        for (int y = yBottom; y <= yTop + 1; y++) {
            for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
                b.set(cx + c[0], y, cz + c[1], column(Direction.Axis.Y));
            }
        }
        b.box(cx - outer, yBottom, cz - outer, cx + outer, yBottom, cz + outer, PAVING);

        int[][] ring = ringCells(outer);
        int offset = entryOffset(entry, outer);

        int y = yTop;
        for (int i = 0; i < ring.length && y >= yBottom; i++) {
            int[] c = ring[(offset + i) % ring.length];
            int x = cx + c[0];
            int z = cz + c[1];
            b.set(x, y, z, i % 4 == 0 ? CHISELED : BRICK);
            if (y - 1 >= yBottom) {
                b.set(x, y - 1, z, BRICK);
            }
            y--;
        }
        return y + 1;
    }

    /** Rayon extérieur d'une vis. Constant : c'est le gabarit d'une cage d'escalier. */
    public static final int SPIRAL_RADIUS = 3;

    private static int entryOffset(Direction entry, int outer) {
        int side = outer * 2;
        return switch (entry) {
            case NORTH -> 0;
            case EAST -> side;
            case SOUTH -> side * 2;
            default -> side * 3;
        } + side / 2;
    }

    /**
     * <b>Où la vis passe-t-elle à la hauteur {@code y}, et de quel côté regarde-t-elle
     * dehors ?</b> Retourne {@code {x, z, dx, dz}} : la case de la marche, et le pas
     * unitaire vers l'extérieur de la cage.
     *
     * <p>Indispensable dès qu'une tour dessert plusieurs niveaux. Une ouverture percée « à
     * peu près au bon endroit » dans le mur d'une cage débouche à côté de la marche —
     * c'est-à-dire <b>dans le vide du puits</b>. C'est exactement le défaut qui avait rendu
     * l'Avant-poste infranchissable ; ici on ne devine plus, on calcule.
     */
    public static int[] spiralExit(int cx, int cz, int yTop, Direction entry, int y) {
        int[][] ring = ringCells(SPIRAL_RADIUS);
        int index = (entryOffset(entry, SPIRAL_RADIUS) + (yTop - y)) % ring.length;
        int[] c = ring[(index + ring.length) % ring.length];
        int dx = Math.abs(c[0]) == SPIRAL_RADIUS ? Integer.signum(c[0]) : 0;
        int dz = dx != 0 ? 0 : Integer.signum(c[1]);
        return new int[] {cx + c[0], cz + c[1], dx, dz};
    }

    /** Les cases du carré de rayon {@code r}, dans l'ordre horaire. */
    private static int[][] ringCells(int r) {
        int side = r * 2;
        int[][] cells = new int[side * 4][];
        int n = 0;
        for (int i = 0; i < side; i++) {
            cells[n++] = new int[] {-r + i, -r};
        }
        for (int i = 0; i < side; i++) {
            cells[n++] = new int[] {r, -r + i};
        }
        for (int i = 0; i < side; i++) {
            cells[n++] = new int[] {r - i, r};
        }
        for (int i = 0; i < side; i++) {
            cells[n++] = new int[] {-r, r - i};
        }
        return cells;
    }

    /**
     * Galerie : un tunnel chemisé, voûté d'un ressaut, trois blocs de haut. Deux salles ne
     * se touchent jamais directement — c'est ce qui empêche le plan de redevenir une
     * grille de cases.
     *
     * <p>La chemise ne couvre <b>que les flancs</b>, jamais les bouts : une galerie doit
     * <b>percer</b> les murs qu'elle relie. Chemiser ses extrémités reboucherait
     * précisément les deux ouvertures qui justifient son existence — un bug qui ne casse
     * rien, ne lève rien, et laisse deux salles murées l'une à l'autre. On lui donne donc
     * une portée qui COMMENCE dans un mur et FINIT dans l'autre.
     */
    public static void gallery(TemplateBuilder b, int x0, int y, int z0, int x1, int z1) {
        boolean alongZ = z1 != z0;
        if (alongZ) {
            b.box(x0 - 1, y - 1, z0, x0 + 1, y + 4, z1, BRICK);
            b.box(x0, y, z0, x0, y + 2, z1, AIR);
            b.box(x0, y - 1, z0, x0, y - 1, z1, PAVING);
            for (int z = z0; z <= z1; z++) {
                b.set(x0 - 1, y + 2, z, stair(Direction.EAST, true));
                b.set(x0 + 1, y + 2, z, stair(Direction.WEST, true));
                if ((z - z0) % 3 == 0) {
                    b.set(x0 - 1, y, z, column(Direction.Axis.Y));
                    b.set(x0 + 1, y, z, column(Direction.Axis.Y));
                }
            }
        } else {
            b.box(x0, y - 1, z0 - 1, x1, y + 4, z0 + 1, BRICK);
            b.box(x0, y, z0, x1, y + 2, z0, AIR);
            b.box(x0, y - 1, z0, x1, y - 1, z0, PAVING);
            for (int x = x0; x <= x1; x++) {
                b.set(x, y + 2, z0 - 1, stair(Direction.SOUTH, true));
                b.set(x, y + 2, z0 + 1, stair(Direction.NORTH, true));
                if ((x - x0) % 3 == 0) {
                    b.set(x, y, z0 - 1, column(Direction.Axis.Y));
                    b.set(x, y, z0 + 1, column(Direction.Axis.Y));
                }
            }
        }
    }

    // =========================================================================
    // Éclairage — jamais suspendu dans le vide
    // =========================================================================

    /**
     * <b>Applique murale.</b> Une lampe posée <i>dans</i> le mur, encadrée de cuivre.
     *
     * <p>C'est la correction d'un défaut visible de loin : les lampes et les conduits
     * étaient posés sur la case <b>intérieure</b> adjacente au mur, donc <b>dans le vide</b>
     * — des blocs qui flottent le long des parois, en rang. Toute décoration murale doit
     * <b>remplacer</b> le bloc de mur, jamais s'y accoler.
     */
    public static void sconce(TemplateBuilder b, int x, int y, int z, Direction.Axis wallAxis) {
        int dx = wallAxis == Direction.Axis.X ? 0 : 1;
        int dz = wallAxis == Direction.Axis.X ? 1 : 0;
        b.set(x, y, z, LAMP);
        b.set(x + dx, y, z + dz, COPPER);
        b.set(x - dx, y, z - dz, COPPER);
        b.set(x, y + 1, z, COPPER_SLAB);
    }

    /**
     * <b>Lustre</b> : une chaîne accrochée à la clé de voûte, une lampe au bout. C'est
     * l'unique façon d'éclairer le centre d'une grande salle sans poser un bloc en
     * lévitation — et une chaîne qui descend d'une voûte est, en soi, un signe
     * d'habitation.
     */
    public static void chandelier(TemplateBuilder b, int x, int yCeiling, int z, int drop) {
        b.set(x, yCeiling, z, CHISELED);
        for (int i = 1; i <= drop; i++) {
            b.set(x, yCeiling - i, z, CHAIN);
        }
        b.set(x, yCeiling - drop - 1, z, LAMP);
    }

    /**
     * Ligne de conduit courant <b>dans</b> un mur (elle remplace la maçonnerie, elle ne
     * s'y accole pas — voir {@link #sconce}). L'axe suit le tracé, sinon le tuyau a l'air
     * haché en travers tous les mètres.
     */
    public static void conduitRun(TemplateBuilder b, int x0, int y, int z0, int x1, int z1) {
        boolean alongZ = z1 != z0;
        BlockState state = conduit(alongZ ? Direction.Axis.Z : Direction.Axis.X);
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                b.set(x, y, z, state);
            }
        }
    }

    /** Descente de conduit le long d'un mur, sur l'axe vertical. */
    public static void conduitDrop(TemplateBuilder b, int x, int y0, int z, int y1) {
        BlockState state = conduit(Direction.Axis.Y);
        for (int y = y0; y <= y1; y++) {
            b.set(x, y, z, state);
        }
    }

    // =========================================================================
    // Ruine — causale, pas aléatoire
    // =========================================================================

    /**
     * <b>Effondrement d'une voûte.</b> On ouvre le plafond sur un disque, et <b>la matière
     * qui manque se retrouve au sol</b>, en cône, exactement dessous : c'est cette
     * correspondance, et elle seule, qui se lit comme « ça s'est écroulé ». Un remplacement
     * de blocs au hasard, si dense soit-il, se lit comme « c'est sale ».
     *
     * <p>Le trou est bouché par de la roche, pas par du vide : au-dessus d'une ruine il y a
     * de la montagne, pas le ciel. Et par de la roche <b>sans gravité</b> : un éboulis de
     * gravier s'effondre au premier chargement de chunk et emporte le dessin avec lui.
     */
    public static void collapse(TemplateBuilder b, int cx, int cz, int radius,
                                int yCeiling, int yFloor, int seed) {
        Random rand = new Random(seed);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius + 0.3) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                for (int y = yCeiling; y <= yCeiling + 3; y++) {
                    b.set(x, y, z, d < radius - 1 ? DEBRIS : ROCK);
                }
                int h = (int) Math.round((radius - d) * 0.9);
                for (int i = 0; i <= h; i++) {
                    b.set(x, yFloor + i, z, i == h && rand.nextBoolean() ? DEBRIS
                        : rand.nextInt(4) == 0 ? CRACKED : RUBBLE);
                }
            }
        }
    }

    /**
     * <b>Pan de mur écroulé.</b> Le mur s'interrompt sur quelques blocs et sa maçonnerie
     * gît en tas à son pied, en s'éloignant : une pierre qui tombe roule vers l'intérieur
     * de la pièce, elle ne se range pas contre la plinthe.
     */
    public static void wallBreach(TemplateBuilder b, int x, int y, int z, int length,
                                  boolean alongZ, int inward, int seed) {
        Random rand = new Random(seed);
        for (int i = 0; i < length; i++) {
            int wx = alongZ ? x : x + i;
            int wz = alongZ ? z + i : z;
            int h = 1 + rand.nextInt(3);
            for (int dy = 0; dy < h; dy++) {
                b.set(wx, y + dy, wz, ROCK);
            }
            for (int j = 1; j <= 2; j++) {
                int px = alongZ ? x + inward * j : wx;
                int pz = alongZ ? wz : z + inward * j;
                if (rand.nextInt(3) > 0) {
                    b.set(px, y, pz, j == 1 ? RUBBLE : DEBRIS);
                }
            }
        }
    }

    /**
     * <b>Dépôt</b> au point bas : neuf siècles d'infiltration ont laissé une croûte, pas
     * une mare. (Les sources d'eau ont été retirées : décoratives sur le papier, elles
     * deviennent une inondation dès qu'un bloc voisin manque, et le donjon se remplit.)
     */
    public static void silt(TemplateBuilder b, int cx, int y, int cz, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > radius) {
                    continue;
                }
                b.set(cx + dx, y, cz + dz, Math.abs(dx) + Math.abs(dz) == radius
                    ? Blocks.DEEPSLATE.defaultBlockState() : DEBRIS);
            }
        }
    }
}
