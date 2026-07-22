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

        // Resonance Catalyst Core : 2 Resonance Component + 1 Refined Crystal +
        // 1 Redstone (05-Machines.md, augment transversal). Item non consommé qui
        // s'insère dans le slot d'augment (+15% de vitesse). Sans forme imposée.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModItems.RESONANCE_CATALYST_CORE.get())
            .requires(ModItems.RESONANCE_COMPONENT.get(), 2)
            .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get())
            .requires(Items.REDSTONE)
            .requires(ModItems.RESONANCE_BLUEPRINT.get())
            .unlockedBy(getHasName(ModItems.REFINED_RESONANCE_CRYSTAL.get()),
                has(ModItems.REFINED_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Resonance Locator : outil T2 (le design ne fixe pas la recette, #7 « craft »).
        // Proposé : 1 Stable Crystal (l'antenne) + 2 Component + 1 Iron + blueprint T2
        // (rendu). Forme sans contrainte de design.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RESONANCE_LOCATOR.get())
            .pattern(" S ")
            .pattern("CIC")
            .pattern(" P ")
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('C', ModItems.RESONANCE_COMPONENT.get())
            .define('I', Items.IRON_INGOT)
            .define('P', ModItems.RESONANCE_BLUEPRINT.get())
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);

        // Resonance Storage Cell : 2 Resonance Component + 1 Stable Resonance
        // Crystal (05-Machines.md #6) + le blueprint T2 (gate physique, rendu au
        // craft — 03-Progression.md). Batterie portable, sans forme imposée.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.TOOLS, ModItems.RESONANCE_STORAGE_CELL.get())
            .requires(ModItems.RESONANCE_COMPONENT.get(), 2)
            .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .requires(ModItems.RESONANCE_BLUEPRINT.get())
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);

        // Flux Purifier : 4 Iron Ingot + 2 Stable Resonance Crystal + 1 Redstone
        // Block (05-Machines.md, recette de construction). Forme vérifiée :
        // exactement 4 I, 2 S, 1 B.
        // + blueprint T2 (P), rendu au craft — gate physique (03-Progression.md).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUX_PURIFIER.get())
            .pattern("ISI")
            .pattern("IBI")
            .pattern("PS ")
            .define('I', Items.IRON_INGOT)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('B', Items.REDSTONE_BLOCK)
            .define('P', ModItems.RESONANCE_BLUEPRINT.get())
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Field Emitter : 4 Resonance Component + 1 Stable Resonance Crystal +
        // 2 Gold Ingot (05-Machines.md #4). Le cristal au centre (le cœur qui
        // émet), les composants aux coins, l'or au-dessus et en dessous. La forme
        // ci-dessous consomme exactement 4 C, 2 G, 1 S — vérifié contre les
        // quantités du design.
        // Gate T2 : la recette exige le blueprint T2 (P), obtenu à la console de
        // l'Avant-poste et RENDU au craft (03-Progression.md). Rien n'est masqué :
        // la recette est visible, il « suffit » d'avoir le plan. Le blueprint occupe
        // une case libre du motif du design (les quantités C/G/S sont inchangées).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FIELD_EMITTER.get())
            .pattern("CGC")
            .pattern("CSC")
            .pattern("PG ")
            .define('C', ModItems.RESONANCE_COMPONENT.get())
            .define('G', Items.GOLD_INGOT)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('P', ModItems.RESONANCE_BLUEPRINT.get())
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);

        // Crystal Roost : 4 Planches + 2 Stable Crystal + 1 Botte de Foin
        // (05-Machines.md #8) + blueprint T2 (gate physique, rendu). Sans forme.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModBlocks.CRYSTAL_ROOST.get())
            .requires(net.minecraft.tags.ItemTags.PLANKS)
            .requires(net.minecraft.tags.ItemTags.PLANKS)
            .requires(net.minecraft.tags.ItemTags.PLANKS)
            .requires(net.minecraft.tags.ItemTags.PLANKS)
            .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 2)
            .requires(Items.HAY_BLOCK)
            .requires(ModItems.RESONANCE_BLUEPRINT.get())
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Crystal Crusher : 3 Cobblestone + 1 Iron Ingot (05-Machines.md #22,
        // tableau "Recettes de construction"). Le design fixe les quantités mais
        // pas la disposition — le fer au cœur (le broyeur), la pierre autour.
        // Forme vérifiée : exactement 3 C, 1 I.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CRYSTAL_CRUSHER.get())
            .pattern(" C ")
            .pattern("CIC")
            .define('C', Items.COBBLESTONE)
            .define('I', Items.IRON_INGOT)
            .unlockedBy(getHasName(ModItems.RAW_RESONANCE_CRYSTAL.get()),
                has(ModItems.RAW_RESONANCE_CRYSTAL.get()))
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

        // Crusher : 1 Raw Crystal → 3 Resonance Dust, 10 s, autonome
        // (05-Machines.md #22). Plus rapide que le Stabilizer (10 s contre 30 s)
        // mais ne produit pas de Stable Crystal (04-Materials.md).
        MachineRecipeBuilder.crushing(ModItems.RESONANCE_DUST.get(), 3)
            .input(ModItems.RAW_RESONANCE_CRYSTAL.get(), 1)
            .time(10 * 20)
            .save(recipeOutput, machineRecipe("crushing/resonance_dust"));

        // Assembler, branche alternative : 3 Resonance Dust + 2 Iron → 2 Component,
        // 5 s, 3 Osc/tick (04-Materials.md + note tâche 2 de 11-Development-Plan.md).
        // Désormais possible sans une ligne de code machine : la poussière existe,
        // le slot d'entrée 0 de l'Assembler l'accepte du seul fait de cette recette
        // (isItemValid piloté par les recettes). Mêmes sortie/temps/Osc que la voie
        // au Stable Crystal — c'est une entrée alternative, pas un meilleur chemin.
        MachineRecipeBuilder.assembling(ModItems.RESONANCE_COMPONENT.get(), 2)
            .input(ModItems.RESONANCE_DUST.get(), 3)
            .input(Items.IRON_INGOT, 2)
            .time(5 * 20)
            .osc(3)
            .save(recipeOutput, machineRecipe("assembling/component_from_dust"));

        // Roost : 2 Quartz → 1 Raw Crystal, 600 s, autonome (05-Machines.md #8). La
        // condition « un Fileur à proximité » n'est pas dans la recette : elle vit
        // dans la machine (CrystalRoostBlockEntity.canRunCycle). 2 Quartz par cycle,
        // 2 cycles par jour MC = 4 Quartz/jour, cohérent avec le design.
        MachineRecipeBuilder.roosting(ModItems.RAW_RESONANCE_CRYSTAL.get(), 1)
            .input(Items.QUARTZ, 2)
            .time(600 * 20)
            .save(recipeOutput, machineRecipe("roosting/raw_crystal"));

        // Whetstone : outil endommagé + 1 Stable Crystal → outil réparé de 25 %,
        // 8 s, autonome (05-Machines.md #3). Type dédié (réparation, pas input→output).
        WhetstoneRecipeBuilder.sharpening()
            .catalyst(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .repairPercent(25)
            .time(8 * 20)
            .save(recipeOutput, machineRecipe("sharpening/whetstone"));

        // Field Emitter : carburants data-driven (14-Configuration.md). Par défaut, un
        // seul carburant, le Stable Crystal à 4000 Osc (06-Energy.md, source primaire).
        // Un datapack en ajoute (ex. Refined Crystal à 9000 Osc, capacité à augmenter
        // en conséquence), en retire ou change les valeurs — sans une ligne de code.
        EmitterFuelRecipeBuilder.fuel(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 4000)
            .save(recipeOutput, machineRecipe("fueling/stable_crystal"));
    }

    private static ResourceLocation machineRecipe(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
