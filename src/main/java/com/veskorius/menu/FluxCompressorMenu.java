package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.FluxCompressorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class FluxCompressorMenu extends AbstractMachineMenu {

    public FluxCompressorMenu(int containerId, Inventory playerInventory, AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.FLUX_COMPRESSOR.get(), containerId, playerInventory, blockEntity);
    }

    public FluxCompressorMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, FluxCompressorBlockEntity.SLOT_INPUT, 56, 35);
        addSlot(inventory, FluxCompressorBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
