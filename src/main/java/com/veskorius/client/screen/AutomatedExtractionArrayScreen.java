package com.veskorius.client.screen;

import com.veskorius.menu.AutomatedExtractionArrayMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AutomatedExtractionArrayScreen extends AbstractMachineScreen<AutomatedExtractionArrayMenu> {

    public AutomatedExtractionArrayScreen(AutomatedExtractionArrayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("automated_extraction_array"));
    }
}
