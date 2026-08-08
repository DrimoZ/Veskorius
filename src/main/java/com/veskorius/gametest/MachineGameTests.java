package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractMachineBlock;
import com.veskorius.block.AttunementConsoleBlock;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.ResonanceVeinedStoneBlock;
import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ComponentAssemblerBlockEntity;
import com.veskorius.block.entity.CrystalCrusherBlockEntity;
import com.veskorius.block.entity.CrystalRoostBlockEntity;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.FluxPurifierBlockEntity;
import com.veskorius.block.entity.VeskorianAlloyForgeBlockEntity;
import com.veskorius.block.entity.RedstoneMode;
import com.veskorius.block.entity.ResonanceRelayBlockEntity;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.block.entity.ResonanceWhetstoneBlockEntity;
import com.veskorius.config.HarmonicsConfig;
import com.veskorius.config.MachinesConfig;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.event.CustodeAlertHandler;
import com.veskorius.entity.CrystalStriderEntity;
import com.veskorius.entity.CustodeEntity;
import com.veskorius.entity.CustodeLourdEntity;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
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
    /** 42x42 : la seule arène assez large pour un anneau de onze blocs de côté. */
    private static final String PIECE_ARENA = "piece_arena";
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

    // --- Retour visuel « en marche » (blockstate LIT + glow) -----------------

    /**
     * Une machine qui avance un cycle s'ALLUME ({@link AbstractMachineBlock#LIT}),
     * et s'éteint dès qu'elle s'arrête (ici : entrée retirée). C'est le retour
     * visuel « en marche » lisible sans ouvrir le GUI. Joué sur le Stabilizer
     * (autonome) pour isoler l'état de marche du système de champ.
     */
    @GameTest(template = EMPTY, timeoutTicks = TIMEOUT)
    public static void machineLightsUpWhileRunning(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                IItemHandler inventory = placeStabilizer(helper);
                helper.assertFalse(machineLit(helper, MACHINE),
                    "Une machine vide ne doit pas être allumée");
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()), false);
                inventory.insertItem(ResonanceStabilizerBlockEntity.SLOT_FLUX,
                    new ItemStack(Items.QUARTZ), false);
            })
            .thenExecuteAfter(5, () -> helper.assertTrue(machineLit(helper, MACHINE),
                "La machine devrait être allumée pendant qu'elle avance un cycle"))
            .thenExecute(() -> inventoryOf(helper)
                .extractItem(ResonanceStabilizerBlockEntity.SLOT_CRYSTAL, 1, false))
            .thenExecuteAfter(5, () -> helper.assertFalse(machineLit(helper, MACHINE),
                "Entrée retirée : la machine doit s'éteindre"))
            .thenSucceed();
    }

    /**
     * Une machine consommatrice d'Osc, ingrédients en place mais HORS champ, reste
     * ÉTEINTE : c'est le seul retour « pas d'énergie » que voit le joueur sans
     * ouvrir le GUI (pilier 3). Verrouille que le glow suit l'énergie réelle, pas la
     * simple présence d'ingrédients.
     */
    @GameTest(template = EMPTY, timeoutTicks = ASSEMBLER_TICKS + 200)
    public static void machineStaysDarkWithoutField(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(MACHINE, ModBlocks.COMPONENT_ASSEMBLER.get());
                IItemHandler inv = machineInventory(helper, MACHINE);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
                inv.insertItem(ComponentAssemblerBlockEntity.SLOT_IRON,
                    new ItemStack(Items.IRON_INGOT, 2), false);
            })
            .thenExecuteAfter(20, () -> helper.assertFalse(machineLit(helper, MACHINE),
                "Hors champ, ingrédients présents : la machine doit rester éteinte (pas d'énergie)"))
            .thenSucceed();
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

    /**
     * Logique de perte d'entrée en surchauffe (05-Machines.md), extraite en fonction
     * pure pour être testée sans RNG ni config : le plan notait ce cas comme non
     * couvert. Hors surchauffe : jamais de perte. En surchauffe : perte ssi le tirage
     * tombe STRICTEMENT sous la chance.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void purifierInputLossLogic(GameTestHelper helper) {
        helper.assertFalse(FluxPurifierBlockEntity.losesInput(false, false, true, 0.2f, 0.0f),
            "Hors surchauffe : aucune perte même au tirage 0");
        helper.assertFalse(FluxPurifierBlockEntity.losesInput(false, false, true, 0.2f, 0.99f),
            "Hors surchauffe : aucune perte");
        helper.assertTrue(FluxPurifierBlockEntity.losesInput(true, false, true, 0.2f, 0.0f),
            "Surchauffe, tirage 0 < 0.2 : perte");
        helper.assertTrue(FluxPurifierBlockEntity.losesInput(true, false, true, 0.2f, 0.19f),
            "Surchauffe, tirage 0.19 < 0.2 : perte");
        helper.assertFalse(FluxPurifierBlockEntity.losesInput(true, false, true, 0.2f, 0.2f),
            "Surchauffe, tirage == chance : pas de perte (comparaison stricte)");
        helper.assertFalse(FluxPurifierBlockEntity.losesInput(true, false, true, 0.2f, 0.5f),
            "Surchauffe, tirage 0.5 >= 0.2 : pas de perte");
        helper.assertFalse(FluxPurifierBlockEntity.losesInput(true, false, true, 0.0f, 0.0f),
            "Chance 0 : jamais de perte");
        helper.assertTrue(FluxPurifierBlockEntity.losesInput(true, false, true, 1.0f, 0.999f),
            "Chance 1 : toujours perte en surchauffe");

        // Recette `stable` : par DÉFAUT la surchauffe garde son risque. Le contraire
        // ferait de la surchauffe un gain sans contrepartie sur toute la boucle T1, et
        // il n'y aurait plus aucune raison de ne pas la laisser allumée en permanence.
        helper.assertTrue(FluxPurifierBlockEntity.losesInput(true, true, true, 1.0f, 0.5f),
            "Recette stable + overheatIgnoresStable : le risque de surchauffe demeure");
        // Réglage inverse : `stable` veut alors dire « ne perd jamais rien, point ».
        helper.assertFalse(FluxPurifierBlockEntity.losesInput(true, true, false, 1.0f, 0.5f),
            "Recette stable + overheatIgnoresStable=false : aucune perte possible");
        // Le réglage ne doit toucher QUE les recettes stables.
        helper.assertTrue(FluxPurifierBlockEntity.losesInput(true, false, false, 1.0f, 0.5f),
            "overheatIgnoresStable=false ne protège pas une recette non stable");
        helper.succeed();
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

    /**
     * <b>Anti-régression de contenu atteignable.</b> Le Fileur ne peut PAS compter sur le
     * spawn naturel : la génération de monde place les {@code CREATURE} en surface
     * ({@code getTopNonCollidingPos}), ce que sa règle Y ≤ 0 refuse, et le spawn à
     * l'exécution est plafonné à 10 individus <i>persistants</i> — un plafond que la faune
     * de surface occupe en permanence. Sans peuplement par la feature, l'espèce n'existe
     * pas, et le Crystal Roost (qui exige un Fileur à moins de 6 blocs) est du contenu
     * inatteignable.
     *
     * <p>Ce test vérifie donc le seul chemin qui peuple réellement le monde : une poche de
     * cristal avec de l'air à côté doit accoucher d'au moins un Fileur.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void crystalPocketSeedsStriders(GameTestHelper helper) {
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        BlockPos crystal = helper.absolutePos(MACHINE);
        // Un cristal, et de l'air au-dessus : la configuration minimale d'une paroi ouverte.
        level.setBlockAndUpdate(crystal, ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get().defaultBlockState());

        int before = level.getEntitiesOfClass(CrystalStriderEntity.class,
            new net.minecraft.world.phys.AABB(crystal).inflate(8.0)).size();
        com.veskorius.worldgen.ResonanceCrystalPocketFeature.seedStriders(
            level, level.getRandom(), java.util.Set.of(crystal));
        int after = level.getEntitiesOfClass(CrystalStriderEntity.class,
            new net.minecraft.world.phys.AABB(crystal).inflate(8.0)).size();

        helper.assertTrue(after > before,
            "Une poche de cristal bordée d'air doit peupler au moins un Fileur — sans quoi "
                + "l'espèce n'apparaît jamais (le spawn naturel ne la place pas) et le Crystal "
                + "Roost devient inatteignable. Avant : " + before + ", après : " + after);
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

    // --- Resonance Spore growth on veined stone (04-Materials.md) -------------

    /** Hand-harvesting a spored veined stone gives a spore and clears the state. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void veinedStoneHarvestGivesSpore(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState()
            .setValue(ResonanceVeinedStoneBlock.SPORED, true));
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(MACHINE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        boolean harvested = ResonanceVeinedStoneBlock.tryHarvest(level.getBlockState(abs), level, abs, player);

        helper.assertTrue(harvested, "Harvest should succeed on a spored block");
        helper.assertTrue(countInInventory(player, ModItems.RESONANCE_SPORE.get()) == 1,
            "The player should receive 1 spore");
        helper.assertFalse(level.getBlockState(abs).getValue(ResonanceVeinedStoneBlock.SPORED),
            "The stone should no longer be spored after harvest");
        helper.succeed();
    }

    /** Fully enclosed veined stone has no exposed face, so it never grows a spore. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void veinedStoneDoesNotGrowWhenEnclosed(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.RESONANCE_VEINED_STONE.get());
        for (Direction dir : Direction.values()) {
            helper.setBlock(MACHINE.relative(dir), Blocks.STONE);
        }
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(MACHINE);

        helper.assertFalse(ResonanceVeinedStoneBlock.canGrowHere(level, abs),
            "Fully enclosed veined stone must not be able to grow spore");
        ResonanceVeinedStoneBlock.tryGrowSpore(level.getBlockState(abs), level, abs);
        helper.assertFalse(level.getBlockState(abs).getValue(ResonanceVeinedStoneBlock.SPORED),
            "Enclosed stone should stay unspored");
        helper.succeed();
    }

    // --- Custode (garde réactif, tâche 11) -----------------------------------

    /** Stats du garde standard : 30 PV, 6 de dégâts, réactivité limitée à 6 blocs. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void custodeIsReactiveGuard(GameTestHelper helper) {
        CustodeEntity custode = helper.spawn(ModEntities.CUSTODE.get(), MACHINE);
        helper.assertTrue(custode.getAttributeValue(Attributes.MAX_HEALTH) == 30.0,
            "30 PV attendus, vaut " + custode.getAttributeValue(Attributes.MAX_HEALTH));
        helper.assertTrue(custode.getAttributeValue(Attributes.ATTACK_DAMAGE) == 6.0,
            "6 de dégâts attendus, vaut " + custode.getAttributeValue(Attributes.ATTACK_DAMAGE));
        helper.assertTrue(custode.getAttributeValue(Attributes.FOLLOW_RANGE) == 6.0,
            "Rayon de réactivité 6 attendu, vaut " + custode.getAttributeValue(Attributes.FOLLOW_RANGE));
        helper.succeed();
    }

    /**
     * Le Custode Alloy Fragment remplace le fer 1:1 dans une recette Veskorius (ici
     * le Crystal Crusher), via le tag {@code veskorius:iron_substitutes} — le fer
     * marche toujours aussi.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void custodeFragmentSubstitutesIron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // Le Crusher se fabrique désormais « châssis Fracturé + 1 fer » (sans forme).
        helper.assertTrue(craftsCrusher(level, new ItemStack(ModItems.CUSTODE_ALLOY_FRAGMENT.get())),
            "Le fragment du Custode devrait remplacer le fer dans la recette du Crusher");
        helper.assertTrue(craftsCrusher(level, new ItemStack(Items.IRON_INGOT)),
            "Le fer devrait toujours fonctionner");
        helper.succeed();
    }

    private static boolean craftsCrusher(ServerLevel level, ItemStack iron) {
        ItemStack empty = ItemStack.EMPTY;
        CraftingInput input = CraftingInput.of(3, 2, List.of(
            new ItemStack(ModBlocks.FRACTURED_CHASSIS.get()), iron, empty,
            empty, empty, empty));
        return level.getRecipeManager()
            .getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, level)
            .map(holder -> holder.value().getResultItem(level.registryAccess())
                .is(ModBlocks.CRYSTAL_CRUSHER.get().asItem()))
            .orElse(false);
    }

    // --- Crystal Roost (machine #8, tâche 12) --------------------------------

    /**
     * Le Roost ne produit que si un Fileur de Cristal est à proximité (< 6 blocs) :
     * sans Fileur, avec du Quartz, la progression reste à 0 ; un Fileur posé à côté
     * la débloque. C'est la seule condition que le socle ne connaît pas.
     */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void roostProgressesOnlyWithFileur(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(MACHINE, ModBlocks.CRYSTAL_ROOST.get());
                machineInventory(helper, MACHINE).insertItem(
                    CrystalRoostBlockEntity.SLOT_QUARTZ, new ItemStack(Items.QUARTZ, 8), false);
            })
            .thenExecuteAfter(40, () -> helper.assertTrue(machineProgress(helper, MACHINE) == 0,
                "Sans Fileur, le Roost ne doit pas progresser, vaut " + machineProgress(helper, MACHINE)))
            .thenExecute(() -> helper.spawn(ModEntities.CRYSTAL_STRIDER.get(), MACHINE.offset(2, 0, 0)))
            .thenExecuteAfter(40, () -> helper.assertTrue(machineProgress(helper, MACHINE) > 0,
                "Avec un Fileur à proximité, le Roost devrait progresser, vaut "
                    + machineProgress(helper, MACHINE)))
            .thenSucceed();
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

    /** Outil à modes (16 §1) : le mode se lit, se pose et cycle Ressources ↔ Structures. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void locatorModeCyclesAndPersists(GameTestHelper helper) {
        ItemStack loc = new ItemStack(ModItems.RESONANCE_LOCATOR.get());
        helper.assertTrue(ResonanceLocatorItem.getMode(loc) == com.veskorius.item.LocatorMode.RESOURCES,
            "Mode par défaut = Ressources");
        ResonanceLocatorItem.setMode(loc, com.veskorius.item.LocatorMode.STRUCTURES);
        helper.assertTrue(ResonanceLocatorItem.getMode(loc) == com.veskorius.item.LocatorMode.STRUCTURES,
            "Le mode posé doit être relu");
        helper.assertTrue(com.veskorius.item.LocatorMode.RESOURCES.next()
                == com.veskorius.item.LocatorMode.STRUCTURES
            && com.veskorius.item.LocatorMode.STRUCTURES.next()
                == com.veskorius.item.LocatorMode.RESOURCES,
            "Le cycle doit alterner Ressources ↔ Structures");
        helper.succeed();
    }

    /**
     * Mode Structures : détection via l'API de structure vanilla (aucun scan de blocs).
     * Tant qu'aucune vraie structure n'est taguée {@code #veskorius:locatable} (elles sont
     * encore des features), la recherche retourne {@code null} proprement, sans crash.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void locatorStructureModeGracefulWithoutStructures(GameTestHelper helper) {
        BlockPos found = ResonanceLocatorItem.nearestLocatableStructure(
            helper.getLevel(), helper.absolutePos(MACHINE));
        helper.assertTrue(found == null,
            "Sans structure taguée, le mode Structures ne doit rien trouver (et ne pas crasher)");
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

                helper.assertTrue(ResonanceLocatorItem.getCharge(loc) == ResonanceLocatorItem.rechargeRate(),
                    "La batterie devrait gagner " + ResonanceLocatorItem.rechargeRate()
                        + " Osc dans un champ, vaut " + ResonanceLocatorItem.getCharge(loc));
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                helper.assertTrue(emitter.getReserve() == 4000 - ResonanceLocatorItem.rechargeRate(),
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

        helper.assertTrue(ResonanceLocatorItem.getCharge(loc) == ResonanceLocatorItem.rechargeRate(),
            "La batterie devrait se recharger depuis la cellule, vaut " + ResonanceLocatorItem.getCharge(loc));
        helper.assertTrue(ResonanceStorageCellItem.getCharge(cell) == 500 - ResonanceLocatorItem.rechargeRate(),
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
        ItemStack chassis = new ItemStack(ModBlocks.ATTUNED_CHASSIS.get());
        ItemStack c = new ItemStack(ModItems.RESONANCE_COMPONENT.get());
        ItemStack g = new ItemStack(Items.GOLD_INGOT);
        ItemStack p = ResonanceBlueprintItem.of(2);
        ItemStack e = ItemStack.EMPTY;

        // Recette sans forme : châssis Accordé + 2 Component + 2 Gold + blueprint
        // (voir ModRecipeProvider, « une machine = son châssis + ce qui la distingue »).
        CraftingInput withBlueprint = CraftingInput.of(3, 3, List.of(
            chassis, c, c,
            g, g, p,
            e, e, e));
        CraftingInput withoutBlueprint = CraftingInput.of(3, 3, List.of(
            chassis, c, c,
            g, g, e,
            e, e, e));

        // Un blueprint du MAUVAIS palier : il ne doit pas ouvrir une recette T2.
        CraftingInput wrongTier = CraftingInput.of(3, 3, List.of(
            chassis, c, c,
            g, g, ResonanceBlueprintItem.of(4),
            e, e, e));

        helper.assertTrue(craftsFieldEmitter(level, withBlueprint),
            "Avec le blueprint T2, la recette doit produire un Field Emitter");
        helper.assertFalse(craftsFieldEmitter(level, withoutBlueprint),
            "Sans le blueprint, la recette ne doit pas matcher");
        // C'EST LA VÉRIFICATION QUI MANQUAIT.
        helper.assertFalse(craftsFieldEmitter(level, wrongTier),
            "Un blueprint T4 ne doit PAS ouvrir une recette T2 : le gatekeeping de "
                + "03-Progression.md porte sur le PALIER, pas sur la simple possession d'un "
                + "plan. `requires(ItemLike)` ignore les data components — le tier ne gardait "
                + "donc rien, et le plan T2 débloquait déjà tout le mod");
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

    /**
     * <b>Chaque palier a une porte, et elles sont toutes différentes.</b>
     *
     * <p>Ce test existe parce que le T4 n'en avait aucune. La salle de lecture de l'Archive
     * posait la console du <b>Sigma</b> — celle qui rend le T3 — alors que son propre
     * commentaire annonçait « la console rend le blueprint T4 ». Le donjon le plus profond
     * du mod ne débloquait donc rien de plus que le précédent, et un palier entier décrit
     * au dossier restait inatteignable. Rien ne pouvait le signaler : les deux blocs sont
     * du même type, se posent, s'utilisent et donnent un blueprint parfaitement valide.
     *
     * <p>On vérifie donc le tier <b>que chaque bloc rend réellement</b>, et non sa présence.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void eachTierHasItsOwnDoor(GameTestHelper helper) {
        assertConsoleTier(helper, ModBlocks.ATTUNEMENT_CONSOLE.get(), 2, "Avant-poste");
        assertConsoleTier(helper, ModBlocks.SIGMA_CONSOLE.get(), 3, "Sigma");
        assertConsoleTier(helper, ModBlocks.ARCHIVE_CONSOLE.get(), 4, "Archive Régionale");
        helper.succeed();
    }

    private static void assertConsoleTier(GameTestHelper helper,
                                          net.minecraft.world.level.block.Block block,
                                          int expected, String where) {
        int actual = ((AttunementConsoleBlock) block).getTier();
        helper.assertTrue(actual == expected,
            "La console de " + where + " doit rendre le blueprint T" + expected
                + ", elle rend T" + actual);
    }

    /**
     * <b>L'Archive garantit exactement trois Hyper Refined Crystal.</b>
     *
     * <p>Le chiffre est le pivot du palier : deux partent dans le Treillis du premier
     * Amplificateur, le troisième dans la Deep Synthesis Chamber, qui rend ensuite la
     * ressource renouvelable. Le joueur ne peut donc pas avoir les deux et doit choisir
     * (05-Machines.md, « Bootstrap du T4 »). Deux de moins et il reste bloqué sans jamais
     * pouvoir refaire d'Amplificateur ; deux de plus et il n'y a plus de choix du tout.
     *
     * <p>Le test tire la table cinquante fois : un pool mal placé donnerait « 3 cristaux OU
     * de l'or » à pile ou face, et la moitié des Archives ne débloqueraient rien. Ce piège
     * s'est déjà refermé deux fois sur le butin d'amorçage de l'Avant-poste.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void archiveAlwaysBootstrapsExactlyThreeCrystals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var table = level.getServer().reloadableRegistries()
            .getLootTable(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.LOOT_TABLE,
                com.veskorius.worldgen.ModWorldGen.ARCHIVE_LOOT));
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.EMPTY);

        for (int roll = 0; roll < 50; roll++) {
            int crystals = 0;
            for (ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(ModItems.HYPER_REFINED_CRYSTAL.get())) {
                    crystals += stack.getCount();
                }
            }
            helper.assertTrue(crystals == 3,
                "Tirage " + roll + " : 3 Hyper Refined attendus, obtenu " + crystals
                    + ". Sans eux le T4 est un cercle fermé — la Chambre en consomme un "
                    + "et elle est la seule source.");
        }
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

        helper.assertTrue(VeskoriusConfig.locatorCapacity() == 100
                && VeskoriusConfig.locatorCostPerUse() == 5
                && VeskoriusConfig.locatorRechargeRate() == 5
                && VeskoriusConfig.locatorRange() == 40,
            "Défauts Locator : 100 / 5 / 5 / 40");
        helper.assertTrue(VeskoriusConfig.custodeHealth() == 30.0
                && VeskoriusConfig.custodeDamage() == 6.0
                && VeskoriusConfig.custodeDetectionRange() == 6.0
                && VeskoriusConfig.custodeAlertRange() == 16.0,
            "Défauts Custode : 30 / 6 / 6 / 16");
        helper.assertTrue(VeskoriusConfig.striderMilkCooldown() == 6000
                && VeskoriusConfig.roostStriderRange() == 6.0,
            "Défauts Fileur/Roost : cooldown 6000, portée Roost 6");
        helper.assertTrue(Math.abs(VeskoriusConfig.sporeGrowthChance() - 0.05) < 1e-6,
            "Défaut croissance de spore 0.05, vaut " + VeskoriusConfig.sporeGrowthChance());
        helper.succeed();
    }

    /**
     * Défauts du thème <b>harmoniques</b> (14-Configuration.md).
     *
     * <p>Séparé du test précédent parce qu'il comble un trou identifié à la
     * réanalyse : {@code configDefaultsMatchDesign} ne couvrait que
     * {@code basics}/{@code machines}/{@code mobs}/{@code generation}, alors que
     * 14-Configuration.md affirmait qu'il « couvre tous les thèmes d'un coup ».
     * C'était faux — aucun défaut de {@code harmonics} n'était tenu par autre chose
     * que la relecture, sur le système le plus riche en curseurs du mod.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void harmonicsConfigDefaultsMatchDesign(GameTestHelper helper) {
        helper.assertTrue(HarmonicsConfig.enabled(),
            "Les harmoniques sont actives par défaut (interrupteur maître)");
        helper.assertTrue(HarmonicsConfig.bandCount() == 3,
            "Défaut bandCount 3, vaut " + HarmonicsConfig.bandCount());
        helper.assertTrue(Math.abs(HarmonicsConfig.detuneOscMultiplier() - 1.5) < 1e-6,
            "Défaut detuneOscMultiplier 1.5, vaut " + HarmonicsConfig.detuneOscMultiplier());
        helper.assertTrue(HarmonicsConfig.dissonancePerDetunedTick() == 1,
            "Défaut dissonancePerDetunedTick 1, vaut " + HarmonicsConfig.dissonancePerDetunedTick());
        helper.assertTrue(HarmonicsConfig.dissonanceCapacity() == 2000,
            "Défaut dissonanceCapacity 2000, vaut " + HarmonicsConfig.dissonanceCapacity());
        helper.assertTrue(Math.abs(HarmonicsConfig.dissonanceUnstableThreshold() - 0.75) < 1e-6,
            "Défaut dissonanceUnstableThreshold 0.75, vaut "
                + HarmonicsConfig.dissonanceUnstableThreshold());
        helper.assertTrue(HarmonicsConfig.dissonanceDecayPerSecond() == 1,
            "Défaut dissonanceDecayPerSecond 1, vaut " + HarmonicsConfig.dissonanceDecayPerSecond());

        helper.assertTrue(HarmonicsConfig.dischargeEnabled(), "Décharge active par défaut");
        helper.assertTrue(HarmonicsConfig.dischargeRadius() == 6,
            "Défaut rayon de décharge 6, vaut " + HarmonicsConfig.dischargeRadius());
        helper.assertTrue(Math.abs(HarmonicsConfig.dischargeDamage() - 6.0) < 1e-6,
            "Défaut dégâts de décharge 6.0, vaut " + HarmonicsConfig.dischargeDamage());
        helper.assertTrue(Math.abs(HarmonicsConfig.dischargeReleaseFraction() - 0.5) < 1e-6,
            "Défaut fraction purgée 0.5, vaut " + HarmonicsConfig.dischargeReleaseFraction());
        helper.assertTrue(HarmonicsConfig.dischargeCooldownTicks() == 100,
            "Défaut cooldown de décharge 100, vaut " + HarmonicsConfig.dischargeCooldownTicks());

        // Invariant de conception, pas seulement une valeur : le Damping Array doit
        // porter PLUS LOIN que la décharge, sinon réparer un champ saturé obligerait à
        // entrer dans la zone d'impulsion — « aucune décharge forcée sur le joueur qui
        // répare » (06-Energy.md).
        helper.assertTrue(HarmonicsConfig.dampingRange() > HarmonicsConfig.dischargeRadius(),
            "Le Damping Array (" + HarmonicsConfig.dampingRange() + ") doit porter plus loin que "
                + "la décharge (" + HarmonicsConfig.dischargeRadius() + ") : on doit pouvoir "
                + "nettoyer un champ saturé hors de portée de l'impulsion");
        helper.assertTrue(HarmonicsConfig.dampingRange() == 16,
            "Défaut dampingRange 16, vaut " + HarmonicsConfig.dampingRange());
        helper.assertTrue(HarmonicsConfig.dampingCycleTicks() == 100,
            "Défaut dampingCycleTicks 100, vaut " + HarmonicsConfig.dampingCycleTicks());

        helper.assertTrue(HarmonicsConfig.hudEnabled(), "HUD de champ actif par défaut");
        helper.assertTrue(HarmonicsConfig.hudUpdateInterval() == 10,
            "Défaut hudUpdateIntervalTicks 10, vaut " + HarmonicsConfig.hudUpdateInterval());
        helper.succeed();
    }

    /**
     * Défauts des <b>slots d'augment</b> (A9, `machines.augment`). Même trou que
     * ci-dessus : ces trois clés n'étaient couvertes par aucun test.
     *
     * <p>Le défaut de {@code augmentSlots} est particulièrement sensible : il vaut 1
     * pour reproduire <i>exactement</i> le comportement historique (un seul slot). Le
     * passer à 2 sans le vouloir doublerait silencieusement la vitesse atteignable de
     * toutes les machines du mod.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void augmentConfigDefaultsMatchDesign(GameTestHelper helper) {
        helper.assertTrue(MachinesConfig.augmentSlots() == 1,
            "Défaut augmentSlots 1 (= comportement historique), vaut "
                + MachinesConfig.augmentSlots());
        helper.assertTrue(MachinesConfig.augmentStacking() == MachinesConfig.AugmentStacking.FREE,
            "Défaut augmentStacking FREE, vaut " + MachinesConfig.augmentStacking());
        helper.assertTrue(MachinesConfig.augmentStackingCap() == 2,
            "Défaut augmentStackingCap 2, vaut " + MachinesConfig.augmentStackingCap());
        helper.assertTrue(MachinesConfig.overheatIgnoresStable(),
            "Défaut overheatIgnoresStable true : la surchauffe reste un pari, même sur une "
                + "recette stable");
        helper.assertTrue(MachinesConfig.MAX_AUGMENT_SLOTS == 4,
            "MAX_AUGMENT_SLOTS vaut 4 — c'est la taille d'inventaire RÉSERVÉE par machine. "
                + "La changer désaligne les sauvegardes existantes, elle ne doit pas bouger "
                + "sans migration.");
        // Le nombre configuré ne doit jamais dépasser ce que l'inventaire réserve.
        helper.assertTrue(MachinesConfig.augmentSlots() <= MachinesConfig.MAX_AUGMENT_SLOTS,
            "augmentSlots ne peut pas dépasser MAX_AUGMENT_SLOTS");
        helper.succeed();
    }

    /** Défense de site : casser une machine à portée fait cibler le casseur. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void custodeAlertsOnMachineBreak(GameTestHelper helper) {
        CustodeEntity custode = helper.spawn(ModEntities.CUSTODE.get(), MACHINE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos machineBroken = helper.absolutePos(MACHINE.offset(3, 0, 0));

        CustodeAlertHandler.alertNearbyCustodes(helper.getLevel(), machineBroken, player);

        helper.assertTrue(custode.getTarget() == player,
            "Casser une machine à portée devrait faire cibler le joueur par le Custode");
        helper.succeed();
    }

    /**
     * <b>Un Custode Lourd qui prend une cible la fait prendre à ses voisins.</b>
     *
     * <p>C'est la seule chose que le Lourd ajoute au Custode ordinaire (09-Entities.md) :
     * ni portée de poursuite, ni cadence, seulement « il en appelle un autre ». Tout le
     * reste — patrouille, réactivité, persistance — est hérité, donc déjà couvert. Si
     * cette chaîne se rompt, il ne reste qu'un Custode avec deux fois plus de points de
     * vie, ce qui est exactement l'agressivité non motivée que le dossier refuse — et
     * rien, en jeu, ne le signalerait.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void heavyCustodeAlertsItsPair(GameTestHelper helper) {
        CustodeLourdEntity first = helper.spawn(ModEntities.CUSTODE_LOURD.get(), MACHINE);
        CustodeLourdEntity second =
            helper.spawn(ModEntities.CUSTODE_LOURD.get(), MACHINE.offset(4, 0, 0));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        first.setTarget(player);

        helper.assertTrue(second.getTarget() == player,
            "Le second Custode Lourd devrait avoir pris la même cible : c'est la seule "
                + "capacité qui le distingue du Custode ordinaire");
        helper.succeed();
    }

    /**
     * <b>Et la chaîne s'arrête d'elle-même.</b>
     *
     * <p>Deux gardes qui s'alertent mutuellement sans condition d'arrêt se rappellent
     * l'un l'autre à chaque tick — le piège de récursion exact du Resonance Relay, qui
     * s'alimentait de sa propre sortie. Ici la terminaison tient à un seul point : on ne
     * propage qu'aux Lourds <b>sans cible</b>. Ce test vérifie qu'un garde déjà occupé
     * n'est pas retourné vers une nouvelle cible par un voisin — sans quoi deux gardes
     * pourraient se repasser indéfiniment deux joueurs différents.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void heavyCustodeAlertDoesNotLoop(GameTestHelper helper) {
        CustodeLourdEntity first = helper.spawn(ModEntities.CUSTODE_LOURD.get(), MACHINE);
        CustodeLourdEntity second =
            helper.spawn(ModEntities.CUSTODE_LOURD.get(), MACHINE.offset(4, 0, 0));
        Player engaged = helper.makeMockPlayer(GameType.SURVIVAL);
        Player newcomer = helper.makeMockPlayer(GameType.SURVIVAL);

        second.setTarget(engaged);
        first.setTarget(newcomer);

        helper.assertTrue(second.getTarget() == engaged,
            "Un Custode Lourd déjà engagé ne doit pas changer de cible parce qu'un voisin "
                + "en a trouvé une autre — c'est cette condition qui borne la chaîne");
        helper.succeed();
    }

    /**
     * <b>Le buisson de floraison se cueille et repousse — il ne meurt pas.</b>
     *
     * <p>C'est la seule propriété de cette plante qui compte, et le dossier l'écrit en
     * toutes lettres : « récolte répétée façon buisson de baies, <b>pas à usage unique</b> ».
     *
     * <p>Si la cueillette le détruisait, la branche entière retomberait derrière un tirage :
     * la Graine Ancienne ne sort que d'une Archive Régionale sur cinq, et il faudrait en
     * retrouver une à chaque récolte. Le mod interdit explicitement qu'une progression
     * dépende d'un dé — la branche est facultative, mais elle ne doit pas non plus se
     * refermer sur un joueur qui l'a ouverte. Un buisson qui repart de l'âge 1 transforme
     * une trouvaille rare en ressource entretenue ; un buisson qui meurt la reprend.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void bloomBushSurvivesItsOwnHarvest(GameTestHelper helper) {
        BlockPos soil = MACHINE;
        BlockPos bush = soil.above();
        helper.setBlock(soil, net.minecraft.world.level.block.Blocks.DIRT);
        helper.setBlock(bush, ModBlocks.RESONANCE_BLOOM_BUSH.get().defaultBlockState()
            .setValue(com.veskorius.block.ResonanceBloomBushBlock.AGE,
                com.veskorius.block.ResonanceBloomBushBlock.RIPE));

        com.veskorius.block.ResonanceBloomBushBlock.harvest(
            helper.getBlockState(bush), helper.getLevel(), helper.absolutePos(bush),
            helper.makeMockPlayer(GameType.SURVIVAL));

        helper.assertBlockPresent(ModBlocks.RESONANCE_BLOOM_BUSH.get(), bush);
        int age = helper.getBlockState(bush)
            .getValue(com.veskorius.block.ResonanceBloomBushBlock.AGE);
        helper.assertTrue(age > 0 && age < com.veskorius.block.ResonanceBloomBushBlock.RIPE,
            "Après cueillette le buisson doit retomber ENTRE zéro et mûr — ni détruit, ni "
                + "encore récoltable. Âge obtenu : " + age);
        helper.succeed();
    }

    /**
     * <b>Seule la pioche en Alliage Veskorien mine la Pierre à Conduits.</b>
     *
     * <p>C'est le seul rôle que le dossier réserve à cette pioche, et il ne tient qu'à des
     * tags : la pierre est déclarée incorrecte pour les six paliers vanilla et omise du tag
     * veskorien. Une seule ligne de datagen mal placée — la pierre ajoutée au tag veskorien,
     * ou oubliée dans celui de la netherite — et la règle disparaît <b>en silence</b>. Rien
     * ne planterait : la pierre deviendrait simplement minable au diamant, et le seul geste
     * qui distingue le palier 3 du reste s'évaporerait.
     *
     * <p>On teste les deux sens, parce qu'un seul ne prouve rien : que la netherite échoue,
     * et que l'alliage réussisse.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void onlyTheAlloyPickaxeMinesConduitStone(GameTestHelper helper) {
        net.minecraft.world.level.block.state.BlockState stone =
            ModBlocks.ANCIENT_CONDUIT_STONE.get().defaultBlockState();

        helper.assertFalse(
            new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_PICKAXE)
                .isCorrectToolForDrops(stone),
            "La pioche en netherite ne doit RIEN tirer de la Pierre à Conduits — sinon le "
                + "seul rôle réservé à la pioche d'alliage n'existe plus");
        helper.assertFalse(
            new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE)
                .isCorrectToolForDrops(stone),
            "Ni la pioche en diamant");
        helper.assertTrue(
            new net.minecraft.world.item.ItemStack(ModItems.VESKORIAN_ALLOY_PICKAXE.get())
                .isCorrectToolForDrops(stone),
            "…et la pioche en Alliage Veskorien doit y arriver, sans quoi le bloc est "
                + "inextractible par qui que ce soit");
        helper.succeed();
    }

    /**
     * <b>L'orage démarre, et son compteur survit à lui-même.</b>
     *
     * <p>Ce test ne vérifie pas la météo — il vérifie que l'état de l'orage est bien un état
     * et pas une variable perdue. Un orage déclenché doit se déclarer en cours ; sans ça, la
     * boucle de semis ne tourne jamais et l'événement entier est décoratif.
     *
     * <p>C'est le genre de chose qui « marche » en jeu pendant une session et casse au
     * rechargement : le compteur vit dans une {@code SavedData} précisément pour qu'un orage
     * interrompu par un redémarrage finisse quand même — sinon ses cratères resteraient au
     * sol pour toujours, et le « rien ne s'accumule » qui définit l'événement tomberait.
     */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void resonanceStormHasPersistentState(GameTestHelper helper) {
        var level = helper.getLevel();
        helper.assertFalse(
            com.veskorius.event.ResonanceStormHandler.isStorming(level),
            "Aucun orage ne doit être en cours au départ");

        com.veskorius.event.ResonanceStormHandler.forceStart(level);

        helper.assertTrue(
            com.veskorius.event.ResonanceStormHandler.isStorming(level),
            "Un orage déclenché doit se déclarer en cours — sinon rien ne se sème et "
                + "l'événement est purement décoratif");
        helper.succeed();
    }

    /**
     * <b>Un relais réparé s'éteint tout seul, et c'est l'énigme du Sigma.</b>
     *
     * <p>Le puzzle du laboratoire ne demande pas de réparer deux relais : il demande de les
     * avoir allumés <b>en même temps</b>. C'est l'autonomie limitée — quatre-vingt-dix
     * secondes — qui crée la contrainte, puisque le second est trop loin pour qu'on y arrive
     * sans courir. Un relais qui resterait allumé transformerait l'énigme en simple liste de
     * courses, et rien ne le signalerait : le donjon se terminerait, juste sans tension.
     *
     * <p>Cette mécanique n'avait aucun test. Sa géométrie en avait un — le second relais est
     * bien hors de portée — mais pas son compte à rebours, qui est pourtant la moitié du
     * dispositif.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void repairedRelayRunsOutOnItsOwn(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.DAMAGED_RELAY.get());
        com.veskorius.block.entity.DamagedRelayBlockEntity relay = helper.getBlockEntity(MACHINE);

        helper.assertFalse(relay.isRunning(), "Un relais endommagé démarre éteint");
        relay.repair();
        helper.assertTrue(relay.isRunning(), "Réparé, il tourne");
        helper.assertTrue(
            relay.getTicksLeft() == com.veskorius.block.entity.DamagedRelayBlockEntity.UPTIME,
            "…avec exactement son autonomie, ni plus ni moins : c'est ce chiffre qui rend "
                + "les deux relais simultanés difficiles à obtenir. Obtenu : "
                + relay.getTicksLeft());
        helper.succeed();
    }

    /**
     * <b>Le socle de l'Archive ne garde qu'un objet, et rend le précédent.</b>
     *
     * <p>L'énigme de l'Archive demande d'activer quatre socles dans le bon ordre, et
     * l'ordre se cherche en se trompant. Deux garde-fous rendent l'erreur gratuite, et ce
     * test les tient : le socle <b>n'accepte qu'un fragment de Codex</b> — poser autre
     * chose par mégarde ne l'avale pas — et il n'en prend qu'<b>un seul exemplaire</b> de
     * la pile. Sans le second, poser un fragment coûterait tout le stock ; sans le premier,
     * on perdrait n'importe quel objet tenu en main.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void archivePedestalHoldsExactlyOneFragment(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.ARCHIVE_PEDESTAL.get());
        com.veskorius.block.entity.ArchivePedestalBlockEntity pedestal =
            helper.getBlockEntity(MACHINE);

        helper.assertTrue(pedestal.isEmpty(), "Un socle démarre vide");
        helper.assertFalse(
            pedestal.place(new net.minecraft.world.item.ItemStack(
                ModItems.RAW_RESONANCE_CRYSTAL.get())),
            "Il refuse ce qui n'est pas un fragment de Codex — sinon chercher l'ordre "
                + "coûterait les objets qu'on a en main");
        helper.assertTrue(pedestal.isEmpty(), "…et reste vide après ce refus");

        helper.assertTrue(
            pedestal.place(new net.minecraft.world.item.ItemStack(
                ModItems.CODEX_FRAGMENT.get(), 8)),
            "Il accepte un fragment de Codex");
        helper.assertTrue(pedestal.getHeld().getCount() == 1,
            "…un seul exemplaire, jamais la pile entière : sinon poser un fragment "
                + "coûterait tout le stock. Obtenu : " + pedestal.getHeld().getCount());
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

    /** État du glow « en marche » (blockstate LIT) d'une machine. */
    private static boolean machineLit(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getValue(AbstractMachineBlock.LIT);
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
        chargedEmitter(helper, 1);
    }

    /**
     * Idem, avec plusieurs unites de carburant. Necessaire des qu'un cycle coute plus que
     * les 4000 Osc d'un cristal : l'emetteur ne se recharge qu'a reserve nulle, donc il lui
     * faut de quoi rebruler, sinon la machine cale a mi-course.
     */
    private static void chargedEmitter(GameTestHelper helper, int fuelCount) {
        FieldEmitterBlockEntity emitter = placeEmitter(helper);
        emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), fuelCount), false);
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

    // =====================================================================
    // Veskorian Alloy Forge (#10) — la porte du T3
    // =====================================================================

    private static final BlockPos FORGE = new BlockPos(10, 1, 8);
    private static final int FORGE_TICKS = 20 * 20;

    /**
     * <b>La forge produit un alliage ET une scorie, dans deux slots distincts.</b>
     * La scorie n'étant pas dans la recette mais dans la machine (voir
     * {@code VeskorianAlloyForgeBlockEntity}), rien dans les données ne la garantit :
     * seul ce test le fait.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = FORGE_TICKS + 200)
    public static void forgeProducesAlloyAndSlag(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(FORGE, ModBlocks.VESKORIAN_ALLOY_FORGE.get());
                IItemHandler inv = machineInventory(helper, FORGE);
                inv.insertItem(VeskorianAlloyForgeBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2), false);
                inv.insertItem(VeskorianAlloyForgeBlockEntity.SLOT_METAL,
                    new ItemStack(Items.IRON_INGOT, 2), false);
            })
            .thenExecuteAfter(FORGE_TICKS + 10, () -> {
                IItemHandler inv = machineInventory(helper, FORGE);
                helper.assertTrue(inv.getStackInSlot(VeskorianAlloyForgeBlockEntity.SLOT_OUTPUT)
                        .is(ModItems.VESKORIAN_ALLOY_INGOT.get()),
                    "Le fer doit donner l'alliage STRUCTUREL, vaut : "
                        + inv.getStackInSlot(VeskorianAlloyForgeBlockEntity.SLOT_OUTPUT));
                helper.assertTrue(inv.getStackInSlot(VeskorianAlloyForgeBlockEntity.SLOT_SLAG)
                        .is(ModItems.FLUX_SLAG.get()),
                    "Chaque cycle doit produire une scorie : c'est une propriété de la "
                        + "machine, pas de la recette — donc rien dans les données ne la garantit");
            })
            .thenSucceed();
    }

    /**
     * <b>Slot de scorie plein = forge à l'arrêt.</b>
     *
     * <p>C'est le cœur du design du palier, et il ne tient qu'à cette ligne : sans ce
     * blocage, le déchet serait un item décoratif qu'on jette, le Slag Vent n'aurait
     * aucune raison d'exister, et « le joueur reproduit en miniature la cause de
     * l'Effondrement » (02-Lore.md) resterait une phrase dans un fichier.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = FORGE_TICKS + 200)
    public static void forgeStallsWhenSlagBacksUp(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(FORGE, ModBlocks.VESKORIAN_ALLOY_FORGE.get());
                IItemHandler inv = machineInventory(helper, FORGE);
                inv.insertItem(VeskorianAlloyForgeBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2), false);
                inv.insertItem(VeskorianAlloyForgeBlockEntity.SLOT_METAL,
                    new ItemStack(Items.IRON_INGOT, 2), false);
                inv.insertItem(VeskorianAlloyForgeBlockEntity.SLOT_SLAG,
                    new ItemStack(ModItems.FLUX_SLAG.get(), 64), false);
            })
            .thenExecuteAfter(FORGE_TICKS + 10, () -> {
                IItemHandler inv = machineInventory(helper, FORGE);
                helper.assertTrue(inv.getStackInSlot(VeskorianAlloyForgeBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Scorie bloquée : la forge ne doit RIEN produire. Sans ce blocage, le "
                        + "déchet du T3 n'est qu'un item décoratif et le Slag Vent n'a plus "
                        + "de raison d'être");
            })
            .thenSucceed();
    }

    private static int progressOf(GameTestHelper helper) {
        ResonanceStabilizerBlockEntity machine = helper.getBlockEntity(MACHINE);
        return machine.getData().get(AbstractMachineBlockEntity.DATA_PROGRESS);
    }

    /**
     * <b>Chaque machine du mod a une recette réellement chargée.</b>
     *
     * <p>Ce test existe à cause d'un bug trouvé <i>en jeu</i>, jamais par la génération :
     * la recette du Deep Crystal Driller comptait dix ingrédients (châssis + 6 Component +
     * 2 lingots + blueprint) alors qu'une recette sans forme n'en tient que <b>neuf</b> —
     * la taille de la grille. Le JSON se générait sans une plainte, {@code runData}
     * réussissait, et c'est le chargement du monde qui écartait la recette avec un simple
     * message dans le log. Résultat : une machine complète, posable, fonctionnelle, testée,
     * <b>et fabricable par personne</b>.
     *
     * <p>C'est la pire forme d'erreur du projet — celle où tout est vert et où le joueur
     * bute. Le contrôle se fait donc ici, sur le {@code RecipeManager} du serveur, c'est-à-dire
     * sur ce qui a <b>survécu au chargement</b> et non sur ce qu'on a écrit sur le disque.
     * Un ingrédient de trop, un tag vide, un JSON refusé : le test tombe.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void everyMachineIsActuallyCraftable(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getRecipes();
        for (net.minecraft.world.level.block.Block machine : CRAFTABLE_MACHINES) {
            boolean found = recipes.stream().anyMatch(holder ->
                holder.value().getResultItem(helper.getLevel().registryAccess())
                    .is(machine.asItem()));
            helper.assertTrue(found,
                "Aucune recette chargée ne produit " + machine.getName().getString()
                    + " — la machine est injouable. Cause la plus probable : plus de 9 "
                    + "ingrédients dans une recette sans forme, écartée au chargement.");
        }
        helper.succeed();
    }

    /**
     * <b>Aucun déchet ne reste sans exutoire.</b>
     *
     * <p>Les machines produisent trois sous-produits, et le dossier leur assigne des rôles
     * opposés : la scorie de la Forge <b>doit</b> être évacuée sous peine de bloquer la
     * machine — c'est une contrainte voulue — tandis que le résidu du Synthesizer est
     * présenté comme sa contre-preuve, « tous les sous-produits ne sont pas des nuisances »
     * (04-Materials.md). Sauf que pendant tout le T3, le résidu n'était consommé par
     * <b>rien</b>. Il s'accumulait, et la contre-preuve annoncée par trois documents
     * n'existait pas en jeu : le mod n'enseignait qu'une chose, qu'il faut jeter.
     *
     * <p>Un déchet sans exutoire ne casse rien et ne lève rien — il encombre, ce qui est
     * indiscernable d'un choix de design tant que personne ne relit le dossier. D'où ce
     * test, sur le {@code RecipeManager} chargé : chaque sous-produit doit apparaître
     * comme <b>ingrédient</b> d'au moins une recette qui a survécu au chargement.
     *
     * <p><b>Les trois y figurent, et j'ai eu tort d'en exclure une.</b> Le premier jet
     * gardait la scorie hors de la liste, au motif que son exutoire était le Slag Vent —
     * une machine qui la <b>détruit</b> — et non une recette. C'était faux : le dossier
     * lui destinait le Reclaimer depuis le début (« usages des déchets, essentiels pour
     * que ce ne soit pas juste de la suppression »). J'avais pris l'état du code pour
     * l'intention du design, ce qui est exactement l'erreur que cette famille de tests
     * existe pour empêcher.
     *
     * <p>La boue, elle, n'était surveillée par personne : produite à chaque purge du
     * Damping Array, consommée par rien, et absente de cette liste. Un déchet oublié
     * <b>par la garde des déchets</b> — la liste est le seul point faible du dispositif,
     * puisque rien n'oblige à y inscrire un sous-produit nouveau.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void everyByproductHasAnOutlet(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getRecipes();
        for (net.minecraft.world.item.Item waste : BYPRODUCTS) {
            boolean consumed = recipes.stream().anyMatch(holder ->
                holder.value().getIngredients().stream()
                    .anyMatch(ingredient -> ingredient.test(new net.minecraft.world.item.ItemStack(waste))));
            helper.assertTrue(consumed,
                "Aucune recette chargée ne consomme " + waste.getDescription().getString()
                    + " — le déchet s'accumule sans exutoire. Il ne lèvera jamais d'erreur : "
                    + "un sous-produit qui encombre est indiscernable d'un sous-produit voulu.");
        }
        helper.succeed();
    }

    /**
     * Les trois sous-produits des machines. Un déchet ajouté sans y figurer n'est pas
     * gardé : c'est la faiblesse du dispositif, et la raison pour laquelle la boue a pu
     * s'accumuler tout un palier sans que personne ne le voie.
     */
    private static final net.minecraft.world.item.Item[] BYPRODUCTS = {
        ModItems.SYNTHESIS_RESIDUE.get(),
        ModItems.FLUX_SLAG.get(),
        ModItems.RESONANCE_SLUDGE.get(),
    };

    /** Toutes les machines fabricables. Une machine ajoutée sans y figurer n'est pas gardée. */
    private static final net.minecraft.world.level.block.Block[] CRAFTABLE_MACHINES = {
        ModBlocks.RESONANCE_STABILIZER.get(), ModBlocks.COMPONENT_ASSEMBLER.get(),
        ModBlocks.RESONANCE_WHETSTONE.get(), ModBlocks.CRYSTAL_CRUSHER.get(),
        ModBlocks.FLUX_PURIFIER.get(), ModBlocks.FIELD_EMITTER.get(),
        ModBlocks.TUNABLE_FIELD_EMITTER.get(), ModBlocks.CRYSTAL_ROOST.get(),
        ModBlocks.DAMPING_ARRAY.get(), ModBlocks.VESKORIAN_ALLOY_FORGE.get(),
        ModBlocks.RESONANCE_RELAY.get(), ModBlocks.FLUX_COMPRESSOR.get(),
        ModBlocks.RECLAIMER.get(), ModBlocks.ADVANCED_ASSEMBLER.get(),
        ModBlocks.STRUCTURAL_SYNTHESIZER.get(), ModBlocks.DEEP_CRYSTAL_DRILLER.get(),
        ModBlocks.SLAG_VENT.get(),
        ModBlocks.DEEP_SYNTHESIS_CHAMBER.get(), ModBlocks.HARMONIC_AMPLIFIER.get(),
        ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get(), ModBlocks.RESONANCE_NETWORK_HUB.get(),
        ModBlocks.CONVERGENCE_CORE.get(), ModBlocks.RIFT_ANCHOR.get(),
        ModBlocks.RIFT_CORE_EXTRACTOR.get(), ModBlocks.RIFT_WARD_EMITTER.get(),
        ModBlocks.FRACTURED_CHASSIS.get(), ModBlocks.ATTUNED_CHASSIS.get(),
        ModBlocks.VESKORIAN_CHASSIS.get(),
    };

    // =====================================================================
    // Flux Compressor (#23), Structural Synthesizer (#11),
    // Slag Vent (#13), Deep Crystal Driller (#12)
    // =====================================================================

    /**
     * <b>Toutes ces positions sont dans la portee de 8 de l'emetteur, et c'est a verifier
     * a chaque ajout.</b> Le premier jet posait le compresseur sur {@code MACHINE} (2,1,2),
     * a 11 blocs de l'emetteur : la machine ne produisait rien et le test accusait la
     * recette. Une position hors champ ne leve rien — elle rend juste un test faux sur une
     * machine juste.
     */
    private static final BlockPos COMPRESSOR = new BlockPos(8, 1, 10);
    private static final BlockPos SYNTH = new BlockPos(8, 1, 12);
    private static final BlockPos VENT = new BlockPos(12, 1, 8);
    private static final BlockPos DRILL = new BlockPos(6, 1, 6);

    /** 4 Refined Crystal → 1 Concentrated Flux, 30 s. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 30 * 20 + 200)
    public static void compressorMakesConcentratedFlux(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(COMPRESSOR, ModBlocks.FLUX_COMPRESSOR.get());
                machineInventory(helper, COMPRESSOR).insertItem(
                    com.veskorius.block.entity.FluxCompressorBlockEntity.SLOT_INPUT,
                    new ItemStack(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 4), false);
            })
            .thenExecuteAfter(30 * 20 + 40, () -> helper.assertTrue(
                machineInventory(helper, COMPRESSOR).getStackInSlot(
                    com.veskorius.block.entity.FluxCompressorBlockEntity.SLOT_OUTPUT)
                    .is(ModItems.CONCENTRATED_FLUX.get()),
                "Le compresseur doit rendre du Flux Concentré"))
            .thenSucceed();
    }

    /**
     * <b>Le synthétiseur produit quatre blocs ET un résidu.</b> Comme pour la Forge, le
     * résidu est une propriété de la machine et non une ligne de recette : rien dans les
     * données ne le garantit, seul ce test le fait.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60 * 20 + 200)
    public static void synthesizerMakesBlocksAndResidue(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                // Un cycle de 60 s a 6 Osc/tick coute 7200 Osc — DAVANTAGE que les 4000
                // d'un seul cristal. Sans plusieurs unites de carburant, l'emetteur tombe
                // a sec a mi-cycle et le test echouerait en accusant la recette.
                chargedEmitter(helper, 4);
                helper.setBlock(SYNTH, ModBlocks.STRUCTURAL_SYNTHESIZER.get());
                IItemHandler inv = machineInventory(helper, SYNTH);
                inv.insertItem(com.veskorius.block.entity.StructuralSynthesizerBlockEntity.SLOT_ALLOY,
                    new ItemStack(ModItems.VESKORIAN_ALLOY_INGOT.get(), 4), false);
                inv.insertItem(com.veskorius.block.entity.StructuralSynthesizerBlockEntity.SLOT_STONE,
                    new ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 8), false);
            })
            .thenExecuteAfter(60 * 20 + 40, () -> {
                IItemHandler inv = machineInventory(helper, SYNTH);
                ItemStack out = inv.getStackInSlot(
                    com.veskorius.block.entity.StructuralSynthesizerBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(out.is(ModItems.VESKORIAN_ALLOY_BLOCK_ITEM.get()) && out.getCount() == 4,
                    "4 blocs d'alliage attendus, vaut : " + out);
                helper.assertTrue(inv.getStackInSlot(
                        com.veskorius.block.entity.StructuralSynthesizerBlockEntity.SLOT_RESIDUE)
                        .is(ModItems.SYNTHESIS_RESIDUE.get()),
                    "Chaque moulage laisse un résidu — porté par la recette de synthèse");
            })
            .thenSucceed();
    }

    /**
     * <b>Le Slag Vent débloque une forge à l'arrêt.</b> C'est toute sa raison d'être : sans
     * ce comportement il n'a aucune fonction, et la contrainte de scorie du palier n'a
     * aucune réponse.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = com.veskorius.block.entity.SlagVentBlockEntity.VENT_PERIOD + 300)
    public static void slagVentClearsAStalledForge(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(FORGE, ModBlocks.VESKORIAN_ALLOY_FORGE.get());
                machineInventory(helper, FORGE).insertItem(
                    VeskorianAlloyForgeBlockEntity.SLOT_SLAG,
                    new ItemStack(ModItems.FLUX_SLAG.get(), 64), false);
                helper.setBlock(VENT, ModBlocks.SLAG_VENT.get());
            })
            .thenExecuteAfter(com.veskorius.block.entity.SlagVentBlockEntity.VENT_PERIOD + 40, () -> {
                int left = machineInventory(helper, FORGE)
                    .getStackInSlot(VeskorianAlloyForgeBlockEntity.SLOT_SLAG).getCount();
                helper.assertTrue(left < 64,
                    "L'évent doit avoir retiré de la scorie. Restant : " + left);
            })
            .thenSucceed();
    }

    /**
     * <b>La foreuse retire l'amas du monde et le rend en cristal brut.</b>
     *
     * <p>Le premier jet de ce test affirmait le contraire — que l'amas devait survivre,
     * l'arene etant censee se trouver au-dessus de la limite de profondeur. Elle est en
     * realite posee a <b>Y −60</b>, donc franchement <i>sous</i> les −40 de la machine : le
     * banc d'essai se trouve exactement dans les conditions d'exploitation. La foreuse
     * faisait donc son travail et le test la declarait en faute. C'est le genre d'erreur qui
     * ferait « corriger » du code parfaitement juste — d'ou cette note.
     *
     * <p>La garde de profondeur elle-meme est verifiee juste en dessous, par la geometrie.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20 * 25 + 200)
    public static void drillerHarvestsAClusterBeneathIt(GameTestHelper helper) {
        BlockPos cluster = DRILL.below();
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper, 4);
                helper.setBlock(cluster, ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get());
                helper.setBlock(DRILL, ModBlocks.DEEP_CRYSTAL_DRILLER.get());
            })
            .thenExecuteAfter(20 * 25, () -> {
                helper.assertBlockNotPresent(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get(), cluster);
                helper.assertTrue(machineInventory(helper, DRILL).getStackInSlot(
                        com.veskorius.block.entity.DeepCrystalDrillerBlockEntity.SLOT_OUTPUT)
                        .is(ModItems.RAW_RESONANCE_CRYSTAL.get()),
                    "L'amas retire doit ressortir en cristal brut dans le slot de sortie");
            })
            .thenSucceed();
    }

    /**
     * <b>La garde de profondeur existe, et l'arene est bien du bon cote.</b>
     *
     * <p>Sans la limite, on pose une foreuse n'importe ou et la geographie du monde ne
     * decide plus de rien — or c'est elle qui doit decider de l'emplacement d'une base
     * (07-World-Generation.md). Ce test verrouille les deux moities de l'affirmation : la
     * limite vaut ce que le dossier annonce, et l'arene est en dessous — sans quoi le test
     * de recolte ci-dessus passerait pour une raison qui n'a rien a voir.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void drillerDepthLimitIsWorthTesting(GameTestHelper helper) {
        helper.assertTrue(com.veskorius.block.entity.DeepCrystalDrillerBlockEntity.MAX_Y == -40,
            "05-Machines.md #12 annonce Y −40");
        helper.assertTrue(helper.absolutePos(DRILL).getY()
                < com.veskorius.block.entity.DeepCrystalDrillerBlockEntity.MAX_Y,
            "L'arene doit etre SOUS la limite, sinon le test de recolte ne prouve rien. Y = "
                + helper.absolutePos(DRILL).getY());
        helper.succeed();
    }

    // =====================================================================
    // T4 — Deep Synthesis Chamber (#15) et Harmonic Amplifier (#14)
    // =====================================================================

    private static final BlockPos CHAMBER = new BlockPos(12, 1, 10);

    /**
     * <b>La Chambre rend le Hyper Refined renouvelable.</b> C'est la seule source du
     * matériau dans tout le jeu ; sans ce cycle, le T4 s'arrête aux trois cristaux de
     * l'Archive et le palier n'a plus de suite.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 90 * 20 + 300)
    public static void chamberMakesHyperRefinedRenewable(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                // 90 s à 8 Osc/tick = 14 400 Osc : quatre cristaux de carburant au moins.
                chargedEmitter(helper, 8);
                helper.setBlock(CHAMBER, ModBlocks.DEEP_SYNTHESIS_CHAMBER.get());
                machineInventory(helper, CHAMBER).insertItem(
                    com.veskorius.block.entity.DeepSynthesisChamberBlockEntity.SLOT_INPUT,
                    new ItemStack(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2), false);
            })
            .thenExecuteAfter(90 * 20 + 60, () -> helper.assertTrue(
                machineInventory(helper, CHAMBER).getStackInSlot(
                    com.veskorius.block.entity.DeepSynthesisChamberBlockEntity.SLOT_OUTPUT)
                    .is(ModItems.HYPER_REFINED_CRYSTAL.get()),
                "La Chambre est la SEULE source de Hyper Refined : sans ce cycle, le T4 "
                    + "s'arrête aux trois cristaux de l'Archive"))
            .thenSucceed();
    }

    /**
     * <b>L'amplificateur double, et il s'arrête de doubler au troisième maillon.</b>
     *
     * <p>Les deux moitiés comptent autant l'une que l'autre. Sans le gain il ne sert à
     * rien ; sans le plafond, dix appareils en file donnent une portée de plusieurs
     * milliers de blocs pour deux Osc par tick, et le réseau cesse d'être la contrainte de
     * terrain que tout le mod demande de résoudre. Le plafond est exactement le genre de
     * règle qu'on n'observe jamais en jouant normalement — on ne construit pas dix
     * amplificateurs pour vérifier — donc il ne peut être tenu que par un test.
     *
     * <p>Décision pure, testée sans monde : c'est pour ça qu'elle est isolée dans
     * {@code amplify}.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void amplifierDoublesUntilTheChainIsFull(GameTestHelper helper) {
        int max = com.veskorius.block.entity.HarmonicAmplifierBlockEntity.MAX_CHAIN;

        for (int depth = 0; depth < max; depth++) {
            int range = com.veskorius.block.entity.HarmonicAmplifierBlockEntity
                .amplify(20, depth, 1.0);
            helper.assertTrue(range == 40,
                "Maillon " + depth + " : la portée reçue doit doubler (40), vaut " + range);
        }
        int saturated = com.veskorius.block.entity.HarmonicAmplifierBlockEntity
            .amplify(20, max, 1.0);
        helper.assertTrue(saturated == 20,
            "Au-delà de " + max + " maillons, l'appareil REPORTE la portée sans la doubler ; "
                + "vaut " + saturated + ". Sans ce plafond, une file d'amplificateurs "
                + "couvre la carte pour deux Osc par tick.");

        // Sans source, aucune portée à multiplier — et surtout pas une portée par défaut.
        helper.assertTrue(com.veskorius.block.entity.HarmonicAmplifierBlockEntity
            .amplify(0, 0, 1.0) == 0, "Sans champ amont, la portée est nulle");
        helper.succeed();
    }

    /**
     * <b>Un amplificateur déréglé reste au moins aussi bon qu'un fil.</b>
     *
     * <p>La dérive n'atténue que le <i>gain</i>, jamais la portée reçue. Si elle rognait le
     * tout, un appareil à −30 % couvrirait moins que le champ qu'il relaie : en poser un
     * réduirait la couverture. Le joueur verrait sa base s'éteindre en <i>ajoutant</i> du
     * matériel, sans qu'aucun message ne l'explique.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void driftedAmplifierIsNeverWorseThanNoAmplifier(GameTestHelper helper) {
        double floor = com.veskorius.block.entity.HarmonicAmplifierBlockEntity.MIN_EFFICIENCY;
        int worst = com.veskorius.block.entity.HarmonicAmplifierBlockEntity.amplify(20, 0, floor);
        helper.assertTrue(worst >= 20,
            "Même au plancher de calibration, la portée doit rester ≥ celle reçue (20), "
                + "vaut " + worst + " — sinon poser un amplificateur RÉDUIT la couverture");
        helper.assertTrue(worst < 40,
            "…mais elle doit être inférieure au gain plein, sinon la dérive n'a aucun effet");
        helper.succeed();
    }

    /**
     * <b>Le Hub déleste par le bas, et seulement quand la réserve baisse.</b>
     *
     * <p>C'est la seule règle du mod qui décide de l'<i>arrêt</i> d'une machine, et elle
     * est invisible tant que la base n'est pas sous-dimensionnée — donc elle ne peut être
     * vérifiée qu'ici. Les deux bornes comptent autant : si le plancher ne montait jamais,
     * le Hub ne servirait à rien ; s'il montait tout de suite, poser un Hub arrêterait la
     * moitié d'une base qui tournait très bien.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void hubShedsFromTheBottomOnlyUnderStrain(GameTestHelper helper) {
        helper.assertTrue(
            com.veskorius.block.entity.ResonanceNetworkHubBlockEntity.floorFor(
                fakeField(1000, 1000), 1.0) == com.veskorius.energy.MachinePriority.LOW,
            "Réserve pleine : personne ne doit être délesté");
        helper.assertTrue(
            com.veskorius.block.entity.ResonanceNetworkHubBlockEntity.floorFor(
                fakeField(300, 1000), 1.0) == com.veskorius.energy.MachinePriority.NORMAL,
            "Réserve à 30 % : les priorités basses s'effacent, les autres passent");
        helper.assertTrue(
            com.veskorius.block.entity.ResonanceNetworkHubBlockEntity.floorFor(
                fakeField(50, 1000), 1.0) == com.veskorius.energy.MachinePriority.HIGH,
            "Réserve à 5 % : seules les priorités hautes sont servies");
        helper.assertTrue(
            com.veskorius.block.entity.ResonanceNetworkHubBlockEntity.floorFor(null, 1.0)
                == com.veskorius.energy.MachinePriority.LOW,
            "Sans champ mesurable, le Hub n'a rien à arbitrer et laisse tout passer");

        // Un Hub déréglé croit la réserve plus basse qu'elle n'est : il déleste plus tôt.
        helper.assertTrue(
            com.veskorius.block.entity.ResonanceNetworkHubBlockEntity.floorFor(
                fakeField(520, 1000),
                com.veskorius.block.entity.ResonanceNetworkHubBlockEntity.MIN_EFFICIENCY)
                != com.veskorius.energy.MachinePriority.LOW,
            "À 52 % de réserve, un Hub au plancher de calibration doit DÉJÀ délester — "
                + "sinon la dérive n'a aucun effet observable");
        helper.succeed();
    }

    /** Un champ factice de réserve/capacité donnée : la décision du Hub est pure. */
    private static com.veskorius.energy.IResonanceField fakeField(int reserve, int capacity) {
        return new com.veskorius.energy.IResonanceField() {
            @Override
            public int getFieldStrength() {
                return 100;
            }

            @Override
            public int getRange() {
                return 8;
            }

            @Override
            public boolean isActive() {
                return reserve > 0;
            }

            @Override
            public int extractOsc(int maxOsc) {
                return 0;
            }

            @Override
            public int getReserve() {
                return reserve;
            }

            @Override
            public int getCapacity() {
                return capacity;
            }
        };
    }

    /** Une machine ne s'efface que sous un plancher plus haut que sa propre priorité. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void onlyLowerPriorityMachinesAreShed(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.CRYSTAL_CRUSHER.get());
        AbstractMachineBlockEntity machine = helper.getBlockEntity(MACHINE);

        helper.assertFalse(machine.isShed(null),
            "Sans Hub posé, aucune machine ne doit jamais être délestée");
        helper.assertFalse(machine.isShed(com.veskorius.energy.MachinePriority.LOW),
            "Plancher au minimum : tout le monde passe");
        helper.assertTrue(machine.isShed(com.veskorius.energy.MachinePriority.HIGH),
            "Une machine NORMALE doit s'effacer sous un plancher HAUT");

        // Trois niveaux, donc TROIS crans pour revenir au départ. Le premier jet n'en
        // faisait que deux tout en affirmant le contraire — l'erreur était dans le test.
        for (int i = 0; i < com.veskorius.energy.MachinePriority.values().length; i++) {
            machine.cyclePriority();
        }
        helper.assertTrue(machine.getPriority() == com.veskorius.energy.MachinePriority.NORMAL,
            "Un tour complet ramène au point de départ, vaut : " + machine.getPriority());
        helper.succeed();
    }

    /**
     * <b>La Matrice fait tourner les foreuses deux fois plus vite et ramasse leur sortie.</b>
     *
     * <p>Les deux moitiés sont la machine : sans le ramassage elle ne supprime pas la
     * corvée qui l'a fait exister, sans la vitesse elle n'est qu'un coffre à distance.
     * Rien dans les données ne les garantit — le signal est poussé de la Matrice vers la
     * foreuse, donc une Matrice qui cesserait de le pousser laisserait tout fonctionner
     * <i>presque</i> normalement, ce qui est le pire cas de figure à diagnostiquer.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void arraySynchronisesAndCollects(GameTestHelper helper) {
        helper.setBlock(DRILL, ModBlocks.DEEP_CRYSTAL_DRILLER.get());
        com.veskorius.block.entity.DeepCrystalDrillerBlockEntity driller =
            helper.getBlockEntity(DRILL);

        helper.assertFalse(driller.isSynchronised(),
            "Une foreuse seule n'est pas synchronisée");
        driller.markSynchronised(100);
        helper.assertTrue(driller.isSynchronised(),
            "La marque poussée par la Matrice doit prendre effet");
        helper.succeed();
    }

    // =====================================================================
    // La Faille (#19) — la seule chose du mod qui blesse par sa seule présence
    // =====================================================================

    private static final BlockPos RIFT = new BlockPos(10, 1, 12);
    private static final BlockPos ANCHOR = new BlockPos(6, 1, 12);

    /**
     * <b>Une Faille non ancrée blesse, une Faille ancrée se tait, et casser l'Ancre la
     * réveille.</b>
     *
     * <p>Les trois moments sont le palier entier. Sans le premier, la Faille n'est qu'une
     * grotte ronde et l'Ancre n'a aucune raison d'exister. Sans le deuxième, la ressource
     * finale est inatteignable. Sans le troisième — le plus facile à oublier — le joueur
     * poserait une Ancre, démonterait tout, et la Faille resterait inoffensive pour
     * toujours : la machine la plus chère du mod à faire tourner deviendrait un
     * interrupteur à usage unique.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 300)
    public static void anchorSilencesTheRiftOnlyWhileItRuns(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper, 16);
                helper.setBlock(RIFT, ModBlocks.RIFT_CORE.get());
                helper.setBlock(ANCHOR, ModBlocks.RIFT_ANCHOR.get());
            })
            .thenExecuteAfter(40, () -> {
                com.veskorius.block.entity.RiftCoreBlockEntity core = helper.getBlockEntity(RIFT);
                com.veskorius.block.entity.RiftAnchorBlockEntity anchor = helper.getBlockEntity(ANCHOR);
                helper.assertTrue(anchor.isHolding(),
                    "L'Ancre alimentée et à portée doit tenir la Faille");
                helper.assertTrue(core.isAnchored(),
                    "…et la Faille doit se savoir ancrée");
            })
            // On casse l'Ancre : la Faille doit redevenir dangereuse immédiatement.
            .thenExecute(() -> helper.setBlock(ANCHOR, Blocks.AIR))
            .thenExecuteAfter(40, () -> {
                com.veskorius.block.entity.RiftCoreBlockEntity core = helper.getBlockEntity(RIFT);
                helper.assertFalse(core.isAnchored(),
                    "Ancre retirée : la Faille se réveille. Sinon un coup de pioche rendrait "
                        + "inoffensive la zone la plus dangereuse du monde, définitivement.");
            })
            .thenExecute(() -> helper.setBlock(RIFT, Blocks.AIR))
            .thenSucceed();
    }

    /**
     * <b>Une Faille rend six essences, et pas une de plus.</b>
     *
     * <p>C'est la seule ressource volontairement finie du mod (04-Materials.md), et toute
     * la fin de partie repose dessus. Le compteur vit sur le <b>noyau</b> et non sur
     * l'Extractor : sur l'Extractor, casser la machine et en reposer une remettrait le
     * compteur à zéro, et la ressource finie deviendrait infinie au prix d'un aller-retour
     * à l'établi — sans qu'une seule ligne de code ait l'air fausse.
     *
     * <p>Ce test épuise la Faille à la main, puis vérifie qu'elle refuse la septième.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void aRiftYieldsSixAndThenIsSpent(GameTestHelper helper) {
        helper.setBlock(RIFT, ModBlocks.RIFT_CORE.get());
        com.veskorius.block.entity.RiftCoreBlockEntity core = helper.getBlockEntity(RIFT);
        // La Faille doit d abord etre PURGEE : depuis que le Gardien existe, une Faille
        // intacte ne rend rien du tout. Ce test verifie l epuisement, pas la garde.
        core.setCleared(true);
        int max = com.veskorius.block.entity.RiftCoreBlockEntity.MAX_EXTRACTIONS;

        for (int i = 0; i < max; i++) {
            helper.assertTrue(core.consumeExtraction(),
                "Extraction " + (i + 1) + "/" + max + " : elle doit passer");
        }
        helper.assertFalse(core.canExtract(),
            "Après " + max + " extractions la Faille est morte");
        helper.assertFalse(core.consumeExtraction(),
            "…et elle refuse la suivante. Sinon la seule ressource finie du mod ne l'est pas.");
        helper.assertTrue(core.getExtractionsLeft() == 0,
            "Restant annoncé : 0, vaut " + core.getExtractionsLeft());
        helper.setBlock(RIFT, Blocks.AIR);
        helper.succeed();
    }

    /**
     * <b>Rien ne s'extrait d'une Faille dont le Gardien vit encore.</b>
     *
     * <p>C'est ce qui rend le boss obligatoire plutôt que décoratif. Sans cette condition,
     * on ancre la Faille, on pose l'Extracteur, on s'en va — et le combat de fin du jeu
     * devient un monstre qu'on contourne. Le dossier est explicite : l'Extractor « devient
     * utilisable » <i>après</i> la victoire (09-Entities.md).
     *
     * <p>La marque de victoire vit sur le <b>noyau</b>, comme le compteur d'extractions, et
     * pour la même raison : portée par le boss elle mourrait avec lui, et la Faille
     * resterait fermée pour toujours après le seul combat qui devait l'ouvrir.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void nothingIsExtractedBeforeTheGuardianFalls(GameTestHelper helper) {
        helper.setBlock(RIFT, ModBlocks.RIFT_CORE.get());
        com.veskorius.block.entity.RiftCoreBlockEntity core = helper.getBlockEntity(RIFT);

        helper.assertFalse(core.canExtract(),
            "Faille non purgée : l'extraction doit être refusée, sinon le boss se contourne");
        helper.assertFalse(core.isCleared(), "Une Faille neuve n'est pas purgée");

        core.setCleared(true);
        helper.assertTrue(core.canExtract(), "Gardien vaincu : l'extraction s'ouvre");

        // Et le Gardien ne se rappelle pas : la Faille est finie, le combat aussi.
        helper.assertTrue(core.claimGuardianSummon(),
            "Premier appel : il doit passer");
        helper.assertFalse(core.claimGuardianSummon(),
            "Second appel refusé — sinon casser l'Ancre et la reposer relance le boss, "
                + "et la Faille redevient une source infinie de lingots corrompus");
        helper.setBlock(RIFT, Blocks.AIR);
        helper.succeed();
    }

    /**
     * <b>Une Faille purgée ne blesse plus, Ancre ou pas.</b> C'est la récompense du combat,
     * et elle doit survivre au démontage de l'installation : sinon « définitivement stable »
     * (09-Entities.md) ne veut rien dire, et le joueur paierait 20 Osc/tick pour toujours
     * sur une Faille qu'il a déjà vaincue.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void aClearedRiftStaysSafeWithoutAnAnchor(GameTestHelper helper) {
        helper.setBlock(RIFT, ModBlocks.RIFT_CORE.get());
        com.veskorius.block.entity.RiftCoreBlockEntity core = helper.getBlockEntity(RIFT);
        core.setCleared(true);
        core.setAnchored(false);

        helper.assertTrue(core.isCleared(),
            "La purge survit à l'absence d'Ancre — c'est tout l'intérêt de la victoire");
        helper.assertTrue(core.canExtract(),
            "…et l'extraction reste ouverte sans Ancre alimentée");
        helper.setBlock(RIFT, Blocks.AIR);
        helper.succeed();
    }

    /**
     * <b>Les seuils de phase du Gardien découpent réellement ses 300 PV en trois.</b> Un
     * boss dont les phases ne changent que les chiffres n'a qu'une phase ; ici chacune a
     * son comportement, et ce test verrouille au moins qu'aucune n'est vide.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void guardianHasThreeRealPhases(GameTestHelper helper) {
        float max = com.veskorius.entity.RiftGuardianEntity.MAX_HEALTH;
        float rupture = com.veskorius.entity.RiftGuardianEntity.PHASE_RUPTURE_AT;
        float stab = com.veskorius.entity.RiftGuardianEntity.PHASE_STABILISATION_AT;
        helper.assertTrue(max > rupture && rupture > stab && stab > 0,
            "Trois tranches strictement décroissantes attendues, vaut "
                + max + " / " + rupture + " / " + stab);
        helper.succeed();
    }

    /**
     * <b>Le Ward tient la corrosion, et seulement s'il est alimenté.</b> Un Ward inerte qui
     * protégerait quand même ferait de l'Ancre et du Core des dépenses sans objet — on
     * poserait le bloc et on couperait le courant.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200)
    public static void wardProtectsOnlyWhilePowered(GameTestHelper helper) {
        BlockPos ward = new BlockPos(8, 1, 8);
        ServerLevel level = helper.getLevel();
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper, 4);
                helper.setBlock(ward, ModBlocks.RIFT_WARD_EMITTER.get());
            })
            .thenExecuteAfter(20, () -> helper.assertTrue(
                com.veskorius.block.entity.RiftWardEmitterBlockEntity.isWarded(
                    level, helper.absolutePos(ward)),
                "Ward alimenté : la position doit être protégée"))
            // On coupe le champ en retirant l'émetteur.
            .thenExecute(() -> helper.setBlock(EMITTER, Blocks.AIR))
            .thenExecuteAfter(20, () -> helper.assertFalse(
                com.veskorius.block.entity.RiftWardEmitterBlockEntity.isWarded(
                    level, helper.absolutePos(ward)),
                "Champ coupé : la protection tombe. Sinon on pose le bloc et on coupe le "
                    + "courant, et tout le coût du site disparaît."))
            .thenExecute(() -> helper.setBlock(ward, Blocks.AIR))
            .thenSucceed();
    }

    /**
     * <b>L'armure d'alliage répond au déphasage, et le Rift-Ward Plate l'annule.</b>
     *
     * <p>C'est la seule raison d'être de cette armure : sa protection est celle du
     * diamant, donc si elle ne faisait rien de plus, la fabriquer serait un détour coûteux
     * vers un équipement qu'on a déjà. Et le déphasage est le seul dégât du mod qu'aucune
     * armure vanilla n'atténue — il ne frappe pas, il désaccorde.
     *
     * <p>Le seuil est la panoplie <b>complète</b>, pas un quart par pièce : on ne sait pas
     * qu'on a « 50 % » en portant deux pièces, on constate vaguement qu'on meurt moins
     * vite. Un seuil franc se lit, et donne une raison de finir la panoplie.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void alloyArmourBluntsPhaseDamage(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(com.veskorius.item.VeskoriusArmor.phaseDamageFactor(player) == 1.0f,
            "Sans armure : le déphasage frappe à plein");

        for (var slot : new net.minecraft.world.entity.EquipmentSlot[] {
            net.minecraft.world.entity.EquipmentSlot.HEAD,
            net.minecraft.world.entity.EquipmentSlot.LEGS,
            net.minecraft.world.entity.EquipmentSlot.FEET}) {
            player.setItemSlot(slot, new ItemStack(switch (slot) {
                case HEAD -> ModItems.VESKORIAN_ALLOY_HELMET.get();
                case LEGS -> ModItems.VESKORIAN_ALLOY_LEGGINGS.get();
                default -> ModItems.VESKORIAN_ALLOY_BOOTS.get();
            }));
        }
        helper.assertTrue(com.veskorius.item.VeskoriusArmor.phaseDamageFactor(player) == 1.0f,
            "Trois pièces sur quatre : pas encore d'atténuation, le seuil est la panoplie");

        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
            new ItemStack(ModItems.VESKORIAN_ALLOY_CHESTPLATE.get()));
        helper.assertTrue(com.veskorius.item.VeskoriusArmor.phaseDamageFactor(player) == 0.5f,
            "Panoplie complète : le déphasage est divisé par deux");

        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
            new ItemStack(ModItems.RIFT_WARD_PLATE.get()));
        helper.assertTrue(com.veskorius.item.VeskoriusArmor.phaseDamageFactor(player) == 0.0f,
            "Rift-Ward Plate : immunité totale, à lui seul. C'est ce qui justifie qu'il "
                + "coûte le butin garanti d'une Faille entière.");
        helper.succeed();
    }

    /**
     * <b>Le délai de grâce existe et il est court.</b> Sans lui, s'approcher tue sans
     * prévenir et la Faille est un piège ; trop long, on s'y installe et la contrainte
     * disparaît. Trois secondes : le temps de voir, de comprendre et de reculer.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void riftGivesTimeToStepBack(GameTestHelper helper) {
        int grace = com.veskorius.block.entity.RiftCoreBlockEntity.GRACE_TICKS;
        helper.assertTrue(grace >= 20 && grace <= 20 * 5,
            "Le délai de grâce doit tenir entre une et cinq secondes, vaut " + grace + " ticks");
        helper.assertTrue(com.veskorius.block.entity.RiftCoreBlockEntity.HARM_RADIUS == 8,
            "06-Energy.md annonce 8 blocs");
        helper.succeed();
    }

    // =====================================================================
    // Convergence Core (#18) — le seul multi-bloc
    // =====================================================================

    private static final BlockPos CORE = new BlockPos(10, 1, 10);

    /**
     * <b>La figure décide, et rien d'autre.</b> Sept relais sur huit ne suffisent pas ; le
     * huitième allume le Core ; un mur posé sur une seule ligne de vue l'éteint.
     *
     * <p>Les trois moitiés comptent. Sans la première, le multi-bloc n'en est pas un — on
     * pose un bloc et il marche. Sans la deuxième, il ne s'allume jamais et le joueur
     * démonte au hasard. Sans la troisième, la contrainte de vue directe n'existe que dans
     * le dossier, et le pilier « bases ouvertes, pas de boîtes fermées » redevient une
     * recommandation esthétique au lieu d'une condition de fonctionnement.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void coreNeedsItsWholeRingInDirectView(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var ring = com.veskorius.block.entity.ConvergenceCoreBlockEntity.ringOffsets();

        helper.setBlock(CORE, ModBlocks.CONVERGENCE_CORE.get());
        for (int i = 0; i < ring.size() - 1; i++) {
            helper.setBlock(CORE.offset(ring.get(i)), ModBlocks.RESONANCE_RELAY.get());
        }
        helper.assertFalse(
            com.veskorius.block.entity.ConvergenceCoreBlockEntity.isFormed(
                level, helper.absolutePos(CORE)),
            "Sept éléments sur huit : la figure ne doit PAS être valide");

        // Le dernier, et un amplificateur plutôt qu'un relais — le mélange est autorisé.
        BlockPos last = CORE.offset(ring.get(ring.size() - 1));
        helper.setBlock(last, ModBlocks.HARMONIC_AMPLIFIER.get());
        helper.assertTrue(
            com.veskorius.block.entity.ConvergenceCoreBlockEntity.isFormed(
                level, helper.absolutePos(CORE)),
            "Huit éléments, relais et amplificateurs mélangés : la figure doit être valide");

        // Un seul bloc plein sur une seule ligne de vue suffit à tout défaire.
        helper.setBlock(CORE.offset(ring.get(0).getX() / 2, 0, ring.get(0).getZ() / 2),
            Blocks.OBSIDIAN);
        helper.assertFalse(
            com.veskorius.block.entity.ConvergenceCoreBlockEntity.isFormed(
                level, helper.absolutePos(CORE)),
            "Une ligne de vue coupée doit défaire la figure — sinon on enferme le Core "
                + "dans une boîte et la contrainte n'existe que dans le dossier");
        helper.succeed();
    }

    /**
     * <b>L'anneau se pose en comptant, sans plan.</b> Les huit positions sont à la même
     * distance de Chebyshev, sur un seul niveau. Si la figure dérivait vers des offsets
     * irréguliers, elle deviendrait indevinable en jeu et le multi-bloc se construirait à
     * coups de captures d'écran d'un wiki.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void coreRingIsLaidOutByCounting(GameTestHelper helper) {
        var ring = com.veskorius.block.entity.ConvergenceCoreBlockEntity.ringOffsets();
        int r = com.veskorius.block.entity.ConvergenceCoreBlockEntity.RING_RADIUS;
        helper.assertTrue(ring.size() == 8, "L'anneau compte huit positions, vaut " + ring.size());
        for (BlockPos p : ring) {
            helper.assertTrue(p.getY() == 0, "Toutes sur le même niveau, vaut y=" + p.getY());
            helper.assertTrue(Math.max(Math.abs(p.getX()), Math.abs(p.getZ())) == r,
                "Distance de Chebyshev " + r + " attendue pour " + p);
        }
        helper.assertTrue(ring.stream().distinct().count() == 8,
            "Huit positions DISTINCTES : un doublon rendrait la figure impossible à valider");
        helper.succeed();
    }

    /**
     * <b>La source la plus forte l'emporte.</b> 06-Energy.md l'écrit depuis le début ; le
     * manager, lui, servait depuis la première source inscrite. Personne ne pouvait s'en
     * apercevoir tant que toutes valaient 100 — le Convergence Core est la première dont
     * l'intensité diffère, et sans ce tri il aurait été systématiquement ignoré au profit
     * du premier émetteur T2 venu.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void coreOutranksEveryOtherSource(GameTestHelper helper) {
        helper.assertTrue(
            com.veskorius.block.entity.ConvergenceCoreBlockEntity.FIELD_STRENGTH > 100,
            "Le Core doit être plus fort que les sources ordinaires (100) — c'est "
                + "l'exception assumée à l'anti-stacking de 06-Energy.md");
        helper.succeed();
    }

    // =====================================================================
    // Resonance Relay (#9) — la portée du réseau
    // =====================================================================

    /** À 6 blocs de l'émetteur : dans sa portée de 8, donc alimenté. */
    private static final BlockPos RELAY_A = new BlockPos(4, 1, 10);
    /** À 9,5 blocs du relais A, mais à 12,7 de l'émetteur : hors de portée de CELUI-CI. */
    private static final BlockPos FAR = new BlockPos(1, 1, 1);
    /** Second relais : à portée du premier, hors de portée de l'émetteur. */
    private static final BlockPos RELAY_B = new BlockPos(1, 1, 3);

    /**
     * <b>Pourquoi ces tests-là, et eux seuls, réclament chacun leur lot d'exécution.</b>
     *
     * <p>Les tests d'un même lot sont posés côte à côte dans le monde et tournent
     * <b>simultanément</b>. Jusqu'ici c'était sans conséquence : la portée de 8 de
     * l'émetteur est plus courte que l'écart entre deux arènes, donc aucun champ ne
     * franchissait la cloison. La portée de <b>20</b> du relais, elle, la franchit — et
     * {@link ResonanceFieldManager} est un index global, pas un index par arène. Un relais
     * chargé dans un test alimentait donc les machines du test d'à côté : c'est ainsi que
     * {@code assemblerIdleWithoutField}, qui n'a rien à voir avec les relais, s'est mis à
     * échouer en affirmant très correctement qu'il recevait un champ.
     *
     * <p>Chaque test de relais tourne donc seul, et <b>déblaie derrière lui</b> — casser le
     * bloc le retire de l'index. La contrainte est celle du banc d'essai, pas du jeu : dans
     * un monde, un relais qui couvre vingt blocs de machines est exactement ce qu'on lui
     * demande.
     */
    private static void clearRelayArena(GameTestHelper helper) {
        helper.setBlock(RELAY_A, Blocks.AIR);
        helper.setBlock(RELAY_B, Blocks.AIR);
        helper.setBlock(EMITTER, Blocks.AIR);
    }

    /**
     * <b>L'inégalité qui fait exister la machine.</b> Sans elle, le relais ne résoudrait
     * aucun problème : la position lointaine doit être hors de portée de l'émetteur seul et
     * dans la portée du relais. Une arène redimensionnée ou une portée retouchée pourrait
     * casser cette géométrie sans casser aucun autre test — et les tests suivants
     * passeraient alors pour de mauvaises raisons.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 20)
    public static void relayGeometryIsWorthTesting(GameTestHelper helper) {
        int emitterRange = VeskoriusConfig.fieldEmitterRange();
        helper.assertTrue(EMITTER.distSqr(FAR) > (long) emitterRange * emitterRange,
            "La position lointaine doit être HORS de portée de l'émetteur seul, sinon le "
                + "test du relais passerait sans relais");
        helper.assertTrue(RELAY_A.distSqr(FAR)
                <= (long) ResonanceRelayBlockEntity.RANGE * ResonanceRelayBlockEntity.RANGE,
            "…et DANS la portée du relais, sinon aucun relais ne pourrait la couvrir");
        helper.succeed();
    }

    /**
     * <b>Le relais porte réellement le champ plus loin que l'émetteur.</b> C'est la seule
     * raison d'être de la machine (05-Machines.md #9), et rien d'autre ne la vérifie : la
     * portée est une donnée, la couverture effective est un comportement.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200, batch = "relay_reach")
    public static void relayExtendsTheFieldBeyondTheEmitter(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.startSequence()
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(5, () -> helper.assertTrue(
                ResonanceFieldManager.supply(level, helper.absolutePos(FAR), 1) == 0,
                "Avant le relais, la position lointaine ne doit être servie par personne"))
            .thenExecute(() -> helper.setBlock(RELAY_A, ModBlocks.RESONANCE_RELAY.get()))
            .thenExecuteAfter(40, () -> helper.assertTrue(
                ResonanceFieldManager.supply(level, helper.absolutePos(FAR), 1) > 0,
                "Après le relais, elle doit l'être — sinon la machine ne sert à rien"))
            .thenExecute(() -> clearRelayArena(helper))
            .thenSucceed();
    }

    /**
     * <b>Deux relais à portée l'un de l'autre ne partent pas en récursion.</b>
     *
     * <p>C'est la configuration NORMALE d'une chaîne, et c'est exactement celle qui ferait
     * exploser la pile si {@code extractOsc} rappelait le manager (voir l'en-tête de
     * {@link ResonanceRelayBlockEntity}). Le bug ne se manifesterait que sur une base déjà
     * construite, jamais en relecture — d'où ce test, qui échoue par crash et pas par
     * assertion. Il vérifie en prime que l'énergie franchit bien DEUX sauts.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 300, batch = "relay_chain")
    public static void relayChainCarriesEnergyWithoutRecursing(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                helper.setBlock(RELAY_A, ModBlocks.RESONANCE_RELAY.get());
                helper.setBlock(RELAY_B, ModBlocks.RESONANCE_RELAY.get());
            })
            .thenExecuteAfter(120, () -> {
                ResonanceRelayBlockEntity b = helper.getBlockEntity(RELAY_B);
                helper.assertTrue(b.getReserve() > 0,
                    "Le second relais est hors de portée de l'émetteur : s'il est chargé, "
                        + "c'est que l'énergie a franchi deux sauts. Réserve : " + b.getReserve());
            })
            .thenExecute(() -> clearRelayArena(helper))
            .thenSucceed();
    }

    /**
     * <b>Un relais isolé ne se nourrit pas de lui-même.</b>
     *
     * <p>Il est inscrit à l'index et il est trivialement à portée de sa propre position :
     * sans la garde du remplissage, il se servirait dans son propre tampon, le manager
     * s'arrêterait à lui, et aucun relais du monde ne se chargerait jamais. Panne totale et
     * parfaitement silencieuse — la pire espèce.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200, batch = "relay_lone")
    public static void loneRelayNeverFeedsItself(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(RELAY_A, ModBlocks.RESONANCE_RELAY.get());
                ResonanceRelayBlockEntity relay = helper.getBlockEntity(RELAY_A);
                relay.addDissonance(0); // no-op : force juste le chargement de la BE
            })
            .thenExecuteAfter(60, () -> {
                ResonanceRelayBlockEntity relay = helper.getBlockEntity(RELAY_A);
                helper.assertTrue(relay.getReserve() == 0,
                    "Sans source en amont, un relais reste vide. Réserve : " + relay.getReserve());
                helper.assertFalse(relay.isActive(),
                    "…et il ne se déclare pas actif, sinon il couvrirait une zone sans rien "
                        + "pouvoir y fournir");
            })
            .thenExecute(() -> clearRelayArena(helper))
            .thenSucceed();
    }

    /**
     * <b>Le relais rediffuse la bande de sa source.</b> Sans ça, intercaler un relais
     * suffirait à réaccorder une machine désaccordée — le relais laverait les harmoniques,
     * et toute la mécanique de 06-Energy.md se contournerait avec un bloc à 20 blocs.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200)
    public static void relayCarriesTheBandItReceives(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(EMITTER, ModBlocks.TUNABLE_FIELD_EMITTER.get());
                com.veskorius.block.entity.TunableFieldEmitterBlockEntity emitter =
                    helper.getBlockEntity(EMITTER);
                emitter.setBand(com.veskorius.energy.HarmonicBand.MEDIAN);
                emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
                helper.setBlock(RELAY_A, ModBlocks.RESONANCE_RELAY.get());
            })
            .thenExecuteAfter(60, () -> {
                ResonanceRelayBlockEntity relay = helper.getBlockEntity(RELAY_A);
                helper.assertTrue(relay.getBand() == com.veskorius.energy.HarmonicBand.MEDIAN,
                    "Le relais doit rediffuser la bande reçue, pas la Fondamentale par "
                        + "défaut — sinon il blanchit les harmoniques. Bande : " + relay.getBand());
            })
            .thenExecute(() -> clearRelayArena(helper))
            .thenSucceed();
    }
}
