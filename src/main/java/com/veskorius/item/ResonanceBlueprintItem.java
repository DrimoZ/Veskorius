package com.veskorius.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Blueprint restauré — la clé de craft d'un tier (03-Progression.md, gatekeeping
 * physique). Requis comme ingrédient dans les recettes du tier, mais **rendu** après
 * le craft ({@link #getCraftingRemainder}) : un seul blueprint sert pour un nombre
 * illimité de machines. Rien n'est masqué ; ce qui bloque, c'est de ne pas l'avoir.
 *
 * Le tier est porté par le Data Component {@link ModDataComponents#BLUEPRINT_TIER}.
 */
public class ResonanceBlueprintItem extends Item {

    public static final int DEFAULT_TIER = 2;

    public ResonanceBlueprintItem(Properties properties) {
        super(properties);
    }

    /** Pile de blueprint d'un tier donné (utilisée par la console et l'onglet créatif). */
    public static ItemStack of(int tier) {
        ItemStack stack = new ItemStack(ModItems.RESONANCE_BLUEPRINT.get());
        stack.set(ModDataComponents.BLUEPRINT_TIER.get(), tier);
        return stack;
    }

    public static int tierOf(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BLUEPRINT_TIER.get(), DEFAULT_TIER);
    }

    /**
     * Rendu du craft : le blueprint reste dans la grille (copie de lui-même), jamais
     * consommé. C'est ce qui en fait une clé réutilisable plutôt qu'un consommable.
     * Il faut surcharger les DEUX méthodes NeoForge : {@code has…} décide qu'un rendu
     * existe, {@code get…} le fournit (sinon le premier renvoie false et le second
     * n'est jamais appelé).
     */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.veskorius.resonance_blueprint.tier", tierOf(stack))
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.veskorius.resonance_blueprint.hint")
            .withStyle(ChatFormatting.DARK_GRAY));
    }
}
