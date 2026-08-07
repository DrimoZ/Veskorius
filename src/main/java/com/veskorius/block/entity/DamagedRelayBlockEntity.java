package com.veskorius.block.entity;

import com.veskorius.block.DamagedRelayBlock;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <b>Relais endommagé</b> — la pièce maîtresse du puzzle du Sigma Laboratory
 * (08-Structures.md : « réparer deux Relais, la salle centrale s'ouvre seulement si les
 * deux sont actifs simultanément »).
 *
 * <p>Réparé, il <b>rediffuse la Résonance</b> pendant {@link #UPTIME} ticks, puis retombe
 * en panne. C'est un vrai {@link IResonanceField} : il s'inscrit dans l'index du
 * {@link ResonanceFieldManager} comme n'importe quel émetteur, donc il ouvre les sas et
 * allume les conduits sans une ligne de cas particulier.
 *
 * <p><b>Comment la simultanéité est obtenue sans inventer de serrure à deux clés.</b> Le
 * réflexe serait un verrou qui compte ses sources. Il n'en a pas besoin : un relais ne se
 * répare que s'il est <b>déjà couvert par un champ actif</b>. Les deux relais sont donc
 * posés <b>en chaîne</b> — le premier dans la portée de l'émetteur encore vivant du Sigma,
 * le second dans la portée du premier seulement. Réparer le second exige que le premier
 * tourne encore : la simultanéité devient une <b>contrainte de trajet</b> (on a une minute
 * et demie pour traverser la roue), et non un compteur caché. Aucune mécanique nouvelle,
 * et le joueur comprend la règle en la vivant.
 *
 * <p><b>Pourquoi la vérification n'a lieu qu'à la réparation, jamais au tick.</b> Si
 * {@code isActive()} demandait « suis-je dans un champ ? », le manager parcourrait les
 * sources, tomberait sur l'autre relais, appellerait son {@code isActive()}… et repartirait
 * en boucle. Une récursion infinie déclenchée par deux blocs posés côte à côte, invisible
 * en relecture. Le contrôle se fait donc une fois, au clic, quand le relais n'est pas
 * encore inscrit.
 */
public class DamagedRelayBlockEntity extends BlockEntity implements IResonanceField {

    /**
     * Durée de fonctionnement après réparation : 90 s. Assez pour traverser la roue du
     * Sigma en connaissant le chemin, trop court pour flâner — c'est <b>ce chiffre</b>
     * qui fait exister le puzzle, et il est le seul curseur à toucher si le trajet se
     * révèle trop dur ou trop mou en playtest.
     */
    public static final int UPTIME = 20 * 90;

    /**
     * Portée. Plus large que le Field Emitter (8) parce qu'un relais sert justement à
     * porter plus loin — c'est la valeur annoncée pour le Resonance Relay T3
     * (05-Machines.md), reprise ici pour que la version « ruine » et la version
     * « machine » ne racontent pas deux choses différentes.
     */
    private static final int RANGE = 20;
    private static final int STRENGTH = 100;

    private int ticksLeft;

    public DamagedRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DAMAGED_RELAY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  DamagedRelayBlockEntity relay) {
        if (relay.ticksLeft <= 0) {
            return;
        }
        relay.ticksLeft--;
        ResonanceFieldManager.register(level, pos);
        if (relay.ticksLeft == 0) {
            ResonanceFieldManager.unregister(level, pos);
            level.setBlock(pos, state.setValue(DamagedRelayBlock.LIT, Boolean.FALSE),
                Block.UPDATE_ALL);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CONDUIT_DEACTIVATE,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.7f);
        }
        relay.setChanged();
    }

    /** Démarre le cycle. Le contrôle « suis-je dans un champ ? » est fait par le bloc. */
    public void repair() {
        ticksLeft = UPTIME;
        setChanged();
    }

    public boolean isRunning() {
        return ticksLeft > 0;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    // --- IResonanceField -----------------------------------------------------

    @Override
    public int getFieldStrength() {
        return STRENGTH;
    }

    @Override
    public int getRange() {
        return RANGE;
    }

    @Override
    public boolean isActive() {
        return ticksLeft > 0;
    }

    /**
     * Un relais ne stocke rien : il <b>rediffuse</b>. Il ne fournit donc aucun Osc à une
     * machine — il ouvre des sas et allume des conduits, ce qui est tout ce que le Sigma
     * lui demande. Faire de lui une source d'énergie gratuite en ferait un raccourci de
     * progression posé au milieu d'un donjon T3.
     */
    @Override
    public int extractOsc(int maxOsc) {
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TicksLeft", ticksLeft);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ticksLeft = tag.getInt("TicksLeft");
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            ResonanceFieldManager.unregister(level, worldPosition);
        }
        super.setRemoved();
    }
}
