package com.veskorius.block.entity;

import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Advanced Assembler</b> (T3, 05-Machines.md « Ajouts de la révision harmonique ») :
 * il compose la <b>Matrice de Résonance</b>, pièce intermédiaire que le dossier veut
 * « requise par les machines T4 ».
 *
 * <p><b>Son seul rôle est d'ajouter une étape, et c'est un vrai rôle.</b> Sans lui, les
 * machines du palier le plus haut se fabriquaient avec des <i>Composants de Résonance</i>
 * — une pièce T1, celle qu'on assemble dans la première heure de jeu. La chaîne sautait
 * donc deux paliers d'un coup, et le T3 tout entier n'apportait rien à ce qu'on bâtissait
 * ensuite : on y forgeait de l'alliage pour des blocs de construction, puis on repartait
 * au T4 avec le même Composant qu'au T1.
 *
 * <p><b>Et il fait payer la branche du métal une troisième fois.</b> La matrice réclame de
 * l'alliage <b>conducteur</b>, pas structurel. Le choix fait à la Forge — fer ou or — se
 * répercute donc sur le Relais, sur le Treillis Harmonique, et maintenant sur chaque
 * machine T4. Une décision prise au début du palier continue de coûter jusqu'à sa fin :
 * c'est ce qui la rend planifiable plutôt qu'anecdotique.
 *
 * <p>Deux entrées, comme la Forge : les Composants d'un côté, le métal de l'autre. La
 * disposition est déjà portée par le socle ; cette classe ne déclare que ses slots.
 */
public class AdvancedAssemblerBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_COMPONENT = 0;
    public static final int SLOT_METAL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    public AdvancedAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_ASSEMBLER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.ADVANCED_ASSEMBLING::get, new int[] {SLOT_COMPONENT, SLOT_METAL},
            SLOT_OUTPUT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.advanced_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.AdvancedAssemblerMenu(containerId, playerInventory, this);
    }
}
