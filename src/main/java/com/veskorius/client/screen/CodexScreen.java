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
import org.jetbrains.annotations.Nullable;

/**
 * Écran du Codex de Résonance (15-Codex-Guidebook.md).
 *
 * <p><b>Refait une seconde fois, et pour des raisons différentes de la première.</b> La
 * version précédente ne tronquait plus le texte, mais elle <b>superposait</b> : dans la
 * page d'entrée, le lien de retour, l'icône et le titre étaient dessinés à quatre pixels
 * les uns des autres, et rien n'était découpé au ciseau — une liste qui défilait
 * s'imprimait par-dessus l'en-tête et débordait du panneau.
 *
 * <p>Deux causes, et elles se corrigent ensemble :
 *
 * <ol>
 *   <li><b>Des coordonnées calculées à la main, une par une.</b> Chaque {@code drawString}
 *       portait son propre {@code top + 26}, {@code paneTop - 16}. Rien ne garantissait
 *       qu'ils ne tombent pas au même endroit, et rien ne l'aurait signalé. La mise en
 *       page est désormais calculée <b>une fois</b>, en zones, et le rendu ne fait que
 *       remplir des rectangles.</li>
 *   <li><b>Aucun découpage.</b> Toute zone qui défile est maintenant sous
 *       {@code enableScissor} : ce qui dépasse est coupé net au lieu de se dessiner sur
 *       le voisin. C'est la seule façon fiable de faire défiler quoi que ce soit.</li>
 * </ol>
 *
 * <p>La forme suit celle d'un vrai livre plutôt que d'un GUI de coffre : onglets en
 * tranche à gauche, <b>double page</b> au centre. Le texte coule de la page de gauche vers
 * celle de droite, donc une entrée entière tient le plus souvent sans tourner.
 *
 * <p>Et l'ARBRE : les paliers en colonnes, les entrées sous leur palier, des flèches entre
 * les colonnes. Une liste de soixante entrées ne dit rien de l'ordre dans lequel on les
 * rencontre ; une carte le dit d'un coup d'œil, et montre les verrouillées à leur place.
 */
public class CodexScreen extends Screen {

    // --- Zones ----------------------------------------------------------------

    /**
     * Mode de <b>parcours</b> — et il n'inclut délibérément PAS la lecture.
     *
     * <p>Il l'incluait, et c'était un crash : « je lis une entrée » vivait à la fois dans
     * cette énumération et dans le champ {@code entry}, deux valeurs devant rester
     * d'accord sans que rien ne l'impose. Vider la barre de recherche pendant la lecture
     * mettait {@code entry} à null en laissant le mode sur ENTRY, et le rendu suivant
     * déréférençait null.
     *
     * <p>La lecture est donc <b>déduite</b> : on lit si et seulement si {@code entry} n'est
     * pas null. L'état incohérent n'existe plus, au lieu d'être rattrapé par un test de
     * nullité au moment de dessiner.
     */
    private enum View { LIST, TREE }

    private static final int TAB_W = CodexLayout.TAB_W;
    private static final int GUTTER = CodexLayout.GUTTER;
    private static final int PAD = CodexLayout.PAD;
    private static final int ROW_H = 20;
    private static final int NODE = 22;

    private static final int COLOR_SHADE = 0xD0000000;
    private static final int COLOR_PAGE = 0xFF16131F;
    private static final int COLOR_PAGE_ALT = 0xFF1C1828;
    private static final int COLOR_FRAME = 0xFF4A3A6A;
    private static final int COLOR_FRAME_HI = 0xFF7C5FA8;
    private static final int COLOR_HOVER = 0x50B58AD6;
    private static final int COLOR_ON = 0x80B58AD6;
    private static final int COLOR_TITLE = 0xFFE8CE8A;
    private static final int COLOR_TEXT = 0xFFDCD8E8;
    private static final int COLOR_DIM = 0xFF9A90B4;
    private static final int COLOR_LOCKED = 0xFF5E586E;
    private static final int COLOR_ACCENT = 0xFFB57CE0;
    private static final int COLOR_SLOT = 0xFF241E33;
    private static final int COLOR_LINK = 0xFF3A2F52;

