package com.veskorius.menu;

import com.veskorius.block.entity.FieldEmitterBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menu du Field Emitter. N'hérite PAS d'{@link AbstractMachineMenu} : le Field
 * Emitter n'est pas une machine à cycle (ni progression ni slot d'augment), il
 * a un seul slot de carburant et une réserve d'Osc à afficher.
 *
 * Convention d'indices conservée : le slot machine (carburant) est en premier,
 * donc son indice de menu (0) == son indice d'inventaire.
 */
public class FieldEmitterMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_SLOTS = 27;
    private static final int PLAYER_HOTBAR_SLOTS = 9;
    private static final int PLAYER_SLOTS = PLAYER_INVENTORY_SLOTS + PLAYER_HOTBAR_SLOTS;
    private static final int MACHINE_SLOTS = 1;

    private final FieldEmitterBlockEntity blockEntity;
    private final ContainerData data;

    public FieldEmitterMenu(int containerId, Inventory playerInventory, FieldEmitterBlockEntity blockEntity) {
        super(ModMenuTypes.FIELD_EMITTER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getData();

        // Slot de carburant, centré comme le slot de combustible d'un four.
        addSlot(new SlotItemHandler(blockEntity.getFuelHandler(),
            FieldEmitterBlockEntity.SLOT_FUEL, 80, 35));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public FieldEmitterMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (FieldEmitterBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public int getReserve() {
        return data.get(FieldEmitterBlockEntity.DATA_RESERVE);
    }

    public int getCapacity() {
        int capacity = data.get(FieldEmitterBlockEntity.DATA_CAPACITY);
        return capacity <= 0 ? 1 : capacity;
    }

    /** Réserve ramenée à une hauteur en pixels, pour la jauge verticale du GUI. */
    public int getScaledReserve(int pixels) {
        return getReserve() * pixels / getCapacity();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved()
            && player.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        int playerStart = MACHINE_SLOTS;
        int playerEnd = MACHINE_SLOTS + PLAYER_SLOTS;

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MACHINE_SLOTS, false)) {
            // moveItemStackTo respecte Slot.mayPlace -> isItemValid : seul un Stable
            // Crystal peut ainsi atterrir dans le slot de carburant.
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }
}
