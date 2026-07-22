package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.item.ModItems;
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

            Advancement.Builder.advancement()
                .parent(awakening)
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
        }
    }
}
