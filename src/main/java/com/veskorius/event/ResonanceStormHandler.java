package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
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

    /** Dix minutes (07-World-Generation.md). */
    private static final int STORM_TICKS = 10 * 60 * 20;

    /** Un tirage par jour Minecraft, gagnant une fois sur six : ~5-7 jours d'attente. */
    private static final int ROLL_INTERVAL = 24000;
    private static final int ROLL_CHANCE = 6;

    /** Rayon de semis autour d'un joueur, et cadence. */
    private static final int SEED_RADIUS = 48;
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
        if (level.getGameTime() % ROLL_INTERVAL == 0 && anyoneReachedTierThree(level)
            && level.random.nextInt(ROLL_CHANCE) == 0) {
            start(level, state);
        }
    }

    private static void start(ServerLevel level, StormState state) {
        state.remaining = STORM_TICKS;
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
        if (state.remaining % SEED_INTERVAL == 0) {
            for (ServerPlayer player : level.players()) {
                seedAround(level, player.blockPosition());
            }
        }
        state.setDirty();
    }

    /**
     * Fin de l'orage : <b>tout cratère encore au sol disparaît</b>.
     *
     * <p>On balaie autour des joueurs, comme on a semé. Un cratère posé dans un chunk
     * depuis déchargé s'en tirerait — c'est accepté : il faudrait indexer chaque pose pour
     * l'attraper, et le coût de cet index dépasse largement celui d'un fragment oublié dans
     * un chunk que personne ne regarde.
     */
    private static void end(ServerLevel level, StormState state) {
        state.remaining = 0;
        state.setDirty();
        for (ServerPlayer player : level.players()) {
            BlockPos at = player.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(
                at.offset(-SEED_RADIUS, -32, -SEED_RADIUS),
                at.offset(SEED_RADIUS, 32, SEED_RADIUS))) {
                if (level.getBlockState(pos).is(ModBlocks.METEORIC_CRATER.get())) {
                    level.removeBlock(pos, false);
                }
            }
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("message.veskorius.storm_ends"));
        }
    }

    /** Sème quelques cratères sur des blocs de surface exposés, au hasard. */
    private static void seedAround(ServerLevel level, BlockPos centre) {
        for (int i = 0; i < SEEDS_PER_PASS; i++) {
            int dx = level.random.nextInt(SEED_RADIUS * 2) - SEED_RADIUS;
            int dz = level.random.nextInt(SEED_RADIUS * 2) - SEED_RADIUS;
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

        public static StormState get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                new Factory<>(StormState::new, StormState::load), NAME);
        }

        private static StormState load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            StormState state = new StormState();
            state.remaining = tag.getInt("remaining");
            return state;
        }

        @Override
        public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            tag.putInt("remaining", remaining);
            return tag;
        }
    }
}