    private final ItemStack codex;

    private CodexLayout.Rect frame = new CodexLayout.Rect(0, 0, 0, 0);
    private CodexLayout.Rect header = new CodexLayout.Rect(0, 0, 0, 0);
    private CodexLayout.Rect toolbar = new CodexLayout.Rect(0, 0, 0, 0);
    private CodexLayout.Rect footer = new CodexLayout.Rect(0, 0, 0, 0);
    private int tabH = 24;
    private CodexLayout.Rect pageL = new CodexLayout.Rect(0, 0, 0, 0);
    private CodexLayout.Rect pageR = new CodexLayout.Rect(0, 0, 0, 0);
    private CodexLayout.Rect body = new CodexLayout.Rect(0, 0, 0, 0);

    private View browse = View.LIST;
    private CodexCategory category = CodexCategory.INTRO;
    @Nullable
    private CodexEntry entry;

    private int listScroll;
    private int treeScrollX;
    private int treeScrollY;
    private int page;

    private List<FormattedCharSequence> lines = List.of();
    private int linesPerColumn = 1;
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
        // Toute la géométrie vient de CodexLayout : elle est calculée une fois, à part, et
        // vérifiée par un test qui la fait tourner sur cinquante tailles d'écran.
        CodexLayout l = CodexLayout.of(width, height, CodexCategory.values().length + 1);
        frame = l.frame();
        header = l.header();
        toolbar = l.toolbar();
        body = l.body();
        pageL = l.pageLeft();
        pageR = l.pageRight();
        footer = l.footer();
        tabH = l.tabHeight();

