package com.veskorius.datagen;

import com.google.common.hash.Hashing;
import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.CodexEntries;
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

    public static final String MODEST_DWELLING = "modest_dwelling";
    public static final String MODEST_DWELLING_WORKSHOP = "modest_dwelling_workshop";
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
            write(cache, MODEST_DWELLING_WORKSHOP, dwellingWorkshop()),
            write(cache, OUTPOST, outpost()));
    }

    // --- Matériaux --------------------------------------------------------------
    // Une palette restreinte et constante fait la « veskorianité » d'un bâtiment mieux
    // qu'un catalogue de blocs : on doit reconnaître leur maçonnerie avant de lire un
    // panneau. Pierre veinée pour la structure, deepslate taillée pour l'appareillage,
    // cuivre pour tout ce qui était mécanique.

    private static final BlockState VEINED = ModBlocks.RESONANCE_VEINED_STONE.get().defaultBlockState();
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState FLOOR = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState FLOOR_ALT = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState PILLAR = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState RUBBLE = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState COPPER = Blocks.WEATHERED_CUT_COPPER.defaultBlockState();
    private static final BlockState LAMP = Blocks.LANTERN.defaultBlockState();

    /**
     * <b>Habitation Modeste, variante « logis ».</b> 11×6×9.
     *
     * <p>Le dossier demande « petites pièces, mobilier simple, jamais de machine » — donc
     * une pièce de vie, pas un donjon : un sol dallé, une alcôve de couchage séparée par
     * un muret, un âtre, et un plafond partiellement effondré qui a laissé des gravats au
     * sol. C'est l'effondrement qui raconte l'histoire ; le mobilier intact dit qu'on est
     * parti vite.
     */
    private static CompoundTag dwelling() {
        TemplateBuilder b = new TemplateBuilder(11, 6, 9);
        room(b, 11, 6, 9);
        floorPattern(b, 1, 1, 10, 8);

        // Alcôve de couchage, fermée par un muret bas plutôt qu'un mur plein : la pièce
        // reste lisible d'un coup d'œil en entrant.
        for (int z = 1; z <= 3; z++) {
            b.set(4, 1, z, PILLAR);
        }
        b.set(4, 2, 2, PILLAR);
        b.set(1, 1, 1, Blocks.RED_BED.defaultBlockState());
        b.set(2, 1, 1, Blocks.BOOKSHELF.defaultBlockState());
        b.set(1, 1, 3, Blocks.BARREL.defaultBlockState());

        // Coin de vie : établi, âtre, réserve.
        b.setLootChest(8, 1, 7, ModWorldGen.MODEST_DWELLING_LOOT);
        b.set(6, 1, 7, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(9, 1, 4, Blocks.FURNACE.defaultBlockState());
        b.set(8, 1, 1, Blocks.CAULDRON.defaultBlockState());
        b.set(6, 1, 3, Blocks.FLOWER_POT.defaultBlockState());

        hangingLamps(b, 6, new int[][] {{3, 4}, {8, 4}});
        collapse(b, 11, 6, 9, 0x5EED1);
        doorway(b, 5, 8, 11, 9);
        return b.build();
    }

    /**
     * <b>Habitation Modeste, variante « atelier de famille ».</b> 9×6×11.
     *
     * <p>Deuxième entrée du même pool : le générateur en tire une au hasard. Deux plans
     * suffisent à casser l'impression de bâtiment tamponné — c'est le premier bénéfice
     * concret du choix « structures en jigsaw » (`16` §2), et il ne coûte qu'une pièce de
     * plus, aucune ligne de plomberie.
     */
    private static CompoundTag dwellingWorkshop() {
        TemplateBuilder b = new TemplateBuilder(9, 6, 11);
        room(b, 9, 6, 11);
        floorPattern(b, 1, 1, 8, 10);

        // Deux rangées de piliers : l'espace se lit comme une halle, pas comme une boîte.
        for (int z = 3; z <= 7; z += 2) {
            b.set(2, 1, z, PILLAR);
            b.set(2, 2, z, PILLAR);
            b.set(6, 1, z, PILLAR);
            b.set(6, 2, z, PILLAR);
        }

        b.setLootChest(1, 1, 9, ModWorldGen.MODEST_DWELLING_LOOT);
        b.set(4, 1, 9, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(7, 1, 9, Blocks.BARREL.defaultBlockState());
        b.set(1, 1, 1, Blocks.RED_BED.defaultBlockState());
        b.set(7, 1, 1, Blocks.BOOKSHELF.defaultBlockState());
        b.set(4, 1, 5, Blocks.CAULDRON.defaultBlockState());

        hangingLamps(b, 6, new int[][] {{4, 3}, {4, 8}});
        collapse(b, 9, 6, 11, 0x5EED2);
        doorway(b, 4, 10, 9, 11);
        return b.build();
    }

    /**
     * <b>Avant-poste — un vrai donjon à quatre salles.</b> 21×9×21.
     *
     * <pre>
     *   z 0  ┌──────────┬──────────┐
     *        │  HALL    │  CORPS   │   entrée au sud-ouest
     *        │ d'entrée │ DE GARDE │   (Custode + coffre)
     *   z 8  ├────╥─────┴────╥─────┤
     *        │      COULOIR        │
     *   z12  ├────╥─────┬────╥─────┤
     *        │ ARCHIVES │ CONSOLE  │   (journal en 4 fragments)
     *        │          │  + garde │   (porte du T2)
     *   z20  └──────────┴──────────┘
     * </pre>
     *
     * <p><b>Pourquoi un plan unique et non des pièces jigsaw tirées au sort.</b> La console
     * est la <b>porte du T2</b> : si elle vivait dans une salle tirée au hasard, une partie
     * des Avant-postes n'en aurait pas et la progression serait de nouveau suspendue à un
     * tirage — exactement la classe de bug déjà trouvée sur le coffre d'amorçage. Ici, la
     * console, l'amorçage et le journal sont dans le plan garanti. Le jigsaw reste
     * disponible pour ajouter plus tard des ailes <i>facultatives</i>, qui sont sa vraie
     * place : du bonus, jamais du chemin critique.
     *
     * <p><b>La difficulté est un parcours, pas une énigme.</b> `08-Structures.md` refuse
     * explicitement l'énigme à l'Avant-poste (« un geste sur place »). Le défi est donc
     * spatial : deux Custodes, l'un au corps de garde qu'on peut contourner, l'autre devant
     * la console qu'on ne peut pas. Et le couloir est effondré : il faut creuser pour
     * atteindre les salles du fond.
     */
    private static CompoundTag outpost() {
        final int w = 21;
        final int h = 9;
        final int d = 21;
        TemplateBuilder b = new TemplateBuilder(w, h, d);
        room(b, w, h, d);
        floorPattern(b, 1, 1, w - 1, d - 1);

        // --- Cloisons : quatre salles autour d'un couloir traversant ------------
        partition(b, h, 10, 1, 10, 7, 4);      // hall | corps de garde
        partition(b, h, 1, 8, 19, 8, -1);      // salles nord | couloir
        partition(b, h, 1, 12, 19, 12, -1);    // couloir | salles sud
        partition(b, h, 10, 13, 10, 19, 16);   // archives | console
        // Portes du couloir : deux passages nord, deux sud.
        for (int[] door : new int[][] {{5, 8}, {15, 8}, {5, 12}, {15, 12}}) {
            openDoor(b, door[0], door[1]);
        }

        // --- Salle 1 : hall d'entrée. Vide, effondré : on comprend qu'on entre
        // dans une ruine avant de croiser quoi que ce soit de vivant.
        doorway(b, 5, 0, w, d);
        b.set(3, 1, 3, RUBBLE);
        b.set(4, 1, 3, RUBBLE);
        b.set(3, 2, 3, RUBBLE);
        b.set(7, 1, 6, Blocks.SMITHING_TABLE.defaultBlockState());
        hangingLamps(b, h, new int[][] {{5, 4}});

        // --- Salle 2 : corps de garde. Un Custode et le butin d'appoint : le
        // combat est optionnel (on peut filer au couloir), la récompense non.
        b.setLootChest(18, 1, 2, ModWorldGen.OUTPOST_LOOT);
        b.set(12, 1, 6, Blocks.BARREL.defaultBlockState());
        b.set(18, 1, 6, Blocks.GRINDSTONE.defaultBlockState());
        hangingLamps(b, h, new int[][] {{15, 4}});
        custode(b, 15.5, 4.5);

        // --- Couloir : effondré en son milieu. Il faut creuser pour passer —
        // c'est le seul « obstacle » du donjon, et il ne demande qu'une pioche.
        for (int x = 8; x <= 12; x++) {
            for (int y = 1; y <= 3; y++) {
                b.set(x, y, 10, x % 2 == 0 ? RUBBLE : CRACKED);
            }
        }
        hangingLamps(b, h, new int[][] {{3, 10}, {17, 10}});

        // --- Salle 3 : cabinet d'archives. LE LORE. Quatre coffres alignés le
        // long du mur, un fragment de journal chacun, DANS L'ORDRE — on lit une
        // descente en traversant la pièce, pas une anecdote tirée au sort.
        for (int z = 14; z <= 18; z += 2) {
            b.set(1, 1, z, Blocks.BOOKSHELF.defaultBlockState());
            b.set(1, 2, z, Blocks.BOOKSHELF.defaultBlockState());
        }
        b.set(5, 1, 16, Blocks.LECTERN.defaultBlockState());
        ResourceLocation[] log = {
            CodexEntries.OUTPOST_LOG_1, CodexEntries.OUTPOST_LOG_2,
            CodexEntries.OUTPOST_LOG_3, CodexEntries.OUTPOST_LOG_4,
        };
        for (int i = 0; i < log.length; i++) {
            b.setFragmentChest(3 + i * 2, 1, 19, log[i]);
        }
        hangingLamps(b, h, new int[][] {{5, 16}});

        // --- Salle 4 : la console. Estrade centrale, piliers de cuivre oxydé,
        // second Custode. Le relief désigne la pièce maîtresse sans éclairage
        // supplémentaire ; c'est la seule salle où le sol monte.
        for (int x = 13; x <= 17; x++) {
            for (int z = 15; z <= 19; z++) {
                b.set(x, 1, z, PILLAR);
            }
        }
        for (int x = 14; x <= 16; x++) {
            for (int z = 16; z <= 18; z++) {
                b.set(x, 2, z, FLOOR_ALT);
            }
        }
        b.set(15, 3, 17, ModBlocks.ATTUNEMENT_CONSOLE.get().defaultBlockState());
        for (int[] c : new int[][] {{12, 14}, {18, 14}, {12, 19}, {18, 19}}) {
            for (int y = 1; y <= 5; y++) {
                b.set(c[0], y, c[1], y == 5 ? COPPER : PILLAR);
            }
        }
        hangingLamps(b, h, new int[][] {{13, 17}, {17, 17}});
        custode(b, 12.5, 16.5);

        collapse(b, w, h, d, 0x5EED3);
        return b.build();
    }

    /** Cloison intérieure, avec une porte optionnelle ({@code doorAt} &lt; 0 = pleine). */
    private static void partition(TemplateBuilder b, int h, int x0, int z0, int x1, int z1, int doorAt) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = 1; y < h - 1; y++) {
                    b.set(x, y, z, VEINED);
                }
            }
        }
        if (doorAt >= 0) {
            // Porte percée dans une cloison verticale (x fixe) ou horizontale (z fixe).
            boolean vertical = x0 == x1;
            for (int y = 1; y <= 2; y++) {
                b.set(vertical ? x0 : doorAt, y, vertical ? doorAt : z0, AIR);
            }
        }
    }

    /** Percée de 2 blocs de haut dans une cloison, à une position donnée. */
    private static void openDoor(TemplateBuilder b, int x, int z) {
        for (int y = 1; y <= 2; y++) {
            b.set(x, y, z, AIR);
        }
    }

    /** Un Custode persistant en poste (ne despawn jamais, réactif seulement de près). */
    private static void custode(TemplateBuilder b, double x, double z) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "veskorius:custode");
        tag.putBoolean("PersistenceRequired", true);
        b.entity(x, 1.0, z, (int) x, 1, (int) z, tag);
    }

    // --- Vocabulaire de construction --------------------------------------------

    /** Coquille : murs de pierre veinée, sol et plafond pleins, intérieur creusé. */
    private static void room(TemplateBuilder b, int w, int h, int d) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    boolean edge = x == 0 || x == w - 1 || z == 0 || z == d - 1;
                    b.set(x, y, z, edge || y == 0 || y == h - 1 ? VEINED : AIR);
                }
            }
        }
    }

    /** Sol en damier de deux dalles : de l'appareillage, pas une nappe de pierre. */
    private static void floorPattern(TemplateBuilder b, int x0, int z0, int x1, int z1) {
        for (int x = x0; x < x1; x++) {
            for (int z = z0; z < z1; z++) {
                b.set(x, 0, z, (x + z) % 2 == 0 ? FLOOR : FLOOR_ALT);
            }
        }
    }

    /** Lanternes suspendues au plafond : un bâtiment habité s'éclaire par le haut. */
    private static void hangingLamps(TemplateBuilder b, int h, int[][] spots) {
        for (int[] s : spots) {
            b.set(s[0], h - 2, s[1], LAMP.setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        }
    }

    /**
     * Effondrement : quelques blocs de plafond remplacés par de la roche, et les gravats
     * correspondants au sol. C'est ce qui distingue une ruine d'une maison vide — et c'est
     * déterministe (graine fixe) pour que la pièce reste reproductible.
     */
    private static void collapse(TemplateBuilder b, int w, int h, int d, int seed) {
        java.util.Random rand = new java.util.Random(seed);
        for (int n = 0; n < 6; n++) {
            int x = 1 + rand.nextInt(w - 2);
            int z = 1 + rand.nextInt(d - 2);
            b.set(x, h - 1, z, rand.nextBoolean() ? RUBBLE : CRACKED);
            if (rand.nextBoolean()) {
                b.set(x, 1, z, RUBBLE);
            }
        }
        // Maçonnerie fissurée au pied des murs : l'usure part toujours du bas.
        for (int n = 0; n < 8; n++) {
            int x = 1 + rand.nextInt(w - 2);
            b.set(x, 1, rand.nextBoolean() ? 0 : d - 1, CRACKED);
        }
    }

    /**
     * Percée d'entrée dans un mur, prolongée d'un pas vers l'extérieur.
     *
     * <p>Sans elle, la pièce est une bulle scellée : le joueur tombe dessus en minant et
     * n'a aucun signe de l'avoir trouvée. Une ouverture donne une chance à une grotte de
     * la croiser, et fait qu'on ENTRE quelque part au lieu de percer un mur.
     */
    private static void doorway(TemplateBuilder b, int x, int z, int w, int d) {
        for (int y = 1; y <= 2; y++) {
            b.set(x, y, z, AIR);
            b.set(x, y, z + 1 < d ? z + 1 : z, AIR);
        }
        b.set(x, 3, z, CRACKED);
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

        /**
         * Coffre au contenu <b>fixe</b> : un fragment de Codex donné.
         *
         * <p>Volontairement pas une table de loot : un journal en quatre parties n'a de
         * sens que si les quatre sont là et dans l'ordre. Une table les rendrait
         * aléatoires, et le joueur lirait la fin avant le début — ou pas du tout.
         */
        void setFragmentChest(int x, int y, int z, ResourceLocation entry) {
            CompoundTag item = new CompoundTag();
            item.putString("id", "veskorius:codex_fragment");
            item.putInt("count", 1);
            CompoundTag components = new CompoundTag();
            components.putString("veskorius:codex_entry", entry.toString());
            item.put("components", components);
            item.putByte("Slot", (byte) 0);

            net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
            items.add(item);
            CompoundTag be = new CompoundTag();
            be.putString("id", "minecraft:chest");
            be.put("Items", items);
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
