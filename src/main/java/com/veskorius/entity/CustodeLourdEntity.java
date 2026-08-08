package com.veskorius.entity;

import com.veskorius.config.VeskoriusConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Custode Lourd</b> (09-Entities.md) — le garde renforcé du Sigma Laboratory et de
 * l'Archive Régionale. 60 PV, 9 de dégâts, réactif à 8 blocs.
 *
 * <p><b>Il hérite du Custode, et c'est tout le propos.</b> Le dossier le décrit comme
 * « même comportement réactif que le Custode standard, mais rayon plus large » : même
 * patrouille, même refus de poursuivre hors du site, même persistance du point de garde,
 * mêmes sons. Réécrire tout ça pour changer trois nombres aurait créé deux gardes qui se
 * ressemblent aujourd'hui et divergent au premier correctif appliqué à un seul des deux.
 * Il gagne aussi gratuitement la défense de site — {@code CustodeAlertHandler} cherche des
 * {@code CustodeEntity}, donc casser une machine sous son nez le retourne déjà contre vous.
 *
 * <p><b>Ce qu'il ajoute : il en appelle un autre.</b> Quand il prend une cible, il alerte
 * les Lourds à moins de 16 blocs. C'est la seule chose qui justifie la difficulté accrue
 * du Sigma et de l'Archive <b>sans inventer d'agressivité</b> : il ne charge pas plus loin,
 * il ne frappe pas plus vite, il n'attaque toujours que si on entre chez lui — mais on ne
 * l'affronte jamais seul. Un joueur qui les prend un par un peut encore le faire, à
 * condition de les isoler ; c'est une contrainte de placement, pas un mur de PV.
 *
 * <p><b>L'alerte ne se propage qu'aux Lourds SANS cible, et cette condition est la
 * terminaison.</b> Sans elle, deux Lourds à portée l'un de l'autre s'alerteraient
 * mutuellement à chaque tick, indéfiniment. C'est exactement le piège de récursion du
 * Resonance Relay, qui s'alimentait lui-même faute d'un garde-fou : ici la chaîne
 * s'arrête d'elle-même, puisqu'un garde déjà alerté ne relaie plus rien.
 */
public class CustodeLourdEntity extends CustodeEntity {

    /** Rayon de réactivité (09-Entities.md). Plus large que le Custode ordinaire (6). */
    private static final double DETECTION_RANGE = 8.0;

    public CustodeLourdEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.ATTACK_DAMAGE, 9.0)
            // Il est blindé, donc plus lent que le garde ordinaire (0.25). La différence
            // est petite mais elle se sent : on peut le semer, ce qui garde vraie la
            // promesse « un combat évitable ».
            .add(Attributes.MOVEMENT_SPEED, 0.23)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.FOLLOW_RANGE, DETECTION_RANGE);
    }

    @Override
    protected double configHealth() {
        return VeskoriusConfig.custodeLourdHealth();
    }

    @Override
    protected double configDamage() {
        return VeskoriusConfig.custodeLourdDamage();
    }

    @Override
    protected double configDetectionRange() {
        return VeskoriusConfig.custodeLourdDetectionRange();
    }

    /**
     * Prend une cible, et la fait prendre aux Lourds voisins qui n'en ont pas.
     *
     * <p>Le filtre sur le changement de cible compte autant que celui sur les voisins :
     * les goals appellent {@code setTarget} à chaque tick avec la même valeur, et sans
     * ce test on rebalaierait une AABB de 16 blocs vingt fois par seconde par garde.
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = getTarget();
        super.setTarget(target);
        if (target == null || target == previous || level().isClientSide()) {
            return;
        }
        double range = VeskoriusConfig.custodeAlertRange();
        AABB area = getBoundingBox().inflate(range);
        for (CustodeLourdEntity other : level().getEntitiesOfClass(CustodeLourdEntity.class, area)) {
            if (other != this && other.getTarget() == null) {
                other.setTarget(target);
            }
        }
    }
}
