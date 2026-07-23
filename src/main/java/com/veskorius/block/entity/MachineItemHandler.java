package com.veskorius.block.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Vue sidée de l'inventaire d'une machine, exposée comme capability
 * {@code ItemHandler} (item I/O, 12-UX-and-Advancements.md). Selon le
 * {@link SideMode} de la face, elle n'autorise que certaines opérations sur certains
 * slots réels :
 * <ul>
 *   <li>slots insérables → insertion autorisée (en respectant {@code isItemValid} de la
 *       machine, donc filtrée par recette) ;</li>
 *   <li>slots extractibles → extraction autorisée.</li>
 * </ul>
 * Le slot d'augment n'est jamais ni insérable ni extractible : l'automatisation ne peut
 * pas voler le Catalyst Core. L'énergie ne passe pas par ici (pas de tuyaux).
 */
public class MachineItemHandler implements IItemHandler {

    private final ItemStackHandler backing;
    private final int[] slots;
    private final boolean[] insertable;
    private final boolean[] extractable;

    /**
     * @param backing     l'inventaire réel de la machine.
     * @param insertSlots slots réels où l'insertion externe est permise.
     * @param extractSlots slots réels d'où l'extraction externe est permise.
     */
    public MachineItemHandler(ItemStackHandler backing, int[] insertSlots, int[] extractSlots) {
        this.backing = backing;
        // Union ordonnée des slots exposés (un slot peut être les deux — improbable ici).
        java.util.TreeSet<Integer> union = new java.util.TreeSet<>();
        for (int s : insertSlots) {
            union.add(s);
        }
        for (int s : extractSlots) {
            union.add(s);
        }
        this.slots = union.stream().mapToInt(Integer::intValue).toArray();
        this.insertable = new boolean[slots.length];
        this.extractable = new boolean[slots.length];
        for (int i = 0; i < slots.length; i++) {
            this.insertable[i] = contains(insertSlots, slots[i]);
            this.extractable[i] = contains(extractSlots, slots[i]);
        }
    }

    private static boolean contains(int[] array, int value) {
        for (int v : array) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSlots() {
        return slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return backing.getStackInSlot(slots[slot]);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!insertable[slot]) {
            return stack;
        }
        return backing.insertItem(slots[slot], stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!extractable[slot]) {
            return ItemStack.EMPTY;
        }
        return backing.extractItem(slots[slot], amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return backing.getSlotLimit(slots[slot]);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return insertable[slot] && backing.isItemValid(slots[slot], stack);
    }
}
