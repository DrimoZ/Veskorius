package com.veskorius.menu;

import com.veskorius.block.entity.AbstractMachineBlockEntity;
import com.veskorius.block.entity.RedstoneMode;
import com.veskorius.block.entity.SideMode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Socle commun aux menus des machines actives.
 *
 * Convention d'indices, valable pour les 23 machines : les slots de la machine
 * viennent EN PREMIER (0..n-1), donc l'indice d'un slot dans le menu est
 * exactement son indice dans l'inventaire de la block entity. L'inventaire du
 * joueur suit (n..n+26), puis la barre d'action (n+27..n+35). Cette regle evite
 * les decalages d'indice qui sont la source d'erreur classique de
 * {@code quickMoveStack}.
 */
public abstract class AbstractMachineMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_SLOTS = 27;
    private static final int PLAYER_HOTBAR_SLOTS = 9;
    private static final int PLAYER_SLOTS = PLAYER_INVENTORY_SLOTS + PLAYER_HOTBAR_SLOTS;

    protected final AbstractMachineBlockEntity blockEntity;
    private final ContainerData data;
    private final int machineSlotCount;

    protected AbstractMachineMenu(MenuType<?> type, int containerId, Inventory playerInventory,
                                  AbstractMachineBlockEntity blockEntity) {
        super(type, containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getData();

        addMachineSlots(blockEntity.getInventory());
        this.machineSlotCount = slots.size();

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    /**
     * Ajoute les slots propres a la machine. Appelee par le constructeur de base,
     * donc l'implementation ne doit lire aucun champ de la sous-classe — tout ce
     * dont elle a besoin est passe en parametre.
     */
    protected abstract void addMachineSlots(IItemHandler inventory);

    /**
     * Slots d'augment (05-Machines.md, « slot d'augment → slots d'augment »). Ajoute le
     * nombre <b>actif</b> de slots (config, défaut 1 → un seul slot en {@code (x, y)},
     * exactement comme avant), empilés verticalement. Les slots réservés mais inactifs ne
     * reçoivent aucun widget. Serveur et client lisent la même config SERVER (synchronisée),
     * donc le nombre de slots est cohérent des deux côtés. Layout placeholder (Phase 6).
     */
    protected void addAugmentSlot(IItemHandler inventory, int x, int y) {
        int first = blockEntity.getAugmentSlot();
        int active = blockEntity.getActiveAugmentSlots();
        for (int i = 0; i < active; i++) {
            addSlot(new SlotItemHandler(inventory, first + i, x, y + i * 18));
        }
    }

    /** Slot de sortie : jamais remplissable a la main (isItemValid renvoie false). */
    protected void addSlot(IItemHandler inventory, int slot, int x, int y) {
        addSlot(new SlotItemHandler(inventory, slot, x, y));
    }

    // --- Progression ---------------------------------------------------------

    public int getProgress() {
        return data.get(AbstractMachineBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return data.get(AbstractMachineBlockEntity.DATA_MAX_PROGRESS);
    }

    /** Progression ramenee a une largeur en pixels, pour la barre du GUI. */
    public int getScaledProgress(int pixels) {
        int max = getMaxProgress();
        return max <= 0 ? 0 : getProgress() * pixels / max;
    }

    // --- Boutons de controle -------------------------------------------------

    public static final int BUTTON_MANUAL = 0;
    public static final int BUTTON_REDSTONE = 1;
    public static final int BUTTON_OVERHEAT = 2;
    /** Boutons de config item I/O : cycle du mode d'une face (base + Direction.get3DDataValue). */
    public static final int BUTTON_CYCLE_SIDE_BASE = 10;
    public static final int BUTTON_AUTO_INPUT = 16;
    public static final int BUTTON_AUTO_OUTPUT = 17;

    /**
     * Recoit un clic de bouton du GUI, cote SERVEUR (declenche par
     * {@code MultiPlayerGameMode.handleInventoryButtonClick} cote client). Aucun
     * packet custom : on reutilise le canal vanilla des boutons de menu.
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= BUTTON_CYCLE_SIDE_BASE && id < BUTTON_CYCLE_SIDE_BASE + 6) {
            blockEntity.cycleSideMode(net.minecraft.core.Direction.from3DDataValue(id - BUTTON_CYCLE_SIDE_BASE));
            return true;
        }
        switch (id) {
            case BUTTON_MANUAL -> blockEntity.toggleManual();
            case BUTTON_REDSTONE -> blockEntity.cycleRedstoneMode();
            case BUTTON_OVERHEAT -> blockEntity.toggleOverheat();
            case BUTTON_AUTO_INPUT -> blockEntity.toggleAutoInput();
            case BUTTON_AUTO_OUTPUT -> blockEntity.toggleAutoOutput();
            default -> {
                return false;
            }
        }
        return true;
    }

    // Etats lus depuis la ContainerData (synchronisee tant que le menu est ouvert).

    public boolean isManualEnabled() {
        return data.get(AbstractMachineBlockEntity.DATA_MANUAL) != 0;
    }

    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byIndex(data.get(AbstractMachineBlockEntity.DATA_REDSTONE_MODE));
    }

    public boolean isOverheatEnabled() {
        return data.get(AbstractMachineBlockEntity.DATA_OVERHEAT) != 0;
    }

    /** Mode d'une face (item I/O), lu depuis la ContainerData synchronisée. */
    public SideMode getSideMode(int faceIndex) {
        return SideMode.byIndex(data.get(AbstractMachineBlockEntity.DATA_SIDE_BASE + faceIndex));
    }

    public boolean isAutoInput() {
        return data.get(AbstractMachineBlockEntity.DATA_AUTO_INPUT) != 0;
    }

    public boolean isAutoOutput() {
        return data.get(AbstractMachineBlockEntity.DATA_AUTO_OUTPUT) != 0;
    }

    /**
     * Support de la surchauffe : lu directement sur la block entity, qui existe
     * aussi cote client (le menu la retrouve via la BlockPos du paquet
     * d'ouverture). C'est une propriete statique de la machine, pas un etat a
     * synchroniser.
     */
    public boolean supportsOverheat() {
        return blockEntity.supportsOverheat();
    }

    /**
     * Vrai si le slot de menu {@code index} est un slot de la machine (par opposition à
     * l'inventaire du joueur). Sert à l'écran, qui dessine une alvéole par slot réel
     * plutôt que de les figer dans la texture de fond.
     */
    public boolean isMachineSlot(int index) {
        return index < machineSlotCount;
    }

    /**
     * Vrai si le slot de menu {@code index} est un slot d'<b>augment</b>. L'écran lui
     * donne une alvéole cerclée de laiton : c'est le seul slot que l'automatisation ne
     * touche jamais (12-UX), il ne doit pas se confondre avec une entrée. La convention
     * d'indices du socle — les slots machine d'abord, dans l'ordre de l'inventaire —
     * rend la correspondance directe.
     */
    public boolean isAugmentSlot(int index) {
        return isMachineSlot(index) && blockEntity.isAugmentSlot(index);
    }

    // --- Inventaire joueur ---------------------------------------------------

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved()
            && player.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        int playerStart = machineSlotCount;
        int playerEnd = machineSlotCount + PLAYER_SLOTS;

        if (index < machineSlotCount) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, machineSlotCount, false)) {
            // moveItemStackTo respecte Slot.mayPlace, donc SlotItemHandler ->
            // isItemValid : un shift-clic ne peut pas atterrir dans un slot de
            // sortie ni forcer un objet quelconque dans le slot d'augment.
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }
}
