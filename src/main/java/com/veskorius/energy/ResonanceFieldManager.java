package com.veskorius.energy;

import com.veskorius.Veskorius;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

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
 *
 * L'index est purgé au déchargement d'un niveau ({@link #onLevelUnload}) : sans ça,
 * la map statique garderait des positions d'un monde solo précédent après un retour
 * au menu puis chargement d'un autre monde (même JVM). La correction est de toute
 * façon garantie par la vérification du type de block entity à la lecture, mais on
 * évite ainsi une accumulation inutile.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID)
public final class ResonanceFieldManager {

    private static final Map<ResourceKey<Level>, Set<BlockPos>> EMITTERS = new ConcurrentHashMap<>();

    private ResonanceFieldManager() {
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            EMITTERS.remove(serverLevel.dimension());
        }
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
        // On sert d'abord la source LA PLUS FORTE, puis on retombe sur les autres si
        // elle est à sec (voir strongestFirst). C'est la règle écrite de 06-Energy.md,
        // « l'intensité retenue est celle de la source la plus forte ».
        for (BlockPos emitterPos : strongestFirst(level, consumerPos, true)) {
            if (!(level.getBlockEntity(emitterPos) instanceof IResonanceField field)) {
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
     * Les sources qui couvrent {@code pos}, <b>de la plus forte à la plus faible</b>, et à
     * force égale dans l'ordre de pose.
     *
     * <p>Ce tri applique enfin ce que 06-Energy.md promettait depuis le début : « champs
     * superposés : pas d'addition, l'intensité retenue est celle de la <b>source la plus
     * forte</b> ». Le code, lui, servait depuis le premier émetteur inscrit, quelle que
     * soit son intensité — et personne ne pouvait s'en apercevoir, puisque <i>toutes</i>
     * les sources du mod valaient 100. L'écart est resté invisible jusqu'au Convergence
     * Core, la première source d'intensité différente : sans ce tri, un Core posé au milieu
     * d'une base ancienne aurait été systématiquement ignoré au profit du premier émetteur
     * T2 venu, et le multi-bloc le plus coûteux du jeu n'aurait rien changé du tout.
     *
     * <p>Le repli sur les sources suivantes est conservé : si la plus forte est vide, on
     * bascule sur la suivante plutôt que de bloquer la machine.
     */
    private static java.util.List<BlockPos> strongestFirst(ServerLevel level, BlockPos pos,
                                                           boolean requireActive) {
        Set<BlockPos> set = EMITTERS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return java.util.List.of();
        }
        java.util.List<BlockPos> covering = new java.util.ArrayList<>();
        // Copie défensive : extractOsc peut, via setChanged, déclencher des effets de
        // bord, et une position invalide est retirée en cours d'itération.
        for (BlockPos emitterPos : set.toArray(BlockPos[]::new)) {
            BlockEntity be = level.getBlockEntity(emitterPos);
            if (!(be instanceof IResonanceField field)) {
                // La position n'héberge plus d'émetteur : nettoyage paresseux.
                set.remove(emitterPos);
                continue;
            }
            if (requireActive && !field.isActive()) {
                continue;
            }
            long rangeSqr = (long) field.getRange() * field.getRange();
            if (emitterPos.distSqr(pos) <= rangeSqr) {
                covering.add(emitterPos);
            }
        }
        if (covering.size() > 1) {
            // Tri STABLE : à intensité égale, l'ordre d'insertion est conservé, donc
            // « première posée, première servie » continue de valoir entre égaux.
            covering.sort(java.util.Comparator.comparingInt(
                (BlockPos p) -> level.getBlockEntity(p) instanceof IResonanceField f
                    ? f.getFieldStrength() : 0).reversed());
        }
        return covering;
    }

    /**
     * L'émetteur qui <b>servirait</b> un consommateur à cette position — même ordre de
     * sélection que {@link #supply}. Sert à lire la <b>bande harmonique</b> du champ
     * avant d'y puiser (accord/désaccord, 06-Energy.md) et à y réinjecter de la
     * dissonance. Ne prélève rien.
     */
    @org.jetbrains.annotations.Nullable
    public static IResonanceField findSource(ServerLevel level, BlockPos consumerPos) {
        return findSource(level, consumerPos, true);
    }

    /**
     * Le champ qui <b>couvre</b> cette position, actif ou non. Sert au HUD de champ
     * (12-UX) : celui-ci doit rester stable et afficher « réserve à zéro » ou « champ
     * instable » — or un émetteur à sec ou instable n'est justement pas « actif », et
     * {@link #findSource} le sauterait, faisant clignoter le HUD au moment précis où il
     * a le plus à dire.
     */
    @org.jetbrains.annotations.Nullable
    public static IResonanceField coveringSource(ServerLevel level, BlockPos pos) {
        return findSource(level, pos, false);
    }

    @org.jetbrains.annotations.Nullable
    private static IResonanceField findSource(ServerLevel level, BlockPos consumerPos, boolean requireActive) {
        // Même ordre que supply : sans ça, une machine lirait la bande harmonique d'une
        // source et puiserait dans une autre — le désaccord se calculerait sur le mauvais
        // champ, et la dissonance s'injecterait chez un innocent.
        for (BlockPos emitterPos : strongestFirst(level, consumerPos, requireActive)) {
            if (level.getBlockEntity(emitterPos) instanceof IResonanceField field) {
                return field;
            }
        }
        return null;
    }

    /**
     * Champ le <b>plus pollué</b> dans un rayon, ou {@code null} si tout est propre.
     * Sert au Damping Array : il s'attaque en priorité au pire foyer de dissonance,
     * plutôt qu'au plus proche. Contrairement à {@link #findSource}, un émetteur à sec
     * ou instable compte quand même — c'est justement celui qu'il faut nettoyer.
     */
    @org.jetbrains.annotations.Nullable
    public static IResonanceField mostDissonantSource(ServerLevel level, BlockPos from, int maxRange) {
        Set<BlockPos> set = EMITTERS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return null;
        }
        long rangeSqr = (long) maxRange * maxRange;
        IResonanceField worst = null;
        int worstDissonance = 0;
        for (BlockPos pos : set.toArray(BlockPos[]::new)) {
            if (!(level.getBlockEntity(pos) instanceof IResonanceField field)) {
                set.remove(pos);
                continue;
            }
            if (pos.distSqr(from) > rangeSqr) {
                continue;
            }
            int dissonance = field.getDissonance();
            if (dissonance > worstDissonance) {
                worstDissonance = dissonance;
                worst = field;
            }
        }
        return worst;
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
}
