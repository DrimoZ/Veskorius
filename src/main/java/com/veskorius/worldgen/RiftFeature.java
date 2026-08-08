package com.veskorius.worldgen;

import com.veskorius.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * <b>La Faille</b> (07-World-Generation.md, « génération spéciale »).
 *
 * <p>Ce n'est pas une structure : c'est une <b>bulle de vide</b> creusée dans la roche, avec
 * un noyau flottant en son centre. Rien n'y est bâti — une Faille est un accident de
 * sur-résonance, pas un site. C'est aussi la seule chose du mod qui <b>retire</b> du monde
 * au lieu d'y ajouter.
 *
 * <p><b>La coquille de pierre déformée est le sujet, pas la décoration.</b> Une Faille est
 * invisible au Resonance Locator (elle ne rayonne pas un champ, elle en déphase un) : le
 * seul moyen de la trouver est de reconnaître ses fissures. Sans cette coquille, la
 * mécanique de repérage du dernier palier n'existerait pas, et l'endgame se découvrirait
 * en creusant au hasard.
 *
 * <p><b>Elle ne perce jamais jusqu'à la bedrock.</b> Le vide s'arrête une couche au-dessus
 * du plancher du monde : une bulle ouverte sur le vide absolu laisserait tomber le joueur
 * hors du monde, et « on meurt sans comprendre » n'est pas une difficulté, c'est un bug.
 */
public class RiftFeature extends Feature<NoneFeatureConfiguration> {

    /** Rayon du vide, tiré dans cet intervalle (07-World-Generation.md : 5 à 9). */
    private static final int MIN_RADIUS = 5;
    private static final int MAX_RADIUS = 9;

    /** Épaisseur de la coquille de pierre déformée — le signe avant-coureur. */
    private static final int SHELL = 2;

    public RiftFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int radius = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int floor = level.getMinBuildHeight() + 1;

        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        BlockState deformed = ModBlocks.DEFORMED_STONE.get().defaultBlockState();

        int outer = radius + SHELL;
        for (int dx = -outer; dx <= outer; dx++) {
            for (int dy = -outer; dy <= outer; dy++) {
                for (int dz = -outer; dz <= outer; dz++) {
                    BlockPos at = origin.offset(dx, dy, dz);
                    if (at.getY() <= floor) {
                        continue;
                    }
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (d <= radius) {
                        level.setBlock(at, air, 2);
                    } else if (d <= outer && !level.getBlockState(at).isAir()) {
                        // La coquille ne remplace que de la roche : sans ce contrôle elle
                        // boucherait les grottes qu'elle traverse, et une Faille se
                        // signalerait par un mur suspect plutôt que par des fissures.
                        level.setBlock(at, deformed, 2);
                    }
                }
            }
        }

        // Le noyau, flottant au centre. Il est ce que le Rift Anchor stabilise et ce que
        // l'Extractor exploite ; sans lui la bulle n'est qu'une grotte ronde.
        level.setBlock(origin, ModBlocks.RIFT_CORE.get().defaultBlockState(), 2);
        return true;
    }
}
