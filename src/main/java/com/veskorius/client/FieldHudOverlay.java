package com.veskorius.client;

import com.veskorius.energy.HarmonicBand;
import com.veskorius.network.FieldHudPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;

/**
 * HUD de champ (12-UX-and-Advancements.md) : overlay discret en coin d'écran affichant le
 * champ <b>où se tient le joueur</b> — pastille de bande, réserve, jauge de dissonance.
 *
 * <p>Le mod rend l'énergie invisible (pas de câbles, pilier 3) ; ce panneau est ce qui
 * évite que « invisible » veuille dire « indevinable ». Il n'apparaît que pour qui porte
 * le Resonance Locator : c'est un instrument qu'on emporte, pas un affichage permanent.
 *
 * <p>Rendu volontairement minimal (texte + rectangles pleins) : la passe visuelle de la
 * Phase 6 lui donnera ses textures, comme au reste des GUI.
 */
public final class FieldHudOverlay implements LayeredDraw.Layer {

    private static final int MARGIN = 6;
    private static final int LINE = 10;
    private static final int PILL = 7;
    private static final int BAR_WIDTH = 60;
    private static final int BAR_HEIGHT = 3;

    private static final int TEXT = 0xFFC8C8C8;
    private static final int BACKDROP = 0x66000000;
    private static final int BAR_BACKDROP = 0xFF202020;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        // Rien pendant un écran de debug ou une caméra libre : le HUD est un confort,
        // il ne doit jamais gêner.
        if (minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }
        FieldHudPayload field = ClientFieldData.current();
        if (field == null) {
            return;
        }

        Font font = minecraft.font;
        int x = MARGIN;
        int y = MARGIN;
        int height = LINE * (field.band() == FieldHudPayload.NO_BAND ? 1 : 2)
            + (field.dissonanceCapacity() > 0 ? LINE : 0);
        graphics.fill(x - 3, y - 3, x + BAR_WIDTH + 34, y + height + 1, BACKDROP);

        if (field.band() != FieldHudPayload.NO_BAND) {
            HarmonicBand band = HarmonicBand.byIndex(field.band());
            graphics.fill(x, y + 1, x + PILL, y + 1 + PILL, band.color());
            graphics.drawString(font, Component.translatable(band.labelKey()),
                x + PILL + 4, y, band.color(), false);
            y += LINE;
        }

        graphics.drawString(font, Component.translatable("gui.veskorius.hud.osc",
            field.reserve(), field.capacity()), x, y, TEXT, false);
        y += LINE;

        if (field.dissonanceCapacity() > 0) {
            renderDissonance(graphics, font, x, y, field);
        }
    }

    /**
     * Jauge de dissonance : elle n'existe visuellement qu'à partir du moment où il y a
     * quelque chose à voir, et vire au rouge avec la mention « instable » quand le champ
     * commence à sauter des ticks — le symptôme et sa cause au même endroit.
     */
    private static void renderDissonance(GuiGraphics graphics, Font font, int x, int y,
                                         FieldHudPayload field) {
        float ratio = Math.min(1.0f, (float) field.dissonance() / field.dissonanceCapacity());
        int filled = Math.round(BAR_WIDTH * ratio);
        int colour = field.unstable() ? 0xFFD04040 : 0xFF8A6FBF;

        int barY = y + 3;
        graphics.fill(x, barY, x + BAR_WIDTH, barY + BAR_HEIGHT, BAR_BACKDROP);
        if (filled > 0) {
            graphics.fill(x, barY, x + filled, barY + BAR_HEIGHT, colour);
        }
        Component label = field.unstable()
            ? Component.translatable("gui.veskorius.hud.unstable").withStyle(ChatFormatting.RED)
            : Component.translatable("gui.veskorius.hud.dissonance");
        graphics.drawString(font, label, x + BAR_WIDTH + 4, y, TEXT, false);
    }
}
