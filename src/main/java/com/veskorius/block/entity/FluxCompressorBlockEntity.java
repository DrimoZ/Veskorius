package com.veskorius.block.entity;

import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Flux Compressor</b> (machine #23, 05-Machines.md) : 4 Refined Crystal → 1
 * Concentrated Flux, 30 s, 6 Osc/tick.
 *
 * <p>La seule machine du mod qui ne fait que <b>condenser</b> : quatre cristaux entrent,
 * un objet sort. C'est volontairement une perte apparente — le Flux Concentré n'a d'intérêt
 * qu'au T4 et au T5, où il remplace des piles entières de cristaux dans des recettes qui
 * seraient sinon injouables à transporter. Poser un Compresseur, c'est décider d'investir
 * maintenant pour un palier qu'on n'a pas encore.
 *
 * <p>Une seule entrée, comme le Crusher : la disposition à un slot est déjà couverte par le
 * socle, cette classe ne déclare donc que ses slots.
 */
public class FluxCompressorBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_AUGMENT = 2;
    public static final int SLOT_COUNT = 3;

    public FluxCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUX_COMPRESSOR.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.COMPRESSING::get, new int[] {SLOT_INPUT}, SLOT_OUTPUT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.flux_compressor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.FluxCompressorMenu(containerId, playerInventory, this);
    }
}
