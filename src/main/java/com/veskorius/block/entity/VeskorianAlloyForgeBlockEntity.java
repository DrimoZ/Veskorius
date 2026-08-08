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
 * <b>Veskorian Alloy Forge</b> (machine #10, 05-Machines.md) — la porte d'entrée du T3.
 *
 * <p>2 Refined Crystal + 2 lingots → 1 lingot d'alliage <b>+ 1 scorie de flux</b>, 20 s,
 * 4 Osc/tick. Le métal d'entrée décide de la branche : <b>fer</b> pour l'alliage
 * structurel, <b>or</b> pour le conducteur — seul admis par le Resonance Relay. C'est un
 * vrai choix de planification (04-Materials.md) et non une variante cosmétique : les deux
 * lingots ne sont pas interchangeables.
 *
 * <p><b>La scorie n'est pas une recette, c'est une propriété de la machine.</b> Elle sort
 * à <i>chaque</i> cycle, quelle que soit la branche, et elle occupe son propre slot. Deux
 * conséquences voulues :
 * <ul>
 *   <li>elle ne peut pas être « oubliée » en ajoutant une recette par datapack — un
 *       datapack qui ajoute un alliage produira sa scorie comme les autres ;</li>
 *   <li><b>slot de scorie plein = forge à l'arrêt.</b> C'est ce qui donne sa raison d'être
 *       au Slag Vent (#13) et, plus loin, au recyclage : le déchet n'est pas un texte de
 *       lore, c'est une contrainte d'exploitation. Le joueur reproduit en miniature ce qui
 *       a causé l'Effondrement (02-Lore.md), et il doit s'en occuper.</li>
 * </ul>
 */
public class VeskorianAlloyForgeBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_CRYSTAL = 0;
    public static final int SLOT_METAL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_SLAG = 3;
    public static final int SLOT_AUGMENT = 4;
    public static final int SLOT_COUNT = 5;

    public VeskorianAlloyForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VESKORIAN_ALLOY_FORGE.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.FORGING::get, new int[] {SLOT_CRYSTAL, SLOT_METAL}, SLOT_OUTPUT);
    }

    /** La scorie sort aussi : sans place pour elle, la forge ne démarre pas. */
    @Override
    protected boolean canRunCycle() {
        return super.canRunCycle() && canInsertInto(SLOT_SLAG, slag());
    }

    @Override
    protected void runCycle() {
        // LU AVANT DE CONSOMMER. super.runCycle() vide les slots d'entrée, après quoi
        // plus aucune recette ne correspond et le sous-produit reviendrait VIDE — la
        // scorie disparaîtrait purement et simplement. Le piège n'existait pas tant que
        // la scorie était une constante Java ; il est apparu le jour où elle est devenue
        // une donnée de recette, et seul le test l'a vu.
        ItemStack slag = slag();
        super.runCycle();
        insertInto(SLOT_SLAG, slag);
    }

    /**
     * La scorie vient de la RECETTE, plus d'une constante. Elle était écrite en dur ici :
     * un datapack pouvait rééquilibrer ce que la Forge produit, jamais ce qu'elle gâche,
     * alors que la contrainte de déchet est une décision d'équilibrage comme une autre.
     */
    private ItemStack slag() {
        return recipeByproduct();
    }

    /** Les deux slots de sortie s'automatisent ; les entrées restent des entrées. */
    @Override
    protected int[] getAutomationOutputSlots() {
        return new int[] {SLOT_OUTPUT, SLOT_SLAG};
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.veskorian_alloy_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.VeskorianAlloyForgeMenu(containerId, playerInventory, this);
    }
}
