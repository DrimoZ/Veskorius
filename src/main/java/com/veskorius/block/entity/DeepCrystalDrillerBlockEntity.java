package com.veskorius.block.entity;

import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Deep Crystal Driller</b> (machine #12, 05-Machines.md) : mine automatiquement une
 * veine détectée sous Y −40. Continu, 6 Osc/tick.
 *
 * <p>C'est la <b>seule machine du mod qui modifie le monde</b>, et c'est ce qui la rend
 * intéressante autant que dangereuse à concevoir. Trois décisions la tiennent :
 *
 * <ol>
 *   <li><b>Elle ne creuse pas, elle récolte.</b> Une foreuse qui casse tout ce qu'elle
 *       traverse serait une quarry — un objet qu'on pose et qu'on oublie, et qui vide la
 *       carte. Celle-ci ne retire que des amas de cristal, jamais la roche : la galerie
 *       reste intacte, et le joueur qui descend voit exactement ce qui a été pris.</li>
 *   <li><b>Elle ne cherche que sous Y −40.</b> La limite vient du dossier, mais elle a une
 *       conséquence de jeu : on ne pose pas un Driller où on veut, on le pose au-dessus
 *       d'une poche profonde. C'est le Resonance Locator qui décide de l'emplacement de la
 *       base, pas le confort — le pilier « le monde impose la géographie ».</li>
 *   <li><b>Elle épuise sa veine.</b> Chaque cycle retire un amas ; quand la colonne est
 *       vide, la machine s'arrête et il faut la déplacer. Une source infinie annulerait
 *       toute la chaîne de raffinage.</li>
 * </ol>
 *
 * <p><b>Le balayage est borné, et il fallait qu'il le soit.</b> La colonne sous la machine
 * peut faire trois cents blocs de haut ; la parcourir à chaque tick d'attente pour découvrir
 * qu'elle est vide serait un scan de plusieurs milliers de positions <i>par tick</i> et par
 * foreuse posée. La cible trouvée est donc mémorisée, et une recherche infructueuse pose un
 * délai avant la suivante ({@link #SCAN_COOLDOWN}). Une foreuse à sec est silencieuse et
 * gratuite en calcul.
 */
public class DeepCrystalDrillerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_AUGMENT = 1;
    public static final int SLOT_COUNT = 2;

    /** 20 s par amas. Assez lent pour qu'une veine dure, assez rapide pour qu'on la pose. */
    private static final int CYCLE_TICKS = 20 * 20;
    private static final int OSC_PER_TICK = 6;

    /**
     * Profondeur maximale d'exploitation (05-Machines.md #12). Un amas plus haut que ça est
     * ignoré même s'il est juste sous la machine : la foreuse est un outil de <b>fond</b>.
     */
    public static final int MAX_Y = -40;

    /** Demi-largeur de la colonne balayée. 2 → un puits de 5×5, la taille d'une poche. */
    private static final int RADIUS = 2;

    /** Délai avant de re-balayer après un échec. 5 s : imperceptible en jeu, décisif en CPU. */
    private static final int SCAN_COOLDOWN = 20 * 5;

    @Nullable
    private BlockPos target;
    private int scanCooldown;

    public DeepCrystalDrillerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DEEP_CRYSTAL_DRILLER.get(), pos, state, SLOT_COUNT);
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
        if (!canInsertInto(SLOT_OUTPUT, harvest())) {
            return false;
        }
        return findTarget(serverLevel) != null;
    }

    @Override
    protected void runCycle() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos hit = findTarget(serverLevel);
        if (hit == null) {
            return;
        }
        // On remplace par de l'air, pas par de la roche : la galerie doit se voir.
        serverLevel.setBlock(hit, Blocks.CAVE_AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        serverLevel.levelEvent(2001, hit, net.minecraft.world.level.block.Block.getId(
            ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get().defaultBlockState()));
        insertInto(SLOT_OUTPUT, harvest());
        target = null;
    }

    private static ItemStack harvest() {
        return new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get());
    }

    /**
     * L'amas exploitable le plus haut sous la machine, ou {@code null}. Mémorisé entre les
     * appels — voir la note de classe sur le coût du balayage.
     */
    @Nullable
    private BlockPos findTarget(ServerLevel level) {
        if (target != null && level.getBlockState(target).is(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())) {
            return target;
        }
        target = null;
        if (scanCooldown > 0) {
            scanCooldown--;
            return null;
        }

        int top = Math.min(MAX_Y, worldPosition.getY() - 1);
        for (int y = top; y >= level.getMinBuildHeight(); y--) {
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    BlockPos p = new BlockPos(worldPosition.getX() + dx, y, worldPosition.getZ() + dz);
                    if (!level.isLoaded(p)) {
                        continue;
                    }
                    if (level.getBlockState(p).is(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())) {
                        target = p;
                        return target;
                    }
                }
            }
        }
        scanCooldown = SCAN_COOLDOWN;
        return null;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        // Rien ne s'insère : la foreuse n'a qu'une sortie (et son slot d'augment, géré
        // par le socle). Sans ça, un hopper remplirait le slot de sortie de n'importe quoi
        // et bloquerait la machine pour de bon.
        return isAugmentSlot(slot) && super.isItemValid(slot, stack);
    }

    @Override
    protected int[] getAutomationInputSlots() {
        return new int[0];
    }

    @Override
    protected int[] getAutomationOutputSlots() {
        return new int[] {SLOT_OUTPUT};
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (target != null) {
            tag.put("target", NbtUtils.writeBlockPos(target));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        target = tag.contains("target") ? NbtUtils.readBlockPos(tag, "target").orElse(null) : null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.deep_crystal_driller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.veskorius.menu.DeepCrystalDrillerMenu(containerId, playerInventory, this);
    }
}
