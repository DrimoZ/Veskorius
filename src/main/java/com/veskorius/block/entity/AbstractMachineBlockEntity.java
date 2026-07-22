package com.veskorius.block.entity;

import com.veskorius.config.VeskoriusConfig;
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

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_MANUAL = 2;
    public static final int DATA_REDSTONE_MODE = 3;
    public static final int DATA_OVERHEAT = 4;
    public static final int DATA_COUNT = 5;

    protected final ItemStackHandler inventory;

    private final int augmentSlot;
    private int progress;

    /** Interrupteur manuel (bouton du GUI). Machine allumee par defaut. */
    private boolean manualEnabled = true;
    /** Mode de controle redstone (bouton du GUI). */
    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;
    /**
     * Surchauffe active (bouton du GUI + Resonance Tuner, tache 9). N'a d'effet
     * que sur les machines qui la supportent ({@link #supportsOverheat}).
     */
    private boolean overheatEnabled = false;

    /**
     * Durée effective du cycle courant, mise à jour côté serveur à chaque tick et
     * synchronisée vers le client via la ContainerData. Doit être un champ stocké
     * (et non un simple recalcul dans le getter) : depuis que le temps vient de la
     * recette, il dépend des entrées, or l'inventaire de la block entity n'est PAS
     * synchronisé au client — seule cette valeur l'est. Le client la lit telle
     * quelle pour dimensionner la barre de progression.
     */
    private int maxProgress = 1;

    /**
     * Synchronisation client de la barre de progression et de l'état des boutons.
     * Passe par ContainerData (mécanisme vanilla du four) plutôt que par un packet
     * custom — voir 12-UX-and-Advancements.md.
     */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> maxProgress;
                case DATA_MANUAL -> manualEnabled ? 1 : 0;
                case DATA_REDSTONE_MODE -> redstoneMode.ordinal();
                case DATA_OVERHEAT -> overheatEnabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Cote client : stocke la valeur synchronisee pour que la barre et les
            // boutons du GUI reflètent l'état réel du serveur.
            switch (index) {
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = value;
                case DATA_MANUAL -> manualEnabled = value != 0;
                case DATA_REDSTONE_MODE -> redstoneMode = RedstoneMode.byIndex(value);
                case DATA_OVERHEAT -> overheatEnabled = value != 0;
                default -> { }
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
                onSlotChanged(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return AbstractMachineBlockEntity.this.isItemValid(slot, stack);
            }
        };
    }

    /**
     * Appelé quand un slot change. Point d'extension : une machine à recette
     * l'utilise pour invalider sa recette en cache quand une entrée bouge.
     */
    protected void onSlotChanged(int slot) {
    }

    // --- Contrat des sous-classes -------------------------------------------

    /** Duree d'un cycle en ticks, sans augment (20 ticks = 1 seconde). */
    protected abstract int getBaseCycleTicks();

    /** Entrees presentes ET place disponible en sortie. Evalue a chaque tick. */
    protected abstract boolean canRunCycle();

    /** Consomme les entrees et produit la sortie. Appele une seule fois par cycle. */
    protected abstract void runCycle();

    /**
     * Osc consommes par tick d'avancement, hors surchauffe (05-Machines.md,
     * colonne Energie). 0 = machine autonome (Stabilizer, Whetstone) : elle n'a
     * besoin d'aucun champ. Redefinir pour une machine qui puise dans le champ.
     */
    protected int getOscPerTick() {
        return 0;
    }

    /**
     * Vrai si cette machine possede un mode surchauffe (05-Machines.md #5, #15).
     * Par defaut non ; le Flux Purifier et la Deep Synthesis Chamber le redefinissent.
     */
    public boolean supportsOverheat() {
        return false;
    }

    // --- Cycle ---------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractMachineBlockEntity machine) {
        machine.tickCycle();
    }

    private void tickCycle() {
        // Tenu à jour côté serveur pour la synchro de la barre de progression : le
        // temps de cycle dépend désormais de la recette (donc des entrées).
        maxProgress = getEffectiveCycleTicks();

        if (!canRunCycle()) {
            // Ingredient absent ou sortie pleine : le cycle ne peut pas exister,
            // on remet a zero (contrairement aux pauses ci-dessous).
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }

        if (!isControlEnabled()) {
            // Coupee a la main ou par la redstone : PAUSE (progression conservee),
            // et aucun Osc n'est preleve tant qu'elle est coupee.
            return;
        }

        if (!drawEnergy()) {
            // Pas assez d'Osc ce tick : PAUSE aussi. Une coupure de courant breve
            // ne doit pas gacher le travail deja fait.
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
        int cost = getEffectiveOscPerTick();
        if (cost <= 0) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return ResonanceFieldManager.supply(serverLevel, worldPosition, cost) >= cost;
    }

    /** Cout d'un tick, surchauffe comprise (consommation multipliee, 06-Energy.md ; facteur configurable). */
    public int getEffectiveOscPerTick() {
        int base = getOscPerTick();
        return isOverheatActive()
            ? (int) Math.round(base * VeskoriusConfig.overheatOscMultiplier())
            : base;
    }

    // --- Controle (redstone, interrupteur manuel, surchauffe) ----------------

    /**
     * Vrai si la machine a le droit d'avancer ce tick, du point de vue du controle
     * (interrupteur manuel + redstone). Independant des ingredients et de
     * l'energie, verifies separement.
     */
    public boolean isControlEnabled() {
        if (!manualEnabled) {
            return false;
        }
        if (redstoneMode == RedstoneMode.IGNORED) {
            return true;
        }
        boolean powered = level != null && level.hasNeighborSignal(worldPosition);
        return redstoneMode.allowsRunning(powered);
    }

    public boolean isManualEnabled() {
        return manualEnabled;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public boolean isOverheatEnabled() {
        return overheatEnabled;
    }

    /** Vrai si la surchauffe est active ET supportee — condition d'effet reelle. */
    public boolean isOverheatActive() {
        return supportsOverheat() && overheatEnabled;
    }

    // Mutations, appelees par le menu (boutons) et plus tard par le Resonance Tuner.

    public void toggleManual() {
        setManualEnabled(!manualEnabled);
    }

    public void cycleRedstoneMode() {
        setRedstoneMode(redstoneMode.next());
    }

    public void toggleOverheat() {
        if (supportsOverheat()) {
            setOverheatEnabled(!overheatEnabled);
        }
    }

    public void setManualEnabled(boolean enabled) {
        this.manualEnabled = enabled;
        setChanged();
    }

    public void setRedstoneMode(RedstoneMode mode) {
        this.redstoneMode = mode;
        setChanged();
    }

    public void setOverheatEnabled(boolean enabled) {
        this.overheatEnabled = enabled;
        setChanged();
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
        // Surchauffe : temps divise par un facteur configurable (06-Energy.md,
        // defaut 2). Appliquee avant l'augment, les deux se cumulent.
        if (isOverheatActive()) {
            base = Math.max(1, (int) Math.round(base / VeskoriusConfig.overheatSpeedMultiplier()));
        }
        if (hasAugment()) {
            base = Math.max(1, (int) Math.round(base / VeskoriusConfig.augmentSpeedMultiplier()));
        }
        return Math.max(1, base);
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
        tag.putBoolean("manualEnabled", manualEnabled);
        tag.putByte("redstoneMode", (byte) redstoneMode.ordinal());
        tag.putBoolean("overheatEnabled", overheatEnabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        // Machine allumee par defaut pour un bloc pose avant l'ajout de ce champ.
        manualEnabled = !tag.contains("manualEnabled") || tag.getBoolean("manualEnabled");
        redstoneMode = RedstoneMode.byIndex(tag.getByte("redstoneMode"));
        overheatEnabled = tag.getBoolean("overheatEnabled");
    }
}
