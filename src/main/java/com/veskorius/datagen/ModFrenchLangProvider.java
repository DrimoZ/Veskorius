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

        // Entités.
        add("entity.veskorius.crystal_strider", "Fileur de Cristal");
        add("entity.veskorius.custode", "Custode");

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
        add("codex.veskorius.custode.watch.title", "Custode — ordre permanent");
        add("codex.veskorius.custode.watch.text",
            "Je ne dors pas. Je ne chasse pas. Je tiens cette porte jusqu'au retour des faiseurs. Si tu prends "
                + "aux morts, ne t'étonne pas que les morts répondent.");

        // Advancements.
        add("advancements.veskorius.tier1_awakening.title", "L'Éveil");
        add("advancements.veskorius.tier1_awakening.description", "Ramasser un Cristal de Résonance Brut");
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
        add("codex.veskorius.fauna.custode.title", "Custode");
        add("codex.veskorius.fauna.custode.text", "Un gardien réactif posté aux Avant-postes. Il ne frappe qu'à quelques blocs, ou si vous cassez une machine de son site. Lâche des fragments d'alliage — substitut du fer.");

        add("codex.veskorius.progression.tier1.title", "Palier 1 — L'Éveil");
        add("codex.veskorius.progression.tier1.text", "Miner un cristal, le stabiliser, assembler des composants. Tout y est autonome ou fait main. Le Codex s'ouvre avec votre premier cristal.");
        add("codex.veskorius.progression.tier2.title", "Palier 2 — Le Champ");
        add("codex.veskorius.progression.tier2.text", "Réveillez une console d'Avant-poste pour le plan du Champ, puis bâtissez l'Émetteur de Champ et tout ce qu'il alimente : Purificateur, Cellule, Localisateur, Perchoir.");
    }
}
