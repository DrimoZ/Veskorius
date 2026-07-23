package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.DampingArrayBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

/** Menu du Damping Array : agent de damping en entrée, déchet cristallisé en sortie. */
public class DampingArrayMenu extends AbstractMachineMenu {

    public DampingArrayMenu(int containerId, Inventory playerInventory,
                            AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.DAMPING_ARRAY.get(), containerId, playerInventory, blockEntity);
    }

    /** Constructeur côté client : relit la block entity depuis la BlockPos du paquet. */
    public DampingArrayMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, DampingArrayBlockEntity.SLOT_AGENT, 56, 35);
        addSlot(inventory, DampingArrayBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
