package com.veskorius.block.entity;

import com.veskorius.config.HarmonicsConfig;
import com.veskorius.energy.IResonanceField;
import com.veskorius.item.ModItems;
import com.veskorius.menu.DampingArrayMenu;
import com.veskorius.recipe.DampingAgentRecipe;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Damping Array (06-Energy.md) : l'<b>infrastructure</b> qui absorbe la dissonance d'un
 * champ. Gérer le dérèglement du réseau n'est pas un slot à remplir, c'est un bloc à
 * poser, à approvisionner et à vider — c'est ce qui en fait un vrai chantier de fin de T3.
 *
 * <p>Cycle : consomme un <b>agent de damping</b> (data-driven, {@code veskorius:damping})
 * et retire sa valeur en dissonance à l'émetteur le plus pollué à portée, en produisant
 * un {@code resonance_sludge} — la dissonance <b>cristallisée</b>, la substance même de
 * l'Effondrement. Quand la sortie est pleine, l'Array s'arrête : le container est plein,
 * il faut le vider.
 *
 * <p><b>Volontairement autonome (0 Osc).</b> S'il dépendait du champ qu'il répare, un
 * champ saturé — donc instable et intermittent — empêcherait sa propre réparation. Ce
 * serait un piège de conception ; l'Array ne doit jamais pouvoir se retrouver bloqué par
 * le problème qu'il existe pour résoudre.
 */
public class DampingArrayBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_AGENT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_AUGMENT = 2;
    public static final int SLOT_COUNT = 3;

    private static final int[] AUTOMATION_INPUTS = {SLOT_AGENT};
    private static final int[] AUTOMATION_OUTPUTS = {SLOT_OUTPUT};

    public DampingArrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DAMPING_ARRAY.get(), pos, state, SLOT_COUNT);
    }

    // --- Cycle ---------------------------------------------------------------

    @Override
    protected int getBaseCycleTicks() {
        return HarmonicsConfig.dampingCycleTicks();
    }

    /** Autonome : voir la note de classe — ne jamais dépendre du champ qu'on répare. */
    @Override
    protected int getOscPerTick() {
        return 0;
    }

    @Override
    protected boolean canRunCycle() {
        return HarmonicsConfig.enabled()
            && findAgent() != null
            && canInsertInto(SLOT_OUTPUT, new ItemStack(ModItems.RESONANCE_SLUDGE.get()))
            && findPollutedField() != null;
    }

    @Override
    protected void runCycle() {
        DampingAgentRecipe agent = findAgent();
        IResonanceField field = findPollutedField();
        if (agent == null || field == null) {
            return;
        }
        field.addDissonance(-agent.dissonance());
        inventory.extractItem(SLOT_AGENT, 1, false);
        insertInto(SLOT_OUTPUT, new ItemStack(ModItems.RESONANCE_SLUDGE.get()));
    }

    /** L'agent présent dans le slot, ou {@code null} s'il n'y en a pas (data-driven). */
    @Nullable
    private DampingAgentRecipe findAgent() {
        ItemStack stack = inventory.getStackInSlot(SLOT_AGENT);
        if (stack.isEmpty() || level == null) {
            return null;
        }
        return findAgent(level, stack);
    }

    @Nullable
    public static DampingAgentRecipe findAgent(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.DAMPING.get(), new SingleRecipeInput(stack), level)
            .map(RecipeHolder::value)
            .orElse(null);
    }

    /** Le champ le plus pollué à portée, ou {@code null} si tout est propre. */
    @Nullable
    private IResonanceField findPollutedField() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return com.veskorius.energy.ResonanceFieldManager.mostDissonantSource(
            serverLevel, worldPosition, HarmonicsConfig.dampingRange());
    }

    // --- Filtre de slot ------------------------------------------------------

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            // Piloté par les recettes : ajouter un agent en JSON ouvre le slot.
            case SLOT_AGENT -> level == null || findAgent(level, stack) != null;
            case SLOT_OUTPUT -> false;
            default -> super.isItemValid(slot, stack);
        };
    }

    @Override
    protected int[] getAutomationInputSlots() {
        return AUTOMATION_INPUTS;
    }

    @Override
    protected int[] getAutomationOutputSlots() {
        return AUTOMATION_OUTPUTS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.damping_array");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DampingArrayMenu(containerId, playerInventory, this);
    }
}
