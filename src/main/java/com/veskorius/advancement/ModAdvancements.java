package com.veskorius.advancement;

import com.veskorius.Veskorius;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * <b>Décerner un advancement depuis le code</b>, pour les moments qu'aucun déclencheur
 * vanilla ne sait décrire.
 *
 * <p>La plupart des advancements du mod se lisent dans un inventaire ou dans une mort :
 * posséder tel plan, avoir vaincu tel gardien. Mais certains sont des <b>événements</b> —
 * un multi-bloc qui se referme, un orage qu'on a traversé — et il n'existe aucun critère
 * pour « la figure est complète ». Le motif standard consiste alors à déclarer un critère
 * {@code impossible}, que rien ne satisfait jamais tout seul, et à le décerner à la main
 * au moment exact.
 *
 * <p><b>Aux joueurs à portée, pas à tous.</b> Un Cœur de Convergence qui se forme à
 * l'autre bout de la carte ne regarde pas celui qui creuse ailleurs : l'advancement dirait
 * « vous avez fait ça » à quelqu'un qui n'a rien fait, et l'arbre perdrait sa valeur de
 * récit.
 */
public final class ModAdvancements {

    /** Portée à laquelle un événement de monde « appartient » à un joueur. */
    private static final double WITNESS_RANGE = 32.0;

    private ModAdvancements() {
    }

    /** Décerne à un joueur précis. */
    public static void award(ServerPlayer player, String path, String criterion) {
        AdvancementHolder holder = player.server.getAdvancements()
            .get(ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path));
        if (holder != null) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    /** Décerne à tous les joueurs assez proches pour avoir vu la chose arriver. */
    public static void awardNearby(ServerLevel level, BlockPos pos, String path, String criterion) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(pos, WITNESS_RANGE)) {
                award(player, path, criterion);
            }
        }
    }
}
