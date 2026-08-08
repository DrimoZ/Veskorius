package com.veskorius.client.screen;

/**
 * La mise en page du Codex, <b>calculée à part et sans rien dessiner</b>.
 *
 * <p>Elle vit dans sa propre classe pour une raison précise : les chevauchements de texte
 * sont revenus <b>deux fois</b>, et à chaque fois parce que des coordonnées étaient
 * calculées à la main, ligne par ligne, au moment de dessiner. Rien ne pouvait signaler
 * que deux éléments réclamaient le même pixel — il fallait ouvrir le jeu, regarder, et
 * espérer tomber sur le cas.
 *
 * <p>Ici les zones sont des <b>données</b>, produites par une fonction pure de la taille de
 * la fenêtre. Un test peut donc les faire calculer pour cinquante tailles d'écran et
 * vérifier qu'aucune n'en croise une autre — voir
 * {@code CodexGameTests#codexLayoutNeverOverlaps}. Le défaut cesse d'être une question de
 * vigilance.
 *
 * <p>Aucune classe cliente n'est touchée ici : c'est de l'arithmétique, donc chargeable et
 * testable côté serveur comme côté client.
 */
public record CodexLayout(Rect frame, Rect header, Rect toolbar, Rect body,
                          Rect pageLeft, Rect pageRight, Rect footer, int tabHeight) {

    /** Un rectangle de mise en page. */
    public record Rect(int x, int y, int w, int h) {
        public int right() {
            return x + w;
        }

        public int bottom() {
            return y + h;
        }

        public boolean has(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        /** Vrai si les deux rectangles partagent au moins un pixel. */
        public boolean overlaps(Rect other) {
            return x < other.right() && other.x() < right()
                && y < other.bottom() && other.y() < bottom();
        }
    }

    public static final int TAB_W = 26;
    public static final int GUTTER = 12;
    public static final int PAD = 8;
    public static final int HEADER_H = 24;
    public static final int TOOLBAR_H = 14;
    public static final int FOOTER_H = 16;

    /** Taille minimale sous laquelle le texte cesse d'être lisible. */
    public static final int MIN_W = 300;
    public static final int MIN_H = 180;

    /** Marge garantie entre le cadre et le bord de l'écran. */
    public static final int MARGIN = 8;

    /**
     * Calcule toutes les zones pour une fenêtre donnée.
     *
     * <p><b>L'ordre des bornes compte.</b> On prend une large part de la fenêtre, puis on
     * relève au minimum lisible, puis on rabat sur la fenêtre moins la marge. Enchaînées
     * dans l'autre sens, le plancher l'emporterait sur la marge et le cadre sortirait de
     * l'écran sur les petites fenêtres — précisément ce qu'on veut éviter.
     */
    public static CodexLayout of(int width, int height, int tabCount) {
        int w = Math.min(Math.max(MIN_W, width * 3 / 4), Math.max(60, width - MARGIN * 2));
        int h = Math.min(Math.max(MIN_H, height * 4 / 5), Math.max(60, height - MARGIN * 2));
        Rect frame = new Rect((width - w) / 2, (height - h) / 2, w, h);

        // Les onglets se resserrent quand la hauteur manque : neuf onglets de 24 px
        // réclament 222 px, ce qu'une fenêtre courte n'a pas.
        int tabHeight = Math.clamp((h - 12) / Math.max(1, tabCount), 12, 24);

        Rect header = new Rect(frame.x() + TAB_W, frame.y(), frame.w() - TAB_W, HEADER_H);
        Rect toolbar = new Rect(header.x() + PAD, header.bottom() + 3,
            header.w() - PAD * 2, TOOLBAR_H);
        Rect footer = new Rect(toolbar.x(), frame.bottom() - FOOTER_H, toolbar.w(), FOOTER_H);
        // Le corps prend ce qui reste entre la barre et le pied, en gardant une ligne de
        // séparation. Il peut devenir très court sur une fenêtre minuscule, jamais négatif.
        int bodyTop = toolbar.bottom() + 16;
        Rect body = new Rect(toolbar.x(), bodyTop, toolbar.w(),
            Math.max(12, footer.y() - bodyTop - 2));

        int half = (body.w() - GUTTER) / 2;
        Rect pageLeft = new Rect(body.x(), body.y(), half, body.h());
        Rect pageRight = new Rect(body.x() + half + GUTTER, body.y(), half, body.h());

        return new CodexLayout(frame, header, toolbar, body, pageLeft, pageRight,
            footer, tabHeight);
    }

    /** Les zones qui ne doivent JAMAIS se croiser. Lue par le test. */
    public Rect[] disjointZones() {
        return new Rect[] {header, toolbar, body, footer};
    }

    /** Vrai si tout le cadre tient dans une fenêtre de cette taille. */
    public boolean fitsIn(int width, int height) {
        return frame.x() >= 0 && frame.y() >= 0
            && frame.right() <= width && frame.bottom() <= height;
    }
}
