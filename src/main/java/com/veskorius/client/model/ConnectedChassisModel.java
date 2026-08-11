package com.veskorius.client.model;

import com.veskorius.block.ChassisFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Le modèle qui regarde autour de lui.</b> Il lit le voisinage du châssis au moment où le
 * chunk se construit, et assemble le cadre à partir de morceaux déjà cuits : une plaque, des
 * baguettes d'arête, des quarts de cadre.
 *
 * <p><b>Pourquoi pas un blockstate multipart.</b> C'est ce qu'on avait, et ça marchait tant
 * qu'il ne s'agissait que des six faces. Le <b>coin rentrant</b> — le bloc d'un angle en L,
 * qui touche ses deux voisins et ne dessine donc aucune bordure — demande de connaître les
 * <b>diagonales</b>. Six booléens font 64 états ; dix-huit en font 262 144, par bloc. Le
 * blockstate ne peut pas porter ça. {@code getModelData} le peut, parce qu'il reçoit le monde.
 *
 * <p><b>Le bénéfice qu'on n'avait pas prévu :</b> plus rien n'est stocké dans l'état du bloc,
 * donc un mur bâti avant tout ça se connecte de lui-même. La version à propriétés laissait
 * les anciens murs figés sur l'état par défaut pour toujours — {@code updateShape} n'est pas
 * appelé au chargement d'un monde.
 *
 * <p>Le calcul n'est fait qu'une fois par configuration : {@link #cache} garde la liste de
 * quads pour chaque voisinage rencontré. Un mur, si grand soit-il, n'en produit qu'une
 * poignée.
 */
public class ConnectedChassisModel extends BakedModelWrapper<BakedModel> {

    /** Le voisinage, encodé par {@link ChassisFrame} : 6 faces + 12 diagonales. */
    public static final ModelProperty<Integer> CONNECTIONS = new ModelProperty<>();

    private final Block block;
    private final Map<EdgeKey, BakedModel> bars;
    private final Map<CornerKey, BakedModel> corners;
    private final Map<Integer, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    public ConnectedChassisModel(BakedModel plate, Block block,
                                 Map<EdgeKey, BakedModel> bars,
                                 Map<CornerKey, BakedModel> corners) {
        super(plate);
        this.block = block;
        this.bars = bars;
        this.corners = corners;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  ModelData modelData) {
        int mask = ChassisFrame.neighbourhood((first, second) -> {
            BlockPos target = pos.relative(first);
            if (second != null) {
                target = target.relative(second);
            }
            // `is(block)` et pas « un châssis quelconque » : les trois paliers ne se fondent
            // pas l'un dans l'autre.
            return level.getBlockState(target).is(block);
        });
        return modelData.derive().with(CONNECTIONS, mask).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData data,
                                    @Nullable net.minecraft.client.renderer.RenderType renderType) {
        // Les faces de la plaque portent leur cullface : c'est ce qui fait qu'entre deux
        // caissons accolés, la face partagée n'est pas dessinée. On les laisse passer telles
        // quelles — le cadre, lui, n'est jamais culled, il vit donc côté `side == null`.
        if (side != null) {
            return originalModel.getQuads(state, side, rand, data, renderType);
        }
        Integer mask = data.get(CONNECTIONS);
        List<BakedQuad> frame = cache.computeIfAbsent(mask == null ? 0 : mask,
            m -> build(m, rand, renderType));
        if (frame.isEmpty()) {
            return originalModel.getQuads(state, null, rand, data, renderType);
        }
        List<BakedQuad> all =
            new ArrayList<>(originalModel.getQuads(state, null, rand, data, renderType));
        all.addAll(frame);
        return all;
    }

    private List<BakedQuad> build(int mask, RandomSource rand,
                                  @Nullable net.minecraft.client.renderer.RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        ChassisFrame.forEachEdge((a, b) -> {
            if (!ChassisFrame.hasBar(mask, a, b)) {
                return;
            }
            BakedModel piece = bars.get(new EdgeKey(a, b));
            if (piece != null) {
                collect(piece, quads, rand, renderType);
            }
        });
        ChassisFrame.forEachFaceCorner((f, p, q) -> {
            if (!ChassisFrame.hasCorner(mask, f, p, q)) {
                return;
            }
            BakedModel piece = corners.get(new CornerKey(f, p, q));
            if (piece != null) {
                collect(piece, quads, rand, renderType);
            }
        });
        return List.copyOf(quads);
    }

    /**
     * Ramasse TOUTES les faces d'un morceau, y compris celles rangées sous une direction.
     *
     * <p>Les morceaux sont générés sans cullface, donc leurs quads devraient tous se trouver
     * sous {@code null} — mais compter là-dessus, c'est perdre une face en silence le jour où
     * la datagen change. On balaie les sept cases.
     */
    private static void collect(BakedModel piece, List<BakedQuad> out, RandomSource rand,
                                @Nullable net.minecraft.client.renderer.RenderType renderType) {
        out.addAll(piece.getQuads(null, null, rand, ModelData.EMPTY, renderType));
        for (Direction d : Direction.values()) {
            out.addAll(piece.getQuads(null, d, rand, ModelData.EMPTY, renderType));
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
