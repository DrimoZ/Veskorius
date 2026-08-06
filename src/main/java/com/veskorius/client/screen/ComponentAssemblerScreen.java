package com.veskorius.client.screen;

import com.veskorius.menu.ComponentAssemblerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ComponentAssemblerScreen extends AbstractMachineScreen<ComponentAssemblerMenu> {

    public ComponentAssemblerScreen(ComponentAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("component_assembler"));
    }
}
