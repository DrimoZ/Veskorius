package com.veskorius.compat.jei;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.recipe.DampingAgentRecipe;
import com.veskorius.recipe.EmitterFuelRecipe;
import com.veskorius.recipe.MachineRecipe;
import com.veskorius.recipe.ModRecipeTypes;
import com.veskorius.recipe.WhetstoneRecipe;
import java.util.List;
import java.util.function.Supplier;
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
import net.minecraft.world.level.block.Block;

/**
 * Intégration JEI : montre les recettes de fonctionnement des machines en jeu
 * (« comment obtient-on un Refined Crystal ? » → clic sur l'objet dans JEI).
 *
 * <p><b>Une machine se déclare une seule fois</b>, dans {@link #MACHINES}. Les trois
 * enregistrements que JEI réclame — la catégorie, les recettes, le catalyseur — en
 * découlent. C'est une correction, pas un embellissement : ces trois listes étaient
 * tenues à la main et il fallait penser aux trois. La Veskorian Alloy Forge n'était dans
 * aucune, et le Damping Array dans aucune non plus — leurs recettes étaient donc
 * <b>introuvables en jeu</b>, alors même qu'elles fonctionnaient parfaitement. Une
 * recette qu'on ne peut pas consulter est une recette qui n'existe pas pour le joueur :
 * rien dans le mod ne dit qu'une forge veut deux cristaux et deux lingots.
 *
 * <p>Chargé uniquement quand JEI est présent (scan des classes {@code @JeiPlugin}) ;
 * inerte sinon — d'où la dépendance JEI en {@code compileOnly}/{@code localRuntime}.
 */
@JeiPlugin
public class VeskoriusJeiPlugin implements IModPlugin {

    /**
     * Une machine à recettes : son type JEI, son type de recette, et le bloc qui sert à la
     * fois d'icône et de catalyseur. Le libellé de la catégorie est le nom du bloc — il
     * n'y a aucune raison qu'ils diffèrent, et une clé de langue de plus serait une clé
     * de plus à oublier.
     */
    private record Machine(RecipeType<MachineRecipe> jeiType,
                           Supplier<net.minecraft.world.item.crafting.RecipeType<MachineRecipe>> recipeType,
                           Supplier<? extends Block> block) {

        Component title() {
            return block.get().getName();
        }
    }

    private static Machine machine(String name,
                                   Supplier<net.minecraft.world.item.crafting.RecipeType<MachineRecipe>> type,
                                   Supplier<? extends Block> block) {
        return new Machine(RecipeType.create(Veskorius.MOD_ID, name, MachineRecipe.class), type, block);
    }

    /** <b>La liste unique.</b> Ajouter une machine à recettes = une ligne, ici. */
    private static final List<Machine> MACHINES = List.of(
        machine("stabilizing", ModRecipeTypes.STABILIZING::get, ModBlocks.RESONANCE_STABILIZER),
        machine("assembling", ModRecipeTypes.ASSEMBLING::get, ModBlocks.COMPONENT_ASSEMBLER),
        machine("purifying", ModRecipeTypes.PURIFYING::get, ModBlocks.FLUX_PURIFIER),
        machine("crushing", ModRecipeTypes.CRUSHING::get, ModBlocks.CRYSTAL_CRUSHER),
        machine("roosting", ModRecipeTypes.ROOSTING::get, ModBlocks.CRYSTAL_ROOST),
        machine("forging", ModRecipeTypes.FORGING::get, ModBlocks.VESKORIAN_ALLOY_FORGE),
        machine("compressing", ModRecipeTypes.COMPRESSING::get, ModBlocks.FLUX_COMPRESSOR),
        machine("synthesis", ModRecipeTypes.SYNTHESIS::get, ModBlocks.DEEP_SYNTHESIS_CHAMBER),
        machine("synthesizing", ModRecipeTypes.SYNTHESIZING::get, ModBlocks.STRUCTURAL_SYNTHESIZER));

    // Les trois catégories qui ne portent pas de MachineRecipe : elles ont chacune leur
    // propre disposition (un outil qu'on répare, un carburant, un agent de purge).
    public static final RecipeType<WhetstoneRecipe> SHARPENING =
        RecipeType.create(Veskorius.MOD_ID, "sharpening", WhetstoneRecipe.class);
    public static final RecipeType<EmitterFuelRecipe> FUELING =
        RecipeType.create(Veskorius.MOD_ID, "fueling", EmitterFuelRecipe.class);
    public static final RecipeType<DampingAgentRecipe> DAMPING =
        RecipeType.create(Veskorius.MOD_ID, "damping", DampingAgentRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        for (Machine m : MACHINES) {
            registration.addRecipeCategories(
                new MachineRecipeCategory(gui, m.jeiType(), m.title(), m.block().get()));
        }
        registration.addRecipeCategories(
            new WhetstoneRecipeCategory(gui, SHARPENING),
            new EmitterFuelRecipeCategory(gui, FUELING,
                Component.translatable("block.veskorius.field_emitter"),
                ModBlocks.FIELD_EMITTER.get()),
            new DampingAgentRecipeCategory(gui, DAMPING,
                Component.translatable("block.veskorius.damping_array"),
                ModBlocks.DAMPING_ARRAY.get()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        for (Machine m : MACHINES) {
            registration.addRecipes(m.jeiType(), all(recipeManager, m.recipeType().get()));
        }
        registration.addRecipes(SHARPENING, all(recipeManager, ModRecipeTypes.SHARPENING.get()));
        registration.addRecipes(FUELING, all(recipeManager, ModRecipeTypes.FUELING.get()));
        registration.addRecipes(DAMPING, all(recipeManager, ModRecipeTypes.DAMPING.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Le bloc de la machine « ouvre » ses recettes dans JEI (clic droit / R).
        for (Machine m : MACHINES) {
            catalyst(registration, m.block().get(), m.jeiType());
        }
        catalyst(registration, ModBlocks.RESONANCE_WHETSTONE.get(), SHARPENING);
        catalyst(registration, ModBlocks.FIELD_EMITTER.get(), FUELING);
        catalyst(registration, ModBlocks.TUNABLE_FIELD_EMITTER.get(), FUELING);
        catalyst(registration, ModBlocks.DAMPING_ARRAY.get(), DAMPING);
    }

    private static <I extends net.minecraft.world.item.crafting.RecipeInput,
                    T extends net.minecraft.world.item.crafting.Recipe<I>> List<T> all(
        RecipeManager manager, net.minecraft.world.item.crafting.RecipeType<T> type) {
        return manager.getAllRecipesFor(type).stream().map(RecipeHolder::value).toList();
    }

    private static void catalyst(IRecipeCatalystRegistration registration, ItemLike block, RecipeType<?> type) {
        registration.addRecipeCatalyst(block, type);
    }
}
