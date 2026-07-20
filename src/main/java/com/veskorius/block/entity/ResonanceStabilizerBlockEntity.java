package com.veskorius.block.entity;

import com.veskorius.item.ModItems;
import com.veskorius.menu.ResonanceStabilizerMenu;
import com.veskorius.tag.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #1 (05-Machines.md) : Raw Resonance Crystal + Quartz -> Stable
 * Resonance Crystal, 30 secondes, autonome (aucune consommation d'Osc).
 *
 * Premiere machine du mod, et premiere implementation de
 * {@link AbstractMachineBlockEntity} : elle sert de reference pour les 22
 * suivantes.
 */
public class ResonanceStabilizerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_CRYSTAL = 0;
    public static final int SLOT_FLUX = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    /** 30 secondes (05-Machines.md #1). */
    private static final int CYCLE_TICKS = 30 * 20;

    public ResonanceStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_STABILIZER.get(), pos, state, SLOT_COUNT);
    }

    @Override
    protected int getBaseCycleTicks() {
        return CYCLE_TICKS;
    }

    @Override
    protected boolean canRunCycle() {
        if (inventory.getStackInSlot(SLOT_CRYSTAL).isEmpty()
            || inventory.getStackInSlot(SLOT_FLUX).isEmpty()) {
            return false;
        }
        return canInsertInto(SLOT_OUTPUT, result());
    }

    @Override
    protected void runCycle() {
        inventory.extractItem(SLOT_CRYSTAL, 1, false);
        inventory.extractItem(SLOT_FLUX, 1, false);
        insertInto(SLOT_OUTPUT, result());
    }

    private static ItemStack result() {
        return new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get());
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CRYSTAL -> stack.is(ModItems.RAW_RESONANCE_CRYSTAL.get());
            case SLOT_FLUX -> stack.is(ModTags.Items.STABILIZER_FLUX);
            case SLOT_OUTPUT -> false;
            default -> super.isItemValid(slot, stack);
        };
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
