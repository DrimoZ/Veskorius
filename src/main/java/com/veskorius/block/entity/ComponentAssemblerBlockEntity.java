package com.veskorius.block.entity;

import com.veskorius.item.ModItems;
import com.veskorius.menu.ComponentAssemblerMenu;
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
 * Machine #2 (05-Machines.md) : 1 Stable Resonance Crystal + 2 Iron Ingot ->
 * 2 Resonance Component, 5 secondes, 3 Osc/tick.
 *
 * Premiere machine du mod qui CONSOMME de l'energie : elle ne tourne que dans un
 * champ de Resonance (voir {@link AbstractMachineBlockEntity#getOscPerTick}). Hors
 * champ, elle met son cycle en pause sans le perdre.
 *
 * Branche alternative prevue par 04-Materials.md (3 Resonance Dust + 2 Iron ->
 * 2 Component, sans consommer de cristal stable) : NON codee ici, car le
 * resonance_dust n'existe pas avant le Crystal Crusher (tache 13). Quand il
 * existera, il faudra brancher canRunCycle/runCycle sur l'un OU l'autre jeu
 * d'entrees (comme le fera l'Alloy Forge selon le metal). Ce n'est pas un simple
 * tag 1:1 (comptes differents : 1 cristal contre 3 poussieres), donc pas
 * pre-cable maintenant.
 */
public class ComponentAssemblerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_CRYSTAL = 0;
    public static final int SLOT_IRON = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    /** 5 secondes (05-Machines.md #2). */
    private static final int CYCLE_TICKS = 5 * 20;

    /** 3 Osc/tick (05-Machines.md #2). */
    private static final int OSC_PER_TICK = 3;

    private static final int IRON_PER_CYCLE = 2;
    private static final int COMPONENTS_PER_CYCLE = 2;

    public ComponentAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPONENT_ASSEMBLER.get(), pos, state, SLOT_COUNT);
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
    protected boolean canRunCycle() {
        if (inventory.getStackInSlot(SLOT_CRYSTAL).isEmpty()) {
            return false;
        }
        if (inventory.getStackInSlot(SLOT_IRON).getCount() < IRON_PER_CYCLE) {
            return false;
        }
        return canInsertInto(SLOT_OUTPUT, result());
    }

    @Override
    protected void runCycle() {
        inventory.extractItem(SLOT_CRYSTAL, 1, false);
        inventory.extractItem(SLOT_IRON, IRON_PER_CYCLE, false);
        insertInto(SLOT_OUTPUT, result());
    }

    private static ItemStack result() {
        return new ItemStack(ModItems.RESONANCE_COMPONENT.get(), COMPONENTS_PER_CYCLE);
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CRYSTAL -> stack.is(ModItems.STABLE_RESONANCE_CRYSTAL.get());
            case SLOT_IRON -> stack.is(Items.IRON_INGOT);
            case SLOT_OUTPUT -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.component_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComponentAssemblerMenu(containerId, playerInventory, this);
    }
}
