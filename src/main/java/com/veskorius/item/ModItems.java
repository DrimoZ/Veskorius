package com.veskorius.item;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Items de la chaine de raffinage principale (voir veskorius-design/04-Materials.md,
 * groupe 1) : Raw -> Stable -> Refined, plus le Resonance Component consomme par
 * les machines a partir du T1.
 *
 * NB : c'est bien {@code DeferredRegister.createItems} et non
 * {@code DeferredRegister.create(BuiltInRegistries.ITEM, ...)} — seule la
 * sous-classe {@link DeferredRegister.Items} expose les helpers
 * {@code registerSimpleItem} / {@code registerSimpleBlockItem}.
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Veskorius.MOD_ID);

    public static final DeferredItem<Item> RAW_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("raw_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> STABLE_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("stable_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> REFINED_RESONANCE_CRYSTAL =
        ITEMS.registerSimpleItem("refined_resonance_crystal", new Item.Properties().stacksTo(64));

    public static final DeferredItem<Item> RESONANCE_COMPONENT =
        ITEMS.registerSimpleItem("resonance_component", new Item.Properties().stacksTo(64));

    /** Outil transversal de configuration des machines (05-Machines.md). */
    public static final DeferredItem<ResonanceTunerItem> RESONANCE_TUNER =
        ITEMS.registerItem("resonance_tuner",
            ResonanceTunerItem::new, new Item.Properties().stacksTo(1));

    // BlockItems des machines (voir ModBlocks.java)
    public static final DeferredItem<BlockItem> RESONANCE_STABILIZER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_STABILIZER);

    public static final DeferredItem<BlockItem> COMPONENT_ASSEMBLER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.COMPONENT_ASSEMBLER);

    public static final DeferredItem<BlockItem> RESONANCE_WHETSTONE_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_WHETSTONE);

    public static final DeferredItem<BlockItem> FLUX_PURIFIER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.FLUX_PURIFIER);

    public static final DeferredItem<BlockItem> FIELD_EMITTER_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.FIELD_EMITTER);
}
