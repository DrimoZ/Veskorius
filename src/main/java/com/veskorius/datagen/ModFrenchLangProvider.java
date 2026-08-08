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

        // Config item I/O par face (l'énergie ne passe jamais par là — objets uniquement).
        add("gui.veskorius.config", "Configurer les faces (objets)");
        add("gui.veskorius.side.down", "Dessous");
        add("gui.veskorius.side.up", "Dessus");
        add("gui.veskorius.side.north", "Nord");
        add("gui.veskorius.side.south", "Sud");
        add("gui.veskorius.side.west", "Ouest");
        add("gui.veskorius.side.east", "Est");
        add("gui.veskorius.sidemode.disabled", "Désactivé");
        add("gui.veskorius.sidemode.input", "Entrée");
        add("gui.veskorius.sidemode.output", "Sortie");
        add("gui.veskorius.auto_input", "Auto-entrée : %s");
        add("gui.veskorius.auto_output", "Auto-sortie : %s");
        add("gui.veskorius.on", "Activé");
        add("gui.veskorius.off", "Désactivé");
        add("gui.veskorius.osc_reserve", "%s/%s Osc");

        // Blocs.
        addBlock(ModBlocks.RESONANCE_STABILIZER, "Stabilisateur de Résonance");
        addBlock(ModBlocks.COMPONENT_ASSEMBLER, "Assembleur de Composants");
        addBlock(ModBlocks.FLUX_PURIFIER, "Purificateur de Flux");
        addBlock(ModBlocks.RESONANCE_WHETSTONE, "Meule de Résonance");
        addBlock(ModBlocks.FIELD_EMITTER, "Émetteur de Champ");
        addBlock(ModBlocks.TUNABLE_FIELD_EMITTER, "Émetteur de Champ Accordable");
        addBlock(ModBlocks.RESONANCE_CRYSTAL_CLUSTER, "Amas de Cristal de Résonance");
        addBlock(ModBlocks.RESONANCE_VEINED_STONE, "Pierre Veinée de Résonance");
        addBlock(ModBlocks.RAW_FLUX_DEPOSIT, "Dépôt de Flux Brut");
        addBlock(ModBlocks.ATTUNEMENT_CONSOLE, "Console d'Attunement");
        addBlock(ModBlocks.CRYSTAL_CRUSHER, "Broyeur de Cristaux");
        addBlock(ModBlocks.CRYSTAL_ROOST, "Perchoir à Cristaux");
        addBlock(ModBlocks.DAMPING_ARRAY, "Matrice d'Amortissement");
        addBlock(ModBlocks.VESKORIAN_ALLOY_FORGE, "Forge d'Alliage Veskorien");
        addBlock(ModBlocks.RESONANCE_RELAY, "Relais de Résonance");
        addBlock(ModBlocks.FLUX_COMPRESSOR, "Compresseur de Flux");
        addBlock(ModBlocks.RECLAIMER, "Récupérateur");
        addBlock(ModBlocks.ADVANCED_ASSEMBLER, "Assembleur Avancé");
        addItem(ModItems.RESONANCE_MATRIX, "Matrice de Résonance");
        add("codex.veskorius.machines.advanced_assembler.title", "Assembleur Avancé");
        add("codex.veskorius.machines.advanced_assembler.text",
            "Compose la Matrice de Résonance : quatre Composants de Résonance et deux lingots d'alliage CONDUCTEUR, 30 secondes, 5 Osc par tick.\n\nTout son rôle est d'ajouter une étape, et c'en est un vrai. Sans lui, les machines du palier 4 se fabriquaient directement avec des Composants de Résonance — une pièce du palier 1, celle qu'on assemble dans la première heure. La chaîne sautait deux paliers d'un coup : tout ce qu'on forgeait au palier 3 partait dans des blocs décoratifs pendant que le palier 4 réclamait le même composant que le palier 1.\n\nLe lingot conducteur, pas le structurel. La branche de métal choisie à la Forge se fait payer ici une troisième fois, après le Relais et le Treillis Harmonique.");
        add("codex.veskorius.crystals.matrix.title", "Matrice de Résonance");
        add("codex.veskorius.crystals.matrix.text",
            "La pièce intermédiaire du palier 3, et les quatre machines du palier 4 en réclament une : l'Amplificateur, la Chambre de Synthèse, le Réseau d'Extraction, le Concentrateur.\n\nElle est l'inverse du Treillis Harmonique, et c'est voulu. Le Treillis est ajouré — il laisse passer un champ et le répartit. La Matrice est pleine : elle contient et ordonne. Deux pièces intermédiaires du même réseau, qu'on sépare d'un coup d'œil dans un inventaire.");
        addBlock(ModBlocks.STRUCTURAL_SYNTHESIZER, "Synthétiseur Structurel");
        addBlock(ModBlocks.DEEP_CRYSTAL_DRILLER, "Foreuse à Cristaux Profonds");
        addBlock(ModBlocks.SLAG_VENT, "Évent à Scorie");
        addBlock(ModBlocks.ARCHIVE_CONSOLE, "Console d'Archive");
        addBlock(ModBlocks.DEEP_SYNTHESIS_CHAMBER, "Chambre de Synthèse Profonde");
        addBlock(ModBlocks.HARMONIC_AMPLIFIER, "Amplificateur Harmonique");
        addBlock(ModBlocks.AUTOMATED_EXTRACTION_ARRAY, "Matrice d'Extraction Automatisée");
        addBlock(ModBlocks.RESONANCE_NETWORK_HUB, "Nœud de Réseau de Résonance");
        addBlock(ModBlocks.CONVERGENCE_CORE, "Cœur de Convergence");
        addBlock(ModBlocks.RIFT_ANCHOR, "Ancre de Faille");
        addBlock(ModBlocks.RIFT_CORE_EXTRACTOR, "Extracteur de Noyau");
        addBlock(ModBlocks.RIFT_WARD_EMITTER, "Émetteur de Garde");
        add("message.veskorius.ward_on", "Garde active — la corrosion est contenue");
        add("message.veskorius.ward_off", "Garde inerte — pas de champ");
        add("item.veskorius.rift_essence.hint", "Six par Faille, et la Faille est morte. Rien n'en refait.");
        addBlock(ModBlocks.RIFT_CORE, "Noyau de Faille");
        addBlock(ModBlocks.DEFORMED_STONE, "Pierre Déformée");
        add("message.veskorius.anchor_holding", "Ancre active — la Faille est stable");
        add("message.veskorius.anchor_idle", "Ancre inerte — aucune Faille à portée, ou pas de champ");
        add("item.veskorius.deformed_stone.hint", "Pierre tordue par une Faille. La bulle est proche — et elle n'est pas sûre.");
        add("message.veskorius.core_formed", "Cœur en ligne — portée %s");
        add("message.veskorius.core_incomplete", "Cœur inerte — il lui faut %s relais ou amplificateurs à %s blocs, chacun en vue directe");
        add("gui.veskorius.priority_low", "Basse");
        add("gui.veskorius.priority_normal", "Normale");
        add("gui.veskorius.priority_high", "Haute");

        // Architecture de donjon (17-Dungeons.md §4)
        addBlock(ModBlocks.VEINED_STONE_BRICKS, "Briques de Pierre Veinée");
        addBlock(ModBlocks.CRACKED_VEINED_STONE_BRICKS, "Briques de Pierre Veinée Fissurées");
        addBlock(ModBlocks.CHISELED_VEINED_STONE, "Pierre Veinée Gravée");
        addBlock(ModBlocks.VEINED_STONE_BRICK_STAIRS, "Escalier de Pierre Veinée");
        addBlock(ModBlocks.VEINED_STONE_BRICK_SLAB, "Dalle de Pierre Veinée");
        addBlock(ModBlocks.VEINED_STONE_BRICK_WALL, "Muret de Pierre Veinée");
        addBlock(ModBlocks.RESONANCE_LAMP, "Lampe de Résonance");
        addBlock(ModBlocks.CONDUIT_LINE, "Conduit de Résonance");
        addBlock(ModBlocks.VEINED_STONE_COLUMN, "Colonne de Pierre Veinée");
        addBlock(ModBlocks.DISSONANCE_BLOOM, "Efflorescence de Dissonance");
        addBlock(ModBlocks.RESONANCE_BULKHEAD, "Sas de Résonance");
        addBlock(ModBlocks.ANCIENT_EMITTER, "Émetteur Ancien");
        addBlock(ModBlocks.SIGMA_CONSOLE, "Console Sigma");
        addBlock(ModBlocks.ARCHIVE_PEDESTAL, "Socle d'Archive");
        add("block.veskorius.archive_pedestal.solved", "L'ordre tient — quelque chose s'éveille plus bas");
        addBlock(ModBlocks.DAMAGED_RELAY, "Relais Endommagé");
        add("block.veskorius.damaged_relay.restored", "Le relais se remet à bourdonner");
        add("block.veskorius.damaged_relay.already", "Ce relais tourne déjà");
        add("block.veskorius.damaged_relay.no_field", "Un relais rediffuse — il lui faut un champ à relayer");

        // Châssis de palier : la base de craft et de texture des machines.
        addBlock(ModBlocks.FRACTURED_CHASSIS, "Châssis Fracturé");
        addBlock(ModBlocks.ATTUNED_CHASSIS, "Châssis Accordé");
        addBlock(ModBlocks.VESKORIAN_CHASSIS, "Châssis Veskorien");

        // Objets.
        addItem(ModItems.RAW_RESONANCE_CRYSTAL, "Cristal de Résonance Brut");
        addItem(ModItems.STABLE_RESONANCE_CRYSTAL, "Cristal de Résonance Stable");
        addItem(ModItems.REFINED_RESONANCE_CRYSTAL, "Cristal de Résonance Raffiné");
        addItem(ModItems.RESONANCE_COMPONENT, "Composant de Résonance");
        addItem(ModItems.RESONANCE_DUST, "Poussière de Résonance");

        // Indices d'onboarding (ItemHintHandler) : la boucle T1 apprise depuis l'objet.
        add("item.veskorius.raw_resonance_crystal.hint", "Instable. Stabilisez-le avec du quartz dans un Stabilisateur de Résonance.");
        add("item.veskorius.stable_resonance_crystal.hint", "Alimente un Émetteur de Champ, ou raffinez-le dans un Purificateur de Flux.");
        add("item.veskorius.resonance_component.hint", "Pièce maîtresse des machines de Palier 2 et des batteries portables.");
        add("item.veskorius.resonance_dust.hint", "Un broyage rapide de cristal brut. Alimente l'Assembleur de Composants.");
        addItem(ModItems.VESKORIAN_ALLOY_INGOT, "Lingot d'Alliage Veskorien");
        addItem(ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT, "Lingot d'Alliage Conducteur");
        addItem(ModItems.FLUX_SLAG, "Scorie de Flux");
        addItem(ModItems.SYNTHESIS_RESIDUE, "Résidu de Synthèse");
        addItem(ModItems.CONCENTRATED_FLUX, "Flux Concentré");
        addItem(ModItems.HYPER_REFINED_CRYSTAL, "Cristal Hyper-Raffiné");
        addItem(ModItems.HARMONIC_LATTICE, "Treillis Harmonique");
        addItem(ModItems.VESKORIAN_ALLOY_SWORD, "Épée en Alliage Veskorien");
        addItem(ModItems.VESKORIAN_ALLOY_PICKAXE, "Pioche en Alliage Veskorien");
        addItem(ModItems.VESKORIAN_ALLOY_HELMET, "Casque en Alliage Veskorien");
        addItem(ModItems.VESKORIAN_ALLOY_CHESTPLATE, "Plastron en Alliage Veskorien");
        addItem(ModItems.VESKORIAN_ALLOY_LEGGINGS, "Jambières en Alliage Veskorien");
        addItem(ModItems.VESKORIAN_ALLOY_BOOTS, "Bottes en Alliage Veskorien");
        addItem(ModItems.RIFT_WARD_PLATE, "Plastron Anti-Faille");
        addItem(ModItems.RIFT_ESSENCE, "Essence de Faille");
        addItem(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT, "Lingot d'Alliage Corrompu");
        addBlock(ModBlocks.VESKORIAN_ALLOY_BLOCK, "Bloc d'Alliage Veskorien");
        addBlock(ModBlocks.SYNTHESIS_RESIDUE_BLOCK, "Résidu de Synthèse Compressé");
        add("item.veskorius.veskorian_alloy_ingot.hint", "Métal structurel du Palier 3. Forgez de l'or au lieu du fer pour la variante conductrice.");
        add("item.veskorius.flux_slag.hint", "Déchet de forge. La substance même qui, à l'échelle d'une région, a causé l'Effondrement.");
        addItem(ModItems.RAW_FLUX_DEPOSIT, "Dépôt de Flux Brut");
        addItem(ModItems.RESONANCE_CATALYST_CORE, "Cœur Catalyseur de Résonance");
        addItem(ModItems.RESONANCE_TUNER, "Accordeur de Résonance");
        addItem(ModItems.RESONANCE_STORAGE_CELL, "Cellule de Stockage de Résonance");
        addItem(ModItems.RESONANCE_LOCATOR, "Localisateur de Résonance");
        addItem(ModItems.RESONANCE_SPORE, "Spore de Résonance");
        addItem(ModItems.RESONANCE_SLUDGE, "Boue de Résonance");
        addItem(ModItems.CRYSTAL_STRIDER_SPAWN_EGG, "Œuf d'apparition de Fileur de Cristal");
        addItem(ModItems.RESONANCE_BLUEPRINT, "Plan de Résonance");
        addItem(ModItems.CODEX_FRAGMENT, "Fragment de Codex");
        addItem(ModItems.FOSSILIZED_RATION, "Ration Fossilisée");
        addItem(ModItems.CUSTODE_ALLOY_FRAGMENT, "Fragment d'Alliage de Custode");
        addItem(ModItems.CUSTODE_SPAWN_EGG, "Œuf d'apparition de Custode");
        addItem(ModItems.CUSTODE_LOURD_SPAWN_EGG, "Œuf d'apparition de Custode Lourd");

        // Entités.
        add("entity.veskorius.crystal_strider", "Fileur de Cristal");
        add("entity.veskorius.custode", "Custode");
        add("entity.veskorius.custode_lourd", "Custode Lourd");
        add("entity.veskorius.rift_guardian", "Gardien de Faille");
        add("entity.veskorius.custode_archiviste", "Custode Archiviste");

        // Accordeur de Résonance.
        add("gui.veskorius.tuner_rotate", "Pivoter la machine");
        add("gui.veskorius.tuner_power", "Marche/Arrêt");
        add("gui.veskorius.tuner_overheat", "Basculer la surchauffe");
        add("gui.veskorius.tuner_redstone", "Cycle du mode redstone");
        add("gui.veskorius.tuner_attune", "Accorder la bande harmonique");

        // Harmoniques : les bandes sont des COULEURS côté joueur (12-UX).
        add("gui.veskorius.band.fundamental", "Fondamentale (violet)");
        add("gui.veskorius.band.median", "Médiane (cyan)");
        add("gui.veskorius.band.high", "Haute (ambre)");
        add("gui.veskorius.band.universal", "Universelle (toute bande)");
        add("item.veskorius.resonance_tuner.attuned", "Accordée sur : %s");
        add("item.veskorius.resonance_tuner.no_band", "Cette machine n'a pas de bande harmonique");

        // HUD de champ (12-UX) : visible en portant le Locator (inventaire ou Curios).
        add("gui.veskorius.hud.osc", "%s / %s Osc");
        add("gui.veskorius.hud.dissonance", "Dissonance");
        add("gui.veskorius.hud.unstable", "Champ instable");
        add("item.veskorius.resonance_locator.hud_hint",
            "Portez-le pour lire le champ où vous vous tenez");

        // Décharge de résonance (06 A6) : message de mort de lore.
        add("death.attack.veskorius.resonance_discharge",
            "%1$s a été déchiqueté par une décharge de résonance");
        add("death.attack.veskorius.resonance_discharge.player",
            "%1$s a été déchiqueté par une décharge de résonance en affrontant %2$s");
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
        add("gui.veskorius.locator.type_structure", "Signature de structure");
        add("gui.veskorius.locator.no_structure", "Aucune signature de structure à portée");
        add("gui.veskorius.locator.mode_resources", "Ressources");
        add("gui.veskorius.locator.mode_structures", "Structures");
        add("item.veskorius.resonance_locator.mode", "Mode : %s");
        add("item.veskorius.resonance_locator.mode_hint", "Maj + clic droit pour changer de mode");
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
        add("codex.veskorius.daily_life.market.title", "Registre — jour de marché");
        add("codex.veskorius.daily_life.market.text",
            "Échangé deux fioles de lumière stable contre toute l'étagère du potier. Le champ atteignait les "
                + "derniers étals aujourd'hui ; même le vieux Merek a gardé ses lampes allumées après le "
                + "crépuscule. Bons présages, disait le crieur.");
        add("codex.veskorius.daily_life.children.title", "Ardoise — d'une main d'enfant");
        add("codex.veskorius.daily_life.children.text",
            "Maman dit de ne pas toucher les pierres qui chantent. J'en ai touché une quand même. Elle était "
                + "tiède, et elle m'a répondu. Je ne lui ai pas dit.");
        add("codex.veskorius.daily_life.festival.title", "Avis — l'Attunement");
        add("codex.veskorius.daily_life.festival.text",
            "Troisième cloche, le quartier se rassemble. Les Architectes vont réveiller la grande tour et "
                + "toutes les lampes s'embraseront d'un coup. N'apportez rien de métal qui vous soit cher ; "
                + "les anciens disent que ça attire.");

        // L'Archive : quatre cotes, à lire dans l'ordre (c'est la serrure).
        add("codex.veskorius.archive.log_1.title", "Cote I — la mesure");
        add("codex.veskorius.archive.log_1.text",
            "Première des quatre. On nous demande de consigner, pas de conclure. Donc : sur les "
            + "onze secteurs, le champ ne rend plus ce qu'on y met. Il rend davantage. "
            + "L'excédent est faible. L'excédent est partout.");
        add("codex.veskorius.archive.log_2.title", "Cote II — la somme");
        add("codex.veskorius.archive.log_2.text",
            "Deuxième des quatre. Deux champs superposés ne s'additionnent pas — nous "
            + "l'enseignons à chaque apprenti depuis deux cents ans. À l'échelle d'une région, "
            + "ils font autre chose. Nous n'avons pas encore de mot pour ça. Il aurait fallu.");
        add("codex.veskorius.archive.log_3.title", "Cote III — la réponse");
        add("codex.veskorius.archive.log_3.text",
            "Troisième des quatre. L'Assemblée a lu la mesure et voté l'extension du réseau. "
            + "Son raisonnement se tient, et repose entièrement sur l'hypothèse que nous étions "
            + "venus mettre en doute. Je dois classer ceci et ne rien ajouter.");
        add("codex.veskorius.archive.log_4.title", "Cote IV — la déchirure");
        add("codex.veskorius.archive.log_4.text",
            "Quatrième des quatre. Quelque chose s'est ouvert cette nuit dans la ligne profonde, "
            + "et ne s'est pas refermé. Ce n'est pas un trou dans la roche. La roche est toujours "
            + "là. C'est un trou dans l'endroit où la roche est. Qui que vous soyez, lisez-les "
            + "dans l'ordre. Cela compte.");
        add("codex.veskorius.outpost.log_1.title", "Journal d'opérateur — premier quart");
        add("codex.veskorius.outpost.log_1.text",
            "Console propre sur les trois bandes. Le secteur ronronne comme il doit. "
            + "Marran dit que la ligne profonde dérive encore ; je l'ai consigné. Troisième fois "
            + "cette saison. On nous répondra que c'est dans les tolérances. C'est dans les tolérances.");
        add("codex.veskorius.outpost.log_2.title", "Journal d'opérateur — la dérive");
        add("codex.veskorius.outpost.log_2.text",
            "Ce n'est pas dans les tolérances. La bande ne tient plus où je la pose. Je réaccorde "
            + "à l'aube et le soir elle a bougé, toujours dans le même sens, toujours vers les "
            + "autres. J'ai demandé un second avis. L'Archive répond que le réseau se corrige "
            + "seul. Le réseau se corrige vers quelque chose.");
        add("codex.veskorius.outpost.log_3.title", "Journal d'opérateur — la nuit où il a chanté");
        add("codex.veskorius.outpost.log_3.text",
            "Tous les émetteurs d'ici à la côte sont tombés sur la même bande en même temps. "
            + "Personne ne l'avait ordonné. Une nuit durant, le réseau entier a sonné comme une "
            + "cloche frappée et les lampes ont brûlé blanc. C'était, et je l'écris sans détour, "
            + "beau. Au matin, la pierre au-dessus de la galerie ouest était fendue de part en part.");
        add("codex.veskorius.outpost.log_4.title", "Journal d'opérateur — dernière entrée");
        add("codex.veskorius.outpost.log_4.text",
            "Ordre est venu d'éteindre le secteur. Je n'ai pas pu. Éteindre suppose que le réseau "
            + "consente, et il ne répond plus aux demandes — seulement au ton. "
            + "Je laisse la console vivante et les scellés ouverts. À qui lira ceci : elle écoute "
            + "encore. Ce n'est pas un avertissement. C'est la seule raison pour laquelle vous "
            + "pourrez la rallumer.");
        add("codex.veskorius.custode.watch.title", "Custode — ordre permanent");
        add("codex.veskorius.custode.watch.text",
            "Je ne dors pas. Je ne chasse pas. Je tiens cette porte jusqu'au retour des faiseurs. Si tu prends "
                + "aux morts, ne t'étonne pas que les morts répondent.");

        // Advancements.
        add("advancements.veskorius.tier1_awakening.title", "L'Éveil");
        add("advancements.veskorius.tier1_awakening.description", "Ramasser un Cristal de Résonance Brut");
        add("advancements.veskorius.find_dwelling.title", "Quelqu'un vivait ici");
        add("advancements.veskorius.find_dwelling.description",
            "Entrer dans une Habitation Modeste. Le mobilier est encore en place — ils sont partis vite.");
        add("advancements.veskorius.find_outpost.title", "La machine morte");
        add("advancements.veskorius.find_outpost.description",
            "Atteindre un Avant-poste. Quelque chose, sous les gravats, écoute encore.");
        add("advancements.veskorius.first_chassis.title", "Châssis récupéré");
        add("advancements.veskorius.first_chassis.description",
            "Fabriquer un Châssis Fracturé. Toutes vos machines commenceront par là.");
        add("advancements.veskorius.first_field.title", "Sans un fil");
        add("advancements.veskorius.first_field.description",
            "Poser un Field Emitter. L'énergie passe désormais par l'air — et pas plus loin.");
        add("advancements.veskorius.first_strider.title", "Quelque chose pousse encore");
        add("advancements.veskorius.first_strider.description",
            "Récolter un Resonance Spore. Les Fileurs de Cristal le suivront n'importe où.");
                add("advancements.veskorius.tier3_relay.title", "Alliage");
        add("advancements.veskorius.tier3_relay.description",
            "Le Sigma Laboratory a rendu son plan. Le métal prend la Résonance — et laisse de la scorie.");
        add("advancements.veskorius.tier4_amplifier.title", "Synthèse profonde");
        add("advancements.veskorius.tier4_amplifier.description",
            "L'Archive Régionale s'est ouverte. Trois cristaux, deux chantiers, une décision.");
        add("advancements.veskorius.tier5_rift.title", "Pierre tordue");
        add("advancements.veskorius.tier5_rift.description",
            "Vous avez reconnu les fissures. Quelque chose, en dessous, n'a jamais cessé de résonner.");
        add("advancements.veskorius.rift_guardian_slain.title", "Il montait la garde");
        add("advancements.veskorius.rift_guardian_slain.description",
            "La Faille est stable, et plus personne ne la gardera.");
