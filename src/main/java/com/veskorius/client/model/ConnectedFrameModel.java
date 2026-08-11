package com.veskorius.client.model;

import com.veskorius.block.ConnectedFrame;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Le modèle qui regarde autour de lui.</b> Il lit le voisinage du bloc au moment où le
 * chunk se construit, et assemble le cadre à partir de morceaux déjà cuits : un fond (plaque
 * de caisson ou vitre), des baguettes d'arête, des quarts de cadre.
 *
 * <p>Un seul modèle pour le verre et pour les châssis. La règle vit dans
 * {@link ConnectedFrame} ; ici il n'y a que du montage.
 *
 * <p><b>Pourquoi pas un blockstate multipart.</b> C'est ce qu'on avait, et ça tenait tant
 * qu'il ne s'agissait que des six faces. Le <b>coin rentrant</b> et l'<b>arête concave</b>
 * demandent de connaître les <b>diagonales</b> : dix-huit booléens, soit 262 144 états par
 * bloc au lieu de 64. {@code getModelData} le peut, parce qu'il reçoit le monde.
 *
 * <p><b>Le bénéfice qu'on n'avait pas prévu :</b> plus rien n'est stocké dans l'état du bloc,
 * donc un mur bâti avant tout ça se connecte de lui-même. La version à propriétés laissait
 * les anciens murs figés sur l'état par défaut pour toujours — {@code updateShape} n'est pas
 * appelé au chargement d'un monde.
 */
public class ConnectedFrameModel extends BakedModelWrapper<BakedModel> {

    /** Le voisinage, encodé par {@link ConnectedFrame} : 6 faces + 12 diagonales. */
    public static final ModelProperty<Integer> CONNECTIONS = new ModelProperty<>();

    /** Un morceau et les couches de rendu où il a le droit d'apparaître. */
    public record Piece(BakedModel model, ChunkRenderTypeSet renderTypes) {

        boolean drawsIn(@Nullable RenderType type) {
            return type == null || renderTypes.contains(type);
        }
    }

    private final Block block;
    private final ChunkRenderTypeSet baseRenderTypes;
    private final Map<EdgeKey, Piece> bars;
    private final Map<CornerKey, Piece> corners;
    private final ChunkRenderTypeSet renderTypes;
    private final Map<Long, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    public ConnectedFrameModel(BakedModel base, Block block,
                               Map<EdgeKey, Piece> bars, Map<CornerKey, Piece> corners) {
        super(base);
        this.block = block;
        this.bars = bars;
        this.corners = corners;

        // L'UNION DES COUCHES, ET IL LA FAUT. Le verre pose sa vitre en translucide et son
        // cadre en cutout ; si le modèle n'annonçait que la couche du fond, le cadre ne serait
        // jamais dessiné — sans erreur, il manquerait simplement à l'image.
        this.baseRenderTypes =
            base.getRenderTypes(block.defaultBlockState(), RandomSource.create(0), ModelData.EMPTY);
        Set<RenderType> union = new LinkedHashSet<>();
        baseRenderTypes.asList().forEach(union::add);
        for (Piece piece : bars.values()) {
            piece.renderTypes().asList().forEach(union::add);
        }
        for (Piece piece : corners.values()) {
            piece.renderTypes().asList().forEach(union::add);
        }
        this.renderTypes = ChunkRenderTypeSet.of(union);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return renderTypes;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  ModelData modelData) {
        int mask = ConnectedFrame.neighbourhood((first, second, third) -> {
            BlockPos target = pos.relative(first);
            if (second != null) {
                target = target.relative(second);
            }
            if (third != null) {
                target = target.relative(third);
            }
            // Le même bloc exactement : deux paliers de châssis ne se fondent pas l'un dans
            // l'autre, et le verre lumineux ne se fond pas dans l'ordinaire.
            return level.getBlockState(target).is(block);
        });
        return modelData.derive().with(CONNECTIONS, mask).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData data,
                                    @Nullable RenderType renderType) {
        // Les faces du fond portent leur cullface : c'est ce qui fait qu'entre deux blocs
        // accolés, la face partagée n'est pas dessinée. On les laisse passer telles quelles —
        // le cadre, lui, n'est jamais culled, il vit donc côté `side == null`.
        // LE FOND AUSSI DOIT ÊTRE FILTRÉ. {@code getQuads} ignore la couche demandée — c'est
        // {@code getRenderTypes} qui trie — donc annoncer l'union sans filtrer ici faisait
        // dessiner la vitre translucide UNE SECONDE FOIS dans la couche cutout du cadre. Deux
        // verres superposés, et un rendu que rien n'expliquait.
        List<BakedQuad> base = renderType == null || baseRenderTypes.contains(renderType)
            ? originalModel.getQuads(state, side, rand, data, renderType)
            : List.of();
        if (side != null) {
            return base;
        }
        Integer mask = data.get(CONNECTIONS);
        List<BakedQuad> frame = cache.computeIfAbsent(
            key(mask == null ? 0 : mask, renderType),
            k -> build(mask == null ? 0 : mask, rand, renderType));
        if (frame.isEmpty()) {
            return base;
        }
        List<BakedQuad> all = new ArrayList<>(base);
        all.addAll(frame);
        return all;
    }

    /** Le cache est indexé par voisinage ET par couche : les deux changent ce qu'on dessine. */
    private static long key(int mask, @Nullable RenderType renderType) {
        return ((long) mask << 32) | (renderType == null ? 0 : renderType.hashCode() & 0xffffffffL);
    }

    private List<BakedQuad> build(int mask, RandomSource rand, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        ConnectedFrame.forEachEdge((a, b) -> {
            if (ConnectedFrame.hasBar(mask, a, b)) {
                collect(bars.get(new EdgeKey(a, b)), quads, rand, renderType);
            }
        });
        ConnectedFrame.forEachFaceCorner((f, p, q) -> {
            if (ConnectedFrame.hasCorner(mask, f, p, q)) {
                collect(corners.get(new CornerKey(f, p, q)), quads, rand, renderType);
            }
        });
        return List.copyOf(quads);
    }

    /**
     * Ramasse toutes les faces d'un morceau, y compris celles rangées sous une direction.
     *
     * <p>Les morceaux sont générés sans cullface, donc leurs quads devraient tous se trouver
     * sous {@code null} — mais compter là-dessus, c'est perdre une face en silence le jour où
     * la datagen change. On balaie les sept cases.
     */
    private static void collect(@Nullable Piece piece, List<BakedQuad> out, RandomSource rand,
                                @Nullable RenderType renderType) {
        if (piece == null || !piece.drawsIn(renderType)) {
            return;
        }
        out.addAll(piece.model().getQuads(null, null, rand, ModelData.EMPTY, renderType));
        for (Direction d : Direction.values()) {
            out.addAll(piece.model().getQuads(null, d, rand, ModelData.EMPTY, renderType));
        }
    }

    /** Une arête du cube, indépendamment de l'ordre des deux faces. */
    public record EdgeKey(Direction a, Direction b) {
        public EdgeKey {
            if (a.ordinal() > b.ordinal()) {
                Direction swap = a;
                a = b;
                b = swap;
            }
        }
    }

    /** Un coin de face : la face, et les deux directions qui le bordent. */
    public record CornerKey(Direction face, Direction p, Direction q) {
        public CornerKey {
            if (p.ordinal() > q.ordinal()) {
                Direction swap = p;
                p = q;
                q = swap;
            }
        }
    }
}
