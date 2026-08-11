package com.veskorius.block;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

/**
 * <b>La règle du cadre connecté, isolée et sans rendu.</b> Étant donné le voisinage d'un bloc,
 * quelles pièces de cadre faut-il poser ?
 *
 * <p>Une seule règle pour le verre et pour les châssis. Les deux ont d'abord eu leur propre
 * implémentation — six booléens de blockstate d'un côté, un modèle dynamique de l'autre — ce
 * qui voulait dire deux fois le même raisonnement, dont une seule moitié corrigée quand un
 * défaut apparaissait.
 *
 * <p>Cette classe ne connaît ni modèle, ni quad, ni client : elle est ici pour être
 * <b>testable</b>. Les trois règles ci-dessous sont exactement le genre de raisonnement qu'on
 * croit juste et qui ne l'est pas — les deux dernières ont été écrites après avoir vu le
 * défaut en jeu, pas avant.
 *
 * <h2>Le voisinage</h2>
 * Dix-huit bits : les <b>6 faces</b>, puis les <b>12 diagonales d'arête</b> (les blocs qui ne
 * partagent avec nous qu'une arête). Ces douze-là sont la raison pour laquelle le cadre <b>ne
 * peut pas</b> vivre dans le blockstate : six booléens font 64 états, dix-huit en font
 * 262 144, par bloc.
 *
 * <h2>Les règles</h2>
 * <ol>
 *   <li><b>Arête convexe</b> — ni {@code a} ni {@code b} n'a de voisin : c'est un bord de la
 *       silhouette. Un bloc isolé garde ses douze baguettes ; celui du milieu d'un mur n'en a
 *       aucune.</li>
 *   <li><b>Arête concave</b> — une seule des deux faces est couverte, et la <b>diagonale</b>
 *       l'est aussi : la surface tourne d'un plan à l'autre. C'est le pied d'un bloc posé sur
 *       une dalle : sa face verticale rejoignait le dessus de la dalle sans la moindre
 *       séparation.</li>
 *   <li><b>Quart de cadre</b> — la face {@code f} est dégagée, {@code p} et {@code q} sont
 *       couverts (donc aucune baguette ne passe par ce coin), et la diagonale {@code p+q} ne
 *       l'est pas. C'est le coin rentrant d'un L, qui restait nu.</li>
 * </ol>
 *
 * <p>La condition sur la diagonale, en 3, n'est pas une précaution : sans elle, le bloc
 * central d'un mur plein poserait quatre quarts de cadre au milieu d'une surface lisse.
 */
public final class ConnectedFrame {

    private ConnectedFrame() {
    }

    /**
     * Un bloc à cadre connecté, et le nom de ses modèles.
     *
     * <p>Cette liste est le <b>contrat entre la datagen et le rendu</b> : la première écrit
     * les fichiers, le second les réclame. Un désaccord d'un caractère rendrait le bloc
     * invisible sans la moindre erreur, d'où l'unique déclaration.
     */
    public record Frame(Supplier<? extends Block> block, String name, String base) {

        /** Le modèle de fond : la plaque d'un caisson, la vitre d'un verre. */
        public String baseModel() {
            return name + base;
        }
    }

    public static final List<Frame> FRAMES = List.of(
        new Frame(ModBlocks.FRACTURED_CHASSIS, "fractured_chassis", "_plate"),
        new Frame(ModBlocks.ATTUNED_CHASSIS, "attuned_chassis", "_plate"),
        new Frame(ModBlocks.VESKORIAN_CHASSIS, "veskorian_chassis", "_plate"),
        new Frame(ModBlocks.RESONANCE_GLASS, "resonance_glass", "_pane"),
        new Frame(ModBlocks.LUMINOUS_RESONANCE_GLASS, "luminous_resonance_glass", "_pane"));

    // --- Le voisinage ---------------------------------------------------------

    /** Bit du voisin direct dans la direction donnée. */
    public static int faceBit(Direction direction) {
        return 1 << direction.ordinal();
    }

