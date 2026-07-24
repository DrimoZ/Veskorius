package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.tag.ModTags;
import com.veskorius.worldgen.ModStructures;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Tags de structures — pour l'instant le seul {@code #veskorius:locatable}, qui allume le
 * <b>mode Structures du Resonance Locator</b> et fait fonctionner {@code /locate} (16 §1).
 * Il restait vide tant que les ruines étaient des <i>features</i> ; la migration en vraies
 * {@code Structure} (A7) le remplit enfin.
 *
 * <p>Ajout par {@code addOptional} : le tag référence les structures par leur identifiant
 * sans exiger leur présence au datagen (elles sont générées dans le même run, registre
 * datapack) et sans planter si un datapack les retire.
 */
public class ModStructureTagsProvider extends TagsProvider<Structure> {

    public ModStructureTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.STRUCTURE, lookup, Veskorius.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Structures.LOCATABLE)
            .addOptional(ModStructures.MODEST_DWELLING.location())
            .addOptional(ModStructures.OUTPOST.location());
    }
}
