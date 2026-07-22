package com.veskorius.item;

import com.veskorius.block.ModBlocks;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.energy.ResonanceFieldManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Resonance Locator (05-Machines.md #7, 06-Energy.md) : détecteur de résonance à
 * courte portée. Clic droit → ping directionnel vers la **source de résonance** la
 * plus proche : une poche de cristal brut (qui rayonne) ou une signature de champ
 * (Field Emitter actif). Non consommable.
 *
 * Énergie (06-Energy.md, « Osc portable ») : batterie interne de {@link #CAPACITY}
 * Osc, {@link #COST_PER_USE} par ping (~20 pings), rechargée automatiquement dans un
 * champ, ou en puisant dans une {@link ResonanceStorageCellItem} portée.
 */
public class ResonanceLocatorItem extends Item {

    /** Rayon du scan de blocs pour les poches (borné pour rester peu coûteux). */
    private static final int SCAN_RADIUS = 32;

    // Valeurs configurables (VeskoriusConfig, section tools) — lues à l'exécution.
    public static int capacity() {
        return VeskoriusConfig.locatorCapacity();
    }

    public static int costPerUse() {
        return VeskoriusConfig.locatorCostPerUse();
    }

    public static int rechargeRate() {
        return VeskoriusConfig.locatorRechargeRate();
    }

    public static int range() {
        return VeskoriusConfig.locatorRange();
    }

    private static final String[] WINDS = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};

    public ResonanceLocatorItem(Properties properties) {
        super(properties);
    }

    // --- Charge ---------------------------------------------------------------

    public static int getCharge(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.LOCATOR_CHARGE.get(), 0);
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.set(ModDataComponents.LOCATOR_CHARGE.get(), Math.clamp(charge, 0, capacity()));
    }

    // --- Usage : ping ---------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (getCharge(stack) < costPerUse()) {
            player.displayClientMessage(Component.translatable("gui.veskorius.locator.empty")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos from = player.blockPosition();
        Hit hit = locateNearest(serverLevel, from);

        setCharge(stack, getCharge(stack) - costPerUse());

        if (hit == null) {
            player.displayClientMessage(Component.translatable("gui.veskorius.locator.none")
                .withStyle(ChatFormatting.GRAY), true);
            level.playSound(null, from, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.5f, 0.6f);
            return InteractionResultHolder.success(stack);
        }

        int dist = (int) Math.round(Math.sqrt(hit.pos.distSqr(from)));
        Component type = Component.translatable(hit.field
            ? "gui.veskorius.locator.type_field" : "gui.veskorius.locator.type_crystal");
        Component dir = Component.translatable("gui.veskorius.dir."
            + windOf(hit.pos.getX() - from.getX(), hit.pos.getZ() - from.getZ()));
        player.displayClientMessage(Component.translatable("gui.veskorius.locator.found", type, dir, dist)
            .withStyle(ChatFormatting.AQUA), true);
        level.playSound(null, from, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.4f);
        emitPointer(serverLevel, player, hit.pos);
        return InteractionResultHolder.success(stack);
    }

    /** Résultat de localisation : position + s'il s'agit d'une signature de champ. */
    private record Hit(BlockPos pos, boolean field) {
    }

    /** Position de la source la plus proche, ou {@code null} (exposé aux GameTest). */
    @Nullable
    public static BlockPos locateForTest(ServerLevel level, BlockPos from) {
        Hit hit = locateNearest(level, from);
        return hit == null ? null : hit.pos;
    }

    @Nullable
    private static Hit locateNearest(ServerLevel level, BlockPos from) {
        // Poche de cristal la plus proche (scan borné : les cristaux ne sont pas indexés).
        BlockPos crystal = nearestCrystal(level, from);
        // Signature de champ la plus proche (via l'index, sans scan).
        BlockPos field = ResonanceFieldManager.nearestSource(level, from, range());

        double crystalSq = crystal == null ? Double.MAX_VALUE : crystal.distSqr(from);
        double fieldSq = field == null ? Double.MAX_VALUE : field.distSqr(from);
        if (crystal == null && field == null) {
            return null;
        }
        return fieldSq < crystalSq ? new Hit(field, true) : new Hit(crystal, false);
    }

    @Nullable
    private static BlockPos nearestCrystal(ServerLevel level, BlockPos from) {
        // Bound the scan cost: crystal detection is capped at SCAN_RADIUS even if the
        // configured range is larger (a full config-range box would be far too big).
        int scan = Math.min(range(), SCAN_RADIUS);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        long bestSq = (long) scan * scan;
        for (int dx = -scan; dx <= scan; dx++) {
            for (int dy = -scan; dy <= scan; dy++) {
                for (int dz = -scan; dz <= scan; dz++) {
                    long sq = (long) dx * dx + (long) dy * dy + (long) dz * dz;
                    if (sq >= bestSq) {
                        continue;
                    }
                    cursor.set(from.getX() + dx, from.getY() + dy, from.getZ() + dz);
                    // isLoaded first: never force-load a chunk from a scan (avoids a stall).
                    if (level.isLoaded(cursor)
                        && level.getBlockState(cursor).is(ModBlocks.RESONANCE_CRYSTAL_CLUSTER.get())) {
                        bestSq = sq;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    /** Traînée de particules du joueur vers la source : retour directionnel visuel. */
    private static void emitPointer(ServerLevel level, Player player, BlockPos target) {
        Vec3 start = player.getEyePosition();
        Vec3 dir = Vec3.atCenterOf(target).subtract(start).normalize();
        for (int i = 1; i <= 6; i++) {
            Vec3 p = start.add(dir.scale(i));
            level.sendParticles(ParticleTypes.WAX_ON, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static String windOf(int dx, int dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz)); // 0 = nord, 90 = est
        int idx = (int) Math.floor((((angle % 360) + 360 + 22.5) % 360) / 45.0) % 8;
        return WINDS[idx];
    }

    // --- Recharge en inventaire ----------------------------------------------

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) {
            return;
        }
        int room = capacity() - getCharge(stack);
        if (room <= 0) {
            return;
        }
        int want = Math.min(room, rechargeRate());
        // Priorité au champ (06-Energy.md), sinon une Storage Cell portée.
        int drawn = ResonanceFieldManager.supply(serverLevel, entity.blockPosition(), want);
        if (drawn <= 0) {
            drawn = drawFromStorageCell(player, want);
        }
        if (drawn > 0) {
            setCharge(stack, getCharge(stack) + drawn);
        }
    }

    private static int drawFromStorageCell(Player player, int want) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack cell = player.getInventory().getItem(slot);
            if (cell.getItem() instanceof ResonanceStorageCellItem && ResonanceStorageCellItem.getCharge(cell) > 0) {
                return ResonanceStorageCellItem.extractCharge(cell, want);
            }
        }
        return 0;
    }

    // --- Affichage ------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.veskorius.resonance_locator.charge", getCharge(stack), capacity())
            .withStyle(ChatFormatting.AQUA));
    }

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
        return 0x33AAFF;
    }

    /** Direction lisible d'un vecteur (exposé aux GameTest). */
    public static String windForTest(Vec3 delta) {
        return windOf((int) delta.x, (int) delta.z);
    }
}
