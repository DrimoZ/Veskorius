package com.veskorius.client.screen;

import com.veskorius.menu.StructuralSynthesizerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StructuralSynthesizerScreen extends AbstractMachineScreen<StructuralSynthesizerMenu> {

    public StructuralSynthesizerScreen(StructuralSynthesizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, texture("structural_synthesizer"));
    }
}
