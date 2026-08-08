package com.veskorius.client.screen;

import com.veskorius.menu.RiftCoreExtractorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RiftCoreExtractorScreen extends AbstractMachineScreen<RiftCoreExtractorMenu> {

    public RiftCoreExtractorScreen(RiftCoreExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("rift_core_extractor"));
    }
}
