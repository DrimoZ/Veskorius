package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.FieldSensitiveBlock;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.entity.ModEntities;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tests des pièces de structure jigsaw (08-Structures.md pour le contenu, 17-Dungeons.md
 * pour la forme). On valide le <b>contenu</b> des pièces NBT générées et les trois
 * mécaniques qui font du donjon autre chose qu'une enfilade de salles : le sas, les
 * conduits, l'émetteur ancien.
 *
 * <p>Le câblage jigsaw lui-même (pools / structure / structure_set) est validé par les
 * codecs au datagen et par le chargement du serveur de test, qui échouerait si un
 * registre datapack était invalide.
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructureGameTests {

    private static final String FIELD_ARENA = "field_arena";
    /** Arène élargie : l'Avant-poste fait 21×20×21 et ne tient pas dans 21³. */
    private static final String PIECE_ARENA = "piece_arena";

    private static final BlockPos ANCHOR = new BlockPos(1, 1, 1);

    // Repères de la pièce de départ de l'Avant-poste (voir ModStructurePieceProvider) :
    // le chemin critique du T2, celui qui n'a le droit d'être ni tiré au sort ni usé.
    private static final BlockPos CONSOLE = ANCHOR.offset(10, 2, 17);
    private static final BlockPos BULKHEAD = ANCHOR.offset(10, 1, 13);
    private static final BlockPos ANCIENT_EMITTER = ANCHOR.offset(14, 1, 10);
    private static final BlockPos RESERVE_CHEST = ANCHOR.offset(12, 1, 10);
    private static final BlockPos BOOTSTRAP_CHEST = ANCHOR.offset(3, 1, 11);

    // =====================================================================
    // Le chemin critique
    // =====================================================================

    /**
     * <b>Tout le chemin critique du T2 est dans la pièce de départ.</b>
     *
     * <p>C'est l'invariant de 17-Dungeons.md §3, et il n'existait pas tant que la
     * profondeur d'assemblage valait 1 : il n'y avait qu'une pièce, donc rien à égarer.
     * Depuis que le jigsaw assemble réellement, déplacer par mégarde la console ou le sas
     * dans une aile facultative rendrait une partie des Avant-postes infranchissables —
     * exactement la classe de bug déjà trouvée deux fois sur le loot d'amorçage.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void outpostStartPieceCarriesTheWholeT2Gate(GameTestHelper helper) {
        place(helper, "outpost");

        helper.assertBlockPresent(ModBlocks.ATTUNEMENT_CONSOLE.get(), CONSOLE);
        helper.assertBlockPresent(ModBlocks.RESONANCE_BULKHEAD.get(), BULKHEAD);
        helper.assertBlockPresent(ModBlocks.ANCIENT_EMITTER.get(), ANCIENT_EMITTER);
        helper.assertBlockPresent(Blocks.CHEST, RESERVE_CHEST);
        helper.assertBlockPresent(Blocks.CHEST, BOOTSTRAP_CHEST);
        // Maçonnerie veskorienne, pas du deepslate vanilla : la coquille elle-même doit
        // dire de quelle civilisation on visite les ruines.
        helper.assertBlockPresent(ModBlocks.VEINED_STONE_BRICKS.get(), ANCHOR.offset(0, 0, 0));
        helper.assertEntityPresent(ModEntities.CUSTODE.get());
        assertChestHasLootTable(helper, BOOTSTRAP_CHEST);
        helper.succeed();
    }

    /**
     * <b>Le coffre-réserve contient de quoi ouvrir le sas, toujours.</b>
     *
     * <p>Le sas ne s'ouvre que dans un champ, et le seul champ disponible sur place vient
     * de l'émetteur ancien, qui est à sec. Un joueur arrivé sans Stable Crystal serait
     * donc coincé devant la porte du T2. Contenu <b>fixe</b> et non table de loot : une
     * table rendrait la porte du T2 aléatoire, ce que le pilier 2 interdit.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void outpostReserveChestHoldsTheFuelToOpenTheGate(GameTestHelper helper) {
        place(helper, "outpost");

        net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(RESERVE_CHEST);
        helper.assertTrue(be instanceof net.minecraft.world.Container,
            "Coffre-réserve attendu en " + RESERVE_CHEST + ", vaut : " + be);
        ItemStack stack = ((net.minecraft.world.Container) be).getItem(0);
        helper.assertTrue(stack.is(ModItems.STABLE_RESONANCE_CRYSTAL.get()) && stack.getCount() >= 1,
            "Le coffre-réserve doit contenir au moins un Stable Resonance Crystal (le "
                + "carburant qui réveille l'émetteur ancien), vaut : " + stack);
        helper.succeed();
    }

    /**
     * <b>Aucune aile facultative ne porte de chemin critique.</b> Le pendant du test
     * ci-dessus : on vérifie non seulement que la pièce de départ a tout, mais que les
     * pièces tirées au sort n'ont rien. Sans ça, l'invariant §3 pourrait se rompre en
     * ajoutant une aile « qui contient aussi une console, ce sera plus pratique ».
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 60)
    public static void optionalWingsNeverCarryTheCriticalPath(GameTestHelper helper) {
        for (String wing : new String[] {
            "outpost_wing_store", "outpost_wing_quarters", "outpost_wing_collapsed",
            "hamlet", "hamlet_dwelling", "hamlet_workshop", "hamlet_collapsed"}) {
            place(helper, wing);
            for (BlockPos pos : allPositions(helper, 12, 8, 12)) {
                BlockState state = helper.getBlockState(pos);
                helper.assertTrue(!state.is(ModBlocks.ATTUNEMENT_CONSOLE.get())
                        && !state.is(ModBlocks.RESONANCE_BULKHEAD.get())
                        && !state.is(ModBlocks.ANCIENT_EMITTER.get()),
                    "La pièce facultative « " + wing + " » porte un élément du chemin "
                        + "critique en " + pos + " (" + state + ") — il doit vivre dans la "
                        + "pièce de départ, jamais dans un pool");
            }
        }
        helper.succeed();
    }

    /**
     * <b>Les pièces portent de vrais connecteurs jigsaw.</b>
     *
     * <p>Le mod a longtemps annoncé des « structures en jigsaw » alors qu'aucune pièce ne
     * contenait un seul bloc jigsaw et que la profondeur d'assemblage valait 1 : ajouter
     * une pièce au pool <i>remplaçait</i> le bâtiment au lieu de l'agrandir. Rien ne
     * signalait l'écart — la structure se générait parfaitement, simplement toujours
     * seule. Ce test rend la promesse vérifiable.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void piecesExposeRealJigsawConnectors(GameTestHelper helper) {
        assertConnectors(helper, "outpost", 2);        // deux ailes facultatives
        assertConnectors(helper, "outpost_wing_store", 2); // entrée + chaînage
        assertConnectors(helper, "hamlet", 4);         // quatre directions
        assertConnectors(helper, "hamlet_dwelling", 2);
        assertConnectors(helper, "outpost_cap", 1);    // le bouchon en a un, et un seul
        helper.succeed();
    }

    private static void assertConnectors(GameTestHelper helper, String name, int expected) {
        StructureTemplate template = template(helper, name);
        int found = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.JIGSAW).size();
        helper.assertTrue(found == expected,
            "La pièce « " + name + " » doit porter " + expected + " connecteur(s) jigsaw, "
                + "trouvé : " + found);
    }

    /**
     * <b>Le journal de l'Avant-poste est complet et dans l'ordre.</b>
     *
     * <p>Les quatre fragments racontent une descente — routine, dérive, la nuit où le
     * réseau a chanté, l'abandon. Un fragment manquant ou interverti casse le seul arc
     * narratif suivi du mod, et c'est le genre de régression qu'on ne voit pas : la
     * structure se génère, les coffres sont là, seul le texte ne raconte plus rien.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void outpostCarriesOperatorLogInOrder(GameTestHelper helper) {
        place(helper, "outpost");

        ResourceLocation[] expected = {
            com.veskorius.item.CodexEntries.OUTPOST_LOG_1,
            com.veskorius.item.CodexEntries.OUTPOST_LOG_2,
            com.veskorius.item.CodexEntries.OUTPOST_LOG_3,
            com.veskorius.item.CodexEntries.OUTPOST_LOG_4,
        };
        for (int i = 0; i < expected.length; i++) {
            BlockPos at = ANCHOR.offset(4 + i * 3, 8, 19);
            net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(at);
            helper.assertTrue(be instanceof net.minecraft.world.Container,
                "Coffre d'archives attendu en " + at + ", vaut : " + be);
            ItemStack stack = ((net.minecraft.world.Container) be).getItem(0);
            helper.assertTrue(stack.is(ModItems.CODEX_FRAGMENT.get()),
                "Le coffre d'archives " + (i + 1) + " doit contenir un fragment, vaut : " + stack);
            ResourceLocation entry = com.veskorius.item.CodexFragmentItem.entryOf(stack);
            helper.assertTrue(expected[i].equals(entry),
                "Fragment " + (i + 1) + " : attendu " + expected[i] + ", trouvé " + entry
                    + " — le journal doit se lire dans l'ordre en traversant la salle");
        }
        helper.succeed();
    }

    // =====================================================================
    // Les trois mécaniques du donjon (17-Dungeons.md R1 et R2)
    // =====================================================================

    private static final BlockPos GATE = new BlockPos(2, 1, 2);
    private static final BlockPos LAMP = new BlockPos(2, 1, 4);
    private static final BlockPos CONDUIT = new BlockPos(4, 1, 2);
    private static final BlockPos EMITTER = new BlockPos(5, 1, 5);

    /**
     * <b>Le sas est fermé sans champ, ouvert avec.</b> La règle R1 du donjon veskorien :
     * la clé n'est jamais un objet, c'est de la Résonance qu'il faut amener sur place.
     * Si ce test tombe, tout le contenu derrière un sas devient soit inaccessible, soit
     * gratuit — et le pilier 3 redevient une mécanique d'usine qu'on laisse à la base.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 120)
    public static void bulkheadOpensOnlyInsideAField(GameTestHelper helper) {
        helper.setBlock(GATE, ModBlocks.RESONANCE_BULKHEAD.get());
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!helper.getBlockState(GATE).getValue(FieldSensitiveBlock.POWERED),
                "Sans champ, le sas doit rester fermé");

            fuelledEmitter(helper);
            // Les blocs sensibles au champ se vérifient sur un tick programmé (≈ 2 à 4 s)
            // plutôt qu'à chaque tick : un donjon en compte des dizaines, et l'information
            // ne change qu'une fois par partie.
            helper.runAfterDelay(90, () -> {
                helper.assertTrue(helper.getBlockState(GATE).getValue(FieldSensitiveBlock.POWERED),
                    "Dans un champ actif, le sas doit s'ouvrir");
                helper.succeed();
            });
        });
    }

    /**
     * <b>Les conduits et les lampes s'allument avec le champ.</b> La règle R2 : c'est ce
     * qui rend l'état du réseau lisible sans carte ni GUI, et c'est le retour visuel qui
     * récompense le geste de rallumer l'émetteur ancien.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 120)
    public static void conduitsAndLampsFollowTheField(GameTestHelper helper) {
        helper.setBlock(LAMP, ModBlocks.RESONANCE_LAMP.get());
        helper.setBlock(CONDUIT, ModBlocks.CONDUIT_LINE.get());
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!helper.getBlockState(LAMP).getValue(FieldSensitiveBlock.POWERED)
                    && !helper.getBlockState(CONDUIT).getValue(FieldSensitiveBlock.POWERED),
                "Sur une branche morte, lampe et conduit restent éteints");

            fuelledEmitter(helper);
            helper.runAfterDelay(90, () -> {
                helper.assertTrue(helper.getBlockState(LAMP).getValue(FieldSensitiveBlock.POWERED),
                    "La lampe doit s'allumer dans un champ actif");
                helper.assertTrue(helper.getBlockState(CONDUIT).getValue(FieldSensitiveBlock.POWERED),
                    "Le conduit doit s'allumer dans un champ actif");
                helper.succeed();
            });
        });
    }

    /**
     * <b>L'émetteur ancien est un vrai Field Emitter.</b> Il partage son bloc, sa block
     * entity et son carburant avec la machine que le joueur fabriquera au T2 — c'est ce
     * qui fait qu'il ne peut pas diverger d'elle, et que le joueur apprend la vraie
     * machine dans la salle où il l'a rencontrée (pilier 2). Il est en revanche
     * <b>indestructible</b> : sinon on le rapporterait chez soi, et l'Avant-poste
     * offrirait une machine T2 avant même le blueprint.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void ancientEmitterIsARealEmitterButUnminable(GameTestHelper helper) {
        helper.setBlock(EMITTER, ModBlocks.ANCIENT_EMITTER.get());
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
        helper.assertTrue(emitter != null,
            "L'émetteur ancien doit porter la block entity du Field Emitter");
        helper.assertTrue(!emitter.isActive(),
            "Il doit être À SEC à la génération : le réveiller est le geste du joueur");

        emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(helper.getBlockEntity(EMITTER) instanceof FieldEmitterBlockEntity be
                    && be.isActive(),
                "Nourri d'un Stable Crystal, il doit fournir un champ comme n'importe quel émetteur");
            BlockState state = helper.getBlockState(EMITTER);
            helper.assertTrue(state.getDestroySpeed(helper.getLevel(),
                    helper.absolutePos(EMITTER)) < 0.0F,
                "L'émetteur ancien ne doit pas être minable");
            helper.succeed();
        });
    }

    // =====================================================================
    // Coquilles
    // =====================================================================

    /** Le Hameau est une place commune : ni console, ni gardien, ni butin garanti. */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void hamletCentreIsACommonPlace(GameTestHelper helper) {
        place(helper, "hamlet");
        helper.assertBlockNotPresent(Blocks.CHEST, ANCHOR.offset(5, 1, 5));
        helper.assertEntityNotPresent(ModEntities.CUSTODE.get());
        helper.succeed();
    }

    /** Le logis porte le loot quotidien de 08-Structures.md, et rien de technique. */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void hamletDwellingIsALootRoomOnly(GameTestHelper helper) {
        place(helper, "hamlet_dwelling");
        helper.assertBlockPresent(Blocks.CHEST, ANCHOR.offset(4, 1, 9));
        assertChestHasLootTable(helper, ANCHOR.offset(4, 1, 9));
        helper.assertEntityNotPresent(ModEntities.CUSTODE.get());
        helper.succeed();
    }

    /** L'intérieur est bien creusé (air), pas un bloc plein : on peut y entrer. */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void pieceInteriorIsHollow(GameTestHelper helper) {
        place(helper, "hamlet_dwelling");
        BlockState interior = helper.getBlockState(ANCHOR.offset(4, 3, 5));
        helper.assertTrue(interior.isAir(), "L'intérieur de la pièce doit être creux, vaut : "
            + interior);
        helper.succeed();
    }

    /**
     * Nombre de tirages de la table de loot vérifiés. « Garanti » veut dire « à
     * <b>chaque</b> tirage » : une vérification à un seul tirage ne distingue pas un
     * pool certain d'un pool à 50 % de chances, et laisse donc passer précisément le
     * bug qu'elle est censée interdire.
     */
    private static final int LOOT_ROLLS = 30;

    /**
     * <b>Anti-régression de progression.</b> La recette du Field Emitter exige des
     * Resonance Component + du Gold, or les Component ne s'obtiennent qu'au Component
     * Assembler — qui a besoin d'un champ, que seul le Field Emitter fournit.
     * L'Avant-poste doit donc <b>garantir</b> l'amorçage, sinon un joueur neuf ne peut
     * jamais atteindre le T2.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void outpostLootGuaranteesBootstrapComponents(GameTestHelper helper) {
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.world.level.storage.loot.LootTable table = level.getServer()
            .reloadableRegistries()
            .getLootTable(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.LOOT_TABLE,
                com.veskorius.worldgen.ModWorldGen.OUTPOST_LOOT));
        net.minecraft.world.level.storage.loot.LootParams params =
            new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                    net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(ANCHOR)))
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);

        for (int roll = 1; roll <= LOOT_ROLLS; roll++) {
            int components = 0;
            int gold = 0;
            for (ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(ModItems.RESONANCE_COMPONENT.get())) {
                    components += stack.getCount();
                } else if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT)) {
                    gold += stack.getCount();
                }
            }
            helper.assertTrue(components >= 4 && gold >= 2,
                "L'Avant-poste doit garantir 4 Resonance Component + 2 Gold (amorçage du 1er "
                    + "Field Emitter) à CHAQUE tirage ; au tirage " + roll + "/" + LOOT_ROLLS
                    + " : " + components + " Component, " + gold + " Gold");
        }
        helper.succeed();
    }

    // --- Utilitaires ---------------------------------------------------------

    /** Un Field Emitter alimenté, à portée des blocs sensibles au champ testés ici. */
    private static void fuelledEmitter(GameTestHelper helper) {
        helper.setBlock(EMITTER, ModBlocks.FIELD_EMITTER.get());
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
        emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
    }

    private static Iterable<BlockPos> allPositions(GameTestHelper helper, int w, int h, int d) {
        return BlockPos.betweenClosed(ANCHOR, ANCHOR.offset(w, h, d));
    }

    /**
     * Vérifie que le coffre placé porte encore une table de loot. Le NBT de block entity
     * du template stocke la clé {@code LootTable} ; si elle ne survivait pas à la pose, le
     * coffre serait vide en jeu — un défaut invisible au simple « le coffre est là ».
     */
    private static void assertChestHasLootTable(GameTestHelper helper, BlockPos relative) {
        net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(relative);
        net.minecraft.nbt.CompoundTag saved =
            be.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(saved.contains("LootTable"),
            "Le coffre de structure doit conserver sa table de loot");
    }

    private static StructureTemplate template(GameTestHelper helper, String name) {
        return helper.getLevel().getServer().getStructureManager()
            .get(ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name))
            .orElseThrow(() -> new AssertionError("Pièce de structure introuvable : " + name));
    }

    /** Pose la pièce NBT du mod à {@link #ANCHOR} (coordonnées relatives au test). */
    private static void place(GameTestHelper helper, String name) {
        StructureTemplate template = template(helper, name);
        BlockPos worldAnchor = helper.absolutePos(ANCHOR);
        template.placeInWorld(helper.getLevel(), worldAnchor, worldAnchor,
            new StructurePlaceSettings(), helper.getLevel().getRandom(), 2);
    }
}
