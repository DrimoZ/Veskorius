package com.veskorius.item;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Resonance Tuner (05-Machines.md, outil transversal). Outil de configuration à
 * modes : il porte un mode courant (Data Component).
 *
 * Gestes (voir 12-UX-and-Advancements.md) :
 * - Clic droit sur une machine : applique le mode courant, SANS ouvrir le GUI.
 * - Clic droit dans le vide : passe au mode suivant.
 * - Shift + clic droit sur un bloc-entité : le démonte (bloc + contenu → inventaire).
 *
 * Les deux interactions sur un bloc (clic / shift-clic) sont gérées par
 * {@link TunerInteractions} via {@code PlayerInteractEvent.RightClickBlock}, et
 * non par {@code useOn} : sinon l'interaction du bloc (ouverture du GUI) gagne la
 * priorité sur un clic droit sans shift et l'action ne se déclenche jamais. Seul le
 * changement de mode (clic droit dans le vide) reste dans {@link #use}.
 */
public class ResonanceTunerItem extends Item {

    public ResonanceTunerItem(Properties properties) {
        super(properties);
    }

    // --- Interaction : changement de mode (clic droit dans le vide) ----------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            cycleMode(stack, player);
        }
        return InteractionResultHolder.success(stack);
    }

    // --- Logique de mode -----------------------------------------------------

    public static TunerMode modeOf(ItemStack stack) {
        Integer index = stack.get(ModDataComponents.TUNER_MODE.get());
        return TunerMode.byIndex(index == null ? 0 : index);
    }

    public static void cycleMode(ItemStack stack, @Nullable Player player) {
        TunerMode next = modeOf(stack).next();
        stack.set(ModDataComponents.TUNER_MODE.get(), next.ordinal());
        actionBar(player, Component.translatable("item.veskorius.resonance_tuner.mode", next.label()));
    }

    /**
     * Applique un mode à la machine en {@code pos}. Retourne vrai si quelque chose
     * a changé. Statique et sans contexte d'interaction pour être directement
     * testable par GameTest.
     */
    public static boolean applyMode(TunerMode mode, Level level, BlockPos pos, @Nullable Player player) {
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);

        switch (mode) {
            case ROTATE -> {
                if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    return false;
                }
                level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()), 3);
                actionBar(player, Component.translatable("item.veskorius.resonance_tuner.rotated"));
                return true;
            }
            case POWER -> {
                if (!(be instanceof AbstractMachineBlockEntity machine)) {
                    return false;
                }
                machine.toggleManual();
                actionBar(player, Component.translatable(machine.isManualEnabled()
                    ? "gui.veskorius.machine_on" : "gui.veskorius.machine_off"));
                return true;
            }
            case OVERHEAT -> {
                if (!(be instanceof AbstractMachineBlockEntity machine) || !machine.supportsOverheat()) {
                    actionBar(player, Component.translatable("item.veskorius.resonance_tuner.no_overheat"));
                    return false;
                }
                machine.toggleOverheat();
                actionBar(player, Component.translatable(machine.isOverheatEnabled()
                    ? "gui.veskorius.overheat_on" : "gui.veskorius.overheat_off"));
                return true;
            }
            case REDSTONE -> {
                if (!(be instanceof AbstractMachineBlockEntity machine)) {
                    return false;
                }
                machine.cycleRedstoneMode();
                actionBar(player, Component.translatable("gui.veskorius.redstone_control")
                    .append(": ").append(machine.getRedstoneMode().label()));
                return true;
            }
        }
        return false;
    }

    static void actionBar(@Nullable Player player, Component message) {
        if (player != null) {
            player.displayClientMessage(message, true);
        }
    }

    // --- Tooltip -------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        TunerMode currentMode = modeOf(stack);

        tooltip.add(Component.literal("▶ ")
            .withStyle(ChatFormatting.DARK_AQUA)
            .append(Component.translatable("item.veskorius.resonance_tuner.current_mode")
                .withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" "))
            .append(currentMode.label().copy().withStyle(ChatFormatting.GOLD)));

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.veskorius.resonance_tuner.available_modes")
            .withStyle(ChatFormatting.YELLOW));

        for (TunerMode mode : TunerMode.values()) {
            boolean selected = mode == currentMode;
            tooltip.add(Component.literal(selected ? " ● " : " ○ ")
                .withStyle(selected ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)
                .append(mode.label().copy().withStyle(selected ? ChatFormatting.GREEN : ChatFormatting.GRAY)));
        }

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
