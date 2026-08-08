package com.veskorius.client.screen;

import com.veskorius.menu.ReclaimerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReclaimerScreen extends AbstractMachineScreen<ReclaimerMenu> {

    public ReclaimerScreen(ReclaimerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("reclaimer"));
    }
}
