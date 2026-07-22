package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.CrystalCrusherBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class CrystalCrusherMenu extends AbstractMachineMenu {

    public CrystalCrusherMenu(int containerId, Inventory playerInventory,
                              AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.CRYSTAL_CRUSHER.get(), containerId, playerInventory, blockEntity);
    }

    /** Constructeur côté client : relit la block entity depuis la BlockPos du paquet. */
    public CrystalCrusherMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        // Une seule entrée : centrée verticalement, en face de la sortie.
        addSlot(inventory, CrystalCrusherBlockEntity.SLOT_INPUT, 56, 35);
        addSlot(inventory, CrystalCrusherBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
