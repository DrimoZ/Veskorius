package com.veskorius.block.entity;

import com.veskorius.energy.ResonanceFieldManager;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * <b>Rift Ward Emitter</b> (machine #21, 05-Machines.md) : annule la corrosion ambiante
 * dans un rayon de {@link #RADIUS} blocs, 5 Osc/tick.
 *
 * <p>Il n'a de sens que parce que la corrosion existe — et elle n'existait pas : le terme
 * n'apparaissait qu'une fois dans tout le dossier, sans définition. Voir
 * {@link RiftCoreBlockEntity#CORROSION_RADIUS}. C'est ce qui transforme un site de Faille
 * ancrée en <b>site exploitable</b> : sans Ward on peut y entrer, avec on peut y travailler.
 *
 * <p><b>Le rayon du Ward est celui de la corrosion.</b> Ni plus — il protégerait des zones
 * qui n'en ont pas besoin et on le poserait n'importe où — ni moins : un Ward qui ne
 * couvrirait pas toute la portée de sa Faille laisserait un anneau rongé autour du site,
 * et le joueur ne saurait jamais où se tenir.
 */
@EventBusSubscriber(modid = com.veskorius.Veskorius.MOD_ID)
public class RiftWardEmitterBlockEntity extends BlockEntity {

    /** Exactement le rayon de la corrosion : un Ward couvre une Faille, pas une fraction. */
    public static final int RADIUS = RiftCoreBlockEntity.CORROSION_RADIUS;

    private static final int OSC_PER_TICK = 5;

    /** Index par dimension, pour que la corrosion n'ait pas à balayer des blocs. */
    private static final Map<ResourceKey<Level>, Set<BlockPos>> WARDS = new ConcurrentHashMap<>();

    private boolean active;

    public RiftWardEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIFT_WARD_EMITTER.get(), pos, state);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WARDS.remove(serverLevel.dimension());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  RiftWardEmitterBlockEntity ward) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        WARDS.computeIfAbsent(level.dimension(), k -> new LinkedHashSet<>()).add(pos.immutable());
        boolean fed = ResonanceFieldManager.supply(serverLevel, pos, OSC_PER_TICK) >= OSC_PER_TICK;
        if (ward.active != fed) {
            ward.active = fed;
            ward.setChanged();
            if (state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)) {
                level.setBlock(pos, state.setValue(com.veskorius.block.FieldEmitterBlock.LIT, fed),
                    Block.UPDATE_ALL);
            }
        }
    }

    /** Vrai si un Ward <b>alimenté</b> couvre cette position. Lu par la corrosion. */
    public static boolean isWarded(ServerLevel level, BlockPos pos) {
        Set<BlockPos> set = WARDS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return false;
        }
        long radiusSqr = (long) RADIUS * RADIUS;
        for (BlockPos wardPos : set.toArray(BlockPos[]::new)) {
            if (!(level.getBlockEntity(wardPos) instanceof RiftWardEmitterBlockEntity ward)) {
                set.remove(wardPos);
                continue;
            }
            if (ward.active && wardPos.distSqr(pos) <= radiusSqr) {
                return true;
            }
        }
        return false;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        active = tag.getBoolean("active");
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            Set<BlockPos> set = WARDS.get(level.dimension());
            if (set != null) {
                set.remove(worldPosition);
            }
        }
        super.setRemoved();
    }
}
