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
                .addCriterion("has_blueprint",
                    InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RESONANCE_BLUEPRINT.get()))
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
