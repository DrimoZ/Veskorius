package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractConnectedBlock;
import com.veskorius.block.ModBlocks;
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
 * bloc</b> dans un mur entier : le blockstate était correct, les douze modèles de baguette
 * étaient générés, l'audit passait — et à l'écran, aucune connexion. Rien, dans la chaîne
 * de génération, ne pouvait le dire : la question n'est pas « le JSON est-il bon ? » mais
 * « l'état du bloc, en jeu, porte-t-il le bon booléen ? ».
 *
 * <p>C'est exactement la classe de bug qui a déjà coûté cher ici — tout est vert et la
 * fonctionnalité est morte. Un test qui pose deux blocs côte à côte et relit leur état est
 * la seule chose qui l'attrape.
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class ConnectedBlockGameTests {

    private static final String EMPTY = "empty";
    private static final BlockPos A = new BlockPos(2, 1, 2);

    /**
     * Deux châssis identiques accolés se reconnaissent — des deux côtés.
     *
     * <p>Le sens du voisin compte : {@code updateShape} n'est appelé que sur le bloc DÉJÀ
     * posé quand le second arrive. Si seul le nouveau venu se mettait à jour, le mur
     * n'aurait de cadre que d'un côté de chaque joint.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void twoChassisSeeEachOther(GameTestHelper helper) {
        Block chassis = ModBlocks.FRACTURED_CHASSIS.get();
        BlockPos b = A.east();
        helper.setBlock(A, chassis);
        helper.setBlock(b, chassis);

        assertLink(helper, A, Direction.EAST, true, "le premier posé doit voir le second");
        assertLink(helper, b, Direction.WEST, true, "le second posé doit voir le premier");
        assertLink(helper, A, Direction.UP, false, "rien au-dessus : aucune liaison");
        helper.succeed();
    }

    /** Retirer un voisin recoud le mur : le côté redevient ouvert, donc le cadre revient. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void breakingANeighbourReopensTheSide(GameTestHelper helper) {
        Block chassis = ModBlocks.FRACTURED_CHASSIS.get();
        BlockPos b = A.east();
        helper.setBlock(A, chassis);
        helper.setBlock(b, chassis);
        helper.setBlock(b, net.minecraft.world.level.block.Blocks.AIR);

        assertLink(helper, A, Direction.EAST, false,
            "le voisin cassé, le côté doit se rouvrir");
        helper.succeed();
    }

    /**
     * Deux paliers différents ne se fondent pas l'un dans l'autre. Le palier est une
     * information qu'on lit sur le bâtiment ; un cuivre qui se lierait à un veskorien la
     * ferait disparaître au joint.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void tiersDoNotMerge(GameTestHelper helper) {
        BlockPos b = A.east();
        helper.setBlock(A, ModBlocks.FRACTURED_CHASSIS.get());
        helper.setBlock(b, ModBlocks.VESKORIAN_CHASSIS.get());

        assertLink(helper, A, Direction.EAST, false, "cuivre contre veskorien : pas de liaison");
        assertLink(helper, b, Direction.WEST, false, "et pas davantage dans l'autre sens");
        helper.succeed();
    }

    /** Le verre connecté suit la même mécanique, et la partage désormais vraiment. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void glassUsesTheSameMachinery(GameTestHelper helper) {
        Block glass = ModBlocks.RESONANCE_GLASS.get();
        BlockPos b = A.east();
        helper.setBlock(A, glass);
        helper.setBlock(b, glass);

        assertLink(helper, A, Direction.EAST, true, "deux verres accolés se voient");
        assertLink(helper, b, Direction.WEST, true, "des deux côtés");
        helper.succeed();
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
