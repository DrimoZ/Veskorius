package com.veskorius.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Fragment de Codex (02-Lore.md, 08-Structures.md) : du **lore lisible**, jamais un
 * déblocage. Clic droit = lire (affiche le texte de l'entrée), **non consommé** :
 * relisible et transmissible. L'entrée est portée par le Data Component
 * {@link ModDataComponents#CODEX_ENTRY}.
 */
public class CodexFragmentItem extends Item {

    public CodexFragmentItem(Properties properties) {
        super(properties);
    }

    /** Pile de fragment portant une entrée de Codex donnée. */
    public static ItemStack of(ResourceLocation entry) {
        ItemStack stack = new ItemStack(ModItems.CODEX_FRAGMENT.get());
        stack.set(ModDataComponents.CODEX_ENTRY.get(), entry);
        return stack;
    }

    @Nullable
    public static ResourceLocation entryOf(ItemStack stack) {
        return stack.get(ModDataComponents.CODEX_ENTRY.get());
    }

    /** Clé de langue du titre d'une entrée : {@code codex.<namespace>.<path avec / → .>.title}. */
    public static String titleKey(ResourceLocation entry) {
        return "codex." + entry.getNamespace() + "." + entry.getPath().replace('/', '.') + ".title";
    }

    /** Clé de langue du corps d'une entrée. */
    public static String textKey(ResourceLocation entry) {
        return "codex." + entry.getNamespace() + "." + entry.getPath().replace('/', '.') + ".text";
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResourceLocation entry = entryOf(stack);
        if (entry == null) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            // Lecture : titre + texte en chat. Non consommé (relisible).
            player.displayClientMessage(Component.translatable(titleKey(entry)).withStyle(ChatFormatting.GOLD), false);
            player.displayClientMessage(Component.translatable(textKey(entry)).withStyle(ChatFormatting.GRAY), false);
            // Lire un fragment consigne sa page de lore dans le Codex (15-Codex-Guidebook.md).
            com.veskorius.codex.CodexUnlocks.grantForFragment(player, entry);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation entry = entryOf(stack);
        if (entry != null) {
            tooltip.add(Component.translatable(titleKey(entry)).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.veskorius.codex_fragment.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
