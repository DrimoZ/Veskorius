package com.veskorius.block.entity;

import com.veskorius.block.AbstractMachineBlock;
import com.veskorius.config.HarmonicsConfig;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.energy.HarmonicBand;
import com.veskorius.energy.IResonanceField;
import com.veskorius.energy.ResonanceFieldManager;
import com.veskorius.tag.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Socle commun a toutes les machines actives de Veskorius (05-Machines.md,
 * tableau "Recettes de fonctionnement").
 *
 * Ce que cette classe impose a ses 23 sous-classes futures :
 *
 * 1. Un inventaire unique, dont le DERNIER slot est TOUJOURS le slot d'augment.
 *    C'est la convention qui permet au slot d'augment d'exister sur toutes les
 *    machines sans code specifique, comme demande par 11-Development-Plan.md
 *    (Phase 1, tache 15 : "implementer le slot des cette phase pour eviter de le
 *    retrofit plus tard sur des machines deja codees").
 * 2. Un cycle unique : tant que {@link #canRunCycle()} est vrai, la progression
 *    monte d'un tick par tick ; arrivee au bout, {@link #runCycle()} est appelee.
 *    Si la condition redevient fausse en cours de route, la progression est
 *    remise a zero — pas de cycle "en pause" a mi-parcours.
 *
 * La consommation d'energie (Osc) n'est PAS geree ici : le Resonance Stabilizer
 * est autonome (05-Machines.md #1) et le systeme de champ n'arrive qu'a la
 * tache 5 de la Phase 1 (capability IResonanceField). Elle se greffera sur
 * {@link #canRunCycle()} a ce moment-la.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_MANUAL = 2;
    public static final int DATA_REDSTONE_MODE = 3;
    public static final int DATA_OVERHEAT = 4;
    /** 6 valeurs (une par face, indexées par {@code Direction.get3DDataValue}). */
    public static final int DATA_SIDE_BASE = 5;
    public static final int DATA_AUTO_INPUT = 11;
    public static final int DATA_AUTO_OUTPUT = 12;
    public static final int DATA_COUNT = 13;

    protected final ItemStackHandler inventory;

    private final int augmentSlot;
    private int progress;

    /** Interrupteur manuel (bouton du GUI). Machine allumee par defaut. */
    private boolean manualEnabled = true;
    /** Mode de controle redstone (bouton du GUI). */
    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;
    /**
     * Surchauffe active (bouton du GUI + Resonance Tuner, tache 9). N'a d'effet
     * que sur les machines qui la supportent ({@link #supportsOverheat}).
     */
    private boolean overheatEnabled = false;

    // --- Automatisation d'objets (item I/O, PAS l'énergie) -------------------
    // L'énergie reste sans tuyaux (par champ) ; en revanche les objets peuvent
    // circuler via hopper/automatisation, configurables par face (12-UX-and-
    // Advancements.md). Défaut « façon four » pour marcher tout de suite : sortie
    // par le dessous, entrée par les autres faces. Un futur écran de config
    // permettra de personnaliser par face + activer l'auto-I/O.

    private static final int[] NO_SLOTS = new int[0];
    private static final int AUTO_INTERVAL = 8;

    private final SideMode[] sideModes = defaultSideModes();
    private boolean autoInput = false;
    private boolean autoOutput = false;

    /** Bande harmonique ; {@code null} = machine universelle (défaut, tout le T1). */
    @Nullable
    private HarmonicBand harmonicBand;

    // --- Lecture visuelle de l'accord (06-Energy.md, 12-UX) ------------------
    // La bande est une COULEUR : le glow d'une machine en marche prend la couleur de
    // SA bande, et clignote entre les deux couleurs quand elle est désaccordée. On
    // diagnostique donc sa base en la regardant, sans ouvrir un GUI. Ces deux champs
    // sont le dernier état constaté au tick d'alimentation ; ils ne sont pas persistés
    // (ils se recalculent au premier tick) et ne servent qu'à l'affichage.

    /** Intervalle entre deux bouffées de particules de bande, en ticks. */
    private static final int BAND_GLOW_INTERVAL = 10;
    private static final int BAND_GLOW_POINTS = 2;
    /** Demi-période du clignotement d'une machine désaccordée, en ticks. */
    private static final int DETUNE_BLINK_HALF_PERIOD = 20;

    /** Vrai si le dernier tick d'alimentation s'est fait en désaccord. */
    private boolean detuned;
    /** Bande du champ qui a servi la machine au dernier tick, {@code null} si hors champ. */
    @Nullable
    private HarmonicBand fieldBand;

    private static SideMode[] defaultSideModes() {
        SideMode[] modes = new SideMode[6];
        for (Direction dir : Direction.values()) {
            modes[dir.get3DDataValue()] = dir == Direction.DOWN ? SideMode.OUTPUT : SideMode.INPUT;
        }
        return modes;
    }

    /**
     * Durée effective du cycle courant, mise à jour côté serveur à chaque tick et
     * synchronisée vers le client via la ContainerData. Doit être un champ stocké
     * (et non un simple recalcul dans le getter) : depuis que le temps vient de la
     * recette, il dépend des entrées, or l'inventaire de la block entity n'est PAS
     * synchronisé au client — seule cette valeur l'est. Le client la lit telle
     * quelle pour dimensionner la barre de progression.
     */
    private int maxProgress = 1;

    /**
     * Synchronisation client de la barre de progression et de l'état des boutons.
     * Passe par ContainerData (mécanisme vanilla du four) plutôt que par un packet
     * custom — voir 12-UX-and-Advancements.md.
     */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index >= DATA_SIDE_BASE && index < DATA_SIDE_BASE + sideModes.length) {
                return sideModes[index - DATA_SIDE_BASE].ordinal();
            }
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> maxProgress;
                case DATA_MANUAL -> manualEnabled ? 1 : 0;
                case DATA_REDSTONE_MODE -> redstoneMode.ordinal();
                case DATA_OVERHEAT -> overheatEnabled ? 1 : 0;
                case DATA_AUTO_INPUT -> autoInput ? 1 : 0;
                case DATA_AUTO_OUTPUT -> autoOutput ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Cote client : stocke la valeur synchronisee pour que la barre et les
            // boutons du GUI reflètent l'état réel du serveur.
            if (index >= DATA_SIDE_BASE && index < DATA_SIDE_BASE + sideModes.length) {
                sideModes[index - DATA_SIDE_BASE] = SideMode.byIndex(value);
                return;
            }
            switch (index) {
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = value;
                case DATA_MANUAL -> manualEnabled = value != 0;
                case DATA_REDSTONE_MODE -> redstoneMode = RedstoneMode.byIndex(value);
                case DATA_OVERHEAT -> overheatEnabled = value != 0;
                case DATA_AUTO_INPUT -> autoInput = value != 0;
                case DATA_AUTO_OUTPUT -> autoOutput = value != 0;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount) {
        super(type, pos, state);
        this.augmentSlot = slotCount - 1;
        this.inventory = new ItemStackHandler(slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                onSlotChanged(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return AbstractMachineBlockEntity.this.isItemValid(slot, stack);
            }
        };
    }

    /**
     * Appelé quand un slot change. Point d'extension : une machine à recette
     * l'utilise pour invalider sa recette en cache quand une entrée bouge.
     */
    protected void onSlotChanged(int slot) {
    }

    // --- Contrat des sous-classes -------------------------------------------

    /** Duree d'un cycle en ticks, sans augment (20 ticks = 1 seconde). */
    protected abstract int getBaseCycleTicks();

    /** Entrees presentes ET place disponible en sortie. Evalue a chaque tick. */
    protected abstract boolean canRunCycle();

    /** Consomme les entrees et produit la sortie. Appele une seule fois par cycle. */
    protected abstract void runCycle();

    /**
     * Osc consommes par tick d'avancement, hors surchauffe (05-Machines.md,
     * colonne Energie). 0 = machine autonome (Stabilizer, Whetstone) : elle n'a
     * besoin d'aucun champ. Redefinir pour une machine qui puise dans le champ.
     */
    protected int getOscPerTick() {
        return 0;
    }

    /**
     * Vrai si cette machine possede un mode surchauffe (05-Machines.md #5, #15).
     * Par defaut non ; le Flux Purifier et la Deep Synthesis Chamber le redefinissent.
     */
    public boolean supportsOverheat() {
        return false;
    }

    // --- Cycle ---------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractMachineBlockEntity machine) {
        machine.tickCycle();
        machine.tickAutomation();
        machine.pulseBandGlow();
    }

    private void tickCycle() {
        // Tenu à jour côté serveur pour la synchro de la barre de progression : le
        // temps de cycle dépend désormais de la recette (donc des entrées).
        maxProgress = getEffectiveCycleTicks();

        if (!canRunCycle()) {
            // Ingredient absent ou sortie pleine : le cycle ne peut pas exister,
            // on remet a zero (contrairement aux pauses ci-dessous).
            setLit(false);
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }

        if (!isControlEnabled()) {
            // Coupee a la main ou par la redstone : PAUSE (progression conservee),
            // et aucun Osc n'est preleve tant qu'elle est coupee.
            setLit(false);
            return;
        }

        if (!drawEnergy()) {
            // Pas assez d'Osc ce tick : PAUSE aussi. Une coupure de courant breve
            // ne doit pas gacher le travail deja fait. Machine eteinte : c'est le
            // seul retour visuel « hors champ » que voit le joueur (pilier 3).
            setLit(false);
            return;
        }

        // La machine avance vraiment ce tick : allumee (retour visuel « en marche »).
        setLit(true);
        progress++;
        if (progress >= getEffectiveCycleTicks()) {
            runCycle();
            progress = 0;
        }
        setChanged();
    }

    /**
     * Reflete l'etat « en marche » dans le blockstate ({@link AbstractMachineBlock#LIT}),
     * qui pilote le glow du bloc. N'ecrit qu'au changement (pas de setBlock inutile a
     * chaque tick). Meme mecanisme que le four vanilla.
     */
    private void setLit(boolean lit) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(AbstractMachineBlock.LIT)
            && state.getValue(AbstractMachineBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(AbstractMachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /**
     * Glow harmonique : quelques particules de la <b>couleur de la bande</b> au-dessus
     * d'une machine en marche (06-Energy.md, « Lecture visuelle — aucun GUI à
     * apprendre »). Désaccordée, elle <b>clignote entre sa couleur et celle du champ</b>
     * — le « ça grince » visuel qui remplace un compteur.
     *
     * <p>Une machine <b>universelle</b> (toute la T1) n'émet rien du tout : la couche
     * harmonique reste littéralement invisible tant que le joueur n'a rien accordé.
     * Purement client-visuel (aucun effet serveur), comme la coupole de l'émetteur.
     */
    private void pulseBandGlow() {
        // Test le moins cher d'abord : une machine universelle (tout le T1) sort ici sur
        // une simple lecture de champ, sans toucher à la config ni au temps du monde.
        if (harmonicBand == null || !HarmonicsConfig.enabled()
            || !(level instanceof ServerLevel serverLevel)
            || serverLevel.getGameTime() % BAND_GLOW_INTERVAL != 0) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(AbstractMachineBlock.LIT) || !state.getValue(AbstractMachineBlock.LIT)) {
            return; // le glow ne colore que ce qui tourne réellement
        }

        HarmonicBand shown = glowBand(serverLevel.getGameTime());
        int argb = shown.color();
        net.minecraft.core.particles.ParticleOptions dust =
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(
                ((argb >> 16) & 0xFF) / 255.0f,
                ((argb >> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f), 1.0f);

        for (int i = 0; i < BAND_GLOW_POINTS; i++) {
            double x = worldPosition.getX() + 0.2 + serverLevel.random.nextDouble() * 0.6;
            double y = worldPosition.getY() + 1.05;
            double z = worldPosition.getZ() + 0.2 + serverLevel.random.nextDouble() * 0.6;
            serverLevel.sendParticles(dust, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Couleur à montrer ce tick : la bande de la machine, ou — si elle est désaccordée —
     * une alternance avec celle du champ. Deux couleurs qui se succèdent au même endroit
     * disent « ces deux-là ne s'accordent pas » sans aucun texte.
     */
    private HarmonicBand glowBand(long gameTime) {
        if (!detuned || fieldBand == null) {
            return harmonicBand;
        }
        boolean showField = (gameTime % (2L * DETUNE_BLINK_HALF_PERIOD)) >= DETUNE_BLINK_HALF_PERIOD;
        return showField ? fieldBand : harmonicBand;
    }

    /** Vrai si le dernier tick d'alimentation s'est fait en désaccord (pilote le clignotement). */
    public boolean isDetuned() {
        return detuned;
    }

    /** Bande du champ qui sert cette machine, {@code null} si elle n'en a pas (ou hors champ). */
    @Nullable
    public HarmonicBand getFieldBand() {
        return fieldBand;
    }

    /**
     * Preleve le cout d'un tick sur le champ de Resonance. Vrai si le plein cout a
     * ete obtenu (la machine peut avancer), faux sinon (pause).
     *
     * Un prelevement partiel (reserve d'emetteur presque vide) est tout de meme
     * consomme : c'est volontaire, il vide l'emetteur jusqu'a zero pour declencher
     * son rechargement au tick suivant. Le "gachis" ainsi induit vaut au plus
     * {@code cout - 1} Osc par cristal brule, soit 1 Osc sur 4000 en pratique.
     */
    private boolean drawEnergy() {
        int cost = getEffectiveOscPerTick();
        if (cost <= 0) {
            // Machine autonome : aucun champ en jeu, donc jamais de désaccord à montrer.
            detuned = false;
            fieldBand = null;
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Harmoniques (06-Energy.md) : puiser sur une bande différente de la sienne
        // coûte plus cher et injecte de la dissonance dans le champ. Ça ne bloque
        // JAMAIS la machine — elle tourne, elle grince.
        IResonanceField source = ResonanceFieldManager.findSource(serverLevel, worldPosition);
        detuned = isDetunedFrom(source);
        fieldBand = source == null ? null : source.getBand();
        if (detuned) {
            cost = (int) Math.ceil(cost * HarmonicsConfig.detuneOscMultiplier());
        }

        boolean powered = ResonanceFieldManager.supply(serverLevel, worldPosition, cost) >= cost;
        if (powered && detuned && source != null) {
            source.addDissonance(HarmonicsConfig.dissonancePerDetunedTick());
        }
        return powered;
    }

    /**
     * Vrai si cette machine tourne sur une bande différente de celle du champ qui la
     * sert. Faux dans tous les cas « simples » : harmoniques désactivées, machine
     * universelle (tout le T1), ou recette marquée {@code stable} — ces recettes sont
     * increvables quel que soit le déréglage.
     */
    public boolean isDetunedFrom(@Nullable IResonanceField source) {
        if (!HarmonicsConfig.enabled() || source == null || harmonicBand == null) {
            return false;
        }
        if (isCurrentRecipeStable()) {
            return false;
        }
        return harmonicBand != source.getBand();
    }

    /**
     * Vrai si la recette en cours est déclarée increvable (`stable`). Redéfini par les
     * machines à recette ; par défaut non.
     */
    protected boolean isCurrentRecipeStable() {
        return false;
    }

    // --- Bande harmonique de la machine --------------------------------------

    /**
     * Vrai si cette machine peut porter une bande (donc être accordée/désaccordée).
     * Faux par défaut : **tout le T1 reste universel**, aucune complexité imposée au
     * début de partie (06-Energy.md, courbe d'introduction).
     */
    public boolean supportsHarmonicBand() {
        return false;
    }

    /** Bande courante, ou {@code null} = machine universelle (accepte tout champ). */
    @Nullable
    public HarmonicBand getHarmonicBand() {
        return harmonicBand;
    }

    public void setHarmonicBand(@Nullable HarmonicBand band) {
        this.harmonicBand = band;
        setChanged();
    }

    /**
     * Vrai si le cycle du Tuner peut ramener la machine à l'état <b>universel</b>. Sans
     * ça, accorder serait un geste à sens unique : une machine accordée sur la mauvaise
     * bande resterait définitivement moins bonne qu'avant qu'on y touche. Une machine T3
     * qui <i>doit</i> porter une bande le redéfinira à {@code false}.
     */
    protected boolean allowsUniversal() {
        return true;
    }

    /** Fait défiler la bande (mode « Accorder » du Resonance Tuner). */
    public void cycleHarmonicBand() {
        if (!supportsHarmonicBand()) {
            return;
        }
        if (harmonicBand == null) {
            setHarmonicBand(HarmonicBand.FUNDAMENTAL);
            return;
        }
        HarmonicBand next = harmonicBand.next(HarmonicsConfig.bandCount());
        // Le cycle repasse par l'universel en bouclant : le geste est réversible.
        setHarmonicBand(next == HarmonicBand.FUNDAMENTAL && allowsUniversal() ? null : next);
    }

    /** Cout d'un tick, surchauffe comprise (consommation multipliee, 06-Energy.md ; facteur configurable). */
    public int getEffectiveOscPerTick() {
        int base = getOscPerTick();
        return isOverheatActive()
            ? (int) Math.round(base * VeskoriusConfig.overheatOscMultiplier())
            : base;
    }

    // --- Controle (redstone, interrupteur manuel, surchauffe) ----------------

    /**
     * Vrai si la machine a le droit d'avancer ce tick, du point de vue du controle
     * (interrupteur manuel + redstone). Independant des ingredients et de
     * l'energie, verifies separement.
     */
    public boolean isControlEnabled() {
        if (!manualEnabled) {
            return false;
        }
        if (redstoneMode == RedstoneMode.IGNORED) {
            return true;
        }
        boolean powered = level != null && level.hasNeighborSignal(worldPosition);
        return redstoneMode.allowsRunning(powered);
    }

    public boolean isManualEnabled() {
        return manualEnabled;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public boolean isOverheatEnabled() {
        return overheatEnabled;
    }

    /** Vrai si la surchauffe est active ET supportee — condition d'effet reelle. */
    public boolean isOverheatActive() {
        return supportsOverheat() && overheatEnabled;
    }

    // Mutations, appelees par le menu (boutons) et plus tard par le Resonance Tuner.

    public void toggleManual() {
        setManualEnabled(!manualEnabled);
    }

    public void cycleRedstoneMode() {
        setRedstoneMode(redstoneMode.next());
    }

    public void toggleOverheat() {
        if (supportsOverheat()) {
            setOverheatEnabled(!overheatEnabled);
        }
    }

    public void setManualEnabled(boolean enabled) {
        this.manualEnabled = enabled;
        setChanged();
    }

    public void setRedstoneMode(RedstoneMode mode) {
        this.redstoneMode = mode;
        setChanged();
    }

    public void setOverheatEnabled(boolean enabled) {
        this.overheatEnabled = enabled;
        setChanged();
    }

    // --- Augment -------------------------------------------------------------

    public int getAugmentSlot() {
        return augmentSlot;
    }

    public boolean hasAugment() {
        return inventory.getStackInSlot(augmentSlot).is(ModTags.Items.MACHINE_AUGMENTS);
    }

    /**
     * Duree reelle d'un cycle, augment compris. Plancher a 1 tick pour qu'une
     * machine tres rapide ne puisse jamais tomber a 0 et boucler indefiniment
     * dans le meme tick.
     */
    public int getEffectiveCycleTicks() {
        int base = getBaseCycleTicks();
        // Surchauffe : temps divise par un facteur configurable (06-Energy.md,
        // defaut 2). Appliquee avant l'augment, les deux se cumulent.
        if (isOverheatActive()) {
            base = Math.max(1, (int) Math.round(base / VeskoriusConfig.overheatSpeedMultiplier()));
        }
        if (hasAugment()) {
            base = Math.max(1, (int) Math.round(base / VeskoriusConfig.augmentSpeedMultiplier()));
        }
        return Math.max(1, base);
    }

    // --- Automatisation d'objets ---------------------------------------------

    /**
     * Slots réels où l'automatisation externe peut insérer. Vide par défaut : une
     * machine qui ne redéfinit pas ces deux méthodes n'expose aucune capability
     * (opt-in). Les sous-classes « traitement » et le Whetstone les redéfinissent.
     */
    protected int[] getAutomationInputSlots() {
        return NO_SLOTS;
    }

    /** Slots réels d'où l'automatisation externe peut extraire (la sortie). */
    protected int[] getAutomationOutputSlots() {
        return NO_SLOTS;
    }

    public SideMode getSideMode(Direction side) {
        return sideModes[side.get3DDataValue()];
    }

    public void setSideMode(Direction side, SideMode mode) {
        sideModes[side.get3DDataValue()] = mode;
        setChanged();
        // La capability mise en cache par NeoForge doit être réévaluée.
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    public void cycleSideMode(Direction side) {
        setSideMode(side, getSideMode(side).next());
    }

    public boolean isAutoInput() {
        return autoInput;
    }

    public boolean isAutoOutput() {
        return autoOutput;
    }

    public void setAutoInput(boolean enabled) {
        this.autoInput = enabled;
        setChanged();
    }

    public void setAutoOutput(boolean enabled) {
        this.autoOutput = enabled;
        setChanged();
    }

    public void toggleAutoInput() {
        setAutoInput(!autoInput);
    }

    public void toggleAutoOutput() {
        setAutoOutput(!autoOutput);
    }

    /**
     * Vue sidée exposée comme capability {@code ItemHandler} (voir {@code ModCapabilities}).
     * {@code null} = pas de capability sur cette face (machine sans I/O, ou face
     * désactivée). {@code side == null} (requête sans face) donne l'accès complet
     * entrée+sortie.
     */
    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        int[] in = getAutomationInputSlots();
        int[] out = getAutomationOutputSlots();
        if (in.length == 0 && out.length == 0) {
            return null;
        }
        if (side == null) {
            return new MachineItemHandler(inventory, in, out);
        }
        return switch (getSideMode(side)) {
            case DISABLED -> null;
            case INPUT -> new MachineItemHandler(inventory, in, NO_SLOTS);
            case OUTPUT -> new MachineItemHandler(inventory, NO_SLOTS, out);
        };
    }

    /** Auto-I/O throttlé : pousse la sortie / tire l'entrée selon les faces configurées. */
    public void tickAutomation() {
        if (!(level instanceof ServerLevel serverLevel)
            || serverLevel.getGameTime() % AUTO_INTERVAL != 0) {
            return;
        }
        if (autoOutput) {
            autoPush(serverLevel);
        }
        if (autoInput) {
            autoPull(serverLevel);
        }
    }

    private void autoPush(ServerLevel level) {
        int[] outSlots = getAutomationOutputSlots();
        if (outSlots.length == 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (getSideMode(dir) != SideMode.OUTPUT) {
                continue;
            }
            IItemHandler neighbour = level.getCapability(
                Capabilities.ItemHandler.BLOCK, worldPosition.relative(dir), dir.getOpposite());
            if (neighbour == null) {
                continue;
            }
            for (int slot : outSlots) {
                ItemStack inSlot = inventory.getStackInSlot(slot);
                if (inSlot.isEmpty()) {
                    continue;
                }
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(neighbour, inSlot.copy(), false);
                int moved = inSlot.getCount() - remainder.getCount();
                if (moved > 0) {
                    inventory.extractItem(slot, moved, false);
                }
            }
        }
    }

    private void autoPull(ServerLevel level) {
        int[] inSlots = getAutomationInputSlots();
        if (inSlots.length == 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (getSideMode(dir) != SideMode.INPUT) {
                continue;
            }
            IItemHandler neighbour = level.getCapability(
                Capabilities.ItemHandler.BLOCK, worldPosition.relative(dir), dir.getOpposite());
            if (neighbour == null) {
                continue;
            }
            if (pullFrom(neighbour, inSlots)) {
                return; // un transfert par tick suffit
            }
        }
    }

    /** Tente un transfert depuis {@code source} vers nos slots d'entrée. Vrai si un objet a bougé. */
    private boolean pullFrom(IItemHandler source, int[] inSlots) {
        for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
            ItemStack available = source.extractItem(sourceSlot, 64, true);
            if (available.isEmpty()) {
                continue;
            }
            int inserted = 0;
            ItemStack working = available.copy();
            for (int slot : inSlots) {
                if (working.isEmpty()) {
                    break;
                }
                if (!inventory.isItemValid(slot, working)) {
                    continue;
                }
                int before = working.getCount();
                working = inventory.insertItem(slot, working, false);
                inserted += before - working.getCount();
            }
            if (inserted > 0) {
                source.extractItem(sourceSlot, inserted, false);
                return true;
            }
        }
        return false;
    }

    // --- Inventaire ----------------------------------------------------------

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    /**
     * Filtre d'insertion par slot. La classe de base ne connait que le slot
     * d'augment ; les sous-classes completent pour leurs entrees et interdisent
     * l'insertion manuelle dans leur slot de sortie.
     */
    protected boolean isItemValid(int slot, ItemStack stack) {
        if (slot == augmentSlot) {
            return stack.is(ModTags.Items.MACHINE_AUGMENTS);
        }
        return true;
    }

    /** Vrai si {@code result} tient dans {@code slot}, vide ou deja entame. */
    protected boolean canInsertInto(int slot, ItemStack result) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (current.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(current, result)
            && current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    /**
     * Depose un resultat de cycle. Contourne volontairement
     * {@link ItemStackHandler#insertItem} : celui-ci passe par
     * {@link #isItemValid}, qui refuse toute insertion dans un slot de sortie.
     * A n'appeler qu'apres un {@link #canInsertInto} positif.
     */
    protected void insertInto(int slot, ItemStack result) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (current.isEmpty()) {
            inventory.setStackInSlot(slot, result.copy());
        } else {
            current.grow(result.getCount());
            inventory.setStackInSlot(slot, current);
        }
    }

    /** Vide l'inventaire au sol. Appele quand le bloc est casse. */
    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            container.setItem(slot, inventory.getStackInSlot(slot));
        }
        Containers.dropContents(level, pos, container);
    }

    // --- Persistance ---------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putBoolean("manualEnabled", manualEnabled);
        tag.putByte("redstoneMode", (byte) redstoneMode.ordinal());
        tag.putBoolean("overheatEnabled", overheatEnabled);

        byte[] modes = new byte[sideModes.length];
        for (int i = 0; i < sideModes.length; i++) {
            modes[i] = (byte) sideModes[i].ordinal();
        }
        tag.putByteArray("sideModes", modes);
        tag.putBoolean("autoInput", autoInput);
        tag.putBoolean("autoOutput", autoOutput);
        // -1 = machine universelle (aucune bande).
        tag.putByte("harmonicBand", (byte) (harmonicBand == null ? -1 : harmonicBand.ordinal()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        // Machine allumee par defaut pour un bloc pose avant l'ajout de ce champ.
        manualEnabled = !tag.contains("manualEnabled") || tag.getBoolean("manualEnabled");
        redstoneMode = RedstoneMode.byIndex(tag.getByte("redstoneMode"));
        overheatEnabled = tag.getBoolean("overheatEnabled");

        byte[] modes = tag.getByteArray("sideModes");
        if (modes.length == sideModes.length) {
            for (int i = 0; i < sideModes.length; i++) {
                sideModes[i] = SideMode.byIndex(modes[i]);
            }
        }
        autoInput = tag.getBoolean("autoInput");
        autoOutput = tag.getBoolean("autoOutput");
        byte band = tag.getByte("harmonicBand");
        harmonicBand = band < 0 ? null : HarmonicBand.byIndex(band);
    }
}
