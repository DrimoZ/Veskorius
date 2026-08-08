package com.veskorius.client.screen;

import com.veskorius.client.ClientCodexData;
import com.veskorius.codex.CodexCategory;
import com.veskorius.codex.CodexEntry;
import com.veskorius.codex.CodexRegistry;
import com.veskorius.codex.CodexUnlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

/**
 * Écran du Codex de Résonance (15-Codex-Guidebook.md).
 *
 * <p><b>Réécrit.</b> La version précédente tenait dans 256×182 — la taille d'un coffre —
 * n'affichait <b>aucune recette</b>, et surtout <b>tronquait silencieusement</b> tout texte
 * dépassant la page : la boucle de rendu s'arrêtait au bas du panneau et la fin de l'entrée
 * n'existait nulle part. Un manuel qui coupe ses propres phrases au milieu n'est pas un
 * manuel un peu juste, c'est un manuel faux.
 *
 * <p>Quatre choses ont changé, dans l'ordre de ce qu'elles réparent :
 *
 * <ol>
 *   <li><b>Pagination réelle.</b> Le texte est découpé en pages entières et on tourne les
 *       pages. Rien n'est jamais perdu, quelle que soit la longueur.</li>
 *   <li><b>Les recettes s'affichent.</b> Un guide de mod technique qui n'en montre aucune
 *       oblige à ouvrir JEI à côté — donc on n'ouvre pas le guide. Voir
 *       {@link CodexRecipeLookup}.</li>
 *   <li><b>Recherche.</b> Avec plus de soixante entrées, parcourir huit catégories à la
 *       souris est plus long que d'aller demander sur un wiki.</li>
 *   <li><b>Clavier.</b> Flèches pour naviguer, Échap pour remonter d'un niveau plutôt que
 *       de tout fermer. On lit un manuel les deux mains sur le clavier.</li>
 * </ol>
 *
 * <p>Tout est dessiné au {@link GuiGraphics} : aucun asset de texture, donc rien à
 * regénérer quand la mise en page change.
 */
public class CodexScreen extends Screen {

    // --- Mise en page ---------------------------------------------------------
    // Le panneau s'adapte à la fenêtre : il visait une taille fixe de 256×182, ce qui
    // donnait sept lignes lisibles sur un écran qui en offrait trente.

    private static final int MAX_W = 400;
    private static final int MAX_H = 240;
    private static final int NAV_W = 104;
    private static final int PAD = 8;
    private static final int HEADER_H = 26;
    private static final int NAV_ROW_H = 18;
    private static final int ENTRY_ROW_H = 18;

    private static final int COLOR_SHADE = 0xC0000000;
    private static final int COLOR_PANEL = 0xFF12101B;
    private static final int COLOR_PANEL_ALT = 0xFF17141F;
    private static final int COLOR_BORDER = 0xFF4A3A6A;
    private static final int COLOR_BORDER_HI = 0xFF7C5FA8;
    private static final int COLOR_HOVER = 0x40B58AD6;
    private static final int COLOR_SELECTED = 0x66B58AD6;
    private static final int COLOR_TITLE = 0xFFE8CE8A;
    private static final int COLOR_TEXT = 0xFFDAD6E6;
    private static final int COLOR_DIM = 0xFF9A90B4;
    private static final int COLOR_LOCKED = 0xFF6A6478;
    private static final int COLOR_ACCENT = 0xFFB57CE0;
    private static final int COLOR_SLOT = 0xFF241E33;

    private final ItemStack codex;

    private int left;
    private int top;
    private int panelW;
    private int panelH;
    private int paneX;
    private int paneW;
    private int paneTop;
    private int paneBottom;

    private CodexCategory selectedCategory = CodexCategory.INTRO;
    @Nullable
    private CodexEntry selectedEntry;
    private int listScroll;
    private int page;

    /** Lignes de l'entrée ouverte, déjà découpées à la largeur de la page. */
    private List<FormattedCharSequence> bodyLines = List.of();
    private int linesPerPage = 1;
    @Nullable
    private CodexRecipeLookup.View recipe;

    @Nullable
    private EditBox search;

    public CodexScreen(ItemStack codex) {
        super(Component.translatable("gui.veskorius.codex.title"));
        this.codex = codex;
    }

