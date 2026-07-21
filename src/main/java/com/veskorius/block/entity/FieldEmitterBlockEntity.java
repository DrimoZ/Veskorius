package com.veskorius.block.entity;

import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.item.ModItems;
import com.veskorius.menu.FieldEmitterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Field Emitter (machine #4, 05-Machines.md) : premier fournisseur de champ de
 * Résonance. Portée 8, réserve interne de 4000 Osc.
 *
 * Ce n'est PAS une machine à cycle : elle ne transforme rien en sortie. Elle
 * n'hérite donc pas d'{@link AbstractMachineBlockEntity} (pas de progression, pas
 * de slot d'augment — le Field Emitter est un « bloc passif » au sens du design,
 * les blocs passifs n'acceptent pas le Catalyst Core).
 *
 * Source d'énergie : elle brûle des Stable Resonance Crystal, 4000 Osc chacun
 * (06-Energy.md, section « Source primaire de l'énergie »). Le cristal est le seul
 * carburant accepté.
 */
public class FieldEmitterBlockEntity extends BlockEntity implements IResonanceField, MenuProvider {

    public static final int DATA_RESERVE = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_COUNT = 2;

    /** Portée du champ (06-Energy.md). */
    private static final int RANGE = 8;

    /** Réserve max = exactement un Stable Crystal (05-Machines.md #4, « réserve 4000 Osc »). */
    private static final int CAPACITY = 4000;

    /** Osc rendus par un Stable Crystal brûlé (06-Energy.md). */
    private static final int OSC_PER_CRYSTAL = 4000;

    /**
     * Intensité de tous les Field Emitter T2. La valeur exacte n'a pas encore
     * d'effet (aucun consommateur n'a de seuil avant le T4) ; ce qui compte est
     * qu'elle soit identique pour tous, donc l'anti-stacking est neutre entre deux
     * Field Emitter. Le Harmonic Amplifier (T4) émettra une intensité supérieure.
     */
    private static final int FIELD_STRENGTH = 100;

    public static final int SLOT_FUEL = 0;

    private final ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.STABLE_RESONANCE_CRYSTAL.get());
        }
    };

    private int reserve;

    /** Synchronise reserve + capacite vers le GUI (jauge « X/4000 Osc », 12-UX). */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_RESERVE -> reserve;
                case DATA_CAPACITY -> CAPACITY;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_RESERVE) {
                reserve = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public FieldEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIELD_EMITTER.get(), pos, state);
    }

    // --- Tick ----------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, FieldEmitterBlockEntity emitter) {
        // Idempotent : se (ré)inscrit à l'index tant qu'il tourne. Couvre aussi le
        // premier tick après un rechargement de chunk, sans dépendre de l'ordre
        // exact des callbacks de cycle de vie.
        ResonanceFieldManager.register(level, pos);
        emitter.refuelIfEmpty();
    }

    /**
     * Ne brûle un cristal que lorsque la réserve peut en absorber un plein (donc,
     * capacité et charge par cristal étant égales, uniquement à réserve nulle) :
     * jamais de cristal gaspillé pour combler un petit déficit.
     */
    private void refuelIfEmpty() {
        if (reserve + OSC_PER_CRYSTAL > CAPACITY) {
            return;
        }
        if (fuel.getStackInSlot(SLOT_FUEL).isEmpty()) {
            return;
        }
        fuel.extractItem(SLOT_FUEL, 1, false);
        reserve += OSC_PER_CRYSTAL;
        setChanged();
    }

    // --- IResonanceField -----------------------------------------------------

    @Override
    public int getFieldStrength() {
        return FIELD_STRENGTH;
    }

    @Override
    public int getRange() {
        return RANGE;
    }

    @Override
    public boolean isActive() {
        return reserve > 0;
    }

    @Override
    public int extractOsc(int maxOsc) {
        int drawn = Math.min(maxOsc, reserve);
        if (drawn > 0) {
            reserve -= drawn;
            setChanged();
        }
        return drawn;
    }

    // --- Accès et cycle de vie ----------------------------------------------

    public ItemStackHandler getFuelHandler() {
        return fuel;
    }

    public ContainerData getData() {
        return data;
    }

    public int getReserve() {
        return reserve;
    }

    public int getCapacity() {
        return CAPACITY;
    }

    // --- MenuProvider (GUI : jauge de réserve + slot de carburant) ------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.field_emitter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FieldEmitterMenu(containerId, playerInventory, this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // Couvre le bloc cassé ET le chunk déchargé : dans les deux cas l'émetteur
        // sort de l'index. S'il s'agit d'un déchargement, il se ré-inscrira à son
        // premier tick après rechargement.
        if (level != null) {
            ResonanceFieldManager.unregister(level, worldPosition);
        }
    }

    /** Vide le slot de carburant au sol. Appelé quand le bloc est cassé. */
    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(fuel.getSlots());
        for (int slot = 0; slot < fuel.getSlots(); slot++) {
            container.setItem(slot, fuel.getStackInSlot(slot));
        }
        Containers.dropContents(level, pos, container);
    }

    // --- Persistance ---------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fuel", fuel.serializeNBT(registries));
        tag.putInt("reserve", reserve);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuel.deserializeNBT(registries, tag.getCompound("fuel"));
        reserve = tag.getInt("reserve");
    }
}
