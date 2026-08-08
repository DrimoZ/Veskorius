package com.veskorius.client.screen;

import com.veskorius.menu.DeepSynthesisChamberMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DeepSynthesisChamberScreen extends AbstractMachineScreen<DeepSynthesisChamberMenu> {

    public DeepSynthesisChamberScreen(DeepSynthesisChamberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("deep_synthesis_chamber"));
    }
}
