package com.veskorius.block.entity;

import com.veskorius.energy.ResonanceFieldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <b>Rift Anchor</b> (machine #19, 05-Machines.md) : posée en bordure de Faille, elle la
 * stabilise. 20 Osc/tick, <b>en continu</b>.
 *
 * <p>C'est la machine la plus chère du mod à faire tourner, et de très loin — presque deux
 * fois le Convergence Core. Ce n'est pas un chiffre d'équilibrage arbitraire : le Core
 * existe précisément pour l'alimenter (05-Machines.md : « sert avant tout à alimenter un
 * Rift Anchor sans lui dédier une base entière de relais »). Les deux dernières machines du
 * mod forment donc une paire — on ne construit pas le monument pour la beauté du geste, on
 * le construit parce que l'Ancre le réclame.
 *
 * <p><b>Elle stabilise tant qu'elle est alimentée, et pas une seconde de plus.</b> Le
 * dossier parle de « pose unique », ce qui pourrait se lire comme un effet définitif. Ce
 * serait un piège : le joueur poserait l'Ancre, verrait la Faille se calmer, démonterait
 * son réseau, et mourrait au retour sans comprendre ce qui a changé. Ici la Faille se
 * réveille dès que le champ tombe — et le tour de grâce du noyau lui laisse trois secondes
 * pour s'en apercevoir et sortir.
 */
public class RiftAnchorBlockEntity extends BlockEntity {

    /** Portée d'ancrage. On la pose « en bordure », pas dedans. */
    public static final int REACH = 12;

    /** 20 Osc/tick (05-Machines.md #19). Le plus lourd appétit du mod. */
    private static final int OSC_PER_TICK = 20;

    private boolean holding;

    public RiftAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIFT_ANCHOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  RiftAnchorBlockEntity anchor) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        RiftCoreBlockEntity core = RiftCoreBlockEntity.nearest(serverLevel, pos, REACH);
        boolean fed = core != null
            && ResonanceFieldManager.supply(serverLevel, pos, OSC_PER_TICK) >= OSC_PER_TICK;

        if (core != null) {
            core.setAnchored(fed);
        }
        if (anchor.holding != fed) {
            anchor.holding = fed;
            anchor.setChanged();
            if (state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)) {
                level.setBlock(pos, state.setValue(com.veskorius.block.FieldEmitterBlock.LIT, fed),
                    Block.UPDATE_ALL);
            }
        }
    }

    /** Vrai si l'Ancre tient effectivement une Faille en ce moment. */
    public boolean isHolding() {
        return holding;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("holding", holding);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        holding = tag.getBoolean("holding");
    }

    @Override
    public void setRemoved() {
        // L'Ancre retirée relâche SA Faille immédiatement. Sans ça, casser l'Ancre
        // laisserait un noyau ancré pour toujours : la Faille la plus dangereuse du monde
        // deviendrait inoffensive au premier coup de pioche.
        if (level instanceof ServerLevel serverLevel) {
            RiftCoreBlockEntity core = RiftCoreBlockEntity.nearest(serverLevel, worldPosition, REACH);
            if (core != null) {
                core.setAnchored(false);
            }
        }
        super.setRemoved();
    }
}
