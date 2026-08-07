package com.veskorius.block.entity;

import com.veskorius.config.HarmonicsConfig;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.menu.FieldEmitterMenu;
import com.veskorius.recipe.EmitterFuelRecipe;
import com.veskorius.recipe.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Field Emitter (machine #4, 05-Machines.md) : premier fournisseur de champ de
 * Résonance. Portée et réserve par défaut : 8 blocs / 4000 Osc — désormais
 * configurables (voir {@link com.veskorius.config.VeskoriusConfig}, section energy).
 *
 * Ce n'est PAS une machine à cycle : elle ne transforme rien en sortie. Elle
 * n'hérite donc pas d'{@link AbstractMachineBlockEntity} (pas de progression, pas
 * de slot d'augment — le Field Emitter est un « bloc passif » au sens du design,
 * les blocs passifs n'acceptent pas le Catalyst Core).
 *
 * Source d'énergie : elle brûle des carburants **data-driven** (type de recette
 * {@code veskorius:fueling} : ingrédient → Osc). Par défaut, un seul carburant, le
 * Stable Resonance Crystal à 4000 Osc (06-Energy.md, « Source primaire de
 * l'énergie ») ; un datapack en ajoute/retire ou change les valeurs sans recompiler
 * (voir {@code 14-Configuration.md}).
 */
public class FieldEmitterBlockEntity extends BlockEntity implements IResonanceField, MenuProvider {

    public static final int DATA_RESERVE = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_COUNT = 2;

    // Portée, capacité de réserve et Osc/cristal sont désormais configurables
    // (VeskoriusConfig, section "energy"). Défauts design : 8 / 4000 / 4000. Lues à
    // l'exécution (jamais mises en cache) : la config SERVER n'est pas chargée au
    // moment où la classe l'est.

    /**
     * Intensité de tous les Field Emitter T2. La valeur exacte n'a pas encore
     * d'effet (aucun consommateur n'a de seuil avant le T4) ; ce qui compte est
     * qu'elle soit identique pour tous, donc l'anti-stacking est neutre entre deux
     * Field Emitter. Le Harmonic Amplifier (T4) émettra une intensité supérieure.
     */
    private static final int FIELD_STRENGTH = 100;

    /**
     * Visualisation de la coupole de champ (pilier 3 : rendre visible l'invisible).
     * Toutes les {@code FIELD_PULSE_INTERVAL} ticks, quelques particules apparaissent
     * sur la sphère de portée quand l'émetteur est actif — au fil du temps elles
     * tracent le dôme, montrant jusqu'où le champ porte sans ouvrir aucun GUI. Épars
     * pour rester lisible et discret. Purement visuel (aucun effet serveur), non
     * couvert par GameTest comme le reste du rendu.
     */
    private static final int FIELD_PULSE_INTERVAL = 40;
    private static final int FIELD_PULSE_POINTS = 6;

    public static final int SLOT_FUEL = 0;

    private final ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isFuel(stack);
        }
    };

    private int reserve;

    /**
     * Dissonance accumulée (06-Energy.md). Injectée par les machines désaccordées qui
     * puisent ici. Au-delà du seuil, le champ devient <b>instable</b> : il saute des
     * ticks d'alimentation, donc les machines hoquettent — un symptôme qu'on voit, pas
     * une dégradation silencieuse. Décroît lentement toute seule.
     */
    private int dissonance;

    /**
     * Compte à rebours entre deux décharges (06-Energy.md, dernière étape de la
     * dissonance). Transitoire : au pire un émetteur saturé décharge à son premier tick
     * après un rechargement de chunk — cohérent, pas un bug. Voir {@link #tickDischarge}.
     */
    private int dischargeCooldown;

    /** Synchronise reserve + capacite vers le GUI (jauge « X/4000 Osc », 12-UX). */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_RESERVE -> reserve;
                case DATA_CAPACITY -> VeskoriusConfig.fieldEmitterCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_RESERVE) {
                reserve = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public FieldEmitterBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FIELD_EMITTER.get(), pos, state);
    }

    /** Pour les variantes d'émetteur (ex. l'Émetteur Accordable). */
    protected FieldEmitterBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                      BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --- Tick ----------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, FieldEmitterBlockEntity emitter) {
        // Idempotent : se (ré)inscrit à l'index tant qu'il tourne. Couvre aussi le
        // premier tick après un rechargement de chunk, sans dépendre de l'ordre
        // exact des callbacks de cycle de vie.
        ResonanceFieldManager.register(level, pos);
        emitter.refuelIfEmpty();
        emitter.decayDissonance();
        emitter.updateLit(level, pos, state);

        if (level instanceof ServerLevel serverLevel) {
            emitter.tickDischarge(serverLevel, pos);
            // Coupole visuelle : montre la portée du champ actif (pilier 3).
            if (emitter.isActive()) {
                emitter.pulseFieldDome(serverLevel, pos);
            }
        }
    }

    /**
     * Reflète « il reste du carburant » dans le blockstate, qui pilote la façade
     * allumée du modèle (voir {@link com.veskorius.block.FieldEmitterBlock#LIT}).
     * N'écrit qu'au changement : un {@code setBlock} par tick recalculerait la lumière
     * en boucle pour rien.
     */
    private void updateLit(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || !state.hasProperty(com.veskorius.block.FieldEmitterBlock.LIT)) {
            return;
        }
        boolean fuelled = reserve > 0;
        if (state.getValue(com.veskorius.block.FieldEmitterBlock.LIT) != fuelled) {
            level.setBlock(pos, state.setValue(com.veskorius.block.FieldEmitterBlock.LIT, fuelled),
                net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    // --- Décharge de résonance (06-Energy.md, dernière étape) -----------------

    /**
     * Quand la dissonance atteint son plafond, le champ libère une <b>impulsion AoE
     * brève</b> — l'écho local de l'Effondrement. C'est la 3<sup>e</sup> et dernière étape
     * d'une dissonance négligée (après : coupole qui grisaille, puis champ intermittent) ;
     * une conséquence qu'on voit venir, jamais silencieuse.
     *
     * <p>La décharge <b>purge une partie</b> de la saturation (soupape) : si la cause
     * persiste — des machines désaccordées qui continuent d'injecter — le champ remonte au
     * plafond et re-décharge, au rythme lisible du cooldown. Sinon, il se rétablit.
     */
    private void tickDischarge(ServerLevel level, BlockPos pos) {
        if (dischargeCooldown > 0) {
            dischargeCooldown--;
            return;
        }
        if (!shouldDischarge()) {
            return;
        }
        discharge(level, pos);
        dischargeCooldown = HarmonicsConfig.dischargeCooldownTicks();
    }

    /** Décision pure « le champ doit-il décharger ? », séparée pour être testable. */
    public boolean shouldDischarge() {
        return HarmonicsConfig.enabled()
            && HarmonicsConfig.dischargeEnabled()
            && dissonance >= HarmonicsConfig.dissonanceCapacity();
    }

    private void discharge(ServerLevel level, BlockPos pos) {
        // Soupape : on évacue une fraction du plafond (au moins 1, sinon un plafond bas
        // avec une fraction nulle bouclerait sans jamais redescendre).
        int release = Math.max(1, (int) Math.round(
            HarmonicsConfig.dissonanceCapacity() * HarmonicsConfig.dischargeReleaseFraction()));
        addDissonance(-release);

        double radius = HarmonicsConfig.dischargeRadius();
        net.minecraft.world.phys.Vec3 center = net.minecraft.world.phys.Vec3.atCenterOf(pos);

        double damage = HarmonicsConfig.dischargeDamage();
        if (damage > 0) {
            net.minecraft.world.damagesource.DamageSource source =
                com.veskorius.energy.ModDamageTypes.discharge(level);
            net.minecraft.world.phys.AABB box =
                new net.minecraft.world.phys.AABB(pos).inflate(radius);
            double radiusSqr = radius * radius;
            for (net.minecraft.world.entity.LivingEntity target
                : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box)) {
                if (target.distanceToSqr(center) > radiusSqr) {
                    continue; // AABB gonflée = cube ; on garde une portée sphérique
                }
                target.hurt(source, (float) damage);
                // Repoussée vers l'extérieur : l'onde « pousse » visiblement.
                net.minecraft.world.phys.Vec3 push = target.position().subtract(center);
                push = (push.lengthSqr() < 1.0e-4 ? new net.minecraft.world.phys.Vec3(0, 1, 0)
                    : push.normalize()).scale(0.6).add(0, 0.3, 0);
                target.push(push.x, push.y, push.z);
            }
        }

        spawnDischargeParticles(level, center, radius);
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CONDUIT_DEACTIVATE,
            net.minecraft.sounds.SoundSource.BLOCKS, 1.2f, 0.6f);
    }

    /** Éclat central + onde de particules de la bande sur la sphère de portée. */
    private void spawnDischargeParticles(ServerLevel level, net.minecraft.world.phys.Vec3 center, double radius) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
            center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        net.minecraft.core.particles.ParticleOptions dust = bandParticle();
        int points = (int) Math.max(24, radius * 8);
        for (int i = 0; i < points; i++) {
            double u = level.random.nextDouble() * 2.0 - 1.0;
            double phi = level.random.nextDouble() * Math.PI * 2.0;
            double s = Math.sqrt(1.0 - u * u);
            level.sendParticles(dust,
                center.x + radius * s * Math.cos(phi),
                center.y + radius * u,
                center.z + radius * s * Math.sin(phi),
                1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Émet quelques particules sur la sphère de portée. Le champ est sphérique
     * (rayon = {@link #getRange()}, voir {@code ResonanceFieldManager}) : les points
     * sont tirés uniformément sur cette sphère pour tracer le dôme réel, pas une
     * approximation. Envoyées via {@link ServerLevel#sendParticles} (les joueurs
     * proches les reçoivent), donc rien à câbler côté client.
     */
    private void pulseFieldDome(ServerLevel level, BlockPos pos) {
        if (level.getGameTime() % FIELD_PULSE_INTERVAL != 0) {
            return;
        }
        double r = getRange();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        // La coupole porte la COULEUR de la bande harmonique : c'est l'interface
        // (12-UX) — on lit l'accord d'un coup d'œil, sans ouvrir un GUI. Un champ
        // dissonant devient plus terne.
        net.minecraft.core.particles.ParticleOptions dust = bandParticle();
        for (int i = 0; i < FIELD_PULSE_POINTS; i++) {
            // Point uniforme sur la sphère : u = cos(theta) tiré uniformément.
            double u = level.random.nextDouble() * 2.0 - 1.0;
            double phi = level.random.nextDouble() * Math.PI * 2.0;
            double s = Math.sqrt(1.0 - u * u);
            double x = cx + r * s * Math.cos(phi);
            double y = cy + r * u;
            double z = cz + r * s * Math.sin(phi);
            level.sendParticles(dust, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Particule de la coupole, teintée par la bande. La saturation baisse avec la
     * dissonance : un champ qui se dérègle « grisaille » avant même de devenir
     * instable — le symptôme se voit venir.
     */
    private net.minecraft.core.particles.ParticleOptions bandParticle() {
        int argb = getBand().color();
        float red = ((argb >> 16) & 0xFF) / 255.0f;
        float green = ((argb >> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;

        if (HarmonicsConfig.enabled() && dissonance > 0) {
            float grey = (red + green + blue) / 3.0f;
            float fouling = Math.min(1.0f, (float) dissonance / HarmonicsConfig.dissonanceCapacity());
            red = red + (grey - red) * fouling;
            green = green + (grey - green) * fouling;
            blue = blue + (grey - blue) * fouling;
        }
        return new net.minecraft.core.particles.DustParticleOptions(
            new org.joml.Vector3f(red, green, blue), 1.0f);
    }

    /**
     * Ne brûle une unité de carburant que lorsque la réserve peut en absorber la
     * pleine valeur : jamais de carburant gaspillé pour combler un petit déficit.
     * Avec les défauts (capacité = valeur d'un cristal), cela revient à « uniquement
     * à réserve nulle » ; si un modpack augmente la capacité, l'émetteur fait le
     * plein de plusieurs unités d'avance. La valeur en Osc vient de la recette de
     * carburant (data-driven), plus d'une constante.
     */
    private void refuelIfEmpty() {
        ItemStack fuelStack = fuel.getStackInSlot(SLOT_FUEL);
        if (fuelStack.isEmpty() || level == null) {
            return;
        }
        EmitterFuelRecipe recipe = findFuel(level, fuelStack);
        if (recipe == null) {
            return;
        }
        if (reserve + recipe.osc() > VeskoriusConfig.fieldEmitterCapacity()) {
            return;
        }
        fuel.extractItem(SLOT_FUEL, 1, false);
        reserve += recipe.osc();
        setChanged();
    }

    /** Vrai si {@code stack} est un carburant enregistré (recette {@code veskorius:fueling}). */
    private boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // Avant que le niveau soit disponible (rare) : permissif, comme le socle des
        // machines pour ses slots d'entrée.
        return level == null || findFuel(level, stack) != null;
    }

    @Nullable
    private static EmitterFuelRecipe findFuel(Level level, ItemStack stack) {
        return level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.FUELING.get(), new SingleRecipeInput(stack), level)
            .map(RecipeHolder::value)
            .orElse(null);
    }

    // --- IResonanceField -----------------------------------------------------

    @Override
    public int getFieldStrength() {
        return FIELD_STRENGTH;
    }

    @Override
    public int getRange() {
        return VeskoriusConfig.fieldEmitterRange();
    }

    @Override
    public boolean isActive() {
        // Un champ instable saute la moitié des ticks : la machine alimentée hoquette
        // visiblement (son glow clignote) au lieu de se dégrader en silence.
        return reserve > 0 && !(isUnstable() && level != null && level.getGameTime() % 4 < 2);
    }

    // --- Harmoniques & Dissonance -------------------------------------------

    @Override
    public int getDissonance() {
        return dissonance;
    }

    /** Ajoute (ou retire, avec un montant négatif — c'est ainsi que le Damping Array purge). */
    @Override
    public void addDissonance(int amount) {
        if (amount == 0) {
            return;
        }
        int capped = Math.clamp(dissonance + amount, 0, HarmonicsConfig.dissonanceCapacity());
        if (capped != dissonance) {
            dissonance = capped;
            setChanged();
        }
    }

    @Override
    public boolean isUnstable() {
        if (!HarmonicsConfig.enabled()) {
            return false;
        }
        return dissonance >= HarmonicsConfig.dissonanceCapacity()
            * HarmonicsConfig.dissonanceUnstableThreshold();
    }

    /** Décroissance naturelle : une base qui cesse de mal se comporter se rétablit seule. */
    private void decayDissonance() {
        if (dissonance <= 0 || level == null || level.getGameTime() % 20 != 0) {
            return;
        }
        int decayed = Math.max(0, dissonance - HarmonicsConfig.dissonanceDecayPerSecond());
        if (decayed != dissonance) {
            dissonance = decayed;
            setChanged();
        }
    }

    @Override
    public int extractOsc(int maxOsc) {
        int drawn = Math.min(maxOsc, reserve);
        if (drawn > 0) {
            reserve -= drawn;
            setChanged();
        }
        return drawn;
    }

    // --- Accès et cycle de vie ----------------------------------------------

    /**
     * Remplit la réserve d'un coup, sans brûler de carburant.
     *
     * <p>Seul appelant : le socle de l'Archive, quand les quatre cotes sont dans l'ordre
     * (voir {@code ArchivePedestalBlockEntity}). C'est la doctrine des donjons appliquée
     * telle quelle — <b>une porte s'ouvre toujours par un champ</b> ; l'énigme ne fait que
     * décider ce qui rallume l'émetteur. Aucun sas n'a de serrure particulière, et il n'y
     * en aura jamais.
     */
    public void restoreReserve(int osc) {
        reserve = Math.min(osc, getCapacity());
        setChanged();
    }

    public ItemStackHandler getFuelHandler() {
        return fuel;
    }

    public ContainerData getData() {
        return data;
    }

    public int getReserve() {
        return reserve;
    }

    public int getCapacity() {
        return VeskoriusConfig.fieldEmitterCapacity();
    }

    // --- MenuProvider (GUI : jauge de réserve + slot de carburant) ------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.veskorius.field_emitter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FieldEmitterMenu(containerId, playerInventory, this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // Couvre le bloc cassé ET le chunk déchargé : dans les deux cas l'émetteur
        // sort de l'index. S'il s'agit d'un déchargement, il se ré-inscrira à son
        // premier tick après rechargement.
        if (level != null) {
            ResonanceFieldManager.unregister(level, worldPosition);
        }
    }

    /** Vide le slot de carburant au sol. Appelé quand le bloc est cassé. */
    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(fuel.getSlots());
        for (int slot = 0; slot < fuel.getSlots(); slot++) {
            container.setItem(slot, fuel.getStackInSlot(slot));
        }
        Containers.dropContents(level, pos, container);
    }

    // --- Persistance ---------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fuel", fuel.serializeNBT(registries));
        tag.putInt("reserve", reserve);
        tag.putInt("dissonance", dissonance);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuel.deserializeNBT(registries, tag.getCompound("fuel"));
        reserve = tag.getInt("reserve");
        dissonance = tag.getInt("dissonance");
    }
}
