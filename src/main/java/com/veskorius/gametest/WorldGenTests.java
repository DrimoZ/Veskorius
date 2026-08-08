package com.veskorius.gametest;

/**
 * Le namespace des tests <b>lourds</b>, ceux qui posent une structure entière.
 *
 * <p><b>Pourquoi une constante et pas une chaîne écrite trois fois.</b> Le runner headless
 * de GameTest ne sait filtrer que par namespace : ni par test, ni par classe, ni par
 * motif. C'est donc le seul levier disponible pour lancer autre chose que la totalité —
 * et il se règle depuis {@code build.gradle}, via
 * {@code neoforge.enabledGameTestNamespaces}. Une faute de frappe entre le
 * {@code GameTestHolder} et la configuration de run ne se manifesterait pas par une
 * erreur, mais par <b>zéro test exécuté</b> et un build vert : exactement la panne
 * silencieuse que cette suite existe pour empêcher.
 */
public final class WorldGenTests {

    /** Namespace des tests de structure. Doit rester aligné avec {@code build.gradle}. */
    public static final String NAMESPACE = "veskorius_world";

    private WorldGenTests() {
    }
}