    @Override
    protected void init() {
        panelW = Math.min(MAX_W, width - 20);
        panelH = Math.min(MAX_H, height - 20);
        left = (width - panelW) / 2;
        top = (height - panelH) / 2;
        paneX = left + NAV_W + PAD;
        paneW = panelW - NAV_W - PAD * 2;
        paneTop = top + HEADER_H + 20;
        paneBottom = top + panelH - PAD;

        search = new EditBox(font, left + PAD, top + HEADER_H, NAV_W - PAD, 14,
            Component.translatable("gui.veskorius.codex.search"));
        search.setHint(Component.translatable("gui.veskorius.codex.search"));
        search.setResponder(s -> {
            listScroll = 0;
            selectedEntry = null;
        });
        addRenderableWidget(search);
        reflow();
    }

    /**
     * Recalcule le découpage en pages. Appelé à l'ouverture d'une entrée <b>et</b> à
     * chaque redimensionnement : une page calculée pour une fenêtre puis affichée dans une
     * autre est exactement ce qui produisait des textes coupés.
     */
    private void reflow() {
        if (selectedEntry == null) {
            bodyLines = List.of();
            recipe = null;
            return;
        }
        boolean unlocked = ClientCodexData.isUnlocked(selectedEntry);
        Component body = unlocked
            ? Component.translatable(selectedEntry.textKey())
            : lockedHint(selectedEntry);
        bodyLines = new ArrayList<>(font.split(body, paneW));
        recipe = unlocked ? CodexRecipeLookup.find(selectedEntry.icon()) : null;

        int textBottom = paneBottom - 12 - (recipe != null ? recipeHeight() : 0);
        linesPerPage = Math.max(1, (textBottom - (paneTop + 22)) / (font.lineHeight + 2));
        page = Math.clamp(page, 0, pageCount() - 1);
    }

    private int pageCount() {
        return Math.max(1, (bodyLines.size() + linesPerPage - 1) / linesPerPage);
    }

    private int recipeHeight() {
        return 58;
    }

