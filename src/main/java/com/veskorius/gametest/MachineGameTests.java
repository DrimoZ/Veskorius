package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.block.entity.ResonanceWhetstoneBlockEntity;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
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
    private static final String FIELD_ARENA = "field_arena";

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

    // --- Resonance Whetstone (machine #3) ------------------------------------

    /** 8 secondes (05-Machines.md #3). */
    private static final int WHETSTONE_TICKS = 8 * 20;

    /**
     * Le Whetstone ne fabrique rien : il repare son entree de 25% de la
     * durabilite maximale et la deplace en sortie. C'est le cas qui verifie que
     * le socle ne suppose pas "entrees consommees -> nouvel objet produit".
     */
    @GameTest(template = EMPTY, timeoutTicks = WHETSTONE_TICKS + 200)
    public static void whetstoneRepairsToolByAQuarter(GameTestHelper helper) {
        ItemStack damaged = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = damaged.getMaxDamage();
        int initialDamage = maxDamage - 10;

        helper.startSequence()
            .thenExecute(() -> {
                IItemHandler inventory = placeWhetstone(helper);
                damaged.setDamageValue(initialDamage);
                inventory.insertItem(ResonanceWhetstoneBlockEntity.SLOT_TOOL, damaged, false);
                inventory.insertItem(ResonanceWhetstoneBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
            })
            .thenExecuteAfter(WHETSTONE_TICKS + 5, () -> {
                IItemHandler inventory = whetstoneInventory(helper);
                ItemStack repaired = inventory.getStackInSlot(ResonanceWhetstoneBlockEntity.SLOT_OUTPUT);

                helper.assertTrue(repaired.is(Items.IRON_PICKAXE),
                    "L'outil repare devrait etre en sortie, trouve : " + repaired);

                int expected = initialDamage - maxDamage / 4;
                helper.assertTrue(repaired.getDamageValue() == expected,
                    "Degats attendus " + expected + ", trouve : " + repaired.getDamageValue());

                helper.assertTrue(
                    inventory.getStackInSlot(ResonanceWhetstoneBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Le cristal stable aurait du etre consomme");
                helper.assertTrue(
                    inventory.getStackInSlot(ResonanceWhetstoneBlockEntity.SLOT_TOOL).isEmpty(),
                    "L'outil ne devrait plus etre dans le slot d'entree");
            })
            .thenSucceed();
    }

    /** Un outil intact ne doit pas consommer de cristal. */
    @GameTest(template = EMPTY, timeoutTicks = WHETSTONE_TICKS + 200)
    public static void whetstoneIgnoresUndamagedTool(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                IItemHandler inventory = placeWhetstone(helper);
                inventory.insertItem(ResonanceWhetstoneBlockEntity.SLOT_TOOL,
                    new ItemStack(Items.IRON_PICKAXE), false);
                inventory.insertItem(ResonanceWhetstoneBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
            })
            .thenExecuteAfter(WHETSTONE_TICKS + 5, () -> {
                IItemHandler inventory = whetstoneInventory(helper);
                helper.assertTrue(
                    !inventory.getStackInSlot(ResonanceWhetstoneBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Un outil intact ne doit pas consommer de cristal");
                helper.assertTrue(
                    inventory.getStackInSlot(ResonanceWhetstoneBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Aucune sortie ne devrait etre produite pour un outil intact");
            })
            .thenSucceed();
    }

    /** La reparation ne doit jamais depasser l'outil neuf. */
    @GameTest(template = EMPTY, timeoutTicks = WHETSTONE_TICKS + 200)
    public static void whetstoneDoesNotOverRepair(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                IItemHandler inventory = placeWhetstone(helper);
                ItemStack barelyDamaged = new ItemStack(Items.IRON_PICKAXE);
                barelyDamaged.setDamageValue(1);
                inventory.insertItem(ResonanceWhetstoneBlockEntity.SLOT_TOOL, barelyDamaged, false);
                inventory.insertItem(ResonanceWhetstoneBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
            })
            .thenExecuteAfter(WHETSTONE_TICKS + 5, () -> {
                ItemStack repaired = whetstoneInventory(helper)
                    .getStackInSlot(ResonanceWhetstoneBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(repaired.getDamageValue() == 0,
                    "Les degats ne doivent pas passer sous zero, trouve : " + repaired.getDamageValue());
            })
            .thenSucceed();
    }

    /** Le slot d'entree n'accepte que des objets endommageables. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void whetstoneToolSlotRejectsNonTools(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_WHETSTONE.get());
        ResonanceWhetstoneBlockEntity machine = helper.getBlockEntity(MACHINE);

        helper.assertFalse(
            machine.getInventory().isItemValid(ResonanceWhetstoneBlockEntity.SLOT_TOOL,
                new ItemStack(Items.COBBLESTONE)),
            "Le slot outil ne doit pas accepter un bloc non endommageable");
        helper.assertTrue(
            machine.getInventory().isItemValid(ResonanceWhetstoneBlockEntity.SLOT_TOOL,
                new ItemStack(Items.IRON_PICKAXE)),
            "Le slot outil doit accepter une pioche");

        helper.succeed();
    }

    // --- Field Emitter + système de champ (machine #4) -----------------------

    /**
     * Émetteur au CENTRE de l'arène 21x21 (et non dans un coin comme MACHINE dans
     * le petit template) : les tests sondent jusqu'à 9 blocs autour, il faut donc
     * de la marge de tous les côtés pour rester dans l'arène et hors de portée des
     * émetteurs des tests voisins. Voir ModStructureTemplateProvider.
     */
    private static final BlockPos EMITTER = new BlockPos(10, 1, 10);

    /**
     * Recharge : insérer un Stable Crystal fait monter la réserve de 4000 Osc, et
     * le cristal est consommé (06-Energy.md, source primaire de l'énergie).
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void emitterBurnsCrystalToFillReserve(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                FieldEmitterBlockEntity emitter = placeEmitter(helper);
                helper.assertTrue(emitter.getReserve() == 0, "La réserve devrait démarrer à 0");
                emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
            })
            .thenExecuteAfter(3, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000,
                    "La réserve devrait être à 4000 après avoir brûlé un cristal, vaut : "
                        + emitter.getReserve());
                helper.assertTrue(
                    emitter.getFuelHandler().getStackInSlot(FieldEmitterBlockEntity.SLOT_FUEL).isEmpty(),
                    "Le cristal aurait dû être consommé");
            })
            .thenSucceed();
    }

    /** Un émetteur ne brûle pas de cristal tant que sa réserve est pleine (pas de gaspillage). */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void emitterDoesNotWasteFuelWhenFull(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                FieldEmitterBlockEntity emitter = placeEmitter(helper);
                emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 2), false);
            })
            .thenExecuteAfter(10, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000,
                    "La réserve ne doit pas dépasser 4000, vaut : " + emitter.getReserve());
                helper.assertTrue(
                    emitter.getFuelHandler().getStackInSlot(FieldEmitterBlockEntity.SLOT_FUEL).getCount() == 1,
                    "Un seul cristal aurait dû être brûlé, l'autre reste en réserve de carburant");
            })
            .thenSucceed();
    }

    /**
     * Portée : une position à 8 blocs est couverte, une position à 9 ne l'est pas.
     * Teste le routage du manager, pas seulement l'émetteur.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void fieldReachesEightBlocksNotNine(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(3, () -> {
                ServerLevel level = helper.getLevel();
                BlockPos emitterAbs = helper.absolutePos(EMITTER);

                helper.assertTrue(
                    ResonanceFieldManager.supply(level, emitterAbs.east(8), 10) == 10,
                    "Une position à 8 blocs devrait être alimentée");
                helper.assertTrue(
                    ResonanceFieldManager.supply(level, emitterAbs.east(9), 10) == 0,
                    "Une position à 9 blocs ne devrait PAS être alimentée");
            })
            .thenSucceed();
    }

    /** La réserve s'épuise : une fois vidée, le champ ne fournit plus rien. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void fieldStopsSupplyingWhenReserveEmpty(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(3, () -> {
                ServerLevel level = helper.getLevel();
                BlockPos consumer = helper.absolutePos(EMITTER).east(3);

                int first = ResonanceFieldManager.supply(level, consumer, 4000);
                helper.assertTrue(first == 4000,
                    "Le premier prélèvement devrait vider la réserve (4000), vaut : " + first);

                int second = ResonanceFieldManager.supply(level, consumer, 10);
                helper.assertTrue(second == 0,
                    "Réserve vide : plus rien à fournir, vaut : " + second);
            })
            .thenSucceed();
    }

    /**
     * Anti-stacking (06-Energy.md) : deux émetteurs couvrant la même position ne
     * fournissent pas le double. Une demande est servie par un seul émetteur.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void overlappingEmittersDoNotStack(GameTestHelper helper) {
        // Second émetteur à deux blocs à l'est du premier (au centre de l'arène).
        BlockPos secondEmitter = EMITTER.east(2);
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(secondEmitter, ModBlocks.FIELD_EMITTER.get());
                FieldEmitterBlockEntity e2 = helper.getBlockEntity(secondEmitter);
                e2.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
            })
            .thenExecuteAfter(3, () -> {
                ServerLevel level = helper.getLevel();
                // Position entre les deux émetteurs, couverte par les deux.
                BlockPos consumer = helper.absolutePos(EMITTER.east(1));

                // Demande de 6000 : un seul émetteur (réserve 4000) répond, donc 4000,
                // jamais 8000. L'intensité ne s'additionne pas.
                int drawn = ResonanceFieldManager.supply(level, consumer, 6000);
                helper.assertTrue(drawn == 4000,
                    "Un seul émetteur devrait servir (4000), pas la somme des deux, vaut : " + drawn);
            })
            .thenSucceed();
    }

    /** Un émetteur cassé sort de l'index : plus aucun champ à sa position. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void brokenEmitterLeavesNoField(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(3, () -> {
                ServerLevel level = helper.getLevel();
                BlockPos consumer = helper.absolutePos(EMITTER).east(3);
                helper.assertTrue(ResonanceFieldManager.supply(level, consumer, 10) == 10,
                    "Le champ devrait être actif avant de casser l'émetteur");

                helper.destroyBlock(EMITTER);
                helper.assertTrue(ResonanceFieldManager.supply(level, consumer, 10) == 0,
                    "Après destruction, plus aucun champ ne devrait couvrir la position");
            })
            .thenSucceed();
    }

    // --- Utilitaires ---------------------------------------------------------

    private static ResonanceStabilizerBlockEntity placeAndGet(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_STABILIZER.get());
        return helper.getBlockEntity(MACHINE);
    }

    private static IItemHandler placeStabilizer(GameTestHelper helper) {
        return placeAndGet(helper).getInventory();
    }

    private static FieldEmitterBlockEntity placeEmitter(GameTestHelper helper) {
        helper.setBlock(EMITTER, ModBlocks.FIELD_EMITTER.get());
        return helper.getBlockEntity(EMITTER);
    }

    /** Pose un Field Emitter et le charge d'un cristal, prêt à fournir. */
    private static void chargedEmitter(GameTestHelper helper) {
        FieldEmitterBlockEntity emitter = placeEmitter(helper);
        emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
    }

    private static IItemHandler placeWhetstone(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_WHETSTONE.get());
        ResonanceWhetstoneBlockEntity machine = helper.getBlockEntity(MACHINE);
        return machine.getInventory();
    }

    private static IItemHandler whetstoneInventory(GameTestHelper helper) {
        ResonanceWhetstoneBlockEntity machine = helper.getBlockEntity(MACHINE);
        return machine.getInventory();
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
