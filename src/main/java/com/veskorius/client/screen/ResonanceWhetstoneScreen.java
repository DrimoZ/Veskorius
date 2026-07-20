package com.veskorius.client.screen;

import com.veskorius.menu.ResonanceWhetstoneMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonanceWhetstoneScreen extends AbstractMachineScreen<ResonanceWhetstoneMenu> {

    public ResonanceWhetstoneScreen(ResonanceWhetstoneMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, STANDARD_TEXTURE);
    }
}
