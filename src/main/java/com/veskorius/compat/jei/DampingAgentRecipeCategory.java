package com.veskorius.compat.jei;

import com.veskorius.recipe.DampingAgentRecipe;
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
 * Catégorie JEI des agents du Damping Array : un objet à gauche, la dissonance qu'il
 * absorbe à droite.
 *
 * <p>Elle manquait. Le registre des agents est data-driven, comme celui des carburants —
 * mais contrairement à eux il n'apparaissait <b>nulle part</b> en jeu. La seule façon
 * d'apprendre qu'un Refined Crystal purge un champ était de lire le JSON du mod. Une
 * machine dont on ne peut pas deviner l'entrée est une machine qu'on ne pose pas.
 */
public class DampingAgentRecipeCategory implements IRecipeCategory<DampingAgentRecipe> {

    private static final int WIDTH = 132;
    private static final int HEIGHT = 28;

    private final RecipeType<DampingAgentRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;

    public DampingAgentRecipeCategory(IGuiHelper guiHelper, RecipeType<DampingAgentRecipe> type,
                                      Component title, ItemLike iconItem) {
        this.type = type;
        this.title = title;
        this.icon = guiHelper.createDrawableItemLike(iconItem);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<DampingAgentRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, DampingAgentRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
            .setBackground(slot, -1, -1)
            .addItemStacks(List.of(recipe.agent().getItems()));
    }

    @Override
    public void draw(DampingAgentRecipe recipe, IRecipeSlotsView slots,
                     GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, "→ " + recipe.dissonance() + " diss.", 30, 9, 0xFF404040, false);
    }
}
