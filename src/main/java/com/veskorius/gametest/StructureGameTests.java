package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tests des pièces de structure jigsaw (08-Structures.md, A7). On valide le <b>contenu</b>
 * des pièces NBT générées — c'est ce que la migration devait préserver : coffre de loot,
 * console de l'Avant-poste, Custode gardien intégré. Le câblage jigsaw lui-même (pools /
 * structure / structure_set) est validé par les codecs au datagen et par le chargement du
 * serveur de test (qui échouerait si un registre datapack était invalide).
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructureGameTests {

    private static final String FIELD_ARENA = "field_arena";

    /** Coin de pose de la pièce (la plus grande fait 13×7×11 ; l'arène en fait 21). */
    private static final BlockPos ANCHOR = new BlockPos(2, 1, 2);

    /**
     * L'Avant-poste embarque sa <b>console d'attunement</b> (la porte du T2), un coffre de
     * loot et un <b>Custode gardien</b> — l'essentiel gameplay que l'ancienne
     * {@code RuinFeature} posait par code et que la pièce NBT doit désormais porter.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void outpostPieceCarriesConsoleChestAndGuardian(GameTestHelper helper) {
        place(helper, "outpost");

        // La console trône sur son estrade centrale (13×7×11, cf. ModStructurePieceProvider).
        helper.assertBlockPresent(ModBlocks.ATTUNEMENT_CONSOLE.get(), ANCHOR.offset(6, 3, 5));
        helper.assertBlockPresent(Blocks.CHEST, ANCHOR.offset(1, 1, 1));
        // Coquille en pierre veinée (un mur d'angle).
        helper.assertBlockPresent(ModBlocks.RESONANCE_VEINED_STONE.get(), ANCHOR.offset(0, 0, 0));
        // Gardien persistant intégré à la pièce.
        helper.assertEntityPresent(ModEntities.CUSTODE.get());
        // Le coffre a bien conservé sa table de loot au passage par le NBT (sinon il
        // serait vide en jeu — la régression la plus silencieuse possible).
        assertChestHasLootTable(helper, ANCHOR.offset(1, 1, 1));
        helper.succeed();
    }

    /**
     * L'Habitation Modeste est une salle de loot <b>sans</b> console ni gardien
     * (08-Structures.md : jamais de machine, jamais de Custode).
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void dwellingPieceIsLootRoomOnly(GameTestHelper helper) {
        place(helper, "modest_dwelling");

        helper.assertBlockPresent(Blocks.CHEST, ANCHOR.offset(8, 1, 7));
        helper.assertBlockNotPresent(ModBlocks.ATTUNEMENT_CONSOLE.get(), ANCHOR.offset(6, 3, 5));
        helper.assertEntityNotPresent(ModEntities.CUSTODE.get());
        helper.succeed();
    }

    /**
     * Nombre de tirages de la table de loot vérifiés. « Garanti » veut dire « à
     * <b>chaque</b> tirage » : une vérification à un seul tirage ne distingue pas un
     * pool certain d'un pool à 50 % de chances, et laisse donc passer précisément le
     * bug qu'elle est censée interdire (c'est arrivé — un pool unique à 1 roll avec
     * deux entrées donnait « Component OU Gold », et ce test passait une fois sur deux).
     */
    private static final int LOOT_ROLLS = 30;

    /**
     * <b>Anti-régression de progression.</b> La recette du Field Emitter exige 4 Resonance
     * Component + 2 Gold, or les Component ne s'obtiennent qu'au Component Assembler — qui a
     * besoin d'un champ, que seul le Field Emitter fournit. L'Avant-poste doit donc
     * <b>garantir</b> de quoi fabriquer le premier Field Emitter, sinon un joueur neuf ne peut
     * jamais atteindre le T2. Ce test roule la table {@link #LOOT_ROLLS} fois et exige
     * l'amorçage complet à chaque fois — « garanti » ne se teste pas sur un tirage unique.
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
            for (net.minecraft.world.item.ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(com.veskorius.item.ModItems.RESONANCE_COMPONENT.get())) {
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

    /** L'intérieur est bien creusé (air), pas un bloc plein : on peut y entrer. */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void pieceInteriorIsHollow(GameTestHelper helper) {
        place(helper, "modest_dwelling");
        BlockState interior = helper.getBlockState(ANCHOR.offset(5, 3, 4));
        helper.assertTrue(interior.isAir(), "L'intérieur de la pièce doit être creux, vaut : "
            + interior);
        helper.succeed();
    }

    /**
     * Vérifie que le coffre placé porte encore une table de loot. Le NBT de block entity
     * du template stocke la clé {@code LootTable} ; si elle ne survivait pas à la pose, le
     * coffre serait vide en jeu — un défaut invisible au simple « le coffre est là ».
     */
    private static void assertChestHasLootTable(GameTestHelper helper, BlockPos relative) {
        net.minecraft.world.level.block.entity.BlockEntity be =
            helper.getBlockEntity(relative);
        net.minecraft.nbt.CompoundTag saved =
            be.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(saved.contains("LootTable"),
            "Le coffre de structure doit conserver sa table de loot");
    }

    /** Pose la pièce NBT du mod à {@link #ANCHOR} (coordonnées relatives au test). */
    private static void place(GameTestHelper helper, String name) {
        StructureTemplate template = helper.getLevel().getServer().getStructureManager()
            .get(ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name))
            .orElseThrow(() -> new AssertionError("Pièce de structure introuvable : " + name));
        BlockPos worldAnchor = helper.absolutePos(ANCHOR);
        template.placeInWorld(helper.getLevel(), worldAnchor, worldAnchor,
            new StructurePlaceSettings(), helper.getLevel().getRandom(), 2);
    }
}
