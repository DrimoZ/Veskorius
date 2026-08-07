package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * <b>Sas de Résonance</b> — le verrou des donjons veskoriens (17-Dungeons.md, règle R1).
 *
 * <p>Une plaque pleine et indestructible qui <b>ne s'ouvre que dans un champ de
 * Résonance actif</b>. Quand le champ arrive, elle s'escamote dans le sol ; quand il
 * meurt, elle remonte. Elle hérite de {@link FieldSensitiveBlock} : {@code POWERED}
 * <i>est</i> l'état « ouvert », ce n'est pas une coïncidence de nommage mais la
 * mécanique elle-même — un sas alimenté est un sas ouvert.
 *
 * <p><b>Ce que ce bloc remplace, et pourquoi c'est mieux.</b> Le réflexe du genre est
 * la clé-objet (keycard, fragment de porte, bouton caché). Elle ferait du champ une
 * mécanique d'usine qu'on laisse à la base, alors que le pilier 3 en fait le cœur du
 * mod. Ici, la seule façon de passer est d'<b>amener de la Résonance jusqu'à la
 * porte</b> — en réveillant l'émetteur ancien du lieu (T2), ou plus tard en apportant
 * le sien. Le joueur apprend le champ dans la salle où il en a eu besoin (pilier 2).
 *
 * <p><b>Pourquoi il est indestructible</b> (dureté −1, résistance de bedrock, aucune
 * loot table). Un verrou qu'on finit par percer n'est pas un verrou, c'est un péage :
 * la règle R1 n'existerait plus, et tout le contenu derrière deviendrait accessible à
 * la pioche. C'est le seul bloc du mod à ce régime, et c'est assumé.
 */
public class ResonanceBulkheadBlock extends FieldSensitiveBlock {

    public static final MapCodec<ResonanceBulkheadBlock> CODEC = simpleCodec(ResonanceBulkheadBlock::new);

    /**
     * Sas escamoté : il reste un seuil de 2 px au sol. Un état totalement vide
     * laisserait un trou dans un mur sans qu'on comprenne ce qui l'a ouvert ; le seuil
     * dit « la plaque est descendue là », et il est assez bas pour qu'on marche dessus
     * sans sauter.
     */
    private static final VoxelShape OPEN_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public ResonanceBulkheadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FieldSensitiveBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(POWERED) ? OPEN_SHAPE : Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(POWERED) ? OPEN_SHAPE : Shapes.block();
    }

    /**
     * Occlusion calculée sur la forme réelle : sans ça, un sas ouvert continuerait de
     * bloquer la lumière et la salle derrière resterait noire alors qu'elle vient de
     * s'allumer — exactement le retour visuel qu'on cherche à produire.
     */
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(POWERED);
    }
}
