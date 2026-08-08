package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ReclaimerBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class ReclaimerMenu extends AbstractMachineMenu {

    public ReclaimerMenu(int containerId, Inventory playerInventory, AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.RECLAIMER.get(), containerId, playerInventory, blockEntity);
    }

    public ReclaimerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, ReclaimerBlockEntity.SLOT_INPUT, 56, 35);
        addSlot(inventory, ReclaimerBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
