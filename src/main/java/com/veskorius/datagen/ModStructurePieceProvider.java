package com.veskorius.datagen;

import com.google.common.hash.Hashing;
import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.worldgen.ModWorldGen;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Génère les <b>pièces de structure</b> (NBT) de l'Habitation Modeste et de l'Avant-poste
 * (08-Structures.md), consommées par le système jigsaw (voir {@code ModStructures}).
 *
 * <p><b>Pourquoi générer le NBT par code plutôt qu'à la main dans un structure block :</b>
 * comme le reste du projet, aucune ressource n'est écrite à la main — les pièces dérivent
 * du code, donc restent alignées sur les registres (bloc de mur, console) sans risque de
 * se désynchroniser. Le format produit (size / palette / blocks / entities) est exactement
 * celui que {@code StructureTemplate.load} relit ; il est déjà éprouvé par
 * {@link ModStructureTemplateProvider} (templates vides des GameTest).
 *
 * <p>Ce sont des pièces <b>placeholder</b> : une salle de {@code resonance_veined_stone}
 * meublée sommairement (les vrais bâtiments arrivent en Phase 6). Le jigsaw à une seule
 * pièce honore malgré tout le choix « structures en jigsaw » (16 §2) : agrandir = ajouter
 * des pièces au pool, sans réécrire.
 */
public class ModStructurePieceProvider implements DataProvider {

    /** Empreinte au sol : salle 7×7, hauteur intérieure 4 (mur y=0 à y=4). */
    private static final int W = 7;
    private static final int H = 5;
    private static final int D = 7;

    public static final String MODEST_DWELLING = "modest_dwelling";
    public static final String OUTPOST = "outpost";

    private final PackOutput.PathProvider pathProvider;

    public ModStructurePieceProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK,
            StructureTemplateManager.STRUCTURE_RESOURCE_DIRECTORY_NAME);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
            write(cache, MODEST_DWELLING, dwelling()),
            write(cache, OUTPOST, outpost()));
    }

    /** Habitation Modeste : salle + coffre de quotidien, aucune machine (08-Structures.md). */
    private static CompoundTag dwelling() {
        TemplateBuilder b = new TemplateBuilder(W, H, D);
        shell(b);
        b.setLootChest(1, 1, 1, ModWorldGen.MODEST_DWELLING_LOOT);
        // Mobilier déterministe (le vrai bâtiment est Phase 6) : une habitation vécue.
        b.set(5, 1, 5, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(5, 1, 1, Blocks.BARREL.defaultBlockState());
        b.set(1, 1, 5, Blocks.BOOKSHELF.defaultBlockState());
        return b.build();
    }

    /**
     * Avant-poste : salle + coffre d'amorçage T2 + <b>console d'attunement</b> (porte du
     * T2) + un <b>Custode</b> gardien persistant intégré à la pièce (08/09). Le blueprint
     * vient de la console, jamais du coffre.
     */
    private static CompoundTag outpost() {
        TemplateBuilder b = new TemplateBuilder(W, H, D);
        shell(b);
        b.setLootChest(1, 1, 1, ModWorldGen.OUTPOST_LOOT);
        b.set(3, 1, 3, ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState());
        b.set(1, 1, 5, Blocks.FURNACE.defaultBlockState());
        b.set(5, 1, 1, Blocks.SMITHING_TABLE.defaultBlockState());
        b.set(5, 1, 5, Blocks.BARREL.defaultBlockState());
        // Gardien du site : persistant (ne despawn jamais), réactif seulement de près.
        CompoundTag custode = new CompoundTag();
        custode.putString("id", "veskorius:custode");
        custode.putBoolean("PersistenceRequired", true);
        b.entity(4.5, 1.0, 4.5, 4, 1, 4, custode);
        return b.build();
    }

    /** Coquille pleine : murs + sol + plafond en pierre veinée, intérieur en air. */
    private static void shell(TemplateBuilder b) {
        BlockState wall = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();
        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                for (int z = 0; z < D; z++) {
                    boolean edge = x == 0 || x == W - 1 || z == 0 || z == D - 1;
                    boolean floorOrCeil = y == 0 || y == H - 1;
                    // Intérieur en air : la pièce est creusée dans la roche souterraine.
                    b.set(x, y, z, edge || floorOrCeil ? wall : air);
                }
            }
        }
    }

    // --- Construction du NBT de template --------------------------------------

    /**
     * Assemble un {@code StructureTemplate} au format NBT attendu par
     * {@code StructureTemplate.load} : une palette de blockstates, la liste des blocs
     * (position + index de palette + NBT de block entity optionnel) et les entités.
     */
    private static final class TemplateBuilder {

        private final int sx;
        private final int sy;
        private final int sz;
        private final Map<String, Integer> paletteIndex = new LinkedHashMap<>();
        private final List<CompoundTag> palette = new ArrayList<>();
        private final Map<Long, CompoundTag> blocks = new LinkedHashMap<>();
        private final List<CompoundTag> entities = new ArrayList<>();

        TemplateBuilder(int sx, int sy, int sz) {
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }

        void set(int x, int y, int z, BlockState state) {
            set(x, y, z, state, null);
        }

        void setLootChest(int x, int y, int z, ResourceLocation lootTable) {
            CompoundTag be = new CompoundTag();
            be.putString("id", "minecraft:chest");
            be.putString("LootTable", lootTable.toString());
            set(x, y, z, Blocks.CHEST.defaultBlockState(), be);
        }

        void set(int x, int y, int z, BlockState state, CompoundTag blockEntity) {
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

        void entity(double px, double py, double pz, int bx, int by, int bz, CompoundTag nbt) {
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

        CompoundTag build() {
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

    private CompletableFuture<?> write(CachedOutput cache, String name, CompoundTag tag) {
        Path target = pathProvider.file(
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name), "nbt");
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                NbtIo.writeCompressed(tag, buffer);
                byte[] bytes = buffer.toByteArray();
                cache.writeIfNeeded(target, bytes, Hashing.sha1().hashBytes(bytes));
            } catch (IOException e) {
                throw new UncheckedIOException("Écriture de la pièce de structure " + name, e);
            }
        }, Util.backgroundExecutor());
    }

    @Override
    public String getName() {
        return "Veskorius structure pieces";
    }
}
