package com.veskorius.energy;

/**
 * Contrat énergétique d'un émetteur de champ de Résonance (06-Energy.md).
 *
 * Exposé comme capability de bloc (voir {@link com.veskorius.energy.ModCapabilities})
 * par le Field Emitter, et plus tard par le Relay, l'Amplifier, le Convergence
 * Core. Une machine consommatrice ne dialogue jamais directement avec un émetteur :
 * elle passe par {@link ResonanceFieldManager}, qui connaît la géométrie (portée,
 * et plus tard la ligne de mire des relais).
 *
 * Cette interface ne décrit donc QUE l'énergie, pas la position ni la portée
 * appliquée — le manager s'en charge, à partir de {@link #getRange()}.
 */
public interface IResonanceField {

    /**
     * Intensité du champ, pour la règle d'anti-stacking (06-Energy.md :
     * « l'intensité retenue est celle de la source la plus forte », jamais une
     * addition). Constante pour tous les Field Emitter T2 ; deviendra variable
     * avec le Harmonic Amplifier (T4). Exposée dès maintenant pour que le manager
     * n'ait pas à être retouché quand un émetteur d'intensité différente arrivera.
     */
    int getFieldStrength();

    /** Portée du champ, en blocs (distance euclidienne). */
    int getRange();

    /** Vrai si l'émetteur a de l'énergie à fournir (réserve &gt; 0). */
    boolean isActive();

    /**
     * Prélève jusqu'à {@code maxOsc} de la réserve, retourne ce qui a réellement
     * été retiré (0 si la réserve est vide). Le prélèvement est immédiat.
     */
    int extractOsc(int maxOsc);

    /**
     * Réserve d'Osc restante. Lecture seule, pour l'affichage (HUD de champ, 12-UX) :
     * la consommation passe toujours par {@link #extractOsc}.
     */
    default int getReserve() {
        return 0;
    }

    /** Capacité de réserve, pour dimensionner une jauge. 0 = source sans réserve bornée. */
    default int getCapacity() {
        return 0;
    }

    // --- Harmoniques & Dissonance (06-Energy.md) -----------------------------

    /**
     * Bande harmonique émise. Une machine accordée sur la même bande travaille
     * proprement ; une machine sur une autre bande tourne quand même, mais coûte plus
     * cher et injecte de la dissonance ici. Défaut : la Fondamentale (le Field Emitter
     * T2 est mono-bande, sans choix — la complexité arrive avec l'Émetteur Accordable).
     */
    default HarmonicBand getBand() {
        return HarmonicBand.FUNDAMENTAL;
    }

    /** Dissonance accumulée dans ce champ (0 = propre). */
    default int getDissonance() {
        return 0;
    }

    /** Injecte de la dissonance (appelé quand une machine désaccordée y puise). */
    default void addDissonance(int amount) {
    }

    /**
     * Vrai quand la dissonance dépasse le seuil : le champ devient <b>instable</b> et
     * saute des ticks d'alimentation — les machines hoquettent visiblement au lieu de
     * se dégrader en silence. Ne bloque jamais définitivement.
     */
    default boolean isUnstable() {
        return false;
    }
}
