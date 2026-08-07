package com.veskorius.datagen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * Assemble un {@code StructureTemplate} au format NBT attendu par
 * {@code StructureTemplate.load} : palette de blockstates, liste de blocs
 * (position + index de palette + NBT de block entity), liste d'entités.
 *
 * <p>Extrait de {@code ModStructurePieceProvider} le 2026-08-07, quand les pièces sont
 * passées de « une salle » à un donjon multi-pièces : le fichier mélangeait la
 * <b>plomberie NBT</b> et le <b>dessin des bâtiments</b>, et il fallait relire la
 * première pour toucher au second.
 *
 * <p><b>Ce que cette classe a gagné à la même occasion : les blocs jigsaw.</b> Sans eux,
 * la structure « en jigsaw » du mod n'en était pas une — les pièces n'avaient aucun
 * connecteur, la profondeur d'assemblage valait 1, et ajouter une pièce au pool
 * <i>remplaçait</i> le bâtiment au lieu de l'agrandir (17-Dungeons.md §0). C'est
 * {@link #jigsaw} qui rend la promesse vraie.
 */
public final class TemplateBuilder {

    private final int sx;
    private final int sy;
    private final int sz;
    private final Map<String, Integer> paletteIndex = new LinkedHashMap<>();
    private final List<CompoundTag> palette = new ArrayList<>();
    private final Map<Long, CompoundTag> blocks = new LinkedHashMap<>();
    private final List<CompoundTag> entities = new ArrayList<>();

    public TemplateBuilder(int sx, int sy, int sz) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
    }

    public int width() {
        return sx;
    }

    public int height() {
        return sy;
    }

    public int depth() {
        return sz;
    }

    public void set(int x, int y, int z, BlockState state) {
        set(x, y, z, state, null);
    }

    public void set(int x, int y, int z, BlockState state, CompoundTag blockEntity) {
        int index = paletteFor(state);
        CompoundTag entry = new CompoundTag();
        entry.put("pos", intList(x, y, z));
        entry.putInt("state", index);
        if (blockEntity != null) {
            entry.put("nbt", blockEntity);
        }
        // Dernière écriture gagne (les surcharges de mobilier remplacent l'air).
        blocks.put(key(x, y, z), entry);
    }

    /** Parallélépipède plein, bornes incluses. */
    public void box(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    set(x, y, z, state);
                }
            }
        }
    }

    public void lootChest(int x, int y, int z, ResourceLocation lootTable, Direction facing) {
        CompoundTag be = new CompoundTag();
        be.putString("id", "minecraft:chest");
        be.putString("LootTable", lootTable.toString());
        set(x, y, z, Blocks.CHEST.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), be);
    }

    /**
     * Coffre au contenu <b>fixe</b> : un fragment de Codex donné.
     *
     * <p>Volontairement pas une table de loot : un journal en quatre parties n'a de sens
     * que si les quatre sont là et dans l'ordre. Une table les rendrait aléatoires, et
     * le joueur lirait la fin avant le début — ou pas du tout.
     */
    public void fragmentChest(int x, int y, int z, ResourceLocation entry, Direction facing) {
        CompoundTag item = new CompoundTag();
        item.putString("id", "veskorius:codex_fragment");
        item.putInt("count", 1);
        CompoundTag components = new CompoundTag();
        components.putString("veskorius:codex_entry", entry.toString());
        item.put("components", components);
        item.putByte("Slot", (byte) 0);

        ListTag items = new ListTag();
        items.add(item);
        CompoundTag be = new CompoundTag();
        be.putString("id", "minecraft:chest");
        be.put("Items", items);
        set(x, y, z, Blocks.CHEST.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), be);
    }

    /**
     * Coffre au contenu fixe, un seul objet donné en quantité. Sert au coffre-réserve
     * de l'Avant-poste, qui doit contenir <b>exactement</b> le carburant nécessaire à
     * réveiller l'émetteur ancien : une table de loot rendrait ce carburant aléatoire,
     * donc la porte du T2 aléatoire (17-Dungeons.md §5.1).
     */
    public void itemChest(int x, int y, int z, String itemId, int count, Direction facing) {
        CompoundTag item = new CompoundTag();
        item.putString("id", itemId);
        item.putInt("count", count);
        item.putByte("Slot", (byte) 0);

        ListTag items = new ListTag();
        items.add(item);
        CompoundTag be = new CompoundTag();
        be.putString("id", "minecraft:chest");
        be.put("Items", items);
        set(x, y, z, Blocks.CHEST.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), be);
    }

    /**
     * Pose un <b>bloc jigsaw</b> : le point d'attache d'une pièce voisine.
     *
     * <p>Le bloc lui-même disparaît à la génération, remplacé par {@code finalState}
     * (de l'air, en pratique : le connecteur devient l'ouverture). {@code name} est ce
     * que cette pièce offre, {@code target} ce qu'elle réclame en face ; {@code pool}
     * est le pool où piocher la suite.
     *
     * <p>{@code aligned} force l'orientation du voisin plutôt que de la laisser tourner
     * : indispensable dès qu'une pièce a un haut et un bas (un escalier, une salle
     * meublée), inutile pour du mobilier symétrique.
     */
    public void jigsaw(int x, int y, int z, Direction front, String name, String target,
                       ResourceKey<StructureTemplatePool> pool, BlockState finalState, boolean aligned) {
        CompoundTag be = new CompoundTag();
        be.putString("id", "minecraft:jigsaw");
        be.putString("name", name);
        be.putString("target", target);
        be.putString("pool", pool.location().toString());
        be.putString("final_state", blockStateString(finalState));
        be.putString("joint", aligned ? "aligned" : "rollable");
        set(x, y, z, Blocks.JIGSAW.defaultBlockState()
            .setValue(BlockStateProperties.ORIENTATION,
                net.minecraft.core.FrontAndTop.fromFrontAndTop(front,
                    front.getAxis().isHorizontal() ? Direction.UP : Direction.NORTH)),
            be);
    }

    public void entity(double px, double py, double pz, int bx, int by, int bz, CompoundTag nbt) {
        CompoundTag entry = new CompoundTag();
        ListTag pos = new ListTag();
        pos.add(DoubleTag.valueOf(px));
        pos.add(DoubleTag.valueOf(py));
        pos.add(DoubleTag.valueOf(pz));
        entry.put("pos", pos);
        entry.put("blockPos", intList(bx, by, bz));
        entry.put("nbt", nbt);
        entities.add(entry);
    }

    public CompoundTag build() {
        CompoundTag tag = new CompoundTag();
        tag.put("size", intList(sx, sy, sz));
        ListTag paletteTag = new ListTag();
        paletteTag.addAll(palette);
        tag.put("palette", paletteTag);
        ListTag blocksTag = new ListTag();
        blocksTag.addAll(blocks.values());
        tag.put("blocks", blocksTag);
        ListTag entitiesTag = new ListTag();
        entitiesTag.addAll(entities);
        tag.put("entities", entitiesTag);
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return tag;
    }

    private int paletteFor(BlockState state) {
        String stateTag = NbtUtils.writeBlockState(state).toString();
        Integer existing = paletteIndex.get(stateTag);
        if (existing != null) {
            return existing;
        }
        int index = palette.size();
        paletteIndex.put(stateTag, index);
        palette.add(NbtUtils.writeBlockState(state));
        return index;
    }

    /**
     * Sérialise un blockstate au format texte du champ {@code final_state} d'un bloc
     * jigsaw ({@code namespace:path[prop=val,…]}) — le même que celui d'une commande.
     * C'est un format distinct du NBT de palette, qui est structuré ; le confondre
     * produit un jigsaw qui pose un bloc d'air par défaut sans le signaler.
     */
    private static String blockStateString(BlockState state) {
        StringBuilder out = new StringBuilder(
            net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        if (!state.getProperties().isEmpty()) {
            out.append('[');
            boolean first = true;
            for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                out.append(property.getName()).append('=').append(nameOf(state, property));
            }
            out.append(']');
        }
        return out.toString();
    }

    private static <T extends Comparable<T>> String nameOf(
        BlockState state, net.minecraft.world.level.block.state.properties.Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static ListTag intList(int a, int b, int c) {
        ListTag list = new ListTag();
        list.add(IntTag.valueOf(a));
        list.add(IntTag.valueOf(b));
        list.add(IntTag.valueOf(c));
        return list;
    }

    private static long key(int x, int y, int z) {
        return ((long) x << 20) | ((long) y << 10) | z;
    }
}