    /**
     * Bit de la diagonale d'arête entre {@code p} et {@code q}.
     *
     * <p>Les deux directions doivent être d'axes différents ; l'ordre n'importe pas, la même
     * arête donne le même bit.
     */
    public static int edgeBit(Direction p, Direction q) {
        int lo = Math.min(p.ordinal(), q.ordinal());
        int hi = Math.max(p.ordinal(), q.ordinal());
        int index = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = i + 1; j < 6; j++) {
                if (Direction.from3DDataValue(i).getAxis() == Direction.from3DDataValue(j).getAxis()) {
                    continue;
                }
                if (i == lo && j == hi) {
                    return 1 << (6 + index);
                }
                index++;
            }
        }
        throw new IllegalArgumentException("pas une arête : " + p + " / " + q);
    }

    /**
     * Construit le voisinage en interrogeant {@code probe} pour les 6 faces puis les 12
     * diagonales.
     *
     * <p>Le balayage vit ici, et pas dans le modèle, pour une raison précise : c'est du code
     * d'indexation. Ici, un test peut lui donner un voisinage inventé et vérifier ce qui en
     * sort.
     */
    public static int neighbourhood(Probe probe) {
        int mask = 0;
        for (Direction d : Direction.values()) {
            if (probe.isConnected(d, null)) {
                mask |= faceBit(d);
            }
        }
        for (Direction p : Direction.values()) {
            for (Direction q : Direction.values()) {
                if (p.getAxis() == q.getAxis() || p.ordinal() > q.ordinal()) {
                    continue;
                }
                if (probe.isConnected(p, q)) {
                    mask |= edgeBit(p, q);
                }
            }
        }
        return mask;
    }

    /** « Y a-t-il le même bloc à un pas de {@code first}, puis éventuellement de {@code second} ? » */
    @FunctionalInterface
    public interface Probe {
        boolean isConnected(Direction first, Direction second);
    }

    public static boolean connected(int mask, Direction direction) {
        return (mask & faceBit(direction)) != 0;
    }

    public static boolean connectedDiagonally(int mask, Direction p, Direction q) {
        return (mask & edgeBit(p, q)) != 0;
    }

    // --- Les règles -----------------------------------------------------------

    /**
     * Faut-il une baguette sur l'arête {@code a}/{@code b} ?
     *
     * <p>Trois cas, et le deuxième manquait :
     * <ul>
     *   <li>les deux faces dégagées : arête de silhouette, on la souligne ;</li>
     *   <li>les deux faces couvertes : arête intérieure, rien n'est visible ;</li>
     *   <li><b>une seule couverte</b> : la baguette n'a de sens que si la surface TOURNE, donc
     *       si la diagonale est occupée. Sinon les deux faces se prolongent l'une l'autre et
     *       une baguette y tracerait un trait au milieu d'un plan.</li>
     * </ul>
     */
    public static boolean hasBar(int mask, Direction a, Direction b) {
        boolean ca = connected(mask, a);
        boolean cb = connected(mask, b);
        if (ca == cb) {
            return !ca;
        }
        return connectedDiagonally(mask, a, b);
    }

    /** Le quart de cadre de la face {@code f}, dans le coin {@code p}/{@code q}. */
    public static boolean hasCorner(int mask, Direction f, Direction p, Direction q) {
        return !connected(mask, f)
            && connected(mask, p)
            && connected(mask, q)
            && !connectedDiagonally(mask, p, q)
            // Si une baguette passe déjà par ce coin, la doubler d'un quart de cadre poserait
            // deux surfaces au même endroit — ça clignoterait.
            && !hasBar(mask, f, p)
            && !hasBar(mask, f, q);
    }

    // --- Balayages et noms ----------------------------------------------------

    /** Les douze arêtes du cube, chacune une seule fois. */
    public static void forEachEdge(EdgeConsumer consumer) {
        for (Direction a : Direction.values()) {
            for (Direction b : Direction.values()) {
                if (a.getAxis() == b.getAxis() || a.ordinal() > b.ordinal()) {
                    continue;
                }
                consumer.accept(a, b);
            }
        }
    }

    /** Les vingt-quatre coins de faces : une face, et l'une des quatre paires qui la bordent. */
    public static void forEachFaceCorner(CornerConsumer consumer) {
        for (Direction f : Direction.values()) {
            for (Direction p : Direction.values()) {
                if (p.getAxis() == f.getAxis()) {
                    continue;
                }
                for (Direction q : Direction.values()) {
                    if (q.getAxis() == f.getAxis() || q.getAxis() == p.getAxis()
                        || q.ordinal() < p.ordinal()) {
                        continue;
                    }
                    consumer.accept(f, p, q);
                }
            }
        }
    }

    public static String barName(String frame, Direction a, Direction b) {
        return frame + "_bar_" + a.getSerializedName() + "_" + b.getSerializedName();
    }

    public static String cornerName(String frame, Direction f, Direction p, Direction q) {
        return frame + "_corner_" + f.getSerializedName()
            + "_" + p.getSerializedName() + "_" + q.getSerializedName();
    }

    @FunctionalInterface
    public interface EdgeConsumer {
        void accept(Direction a, Direction b);
    }

    @FunctionalInterface
    public interface CornerConsumer {
        void accept(Direction f, Direction p, Direction q);
    }
}
