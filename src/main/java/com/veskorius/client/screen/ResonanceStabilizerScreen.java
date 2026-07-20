package com.veskorius.client.screen;

import com.veskorius.Veskorius;
import com.veskorius.menu.ResonanceStabilizerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ResonanceStabilizerScreen extends AbstractMachineScreen<ResonanceStabilizerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/gui/container/resonance_stabilizer.png");

    public ResonanceStabilizerScreen(ResonanceStabilizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
    }
}
