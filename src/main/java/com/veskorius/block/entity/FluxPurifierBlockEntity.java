package com.veskorius.block.entity;

import com.veskorius.item.ModItems;
import com.veskorius.menu.FluxPurifierMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #5 (05-Machines.md) : Stable Resonance Crystal + Redstone -> Refined
 * Resonance Crystal, 45 s (22 s en surchauffe), 2 Osc/tick (4 en surchauffe).
 *
 * Première machine à **mode surchauffe** (05-Machines.md, style de craft #1) :
 * temps ÷2 et consommation ×2 sont gérés génériquement par le socle
 * ({@code getEffectiveCycleTicks} / {@code getEffectiveOscPerTick}). Ce qui reste
 * ici, c'est le seul effet que le socle ne peut pas connaître : le **risque de
 * perte de l'input** — 20 % de chance, en surchauffe, que le cycle consomme
 * l'entrée sans produire la sortie.
 */
public class FluxPurifierBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_CRYSTAL = 0;
    public static final int SLOT_REDSTONE = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    /** 45 secondes hors surchauffe (05-Machines.md #5). */
    private static final int CYCLE_TICKS = 45 * 20;

    /** 2 Osc/tick hors surchauffe (05-Machines.md #5). */
    private static final int OSC_PER_TICK = 2;

    /** 20 % de chance de perdre l'input en surchauffe (06-Energy.md). */
    private static final float OVERHEAT_LOSS_CHANCE = 0.2f;

    public FluxPurifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUX_PURIFIER.get(), pos, state, SLOT_COUNT);
    }

    @Override
    protected int getBaseCycleTicks() {
        return CYCLE_TICKS;
    }

    @Override
    protected int getOscPerTick() {
        return OSC_PER_TICK;
    }

    @Override
    public boolean supportsOverheat() {
        return true;
    }

    @Override
    protected boolean canRunCycle() {
        if (inventory.getStackInSlot(SLOT_CRYSTAL).isEmpty()
            || inventory.getStackInSlot(SLOT_REDSTONE).isEmpty()) {
            return false;
        }
        return canInsertInto(SLOT_OUTPUT, result());
    }

    @Override
    protected void runCycle() {
        inventory.extractItem(SLOT_CRYSTAL, 1, false);
        inventory.extractItem(SLOT_REDSTONE, 1, false);

        // En surchauffe, 20 % de chance que l'entrée parte en fumée sans sortie.
        // level est forcément non nul et serveur ici (appelé depuis serverTick).
        if (isOverheatActive() && level.getRandom().nextFloat() < OVERHEAT_LOSS_CHANCE) {
            return;
        }
        insertInto(SLOT_OUTPUT, result());
    }

    private static ItemStack result() {
        return new ItemStack(ModItems.REFINED_RESONANCE_CRYSTAL.get());
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CRYSTAL -> stack.is(ModItems.STABLE_RESONANCE_CRYSTAL.get());
            case SLOT_REDSTONE -> stack.is(Items.REDSTONE);
            case SLOT_OUTPUT -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.flux_purifier");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FluxPurifierMenu(containerId, playerInventory, this);
    }
}
