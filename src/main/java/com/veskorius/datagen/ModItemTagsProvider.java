package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.tag.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> registries,
                               CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, blockTags, Veskorius.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Seconde entree du Resonance Stabilizer. Le Raw Flux Deposit rejoindra
        // ce tag en 1:1 a la tache 14 de la Phase 1 (04-Materials.md, groupe 2) —
        // aucune modification de code machine ne sera necessaire.
        tag(ModTags.Items.STABILIZER_FLUX)
            .add(Items.QUARTZ);

        // Volontairement vide pour l'instant : le Resonance Catalyst Core arrive a
        // la tache 15. Le tag est declare des maintenant pour que le slot
        // d'augment existe et refuse tout, plutot que d'accepter n'importe quoi.
        tag(ModTags.Items.MACHINE_AUGMENTS);
    }
}
