package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * French translations (fr_fr). The mod's canonical language is English (en_us,
 * {@link ModLanguageProvider}); this file is an optional localization for French
 * players. Keys must stay in sync with {@link ModLanguageProvider}.
 */
public class ModFrenchLangProvider extends LanguageProvider {

    public ModFrenchLangProvider(PackOutput output) {
        super(output, Veskorius.MOD_ID, "fr_fr");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.veskorius.main_tab", "Veskorius");

        // Boutons de contrôle des machines.
        add("gui.veskorius.machine_on", "Machine : allumée (cliquer pour éteindre)");
        add("gui.veskorius.machine_off", "Machine : éteinte (cliquer pour allumer)");
        add("gui.veskorius.redstone_control", "Contrôle redstone");
        add("gui.veskorius.redstone_ignored", "Ignoré");
        add("gui.veskorius.redstone_requires_signal", "Requiert un signal");
        add("gui.veskorius.redstone_requires_no_signal", "Requiert l'absence de signal");
        add("gui.veskorius.overheat_on", "Surchauffe : active (÷2 vitesse, ×2 Osc, 20 % de perte)");
        add("gui.veskorius.overheat_off", "Surchauffe : inactive");
        add("gui.veskorius.osc_reserve", "%s/%s Osc");

        // Blocs.
        addBlock(ModBlocks.RESONANCE_STABILIZER, "Stabilisateur de Résonance");
        addBlock(ModBlocks.COMPONENT_ASSEMBLER, "Assembleur de Composants");
        addBlock(ModBlocks.FLUX_PURIFIER, "Purificateur de Flux");
        addBlock(ModBlocks.RESONANCE_WHETSTONE, "Meule de Résonance");
        addBlock(ModBlocks.FIELD_EMITTER, "Émetteur de Champ");
        addBlock(ModBlocks.RESONANCE_CRYSTAL_CLUSTER, "Amas de Cristal de Résonance");
        addBlock(ModBlocks.RESONANCE_VEINED_STONE, "Pierre Veinée de Résonance");
        addBlock(ModBlocks.RAW_FLUX_DEPOSIT, "Dépôt de Flux Brut");
        addBlock(ModBlocks.ATTUNEMENT_CONSOLE, "Console d'Attunement");
        addBlock(ModBlocks.CRYSTAL_CRUSHER, "Broyeur de Cristaux");
        addBlock(ModBlocks.CRYSTAL_ROOST, "Perchoir à Cristaux");

        // Objets.
        addItem(ModItems.RAW_RESONANCE_CRYSTAL, "Cristal de Résonance Brut");
        addItem(ModItems.STABLE_RESONANCE_CRYSTAL, "Cristal de Résonance Stable");
        addItem(ModItems.REFINED_RESONANCE_CRYSTAL, "Cristal de Résonance Raffiné");
        addItem(ModItems.RESONANCE_COMPONENT, "Composant de Résonance");
        addItem(ModItems.RESONANCE_DUST, "Poussière de Résonance");
        addItem(ModItems.RAW_FLUX_DEPOSIT, "Dépôt de Flux Brut");
        addItem(ModItems.RESONANCE_CATALYST_CORE, "Cœur Catalyseur de Résonance");
        addItem(ModItems.RESONANCE_TUNER, "Accordeur de Résonance");
        addItem(ModItems.RESONANCE_STORAGE_CELL, "Cellule de Stockage de Résonance");
        addItem(ModItems.RESONANCE_LOCATOR, "Localisateur de Résonance");
        addItem(ModItems.RESONANCE_SPORE, "Spore de Résonance");
        addItem(ModItems.CRYSTAL_STRIDER_SPAWN_EGG, "Œuf d'apparition de Fileur de Cristal");
        addItem(ModItems.RESONANCE_BLUEPRINT, "Plan de Résonance");
        addItem(ModItems.CODEX_FRAGMENT, "Fragment de Codex");
        addItem(ModItems.FOSSILIZED_RATION, "Ration Fossilisée");
        addItem(ModItems.CUSTODE_ALLOY_FRAGMENT, "Fragment d'Alliage de Custode");
        addItem(ModItems.CUSTODE_SPAWN_EGG, "Œuf d'apparition de Custode");

        // Entités.
        add("entity.veskorius.crystal_strider", "Fileur de Cristal");
        add("entity.veskorius.custode", "Custode");

        // Accordeur de Résonance.
        add("gui.veskorius.tuner_rotate", "Pivoter la machine");
        add("gui.veskorius.tuner_power", "Marche/Arrêt");
        add("gui.veskorius.tuner_overheat", "Basculer la surchauffe");
        add("gui.veskorius.tuner_redstone", "Cycle du mode redstone");
        add("item.veskorius.resonance_tuner.current_mode", "Mode courant");
        add("item.veskorius.resonance_tuner.available_modes", "Modes disponibles");
        add("item.veskorius.resonance_tuner.controls", "Commandes");
        add("item.veskorius.resonance_tuner.ctrl_apply", "Clic droit sur une machine : applique le mode");
        add("item.veskorius.resonance_tuner.ctrl_cycle", "Clic droit dans le vide : change de mode");
        add("item.veskorius.resonance_tuner.ctrl_dismantle", "Maj + clic droit : démonter le bloc");
        add("tooltip.veskorius.hold_shift", "Maj pour les commandes");
        add("item.veskorius.resonance_tuner.mode", "Mode : %s");
        add("item.veskorius.resonance_tuner.rotated", "Machine pivotée");
        add("item.veskorius.resonance_tuner.no_overheat", "Cette machine ne supporte pas la surchauffe");
        add("item.veskorius.resonance_tuner.dismantled", "Démontée");

        // Objets de progression.
        add("item.veskorius.resonance_blueprint.tier", "Plan restauré — Palier %s");
        add("item.veskorius.resonance_blueprint.hint", "Conservé lors du craft. Restaure les machines de ce palier.");
        add("item.veskorius.codex_fragment.hint", "Clic droit pour lire");
        add("item.veskorius.resonance_storage_cell.charge", "%s / %s Osc");

        // Localisateur.
        add("item.veskorius.resonance_locator.charge", "%s / %s Osc");
        add("gui.veskorius.locator.empty", "Le localisateur n'a plus de charge");
        add("gui.veskorius.locator.none", "Aucune résonance à portée");
        add("gui.veskorius.locator.found", "%s vers le %s (%s blocs)");
        add("gui.veskorius.locator.type_crystal", "Résonance de cristal");
        add("gui.veskorius.locator.type_field", "Signature de champ");
        add("gui.veskorius.dir.n", "nord");
        add("gui.veskorius.dir.ne", "nord-est");
        add("gui.veskorius.dir.e", "est");
        add("gui.veskorius.dir.se", "sud-est");
        add("gui.veskorius.dir.s", "sud");
        add("gui.veskorius.dir.sw", "sud-ouest");
        add("gui.veskorius.dir.w", "ouest");
        add("gui.veskorius.dir.nw", "nord-ouest");

        // Console d'attunement + Fileur.
        add("block.veskorius.attunement_console.restored", "La console s'éveille — plan restauré");
        add("block.veskorius.attunement_console.already", "Vous possédez déjà ce plan");
        add("gui.veskorius.strider.milk_cooldown", "Le Fileur a besoin de %s secondes de plus");

        // Codex (lore).
        add("codex.veskorius.daily_life.lamps.title", "Note domestique — la lumière");
        add("codex.veskorius.daily_life.lamps.text",
            "On nous livrait la lumière sans fil ni flamme. Les anciens disaient qu'il fallait "
                + "« rester dans le champ ». Au bord du village, les lampes faiblissaient.");
        add("codex.veskorius.daily_life.ration.title", "Registre — cycle sec, ration 14");
        add("codex.veskorius.daily_life.ration.text",
            "Le grain tient, le cristal aussi. Tant que la Tour du quartier chante, on ne manque de rien.");
        add("codex.veskorius.hint.workshop.title", "Note domestique — l'atelier d'en bas");
        add("codex.veskorius.hint.workshop.text",
            "L'atelier d'en bas tenait encore quand on est partis. Sa console répondait à qui savait la réveiller.");

        // Advancements.
        add("advancements.veskorius.tier1_awakening.title", "L'Éveil");
        add("advancements.veskorius.tier1_awakening.description", "Ramasser un Cristal de Résonance Brut");
        add("advancements.veskorius.tier2_field.title", "Réseau court");
        add("advancements.veskorius.tier2_field.description", "Restaurer le plan du champ à une console d'Avant-poste");
    }
}
