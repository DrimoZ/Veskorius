package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.DeepSynthesisChamberBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class DeepSynthesisChamberMenu extends AbstractMachineMenu {

    public DeepSynthesisChamberMenu(int containerId, Inventory playerInventory, AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.DEEP_SYNTHESIS_CHAMBER.get(), containerId, playerInventory, blockEntity);
    }

    public DeepSynthesisChamberMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, DeepSynthesisChamberBlockEntity.SLOT_INPUT, 56, 35);
        addSlot(inventory, DeepSynthesisChamberBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
