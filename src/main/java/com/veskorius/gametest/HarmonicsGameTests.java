package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.ComponentAssemblerBlockEntity;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.FluxPurifierBlockEntity;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.config.HarmonicsConfig;
import com.veskorius.energy.HarmonicBand;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tests du système Harmoniques & Dissonance (06-Energy.md).
 *
 * Ce qui est verrouillé ici : <b>le T1 reste simple</b> (machines universelles), le
 * désaccord <b>coûte mais ne bloque jamais</b>, une recette {@code stable} est
 * <b>increvable</b>, et la dissonance rend le champ instable puis se résorbe.
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class HarmonicsGameTests {

    private static final String EMPTY = "empty";
    private static final String FIELD_ARENA = "field_arena";

    private static final BlockPos MACHINE = new BlockPos(2, 1, 2);
    private static final BlockPos EMITTER = new BlockPos(10, 1, 10);
    private static final BlockPos PURIFIER = new BlockPos(10, 1, 8);
    private static final BlockPos ASSEMBLER = new BlockPos(10, 1, 12);

    /** Pose un Field Emitter chargé (bande Fondamentale par défaut). */
    private static void chargedEmitter(GameTestHelper helper) {
        helper.setBlock(EMITTER, ModBlocks.FIELD_EMITTER.get());
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
        emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
    }

    /**
     * Le T1 ne gagne aucune complexité : une machine est <b>universelle</b> par défaut
     * (aucune bande), ne supporte pas l'accord, et ne se désaccorde donc jamais.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void machinesAreUniversalByDefault(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_STABILIZER.get());
        ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);

        helper.assertTrue(machine.getHarmonicBand() == null,
            "Une machine doit être universelle (aucune bande) par défaut");
        helper.assertFalse(machine.supportsHarmonicBand(),
            "Une machine T1 ne doit pas supporter l'accord harmonique");
        helper.assertFalse(machine.isDetunedFrom(null),
            "Sans champ, aucune notion de désaccord");
        helper.succeed();
    }

    /** L'émetteur émet sur la Fondamentale et démarre sans aucune dissonance. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void emitterStartsCleanOnFundamental(GameTestHelper helper) {
        chargedEmitter(helper);
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
        helper.assertTrue(emitter.getBand() == HarmonicBand.FUNDAMENTAL,
            "Le Field Emitter T2 émet sur la Fondamentale");
        helper.assertTrue(emitter.getDissonance() == 0, "Un émetteur neuf est propre");
        helper.assertFalse(emitter.isUnstable(), "Un émetteur propre est stable");
        helper.succeed();
    }

    /**
     * Machine désaccordée sur une recette NON stable (le Purifier) : elle tourne quand
     * même, mais puise <b>plus d'Osc</b> et <b>injecte de la dissonance</b> dans le champ.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200)
    public static void detunedMachineCostsMoreAndFeedsDissonance(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(PURIFIER, ModBlocks.FLUX_PURIFIER.get());
                FluxPurifierBlockEntity purifier = helper.getBlockEntity(PURIFIER);
                // Simule une machine T3 accordée sur une autre bande que le champ.
                purifier.setHarmonicBand(HarmonicBand.MEDIAN);
                purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 64), false);
                purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_REDSTONE,
                    new ItemStack(Items.REDSTONE, 64), false);
            })
            .thenExecuteAfter(100, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                FluxPurifierBlockEntity purifier = helper.getBlockEntity(PURIFIER);

                helper.assertTrue(purifier.isDetunedFrom(emitter),
                    "Bande MEDIAN dans un champ FUNDAMENTAL = désaccordée");
                // Base = 2 Osc/tick ; désaccordée = 2 x 1.5 = 3. On vérifie qu'on a
                // consommé strictement plus que le tarif de base sur la fenêtre.
                int baseDrain = 2 * 100;
                helper.assertTrue(emitter.getReserve() < 4000 - baseDrain,
                    "Le désaccord doit coûter plus que le tarif de base, réserve : "
                        + emitter.getReserve());
                helper.assertTrue(emitter.getDissonance() > 0,
                    "Une machine désaccordée doit injecter de la dissonance, vaut : "
                        + emitter.getDissonance());
                // Et surtout : elle n'est PAS bloquée.
                helper.assertTrue(purifier.getData().get(
                        com.veskorius.block.entity.AbstractMachineBlockEntity.DATA_PROGRESS) > 0,
                    "Le désaccord ne doit JAMAIS bloquer la machine");
            })
            .thenSucceed();
    }

    /**
     * Une recette marquée {@code stable} est increvable : même désaccordée, la machine
     * paie le tarif normal et ne produit aucune dissonance. C'est la garantie donnée au
     * début de partie (toutes les recettes T1 sont stable).
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200)
    public static void stableRecipeIgnoresDetune(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(ASSEMBLER, ModBlocks.COMPONENT_ASSEMBLER.get());
                ComponentAssemblerBlockEntity assembler = helper.getBlockEntity(ASSEMBLER);
                assembler.setHarmonicBand(HarmonicBand.MEDIAN);
                assembler.getInventory().insertItem(ComponentAssemblerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 64), false);
                assembler.getInventory().insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
                    new ItemStack(Items.IRON_INGOT, 64), false);
            })
            .thenExecuteAfter(100, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                ComponentAssemblerBlockEntity assembler = helper.getBlockEntity(ASSEMBLER);

                helper.assertFalse(assembler.isDetunedFrom(emitter),
                    "Une recette stable immunise contre le désaccord");
                helper.assertTrue(emitter.getDissonance() == 0,
                    "Une recette stable ne doit produire AUCUNE dissonance, vaut : "
                        + emitter.getDissonance());
            })
            .thenSucceed();
    }

    /**
     * Au-delà du seuil, le champ devient instable (il saute des ticks : la machine
     * hoquette visiblement). Sous le seuil, il reste sain.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void dissonanceMakesFieldUnstable(GameTestHelper helper) {
        chargedEmitter(helper);
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);

        int capacity = HarmonicsConfig.dissonanceCapacity();
        int threshold = (int) (capacity * HarmonicsConfig.dissonanceUnstableThreshold());

        emitter.addDissonance(threshold - 1);
        helper.assertFalse(emitter.isUnstable(),
            "Sous le seuil, le champ reste stable (dissonance " + emitter.getDissonance() + ")");

        emitter.addDissonance(2);
        helper.assertTrue(emitter.isUnstable(),
            "Au-dessus du seuil, le champ doit devenir instable (dissonance "
                + emitter.getDissonance() + ")");

        // La dissonance est bornée par la capacité (pas d'accumulation infinie).
        emitter.addDissonance(capacity * 10);
        helper.assertTrue(emitter.getDissonance() == capacity,
            "La dissonance doit être plafonnée à la capacité, vaut : " + emitter.getDissonance());
        helper.succeed();
    }
}
