package com.veskorius.block;

import com.veskorius.Veskorius;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NOTE IMPORTANTE : ceci enregistre le Resonance Stabilizer comme un bloc
 * simple (registerSimpleBlock), sans BlockEntity ni logique de craft.
 *
 * C'est volontaire (voir 09-Development, principe "small focused changes") :
 * on prouve d'abord que le bloc existe, se pose et se casse correctement
 * avant d'ajouter la logique de stabilisation (30s, input Quartz + Raw
 * Resonance Crystal -> Stable Resonance Crystal, voir TECH-SPEC.md).
 *
 * Prochaine etape de code : remplacer Block par une classe
 * ResonanceStabilizerBlock + un ResonanceStabilizerBlockEntity qui implemente
 * le cycle de craft (voir references/neoforge-api.md du skill minecraft-mod-dev
 * pour le pattern Block Entity + Menu/GUI).
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Veskorius.MOD_ID);

    public static final DeferredBlock<Block> RESONANCE_STABILIZER =
        BLOCKS.registerSimpleBlock("resonance_stabilizer",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.5f, 6.0f)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops());
}
