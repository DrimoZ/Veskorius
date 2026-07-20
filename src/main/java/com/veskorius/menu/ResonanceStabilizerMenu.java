package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.ResonanceStabilizerBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;

public class ResonanceStabilizerMenu extends AbstractMachineMenu {

    public ResonanceStabilizerMenu(int containerId, Inventory playerInventory,
                                   AbstractMachineBlockEntity blockEntity) {
        super(ModMenuTypes.RESONANCE_STABILIZER.get(), containerId, playerInventory, blockEntity);
    }

    /**
     * Constructeur cote client. La BlockPos a ete ecrite dans le paquet par
     * {@code player.openMenu(provider, pos)} ; on relit la block entity depuis le
     * niveau client pour que le menu pointe sur la meme machine des deux cotes.
     */
    public ResonanceStabilizerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
            (AbstractMachineBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    protected void addMachineSlots(IItemHandler inventory) {
        addSlot(inventory, ResonanceStabilizerBlockEntity.SLOT_CRYSTAL, 56, 17);
        addSlot(inventory, ResonanceStabilizerBlockEntity.SLOT_FLUX, 56, 53);
        addSlot(inventory, ResonanceStabilizerBlockEntity.SLOT_OUTPUT, 116, 35);
        addAugmentSlot(inventory, 152, 17);
    }
}
