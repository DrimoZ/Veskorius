package com.veskorius.client.screen;

import com.veskorius.Veskorius;
import com.veskorius.block.entity.SideMode;
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
    private static final int COLOR_CONFIG_ON = 0xFF3B6C8C;    // bleu : panneau config ouvert
    private static final int COLOR_CONFIG_OFF = 0xFF555555;

    // Modes de face (item I/O).
    private static final int COLOR_SIDE_DISABLED = 0xFF555555;
    private static final int COLOR_SIDE_INPUT = 0xFF3B8C3B;   // vert : entrée
    private static final int COLOR_SIDE_OUTPUT = 0xFF2A6CA0;  // bleu : sortie

    private static final String[] FACE_LETTERS = {"D", "U", "N", "S", "W", "E"};
    private static final String[] FACE_KEYS = {"down", "up", "north", "south", "west", "east"};

    /** Panneau de config item I/O ouvert (client uniquement, ne touche pas le serveur). */
    private boolean showConfig = false;

    @Override
    protected void init() {
        super.init();
        // Titre aligne sur le bord gauche du panneau, comme les GUI vanilla.
        this.titleLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;

        addControlButtons();
        addConfigButtons();
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

        // Config item I/O : "C", ouvre/ferme le panneau de configuration des faces.
        addRenderableWidget(new MachineControlButton(x, y + 3 * step,
            () -> "C",
            () -> showConfig ? COLOR_CONFIG_ON : COLOR_CONFIG_OFF,
            () -> Component.translatable("gui.veskorius.config"),
            () -> true,
            () -> showConfig = !showConfig));
    }

    /**
     * Panneau de config item I/O (12-UX-and-Advancements.md) : 6 boutons de face (mode
     * Désactivé/Entrée/Sortie, couleur + tooltip) + 2 bascules auto. Visibles seulement
     * quand le panneau est ouvert. Layout placeholder : la passe GUI (Phase 6) posera
     * un vrai patron de faces. L'énergie ne passe jamais par ici — objets uniquement.
     */
    private void addConfigButtons() {
        int step = MachineControlButton.SIZE + BUTTONS_GAP;
        int gx = leftPos + 116;
        int gy = topPos + 16;

        for (int i = 0; i < 6; i++) {
            int face = i;
            int bx = gx + (i % 3) * step;
            int by = gy + (i / 3) * step;
            addRenderableWidget(new MachineControlButton(bx, by,
                () -> FACE_LETTERS[face],
                () -> sideColor(menu.getSideMode(face)),
                () -> Component.translatable("gui.veskorius.side." + FACE_KEYS[face])
                    .append(" — ").append(Component.translatable(
                        "gui.veskorius.sidemode." + menu.getSideMode(face).name().toLowerCase())),
                () -> showConfig,
                () -> sendButton(AbstractMachineMenu.BUTTON_CYCLE_SIDE_BASE + face)));
        }

        int autoY = gy + 2 * step;
        addRenderableWidget(new MachineControlButton(gx, autoY,
            () -> "↓",
            () -> menu.isAutoInput() ? COLOR_ON : COLOR_OFF,
            () -> Component.translatable("gui.veskorius.auto_input",
                onOff(menu.isAutoInput())),
            () -> showConfig,
            () -> sendButton(AbstractMachineMenu.BUTTON_AUTO_INPUT)));
        addRenderableWidget(new MachineControlButton(gx + step, autoY,
            () -> "↑",
            () -> menu.isAutoOutput() ? COLOR_ON : COLOR_OFF,
            () -> Component.translatable("gui.veskorius.auto_output",
                onOff(menu.isAutoOutput())),
            () -> showConfig,
            () -> sendButton(AbstractMachineMenu.BUTTON_AUTO_OUTPUT)));
    }

    private static int sideColor(SideMode mode) {
        return switch (mode) {
            case DISABLED -> COLOR_SIDE_DISABLED;
            case INPUT -> COLOR_SIDE_INPUT;
            case OUTPUT -> COLOR_SIDE_OUTPUT;
        };
    }

    private static Component onOff(boolean on) {
        return Component.translatable(on ? "gui.veskorius.on" : "gui.veskorius.off");
    }

    private void sendButton(int buttonId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            // Canal vanilla : declenche menu.clickMenuButton cote serveur.
            mc.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    /** Alvéoles de slot, rangées dans l'atlas à droite du panneau. */
    private static final int SLOT_U = 200;
    private static final int SLOT_AUGMENT_U = 220;
    private static final int SLOT_V = 0;
    private static final int SLOT_SIZE = 18;

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderSlotWells(graphics);
        renderProgressArrow(graphics);
    }

    /**
     * Dessine une alvéole <b>par slot réellement présent</b>, au lieu de les figer dans
     * la texture de fond.
     *
     * <p>C'est ce qui permet un seul fichier de fond pour toutes les machines : elles
     * n'ont pas la même disposition (une ou deux entrées selon la machine, et de 1 à 4
     * slots d'augment selon la configuration). Un fond figé aurait imposé soit une
     * texture par machine, soit un compromis faux pour toutes — et il aurait de toute
     * façon menti dès qu'un modpack change {@code machines.augment.augmentSlots}.
     */
    private void renderSlotWells(GuiGraphics graphics) {
        for (int i = 0; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            int u = menu.isAugmentSlot(i) ? SLOT_AUGMENT_U : SLOT_U;
            graphics.blit(texture,
                leftPos + slot.x - 1, topPos + slot.y - 1,
                u, SLOT_V, SLOT_SIZE, SLOT_SIZE);
        }
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
