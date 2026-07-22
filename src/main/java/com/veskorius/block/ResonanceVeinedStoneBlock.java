package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Resonance Veined Stone: the shell around crystal pockets and the visual "tell"
 * (07-World-Generation.md). It also <b>grows Resonance Spore</b> (04-Materials.md):
 * when a block has an exposed face in low light, random ticks may set its
 * {@link #SPORED} state. A player then <b>harvests the spore by hand</b> (right-click)
 * without breaking the stone; the spore regrows over time (the {@code spored} state
 * returns via random ticks).
 *
 * Modelled as a state on the veined stone rather than a separate glow-lichen block:
 * simpler and faithful enough ("the spore grows on the veined stone").
 */
public class ResonanceVeinedStoneBlock extends Block {

    public static final BooleanProperty SPORED = BooleanProperty.create("spored");
    public static final MapCodec<ResonanceVeinedStoneBlock> CODEC = simpleCodec(ResonanceVeinedStoneBlock::new);

    /** A spore only grows when a face is exposed to air at this light level or below. */
    private static final int MAX_GROWTH_LIGHT = 7;

    public ResonanceVeinedStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SPORED, false));
    }

    @Override
    protected MapCodec<? extends ResonanceVeinedStoneBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPORED);
    }

    // Only tick while there is no spore yet (nothing to do once grown until harvested).
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(SPORED);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextDouble() < VeskoriusConfig.sporeGrowthChance()) {
            tryGrowSpore(state, level, pos);
        }
    }

    /** Grows a spore if this stone has an exposed face in low light. Exposed for GameTests. */
    public static void tryGrowSpore(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.getValue(SPORED) || !canGrowHere(level, pos)) {
            return;
        }
        level.setBlock(pos, state.setValue(SPORED, true), 2);
    }

    /** True if at least one neighbouring face is air and dark enough for a spore. */
    public static boolean canGrowHere(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbour = pos.relative(dir);
            if (level.getBlockState(neighbour).isAir()
                && level.getMaxLocalRawBrightness(neighbour) <= MAX_GROWTH_LIGHT) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!state.getValue(SPORED)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            tryHarvest(state, level, pos, player);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Hand-harvest: if spored, give a spore and clear the {@code spored} state (it
     * regrows later). Returns true if a spore was harvested. Exposed for GameTests.
     */
    public static boolean tryHarvest(BlockState state, Level level, BlockPos pos, Player player) {
        if (!state.getValue(SPORED)) {
            return false;
        }
        level.setBlock(pos, state.setValue(SPORED, false), 3);
        ItemStack spore = new ItemStack(ModItems.RESONANCE_SPORE.get());
        if (!player.addItem(spore)) {
            player.drop(spore, false);
        }
        level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 0.8f, 1.1f);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 5, 0.4, 0.4, 0.4, 0.01);
        }
        return true;
    }
}
