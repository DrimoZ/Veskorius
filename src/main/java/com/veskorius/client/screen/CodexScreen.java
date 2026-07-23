package com.veskorius.client.screen;

import com.veskorius.client.ClientCodexData;
import com.veskorius.codex.CodexCategory;
import com.veskorius.codex.CodexEntry;
import com.veskorius.codex.CodexRegistry;
import com.veskorius.codex.CodexUnlock;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Écran du Codex de Résonance (15-Codex-Guidebook.md). Manuel en jeu : catégories à
 * gauche, entrées au centre, page de lecture à droite. Se dessine intégralement au
 * {@link GuiGraphics} (aucun asset de texture requis).
 *
 * Convivialité : les entrées verrouillées restent visibles (« ??? ») et sont
 * cliquables — elles ouvrent une page qui explique <b>comment</b> les débloquer
 * (obtenir tel objet, progresser, lire un fragment), pour guider sans tout dévoiler.
 * L'état de déblocage est lu du cache client ({@link ClientCodexData}), alimenté par le
 * serveur : la connaissance est celle du joueur, pas de l'objet.
 */
public class CodexScreen extends Screen {

    private static final int PANEL_W = 256;
    private static final int PANEL_H = 182;
    private static final int NAV_ROW_H = 18;
    private static final int ENTRY_ROW_H = 16;
    private static final int ENTRY_LIST_TOP_OFFSET = 50;
    private static final int VISIBLE_ROWS = 7;

    private static final int COLOR_PANEL = 0xF00A0012;
    private static final int COLOR_BORDER = 0xFF3A2A55;
    private static final int COLOR_SELECTED = 0x40B58AD6;
    private static final int COLOR_TITLE = 0xFFFFD54A;
    private static final int COLOR_TEXT = 0xFFE0E0E0;
    private static final int COLOR_LOCKED = 0xFF6A6A6A;
    private static final int COLOR_COUNT = 0xFFA090C0;

    private final ItemStack codex;

    private int left;
    private int top;
    private int navX;
    private int paneX;
    private int paneW;

    private CodexCategory selectedCategory = CodexCategory.INTRO;
    @Nullable
    private CodexEntry selectedEntry;
    private int scrollOffset;

    public CodexScreen(ItemStack codex) {
        super(Component.translatable("gui.veskorius.codex.title"));
        this.codex = codex;
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        navX = left + 8;
        paneX = left + 112;
        paneW = PANEL_W - 112 - 8;
    }

    private int navRowY(int index) {
        return top + 26 + index * NAV_ROW_H;
    }

    private int entryRowY(int visibleIndex) {
        return top + ENTRY_LIST_TOP_OFFSET + visibleIndex * ENTRY_ROW_H;
    }

    private int maxScroll() {
        return Math.max(0, CodexRegistry.byCategory(selectedCategory).size() - VISIBLE_ROWS);
    }

    // --- Rendu ---------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, COLOR_PANEL);
        graphics.renderOutline(left, top, PANEL_W, PANEL_H, COLOR_BORDER);
        graphics.drawString(font, title, left + 8, top + 8, COLOR_TITLE, false);
        graphics.drawString(font, Component.translatable("gui.veskorius.codex.total",
                ClientCodexData.totalUnlocked(), CodexRegistry.all().size()),
            left + PANEL_W - 8 - font.width(Component.translatable("gui.veskorius.codex.total",
                ClientCodexData.totalUnlocked(), CodexRegistry.all().size())),
            top + 8, COLOR_COUNT, false);
        graphics.fill(paneX - 6, top + 22, paneX - 5, top + PANEL_H - 8, COLOR_BORDER);

