package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Les noms en jeu restent en anglais (convention posee par
 * 13-Registry-Index.md : registry names et affichage en anglais, la prose de
 * conception en francais).
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, Veskorius.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.veskorius.main_tab", "Veskorius");

        // Boutons de contrôle des machines (voir MachineControlButton).
        add("message.veskorius.relay_charge", "Relay: %s/%s Osc");
        add("message.veskorius.slag_vent_status", "Vent: %s forge(s) cleared last pass");
        add("message.veskorius.amplifier_status", "Amplifier: range %s, link %s/%s, calibration %s%%");
        add("item.veskorius.resonance_tuner.recalibrated", "Recalibrated (1 Resonance Component used)");
        add("item.veskorius.resonance_tuner.no_component", "Recalibration needs 1 Resonance Component");
        add("gui.veskorius.tuner_calibrate", "Recalibrate");
        add("gui.veskorius.tuner_priority", "Set priority");
        add("gui.veskorius.priority_low", "Low");
        add("gui.veskorius.priority_normal", "Normal");
        add("gui.veskorius.priority_high", "High");
        add("message.veskorius.hub_status", "Hub: shedding below %s, calibration %s%%");
        add("message.veskorius.priority_set", "Priority: %s");
        add("message.veskorius.anchor_holding", "Anchor holding — the rift is stable");
        add("message.veskorius.ward_on", "Ward active — corrosion held back");
        add("message.veskorius.ward_off", "Ward idle — no field");
        add("item.veskorius.rift_essence.hint", "Six per rift, and the rift is spent. Nothing makes more.");
        add("message.veskorius.rift_not_cleared", "The rift still has a guardian. Nothing can be drawn from it.");
        add("message.veskorius.anchor_idle", "Anchor idle — no rift in reach, or no field");
        add("item.veskorius.deformed_stone.hint", "Stone twisted by a rift. The bubble is close — and it is not safe.");
        add("message.veskorius.core_formed", "Core online — field range %s");
        add("message.veskorius.core_incomplete", "Core idle — needs %s relays or amplifiers at %s blocks, each in direct view");
        add("item.veskorius.resonance_tuner.no_priority", "Only machines have a priority");
        add("gui.veskorius.machine_on", "Machine: On (click to turn off)");
        add("gui.veskorius.machine_off", "Machine: Off (click to turn on)");
        add("gui.veskorius.redstone_control", "Redstone control");
        add("gui.veskorius.redstone_ignored", "Ignored");
        add("gui.veskorius.redstone_requires_signal", "Requires signal");
        add("gui.veskorius.redstone_requires_no_signal", "Requires no signal");
        add("gui.veskorius.overheat_on", "Overheat: On (÷2 speed, ×2 Osc, 20% input loss)");
        add("gui.veskorius.overheat_off", "Overheat: Off");

        // Config item I/O par face (energy never uses this — items only).
        add("gui.veskorius.config", "Configure item sides");
        add("gui.veskorius.side.down", "Bottom");
        add("gui.veskorius.side.up", "Top");
        add("gui.veskorius.side.north", "North");
        add("gui.veskorius.side.south", "South");
        add("gui.veskorius.side.west", "West");
        add("gui.veskorius.side.east", "East");
        add("gui.veskorius.sidemode.disabled", "Disabled");
        add("gui.veskorius.sidemode.input", "Input");
        add("gui.veskorius.sidemode.output", "Output");
        add("gui.veskorius.auto_input", "Auto-pull items: %s");
        add("gui.veskorius.auto_output", "Auto-push output: %s");
        add("gui.veskorius.on", "On");
        add("gui.veskorius.off", "Off");

        // Field Emitter : jauge de réserve d'Osc.
        add("gui.veskorius.osc_reserve", "%s/%s Osc");

        addBlock(ModBlocks.RESONANCE_STABILIZER, "Resonance Stabilizer");
        addBlock(ModBlocks.COMPONENT_ASSEMBLER, "Component Assembler");
        addBlock(ModBlocks.FLUX_PURIFIER, "Flux Purifier");
        addBlock(ModBlocks.RESONANCE_WHETSTONE, "Resonance Whetstone");
        addBlock(ModBlocks.FIELD_EMITTER, "Field Emitter");
        addBlock(ModBlocks.TUNABLE_FIELD_EMITTER, "Tunable Field Emitter");
        addBlock(ModBlocks.CRYSTAL_CRUSHER, "Crystal Crusher");
        addBlock(ModBlocks.CRYSTAL_ROOST, "Crystal Roost");
        addBlock(ModBlocks.DAMPING_ARRAY, "Damping Array");
        addBlock(ModBlocks.VESKORIAN_ALLOY_FORGE, "Veskorian Alloy Forge");
        addBlock(ModBlocks.RESONANCE_RELAY, "Resonance Relay");
        addBlock(ModBlocks.FLUX_COMPRESSOR, "Flux Compressor");
        addBlock(ModBlocks.STRUCTURAL_SYNTHESIZER, "Structural Synthesizer");
        addBlock(ModBlocks.DEEP_CRYSTAL_DRILLER, "Deep Crystal Driller");
        addBlock(ModBlocks.SLAG_VENT, "Slag Vent");
        addBlock(ModBlocks.ARCHIVE_CONSOLE, "Archive Console");
        addBlock(ModBlocks.DEEP_SYNTHESIS_CHAMBER, "Deep Synthesis Chamber");
        addBlock(ModBlocks.HARMONIC_AMPLIFIER, "Harmonic Amplifier");
        addBlock(ModBlocks.AUTOMATED_EXTRACTION_ARRAY, "Automated Extraction Array");
        addBlock(ModBlocks.RESONANCE_NETWORK_HUB, "Resonance Network Hub");
        addBlock(ModBlocks.CONVERGENCE_CORE, "Convergence Core");
        addBlock(ModBlocks.RIFT_ANCHOR, "Rift Anchor");
        addBlock(ModBlocks.RIFT_CORE_EXTRACTOR, "Rift Core Extractor");
        addBlock(ModBlocks.RIFT_WARD_EMITTER, "Rift Ward Emitter");
        addBlock(ModBlocks.RIFT_CORE, "Rift Core");
        addBlock(ModBlocks.DEFORMED_STONE, "Deformed Stone");

        // Châssis de palier : la base de craft et de texture des machines.
        addBlock(ModBlocks.FRACTURED_CHASSIS, "Fractured Chassis");
        addBlock(ModBlocks.ATTUNED_CHASSIS, "Attuned Chassis");
        addBlock(ModBlocks.VESKORIAN_CHASSIS, "Veskorian Chassis");
        addBlock(ModBlocks.RESONANCE_CRYSTAL_CLUSTER, "Resonance Crystal Cluster");
        addBlock(ModBlocks.RESONANCE_VEINED_STONE, "Resonance Veined Stone");
        addBlock(ModBlocks.RAW_FLUX_DEPOSIT, "Raw Flux Deposit");
        addBlock(ModBlocks.ATTUNEMENT_CONSOLE, "Attunement Console");

        // Architecture de donjon (17-Dungeons.md §4)
        addBlock(ModBlocks.VEINED_STONE_BRICKS, "Veined Stone Bricks");
        addBlock(ModBlocks.CRACKED_VEINED_STONE_BRICKS, "Cracked Veined Stone Bricks");
        addBlock(ModBlocks.CHISELED_VEINED_STONE, "Chiseled Veined Stone");
        addBlock(ModBlocks.VEINED_STONE_BRICK_STAIRS, "Veined Stone Brick Stairs");
        addBlock(ModBlocks.VEINED_STONE_BRICK_SLAB, "Veined Stone Brick Slab");
        addBlock(ModBlocks.VEINED_STONE_BRICK_WALL, "Veined Stone Brick Wall");
        addBlock(ModBlocks.RESONANCE_LAMP, "Resonance Lamp");
        addBlock(ModBlocks.CONDUIT_LINE, "Resonance Conduit");
        addBlock(ModBlocks.VEINED_STONE_COLUMN, "Veined Stone Column");
        addBlock(ModBlocks.DISSONANCE_BLOOM, "Dissonance Bloom");
        addBlock(ModBlocks.RESONANCE_BULKHEAD, "Resonance Bulkhead");
        addBlock(ModBlocks.ANCIENT_EMITTER, "Ancient Emitter");
        addBlock(ModBlocks.SIGMA_CONSOLE, "Sigma Console");
        addBlock(ModBlocks.ARCHIVE_PEDESTAL, "Archive Pedestal");
        add("block.veskorius.archive_pedestal.solved", "The order holds — something wakes below");
        addBlock(ModBlocks.DAMAGED_RELAY, "Damaged Relay");
        add("block.veskorius.damaged_relay.restored", "The relay hums back to life");
        add("block.veskorius.damaged_relay.already", "This relay is already running");
        add("block.veskorius.damaged_relay.no_field", "A relay rebroadcasts — it needs a field to carry");
        add("block.veskorius.attunement_console.restored", "The console wakes — blueprint restored");
        add("block.veskorius.attunement_console.already", "You already carry this blueprint");

        addItem(ModItems.RAW_RESONANCE_CRYSTAL, "Raw Resonance Crystal");
        addItem(ModItems.STABLE_RESONANCE_CRYSTAL, "Stable Resonance Crystal");
        addItem(ModItems.REFINED_RESONANCE_CRYSTAL, "Refined Resonance Crystal");
        addItem(ModItems.RESONANCE_COMPONENT, "Resonance Component");
        addItem(ModItems.RESONANCE_DUST, "Resonance Dust");

        // Onboarding hints (ItemHintHandler): teach the T1 loop from the item alone.
        add("item.veskorius.raw_resonance_crystal.hint", "Unstable. Stabilize it with quartz in a Resonance Stabilizer.");
        add("item.veskorius.stable_resonance_crystal.hint", "Fuels a Field Emitter, or refine it in a Flux Purifier.");
        add("item.veskorius.resonance_component.hint", "Core part of Tier 2 machines and portable batteries.");
        add("item.veskorius.resonance_dust.hint", "A quick crush of raw crystal. Feeds the Component Assembler.");
        addItem(ModItems.VESKORIAN_ALLOY_INGOT, "Veskorian Alloy Ingot");
        addItem(ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT, "Veskorian Conductive Alloy Ingot");
        addItem(ModItems.FLUX_SLAG, "Flux Slag");
        addItem(ModItems.SYNTHESIS_RESIDUE, "Synthesis Residue");
        addItem(ModItems.CONCENTRATED_FLUX, "Concentrated Flux");
        addItem(ModItems.HYPER_REFINED_CRYSTAL, "Hyper Refined Crystal");
        addItem(ModItems.HARMONIC_LATTICE, "Harmonic Lattice");
        addItem(ModItems.RIFT_ESSENCE, "Rift Essence");
        addItem(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT, "Corrupted Veskorian Alloy Ingot");
        addBlock(ModBlocks.VESKORIAN_ALLOY_BLOCK, "Block of Veskorian Alloy");
        add("item.veskorius.veskorian_alloy_ingot.hint", "Structural metal of Tier 3. Forge gold instead of iron for the conductive kind.");
        add("item.veskorius.flux_slag.hint", "Forge waste. The same residue that, region-wide, caused the Collapse.");
        addItem(ModItems.RAW_FLUX_DEPOSIT, "Raw Flux Deposit");
        addItem(ModItems.RESONANCE_CATALYST_CORE, "Resonance Catalyst Core");
        addItem(ModItems.RESONANCE_TUNER, "Resonance Tuner");
        addItem(ModItems.RESONANCE_STORAGE_CELL, "Resonance Storage Cell");
        add("item.veskorius.resonance_storage_cell.charge", "%s / %s Osc");
        addItem(ModItems.RESONANCE_LOCATOR, "Resonance Locator");
        add("item.veskorius.resonance_locator.charge", "%s / %s Osc");
        add("gui.veskorius.locator.empty", "The locator has no charge");
        add("gui.veskorius.locator.none", "No resonance within range");
        add("gui.veskorius.locator.found", "%s to the %s (%s blocks)");
        add("gui.veskorius.locator.type_crystal", "Crystal resonance");
        add("gui.veskorius.locator.type_field", "Field signature");
        add("gui.veskorius.locator.type_structure", "Structure signature");
        add("gui.veskorius.locator.no_structure", "No structure signature in range");
        add("gui.veskorius.locator.mode_resources", "Resources");
        add("gui.veskorius.locator.mode_structures", "Structures");
        add("item.veskorius.resonance_locator.mode", "Mode: %s");
        add("item.veskorius.resonance_locator.mode_hint", "Shift + right-click to change mode");
        add("gui.veskorius.dir.n", "north");
        add("gui.veskorius.dir.ne", "north-east");
        add("gui.veskorius.dir.e", "east");
        add("gui.veskorius.dir.se", "south-east");
        add("gui.veskorius.dir.s", "south");
        add("gui.veskorius.dir.sw", "south-west");
        add("gui.veskorius.dir.w", "west");
        add("gui.veskorius.dir.nw", "north-west");
        addItem(ModItems.RESONANCE_SPORE, "Resonance Spore");
        addItem(ModItems.RESONANCE_SLUDGE, "Resonance Sludge");
        addItem(ModItems.CRYSTAL_STRIDER_SPAWN_EGG, "Crystal Strider Spawn Egg");

        addItem(ModItems.CUSTODE_ALLOY_FRAGMENT, "Custode Alloy Fragment");
        addItem(ModItems.CUSTODE_SPAWN_EGG, "Custode Spawn Egg");

        // Entités (09-Entities.md).
        add("entity.veskorius.crystal_strider", "Crystal Strider");
        add("entity.veskorius.custode", "Custode");
        add("entity.veskorius.rift_guardian", "Rift Guardian");
        add("gui.veskorius.strider.milk_cooldown", "The strider needs %s more seconds");

        // Progression : plans, fragments, ration (tâche 10).
        addItem(ModItems.RESONANCE_BLUEPRINT, "Resonance Blueprint");
        addItem(ModItems.CODEX_FRAGMENT, "Codex Fragment");
        addItem(ModItems.FOSSILIZED_RATION, "Fossilized Ration");
        add("item.veskorius.resonance_blueprint.tier", "Restored blueprint — Tier %s");
        add("item.veskorius.resonance_blueprint.hint", "Kept when crafting. Restore machines of this tier.");
        add("item.veskorius.codex_fragment.hint", "Right-click to read");

        // --- Codex : entrées T3, T4 et T5 -------------------------------
        // Le Codex s'arrêtait au T2. Un texte utile répond à trois questions et pas
        // une de plus : à quoi ça sert, ce qui ne se devine pas, et quoi faire ensuite.
        // Les chiffres y sont explicites — « assez longtemps » envoie le joueur mesurer.
        add("codex.veskorius.machines.forge.title", "Veskorian Alloy Forge");
        add("codex.veskorius.machines.forge.text",
            "Two Refined Crystals and two ingots become one alloy ingot in 20 seconds. The metal you feed it decides the branch: IRON gives structural alloy, GOLD gives the conductive kind — and only the conductive one goes into a Relay. They are not interchangeable.\n\nEvery cycle also drops one Flux Slag into its own slot, whatever you forged. When that slot fills, the Forge stops. Empty it by hand, or let a Slag Vent do it.");
        add("codex.veskorius.machines.relay.title", "Resonance Relay");
        add("codex.veskorius.machines.relay.text",
            "Carries a field 20 blocks further, and relays chain into one another. It draws 1 Osc per tick whether anything uses it or not — range is never free.\n\nA relay holds no charge of its own worth speaking of: it fills from the field upstream and serves what it holds. It also carries the harmonic band of its source, so slipping one in front of a machine will not fix a detuned setup.");
        add("codex.veskorius.machines.synthesizer.title", "Structural Synthesizer");
        add("codex.veskorius.machines.synthesizer.text",
            "Four alloy ingots and eight stone become four Veskorian Alloy Blocks in 60 seconds. This is what makes the tier buildable — without it the alloy stays a crafting material.\n\nLike the Forge, it leaves a by-product: one Synthesis Residue per cycle, in its own slot, and a full slot stops the machine. A Slag Vent will NOT clear it. That one is still your problem.");
        add("codex.veskorius.machines.driller.title", "Deep Crystal Driller");
        add("codex.veskorius.machines.driller.text",
            "Harvests crystal clusters from the 5x5 column beneath it, one every 20 seconds, at 6 Osc per tick. It only reaches below Y -40 — placing one higher does nothing at all.\n\nIt takes the clusters and leaves the rock: the gallery stays intact and you can see exactly what was taken. It also exhausts its vein. When the column is empty it stops, and you move it.");
        add("codex.veskorius.machines.slag_vent.title", "Slag Vent");
        add("codex.veskorius.machines.slag_vent.text",
            "Clears one Flux Slag every 10 seconds from every Forge within 8 blocks. It exists because a full slag slot stops a Forge dead.\n\nOne clear per pass per Forge: a battery of six will outrun a single Vent. It costs field continuously, so disposing of waste is a standing line in your energy budget, not a button you press once.");
        add("codex.veskorius.machines.compressor.title", "Flux Compressor");
        add("codex.veskorius.machines.compressor.text",
            "Four Refined Crystals become one Concentrated Flux in 30 seconds. An apparent loss, and a deliberate one.\n\nConcentrated Flux has exactly one consumer in the whole mod: the Convergence Core. Build a Compressor when you have decided to build that, not before.");
        add("codex.veskorius.crystals.alloy.title", "Veskorian Alloy");
        add("codex.veskorius.crystals.alloy.text",
            "The first material you MAKE rather than refine. The T1-T2 chain started from crystal and purified it; this one starts from metal and alloys it. That change of nature is what marks the tier, not the number of steps.\n\nThe conductive variant is the same forge with gold instead of iron. Keep them apart — the Relay and the Harmonic Lattice accept only the conductive one.");
        add("codex.veskorius.crystals.slag.title", "Flux Slag");
        add("codex.veskorius.crystals.slag.text",
            "The Forge's waste, and not an inert one. Chemically it is the same substance that, at regional scale, drove the Collapse.\n\nYou are reproducing the cause in miniature, and it will stop your Forge if you ignore it. That is the intended lesson.");
        add("codex.veskorius.machines.chamber.title", "Deep Synthesis Chamber");
        add("codex.veskorius.machines.chamber.text",
            "Two Refined Crystals become one Hyper Refined Crystal in 90 seconds, at 8 Osc per tick. Nothing else in the world makes them.\n\nBuilding it CONSUMES one Hyper Refined Crystal, which becomes its permanent catalyst and never appears as a cycle input again. The Regional Archive gives you exactly three: two go into your first Harmonic Lattice, the third goes here. You cannot do both at once — that choice is the tier.\n\nIt accepts overheat: half the time, double the Osc, and one cycle in five loses its input.");
        add("codex.veskorius.machines.amplifier.title", "Harmonic Amplifier");
        add("codex.veskorius.machines.amplifier.text",
            "Doubles the range of the field it RECEIVES, for 2 Osc per tick. Behind a T2 emitter that is 16 blocks; behind a Relay it is 40. A Relay carries a fixed range, this multiplies one.\n\nThree links at most. Past the third, an amplifier still carries the field but stops doubling it.\n\nIt drifts: 1% efficiency lost per Minecraft day of running, down to a floor of -30%. Right-click it with a Resonance Tuner in Recalibrate mode and one Resonance Component to reset it. The drift only eats the GAIN, never the range it received — an amplifier is never worse than no amplifier.");
        add("codex.veskorius.machines.hub.title", "Resonance Network Hub");
        add("codex.veskorius.machines.hub.text",
            "Decides who stops when a field runs short. Without one, machines are served in whatever order they happen to tick, and an underfed base stutters everywhere at once with nothing telling you what you lost.\n\nThe Hub sheds from the bottom: above half reserve everyone runs, between a fifth and a half the LOW priority machines go quiet, below that only HIGH is served. Set any machine's priority with the Tuner.\n\nIt is passive and costs nothing — taxing an arbiter at the exact moment there is no energy left would be absurd.");
        add("codex.veskorius.machines.extraction_array.title", "Automated Extraction Array");
        add("codex.veskorius.machines.extraction_array.text",
            "Commands every Deep Crystal Driller within 12 blocks: it empties their output into its own chest, and the drillers it commands run TWICE as fast.\n\nIt answers the chore the Driller creates. With five drillers at the bottom of a mine, walking to each one is most of what the tier asks of you. 10 Osc per tick to stop doing that.");
        add("codex.veskorius.machines.convergence_core.title", "Convergence Core");
        add("codex.veskorius.machines.convergence_core.text",
            "The only multi-block in the mod, and the only field source stronger than any other.\n\nPlaced alone it is inert. It needs EIGHT Resonance Relays or Harmonic Amplifiers (mixing is fine) at exactly 5 blocks — the four axes and the four corners of an 11-block ring — and each one must have a clear line of sight to the centre. You cannot box it in.\n\nOnce formed it emits range 40 at maximum intensity for 12 Osc per tick. It is checked every two seconds: wall off one sightline and it goes dark.\n\nIt exists mainly to feed a Rift Anchor without dedicating a whole base of relays to it.");
        add("codex.veskorius.crystals.hyper.title", "Hyper Refined Crystal");
        add("codex.veskorius.crystals.hyper.text",
            "The fourth state of the crystal. It cannot be mined, found or crafted — the Deep Synthesis Chamber is its only source, and building that Chamber spends one.\n\nThe three from the Regional Archive are the tier's entire starting stock. Spend them knowing that.");
        add("codex.veskorius.fields.lattice.title", "Harmonic Lattice");
        add("codex.veskorius.fields.lattice.text",
            "Four CONDUCTIVE alloy ingots and two Hyper Refined Crystals. The metal branch you picked at the Forge is charged for a second time, one tier later.\n\nOnly two things ever want a Lattice: the Harmonic Amplifier and the Convergence Core. It is the T4 network in one item.");
        add("codex.veskorius.fields.calibration.title", "Calibration and drift");
        add("codex.veskorius.fields.calibration.text",
            "Amplifiers and Hubs lose 1% efficiency per Minecraft day of running, down to -30% and no further. A neglected amplifier carries less far than it did; a neglected Hub sheds earlier than it should.\n\nThe cure is the same gesture as everything else that drifts in this world: a Resonance Tuner in Recalibrate mode, at the cost of one Resonance Component.\n\nThe floor matters. Nothing here ever stops working outright — maintenance, never a wall.");
        add("codex.veskorius.world.rift.title", "Reading a rift");
        add("codex.veskorius.world.rift.text",
            "A rift is a bubble of nothing, torn below Y -60, with a core floating at its centre. It is not built and it is not a structure — it is an accident of over-resonance.\n\nThe Resonance Locator cannot find one: a rift does not radiate a field, it phases one. The only sign is the stone around it, pulled and cracked. Learn that stone and you can find rifts. There is no instrument for this.\n\nUnanchored, a core hurts anything within 8 blocks after three seconds. Three seconds is enough to look and step back. It is not enough to stay.");
        add("codex.veskorius.machines.rift_anchor.title", "Rift Anchor");
        add("codex.veskorius.machines.rift_anchor.text",
            "Placed within 12 blocks of a rift core, it stabilises the rift — and summons its Guardian, once, the first time it holds.\n\n20 Osc per tick, continuously: the heaviest appetite in the mod, and the reason the Convergence Core exists.\n\nIt holds only WHILE it is fed. Cut the field and the rift wakes. Break the anchor and it wakes immediately. There is no permanent switch here.");
        add("codex.veskorius.machines.rift_extractor.title", "Rift Core Extractor");
        add("codex.veskorius.machines.rift_extractor.text",
            "One Rift Essence every 120 seconds, at 15 Osc per tick, from a rift that is anchored AND cleared of its Guardian. Roughly one extraction in seven also yields a Corrupted Alloy Ingot.\n\nSix extractions per rift. Then that rift is spent, forever. The count lives on the core, so replacing the Extractor changes nothing.\n\nRift Essence is the only finite resource in this mod. Nothing regenerates it.");
        add("codex.veskorius.machines.rift_ward.title", "Rift Ward Emitter");
        add("codex.veskorius.machines.rift_ward.text",
            "Holds back the ambient corrosion of an anchored rift within 12 blocks, for 5 Osc per tick.\n\nAnchoring a rift stops the acute damage; it does not stop the rift. What remains eats your equipment — one point of wear on a worn piece every 5 seconds. Survivable, and unworkable: you can enter without a Ward, you cannot operate there.\n\nIt costs two Rift Essence out of the six a rift will ever give. That is the only item in the mod paid for out of what it unlocks.");
        add("codex.veskorius.fauna.guardian.title", "Rift Guardian");
        add("codex.veskorius.fauna.guardian.text",
            "Never a random encounter: it rises from the first Rift Anchor that holds, once per rift, and never again.\n\nThree phases, and each one asks something different. In ECHO it backs away as you close — you have to chase it. In RUPTURE it opens the floor beneath you. In STABILISATION it walks to the core and heals if it reaches it, so you must hold the centre, where the rift is worst.\n\nBeating it stabilises the rift permanently — the phase damage stops even with the anchor switched off — and opens extraction. Guaranteed drop: three Corrupted Veskorian Alloy Ingots.");
        add("codex.veskorius.crystals.essence.title", "Rift Essence");
        add("codex.veskorius.crystals.essence.text",
            "Six per rift, and the rift is finished. There is no machine that makes more, and there will not be one — that was decided and it is not an oversight.\n\nWhat you do with them is the last real choice the mod gives you.");
        add("codex.veskorius.progression.tier3.title", "Tier 3 — Alloy");
        add("codex.veskorius.progression.tier3.text",
            "The tier where you stop refining and start MAKING. The Veskorian Alloy Forge is the door, and the Sigma Laboratory console holds its blueprint.\n\nTwo things are new and both are constraints. The metal you forge splits your materials into two families that do not substitute for each other. And every machine that makes the tier's material also makes waste that will stop it if you ignore it.");
        add("codex.veskorius.progression.tier4.title", "Tier 4 — Deep synthesis");
        add("codex.veskorius.progression.tier4.text",
            "Opened by the Regional Archive, which gives its blueprint and exactly three Hyper Refined Crystals.\n\nThis is where the network stops being a set of machines and becomes something you administer: amplifiers that reach across a region, a Hub that decides who eats when there is not enough, and calibration that decays if you never come back.");
        add("codex.veskorius.progression.tier5.title", "Tier 5 — The rift");
        add("codex.veskorius.progression.tier5.text",
            "Not unlocked by crafting anything. You unlock it by FINDING a rift, and you find one by recognising deformed stone.\n\nEverything here is finite. One Guardian per rift, six essences per rift, and no way to make more of either. The mod ends where the resource does.");

        // Entrées de Codex (lore). Clés : codex.<ns>.<path>.title/.text.
        add("codex.veskorius.daily_life.lamps.title", "Household note — the light");
        add("codex.veskorius.daily_life.lamps.text",
            "They gave us light with no wire, no flame. The elders said to \"stay in the field\". "
                + "At the edge of the village, the lamps grew faint.");
        add("codex.veskorius.daily_life.ration.title", "Ledger — dry cycle, ration 14");
        add("codex.veskorius.daily_life.ration.text",
            "The grain holds, so does the crystal. As long as the quarter's Tower sings, we want for nothing.");
        add("codex.veskorius.hint.workshop.title", "Household note — the workshop below");
        add("codex.veskorius.hint.workshop.text",
            "The workshop downstairs still held when we left. Its console answered anyone who knew how to wake it.");
        add("codex.veskorius.daily_life.market.title", "Ledger — market day");
        add("codex.veskorius.daily_life.market.text",
            "Traded two vials of stable light for the potter's whole shelf. The field reached the far "
                + "stalls today; even old Merek kept his lamps lit past dusk. Good omens, the crier said.");
        add("codex.veskorius.daily_life.children.title", "Slate — a child's hand");
        add("codex.veskorius.daily_life.children.text",
            "Mother says do not touch the humming stones. I touched one anyway. It was warm, and it sang "
                + "back. I did not tell her.");
        add("codex.veskorius.daily_life.festival.title", "Notice — the Attunement");
        add("codex.veskorius.daily_life.festival.text",
            "Third bell, the quarter gathers. The Architects will wake the great tower and every lamp will "
                + "flare at once. Bring nothing metal that you value; the old ones say it pulls.");

        // L'Archive : quatre cotes. Elles portent leur RANG dans leur texte — c'est la
        // seule indication de l'ordre, et l'ordre est la serrure de la salle de lecture.
        add("codex.veskorius.archive.log_1.title", "Shelf mark I — the measure");
        add("codex.veskorius.archive.log_1.text",
            "First of four. We are asked to record, not to conclude. So: across the eleven "
            + "sectors, the field no longer returns what we put in. It returns more. "
            + "The excess is small. The excess is everywhere.");
        add("codex.veskorius.archive.log_2.title", "Shelf mark II — the sum");
        add("codex.veskorius.archive.log_2.text",
            "Second of four. Two fields laid over one another do not add — we have taught "
            + "this to every apprentice for two hundred years. At the scale of a region, "
            + "they do something else. We have no word for it yet. We should have made one.");
        add("codex.veskorius.archive.log_3.title", "Shelf mark III — the answer");
        add("codex.veskorius.archive.log_3.text",
            "Third of four. The Assembly has read the measure and voted to extend the grid. "
            + "Their reasoning is sound and rests entirely on the assumption we came here to "
            + "question. I am to file this and say nothing further.");
        add("codex.veskorius.archive.log_4.title", "Shelf mark IV — the tear");
        add("codex.veskorius.archive.log_4.text",
            "Fourth of four. Something opened in the deep line last night and did not close. "
            + "It is not a hole in the rock. The rock is still there. It is a hole in the "
            + "place where the rock is. Whoever reads these, read them in order. It matters.");
        add("codex.veskorius.outpost.log_1.title", "Operator's log — first quarter");
        add("codex.veskorius.outpost.log_1.text",
            "Console reads clean on all three bands. Sector hums the way it should. "
            + "Marran says the deep line is drifting again; I logged it. Third time this season. "
            + "They will tell us it is within tolerance. It is within tolerance.");
        add("codex.veskorius.outpost.log_2.title", "Operator's log — the drift");
        add("codex.veskorius.outpost.log_2.text",
            "It is not within tolerance. The band will not hold where I set it. I re-tune at "
            + "dawn and by dusk it has wandered, always the same way, always toward the others. "
            + "I have asked for a second opinion. The Archive answers that the network is "
            + "self-correcting. The network is correcting toward something.");
        add("codex.veskorius.outpost.log_3.title", "Operator's log — the night it sang");
        add("codex.veskorius.outpost.log_3.text",
            "Every emitter from here to the coast fell into the same band at once. No one "
            + "ordered it. For one night the whole network rang like a struck bell and the "
            + "lamps burned white. It was, and I will write this plainly, beautiful. "
            + "In the morning the stone above the west gallery had cracked end to end.");
        add("codex.veskorius.outpost.log_4.title", "Operator's log — last entry");
        add("codex.veskorius.outpost.log_4.text",
            "Orders came to shut the sector down. I could not. Shutting down requires the "
            + "network to agree, and it no longer answers requests — only the tone. "
            + "I am leaving the console live and the seals open. Whoever reads this: it is "
            + "still listening. That is not a warning. It is the only reason you can start it again.");
        add("codex.veskorius.custode.watch.title", "Custode — standing order");
        add("codex.veskorius.custode.watch.text",
            "I do not sleep. I do not hunt. I hold this door until the makers return. If you take from the "
                + "dead, do not be surprised that the dead answer.");

        // Advancements (feedback, tâche 10).
        add("advancements.veskorius.tier1_awakening.title", "The Awakening");
        add("advancements.veskorius.tier1_awakening.description", "Pick up a Raw Resonance Crystal");
        add("advancements.veskorius.find_dwelling.title", "Someone Lived Here");
        add("advancements.veskorius.find_dwelling.description",
            "Step inside a Modest Dwelling. The furniture is still in place — they left in a hurry.");
        add("advancements.veskorius.find_outpost.title", "The Dead Machine");
        add("advancements.veskorius.find_outpost.description",
            "Reach an Outpost. Something in the rubble is still listening.");
        add("advancements.veskorius.first_chassis.title", "Salvaged Frame");
        add("advancements.veskorius.first_chassis.description",
            "Build a Fractured Chassis. Every machine you will ever make starts as one of these.");
        add("advancements.veskorius.first_field.title", "No Wires");
        add("advancements.veskorius.first_field.description",
            "Place a Field Emitter. Power now travels through the air, and only so far.");
        add("advancements.veskorius.first_strider.title", "Something Still Grows");
        add("advancements.veskorius.first_strider.description",
            "Harvest a Resonance Spore. The Crystal Striders will follow it anywhere.");
        add("advancements.veskorius.tier2_field.title", "Short Network");
        add("advancements.veskorius.tier2_field.description", "Restore the field blueprint at an Outpost console");

        // Resonance Tuner
        add("gui.veskorius.tuner_rotate", "Rotate Machine");
        add("gui.veskorius.tuner_power", "Toggle Power");
        add("gui.veskorius.tuner_overheat", "Toggle Overheat");
        add("gui.veskorius.tuner_redstone", "Cycle Redstone Mode");
        add("gui.veskorius.tuner_attune", "Attune Harmonic Band");

        // Harmoniques : les bandes sont des COULEURS côté joueur (12-UX).
        add("gui.veskorius.band.fundamental", "Fundamental (violet)");
        add("gui.veskorius.band.median", "Median (cyan)");
        add("gui.veskorius.band.high", "High (amber)");
        add("gui.veskorius.band.universal", "Universal (any band)");
        add("item.veskorius.resonance_tuner.attuned", "Attuned to: %s");
        add("item.veskorius.resonance_tuner.no_band", "This machine has no harmonic band");

        // HUD de champ (12-UX) : visible en portant le Locator (inventaire ou Curios).
        add("gui.veskorius.hud.osc", "%s / %s Osc");
        add("gui.veskorius.hud.dissonance", "Dissonance");
        add("gui.veskorius.hud.unstable", "Unstable field");
        add("item.veskorius.resonance_locator.hud_hint",
            "Carry it to read the field you stand in");

        // Décharge de résonance (06 A6) : message de mort de lore.
        add("death.attack.veskorius.resonance_discharge",
            "%1$s was torn apart by a resonance discharge");
        add("death.attack.veskorius.resonance_discharge.player",
            "%1$s was torn apart by a resonance discharge while fighting %2$s");

        add("item.veskorius.resonance_tuner.current_mode", "Current Mode");
        add("item.veskorius.resonance_tuner.available_modes", "Available Modes");
        add("item.veskorius.resonance_tuner.controls", "Controls");
        add("item.veskorius.resonance_tuner.ctrl_apply", "Right-click a machine: apply mode");
        add("item.veskorius.resonance_tuner.ctrl_cycle", "Right-click in air: change mode");
        add("item.veskorius.resonance_tuner.ctrl_dismantle", "Shift + right-click: dismantle block");
        add("tooltip.veskorius.hold_shift", "Hold Shift for controls");

        add("item.veskorius.resonance_tuner.mode", "Mode: %s");
        add("item.veskorius.resonance_tuner.rotated", "Machine rotated");
        add("item.veskorius.resonance_tuner.no_overheat", "This machine does not support overheat");
        add("item.veskorius.resonance_tuner.dismantled", "Dismantled");

        // --- Codex de Résonance (15-Codex-Guidebook.md) ---
        addItem(ModItems.RESONANCE_CODEX, "Resonance Codex");
        add("item.veskorius.resonance_codex.hint", "Right-click to open. It writes itself as you progress.");
        add("gui.veskorius.codex.search", "Search…");
        add("gui.veskorius.codex.tree", "Progression");
        add("gui.veskorius.codex.tier_intro", "Start");
        add("gui.veskorius.codex.next", "Next: %s →");
        add("gui.veskorius.codex.toast", "New Codex entry");
        add("gui.veskorius.codex.results", "%s result(s)");
        add("gui.veskorius.codex.no_results", "Nothing here yet.");
        add("gui.veskorius.codex.sealed", "Sealed entry");
        add("gui.veskorius.codex.recipe", "Recipe");
        add("gui.veskorius.codex.recipe_shaped", "Recipe (shaped)");
        add("gui.veskorius.codex.machine_note", "%ss · %s Osc/tick");
        add("gui.veskorius.codex.title", "Resonance Codex");
        add("gui.veskorius.codex.discovered", "%s / %s discovered");
        add("gui.veskorius.codex.total", "%s / %s known");
        add("gui.veskorius.codex.back", "‹ Back");
        add("gui.veskorius.codex.new_entry", "New Codex entry: %s");
        add("gui.veskorius.codex.locked", "Locked.");
        add("gui.veskorius.codex.locked_item", "Locked — obtain: %s");
        add("gui.veskorius.codex.locked_advancement", "Locked — progress further to reveal this page.");
        add("gui.veskorius.codex.locked_fragment", "Locked — read the matching Codex Fragment to reveal this page.");

        add("codex.category.intro", "Introduction");
        add("codex.category.crystals", "Crystals & Refining");
        add("codex.category.fields", "Fields & Energy");
        add("codex.category.machines", "Machines");
        add("codex.category.world", "World & Structures");
        add("codex.category.fauna", "Fauna");
        add("codex.category.lore", "Lore");
        add("codex.category.progression", "Progression");

        add("codex.veskorius.intro.welcome.title", "The Resonance Codex");
        add("codex.veskorius.intro.welcome.text", "Your own restored knowledge. It writes itself as you rebuild what the Collapse undid — new pages appear as you discover crystals, machines, places and creatures.");
        add("codex.veskorius.intro.using_codex.title", "Reading the Codex");
        add("codex.veskorius.intro.using_codex.text", "Pick a category on the left, then an entry. Locked pages show as ??? — open one anyway to learn how to unlock it. The Codex is yours: it fills in even when it sits in a chest.");
        add("codex.veskorius.intro.getting_started.title", "Getting Started");
        add("codex.veskorius.intro.getting_started.text", "1) Mine a Raw Resonance Crystal underground. 2) Build a Resonance Stabilizer and turn it into Stable Crystal with quartz. 3) Build a Component Assembler. 4) Find an Outpost, wake its console for the Field blueprint, and raise your first Field Emitter.");

        add("codex.veskorius.crystals.raw.title", "Raw Resonance Crystal");
        add("codex.veskorius.crystals.raw.text", "Mined from crystal pockets underground. Unstable alone — stabilize it with quartz in a Resonance Stabilizer, or crush it into dust.");
        add("codex.veskorius.crystals.stable.title", "Stable Resonance Crystal");
        add("codex.veskorius.crystals.stable.text", "The workhorse of the early game: fuel for Field Emitters, input for Components, and the crystal you refine further.");
        add("codex.veskorius.crystals.refined.title", "Refined Resonance Crystal");
        add("codex.veskorius.crystals.refined.text", "Purified in a Flux Purifier. A denser, cleaner charge used by higher-tier recipes and the Catalyst Core.");
        add("codex.veskorius.crystals.dust.title", "Resonance Dust");
        add("codex.veskorius.crystals.dust.text", "A quick crush of raw crystal — faster than stabilizing, but yields no Stable Crystal. Feeds the Assembler's alternative recipe.");
        add("codex.veskorius.crystals.pockets.title", "Crystal Pockets");
        add("codex.veskorius.crystals.pockets.text", "Crystals grow in small pockets deep underground, wrapped in a shell of Resonance Veined Stone. Spot the veined stone and a pocket is close. Some pocket walls carry a brushable flux crust.");

        add("codex.veskorius.fields.osc.title", "Osc & Fields");
        add("codex.veskorius.fields.osc.text", "Osc is resonance energy. There are no cables: an Emitter fills a field around it, and any machine standing inside draws what it needs. A running machine glows; one that stays dark inside a field has no power to draw.");
        add("codex.veskorius.fields.emitter.title", "Field Emitter");
        add("codex.veskorius.fields.emitter.text", "Projects a resonance field (range 8) by burning Stable Crystals. Machines inside it draw power — no cables. Its dome of particles shows the reach.");
        add("codex.veskorius.fields.storage_cell.title", "Resonance Storage Cell");
        add("codex.veskorius.fields.storage_cell.text", "A portable battery. Charges inside a field, up to 8000 Osc, and powers the Locator away from home.");
        add("codex.veskorius.fields.locator.title", "Resonance Locator");
        add("codex.veskorius.fields.locator.text", "Points to the nearest resonance — a crystal pocket or an active field. Short range, on a small charge topped up in a field or from a Storage Cell.");

        add("codex.veskorius.machines.stabilizer.title", "Resonance Stabilizer");
        add("codex.veskorius.machines.stabilizer.text", "Turns Raw Crystal + quartz into Stable Crystal. Self-powered, no field needed — the first machine you build.");
        add("codex.veskorius.machines.assembler.title", "Component Assembler");
        add("codex.veskorius.machines.assembler.text", "Combines a Stable Crystal (or dust) with iron into Resonance Components. The first machine that draws on a field.");
        add("codex.veskorius.machines.whetstone.title", "Resonance Whetstone");
        add("codex.veskorius.machines.whetstone.text", "Repairs a damaged tool by a quarter of its durability, spending one Stable Crystal. Self-powered.");
        add("codex.veskorius.machines.purifier.title", "Flux Purifier");
        add("codex.veskorius.machines.purifier.text", "Refines Stable into Refined Crystal. Optional overheat: twice the speed for twice the Osc, and a 20% chance to lose the input.");
        add("codex.veskorius.machines.crusher.title", "Crystal Crusher");
        add("codex.veskorius.machines.crusher.text", "Grinds one Raw Crystal into three Resonance Dust in ten seconds. Self-powered — a fast T1 branch.");
        add("codex.veskorius.machines.roost.title", "Crystal Roost");
        add("codex.veskorius.machines.roost.text", "Passive production: fed quartz, it yields Raw Crystal over time — but only while a Crystal Strider stays nearby.");
        add("codex.veskorius.machines.tuner.title", "Resonance Tuner");
        add("codex.veskorius.machines.tuner.text", "A mode tool: rotate, toggle power, overheat, or cycle redstone on a machine. Shift + right-click dismantles any block entity, keeping its contents.");
        add("codex.veskorius.machines.catalyst_core.title", "Resonance Catalyst Core");
        add("codex.veskorius.machines.catalyst_core.text", "Slots into any active machine's augment slot for a permanent +15% speed. One per machine, never consumed.");
        add("codex.veskorius.machines.control.title", "Controlling Machines");
        add("codex.veskorius.machines.control.text", "Every active machine has three buttons in its GUI: a manual switch, a redstone mode (ignored / needs signal / needs no signal), and — where supported — overheat. Cutting power or redstone pauses a machine and keeps its progress; only a missing ingredient resets it. The Resonance Tuner applies these same toggles in the world.");

        add("codex.veskorius.world.veined_stone.title", "Resonance Veined Stone");
        add("codex.veskorius.world.veined_stone.text", "The shell around crystal pockets — see it, and a pocket is near. In dim light it can grow a Resonance Spore on an exposed face, harvested by hand.");
        add("codex.veskorius.world.flux_deposit.title", "Raw Flux Deposit");
        add("codex.veskorius.world.flux_deposit.text", "A brushable crust on pocket walls. Brush it to collect the flux; mining destroys it. An alternative to quartz for the Stabilizer.");
        add("codex.veskorius.world.outpost.title", "The Outpost");
        add("codex.veskorius.world.outpost.text", "A buried ruin marked by a stub of veined stone at the surface. Its Attunement Console, once woken, restores the Tier 2 blueprint — and a Custode guards the site.");

        add("codex.veskorius.fauna.strider.title", "Crystal Strider");
        add("codex.veskorius.fauna.strider.text", "Neutral underground fauna. Right-click bare-handed to milk a Raw Crystal (with a cooldown), and breed them with Resonance Spore.");
        add("codex.veskorius.fauna.custode.title", "Custode");
        add("codex.veskorius.fauna.custode.text", "A reactive guardian posted at Outposts. It strikes only within a few blocks, or if you break a machine on its site. Drops alloy fragments — an iron substitute.");

        add("codex.veskorius.progression.tier1.title", "Tier 1 — Awakening");
        add("codex.veskorius.progression.tier1.text", "Mine a crystal, stabilize it, assemble components. Everything here is self-powered or hand-made. The Codex opens with your first crystal.");
        add("codex.veskorius.progression.tier2.title", "Tier 2 — The Field");
        add("codex.veskorius.progression.tier2.text", "Wake an Outpost console for the Field blueprint, then build the Field Emitter and all it powers: Purifier, Storage Cell, Locator, Roost.");
    }
}
