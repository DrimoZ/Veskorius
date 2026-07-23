package com.veskorius.block.entity;

import com.veskorius.config.HarmonicsConfig;
import com.veskorius.energy.HarmonicBand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Émetteur Accordable (06-Energy.md, courbe d'introduction) : un Field Emitter dont on
 * <b>choisit la bande harmonique</b>, au Resonance Tuner (mode « Accorder »).
 *
 * <p>C'est lui qui introduit le <b>choix</b> de fréquence — le Field Emitter T2 de base
 * reste volontairement mono-bande, pour que le T2 n'impose aucune décision. Son intérêt
 * n'est pas d'éviter une pénalité, c'est de <b>router l'énergie</b> : deux émetteurs qui
 * se chevauchent sur deux bandes alimentent deux groupes de machines distincts au même
 * endroit, sans un seul fil.
 *
 * <p>Tout le reste (réserve, carburant, GUI, coupole) est hérité tel quel.
 */
public class TunableFieldEmitterBlockEntity extends FieldEmitterBlockEntity {

    private HarmonicBand band = HarmonicBand.FUNDAMENTAL;

    public TunableFieldEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUNABLE_FIELD_EMITTER.get(), pos, state);
    }

    @Override
    public HarmonicBand getBand() {
        return band;
    }

    public void setBand(HarmonicBand band) {
        this.band = band;
        setChanged();
    }

    /** Fait défiler la bande, bornée par le nombre de bandes actives en config. */
    public void cycleBand() {
        setBand(band.next(HarmonicsConfig.bandCount()));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.tunable_field_emitter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("band", (byte) band.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        band = HarmonicBand.byIndex(tag.getByte("band"));
    }
}
