package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Signalétique de progression (12-UX-and-Advancements.md, onboarding) : une ligne
 * d'indice grisée sur les objets clés du début de partie, pour que la boucle T1
 * (miner → stabiliser → assembler/purifier) s'apprenne depuis l'objet lui-même,
 * sans dépendre de JEI ni d'un wiki.
 *
 * Handler client (les tooltips ne se rendent que côté client). Les indices sont
 * volontairement courts et pointent vers l'étape suivante sans tout dévoiler ;
 * le texte vit dans les fichiers de langue (clés {@code item.veskorius.<id>.hint}).
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, value = Dist.CLIENT)
public final class ItemHintHandler {

    private ItemHintHandler() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        String key = hintKey(event.getItemStack());
        if (key != null) {
            event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }

    /** Clé de langue de l'indice pour cet objet, ou {@code null} s'il n'en a pas. */
    @Nullable
    private static String hintKey(ItemStack stack) {
        if (stack.is(ModItems.RAW_RESONANCE_CRYSTAL.get())) {
            return "item.veskorius.raw_resonance_crystal.hint";
        }
        if (stack.is(ModItems.STABLE_RESONANCE_CRYSTAL.get())) {
            return "item.veskorius.stable_resonance_crystal.hint";
        }
        if (stack.is(ModItems.RESONANCE_COMPONENT.get())) {
            return "item.veskorius.resonance_component.hint";
        }
        if (stack.is(ModItems.RESONANCE_DUST.get())) {
            return "item.veskorius.resonance_dust.hint";
        }
        return null;
    }
}
