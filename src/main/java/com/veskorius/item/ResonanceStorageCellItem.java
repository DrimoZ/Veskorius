package com.veskorius.item;

import com.veskorius.config.VeskoriusConfig;
import com.veskorius.energy.ResonanceFieldManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Resonance Storage Cell (05-Machines.md #6, 06-Energy.md « Osc portable »).
 *
 * Batterie <em>portable</em> : elle stocke jusqu'à {@link #capacity()} Osc, état de
 * charge porté par l'item ({@link ModDataComponents#STORAGE_CELL_CHARGE}). Tant
 * qu'elle est dans l'inventaire d'un joueur situé dans un champ actif, elle absorbe
 * des Osc <em>prélevés sur ce champ</em> — donc sur la réserve d'un émetteur, la
 * même source que les machines (cohérent avec « pas de câble », pilier 3 : aucune
 * conversion cachée, la recharge portable passe elle aussi par le champ).
 *
 * Elle ne consomme rien seule : son unique client est le Resonance Locator
 * (tâche 8), qui puisera via {@link #extractCharge}. Cette classe fournit donc le
 * stockage et la recharge ; la dépense viendra avec l'outil.
 */
public class ResonanceStorageCellItem extends Item {

    /** Capacité maximale (06-Energy.md ; défaut 8000, configurable). */
    public static int capacity() {
        return VeskoriusConfig.storageCellCapacity();
    }

    /**
     * Débit de charge par tick quand la cellule est dans un champ (défaut 20,
     * configurable). Une cellule vide se remplit en {@code capacity / rate} ticks.
     */
    public static int chargeRate() {
        return VeskoriusConfig.storageCellChargeRate();
    }

    public ResonanceStorageCellItem(Properties properties) {
        super(properties);
    }

    // --- État de charge ------------------------------------------------------

    public static int getCharge(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORAGE_CELL_CHARGE.get(), 0);
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.set(ModDataComponents.STORAGE_CELL_CHARGE.get(), Math.clamp(charge, 0, capacity()));
    }

    /**
     * Retire jusqu'à {@code amount} Osc de la cellule. Retourne la quantité
     * réellement retirée (bornée par la charge disponible). Point d'entrée du
     * Resonance Locator (tâche 8) et de tout futur outil alimenté par la cellule.
     */
    public static int extractCharge(ItemStack stack, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int available = getCharge(stack);
        int extracted = Math.min(amount, available);
        if (extracted > 0) {
            setCharge(stack, available - extracted);
        }
        return extracted;
    }

    /**
     * Un tick de recharge depuis le champ à {@code pos}. Prélève au plus
     * {@link #chargeRate()}, et jamais plus que la place restante, sur un émetteur
     * couvrant la position (via {@link ResonanceFieldManager#supply}). Retourne
     * l'Osc réellement transféré. Isolé de {@link #inventoryTick} pour être testable
     * sans simuler un joueur entier.
     */
    public static int tickCharge(ServerLevel level, BlockPos pos, ItemStack stack) {
        int room = capacity() - getCharge(stack);
        if (room <= 0) {
            return 0;
        }
        int want = Math.min(room, chargeRate());
        int drawn = ResonanceFieldManager.supply(level, pos, want);
        if (drawn > 0) {
            setCharge(stack, getCharge(stack) + drawn);
        }
        return drawn;
    }

    // --- Recharge en inventaire ----------------------------------------------

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Recharge uniquement côté serveur, portée par un joueur (06-Energy.md :
        // « dans l'inventaire d'un joueur situé dans un champ actif »).
        if (level instanceof ServerLevel serverLevel && entity instanceof Player) {
            tickCharge(serverLevel, entity.blockPosition(), stack);
        }
    }

    // --- Affichage -----------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.veskorius.resonance_storage_cell.charge",
            getCharge(stack), capacity()).withStyle(ChatFormatting.AQUA));
    }

    /** Barre de charge (réutilise la barre de durabilité vanilla), visible dès qu'il y a du courant. */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getCharge(stack) / capacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Cyan Résonance, constant (la largeur suffit à exprimer le niveau).
        return 0x33AAFF;
    }
}
