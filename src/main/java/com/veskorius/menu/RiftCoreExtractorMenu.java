package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.RiftCoreExtractorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class RiftCoreExtractorMenu extends AbstractMachineMenu {

    public RiftCoreExtractorMenu(int containerId, Inventory playerInventory,
                                 AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.RIFT_CORE_EXTRACTOR.get(), containerId, playerInventory, blockEntity);
    }

    public RiftCoreExtractorMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    /** Aucune entrée : la matière vient de la Faille. Deux sorties, l'essence et la prime. */
    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, RiftCoreExtractorBlockEntity.SLOT_OUTPUT, 116, 26);
        addSlot(inventory, RiftCoreExtractorBlockEntity.SLOT_BONUS, 116, 50);
        addAugmentSlot(inventory, 152, 17);
    }
}
