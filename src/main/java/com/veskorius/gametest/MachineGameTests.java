package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Tests du socle de machine, joues sur le Resonance Stabilizer.
 *
 * Lancer avec {@code ./gradlew runGameTestServer} : le serveur demarre, execute
 * les tests dans une structure vide et rend la main. Aucune partie manuelle.
 *
 * Ce qui est teste ici est le comportement de {@link AbstractMachineBlockEntity},
 * pas les specificites du Stabilizer — les 22 machines suivantes heritent du
 * meme cycle, donc une regression ici les casserait toutes en meme temps.
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class MachineGameTests {

    private static final String EMPTY = "empty";

    /** Position de la machine dans le template 5x5x5. */
    private static final BlockPos MACHINE = new BlockPos(2, 1, 2);

    /**
     * 30 secondes (05-Machines.md #1). Volontairement re-ecrit ici plutot
     * qu'importe depuis la machine : si quelqu'un change la duree du cycle sans
     * mettre a jour le dossier de conception, ce test doit echouer.
     */
    private static final int CYCLE_TICKS = 30 * 20;

    private static final int TIMEOUT = CYCLE_TICKS + 200;

    // --- Cas nominal ---------------------------------------------------------

    @GameTest(template = EMPTY, timeoutTicks = TIMEOUT)
    public static void stabilizerProducesStableCrystal(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                IItemHandler inventory = placeStabilizer(helper);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
                    new ItemStack(Items.QUARTZ), false);
            })
            .thenExecuteAfter(CYCLE_TICKS + 5, () -> {
                IItemHandler inventory = inventoryOf(helper);

                ItemStack output = inventory.getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(output.is(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                    "La sortie devrait contenir un Stable Resonance Crystal, trouve : " + output);
                helper.assertTrue(output.getCount() == 1,
                    "La sortie devrait contenir exactement 1 cristal, trouve : " + output.getCount());

                helper.assertTrue(
                    inventory.getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Le cristal brut aurait du etre consomme");
                helper.assertTrue(
                    inventory.getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_FLUX).isEmpty(),
                    "Le quartz aurait du etre consomme");
            })
            .thenSucceed();
    }

    /** Le cycle ne doit pas s'achever avant l'heure. */
    @GameTest(template = EMPTY, timeoutTicks = TIMEOUT)
    public static void stabilizerDoesNotFinishEarly(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                IItemHandler inventory = placeStabilizer(helper);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
                    new ItemStack(Items.QUARTZ), false);
            })
            .thenExecuteAfter(CYCLE_TICKS - 20, () -> helper.assertTrue(
                inventoryOf(helper).getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_OUTPUT).isEmpty(),
                "Le cycle s'est termine avant les 30 secondes prevues"))
            .thenSucceed();
    }

    // --- Interruption --------------------------------------------------------

    /**
     * Retirer une entree en cours de cycle remet la progression a zero : pas de
     * cycle "en pause" qu'on reprendrait plus tard.
     */
    @GameTest(template = EMPTY, timeoutTicks = TIMEOUT)
    public static void stabilizerResetsProgressWhenInputRemoved(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                IItemHandler inventory = placeStabilizer(helper);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
                    new ItemStack(Items.QUARTZ), false);
            })
            .thenExecuteAfter(100, () -> {
                helper.assertTrue(progressOf(helper) > 0,
                    "La progression aurait du demarrer");
                inventoryOf(helper).extractItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL, 1, false);
            })
            .thenExecuteAfter(5, () -> helper.assertTrue(progressOf(helper) == 0,
                "La progression aurait du etre remise a zero, vaut : " + progressOf(helper)))
            .thenSucceed();
    }

    /** Sortie pleine : la machine ne doit pas demarrer, ni detruire ses entrees. */
    @GameTest(template = EMPTY, timeoutTicks = TIMEOUT)
    public static void stabilizerWaitsWhenOutputFull(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                ResonanceStabilizerBlockEntity machine = placeAndGet(helper);
                IItemHandler inventory = machine.getInventory();
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
                    new ItemStack(Items.QUARTZ), false);
                // Le slot de sortie refuse toute insertion (isItemValid), d'ou
                // setStackInSlot pour le remplir de force cote test.
                machine.getInventory().setStackInSlot(ResonanceStabilizerBlockEntity.SLOT_OUTPUT,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 64));
            })
            .thenExecuteAfter(60, () -> {
                helper.assertTrue(progressOf(helper) == 0,
                    "La machine ne devrait pas progresser sortie pleine, progression : "
                        + progressOf(helper));
                helper.assertTrue(
                    !inventoryOf(helper).getStackInSlot(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Les entrees ne doivent pas etre consommees quand la sortie est pleine");
            })
            .thenSucceed();
    }

    // --- Slot d'augment ------------------------------------------------------

    /**
     * Le slot d'augment n'accepte que le tag {@code veskorius:machine_augments},
     * aujourd'hui vide : il doit donc tout refuser, y compris les objets du mod.
     *
     * A completer a la tache 15 de la Phase 1, quand le Resonance Catalyst Core
     * existera : verifier qu'il est accepte, et que le cycle passe alors de 600
     * a 522 ticks (+15%).
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void augmentSlotRejectsNonAugments(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = placeAndGet(helper);
        int augmentSlot = machine.getAugmentSlot();

        helper.assertTrue(augmentSlot == ResonanceStabilizerBlockEntity.SLOT_AUGMENT,
            "Le slot d'augment doit etre le dernier de l'inventaire, vaut : " + augmentSlot);

        helper.assertFalse(
            machine.getInventory().isItemValid(augmentSlot, new ItemStack(Items.DIAMOND)),
            "Le slot d'augment ne doit pas accepter un objet hors du tag machine_augments");
        helper.assertFalse(
            machine.getInventory().isItemValid(augmentSlot,
                new ItemStack(ModItems.RESONANCE_COMPONENT.get())),
            "Le slot d'augment ne doit pas accepter un composant quelconque du mod");

        helper.assertTrue(machine.getEffectiveCycleTicks() == CYCLE_TICKS,
            "Sans augment le cycle doit durer " + CYCLE_TICKS + " ticks, vaut : "
                + machine.getEffectiveCycleTicks());

        helper.succeed();
    }

    // --- Utilitaires ---------------------------------------------------------

    private static ResonanceStabilizerBlockEntity placeAndGet(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_STABILIZER.get());
        return helper.getBlockEntity(MACHINE);
    }

    private static IItemHandler placeStabilizer(GameTestHelper helper) {
        return placeAndGet(helper).getInventory();
    }

    private static IItemHandler inventoryOf(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);
        return machine.getInventory();
    }

    private static int progressOf(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);
        return machine.getData().get(AbstractMachineBlockEntity.DATA_PROGRESS);
    }
}
