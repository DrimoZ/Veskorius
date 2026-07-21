package com.veskorius.compat.jei;

import com.veskorius.recipe.MachineRecipe;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Catégorie JEI réutilisable pour les machines input→output ({@link MachineRecipe}).
 * Une instance par machine — chacune reçoit son type JEI, son titre et son icône.
 * Affiche les entrées à gauche, le résultat à droite, et sous la flèche le temps
 * de cycle et le coût en Osc.
 */
public class MachineRecipeCategory implements IRecipeCategory<MachineRecipe> {

    private static final int WIDTH = 132;
    private static final int HEIGHT = 46;

    private final RecipeType<MachineRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;

    public MachineRecipeCategory(IGuiHelper guiHelper, RecipeType<MachineRecipe> type,
                                 Component title, ItemLike iconItem) {
        this.type = type;
        this.title = title;
        this.icon = guiHelper.createDrawableItemLike(iconItem);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<MachineRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
        List<SizedIngredient> ingredients = recipe.ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 5 + i * 20)
                .setBackground(slot, -1, -1)
                .addItemStacks(List.of(ingredients.get(i).getItems()));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 22, 14)
            .setBackground(slot, -1, -1)
            .addItemStack(recipe.result());
    }

    @Override
    public void draw(MachineRecipe recipe, IRecipeSlotsView slots,
                     GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        // Flèche entrées → sortie.
        graphics.drawString(font, "→", WIDTH - 46, 16, 0xFF808080, false);

        StringBuilder info = new StringBuilder(recipe.time() / 20 + "s");
        if (recipe.oscPerTick() > 0) {
            info.append("  ").append(recipe.oscPerTick()).append(" Osc/t");
        }
        graphics.drawString(font, info.toString(), 5, HEIGHT - 9, 0xFF404040, false);
    }
}
