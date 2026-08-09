package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/**
 * <b>Châssis de caisson, à cadre connecté.</b> Un châssis isolé montre son cadre métallique
 * sur ses douze arêtes — une caisse. Accolez-en plusieurs : le cadre disparaît des arêtes
 * intérieures et ne subsiste qu'autour du groupe, qui se lit alors comme <i>un</i> panneau
 * et non comme une grille de cubes.
 *
 * <p><b>Pourquoi le cadre est de la géométrie et pas de la texture.</b> Une texture
 * connectée à la Optifine demande 47 tuiles et un mod de rendu ; le jeu de base n'en a pas.
 * Poser le cadre en relief sur les arêtes ouvertes donne le même résultat avec deux
 * textures, et en donne même un peu plus : le cadre <b>déborde</b> légèrement, donc il
 * accroche la lumière et se lit comme une pièce rapportée, pas comme un dessin.
 *
 * <p>Le débord n'entre jamais dans un voisin par construction : une baguette n'existe que
 * là où les deux faces qui bordent l'arête sont libres de châssis.
 *
 * <p><b>Les trois paliers ne se connectent pas entre eux.</b> Un caisson fracturé contre un
 * caisson veskorien garde son cadre des deux côtés — le palier est une information qu'on
 * lit sur le bâtiment, et la fondre serait la perdre.
 */
public class ConnectedChassisBlock extends AbstractConnectedBlock {

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
