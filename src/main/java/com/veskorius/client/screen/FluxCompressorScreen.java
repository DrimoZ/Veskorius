package com.veskorius.client.screen;

import com.veskorius.menu.FluxCompressorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluxCompressorScreen extends AbstractMachineScreen<FluxCompressorMenu> {

    public FluxCompressorScreen(FluxCompressorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("flux_compressor"));
    }
}
