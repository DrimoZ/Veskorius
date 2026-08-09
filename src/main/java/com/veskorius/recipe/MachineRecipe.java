package com.veskorius.recipe;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Recette de fonctionnement d'une machine « traitement » (input → output) :
 * Resonance Stabilizer, Component Assembler, Flux Purifier, et toute machine
 * future du même style.
 *
 * Une seule classe pour toutes ces machines — ce qui les distingue est leur
 * {@link RecipeType} (un par machine), injecté par le serializer. Ainsi
 * {@code getRecipeFor(ASSEMBLING, ...)} ne renvoie jamais une recette de
 * Stabilizer, tout en réutilisant ce code.
 *
 * Au-delà d'un simple input→output, la recette porte le **temps de cycle** et le
 * **coût en Osc/tick** : ce sont des paramètres de la recette, plus des constantes
 * en dur. Ajouter un champ (sous-produit, chance d'échec propre à la recette, XP…)
 * ne demande qu'une ligne de plus dans {@link MachineRecipeSerializer}.
 *
 * Les ingrédients sont des {@link SizedIngredient} (item ou tag + quantité) et
 * sont appariés **positionnellement** aux slots d'entrée : l'ingrédient d'indice
 * i doit être satisfait par le slot d'entrée i.
 */
public class MachineRecipe implements Recipe<MachineRecipeInput> {

    private final Supplier<? extends RecipeType<?>> type;
    private final Supplier<? extends RecipeSerializer<?>> serializer;
    private final List<SizedIngredient> ingredients;
    private final ItemStack result;
    private final int time;
    private final int oscPerTick;
    private final boolean stable;
    private final ItemStack byproduct;

    public MachineRecipe(Supplier<? extends RecipeType<?>> type, Supplier<? extends RecipeSerializer<?>> serializer,
                         List<SizedIngredient> ingredients, ItemStack result, int time, int oscPerTick,
                         boolean stable, ItemStack byproduct) {
        this.type = type;
        this.serializer = serializer;
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
        this.time = time;
        this.oscPerTick = oscPerTick;
        this.stable = stable;
        this.byproduct = byproduct;
    }

    // --- Champs (getters nommés pour les codecs) -----------------------------

    public List<SizedIngredient> ingredients() {
        return ingredients;
    }

    public ItemStack result() {
        return result;
    }

    public int time() {
        return time;
    }

    public int oscPerTick() {
        return oscPerTick;
    }

    /**
     * Recette « increvable » (06-Energy.md) : elle réussit toujours, quel que soit le
     * déréglage harmonique de la machine — aucun surcoût de désaccord, aucune
     * dissonance produite. Toutes les recettes T1 le sont, pour que la boucle de
     * départ ne puisse jamais frustrer ; un modpack peut en marquer d'autres.
     */
    public boolean stable() {
        return stable;
    }

    /**
     * <b>Sous-produit</b> du cycle, vide par defaut.
     *
     * <p>Il etait code EN DUR dans les machines : la Forge retournait un
     * {@code new ItemStack(FLUX_SLAG)}, le Synthesizer un residu, le Damping Array de
     * la boue. Un datapack pouvait donc changer ce qu'une machine PRODUIT mais pas ce
     * qu'elle GACHE — la moitie de la boucle economique restait hors de portee, alors
     * que le reste du mod est data-driven de bout en bout.
     *
     * <p>La javadoc de cette classe annoncait pourtant le champ depuis le debut :
     * « ajouter un champ (sous-produit, chance d'echec propre a la recette, XP…) ne
     * demande qu'une ligne de plus dans le serializer ». Elle avait raison.
     */
    public ItemStack byproduct() {
        return byproduct;
    }

    // --- Recipe --------------------------------------------------------------

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        if (ingredients.size() > input.size()) {
            return false;
        }
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack inSlot = input.getItem(slot);
            if (slot < ingredients.size()) {
                SizedIngredient ingredient = ingredients.get(slot);
                if (!ingredient.test(inSlot) || inSlot.getCount() < ingredient.count()) {
                    return false;
                }
            } else if (!inSlot.isEmpty()) {
                // Un slot d'entrée sans ingrédient associé doit être vide : on ne
                // veut pas qu'un objet parasite passe inaperçu.
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient sized : ingredients) {
            list.add(sized.ingredient());
        }
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    /**
     * <b>Hors du livre de recettes vanilla.</b>
     *
     * <p>Le livre classe toutes les recettes chargées et se plaignait des nôtres à chaque
     * entrée dans un monde — vingt lignes de « Unknown recipe category » par chargement,
     * avec un identifiant malformé où le namespace apparaissait deux fois. Il essayait de
     * ranger dans un onglet d'établi des recettes qui ne se font pas à l'établi.
     *
     * <p>{@code isSpecial()} est le mécanisme prévu pour ça : une recette spéciale ne
     * s'affiche pas au livre et ne se débloque pas. C'est exact ici — on ne fabrique pas
     * ces recettes, une machine les exécute — et ça vide le journal d'un bruit que
     * personne n'aurait fini par lire.
     *
     * <p>JEI, lui, les montre : il a ses propres catégories, déclarées par le plugin du
     * mod. Le livre vanilla et JEI ne servent pas la même chose.
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return type.get();
    }
}
