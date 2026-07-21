package com.veskorius.block.entity;

import com.veskorius.recipe.MachineRecipe;
import com.veskorius.recipe.MachineRecipeInput;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

/**
 * Socle des machines « traitement » (input → output) pilotées par une recette
 * data-driven ({@link MachineRecipe}).
 *
 * Tout le cycle est générique : trouver la recette qui correspond aux entrées,
 * en tirer le temps et le coût en Osc, consommer les ingrédients, produire le
 * résultat. Une nouvelle machine de ce style ne code AUCUNE recette : elle
 * déclare son {@link RecipeType}, ses slots d'entrée et son slot de sortie, et
 * fournit ses recettes en JSON.
 *
 * La recette courante est mise en cache et réévaluée seulement quand une entrée
 * change (voir {@link #onSlotChanged}), pour ne pas rescanner à chaque tick.
 */
public abstract class AbstractProcessingMachineBlockEntity extends AbstractMachineBlockEntity {

    /** Durée repli quand aucune recette ne correspond (la machine ne tourne pas alors). */
    private static final int NO_RECIPE_TICKS = 1;

    private final Supplier<RecipeType<MachineRecipe>> recipeType;
    private final int[] inputSlots;
    private final int outputSlot;

    @Nullable
    private RecipeHolder<MachineRecipe> cachedRecipe;
    private boolean recipeDirty = true;

    protected AbstractProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                                   int slotCount, Supplier<RecipeType<MachineRecipe>> recipeType,
                                                   int[] inputSlots, int outputSlot) {
        super(type, pos, state, slotCount);
        this.recipeType = recipeType;
        this.inputSlots = inputSlots;
        this.outputSlot = outputSlot;
    }

    // --- Recherche de recette ------------------------------------------------

    @Override
    protected void onSlotChanged(int slot) {
        for (int inputSlot : inputSlots) {
            if (inputSlot == slot) {
                recipeDirty = true;
                return;
            }
        }
    }

    @Nullable
    private RecipeHolder<MachineRecipe> currentRecipe() {
        if (recipeDirty) {
            recipeDirty = false;
            if (level == null) {
                cachedRecipe = null;
            } else {
                cachedRecipe = level.getRecipeManager()
                    .getRecipeFor(recipeType.get(), makeInput(), level)
                    .orElse(null);
            }
        }
        return cachedRecipe;
    }

    private MachineRecipeInput makeInput() {
        List<ItemStack> stacks = new ArrayList<>(inputSlots.length);
        for (int slot : inputSlots) {
            stacks.add(inventory.getStackInSlot(slot));
        }
        return new MachineRecipeInput(stacks);
    }

    private int inputIndexOf(int slot) {
        for (int i = 0; i < inputSlots.length; i++) {
            if (inputSlots[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Filtre d'entrée piloté par les recettes : un slot d'entrée n'accepte qu'un
     * objet qui figure comme ingrédient à cette position dans au moins une recette
     * de la machine. Ajouter une recette ouvre donc automatiquement le slot au
     * nouvel ingrédient — aucune restriction en dur à maintenir.
     */
    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        if (slot == outputSlot) {
            return false;
        }
        int inputIndex = inputIndexOf(slot);
        if (inputIndex < 0) {
            // Slot d'augment (ou autre) : délègue au socle.
            return super.isItemValid(slot, stack);
        }
        if (level == null) {
            // Avant que le niveau/les recettes soient disponibles : permissif.
            return true;
        }
        for (RecipeHolder<MachineRecipe> holder : level.getRecipeManager().getAllRecipesFor(recipeType.get())) {
            List<SizedIngredient> ingredients = holder.value().ingredients();
            if (inputIndex < ingredients.size() && ingredients.get(inputIndex).ingredient().test(stack)) {
                return true;
            }
        }
        return false;
    }

    // --- Cycle ---------------------------------------------------------------

    @Override
    protected int getBaseCycleTicks() {
        RecipeHolder<MachineRecipe> recipe = currentRecipe();
        return recipe != null ? recipe.value().time() : NO_RECIPE_TICKS;
    }

    @Override
    protected int getOscPerTick() {
        RecipeHolder<MachineRecipe> recipe = currentRecipe();
        return recipe != null ? recipe.value().oscPerTick() : 0;
    }

    @Override
    protected boolean canRunCycle() {
        RecipeHolder<MachineRecipe> recipe = currentRecipe();
        return recipe != null && canInsertInto(outputSlot, recipe.value().getResultItem(null));
    }

    @Override
    protected void runCycle() {
        RecipeHolder<MachineRecipe> recipe = currentRecipe();
        if (recipe == null) {
            return;
        }
        MachineRecipe value = recipe.value();

        // Consomme chaque ingrédient dans son slot d'entrée (appariement positionnel).
        List<SizedIngredient> ingredients = value.ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            inventory.extractItem(inputSlots[i], ingredients.get(i).count(), false);
        }

        // Les entrées sont toujours consommées ; la sortie peut être omise (par
        // ex. perte en surchauffe). Voir {@link #shouldProduceResult}.
        if (shouldProduceResult()) {
            insertInto(outputSlot, value.result().copy());
        }
    }

    /**
     * Décide, en fin de cycle, si la sortie est produite. Vrai par défaut ; une
     * machine à surchauffe le redéfinit pour introduire un risque de perte.
     */
    protected boolean shouldProduceResult() {
        return true;
    }
}
