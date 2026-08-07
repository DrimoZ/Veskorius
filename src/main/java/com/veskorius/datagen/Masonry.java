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
 * <p><b>Pourquoi cette classe existe, et ce qu'elle corrige.</b> La première version des
 * donjons empilait des <i>boîtes creuses</i> : une coquille rectangulaire, un sol plat, un
 * plafond plat, des cloisons droites. Ça se voyait immédiatement — « des salles cubiques
 * posées côte à côte par un ordinateur ». Trois causes, toutes traitées ici :
 *
 * <ol>
 *   <li><b>On construisait au lieu de creuser.</b> Une architecture souterraine ne pose pas
 *       une boîte dans le vide : elle <b>évide une masse</b>. Les pièces n'écrivent donc
 *       plus de coquille englobante — elles creusent des salles et les <b>chemisent</b>
 *       ({@link #chamber}). Entre deux salles, il reste de la <b>roche du monde</b>, pas de
 *       l'air. La silhouette extérieure cesse d'être un pavé, et l'intérieur cesse d'être
 *       un plan d'appartement.</li>
 *   <li><b>Les plafonds étaient plats.</b> Rien ne dit « bâti par des mains » comme une
 *       <b>voûte</b> ({@link #barrelVault}, {@link #dome}). C'est le seul geste qui change
 *       tout à lui seul.</li>
 *   <li><b>La ruine était du bruit.</b> Six blocs remplacés au hasard ne racontent rien.
 *       Ici l'effondrement est <b>causal</b> ({@link #collapse}) : la voûte s'ouvre, et la
 *       matière qui manque se retrouve <b>au sol, en cône, sous le trou</b>. L'œil complète
 *       l'histoire tout seul — c'est ça qui se lit comme « vieux » plutôt que « abîmé ».</li>
 * </ol>
 *
 * <p>Tout est déterministe (graines fixes) : une pièce doit rester reproductible d'un
 * datagen à l'autre. La variété entre deux ruines du même plan vient des
 * <i>processors</i> et des pools, pas d'un tirage ici.
 */
public final class Masonry {

    // --- Palette ---------------------------------------------------------------

    public static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    public static final BlockState BRICK = ModBlocks.VEINED_STONE_BRICKS.get().defaultBlockState();
    public static final BlockState CRACKED = ModBlocks.CRACKED_VEINED_STONE_BRICKS.get().defaultBlockState();
    public static final BlockState CHISELED = ModBlocks.CHISELED_VEINED_STONE.get().defaultBlockState();
    public static final BlockState ROCK = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();
    public static final BlockState CONDUIT = ModBlocks.CONDUIT_LINE.get().defaultBlockState();
    public static final BlockState LAMP = ModBlocks.RESONANCE_LAMP.get().defaultBlockState();
    public static final BlockState BLOOM = ModBlocks.DISSONANCE_BLOOM.get().defaultBlockState();
    public static final BlockState BULKHEAD = ModBlocks.RESONANCE_BULKHEAD.get().defaultBlockState();
    public static final BlockState GLASS = Blocks.PURPLE_STAINED_GLASS.defaultBlockState();

    /** Gravats : de la maçonnerie tombée, pas de la pierre naturelle. */
    public static final BlockState RUBBLE = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    public static final BlockState DEBRIS = Blocks.GRAVEL.defaultBlockState();
    public static final BlockState WATER = Blocks.WATER.defaultBlockState();

    public static final BlockState SLAB = ModBlocks.VEINED_STONE_BRICK_SLAB.get().defaultBlockState()
        .setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
    public static final BlockState SLAB_TOP = ModBlocks.VEINED_STONE_BRICK_SLAB.get().defaultBlockState()
        .setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);

    private Masonry() {
    }

    /** Escalier de maçonnerie, orienté et posé en haut ou en bas de sa case. */
    public static BlockState stair(Direction facing, boolean top) {
        return ModBlocks.VEINED_STONE_BRICK_STAIRS.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
            .setValue(BlockStateProperties.HALF, top ? Half.TOP : Half.BOTTOM);
    }

    // =========================================================================
    // Salles
    // =========================================================================

    /**
     * Style d'une salle. Un style n'est pas de la décoration : c'est ce qui distingue une
     * <b>salle noble</b> (voûtée, à pilastres, dallée) d'un <b>couloir de service</b> (bas,
     * nu). Sans cette distinction, toutes les pièces se ressemblent quelle que soit leur
     * fonction — le défaut principal de la première version.
     */
    public record Style(int vaultSteps, boolean pilasters, boolean stringCourse,
                        int chamfer, BlockState floor, BlockState accent) {

        /** Grande salle : voûte haute, pilastres, bandeau, angles coupés. */
        public static Style noble() {
            return new Style(3, true, true, 2, CHISELED, CHISELED);
        }

        /** Salle ordinaire : voûte basse, pilastres, angles à peine coupés. */
        public static Style common() {
            return new Style(2, true, false, 1, BRICK, CHISELED);
        }

        /** Service : pas de voûte, pas d'ornement. Il en faut, sinon le noble ne se voit plus. */
        public static Style plain() {
            return new Style(0, false, false, 0, BRICK, BRICK);
        }
    }

    /**
     * <b>Creuse et chemise une salle.</b> Les coordonnées données sont l'<b>intérieur</b> ;
     * le sol, le plafond et les murs sont ajoutés autour. Rien n'est écrit au-delà : la
     * roche du monde reste la roche du monde.
     *
     * <p>Trois détails font tout le travail visuel, et aucun ne coûte de place jouable :
     * les <b>angles coupés</b> (une salle à angles droits se lit comme un cube, une salle
     * à angles cassés se lit comme une pièce), le <b>bandeau</b> à mi-hauteur (l'œil a
     * enfin une horizontale à autre chose que le sol et le plafond), et les
     * <b>pilastres</b> (le mur cesse d'être une surface extrudée).
     */
    public static void chamber(TemplateBuilder b, int x0, int y0, int z0,
                               int x1, int y1, int z1, Style style) {
        // Chemise : une gaine pleine, puis on évide. Écrire le plein d'abord garantit
        // qu'aucune grotte du monde ne débouche accidentellement dans la salle par un
        // angle qu'on aurait oublié.
        b.box(x0 - 1, y0 - 1, z0 - 1, x1 + 1, y1 + 1, z1 + 1, BRICK);
        b.box(x0, y0, z0, x1, y1, z1, AIR);
        b.box(x0, y0 - 1, z0, x1, y0 - 1, z1, style.floor());

        for (int i = 1; i <= style.chamfer(); i++) {
            chamferCorners(b, x0, y0, z0, x1, y1, z1, i);
        }
        if (style.pilasters()) {
            pilasters(b, x0, y0, z0, x1, y1, z1, style.accent());
        }
        if (style.stringCourse()) {
            int y = y0 + (y1 - y0) / 2;
            ringWall(b, x0, y, z0, x1, z1, SLAB_TOP);
        }
        if (style.vaultSteps() > 0) {
            barrelVault(b, x0, z0, x1, z1, y1 + 1, style.vaultSteps(), (x1 - x0) <= (z1 - z0));
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

    /** Pilastres engagés dans les longs murs, tous les 4 blocs. */
    private static void pilasters(TemplateBuilder b, int x0, int y0, int z0,
                                  int x1, int y1, int z1, BlockState accent) {
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            for (int y = y0; y <= y1; y++) {
                b.set(x0, y, z, y == y1 ? accent : BRICK);
                b.set(x1, y, z, y == y1 ? accent : BRICK);
            }
        }
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            for (int y = y0; y <= y1; y++) {
                b.set(x, y, z0, y == y1 ? accent : BRICK);
                b.set(x, y, z1, y == y1 ? accent : BRICK);
            }
        }
    }

    /** Bandeau horizontal courant sur les quatre murs. */
    private static void ringWall(TemplateBuilder b, int x0, int y, int z0, int x1, int z1,
                                 BlockState state) {
        for (int x = x0; x <= x1; x++) {
            b.set(x, y, z0, state);
            b.set(x, y, z1, state);
        }
        for (int z = z0; z <= z1; z++) {
            b.set(x0, y, z, state);
            b.set(x1, y, z, state);
        }
    }

    /**
     * <b>Voûte en berceau.</b> Le plafond ne s'arrête pas à une dalle plate : il monte par
     * ressauts d'escaliers retournés jusqu'à une clé. C'est <i>le</i> geste qui transforme
     * une boîte en salle — il vaut à lui seul tous les ornements de mur réunis.
     *
     * @param alongZ vrai si la voûte court selon Z (donc naît des murs est et ouest)
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
    // Rotonde et coupole
    // =========================================================================

    /**
     * <b>Salle octogonale surmontée d'une coupole.</b> Réservée à ce qui compte : une
     * pièce dont le plan n'est pas rectangulaire est immédiatement lue comme importante,
     * sans qu'on ait besoin de l'éclairer davantage ni d'y écrire quoi que ce soit.
     */
    public static void rotunda(TemplateBuilder b, int cx, int y0, int cz, int radius, int height) {
        int cut = radius / 2;
        // Masse pleine, puis évidement en octogone.
        b.box(cx - radius - 1, y0 - 1, cz - radius - 1,
            cx + radius + 1, y0 + height + radius, cz + radius + 1, BRICK);
        for (int y = y0; y < y0 + height; y++) {
            octagonLayer(b, cx, y, cz, radius, cut, AIR);
        }
        b.box(cx - radius, y0 - 1, cz - radius, cx + radius, y0 - 1, cz + radius, CHISELED);

        // Coupole : des anneaux d'octogones de rayon décroissant, bordés d'escaliers
        // retournés. Chaque anneau est un ressaut ; l'œil lit une courbe.
        for (int i = 0; i <= radius; i++) {
            int r = radius - i;
            int y = y0 + height + i;
            if (r < 1) {
                b.set(cx, y, cz, LAMP);
                break;
            }
            octagonLayer(b, cx, y, cz, r, Math.max(0, r / 2), AIR);
            octagonEdge(b, cx, y, cz, r, Math.max(0, r / 2));
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

    /** Bord d'un anneau de coupole, en escaliers retournés tournés vers le centre. */
    private static void octagonEdge(TemplateBuilder b, int cx, int y, int cz, int r, int cut) {
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
     * existe, et le seul moyen honnête de relier trois niveaux sans une rampe droite de
     * quinze mètres — qui, elle, se lit comme un tapis roulant.
     *
     * @return la hauteur atteinte à la dernière marche
     */
    public static int spiralStair(TemplateBuilder b, int cx, int cz, int yTop, int yBottom,
                                  Direction entry) {
        final int outer = 3;
        // La cage : une masse évidée sur toute la hauteur, plus un noyau plein au centre.
        // Le vide monte DEUX blocs au-dessus de la marche haute : on doit pouvoir se tenir
        // debout sur la dernière marche, pas seulement y poser les pieds.
        b.box(cx - outer - 1, yBottom - 1, cz - outer - 1, cx + outer + 1, yTop + 3, cz + outer + 1, BRICK);
        b.box(cx - outer, yBottom, cz - outer, cx + outer, yTop + 2, cz + outer, AIR);
        b.box(cx - 1, yBottom - 1, cz - 1, cx + 1, yTop + 1, cz + 1, CHISELED);

        // Le fond de la cage, plat : c'est le palier d'arrivée, et il doit être au même
        // niveau que la salle qu'il dessert.
        b.box(cx - outer, yBottom, cz - outer, cx + outer, yBottom, cz + outer, BRICK);

        // Les cases de l'anneau extérieur, parcourues dans l'ordre : une marche par case,
        // un bloc de descente par marche.
        //
        // <b>Le parcours commence du côté {@code entry}</b>, et ce n'est pas un détail de
        // confort : la première marche doit tomber pile devant la galerie qui amène ici.
        // Quand elle tombait ailleurs, on sortait de la galerie <b>dans le vide de la
        // cage</b> et on chutait de dix blocs — une structure parfaitement valide, et
        // infranchissable. Trouvé par le parcours automatisé, pas à la relecture.
        int[][] ring = ringCells(outer);
        int side = outer * 2;
        int offset = switch (entry) {
            case NORTH -> 0;
            case EAST -> side;
            case SOUTH -> side * 2;
            default -> side * 3;
        } + side / 2;

        int y = yTop;
        for (int i = 0; i < ring.length && y >= yBottom; i++) {
            int[] c = ring[(offset + i) % ring.length];
            int x = cx + c[0];
            int z = cz + c[1];
            b.set(x, y, z, i % 4 == 0 ? CHISELED : BRICK);
            // Contremarche : sans elle, une vis se lit comme des dalles flottantes.
            if (y - 1 >= yBottom) {
                b.set(x, y - 1, z, BRICK);
            }
            y--;
        }
        return y + 1;
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
     * Galerie : un tunnel chemisé, voûté d'un simple ressaut. Deux salles ne se touchent
     * jamais directement — c'est ce qui empêche le plan de redevenir une grille de cases.
     */
    public static void gallery(TemplateBuilder b, int x0, int y, int z0, int x1, int z1) {
        boolean alongZ = z1 != z0;
        // La chemise ne couvre QUE les flancs, jamais les deux bouts : une galerie doit
        // <b>percer</b> les murs qu'elle relie. Chemiser ses extrémités reboucherait
        // précisément les deux ouvertures qui justifient son existence — le genre de bug
        // qui ne casse rien, ne lève rien, et laisse simplement deux salles murées l'une
        // à l'autre. On donne donc une portée qui COMMENCE dans un mur et FINIT dans
        // l'autre.
        if (alongZ) {
            b.box(x0 - 1, y - 1, z0, x0 + 1, y + 3, z1, BRICK);
            b.box(x0, y, z0, x0, y + 2, z1, AIR);
            for (int z = z0; z <= z1; z++) {
                b.set(x0 - 1, y + 2, z, stair(Direction.EAST, true));
                b.set(x0 + 1, y + 2, z, stair(Direction.WEST, true));
            }
        } else {
            b.box(x0, y - 1, z0 - 1, x1, y + 3, z0 + 1, BRICK);
            b.box(x0, y, z0, x1, y + 2, z0, AIR);
            for (int x = x0; x <= x1; x++) {
                b.set(x, y + 2, z0 - 1, stair(Direction.SOUTH, true));
                b.set(x, y + 2, z0 + 1, stair(Direction.NORTH, true));
            }
        }
    }

    /** Percée de porte encadrée, deux blocs de haut, dans un mur déjà bâti. */
    public static void doorway(TemplateBuilder b, int x, int y, int z, boolean alongX) {
        int dx = alongX ? 0 : 1;
        int dz = alongX ? 1 : 0;
        b.set(x, y, z, AIR);
        b.set(x, y + 1, z, AIR);
        b.set(x + dx, y + 2, z + dz, CHISELED);
        b.set(x - dx, y + 2, z - dz, CHISELED);
        b.set(x, y + 2, z, CHISELED);
    }

    // =========================================================================
    // Ruine — causale, pas aléatoire
    // =========================================================================

    /**
     * <b>Effondrement d'une voûte.</b> On ouvre le plafond sur un disque, et
     * <b>la matière qui manque se retrouve au sol</b>, en cône, exactement dessous : c'est
     * cette correspondance, et elle seule, qui se lit comme « ça s'est écroulé ». Un
     * remplacement de blocs au hasard, si dense soit-il, se lit comme « c'est sale ».
     *
     * <p>Le trou est bouché par de la roche naturelle et du gravier, pas par du vide : au
     * -dessus d'une ruine il y a de la montagne, pas le ciel.
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
                // La voûte disparaît sur toute son épaisseur — clé comprise, sinon le trou
                // reste coiffé et l'effondrement ne se voit pas d'en dessous — remplacée
                // par la masse rocheuse qui l'écrasait.
                for (int y = yCeiling; y <= yCeiling + 3; y++) {
                    b.set(x, y, z, d < radius - 1 ? DEBRIS : ROCK);
                }
                // Le cône de gravats : le plus haut au centre, effilé sur les bords.
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
     * Flaque au point bas : neuf siècles d'infiltration. Une salle souterraine parfaitement
     * sèche est une salle qu'on vient de finir de construire.
     */
    public static void puddle(TemplateBuilder b, int cx, int y, int cz, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > radius) {
                    continue;
                }
                b.set(cx + dx, y - 1, cz + dz, Blocks.DEEPSLATE.defaultBlockState());
                b.set(cx + dx, y, cz + dz, WATER);
            }
        }
    }

    /** Concrétions au plafond d'une salle humide : le temps se voit vers le haut aussi. */
    public static void dripstone(TemplateBuilder b, int x, int yCeiling, int z, int length) {
        for (int i = 0; i < length; i++) {
            b.set(x, yCeiling - 1 - i, z, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                .setValue(BlockStateProperties.VERTICAL_DIRECTION, Direction.DOWN)
                .setValue(BlockStateProperties.DRIPSTONE_THICKNESS,
                    i == length - 1
                        ? net.minecraft.world.level.block.state.properties.DripstoneThickness.TIP
                        : net.minecraft.world.level.block.state.properties.DripstoneThickness.FRUSTUM));
        }
    }
}
