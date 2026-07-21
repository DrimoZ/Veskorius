package com.veskorius.compat.jei;

import com.veskorius.block.ModBlocks;
import com.veskorius.recipe.WhetstoneRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Catégorie JEI du Resonance Whetstone (réparation). Le résultat dépend de l'outil,
 * donc on montre un exemple concret (pioche en fer abîmée → réparée) plus le
 * catalyseur, et on précise le pourcentage réparé.
 */
public class WhetstoneRecipeCategory implements IRecipeCategory<WhetstoneRecipe> {

    private static final int WIDTH = 132;
    private static final int HEIGHT = 46;

    private final RecipeType<WhetstoneRecipe> type;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable slot;

    public WhetstoneRecipeCategory(IGuiHelper guiHelper, RecipeType<WhetstoneRecipe> type) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.RESONANCE_WHETSTONE.get());
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<WhetstoneRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.veskorius.resonance_whetstone");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WhetstoneRecipe recipe, IFocusGroup focuses) {
        // Exemple d'outil : une pioche en fer bien abîmée, pour illustrer.
        ItemStack damaged = new ItemStack(Items.IRON_PICKAXE);
        damaged.setDamageValue(damaged.getMaxDamage() - 1);

        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
            .setBackground(slot, -1, -1)
            .addItemStack(damaged);
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 25)
            .setBackground(slot, -1, -1)
            .addItemStacks(List.of(recipe.catalyst().getItems()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 22, 14)
            .setBackground(slot, -1, -1)
            .addItemStack(recipe.repair(damaged));
    }

    @Override
    public void draw(WhetstoneRecipe recipe, IRecipeSlotsView slots,
                     GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, "→", WIDTH - 46, 16, 0xFF808080, false);
        graphics.drawString(font, "+" + recipe.repairPercent() + "%  " + recipe.time() / 20 + "s",
            5, HEIGHT - 9, 0xFF404040, false);
    }
}
