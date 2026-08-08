package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.StructuralSynthesizerBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class StructuralSynthesizerMenu extends AbstractMachineMenu {

    public StructuralSynthesizerMenu(int containerId, Inventory playerInventory, AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.STRUCTURAL_SYNTHESIZER.get(), containerId, playerInventory, blockEntity);
    }

    public StructuralSynthesizerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, StructuralSynthesizerBlockEntity.SLOT_ALLOY, 44, 17);
        addSlot(inventory, StructuralSynthesizerBlockEntity.SLOT_STONE, 44, 53);
        addSlot(inventory, StructuralSynthesizerBlockEntity.SLOT_OUTPUT, 116, 26);
        addSlot(inventory, StructuralSynthesizerBlockEntity.SLOT_RESIDUE, 116, 50);
        addAugmentSlot(inventory, 152, 17);
    }
}
