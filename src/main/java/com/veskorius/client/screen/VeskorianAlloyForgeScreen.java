package com.veskorius.client.screen;

import com.veskorius.menu.VeskorianAlloyForgeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VeskorianAlloyForgeScreen extends AbstractMachineScreen<VeskorianAlloyForgeMenu> {

    public VeskorianAlloyForgeScreen(VeskorianAlloyForgeMenu menu, Inventory playerInventory,
                                     Component title) {
        super(menu, playerInventory, title, texture("veskorian_alloy_forge"));
    }
}
