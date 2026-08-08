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
 * <b>Reclaimer</b> (T3, 05-Machines.md « Ajouts de la révision harmonique ») : il
 * <b>re-stabilise les déchets</b>. Un lot de scorie ou de boue entre, une fraction de
 * matériau de base sort.
 *
 * <p><b>Il ferme la seule boucle du mod qui restait ouverte.</b> Trois machines produisent
 * des déchets et, jusqu'ici, deux d'entre eux ne menaient nulle part : la scorie de la
 * Forge ne pouvait qu'être <i>détruite</i> par un Slag Vent, et la boue du Damping Array
 * n'avait strictement aucun destinataire — elle s'entassait. Le dossier est explicite sur
 * le fait que ce n'était pas l'intention : « usages des déchets, essentiels pour que ce ne
 * soit pas juste de la suppression » (16-Revision-and-Expansion.md).
 *
 * <p><b>Une fraction, et pas une restitution.</b> Quatre scories rendent un gravier de
 * scorie, quatre boues rendent une poussière de résonance. Le taux est délibérément
 * mauvais : recycler doit rester moins rentable que miner, sinon la boucle remplacerait
 * l'exploration au lieu de la prolonger. Ce que le joueur achète ici, ce n'est pas du
 * rendement — c'est de ne plus avoir à jeter.
 *
 * <p><b>Le Slag Vent survit, et garde son rôle.</b> Le Reclaimer ne le remplace pas : il
 * demande un champ, un cycle et de la place, là où l'Évent ne demande que d'exister. Qui
 * veut se débarrasser de la scorie sans y penser continue de venter ; qui veut la
 * récupérer paie l'infrastructure. La contrainte de la Forge reste entière, elle gagne
 * seulement une seconde réponse.
 */
public class ReclaimerBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_AUGMENT = 2;
    public static final int SLOT_COUNT = 3;

    public ReclaimerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECLAIMER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.RECLAIMING::get, new int[] {SLOT_INPUT}, SLOT_OUTPUT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.reclaimer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.ReclaimerMenu(containerId, playerInventory, this);
    }
}
