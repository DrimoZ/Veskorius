package com.veskorius.item;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Resonance Tuner (05-Machines.md, outil transversal). Outil de configuration à
 * modes : il porte un mode courant (Data Component), un clic droit sur une machine
 * applique l'action du mode, un shift-clic droit change de mode.
 *
 * Modes implémentés maintenant : Pivoter, On/Off manuel, Surchauffe, Redstone —
 * tous adossés à la couche de contrôle de {@link AbstractMachineBlockEntity}. Les
 * fonctions liées à du contenu plus tardif (priorité du Network Hub, recalibration
 * de l'Amplifier, retrait d'un Catalyst Core) s'ajouteront comme nouveaux modes
 * quand ces machines/objets existeront (phases 3-4).
 */
public class ResonanceTunerItem extends Item {

    public ResonanceTunerItem(Properties properties) {
        super(properties);
    }

    // --- Interaction ---------------------------------------------------------

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (context.isSecondaryUseActive()) {
            // Shift-clic : change de mode, sans agir sur la machine.
            if (!level.isClientSide) {
                cycleMode(stack, player);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean applied = applyMode(modeOf(stack), level, context.getClickedPos(), player);
        return applied ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // Shift-clic dans le vide : change aussi de mode (pratique loin d'une machine).
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                cycleMode(stack, player);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    // --- Logique de mode -----------------------------------------------------

    public static TunerMode modeOf(ItemStack stack) {
        Integer index = stack.get(ModDataComponents.TUNER_MODE.get());
        return TunerMode.byIndex(index == null ? 0 : index);
    }

    private static void cycleMode(ItemStack stack, @Nullable Player player) {
        TunerMode next = modeOf(stack).next();
        stack.set(ModDataComponents.TUNER_MODE.get(), next.ordinal());
        actionBar(player, Component.translatable("item.veskorius.resonance_tuner.mode", next.label()));
    }

    /**
     * Applique un mode à la machine en {@code pos}. Retourne vrai si quelque chose
     * a changé. Extrait ici (statique, sans {@link UseOnContext}) pour être
     * directement testable par GameTest.
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

    private static void actionBar(@Nullable Player player, Component message) {
        if (player != null) {
            player.displayClientMessage(message, true);
        }
    }

    // --- Tooltip -------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.veskorius.resonance_tuner.mode", modeOf(stack).label()));
        tooltip.add(Component.translatable("item.veskorius.resonance_tuner.hint"));
    }
}
