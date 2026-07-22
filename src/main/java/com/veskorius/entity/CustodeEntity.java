package com.veskorius.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Custode (09-Entities.md) : garde standard des sites veskoriens. **Réactif, pas
 * agressif** (pilier 4) : il ne cible un joueur que dans un **rayon de 6 blocs**
 * (porté par la faible portée de suivi {@code FOLLOW_RANGE}) ou s'il se fait
 * frapper. Un joueur qui reste à distance n'est jamais attaqué. 30 PV, 6 de dégâts.
 *
 * Il garde un site : il est posé par la génération de l'Avant-poste
 * ({@code RuinFeature}) plutôt que par un spawn naturel — cohérent avec « les
 * Custodes gardent des sites, pas des territoires » (09-Entities.md).
 *
 * Non encore fait (différé) : réagir aussi quand une machine du site est endommagée
 * (demande un suivi d'événements), et patrouiller/retourner à un point fixe.
 */
public class CustodeEntity extends Monster {

    /** Rayon de détection (09-Entities.md), exprimé via la portée de suivi. */
    private static final double DETECTION_RANGE = 6.0;

    public CustodeEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            // Portée de suivi = rayon de réactivité : il ne « voit » un joueur qu'à 6 blocs.
            .add(Attributes.FOLLOW_RANGE, DETECTION_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Riposte s'il est frappé (même hors des 6 blocs : on l'a provoqué).
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Sinon, ne cible un joueur que dans la portée de suivi (6 blocs) : réactif.
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
