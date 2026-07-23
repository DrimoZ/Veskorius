package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.block.entity.SideMode;
import com.veskorius.item.ModItems;
import com.veskorius.menu.AbstractMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Tests de l'automatisation d'OBJETS des machines (item I/O sidé, config par face) —
 * l'énergie, elle, ne passe jamais par une capability (pas de tuyaux). Jouée sur le
 * Resonance Stabilizer, dont le comportement vient du socle {@code AbstractMachineBlockEntity},
 * donc valable pour toutes les machines actives.
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class AutomationGameTests {

    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(2, 1, 2);

    private static ResonanceStabilizerBlockEntity place(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_STABILIZER.get());
        return helper.getBlockEntity(MACHINE);
    }

    private static IItemHandler cap(GameTestHelper helper, BlockPos pos, Direction side) {
        return helper.getLevel().getCapability(
            Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), side);
    }

    /** Défaut « façon four » : sortie sous le bloc, entrée par les autres faces, cap exposée. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void defaultSideConfigIsFurnaceLike(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = place(helper);
        helper.assertTrue(machine.getSideMode(Direction.DOWN) == SideMode.OUTPUT,
            "Le dessous doit être en sortie par défaut");
        helper.assertTrue(machine.getSideMode(Direction.UP) == SideMode.INPUT,
            "Le dessus doit être en entrée par défaut");
        helper.assertTrue(machine.getSideMode(Direction.NORTH) == SideMode.INPUT,
            "Les côtés doivent être en entrée par défaut");
        helper.assertTrue(cap(helper, MACHINE, Direction.UP) != null,
            "Une capability doit être exposée sur une face entrée");
        helper.assertTrue(cap(helper, MACHINE, Direction.DOWN) != null,
            "Une capability doit être exposée sur une face sortie");
        helper.succeed();
    }

    /** Face entrée : insère dans les slots d'entrée, filtre par recette, n'extrait pas. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void inputFaceInsertsFilteredAndCannotExtract(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = place(helper);
        IItemHandler up = cap(helper, MACHINE, Direction.UP);

        ItemStack leftover = ItemHandlerHelper.insertItemStacked(
            up, new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
        helper.assertTrue(leftover.isEmpty(), "Le cristal brut doit entrer par la face entrée");
        helper.assertTrue(
            machine.getInventory().getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL)
                .is(ModItems.RAW_RESONANCE_CRYSTAL.get()),
            "Le cristal doit arriver dans le slot d'entrée cristal");

        ItemStack rejected = ItemHandlerHelper.insertItemStacked(up, new ItemStack(Items.DIAMOND), false);
        helper.assertFalse(rejected.isEmpty(), "Un objet hors recette doit être refusé");

        // Aucune extraction possible par une face entrée.
        for (int slot = 0; slot < up.getSlots(); slot++) {
            helper.assertTrue(up.extractItem(slot, 64, false).isEmpty(),
                "Une face entrée ne doit jamais permettre l'extraction");
        }
        helper.succeed();
    }

    /** Face sortie : extrait la sortie, refuse l'insertion. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void outputFaceExtractsAndCannotInsert(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = place(helper);
        machine.getInventory().setStackInSlot(ResonanceStabilizerBlockEntity.SLOT_OUTPUT,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 3));
        IItemHandler down = cap(helper, MACHINE, Direction.DOWN);

        ItemStack rejected = ItemHandlerHelper.insertItemStacked(
            down, new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
        helper.assertFalse(rejected.isEmpty(), "Une face sortie ne doit pas accepter l'insertion");

        ItemStack extracted = down.extractItem(0, 64, false);
        helper.assertTrue(extracted.is(ModItems.STABLE_RESONANCE_CRYSTAL.get()) && extracted.getCount() == 3,
            "Une face sortie doit permettre d'extraire la sortie, obtenu : " + extracted);
        helper.succeed();
    }

    /** L'automatisation ne peut jamais atteindre le slot d'augment. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void augmentSlotNeverExposed(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = place(helper);

        // Vue complète (side == null) = 2 entrées + 1 sortie = 3 slots, jamais l'augment.
        IItemHandler full = machine.getItemHandler(null);
        helper.assertTrue(full != null && full.getSlots() == 3,
            "La vue complète doit exposer 3 slots (2 entrées + 1 sortie), pas l'augment");

        IItemHandler up = cap(helper, MACHINE, Direction.UP);
        ItemStack core = ItemHandlerHelper.insertItemStacked(
            up, new ItemStack(ModItems.RESONANCE_CATALYST_CORE.get()), false);
        helper.assertFalse(core.isEmpty(), "L'automatisation ne doit pas pouvoir insérer un augment");
        helper.assertTrue(
            machine.getInventory().getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_AUGMENT).isEmpty(),
            "Le slot d'augment doit rester intact");
        helper.succeed();
    }

    /** Une face désactivée n'expose aucune capability ; le cycle de modes fonctionne. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void disabledFaceHidesCapabilityAndCyclesModes(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = place(helper);
        machine.setSideMode(Direction.NORTH, SideMode.DISABLED);
        helper.assertTrue(machine.getSideMode(Direction.NORTH) == SideMode.DISABLED,
            "Le mode de face doit être modifiable");
        helper.assertTrue(cap(helper, MACHINE, Direction.NORTH) == null,
            "Une face désactivée ne doit exposer aucune capability");

        machine.setSideMode(Direction.NORTH, SideMode.INPUT);
        machine.cycleSideMode(Direction.NORTH);
        helper.assertTrue(machine.getSideMode(Direction.NORTH) == SideMode.OUTPUT,
            "Le cycle de mode doit passer INPUT -> OUTPUT");
        helper.succeed();
    }

    /** Auto-sortie : pousse la sortie vers un conteneur adjacent sur une face OUTPUT. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void autoOutputPushesToNeighbour(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                ResonanceStabilizerBlockEntity machine = place(helper);
                machine.setSideMode(Direction.EAST, SideMode.OUTPUT);
                machine.setAutoOutput(true);
                machine.getInventory().setStackInSlot(ResonanceStabilizerBlockEntity.SLOT_OUTPUT,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 5));
                helper.setBlock(MACHINE.east(), Blocks.BARREL);
            })
            .thenExecuteAfter(14, () -> {
                ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);
                helper.assertTrue(
                    machine.getInventory().getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "La sortie de la machine doit avoir été poussée");
                IItemHandler barrel = cap(helper, MACHINE.east(), Direction.WEST);
                helper.assertTrue(countIn(barrel, ModItems.STABLE_RESONANCE_CRYSTAL.get()) == 5,
                    "Le tonneau adjacent doit avoir reçu les 5 cristaux");
            })
            .thenSucceed();
    }

    /** Auto-entrée : tire d'un conteneur adjacent vers les slots d'entrée sur une face INPUT. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void autoInputPullsFromNeighbour(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                ResonanceStabilizerBlockEntity machine = place(helper);
                machine.setSideMode(Direction.EAST, SideMode.INPUT);
                machine.setAutoInput(true);
                helper.setBlock(MACHINE.east(), Blocks.BARREL);
                IItemHandler barrel = cap(helper, MACHINE.east(), Direction.WEST);
                barrel.insertItem(0, new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get(), 3), false);
            })
            .thenExecuteAfter(14, () -> {
                ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);
                helper.assertTrue(
                    machine.getInventory().getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL)
                        .getCount() == 3,
                    "Les 3 cristaux du tonneau doivent avoir été tirés dans le slot d'entrée");
                IItemHandler barrel = cap(helper, MACHINE.east(), Direction.WEST);
                helper.assertTrue(countIn(barrel, ModItems.RAW_RESONANCE_CRYSTAL.get()) == 0,
                    "Le tonneau doit avoir été vidé");
            })
            .thenSucceed();
    }

    /** Les boutons de config du GUI (canal menu vanilla) modifient bien l'état serveur. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void menuButtonsConfigureSides(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = place(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = machine.createMenu(0, player.getInventory(), player);

        helper.assertTrue(machine.getSideMode(Direction.NORTH) == SideMode.INPUT,
            "Une face latérale est en entrée par défaut");
        menu.clickMenuButton(player,
            AbstractMachineMenu.BUTTON_CYCLE_SIDE_BASE + Direction.NORTH.get3DDataValue());
        helper.assertTrue(machine.getSideMode(Direction.NORTH) == SideMode.OUTPUT,
            "Le bouton de face doit cycler INPUT -> OUTPUT, vaut " + machine.getSideMode(Direction.NORTH));

        helper.assertFalse(machine.isAutoOutput(), "Auto-sortie inactive par défaut");
        menu.clickMenuButton(player, AbstractMachineMenu.BUTTON_AUTO_OUTPUT);
        helper.assertTrue(machine.isAutoOutput(), "Le bouton auto-sortie doit l'activer");
        helper.succeed();
    }

    private static int countIn(IItemHandler handler, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
