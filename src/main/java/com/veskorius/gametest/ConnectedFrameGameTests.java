package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ConnectedFrame;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * <b>La règle du cadre connecté, interrogée avec des voisinages inventés.</b>
 *
 * <p>Ces tests existent parce que ce cadre a échoué quatre fois d'affilée, et jamais de la
 * même façon : cadre autour de chaque bloc d'un mur, liserés clairs sur chaque bord,
 * scintillement, ciel visible à travers la paroi. Aucun de ces défauts n'était visible
 * ailleurs qu'à l'écran — le blockstate était correct, les modèles générés, l'audit vert.
 *
 * <p>Ce qui est vérifiable sans écran, c'est la <b>règle</b> : quelles pièces pour quel
 * voisinage. Les deux cas qui ont coûté le plus cher — l'arête concave et le coin rentrant —
 * sont ici, avec leur contre-exemple, parce que dans les deux cas la règle naïve produit
 * exactement l'inverse de ce qu'il faut au milieu d'une surface lisse.
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class ConnectedFrameGameTests {

    private static final String EMPTY = "empty";

    /** Aucun de ces blocs ne porte d'état : c'est ce qui rend les diagonales possibles. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void connectedBlocksCarryNoState(GameTestHelper helper) {
        for (ConnectedFrame.Frame frame : ConnectedFrame.FRAMES) {
            var definition = frame.block().get().getStateDefinition();
            helper.assertTrue(definition.getProperties().isEmpty(),
                frame.name() + " ne doit porter aucune propriété : son voisinage est lu au "
                    + "rendu, diagonales comprises, ce qui ferait 262 144 états s'il fallait "
                    + "le stocker");
            helper.assertTrue(definition.getPossibleStates().size() == 1,
                frame.name() + " : un seul état attendu");
        }
        helper.assertTrue(ConnectedFrame.FRAMES.size() == 5,
            "trois châssis et deux verres partagent la même mécanique");
        helper.succeed();
    }

    /** Les douze arêtes sont bien douze, et chacune a son bit, quel que soit l'ordre. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void everyEdgeHasItsOwnBit(GameTestHelper helper) {
        Set<Integer> bits = new HashSet<>();
        ConnectedFrame.forEachEdge((a, b) -> {
            bits.add(ConnectedFrame.edgeBit(a, b));
            helper.assertTrue(ConnectedFrame.edgeBit(a, b) == ConnectedFrame.edgeBit(b, a),
                "l'arête " + a + "/" + b + " doit donner le même bit dans les deux sens");
        });
        helper.assertTrue(bits.size() == 12, "douze arêtes distinctes, obtenu " + bits.size());

        int[] cornerCount = {0};
        ConnectedFrame.forEachFaceCorner((f, p, q) -> cornerCount[0]++);
        helper.assertTrue(cornerCount[0] == 24, "vingt-quatre coins de face");
        helper.succeed();
    }

    /** Un bloc isolé est une cage : douze baguettes, aucun quart de cadre. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void anIsolatedBlockIsACage(GameTestHelper helper) {
        helper.assertTrue(bars(0) == 12, "douze baguettes attendues, obtenu " + bars(0));
        helper.assertTrue(corners(0) == 0, "et aucun quart de cadre");
        helper.succeed();
    }

    /** Au milieu d'un mur plein, plus rien : ni baguette, ni coin. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void theMiddleOfAWallIsBare(GameTestHelper helper) {
        // Un mur dans le plan XY : les quatre côtés de ce plan, et ses quatre diagonales.
        int mask = ConnectedFrame.neighbourhood((first, second, third) ->
            first.getAxis() != Direction.Axis.Z
                && (second == null || second.getAxis() != Direction.Axis.Z)
                && third == null);

        helper.assertTrue(bars(mask) == 0,
            "aucune baguette au milieu d'un mur, obtenu " + bars(mask));
        helper.assertTrue(corners(mask) == 0,
            "et surtout aucun quart de cadre : ils apparaîtraient en pleine surface lisse");
        helper.succeed();
    }

    /**
     * <b>L'arête concave.</b> Un bloc posé sur une dalle : sa face est dégagée, son dessous
     * est pris, et la diagonale l'est aussi — la surface tourne d'un plan à l'autre. Sans
     * cette règle, la face verticale du bloc rejoignait le dessus de la dalle sans la moindre
     * séparation.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void aBlockStandingOnASlabGetsItsConcaveEdge(GameTestHelper helper) {
        int mask = ConnectedFrame.faceBit(Direction.DOWN)
            | ConnectedFrame.edgeBit(Direction.EAST, Direction.DOWN);

        helper.assertTrue(ConnectedFrame.hasBar(mask, Direction.EAST, Direction.DOWN, CREASES),
            "la face est est dégagée, le dessous est pris, la diagonale est-bas aussi : "
                + "la surface tourne, il faut une baguette");
        helper.assertTrue(!ConnectedFrame.hasBar(mask, Direction.WEST, Direction.DOWN, CREASES),
            "mais rien à l'ouest, où la dalle ne continue pas : les deux faces se prolongent");
        helper.succeed();
    }

    /**
     * Le contre-exemple, et il compte autant : dans un mur plat, chaque bloc a un voisin au-
     * dessus sans que rien ne dépasse devant. Une baguette y tracerait un trait horizontal au
     * milieu du panneau, une fois par bloc.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void aFlatSurfaceGetsNoConcaveEdge(GameTestHelper helper) {
        int mask = ConnectedFrame.faceBit(Direction.UP);

        helper.assertTrue(!ConnectedFrame.hasBar(mask, Direction.SOUTH, Direction.UP, CREASES),
            "voisin au-dessus mais rien en diagonale : les deux faces se prolongent, "
                + "aucune baguette");
        helper.succeed();
    }

    /**
     * <b>Le coin rentrant.</b> Disposition en L : le bloc de l'angle touche le voisin du
     * dessus et celui de l'ouest, mais la diagonale est vide. Il ne dessine aucune bordure —
     * et c'est précisément là que son coin restait nu.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void theConcaveCornerGetsItsQuarter(GameTestHelper helper) {
        int mask = ConnectedFrame.faceBit(Direction.UP) | ConnectedFrame.faceBit(Direction.WEST);

        helper.assertTrue(
            ConnectedFrame.hasCorner(mask, Direction.SOUTH, Direction.UP, Direction.WEST, CREASES),
            "la face sud est dégagée, le haut et l'ouest sont pris, la diagonale est vide : "
                + "il faut un quart de cadre");
        helper.assertTrue(
            !ConnectedFrame.hasCorner(mask, Direction.UP, Direction.SOUTH, Direction.WEST, CREASES),
            "mais rien sur la face du haut : elle est contre un voisin, on ne la voit pas");
        helper.assertTrue(corners(mask) == 2,
            "deux quarts de cadre exactement — sud et nord, obtenu " + corners(mask));
        helper.succeed();
    }

    /**
     * <b>Le pourtour du pied d'un bloc posé sur une dalle.</b> Aux quatre coins de ce pourtour,
     * les blocs de la dalle sont en <b>diagonale</b> du bloc debout : leur surface se prolonge
     * dans les deux directions, mais elle monte en marche par le coin. Sans le bloc de
     * <b>sommet</b>, on ne peut pas distinguer ce cas d'une surface plate — et les quatre
     * bordures concaves se rejoignaient en laissant un trou à chaque angle.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void theFootOfAStandingBlockClosesItsCorners(GameTestHelper helper) {
        // Vu depuis le bloc de dalle en diagonale : la dalle continue à l'ouest et au sud, et
        // le bloc debout occupe le sommet ouest-sud-haut.
        int mask = ConnectedFrame.faceBit(Direction.WEST)
            | ConnectedFrame.faceBit(Direction.SOUTH)
            | ConnectedFrame.edgeBit(Direction.WEST, Direction.SOUTH)
            | ConnectedFrame.vertexBit(Direction.WEST, Direction.SOUTH, Direction.UP);

        helper.assertTrue(
            ConnectedFrame.hasCorner(mask, Direction.UP, Direction.WEST, Direction.SOUTH, CREASES),
            "la surface monte en marche par le coin : il faut le fermer");

        // Le même voisinage SANS le bloc debout : c'est une dalle plate, il ne faut rien.
        int flat = mask & ~ConnectedFrame.vertexBit(Direction.WEST, Direction.SOUTH, Direction.UP);
        helper.assertTrue(
            !ConnectedFrame.hasCorner(flat, Direction.UP, Direction.WEST, Direction.SOUTH, CREASES),
            "sans le bloc debout, la dalle est plate : aucun quart de cadre");
        helper.assertTrue(corners(flat) == 0, "et nulle part ailleurs non plus");
        helper.succeed();
    }

    /**
     * La même disposition, mais la diagonale est occupée : c'est l'intérieur d'un carré plein,
     * et il ne doit RIEN y avoir. Sans cette condition, le bloc central d'un mur poserait
     * quatre quarts de cadre au milieu d'une surface lisse.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void aFilledDiagonalLeavesTheCornerAlone(GameTestHelper helper) {
        int mask = ConnectedFrame.faceBit(Direction.UP)
            | ConnectedFrame.faceBit(Direction.WEST)
            | ConnectedFrame.edgeBit(Direction.UP, Direction.WEST);

        helper.assertTrue(
            !ConnectedFrame.hasCorner(mask, Direction.SOUTH, Direction.UP, Direction.WEST, CREASES),
            "diagonale occupée : le coin est intérieur, aucun cadre");
        helper.assertTrue(corners(mask) == 0, "aucun quart de cadre nulle part");
        helper.succeed();
    }

    /**
     * Une baguette et un quart de cadre ne se superposent jamais. Deux surfaces au même
     * endroit clignotent, et c'est un défaut qu'on n'aurait vu qu'en jeu, dans une
     * configuration précise.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void barsAndCornersNeverOverlap(GameTestHelper helper) {
        // Tous les voisinages où les six faces et les douze diagonales varient librement
        // seraient 262 144 cas ; on balaie les six faces et on fait varier les diagonales du
        // plan concerné, ce qui suffit à couvrir la superposition.
        for (int faces = 0; faces < 64; faces++) {
            for (int diagonals = 0; diagonals < 64; diagonals++) {
                int mask = faces | (diagonals << 6);
                ConnectedFrame.forEachFaceCorner((f, p, q) -> {
                    if (!ConnectedFrame.hasCorner(mask, f, p, q, CREASES)) {
                        return;
                    }
                    helper.assertTrue(!ConnectedFrame.hasBar(mask, f, p, CREASES),
                        "quart de cadre et baguette " + f + "/" + p + " au même endroit");
                    helper.assertTrue(!ConnectedFrame.hasBar(mask, f, q, CREASES),
                        "quart de cadre et baguette " + f + "/" + q + " au même endroit");
                });
            }
        }
        helper.succeed();
    }

    /** Les caissons soulignent les plis ; le verre ne dessine que sa silhouette. */
    private static final boolean CREASES = true;
    private static final boolean SILHOUETTE = false;

    /**
     * <b>Le verre ne souligne pas les plis, et les caissons si.</b>
     *
     * <p>Un caisson est opaque : ses plis sont des arêtes qu'on voit, et les souligner rend la
     * forme lisible. Le verre du mod est transparent à 96 % — il n'a presque pas de surface —
     * et une baguette posée sur un pli n'y borde rien : elle flotte en l'air.
     *
     * <p>La <b>silhouette</b>, elle, vaut pour les deux : le contour du groupe et le coin
     * rentrant d'un L se ferment dans les deux styles.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void glassDrawsItsSilhouetteOnly(GameTestHelper helper) {
        // Le pied d'un bloc posé sur une dalle : un pli.
        int step = ConnectedFrame.faceBit(Direction.DOWN)
            | ConnectedFrame.edgeBit(Direction.EAST, Direction.DOWN);
        helper.assertTrue(ConnectedFrame.hasBar(step, Direction.EAST, Direction.DOWN, CREASES),
            "un caisson souligne le pli");
        helper.assertTrue(!ConnectedFrame.hasBar(step, Direction.EAST, Direction.DOWN, SILHOUETTE),
            "le verre ne le souligne pas : la baguette flotterait dans le vide");

        // Un bloc isolé : douze baguettes dans les deux styles.
        helper.assertTrue(bars(0) == 12 && barsOf(0, SILHOUETTE) == 12,
            "la silhouette d'un bloc isolé ne dépend pas du style");

        // Le coin rentrant d'un L : silhouette, donc les deux le ferment.
        int corner = ConnectedFrame.faceBit(Direction.UP) | ConnectedFrame.faceBit(Direction.WEST);
        helper.assertTrue(
            ConnectedFrame.hasCorner(corner, Direction.SOUTH, Direction.UP, Direction.WEST, SILHOUETTE),
            "le coin rentrant appartient à la silhouette : le verre le ferme aussi");

        // La marche par le coin, elle, est un pli.
        int stepCorner = ConnectedFrame.faceBit(Direction.WEST)
            | ConnectedFrame.faceBit(Direction.SOUTH)
            | ConnectedFrame.edgeBit(Direction.WEST, Direction.SOUTH)
            | ConnectedFrame.vertexBit(Direction.WEST, Direction.SOUTH, Direction.UP);
        helper.assertTrue(
            ConnectedFrame.hasCorner(stepCorner, Direction.UP, Direction.WEST, Direction.SOUTH, CREASES),
            "un caisson ferme l'angle du pourtour");
        helper.assertTrue(
            !ConnectedFrame.hasCorner(stepCorner, Direction.UP, Direction.WEST, Direction.SOUTH, SILHOUETTE),
            "le verre n'a pas de pourtour à fermer, puisqu'il ne trace pas le pli");
        helper.succeed();
    }

    private static int barsOf(int mask, boolean creases) {
        int[] n = {0};
        ConnectedFrame.forEachEdge((a, b) -> {
            if (ConnectedFrame.hasBar(mask, a, b, creases)) {
                n[0]++;
            }
        });
        return n[0];
    }

    private static int bars(int mask) {
        int[] n = {0};
        ConnectedFrame.forEachEdge((a, b) -> {
            if (ConnectedFrame.hasBar(mask, a, b, CREASES)) {
                n[0]++;
            }
        });
        return n[0];
    }

    private static int corners(int mask) {
        int[] n = {0};
        ConnectedFrame.forEachFaceCorner((f, p, q) -> {
            if (ConnectedFrame.hasCorner(mask, f, p, q, CREASES)) {
                n[0]++;
            }
        });
        return n[0];
    }
}
