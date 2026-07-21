package com.veskorius.block.entity;

import com.veskorius.menu.ComponentAssemblerMenu;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #2 (05-Machines.md). Recette de fonctionnement en JSON, type
 * {@code veskorius:assembling}. Consomme des Osc — mais c'est la recette qui porte
 * le coût (3 Osc/tick), plus une constante ici.
 *
 * La branche alternative prévue par 04-Materials.md (3 Resonance Dust + 2 Iron →
 * 2 Component) devient un simple second JSON de type {@code veskorius:assembling}
 * une fois le Resonance Dust codé (tâche 13) : aucune ligne de code à ajouter ici.
 */
public class ComponentAssemblerBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_CRYSTAL = 0;
    public static final int SLOT_IRON = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    public ComponentAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPONENT_ASSEMBLER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.ASSEMBLING::get, new int[] {SLOT_CRYSTAL, SLOT_IRON}, SLOT_OUTPUT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.component_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComponentAssemblerMenu(containerId, playerInventory, this);
    }
}
