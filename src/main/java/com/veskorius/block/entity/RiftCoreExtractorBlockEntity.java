package com.veskorius.block.entity;

import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Rift Core Extractor</b> (machine #20, 05-Machines.md) : placé près d'une Faille
 * <b>ancrée</b>, il en tire 1 Rift Essence toutes les 120 s, six fois au maximum, pour
 * 15 Osc/tick. Une extraction sur sept environ rend en prime un lingot corrompu.
 *
 * <p><b>C'est la fin du jeu, et elle s'épuise.</b> La Rift Essence est la seule ressource
 * volontairement finie du mod (04-Materials.md) — six par Faille, et la Faille est morte.
 * Le dossier a explicitement rejeté toute machine de régénération : ce qui rend la fin
 * satisfaisante n'est pas qu'elle soit riche, c'est qu'elle soit <b>comptée</b>.
 *
 * <p><b>Le compteur vit sur le noyau, pas ici.</b> Sur l'Extractor, il suffirait de casser
 * la machine et d'en reposer une pour repartir de zéro — la ressource finie deviendrait
 * infinie au prix d'un aller-retour à l'établi, et tout le pilier tomberait sans qu'aucune
 * ligne de code n'ait l'air fausse.
 *
 * <p><b>Il exige une Faille ANCRÉE.</b> Sans cette condition, on pourrait extraire d'une
 * Faille sauvage, et le Rift Anchor — la machine la plus chère du mod à faire tourner —
 * n'aurait plus aucune raison d'être posée.
 */
public class RiftCoreExtractorBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_BONUS = 1;
    public static final int SLOT_AUGMENT = 2;
    public static final int SLOT_COUNT = 3;

    /** 120 s par extraction (05-Machines.md #20). */
    private static final int CYCLE_TICKS = 20 * 120;
    private static final int OSC_PER_TICK = 15;

    /** Portée de travail : la même que celle de l'Ancre, pour un site cohérent. */
    public static final int REACH = RiftAnchorBlockEntity.REACH;

    /** 15 % de chance d'un lingot corrompu en prime (04-Materials.md). */
    private static final float CORRUPTED_CHANCE = 0.15f;

    public RiftCoreExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIFT_CORE_EXTRACTOR.get(), pos, state, SLOT_COUNT);
    }

    @Override
    protected int getBaseCycleTicks() {
        return CYCLE_TICKS;
    }

    @Override
    protected int getOscPerTick() {
        return OSC_PER_TICK;
    }

    @Override
    protected boolean canRunCycle() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        RiftCoreBlockEntity core = RiftCoreBlockEntity.nearest(serverLevel, worldPosition, REACH);
        return core != null && core.isAnchored() && core.canExtract()
            && canInsertInto(SLOT_OUTPUT, essence());
    }

    @Override
    protected void runCycle() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        RiftCoreBlockEntity core = RiftCoreBlockEntity.nearest(serverLevel, worldPosition, REACH);
        // Re-vérifié au moment de produire : deux cycles de deux minutes peuvent se
        // terminer sur la même dernière extraction, et un contrôle fait seulement au
        // démarrage laisserait la Faille en rendre sept.
        if (core == null || !core.consumeExtraction()) {
            return;
        }
        insertInto(SLOT_OUTPUT, essence());

        if (serverLevel.getRandom().nextFloat() < CORRUPTED_CHANCE) {
            ItemStack corrupted = new ItemStack(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT.get());
            if (canInsertInto(SLOT_BONUS, corrupted)) {
                insertInto(SLOT_BONUS, corrupted);
            }
            // Slot de prime plein : le lingot est perdu, l'extraction compte quand même.
            // C'est volontaire — bloquer la fin du jeu sur un slot secondaire plein serait
            // une punition sans rapport avec ce que le joueur essaie de faire.
        }
    }

    private static ItemStack essence() {
        return new ItemStack(ModItems.RIFT_ESSENCE.get());
    }

    /** Extractions restantes sur la Faille servie, ou −1 si aucune à portée. */
    public int getExtractionsLeft() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return -1;
        }
        RiftCoreBlockEntity core = RiftCoreBlockEntity.nearest(serverLevel, worldPosition, REACH);
        return core == null ? -1 : core.getExtractionsLeft();
    }

    /** Rien ne s'insère : la matière vient de la Faille. */
    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return isAugmentSlot(slot) && super.isItemValid(slot, stack);
    }

    @Override
    protected int[] getAutomationInputSlots() {
        return new int[0];
    }

    @Override
    protected int[] getAutomationOutputSlots() {
        return new int[] {SLOT_OUTPUT, SLOT_BONUS};
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.rift_core_extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.RiftCoreExtractorMenu(containerId, playerInventory, this);
    }
}
