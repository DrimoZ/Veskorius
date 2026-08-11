package com.veskorius.block;

import net.minecraft.core.Direction;

/**
 * <b>La règle du cadre connecté, isolée et sans rendu.</b> Étant donné le voisinage d'un
 * châssis, quelles pièces de cadre faut-il poser ?
 *
 * <p>Cette classe ne connaît ni modèle, ni quad, ni client. Elle est ici pour être
 * <b>testable</b> : la règle du coin rentrant est exactement le genre de raisonnement qu'on
 * croit juste et qui ne l'est pas, et le vérifier en jeu coûte un aller-retour de plusieurs
 * minutes. Elle est vérifiée par {@code ChassisFrameGameTests}.
 *
 * <h2>Le voisinage</h2>
 * Dix-huit bits : les <b>6 faces</b>, puis les <b>12 diagonales d'arête</b> (les blocs qui ne
 * partagent avec nous qu'une arête). Ces douze-là sont la raison d'être de tout ce mécanisme
 * — et la raison pour laquelle le cadre <b>ne peut pas</b> vivre dans le blockstate : six
 * booléens font 64 états, dix-huit en font 262 144, par bloc.
 *
 * <h2>Les deux règles</h2>
 * <ol>
 *   <li><b>Une baguette d'arête</b> sur l'arête entre les faces {@code a} et {@code b} : si
 *       NI {@code a} NI {@code b} n'a de voisin. Un châssis isolé garde ses douze baguettes ;
 *       celui du milieu d'un mur n'en a aucune.</li>
 *   <li><b>Un quart de cadre</b> dans le coin de la face {@code f} entre {@code p} et
 *       {@code q} : si {@code f} est dégagée, si {@code p} ET {@code q} ont un voisin (donc
 *       aucune baguette ne couvre ce coin), et si la <b>diagonale</b> {@code p+q} n'en a pas.
 *       C'est le coin rentrant : dans une disposition en L, le bloc de l'angle touche ses
 *       deux voisins, ne dessine donc aucune bordure — et son coin restait nu.</li>
 * </ol>
 *
 * <p>La troisième condition n'est pas une précaution : sans elle, le bloc central d'un mur
 * plein — qui a ses quatre côtés connectés — poserait ses quatre quarts de cadre au milieu
 * d'une surface lisse.
 */
public final class ChassisFrame {

    private ChassisFrame() {
    }

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
        // Index dense des 12 paires d'axes différents, dans l'ordre des ordinaux.
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

    /** « Un châssis du même palier est de ce côté. » */
    public static boolean connected(int mask, Direction direction) {
        return (mask & faceBit(direction)) != 0;
    }

    /** « Un châssis du même palier occupe la diagonale de cette arête. » */
    public static boolean connectedDiagonally(int mask, Direction p, Direction q) {
        return (mask & edgeBit(p, q)) != 0;
    }

    /**
     * Construit le voisinage en interrogeant {@code probe} pour les 6 faces puis les 12
     * diagonales.
     *
     * <p>Le balayage vit ici, et pas dans le modèle, pour une raison précise : c'est du code
     * d'indexation, exactement le genre qu'on croit juste. Ici, un test peut lui donner un
     * voisinage inventé et vérifier ce qui en sort.
     */
    public static int neighbourhood(Probe probe) {
        int mask = 0;
        for (Direction d : Direction.values()) {
            if (probe.isChassis(d, null)) {
                mask |= faceBit(d);
            }
        }
        for (Direction p : Direction.values()) {
            for (Direction q : Direction.values()) {
                if (p.getAxis() == q.getAxis() || p.ordinal() > q.ordinal()) {
                    continue;
                }
                if (probe.isChassis(p, q)) {
                    mask |= edgeBit(p, q);
                }
            }
        }
        return mask;
    }

    /** « Y a-t-il un châssis à un pas de {@code first}, puis éventuellement de {@code second} ? » */
    @FunctionalInterface
    public interface Probe {
        boolean isChassis(Direction first, Direction second);
    }

    /** Règle 1 : la baguette de l'arête {@code a}/{@code b}. */
    public static boolean hasBar(int mask, Direction a, Direction b) {
        return !connected(mask, a) && !connected(mask, b);
    }

    /** Règle 2 : le quart de cadre de la face {@code f}, dans le coin {@code p}/{@code q}. */
    public static boolean hasCorner(int mask, Direction f, Direction p, Direction q) {
        return !connected(mask, f)
            && connected(mask, p)
            && connected(mask, q)
            && !connectedDiagonally(mask, p, q);
    }

    /**
     * Les douze arêtes du cube, chacune une seule fois : deux faces d'axes différents, prises
     * dans l'ordre des ordinaux.
     */
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

    /**
     * Les vingt-quatre coins de faces : une face, et l'une des quatre paires de directions
     * qui la bordent.
     */
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

    /**
     * Le nom du modèle d'une baguette d'arête.
     *
     * <p>Ces deux fabriques de noms vivent ici, en commun, et pas du côté du client : la
     * datagen écrit les fichiers, le modèle dynamique les réclame, et si les deux ne
     * s'accordent pas au caractère près le bloc devient invisible <b>sans une seule erreur</b>.
     * Une constante partagée est la seule façon de rendre ce désaccord impossible.
     */
    public static String barName(String chassis, Direction a, Direction b) {
        return chassis + "_bar_" + a.getSerializedName() + "_" + b.getSerializedName();
    }

    /** Le nom du modèle d'un quart de cadre. */
    public static String cornerName(String chassis, Direction f, Direction p, Direction q) {
        return chassis + "_corner_" + f.getSerializedName()
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
