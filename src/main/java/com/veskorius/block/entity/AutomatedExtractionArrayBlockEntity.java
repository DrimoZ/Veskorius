package com.veskorius.block.entity;

import com.veskorius.energy.ResonanceFieldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Automated Extraction Array</b> (machine #16, 05-Machines.md) : synchronise les Deep
 * Crystal Driller déjà posés. Continu, 10 Osc/tick.
 *
 * <p>Elle répond à la corvée que la Foreuse crée. Une Foreuse seule épuise sa colonne puis
 * s'arrête, et son slot de sortie se remplit : il faut descendre, vider, déplacer. Avec
 * quatre ou cinq foreuses au fond d'une mine, c'est la seule chose que le joueur fait de
 * son T3. L'Array supprime ce va-et-vient de deux façons, et pas une de plus :
 *
 * <ol>
 *   <li><b>Elle ramasse.</b> Chaque passage vide la sortie de toutes les foreuses à portée
 *       dans son propre coffre. Un seul point de collecte pour tout un fond de mine.</li>
 *   <li><b>Elle synchronise.</b> Une foreuse qu'elle sert tourne <b>deux fois plus vite</b>
 *       — c'est le sens du mot du dossier : elles ne creusent plus chacune dans son coin,
 *       elles travaillent au même rythme sous une même commande.</li>
 * </ol>
 *
 * <p><b>Le signal est POUSSÉ vers les foreuses, jamais tiré par elles.</b> Le réflexe
 * inverse — chaque foreuse cherche un Array à chaque tick — coûterait un balayage de
 * voisinage par foreuse et par tick, sur des machines qu'on pose justement par paquets. Ici
 * l'Array marque celles qu'il sert à chaque passage, et la marque expire toute seule si
 * l'Array s'arrête ou est cassé. Aucun désenregistrement à écrire, donc aucun moyen de
 * l'oublier.
 */
public class AutomatedExtractionArrayBlockEntity extends AbstractMachineBlockEntity {

    /** Un coffre entier : c'est un point de collecte, pas un tampon. */
    public static final int SLOT_COUNT = 10;
    public static final int SLOT_AUGMENT = 9;

    /** Rayon de commande, en blocs. Couvre un fond de mine, pas une région. */
    public static final int RADIUS = 12;

    /** Période de ramassage. 5 s : assez pour ne pas balayer souvent, assez pour suivre. */
    private static final int SWEEP_PERIOD = 20 * 5;

    private static final int OSC_PER_TICK = 10;

    private int sweepTimer;
    private int lastServed;

    public AutomatedExtractionArrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOMATED_EXTRACTION_ARRAY.get(), pos, state, SLOT_COUNT);
    }

    /**
     * L'Array n'a pas de cycle au sens du socle : elle ne transforme rien. Le socle
     * n'accepte pourtant que des machines à cycle, donc on lui en donne un — court, sans
     * condition d'entrée, dont le seul effet est le balayage. C'est ce qui lui fait payer
     * ses 10 Osc/tick par le chemin normal : une machine qui inventerait sa propre
     * comptabilité d'énergie échapperait au Hub, à la dissonance et au désaccord.
     */
    @Override
    protected int getBaseCycleTicks() {
        return SWEEP_PERIOD;
    }

    @Override
    protected int getOscPerTick() {
        return OSC_PER_TICK;
    }

    @Override
    protected boolean canRunCycle() {
        return true;
    }

    @Override
    protected void runCycle() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        sweep(serverLevel);
    }

    /** Marque et vide toutes les foreuses à portée. */
    private void sweep(ServerLevel level) {
        int served = 0;
        long radiusSqr = (long) RADIUS * RADIUS;
        for (BlockPos p : BlockPos.betweenClosed(
            worldPosition.offset(-RADIUS, -RADIUS, -RADIUS),
            worldPosition.offset(RADIUS, RADIUS, RADIUS))) {
            if (p.distSqr(worldPosition) > radiusSqr || !level.isLoaded(p)) {
                continue;
            }
            if (!(level.getBlockEntity(p) instanceof DeepCrystalDrillerBlockEntity driller)) {
                continue;
            }
            // La marque vaut jusqu'au prochain passage, plus une marge : si l'Array
            // s'arrête, elle expire d'elle-même et la foreuse reprend son rythme normal.
            driller.markSynchronised(SWEEP_PERIOD * 2);
            collectFrom(driller);
            served++;
        }
        lastServed = served;
        sweepTimer = 0;
        setChanged();
    }

    /** Vide la sortie d'une foreuse dans le coffre, autant que la place le permet. */
    private void collectFrom(DeepCrystalDrillerBlockEntity driller) {
        ItemStack held = driller.getInventory()
            .getStackInSlot(DeepCrystalDrillerBlockEntity.SLOT_OUTPUT);
        if (held.isEmpty()) {
            return;
        }
        ItemStack leftover = ItemHandlerHelper.insertItem(inventory, held.copy(), false);
        driller.getInventory().setStackInSlot(
            DeepCrystalDrillerBlockEntity.SLOT_OUTPUT, leftover);
    }

    /** Nombre de foreuses servies au dernier passage. Pour l'infobulle. */
    public int getLastServed() {
        return lastServed;
    }

    /**
     * Rien ne s'insère à la main : le contenu vient des foreuses. Sans ça, un hopper
     * remplirait le coffre de gravier et le ramassage s'arrêterait sans explication.
     */
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
        int[] slots = new int[SLOT_AUGMENT];
        for (int i = 0; i < SLOT_AUGMENT; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("sweepTimer", sweepTimer);
        tag.putInt("lastServed", lastServed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        sweepTimer = tag.getInt("sweepTimer");
        lastServed = tag.getInt("lastServed");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.automated_extraction_array");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.AutomatedExtractionArrayMenu(containerId, playerInventory, this);
    }
}
