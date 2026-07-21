package com.veskorius.client.screen;

import com.veskorius.Veskorius;
import com.veskorius.menu.AbstractMachineMenu;
import net.minecraft.client.Minecraft;
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

    // Colonne de boutons de controle, a gauche des slots machine.
    private static final int BUTTONS_X = 7;
    private static final int BUTTONS_Y = 17;
    private static final int BUTTONS_GAP = 2;

    // Couleurs d'etat des boutons.
    private static final int COLOR_ON = 0xFF3B8C3B;      // vert : actif
    private static final int COLOR_OFF = 0xFF8C3B3B;     // rouge : coupe
    private static final int COLOR_RS_IGNORED = 0xFF555555;   // gris : redstone ignoree
    private static final int COLOR_RS_SIGNAL = 0xFFC03030;    // rouge vif : requiert un signal
    private static final int COLOR_RS_NO_SIGNAL = 0xFF803030; // rouge sombre : requiert l'absence
    private static final int COLOR_OVERHEAT_ON = 0xFFD87A20;  // orange : surchauffe active
    private static final int COLOR_OVERHEAT_OFF = 0xFF555555;

    @Override
    protected void init() {
        super.init();
        // Titre aligne sur le bord gauche du panneau, comme les GUI vanilla.
        this.titleLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;

        addControlButtons();
    }

    private void addControlButtons() {
        int step = MachineControlButton.SIZE + BUTTONS_GAP;
        int x = leftPos + BUTTONS_X;
        int y = topPos + BUTTONS_Y;

        // Interrupteur manuel : "I" (marche, vert) / "O" (arret, rouge).
        addRenderableWidget(new MachineControlButton(x, y,
            () -> menu.isManualEnabled() ? "I" : "O",
            () -> menu.isManualEnabled() ? COLOR_ON : COLOR_OFF,
            () -> Component.translatable(menu.isManualEnabled()
                ? "gui.veskorius.machine_on" : "gui.veskorius.machine_off"),
            () -> true,
            () -> sendButton(AbstractMachineMenu.BUTTON_MANUAL)));

        // Mode redstone : "R", couleur selon le mode, tooltip explicite.
        addRenderableWidget(new MachineControlButton(x, y + step,
            () -> "R",
            () -> switch (menu.getRedstoneMode()) {
                case IGNORED -> COLOR_RS_IGNORED;
                case REQUIRES_SIGNAL -> COLOR_RS_SIGNAL;
                case REQUIRES_NO_SIGNAL -> COLOR_RS_NO_SIGNAL;
            },
            () -> Component.translatable("gui.veskorius.redstone_control")
                .append(": ").append(menu.getRedstoneMode().label()),
            () -> true,
            () -> sendButton(AbstractMachineMenu.BUTTON_REDSTONE)));

        // Surchauffe : "H" (heat), visible seulement si la machine la supporte.
        addRenderableWidget(new MachineControlButton(x, y + 2 * step,
            () -> "H",
            () -> menu.isOverheatEnabled() ? COLOR_OVERHEAT_ON : COLOR_OVERHEAT_OFF,
            () -> Component.translatable(menu.isOverheatEnabled()
                ? "gui.veskorius.overheat_on" : "gui.veskorius.overheat_off"),
            menu::supportsOverheat,
            () -> sendButton(AbstractMachineMenu.BUTTON_OVERHEAT)));
    }

    private void sendButton(int buttonId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            // Canal vanilla : declenche menu.clickMenuButton cote serveur.
            mc.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
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