add("advancements.veskorius.tier2_field.title", "Réseau court");
        add("advancements.veskorius.tier2_field.description", "Restaurer le plan du champ à une console d'Avant-poste");

        // --- Codex de Résonance (15-Codex-Guidebook.md) ---
        addItem(ModItems.RESONANCE_CODEX, "Codex de Résonance");
        add("item.veskorius.resonance_codex.hint", "Clic droit pour ouvrir. Il s'écrit tout seul au fil de votre progression.");
        add("gui.veskorius.codex.title", "Codex de Résonance");
        add("gui.veskorius.codex.discovered", "%s / %s découvert(e)s");
        add("gui.veskorius.codex.total", "%s / %s connues");
        add("gui.veskorius.codex.back", "‹ Retour");
        add("gui.veskorius.codex.new_entry", "Nouvelle entrée du Codex : %s");
        add("gui.veskorius.codex.locked", "Verrouillé.");
        add("gui.veskorius.codex.locked_item", "Verrouillé — obtenir : %s");
        add("gui.veskorius.codex.locked_advancement", "Verrouillé — progressez pour révéler cette page.");
        add("gui.veskorius.codex.locked_fragment", "Verrouillé — lisez le Fragment de Codex correspondant pour révéler cette page.");

        add("codex.category.intro", "Introduction");
        add("codex.category.crystals", "Cristaux & Raffinage");
        add("codex.category.fields", "Champs & Énergie");
        add("codex.category.machines", "Machines");
        add("codex.category.world", "Monde & Structures");
        add("codex.category.fauna", "Faune");
        add("codex.category.gear", "Équipement");
        add("codex.category.lore", "Lore");
        add("codex.category.progression", "Progression");

        add("codex.veskorius.intro.welcome.title", "Le Codex de Résonance");
        add("codex.veskorius.intro.welcome.text", "Votre propre savoir, restauré. Il s'écrit tout seul à mesure que vous relevez ce que l'Effondrement a défait — de nouvelles pages apparaissent quand vous découvrez cristaux, machines, lieux et créatures.");
        add("codex.veskorius.intro.using_codex.title", "Lire le Codex");
        add("codex.veskorius.intro.using_codex.text", "Choisissez une catégorie à gauche, puis une entrée. Les pages verrouillées s'affichent « ??? » — ouvrez-en une quand même pour apprendre comment la débloquer. Le Codex est le vôtre : il se remplit même rangé dans un coffre.");
        add("codex.veskorius.intro.getting_started.title", "Bien démarrer");
        add("codex.veskorius.intro.getting_started.text", "1) Minez un Cristal de Résonance Brut sous terre. 2) Posez un Stabilisateur et transformez-le en Cristal Stable avec du quartz. 3) Posez un Assembleur de Composants. 4) Trouvez un Avant-poste, réveillez sa console pour le plan du Champ, et dressez votre premier Émetteur de Champ.");

        add("codex.veskorius.crystals.raw.title", "Cristal de Résonance Brut");
        add("codex.veskorius.crystals.raw.text", "Miné dans les poches de cristal souterraines. Instable seul — stabilisez-le avec du quartz dans un Stabilisateur, ou broyez-le en poussière.");
        add("codex.veskorius.crystals.stable.title", "Cristal de Résonance Stable");
        add("codex.veskorius.crystals.stable.text", "Le pilier du début de partie : carburant des Émetteurs de Champ, entrée des Composants, et le cristal que l'on raffine ensuite.");
        add("codex.veskorius.crystals.refined.title", "Cristal de Résonance Raffiné");
        add("codex.veskorius.crystals.refined.text", "Purifié dans un Purificateur de Flux. Une charge plus dense et plus pure, utilisée par les recettes de tier supérieur et le Cœur Catalyseur.");
        add("codex.veskorius.crystals.dust.title", "Poussière de Résonance");
        add("codex.veskorius.crystals.dust.text", "Un broyage rapide de cristal brut — plus vite que la stabilisation, mais sans Cristal Stable. Alimente la recette alternative de l'Assembleur.");
        add("codex.veskorius.crystals.pockets.title", "Poches de Cristal");
        add("codex.veskorius.crystals.pockets.text", "Les cristaux poussent en petites poches, loin sous terre, enveloppées d'une coquille de Pierre Veinée de Résonance. Repérez la pierre veinée : une poche est proche. Certaines parois portent une croûte de flux à brosser.");

        add("codex.veskorius.fields.osc.title", "L'Osc & les Champs");
        add("codex.veskorius.fields.osc.text", "L'Osc est l'énergie de résonance. Aucun câble : un Émetteur remplit un champ autour de lui, et toute machine à l'intérieur y puise ce qu'il lui faut. Une machine qui tourne rayonne ; une machine qui reste éteinte dans un champ n'a pas d'énergie à puiser.");
        add("codex.veskorius.fields.emitter.title", "Émetteur de Champ");
        add("codex.veskorius.fields.emitter.text", "Projette un champ de résonance (portée 8) en brûlant des Cristaux Stables. Les machines à l'intérieur y puisent — sans câbles. Son dôme de particules montre la portée.");
        add("codex.veskorius.fields.storage_cell.title", "Cellule de Stockage de Résonance");
        add("codex.veskorius.fields.storage_cell.text", "Une batterie portable. Se recharge dans un champ, jusqu'à 8000 Osc, et alimente le Localisateur loin de la base.");
        add("codex.veskorius.fields.locator.title", "Localisateur de Résonance");
        add("codex.veskorius.fields.locator.text", "Pointe vers la résonance la plus proche — poche de cristal ou champ actif. Courte portée, sur une petite charge rechargée dans un champ ou depuis une Cellule.");

        // --- Interface et messages non traduits ---------------------------
        //
        // Les libellés du Codex lui-même — la boîte de recherche, « Ensuite », « Entrée
        // scellée », la notification de déblocage — restaient en anglais dans une
        // interface entièrement française. Plus visibles encore que les entrées, parce
        // qu'on les lit à chaque ouverture du livre.
        add("gui.veskorius.codex.machine_note", "%s s · %s Osc/tick");
        add("gui.veskorius.codex.next", "Ensuite : %s →");
        add("gui.veskorius.codex.no_results", "Rien ici pour l'instant.");
        add("gui.veskorius.codex.recipe", "Recette");
        add("gui.veskorius.codex.recipe_shaped", "Recette (avec forme)");
        add("gui.veskorius.codex.results", "%s résultat(s)");
        add("gui.veskorius.codex.sealed", "Entrée scellée");
        add("gui.veskorius.codex.search", "Rechercher…");
        add("gui.veskorius.codex.tier_intro", "Début");
        add("gui.veskorius.codex.toast", "Nouvelle entrée du Codex");
        add("gui.veskorius.codex.tree", "Progression");
        add("gui.veskorius.tuner_calibrate", "Recalibrer");
        add("gui.veskorius.tuner_priority", "Définir la priorité");
        add("item.veskorius.resonance_tuner.no_component", "La recalibration demande 1 Composant de Résonance");
        add("item.veskorius.resonance_tuner.no_priority", "Seules les machines ont une priorité");
        add("item.veskorius.resonance_tuner.recalibrated", "Recalibré (1 Composant de Résonance consommé)");
        add("item.veskorius.rift_ward_plate.hint", "Immunité totale au déphasage. Coûte 10 % de vitesse de minage tant qu'il est porté.");
        add("message.veskorius.amplifier_status", "Amplificateur : portée %s, maillon %s/%s, calibration %s %%");
        add("message.veskorius.hub_status", "Concentrateur : délestage sous %s, calibration %s %%");
        add("message.veskorius.priority_set", "Priorité : %s");
        add("message.veskorius.relay_charge", "Relais : %s/%s Osc");
        add("message.veskorius.rift_not_cleared", "La Faille a encore son gardien. Rien ne peut en être tiré.");
        add("message.veskorius.slag_vent_status", "Évent : %s forge(s) vidée(s) à la dernière passe");

        // --- Codex : T3, T4, T5 -------------------------------------------
        //
        // VINGT-CINQ ENTRÉES N'AVAIENT AUCUN TEXTE FRANÇAIS. Tout le T3, tout le T4,
        // tout le T5 : en jeu en français, le manuel affichait des clés brutes sur la
        // moitié des machines — c'est-à-dire exactement sur la partie du mod qu'on ne
        // peut pas deviner sans lire. Le test everyCodexEntryHasRealText ne l'a pas vu :
        // il tourne sur la langue active du serveur de test, qui est l'anglais.
        add("codex.veskorius.crystals.alloy.title", "Alliage Veskorien");
        add("codex.veskorius.crystals.alloy.text",
            "La première matière que vous FABRIQUEZ au lieu de la raffiner. La chaîne T1-T2 partait du cristal et le purifiait ; celle-ci part du métal et l'allie. C'est ce changement de nature qui marque le palier, pas le nombre d'étapes.\n\nLa variante conductrice, c'est la même forge avec de l'or au lieu du fer. Ne les mélangez pas : le Relais et le Treillis Harmonique n'acceptent que la conductrice.");
        add("codex.veskorius.crystals.essence.title", "Essence de Faille");
        add("codex.veskorius.crystals.essence.text",
            "Six par Faille, et la Faille est finie. Aucune machine n'en produit davantage, et il n'y en aura pas — c'est une décision, pas un oubli.\n\nDeux des six partent dans l'Émetteur de Garde qui rend le site exploitable, donc la première Faille que vous purgerez vous en rendra réellement quatre. Ce que vous en faites est le dernier vrai choix que le mod vous propose.");
        add("codex.veskorius.crystals.hyper.title", "Cristal Hyper-Raffiné");
        add("codex.veskorius.crystals.hyper.text",
            "Le quatrième état du cristal. Il ne se mine pas, ne se trouve pas, ne se fabrique pas à l'établi : la Chambre de Synthèse Profonde en est la seule source, et bâtir cette Chambre en dépense un.\n\nLes trois de l'Archive Régionale sont tout le stock de départ du palier : deux vont dans votre premier Treillis Harmonique, le troisième dans la Chambre. Vous ne pouvez pas faire les deux, et ce choix EST le palier 4. Une fois la Chambre debout, la ressource devient renouvelable et la pression retombe.");
        add("codex.veskorius.crystals.slag.title", "Scorie de Flux");
        add("codex.veskorius.crystals.slag.text",
            "Le déchet de la Forge, et il n'est pas inerte. Chimiquement, c'est la substance qui, à l'échelle d'une région, a provoqué l'Effondrement.\n\nUne sort à chaque cycle de forge, dans son propre slot, et un slot plein ARRÊTE la Forge. Videz-le à la main, ou posez un Évent à Scorie qui le fera pour vous. Vous reproduisez la cause de l'Effondrement en miniature, et elle stoppera votre production si vous l'ignorez. C'est la leçon voulue.");
        add("codex.veskorius.fauna.guardian.title", "Gardien de Faille");
        add("codex.veskorius.fauna.guardian.text",
            "Jamais une rencontre au hasard : il se lève de la première Ancre de Faille qui tient, une fois par Faille, et plus jamais.\n\nTrois phases, et chacune demande autre chose. En ÉCHO il recule quand vous approchez — il faut le poursuivre. En RUPTURE il ouvre le sol sous vos pieds. En STABILISATION il marche vers le cœur et se soigne s'il l'atteint : il faut donc tenir le centre, là où la Faille est la pire.\n\nLe vaincre stabilise la Faille définitivement — les dégâts de phase cessent même ancre éteinte — et ouvre l'extraction. Butin garanti : trois Lingots d'Alliage Veskorien Corrompu.");
        add("codex.veskorius.fields.calibration.title", "Calibration et dérive");
        add("codex.veskorius.fields.calibration.text",
            "Amplificateurs et Concentrateurs perdent 1 % d'efficacité par jour Minecraft de fonctionnement, jusqu'à −30 % et pas au-delà. Un amplificateur négligé porte moins loin qu'avant ; un Concentrateur négligé délestera plus tôt qu'il ne devrait.\n\nLe remède est le même geste que pour tout ce qui dérive dans ce monde : un Accordeur de Résonance en mode Recalibrer, au prix d'un Composant de Résonance.\n\nLe plancher compte. Rien ici ne cesse jamais de fonctionner : de l'entretien, jamais un mur.");
        add("codex.veskorius.fields.lattice.title", "Treillis Harmonique");
        add("codex.veskorius.fields.lattice.text",
            "Quatre lingots d'alliage CONDUCTEUR et deux Cristaux Hyper-Raffinés. La branche de métal choisie à la Forge se paie une seconde fois, un palier plus tard.\n\nDeux choses seulement réclament un Treillis : l'Amplificateur Harmonique et le Cœur de Convergence. C'est le réseau T4 en un objet.");
        add("codex.veskorius.machines.amplifier.title", "Amplificateur Harmonique");
        add("codex.veskorius.machines.amplifier.text",
            "Double la portée du champ qu'il REÇOIT, pour 2 Osc par tick. Derrière un émetteur T2, cela fait 16 blocs ; derrière un Relais, 40. Un Relais porte une portée fixe, celui-ci en multiplie une.\n\nTrois maillons au plus. Passé le troisième, un amplificateur transporte encore le champ mais cesse de le doubler.\n\nIl dérive : 1 % d'efficacité par jour Minecraft de fonctionnement, jusqu'à un plancher de −30 %. Clic droit avec un Accordeur en mode Recalibrer et un Composant de Résonance pour le remettre à neuf. La dérive ne mange que le GAIN, jamais la portée reçue — un amplificateur n'est jamais pire que pas d'amplificateur.");
        add("codex.veskorius.machines.chamber.title", "Chambre de Synthèse Profonde");
        add("codex.veskorius.machines.chamber.text",
            "Deux Cristaux Raffinés deviennent un Cristal Hyper-Raffiné en 90 secondes, à 8 Osc par tick. Rien d'autre au monde n'en produit.\n\nLa bâtir CONSOMME un Cristal Hyper-Raffiné, qui devient son catalyseur permanent et ne réapparaît jamais comme entrée de cycle. L'Archive Régionale vous en donne exactement trois : deux pour votre premier Treillis Harmonique, le troisième ici. Vous ne pouvez pas faire les deux — ce choix est le palier.\n\nElle accepte la surchauffe : moitié moins de temps, deux fois plus d'Osc, et un cycle sur cinq perd son entrée.");
        add("codex.veskorius.machines.compressor.title", "Compresseur de Flux");
        add("codex.veskorius.machines.compressor.text",
            "Quatre Cristaux Raffinés deviennent un Flux Concentré en 30 secondes, à 6 Osc par tick. Une perte apparente, et voulue.\n\nLe Flux Concentré a exactement UN consommateur dans tout le mod : le Cœur de Convergence. Posez un Compresseur quand vous avez décidé de le bâtir, pas avant — il n'y a rien d'autre à faire de sa production.");
        add("codex.veskorius.machines.convergence_core.title", "Cœur de Convergence");
        add("codex.veskorius.machines.convergence_core.text",
            "Le seul multi-bloc du mod, et la seule source de champ plus forte que toutes les autres.\n\nPosé seul, il est inerte. Il lui faut HUIT Relais de Résonance ou Amplificateurs Harmoniques (le mélange est permis) à exactement 5 blocs — les quatre axes et les quatre coins d'un anneau de 11 blocs — et chacun doit voir le centre à découvert. On ne l'enferme pas.\n\nUne fois formé, il émet une portée de 40 à intensité maximale pour 12 Osc par tick. Il est revérifié toutes les deux secondes : murez une seule ligne de vue et il s'éteint.\n\nIl existe surtout pour alimenter une Ancre de Faille sans y consacrer une base entière de relais.");
        add("codex.veskorius.machines.driller.title", "Foreuse à Cristaux Profonds");
        add("codex.veskorius.machines.driller.text",
            "Récolte les amas de cristal dans la colonne de 5×5 sous elle, un toutes les 20 secondes, à 6 Osc par tick. Elle n'atteint que sous Y −40 — en poser une plus haut ne fait strictement rien.\n\nElle prend les amas et laisse la roche : la galerie reste intacte et vous voyez exactement ce qui a été pris. Elle épuise aussi son filon. Quand la colonne est vide, elle s'arrête, et vous la déplacez.");
        add("codex.veskorius.machines.extraction_array.title", "Réseau d'Extraction Automatisé");
        add("codex.veskorius.machines.extraction_array.text",
            "Commande toutes les Foreuses à Cristaux Profonds dans un rayon de 12 blocs : il vide leur sortie dans son propre coffre, et les foreuses qu'il commande tournent DEUX FOIS plus vite.\n\nIl répond à la corvée que la Foreuse crée. Avec cinq foreuses au fond d'une mine, marcher jusqu'à chacune est l'essentiel de ce que le palier vous demande. 10 Osc par tick pour cesser de le faire.");
        add("codex.veskorius.machines.forge.title", "Forge à Alliage Veskorien");
        add("codex.veskorius.machines.forge.text",
            "Deux Cristaux Raffinés et deux lingots donnent un lingot d'alliage en 20 secondes. Le métal que vous lui donnez décide de la branche : le FER donne l'alliage structurel, l'OR l'alliage conducteur — et seul le conducteur entre dans un Relais. Ils ne se remplacent pas.\n\nChaque cycle laisse une Scorie de Flux dans son slot, et un slot plein arrête la Forge.");
        add("codex.veskorius.machines.hub.title", "Concentrateur de Réseau");
        add("codex.veskorius.machines.hub.text",
            "Décide qui s'arrête quand un champ vient à manquer. Sans lui, les machines sont servies dans l'ordre où elles tickent, et une base sous-alimentée bégaie partout à la fois sans que rien ne vous dise ce que vous avez perdu.\n\nLe Concentrateur déleste par le bas : au-dessus de la moitié de réserve tout le monde tourne, entre un cinquième et la moitié les machines BASSES se taisent, en dessous seules les HAUTES sont servies. Réglez la priorité d'une machine à l'Accordeur.\n\nIl est passif et ne coûte rien — taxer un arbitre au moment précis où il n'y a plus d'énergie serait absurde.");
        add("codex.veskorius.machines.reclaimer.title", "Récupérateur");
        add("codex.veskorius.machines.reclaimer.text",
            "Renvoie les déchets dans l'économie. Quatre Scories de Flux donnent un gravier ; quatre Boues de Résonance donnent une Poussière de Résonance. 20 secondes, 4 Osc par tick.\n\nLe taux est délibérément mauvais. Recycler doit rester moins rentable que miner, sinon la boucle remplacerait l'exploration au lieu de la prolonger. Ce que vous achetez ici, ce n'est pas du rendement — c'est de ne plus avoir à jeter.\n\nIl ne remplace pas l'Évent à Scorie. L'Évent ne demande que d'exister ; le Récupérateur veut un champ, un cycle et de la place. Qui veut se débarrasser de la scorie sans y penser continue de venter. Qui veut la récupérer paie l'infrastructure.\n\nAvant lui, la Scorie de Flux ne pouvait qu'être détruite, et la Boue de Résonance n'allait nulle part.");
        add("codex.veskorius.machines.relay.title", "Relais de Résonance");
        add("codex.veskorius.machines.relay.text",
            "Porte un champ 20 blocs plus loin, et les relais se chaînent. Il consomme 1 Osc par tick que quelque chose s'en serve ou non — la portée n'est jamais gratuite.\n\nUn relais n'a pas de réserve propre digne de ce nom : il se remplit du champ en amont et sert ce qu'il détient. Il transporte aussi la bande harmonique de sa source, donc en glisser un devant une machine ne réparera pas un montage désaccordé.");
        add("codex.veskorius.machines.rift_anchor.title", "Ancre de Faille");
        add("codex.veskorius.machines.rift_anchor.text",
            "Posée à moins de 12 blocs d'un cœur de Faille, elle stabilise la Faille — et convoque son Gardien, une fois, la première fois qu'elle tient.\n\n20 Osc par tick, en continu : le plus gros appétit du mod, et la raison d'être du Cœur de Convergence.\n\nElle ne tient que TANT QU'elle est alimentée. Coupez le champ et la Faille se réveille. Cassez l'ancre et elle se réveille aussitôt. Il n'y a pas d'interrupteur définitif ici.");
        add("codex.veskorius.machines.rift_extractor.title", "Extracteur de Cœur de Faille");
        add("codex.veskorius.machines.rift_extractor.text",
            "Une Essence de Faille toutes les 120 secondes, à 15 Osc par tick, sur une Faille ancrée ET purgée de son Gardien. Environ une extraction sur sept rend en plus un Lingot d'Alliage Corrompu.\n\nSix extractions par Faille. Ensuite cette Faille est épuisée, définitivement. Le compte vit sur le cœur, donc remplacer l'Extracteur n'y change rien.\n\nL'Essence de Faille est la seule ressource finie de ce mod. Rien ne la régénère.");
        add("codex.veskorius.machines.rift_ward.title", "Émetteur de Garde");
        add("codex.veskorius.machines.rift_ward.text",
            "Contient la corrosion ambiante d'une Faille ancrée dans un rayon de 12 blocs, pour 5 Osc par tick.\n\nAncrer une Faille arrête les dégâts aigus ; ça n'arrête pas la Faille. Ce qui reste ronge votre équipement — un point d'usure sur une pièce portée toutes les 5 secondes. Survivable, et inexploitable : on peut entrer sans Garde, on ne peut pas y travailler.\n\nIl coûte deux Essences de Faille sur les six qu'une Faille rendra jamais. C'est le seul objet du mod payé sur ce qu'il débloque.");
        add("codex.veskorius.machines.slag_vent.title", "Évent à Scorie");
        add("codex.veskorius.machines.slag_vent.text",
            "Évacue une Scorie de Flux toutes les 10 secondes de chaque Forge dans un rayon de 8 blocs. Il existe parce qu'un slot de scorie plein arrête net une Forge.\n\nUne évacuation par passe et par Forge : une batterie de six dépassera un seul Évent. Il coûte du champ en continu, donc se débarrasser des déchets est une ligne permanente de votre budget d'énergie, pas un bouton qu'on presse une fois.");
        add("codex.veskorius.machines.synthesizer.title", "Synthétiseur Structurel");
        add("codex.veskorius.machines.synthesizer.text",
            "Quatre lingots d'alliage et huit pierres donnent quatre Blocs d'Alliage Veskorien en 60 secondes. C'est ce qui rend le palier bâtissable — sans lui, l'alliage reste un matériau d'artisanat.\n\nComme la Forge, il laisse un sous-produit : un Résidu de Synthèse par cycle, dans son propre slot, et un slot plein arrête la machine. Un Évent à Scorie ne l'évacuera PAS. Celui-là reste votre problème — ou celui du Récupérateur.");
        add("codex.veskorius.progression.tier3.title", "Palier 3 — L'alliage");
        add("codex.veskorius.progression.tier3.text",
            "Le palier où l'on cesse de raffiner pour se mettre à FABRIQUER. La Forge à Alliage Veskorien en est la porte, et la console du Sigma Laboratory en détient le plan.\n\nDeux nouveautés, et ce sont deux contraintes. Le métal que vous forgez sépare vos matériaux en deux familles qui ne se remplacent pas. Et chaque machine qui produit la matière du palier produit aussi un déchet qui l'arrêtera si vous l'ignorez.");
        add("codex.veskorius.progression.tier4.title", "Palier 4 — La synthèse profonde");
        add("codex.veskorius.progression.tier4.text",
            "Ouvert par l'Archive Régionale, qui rend son plan et exactement trois Cristaux Hyper-Raffinés.\n\nC'est là que le réseau cesse d'être un ensemble de machines pour devenir quelque chose qui s'administre : des amplificateurs qui portent à l'échelle d'une région, un Concentrateur qui décide qui mange quand il n'y en a pas assez, et une calibration qui se dégrade si vous ne revenez jamais.");
        add("codex.veskorius.progression.tier5.title", "Palier 5 — La Faille");
        add("codex.veskorius.progression.tier5.text",
            "Il ne se débloque en fabriquant rien du tout. On l'ouvre en TROUVANT une Faille, et on en trouve une en reconnaissant la pierre déformée.\n\nTout y est fini. Un Gardien par Faille, six essences par Faille, et aucun moyen d'en produire davantage. Le mod s'arrête où la ressource s'arrête.");
        add("codex.veskorius.world.rift.title", "Lire une Faille");
        add("codex.veskorius.world.rift.text",
            "Une Faille est une bulle de rien, déchirée sous Y −60, avec un cœur qui flotte en son centre. Elle n'est pas bâtie et ce n'est pas une structure — c'est un accident de sur-résonance.\n\nLe Localisateur de Résonance ne peut pas en trouver : une Faille ne rayonne pas un champ, elle le déphase. Le seul signe est la pierre autour, tirée et fendue. Apprenez cette pierre et vous trouverez des Failles. Il n'y a pas d'instrument pour ça.\n\nNon ancré, un cœur blesse tout ce qui se tient à moins de 8 blocs au bout de trois secondes. Trois secondes suffisent pour regarder et reculer. Elles ne suffisent pas pour rester.");

        add("codex.veskorius.machines.stabilizer.title", "Stabilisateur de Résonance");
        add("codex.veskorius.machines.stabilizer.text", "Transforme Cristal Brut + quartz en Cristal Stable. Autonome, aucun champ requis — la première machine que l'on pose.");
        add("codex.veskorius.machines.assembler.title", "Assembleur de Composants");
        add("codex.veskorius.machines.assembler.text", "Combine un Cristal Stable (ou de la poussière) avec du fer en Composants de Résonance. La première machine qui puise dans un champ.");
        add("codex.veskorius.machines.whetstone.title", "Meule de Résonance");
        add("codex.veskorius.machines.whetstone.text", "Répare un outil endommagé d'un quart de sa durabilité, contre un Cristal Stable. Autonome.");
        add("codex.veskorius.machines.purifier.title", "Purificateur de Flux");
        add("codex.veskorius.machines.purifier.text", "Raffine le Stable en Raffiné. Surchauffe optionnelle : deux fois plus vite pour deux fois plus d'Osc, et 20 % de risque de perdre l'entrée.");
        add("codex.veskorius.machines.crusher.title", "Broyeur de Cristaux");
        add("codex.veskorius.machines.crusher.text", "Broie un Cristal Brut en trois Poussières de Résonance en dix secondes. Autonome — une voie T1 rapide.");
        add("codex.veskorius.machines.roost.title", "Perchoir à Cristaux");
        add("codex.veskorius.machines.roost.text", "Production passive : nourri au quartz, il donne du Cristal Brut avec le temps — mais seulement tant qu'un Fileur de Cristal reste à proximité.");
        add("codex.veskorius.machines.tuner.title", "Accordeur de Résonance");
        add("codex.veskorius.machines.tuner.text", "Un outil à modes : pivoter, marche/arrêt, surchauffe, ou cycle redstone sur une machine. Maj + clic droit démonte tout bloc-entité en gardant son contenu.");
        add("codex.veskorius.machines.catalyst_core.title", "Cœur Catalyseur de Résonance");
        add("codex.veskorius.machines.catalyst_core.text", "S'insère dans le slot d'augment de toute machine active pour un +15 % de vitesse permanent. Un seul par machine, jamais consommé.");
        add("codex.veskorius.machines.control.title", "Piloter les machines");
        add("codex.veskorius.machines.control.text", "Toute machine active a trois boutons dans son GUI : un interrupteur manuel, un mode redstone (ignoré / requiert un signal / requiert son absence) et — si supportée — la surchauffe. Couper le courant ou la redstone met la machine en pause et conserve sa progression ; seul un ingrédient manquant la réinitialise. L'Accordeur de Résonance applique ces mêmes bascules dans le monde.");

        add("codex.veskorius.world.veined_stone.title", "Pierre Veinée de Résonance");
        add("codex.veskorius.world.veined_stone.text", "La coquille autour des poches de cristal — la voir, c'est qu'une poche est proche. En faible lumière, un Spore de Résonance pousse sur une face exposée, récolté à la main.");
        add("codex.veskorius.world.flux_deposit.title", "Dépôt de Flux Brut");
        add("codex.veskorius.world.flux_deposit.text", "Une croûte brossable sur les parois des poches. Brossez-la pour le flux ; la miner la détruit. Une alternative au quartz pour le Stabilisateur.");
        add("codex.veskorius.world.outpost.title", "L'Avant-poste");
        add("codex.veskorius.world.outpost.text", "Une ruine enfouie, signalée par une amorce de pierre veinée en surface. Sa Console d'Attunement, une fois réveillée, restaure le plan de Palier 2 — et un Custode garde le site.");

        add("codex.veskorius.fauna.strider.title", "Fileur de Cristal");
        add("codex.veskorius.fauna.strider.text", "Faune souterraine neutre. Clic droit à main nue pour traire un Cristal Brut (avec un délai), et reproduction au Spore de Résonance.");
        add("codex.veskorius.fauna.archiviste.title", "Custode Archiviste");
        add("codex.veskorius.fauna.archiviste.text",
            "L'élite qui garde la salle profonde de l'Archive. 150 PV, 12 de dégâts, et il réagit à dix blocs — bien avant que vous ne le voyiez.\n\nIl marque le sol sous vos pieds et fait détoner la marque une seconde plus tard. Cette seule attaque décide du combat : impossible d'échanger des coups immobile, et la salle de lecture est encombrée de rayonnages. Guettez la flamme, écartez-vous.\n\nL'affronter est facultatif. Sa salle contient deux Cristaux Hyper-Raffinés de plus, ce qui permet exactement de bâtir le premier Amplificateur ET la Chambre de Synthèse, au lieu de choisir entre les deux.");
        add("codex.veskorius.gear.tools.title", "Outils d'alliage");
        add("codex.veskorius.gear.tools.text",
            "Épée et pioche forgées dans l'Alliage Veskorien. La pioche mine tout ce que mine la netherite ; l'épée frappe au niveau du diamant. 1873 utilisations — environ une fois et demie un outil en diamant.\n\nIls se réparent au lingot d'alliage, et la Meule de Résonance leur rend un quart de leur durabilité en huit secondes, sans rien consommer d'autre que du temps et un champ.\n\nL'alliage sort de la Forge Veskorienne, qui produit de la scorie à chaque cycle. Prévoyez un Évent à Scorie avant de prévoir une panoplie.");
        add("codex.veskorius.gear.armor.title", "Armure d'alliage");
        add("codex.veskorius.gear.armor.text",
            "Quatre pièces, protection au niveau du diamant, 33 unités de durabilité par point — sensiblement plus solide que le diamant, en deçà de la netherite.\n\nLa panoplie complète divise par deux les dégâts de phase d'un Gardien de Faille. Pas d'immunité : la moitié. Trois pièces ne donnent rien, donc la panoplie vaut d'être complétée avant de descendre.\n\nSe répare au lingot d'Alliage Veskorien.");
        add("codex.veskorius.gear.ward_plate.title", "Plastron de Garde");
        add("codex.veskorius.gear.ward_plate.text",
            "Un plastron bâti autour de l'Essence de Faille, et la seule chose de Veskorius qui ramène les dégâts de phase à zéro.\n\nLes dégâts de phase sont ceux du Gardien de Faille en phase ÉCHO, et ceux qu'un Cœur de Faille déstabilisé irradie dans un rayon de huit blocs. L'armure ordinaire ne s'y applique pas. L'armure d'alliage les divise par deux. Celui-ci ne divise rien — il les supprime.\n\nIl remplace le plastron d'alliage, donc le porter vous coûte le bonus de panoplie sur les trois autres pièces. C'est tout l'arbitrage : immunité totale à un type de dégâts, ou réduction générale sur tout le reste.");
        add("codex.veskorius.fauna.lourd.title", "Custode Lourd");
        add("codex.veskorius.fauna.lourd.text",
            "Le garde renforcé du Sigma Laboratory et de l'Archive Régionale. 60 PV, 9 de dégâts, réactif à huit blocs — deux de plus que le Custode ordinaire.\n\nTout le reste est le même garde : il patrouille un point, il ne vous poursuit pas hors de son site, et il n'attaque que si vous venez à lui ou si vous cassez une de ses machines. Ce qu'il ajoute, c'est qu'il en appelle un autre. Qu'il prenne une cible, et tous les Custodes Lourds à seize blocs prennent la même.\n\nIls sont postés par paires. C'est là toute la difficulté de ces deux ruines, et la réponse n'est pas d'avoir plus de PV — c'est de ne pas les affronter ensemble. Isolez-en un, ou contournez.\n\nLâche 4 à 7 fragments d'alliage, qui remplacent le lingot de fer dans toute recette Veskorius.");
        add("codex.veskorius.fauna.custode.title", "Custode");
        add("codex.veskorius.fauna.custode.text", "Un gardien réactif posté aux Avant-postes. Il ne frappe qu'à quelques blocs, ou si vous cassez une machine de son site. Lâche des fragments d'alliage — substitut du fer.");

        add("codex.veskorius.progression.tier1.title", "Palier 1 — L'Éveil");
        add("codex.veskorius.progression.tier1.text", "Miner un cristal, le stabiliser, assembler des composants. Tout y est autonome ou fait main. Le Codex s'ouvre avec votre premier cristal.");
        add("codex.veskorius.progression.tier2.title", "Palier 2 — Le Champ");
        add("codex.veskorius.progression.tier2.text", "Réveillez une console d'Avant-poste pour le plan du Champ, puis bâtissez l'Émetteur de Champ et tout ce qu'il alimente : Purificateur, Cellule, Localisateur, Perchoir.");

        assertNoKeyLeftUntranslated();
    }

    /**
     * <b>Toute clé anglaise doit avoir sa contrepartie française.</b>
     *
     * <p>La javadoc de cette classe affirmait déjà que « les clés doivent rester
     * synchronisées avec {@link ModLanguageProvider} ». Rien ne le vérifiait, et
     * <b>quarante-huit clés manquaient</b> : tout le Codex du T3, du T4 et du T5, plus les
     * libellés du livre lui-même — la boîte de recherche, « Ensuite », « Entrée scellée »,
     * la notification de déblocage. En jeu en français, le manuel affichait des clés brutes
     * sur la moitié des machines, c'est-à-dire précisément sur la partie du mod qu'on ne
     * peut pas deviner sans lire.
     *
     * <p><b>Rien ne pouvait l'attraper</b>, et c'est le vrai enseignement. Une clé absente
     * n'est pas une erreur pour Minecraft : il affiche l'identifiant et continue. Le
     * {@code GameTest} qui vérifie que chaque entrée du Codex a un texte réel tourne sur la
     * langue active du serveur de test — l'anglais — donc il passait au vert sur un livre
     * français à moitié vide. Le contrôle doit donc vivre ici, au datagen, où l'échec est
     * bruyant et immédiat.
     *
     * <p>La sonde reconstruit les clés anglaises en rejouant {@code addTranslations} sur une
     * sous-classe qui n'enregistre rien : c'est le seul moyen d'obtenir la liste sans
     * dépendre de l'ordre d'exécution des providers, qui n'est pas garanti.
     */
    private void assertNoKeyLeftUntranslated() {
        java.util.Set<String> english = new java.util.TreeSet<>();
        ModLanguageProvider probe = new ModLanguageProvider(null) {
            @Override
            public void add(String key, String value) {
                english.add(key);
            }
        };
        probe.addTranslations();

        java.util.List<String> missing = english.stream()
            .filter(key -> !hasKey(key))
            .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException(missing.size()
                + " clé(s) anglaise(s) sans traduction française — elles s'afficheront en jeu "
                + "sous forme d'identifiant brut, sans qu'aucune erreur ne soit levée : "
                + missing);
        }
    }

    /** Les clés déjà posées par ce provider. */
    private final java.util.Set<String> keys = new java.util.HashSet<>();

    private boolean hasKey(String key) {
        return keys.contains(key);
    }

    @Override
    public void add(String key, String value) {
        keys.add(key);
        super.add(key, value);
    }
}
