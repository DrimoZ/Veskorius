package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Bloc <b>passif qui lit le champ de Résonance</b> : il ne consomme rien, il ne fait
 * qu'exposer, dans son blockstate, l'existence d'un champ actif à sa position
 * ({@code POWERED}).
 *
 * <p><b>Pourquoi ce bloc existe</b> (17-Dungeons.md, règle R2 « la lumière raconte
 * l'état du réseau »). Un donjon veskorien doit se lire sans carte : une branche morte
 * est froide et éteinte, une branche alimentée s'allume. La même mécanique sert au
 * joueur dans sa base — poser des lampes de résonance, c'est obtenir un éclairage qui
 * s'éteint quand l'émetteur tombe en panne. Un seul comportement, deux usages.
 *
 * <p><b>Pourquoi un tick programmé plutôt qu'une block entity.</b> Un donjon en compte
 * des dizaines ; une block entity par conduit coûterait un tick d'objet chacun, pour
 * une information qui change une fois par partie. Le tick programmé se réarme lui-même
 * toutes les {@link #PERIOD} ticks, ne fait qu'une requête au gestionnaire de champ, et
 * ne coûte rien tant que le chunk n'est pas chargé. Le déphasage aléatoire évite que
 * tous les conduits d'une salle se réveillent sur la même tick.
 *
 * <p><b>Pourquoi pas non plus une notification poussée par l'émetteur.</b> Elle
 * demanderait à l'émetteur de connaître la géométrie de ce qu'il éclaire — donc de
 * scanner sa portée à chaque allumage. Le sens de lecture retenu (c'est le bloc qui
 * interroge) garde le manager comme unique détenteur de la géométrie, ce qui est déjà
 * sa règle pour les machines.
 */
public class FieldSensitiveBlock extends Block {

    public static final MapCodec<FieldSensitiveBlock> CODEC = simpleCodec(FieldSensitiveBlock::new);

    /** Vrai quand un champ de Résonance actif couvre ce bloc. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /**
     * Période de vérification, en ticks (2 s). Assez lent pour être invisible en coût,
     * assez rapide pour que rallumer l'émetteur d'un donjon se voie tout de suite —
     * c'est le retour visuel de la règle R1, il ne doit pas se faire attendre.
     */
    private static final int PERIOD = 40;

    public FieldSensitiveBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(POWERED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends FieldSensitiveBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    // --- Boucle de vérification ---------------------------------------------

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        // Vaut aussi bien pour un bloc posé par un joueur que pour un bloc écrit par
        // la génération de structure : les deux passent par setBlock, donc par ici.
        schedule(level, pos, level.random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        IResonanceField source = ResonanceFieldManager.coveringSource(level, pos);
        boolean powered = source != null && source.isActive();
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
        schedule(level, pos, random);
    }

    private void schedule(LevelAccessor level, BlockPos pos, RandomSource random) {
        // Déphasage : sans lui, tous les conduits posés par la même structure
        // tomberaient sur la même tick pour toujours.
        level.scheduleTick(pos, this, PERIOD + random.nextInt(PERIOD));
    }
}
