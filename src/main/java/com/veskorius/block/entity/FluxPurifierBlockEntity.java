package com.veskorius.block.entity;

import com.veskorius.config.VeskoriusConfig;
import com.veskorius.menu.FluxPurifierMenu;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Machine #5 (05-Machines.md). Recette de fonctionnement en JSON, type
 * {@code veskorius:purifying} (Stable Crystal + Redstone → Refined Crystal).
 *
 * Première machine à mode surchauffe. Le temps ÷2 et la consommation ×2 sont gérés
 * génériquement par le socle ({@code getEffectiveCycleTicks} /
 * {@code getEffectiveOscPerTick}). Il ne reste ici que l'effet que le socle ne peut
 * pas connaître : le **risque de 20 % de perte de l'input** en surchauffe.
 */
public class FluxPurifierBlockEntity extends AbstractProcessingMachineBlockEntity {

    public static final int SLOT_CRYSTAL = 0;
    public static final int SLOT_REDSTONE = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_COUNT = 4;

    public FluxPurifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUX_PURIFIER.get(), pos, state, SLOT_COUNT,
            ModRecipeTypes.PURIFYING::get, new int[] {SLOT_CRYSTAL, SLOT_REDSTONE}, SLOT_OUTPUT);
    }

    @Override
    public boolean supportsOverheat() {
        return true;
    }

    /**
     * Seule machine <b>accordable</b> avant la T3 (06-Energy.md, courbe d'introduction).
     * Elle reste <b>universelle par défaut</b> : rien ne change pour qui ne touche pas au
     * Tuner — la T2 garde donc sa promesse « aucune décision ». Mais c'est la première
     * machine qui puise vraiment dans le champ sur une recette non {@code stable}, donc
     * la seule sur laquelle le mode « Accorder » ait un sens aujourd'hui : sans elle,
     * ce mode et la lecture par couleur resteraient inobservables jusqu'à la Phase 2.
     */
    @Override
    public boolean supportsHarmonicBand() {
        return true;
    }

    @Override
    protected boolean shouldProduceResult() {
        // En surchauffe, une chance configurable (défaut 20 %) que l'entrée parte en
        // fumée sans sortie. level est forcément non nul et serveur ici (serverTick).
        return !losesInput(isOverheatActive(), isCurrentRecipeStable(),
            com.veskorius.config.MachinesConfig.overheatIgnoresStable(),
            VeskoriusConfig.overheatInputLossChance(), level.getRandom().nextFloat());
    }

    /**
     * Décision pure « l'entrée est-elle perdue ce cycle ? », extraite pour être
     * testable sans RNG ni monde.
     *
     * <p>Hors surchauffe : jamais de perte. En surchauffe : perte si le tirage
     * {@code roll} (∈ [0,1)) tombe sous {@code lossChance}.
     *
     * <p>Le cas d'une recette {@code stable} est le seul qui demande une décision.
     * Une recette stable ne rate jamais à cause du <b>désaccord harmonique</b> — c'est
     * ce qui garantit que la boucle de départ ne peut pas frustrer (06-Energy.md). Mais
     * la surchauffe n'est pas subie : c'est un pari que le joueur <i>active</i>. Par
     * défaut ({@code overheatIgnoresStable = true}) elle garde donc son risque même là,
     * sinon toute recette stable voudrait la surchauffe en permanence et le choix
     * disparaîtrait. Un modpack qui préfère que {@code stable} veuille dire « ne perd
     * jamais rien, point » passe le réglage à {@code false} — c'est la clé que `05` et
     * `06` promettaient sous la formule « sauf réglage contraire en config ».
     */
    public static boolean losesInput(boolean overheatActive, boolean recipeStable,
                                     boolean overheatIgnoresStable, double lossChance, double roll) {
        if (!overheatActive) {
            return false;
        }
        if (recipeStable && !overheatIgnoresStable) {
            return false;
        }
        return roll < lossChance;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.flux_purifier");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FluxPurifierMenu(containerId, playerInventory, this);
    }
}
