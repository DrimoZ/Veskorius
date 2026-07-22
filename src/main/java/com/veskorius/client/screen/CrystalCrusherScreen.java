package com.veskorius.client.screen;

import com.veskorius.menu.CrystalCrusherMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalCrusherScreen extends AbstractMachineScreen<CrystalCrusherMenu> {

    public CrystalCrusherScreen(CrystalCrusherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, STANDARD_TEXTURE);
    }
}
