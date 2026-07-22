package com.veskorius.block.entity;

import com.veskorius.entity.CrystalStriderEntity;
import com.veskorius.menu.CrystalRoostMenu;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Crystal Roost (05-Machines.md #8, 09-Entities.md) : production passive de cristal
 * brut, alternative lente au minage (utile maintenant que les poches sont rares).
 *
 * Machine « traitement » autonome pilotée par une recette JSON
 * ({@code veskorius:roosting} : 2 Quartz → 1 Raw Resonance Crystal, 600 s), avec
 * **une condition en plus** que le socle ne connaît pas : au moins un Fileur de
 * Cristal doit se tenir à moins de {@link #FILEUR_RANGE} blocs. Sans Fileur, le
 * Roost est inerte (et ne consomme pas son Quartz).
 */
public class CrystalRoostBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_QUARTZ = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_AUGMENT = 2;
    public static final int SLOT_COUNT = 3;

    /** Rayon dans lequel un Fileur active le Roost (09-Entities.md : « moins de 6 blocs »). */
    private static final double FILEUR_RANGE = 6.0;

    public CrystalRoostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_ROOST.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.ROOSTING::get, new int[] {SLOT_QUARTZ}, SLOT_OUTPUT);
    }

    @Override
    protected boolean canRunCycle() {
        // Le socle vérifie recette + place en sortie ; on ajoute le Fileur proche.
        return super.canRunCycle() && hasFileurNearby();
    }

    public boolean hasFileurNearby() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        AABB area = new AABB(worldPosition).inflate(FILEUR_RANGE);
        return !serverLevel.getEntitiesOfClass(CrystalStriderEntity.class, area).isEmpty();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.crystal_roost");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CrystalRoostMenu(containerId, playerInventory, this);
    }
}
