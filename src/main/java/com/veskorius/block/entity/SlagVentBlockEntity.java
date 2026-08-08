package com.veskorius.block.entity;

import com.veskorius.energy.ResonanceFieldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <b>Slag Vent</b> (machine #13, 05-Machines.md) : passif, évacue 1 Flux Slag toutes les
 * 10 s dans chaque Veskorian Alloy Forge située à 8 blocs ou moins. 1 Osc/tick.
 *
 * <p><b>Il n'existe que parce que la Forge se bloque.</b> La scorie sort à chaque cycle
 * dans son propre slot, et un slot plein arrête la forge ; sans réponse à ce blocage, le
 * joueur passerait sa partie à vider un slot à la main, ou brancherait un tuyau vers un
 * coffre à jeter — et le déchet du T3 redeviendrait une formalité. Le Vent est la réponse
 * assumée : il fait <b>disparaître</b> la scorie, et il la fait payer.
 *
 * <p>Ce qu'il coûte n'est pas anodin. Il ne tient qu'un <b>seul cycle par période</b> et
 * par forge : une batterie de six forges saturera un Vent, il en faudra plusieurs, et
 * chacun consomme du champ en continu. Évacuer ses déchets est une ligne de budget
 * énergétique permanente, pas un bouton qu'on presse une fois. C'est exactement la leçon
 * que 02-Lore.md attribue à l'Effondrement, jouée plutôt qu'écrite.
 *
 * <p><b>Il ne sait évacuer que la scorie</b>, jamais le résidu du Structural Synthesizer.
 * Deux déchets, un seul exutoire : le second reste un problème ouvert, et c'est voulu — un
 * palier qui résout tous ses déchets d'un coup n'a plus rien à dire au palier suivant.
 *
 * <p><b>Coût prélevé d'un coup à l'évacuation</b>, pas tick par tick. Un Vent au repos ne
 * consomme rien : facturer un appareil qui n'a rien à évacuer serait un impôt invisible sur
 * une base bien rangée. L'économie annoncée est respectée à l'Osc près — la période fait
 * exactement {@link #VENT_PERIOD} ticks et le prélèvement vaut autant d'Osc.
 */
public class SlagVentBlockEntity extends BlockEntity {

    /** 10 s (05-Machines.md #13). C'est aussi la période de balayage : voir la note. */
    public static final int VENT_PERIOD = 20 * 10;

    /** Portée d'action, en blocs. Volontairement la même que le Field Emitter (8). */
    public static final int RADIUS = 8;

    /** 1 Osc/tick sur la période, prélevé en une fois. */
    private static final int OSC_PER_VENT = VENT_PERIOD;

    private int timer;

    /** Nombre de forges servies au dernier passage. Pilote la façade allumée. */
    private int lastVented;

    public SlagVentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLAG_VENT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SlagVentBlockEntity vent) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (++vent.timer < VENT_PERIOD) {
            return;
        }
        vent.timer = 0;
        vent.vent(serverLevel, pos, state);
    }

    private void vent(ServerLevel level, BlockPos pos, BlockState state) {
        // On cherche AVANT de payer : un Vent sans rien à faire ne coûte rien.
        java.util.List<VeskorianAlloyForgeBlockEntity> forges = nearbyLoadedForges(level, pos);
        if (forges.isEmpty()) {
            setLit(level, pos, state, 0);
            return;
        }
        if (ResonanceFieldManager.supply(level, pos, OSC_PER_VENT) < OSC_PER_VENT) {
            setLit(level, pos, state, 0);
            return;
        }

        int vented = 0;
        for (VeskorianAlloyForgeBlockEntity forge : forges) {
            if (!forge.getInventory()
                .extractItem(VeskorianAlloyForgeBlockEntity.SLOT_SLAG, 1, false).isEmpty()) {
                vented++;
            }
        }
        setLit(level, pos, state, vented);
        if (vented > 0) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.02);
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.6f);
        }
    }

    /**
     * Les forges chargées dans le rayon, <b>et qui ont effectivement de la scorie</b>.
     *
     * <p>On passe par {@link Level#getBlockEntity} sur un cube de positions plutôt que par
     * un scan de chunks : le rayon est de 8, soit 4913 positions consultées une fois toutes
     * les 10 s, et une position hors chunk chargé rend simplement {@code null} — un Vent ne
     * doit pas forcer le chargement de la base d'à côté.
     */
    private static java.util.List<VeskorianAlloyForgeBlockEntity> nearbyLoadedForges(
        ServerLevel level, BlockPos pos) {
        java.util.List<VeskorianAlloyForgeBlockEntity> found = new java.util.ArrayList<>();
        long radiusSqr = (long) RADIUS * RADIUS;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RADIUS, -RADIUS, -RADIUS),
            pos.offset(RADIUS, RADIUS, RADIUS))) {
            if (p.distSqr(pos) > radiusSqr || !level.isLoaded(p)) {
                continue;
            }
            if (level.getBlockEntity(p) instanceof VeskorianAlloyForgeBlockEntity forge
                && !forge.getInventory()
                    .getStackInSlot(VeskorianAlloyForgeBlockEntity.SLOT_SLAG).isEmpty()) {
                found.add(forge);
            }
        }
        return found;
    }

    private void setLit(Level level, BlockPos pos, BlockState state, int vented) {
        lastVented = vented;
        boolean lit = vented > 0;
        if (state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)
            && state.getValue(com.veskorius.block.FieldEmitterBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(com.veskorius.block.FieldEmitterBlock.LIT, lit),
                Block.UPDATE_ALL);
        }
        setChanged();
    }

    /** Pour l'infobulle au clic droit : « j'ai servi N forge(s) au dernier passage ». */
    public int getLastVented() {
        return lastVented;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("timer", timer);
        tag.putInt("lastVented", lastVented);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        timer = tag.getInt("timer");
        lastVented = tag.getInt("lastVented");
    }
}
