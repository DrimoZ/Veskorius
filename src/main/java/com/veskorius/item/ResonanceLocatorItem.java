package com.veskorius.item;

import com.mojang.datafixers.util.Pair;
import com.veskorius.block.ModBlocks;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.tag.ModTags;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
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

    /**
     * Rayon du scan de blocs pour les poches. Volontairement court : un ping scanne
     * un cube {@code (2r+1)³}, soit ~35 700 blocs à r=16 — déjà lourd pour un clic
     * droit, et un r=32 le multipliait par ~8 (≈275 000, hitch serveur perceptible).
     * Les cristaux ne « rayonnent » que de près ; la détection longue portée (jusqu'à
     * {@link #range()}) reste réservée aux signatures de champ, via l'index O(n) du
     * {@link ResonanceFieldManager}, sans aucun scan de blocs.
     */
    private static final int SCAN_RADIUS = 16;

    /** Rayon de recherche de structure (en chunks) pour le mode Structures — API vanilla. */
    private static final int STRUCTURE_SEARCH_CHUNKS = 32;

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
        // Shift + clic droit : change de mode (sans ping ni consommation de charge).
        if (player.isShiftKeyDown()) {
            LocatorMode next = getMode(stack).next();
            setMode(stack, next);
            player.displayClientMessage(Component.translatable("item.veskorius.resonance_locator.mode",
                Component.translatable(next.labelKey())).withStyle(ChatFormatting.AQUA), true);
            return InteractionResultHolder.success(stack);
        }
        if (getCharge(stack) < costPerUse()) {
            player.displayClientMessage(Component.translatable("gui.veskorius.locator.empty")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos from = player.blockPosition();
        LocatorMode mode = getMode(stack);
        Hit hit = mode == LocatorMode.STRUCTURES
            ? locateStructure(serverLevel, from)
            : locateResource(serverLevel, from);

        setCharge(stack, getCharge(stack) - costPerUse());

        if (hit == null) {
            String key = mode == LocatorMode.STRUCTURES
                ? "gui.veskorius.locator.no_structure" : "gui.veskorius.locator.none";
            player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), true);
            level.playSound(null, from, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.5f, 0.6f);
            return InteractionResultHolder.success(stack);
        }

        int dist = (int) Math.round(Math.sqrt(hit.pos.distSqr(from)));
        Component type = Component.translatable(hit.typeKey);
        Component dir = Component.translatable("gui.veskorius.dir."
            + windOf(hit.pos.getX() - from.getX(), hit.pos.getZ() - from.getZ()));
        player.displayClientMessage(Component.translatable("gui.veskorius.locator.found", type, dir, dist)
            .withStyle(ChatFormatting.AQUA), true);
        level.playSound(null, from, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.4f);
        emitPointer(serverLevel, player, hit.pos);
        return InteractionResultHolder.success(stack);
    }

    // --- Mode (outil à modes, 16 §1) -----------------------------------------

    public static LocatorMode getMode(ItemStack stack) {
        return LocatorMode.byIndex(stack.getOrDefault(ModDataComponents.LOCATOR_MODE.get(), 0));
    }

    public static void setMode(ItemStack stack, LocatorMode mode) {
        stack.set(ModDataComponents.LOCATOR_MODE.get(), mode.ordinal());
    }

    // --- Localisation --------------------------------------------------------

    /** Résultat de localisation : position + clé de langue du type de source. */
    private record Hit(BlockPos pos, String typeKey) {
    }

    /** Position de la ressource la plus proche, ou {@code null} (exposé aux GameTest). */
    @Nullable
    public static BlockPos locateForTest(ServerLevel level, BlockPos from) {
        Hit hit = locateResource(level, from);
        return hit == null ? null : hit.pos;
    }

    @Nullable
    private static Hit locateResource(ServerLevel level, BlockPos from) {
        // Poche de cristal la plus proche (scan borné) + signature de champ (index, sans scan).
        BlockPos crystal = nearestCrystal(level, from);
        BlockPos field = ResonanceFieldManager.nearestSource(level, from, range());

        double crystalSq = crystal == null ? Double.MAX_VALUE : crystal.distSqr(from);
        double fieldSq = field == null ? Double.MAX_VALUE : field.distSqr(from);
        if (crystal == null && field == null) {
            return null;
        }
        return fieldSq < crystalSq
            ? new Hit(field, "gui.veskorius.locator.type_field")
            : new Hit(crystal, "gui.veskorius.locator.type_crystal");
    }

    @Nullable
    private static Hit locateStructure(ServerLevel level, BlockPos from) {
        BlockPos pos = nearestLocatableStructure(level, from);
        return pos == null ? null : new Hit(pos, "gui.veskorius.locator.type_structure");
    }

    /**
     * Structure du tag {@code #veskorius:locatable} la plus proche, via l'API vanilla
     * {@code findNearestMapStructure} — <b>aucun scan de blocs</b> (16 §1). Retourne
     * {@code null} tant qu'aucune vraie structure n'est taguée (les structures actuelles
     * sont des <i>features</i> ; se remplira à la migration). Exposé aux GameTest.
     */
    @Nullable
    public static BlockPos nearestLocatableStructure(ServerLevel level, BlockPos from) {
        Optional<HolderSet.Named<Structure>> tag = level.registryAccess()
            .registryOrThrow(Registries.STRUCTURE).getTag(ModTags.Structures.LOCATABLE);
        if (tag.isEmpty() || tag.get().size() == 0) {
            return null;
        }
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
            .findNearestMapStructure(level, tag.get(), from, STRUCTURE_SEARCH_CHUNKS, false);
        return found == null ? null : found.getFirst();
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

    /**
     * Puise {@code want} Osc sur les Storage Cell portées, en <b>enchaînant les cellules</b>
     * jusqu'à obtenir le compte. S'arrêter à la première non vide donnait une recharge
     * amputée dès qu'une cellule presque à plat passait devant une cellule pleine : le
     * joueur transporte l'énergie, l'outil doit la voir en entier.
     */
    private static int drawFromStorageCell(Player player, int want) {
        int drawn = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && drawn < want; slot++) {
            ItemStack cell = player.getInventory().getItem(slot);
            if (cell.getItem() instanceof ResonanceStorageCellItem) {
                drawn += ResonanceStorageCellItem.extractCharge(cell, want - drawn);
            }
        }
        return drawn;
    }

    // --- Affichage ------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.veskorius.resonance_locator.charge", getCharge(stack), capacity())
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.veskorius.resonance_locator.mode",
            Component.translatable(getMode(stack).labelKey())).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.veskorius.resonance_locator.mode_hint")
            .withStyle(ChatFormatting.DARK_GRAY));
        // Le HUD de champ ne s'explique nulle part ailleurs : il apparaît du seul fait de
        // porter cet objet, donc l'objet doit le dire (12-UX, onboarding).
        tooltip.add(Component.translatable("item.veskorius.resonance_locator.hud_hint")
            .withStyle(ChatFormatting.DARK_GRAY));
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
