package com.veskorius.block.entity;

import com.veskorius.menu.CrystalCrusherMenu;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #22 (05-Machines.md). Sa recette de fonctionnement (1 Raw Crystal →
 * 3 Resonance Dust, 10 s, autonome) vit en JSON — voir le type de recette
 * {@code veskorius:crushing}. Cette classe ne fait que déclarer sa disposition
 * de slots ; tout le cycle est dans {@link AbstractProcessingMachineBlockEntity}.
 *
 * Première machine « traitement » à une seule entrée : elle vérifie que le socle
 * ne suppose pas deux slots d'entrée comme le Stabilizer ou l'Assembler.
 */
public class CrystalCrusherBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_AUGMENT = 2;
    public static final int SLOT_COUNT = 3;

    public CrystalCrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_CRUSHER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.CRUSHING::get, new int[] {SLOT_INPUT}, SLOT_OUTPUT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.crystal_crusher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CrystalCrusherMenu(containerId, playerInventory, this);
    }
}