        renderCategories(graphics);
        if (selectedEntry == null) {
            renderEntryList(graphics);
        } else {
            renderEntryPage(graphics);
        }
    }

    private void renderCategories(GuiGraphics graphics) {
        CodexCategory[] categories = CodexCategory.values();
        for (int i = 0; i < categories.length; i++) {
            CodexCategory cat = categories[i];
            int y = navRowY(i);
            if (cat == selectedCategory) {
                graphics.fill(navX - 2, y - 1, navX + 96, y + NAV_ROW_H - 3, COLOR_SELECTED);
            }
            graphics.renderFakeItem(cat.icon(), navX, y);
            graphics.drawString(font, Component.translatable(cat.titleKey()),
                navX + 20, y + 4, COLOR_TEXT, false);
        }
    }

    private void renderEntryList(GuiGraphics graphics) {
        List<CodexEntry> entries = CodexRegistry.byCategory(selectedCategory);
        int unlocked = ClientCodexData.unlockedCount(selectedCategory);
        graphics.drawString(font, Component.translatable(selectedCategory.titleKey()),
            paneX, top + 26, COLOR_TITLE, false);
        graphics.drawString(font,
            Component.translatable("gui.veskorius.codex.discovered", unlocked, entries.size()),
            paneX, top + 38, COLOR_COUNT, false);

        int end = Math.min(entries.size(), scrollOffset + VISIBLE_ROWS);
        for (int i = scrollOffset; i < end; i++) {
            CodexEntry entry = entries.get(i);
            int y = entryRowY(i - scrollOffset);
            if (ClientCodexData.isUnlocked(entry)) {
                graphics.renderFakeItem(entry.icon(), paneX, y - 4);
                graphics.drawString(font, Component.translatable(entry.titleKey()),
                    paneX + 20, y, COLOR_TEXT, false);
            } else {
                graphics.drawString(font, Component.literal("? ? ?"),
                    paneX + 20, y, COLOR_LOCKED, false);
            }
        }
        // Flèches de défilement si la liste dépasse.
        if (scrollOffset > 0) {
            graphics.drawString(font, Component.literal("▲"), paneX + paneW - 8, top + 38, COLOR_COUNT, false);
        }
        if (scrollOffset < maxScroll()) {
            graphics.drawString(font, Component.literal("▼"), paneX + paneW - 8,
                top + PANEL_H - 16, COLOR_COUNT, false);
        }
    }

    private void renderEntryPage(GuiGraphics graphics) {
        CodexEntry entry = selectedEntry;
        graphics.drawString(font, Component.translatable("gui.veskorius.codex.back"),
            paneX, top + 26, COLOR_COUNT, false);

        boolean unlocked = ClientCodexData.isUnlocked(entry);
        graphics.renderFakeItem(entry.icon(), paneX, top + 40);
        graphics.drawString(font,
            unlocked ? Component.translatable(entry.titleKey()) : Component.literal("? ? ?"),
            paneX + 20, top + 44, unlocked ? COLOR_TITLE : COLOR_LOCKED, false);

        Component body = unlocked
            ? Component.translatable(entry.textKey())
            : lockedHint(entry);
        List<FormattedCharSequence> lines = font.split(body, paneW);
        int y = top + 62;
        for (FormattedCharSequence line : lines) {
            if (y > top + PANEL_H - 12) {
                break;
            }
            graphics.drawString(font, line, paneX, y, unlocked ? COLOR_TEXT : COLOR_LOCKED, false);
            y += font.lineHeight + 1;
        }
    }

    /** Texte expliquant comment débloquer une entrée encore verrouillée. */
    private static Component lockedHint(CodexEntry entry) {
        CodexUnlock unlock = entry.unlock();
        return switch (unlock.type()) {
            case ITEM -> unlock.item() == null
                ? Component.translatable("gui.veskorius.codex.locked")
                : Component.translatable("gui.veskorius.codex.locked_item",
                    new ItemStack(unlock.item()).getHoverName());
            case ADVANCEMENT -> Component.translatable("gui.veskorius.codex.locked_advancement");
            case FRAGMENT -> Component.translatable("gui.veskorius.codex.locked_fragment");
            case ALWAYS -> Component.translatable("gui.veskorius.codex.locked");
        };
    }

    // --- Interaction ---------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            CodexCategory[] categories = CodexCategory.values();
            for (int i = 0; i < categories.length; i++) {
                int y = navRowY(i);
                if (inBox(mouseX, mouseY, navX - 2, y - 1, 98, NAV_ROW_H - 2)) {
                    selectedCategory = categories[i];
                    selectedEntry = null;
                    scrollOffset = 0;
                    return true;
                }
            }
            if (selectedEntry == null) {
                List<CodexEntry> entries = CodexRegistry.byCategory(selectedCategory);
                int end = Math.min(entries.size(), scrollOffset + VISIBLE_ROWS);
                for (int i = scrollOffset; i < end; i++) {
                    int y = entryRowY(i - scrollOffset);
                    if (inBox(mouseX, mouseY, paneX, y - 4, paneW, ENTRY_ROW_H)) {
                        // Verrouillée comprise : ouvre une page qui explique le déblocage.
                        selectedEntry = entries.get(i);
                        return true;
                    }
                }
            } else if (inBox(mouseX, mouseY, paneX, top + 26, paneW, 10)) {
                selectedEntry = null;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selectedEntry == null && scrollY != 0) {
            int next = scrollOffset - (int) Math.signum(scrollY);
            scrollOffset = Math.max(0, Math.min(maxScroll(), next));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static boolean inBox(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
