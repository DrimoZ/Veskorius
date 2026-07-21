package com.veskorius.client.screen;

import com.veskorius.Veskorius;
import com.veskorius.menu.FieldEmitterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Écran du Field Emitter : le slot de carburant et la jauge de réserve « X/4000
 * Osc » (12-UX-and-Advancements.md, affichage de la consommation Osc).
 *
 * Ne partage pas {@link AbstractMachineScreen} : pas de barre de progression ni
 * de boutons de contrôle (le Field Emitter est passif, sans cycle).
 */
public class FieldEmitterScreen extends AbstractContainerScreen<FieldEmitterMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/gui/container/field_emitter.png");

    // Jauge : piste dans le fond (152,18) taille 10x52 ; le sprite plein est en (176,0).
    private static final int GAUGE_X = 152;
    private static final int GAUGE_Y = 18;
    private static final int GAUGE_WIDTH = 10;
    private static final int GAUGE_HEIGHT = 52;
    private static final int GAUGE_TEXTURE_U = 176;
    private static final int GAUGE_TEXTURE_V = 0;

    public FieldEmitterScreen(FieldEmitterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderReserveGauge(graphics);
    }

    /** Jauge remplie du bas vers le haut, proportionnelle à la réserve. */
    private void renderReserveGauge(GuiGraphics graphics) {
        int filled = menu.getScaledReserve(GAUGE_HEIGHT);
        if (filled <= 0) {
            return;
        }
        int top = GAUGE_HEIGHT - filled;
        graphics.blit(TEXTURE,
            leftPos + GAUGE_X, topPos + GAUGE_Y + top,
            GAUGE_TEXTURE_U, GAUGE_TEXTURE_V + top,
            GAUGE_WIDTH, filled);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        // « X/4000 Osc » sous le titre. Coordonnées relatives au panneau (renderLabels
        // translate déjà l'origine en leftPos/topPos).
        Component reserve = Component.translatable("gui.veskorius.osc_reserve",
            menu.getReserve(), menu.getCapacity());
        graphics.drawString(font, reserve, 8, 20, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
