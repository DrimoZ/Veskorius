package com.veskorius.block.entity;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

/**
 * Rattache le Raw Flux Deposit au type de block entity vanilla
 * {@code BRUSHABLE_BLOCK}. {@link net.minecraft.world.level.block.entity.BrushableBlockEntity}
 * est construit avec ce type en dur ; sans cet ajout, poser notre bloc ne créerait
 * pas de block entity valide et le brossage ne marcherait pas.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModBrushableBlocks {

    private ModBrushableBlocks() {
    }

    @SubscribeEvent
    public static void addBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BRUSHABLE_BLOCK, ModBlocks.RAW_FLUX_DEPOSIT.get());
    }
}
