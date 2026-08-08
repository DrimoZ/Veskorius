package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import com.veskorius.worldgen.ModStructures;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Advancements de **feedback** (12-UX-and-Advancements.md, révisé) : ils ne
 * débloquent rien (le gating est physique, via le blueprint) — ils affichent un
 * toast quand le joueur franchit une étape. Déclenchés automatiquement par la
 * possession de l'objet-repère (cristal brut, puis blueprint T2).
 */
public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                  ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new Generator()));
    }

    private static final ResourceLocation BACKGROUND =
        ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png");

    private static class Generator implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
                             ExistingFileHelper existingFileHelper) {
            AdvancementHolder awakening = Advancement.Builder.advancement()
                .display(
                    ModItems.RAW_RESONANCE_CRYSTAL.get(),
                    Component.translatable("advancements.veskorius.tier1_awakening.title"),
                    Component.translatable("advancements.veskorius.tier1_awakening.description"),
                    BACKGROUND,
                    AdvancementType.TASK,
                    true, true, false)
                .addCriterion("has_crystal",
                    InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RAW_RESONANCE_CRYSTAL.get()))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "tier1_awakening"),
                    existingFileHelper);

            // --- Exploration : entrer dans une ruine -------------------------
            // Déclenché par la PRÉSENCE dans la structure, pas par un objet ramassé :
            // c'est le seul moment où le jeu peut dire « tu as trouvé un truc » avant
            // que le joueur ait compris ce qu'il regarde. Un toast à l'entrée d'une
            // ruine vaut mieux qu'un toast à l'ouverture d'un coffre — il récompense
            // l'exploration, pas le pillage.
            AdvancementHolder dwelling = Advancement.Builder.advancement()
                .parent(awakening)
                .display(
                    ModItems.FOSSILIZED_RATION.get(),
                    Component.translatable("advancements.veskorius.find_dwelling.title"),
                    Component.translatable("advancements.veskorius.find_dwelling.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false)
                .addCriterion("in_dwelling", inStructure(registries, ModStructures.MODEST_DWELLING))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "find_dwelling"),
                    existingFileHelper);

            AdvancementHolder outpost = Advancement.Builder.advancement()
                .parent(dwelling)
                .display(
                    // PAS la console : elle n'a volontairement pas d'objet (non
                    // récupérable), donc son ItemStack vaut air et l'icône est refusée.
                    ModItems.RESONANCE_COMPONENT.get(),
                    Component.translatable("advancements.veskorius.find_outpost.title"),
                    Component.translatable("advancements.veskorius.find_outpost.description"),
                    null,
                    AdvancementType.GOAL,
                    true, true, false)
                .addCriterion("in_outpost", inStructure(registries, ModStructures.OUTPOST))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "find_outpost"),
                    existingFileHelper);

            // Le palier T2 découle de l'Avant-poste : l'arbre raconte donc le chemin
            // réel (ruine → avant-poste → console → réseau), pas une liste d'objets.
            AdvancementHolder tier2 = Advancement.Builder.advancement()
                .parent(outpost)
                .display(
                    ModItems.RESONANCE_BLUEPRINT.get(),
                    Component.translatable("advancements.veskorius.tier2_field.title"),
                    Component.translatable("advancements.veskorius.tier2_field.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false)
                // Le PALIER, pas seulement l'objet. Le critère portait sur
                // « posséder un resonance_blueprint », sans regarder son tier : ramasser
                // le plan T4 de l'Archive aurait donc décerné le T2 au passage, et une
                // fois les trois paliers ajoutés, les trois toasts seraient partis
                // ensemble. Le même oubli que sur les recettes, transposé à l'arbre.
                .addCriterion("has_blueprint", hasBlueprint(2))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "tier2_field"),
                    existingFileHelper);

            // --- Jalons de fabrication ---------------------------------------
            // Un par geste structurant, pas un par bloc : le châssis (on entre dans la
            // grammaire de craft du mod), le champ (le pilier 3 devient tangible), et
            // le Fileur (la boucle de production passive s'ouvre).
            Advancement.Builder.advancement()
                .parent(awakening)
                .display(
                    ModBlocks.FRACTURED_CHASSIS.get(),
                    Component.translatable("advancements.veskorius.first_chassis.title"),
                    Component.translatable("advancements.veskorius.first_chassis.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false)
                .addCriterion("has_chassis",
                    InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.FRACTURED_CHASSIS.get()))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "first_chassis"),
                    existingFileHelper);

            Advancement.Builder.advancement()
                .parent(tier2)
                .display(
                    ModBlocks.FIELD_EMITTER.get(),
                    Component.translatable("advancements.veskorius.first_field.title"),
                    Component.translatable("advancements.veskorius.first_field.description"),
                    null,
                    AdvancementType.GOAL,
                    true, true, false)
                .addCriterion("has_emitter",
                    InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.FIELD_EMITTER.get()))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "first_field"),
                    existingFileHelper);

            // --- Les paliers hauts (12-UX-and-Advancements.md) -----------------
            //
            // L'arbre s'arrêtait au T2 pendant que le jeu allait au T5 : un joueur qui
            // terminait la partie n'avait plus aucun retour après son deuxième plan. Les
            // advancements ne débloquent rien ici — ils SONT le récit de la progression,
            // et un récit qui s'interrompt aux deux cinquièmes ne raconte rien.
            AdvancementHolder tier3 = Advancement.Builder.advancement()
                .parent(tier2)
                .display(
                    ModBlocks.VESKORIAN_ALLOY_FORGE.get(),
                    Component.translatable("advancements.veskorius.tier3_relay.title"),
                    Component.translatable("advancements.veskorius.tier3_relay.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false)
                .addCriterion("has_blueprint", hasBlueprint(3))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "tier3_relay"),
                    existingFileHelper);

            AdvancementHolder tier4 = Advancement.Builder.advancement()
                .parent(tier3)
                .display(
                    ModItems.HYPER_REFINED_CRYSTAL.get(),
                    Component.translatable("advancements.veskorius.tier4_amplifier.title"),
                    Component.translatable("advancements.veskorius.tier4_amplifier.description"),
                    null,
                    AdvancementType.GOAL,
                    true, true, false)
                .addCriterion("has_blueprint", hasBlueprint(4))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "tier4_amplifier"),
                    existingFileHelper);

            // Le T5 ne s'obtient pas par un plan : il n'y en a pas. On y entre en
            // TROUVANT une Faille, ce que le dossier pose comme règle — « ne se débloque
            // pas par craft mais par découverte en jeu ». Le critère suit donc la pierre
            // déformée, qui est le seul signe qu'on en a vu une.
            AdvancementHolder tier5 = Advancement.Builder.advancement()
                .parent(tier4)
                .display(
                    ModBlocks.DEFORMED_STONE.get(),
                    Component.translatable("advancements.veskorius.tier5_rift.title"),
                    Component.translatable("advancements.veskorius.tier5_rift.description"),
                    null,
                    AdvancementType.GOAL,
                    true, true, false)
                .addCriterion("has_deformed_stone",
                    InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.DEFORMED_STONE.get()))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "tier5_rift"),
                    existingFileHelper);

            // La fin. CHALLENGE et non GOAL : c'est le seul contenu du mod qui se gagne
            // en combattant, et le seul qui ne se rejoue jamais.
            Advancement.Builder.advancement()
                .parent(tier5)
                .display(
                    ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT.get(),
                    Component.translatable("advancements.veskorius.rift_guardian_slain.title"),
                    Component.translatable("advancements.veskorius.rift_guardian_slain.description"),
                    null,
                    AdvancementType.CHALLENGE,
                    true, true, false)
                .addCriterion("slain", net.minecraft.advancements.critereon.KilledTrigger.TriggerInstance
                    .playerKilledEntity(net.minecraft.advancements.critereon.EntityPredicate.Builder.entity()
                        .of(com.veskorius.entity.ModEntities.RIFT_GUARDIAN.get())))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "rift_guardian_slain"),
                    existingFileHelper);

            Advancement.Builder.advancement()
                .parent(awakening)
                .display(
                    ModItems.RESONANCE_SPORE.get(),
                    Component.translatable("advancements.veskorius.first_strider.title"),
                    Component.translatable("advancements.veskorius.first_strider.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false)
                .addCriterion("has_spore",
                    InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RESONANCE_SPORE.get()))
                .save(saver, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "first_strider"),
                    existingFileHelper);
        }

        /**
         * Critère « le joueur possède le blueprint de CE palier ».
         *
         * <p>Le tier vit dans un data component, et un critère d'inventaire nu l'ignore —
         * exactement le piège qui rendait la garde des recettes décorative avant qu'on
         * la répare. Ici il aurait fait sonner les quatre paliers au premier plan ramassé.
         */
        private static net.minecraft.advancements.Criterion<InventoryChangeTrigger.TriggerInstance>
                hasBlueprint(int tier) {
            return InventoryChangeTrigger.TriggerInstance.hasItems(
                net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                    .of(ModItems.RESONANCE_BLUEPRINT.get())
                    .hasComponents(net.minecraft.core.component.DataComponentPredicate.builder()
                        .expect(com.veskorius.item.ModDataComponents.BLUEPRINT_TIER.get(), tier)
                        .build()));
        }

        /**
         * Critère « le joueur se tient dans cette structure ». Passe par
         * {@code PlayerTrigger.located} + un {@code LocationPredicate} sur la structure :
         * c'est le mécanisme vanilla des advancements de biome/structure, donc il suit le
         * joueur sans qu'on ait à tick quoi que ce soit.
         */
        private static net.minecraft.advancements.Criterion<net.minecraft.advancements.critereon.PlayerTrigger.TriggerInstance>
                inStructure(HolderLookup.Provider registries,
                            net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> structure) {
            var lookup = registries.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            return net.minecraft.advancements.critereon.PlayerTrigger.TriggerInstance.located(
                net.minecraft.advancements.critereon.LocationPredicate.Builder.inStructure(
                    lookup.getOrThrow(structure)));
        }
    }
}