        int searchW = Math.clamp(toolbar.w() / 3, 70, 130);
        search = new EditBox(font, toolbar.x(), toolbar.y(), searchW, 14,
            Component.translatable("gui.veskorius.codex.search"));
        search.setHint(Component.translatable("gui.veskorius.codex.search"));
        search.setResponder(str -> {
            listScroll = 0;
            entry = null;
            if (!str.isEmpty()) {
                browse = View.LIST;
            }
        });
        addWidget(search);
        reflow();
    }

    private void reflow() {
        if (entry == null) {
            lines = List.of();
            recipe = null;
            return;
        }
        boolean unlocked = readable(entry);
        Component text = unlocked
            ? Component.translatable(entry.textKey())
            : lockedHint(entry);
        lines = new ArrayList<>(font.split(text, pageL.w()));
        recipe = unlocked ? CodexRecipeLookup.find(entry.icon()) : null;

        // La recette occupe le bas de la page de DROITE, donc elle ne rogne que celle-ci.
        // La gauche garde toute sa hauteur : c'est ce qui permet à une entrée courte de
        // tenir entièrement à gauche et de laisser la recette respirer à droite.
        linesPerColumn = Math.max(1, body.h() / (font.lineHeight + 2));
        page = Math.clamp(page, 0, pageCount() - 1);
    }

    private int linesRight() {
        int usable = recipe != null ? body.h() - recipeHeight() : body.h();
        return Math.max(1, usable / (font.lineHeight + 2));
    }

    private int linesPerSpread() {
        return linesPerColumn + linesRight();
    }

    private int pageCount() {
        return Math.max(1, (lines.size() + linesPerSpread() - 1) / linesPerSpread());
    }

    private static int recipeHeight() {
        return 62;
    }

    // --- Rendu ----------------------------------------------------------------

    /**
     * <b>On ne délègue pas le fond au socle.</b> {@code Screen.render} commence par
     * repeindre son propre fond assombri : appelé après notre dessin, il délavait le
     * titre, les onglets et le cadre — tout ce qui avait été tracé avant lui. On dessine
     * donc tout nous-mêmes, dans l'ordre, et le champ de recherche en dernier.
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, COLOR_SHADE);

        // Le livre : deux pages sur un dos, et non un panneau unique.
        graphics.fill(frame.x() + TAB_W, frame.y(), frame.right(), frame.bottom(), COLOR_PAGE);
        graphics.renderOutline(frame.x() + TAB_W, frame.y(),
            frame.w() - TAB_W, frame.h(), COLOR_FRAME);
        graphics.fill(header.x() + 1, header.y() + 1, header.right() - 1, header.bottom(),
            COLOR_PAGE_ALT);
        graphics.hLine(header.x(), header.right() - 1, header.bottom(), COLOR_FRAME_HI);

        graphics.drawString(font, title, header.x() + PAD, header.y() + 8, COLOR_TITLE, false);
        Component progress = Component.translatable("gui.veskorius.codex.total",
            ClientCodexData.totalUnlocked(), CodexRegistry.all().size());
        graphics.drawString(font, progress, header.right() - PAD - font.width(progress),
            header.y() + 8, COLOR_DIM, false);

        renderTabs(graphics, mouseX, mouseY);
        // Pas de champ de recherche pendant la lecture : il partageait le coin gauche de
        // la barre avec le lien de retour, donc les deux se dessinaient l'un sur l'autre.
        // Et chercher depuis une page ouverte n'a de toute façon aucun sens — on remonte
        // d'abord.
        if (search != null && entry == null) {
            search.render(graphics, mouseX, mouseY, partialTick);
        }

        // Le dos, entre les deux pages. Dessiné avant le contenu : il appartient au livre.
        if (browse != View.TREE || entry != null) {
            int spine = pageL.right() + GUTTER / 2;
            graphics.vLine(spine, body.y() - 4, body.bottom(), COLOR_FRAME);
        }

        if (entry != null) {
            renderEntry(graphics, mouseX, mouseY);
        } else if (browse == View.TREE) {
            renderTree(graphics, mouseX, mouseY);
        } else {
            renderList(graphics, mouseX, mouseY);
        }
    }

    /** Onglets en tranche, à gauche du livre — comme les signets d'un vrai volume. */
    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        CodexCategory[] cats = CodexCategory.values();
        for (int i = 0; i <= cats.length; i++) {
            CodexLayout.Rect tab = tabRect(i);
            boolean isTree = i == cats.length;
            boolean active = isTree ? browse == View.TREE
                : (browse != View.TREE && category == cats[i]);
            graphics.fill(tab.x(), tab.y(), tab.right(), tab.bottom(),
                active ? COLOR_PAGE : COLOR_PAGE_ALT);
            graphics.renderOutline(tab.x(), tab.y(), tab.w(), tab.h(),
                active ? COLOR_FRAME_HI : COLOR_FRAME);
            if (!active && tab.has(mouseX, mouseY)) {
                graphics.fill(tab.x() + 1, tab.y() + 1, tab.right() - 1, tab.bottom() - 1,
                    COLOR_HOVER);
            }
            if (isTree) {
                graphics.drawString(font, "◆", tab.x() + 9, tab.y() + (tab.h() - 8) / 2,
                    COLOR_ACCENT, false);
            } else {
                graphics.renderFakeItem(cats[i].icon(), tab.x() + 5,
                    tab.y() + (tab.h() - 16) / 2);
            }
            if (tab.has(mouseX, mouseY)) {
                graphics.renderTooltip(font, isTree
                    ? Component.translatable("gui.veskorius.codex.tree")
                    : Component.translatable(cats[i].titleKey()), mouseX, mouseY);
            }
        }
    }

    private CodexLayout.Rect tabRect(int index) {
        return new CodexLayout.Rect(frame.x(), frame.y() + 6 + index * (tabH + 2), TAB_W, tabH);
    }

    /** Le lien de retour, à gauche de la barre. Une seule définition pour le rendu ET le clic. */
    private CodexLayout.Rect backRect() {
        return new CodexLayout.Rect(toolbar.x(), toolbar.y(), font.width(
            Component.translatable("gui.veskorius.codex.back")) + 4, 14);
    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY) {
        List<CodexEntry> entries = visibleEntries();
        boolean searching = search != null && !search.getValue().trim().isEmpty();
        Component heading = searching
            ? Component.translatable("gui.veskorius.codex.results", entries.size())
            : Component.translatable(category.titleKey());
        graphics.drawString(font, heading, toolbar.right() - font.width(heading),
            toolbar.y() + 3, COLOR_TITLE, false);

        if (entries.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.veskorius.codex.no_results"),
                body.x(), body.y() + 4, COLOR_LOCKED, false);
            return;
        }

        // La liste occupe les DEUX pages : deux colonnes, donc deux fois moins de
        // défilement — et sur soixante entrées, ça change tout.
        int rows = Math.max(1, body.h() / ROW_H);
        graphics.enableScissor(body.x(), body.y(), body.right(), body.bottom());
        for (int i = 0; i < rows * 2; i++) {
            int index = listScroll + i;
            if (index >= entries.size()) {
                break;
            }
            CodexLayout.Rect row = listRow(i, rows);
            CodexEntry e = entries.get(index);
            boolean unlocked = readable(e);
            if (row.has(mouseX, mouseY)) {
                graphics.fill(row.x(), row.y(), row.right(), row.bottom(), COLOR_HOVER);
            }
            // Le TITRE s'affiche toujours pour ce qui est lisible ; la couleur dit
            // seulement si on l'a déjà rencontré. Masquer le nom d'une page qu'on a le
            // droit de lire n'aurait servi qu'à empêcher de la trouver.
            boolean found = ClientCodexData.isUnlocked(e);
            if (unlocked) {
                graphics.renderFakeItem(e.icon(), row.x() + 1, row.y() + 1);
                graphics.drawString(font, font.plainSubstrByWidth(
                        Component.translatable(e.titleKey()).getString(), row.w() - 22),
                    row.x() + 21, row.y() + 5, found ? COLOR_TEXT : COLOR_DIM, false);
            } else {
                graphics.drawString(font, "▪", row.x() + 7, row.y() + 5, COLOR_LOCKED, false);
                graphics.drawString(font, Component.translatable("gui.veskorius.codex.sealed"),
                    row.x() + 21, row.y() + 5, COLOR_LOCKED, false);
            }
        }
        graphics.disableScissor();
        renderScrollbar(graphics, entries.size(), rows * 2, listScroll);
    }

    /** Ligne {@code i} de la liste : colonne gauche puis colonne droite. */
    private CodexLayout.Rect listRow(int i, int rows) {
        CodexLayout.Rect column = i < rows ? pageL : pageR;
        return new CodexLayout.Rect(column.x(), body.y() + (i % rows) * ROW_H, column.w(), ROW_H - 2);
    }

    private void renderScrollbar(GuiGraphics graphics, int total, int shown, int scroll) {
        if (total <= shown) {
            return;
        }
        int x = frame.right() - 4;
        graphics.fill(x, body.y(), x + 2, body.bottom(), 0xFF1C1728);
        int thumb = Math.max(14, body.h() * shown / total);
        int y = body.y() + (body.h() - thumb) * scroll / Math.max(1, total - shown);
        graphics.fill(x, y, x + 2, y + thumb, COLOR_FRAME_HI);
    }

    private void renderEntry(GuiGraphics graphics, int mouseX, int mouseY) {
        CodexEntry e = entry;
        boolean unlocked = readable(e);

        // EN-TÊTE DE L'ENTRÉE, sur sa propre bande. C'est ici que trois éléments se
        // marchaient dessus ; ils ont maintenant chacun leur place réservée.
        CodexLayout.Rect back = backRect();
        graphics.drawString(font, Component.translatable("gui.veskorius.codex.back"),
            back.x(), back.y() + 3, back.has(mouseX, mouseY) ? COLOR_ACCENT : COLOR_DIM, false);

        // Le titre de l'entrée est ALIGNÉ À DROITE de la barre, le retour à gauche : c'est
        // ce qui garantit qu'ils ne se croisent jamais, quelle que soit la longueur.
        Component name = unlocked ? Component.translatable(e.titleKey())
            : Component.translatable("gui.veskorius.codex.sealed");
        int avail = toolbar.w() - back.w() - 26;
        String shown = font.plainSubstrByWidth(name.getString(), avail);
        int nameX = toolbar.right() - font.width(shown);
        graphics.renderFakeItem(e.icon(), nameX - 20, toolbar.y() - 1);
        graphics.drawString(font, shown, nameX, toolbar.y() + 3,
            unlocked ? COLOR_TITLE : COLOR_LOCKED, false);
        graphics.hLine(body.x(), body.right() - 1, body.y() - 5, COLOR_FRAME);

        int first = page * linesPerSpread();
        int color = unlocked ? COLOR_TEXT : COLOR_LOCKED;

        graphics.enableScissor(body.x(), body.y(), body.right(), body.bottom());
        int y = body.y();
        for (int i = 0; i < linesPerColumn && first + i < lines.size(); i++) {
            graphics.drawString(font, lines.get(first + i), pageL.x(), y, color, false);
            y += font.lineHeight + 2;
        }
        y = body.y();
        int rightStart = first + linesPerColumn;
        for (int i = 0; i < linesRight() && rightStart + i < lines.size(); i++) {
            graphics.drawString(font, lines.get(rightStart + i), pageR.x(), y, color, false);
            y += font.lineHeight + 2;
        }
        graphics.disableScissor();

        if (recipe != null) {
            renderRecipe(graphics, recipe, pageR.x(), body.bottom() - recipeHeight() + 8,
                mouseX, mouseY);
        }
        renderFooter(graphics, e, mouseX, mouseY);
    }

    /** Pied de page : tourne-page à gauche, « et ensuite ? » à droite. Jamais superposés. */
    private void renderFooter(GuiGraphics graphics, CodexEntry e, int mouseX, int mouseY) {
        int y = footer.y() + 4;
        if (pageCount() > 1) {
            String label = (page + 1) + " / " + pageCount();
            graphics.drawString(font, label, pageL.x(), y, COLOR_DIM, false);
            if (page > 0) {
                graphics.drawString(font, "◀", pageL.x() + 30, y, COLOR_ACCENT, false);
            }
            if (page < pageCount() - 1) {
                graphics.drawString(font, "▶", pageL.x() + 44, y, COLOR_ACCENT, false);
            }
        }
        CodexEntry follow = nextReadable(e);
        if (follow != null && page == pageCount() - 1) {
            Component link = Component.translatable("gui.veskorius.codex.next",
                Component.translatable(follow.titleKey()));
            int x = frame.right() - PAD - font.width(link);
            boolean over = mouseX >= x && mouseX < frame.right() - PAD
                && mouseY >= y - 2 && mouseY < y + 11;
            graphics.drawString(font, link, x, y, over ? COLOR_ACCENT : COLOR_DIM, false);
        }
    }

    /**
     * <b>L'arbre de progression</b> : une colonne par palier, les entrées dessous, des
     * flèches d'un palier au suivant.
     *
     * <p>Les entrées scellées y gardent leur <b>place</b> mais pas leur identité — un cadre
     * vide. On voit combien il reste et où, sans qu'un titre ne déflore ce qui vient.
     */
    private void renderTree(GuiGraphics graphics, int mouseX, int mouseY) {
        Component heading = Component.translatable("gui.veskorius.codex.tree");
        graphics.drawString(font, heading, toolbar.right() - font.width(heading),
            toolbar.y() + 3, COLOR_TITLE, false);

        graphics.enableScissor(body.x(), body.y(), body.right(), body.bottom());
        @Nullable CodexEntry hovered = null;

        int colW = NODE * 3 + 16;
        for (int tier = 0; tier <= 5; tier++) {
            List<CodexEntry> column = tierEntries(tier);
            if (column.isEmpty()) {
                continue;
            }
            int cx = body.x() + 6 + tier * colW - treeScrollX;
            int cy = body.y() + 16 - treeScrollY;

            // La flèche vers le palier suivant, tracée AVANT les nœuds pour passer dessous.
            if (tier < 5 && !tierEntries(tier + 1).isEmpty()) {
                int ay = body.y() + 6 - treeScrollY;
                graphics.hLine(cx + colW - 18, cx + colW - 6, ay, COLOR_LINK);
                graphics.drawString(font, "▸", cx + colW - 12, ay - 4, COLOR_FRAME_HI, false);
            }
            String label = tier == 0
                ? Component.translatable("gui.veskorius.codex.tier_intro").getString()
                : "T" + tier;
            graphics.drawString(font, label, cx, body.y() + 2 - treeScrollY, COLOR_ACCENT, false);

            for (int i = 0; i < column.size(); i++) {
                int nx = cx + (i % 3) * NODE;
                int ny = cy + (i / 3) * NODE;
                CodexEntry e = column.get(i);
                boolean unlocked = readable(e);
                boolean over = mouseX >= nx && mouseX < nx + 18
                    && mouseY >= ny && mouseY < ny + 18
                    && body.has(mouseX, mouseY);
                if (over) {
                    graphics.fill(nx - 1, ny - 1, nx + 19, ny + 19, COLOR_HOVER);
                    hovered = e;
                }
                slot(graphics, nx, ny);
                if (unlocked) {
                    graphics.renderFakeItem(e.icon(), nx + 1, ny + 1);
                } else {
                    graphics.drawString(font, "▪", nx + 7, ny + 5, COLOR_LOCKED, false);
                }
            }
        }
        graphics.disableScissor();

        if (hovered != null) {
            graphics.renderTooltip(font, readable(hovered)
                ? Component.translatable(hovered.titleKey())
                : Component.translatable("gui.veskorius.codex.sealed"), mouseX, mouseY);
        }
    }

    private static List<CodexEntry> tierEntries(int tier) {
        List<CodexEntry> out = new ArrayList<>();
        for (CodexEntry e : CodexRegistry.all()) {
            if (e.tier() == tier) {
                out.add(e);
            }
        }
        return out;
    }

    private int treeWidth() {
        return (NODE * 3 + 16) * 6 + 12;
    }

    private int treeHeight() {
        int max = 0;
        for (int t = 0; t <= 5; t++) {
            max = Math.max(max, ((tierEntries(t).size() + 2) / 3) * NODE);
        }
        return max + 26;
    }

    private void renderRecipe(GuiGraphics graphics, CodexRecipeLookup.View v,
                              int x, int y, int mouseX, int mouseY) {
        graphics.hLine(x, pageR.right() - 1, y - 6, COLOR_FRAME);
        graphics.drawString(font, Component.translatable(v.shaped()
            ? "gui.veskorius.codex.recipe_shaped" : "gui.veskorius.codex.recipe"),
            x, y - 2, COLOR_DIM, false);

        int gridTop = y + 10;
        int cols = Math.max(1, v.gridWidth());
        for (int i = 0; i < v.inputs().size(); i++) {
            int sx = x + (i % cols) * 18;
            int sy = gridTop + (i / cols) * 18;
            slot(graphics, sx, sy);
            ItemStack[] options = v.inputs().get(i).getItems();
            if (options.length == 0) {
                continue;
            }
            ItemStack shown = options[(int) (System.currentTimeMillis() / 1500 % options.length)];
            graphics.renderFakeItem(shown, sx + 1, sy + 1);
            graphics.renderItemDecorations(font, shown, sx + 1, sy + 1);
            if (sx <= mouseX && mouseX < sx + 18 && sy <= mouseY && mouseY < sy + 18) {
                graphics.renderTooltip(font, shown, mouseX, mouseY);
            }
        }
        int arrowX = x + cols * 18 + 4;
        graphics.drawString(font, "→", arrowX, gridTop + 5, COLOR_ACCENT, false);
        int rx = arrowX + 12;
        slot(graphics, rx, gridTop);
        graphics.renderFakeItem(v.result(), rx + 1, gridTop + 1);
        graphics.renderItemDecorations(font, v.result(), rx + 1, gridTop + 1);
        if (rx <= mouseX && mouseX < rx + 18 && gridTop <= mouseY && mouseY < gridTop + 18) {
            graphics.renderTooltip(font, v.result(), mouseX, mouseY);
        }
        if (v.note() != null) {
            graphics.drawString(font, v.note(), x, gridTop + 22, COLOR_DIM, false);
        }
    }

    private static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, COLOR_SLOT);
        graphics.renderOutline(x, y, 18, 18, COLOR_FRAME);
    }

    private List<CodexEntry> visibleEntries() {
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return CodexRegistry.byCategory(category);
        }
        List<CodexEntry> hits = new ArrayList<>();
        for (CodexEntry e : CodexRegistry.all()) {
            if (!readable(e)) {
                continue;
            }
            String title = Component.translatable(e.titleKey()).getString().toLowerCase(Locale.ROOT);
            String text = Component.translatable(e.textKey()).getString().toLowerCase(Locale.ROOT);
            if (title.contains(query) || text.contains(query)) {
                hits.add(e);
            }
        }
        return hits;
    }

    /**
     * Le texte de cette entrée est-il lisible maintenant ?
     *
     * <p>Presque toujours oui : seul le lore attend d'être trouvé. À ne pas confondre avec
     * « découverte », qui reste le compteur de progression — on peut parfaitement lire la
     * page de la Forge avant d'en posséder une, et c'est précisément l'intérêt.
     */
    private static boolean readable(CodexEntry e) {
        return !e.hidesTextUntilFound() || ClientCodexData.isUnlocked(e);
    }

    @Nullable
    private static CodexEntry nextReadable(CodexEntry e) {
        CodexEntry follow = CodexRegistry.next(e);
        return follow != null && readable(follow) ? follow : null;
    }

    private static Component lockedHint(CodexEntry e) {
        CodexUnlock unlock = e.unlock();
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
        if (entry == null && super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }

        CodexCategory[] cats = CodexCategory.values();
        for (int i = 0; i <= cats.length; i++) {
            if (!tabRect(i).has(mouseX, mouseY)) {
                continue;
            }
            entry = null;
            listScroll = 0;
            if (i == cats.length) {
                browse = View.TREE;
            } else {
                browse = View.LIST;
                category = cats[i];
            }
            if (search != null) {
                search.setValue("");
            }
            reflow();
            return true;
        }

        if (entry != null) {
            return clickEntry(mouseX, mouseY);
        }
        return browse == View.TREE ? clickTree(mouseX, mouseY) : clickList(mouseX, mouseY);
    }

    private boolean clickList(double mouseX, double mouseY) {
        List<CodexEntry> entries = visibleEntries();
        int rows = Math.max(1, body.h() / ROW_H);
        for (int i = 0; i < rows * 2; i++) {
            int index = listScroll + i;
            if (index >= entries.size()) {
                break;
            }
            if (listRow(i, rows).has(mouseX, mouseY)) {
                open(entries.get(index));
                return true;
            }
        }
        return false;
    }

    private boolean clickTree(double mouseX, double mouseY) {
        if (!body.has(mouseX, mouseY)) {
            return false;
        }
        int colW = NODE * 3 + 16;
        for (int tier = 0; tier <= 5; tier++) {
            List<CodexEntry> column = tierEntries(tier);
            int cx = body.x() + 6 + tier * colW - treeScrollX;
            int cy = body.y() + 16 - treeScrollY;
            for (int i = 0; i < column.size(); i++) {
                int nx = cx + (i % 3) * NODE;
                int ny = cy + (i / 3) * NODE;
                if (mouseX >= nx && mouseX < nx + 18 && mouseY >= ny && mouseY < ny + 18) {
                    open(column.get(i));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean clickEntry(double mouseX, double mouseY) {
        int y = footer.y() + 4;
        if (pageCount() > 1) {
            if (page > 0 && mouseX >= pageL.x() + 28 && mouseX < pageL.x() + 40
                && mouseY >= y - 2 && mouseY < y + 11) {
                page--;
                return true;
            }
            if (page < pageCount() - 1 && mouseX >= pageL.x() + 42 && mouseX < pageL.x() + 54
                && mouseY >= y - 2 && mouseY < y + 11) {
                page++;
                return true;
            }
        }
        CodexEntry follow = nextReadable(entry);
        if (follow != null && page == pageCount() - 1) {
            Component link = Component.translatable("gui.veskorius.codex.next",
                Component.translatable(follow.titleKey()));
            int lx = frame.right() - PAD - font.width(link);
            if (mouseX >= lx && mouseX < frame.right() - PAD && mouseY >= y - 2 && mouseY < y + 11) {
                open(follow);
                return true;
            }
        }
        if (backRect().has(mouseX, mouseY)) {
            back();
            return true;
        }
        return false;
    }

    private void open(CodexEntry e) {
        cameFromTree = browse == View.TREE;
        entry = e;
        page = 0;
        reflow();
    }

    /** D'où l'entrée ouverte a été atteinte : le retour doit y ramener. */
    private boolean cameFromTree;

    /** Retour : vers l'arbre si on en venait, vers la liste sinon. */
    private void back() {
        entry = null;
        browse = cameFromTree ? View.TREE : View.LIST;
        reflow();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int step = (int) Math.signum(scrollY);
        if (entry != null) {
            page = Math.clamp(page - step, 0, pageCount() - 1);
        } else if (browse == View.TREE) {
            treeScrollY = Math.clamp(treeScrollY - step * NODE, 0,
                Math.max(0, treeHeight() - body.h()));
        } else {
            int rows = Math.max(1, body.h() / ROW_H);
            listScroll = Math.clamp(listScroll - step * rows, 0,
                Math.max(0, visibleEntries().size() - rows * 2));
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        // L'arbre se déplace à la souris : plus large que la page, il serait sinon
        // inatteignable au-delà du troisième palier.
        if (entry == null && browse == View.TREE && button == 0 && body.has(mouseX, mouseY)) {
            treeScrollX = Math.clamp(treeScrollX - (int) dx, 0,
                Math.max(0, treeWidth() - body.w()));
            treeScrollY = Math.clamp(treeScrollY - (int) dy, 0,
                Math.max(0, treeHeight() - body.h()));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256 && (entry != null || browse == View.TREE)) {
            back();
            return true;
        }
        if (search != null && search.isFocused()) {
            return super.keyPressed(key, scanCode, modifiers);
        }
        if (entry != null) {
            if (key == 263 || key == 266) {
                page = Math.max(0, page - 1);
                return true;
            }
            if (key == 262 || key == 267) {
                page = Math.min(pageCount() - 1, page + 1);
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int newWidth, int newHeight) {
        CodexEntry keep = entry;
        View keepBrowse = browse;
        String query = search == null ? "" : search.getValue();
        super.resize(minecraft, newWidth, newHeight);
        entry = keep;
        browse = keepBrowse;
        if (search != null) {
            search.setValue(query);
        }
        reflow();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
