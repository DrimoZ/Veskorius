package com.veskorius.block.entity;

import com.veskorius.menu.ResonanceStabilizerMenu;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #1 (05-Machines.md). Sa recette de fonctionnement (Raw Crystal + Quartz
 * → Stable Crystal, 30 s, autonome) vit désormais en JSON — voir le type de
 * recette {@code veskorius:stabilizing}. Cette classe ne fait que déclarer sa
 * disposition de slots ; tout le cycle est dans {@link AbstractProcessingMachineBlockEntity}.
 */
public class ResonanceStabilizerBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_CRYSTAL = 0;
    public static final int SLOT_FLUX = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    public ResonanceStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_STABILIZER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.STABILIZING::get, new int[] {SLOT_CRYSTAL, SLOT_FLUX}, SLOT_OUTPUT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.resonance_stabilizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ResonanceStabilizerMenu(containerId, playerInventory, this);
    }
}
