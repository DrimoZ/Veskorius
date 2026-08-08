package com.veskorius.datagen;

import com.google.common.hash.Hashing;
import com.veskorius.Veskorius;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Genere les structures vides utilisees par les GameTest.
 *
 * Pourquoi ce provider existe : le framework GameTest exige un template de
 * structure pour chaque test, et NeoForge 21.1 n'en fournit aucun de vide
 * (verifie en listant le contenu de neoforge-21.1.172.jar — l'annotation
 * {@code @EmptyTemplate} des versions plus recentes n'existe pas ici). Sans ce
 * provider, il faudrait produire un .nbt binaire a la main et le committer,
 * c'est-a-dire exactement la ressource ecrite a la main que le reste du projet
 * evite.
 *
 * Un template vide suffit : {@code StructureUtils.clearSpaceForStructure} vide
 * la zone et construit un sol juste sous la structure avant chaque test.
 */
public class ModStructureTemplateProvider implements DataProvider {

    /** Petite structure pour les machines à cycle : poser un bloc, tourner autour, rester rapide. */
    private static final int EMPTY_SIZE = 5;
    public static final String EMPTY_TEMPLATE = "empty";

    /**
     * Grande arène pour les tests du système de champ. Le
     * {@link com.veskorius.energy.ResonanceFieldManager} est un index GLOBAL par
     * dimension : sans isolation spatiale, un émetteur d'un test voisin (le
     * framework GameTest place les structures à quelques blocs les unes des
     * autres) fausserait les prélèvements d'un autre test.
     *
     * Dimension : émetteur au centre (10,·,10), les tests sondent jusqu'à 9 blocs
     * de lui. À 21 de côté, l'émetteur d'une arène adjacente est à ≥ 21−9 = 12
     * blocs de toute position sondée — hors de la portée 8 du Field Emitter, donc
     * aucune contamination. À réviser quand des portées plus grandes seront
     * testées (Relay 20, Convergence Core 40) : il faudra agrandir en conséquence.
     */
    private static final int ARENA_SIZE = 21;
    /**
     * Arène dédiée aux tests de structure. L'Avant-poste fait <b>33×26×33</b> depuis qu'il
     * est un vrai donjon (deux niveaux voûtés, un escalier en vis, une rotonde à coupole) :
     * posé à l'ancre (1,1,1), il lui faut 35 de côté. Une arène trop petite fait échouer
     * les tests de façon opaque (« Expected X, got Air ») sans jamais dire que c'est la
     * place qui manque — d'où cette note, qui a déjà servi deux fois.
     */
    private static final int PIECE_ARENA_SIZE = 42;
    public static final String PIECE_ARENA_TEMPLATE = "piece_arena";
    public static final String FIELD_ARENA_TEMPLATE = "field_arena";

    private final PackOutput.PathProvider pathProvider;

    public ModStructureTemplateProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK,
            StructureTemplateManager.STRUCTURE_RESOURCE_DIRECTORY_NAME);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
            write(cache, EMPTY_TEMPLATE, emptyTemplate(EMPTY_SIZE)),
            write(cache, FIELD_ARENA_TEMPLATE, emptyTemplate(ARENA_SIZE)),
            write(cache, PIECE_ARENA_TEMPLATE, emptyTemplate(PIECE_ARENA_SIZE)),
            // LES MÊMES ARÈNES, SOUS LE NAMESPACE DES TESTS DE STRUCTURE.
            //
            // GameTest cherche toujours un gabarit sous le namespace du @GameTestHolder de
            // la classe de test, et StructureGameTests en a un à part pour être filtrable
            // (voir WorldGenTests.NAMESPACE). Sans ces deux copies, le serveur de test
            // s'écrase au démarrage sur « Missing test structure » — avant le premier tick,
            // donc sans qu'aucun test ne rougisse.
            write(cache, com.veskorius.gametest.WorldGenTests.NAMESPACE,
                FIELD_ARENA_TEMPLATE, emptyTemplate(ARENA_SIZE)),
            write(cache, com.veskorius.gametest.WorldGenTests.NAMESPACE,
                PIECE_ARENA_TEMPLATE, emptyTemplate(PIECE_ARENA_SIZE)));
    }

    /**
     * Structure cubique sans aucun bloc. {@code StructureTemplate.save} accepte
     * explicitement une palette vide, et {@code load} relit "size", "blocks",
     * "palette" et "entities" — les quatre cles produites ici.
     */
    private static CompoundTag emptyTemplate(int size) {
        ListTag sizeTag = new ListTag();
        sizeTag.add(IntTag.valueOf(size));
        sizeTag.add(IntTag.valueOf(size));
        sizeTag.add(IntTag.valueOf(size));

        CompoundTag tag = new CompoundTag();
        tag.put("size", sizeTag);
        tag.put("blocks", new ListTag());
        tag.put("palette", new ListTag());
        tag.put("entities", new ListTag());
        // Sans DataVersion, le manager suppose 500 et fait tourner tout le
        // DataFixer depuis cette version a chaque chargement.
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return tag;
    }

    private CompletableFuture<?> write(CachedOutput cache, String name, CompoundTag tag) {
        return write(cache, Veskorius.MOD_ID, name, tag);
    }

    private CompletableFuture<?> write(CachedOutput cache, String namespace, String name,
                                       CompoundTag tag) {
        Path target = pathProvider.file(
            ResourceLocation.fromNamespaceAndPath(namespace, name), "nbt");

        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                NbtIo.writeCompressed(tag, buffer);
                byte[] bytes = buffer.toByteArray();
                cache.writeIfNeeded(target, bytes, Hashing.sha1().hashBytes(bytes));
            } catch (IOException e) {
                throw new UncheckedIOException("Ecriture du template de GameTest " + name, e);
            }
        }, Util.backgroundExecutor());
    }

    @Override
    public String getName() {
        return "Veskorius GameTest structure templates";
    }
}
