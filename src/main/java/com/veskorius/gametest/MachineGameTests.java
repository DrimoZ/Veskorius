package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractMachineBlock;
import com.veskorius.block.AttunementConsoleBlock;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ComponentAssemblerBlockEntity;
import com.veskorius.block.entity.CrystalCrusherBlockEntity;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.FluxPurifierBlockEntity;
import com.veskorius.block.entity.RedstoneMode;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.block.entity.ResonanceWhetstoneBlockEntity;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.entity.CrystalStriderEntity;
import com.veskorius.entity.ModEntities;
import com.veskorius.item.CodexEntries;
import com.veskorius.item.CodexFragmentItem;
import com.veskorius.item.ModItems;
import com.veskorius.item.ResonanceBlueprintItem;
import com.veskorius.item.ResonanceLocatorItem;
import com.veskorius.item.ResonanceStorageCellItem;
import com.veskorius.item.ResonanceTunerItem;
import com.veskorius.item.TunerInteractions;
import com.veskorius.item.TunerMode;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
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
     * Le slot d'augment n'accepte que le tag {@code veskorius:machine_augments} :
     * il doit refuser tout objet hors du tag, y compris les objets du mod. Le cas
     * « accepte le Catalyst Core et accelere le cycle » est couvert par
     * {@link #augmentSlotAcceptsCatalystCoreAndSpeedsUp}.
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

        // Avec des entrees valides, une recette matche : le cycle dure alors le
        // temps de la recette (600 ticks). Le temps vient de la recette, pas d'une
        // constante — donc il faut des entrees pour qu'il soit defini.
        machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
            new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
        machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
            new ItemStack(Items.QUARTZ), false);
        helper.assertTrue(machine.getEffectiveCycleTicks() == CYCLE_TICKS,
            "Sans augment le cycle doit durer " + CYCLE_TICKS + " ticks, vaut : "
                + machine.getEffectiveCycleTicks());

        helper.succeed();
    }

    /**
     * Tache 15 : le Resonance Catalyst Core est accepte dans le slot d'augment, et
     * sa presence accelere le cycle de +15% (600 -> 522 ticks, 600/1.15 arrondi).
     * L'effet vient du socle ({@code AUGMENT_SPEED_MULTIPLIER}) : ajouter l'item au
     * tag a suffi, aucun code machine touche. Couvre le critere de sortie de la
     * phase « installation d'un Catalyst Core sur une machine T1 ».
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void augmentSlotAcceptsCatalystCoreAndSpeedsUp(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = placeAndGet(helper);
        int augmentSlot = machine.getAugmentSlot();

        // Entrees valides : le temps de base (600 ticks) vient de la recette.
        machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
            new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
        machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
            new ItemStack(Items.QUARTZ), false);
        helper.assertTrue(machine.getEffectiveCycleTicks() == CYCLE_TICKS,
            "Sans augment : " + CYCLE_TICKS + " ticks, vaut " + machine.getEffectiveCycleTicks());

        // Le slot accepte le Catalyst Core, et l'insertion aboutit.
        ItemStack core = new ItemStack(ModItems.RESONANCE_CATALYST_CORE.get());
        helper.assertTrue(machine.getInventory().isItemValid(augmentSlot, core),
            "Le slot d'augment doit accepter le Resonance Catalyst Core");
        ItemStack leftover = machine.getInventory().insertItem(augmentSlot, core, false);
        helper.assertTrue(leftover.isEmpty(), "Le Catalyst Core aurait du entrer dans le slot");
        helper.assertTrue(machine.hasAugment(), "La machine devrait se voir augmentee");

        int expected = Math.round(CYCLE_TICKS / 1.15f); // 522
        helper.assertTrue(machine.getEffectiveCycleTicks() == expected,
            "Avec augment : " + expected + " ticks attendus (+15%), vaut "
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

    /**
     * Carburants data-driven : le slot n'accepte que ce que déclare une recette
     * {@code veskorius:fueling} (par défaut le Stable Crystal), et refuse le reste —
     * y compris le Raw Crystal, proche mais non enregistré. Verrouille le fait que
     * le filtre suit le registre, plus un item codé en dur.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void emitterFuelSlotFollowsRegistry(GameTestHelper helper) {
        FieldEmitterBlockEntity emitter = placeEmitter(helper);
        helper.assertTrue(
            emitter.getFuelHandler().isItemValid(FieldEmitterBlockEntity.SLOT_FUEL,
                new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get())),
            "Le Stable Crystal est un carburant enregistré : il doit être accepté");
        helper.assertFalse(
            emitter.getFuelHandler().isItemValid(FieldEmitterBlockEntity.SLOT_FUEL,
                new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get())),
            "Le Raw Crystal n'est pas un carburant enregistré : il doit être refusé");
        helper.succeed();
    }

    // --- Contrôle des machines (redstone + interrupteur manuel) --------------

    /** Interrupteur manuel coupé : la machine ne progresse pas, même tout en ordre. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void manualOffStopsMachine(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                ResonanceStabilizerBlockEntity machine = placeAndGet(helper);
                machine.setManualEnabled(false);
                machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
                machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
                    new ItemStack(Items.QUARTZ), false);
            })
            .thenExecuteAfter(100, () -> helper.assertTrue(progressOf(helper) == 0,
                "Coupée manuellement, la machine ne doit pas progresser, vaut : " + progressOf(helper)))
            .thenSucceed();
    }

    /**
     * Mode REQUIRES_SIGNAL : inerte sans redstone, démarre quand un bloc de
     * redstone est posé à côté.
     */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void redstoneRequiresSignalGatesMachine(GameTestHelper helper) {
        BlockPos leverPos = MACHINE.above();
        helper.startSequence()
            .thenExecute(() -> {
                ResonanceStabilizerBlockEntity machine = placeAndGet(helper);
                machine.setRedstoneMode(RedstoneMode.REQUIRES_SIGNAL);
                machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
                machine.getInventory().insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
                    new ItemStack(Items.QUARTZ), false);
            })
            .thenExecuteAfter(40, () -> {
                helper.assertTrue(progressOf(helper) == 0,
                    "Sans signal, REQUIRES_SIGNAL doit rester à 0, vaut : " + progressOf(helper));
                // Fournit un signal redstone adjacent.
                helper.setBlock(leverPos, Blocks.REDSTONE_BLOCK);
            })
            .thenExecuteAfter(40, () -> helper.assertTrue(progressOf(helper) > 0,
                "Avec signal, la machine aurait dû démarrer, progression : " + progressOf(helper)))
            .thenSucceed();
    }

    // --- Component Assembler (machine #2) : premier consommateur d'Osc -------

    /** Assembleur à 2 blocs de l'émetteur, dans sa portée. */
    private static final BlockPos ASSEMBLER = new BlockPos(10, 1, 12);

    /** 5 secondes (05-Machines.md #2). */
    private static final int ASSEMBLER_TICKS = 5 * 20;

    /** 3 Osc/tick × 100 ticks = 300 Osc pour un cycle complet. */
    private static final int ASSEMBLER_CYCLE_COST = 3 * ASSEMBLER_TICKS;

    /**
     * Dans un champ : produit 2 Component, consomme 1 cristal + 2 fer, et **prélève
     * réellement l'énergie** sur l'émetteur (la réserve baisse d'exactement le coût
     * d'un cycle). C'est le test qui prouve que la machine est bien branchée sur le
     * système de champ, pas seulement qu'elle tourne.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = ASSEMBLER_TICKS + 200)
    public static void assemblerRunsInFieldAndConsumesOsc(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(ASSEMBLER, ModBlocks.COMPONENT_ASSEMBLER.get());
                IItemHandler inv = assemblerInventory(helper);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
                    new ItemStack(Items.IRON_INGOT, 2), false);
            })
            .thenExecuteAfter(ASSEMBLER_TICKS + 5, () -> {
                IItemHandler inv = assemblerInventory(helper);

                ItemStack output = inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(output.is(ModItems.RESONANCE_COMPONENT.get()) && output.getCount() == 2,
                    "La sortie devrait contenir 2 Resonance Component, trouve : " + output);
                helper.assertTrue(
                    inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Le cristal stable aurait du etre consomme");
                helper.assertTrue(
                    inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_IRON).isEmpty(),
                    "Les 2 lingots de fer auraient du etre consommes");

                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000 - ASSEMBLER_CYCLE_COST,
                    "La réserve devrait avoir baissé de " + ASSEMBLER_CYCLE_COST
                        + " Osc (un cycle), vaut : " + emitter.getReserve());
            })
            .thenSucceed();
    }

    /**
     * Hors champ : la machine ne progresse pas et ne consomme aucun ingrédient.
     * Une machine consommatrice d'Osc est inerte sans champ (06-Energy.md).
     */
    @GameTest(template = EMPTY, timeoutTicks = ASSEMBLER_TICKS + 200)
    public static void assemblerIdleWithoutField(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(MACHINE, ModBlocks.COMPONENT_ASSEMBLER.get());
                IItemHandler inv = machineInventory(helper, MACHINE);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
                    new ItemStack(Items.IRON_INGOT, 2), false);
            })
            .thenExecuteAfter(ASSEMBLER_TICKS + 20, () -> {
                IItemHandler inv = machineInventory(helper, MACHINE);
                helper.assertTrue(
                    inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Sans champ, la machine ne devrait rien produire");
                helper.assertTrue(
                    !inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Sans champ, le cristal ne doit pas etre consomme");
                helper.assertTrue(machineProgress(helper, MACHINE) == 0,
                    "Sans champ, la progression doit rester à 0, vaut : "
                        + machineProgress(helper, MACHINE));
            })
            .thenSucceed();
    }

    /**
     * Coupure de courant en cours de cycle : la progression est CONSERVÉE (pause),
     * pas remise à zéro — c'est la décision de conception prise à la tâche 2, et ce
     * test la verrouille.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = ASSEMBLER_TICKS + 200)
    public static void assemblerPauseHoldsProgressOnPowerCut(GameTestHelper helper) {
        int[] progressAtCut = new int[1];
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(ASSEMBLER, ModBlocks.COMPONENT_ASSEMBLER.get());
                IItemHandler inv = assemblerInventory(helper);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
                    new ItemStack(Items.IRON_INGOT, 2), false);
            })
            .thenExecuteAfter(40, () -> {
                progressAtCut[0] = machineProgress(helper, ASSEMBLER);
                helper.assertTrue(progressAtCut[0] > 0,
                    "La machine aurait du progresser dans le champ");
                // Coupe l'alimentation en cassant l'émetteur.
                helper.destroyBlock(EMITTER);
            })
            .thenExecuteAfter(20, () -> helper.assertTrue(
                machineProgress(helper, ASSEMBLER) == progressAtCut[0],
                "La progression doit rester figée pendant la coupure, était "
                    + progressAtCut[0] + ", vaut " + machineProgress(helper, ASSEMBLER)))
            .thenSucceed();
    }

    // --- Flux Purifier (machine #5) : surchauffe -----------------------------

    private static final BlockPos PURIFIER = new BlockPos(10, 1, 8);
    private static final int PURIFIER_TICKS = 45 * 20;

    /**
     * Effets déterministes de la surchauffe : temps ÷2 (900→450) et conso ×2
     * (2→4). Testé sur les getters, sans faire tourner la machine — donc sans
     * dépendre du tirage à 20 % de perte, lui non testé automatiquement (voir la
     * note dans FluxPurifierBlockEntity).
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void purifierOverheatChangesTimeAndCost(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.FLUX_PURIFIER.get());
        FluxPurifierBlockEntity purifier = helper.getBlockEntity(MACHINE);
        // Entrees valides pour qu'une recette matche : le temps et le coût
        // viennent de la recette, pas de constantes.
        purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_CRYSTAL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
        purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_REDSTONE,
            new ItemStack(Items.REDSTONE), false);

        helper.assertTrue(purifier.supportsOverheat(),
            "Le Flux Purifier doit supporter la surchauffe");
        helper.assertTrue(purifier.getEffectiveCycleTicks() == PURIFIER_TICKS,
            "Hors surchauffe : 900 ticks, vaut " + purifier.getEffectiveCycleTicks());
        helper.assertTrue(purifier.getEffectiveOscPerTick() == 2,
            "Hors surchauffe : 2 Osc/tick, vaut " + purifier.getEffectiveOscPerTick());

        purifier.setOverheatEnabled(true);
        helper.assertTrue(purifier.getEffectiveCycleTicks() == PURIFIER_TICKS / 2,
            "En surchauffe : 450 ticks, vaut " + purifier.getEffectiveCycleTicks());
        helper.assertTrue(purifier.getEffectiveOscPerTick() == 4,
            "En surchauffe : 4 Osc/tick, vaut " + purifier.getEffectiveOscPerTick());

        helper.succeed();
    }

    /** Fonctionnement normal (hors surchauffe, donc sans perte) : produit un Refined Crystal. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = PURIFIER_TICKS + 200)
    public static void purifierProducesRefinedCrystal(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(PURIFIER, ModBlocks.FLUX_PURIFIER.get());
                IItemHandler inv = machineInventory(helper, PURIFIER);
                inv.insertItem(FluxPurifierBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
                inv.insertItem(FluxPurifierBlockEntity.SLOT_REDSTONE,
                    new ItemStack(Items.REDSTONE), false);
            })
            .thenExecuteAfter(PURIFIER_TICKS + 5, () -> {
                IItemHandler inv = machineInventory(helper, PURIFIER);
                ItemStack output = inv.getStackInSlot(FluxPurifierBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(output.is(ModItems.REFINED_RESONANCE_CRYSTAL.get()) && output.getCount() == 1,
                    "1 Refined Resonance Crystal attendu, trouve : " + output);
                helper.assertTrue(
                    inv.getStackInSlot(FluxPurifierBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Le cristal stable aurait du etre consomme");
            })
            .thenSucceed();
    }

    /**
     * En surchauffe, la machine prélève 4 Osc/tick (le double) pendant qu'elle
     * tourne. Vérifié sur 100 ticks, avant qu'un cycle (450 ticks) ne s'achève —
     * donc indépendant du tirage de perte.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200)
    public static void purifierOverheatDoublesOscDrain(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(PURIFIER, ModBlocks.FLUX_PURIFIER.get());
                FluxPurifierBlockEntity purifier = helper.getBlockEntity(PURIFIER);
                purifier.setOverheatEnabled(true);
                purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 64), false);
                purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_REDSTONE,
                    new ItemStack(Items.REDSTONE, 64), false);
            })
            .thenExecuteAfter(100, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000 - 4 * 100,
                    "Surchauffe : 400 Osc prélevés en 100 ticks, réserve vaut " + emitter.getReserve());
            })
            .thenSucceed();
    }

    // --- Crystal Crusher (machine #22) : entrée unique, sortie multiple --------

    /** 10 secondes (05-Machines.md #22). */
    private static final int CRUSHER_TICKS = 10 * 20;

    /**
     * Une seule entrée (Raw Crystal), une sortie de 3 (Resonance Dust), autonome.
     * Vérifie que le socle « traitement » gère une machine à un seul slot d'entrée
     * et un résultat de count > 1.
     */
    @GameTest(template = EMPTY, timeoutTicks = CRUSHER_TICKS + 200)
    public static void crusherProducesThreeDust(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(MACHINE, ModBlocks.CRYSTAL_CRUSHER.get());
                IItemHandler inv = machineInventory(helper, MACHINE);
                inv.insertItem(CrystalCrusherBlockEntity.SLOT_INPUT,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
            })
            .thenExecuteAfter(CRUSHER_TICKS + 5, () -> {
                IItemHandler inv = machineInventory(helper, MACHINE);
                ItemStack output = inv.getStackInSlot(CrystalCrusherBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(output.is(ModItems.RESONANCE_DUST.get()) && output.getCount() == 3,
                    "3 Resonance Dust attendus, trouve : " + output);
                helper.assertTrue(
                    inv.getStackInSlot(CrystalCrusherBlockEntity.SLOT_INPUT).isEmpty(),
                    "Le cristal brut aurait du etre consomme");
            })
            .thenSucceed();
    }

    /** Autonome : aucun champ requis, contrairement à l'Assembler ou au Purifier. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void crusherRunsWithoutField(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.CRYSTAL_CRUSHER.get());
        CrystalCrusherBlockEntity crusher = helper.getBlockEntity(MACHINE);
        crusher.getInventory().insertItem(CrystalCrusherBlockEntity.SLOT_INPUT,
            new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);

        helper.assertTrue(crusher.getEffectiveOscPerTick() == 0,
            "Le Crusher est autonome : 0 Osc/tick, vaut " + crusher.getEffectiveOscPerTick());
        helper.assertTrue(crusher.getEffectiveCycleTicks() == CRUSHER_TICKS,
            "Cycle de " + CRUSHER_TICKS + " ticks attendu, vaut " + crusher.getEffectiveCycleTicks());
        helper.succeed();
    }

    /**
     * Branche alternative de l'Assembler (04-Materials.md + tâche 2) : 3 Resonance
     * Dust + 2 Iron → 2 Component, sans une ligne de code machine. Ce test verrouille
     * la promesse « zéro code » : le slot d'entrée 0 de l'Assembler accepte la
     * poussière du seul fait de la recette JSON, exactement comme le Stable Crystal.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = ASSEMBLER_TICKS + 200)
    public static void assemblerAlternativeBranchUsesDust(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(ASSEMBLER, ModBlocks.COMPONENT_ASSEMBLER.get());
                IItemHandler inv = assemblerInventory(helper);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RESONANCE_DUST.get(), 3), false);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
                    new ItemStack(Items.IRON_INGOT, 2), false);
            })
            .thenExecuteAfter(ASSEMBLER_TICKS + 5, () -> {
                IItemHandler inv = assemblerInventory(helper);
                ItemStack output = inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(output.is(ModItems.RESONANCE_COMPONENT.get()) && output.getCount() == 2,
                    "La branche alternative devrait produire 2 Resonance Component, trouve : " + output);
                helper.assertTrue(
                    inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_CRYSTAL).isEmpty(),
                    "Les 3 poussieres auraient du etre consommees");
                helper.assertTrue(
                    inv.getStackInSlot(ComponentAssemblerBlockEntity.SLOT_IRON).isEmpty(),
                    "Les 2 lingots de fer auraient du etre consommes");
            })
            .thenSucceed();
    }

    // --- Resonance Storage Cell (item #6) : batterie portable ----------------

    /** Capacité et débit re-écrits ici (06-Energy.md) : un changement non répercuté fait échouer. */
    private static final int CELL_CAPACITY = 8000;
    private static final int CELL_CHARGE_RATE = 20;

    /**
     * Dans un champ, la cellule absorbe {@code CELL_CHARGE_RATE} Osc en un tick, et
     * ces Osc sont bien prélevés sur la réserve de l'émetteur (même source que les
     * machines, 06-Energy.md). Teste le transfert réel, pas seulement l'incrément.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void storageCellChargesFromField(GameTestHelper helper) {
        ItemStack cell = new ItemStack(ModItems.RESONANCE_STORAGE_CELL.get());
        helper.startSequence()
            // L'émetteur a besoin de quelques ticks pour s'enregistrer dans le
            // manager et brûler son cristal (réserve à 0 au tick de pose).
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(3, () -> {
                ServerLevel level = helper.getLevel();
                int drawn = ResonanceStorageCellItem.tickCharge(
                    level, helper.absolutePos(EMITTER).east(3), cell);

                helper.assertTrue(drawn == CELL_CHARGE_RATE,
                    "Un tick devrait transférer " + CELL_CHARGE_RATE + " Osc, vaut " + drawn);
                helper.assertTrue(ResonanceStorageCellItem.getCharge(cell) == CELL_CHARGE_RATE,
                    "La charge devrait valoir " + CELL_CHARGE_RATE + ", vaut "
                        + ResonanceStorageCellItem.getCharge(cell));
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000 - CELL_CHARGE_RATE,
                    "La réserve de l'émetteur aurait dû baisser de " + CELL_CHARGE_RATE
                        + ", vaut " + emitter.getReserve());
            })
            .thenSucceed();
    }

    /** Hors champ : aucune charge (pas de conversion cachée, pilier 3). */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void storageCellDoesNotChargeWithoutField(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack cell = new ItemStack(ModItems.RESONANCE_STORAGE_CELL.get());

        int drawn = ResonanceStorageCellItem.tickCharge(level, helper.absolutePos(MACHINE), cell);

        helper.assertTrue(drawn == 0, "Hors champ, rien ne devrait être transféré, vaut " + drawn);
        helper.assertTrue(ResonanceStorageCellItem.getCharge(cell) == 0,
            "La charge devrait rester à 0, vaut " + ResonanceStorageCellItem.getCharge(cell));
        helper.succeed();
    }

    /**
     * Près de la capacité, un tick ne prend que la place restante (pas le débit
     * plein) : ni dépassement de capacité, ni Osc gaspillé sur l'émetteur.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void storageCellStopsAtCapacity(GameTestHelper helper) {
        ItemStack cell = new ItemStack(ModItems.RESONANCE_STORAGE_CELL.get());
        ResonanceStorageCellItem.setCharge(cell, CELL_CAPACITY - 5);
        helper.startSequence()
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(3, () -> {
                ServerLevel level = helper.getLevel();
                int drawn = ResonanceStorageCellItem.tickCharge(
                    level, helper.absolutePos(EMITTER).east(3), cell);

                helper.assertTrue(drawn == 5,
                    "Seuls les 5 Osc de place restante devraient être pris, vaut " + drawn);
                helper.assertTrue(ResonanceStorageCellItem.getCharge(cell) == CELL_CAPACITY,
                    "La cellule devrait être pleine (" + CELL_CAPACITY + "), vaut "
                        + ResonanceStorageCellItem.getCharge(cell));
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000 - 5,
                    "Seuls 5 Osc auraient dû être prélevés sur l'émetteur, réserve vaut "
                        + emitter.getReserve());
            })
            .thenSucceed();
    }

    /**
     * {@code extractCharge} rend l'Osc demandé, borné par la charge disponible —
     * c'est le point d'entrée du futur Resonance Locator (tâche 8).
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void storageCellExtractIsClampedToCharge(GameTestHelper helper) {
        ItemStack cell = new ItemStack(ModItems.RESONANCE_STORAGE_CELL.get());
        ResonanceStorageCellItem.setCharge(cell, 100);

        helper.assertTrue(ResonanceStorageCellItem.extractCharge(cell, 30) == 30,
            "extractCharge(30) devrait rendre 30");
        helper.assertTrue(ResonanceStorageCellItem.getCharge(cell) == 70,
            "Après retrait de 30, charge = 70, vaut " + ResonanceStorageCellItem.getCharge(cell));
        helper.assertTrue(ResonanceStorageCellItem.extractCharge(cell, 100) == 70,
            "extractCharge(100) sur 70 dispo devrait rendre 70 (borné)");
        helper.assertTrue(ResonanceStorageCellItem.getCharge(cell) == 0,
            "La cellule devrait être vide, vaut " + ResonanceStorageCellItem.getCharge(cell));
        helper.succeed();
    }

    // --- Resonance Tuner (outil transversal, modes) --------------------------

    /** Mode ROTATE : fait pivoter la face de 90° (NORTH → EAST). */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void tunerRotateTurnsFacing(GameTestHelper helper) {
        BlockPos pos = MACHINE;
        helper.setBlock(pos, ModBlocks.RESONANCE_STABILIZER.get());
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);

        Direction before = level.getBlockState(abs).getValue(AbstractMachineBlock.FACING);
        boolean applied = ResonanceTunerItem.applyMode(TunerMode.ROTATE, level, abs, null);
        Direction after = level.getBlockState(abs).getValue(AbstractMachineBlock.FACING);

        helper.assertTrue(applied, "ROTATE aurait dû s'appliquer");
        helper.assertTrue(after == before.getClockWise(),
            "La face aurait dû tourner de " + before + " vers " + before.getClockWise()
                + ", vaut " + after);
        helper.succeed();
    }

    /** Mode POWER : bascule l'interrupteur manuel. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void tunerPowerTogglesManual(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = placeAndGet(helper);
        helper.assertTrue(machine.isManualEnabled(), "La machine démarre allumée");

        ResonanceTunerItem.applyMode(TunerMode.POWER, helper.getLevel(), helper.absolutePos(MACHINE), null);
        helper.assertTrue(!machine.isManualEnabled(), "POWER aurait dû couper la machine");
        helper.succeed();
    }

    /** Mode REDSTONE : fait défiler le mode de contrôle redstone. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void tunerRedstoneCyclesMode(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = placeAndGet(helper);
        helper.assertTrue(machine.getRedstoneMode() == RedstoneMode.IGNORED, "Départ en IGNORED");

        ResonanceTunerItem.applyMode(TunerMode.REDSTONE, helper.getLevel(), helper.absolutePos(MACHINE), null);
        helper.assertTrue(machine.getRedstoneMode() == RedstoneMode.REQUIRES_SIGNAL,
            "REDSTONE aurait dû passer à REQUIRES_SIGNAL, vaut " + machine.getRedstoneMode());
        helper.succeed();
    }

    /**
     * Mode OVERHEAT : bascule la surchauffe sur une machine qui la supporte
     * (Purifier), sans effet sur une machine qui ne la supporte pas (Stabilizer).
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void tunerOverheatOnlyOnSupportingMachines(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // Sur le Purifier : bascule.
        helper.setBlock(PURIFIER_AT_MACHINE, ModBlocks.FLUX_PURIFIER.get());
        FluxPurifierBlockEntity purifier = helper.getBlockEntity(PURIFIER_AT_MACHINE);
        boolean onPurifier = ResonanceTunerItem.applyMode(TunerMode.OVERHEAT, level,
            helper.absolutePos(PURIFIER_AT_MACHINE), null);
        helper.assertTrue(onPurifier && purifier.isOverheatEnabled(),
            "OVERHEAT aurait dû activer la surchauffe du Purifier");

        // Sur le Stabilizer : aucun effet.
        helper.setBlock(STABILIZER_AT, ModBlocks.RESONANCE_STABILIZER.get());
        boolean onStabilizer = ResonanceTunerItem.applyMode(TunerMode.OVERHEAT, level,
            helper.absolutePos(STABILIZER_AT), null);
        helper.assertTrue(!onStabilizer,
            "OVERHEAT ne doit rien faire sur une machine sans surchauffe");
        helper.succeed();
    }

    private static final BlockPos PURIFIER_AT_MACHINE = new BlockPos(1, 1, 1);
    private static final BlockPos STABILIZER_AT = new BlockPos(3, 1, 3);

    /**
     * Démontage : la collecte du contenu vide l'inventaire de la machine et rend
     * chaque pile. Testé directement sur {@code collectContents} (sans joueur).
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void tunerDismantleCollectsContents(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.COMPONENT_ASSEMBLER.get());
        ComponentAssemblerBlockEntity be = helper.getBlockEntity(MACHINE);
        be.getInventory().insertItem(ComponentAssemblerBlockEntity.SLOT_CRYSTAL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
        be.getInventory().insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
            new ItemStack(Items.IRON_INGOT, 2), false);

        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(MACHINE);
        List<ItemStack> contents = TunerInteractions.collectContents(level, abs, level.getBlockState(abs), be);

        helper.assertTrue(contents.size() == 2, "2 piles attendues (cristal + fer), trouve " + contents.size());
        helper.assertTrue(
            be.getInventory().getStackInSlot(ComponentAssemblerBlockEntity.SLOT_CRYSTAL).isEmpty()
                && be.getInventory().getStackInSlot(ComponentAssemblerBlockEntity.SLOT_IRON).isEmpty(),
            "L'inventaire de la machine devrait être vide après la collecte");
        helper.succeed();
    }

    /**
     * Démontage complet : le bloc est retiré et le joueur reçoit le bloc + son
     * contenu dans son inventaire.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void tunerDismantleReturnsBlockAndContents(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.COMPONENT_ASSEMBLER.get());
        ComponentAssemblerBlockEntity be = helper.getBlockEntity(MACHINE);
        be.getInventory().insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
            new ItemStack(Items.IRON_INGOT, 2), false);

        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(MACHINE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        TunerInteractions.dismantle(level, abs, player);

        helper.assertTrue(level.getBlockState(abs).isAir(),
            "Le bloc aurait dû être retiré");
        helper.assertTrue(countInInventory(player, ModBlocks.COMPONENT_ASSEMBLER.get().asItem()) == 1,
            "Le joueur aurait dû recevoir le bloc démonté");
        helper.assertTrue(countInInventory(player, Items.IRON_INGOT) == 2,
            "Le joueur aurait dû recevoir les 2 lingots de fer");
        helper.succeed();
    }

    private static int countInInventory(Player player, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    // --- Fileur de Cristal (entité #Fileur, 09-Entities.md) ------------------

    /** 5 minutes de cooldown de traite, re-écrit ici (09-Entities.md). */
    private static final int MILK_COOLDOWN = 5 * 60 * 20;

    /**
     * Traite : un clic droit à main nue sur un adulte rend 1 Raw Resonance Crystal
     * puis pose un cooldown de 5 minutes ; un second clic immédiat ne rend rien.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void striderMilkGivesCrystalThenCoolsDown(GameTestHelper helper) {
        CrystalStriderEntity strider = helper.spawn(ModEntities.CRYSTAL_STRIDER.get(), MACHINE);
        strider.setAge(0); // adulte
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        strider.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(countInInventory(player, ModItems.RAW_RESONANCE_CRYSTAL.get()) == 1,
            "La traite aurait dû donner 1 Raw Resonance Crystal");
        helper.assertTrue(strider.getMilkCooldown() == MILK_COOLDOWN,
            "Le cooldown de traite devrait être armé à " + MILK_COOLDOWN
                + ", vaut " + strider.getMilkCooldown());

        strider.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(countInInventory(player, ModItems.RAW_RESONANCE_CRYSTAL.get()) == 1,
            "Une seconde traite immédiate ne doit rien rendre (cooldown)");
        helper.succeed();
    }

    /** Reproduction : le Fileur accepte le Resonance Spore comme nourriture, rien d'autre. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void striderFoodIsResonanceSpore(GameTestHelper helper) {
        CrystalStriderEntity strider = helper.spawn(ModEntities.CRYSTAL_STRIDER.get(), MACHINE);
        helper.assertTrue(strider.isFood(new ItemStack(ModItems.RESONANCE_SPORE.get())),
            "Le Resonance Spore doit être une nourriture de reproduction");
        helper.assertFalse(strider.isFood(new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get())),
            "Le Raw Crystal ne doit pas être une nourriture");
        helper.assertFalse(strider.isFood(new ItemStack(Items.WHEAT)),
            "Le blé (nourriture d'animal vanilla) ne doit pas convenir");
        helper.succeed();
    }

    /** Le bébé issu de la reproduction est bien un autre Fileur de Cristal. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void striderBreedsIntoStrider(GameTestHelper helper) {
        CrystalStriderEntity parent = helper.spawn(ModEntities.CRYSTAL_STRIDER.get(), MACHINE);
        var baby = parent.getBreedOffspring(helper.getLevel(), parent);
        helper.assertTrue(baby instanceof CrystalStriderEntity,
            "Le bébé devrait être un Crystal Strider, trouve : " + baby);
        helper.succeed();
    }

    // --- Resonance Locator (outil #7, tâche 8) -------------------------------

    /** Un ping consomme 5 Osc de la batterie interne. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void locatorUseConsumesCharge(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack loc = new ItemStack(ModItems.RESONANCE_LOCATOR.get());
        ResonanceLocatorItem.setCharge(loc, 100);
        player.setItemInHand(InteractionHand.MAIN_HAND, loc);

        ModItems.RESONANCE_LOCATOR.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.assertTrue(ResonanceLocatorItem.getCharge(loc) == 95,
            "Un ping devrait retirer 5 Osc (100 -> 95), vaut " + ResonanceLocatorItem.getCharge(loc));
        helper.succeed();
    }

    /** Sans charge suffisante, le locator ne fait rien et ne consomme pas. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void locatorBlocksWhenEmpty(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack loc = new ItemStack(ModItems.RESONANCE_LOCATOR.get());
        ResonanceLocatorItem.setCharge(loc, 4);
        player.setItemInHand(InteractionHand.MAIN_HAND, loc);

        ModItems.RESONANCE_LOCATOR.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.assertTrue(ResonanceLocatorItem.getCharge(loc) == 4,
            "Sans charge suffisante, rien n'est consommé, vaut " + ResonanceLocatorItem.getCharge(loc));
        helper.succeed();
    }

    /** Le locator pointe la poche de cristal la plus proche (source de résonance). */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void locatorFindsNearestCrystal(GameTestHelper helper) {
        BlockPos crystal = MACHINE.offset(1, 0, 0);
        helper.setBlock(crystal, ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get());

        BlockPos found = ResonanceLocatorItem.locateForTest(helper.getLevel(), helper.absolutePos(MACHINE));
        helper.assertTrue(found != null && found.equals(helper.absolutePos(crystal)),
            "Le locator devrait pointer la poche adjacente, trouve : " + found);
        helper.succeed();
    }

    /** La direction rendue suit le vecteur (est/nord/sud/ouest = +X/-Z/+Z/-X). */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void locatorWindDirections(GameTestHelper helper) {
        helper.assertTrue("e".equals(ResonanceLocatorItem.windForTest(new Vec3(10, 0, 0))), "+X = est");
        helper.assertTrue("w".equals(ResonanceLocatorItem.windForTest(new Vec3(-10, 0, 0))), "-X = ouest");
        helper.assertTrue("n".equals(ResonanceLocatorItem.windForTest(new Vec3(0, 0, -10))), "-Z = nord");
        helper.assertTrue("s".equals(ResonanceLocatorItem.windForTest(new Vec3(0, 0, 10))), "+Z = sud");
        helper.succeed();
    }

    /** Dans un champ, la batterie interne se recharge en puisant sur l'émetteur. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void locatorRechargesFromField(GameTestHelper helper) {
        ItemStack loc = new ItemStack(ModItems.RESONANCE_LOCATOR.get());
        helper.startSequence()
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(3, () -> {
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                BlockPos near = helper.absolutePos(EMITTER).east(2);
                player.setPos(near.getX() + 0.5, near.getY(), near.getZ() + 0.5);
                ModItems.RESONANCE_LOCATOR.get().inventoryTick(loc, helper.getLevel(), player, 0, true);

                helper.assertTrue(ResonanceLocatorItem.getCharge(loc) == ResonanceLocatorItem.RECHARGE_RATE,
                    "La batterie devrait gagner " + ResonanceLocatorItem.RECHARGE_RATE
                        + " Osc dans un champ, vaut " + ResonanceLocatorItem.getCharge(loc));
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000 - ResonanceLocatorItem.RECHARGE_RATE,
                    "L'émetteur aurait dû fournir la recharge, réserve vaut " + emitter.getReserve());
            })
            .thenSucceed();
    }

    /** Hors champ, la recharge puise dans une Storage Cell portée. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void locatorRechargesFromStorageCell(GameTestHelper helper) {
        // Coin de l'arène, loin de tout émetteur (aucun posé) : pas de champ.
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos corner = helper.absolutePos(new BlockPos(1, 1, 1));
        player.setPos(corner.getX() + 0.5, corner.getY(), corner.getZ() + 0.5);

        ItemStack cell = new ItemStack(ModItems.RESONANCE_STORAGE_CELL.get());
        ResonanceStorageCellItem.setCharge(cell, 500);
        // setItem (et non add) : garde la même référence pour lire sa charge après.
        player.getInventory().setItem(0, cell);

        ItemStack loc = new ItemStack(ModItems.RESONANCE_LOCATOR.get());
        ModItems.RESONANCE_LOCATOR.get().inventoryTick(loc, helper.getLevel(), player, 0, true);

        helper.assertTrue(ResonanceLocatorItem.getCharge(loc) == ResonanceLocatorItem.RECHARGE_RATE,
            "La batterie devrait se recharger depuis la cellule, vaut " + ResonanceLocatorItem.getCharge(loc));
        helper.assertTrue(ResonanceStorageCellItem.getCharge(cell) == 500 - ResonanceLocatorItem.RECHARGE_RATE,
            "La cellule aurait dû fournir la recharge, vaut " + ResonanceStorageCellItem.getCharge(cell));
        helper.succeed();
    }

    // --- Gatekeeping T2 : blueprint, recette, console (tâche 10) -------------

    /**
     * Le blueprint est une clé **réutilisable** : le craft le rend au lieu de le
     * consommer. C'est ce qui évite qu'un seul plan trouvé ne serve qu'une fois.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void blueprintIsReturnedNotConsumed(GameTestHelper helper) {
        ItemStack bp = ResonanceBlueprintItem.of(2);
        helper.assertTrue(bp.getItem().hasCraftingRemainingItem(bp),
            "Le blueprint doit déclarer un rendu de craft");
        ItemStack remainder = bp.getItem().getCraftingRemainingItem(bp);
        helper.assertTrue(remainder.is(ModItems.RESONANCE_BLUEPRINT.get()),
            "Le rendu du craft doit être le blueprint lui-même, trouve : " + remainder);
        helper.succeed();
    }

    /**
     * La recette du Field Emitter (T2) exige le blueprint — rien n'est masqué, c'est
     * un ingrédient : sans lui la recette ne matche pas, avec lui elle produit le bloc.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void fieldEmitterRecipeRequiresBlueprint(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack c = new ItemStack(ModItems.RESONANCE_COMPONENT.get());
        ItemStack g = new ItemStack(Items.GOLD_INGOT);
        ItemStack s = new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get());
        ItemStack p = ResonanceBlueprintItem.of(2);
        ItemStack e = ItemStack.EMPTY;

        // Motif CGC / CSC / PG_ (voir ModRecipeProvider).
        CraftingInput withBlueprint = CraftingInput.of(3, 3, List.of(
            c, g, c,
            c, s, c,
            p, g, e));
        CraftingInput withoutBlueprint = CraftingInput.of(3, 3, List.of(
            c, g, c,
            c, s, c,
            e, g, e));

        helper.assertTrue(craftsFieldEmitter(level, withBlueprint),
            "Avec le blueprint, la recette doit produire un Field Emitter");
        helper.assertFalse(craftsFieldEmitter(level, withoutBlueprint),
            "Sans le blueprint, la recette ne doit pas matcher");
        helper.succeed();
    }

    private static boolean craftsFieldEmitter(ServerLevel level, CraftingInput input) {
        return level.getRecipeManager()
            .getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, level)
            .map(holder -> holder.value().getResultItem(level.registryAccess())
                .is(ModBlocks.FIELD_EMITTER.get().asItem()))
            .orElse(false);
    }

    /** La console de l'Avant-poste donne le blueprint T2 une fois, jamais en double. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void attunementConsoleGivesBlueprintOnce(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        helper.assertTrue(AttunementConsoleBlock.tryGiveBlueprint(player, 2),
            "La première interaction doit donner un blueprint");
        helper.assertTrue(countInInventory(player, ModItems.RESONANCE_BLUEPRINT.get()) == 1,
            "Le joueur devrait avoir 1 blueprint");
        helper.assertFalse(AttunementConsoleBlock.tryGiveBlueprint(player, 2),
            "Une seconde interaction ne doit pas donner de doublon");
        helper.assertTrue(countInInventory(player, ModItems.RESONANCE_BLUEPRINT.get()) == 1,
            "Le joueur devrait toujours avoir 1 seul blueprint");
        helper.succeed();
    }

    /** Un fragment de Codex porte son entrée de lore (Data Component, round-trip). */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void codexFragmentCarriesEntry(GameTestHelper helper) {
        ItemStack frag = CodexFragmentItem.of(CodexEntries.DAILY_LIFE_LAMPS);
        helper.assertTrue(CodexEntries.DAILY_LIFE_LAMPS.equals(CodexFragmentItem.entryOf(frag)),
            "Le fragment devrait porter l'entrée de Codex qu'on lui a donnée");
        helper.succeed();
    }

    // --- Configuration (VeskoriusConfig) -------------------------------------

    /**
     * Les défauts de config reproduisent EXACTEMENT les valeurs de design (celles
     * qui étaient codées en dur avant la config). Ce test échoue si un défaut est
     * changé sans mise à jour du dossier — même rôle que la réécriture des valeurs
     * de référence dans les autres tests. Vérifie aussi, indirectement, que le
     * ModConfigSpec SERVER est bien chargé quand le jeu tourne.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void configDefaultsMatchDesign(GameTestHelper helper) {
        helper.assertTrue(VeskoriusConfig.fieldEmitterRange() == 8,
            "Portée émetteur défaut 8, vaut " + VeskoriusConfig.fieldEmitterRange());
        helper.assertTrue(VeskoriusConfig.fieldEmitterCapacity() == 4000,
            "Capacité émetteur défaut 4000, vaut " + VeskoriusConfig.fieldEmitterCapacity());
        helper.assertTrue(VeskoriusConfig.storageCellCapacity() == 8000,
            "Capacité cellule défaut 8000, vaut " + VeskoriusConfig.storageCellCapacity());
        helper.assertTrue(VeskoriusConfig.storageCellChargeRate() == 20,
            "Débit cellule défaut 20, vaut " + VeskoriusConfig.storageCellChargeRate());
        helper.assertTrue(Math.abs(VeskoriusConfig.augmentSpeedMultiplier() - 1.15) < 1e-6,
            "Multiplicateur augment défaut 1.15, vaut " + VeskoriusConfig.augmentSpeedMultiplier());
        helper.assertTrue(Math.abs(VeskoriusConfig.overheatSpeedMultiplier() - 2.0) < 1e-6,
            "Facteur vitesse surchauffe défaut 2.0, vaut " + VeskoriusConfig.overheatSpeedMultiplier());
        helper.assertTrue(Math.abs(VeskoriusConfig.overheatOscMultiplier() - 2.0) < 1e-6,
            "Facteur Osc surchauffe défaut 2.0, vaut " + VeskoriusConfig.overheatOscMultiplier());
        helper.assertTrue(Math.abs(VeskoriusConfig.overheatInputLossChance() - 0.2) < 1e-6,
            "Perte surchauffe défaut 0.2, vaut " + VeskoriusConfig.overheatInputLossChance());
        helper.succeed();
    }

    // --- Utilitaires ---------------------------------------------------------

    private static IItemHandler assemblerInventory(GameTestHelper helper) {
        return machineInventory(helper, ASSEMBLER);
    }

    private static IItemHandler machineInventory(GameTestHelper helper, BlockPos pos) {
        AbstractMachineBlockEntity machine = helper.getBlockEntity(pos);
        return machine.getInventory();
    }

    private static int machineProgress(GameTestHelper helper, BlockPos pos) {
        AbstractMachineBlockEntity machine = helper.getBlockEntity(pos);
        return machine.getData().get(AbstractMachineBlockEntity.DATA_PROGRESS);
    }

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
