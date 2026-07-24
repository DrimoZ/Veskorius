package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.ComponentAssemblerBlockEntity;
import com.veskorius.block.entity.DampingArrayBlockEntity;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.FluxPurifierBlockEntity;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import com.veskorius.block.entity.TunableFieldEmitterBlockEntity;
import com.veskorius.config.HarmonicsConfig;
import com.veskorius.energy.HarmonicBand;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.event.FieldHudHandler;
import com.veskorius.item.ModItems;
import com.veskorius.network.FieldHudPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
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
    private static final BlockPos DAMPER = new BlockPos(10, 1, 14);

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
                // État de lecture visuelle (le glow clignote entre les deux couleurs) :
                // la machine se sait désaccordée et connaît la bande du champ.
                helper.assertTrue(purifier.isDetuned(),
                    "Une machine désaccordée doit se signaler comme telle (glow clignotant)");
                helper.assertTrue(purifier.getFieldBand() == HarmonicBand.FUNDAMENTAL,
                    "La machine doit connaître la bande du champ pour clignoter entre les deux");
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

    /** L'Émetteur Accordable fait défiler sa bande (c'est lui qui porte le choix). */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void tunableEmitterCyclesBand(GameTestHelper helper) {
        helper.setBlock(EMITTER, ModBlocks.TUNABLE_FIELD_EMITTER.get());
        TunableFieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);

        helper.assertTrue(emitter.getBand() == HarmonicBand.FUNDAMENTAL,
            "Un Émetteur Accordable démarre sur la Fondamentale");
        emitter.cycleBand();
        helper.assertTrue(emitter.getBand() == HarmonicBand.MEDIAN,
            "Le cycle doit passer à la Médiane, vaut : " + emitter.getBand());
        emitter.cycleBand();
        helper.assertTrue(emitter.getBand() == HarmonicBand.HIGH,
            "Puis à la Haute, vaut : " + emitter.getBand());
        helper.succeed();
    }

    /**
     * Le cœur du système : une machine <b>accordée sur la bande de l'émetteur</b>
     * travaille proprement — pas de surcoût, pas de dissonance. C'est ce qui rend les
     * bandes utiles (router l'énergie) plutôt que punitives.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 200)
    public static void machineMatchingTunableEmitterIsAccorded(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(EMITTER, ModBlocks.TUNABLE_FIELD_EMITTER.get());
                TunableFieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                emitter.setBand(HarmonicBand.MEDIAN);
                emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);

                helper.setBlock(PURIFIER, ModBlocks.FLUX_PURIFIER.get());
                FluxPurifierBlockEntity purifier = helper.getBlockEntity(PURIFIER);
                purifier.setHarmonicBand(HarmonicBand.MEDIAN); // accordée sur le champ
                purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_CRYSTAL,
                    new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 64), false);
                purifier.getInventory().insertItem(FluxPurifierBlockEntity.SLOT_REDSTONE,
                    new ItemStack(Items.REDSTONE, 64), false);
            })
            .thenExecuteAfter(100, () -> {
                TunableFieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                FluxPurifierBlockEntity purifier = helper.getBlockEntity(PURIFIER);

                helper.assertFalse(purifier.isDetunedFrom(emitter),
                    "Même bande que le champ = accordée");
                helper.assertTrue(emitter.getDissonance() == 0,
                    "Une machine accordée ne produit aucune dissonance, vaut : "
                        + emitter.getDissonance());
                helper.assertTrue(purifier.getData().get(
                        com.veskorius.block.entity.AbstractMachineBlockEntity.DATA_PROGRESS) > 0,
                    "La machine accordée doit tourner normalement");
                helper.assertFalse(purifier.isDetuned(),
                    "Une machine accordée ne doit pas clignoter (glow d'une seule couleur)");
            })
            .thenSucceed();
    }

    /**
     * Le mode « Accorder » est <b>réversible</b> : le cycle repasse par l'universel. Sans
     * ça, un joueur qui accorde par curiosité dégraderait sa machine sans retour possible.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void attuningIsReversible(GameTestHelper helper) {
        helper.setBlock(MACHINE, ModBlocks.FLUX_PURIFIER.get());
        FluxPurifierBlockEntity purifier = helper.getBlockEntity(MACHINE);

        helper.assertTrue(purifier.supportsHarmonicBand(),
            "Le Flux Purifier est la machine accordable de la T2");
        helper.assertTrue(purifier.getHarmonicBand() == null,
            "Elle reste universelle par défaut : la T2 ne gagne aucune décision imposée");

        for (int i = 0; i < HarmonicsConfig.bandCount(); i++) {
            purifier.cycleHarmonicBand();
            helper.assertTrue(purifier.getHarmonicBand() != null,
                "Le cycle doit passer par chaque bande, étape " + i);
        }
        purifier.cycleHarmonicBand();
        helper.assertTrue(purifier.getHarmonicBand() == null,
            "Le cycle doit revenir à l'universel, vaut : " + purifier.getHarmonicBand());
        helper.succeed();
    }

    /**
     * Le Damping Array nettoie le champ : il consomme un agent, retire sa valeur en
     * dissonance et <b>cristallise le déchet</b>. C'est la boucle d'entretien complète.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 250)
    public static void dampingArrayCleansFieldAndProducesSludge(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                emitter.addDissonance(1000);

                helper.setBlock(DAMPER, ModBlocks.DAMPING_ARRAY.get());
                DampingArrayBlockEntity damper = helper.getBlockEntity(DAMPER);
                damper.getInventory().insertItem(DampingArrayBlockEntity.SLOT_AGENT,
                    new ItemStack(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 4), false);
            })
            .thenExecuteAfter(HarmonicsConfig.dampingCycleTicks() + 10, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                DampingArrayBlockEntity damper = helper.getBlockEntity(DAMPER);

                helper.assertTrue(emitter.getDissonance() < 1000,
                    "Le Damping Array doit avoir retiré de la dissonance, vaut : "
                        + emitter.getDissonance());
                helper.assertTrue(
                    damper.getInventory().getStackInSlot(DampingArrayBlockEntity.SLOT_OUTPUT)
                        .is(ModItems.RESONANCE_SLUDGE.get()),
                    "La dissonance retirée doit se cristalliser en resonance_sludge");
                helper.assertTrue(
                    damper.getInventory().getStackInSlot(DampingArrayBlockEntity.SLOT_AGENT)
                        .getCount() < 4,
                    "Un agent de damping doit avoir été consommé");
            })
            .thenSucceed();
    }

    /** Champ propre : l'Array reste inerte et ne gaspille pas son agent. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 250)
    public static void dampingArrayIdlesOnCleanField(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper); // aucune dissonance
                helper.setBlock(DAMPER, ModBlocks.DAMPING_ARRAY.get());
                DampingArrayBlockEntity damper = helper.getBlockEntity(DAMPER);
                damper.getInventory().insertItem(DampingArrayBlockEntity.SLOT_AGENT,
                    new ItemStack(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 4), false);
            })
            .thenExecuteAfter(HarmonicsConfig.dampingCycleTicks() + 10, () -> {
                DampingArrayBlockEntity damper = helper.getBlockEntity(DAMPER);
                helper.assertTrue(
                    damper.getInventory().getStackInSlot(DampingArrayBlockEntity.SLOT_AGENT)
                        .getCount() == 4,
                    "Sur un champ propre, aucun agent ne doit être consommé");
                helper.assertTrue(
                    damper.getInventory().getStackInSlot(DampingArrayBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Sur un champ propre, aucun déchet ne doit être produit");
            })
            .thenSucceed();
    }

    /**
     * HUD de champ : il n'est alimenté que pour un joueur qui <b>porte</b> l'objet de
     * lecture (le Locator). C'est ce qui en fait un instrument qu'on emporte plutôt qu'un
     * affichage permanent — et ce qui garantit qu'un serveur n'émet aucun paquet pour les
     * joueurs qui ne l'ont pas.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void fieldHudOnlyForCarriers(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(FieldHudHandler.carriesReader(player),
            "Sans Locator, aucun HUD (et aucun paquet)");

        player.getInventory().add(new ItemStack(ModItems.RESONANCE_LOCATOR.get()));
        helper.assertTrue(FieldHudHandler.carriesReader(player),
            "Le Locator dans l'inventaire suffit — Curios reste un confort, jamais un prérequis");
        helper.succeed();
    }

    /**
     * Le HUD lit le champ qui <b>couvre</b> le joueur, actif ou non : un émetteur saturé
     * (donc instable, donc intermittent) doit rester affiché — c'est précisément le moment
     * où le joueur a besoin de le voir. Verrouille la distinction entre
     * {@code coveringSource} et {@code findSource}.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 100)
    public static void fieldHudReadsSaturatedField(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> chargedEmitter(helper))
            .thenExecuteAfter(10, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                emitter.addDissonance(HarmonicsConfig.dissonanceCapacity());
                helper.assertTrue(emitter.isUnstable(), "Champ saturé = instable");

                IResonanceField covering = ResonanceFieldManager.coveringSource(
                    helper.getLevel(), helper.absolutePos(PURIFIER));
                helper.assertTrue(covering != null,
                    "Le HUD doit voir le champ même quand il est instable");

                FieldHudPayload reading = FieldHudHandler.of(covering);
                helper.assertTrue(reading.unstable(), "La lecture doit signaler l'instabilité");
                helper.assertTrue(reading.band() == HarmonicBand.FUNDAMENTAL.ordinal(),
                    "La lecture doit porter la bande du champ, vaut : " + reading.band());
                helper.assertTrue(reading.dissonance() > 0,
                    "La lecture doit porter la dissonance du champ");
                helper.assertTrue(reading.reserve() > 0 && reading.capacity() > 0,
                    "La lecture doit porter la réserve, vaut : " + reading.reserve()
                        + "/" + reading.capacity());
            })
            .thenSucceed();
    }

    /**
     * Dernière étape de la dissonance : au plafond, le champ <b>décharge</b> — il purge
     * une partie de sa saturation (soupape) et blesse ce qui est à portée. C'est la
     * conséquence visible d'une dissonance laissée filer, jamais silencieuse.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 100)
    public static void saturatedFieldDischargesAndReleases(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                emitter.addDissonance(HarmonicsConfig.dissonanceCapacity());
                helper.assertTrue(emitter.shouldDischarge(),
                    "Au plafond, le champ doit être prêt à décharger");
            })
            .thenExecuteAfter(5, () -> {
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                // La soupape a évacué une fraction du plafond : plus au max, plus prêt
                // à re-décharger tant qu'il n'est pas remonté.
                helper.assertTrue(emitter.getDissonance() < HarmonicsConfig.dissonanceCapacity(),
                    "La décharge doit purger une partie de la dissonance, vaut : "
                        + emitter.getDissonance());
                helper.assertFalse(emitter.shouldDischarge(),
                    "Après décharge, le champ n'est plus au plafond");
            })
            .thenSucceed();
    }

    /** La décharge blesse ce qui se tient dans son rayon (l'onde AoE). */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 100)
    public static void dischargeDamagesNearbyEntities(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, EMITTER.offset(1, 0, 0));
        float fullHealth = cow.getHealth();
        helper.startSequence()
            .thenExecute(() -> {
                chargedEmitter(helper);
                FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
                emitter.addDissonance(HarmonicsConfig.dissonanceCapacity());
            })
            .thenExecuteAfter(5, () -> helper.assertTrue(cow.getHealth() < fullHealth,
                "La décharge doit blesser une entité à portée, PV : " + cow.getHealth()))
            .thenSucceed();
    }

    /**
     * Interrupteur : décharge désactivée, la dissonance reste plafonnée (le champ reste
     * instable) mais aucune impulsion ne part. Le modpack maker garde la main.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 60)
    public static void dischargeRespectsConfigSwitch(GameTestHelper helper) {
        chargedEmitter(helper);
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
        emitter.addDissonance(HarmonicsConfig.dissonanceCapacity());

        if (HarmonicsConfig.dischargeEnabled()) {
            // Défaut : activée — on vérifie juste que la décision est cohérente.
            helper.assertTrue(emitter.shouldDischarge(),
                "Décharge activée + plafond = doit décharger");
        } else {
            helper.assertFalse(emitter.shouldDischarge(),
                "Décharge désactivée = jamais d'impulsion");
        }
        helper.succeed();
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
