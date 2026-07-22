package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.CrystalRoostBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class CrystalRoostMenu extends AbstractMachineMenu {

    public CrystalRoostMenu(int containerId, Inventory playerInventory,
                            AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.CRYSTAL_ROOST.get(), containerId, playerInventory, blockEntity);
    }

    public CrystalRoostMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, CrystalRoostBlockEntity.SLOT_QUARTZ, 56, 35);
        addSlot(inventory, CrystalRoostBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
