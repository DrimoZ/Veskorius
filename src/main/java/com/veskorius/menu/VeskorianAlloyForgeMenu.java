package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.VeskorianAlloyForgeBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class VeskorianAlloyForgeMenu extends AbstractMachineMenu {

    public VeskorianAlloyForgeMenu(int containerId, Inventory playerInventory,
                                   AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.VESKORIAN_ALLOY_FORGE.get(), containerId, playerInventory, blockEntity);
    }

    public VeskorianAlloyForgeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    /** Deux sorties : le lingot, et la scorie qu'on ne peut pas ignorer. */
    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, VeskorianAlloyForgeBlockEntity.SLOT_CRYSTAL, 44, 17);
        addSlot(inventory, VeskorianAlloyForgeBlockEntity.SLOT_METAL, 44, 53);
        addSlot(inventory, VeskorianAlloyForgeBlockEntity.SLOT_OUTPUT, 116, 26);
        addSlot(inventory, VeskorianAlloyForgeBlockEntity.SLOT_SLAG, 116, 50);
        addAugmentSlot(inventory, 152, 17);
    }
}
