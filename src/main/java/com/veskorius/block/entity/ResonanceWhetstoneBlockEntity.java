package com.veskorius.block.entity;

import com.veskorius.item.ModItems;
import com.veskorius.menu.ResonanceWhetstoneMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #3 (05-Machines.md) : outil endommage + Stable Resonance Crystal ->
 * outil repare de 25%, 8 secondes, autonome.
 *
 * Premiere machine du mod qui ne fabrique rien : elle transforme son entree au
 * lieu de la consommer pour produire autre chose. Elle sert donc de test du
 * socle autant que de contenu — {@link AbstractMachineBlockEntity} ne suppose
 * rien sur la nature du cycle, seulement qu'il a une condition et un effet.
 */
public class ResonanceWhetstoneBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_CRYSTAL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    /** 8 secondes (05-Machines.md #3). */
    private static final int CYCLE_TICKS = 8 * 20;

    /** "Outil repare de 25%" : 25% de la durabilite maximale, pas des degats subis. */
    private static final int REPAIR_FRACTION = 4;

    public ResonanceWhetstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_WHETSTONE.get(), pos, state, SLOT_COUNT);
    }

    @Override
    protected int getBaseCycleTicks() {
        return CYCLE_TICKS;
    }

    @Override
    protected boolean canRunCycle() {
        ItemStack tool = inventory.getStackInSlot(SLOT_TOOL);
        if (!tool.isDamaged()) {
            // Couvre aussi le slot vide et les objets non endommageables : inutile
            // d'user un cristal sur un outil deja intact.
            return false;
        }
        if (inventory.getStackInSlot(SLOT_CRYSTAL).isEmpty()) {
            return false;
        }
        // L'outil repare part en sortie : il faut que le slot soit libre. Pas de
        // canInsertInto ici, un outil endommageable ne s'empile pas.
        return inventory.getStackInSlot(SLOT_OUTPUT).isEmpty();
    }

    @Override
    protected void runCycle() {
        ItemStack tool = inventory.extractItem(SLOT_TOOL, 1, false);
        inventory.extractItem(SLOT_CRYSTAL, 1, false);

        int repaired = Math.max(0, tool.getDamageValue() - tool.getMaxDamage() / REPAIR_FRACTION);
        tool.setDamageValue(repaired);

        inventory.setStackInSlot(SLOT_OUTPUT, tool);
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_TOOL -> stack.isDamageableItem();
            case SLOT_CRYSTAL -> stack.is(ModItems.STABLE_RESONANCE_CRYSTAL.get());
            case SLOT_OUTPUT -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.resonance_whetstone");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ResonanceWhetstoneMenu(containerId, playerInventory, this);
    }
}
