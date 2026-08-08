package com.veskorius.block.entity;

import com.veskorius.energy.HarmonicBand;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Convergence Core</b> (machine #18, 05-Machines.md) — le seul <b>multi-bloc</b> du mod,
 * et la charnière T4 → T5.
 *
 * <p>Posé seul, c'est un bloc inerte. Il ne s'allume que si <b>huit</b> Resonance Relay ou
 * Harmonic Amplifier (mélange autorisé) l'entourent à exactement {@link #RING_RADIUS}
 * blocs, <b>chacun en vue directe du centre</b>. Alors il émet un champ de portée
 * {@link #RANGE} à intensité maximale, pour 12 Osc/tick.
 *
 * <p><b>Pourquoi une figure et non une recette.</b> Tout le reste du mod se fabrique dans
 * une grille ; celui-ci se <i>bâtit</i>. C'est le premier vrai chantier du jeu, et la
 * contrainte de vue directe en fait un chantier <b>ouvert</b> : on ne peut pas enfermer le
 * Core dans une boîte, il faut lui dégager huit couloirs. Le pilier « bases ouvertes, pas
 * de boîtes fermées » (06-Energy.md) cesse d'être une recommandation esthétique — ici il
 * décide si la machine fonctionne.
 *
 * <p><b>La figure est un carré, pas un cercle.</b> Huit positions à distance de Chebyshev 5 :
 * les quatre axes et les quatre coins d'un anneau de côté 11. C'est ce qu'un joueur peut
 * poser en comptant cinq blocs dans chaque direction, sans plan ni tableur. Un vrai cercle
 * euclidien aurait donné des positions diagonales à 3-4 blocs qu'on ne devine pas, et le
 * multi-bloc se serait construit à coups de captures d'écran d'un wiki.
 *
 * <p><b>Revalidé en continu</b>, pas seulement à la pose. Le dossier ne demandait qu'un
 * contrôle « au moment de la pose du dernier élément » ; ça suffirait si rien ne changeait
 * jamais, mais un creeper, un mur bâti après coup ou un relais miné laisseraient un Core
 * allumé sur une figure détruite. La vérification coûte huit parcours de segment toutes les
 * deux secondes — le prix d'un multi-bloc qui reste vrai.
 */
public class ConvergenceCoreBlockEntity extends BlockEntity implements IResonanceField {

    /** Distance de Chebyshev entre le Core et chaque élément de l'anneau. */
    public static final int RING_RADIUS = 5;

    /** Portée émise une fois la figure valide (05-Machines.md #18). */
    public static final int RANGE = 40;

    /**
     * Intensité maximale — l'<b>exception assumée</b> à l'anti-stacking (06-Energy.md).
     * C'est la première source du mod dont l'intensité diffère de 100, donc la première
     * pour laquelle la règle « la plus forte l'emporte » a un effet observable.
     */
    public static final int FIELD_STRENGTH = 1000;

    private static final int UPKEEP_OSC = 12;
    public static final int CAPACITY = 2400;
    private static final int DRAW_PER_TICK = 120;

    /** Période de revalidation de la figure, en ticks. */
    private static final int CHECK_INTERVAL = 40;

    private int buffer;
    private HarmonicBand band = HarmonicBand.FUNDAMENTAL;
    private boolean formed;
    private int checkTimer;

    /** Voir {@link ResonanceRelayBlockEntity} : garde anti-auto-alimentation. */
    private transient boolean drawing;

    public ConvergenceCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONVERGENCE_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ConvergenceCoreBlockEntity core) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (--core.checkTimer <= 0) {
            core.checkTimer = CHECK_INTERVAL;
            boolean wasFormed = core.formed;
            core.formed = isFormed(serverLevel, pos);
            if (core.formed != wasFormed) {
                core.onFormChanged(serverLevel, pos, state);
                if (core.formed) {
                    // LE SEUL MOMENT DU MOD QU'AUCUN CRITÈRE VANILLA NE SAIT DÉCRIRE.
                    // « Posséder le bloc » ne veut rien dire ici : le Cœur seul est inerte,
                    // et tout le travail est dans les huit relais posés à cinq blocs avec
                    // vue dégagée. C'est la figure qui se referme qu'on récompense, pas
                    // l'objet qu'on a fabriqué.
                    com.veskorius.advancement.ModAdvancements.awardNearby(
                        serverLevel, pos, "convergence_formed", "formed");
                }
            }
        }
        if (!core.formed) {
            // Figure incomplète : le Core sort de l'index plutôt que d'y rester inerte.
            // Une source enregistrée mais jamais active ferait perdre du temps à chaque
            // machine qui interroge le manager, pour rien.
            ResonanceFieldManager.unregister(level, pos);
            return;
        }
        ResonanceFieldManager.register(level, pos);
        core.pull(serverLevel, pos);
        core.payUpkeep();
    }

    private void onFormChanged(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)) {
            level.setBlock(pos, state.setValue(com.veskorius.block.FieldEmitterBlock.LIT, formed),
                Block.UPDATE_ALL);
        }
        level.playSound(null, pos,
            formed ? net.minecraft.sounds.SoundEvents.CONDUIT_ACTIVATE
                : net.minecraft.sounds.SoundEvents.CONDUIT_DEACTIVATE,
            net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, formed ? 1.0f : 0.6f);
        setChanged();
    }

    // --- Validation de la figure ---------------------------------------------

    /**
     * Les huit positions de l'anneau, relatives au centre. Publique et calculée une fois :
     * c'est aussi ce que le Codex et les tests doivent pouvoir citer sans le redériver —
     * une figure décrite à deux endroits est une figure qui divergera.
     */
    public static List<BlockPos> ringOffsets() {
        List<BlockPos> ring = new ArrayList<>(8);
        int r = RING_RADIUS;
        for (int[] d : new int[][] {{r, 0}, {-r, 0}, {0, r}, {0, -r},
            {r, r}, {r, -r}, {-r, r}, {-r, -r}}) {
            ring.add(new BlockPos(d[0], 0, d[1]));
        }
        return ring;
    }

    /** Vrai si les huit positions portent un relais ou un amplificateur, tous en vue. */
    public static boolean isFormed(ServerLevel level, BlockPos corePos) {
        for (BlockPos offset : ringOffsets()) {
            BlockPos at = corePos.offset(offset);
            if (!level.isLoaded(at) || !isRingElement(level.getBlockEntity(at))) {
                return false;
            }
            if (!hasLineOfSight(level, at, corePos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Relais ou amplificateur, au choix (05-Machines.md : « mélange autorisé »). On accepte
     * les deux parce que le joueur qui arrive ici a des deux, et l'obliger à uniformiser son
     * anneau serait une contrainte sans contenu.
     */
    private static boolean isRingElement(@org.jetbrains.annotations.Nullable BlockEntity be) {
        return be instanceof ResonanceRelayBlockEntity
            || be instanceof HarmonicAmplifierBlockEntity;
    }

    /**
     * Vue directe entre un élément de l'anneau et le Core : <b>aucun bloc solide entre les
     * deux</b>, les deux extrémités exclues.
     *
     * <p>Écrit comme un parcours de segment et non comme un lancer de rayon, après que
     * celui-ci a échoué. {@code level.clip} part du <i>centre</i> du bloc d'origine — or ce
     * centre est <b>à l'intérieur</b> de la forme de collision du relais comme de
     * l'amplificateur (leur fût occupe le milieu du bloc). Le rayon accrochait donc son
     * propre point de départ et la figure n'était valide dans <b>aucun</b> cas de figure.
     * Le symptôme était trompeur : le test échouait sur la pose du huitième élément, alors
     * que les huit lignes de vue étaient refusées depuis le début.
     *
     * <p>Exclure les deux extrémités est ce qui rend le contrôle correct <i>et</i> lisible :
     * on ne se demande plus si le bloc visé compte comme un obstacle, il n'est jamais
     * consulté.
     */
    private static boolean hasLineOfSight(ServerLevel level, BlockPos from, BlockPos to) {
        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);
        Vec3 delta = end.subtract(start);
        // Un pas d'un quart de bloc : assez fin pour ne sauter aucun bloc traversé, même
        // en diagonale, et sans coût mesurable sur une portée de cinq blocs.
        int steps = (int) Math.ceil(delta.length() * 4);
        for (int i = 1; i < steps; i++) {
            Vec3 p = start.add(delta.scale((double) i / steps));
            BlockPos at = BlockPos.containing(p);
            if (at.equals(from) || at.equals(to)) {
                continue;
            }
            if (!level.getBlockState(at).getCollisionShape(level, at).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFormed() {
        return formed;
    }

    // --- Énergie -------------------------------------------------------------

    private void pull(ServerLevel level, BlockPos pos) {
        int want = Math.min(CAPACITY - buffer, DRAW_PER_TICK);
        if (want <= 0) {
            return;
        }
        drawing = true;
        try {
            IResonanceField source = ResonanceFieldManager.findSource(level, pos);
            if (source != null) {
                band = source.getBand();
            }
            int drawn = ResonanceFieldManager.supply(level, pos, want);
            if (drawn > 0) {
                buffer += drawn;
                setChanged();
            }
        } finally {
            drawing = false;
        }
    }

    private void payUpkeep() {
        if (buffer <= 0) {
            return;
        }
        buffer = Math.max(0, buffer - UPKEEP_OSC);
        setChanged();
    }

    @Override
    public int getFieldStrength() {
        return FIELD_STRENGTH;
    }

    @Override
    public int getRange() {
        return formed ? RANGE : 0;
    }

    @Override
    public boolean isActive() {
        return formed && !drawing && buffer > 0;
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("buffer", buffer);
        tag.putInt("band", band.ordinal());
        tag.putBoolean("formed", formed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        buffer = tag.getInt("buffer");
        band = HarmonicBand.byIndex(tag.getInt("band"));
        formed = tag.getBoolean("formed");
        // Revalidation immédiate au chargement : la figure a pu être défaite pendant que
        // le chunk dormait, et un Core qui se croirait formé alimenterait une base entière
        // depuis une structure qui n'existe plus.
        checkTimer = 1;
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            ResonanceFieldManager.unregister(level, worldPosition);
        }
        super.setRemoved();
    }
}
