package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/**
 * <b>Châssis de caisson, à cadre connecté.</b> Un châssis isolé montre son cadre métallique
 * sur ses douze arêtes — une caisse. Accolez-en plusieurs : le cadre disparaît des arêtes
 * intérieures et ne subsiste qu'autour du groupe, qui se lit alors comme <i>un</i> panneau
 * et non comme une grille de cubes.
 *
 * <p><b>Ce bloc n'a aucune propriété de blockstate, et c'est le cœur du dispositif.</b> Une
 * première version portait six booléens — un par face — tenus à jour par {@code updateShape}.
 * Elle butait sur le <b>coin rentrant</b> : dans une disposition en L, le bloc de l'angle
 * touche ses deux voisins, ne dessine donc aucune bordure, et son coin reste nu. Le combler
 * demande de savoir si la <b>diagonale</b> est occupée — douze bits de plus, soit 262 144
 * états par bloc au lieu de 64. Ce n'est pas une option.
 *
 * <p>Le voisinage est donc lu <b>au moment où le chunk se construit</b>, par un modèle
 * dynamique côté client ({@code ConnectedChassisModel}), qui a accès au monde. Trois
 * conséquences, toutes bonnes : les diagonales sont disponibles, l'état ne pèse rien, et
 * <b>un mur bâti avant l'arrivée de cette fonctionnalité se connecte tout seul</b> — il n'y a
 * plus d'état sauvegardé à migrer.
 *
 * <p><b>Les trois paliers ne se connectent pas entre eux.</b> Un caisson fracturé contre un
 * caisson veskorien garde son cadre des deux côtés — le palier est une information qu'on lit
 * sur le bâtiment, et la fondre serait la perdre.
 */
public class ConnectedChassisBlock extends Block {

    public static final MapCodec<ConnectedChassisBlock> CODEC =
        simpleCodec(ConnectedChassisBlock::new);

    public ConnectedChassisBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
