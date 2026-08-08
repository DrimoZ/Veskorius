package com.veskorius.block.entity;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * <b>Noyau de Faille</b> — le cœur de la bulle, et la seule chose du mod qui blesse le
 * joueur simplement parce qu'il est là.
 *
 * <p>Non ancré, il inflige des <b>dégâts de déphasage</b> à moins de {@link #HARM_RADIUS}
 * blocs, après {@link #GRACE_TICKS} de présence (06-Energy.md). Ancré, il se tait.
 *
 * <p><b>Le délai de grâce est la mécanique, pas un adoucissement.</b> Sans lui, s'approcher
 * tuerait sans prévenir et la Faille serait un piège ; avec lui, le joueur a trois secondes
 * pour voir l'écran se déformer, comprendre, et reculer. Il peut donc <b>visiter</b> une
 * Faille non ancrée — le temps de repérer où poser l'Ancre — mais pas y séjourner. C'est
 * exactement ce que le dernier palier demande : y aller une fois, sans équipement, pour
 * savoir où revenir avec.
 *
 * <p>Le compteur est <b>par joueur</b> et remis à zéro dès qu'on sort du rayon. Un compteur
 * global punirait le second visiteur pour le temps passé par le premier.
 */
@EventBusSubscriber(modid = com.veskorius.Veskorius.MOD_ID)
public class RiftCoreBlockEntity extends BlockEntity {

    /** Rayon de nocivité, en blocs (06-Energy.md : « Faille non ancrée, &lt; 8 blocs »). */
    public static final int HARM_RADIUS = 8;

    /** Trois secondes avant le premier dégât. Voir la note de classe. */
    public static final int GRACE_TICKS = 20 * 3;

    /** 2 cœurs par seconde une fois le délai écoulé. */
    private static final float DAMAGE = 4.0f;
    private static final int DAMAGE_INTERVAL = 20;

    /**
     * Index des noyaux par dimension. Le handler de dégâts tourne sur chaque joueur à
     * chaque tick ; sans index, il balaierait un cube de blocs par joueur et par tick pour
     * découvrir presque toujours qu'il n'y a rien.
     */
    private static final Map<ResourceKey<Level>, Set<BlockPos>> CORES = new ConcurrentHashMap<>();

    /** Ticks d'exposition accumulés, par joueur. Transitoire : rien à persister. */
    private final Map<java.util.UUID, Integer> exposure = new java.util.HashMap<>();

    private boolean anchored;

    /**
     * Vrai une fois le Gardien vaincu. Trois conséquences, toutes portées par ce seul
     * booléen : la Faille cesse définitivement de blesser (même Ancre coupée), l'extraction
     * s'ouvre, et le Gardien ne réapparaît jamais.
     *
     * <p>Il vit ici et non sur le boss : une marque portée par l'entité disparaîtrait avec
     * elle, et la Faille resterait fermée pour toujours après le seul combat qui devait
     * l'ouvrir. Le dossier a explicitement rejeté un boss répétable (09-Entities.md) — ce
     * champ est ce qui l'empêche.
     */
    private boolean cleared;

    /** Vrai si le Gardien a déjà été appelé, vaincu ou non : on n'en invoque pas deux. */
    private boolean guardianSummoned;

    public RiftCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RIFT_CORE.get(), pos, state);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            CORES.remove(serverLevel.dimension());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  RiftCoreBlockEntity core) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CORES.computeIfAbsent(level.dimension(), k -> new LinkedHashSet<>()).add(pos.immutable());
        core.tickHarm(serverLevel, pos);
        if (level.getGameTime() % 10 == 0) {
            serverLevel.sendParticles(
                core.anchored ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                core.anchored ? 2 : 8, 0.4, 0.4, 0.4, 0.02);
        }
    }

    /**
     * <b>Corrosion ambiante</b> : ce qui reste d'une Faille une fois ancrée.
     *
     * <p>L'Ancre supprime les dégâts aigus, pas la Faille. Elle continue de ronger
     * l'équipement porté à {@link #CORROSION_RADIUS} blocs — un point d'usure toutes les
     * {@link #CORROSION_INTERVAL} ticks. Non létal, et c'est le but : on peut y <b>entrer</b>,
     * on ne peut pas y <b>exploiter</b> sans Rift Ward Emitter.
     *
     * <p>Le terme figurait dans 05-Machines.md sans définition nulle part : le Ward
     * annulait donc une mécanique inexistante. La définir était la seule façon de lui
     * donner un métier — une machine dont l'effet est « rien » est pire qu'une machine
     * absente, parce qu'on la fabrique et qu'on ne comprend pas pourquoi.
     */
    public static final int CORROSION_RADIUS = 12;
    private static final int CORROSION_INTERVAL = 20 * 5;

    private void tickCorrosion(ServerLevel level, BlockPos pos) {
        if (level.getGameTime() % CORROSION_INTERVAL != 0) {
            return;
        }
        var box = new net.minecraft.world.phys.AABB(pos).inflate(CORROSION_RADIUS);
        long radiusSqr = (long) CORROSION_RADIUS * CORROSION_RADIUS;
        for (net.minecraft.world.entity.player.Player player
            : level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, box)) {
            if (player.blockPosition().distSqr(pos) > radiusSqr
                || RiftWardEmitterBlockEntity.isWarded(level, player.blockPosition())) {
                continue;
            }
            corrode(player);
        }
    }

    /**
     * Ronge une pièce d'équipement au hasard parmi celles qui s'usent. On tire une seule
     * pièce par passage plutôt que toutes : corroder l'armure entière d'un coup viderait
     * une panoplie en quelques minutes, et le joueur fuirait la Faille au lieu de
     * construire le Ward qui existe pour ça.
     */
    private static void corrode(net.minecraft.world.entity.player.Player player) {
        java.util.List<net.minecraft.world.item.ItemStack> wearable = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.EquipmentSlot slot
            : net.minecraft.world.entity.EquipmentSlot.values()) {
            net.minecraft.world.item.ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                wearable.add(stack);
            }
        }
        if (wearable.isEmpty()) {
            return;
        }
        net.minecraft.world.item.ItemStack victim =
            wearable.get(player.getRandom().nextInt(wearable.size()));
        victim.setDamageValue(Math.min(victim.getMaxDamage() - 1, victim.getDamageValue() + 1));
    }

    private void tickHarm(ServerLevel level, BlockPos pos) {
        // Faille purgée : plus aucun dégât, Ancre ou pas. C'est la récompense du boss, et
        // elle doit survivre au démontage de l'installation — sinon « définitivement
        // stable » (09-Entities.md) ne veut rien dire.
        if (cleared) {
            exposure.clear();
            return;
        }
        if (anchored) {
            exposure.clear();
            tickCorrosion(level, pos);
            return;
        }
        var box = new net.minecraft.world.phys.AABB(pos).inflate(HARM_RADIUS);
        long radiusSqr = (long) HARM_RADIUS * HARM_RADIUS;
        Set<java.util.UUID> present = new java.util.HashSet<>();

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target.blockPosition().distSqr(pos) > radiusSqr) {
                continue; // AABB gonflée = cube ; on garde une portée sphérique
            }
            present.add(target.getUUID());
            int ticks = exposure.merge(target.getUUID(), 1, Integer::sum);
            if (ticks > GRACE_TICKS && (ticks - GRACE_TICKS) % DAMAGE_INTERVAL == 0) {
                // L'armure d'alliage répond au déphasage — c'est sa seule vraie raison
                // d'être, sa protection étant celle du diamant. Facteur nul avec le
                // Rift-Ward Plate : la Faille cesse de mordre.
                float damage = DAMAGE * com.veskorius.item.VeskoriusArmor.phaseDamageFactor(target);
                if (damage > 0.0f) {
                    target.hurt(com.veskorius.energy.ModDamageTypes.discharge(level), damage);
                }
            }
        }
        // Sorti du rayon = compteur remis à zéro : le déphasage ne s'accumule pas d'une
        // visite à l'autre, sinon la deuxième approche serait mortelle sans raison lisible.
        exposure.keySet().retainAll(present);
    }

    // --- Épuisement (05-Machines.md #20) -------------------------------------

    /**
     * Extractions maximales par Faille. <b>Le compteur vit ici, sur le noyau</b>, et pas
     * sur l'Extractor — sinon casser l'Extractor et en reposer un remettrait le compteur à
     * zéro, et la « seule ressource volontairement finie du mod » (04-Materials.md)
     * deviendrait infinie au prix d'un aller-retour à l'établi.
     */
    public static final int MAX_EXTRACTIONS = 6;

    private int extractions;

    /**
     * Vrai s'il reste quelque chose à extraire. <b>Exige la Faille purgée</b> : avant le
     * Gardien, un Extracteur posé ne rend rien (09-Entities.md, « le Rift Core Extractor
     * devient utilisable » APRÈS la victoire). Sans cette condition, le boss serait
     * facultatif et la fin de partie se contournerait en l'ignorant.
     */
    public boolean canExtract() {
        return cleared && extractions < MAX_EXTRACTIONS;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        if (this.cleared != cleared) {
            this.cleared = cleared;
            setChanged();
        }
    }

    /** Marque le Gardien comme appelé. Vrai si c'est la première fois. */
    public boolean claimGuardianSummon() {
        if (guardianSummoned) {
            return false;
        }
        guardianSummoned = true;
        setChanged();
        return true;
    }

    public int getExtractionsLeft() {
        return Math.max(0, MAX_EXTRACTIONS - extractions);
    }

    /** Consomme une extraction. Vrai si elle a eu lieu. */
    public boolean consumeExtraction() {
        if (!canExtract()) {
            return false;
        }
        extractions++;
        setChanged();
        return true;
    }

    /** Vrai si un Rift Anchor tient cette Faille. Écrit par l'Ancre, jamais deviné ici. */
    public boolean isAnchored() {
        return anchored;
    }

    public void setAnchored(boolean anchored) {
        if (this.anchored != anchored) {
            this.anchored = anchored;
            setChanged();
        }
    }

    /** Le noyau le plus proche dans un rayon, ou {@code null}. Sert au Rift Anchor. */
    @org.jetbrains.annotations.Nullable
    public static RiftCoreBlockEntity nearest(ServerLevel level, BlockPos from, int maxRange) {
        Set<BlockPos> set = CORES.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return null;
        }
        double bestSq = (double) maxRange * maxRange;
        RiftCoreBlockEntity best = null;
        for (BlockPos p : set.toArray(BlockPos[]::new)) {
            if (!(level.getBlockEntity(p) instanceof RiftCoreBlockEntity core)) {
                set.remove(p);
                continue;
            }
            double d = p.distSqr(from);
            if (d <= bestSq) {
                bestSq = d;
                best = core;
            }
        }
        return best;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("anchored", anchored);
        tag.putInt("extractions", extractions);
        tag.putBoolean("cleared", cleared);
        tag.putBoolean("guardianSummoned", guardianSummoned);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        anchored = tag.getBoolean("anchored");
        extractions = tag.getInt("extractions");
        cleared = tag.getBoolean("cleared");
        guardianSummoned = tag.getBoolean("guardianSummoned");
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            Set<BlockPos> set = CORES.get(level.dimension());
            if (set != null) {
                set.remove(worldPosition);
            }
        }
        super.setRemoved();
    }
}
