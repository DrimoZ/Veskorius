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
}
