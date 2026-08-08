package com.veskorius.client.screen;

import com.veskorius.menu.AdvancedAssemblerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedAssemblerScreen extends AbstractMachineScreen<AdvancedAssemblerMenu> {

    public AdvancedAssemblerScreen(AdvancedAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("advanced_assembler"));
    }
}
