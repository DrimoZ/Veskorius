package com.veskorius.energy;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Aiguillage entre les machines consommatrices et les émetteurs de champ.
 *
 * C'est la pièce que le pilier « le réseau est vivant, jamais un tuyau »
 * (01-Vision-Pillars.md) rend nécessaire : une machine ne se branche à rien, elle
 * demande simplement « y a-t-il de l'énergie à ma position ? ». Le manager tient
 * l'index des émetteurs par dimension et répond.
 *
 * Non persisté volontairement : les émetteurs se ré-enregistrent à leur premier
 * tick après un chargement de monde, et leur réserve d'Osc, elle, vit dans le NBT
 * de leur block entity. L'index ne fait que retrouver *qui* couvre *quoi* ; il n'y
 * a donc rien à sauvegarder.
 *
 * Un seul thread par niveau touche cet index (le thread serveur), mais la map de
 * premier niveau est en {@link ConcurrentHashMap} par prudence face aux niveaux
 * multiples.
 */
public final class ResonanceFieldManager {

    private static final Map<ResourceKey<Level>, Set<BlockPos>> EMITTERS = new ConcurrentHashMap<>();

    private ResonanceFieldManager() {
    }

    /**
     * Enregistre un émetteur. Idempotent : un émetteur peut appeler ceci à chaque
     * tick sans conséquence. L'ordre d'insertion (LinkedHashSet) sert de proxy à la
     * règle « première posée, première servie » (06-Energy.md).
     */
    public static void register(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        EMITTERS.computeIfAbsent(level.dimension(), k -> new LinkedHashSet<>()).add(pos.immutable());
    }

    /** Retire un émetteur (bloc cassé ou chunk déchargé). */
    public static void unregister(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        Set<BlockPos> set = EMITTERS.get(level.dimension());
        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                EMITTERS.remove(level.dimension());
            }
        }
    }

    /**
     * Fournit jusqu'à {@code osc} à un consommateur situé en {@code consumerPos}.
     * Retourne l'Osc réellement fourni (0 si aucun champ actif ne le couvre).
     *
     * Sert depuis un seul émetteur — le premier, dans l'ordre de pose, qui couvre
     * la position et n'est pas vide. C'est délibérément non cumulatif :
     * superposer deux émetteurs n'additionne ni l'intensité ni le débit
     * (06-Energy.md, anti-stacking). Si le plus ancien est à sec, on bascule sur
     * le suivant plutôt que de bloquer la machine.
     */
    public static int supply(ServerLevel level, BlockPos consumerPos, int osc) {
        if (osc <= 0) {
            return 0;
        }
        Set<BlockPos> set = EMITTERS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return 0;
        }

        // Copie défensive : extractOsc peut, via setChanged, déclencher des effets
        // de bord, et un émetteur invalide est retiré en cours d'itération.
        for (BlockPos emitterPos : set.toArray(BlockPos[]::new)) {
            BlockEntity be = level.getBlockEntity(emitterPos);
            if (!(be instanceof IResonanceField field)) {
                // La position n'héberge plus d'émetteur : nettoyage paresseux.
                set.remove(emitterPos);
                continue;
            }
            if (!field.isActive()) {
                continue;
            }
            long rangeSqr = (long) field.getRange() * field.getRange();
            if (emitterPos.distSqr(consumerPos) > rangeSqr) {
                continue;
            }
            int drawn = field.extractOsc(osc);
            if (drawn > 0) {
                return drawn;
            }
        }
        return 0;
    }

    /**
     * Émetteur actif le plus proche de {@code from} dans un rayon de {@code maxRange}
     * blocs, ou {@code null}. Sert au Resonance Locator (détection de signature de
     * champ, 07-World-Generation.md). Parcourt l'index — pas de scan de blocs.
     */
    @org.jetbrains.annotations.Nullable
    public static BlockPos nearestSource(ServerLevel level, BlockPos from, int maxRange) {
        Set<BlockPos> set = EMITTERS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return null;
        }
        double bestSq = (double) maxRange * maxRange;
        BlockPos best = null;
        for (BlockPos p : set.toArray(BlockPos[]::new)) {
            if (level.getBlockEntity(p) instanceof IResonanceField field && field.isActive()) {
                double d = p.distSqr(from);
                if (d <= bestSq) {
                    bestSq = d;
                    best = p;
                }
            }
        }
        return best;
    }

    /**
     * Vrai si au moins un émetteur actif couvre {@code consumerPos}. Pour une
     * machine qui veut savoir si elle est dans un champ sans encore rien prélever.
     */
    public static boolean hasFieldAt(ServerLevel level, BlockPos consumerPos) {
        Set<BlockPos> set = EMITTERS.get(level.dimension());
        if (set == null) {
            return false;
        }
        for (BlockPos emitterPos : set) {
            if (level.getBlockEntity(emitterPos) instanceof IResonanceField field
                && field.isActive()
                && emitterPos.distSqr(consumerPos) <= (long) field.getRange() * field.getRange()) {
                return true;
            }
        }
        return false;
    }
}
