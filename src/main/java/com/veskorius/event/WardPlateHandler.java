package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.item.VeskoriusArmor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * La contrepartie du Rift-Ward Plate : <b>−10 % de vitesse de minage tant qu'il est
 * porté</b> (04-Materials.md).
 *
 * <p>Elle n'est pas décorative. Le Plate donne l'immunité totale au déphasage sur tout le
 * corps, c'est-à-dire la seule protection absolue du mod, obtenue avec le butin garanti
 * d'une unique Faille. Sans coût permanent, il n'y aurait aucune raison de le retirer, et
 * la dernière pièce d'équipement du jeu deviendrait un choix qu'on ne repose jamais.
 *
 * <p>Le malus est <b>léger et constant</b> plutôt que spectaculaire : on doit sentir qu'on
 * porte quelque chose de lourd sans que la partie devienne pénible — le joueur qui exploite
 * une Faille mine peu, celui qui creuse un tunnel a intérêt à changer de plastron.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID)
public final class WardPlateHandler {

    /** Fraction de vitesse conservée. 0,9 = −10 %. */
    private static final float SPEED_FACTOR = 0.9f;

    private WardPlateHandler() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (VeskoriusArmor.wearsWardPlate(event.getEntity())) {
            event.setNewSpeed(event.getNewSpeed() * SPEED_FACTOR);
        }
    }
}
