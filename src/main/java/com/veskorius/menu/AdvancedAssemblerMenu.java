package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.AdvancedAssemblerBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class AdvancedAssemblerMenu extends AbstractMachineMenu {

    public AdvancedAssemblerMenu(int containerId, Inventory playerInventory, AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.ADVANCED_ASSEMBLER.get(), containerId, playerInventory, blockEntity);
    }

    public AdvancedAssemblerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        // Deux entrées empilées, comme à la Forge : la disposition dit d'elle-même que
        // les deux matières ne sont pas interchangeables.
        addSlot(inventory, AdvancedAssemblerBlockEntity.SLOT_COMPONENT, 44, 17);
        addSlot(inventory, AdvancedAssemblerBlockEntity.SLOT_METAL, 44, 53);
        addSlot(inventory, AdvancedAssemblerBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
