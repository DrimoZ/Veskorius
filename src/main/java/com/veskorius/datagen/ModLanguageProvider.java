package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Les noms en jeu restent en anglais (convention posee par
 * 13-Registry-Index.md : registry names et affichage en anglais, la prose de
 * conception en francais).
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, Veskorius.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.veskorius.main_tab", "Veskorius");

        addBlock(ModBlocks.RESONANCE_STABILIZER, "Resonance Stabilizer");

        addItem(ModItems.RAW_RESONANCE_CRYSTAL, "Raw Resonance Crystal");
        addItem(ModItems.STABLE_RESONANCE_CRYSTAL, "Stable Resonance Crystal");
        addItem(ModItems.REFINED_RESONANCE_CRYSTAL, "Refined Resonance Crystal");
        addItem(ModItems.RESONANCE_COMPONENT, "Resonance Component");
    }
}
