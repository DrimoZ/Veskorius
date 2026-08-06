package com.veskorius.client.screen;

import com.veskorius.menu.ResonanceStabilizerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonanceStabilizerScreen extends AbstractMachineScreen<ResonanceStabilizerMenu> {

    public ResonanceStabilizerScreen(ResonanceStabilizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("resonance_stabilizer"));
    }
}
