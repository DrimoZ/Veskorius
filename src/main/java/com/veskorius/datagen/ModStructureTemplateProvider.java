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
            write(cache, FIELD_ARENA_TEMPLATE, emptyTemplate(ARENA_SIZE)));
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
        Path target = pathProvider.file(
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name), "nbt");

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
