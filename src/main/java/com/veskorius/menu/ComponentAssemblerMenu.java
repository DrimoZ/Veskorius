package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ComponentAssemblerBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class ComponentAssemblerMenu extends AbstractMachineMenu {

    public ComponentAssemblerMenu(int containerId, Inventory playerInventory,
                                  AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.COMPONENT_ASSEMBLER.get(), containerId, playerInventory, blockEntity);
    }

    public ComponentAssemblerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, ComponentAssemblerBlockEntity.SLOT_CRYSTAL, 56, 17);
        addSlot(inventory, ComponentAssemblerBlockEntity.SLOT_IRON, 56, 53);
        addSlot(inventory, ComponentAssemblerBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
