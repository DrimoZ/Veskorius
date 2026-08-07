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

        buildChassisRecipes(recipeOutput);

        buildArchitectureRecipes(recipeOutput);

        // --- Machines T1 : châssis Fracturé + ce qui les distingue -------------
        //
        // La grammaire de fabrication est désormais « le boîtier de mon palier, plus
        // la pièce qui fait le métier de cette machine ». Avant, chaque machine avait
        // sa forme complète : le joueur réapprenait un motif entier à chaque bloc, et
        // rien ne disait à l'œil que le Stabilizer et le Crusher étaient du même âge.
        // Les quantités « boîtier » (pierre, fer de structure) sont absorbées par le
        // châssis ; seuls restent les ingrédients porteurs de sens.

        machine(recipeOutput, ModBlocks.RESONANCE_STABILIZER.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.RAW_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.RAW_RESONANCE_CRYSTAL.get()));

        machine(recipeOutput, ModBlocks.COMPONENT_ASSEMBLER.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 2)
                .requires(Items.REDSTONE));

        machine(recipeOutput, ModBlocks.RESONANCE_WHETSTONE.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get()));

        // Resonance Tuner : 2 Iron Ingot + 1 Resonance Component + 1 Redstone
        // (05-Machines.md, section outil transversal). Sans forme imposée.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.TOOLS, ModItems.RESONANCE_TUNER.get())
            .requires(ModTags.Items.IRON_SUBSTITUTES)
            .requires(ModTags.Items.IRON_SUBSTITUTES)
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
            .define('I', ModTags.Items.IRON_SUBSTITUTES)
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

        // --- Machines T2 : châssis Accordé + blueprint T2 (rendu) --------------

        machine(recipeOutput, ModBlocks.FLUX_PURIFIER.get(), ModBlocks.ATTUNED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 2)
                .requires(Items.REDSTONE_BLOCK)
                .requires(ModItems.RESONANCE_BLUEPRINT.get()));

        // Field Emitter : c'est LUI que l'amorçage garanti de l'Avant-poste doit
        // permettre de fabriquer (4 Component + 2 Gold dans le coffre, voir
        // ModChestLootProvider). Il en consomme 2 et 2 : le lot garanti couvre donc la
        // recette avec de la marge, et le châssis ne demande, lui, que des matériaux
        // T1 (pierre, cuivre, fer, cristaux stables) — aucun Component. La dépendance
        // circulaire Component ⇄ champ ne peut donc pas se reformer par ce chemin.
        machine(recipeOutput, ModBlocks.FIELD_EMITTER.get(), ModBlocks.ATTUNED_CHASSIS.get(),
            ModItems.RESONANCE_COMPONENT.get(), b -> b
                .requires(ModItems.RESONANCE_COMPONENT.get(), 2)
                .requires(Items.GOLD_INGOT, 2)
                .requires(ModItems.RESONANCE_BLUEPRINT.get()));

        // --- Machine T3 : châssis Veskorien ------------------------------------

        machine(recipeOutput, ModBlocks.DAMPING_ARRAY.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.REFINED_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get())
                .requires(Items.REDSTONE_BLOCK)
                .requires(ModItems.RESONANCE_BLUEPRINT.get()));

        // Émetteur Accordable : un Field Emitter + 2 Refined Crystal (l'accord demande
        // du cristal raffiné) + blueprint T2 rendu. Upgrade, pas une machine de plus.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModBlocks.TUNABLE_FIELD_EMITTER.get())
            .requires(ModBlocks.FIELD_EMITTER.get())
            .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
            .requires(ModItems.RESONANCE_BLUEPRINT.get())
            .unlockedBy(getHasName(ModItems.REFINED_RESONANCE_CRYSTAL.get()),
                has(ModItems.REFINED_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Crystal Roost : le nichoir se monte sur un châssis T2, garni de planches et
        // de foin (05-Machines.md #8) + blueprint T2 rendu.
        machine(recipeOutput, ModBlocks.CRYSTAL_ROOST.get(), ModBlocks.ATTUNED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(Items.HAY_BLOCK)
                .requires(ModItems.RESONANCE_BLUEPRINT.get()));

        // Resonance Codex : recette de secours (Livre + Cristal Brut). Le Codex est
        // donné à la première connexion (15-Codex-Guidebook.md) ; ce craft ne sert qu'à
        // en refaire un s'il est perdu — un exemplaire neuf se re-remplit tout seul des
        // objets possédés et des paliers atteints (CodexUnlocks, scan périodique).
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.TOOLS, ModItems.RESONANCE_CODEX.get())
            .requires(Items.BOOK)
            .requires(ModItems.RAW_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.RAW_RESONANCE_CRYSTAL.get()),
                has(ModItems.RAW_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        machine(recipeOutput, ModBlocks.CRYSTAL_CRUSHER.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.RAW_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModTags.Items.IRON_SUBSTITUTES));
    }

    /**
     * Les trois châssis de palier (05-Machines.md, « Châssis par palier »). Chacun
     * <b>contient</b> le précédent : le T2 est un T1 restauré, le T3 un T2 réarmé. La
     * progression est donc littérale — on ne jette rien, on améliore, ce qui colle au
     * pilier « restaurer plutôt que conquérir » (01-Vision-Pillars.md).
     */
    private void buildChassisRecipes(RecipeOutput recipeOutput) {
        // T1 « Fracturé » : de la pierre et du cuivre, rien d'autre. Craftable dès la
        // première minute — c'est le point d'entrée de toute la chaîne.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FRACTURED_CHASSIS.get())
            .pattern("CUC")
            .pattern("C C")
            .pattern("CUC")
            .define('C', Items.COBBLESTONE)
            .define('U', Items.COPPER_INGOT)
            .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
            .save(recipeOutput);

        // T2 « Accordé » : le châssis T1 renforcé de fer et serti de cristaux stables.
        // Volontairement SANS Resonance Component ni blueprint — il doit rester
        // fabricable avant d'avoir un champ, sinon la boucle d'amorçage T2 se referme.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ATTUNED_CHASSIS.get())
            .pattern(" I ")
            .pattern("SFS")
            .pattern(" I ")
            .define('I', ModTags.Items.IRON_SUBSTITUTES)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('F', ModBlocks.FRACTURED_CHASSIS.get())
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // T3 « Veskorien » : cristal raffiné, donc Flux Purifier, donc un champ. Le
        // palier se paie en infrastructure, pas en minage.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VESKORIAN_CHASSIS.get())
            .pattern(" R ")
            .pattern("IAI")
            .pattern(" R ")
            .define('R', ModItems.REFINED_RESONANCE_CRYSTAL.get())
            .define('I', ModTags.Items.IRON_SUBSTITUTES)
            .define('A', ModBlocks.ATTUNED_CHASSIS.get())
            .unlockedBy(getHasName(ModItems.REFINED_RESONANCE_CRYSTAL.get()),
                has(ModItems.REFINED_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);
    }

    /**
     * Maçonnerie et éclairage veskoriens (17-Dungeons.md §4).
     *
     * <p>Tout part du {@code resonance_veined_stone}, qu'on mine autour des poches : la
     * matière du donjon est celle qu'on trouve dans le sol, appareillée. Aucune de ces
     * recettes ne demande de blueprint ni de Component — bâtir n'est pas un palier, et
     * une brique verrouillée derrière un tier serait une brique qu'on n'utilise jamais.
     *
     * <p>Chaque forme est aussi taillable à la scie de pierre : c'est l'attente vanilla
     * pour une famille de blocs de construction, et ça divise par deux le coût en pierre
     * — donc ça rend la maçonnerie réellement utilisable pour un vrai bâtiment.
     */
    private void buildArchitectureRecipes(RecipeOutput output) {
        var stone = ModBlocks.RESONANCE_VEINED_STONE.get();
        var bricks = ModBlocks.VEINED_STONE_BRICKS.get();

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4)
            .pattern("##").pattern("##")
            .define('#', stone)
            .unlockedBy(getHasName(stone), has(stone))
            .save(output);

        // Fissurées : la brique passée au four. Le raccourci vanilla pour « vieilli »,
        // et le seul moyen d'en obtenir sans démonter une ruine.
        net.minecraft.data.recipes.SimpleCookingRecipeBuilder
            .smelting(net.minecraft.world.item.crafting.Ingredient.of(bricks),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.CRACKED_VEINED_STONE_BRICKS.get(), 0.1F, 200)
            .unlockedBy(getHasName(bricks), has(bricks))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHISELED_VEINED_STONE.get())
            .pattern("#").pattern("#")
            .define('#', ModBlocks.VEINED_STONE_BRICK_SLAB.get())
            .unlockedBy(getHasName(bricks), has(bricks))
            .save(output);

        stairBuilder(ModBlocks.VEINED_STONE_BRICK_STAIRS.get(),
            net.minecraft.world.item.crafting.Ingredient.of(bricks))
            .unlockedBy(getHasName(bricks), has(bricks)).save(output);
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VEINED_STONE_BRICK_SLAB.get(),
            net.minecraft.world.item.crafting.Ingredient.of(bricks))
            .unlockedBy(getHasName(bricks), has(bricks)).save(output);
        wallBuilder(RecipeCategory.DECORATIONS, ModBlocks.VEINED_STONE_BRICK_WALL.get(),
            net.minecraft.world.item.crafting.Ingredient.of(bricks))
            .unlockedBy(getHasName(bricks), has(bricks)).save(output);

        for (var target : java.util.List.of(bricks, ModBlocks.CHISELED_VEINED_STONE.get(),
            ModBlocks.VEINED_STONE_BRICK_STAIRS.get(), ModBlocks.VEINED_STONE_BRICK_WALL.get())) {
            stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, target, stone);
            if (target != bricks) {
                stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, target, bricks);
            }
        }
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS,
            ModBlocks.VEINED_STONE_BRICK_SLAB.get(), stone, 2);
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS,
            ModBlocks.VEINED_STONE_BRICK_SLAB.get(), bricks, 2);

        // Lampe : la brique + un Stable Crystal. Fabricable dès le T1 (le Stabilizer est
        // autonome), même si elle ne s'allumera qu'une fois un champ posé — c'est
        // volontaire : découvrir qu'une lampe déjà bâtie s'allume quand l'émetteur
        // démarre est un bien meilleur professeur qu'un tooltip.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.RESONANCE_LAMP.get(), 2)
            .pattern(" # ").pattern("#c#").pattern(" # ")
            .define('#', bricks)
            .define('c', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(output);

        // Conduit : la poussière du Crusher, l'autre voie T1 — les deux branches de
        // départ mènent donc chacune à un bloc d'architecture, et aucune n'est requise.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CONDUIT_LINE.get(), 4)
            .pattern("###").pattern("ddd").pattern("###")
            .define('#', bricks)
            .define('d', ModItems.RESONANCE_DUST.get())
            .unlockedBy(getHasName(ModItems.RESONANCE_DUST.get()), has(ModItems.RESONANCE_DUST.get()))
            .save(output);
    }

    /**
     * Une machine = le châssis de son palier + ce qui la distingue. Sans forme imposée :
     * la disposition n'apprend plus rien au joueur une fois la grammaire comprise, et
     * une recette informe se retient mieux qu'un motif de plus.
     */
    private void machine(RecipeOutput output, net.minecraft.world.level.block.Block result,
                         net.minecraft.world.level.block.Block chassis,
                         net.minecraft.world.level.ItemLike trigger,
                         java.util.function.UnaryOperator<net.minecraft.data.recipes.ShapelessRecipeBuilder> parts) {
        net.minecraft.data.recipes.ShapelessRecipeBuilder builder =
            net.minecraft.data.recipes.ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, result)
                .requires(chassis);
        parts.apply(builder)
            .unlockedBy(getHasName(trigger), has(trigger))
            .save(output);
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
            .stable()
            .save(recipeOutput, machineRecipe("stabilizing/stable_crystal"));

        // Assembler : 1 Stable Crystal + 2 Iron → 2 Component, 5 s, 3 Osc/tick
        // (05-Machines.md #2).
        MachineRecipeBuilder.assembling(ModItems.RESONANCE_COMPONENT.get(), 2)
            .input(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .input(ModTags.Items.IRON_SUBSTITUTES, 2)
            .time(5 * 20)
            .osc(3)
            .stable()
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
            .stable()
            .save(recipeOutput, machineRecipe("crushing/resonance_dust"));

        // Assembler, branche alternative : 3 Resonance Dust + 2 Iron → 2 Component,
        // 5 s, 3 Osc/tick (04-Materials.md + note tâche 2 de 11-Development-Plan.md).
        // Désormais possible sans une ligne de code machine : la poussière existe,
        // le slot d'entrée 0 de l'Assembler l'accepte du seul fait de cette recette
        // (isItemValid piloté par les recettes). Mêmes sortie/temps/Osc que la voie
        // au Stable Crystal — c'est une entrée alternative, pas un meilleur chemin.
        MachineRecipeBuilder.assembling(ModItems.RESONANCE_COMPONENT.get(), 2)
            .input(ModItems.RESONANCE_DUST.get(), 3)
            .input(ModTags.Items.IRON_SUBSTITUTES, 2)
            .time(5 * 20)
            .osc(3)
            .stable()
            .save(recipeOutput, machineRecipe("assembling/component_from_dust"));

        // Roost : 2 Quartz → 1 Raw Crystal, 600 s, autonome (05-Machines.md #8). La
        // condition « un Fileur à proximité » n'est pas dans la recette : elle vit
        // dans la machine (CrystalRoostBlockEntity.canRunCycle). 2 Quartz par cycle,
        // 2 cycles par jour MC = 4 Quartz/jour, cohérent avec le design.
        MachineRecipeBuilder.roosting(ModItems.RAW_RESONANCE_CRYSTAL.get(), 1)
            .input(Items.QUARTZ, 2)
            .time(600 * 20)
            .stable()
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

        // Agents de damping, data-driven (06-Energy.md). Le Refined Crystal ouvre la
        // voie dès le T2 ; le Concentrated Flux (T3) prendra le relais avec une valeur
        // bien supérieure — un simple JSON de plus, aucune ligne de code.
        DampingAgentRecipeBuilder.agent(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 500)
            .save(recipeOutput, machineRecipe("damping/refined_crystal"));
    }

    private static ResourceLocation machineRecipe(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
