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
        add("gui.veskorius.strider.milk_cooldown", "The strider needs %s more seconds");

        // Progression : plans, fragments, ration (tâche 10).
        addItem(ModItems.RESONANCE_BLUEPRINT, "Resonance Blueprint");
        addItem(ModItems.CODEX_FRAGMENT, "Codex Fragment");
        addItem(ModItems.FOSSILIZED_RATION, "Fossilized Ration");
        add("item.veskorius.resonance_blueprint.tier", "Restored blueprint — Tier %s");
        add("item.veskorius.resonance_blueprint.hint", "Kept when crafting. Restore machines of this tier.");
        add("item.veskorius.codex_fragment.hint", "Right-click to read");

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
