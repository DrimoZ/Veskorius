package com.veskorius.compat.jei;

import com.veskorius.recipe.EmitterFuelRecipe;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/**
 * Catégorie JEI des carburants du Field Emitter ({@link EmitterFuelRecipe}) : un
 * carburant à gauche, sa valeur en Osc à droite. Rend le registre data-driven
 * visible en jeu — « qu'est-ce que je peux brûler, et combien ça rend ? ».
 */
public class EmitterFuelRecipeCategory implements IRecipeCategory<EmitterFuelRecipe> {

    private static final int WIDTH = 132;
    private static final int HEIGHT = 28;

    private final RecipeType<EmitterFuelRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;

    public EmitterFuelRecipeCategory(IGuiHelper guiHelper, RecipeType<EmitterFuelRecipe> type,
                                     Component title, ItemLike iconItem) {
        this.type = type;
        this.title = title;
        this.icon = guiHelper.createDrawableItemLike(iconItem);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<EmitterFuelRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EmitterFuelRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
            .setBackground(slot, -1, -1)
            .addItemStacks(List.of(recipe.fuel().getItems()));
    }

    @Override
    public void draw(EmitterFuelRecipe recipe, IRecipeSlotsView slots,
                     GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, "→ " + recipe.osc() + " Osc", 30, 9, 0xFF404040, false);
    }
}
