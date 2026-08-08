package com.veskorius.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Custode Archiviste</b> (09-Entities.md) — le gardien d'élite de la salle profonde de
 * l'Archive Régionale. 150 PV, 12 de dégâts, réactif à 10 blocs.
 *
 * <p><b>Le combattre est facultatif, et sa récompense retire un choix plutôt que d'ajouter
 * une ressource.</b> L'Archive donne exactement trois Hyper Refined Crystal, ce qui force à
 * choisir entre le premier Amplificateur et la Chambre de Synthèse. Les deux qu'il garde
 * permettent d'avoir les deux. C'est la bonne forme pour une récompense optionnelle :
 * elle ne rend rien plus rapide, elle rend une décision moins amère — et un joueur qui
 * l'ignore ne perd rien d'essentiel.
 *
 * <p><b>Une seule phase, et une attaque qui interdit de camper.</b> Il n'est pas un boss
 * narratif : c'est un examen de fin de T3. Son attaque signature <b>marque le sol sous sa
 * cible</b> et le fait détoner une seconde plus tard. On ne peut donc pas le combattre
 * immobile, dos au mur, en échangeant des coups — il faut bouger, ce qui est exactement ce
 * qu'une salle d'archive encombrée de rayonnages rend difficile.
 *
 * <p>Le délai avant détonation est <b>le</b> réglage de ce combat : trop court il devient
 * un dégât inévitable qu'on subit, trop long on l'ignore en marchant. Une seconde laisse
 * voir la marque et s'écarter.
 */
public class CustodeArchivisteEntity extends Monster {

    /** Rayon de réactivité (09-Entities.md). Plus large que le Custode ordinaire (6). */
    private static final double DETECTION_RANGE = 10.0;

    /** Il garde une salle, pas un territoire : il ne la quitte pas. */
    private static final int GUARD_RADIUS = 10;

    /** Intervalle entre deux marques, en ticks. */
    private static final int MARK_INTERVAL = 60;

    /** Délai entre la marque et la détonation. Voir la note de classe. */
    private static final int FUSE_TICKS = 20;

    /** Rayon et dégâts de la détonation. */
    private static final double BLAST_RADIUS = 2.5;
    private static final float BLAST_DAMAGE = 5.0f;

    @Nullable
    private BlockPos mark;
    private int fuse;

    public CustodeArchivisteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 150.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.MOVEMENT_SPEED, 0.26)
            .add(Attributes.ARMOR, 6.0)
            .add(Attributes.FOLLOW_RANGE, DETECTION_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.9));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
        net.minecraft.world.level.ServerLevelAccessor level,
        net.minecraft.world.DifficultyInstance difficulty,
        net.minecraft.world.entity.MobSpawnType reason,
        @Nullable net.minecraft.world.entity.SpawnGroupData data) {
        restrictTo(blockPosition(), GUARD_RADIUS);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            tickMark(serverLevel);
        }
    }

    /**
     * Marque le sol sous la cible, puis fait détoner la marque après le délai. La marque
     * est <b>visible</b> pendant tout le délai : sans le signal, l'attaque ne serait pas
     * une contrainte de placement, seulement des dégâts périodiques.
     */
    private void tickMark(ServerLevel level) {
        if (mark != null) {
            fuse--;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                mark.getX() + 0.5, mark.getY() + 0.2, mark.getZ() + 0.5,
                8, BLAST_RADIUS / 2, 0.1, BLAST_RADIUS / 2, 0.0);
            if (fuse <= 0) {
                detonate(level, mark);
                mark = null;
            }
            return;
        }
        if (tickCount % MARK_INTERVAL != 0) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null) {
            return;
        }
        mark = target.blockPosition();
        fuse = FUSE_TICKS;
        level.playSound(null, mark, SoundEvents.BEACON_POWER_SELECT,
            net.minecraft.sounds.SoundSource.HOSTILE, 0.7f, 1.6f);
    }

    private void detonate(ServerLevel level, BlockPos at) {
        Vec3 centre = Vec3.atCenterOf(at);
        AABB box = new AABB(at).inflate(BLAST_RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box)) {
            // Il ne se blesse pas lui-même : il projette la Résonance, il ne la subit pas.
            if (victim == this || victim.distanceToSqr(centre) > BLAST_RADIUS * BLAST_RADIUS) {
                continue;
            }
            victim.hurt(com.veskorius.energy.ModDamageTypes.discharge(level), BLAST_DAMAGE);
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM,
            centre.x, centre.y + 0.5, centre.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, at, SoundEvents.WARDEN_SONIC_BOOM,
            net.minecraft.sounds.SoundSource.HOSTILE, 0.9f, 1.4f);
    }

    /** La marque en cours, ou {@code null}. Exposée pour les tests. */
    @Nullable
    public BlockPos getMark() {
        return mark;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("fuse", fuse);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuse = tag.getInt("fuse");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
