package com.veskorius.block.entity;

import com.veskorius.energy.HarmonicBand;
import com.veskorius.energy.IResonanceField;
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
 * <b>Harmonic Amplifier</b> (machine #14, 05-Machines.md) : <b>double la portée du champ
 * qu'il reçoit</b>, 2 Osc/tick prélevés, trois au maximum en chaîne.
 *
 * <p>Il partage l'architecture du {@link ResonanceRelayBlockEntity} — tampon interne, seul
 * le tick parle au {@link ResonanceFieldManager}, drapeau anti-auto-alimentation — et pour
 * les mêmes raisons, qui ne sont pas négociables : sans elles, deux amplificateurs à portée
 * l'un de l'autre partent en récursion infinie. Ce qui les sépare tient en une phrase :
 *
 * <p><b>le relais reporte une portée, l'amplificateur la multiplie.</b> Le relais a une
 * portée fixe de 20 ; celle de l'amplificateur dépend de ce qu'il reçoit. Poser un
 * amplificateur derrière un émetteur T2 donne 16 blocs ; le poser derrière un relais en
 * donne 40. C'est ce qui en fait un outil de <b>réseau régional</b> plutôt qu'un relais un
 * peu meilleur, et ce qui justifie qu'il coûte un Treillis Harmonique.
 *
 * <p><b>Trois au maximum, et la limite se compte au bout de la chaîne, pas au début.</b>
 * Chaque amplificateur retient la profondeur de celui qui l'alimente et y ajoute un ; passé
 * {@link #MAX_CHAIN}, il transporte encore le champ mais <b>cesse de doubler</b>. Sans ce
 * plafond, dix amplificateurs en file donneraient une portée de plusieurs milliers de blocs
 * pour le prix d'un Osc — et le réseau cesserait d'être une contrainte de terrain, ce qui
 * est précisément ce que le mod demande au joueur de résoudre.
 *
 * <p><b>La dérive de calibration.</b> Il perd 1 % d'efficacité par jour Minecraft de
 * fonctionnement, plancher {@link #MIN_EFFICIENCY}. Ce n'est pas une taxe : c'est le
 * <b>même geste</b> que la dissonance d'un cran au-dessus (06-Energy.md, « la Résonance se
 * désaccorde à l'usage »), et il se soigne au Resonance Tuner comme le reste. Un
 * amplificateur oublié depuis un mois porte moins loin qu'au premier jour, et ça se voit sur
 * la coupole avant de se voir dans une machine à l'arrêt.
 */
public class HarmonicAmplifierBlockEntity extends BlockEntity implements IResonanceField {

    /** Facteur de portée (06-Energy.md : « ×2 par amplificateur »). */
    public static final int GAIN = 2;

    /** Trois en chaîne, soit ≈ 120 blocs effectifs au maximum depuis un relais. */
    public static final int MAX_CHAIN = 3;

    /** 2 Osc/tick prélevés, comme annoncé. Deux fois le relais : la portée se paie. */
    private static final int UPKEEP_OSC = 2;

    public static final int CAPACITY = 400;
    private static final int DRAW_PER_TICK = 40;
    private static final int FIELD_STRENGTH = 100;

    /** Dérive : −1 % par jour Minecraft (24 000 ticks) de fonctionnement effectif. */
    private static final int DRIFT_INTERVAL = 24000;
    private static final double DRIFT_STEP = 0.01;

    /** Plancher de la dérive : −30 %. Un amplificateur négligé faiblit, il ne meurt pas. */
    public static final double MIN_EFFICIENCY = 0.70;

    private int buffer;
    private HarmonicBand band = HarmonicBand.FUNDAMENTAL;
    private boolean relayedUnstable;

    /** Portée reçue au dernier remplissage. 0 = pas de source, donc rien à amplifier. */
    private int sourceRange;

    /** Profondeur dans la chaîne d'amplificateurs. 0 = premier après une vraie source. */
    private int chainDepth;

    private double efficiency = 1.0;
    private int driftTicks;

    /** Voir {@link ResonanceRelayBlockEntity} : garde anti-auto-alimentation. */
    private transient boolean drawing;

    public HarmonicAmplifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HARMONIC_AMPLIFIER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  HarmonicAmplifierBlockEntity amp) {
        ResonanceFieldManager.register(level, pos);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        amp.pull(serverLevel, pos);
        amp.payUpkeep();
        amp.updateLit(level, pos, state);
    }

    private void pull(ServerLevel level, BlockPos pos) {
        int want = Math.min(CAPACITY - buffer, DRAW_PER_TICK);
        if (want <= 0) {
            return;
        }
        drawing = true;
        try {
            IResonanceField source = ResonanceFieldManager.findSource(level, pos);
            if (source == null) {
                // Plus de source : on cesse d'annoncer une portée qu'on ne tient plus.
                sourceRange = 0;
                chainDepth = 0;
                relayedUnstable = false;
                return;
            }
            band = source.getBand();
            relayedUnstable = source.isUnstable();
            sourceRange = source.getRange();
            // Un amplificateur alimenté par un amplificateur s'enfonce d'un cran ; toute
            // autre source le remet à zéro. La profondeur suit donc le trajet réel de
            // l'énergie, et se recalcule à chaque tick — déplacer un maillon la corrige
            // sans qu'on ait à propager quoi que ce soit.
            chainDepth = source instanceof HarmonicAmplifierBlockEntity up ? up.chainDepth + 1 : 0;

            int drawn = ResonanceFieldManager.supply(level, pos, want);
            if (drawn > 0) {
                buffer += drawn;
            }
            setChanged();
        } finally {
            drawing = false;
        }
    }

    private void payUpkeep() {
        if (buffer <= 0) {
            return;
        }
        buffer = Math.max(0, buffer - UPKEEP_OSC);
        tickDrift();
        setChanged();
    }

    /** La dérive ne court que quand l'appareil <b>fonctionne</b> : à sec, il ne s'use pas. */
    private void tickDrift() {
        if (++driftTicks < DRIFT_INTERVAL) {
            return;
        }
        driftTicks = 0;
        efficiency = Math.max(MIN_EFFICIENCY, efficiency - DRIFT_STEP);
    }

    /** Remet la calibration à neuf. Appelé par le Resonance Tuner, contre un Component. */
    public void recalibrate() {
        efficiency = 1.0;
        driftTicks = 0;
        setChanged();
    }

    public double getEfficiency() {
        return efficiency;
    }

    public int getChainDepth() {
        return chainDepth;
    }

    private void updateLit(Level level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)) {
            return;
        }
        boolean carrying = buffer > 0 && sourceRange > 0;
        if (state.getValue(com.veskorius.block.FieldEmitterBlock.LIT) != carrying) {
            level.setBlock(pos, state.setValue(com.veskorius.block.FieldEmitterBlock.LIT, carrying),
                Block.UPDATE_ALL);
        }
    }

    // --- IResonanceField -----------------------------------------------------

    @Override
    public int getFieldStrength() {
        return FIELD_STRENGTH;
    }

    /**
     * La portée reçue, doublée — sauf au bout de la chaîne, où l'appareil se contente de
     * la reporter. Décision pure isolée dans {@link #amplify} pour être testable sans monde.
     */
    @Override
    public int getRange() {
        return amplify(sourceRange, chainDepth, efficiency);
    }

    /**
     * {@code sourceRange} × 2 si la chaîne n'est pas saturée, sinon {@code sourceRange} tel
     * quel ; le tout pondéré par la calibration. Le gain seul est atténué, jamais la portée
     * reçue : un amplificateur déréglé doit rester <b>au moins aussi bon qu'un fil</b>,
     * sinon en poser un pourrait réduire la couverture, ce qui serait incompréhensible.
     */
    public static int amplify(int sourceRange, int chainDepth, double efficiency) {
        if (sourceRange <= 0) {
            return 0;
        }
        if (chainDepth >= MAX_CHAIN) {
            return sourceRange;
        }
        int gain = sourceRange * (GAIN - 1);
        return sourceRange + (int) Math.round(gain * efficiency);
    }

    @Override
    public boolean isActive() {
        if (drawing) {
            return false;
        }
        return buffer > 0 && sourceRange > 0
            && !(isUnstable() && level != null && level.getGameTime() % 4 < 2);
    }

    @Override
    public int extractOsc(int maxOsc) {
        int drawn = Math.min(maxOsc, buffer);
        if (drawn > 0) {
            buffer -= drawn;
            setChanged();
        }
        return drawn;
    }

    @Override
    public int getReserve() {
        return buffer;
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    public HarmonicBand getBand() {
        return band;
    }

    @Override
    public boolean isUnstable() {
        return com.veskorius.config.HarmonicsConfig.enabled() && relayedUnstable;
    }

    // --- Persistance ---------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("buffer", buffer);
        tag.putInt("band", band.ordinal());
        tag.putInt("sourceRange", sourceRange);
        tag.putInt("chainDepth", chainDepth);
        tag.putDouble("efficiency", efficiency);
        tag.putInt("driftTicks", driftTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        buffer = tag.getInt("buffer");
        band = HarmonicBand.byIndex(tag.getInt("band"));
        sourceRange = tag.getInt("sourceRange");
        chainDepth = tag.getInt("chainDepth");
        // Un monde d'avant la dérive n'a pas la clé : 1.0 plutôt que 0.0, sinon un
        // amplificateur déjà posé perdrait tout son gain au chargement.
        efficiency = tag.contains("efficiency") ? tag.getDouble("efficiency") : 1.0;
        driftTicks = tag.getInt("driftTicks");
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            ResonanceFieldManager.unregister(level, worldPosition);
        }
        super.setRemoved();
    }
}
