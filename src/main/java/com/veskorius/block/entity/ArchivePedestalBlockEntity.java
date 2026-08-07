package com.veskorius.block.entity;

import com.veskorius.item.CodexEntries;
import com.veskorius.item.CodexFragmentItem;
import com.veskorius.item.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <b>Socle d'archive</b> — la serrure de la salle de lecture (08-Structures.md :
 * « reconstituer l'ordre de 4 fragments de Codex dispersés »).
 *
 * <p>Quatre socles alignés devant la porte. On y pose les quatre cotes trouvées dans le
 * bâtiment ; quand elles sont <b>dans l'ordre</b>, l'émetteur de la salle se rallume et le
 * sas s'ouvre.
 *
 * <p><b>Pourquoi l'issue passe par l'émetteur et non par une serrure dédiée.</b> C'est la
 * doctrine des donjons appliquée telle quelle : <b>une porte s'ouvre toujours par un
 * champ</b> (17-Dungeons.md, R1). L'énigme ne fait que décider <i>ce qui rallume
 * l'émetteur</i>. Le sas de l'Archive est donc exactement celui de l'Avant-poste et du
 * Sigma, sans une ligne de cas particulier — et le joueur, qui a déjà vu deux fois que la
 * Résonance ouvre les portes, sait ce qu'il vient de provoquer.
 *
 * <p><b>Pourquoi c'est le premier fragment qui sert à autre chose qu'à être lu.</b> Le
 * pilier 2 (« la connaissance est spatiale ») trouve ici son application la plus littérale
 * du mod : on ne peut pas résoudre sans avoir lu. Les cotes portent leur rang dans leur
 * texte ; l'ordre n'est écrit nulle part ailleurs.
 */
public class ArchivePedestalBlockEntity extends BlockEntity {

    /**
     * Rayon de recherche des socles frères et de l'émetteur. 12 couvre la salle du
     * cadran et rien d'autre — assez large pour que le placement des socles reste libre,
     * assez étroit pour que deux Archives voisines ne se répondent jamais.
     */
    private static final int SCAN = 12;

    private ItemStack held = ItemStack.EMPTY;

    public ArchivePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCHIVE_PEDESTAL.get(), pos, state);
    }

    public ItemStack getHeld() {
        return held;
    }

    public boolean isEmpty() {
        return held.isEmpty();
    }

    /** Pose un fragment. Retourne vrai si le socle l'a accepté. */
    public boolean place(ItemStack stack) {
        if (!held.isEmpty() || !stack.is(ModItems.CODEX_FRAGMENT.get())) {
            return false;
        }
        held = stack.copyWithCount(1);
        setChanged();
        return true;
    }

    /** Reprend le fragment posé. */
    public ItemStack take() {
        ItemStack out = held;
        held = ItemStack.EMPTY;
        setChanged();
        return out;
    }

    /**
     * <b>Les quatre socles portent-ils les quatre cotes, dans l'ordre ?</b>
     *
     * <p>L'ordre est celui de leur <b>position dans l'espace</b>, pas celui de leur pose :
     * on lit le cadran de gauche à droite comme on lirait une étagère. Le tri se fait sur
     * (x, z) — une seule règle, qui vaut quelle que soit l'orientation que le jigsaw a
     * donnée au bâtiment.
     */
    public static boolean solved(Level level, BlockPos around) {
        List<ArchivePedestalBlockEntity> pedestals = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(around.offset(-SCAN, -3, -SCAN),
            around.offset(SCAN, 3, SCAN))) {
            if (level.getBlockEntity(pos) instanceof ArchivePedestalBlockEntity pedestal) {
                pedestals.add(pedestal);
            }
        }
        if (pedestals.size() != CodexEntries.ARCHIVE_LOG.length) {
            return false;
        }
        pedestals.sort(Comparator
            .comparingInt((ArchivePedestalBlockEntity p) -> p.getBlockPos().getX())
            .thenComparingInt(p -> p.getBlockPos().getZ()));

        for (int i = 0; i < pedestals.size(); i++) {
            ItemStack stack = pedestals.get(i).held;
            if (!stack.is(ModItems.CODEX_FRAGMENT.get())) {
                return false;
            }
            ResourceLocation entry = CodexFragmentItem.entryOf(stack);
            if (!CodexEntries.ARCHIVE_LOG[i].equals(entry)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!held.isEmpty()) {
            tag.put("Held", held.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        held = tag.contains("Held")
            ? ItemStack.parse(registries, tag.getCompound("Held")).orElse(ItemStack.EMPTY)
            : ItemStack.EMPTY;
    }
}
