package com.veskorius.entity;

import com.veskorius.block.entity.RiftAnchorBlockEntity;
import com.veskorius.block.entity.RiftCoreBlockEntity;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * <b>Gardien de Faille</b> (09-Entities.md, boss final).
 *
 * <p>Il n'apparaît jamais au hasard : il naît de la <b>pose d'une Ancre fonctionnelle</b>
 * sur une Faille, une fois par Faille et jamais deux. Le vaincre <b>stabilise la Faille
 * définitivement</b> — les dégâts de déphasage cessent même sans Ancre alimentée — et
 * <b>ouvre l'extraction</b>. Avant lui, un Extracteur posé ne rend rien.
 *
 * <p><b>Trois phases, trois comportements réellement différents</b>, parce qu'un boss dont
 * les phases ne changent que les chiffres n'a qu'une phase :
 *
 * <ol>
 *   <li><b>Écho</b> (300 → 200 PV) : il se tient à distance et frappe par échos. Il
 *       <b>recule</b> quand on l'approche, ce qui force à le poursuivre à travers la
 *       bulle plutôt qu'à l'user sur place.</li>
 *   <li><b>Rupture</b> (200 → 80 PV) : il charge, et il <b>ouvre le sol sous le joueur</b>.
 *       La chute n'est pas mortelle — la bulle a un plancher — mais elle sort du combat le
 *       temps de remonter, et c'est le prix d'un mauvais placement.</li>
 *   <li><b>Stabilisation</b> (80 → 0 PV) : il tente de refermer la Faille. Il marche vers
 *       le noyau et, s'il l'atteint, <b>se soigne</b>. Le joueur doit le tenir à distance
 *       du centre : c'est la seule phase où reculer coûte plus cher qu'avancer.</li>
 * </ol>
 *
 * <p><b>Pas de répétition.</b> Le dossier a explicitement rejeté un boss repopulé : la
 * Faille est une ressource finie, et un combat rejouable la rendrait infinie par la bande.
 * La marque de victoire vit donc sur le <b>noyau</b>, comme le compteur d'extractions.
 */
public class RiftGuardianEntity extends Monster {

    public static final float MAX_HEALTH = 300.0f;

    /** Seuils de phase, en PV (09-Entities.md). */
    public static final float PHASE_RUPTURE_AT = 200.0f;
    public static final float PHASE_STABILISATION_AT = 80.0f;

