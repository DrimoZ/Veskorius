package com.veskorius.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Le Codex de Résonance (15-Codex-Guidebook.md) : le manuel en jeu, qui s'écrit tout
 * seul au fil de la progression. Clic droit → ouvre l'écran du Codex (côté client).
 * L'état de déblocage est porté par l'objet ({@link ModDataComponents#CODEX_UNLOCKED}),
 * lu directement par le GUI — pas de packet custom.
 */
public class ResonanceCodexItem extends Item {

    public ResonanceCodexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            // Classe client uniquement : chargée seulement quand cette branche
            // s'exécute, donc jamais sur un serveur dédié.
            com.veskorius.client.CodexClient.open(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.veskorius.resonance_codex.hint")
            .withStyle(ChatFormatting.GRAY));
    }
}
