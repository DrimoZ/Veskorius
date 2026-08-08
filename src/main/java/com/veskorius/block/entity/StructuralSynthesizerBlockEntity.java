package com.veskorius.block.entity;

import com.veskorius.item.ModItems;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Structural Synthesizer</b> (machine #11, 05-Machines.md) : 4 lingots d'alliage +
 * 8 pierres → 4 Veskorian Alloy Block <b>+ 1 Synthesis Residue</b>, 60 s.
 *
 * <p>C'est la machine qui rend le palier <b>bâtissable</b> : sans elle l'alliage reste un
 * lingot d'artisanat, avec elle il devient un matériau de construction, et c'est ce qui
 * permet aux machines T5 d'exiger des blocs entiers sans que la demande soit absurde.
 *
 * <p><b>Le résidu suit la même règle que la scorie de la Forge</b> : ce n'est pas une ligne
 * de recette mais une propriété de la machine, il sort à chaque cycle dans son propre slot,
 * et <b>slot plein = synthétiseur à l'arrêt</b>. Le T3 est le palier où le déchet cesse
 * d'être un texte de lore : les deux machines qui fabriquent la matière du palier
 * produisent chacune la leur, et il faut s'en occuper (voir 02-Lore.md sur l'Effondrement,
 * dont le joueur reproduit ici la cause en miniature).
 *
 * <p>Le résidu est <b>distinct</b> de la scorie, et pas par cosmétique : la scorie sort
 * d'une fusion, le résidu d'un moulage. Le Slag Vent (#13) ne sait évacuer que la première
 * — le synthétiseur, lui, ne se déverrouille pas tout seul, il faut vider son slot.
 */
public class StructuralSynthesizerBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_ALLOY = 0;
    public static final int SLOT_STONE = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_RESIDUE = 3;
    public static final int SLOT_AUGMENT = 4;
    public static final int SLOT_COUNT = 5;

    public StructuralSynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRUCTURAL_SYNTHESIZER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.SYNTHESIZING::get, new int[] {SLOT_ALLOY, SLOT_STONE}, SLOT_OUTPUT);
    }

    @Override
    protected boolean canRunCycle() {
        return super.canRunCycle() && canInsertInto(SLOT_RESIDUE, residue());
    }

    @Override
    protected void runCycle() {
        super.runCycle();
        insertInto(SLOT_RESIDUE, residue());
    }

    private static ItemStack residue() {
        return new ItemStack(ModItems.SYNTHESIS_RESIDUE.get());
    }

    @Override
    protected int[] getAutomationOutputSlots() {
        return new int[] {SLOT_OUTPUT, SLOT_RESIDUE};
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.structural_synthesizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.StructuralSynthesizerMenu(containerId, playerInventory, this);
    }
}
