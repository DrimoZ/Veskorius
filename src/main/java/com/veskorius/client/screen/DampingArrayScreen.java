package com.veskorius.client.screen;

import com.veskorius.menu.DampingArrayMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DampingArrayScreen extends AbstractMachineScreen<DampingArrayMenu> {

    public DampingArrayScreen(DampingArrayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("damping_array"));
    }
}
