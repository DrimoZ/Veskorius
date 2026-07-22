package com.veskorius.block;

import com.veskorius.item.ModItems;
import com.veskorius.item.ResonanceBlueprintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Console d'attunement de l'Avant-poste (08-Structures.md) : la « machine morte »
 * qu'on peut en fait réveiller <em>sur place</em>. Clic droit → remet au joueur le
 * {@code resonance_blueprint} T2 (la clé de craft du T2), s'il n'en a pas déjà un.
 *
 * C'est la porte du T2 : un geste physique, dans la ruine, pas une case cochée.
 * Générée uniquement en structure ; pas d'objet (non récupérable) ; minée = rien.
 */
public class AttunementConsoleBlock extends Block {

    /** Tier du blueprint que cette console restaure. */
    private static final int TIER = 2;

    public AttunementConsoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean given = tryGiveBlueprint(player, TIER);
        // L'advancement tier2_field se déclenche tout seul quand le joueur a un
        // blueprint (InventoryChangeTrigger, voir ModAdvancementProvider).
        player.displayClientMessage(Component.translatable(given
            ? "block.veskorius.attunement_console.restored"
            : "block.veskorius.attunement_console.already")
            .withStyle(given ? ChatFormatting.GOLD : ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }

    /**
     * Donne un blueprint du tier au joueur s'il n'en a pas déjà un (anti-doublon).
     * Retourne vrai si un blueprint a été donné. Statique pour être testable sans
     * simuler l'interaction complète.
     */
    public static boolean tryGiveBlueprint(Player player, int tier) {
        if (hasBlueprintOfTier(player, tier)) {
            return false;
        }
        ItemStack blueprint = ResonanceBlueprintItem.of(tier);
        if (!player.addItem(blueprint)) {
            player.drop(blueprint, false);
        }
        return true;
    }

    private static boolean hasBlueprintOfTier(Player player, int tier) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.RESONANCE_BLUEPRINT.get()) && ResonanceBlueprintItem.tierOf(stack) == tier) {
                return true;
            }
        }
        return false;
    }
}
