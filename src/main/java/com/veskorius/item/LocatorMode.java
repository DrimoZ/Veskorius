package com.veskorius.item;

/**
 * Mode courant du Resonance Locator (16-Revision-and-Expansion.md §1) : l'outil est
 * un outil à modes. Le mode est stocké sur l'item (Data Component
 * {@link ModDataComponents#LOCATOR_MODE}) et change au shift+clic droit.
 * <ul>
 *   <li>{@link #RESOURCES} — poches de cristal + signatures de champ (courte portée).</li>
 *   <li>{@link #STRUCTURES} — grandes structures, via l'API de structure vanilla
 *       (aucun scan de blocs) ; s'active quand de vraies structures existent.</li>
 * </ul>
 * L'ordinal est persisté : ne pas réordonner sans migration.
 */
public enum LocatorMode {
    RESOURCES,
    STRUCTURES;

    private static final LocatorMode[] VALUES = values();

    public static LocatorMode byIndex(int index) {
        return VALUES[Math.floorMod(index, VALUES.length)];
    }

    public LocatorMode next() {
        return byIndex(ordinal() + 1);
    }

    /** Clé de langue du nom du mode : {@code gui.veskorius.locator.mode_<name>}. */
    public String labelKey() {
        return "gui.veskorius.locator.mode_" + name().toLowerCase();
    }
}
