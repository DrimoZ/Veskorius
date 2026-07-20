package com.veskorius.client.screen;

import com.veskorius.Veskorius;
import com.veskorius.menu.AbstractMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Socle commun aux ecrans de machines.
 *
 * Conventions imposees par 12-UX-and-Advancements.md : barre de progression
 * horizontale, gauche -> droite, identique au four vanilla. Les coordonnees
 * ci-dessous reprennent donc exactement celles du four.
 */
public abstract class AbstractMachineScreen<T extends AbstractMachineMenu> extends AbstractContainerScreen<T> {

    /**
     * Fond commun aux machines a disposition standard (2 entrees, 1 sortie,
     * 1 augment). Tant que les textures sont des placeholders, un seul fichier
     * suffit pour toutes — la Phase 6 pourra en donner un par machine en
     * surchargeant le constructeur qui prend une ResourceLocation.
     */
    public static final ResourceLocation STANDARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Veskorius.MOD_ID, "textures/gui/container/machine_standard.png");

    protected static final int PROGRESS_X = 79;
    protected static final int PROGRESS_Y = 34;
    protected static final int PROGRESS_WIDTH = 24;
    protected static final int PROGRESS_HEIGHT = 17;

    /** La fleche "pleine" est stockee a droite du fond, en (176, 0). */
    protected static final int PROGRESS_TEXTURE_U = 176;
    protected static final int PROGRESS_TEXTURE_V = 0;

    private final ResourceLocation texture;

    protected AbstractMachineScreen(T menu, Inventory playerInventory, Component title, ResourceLocation texture) {
        super(menu, playerInventory, title);
        this.texture = texture;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // Titre aligne sur le bord gauche du panneau, comme les GUI vanilla.
        this.titleLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderProgressArrow(graphics);
    }

    private void renderProgressArrow(GuiGraphics graphics) {
        int filled = menu.getScaledProgress(PROGRESS_WIDTH);
        if (filled <= 0) {
            return;
        }
        graphics.blit(texture,
            leftPos + PROGRESS_X, topPos + PROGRESS_Y,
            PROGRESS_TEXTURE_U, PROGRESS_TEXTURE_V,
            filled, PROGRESS_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