    /** Phase courante, synchronisée : le client en a besoin pour la barre et les effets. */
    private static final EntityDataAccessor<Integer> DATA_PHASE =
        SynchedEntityData.defineId(RiftGuardianEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        net.minecraft.network.chat.Component.translatable("entity.veskorius.rift_guardian"),
        BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    /** Position du noyau dont il est né. Sert à la phase 3 et à marquer la victoire. */
    private BlockPos corePos = BlockPos.ZERO;

    public RiftGuardianEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.ATTACK_DAMAGE, 14.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.ARMOR, 8.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 24.0f));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Appelé à l'apparition : il faut savoir de quelle Faille il est le gardien. */
    public void bindTo(BlockPos core) {
        this.corePos = core.immutable();
    }

    public int getPhase() {
        return entityData.get(DATA_PHASE);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        updatePhase();
        bossEvent.setProgress(getHealth() / getMaxHealth());

        switch (getPhase()) {
            case 1 -> tickEcho();
            case 2 -> tickRupture(serverLevel);
            default -> tickStabilisation(serverLevel);
        }
    }

    private void updatePhase() {
        int phase = getHealth() > PHASE_RUPTURE_AT ? 1
            : getHealth() > PHASE_STABILISATION_AT ? 2 : 3;
        if (phase != getPhase()) {
            entityData.set(DATA_PHASE, phase);
            level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.5f, 0.7f + phase * 0.15f);
        }
    }

    /**
     * Phase 1 : il <b>recule</b> quand le joueur approche. Sans ça, un boss « à distance »
     * se combat exactement comme un boss de mêlée — on colle et on frappe — et la phase
     * n'existe que dans le texte.
     */
    private void tickEcho() {
        Player target = level().getNearestPlayer(this, 6.0);
        if (target == null) {
            return;
        }
        var away = position().subtract(target.position());
        if (away.lengthSqr() < 1.0e-4) {
            return;
        }
        away = away.normalize().scale(0.12);
        setDeltaMovement(getDeltaMovement().add(away.x, 0, away.z));
    }

    /**
     * Phase 2 : le sol s'ouvre sous le joueur. On ne casse qu'un bloc à la fois et
     * seulement <b>dans la bulle</b> : ailleurs, le boss deviendrait un outil de
     * terraformation qui creuserait la base du joueur pendant le combat.
     */
    private void tickRupture(ServerLevel level) {
        if (tickCount % 40 != 0) {
            return;
        }
        Player target = level.getNearestPlayer(this, 16.0);
        if (target == null) {
            return;
        }
        BlockPos under = target.blockPosition().below();
        if (under.distSqr(corePos) > (long) RiftCoreBlockEntity.HARM_RADIUS * RiftCoreBlockEntity.HARM_RADIUS) {
            return;
        }
        if (!level.getBlockState(under).isAir()
            && level.getBlockState(under).getDestroySpeed(level, under) >= 0) {
            level.destroyBlock(under, false);
        }
    }

    /**
     * Phase 3 : il marche vers le noyau et se soigne s'il l'atteint. C'est la seule phase
     * où <b>reculer coûte plus cher qu'avancer</b> — le joueur doit tenir la position au
     * centre, là où la Faille est la plus dangereuse.
     */
    private void tickStabilisation(ServerLevel level) {
        if (corePos.equals(BlockPos.ZERO)) {
            return;
        }
        if (tickCount % 20 != 0) {
            return;
        }
        double d = blockPosition().distSqr(corePos);
        if (d <= 9.0) {
            heal(2.0f);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                getX(), getY() + 1.0, getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        } else {
            getNavigation().moveTo(corePos.getX() + 0.5, corePos.getY(), corePos.getZ() + 0.5, 1.0);
        }
    }

    /**
     * Victoire : la Faille est <b>définitivement</b> stable et l'extraction s'ouvre. La
     * marque vit sur le noyau et non sur l'entité — une marque portée par le boss
     * disparaîtrait avec lui, et la Faille resterait fermée pour toujours après le seul
     * combat qui devait l'ouvrir.
     */
    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel serverLevel
            && serverLevel.getBlockEntity(corePos) instanceof RiftCoreBlockEntity core) {
            core.setCleared(true);
        }
        super.die(source);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean hitByPlayer) {
        super.dropCustomDeathLoot(level, source, hitByPlayer);
        // Garanti, pas tiré au sort : c'est la matière du Rift-Ward Plate, et le boss ne
        // se rejoue pas. Un drop aléatoire sur un combat unique serait une loterie.
        spawnAtLocation(new ItemStack(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT.get(), 3));
    }

    // --- Barre de boss --------------------------------------------------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    // --- Persistance ----------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("coreX", corePos.getX());
        tag.putInt("coreY", corePos.getY());
        tag.putInt("coreZ", corePos.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        corePos = new BlockPos(tag.getInt("coreX"), tag.getInt("coreY"), tag.getInt("coreZ"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** Ni noyade ni chute : la bulle n'a pas de sol fiable, et il l'ouvre lui-même. */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    /** Portée d'apparition depuis l'Ancre. Voir {@link RiftAnchorBlockEntity}. */
    public static final int SPAWN_REACH = RiftAnchorBlockEntity.REACH;

    /** Le sol ne le porte pas mieux qu'un joueur, mais la lave et le feu ne le gênent pas. */
    @Override
    public boolean fireImmune() {
        return true;
    }
}
