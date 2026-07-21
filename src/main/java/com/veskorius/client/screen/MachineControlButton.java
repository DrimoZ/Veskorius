package com.veskorius.client.screen;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Petit bouton carré (18×18) dessiné à la main, pour les contrôles de machine
 * (interrupteur manuel, mode redstone, surchauffe).
 *
 * Dessiné plutôt que via {@code Button} vanilla : on veut une couleur d'état
 * lisible d'un coup d'œil (le sens de la demande « avoir du visuel »), ce que le
 * bouton vanilla ne permet pas simplement. La couleur de fond et le tooltip sont
 * relus à chaque frame via des suppliers, donc ils suivent l'état synchronisé
 * sans qu'on ait à les rafraîchir manuellement.
 */
public class MachineControlButton extends AbstractWidget {

    public static final int SIZE = 16;

    private static final int BORDER = 0xFF000000;
    private static final int BORDER_HOVER = 0xFFFFFFFF;
    private static final int TEXT = 0xFFFFFFFF;

    private final Supplier<String> icon;
    private final IntSupplier backgroundColor;
    private final Supplier<Component> tooltip;
    private final Runnable onPress;
    private final BooleanSupplier visibility;

    public MachineControlButton(int x, int y, Supplier<String> icon, IntSupplier backgroundColor,
                                Supplier<Component> tooltip, BooleanSupplier visible, Runnable onPress) {
        super(x, y, SIZE, SIZE, Component.empty());
        this.icon = icon;
        this.backgroundColor = backgroundColor;
        this.tooltip = tooltip;
        this.visibility = visible;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) {
            return;
        }
        int x = getX();
        int y = getY();

        graphics.fill(x, y, x + SIZE, y + SIZE, backgroundColor.getAsInt());
        graphics.renderOutline(x, y, SIZE, SIZE, isHovered() ? BORDER_HOVER : BORDER);

        String label = icon.get();
        int textX = x + (SIZE - font().width(label)) / 2;
        int textY = y + (SIZE - 8) / 2;
        graphics.drawString(font(), label, textX, textY, TEXT, true);

        // Tooltip suivi en direct : evite un rafraichissement manuel par tick.
        setTooltip(Tooltip.create(tooltip.get()));
    }

    private static net.minecraft.client.gui.Font font() {
        return net.minecraft.client.Minecraft.getInstance().font;
    }

    public boolean isVisible() {
        return visibility.getAsBoolean();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isVisible()) {
            onPress.run();
        }
    }

    @Override
    public boolean isActive() {
        return isVisible();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isVisible() && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
