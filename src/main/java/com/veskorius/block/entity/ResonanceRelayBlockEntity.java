package com.veskorius.block.entity;

import com.veskorius.config.HarmonicsConfig;
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
 * <b>Resonance Relay</b> (machine #9, 05-Machines.md) : portée 20, chaînable, 1 Osc/tick.
 *
 * <p>C'est la première machine du mod dont le rôle est <b>purement topologique</b>. Elle ne
 * transforme rien : elle prend un champ là où il existe et le fait exister vingt blocs plus
 * loin. Le pilier « le réseau est vivant, jamais un tuyau » (01-Vision-Pillars.md) tient
 * justement à ce qu'on n'ait jamais posé de câble ; le relais est la réponse au problème que
 * le câble résout ailleurs, et il la donne sans câble — on <b>déplace une couverture</b>, on
 * ne raccorde pas deux points.
 *
 * <p><b>Un relais est un tampon, pas un fil.</b> Le réflexe serait de faire suivre la demande
 * en amont au moment où elle arrive : une machine puise dans le relais, le relais puise dans
 * l'émetteur, et ainsi de suite. Ce réflexe est un piège. {@link ResonanceFieldManager#supply}
 * appelle {@link #extractOsc} sur chaque source candidate ; si {@code extractOsc} rappelait le
 * manager, deux relais à portée l'un de l'autre — c'est-à-dire <b>la configuration normale
 * d'une chaîne</b> — se renverraient l'appel indéfiniment. Une récursion infinie déclenchée
 * par la pose de deux blocs, invisible en relecture, et qui ne se manifeste que sur une base
 * déjà construite. C'est exactement le piège qui avait déjà été identifié sur le relais
 * endommagé du Sigma ({@link DamagedRelayBlockEntity}), résolu là-bas en ne vérifiant qu'au
 * clic ; ici on ne peut pas se contenter d'une vérification ponctuelle, puisque le relais doit
 * transporter de l'énergie en continu.
 *
 * <p>La sortie est donc l'inversion du sens : <b>seul le tick appelle le manager</b>, jamais
 * {@code extractOsc}. Le relais se remplit tout seul à chaque tick jusqu'à {@link #CAPACITY},
 * et sert ses consommateurs sur ce tampon. Le graphe d'appels est plat par construction, quelle
 * que soit la longueur ou la forme de la chaîne — y compris circulaire.
 *
 * <p><b>Le drapeau {@link #drawing}.</b> Un relais est enregistré dans l'index, et il est
 * évidemment à portée de lui-même. Sans garde, le premier candidat que le manager lui
 * renverrait pendant son propre remplissage serait <b>lui-même</b> : il se servirait dans son
 * propre tampon, le manager s'arrêterait là (il sert depuis une seule source), et le relais ne
 * se chargerait jamais. Panne silencieuse et totale. Pendant son remplissage il se déclare
 * donc inactif, ce que le manager sait déjà sauter.
 *
 * <p><b>Ce qu'il transporte en plus de l'énergie.</b> Un relais qui ne relaierait que des Osc
 * serait une blanchisseuse d'harmoniques : il suffirait d'en intercaler un pour qu'une machine
 * désaccordée cesse de l'être, la {@link #getBand() bande} du relais étant par défaut la
 * Fondamentale. Il rediffuse donc la bande de sa source, et <b>renvoie en amont</b> la
 * dissonance qu'on lui injecte — un relais ne fait pas disparaître la saleté, il la ramène là
 * où elle se voit. Il propage de même l'instabilité : une chaîne branchée sur un champ pollué
 * hoquette sur toute sa longueur.
 */
public class ResonanceRelayBlockEntity extends BlockEntity implements IResonanceField {

    /**
     * Portée rediffusée, en blocs (05-Machines.md #9). Deux fois et demie celle de
     * l'émetteur : un relais doit visiblement <b>valoir le détour</b>, sinon on préfère
     * poser un second émetteur, qui coûte moins cher et ne consomme rien en continu.
     */
    public static final int RANGE = 20;

    /**
     * Entretien : 1 Osc/tick, prélevé sur le tampon, qu'on tire ou non dessus. C'est le
     * prix de la portée et il doit se sentir — 1200 Osc/minute, soit un peu moins d'un
     * cristal stable toutes les trois minutes et demie pour un relais qui ne sert à rien.
     * Un réseau de relais oubliés se paie ; un réseau utile s'assume.
     */
    private static final int UPKEEP_OSC = 1;

    /**
     * Tampon volontairement <b>petit</b>. Ce n'est pas une batterie — la Resonance Storage
     * Cell existe pour ça, et un relais capable de stocker aurait fait d'elle un objet
     * inutile. 200 Osc, c'est dix secondes d'entretien : de quoi lisser un émetteur qui se
     * recharge, pas de quoi tenir un chantier.
     */
    public static final int CAPACITY = 200;

    /** Débit de remplissage. Plafonné pour qu'une chaîne se remplisse de proche en proche. */
    private static final int DRAW_PER_TICK = 20;

    private static final int FIELD_STRENGTH = 100;

    private int buffer;

    /** Bande rediffusée : celle de la dernière source servie. Voir l'en-tête de classe. */
    private HarmonicBand band = HarmonicBand.FUNDAMENTAL;

    /** Dissonance reçue en aval, en attente d'être renvoyée à la source. */
    private int pendingDissonance;

    /** Instabilité constatée en amont au dernier remplissage. */
    private boolean relayedUnstable;

    /** Voir l'en-tête de classe : garde anti-auto-alimentation, transitoire (non persistée). */
    private transient boolean drawing;

    public ResonanceRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_RELAY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ResonanceRelayBlockEntity relay) {
        // Idempotent, comme pour l'émetteur : couvre le premier tick après un
        // rechargement de chunk sans dépendre de l'ordre des callbacks de cycle de vie.
        ResonanceFieldManager.register(level, pos);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        relay.pull(serverLevel, pos);
        relay.payUpkeep();
        relay.updateLit(level, pos, state);
    }

    /**
     * Remplit le tampon depuis le champ amont. <b>Seul point du relais qui parle au
     * manager</b> — voir l'en-tête de classe pour pourquoi ce n'est pas négociable.
     */
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
                relayedUnstable = source.isUnstable();
                if (pendingDissonance > 0) {
                    // On ne l'absorbe pas : on la renvoie où elle se voit.
                    source.addDissonance(pendingDissonance);
                    pendingDissonance = 0;
                }
            } else {
                relayedUnstable = false;
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

    /** L'entretien se paie même sans consommateur : la portée coûte, tout le temps. */
    private void payUpkeep() {
        if (buffer <= 0) {
            return;
        }
        buffer = Math.max(0, buffer - UPKEEP_OSC);
        setChanged();
    }

    /**
     * Reflète « il reste de quoi rediffuser » dans le blockstate. Branché sur le tampon et
     * non sur {@link #isActive()}, pour la même raison que l'émetteur : un champ instable
     * bascule toutes les deux ticks, et un {@code setBlock} à ce rythme recalculerait la
     * lumière en boucle.
     */
    private void updateLit(Level level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)) {
            return;
        }
        boolean carrying = buffer > 0;
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

    @Override
    public int getRange() {
        return RANGE;
    }

    @Override
    public boolean isActive() {
        if (drawing) {
            return false; // Voir l'en-tête de classe.
        }
        return buffer > 0
            && !(isUnstable() && level != null && level.getGameTime() % 4 < 2);
    }

    /**
     * Sert depuis le tampon, et <b>uniquement</b> depuis le tampon. Aucun appel au manager
     * ici, jamais : c'est l'invariant qui tient tout le reste debout.
     */
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
    public int getDissonance() {
        return pendingDissonance;
    }

    /** Mise en attente : elle repartira en amont au prochain remplissage. */
    @Override
    public void addDissonance(int amount) {
        if (amount == 0) {
            return;
        }
        pendingDissonance = Math.clamp(pendingDissonance + amount, 0,
            HarmonicsConfig.dissonanceCapacity());
        setChanged();
    }

    /** L'instabilité se propage : une chaîne branchée sur un champ pollué hoquette entière. */
    @Override
    public boolean isUnstable() {
        return HarmonicsConfig.enabled() && relayedUnstable;
    }

    // --- Persistance et cycle de vie -----------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("buffer", buffer);
        tag.putInt("band", band.ordinal());
        tag.putInt("pendingDissonance", pendingDissonance);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        buffer = tag.getInt("buffer");
        band = HarmonicBand.byIndex(tag.getInt("band"));
        pendingDissonance = tag.getInt("pendingDissonance");
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            ResonanceFieldManager.unregister(level, worldPosition);
        }
        super.setRemoved();
    }
}
