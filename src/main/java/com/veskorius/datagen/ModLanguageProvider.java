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
        addBlock(ModBlocks.RESONANCE_CRYSTAL_CLUSTER, "Resonance Crystal Cluster");

        addItem(ModItems.RAW_RESONANCE_CRYSTAL, "Raw Resonance Crystal");
        addItem(ModItems.STABLE_RESONANCE_CRYSTAL, "Stable Resonance Crystal");
        addItem(ModItems.REFINED_RESONANCE_CRYSTAL, "Refined Resonance Crystal");
        addItem(ModItems.RESONANCE_COMPONENT, "Resonance Component");
        addItem(ModItems.RESONANCE_TUNER, "Resonance Tuner");

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
