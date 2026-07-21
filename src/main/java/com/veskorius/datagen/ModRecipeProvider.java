package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import com.veskorius.tag.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        buildMachineRecipes(recipeOutput);

        // Recette de construction du Resonance Stabilizer : 4 Cobblestone +
        // 2 Copper Ingot + 1 Raw Resonance Crystal (05-Machines.md, tableau
        // "Recettes de construction"). Le dossier de conception fixe les
        // quantites mais pas la disposition — la forme ci-dessous place le
        // cristal au centre, encadre verticalement par le cuivre, la pierre
        // formant la structure. Si une disposition canonique est decidee plus
        // tard, c'est 05-Machines.md qu'il faut modifier d'abord.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_STABILIZER.get())
            .pattern(" U ")
            .pattern("CRC")
            .pattern("CUC")
            .define('U', Items.COPPER_INGOT)
            .define('C', Items.COBBLESTONE)
            .define('R', ModItems.RAW_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.RAW_RESONANCE_CRYSTAL.get()),
                has(ModItems.RAW_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Component Assembler : 3 Iron Ingot + 2 Stable Resonance Crystal +
        // 1 Redstone (05-Machines.md, recette de construction). Le fer forme la
        // structure, les cristaux l'alimentent, la redstone la pilote. Forme
        // vérifiée : exactement 3 I, 2 S, 1 R.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.COMPONENT_ASSEMBLER.get())
            .pattern("SIS")
            .pattern("IRI")
            .define('I', Items.IRON_INGOT)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('R', Items.REDSTONE)
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Resonance Whetstone : 2 Cobblestone + 1 Iron Ingot + 1 Stable
        // Resonance Crystal (05-Machines.md). Le cristal est pose sur le socle
        // de pierre, le fer le maintient — meme remarque que ci-dessus sur la
        // disposition, seules les quantites sont imposees par la conception.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.RESONANCE_WHETSTONE.get())
            .pattern(" S ")
            .pattern("CIC")
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('C', Items.COBBLESTONE)
            .define('I', Items.IRON_INGOT)
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Resonance Tuner : 2 Iron Ingot + 1 Resonance Component + 1 Redstone
        // (05-Machines.md, section outil transversal). Sans forme imposée.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.TOOLS, ModItems.RESONANCE_TUNER.get())
            .requires(Items.IRON_INGOT, 2)
            .requires(ModItems.RESONANCE_COMPONENT.get())
            .requires(Items.REDSTONE)
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);

        // Flux Purifier : 4 Iron Ingot + 2 Stable Resonance Crystal + 1 Redstone
        // Block (05-Machines.md, recette de construction). Forme vérifiée :
        // exactement 4 I, 2 S, 1 B.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUX_PURIFIER.get())
            .pattern("ISI")
            .pattern("IBI")
            .pattern(" S ")
            .define('I', Items.IRON_INGOT)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('B', Items.REDSTONE_BLOCK)
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Field Emitter : 4 Resonance Component + 1 Stable Resonance Crystal +
        // 2 Gold Ingot (05-Machines.md #4). Le cristal au centre (le cœur qui
        // émet), les composants aux coins, l'or au-dessus et en dessous. La forme
        // ci-dessous consomme exactement 4 C, 2 G, 1 S — vérifié contre les
        // quantités du design.
        // NB : cette recette est débloquée en jeu par le fragment de l'Avant-poste
        // (advancement veskorius:tier2_field, 12-UX-and-Advancements.md) — le
        // JSON produit ici reste inerte tant que l'advancement n'est pas obtenu,
        // ce câblage viendra avec la tâche 10 (structures + fragments).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FIELD_EMITTER.get())
            .pattern("CGC")
            .pattern("CSC")
            .pattern(" G ")
            .define('C', ModItems.RESONANCE_COMPONENT.get())
            .define('G', Items.GOLD_INGOT)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);
    }

    /**
     * Recettes de FONCTIONNEMENT des machines (ce qu'elles transforment une fois
     * posées), désormais data-driven. Ces valeurs reproduisent celles qui étaient
     * en dur ; un datapack peut les changer ou en ajouter sans recompiler.
     */
    private void buildMachineRecipes(RecipeOutput recipeOutput) {
        // Stabilizer : Raw Crystal + flux (Quartz via tag) → Stable Crystal, 30 s,
        // autonome (05-Machines.md #1).
        MachineRecipeBuilder.stabilizing(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .input(ModItems.RAW_RESONANCE_CRYSTAL.get(), 1)
            .input(ModTags.Items.STABILIZER_FLUX, 1)
            .time(30 * 20)
            .save(recipeOutput, machineRecipe("stabilizing/stable_crystal"));

        // Assembler : 1 Stable Crystal + 2 Iron → 2 Component, 5 s, 3 Osc/tick
        // (05-Machines.md #2).
        MachineRecipeBuilder.assembling(ModItems.RESONANCE_COMPONENT.get(), 2)
            .input(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .input(Items.IRON_INGOT, 2)
            .time(5 * 20)
            .osc(3)
            .save(recipeOutput, machineRecipe("assembling/component"));

        // Purifier : 1 Stable Crystal + 1 Redstone → 1 Refined Crystal, 45 s,
        // 2 Osc/tick (05-Machines.md #5).
        MachineRecipeBuilder.purifying(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 1)
            .input(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .input(Items.REDSTONE, 1)
            .time(45 * 20)
            .osc(2)
            .save(recipeOutput, machineRecipe("purifying/refined_crystal"));

        // Whetstone : outil endommagé + 1 Stable Crystal → outil réparé de 25 %,
        // 8 s, autonome (05-Machines.md #3). Type dédié (réparation, pas input→output).
        WhetstoneRecipeBuilder.sharpening()
            .catalyst(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .repairPercent(25)
            .time(8 * 20)
            .save(recipeOutput, machineRecipe("sharpening/whetstone"));
    }

    private static ResourceLocation machineRecipe(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
