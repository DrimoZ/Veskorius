package com.veskorius.item;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Items du MVP (voir TECH-SPEC.md, section Materiaux).
 * Un seul chemin de recette pour l'instant : Raw -> Stable -> Refined,
 * plus le Resonance Component utilise par les machines T1+.
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(BuiltInRegistries.ITEM, Veskorius.MOD_ID);

    public static final DeferredItem<Item> RAW_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("raw_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> STABLE_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("stable_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> REFINED_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("refined_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> RESONANCE_COMPONENT =
        ITEMS.registerSimpleItem("resonance_component", new Item.Properties().stacksTo(64));

    // BlockItem du Resonance Stabilizer (voir ModBlocks.java)
    public static final DeferredItem<BlockItem> RESONANCE_STABILIZER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_STABILIZER);
}
