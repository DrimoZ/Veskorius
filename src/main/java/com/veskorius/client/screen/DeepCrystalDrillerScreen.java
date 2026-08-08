package com.veskorius.client.screen;

import com.veskorius.menu.DeepCrystalDrillerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DeepCrystalDrillerScreen extends AbstractMachineScreen<DeepCrystalDrillerMenu> {

    public DeepCrystalDrillerScreen(DeepCrystalDrillerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("deep_crystal_driller"));
    }
}
