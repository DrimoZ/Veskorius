package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.config.VeskoriusConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * <b>Orage de Résonance</b> (07-World-Generation.md) — le seul événement météo du mod.
 *
 * <p>Un tirage tous les cinq à sept jours Minecraft ; s'il passe, l'orage dure dix minutes
 * et sème des cratères météoriques sur les blocs de surface exposés autour des joueurs.
 * <b>Tout ce qui n'est pas ramassé avant la fin disparaît.</b> Le dossier y insiste : « aucun
 * stock à faire indéfiniment, juste une fenêtre à saisir ». C'est ce qui en fait une chasse
 * et pas un gisement.
 *
 * <p><b>Il ne se déclenche qu'après le palier 3</b>, et la condition est littérale : au moins
 * un joueur connecté doit avoir décroché l'advancement du T3. Le lore le justifie — « les
 * premiers signes de l'Effondrement ne prennent sens qu'une fois le Sigma visité » — mais la
 * raison pratique compte autant : un orage qui tomberait la première nuit ne serait qu'un
 * effet météo inexpliqué, et le fragment qu'il laisse n'aurait aucun usage avant des heures.
 *
 * <p><b>L'état est persisté.</b> Un orage en cours qui s'évaporerait au redémarrage du
 * serveur laisserait derrière lui des cratères que plus rien ne viendrait nettoyer — donc
 * un stock permanent, exactement ce que le design refuse. Le compteur vit dans une
 * {@link SavedData} de la dimension.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID)
public final class ResonanceStormHandler {

    // DURÉE, FRÉQUENCE ET RAYON VIENNENT DE LA CONFIG (14-Configuration.md). L'orage
    // est le seul événement aléatoire du mod : c'est précisément celui qu'un serveur
    // peut vouloir calmer ou intensifier, et ses valeurs étaient les seules du mod à
    // n'être réglables par personne.
    //
    // La cadence de semis, elle, reste en dur : ce n'est pas un réglage d'équilibrage
    // mais un pas de simulation. L'exposer inviterait à le baisser, et à balayer la
    // heightmap vingt fois par seconde pour un gain nul.
    private static final int SEED_INTERVAL = 40;
    private static final int SEEDS_PER_PASS = 2;

