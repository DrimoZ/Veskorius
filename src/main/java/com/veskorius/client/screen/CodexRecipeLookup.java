package com.veskorius.client.screen;

import com.veskorius.recipe.MachineRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * Trouve, pour un objet donné, la recette à <b>montrer</b> dans le Codex.
 *
 * <p>C'est la pièce qui manquait le plus : un guide de mod technique qui n'affiche aucune
 * recette n'est pas un guide, c'est une nouvelle. Le joueur devait sortir du Codex, ouvrir
 * JEI, et y chercher ce que le Codex venait de lui décrire — donc il n'ouvrait pas le
 * Codex.
 *
 * <p>Deux formes sont reconnues, et une seule est retournée : la recette d'<b>établi</b>
 * (comment fabriquer l'objet) et la recette de <b>machine</b> (ce qu'une machine posée
 * transforme). Elles ne se concurrencent pas — un bloc de machine a la première, un
 * matériau la seconde.
 *
 * <p>Tout se lit dans le {@code RecipeManager} du client, donc <b>ce que le serveur a
 * réellement chargé</b> : un datapack qui change une recette change la page du Codex, et
 * une recette écartée au chargement n'apparaît pas — le Codex ne peut pas mentir sur ce
 * point.
 */
public final class CodexRecipeLookup {

    /** Une recette prête à dessiner : des entrées, un résultat, et de quoi la légender. */
    public record View(List<Ingredient> inputs, ItemStack result, boolean shaped,
                       int gridWidth, @Nullable net.minecraft.network.chat.Component note) {
    }

    private CodexRecipeLookup() {
    }

    @Nullable
    public static View find(ItemStack target) {
        if (target.isEmpty() || Minecraft.getInstance().level == null) {
            return null;
        }
        var manager = Minecraft.getInstance().level.getRecipeManager();
        var registries = Minecraft.getInstance().level.registryAccess();

        View crafting = null;
        View machine = null;
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            Recipe<?> recipe = holder.value();
            ItemStack result = recipe.getResultItem(registries);
            if (result.isEmpty() || !ItemStack.isSameItem(result, target)) {
                continue;
            }
            if (recipe instanceof MachineRecipe m && machine == null) {
                machine = machineView(m, result);
            } else if (recipe instanceof CraftingRecipe && crafting == null) {
                crafting = craftingView(recipe, result);
            }
        }
        // L'établi d'abord : « comment j'obtiens cet objet » prime sur « ce que fait la
        // machine que cet objet est », et c'est la question qu'on se pose en ouvrant.
        return crafting != null ? crafting : machine;
    }

    private static View craftingView(Recipe<?> recipe, ItemStack result) {
        List<Ingredient> inputs = new ArrayList<>(recipe.getIngredients());
        int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
        return new View(inputs, result, recipe instanceof ShapedRecipe, Math.max(1, width), null);
    }

    private static View machineView(MachineRecipe recipe, ItemStack result) {
        List<Ingredient> inputs = new ArrayList<>(recipe.getIngredients());
        // La légende porte les deux chiffres qu'on cherche vraiment en lisant une machine :
        // combien de temps, et combien ça coûte.
        var note = net.minecraft.network.chat.Component.translatable(
            "gui.veskorius.codex.machine_note",
            String.format(java.util.Locale.ROOT, "%.0f", recipe.time() / 20.0),
            recipe.oscPerTick());
        return new View(inputs, result, false, 3, note);
    }
}
