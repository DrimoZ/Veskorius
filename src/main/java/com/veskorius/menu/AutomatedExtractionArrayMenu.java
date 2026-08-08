package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.AutomatedExtractionArrayBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class AutomatedExtractionArrayMenu extends AbstractMachineMenu {

    public AutomatedExtractionArrayMenu(int containerId, Inventory playerInventory,
                                        AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.AUTOMATED_EXTRACTION_ARRAY.get(), containerId, playerInventory, blockEntity);
    }

    public AutomatedExtractionArrayMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    /** Un coffre : neuf slots de collecte sur une rangée, plus l'augment. */
    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        for (int i = 0; i < AutomatedExtractionArrayBlockEntity.SLOT_AUGMENT; i++) {
            addSlot(inventory, i, 26 + i * 18, 35);
        }
        addAugmentSlot(inventory, 152, 17);
    }
}
