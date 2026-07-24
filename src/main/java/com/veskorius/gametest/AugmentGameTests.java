package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.config.MachinesConfig;
import com.veskorius.config.MachinesConfig.AugmentStacking;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tests des augments multi-slots + règles de cumul (05-Machines.md, A9).
 *
 * <p>Ce qui est verrouillé ici : le <b>défaut est strictement l'ancien comportement</b>
 * (un seul slot actif), les slots réservés au-delà <b>refusent</b> les objets, et la
 * <b>règle de cumul</b> est une fonction pure et correcte. Le cumul « en direct » (2 slots
 * actifs) n'est pas testable ici — la config par défaut n'expose qu'un slot — d'où le test
 * de la fonction pure, qui est le cœur de la règle.
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class AugmentGameTests {

    private static final String EMPTY = "empty";
    private static final BlockPos MACHINE = new BlockPos(2, 1, 2);

    /** La règle de cumul, isolée : interdit = 1, plafonné = min(n, cap), libre = n. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void stackingRuleIsPure(GameTestHelper helper) {
        helper.assertTrue(MachinesConfig.effectiveStack(0, AugmentStacking.FREE, 2) == 0,
            "Aucun augment = 0, quel que soit le mode");
        helper.assertTrue(MachinesConfig.effectiveStack(3, AugmentStacking.FORBID, 2) == 1,
            "FORBID plafonne à 1");
        helper.assertTrue(MachinesConfig.effectiveStack(3, AugmentStacking.CAPPED, 2) == 2,
            "CAPPED plafonne au cap");
        helper.assertTrue(MachinesConfig.effectiveStack(1, AugmentStacking.CAPPED, 2) == 1,
            "CAPPED sous le cap = le nombre réel");
        helper.assertTrue(MachinesConfig.effectiveStack(4, AugmentStacking.FREE, 2) == 4,
            "FREE ne plafonne pas");
        helper.succeed();
    }

    /**
     * Défaut = ancien comportement exact : un seul slot d'augment actif, mais
     * {@code MAX_AUGMENT_SLOTS} réservés en coulisse pour ne jamais avoir à refactorer.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void defaultIsOneActiveSlot(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_STABILIZER.get());
        ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);

        helper.assertTrue(machine.getActiveAugmentSlots() == 1,
            "Par défaut, un seul slot d'augment actif (comportement historique)");
        helper.assertTrue(machine.getMaxAugmentSlots() == MachinesConfig.MAX_AUGMENT_SLOTS,
            "Le socle réserve MAX_AUGMENT_SLOTS slots");
        // L'inventaire réel = slots déclarés (moins l'augment) + MAX augments réservés.
        int expected = machine.getAugmentSlot() + MachinesConfig.MAX_AUGMENT_SLOTS;
        helper.assertTrue(machine.getInventory().getSlots() == expected,
            "L'inventaire doit réserver les slots d'augment, taille : "
                + machine.getInventory().getSlots());
        helper.succeed();
    }

    /** Un augment dans le slot actif compte ; le socle applique bien l'effet. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void activeAugmentCounts(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_STABILIZER.get());
        ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);

        machine.getInventory().setStackInSlot(machine.getAugmentSlot(),
            new ItemStack(ModItems.RESONANCE_CATALYST_CORE.get()));
        helper.assertTrue(machine.hasAugment(), "Un augment dans le slot actif doit compter");
        helper.assertTrue(machine.countAugments() == 1, "Un seul augment posé");
        helper.assertTrue(machine.effectiveAugmentCount() == 1,
            "Un augment, effectif = 1 (quelle que soit la règle)");
        helper.succeed();
    }

    /**
     * Le second slot d'augment est <b>réservé mais inactif</b> par défaut : il refuse
     * l'insertion (via {@code isItemValid}). C'est la garantie que « N slots » ne fuit pas
     * au-delà du nombre configuré.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void reservedSlotRejectsItems(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_STABILIZER.get());
        ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);

        int reserved = machine.getAugmentSlot() + 1; // 2e slot d'augment, inactif au défaut
        helper.assertFalse(machine.isActiveAugmentSlot(reserved),
            "Le 2e slot d'augment doit être inactif au défaut");
        ItemStack core = new ItemStack(ModItems.RESONANCE_CATALYST_CORE.get());
        ItemStack leftover = machine.getInventory().insertItem(reserved, core, false);
        helper.assertTrue(leftover.getCount() == 1,
            "Un slot d'augment réservé (inactif) doit refuser l'objet");
        helper.assertTrue(machine.getInventory().getStackInSlot(reserved).isEmpty(),
            "Rien ne doit entrer dans un slot d'augment inactif");
        helper.succeed();
    }
}