    /** L'advancement qui ouvre l'événement. */
    private static final ResourceLocation TIER3 =
        ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "tier3_relay");

    private ResonanceStormHandler() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        StormState state = StormState.get(level);
        if (state.remaining > 0) {
            tickStorm(level, state);
            return;
        }
        if (level.getGameTime() % VeskoriusConfig.stormRollInterval() == 0
            && anyoneReachedTierThree(level)
            && level.random.nextInt(VeskoriusConfig.stormRollChance()) == 0) {
            start(level, state);
        }
    }

    private static void start(ServerLevel level, StormState state) {
        state.remaining = VeskoriusConfig.stormDurationTicks();
        state.setDirty();
        for (ServerPlayer player : level.players()) {
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.WEATHER, 1.0f, 0.6f);
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("message.veskorius.storm_begins"));
        }
    }

    private static void tickStorm(ServerLevel level, StormState state) {
        state.remaining--;
        if (state.remaining <= 0) {
            end(level, state);
            return;
        }
        // MARQUÉ SALE UNIQUEMENT AUX PASSES DE SEMIS, pas à chaque tick. Le premier jet
        // appelait setDirty() vingt fois par seconde pendant dix minutes : l'état était
        // re-sérialisé à chaque sauvegarde du monde sans que rien n'ait changé d'utile.
        // Perdre jusqu'à deux secondes de compteur sur un redémarrage n'a aucune
        // conséquence ; les positions semées, elles, sont enregistrées à la passe même.
        if (state.remaining % SEED_INTERVAL == 0) {
            for (ServerPlayer player : level.players()) {
                seedAround(level, state, player.blockPosition());
            }
            state.setDirty();
        }
    }

    /**
     * Fin de l'orage : <b>tout cratère encore au sol disparaît</b>.
     *
     * <p><b>On retire ce qu'on a POSÉ, pas ce qu'on retrouve.</b> Le premier jet balayait
     * une boîte de 97×65×97 autour de chaque joueur pour y chercher des cratères — six cent
     * onze mille lectures de bloc, sur le fil du serveur, dans un seul tick, et autant de
     * fois qu'il y a de joueurs. Un gel garanti à chaque fin d'orage.
     *
     * <p>Les positions semées sont donc mémorisées. Le nettoyage devient proportionnel au
     * nombre de cratères réellement posés — quelques centaines — au lieu du volume fouillé.
     * Et il devient EXACT : le balayage laissait derrière lui tout cratère posé dans un
     * chunk depuis déchargé, ce qui constituait exactement le stock permanent que
     * l'événement existe pour empêcher.
     */
    private static void end(ServerLevel level, StormState state) {
        state.remaining = 0;
        for (long packed : state.craters) {
            BlockPos pos = BlockPos.of(packed);
            // isLoaded d'abord : lire un bloc dans un chunk déchargé le ferait charger,
            // ce qui rendrait le nettoyage plus coûteux que le balayage qu'on remplace.
            if (level.isLoaded(pos) && level.getBlockState(pos).is(ModBlocks.METEORIC_CRATER.get())) {
                level.removeBlock(pos, false);
            }
        }
        state.craters.clear();
        state.setDirty();
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("message.veskorius.storm_ends"));
        }
    }

    /** Sème quelques cratères sur des blocs de surface exposés, au hasard. */
    private static void seedAround(ServerLevel level, StormState state, BlockPos centre) {
        int radius = VeskoriusConfig.stormSeedRadius();
        for (int i = 0; i < SEEDS_PER_PASS; i++) {
            int dx = level.random.nextInt(radius * 2) - radius;
            int dz = level.random.nextInt(radius * 2) - radius;
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                centre.offset(dx, 0, dz));
            if (!level.isLoaded(ground) || !level.canSeeSky(ground)) {
                continue;
            }
            if (!level.getBlockState(ground).isAir()
                || !level.getBlockState(ground.below()).isFaceSturdy(level, ground.below(),
                    net.minecraft.core.Direction.UP)) {
                continue;
            }
            level.setBlockAndUpdate(ground, ModBlocks.METEORIC_CRATER.get().defaultBlockState());
            state.craters.add(ground.asLong());
            level.playSound(null, ground, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.WEATHER, 0.6f, 1.2f);
        }
    }

    private static boolean anyoneReachedTierThree(ServerLevel level) {
        var advancement = level.getServer().getAdvancements().get(TIER3);
        if (advancement == null) {
            return false;
        }
        for (ServerPlayer player : level.players()) {
            if (player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                return true;
            }
        }
        return false;
    }

    /** Vrai si un orage est en cours. Exposé pour les GameTest. */
    public static boolean isStorming(ServerLevel level) {
        return StormState.get(level).remaining > 0;
    }

    /** Déclenche un orage immédiatement. Exposé pour les GameTest. */
    public static void forceStart(ServerLevel level) {
        start(level, StormState.get(level));
    }

    /** Le compteur, persisté avec la dimension. */
    public static class StormState extends SavedData {

        private static final String NAME = "veskorius_storm";

        private int remaining;

        /**
         * Les cratères posés par l'orage en cours, en positions compactées.
         *
         * <p>Persistée avec le reste : un serveur redémarré au milieu d'un orage doit
         * encore savoir ce qu'il a semé, sinon ces cratères deviennent permanents.
         */
        private final java.util.List<Long> craters = new java.util.ArrayList<>();

        public static StormState get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                new Factory<>(StormState::new, StormState::load), NAME);
        }

        private static StormState load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            StormState state = new StormState();
            state.remaining = tag.getInt("remaining");
            for (long packed : tag.getLongArray("craters")) {
                state.craters.add(packed);
            }
            return state;
        }

        @Override
        public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            tag.putInt("remaining", remaining);
            tag.putLongArray("craters", craters.stream().mapToLong(Long::longValue).toArray());
            return tag;
        }
    }
}
