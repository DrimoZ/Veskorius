package com.veskorius.block;

import com.veskorius.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * <b>Buisson de Floraison de Résonance</b> — la seule culture du mod (04-Materials.md,
 * groupe 3).
 *
 * <p><b>Il se récolte, il ne se moissonne pas</b>, et c'est écrit noir sur blanc au
 * dossier : « récolte répétée façon buisson de baies (<b>pas à usage unique</b>) ». Une
 * culture qu'on casse et qu'on replante est un champ de blé de plus ; celle-ci se
 * cueille au clic droit, revient à mi-croissance et repousse. On la plante une fois.
 *
 * <p><b>Pourquoi ça compte ici.</b> La graine ne s'obtient qu'en fouillant l'Archive
 * Régionale, et pas à tous les coups. Si le buisson mourait à la récolte, la branche
 * entière dépendrait de retrouver une Archive — c'est-à-dire d'un tirage, ce que le
 * dossier interdit explicitement pour la progression. Un plant permanent transforme une
 * trouvaille rare en ressource entretenue, ce qui est exactement ce qu'on attend d'une
 * agriculture.
 *
 * <p><b>Trois engrais, pas un.</b> L'os à moelle vanilla, la Poussière de Résonance et la
 * <b>Boue de Résonance</b> font tous pousser. Le dernier n'est pas une coquetterie : la
 * boue est un déchet, et lui donner un second débouché après le Reclaimer referme la
 * boucle économique par le bas. Le joueur qui purge un champ pollué en tire de quoi
 * nourrir un champ.
 */
public class ResonanceBloomBushBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<ResonanceBloomBushBlock> CODEC =
        simpleCodec(ResonanceBloomBushBlock::new);

    /** Quatre étapes, comme le buisson de baies : deux stériles, deux qui donnent. */
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    /** Âge à partir duquel on peut cueillir. */
    public static final int RIPE = 3;

    /** Âge auquel le buisson retombe après cueillette — il ne repart JAMAIS de zéro. */
    private static final int AFTER_HARVEST = 1;

    private static final VoxelShape SAPLING_SHAPE = box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape MID_SHAPE = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public ResonanceBloomBushBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return state.getValue(AGE) == 0 ? SAPLING_SHAPE : MID_SHAPE;
    }

    /**
     * Elle pousse sur la terre <b>et sur la pierre veinée</b>. La seconde est le geste qui
     * dit d'où vient cette plante : elle a poussé dans les ruines veskoriennes, pas dans un
     * potager. Un joueur qui la plante sur du veiné bâtit une serre à sa place.
     */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(net.minecraft.tags.BlockTags.DIRT)
            || state.is(net.minecraft.world.level.block.Blocks.FARMLAND)
            || state.is(ModBlocks.RESONANCE_VEINED_STONE.get());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < RIPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        // Lente : c'est une plante de ruine, pas une céréale. La comparaison utile est le
        // buisson de baies vanilla, qui met le même genre de temps.
        if (age < RIPE && random.nextInt(5) == 0
            && level.getRawBrightness(pos.above(), 0) >= 9) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < RIPE) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            harvest(state, (ServerLevel) level, pos, player);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Cueille le buisson mûr : deux ou trois floraisons, et il retombe à
     * {@link #AFTER_HARVEST}. Exposé pour les GameTest.
     */
    public static void harvest(BlockState state, ServerLevel level, BlockPos pos, Player player) {
        int count = 2 + level.random.nextInt(2);
        popResource(level, pos, new ItemStack(ModItems.RESONANCE_BLOOM.get(), count));
        level.setBlock(pos, state.setValue(AGE, AFTER_HARVEST), 2);
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
            SoundSource.BLOCKS, 1.0f, 0.8f + level.random.nextFloat() * 0.4f);
        level.sendParticles(ParticleTypes.END_ROD,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.0);
    }

    // --- Engrais --------------------------------------------------------------
    //
    // BonemealableBlock couvre l'os à moelle vanilla. La Poussière et la Boue de
    // Résonance passent par le même chemin, branchées dans ModEvents : trois engrais
    // pour un seul comportement, plutôt que trois comportements à garder alignés.

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < RIPE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(RIPE, state.getValue(AGE) + 1)), 2);
    }
}
