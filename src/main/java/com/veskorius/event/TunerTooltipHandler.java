package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.item.ResonanceTunerItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Bloc « commandes » du tooltip du Resonance Tuner (12-UX-and-Advancements.md) : replié
 * derrière ⇧ pour ne pas noyer la liste des modes, qui est l'information utile au
 * quotidien.
 *
 * <p>Pourquoi ici et pas dans {@code ResonanceTunerItem.appendHoverText} : savoir si Shift
 * est enfoncé passe par {@link Screen}, une classe du package {@code net.minecraft.client}
 * qui <b>n'existe pas sur un serveur dédié</b>. La référencer depuis l'item — instancié des
 * deux côtés — arme un {@code NoClassDefFoundError} qui part au premier appelant serveur de
 * {@code appendHoverText}. Un handler {@link Dist#CLIENT} n'est, lui, jamais chargé
 * ailleurs. Même motif que {@link ItemHintHandler}.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, value = Dist.CLIENT)
public final class TunerTooltipHandler {

    private TunerTooltipHandler() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof ResonanceTunerItem)) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.veskorius.resonance_tuner.controls")
                .withStyle(ChatFormatting.YELLOW));
            control(tooltip, "item.veskorius.resonance_tuner.ctrl_apply");
            control(tooltip, "item.veskorius.resonance_tuner.ctrl_cycle");
            control(tooltip, "item.veskorius.resonance_tuner.ctrl_dismantle");
        } else {
            tooltip.add(Component.literal("⇧ ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("tooltip.veskorius.hold_shift")
                    .withStyle(ChatFormatting.GRAY)));
        }
    }

    private static void control(List<Component> tooltip, String key) {
        tooltip.add(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.translatable(key).withStyle(ChatFormatting.GRAY)));
    }
}
