package com.veskorius.compat.jei;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.recipe.EmitterFuelRecipe;
import com.veskorius.recipe.MachineRecipe;
import com.veskorius.recipe.ModRecipeTypes;
import com.veskorius.recipe.WhetstoneRecipe;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

/**
 * Intégration JEI : montre les recettes de fonctionnement des machines dans
 * l'interface JEI, pour les vérifier en jeu (« comment obtient-on un Refined
 * Crystal ? » → clic sur l'objet dans JEI).
 *
 * Chargé uniquement quand JEI est présent (scanne les classes {@code @JeiPlugin}) ;
 * inerte sinon — d'où la dépendance JEI en {@code compileOnly}/{@code localRuntime}.
 */
@JeiPlugin
public class VeskoriusJeiPlugin implements IModPlugin {

    // Types de recette côté JEI (distincts des RecipeType vanilla, même identité).
    public static final RecipeType<MachineRecipe> STABILIZING =
        RecipeType.create(Veskorius.MOD_ID, "stabilizing", MachineRecipe.class);
    public static final RecipeType<MachineRecipe> ASSEMBLING =
        RecipeType.create(Veskorius.MOD_ID, "assembling", MachineRecipe.class);
    public static final RecipeType<MachineRecipe> PURIFYING =
        RecipeType.create(Veskorius.MOD_ID, "purifying", MachineRecipe.class);
    public static final RecipeType<MachineRecipe> CRUSHING =
        RecipeType.create(Veskorius.MOD_ID, "crushing", MachineRecipe.class);
    public static final RecipeType<MachineRecipe> ROOSTING =
        RecipeType.create(Veskorius.MOD_ID, "roosting", MachineRecipe.class);
    public static final RecipeType<WhetstoneRecipe> SHARPENING =
        RecipeType.create(Veskorius.MOD_ID, "sharpening", WhetstoneRecipe.class);
    public static final RecipeType<EmitterFuelRecipe> FUELING =
        RecipeType.create(Veskorius.MOD_ID, "fueling", EmitterFuelRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new MachineRecipeCategory(gui, STABILIZING,
                Component.translatable("block.veskorius.resonance_stabilizer"),
                ModBlocks.RESONANCE_STABILIZER.get()),
            new MachineRecipeCategory(gui, ASSEMBLING,
                Component.translatable("block.veskorius.component_assembler"),
                ModBlocks.COMPONENT_ASSEMBLER.get()),
            new MachineRecipeCategory(gui, PURIFYING,
                Component.translatable("block.veskorius.flux_purifier"),
                ModBlocks.FLUX_PURIFIER.get()),
            new MachineRecipeCategory(gui, CRUSHING,
                Component.translatable("block.veskorius.crystal_crusher"),
                ModBlocks.CRYSTAL_CRUSHER.get()),
            new MachineRecipeCategory(gui, ROOSTING,
                Component.translatable("block.veskorius.crystal_roost"),
                ModBlocks.CRYSTAL_ROOST.get()),
            new WhetstoneRecipeCategory(gui, SHARPENING),
            new EmitterFuelRecipeCategory(gui, FUELING,
                Component.translatable("block.veskorius.field_emitter"),
                ModBlocks.FIELD_EMITTER.get()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registration.addRecipes(STABILIZING, machineRecipes(recipeManager, ModRecipeTypes.STABILIZING.get()));
        registration.addRecipes(ASSEMBLING, machineRecipes(recipeManager, ModRecipeTypes.ASSEMBLING.get()));
        registration.addRecipes(PURIFYING, machineRecipes(recipeManager, ModRecipeTypes.PURIFYING.get()));
        registration.addRecipes(CRUSHING, machineRecipes(recipeManager, ModRecipeTypes.CRUSHING.get()));
        registration.addRecipes(ROOSTING, machineRecipes(recipeManager, ModRecipeTypes.ROOSTING.get()));
        registration.addRecipes(SHARPENING, recipeManager.getAllRecipesFor(ModRecipeTypes.SHARPENING.get())
            .stream().map(RecipeHolder::value).toList());
        registration.addRecipes(FUELING, recipeManager.getAllRecipesFor(ModRecipeTypes.FUELING.get())
            .stream().map(RecipeHolder::value).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Le bloc de la machine « ouvre » ses recettes dans JEI (clic droit / R).
        catalyst(registration, ModBlocks.RESONANCE_STABILIZER.get(), STABILIZING);
        catalyst(registration, ModBlocks.COMPONENT_ASSEMBLER.get(), ASSEMBLING);
        catalyst(registration, ModBlocks.FLUX_PURIFIER.get(), PURIFYING);
        catalyst(registration, ModBlocks.CRYSTAL_CRUSHER.get(), CRUSHING);
        catalyst(registration, ModBlocks.CRYSTAL_ROOST.get(), ROOSTING);
        catalyst(registration, ModBlocks.RESONANCE_WHETSTONE.get(), SHARPENING);
        catalyst(registration, ModBlocks.FIELD_EMITTER.get(), FUELING);
    }

    private static List<MachineRecipe> machineRecipes(RecipeManager manager,
                                                      net.minecraft.world.item.crafting.RecipeType<MachineRecipe> type) {
        return manager.getAllRecipesFor(type).stream().map(RecipeHolder::value).toList();
    }

    private static void catalyst(IRecipeCatalystRegistration registration, ItemLike block, RecipeType<?> type) {
        registration.addRecipeCatalyst(block, type);
    }
}
