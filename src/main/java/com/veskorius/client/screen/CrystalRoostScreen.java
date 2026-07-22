package com.veskorius.client.screen;

import com.veskorius.menu.CrystalRoostMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalRoostScreen extends AbstractMachineScreen<CrystalRoostMenu> {

    public CrystalRoostScreen(CrystalRoostMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, STANDARD_TEXTURE);
    }
}
