package com.veskorius.block.entity;

import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.tag.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Socle commun a toutes les machines actives de Veskorius (05-Machines.md,
 * tableau "Recettes de fonctionnement").
 *
 * Ce que cette classe impose a ses 23 sous-classes futures :
 *
 * 1. Un inventaire unique, dont le DERNIER slot est TOUJOURS le slot d'augment.
 *    C'est la convention qui permet au slot d'augment d'exister sur toutes les
 *    machines sans code specifique, comme demande par 11-Development-Plan.md
 *    (Phase 1, tache 15 : "implementer le slot des cette phase pour eviter de le
 *    retrofit plus tard sur des machines deja codees").
 * 2. Un cycle unique : tant que {@link #canRunCycle()} est vrai, la progression
 *    monte d'un tick par tick ; arrivee au bout, {@link #runCycle()} est appelee.
 *    Si la condition redevient fausse en cours de route, la progression est
 *    remise a zero — pas de cycle "en pause" a mi-parcours.
 *
 * La consommation d'energie (Osc) n'est PAS geree ici : le Resonance Stabilizer
 * est autonome (05-Machines.md #1) et le systeme de champ n'arrive qu'a la
 * tache 5 de la Phase 1 (capability IResonanceField). Elle se greffera sur
 * {@link #canRunCycle()} a ce moment-la.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {

    /** +15% de vitesse permanents quand un augment occupe le slot (05-Machines.md). */
    private static final float AUGMENT_SPEED_MULTIPLIER = 1.15f;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_COUNT = 2;

    protected final ItemStackHandler inventory;

    private final int augmentSlot;
    private int progress;

    /**
     * Synchronisation client de la barre de progression. Passe par ContainerData
     * (mecanisme vanilla du four) plutot que par un packet custom — voir
     * 12-UX-and-Advancements.md, qui demande une barre "identique au four vanilla".
     *
     * DATA_MAX_PROGRESS est recalcule a la lecture au lieu d'etre stocke : le
     * maximum change des que l'augment est pose ou retire, et un champ stocke
     * finirait tot ou tard desynchronise.
     */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> getEffectiveCycleTicks();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_PROGRESS) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount) {
        super(type, pos, state);
        this.augmentSlot = slotCount - 1;
        this.inventory = new ItemStackHandler(slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return AbstractMachineBlockEntity.this.isItemValid(slot, stack);
            }
        };
    }

    // --- Contrat des sous-classes -------------------------------------------

    /** Duree d'un cycle en ticks, sans augment (20 ticks = 1 seconde). */
    protected abstract int getBaseCycleTicks();

    /** Entrees presentes ET place disponible en sortie. Evalue a chaque tick. */
    protected abstract boolean canRunCycle();

    /** Consomme les entrees et produit la sortie. Appele une seule fois par cycle. */
    protected abstract void runCycle();

    /**
     * Osc consommes par tick d'avancement (05-Machines.md, colonne Energie).
     * 0 = machine autonome (Stabilizer, Whetstone) : elle n'a besoin d'aucun
     * champ. Redefinir pour une machine qui puise dans le champ de Resonance.
     */
    protected int getOscPerTick() {
        return 0;
    }

    // --- Cycle ---------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractMachineBlockEntity machine) {
        machine.tickCycle();
    }

    private void tickCycle() {
        if (!canRunCycle()) {
            // Ingredient absent ou sortie pleine : le cycle ne peut pas exister,
            // on remet a zero (contrairement a une coupure d'energie ci-dessous).
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }

        if (!drawEnergy()) {
            // Pas assez d'Osc ce tick : on met en PAUSE (la progression est
            // conservee) plutot que de remettre a zero. Une coupure de courant
            // breve ne doit pas gacher le travail deja fait.
            return;
        }

        progress++;
        if (progress >= getEffectiveCycleTicks()) {
            runCycle();
            progress = 0;
        }
        setChanged();
    }

    /**
     * Preleve le cout d'un tick sur le champ de Resonance. Vrai si le plein cout a
     * ete obtenu (la machine peut avancer), faux sinon (pause).
     *
     * Un prelevement partiel (reserve d'emetteur presque vide) est tout de meme
     * consomme : c'est volontaire, il vide l'emetteur jusqu'a zero pour declencher
     * son rechargement au tick suivant. Le "gachis" ainsi induit vaut au plus
     * {@code cout - 1} Osc par cristal brule, soit 1 Osc sur 4000 en pratique.
     */
    private boolean drawEnergy() {
        int cost = getOscPerTick();
        if (cost <= 0) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return ResonanceFieldManager.supply(serverLevel, worldPosition, cost) >= cost;
    }

    // --- Augment -------------------------------------------------------------

    public int getAugmentSlot() {
        return augmentSlot;
    }

    public boolean hasAugment() {
        return inventory.getStackInSlot(augmentSlot).is(ModTags.Items.MACHINE_AUGMENTS);
    }

    /**
     * Duree reelle d'un cycle, augment compris. Plancher a 1 tick pour qu'une
     * machine tres rapide ne puisse jamais tomber a 0 et boucler indefiniment
     * dans le meme tick.
     */
    public int getEffectiveCycleTicks() {
        int base = getBaseCycleTicks();
        return hasAugment() ? Math.max(1, Math.round(base / AUGMENT_SPEED_MULTIPLIER)) : base;
    }

    // --- Inventaire ----------------------------------------------------------

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    /**
     * Filtre d'insertion par slot. La classe de base ne connait que le slot
     * d'augment ; les sous-classes completent pour leurs entrees et interdisent
     * l'insertion manuelle dans leur slot de sortie.
     */
    protected boolean isItemValid(int slot, ItemStack stack) {
        if (slot == augmentSlot) {
            return stack.is(ModTags.Items.MACHINE_AUGMENTS);
        }
        return true;
    }

    /** Vrai si {@code result} tient dans {@code slot}, vide ou deja entame. */
    protected boolean canInsertInto(int slot, ItemStack result) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (current.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(current, result)
            && current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    /**
     * Depose un resultat de cycle. Contourne volontairement
     * {@link ItemStackHandler#insertItem} : celui-ci passe par
     * {@link #isItemValid}, qui refuse toute insertion dans un slot de sortie.
     * A n'appeler qu'apres un {@link #canInsertInto} positif.
     */
    protected void insertInto(int slot, ItemStack result) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (current.isEmpty()) {
            inventory.setStackInSlot(slot, result.copy());
        } else {
            current.grow(result.getCount());
            inventory.setStackInSlot(slot, current);
        }
    }

    /** Vide l'inventaire au sol. Appele quand le bloc est casse. */
    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            container.setItem(slot, inventory.getStackInSlot(slot));
        }
        Containers.dropContents(level, pos, container);
    }

    // --- Persistance ---------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }
}
