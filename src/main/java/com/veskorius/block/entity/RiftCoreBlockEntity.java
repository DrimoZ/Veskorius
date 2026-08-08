package com.veskorius.block.entity;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * <b>Noyau de Faille</b> — le cœur de la bulle, et la seule chose du mod qui blesse le
 * joueur simplement parce qu'il est là.
 *
 * <p>Non ancré, il inflige des <b>dégâts de déphasage</b> à moins de {@link #HARM_RADIUS}
 * blocs, après {@link #GRACE_TICKS} de présence (06-Energy.md). Ancré, il se tait.
 *
 * <p><b>Le délai de grâce est la mécanique, pas un adoucissement.</b> Sans lui, s'approcher
 * tuerait sans prévenir et la Faille serait un piège ; avec lui, le joueur a trois secondes
 * pour voir l'écran se déformer, comprendre, et reculer. Il peut donc <b>visiter</b> une
 * Faille non ancrée — le temps de repérer où poser l'Ancre — mais pas y séjourner. C'est
 * exactement ce que le dernier palier demande : y aller une fois, sans équipement, pour
 * savoir où revenir avec.
 *
 * <p>Le compteur est <b>par joueur</b> et remis à zéro dès qu'on sort du rayon. Un compteur
 * global punirait le second visiteur pour le temps passé par le premier.
 */
@EventBusSubscriber(modid = com.veskorius.Veskorius.MOD_ID)
public class RiftCoreBlockEntity extends BlockEntity {

    /** Rayon de nocivité, en blocs (06-Energy.md : « Faille non ancrée, &lt; 8 blocs »). */
    public static final int HARM_RADIUS = 8;

    /** Trois secondes avant le premier dégât. Voir la note de classe. */
    public static final int GRACE_TICKS = 20 * 3;

    /** 2 cœurs par seconde une fois le délai écoulé. */
    private static final float DAMAGE = 4.0f;
    private static final int DAMAGE_INTERVAL = 20;

    /**
     * Index des noyaux par dimension. Le handler de dégâts tourne sur chaque joueur à
     * chaque tick ; sans index, il balaierait un cube de blocs par joueur et par tick pour
     * découvrir presque toujours qu'il n'y a rien.
     */
    private static final Map<ResourceKey<Level>, Set<BlockPos>> CORES = new ConcurrentHashMap<>();

    /** Ticks d'exposition accumulés, par joueur. Transitoire : rien à persister. */
    private final Map<java.util.UUID, Integer> exposure = new java.util.HashMap<>();

    private boolean anchored;

    public RiftCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIFT_CORE.get(), pos, state);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            CORES.remove(serverLevel.dimension());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  RiftCoreBlockEntity core) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CORES.computeIfAbsent(level.dimension(), k -> new LinkedHashSet<>()).add(pos.immutable());
        core.tickHarm(serverLevel, pos);
        if (level.getGameTime() % 10 == 0) {
            serverLevel.sendParticles(
                core.anchored ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                core.anchored ? 2 : 8, 0.4, 0.4, 0.4, 0.02);
        }
    }

    private void tickHarm(ServerLevel level, BlockPos pos) {
        if (anchored) {
            exposure.clear();
            return;
        }
        var box = new net.minecraft.world.phys.AABB(pos).inflate(HARM_RADIUS);
        long radiusSqr = (long) HARM_RADIUS * HARM_RADIUS;
        Set<java.util.UUID> present = new java.util.HashSet<>();

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target.blockPosition().distSqr(pos) > radiusSqr) {
                continue; // AABB gonflée = cube ; on garde une portée sphérique
            }
            present.add(target.getUUID());
            int ticks = exposure.merge(target.getUUID(), 1, Integer::sum);
            if (ticks > GRACE_TICKS && (ticks - GRACE_TICKS) % DAMAGE_INTERVAL == 0) {
                target.hurt(com.veskorius.energy.ModDamageTypes.discharge(level), DAMAGE);
            }
        }
        // Sorti du rayon = compteur remis à zéro : le déphasage ne s'accumule pas d'une
        // visite à l'autre, sinon la deuxième approche serait mortelle sans raison lisible.
        exposure.keySet().retainAll(present);
    }

    /** Vrai si un Rift Anchor tient cette Faille. Écrit par l'Ancre, jamais deviné ici. */
    public boolean isAnchored() {
        return anchored;
    }

    public void setAnchored(boolean anchored) {
        if (this.anchored != anchored) {
            this.anchored = anchored;
            setChanged();
        }
    }

    /** Le noyau le plus proche dans un rayon, ou {@code null}. Sert au Rift Anchor. */
    @org.jetbrains.annotations.Nullable
    public static RiftCoreBlockEntity nearest(ServerLevel level, BlockPos from, int maxRange) {
        Set<BlockPos> set = CORES.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return null;
        }
        double bestSq = (double) maxRange * maxRange;
        RiftCoreBlockEntity best = null;
        for (BlockPos p : set.toArray(BlockPos[]::new)) {
            if (!(level.getBlockEntity(p) instanceof RiftCoreBlockEntity core)) {
                set.remove(p);
                continue;
            }
            double d = p.distSqr(from);
            if (d <= bestSq) {
                bestSq = d;
                best = core;
            }
        }
        return best;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("anchored", anchored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        anchored = tag.getBoolean("anchored");
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            Set<BlockPos> set = CORES.get(level.dimension());
            if (set != null) {
                set.remove(worldPosition);
            }
        }
        super.setRemoved();
    }
}
