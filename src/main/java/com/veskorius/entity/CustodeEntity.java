package com.veskorius.entity;

import com.veskorius.config.VeskoriusConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Custode (09-Entities.md) : garde standard des sites veskoriens. **Réactif, pas
 * agressif** (pilier 4) : il ne cible un joueur que dans un **rayon de 6 blocs**
 * (porté par la faible portée de suivi {@code FOLLOW_RANGE}) ou s'il se fait
 * frapper. Un joueur qui reste à distance n'est jamais attaqué. 30 PV, 6 de dégâts.
 *
 * Il garde un site : il est intégré à la pièce de structure de l'Avant-poste
 * (voir {@code ModStructurePieceProvider}, entité persistante) plutôt que par un
 * spawn naturel — cohérent avec « les Custodes gardent des sites, pas des
 * territoires » (09-Entities.md).
 *
 * Il réagit aussi quand une machine du site est cassée ({@code CustodeAlertHandler}),
 * et patrouille autour de son point de garde ({@code restrictTo} +
 * {@link MoveTowardsRestrictionGoal}, persisté au rechargement).
 */
public class CustodeEntity extends Monster {

    /** Rayon de détection (09-Entities.md), exprimé via la portée de suivi. */
    private static final double DETECTION_RANGE = 6.0;

    /** Rayon dans lequel le Custode reste autour de son point de garde (patrouille). */
    private static final int GUARD_RADIUS = 12;

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
        // Reste près de son point de garde (patrouille, ne s'éloigne pas du site).
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.9));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Riposte s'il est frappé (même hors des 6 blocs : on l'a provoqué).
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Sinon, ne cible un joueur que dans la portée de suivi (6 blocs) : réactif.
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Applique les stats configurées aux individus nouvellement apparus (les attributs
     * de base sont posés au chargement du mod, avant la config SERVER — on les
     * réécrit ici, quand le monde et la config sont chargés).
     */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        // Point de garde = là où il apparaît (l'Avant-poste).
        restrictTo(blockPosition(), GUARD_RADIUS);
        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(configHealth());
        }
        setHealth(getMaxHealth());
        AttributeInstance damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(configDamage());
        }
        AttributeInstance range = getAttribute(Attributes.FOLLOW_RANGE);
        if (range != null) {
            range.setBaseValue(configDetectionRange());
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    // --- Valeurs de config, redéfinissables par les variantes ------------------
    //
    // Trois accesseurs plutôt que trois appels directs à VeskoriusConfig : le Custode
    // Lourd hérite de toute cette classe (IA, patrouille, persistance du point de garde,
    // sons) et n'en change QUE ses chiffres. Sans ces crochets il lui faudrait réécrire
    // finalizeSpawn en entier — donc dupliquer le restrictTo et le setHealth, et les
    // laisser diverger le jour où l'un des deux change.

    protected double configHealth() {
        return VeskoriusConfig.custodeHealth();
    }

    protected double configDamage() {
        return VeskoriusConfig.custodeDamage();
    }

    protected double configDetectionRange() {
        return VeskoriusConfig.custodeDetectionRange();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    // --- Persistance du point de garde (sinon perdu au rechargement) ----------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (hasRestriction()) {
            BlockPos home = getRestrictCenter();
            tag.putInt("HomeX", home.getX());
            tag.putInt("HomeY", home.getY());
            tag.putInt("HomeZ", home.getZ());
            tag.putInt("HomeRadius", (int) getRestrictRadius());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomeRadius")) {
            restrictTo(new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ")),
                tag.getInt("HomeRadius"));
        }
    }
}