    /** Les entrées affichées : celles de la catégorie, ou le résultat de la recherche. */
    private List<CodexEntry> visibleEntries() {
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return CodexRegistry.byCategory(selectedCategory);
        }
        // La recherche traverse TOUTES les catégories : on cherche un mot, pas un rayon.
        // Les entrées verrouillées en sont exclues — les faire apparaître révélerait par
        // leur titre ce que le Codex garde justement pour plus tard.
        List<CodexEntry> hits = new ArrayList<>();
        for (CodexEntry entry : CodexRegistry.all()) {
            if (!ClientCodexData.isUnlocked(entry)) {
                continue;
            }
            String title = Component.translatable(entry.titleKey()).getString().toLowerCase(Locale.ROOT);
            String text = Component.translatable(entry.textKey()).getString().toLowerCase(Locale.ROOT);
            if (title.contains(query) || text.contains(query)) {
                hits.add(entry);
            }
        }
        return hits;
    }

    private int visibleRows() {
        return Math.max(1, (paneBottom - paneTop) / ENTRY_ROW_H);
    }

    private int maxScroll() {
        return Math.max(0, visibleEntries().size() - visibleRows());
    }

    // --- Rendu ----------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, COLOR_SHADE);
        panel(graphics, left, top, panelW, panelH);

        // Bandeau de titre, séparé du corps par un filet d'accent.
        graphics.fill(left + 1, top + 1, left + panelW - 1, top + HEADER_H - 4, COLOR_PANEL_ALT);
        graphics.drawString(font, title, left + PAD, top + 9, COLOR_TITLE, false);
        Component progress = Component.translatable("gui.veskorius.codex.total",
            ClientCodexData.totalUnlocked(), CodexRegistry.all().size());
        graphics.drawString(font, progress,
            left + panelW - PAD - font.width(progress), top + 9, COLOR_DIM, false);
        graphics.hLine(left + PAD, left + panelW - PAD - 1, top + HEADER_H - 4, COLOR_BORDER_HI);
        // Séparateur vertical entre la navigation et la page.
        graphics.vLine(left + NAV_W, top + HEADER_H, top + panelH - PAD, COLOR_BORDER);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderCategories(graphics, mouseX, mouseY);
        if (selectedEntry == null) {
            renderEntryList(graphics, mouseX, mouseY);
        } else {
            renderEntryPage(graphics, mouseX, mouseY);
        }
    }

    /** Cadre à deux tons : un simple contour d'un pixel se lisait comme une boîte de debug. */
    private static void panel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, COLOR_PANEL);
        graphics.renderOutline(x, y, w, h, COLOR_BORDER);
        graphics.renderOutline(x + 1, y + 1, w - 2, h - 2, 0xFF221C33);
    }

    private void renderCategories(GuiGraphics graphics, int mouseX, int mouseY) {
        CodexCategory[] categories = CodexCategory.values();
        for (int i = 0; i < categories.length; i++) {
            CodexCategory cat = categories[i];
            int y = navRowY(i);
            boolean hovered = inBox(mouseX, mouseY, left + PAD - 2, y - 2, NAV_W - PAD, NAV_ROW_H);
            if (cat == selectedCategory && search != null && search.getValue().isEmpty()) {
                graphics.fill(left + PAD - 2, y - 2, left + NAV_W - 4, y + NAV_ROW_H - 4, COLOR_SELECTED);
            } else if (hovered) {
                graphics.fill(left + PAD - 2, y - 2, left + NAV_W - 4, y + NAV_ROW_H - 4, COLOR_HOVER);
            }
            graphics.renderFakeItem(cat.icon(), left + PAD, y - 1);
            graphics.drawString(font, Component.translatable(cat.titleKey()),
                left + PAD + 20, y + 3, COLOR_TEXT, false);

            // Compteur par catégorie : on voit d'un coup où il reste à découvrir.
            int total = CodexRegistry.byCategory(cat).size();
            int got = ClientCodexData.unlockedCount(cat);
            String count = got + "/" + total;
            graphics.drawString(font, count, left + NAV_W - 8 - font.width(count), y + 3,
                got == total ? COLOR_ACCENT : COLOR_LOCKED, false);
        }
    }

    private int navRowY(int index) {
        return top + HEADER_H + 22 + index * NAV_ROW_H;
    }

    private void renderEntryList(GuiGraphics graphics, int mouseX, int mouseY) {
        List<CodexEntry> entries = visibleEntries();
        boolean searching = search != null && !search.getValue().trim().isEmpty();

        Component heading = searching
            ? Component.translatable("gui.veskorius.codex.results", entries.size())
            : Component.translatable(selectedCategory.titleKey());
        graphics.drawString(font, heading, paneX, top + HEADER_H + 2, COLOR_TITLE, false);
        graphics.hLine(paneX, paneX + paneW - 1, top + HEADER_H + 14, COLOR_BORDER);

        if (entries.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.veskorius.codex.no_results"),
                paneX, paneTop + 4, COLOR_LOCKED, false);
            return;
        }

        int rows = visibleRows();
        int end = Math.min(entries.size(), listScroll + rows);
        for (int i = listScroll; i < end; i++) {
            CodexEntry entry = entries.get(i);
            int y = paneTop + (i - listScroll) * ENTRY_ROW_H;
            boolean unlocked = ClientCodexData.isUnlocked(entry);
            if (inBox(mouseX, mouseY, paneX, y - 2, paneW, ENTRY_ROW_H)) {
                graphics.fill(paneX - 2, y - 2, paneX + paneW, y + ENTRY_ROW_H - 4, COLOR_HOVER);
            }
            if (unlocked) {
                graphics.renderFakeItem(entry.icon(), paneX, y - 1);
                graphics.drawString(font, Component.translatable(entry.titleKey()),
                    paneX + 20, y + 3, COLOR_TEXT, false);
            } else {
                // Un cadenas plutôt qu'un « ??? » : la ligne dit qu'il y a quelque chose là
                // ET qu'elle n'est pas encore à lire, sans imiter un titre.
                graphics.drawString(font, "■", paneX + 5, y + 3, COLOR_LOCKED, false);
                graphics.drawString(font, Component.translatable("gui.veskorius.codex.sealed"),
                    paneX + 20, y + 3, COLOR_LOCKED, false);
            }
        }
        renderScrollbar(graphics, entries.size(), rows, listScroll);
    }

    /** Une vraie barre plutôt que deux glyphes ▲▼ : on voit où on en est dans la liste. */
    private void renderScrollbar(GuiGraphics graphics, int total, int rows, int scroll) {
        if (total <= rows) {
            return;
        }
        int x = paneX + paneW - 3;
        int trackTop = paneTop - 2;
        int trackH = rows * ENTRY_ROW_H;
        graphics.fill(x, trackTop, x + 3, trackTop + trackH, 0xFF1C1728);
        int thumbH = Math.max(12, trackH * rows / total);
        int thumbY = trackTop + (trackH - thumbH) * scroll / Math.max(1, total - rows);
        graphics.fill(x, thumbY, x + 3, thumbY + thumbH, COLOR_BORDER_HI);
    }

    private void renderEntryPage(GuiGraphics graphics, int mouseX, int mouseY) {
        CodexEntry entry = selectedEntry;
        boolean unlocked = ClientCodexData.isUnlocked(entry);

        Component back = Component.translatable("gui.veskorius.codex.back");
        boolean backHover = inBox(mouseX, mouseY, paneX, top + HEADER_H, font.width(back), 10);
        graphics.drawString(font, back, paneX, top + HEADER_H + 2,
            backHover ? COLOR_ACCENT : COLOR_DIM, false);

        graphics.renderFakeItem(entry.icon(), paneX, paneTop - 20);
        graphics.drawString(font,
            unlocked ? Component.translatable(entry.titleKey())
                : Component.translatable("gui.veskorius.codex.sealed"),
            paneX + 20, paneTop - 16, unlocked ? COLOR_TITLE : COLOR_LOCKED, false);
        graphics.hLine(paneX, paneX + paneW - 1, paneTop - 4, COLOR_BORDER);

        int y = paneTop + 4;
        int first = page * linesPerPage;
        int last = Math.min(bodyLines.size(), first + linesPerPage);
        for (int i = first; i < last; i++) {
            graphics.drawString(font, bodyLines.get(i), paneX, y,
                unlocked ? COLOR_TEXT : COLOR_LOCKED, false);
            y += font.lineHeight + 2;
        }

        if (recipe != null) {
            renderRecipe(graphics, recipe, paneX, paneBottom - recipeHeight(), mouseX, mouseY);
        }

        if (pageCount() > 1) {
            String label = (page + 1) + " / " + pageCount();
            int py = paneBottom - 10;
            graphics.drawString(font, label, paneX + (paneW - font.width(label)) / 2, py,
                COLOR_DIM, false);
            if (page > 0) {
                graphics.drawString(font, "◀", paneX, py, COLOR_ACCENT, false);
            }
            if (page < pageCount() - 1) {
                graphics.drawString(font, "▶", paneX + paneW - 6, py, COLOR_ACCENT, false);
            }
        }
    }

    /**
     * Dessine une recette : les entrées à gauche, une flèche, le résultat à droite. Les
     * ingrédients à choix multiple (les tags) <b>défilent</b> comme dans JEI — figer le
     * premier ferait croire que seul le fer convient là où le mod accepte aussi le
     * fragment de Custode.
     */
    private void renderRecipe(GuiGraphics graphics, CodexRecipeLookup.View view,
                              int x, int y, int mouseX, int mouseY) {
        graphics.hLine(x, x + paneW - 1, y - 6, COLOR_BORDER);
        graphics.drawString(font, Component.translatable(view.shaped()
            ? "gui.veskorius.codex.recipe_shaped" : "gui.veskorius.codex.recipe"),
            x, y - 2, COLOR_DIM, false);

        int gridTop = y + 10;
        int cols = Math.max(1, view.gridWidth());
        for (int i = 0; i < view.inputs().size(); i++) {
            int sx = x + (i % cols) * 18;
            int sy = gridTop + (i / cols) * 18;
            slot(graphics, sx, sy);
            ItemStack[] options = view.inputs().get(i).getItems();
            if (options.length == 0) {
                continue;
            }
            ItemStack shown = options[(int) (System.currentTimeMillis() / 1500 % options.length)];
            graphics.renderFakeItem(shown, sx + 1, sy + 1);
            if (inBox(mouseX, mouseY, sx, sy, 18, 18)) {
                graphics.renderTooltip(font, shown, mouseX, mouseY);
            }
        }

        int arrowX = x + cols * 18 + 6;
        graphics.drawString(font, "→", arrowX, gridTop + 5, COLOR_ACCENT, false);
        int resultX = arrowX + 14;
        slot(graphics, resultX, gridTop);
        graphics.renderFakeItem(view.result(), resultX + 1, gridTop + 1);
        graphics.renderItemDecorations(font, view.result(), resultX + 1, gridTop + 1);
        if (inBox(mouseX, mouseY, resultX, gridTop, 18, 18)) {
            graphics.renderTooltip(font, view.result(), mouseX, mouseY);
        }

        if (view.note() != null) {
            graphics.drawString(font, view.note(), resultX, gridTop + 22, COLOR_DIM, false);
        }
    }

    private static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, COLOR_SLOT);
        graphics.renderOutline(x, y, 18, 18, COLOR_BORDER);
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

    // --- Interaction ----------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }

        CodexCategory[] categories = CodexCategory.values();
        for (int i = 0; i < categories.length; i++) {
            if (inBox(mouseX, mouseY, left + PAD - 2, navRowY(i) - 2, NAV_W - PAD, NAV_ROW_H)) {
                selectedCategory = categories[i];
                selectedEntry = null;
                listScroll = 0;
                if (search != null) {
                    search.setValue("");
                }
                reflow();
                return true;
            }
        }

        if (selectedEntry == null) {
            List<CodexEntry> entries = visibleEntries();
            int rows = visibleRows();
            int end = Math.min(entries.size(), listScroll + rows);
            for (int i = listScroll; i < end; i++) {
                int y = paneTop + (i - listScroll) * ENTRY_ROW_H;
                if (inBox(mouseX, mouseY, paneX, y - 2, paneW, ENTRY_ROW_H)) {
                    // Verrouillée comprise : la page explique alors comment la débloquer.
                    open(entries.get(i));
                    return true;
                }
            }
            return false;
        }

        int py = paneBottom - 10;
        if (page > 0 && inBox(mouseX, mouseY, paneX - 2, py - 2, 14, 14)) {
            page--;
            return true;
        }
        if (page < pageCount() - 1 && inBox(mouseX, mouseY, paneX + paneW - 10, py - 2, 14, 14)) {
            page++;
            return true;
        }
        if (inBox(mouseX, mouseY, paneX, top + HEADER_H, paneW, 12)) {
            selectedEntry = null;
            reflow();
            return true;
        }
        return false;
    }

    private void open(CodexEntry entry) {
        selectedEntry = entry;
        page = 0;
        reflow();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (selectedEntry == null) {
            listScroll = Math.clamp(listScroll - (int) Math.signum(scrollY), 0, maxScroll());
        } else {
            page = Math.clamp(page - (int) Math.signum(scrollY), 0, pageCount() - 1);
        }
        return true;
    }

    /**
     * Clavier. Échap <b>remonte d'un niveau</b> au lieu de tout fermer : dans un manuel, on
     * sort d'un article pour revenir au sommaire, pas pour quitter le livre.
     */
    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256 && selectedEntry != null) {
            selectedEntry = null;
            reflow();
            return true;
        }
        if (search != null && search.isFocused()) {
            return super.keyPressed(key, scanCode, modifiers);
        }
        if (selectedEntry != null) {
            if (key == 263 || key == 266) {
                page = Math.max(0, page - 1);
                return true;
            }
            if (key == 262 || key == 267) {
                page = Math.min(pageCount() - 1, page + 1);
                return true;
            }
        } else {
            if (key == 264) {
                listScroll = Math.min(maxScroll(), listScroll + 1);
                return true;
            }
            if (key == 265) {
                listScroll = Math.max(0, listScroll - 1);
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int newWidth, int newHeight) {
        // On garde l'entrée ouverte et on RECALCULE le découpage : sans ce reflow, les
        // pages resteraient dimensionnées pour l'ancienne fenêtre et le texte se remettrait
        // à disparaître — le bug d'origine, sous une autre forme.
        CodexEntry keep = selectedEntry;
        String query = search == null ? "" : search.getValue();
        super.resize(minecraft, newWidth, newHeight);
        selectedEntry = keep;
        if (search != null) {
            search.setValue(query);
        }
        reflow();
    }

    private static boolean inBox(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
