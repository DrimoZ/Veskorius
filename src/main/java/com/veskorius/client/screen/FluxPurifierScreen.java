package com.veskorius.client.screen;

import com.veskorius.menu.FluxPurifierMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluxPurifierScreen extends AbstractMachineScreen<FluxPurifierMenu> {

    public FluxPurifierScreen(FluxPurifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, STANDARD_TEXTURE);
    }
}
