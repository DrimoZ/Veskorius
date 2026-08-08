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
        addBlock(ModBlocks.RECLAIMER, "Reclaimer");
        addBlock(ModBlocks.ADVANCED_ASSEMBLER, "Advanced Assembler");
        addItem(ModItems.RESONANCE_MATRIX, "Resonance Matrix");
        add("codex.veskorius.machines.advanced_assembler.title", "Advanced Assembler");
        add("codex.veskorius.machines.advanced_assembler.text",
            "Composes the Resonance Matrix: four Resonance Components and two CONDUCTIVE alloy ingots, 30 seconds, 5 Osc per tick.\n\nIts whole job is to add a step, and that is a real job. Without it, Tier 4 machines were built straight from Resonance Components — a Tier 1 part, the one you assemble in your first hour. The chain skipped two tiers at once: everything you forged in Tier 3 went into decorative blocks while Tier 4 kept asking for the same component as Tier 1.\n\nThe conductive ingot, not the structural one. The metal branch you chose at the Forge is charged a third time here, after the Relay and the Harmonic Lattice.");
        add("codex.veskorius.crystals.matrix.title", "Resonance Matrix");
        add("codex.veskorius.crystals.matrix.text",
            "The intermediate part of Tier 3, and all four Tier 4 machines want one: the Amplifier, the Synthesis Chamber, the Extraction Array, the Network Hub.\n\nIt is the opposite of the Harmonic Lattice on purpose. The Lattice is open — it lets a field through and distributes it. The Matrix is solid: it holds and orders. Two intermediate parts of the same network, told apart at a glance in an inventory.");
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
        addItem(ModItems.VESKORIAN_ALLOY_SWORD, "Veskorian Alloy Sword");
        addItem(ModItems.VESKORIAN_ALLOY_PICKAXE, "Veskorian Alloy Pickaxe");
        addItem(ModItems.VESKORIAN_ALLOY_HELMET, "Veskorian Alloy Helmet");
        addItem(ModItems.VESKORIAN_ALLOY_CHESTPLATE, "Veskorian Alloy Chestplate");
        addItem(ModItems.VESKORIAN_ALLOY_LEGGINGS, "Veskorian Alloy Leggings");
        addItem(ModItems.VESKORIAN_ALLOY_BOOTS, "Veskorian Alloy Boots");
        addItem(ModItems.RIFT_WARD_PLATE, "Rift-Ward Plate");
        add("item.veskorius.rift_ward_plate.hint", "Total phase immunity. Costs 10% mining speed while worn.");
        addItem(ModItems.RIFT_ESSENCE, "Rift Essence");
        addItem(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT, "Corrupted Veskorian Alloy Ingot");
        addBlock(ModBlocks.VESKORIAN_ALLOY_BLOCK, "Block of Veskorian Alloy");
        addBlock(ModBlocks.RESONANCE_BLOOM_BUSH, "Resonance Bloom Bush");
        addItem(ModItems.ANCIENT_SEED, "Ancient Seed");
        addItem(ModItems.RESONANCE_BLOOM, "Resonance Bloom");
        add("codex.veskorius.world.bloom.title", "Ancient Seed and Resonance Bloom");
        add("codex.veskorius.world.bloom.text",
            "One seed in five Regional Archives, and one plant is enough forever.\n\nThe bush is picked, not harvested: right-click it ripe and it gives two or three blooms, then drops back to half growth and regrows. It never has to be replanted, which is the whole point — the seed comes from a dig that may not yield one, so a plant that died on harvest would put the branch behind a dice roll.\n\nIt grows on dirt, farmland, and on Resonance Veined Stone — plant it on the veined rock and you have built a greenhouse without meaning to.\n\nBone meal grows it. So do Resonance Dust and Resonance Sludge, on this and on any vanilla crop: the sludge is a waste product, and feeding a field with what you purged from a sick network closes the loop from the bottom.\n\nEating a bloom gives Night Vision for a minute. The rest go to the Advanced Assembler's neighbour, ground into Luminous Extract.");
        addBlock(ModBlocks.LUMINOUS_RESONANCE_GLASS, "Luminous Resonance Glass");
        addBlock(ModBlocks.METEORIC_CRATER, "Meteoric Crater");
        addItem(ModItems.METEORIC_RESONANCE_SHARD, "Meteoric Resonance Shard");
        add("message.veskorius.storm_begins", "The air hums. A Resonance Storm is breaking.");
        add("message.veskorius.storm_ends", "The storm passes. What was left on the ground is gone.");
        add("codex.veskorius.world.storm.title", "The Resonance Storm");
        add("codex.veskorius.world.storm.text",
            "It only happens once you have reached Tier 3 — the first signs of the Collapse mean nothing before the Sigma Laboratory. After that, roughly one Minecraft day in six carries the roll, so expect one every five to seven days.\n\nIt lasts ten minutes. While it runs, meteoric craters settle on exposed surface blocks around you; right-click one and the shard is yours.\n\nEVERYTHING STILL ON THE GROUND WHEN IT ENDS IS GONE. There is no stockpile to build here, only a window to catch. Go outside.\n\nThe shard is the best damping agent in the mod — six thousand, against five hundred for a Refined Crystal. It unlocks nothing: Refined Crystal and Concentrated Flux already cover every need. It is a souvenir you keep for a bad day, and something that falls out of the sky should not be something you plan around.");
        addBlock(ModBlocks.ANCIENT_CONDUIT_STONE, "Ancient Conduit Stone");
        add("codex.veskorius.world.conduit_stone.title", "Ancient Conduit Stone");
        add("codex.veskorius.world.conduit_stone.text",
            "The tell of an Architect ruin, the way Resonance Veined Stone is the tell of a crystal pocket. Channels cut through it — not veins. A vein says there is crystal inside; a channel says someone ran something through here.\n\nIt cannot be crafted, and exactly one thing mines it: the Veskorian Alloy Pickaxe. Neither diamond nor netherite takes anything from it.\n\nYou will meet these walls at Tier 2, be unable to do anything with them, and walk on. Come back with the alloy and they give. A ruin that still holds something when you find it again is worth more than one emptied in a single visit — and nothing depends on it, so waiting costs you no progress.\n\nFollow a lit conduit to its end and you will find this stone: that is where the line comes out of the wall.");
        addItem(ModItems.LUMINOUS_EXTRACT, "Luminous Extract");
        add("codex.veskorius.crystals.extract.title", "Luminous Extract");
        add("codex.veskorius.crystals.extract.text",
            "Two Resonance Blooms crushed in a Crystal Crusher — the Tier 1 machine, twenty hours later. It is the only thing in the mod that still needs it.\n\nIt tints exactly one thing: Resonance Glass, which goes from light level 8 to 15. Eight glass around one extract, the same grid as vanilla stained glass.\n\nOne dye and not sixteen, because what it changes is not a colour but an INTENSITY. Luminous glass is not another shade of purple — it is the one that lights a whole room, and you see that without holding two blocks side by side. Farming for hours to obtain a tint would have been a chore dressed as a reward.");
        addBlock(ModBlocks.RESONANCE_SAND, "Resonance Sand");
        addBlock(ModBlocks.RESONANCE_GLASS, "Resonance Glass");
        add("codex.veskorius.crystals.glass.title", "Resonance Glass");
        add("codex.veskorius.crystals.glass.text",
            "Four sand and one Stable Resonance Crystal make four Resonance Sand; each one smelts into glass in an ordinary furnace. The same gesture as vanilla glass, because that is what it is.\n\nIt gives off light level 8 — enough to light a room without putting a lamp in it.\n\nThis is the only block in Veskorius built purely to be looked at: no recipe downstream wants it, no machine uses it. That is worth saying, because everything else here justifies itself with a mechanic. A building game needs materials that are only beautiful.");
        addBlock(ModBlocks.SYNTHESIS_RESIDUE_BLOCK, "Compressed Synthesis Residue");
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
        addItem(ModItems.CUSTODE_LOURD_SPAWN_EGG, "Heavy Custode Spawn Egg");

        // Entités (09-Entities.md).
        add("entity.veskorius.crystal_strider", "Crystal Strider");
        add("entity.veskorius.custode", "Custode");
        add("entity.veskorius.custode_lourd", "Heavy Custode");
        add("entity.veskorius.rift_guardian", "Rift Guardian");
        add("entity.veskorius.custode_archiviste", "Custode Archivist");
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
        add("codex.veskorius.machines.reclaimer.title", "Reclaimer");
        add("codex.veskorius.machines.reclaimer.text",
            "Feeds waste back into the economy. Four Flux Slag become one gravel; four Resonance Sludge become one Resonance Dust. 20 seconds, 4 Osc per tick.\n\nThe rate is deliberately poor. Reclaiming has to stay worse than mining, or the loop would replace exploration instead of extending it. What you buy here is not yield — it is no longer having to throw anything away.\n\nIt does not replace the Slag Vent. The Vent asks nothing but to exist; the Reclaimer wants a field, a cycle and floor space. Whoever wants slag gone without thinking keeps venting. Whoever wants it back pays for the infrastructure.\n\nBefore it existed, Flux Slag could only be destroyed and Resonance Sludge had nowhere to go at all.");
        add("codex.veskorius.machines.compressor.title", "Flux Compressor");
        add("codex.veskorius.machines.compressor.text",
            "Four Refined Crystals become one Concentrated Flux in 30 seconds, at 6 Osc per tick. An apparent loss, and a deliberate one.\n\nConcentrated Flux has exactly ONE consumer in the whole mod: the Convergence Core. Build a Compressor when you have decided to build that, and not before — there is nothing else to spend the output on.");
        add("codex.veskorius.crystals.alloy.title", "Veskorian Alloy");
        add("codex.veskorius.crystals.alloy.text",
            "The first material you MAKE rather than refine. The T1-T2 chain started from crystal and purified it; this one starts from metal and alloys it. That change of nature is what marks the tier, not the number of steps.\n\nThe conductive variant is the same forge with gold instead of iron. Keep them apart — the Relay and the Harmonic Lattice accept only the conductive one.");
        add("codex.veskorius.crystals.slag.title", "Flux Slag");
        add("codex.veskorius.crystals.slag.text",
            "The Forge's waste, and not an inert one. Chemically it is the same substance that, at regional scale, drove the Collapse.\n\nOne comes out of every forging cycle, into its own slot, and a full slot STOPS the Forge. Empty it by hand, or build a Slag Vent to clear it for you. You are reproducing the cause of the Collapse in miniature, and it will stop your production if you ignore it. That is the intended lesson.");
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
            "The fourth state of the crystal. It cannot be mined, found or crafted — the Deep Synthesis Chamber is its only source, and building that Chamber spends one.\n\nThe three from the Regional Archive are the tier's entire starting stock: two go into your first Harmonic Lattice, the third into the Chamber. You cannot do both at once, and that choice IS Tier 4. Once the Chamber stands, the resource becomes renewable and the pressure lifts.");
        add("codex.veskorius.fields.lattice.title", "Harmonic Lattice");
        add("codex.veskorius.fields.lattice.text",
            "Four CONDUCTIVE alloy ingots and two Hyper Refined Crystals. The metal branch you picked at the Forge is charged for a second time, one tier later.\n\nOnly two things ever want a Lattice: the Harmonic Amplifier and the Convergence Core. It is the T4 network in one item.");
        add("codex.veskorius.fields.harmonics.title", "Harmonic bands");
        add("codex.veskorius.fields.harmonics.text",
            "Every field carries one of three bands, and from Tier 3 every machine carries one too. Set both with a Resonance Tuner.\n\nA machine on the wrong band STILL RUNS. That is the whole design: nothing here ever refuses to work. It costs half again as much Osc, and it injects dissonance into the field — one point per tick of running.\n\nSo a detuned setup is not broken. It is expensive, and it slowly poisons the network it feeds from. You will not notice for a while, which is exactly why it is worth knowing about now.\n\nTier 1 machines have no band at all and accept any field: the opening loop never gains this complexity.");
        add("codex.veskorius.fields.dissonance.title", "Dissonance, and what it does to you");
        add("codex.veskorius.fields.dissonance.text",
            "Dissonance is what a detuned machine leaves behind in a field. It accumulates to 2000, and it announces itself in three stages — you are meant to catch it at the first.\n\nONE: the particle dome greys out. Nothing is wrong yet. TWO: past three quarters, the field goes intermittent — machines stutter for no visible reason. THREE: it discharges. Six damage in a six-block radius, half the stored dissonance spent, then a five-second cooldown before it can happen again.\n\nIt decays on its own at one per second, which is slower than a single detuned machine produces it. Left alone, a bad setup only gets worse.\n\nTwo things clear it: a Damping Array, or a Resonance Relay — a relay pushes dissonance back upstream toward its source rather than passing it on.");
        add("codex.veskorius.machines.damping.title", "Damping Array");
        add("codex.veskorius.machines.damping.text",
            "The cure for dissonance. It purges the field within 16 blocks, one cycle every five seconds, and it eats an agent to do it.\n\nThree agents, and they only differ in how much they absorb: Refined Crystal 500, Concentrated Flux 2500, Meteoric Resonance Shard 6000. The first is available at Tier 2, so the cure never arrives after the problem.\n\nIt fills with Resonance Sludge as it works — the dissonance, crystallised. That sludge is not waste to throw away: it fertilises crops like bone meal, and a Reclaimer turns four of it back into Resonance Dust. What you pull out of a sick network feeds a field.");
        add("codex.veskorius.machines.tunable_emitter.title", "Tunable Field Emitter");
        add("codex.veskorius.machines.tunable_emitter.text",
            "The same emitter, with a band you choose. Craft: a Field Emitter plus two Refined Crystals — an upgrade, not a second machine.\n\nThe plain Tier 2 emitter is single-band and asks nothing of you. This one introduces the choice, and the choice only starts to matter once your machines carry bands of their own.\n\nRun two networks on different bands and they will not interfere. That is the point: a field is a place, and two places can overlap without fighting.");
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
            "Six per rift, and the rift is finished. There is no machine that makes more, and there will not be one — that was decided, and it is not an oversight.\n\nTwo of the six go into the Rift Ward Emitter that makes the site workable at all, so the first rift you clear will really give you four. What you do with them is the last real choice the mod offers.");
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
        add("advancements.veskorius.convergence_formed.title", "The Figure Closes");
        add("advancements.veskorius.convergence_formed.description",
            "Eight relays at five blocks, every one of them seeing the centre. The Core was inert until the last one went down.");
        add("advancements.veskorius.archivist_slain.title", "The Reading Room Is Yours");
        add("advancements.veskorius.archivist_slain.description",
            "It guarded two crystals you did not strictly need. You went anyway.");
        add("advancements.veskorius.storm_caught.title", "Out in the Storm");
        add("advancements.veskorius.storm_caught.description",
            "Ten minutes, and what you leave on the ground is gone. You went outside.");
        add("advancements.veskorius.closed_loop.title", "Nothing Is Thrown Away");
        add("advancements.veskorius.closed_loop.description",
            "Slag becomes gravel, sludge becomes dust. The network stopped costing you material.");
        add("advancements.veskorius.first_bloom.title", "It Grew");
        add("advancements.veskorius.first_bloom.description",
            "One seed in five Archives, and one plant is enough forever.");
        add("advancements.veskorius.luminous_glass.title", "Light Made of Flowers");
        add("advancements.veskorius.luminous_glass.description",
            "Farmed, crushed, and fired into a wall that lights the room by itself.");
        add("advancements.veskorius.tier3_relay.title", "Alloy");
        add("advancements.veskorius.tier3_relay.description",
            "The Sigma Laboratory gave up its plan. Metal now takes resonance — and leaves slag behind.");
        add("advancements.veskorius.tier4_amplifier.title", "Deep Synthesis");
        add("advancements.veskorius.tier4_amplifier.description",
            "The Regional Archive opened. Three crystals, two projects, one decision.");
        add("advancements.veskorius.tier5_rift.title", "Torn Stone");
        add("advancements.veskorius.tier5_rift.description",
            "You recognised the cracks. Something below never stopped resonating.");
        add("advancements.veskorius.rift_guardian_slain.title", "It Kept the Watch");
        add("advancements.veskorius.rift_guardian_slain.description",
            "The rift is stable, and it will not be guarded again.");
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
        add("codex.category.gear", "Gear");
        add("codex.category.lore", "Lore");
        add("codex.category.progression", "Progression");

        add("codex.veskorius.intro.welcome.title", "The Resonance Codex");
        add("codex.veskorius.intro.welcome.text",
            "This book writes itself. Every page about a machine, a material or a mechanic is readable from the very first minute — you are meant to read ahead and decide what to build next. Only the lore fragments stay sealed until you find them in the world.\n\nA page turns dimmer in the list when you have not yet met the thing it describes. That is a progress marker, not a lock.");
        add("codex.veskorius.intro.using_codex.title", "Reading the Codex");
        add("codex.veskorius.intro.using_codex.text",
            "Left edge: one tab per section, plus a ◆ tab for the progression map. The map lays out every entry by tier, so you can see what a tier contains before you reach it.\n\nSearch covers titles and body text across every section. Arrow keys turn pages, Escape steps back one level instead of closing the book. At the bottom of a page, a link takes you to the next entry in the same section.\n\nWhen a page has a recipe, it is drawn from the recipes your world actually loaded — never from a wiki that has gone stale.");
        add("codex.veskorius.intro.getting_started.title", "Getting Started");
        add("codex.veskorius.intro.getting_started.text",
            "Dig. Resonance crystal grows in pockets between Y -20 and Y 0, and those pockets are wrapped in Resonance Veined Stone — that shell is how you spot one before you break anything.\n\nRaw crystal is unstable and useless as it comes. Your first machine is a Resonance Stabilizer, which needs only cobblestone, copper and one raw crystal. It runs on nothing — no field, no fuel — and that is deliberate: the first loop must close without infrastructure.\n\nAfter that, the Attunement Console in a Veskorian Outpost opens Tier 2.");

        add("codex.veskorius.crystals.raw.title", "Raw Resonance Crystal");
        add("codex.veskorius.crystals.raw.text",
            "What you mine. It is unstable: it does nothing on its own and no recipe accepts it directly except the two machines that process it.\n\nTwo ways forward, and they are not the same. A Resonance Stabilizer turns it into a Stable Crystal in 30 seconds — slow, and the backbone of everything. A Crystal Crusher turns it into 3 Resonance Dust in 10 seconds — fast, but dust is not a crystal and cannot fuel anything.\n\nCarrying raw crystal for more than two minutes without processing it starts to hurt.");
        add("codex.veskorius.crystals.stable.title", "Stable Resonance Crystal");
        add("codex.veskorius.crystals.stable.text",
            "Raw crystal held steady. It is the first thing in the mod that stores energy: one Stable Crystal is 4000 Osc of fuel in a Field Emitter, which is exactly one full reserve.\n\nIt is also the input of the Flux Purifier, which refines it further. Early on you will want both, and there is never quite enough — that tension is the Tier 2 loop.");
        add("codex.veskorius.crystals.refined.title", "Refined Resonance Crystal");
        add("codex.veskorius.crystals.refined.text",
            "Stable crystal purified: 45 seconds in a Flux Purifier with one redstone, or 22 seconds if you accept overheat and its one-in-five chance of losing the batch.\n\nIt is the gate material of Tier 3. The Veskorian Chassis needs two, the Alloy Forge consumes two per cycle, and the Flux Compressor eats four at a time. Refining is the bottleneck of the mid game, and building a second Purifier is usually the right call before building anything clever.");
        add("codex.veskorius.crystals.dust.title", "Resonance Dust");
        add("codex.veskorius.crystals.dust.text",
            "Three dust from one raw crystal, in 10 seconds. Faster than stabilising, and worth understanding before you commit: dust is NOT a cheaper stable crystal. It cannot fuel a Field Emitter and cannot be refined.\n\nWhat it does is feed the Component Assembler through its alternative branch, and make Resonance Conduits. It is the shortcut for someone who needs components now and infrastructure later.");
        add("codex.veskorius.crystals.pockets.title", "Crystal Pockets");
        add("codex.veskorius.crystals.pockets.text",
            "Crystal grows in small pockets between Y -20 and Y 0, wrapped in a shell of Resonance Veined Stone. Learn the shell and you stop digging blindly — that is the whole point of it existing.\n\nSome pockets carry a Raw Flux Deposit on their walls: brush it, do not mine it. Mining destroys the crust and gives nothing.\n\nCrystal Striders live near pockets. They are harmless, and they are a slow renewable source of raw crystal if you keep some.");

        add("codex.veskorius.fields.osc.title", "Osc & Fields");
        add("codex.veskorius.fields.osc.text",
            "Osc is the unit of Resonance energy, and it never travels through a pipe. A Field Emitter covers a SPHERE, and any machine inside that sphere draws from it — there is nothing to connect, and nothing to route.\n\nFields do not add up. When two overlap, the STRONGEST source serves; between equals, the one placed first. So doubling your emitters doubles your reserve, never your throughput.\n\nA machine that cannot draw its cost simply pauses, and resumes when the field comes back. Nothing is ever lost to a brownout.");
        add("codex.veskorius.fields.emitter.title", "Field Emitter");
        add("codex.veskorius.fields.emitter.text",
            "The first source of field: 8 blocks of radius, a 4000 Osc reserve, and one Stable Crystal refills it completely.\n\nIt is not a machine with a cycle — it burns fuel and covers ground. The fuels are data-driven, so a modpack can add its own without touching code.\n\nIts lit face tells you at a glance whether it still has reserve. Running dry is the most ordinary failure in this mod, and it is the one you should be able to see while walking past.");
        add("codex.veskorius.fields.storage_cell.title", "Resonance Storage Cell");
        add("codex.veskorius.fields.storage_cell.text",
            "A portable 8000 Osc battery — twice an emitter's reserve, in your pocket.\n\nIt does not power machines. It powers the Resonance Locator, which would otherwise be useless away from a field, and that is exactly when you need a Locator: out exploring, far from any base.");
        add("codex.veskorius.fields.locator.title", "Resonance Locator");
        add("codex.veskorius.fields.locator.text",
            "A short-range detector with two modes; shift-right-click switches. RESOURCES points at nearby crystal pockets. STRUCTURES points at ruins you have not entered.\n\nIt costs 5 Osc per use, drawn from a Resonance Storage Cell in your inventory.\n\nCarrying one also switches on the field HUD, which shows the reserve and harmonic band of whatever field you are standing in. Once you have a base, that readout is worth more than the pinging.");

        add("codex.veskorius.machines.stabilizer.title", "Resonance Stabilizer");
        add("codex.veskorius.machines.stabilizer.text",
            "Raw Resonance Crystal plus a flux (quartz, or Raw Flux Deposit — the tag accepts both) becomes one Stable Crystal in 30 seconds.\n\nIt runs on NOTHING. No field, no fuel, no redstone. That is deliberate and it matters: the first machine of the mod has to work before you have any infrastructure at all, or the chain could never start.\n\nIts recipe is a Fractured Chassis and one raw crystal. Cobblestone and copper — nothing you have to go looking for.");
        add("codex.veskorius.machines.assembler.title", "Component Assembler");
        add("codex.veskorius.machines.assembler.text",
            "One Stable Crystal and two iron become two Resonance Components in 5 seconds, at 3 Osc per tick. Your first machine that needs a field.\n\nIron here means the tag, not the ingot: a Custode Alloy Fragment works just as well, so fighting the guards of a ruin is a real alternative to mining.\n\nThere is a second branch that takes Resonance Dust instead of a stable crystal — faster to feed, if you went the Crusher route.");
        add("codex.veskorius.machines.whetstone.title", "Resonance Whetstone");
        add("codex.veskorius.machines.whetstone.text",
            "A damaged tool and a Stable Crystal restore 25% of its durability in 8 seconds. Autonomous, like the Stabilizer — no field needed.\n\nIt does not consume enchantments and has no anvil cost that climbs. It is not meant to replace an anvil; it is meant to keep your pickaxe alive during the long early dig, when you have crystal but no experience to spare.");
        add("codex.veskorius.machines.purifier.title", "Flux Purifier");
        add("codex.veskorius.machines.purifier.text",
            "One Stable Crystal and one redstone become a Refined Crystal in 45 seconds, at 2 Osc per tick.\n\nIt is the first machine that accepts OVERHEAT: 22 seconds instead of 45, double the Osc, and one cycle in five destroys the input without producing anything. Switch it with a Resonance Tuner. It is a bet, not an upgrade — nobody should leave it on by default.\n\nIt is also the first machine that can be tuned to a harmonic band, which is where the Tier 2 energy game actually begins.");
        add("codex.veskorius.machines.crusher.title", "Crystal Crusher");
        add("codex.veskorius.machines.crusher.text",
            "One Raw Crystal becomes 3 Resonance Dust in 10 seconds. Autonomous.\n\nIt is the alternative to the Stabilizer, not a supplement: three times the output in a third of the time, but dust cannot fuel an emitter and cannot be refined. Choose it when you need Components fast, and keep a Stabilizer for everything else.");
        add("codex.veskorius.machines.roost.title", "Crystal Roost");
        add("codex.veskorius.machines.roost.text",
            "A nest, not a factory. Feed it quartz — four per Minecraft day — and it attracts and keeps Crystal Striders. If a Strider is nearby, it yields one Raw Crystal every 600 seconds.\n\nIt costs no energy at all. It is slower than mining on purpose: it is a floor under your supply, not a replacement for going out.");
        add("codex.veskorius.machines.tuner.title", "Resonance Tuner");
        add("codex.veskorius.machines.tuner.text",
            "One tool for every machine setting. It carries a current mode; right-click a machine to apply it, shift-right-click in the air to cycle modes.\n\nROTATE turns the face. POWER is the on/off switch. OVERHEAT toggles the risky mode where it exists. REDSTONE cycles the control mode. ATTUNE changes a harmonic band. PRIORITY sets who gets shed first when a Network Hub runs short. RECALIBRATE repairs the drift of an Amplifier or a Hub, at the cost of one Resonance Component.\n\nShift-right-click a block entity to DISMANTLE it: the block and everything in it go straight to your inventory. That works on other mods' blocks too.");
        add("codex.veskorius.machines.catalyst_core.title", "Resonance Catalyst Core");
        add("codex.veskorius.machines.catalyst_core.text",
            "Slots into the augment slot of any active machine: +15% cycle speed, permanently, and it is never consumed.\n\nIt does not drift and needs no maintenance — unlike an Amplifier or a Hub, it is a one-off investment rather than an upkeep. Take it back out with a Resonance Tuner rather than breaking the machine.\n\nThe number of augment slots is configurable, so a modpack can loosen or tighten this.");
        add("codex.veskorius.machines.control.title", "Controlling Machines");
        add("codex.veskorius.machines.control.text",
            "Every active machine shares the same controls, and they are worth knowing once.\n\nA manual on/off switch. A redstone mode: ignored, requires a signal, or requires no signal. Per-face item configuration — input, output or disabled — plus auto-pull and auto-push toggles, so hoppers and pipes work the way you expect.\n\nEnergy never uses any of that. It comes from the field, and the field has no sides.");

        add("codex.veskorius.world.veined_stone.title", "Resonance Veined Stone");
        add("codex.veskorius.world.veined_stone.text",
            "The shell around a crystal pocket, and the single most useful thing to recognise underground: seeing it means crystal is within a few blocks.\n\nIt also grows Resonance Spores on exposed faces in low light — right-click to harvest, and it regrows. Spores are what you breed Crystal Striders with.\n\nIt is a fine building block, and it is the base of the whole Veined Stone Brick family.");
        add("codex.veskorius.world.flux_deposit.title", "Raw Flux Deposit");
        add("codex.veskorius.world.flux_deposit.text",
            "A brittle crust on the walls of some crystal pockets. BRUSH it — mining destroys the crust and yields nothing at all.\n\nWhat it gives, Raw Flux Deposit, substitutes for quartz in the Resonance Stabilizer one for one. On a world where quartz means a trip to the Nether, that substitution is the difference between starting today and starting next session.");
        add("codex.veskorius.world.outpost.title", "The Outpost");
        add("codex.veskorius.world.outpost.text",
            "A Veskorian ruin, and your gate into Tier 2. Its Attunement Console gives the Tier 2 blueprint, which every Tier 2 recipe requires and returns.\n\nIts chest guarantees 4 Resonance Components and 2 gold — exactly enough for one Field Emitter. That is not generosity: the Emitter recipe needs Components, Components need an Assembler, and the Assembler needs a field. The outpost breaks that circle, once.\n\nA Custode guards the site. It only reacts within 6 blocks, or if you break its machines.");

        add("codex.veskorius.fauna.strider.title", "Crystal Strider");
        add("codex.veskorius.fauna.strider.text",
            "A harmless creature that grazes near crystal pockets. It never attacks, and it should not be killed for anything.\n\nRight-click to milk it: one Raw Crystal, with a five-minute cooldown. Feed a Resonance Spore to two adults to breed them — spores grow on Resonance Veined Stone in low light.\n\nA Crystal Roost will attract and keep them, which turns a chance encounter into a small standing supply.");
        add("codex.veskorius.fauna.archiviste.title", "Archivist Custode");
        add("codex.veskorius.fauna.archiviste.text",
            "The elite that guards the Archive's deep room. 150 health, 12 damage, and it reacts from ten blocks away — well before you see it.\n\nIt marks the ground under your feet and detonates the mark one second later. That single attack decides the fight: you cannot trade blows standing still, and the reading room is full of shelving to trip over. Watch for the flame, step off it.\n\nFighting it is optional. Its room holds two more Hyper Refined Crystals, which is exactly what lets you build both the first Amplifier and the Synthesis Chamber instead of choosing between them.");
        add("codex.veskorius.gear.tools.title", "Alloy Tools");
        add("codex.veskorius.gear.tools.text",
            "Sword and pickaxe forged from Veskorian Alloy. The pickaxe mines everything netherite does; the sword hits for diamond damage. 1873 uses — roughly one and a half times a diamond tool.\n\nThey repair with alloy ingots, and the Resonance Whetstone restores a quarter of their durability in eight seconds, consuming nothing but time and a field.\n\nThe alloy comes out of the Veskorian Alloy Forge, which also produces slag on every cycle. Plan a Slag Vent before you plan a tool set.");
        add("codex.veskorius.gear.armor.title", "Alloy Armor");
        add("codex.veskorius.gear.armor.text",
            "Four pieces, diamond-level protection, 33 durability units per point — noticeably tougher than diamond, short of netherite.\n\nThe full set halves the phase damage a Rift Guardian deals. Not immunity: half. Three pieces give nothing, so the set is worth completing before you go down.\n\nRepairs with Veskorian Alloy ingots.");
        add("codex.veskorius.gear.ward_plate.title", "Rift-Ward Plate");
        add("codex.veskorius.gear.ward_plate.text",
            "A chestplate built around Rift Essence, and the only thing in Veskorius that reduces phase damage to zero.\n\nPhase damage is what the Rift Guardian deals during its ECHO phase, and what a destabilised Rift Core radiates within eight blocks. Ordinary armor does not apply to it. Alloy armor halves it. This halves nothing — it removes it.\n\nIt replaces the alloy chestplate, so wearing it costs you the full-set bonus on the other three pieces. That trade is the point: total immunity to one damage type, or a general reduction across everything else.");
        add("codex.veskorius.fauna.lourd.title", "Heavy Custode");
        add("codex.veskorius.fauna.lourd.text",
            "The reinforced guard of the Sigma Laboratory and the Regional Archive. 60 health, 9 damage, reactive from eight blocks — two more than the ordinary Custode.\n\nEverything else about it is the same guard: it patrols one spot, it will not chase you off its site, and it only attacks if you come to it or break one of its machines. What it adds is that it calls another. Take a target and every Heavy Custode within sixteen blocks takes the same one.\n\nThey are posted in pairs. That is the whole difficulty of these two ruins, and the answer is not more health — it is not fighting them together. Isolate one, or go around.\n\nDrops 4 to 7 alloy fragments, which substitute for iron ingots in any Veskorius recipe.");
        add("codex.veskorius.fauna.custode.title", "Custode");
        add("codex.veskorius.fauna.custode.text",
            "A Veskorian guard, still at its post. It patrols a fixed point and only turns hostile within 6 blocks, or if you break a machine on its site — it guards places, not territory, and it can be walked around.\n\nKilling one drops 2 to 4 Custode Alloy Fragments, which substitute for iron ingots in every Veskorius recipe. Fighting is therefore a genuine alternative to mining, not a detour.");

        add("codex.veskorius.progression.tier1.title", "Tier 1 — Awakening");
        add("codex.veskorius.progression.tier1.text",
            "Dig, stabilise, assemble. Everything here runs without a field, on cobblestone and copper, and that is the point: the first loop has to close before you own any infrastructure.\n\nWhat opens the next tier is not a craft but a place — the Attunement Console of a Veskorian Outpost.");
        add("codex.veskorius.progression.tier2.title", "Tier 2 — The Field");
        add("codex.veskorius.progression.tier2.text",
            "The tier of the FIELD. Energy stops being something a machine has and becomes something a place has: a Field Emitter covers a sphere, and everything inside it draws from that sphere.\n\nIt also brings the first real decisions — overheat on the Purifier, harmonic bands, and the fact that overlapping fields never add up.\n\nTier 3 opens at the Sigma Laboratory, and getting in means solving something rather than crafting something.");
    }
}
