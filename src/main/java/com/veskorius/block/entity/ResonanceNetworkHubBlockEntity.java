package com.veskorius.block.entity;

import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.MachinePriority;
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
 * <b>Resonance Network Hub</b> (machine #17, 05-Machines.md) : quand un champ ne fournit
 * plus assez pour tout le monde, il décide <b>qui s'arrête</b>.
 *
 * <p>Sans lui, la règle est celle de 06-Energy.md : premier servi, premier alimenté — en
 * pratique l'ordre de tick, c'est-à-dire le hasard. Une base sous-dimensionnée hoquette
 * alors de partout, et rien ne dit au joueur laquelle de ses machines il vient de perdre.
 * C'est volontairement frustrant, et c'est ce qui donne au Hub sa raison d'être.
 *
 * <p><b>Ce qu'il fait exactement : il déleste par le bas.</b> Il lit la réserve du champ
 * qui le couvre et en déduit un <b>plancher de priorité</b> — au-dessus de la moitié, tout
 * le monde passe ; entre un cinquième et la moitié, les machines de priorité basse
 * s'effacent ; en dessous, seules les hautes sont servies. La priorité de chaque machine se
 * règle au Resonance Tuner, comme tout le reste.
 *
 * <p><b>Pourquoi un plancher et non une file d'attente.</b> Le réflexe serait de trier les
 * consommateurs et de les servir dans l'ordre. Mais les machines ne demandent pas leur
 * énergie à un instant commun : chacune tire à son propre tick, sans savoir combien
 * d'autres tireront après elle. Un ordonnanceur exigerait donc de collecter toutes les
 * demandes d'abord, puis de les servir — soit un second passage sur chaque machine, chaque
 * tick, pour un résultat que le joueur ne verrait pas mieux. Le plancher donne le même
 * comportement observable (« mes machines secondaires s'arrêtent quand la réserve baisse »)
 * sans rien changer au chemin d'énergie existant.
 *
 * <p><b>La dérive.</b> Comme l'Amplificateur, il perd 1 % d'efficacité par jour et se
 * recalibre au Tuner. Ici la dérive <b>relève les seuils</b> : un Hub négligé déleste plus
 * tôt qu'il ne devrait, ce qui se voit — des machines s'arrêtent alors que la réserve
 * paraît confortable.
 */
@EventBusSubscriber(modid = com.veskorius.Veskorius.MOD_ID)
public class ResonanceNetworkHubBlockEntity extends BlockEntity {

    /** Rayon d'autorité, en blocs. Assez pour couvrir une base, pas une région. */
    public static final int RADIUS = 24;

    /** Au-dessus de ce taux de réserve, personne n'est délesté. */
    public static final double FULL_SERVICE = 0.50;

    /** En dessous de ce taux, seules les priorités hautes sont servies. */
    public static final double CRITICAL = 0.20;

    private static final int DRIFT_INTERVAL = 24000;
    private static final double DRIFT_STEP = 0.01;
    public static final double MIN_EFFICIENCY = 0.70;

    /**
     * Index des Hubs par dimension. Même raison d'être que celui des émetteurs : une
     * machine ne doit pas balayer son voisinage à chaque tick pour savoir si un Hub la
     * gouverne. Purgé au déchargement du niveau, non persisté — les Hubs se réinscrivent
     * à leur premier tick.
     */
    private static final Map<ResourceKey<Level>, Set<BlockPos>> HUBS = new ConcurrentHashMap<>();

    private double efficiency = 1.0;
    private int driftTicks;

    /** Dernier plancher calculé, pour l'afficher au clic droit. */
    private MachinePriority floor = MachinePriority.LOW;

    public ResonanceNetworkHubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_NETWORK_HUB.get(), pos, state);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            HUBS.remove(serverLevel.dimension());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ResonanceNetworkHubBlockEntity hub) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        HUBS.computeIfAbsent(level.dimension(), k -> new LinkedHashSet<>()).add(pos.immutable());
        // Le Hub est PASSIF (05-Machines.md #17) : il ne consomme rien. Il ne fait que
        // lire l'état du champ et publier une règle — facturer un arbitre serait ajouter
        // une charge au moment précis où il n'y a plus assez d'énergie.
        hub.floor = floorFor(ResonanceFieldManager.coveringSource(serverLevel, pos), hub.efficiency);
        hub.tickDrift();
    }

    /**
     * <b>La décision, isolée et pure.</b> Le taux de réserve du champ donne le plancher ;
     * la calibration resserre les seuils. Séparée du monde pour être testable — c'est la
     * seule règle du mod qui décide de l'arrêt d'une machine, et elle doit pouvoir être
     * vérifiée sans construire une base.
     */
    public static MachinePriority floorFor(@org.jetbrains.annotations.Nullable IResonanceField source,
                                           double efficiency) {
        if (source == null || source.getCapacity() <= 0) {
            // Pas de champ mesurable : le Hub n'a rien à arbitrer, il laisse passer.
            return MachinePriority.LOW;
        }
        double ratio = (double) source.getReserve() / source.getCapacity();
        // Un Hub déréglé croit la réserve plus basse qu'elle n'est, donc déleste plus tôt.
        double perceived = ratio * efficiency;
        if (perceived >= FULL_SERVICE) {
            return MachinePriority.LOW;
        }
        return perceived >= CRITICAL ? MachinePriority.NORMAL : MachinePriority.HIGH;
    }

    /**
     * Le plancher qui s'applique à {@code pos}, ou {@code null} si aucun Hub ne le couvre.
     * Appelé par chaque machine à chaque tick de puisage : parcourt l'index, jamais les
     * blocs. Nettoyage paresseux des positions qui n'hébergent plus de Hub.
     */
    @org.jetbrains.annotations.Nullable
    public static MachinePriority floorAt(ServerLevel level, BlockPos pos) {
        Set<BlockPos> set = HUBS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return null;
        }
        long radiusSqr = (long) RADIUS * RADIUS;
        for (BlockPos hubPos : set.toArray(BlockPos[]::new)) {
            if (!(level.getBlockEntity(hubPos) instanceof ResonanceNetworkHubBlockEntity hub)) {
                set.remove(hubPos);
                continue;
            }
            if (hubPos.distSqr(pos) <= radiusSqr) {
                return hub.floor;
            }
        }
        return null;
    }

    private void tickDrift() {
        if (++driftTicks < DRIFT_INTERVAL) {
            return;
        }
        driftTicks = 0;
        efficiency = Math.max(MIN_EFFICIENCY, efficiency - DRIFT_STEP);
        setChanged();
    }

    public void recalibrate() {
        efficiency = 1.0;
        driftTicks = 0;
        setChanged();
    }

    public double getEfficiency() {
        return efficiency;
    }

    public MachinePriority getFloor() {
        return floor;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("efficiency", efficiency);
        tag.putInt("driftTicks", driftTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        efficiency = tag.contains("efficiency") ? tag.getDouble("efficiency") : 1.0;
        driftTicks = tag.getInt("driftTicks");
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            Set<BlockPos> set = HUBS.get(level.dimension());
            if (set != null) {
                set.remove(worldPosition);
            }
        }
        super.setRemoved();
    }

    /** Le bloc, pour le ticker. Sans lui, le blockstate LIT ne suivrait rien. */
    public static void updateLit(Level level, BlockPos pos, BlockState state, MachinePriority floor) {
        boolean shedding = floor != MachinePriority.LOW;
        if (state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)
            && state.getValue(com.veskorius.block.FieldEmitterBlock.LIT) != shedding) {
            level.setBlock(pos, state.setValue(com.veskorius.block.FieldEmitterBlock.LIT, shedding),
                Block.UPDATE_ALL);
        }
    }
}
