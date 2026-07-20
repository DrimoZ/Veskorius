package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ResonanceWhetstoneBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class ResonanceWhetstoneMenu extends AbstractMachineMenu {

    public ResonanceWhetstoneMenu(int containerId, Inventory playerInventory,
                                  AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.RESONANCE_WHETSTONE.get(), containerId, playerInventory, blockEntity);
    }

    public ResonanceWhetstoneMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, ResonanceWhetstoneBlockEntity.SLOT_TOOL, 56, 17);
        addSlot(inventory, ResonanceWhetstoneBlockEntity.SLOT_CRYSTAL, 56, 53);
        addSlot(inventory, ResonanceWhetstoneBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
