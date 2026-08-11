package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractConnectedBlock;
import com.veskorius.block.ChassisFrame;
import com.veskorius.block.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * <b>Les blocs connectés se voient-ils vraiment ?</b>
 *
 * <p>Ces tests existent parce que le cadre des châssis s'est affiché <b>autour de chaque
 * bloc</b> dans un mur entier : le blockstate était correct, les modèles étaient générés,
 * l'audit passait — et à l'écran, aucune connexion. Rien, dans la chaîne de génération, ne
 * pouvait le dire.
 *
 * <p>Deux mécaniques cohabitent, et on teste les deux là où elles vivent :
 * <ul>
 *   <li>le <b>verre</b> porte ses six côtés dans son blockstate, donc on pose deux blocs et
 *       on relit l'état ;</li>
 *   <li>le <b>châssis</b> n'a plus aucune propriété — son voisinage est lu au rendu, avec les
 *       diagonales — donc c'est la <b>règle</b> qu'on interroge, avec des voisinages inventés.
 *       C'est là que vit le coin rentrant, et c'est exactement le raisonnement qu'on croit
 *       juste et qui ne l'est pas.</li>
 * </ul>
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class ConnectedBlockGameTests {

    private static final String EMPTY = "empty";
    private static final BlockPos A = new BlockPos(2, 1, 2);

    // --- Le verre : l'état du bloc ------------------------------------------

    /**
     * Deux verres identiques accolés se reconnaissent — des deux côtés.
     *
     * <p>Le sens compte : {@code updateShape} n'est appelé que sur le bloc DÉJÀ posé quand le
     * second arrive. Si seul le nouveau venu se mettait à jour, le mur n'aurait de cadre que
     * d'un côté de chaque joint.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void twoGlassPanesSeeEachOther(GameTestHelper helper) {
        Block glass = ModBlocks.RESONANCE_GLASS.get();
        BlockPos b = A.east();
        helper.setBlock(A, glass);
        helper.setBlock(b, glass);

        assertLink(helper, A, Direction.EAST, true, "le premier posé doit voir le second");
        assertLink(helper, b, Direction.WEST, true, "le second posé doit voir le premier");
        assertLink(helper, A, Direction.UP, false, "rien au-dessus : aucune liaison");
        helper.succeed();
    }

    /** Retirer un voisin recoud le mur : le côté redevient ouvert, donc le cadre revient. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void breakingANeighbourReopensTheSide(GameTestHelper helper) {
        Block glass = ModBlocks.RESONANCE_GLASS.get();
        BlockPos b = A.east();
        helper.setBlock(A, glass);
        helper.setBlock(b, glass);
        helper.setBlock(b, net.minecraft.world.level.block.Blocks.AIR);

        assertLink(helper, A, Direction.EAST, false,
            "le voisin cassé, le côté doit se rouvrir");
        helper.succeed();
    }

    /**
     * Le verre lumineux ne se fond pas dans l'ordinaire : ils n'ont pas la même luminosité,
     * donc le joint entre les deux est une information qu'on veut voir.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void glassKindsDoNotMerge(GameTestHelper helper) {
        BlockPos b = A.east();
        helper.setBlock(A, ModBlocks.RESONANCE_GLASS.get());
        helper.setBlock(b, ModBlocks.LUMINOUS_RESONANCE_GLASS.get());

        assertLink(helper, A, Direction.EAST, false, "deux verres différents ne se lient pas");
        assertLink(helper, b, Direction.WEST, false, "et pas davantage dans l'autre sens");
        helper.succeed();
    }

    /** Le châssis, lui, n'a plus AUCUNE propriété — c'est ce qui rend son cadre possible. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void chassisCarriesNoState(GameTestHelper helper) {
        Block chassis = ModBlocks.FRACTURED_CHASSIS.get();
        helper.assertTrue(chassis.getStateDefinition().getProperties().isEmpty(),
            "le châssis ne doit porter aucune propriété : son voisinage est lu au rendu, "
                + "diagonales comprises, ce qui ferait 262 144 états s'il fallait le stocker");
        helper.assertTrue(chassis.getStateDefinition().getPossibleStates().size() == 1,
            "donc un seul état");
        helper.succeed();
    }

    // --- Le châssis : la règle ----------------------------------------------

    /** Les douze arêtes sont bien douze, et chacune a son bit, quel que soit l'ordre. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void everyEdgeHasItsOwnBit(GameTestHelper helper) {
        Set<Integer> bits = new HashSet<>();
        ChassisFrame.forEachEdge((a, b) -> {
            bits.add(ChassisFrame.edgeBit(a, b));
            helper.assertTrue(ChassisFrame.edgeBit(a, b) == ChassisFrame.edgeBit(b, a),
                "l'arête " + a + "/" + b + " doit donner le même bit dans les deux sens");
        });
        helper.assertTrue(bits.size() == 12, "douze arêtes distinctes, obtenu " + bits.size());
        helper.assertTrue(count(ChassisFrame::forEachFaceCorner) == 24,
            "vingt-quatre coins de face");
        helper.succeed();
    }

    /** Un châssis isolé est une cage : douze baguettes, aucun quart de cadre. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void anIsolatedChassisIsACage(GameTestHelper helper) {
        int mask = 0;
        int[] bars = {0};
        ChassisFrame.forEachEdge((a, b) -> {
            if (ChassisFrame.hasBar(mask, a, b)) {
                bars[0]++;
            }
        });
        helper.assertTrue(bars[0] == 12, "douze baguettes attendues, obtenu " + bars[0]);
        helper.assertTrue(corners(mask) == 0, "et aucun quart de cadre");
        helper.succeed();
    }

    /** Au milieu d'un mur plein, plus rien : ni baguette, ni coin. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void theMiddleOfAWallIsBare(GameTestHelper helper) {
        // Les quatre côtés du plan du mur, ET les quatre diagonales de ce plan.
        int mask = ChassisFrame.neighbourhood((first, second) -> {
            if (first.getAxis() == Direction.Axis.Z || (second != null && second.getAxis() == Direction.Axis.Z)) {
                return false;
            }
            return true;
        });
        int[] bars = {0};
        ChassisFrame.forEachEdge((a, b) -> {
            if (ChassisFrame.hasBar(mask, a, b)) {
                bars[0]++;
            }
        });
        helper.assertTrue(bars[0] == 0, "aucune baguette au milieu d'un mur, obtenu " + bars[0]);
        helper.assertTrue(corners(mask) == 0,
            "et surtout aucun quart de cadre : ils apparaîtraient en plein milieu d'une "
                + "surface lisse");
        helper.succeed();
    }

    /**
     * <b>Le coin rentrant.</b> Disposition en L : le bloc de l'angle touche le voisin du
     * dessus et celui de l'ouest, mais la diagonale est vide. Il ne dessine aucune bordure —
     * et c'est précisément là que son coin restait nu.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void theConcaveCornerGetsItsQuarter(GameTestHelper helper) {
        int mask = ChassisFrame.faceBit(Direction.UP) | ChassisFrame.faceBit(Direction.WEST);

        helper.assertTrue(
            ChassisFrame.hasCorner(mask, Direction.SOUTH, Direction.UP, Direction.WEST),
            "la face sud est dégagée, le haut et l'ouest sont pris, la diagonale est vide : "
                + "il faut un quart de cadre");
        helper.assertTrue(
            ChassisFrame.hasCorner(mask, Direction.NORTH, Direction.UP, Direction.WEST),
            "et pareil sur la face nord, qui est dégagée elle aussi");
        helper.assertTrue(
            !ChassisFrame.hasCorner(mask, Direction.UP, Direction.SOUTH, Direction.WEST),
            "mais rien sur la face du haut : elle est contre un voisin, on ne la voit pas");
        helper.assertTrue(corners(mask) == 2, "deux quarts de cadre exactement, obtenu " + corners(mask));
        helper.succeed();
    }

    /**
     * La même disposition, mais la diagonale est occupée : c'est l'intérieur d'un carré plein,
     * et il ne doit RIEN y avoir. Sans cette condition, le bloc central d'un mur poserait
     * quatre quarts de cadre au milieu d'une surface lisse.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void aFilledDiagonalLeavesTheCornerAlone(GameTestHelper helper) {
        int mask = ChassisFrame.faceBit(Direction.UP)
            | ChassisFrame.faceBit(Direction.WEST)
            | ChassisFrame.edgeBit(Direction.UP, Direction.WEST);

        helper.assertTrue(
            !ChassisFrame.hasCorner(mask, Direction.SOUTH, Direction.UP, Direction.WEST),
            "diagonale occupée : le coin est intérieur, aucun cadre");
        helper.assertTrue(corners(mask) == 0, "aucun quart de cadre nulle part");
        helper.succeed();
    }

    // --- Outillage -----------------------------------------------------------

    private static int corners(int mask) {
        int[] n = {0};
        ChassisFrame.forEachFaceCorner((f, p, q) -> {
            if (ChassisFrame.hasCorner(mask, f, p, q)) {
                n[0]++;
            }
        });
        return n[0];
    }

    private static int count(java.util.function.Consumer<ChassisFrame.CornerConsumer> walker) {
        int[] n = {0};
        walker.accept((f, p, q) -> n[0]++);
        return n[0];
    }

    private static void assertLink(GameTestHelper helper, BlockPos pos, Direction side,
                                   boolean expected, String why) {
        BlockState state = helper.getBlockState(pos);
        helper.assertTrue(state.hasProperty(AbstractConnectedBlock.property(side)),
            "le bloc doit porter la propriété " + side.getSerializedName());
        boolean actual = state.getValue(AbstractConnectedBlock.property(side));
        helper.assertTrue(actual == expected,
            why + " (attendu " + expected + ", obtenu " + actual + ")");
    }
}
