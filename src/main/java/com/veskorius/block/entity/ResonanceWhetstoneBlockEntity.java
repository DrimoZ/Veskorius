package com.veskorius.block.entity;

import com.veskorius.menu.ResonanceWhetstoneMenu;
import com.veskorius.recipe.MachineRecipeInput;
import com.veskorius.recipe.ModRecipeTypes;
import com.veskorius.recipe.WhetstoneRecipe;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #3 (05-Machines.md). Répare un outil endommagé — une forme qui ne rentre
 * pas dans le moule input→output, d'où son type de recette dédié
 * {@code veskorius:sharpening} et cette classe custom (elle n'hérite pas d'
 * {@link AbstractProcessingMachineBlockEntity}, dont le résultat est fixe).
 *
 * Ce qui vient de la recette (JSON) : le catalyseur, le pourcentage réparé, le
 * temps. Ce qui reste ici : consommer les entrées et poser l'outil réparé.
 */
public class ResonanceWhetstoneBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_CRYSTAL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    private static final int NO_RECIPE_TICKS = 1;

    @Nullable
    private RecipeHolder<WhetstoneRecipe> cachedRecipe;
    private boolean recipeDirty = true;

    private static final int[] AUTOMATION_INPUTS = {SLOT_TOOL, SLOT_CRYSTAL};
    private static final int[] AUTOMATION_OUTPUTS = {SLOT_OUTPUT};

    public ResonanceWhetstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_WHETSTONE.get(), pos, state, SLOT_COUNT);
    }

    @Override
    protected int[] getAutomationInputSlots() {
        return AUTOMATION_INPUTS;
    }

    @Override
    protected int[] getAutomationOutputSlots() {
        return AUTOMATION_OUTPUTS;
    }

    // --- Recherche de recette ------------------------------------------------

    @Override
    protected void onSlotChanged(int slot) {
        if (slot == SLOT_TOOL || slot == SLOT_CRYSTAL) {
            recipeDirty = true;
        }
    }

    @Nullable
    private RecipeHolder<WhetstoneRecipe> currentRecipe() {
        if (recipeDirty) {
            recipeDirty = false;
            cachedRecipe = level == null ? null : level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.SHARPENING.get(), makeInput(), level)
                .orElse(null);
        }
        return cachedRecipe;
    }

    private MachineRecipeInput makeInput() {
        return new MachineRecipeInput(List.of(
            inventory.getStackInSlot(SLOT_TOOL),
            inventory.getStackInSlot(SLOT_CRYSTAL)));
    }

    // --- Cycle ---------------------------------------------------------------

    @Override
    protected int getBaseCycleTicks() {
        RecipeHolder<WhetstoneRecipe> recipe = currentRecipe();
        return recipe != null ? recipe.value().time() : NO_RECIPE_TICKS;
    }

    @Override
    protected boolean canRunCycle() {
        // matches() garantit déjà un outil endommagé + le catalyseur présent ; il
        // ne reste qu'à vérifier que la sortie est libre (un outil ne s'empile pas).
        return currentRecipe() != null && inventory.getStackInSlot(SLOT_OUTPUT).isEmpty();
    }

    @Override
    protected void runCycle() {
        RecipeHolder<WhetstoneRecipe> recipe = currentRecipe();
        if (recipe == null) {
            return;
        }
        WhetstoneRecipe value = recipe.value();

        ItemStack tool = inventory.extractItem(SLOT_TOOL, 1, false);
        inventory.extractItem(SLOT_CRYSTAL, value.catalyst().count(), false);
        inventory.setStackInSlot(SLOT_OUTPUT, value.repair(tool));
    }

    // --- Filtre d'entrée -----------------------------------------------------

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_TOOL -> stack.isDamageableItem();
            case SLOT_CRYSTAL -> acceptsCatalyst(stack);
            case SLOT_OUTPUT -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    /** Piloté par les recettes : accepte tout catalyseur d'une recette de sharpening. */
    private boolean acceptsCatalyst(ItemStack stack) {
        if (level == null) {
            return true;
        }
        for (RecipeHolder<WhetstoneRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.SHARPENING.get())) {
            if (holder.value().catalyst().ingredient().test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.resonance_whetstone");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ResonanceWhetstoneMenu(containerId, playerInventory, this);
    }
}
