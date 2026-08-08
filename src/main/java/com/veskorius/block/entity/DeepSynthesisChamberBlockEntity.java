package com.veskorius.block.entity;

import com.veskorius.config.VeskoriusConfig;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Deep Synthesis Chamber</b> (machine #15, 05-Machines.md) : 2 Refined Crystal →
 * 1 Hyper Refined Crystal, 90 s (45 s en surchauffe), 8 Osc/tick (16 en surchauffe).
 *
 * <p><b>C'est elle qui ferme la boucle du T4.</b> Le Hyper Refined n'existe nulle part
 * ailleurs — il ne se mine pas, ne se trouve pas, ne se craft pas. Les trois exemplaires
 * de l'Archive Régionale sont le stock de départ du palier, et l'un des trois est
 * <b>consommé pour construire cette Chambre</b> : il devient son catalyseur permanent, et
 * n'apparaît plus jamais comme entrée de cycle. Une fois posée, la ressource devient
 * renouvelable. Avant, elle ne l'est pas.
 *
 * <p>Ce détail de recette est ce qui rend le choix du palier réel : deux cristaux partent
 * dans le Treillis du premier Amplificateur, le troisième ici. On ne peut pas faire les
 * deux (05-Machines.md, « Bootstrap du T4 »).
 *
 * <p><b>Surchauffe.</b> Deuxième machine du mod à l'accepter, après le Flux Purifier, et
 * c'est voulu : le pari « deux fois plus vite, deux fois plus cher, une chance sur cinq de
 * tout perdre » doit se retrouver sur la ressource la plus chère du jeu, pas seulement sur
 * une intermédiaire. La décision de perte est celle du Purifier, réutilisée telle quelle —
 * deux implémentations divergeraient au premier réglage de config.
 */
public class DeepSynthesisChamberBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_AUGMENT = 2;
    public static final int SLOT_COUNT = 3;

    public DeepSynthesisChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DEEP_SYNTHESIS_CHAMBER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.SYNTHESIS::get, new int[] {SLOT_INPUT}, SLOT_OUTPUT);
    }

    @Override
    public boolean supportsOverheat() {
        return true;
    }

    @Override
    protected boolean shouldProduceResult() {
        return !FluxPurifierBlockEntity.losesInput(isOverheatActive(), isCurrentRecipeStable(),
            com.veskorius.config.MachinesConfig.overheatIgnoresStable(),
            VeskoriusConfig.overheatInputLossChance(), level.getRandom().nextFloat());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.deep_synthesis_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.DeepSynthesisChamberMenu(containerId, playerInventory, this);
    }
}
