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
        add("gui.veskorius.machine_on", "Machine: On (click to turn off)");
        add("gui.veskorius.machine_off", "Machine: Off (click to turn on)");
        add("gui.veskorius.redstone_control", "Redstone control");
        add("gui.veskorius.redstone_ignored", "Ignored");
        add("gui.veskorius.redstone_requires_signal", "Requires signal");
        add("gui.veskorius.redstone_requires_no_signal", "Requires no signal");
        add("gui.veskorius.overheat_on", "Overheat: On (÷2 speed, ×2 Osc, 20% input loss)");
        add("gui.veskorius.overheat_off", "Overheat: Off");

        // Field Emitter : jauge de réserve d'Osc.
        add("gui.veskorius.osc_reserve", "%s/%s Osc");

        addBlock(ModBlocks.RESONANCE_STABILIZER, "Resonance Stabilizer");
        addBlock(ModBlocks.COMPONENT_ASSEMBLER, "Component Assembler");
        addBlock(ModBlocks.FLUX_PURIFIER, "Flux Purifier");
        addBlock(ModBlocks.RESONANCE_WHETSTONE, "Resonance Whetstone");
        addBlock(ModBlocks.FIELD_EMITTER, "Field Emitter");
        addBlock(ModBlocks.CRYSTAL_CRUSHER, "Crystal Crusher");
        addBlock(ModBlocks.CRYSTAL_ROOST, "Crystal Roost");
        addBlock(ModBlocks.RESONANCE_CRYSTAL_CLUSTER, "Resonance Crystal Cluster");
        addBlock(ModBlocks.RESONANCE_VEINED_STONE, "Resonance Veined Stone");
        addBlock(ModBlocks.RAW_FLUX_DEPOSIT, "Raw Flux Deposit");
        addBlock(ModBlocks.ATTUNEMENT_CONSOLE, "Attunement Console");
        add("block.veskorius.attunement_console.restored", "The console wakes — blueprint restored");
        add("block.veskorius.attunement_console.already", "You already carry this blueprint");

        addItem(ModItems.RAW_RESONANCE_CRYSTAL, "Raw Resonance Crystal");
        addItem(ModItems.STABLE_RESONANCE_CRYSTAL, "Stable Resonance Crystal");
        addItem(ModItems.REFINED_RESONANCE_CRYSTAL, "Refined Resonance Crystal");
        addItem(ModItems.RESONANCE_COMPONENT, "Resonance Component");
        addItem(ModItems.RESONANCE_DUST, "Resonance Dust");
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
        add("gui.veskorius.dir.n", "north");
        add("gui.veskorius.dir.ne", "north-east");
        add("gui.veskorius.dir.e", "east");
        add("gui.veskorius.dir.se", "south-east");
        add("gui.veskorius.dir.s", "south");
        add("gui.veskorius.dir.sw", "south-west");
        add("gui.veskorius.dir.w", "west");
        add("gui.veskorius.dir.nw", "north-west");
        addItem(ModItems.RESONANCE_SPORE, "Resonance Spore");
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

        // Advancements (feedback, tâche 10).
        add("advancements.veskorius.tier1_awakening.title", "The Awakening");
        add("advancements.veskorius.tier1_awakening.description", "Pick up a Raw Resonance Crystal");
        add("advancements.veskorius.tier2_field.title", "Short Network");
        add("advancements.veskorius.tier2_field.description", "Restore the field blueprint at an Outpost console");

        // Resonance Tuner
        add("gui.veskorius.tuner_rotate", "Rotate Machine");
        add("gui.veskorius.tuner_power", "Toggle Power");
        add("gui.veskorius.tuner_overheat", "Toggle Overheat");
        add("gui.veskorius.tuner_redstone", "Cycle Redstone Mode");

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
    }
}
