package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import com.veskorius.tag.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        buildMachineRecipes(recipeOutput);

        buildChassisRecipes(recipeOutput);

        buildArchitectureRecipes(recipeOutput);

        // Décompactage du bloc d'alliage. Le COMPACTAGE, lui, n'est PAS une recette
        // d'établi : le bloc sort du Structural Synthesizer (05-Machines.md #11), qui
        // exige un champ. Offrir un 9-en-1 à l'établi court-circuiterait la machine —
        // c'est la classe d'erreur qui vide un palier de son contenu sans qu'on s'en
        // aperçoive, parce que la recette « évidente » a l'air inoffensive.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModItems.VESKORIAN_ALLOY_INGOT.get(), 9)
            .requires(ModBlocks.VESKORIAN_ALLOY_BLOCK.get())
            .unlockedBy(getHasName(ModItems.VESKORIAN_ALLOY_INGOT.get()),
                has(ModItems.VESKORIAN_ALLOY_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(
                Veskorius.MOD_ID, "veskorian_alloy_ingot_from_block"));

        // Le residu, dans les deux sens, et A L'ETABLI cette fois.
        //
        // La regle qui interdit le 9-en-1 du bloc d'alliage ne s'applique pas ici : la
        // compresser a l'etabli court-circuiterait le Structural Synthesizer, alors que
        // le residu SORT du Synthesizer. Il n'y a aucune machine a contourner — c'est le
        // dechet de la machine, pas son produit.
        net.minecraft.data.recipes.ShapedRecipeBuilder
            .shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SYNTHESIS_RESIDUE_BLOCK.get())
            .pattern("RRR").pattern("RRR").pattern("RRR")
            .define('R', ModItems.SYNTHESIS_RESIDUE.get())
            .unlockedBy(getHasName(ModItems.SYNTHESIS_RESIDUE.get()),
                has(ModItems.SYNTHESIS_RESIDUE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(
                Veskorius.MOD_ID, "synthesis_residue_block"));

        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModItems.SYNTHESIS_RESIDUE.get(), 9)
            .requires(ModBlocks.SYNTHESIS_RESIDUE_BLOCK.get())
            .unlockedBy(getHasName(ModItems.SYNTHESIS_RESIDUE.get()),
                has(ModItems.SYNTHESIS_RESIDUE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(
                Veskorius.MOD_ID, "synthesis_residue_from_block"));

        // --- Machines T1 : châssis Fracturé + ce qui les distingue -------------
        //
        // La grammaire de fabrication est désormais « le boîtier de mon palier, plus
        // la pièce qui fait le métier de cette machine ». Avant, chaque machine avait
        // sa forme complète : le joueur réapprenait un motif entier à chaque bloc, et
        // rien ne disait à l'œil que le Stabilizer et le Crusher étaient du même âge.
        // Les quantités « boîtier » (pierre, fer de structure) sont absorbées par le
        // châssis ; seuls restent les ingrédients porteurs de sens.

        machine(recipeOutput, ModBlocks.RESONANCE_STABILIZER.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.RAW_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.RAW_RESONANCE_CRYSTAL.get()));

        machine(recipeOutput, ModBlocks.COMPONENT_ASSEMBLER.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 2)
                .requires(Items.REDSTONE));

        machine(recipeOutput, ModBlocks.RESONANCE_WHETSTONE.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get()));

        // Resonance Tuner : 2 Iron Ingot + 1 Resonance Component + 1 Redstone
        // (05-Machines.md, section outil transversal). Sans forme imposée.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.TOOLS, ModItems.RESONANCE_TUNER.get())
            .requires(ModTags.Items.IRON_SUBSTITUTES)
            .requires(ModTags.Items.IRON_SUBSTITUTES)
            .requires(ModItems.RESONANCE_COMPONENT.get())
            .requires(Items.REDSTONE)
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);

        // Resonance Catalyst Core : 2 Resonance Component + 1 Refined Crystal +
        // 1 Redstone (05-Machines.md, augment transversal). Item non consommé qui
        // s'insère dans le slot d'augment (+15% de vitesse). Sans forme imposée.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModItems.RESONANCE_CATALYST_CORE.get())
            .requires(ModItems.RESONANCE_COMPONENT.get(), 2)
            .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get())
            .requires(Items.REDSTONE)
            .requires(blueprint(2))
            .unlockedBy(getHasName(ModItems.REFINED_RESONANCE_CRYSTAL.get()),
                has(ModItems.REFINED_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Resonance Locator : outil T2 (le design ne fixe pas la recette, #7 « craft »).
        // Proposé : 1 Stable Crystal (l'antenne) + 2 Component + 1 Iron + blueprint T2
        // (rendu). Forme sans contrainte de design.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RESONANCE_LOCATOR.get())
            .pattern(" S ")
            .pattern("CIC")
            .pattern(" P ")
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('C', ModItems.RESONANCE_COMPONENT.get())
            .define('I', ModTags.Items.IRON_SUBSTITUTES)
            .define('P', blueprint(2))
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);

        // Resonance Storage Cell : 2 Resonance Component + 1 Stable Resonance
        // Crystal (05-Machines.md #6) + le blueprint T2 (gate physique, rendu au
        // craft — 03-Progression.md). Batterie portable, sans forme imposée.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.TOOLS, ModItems.RESONANCE_STORAGE_CELL.get())
            .requires(ModItems.RESONANCE_COMPONENT.get(), 2)
            .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .requires(blueprint(2))
            .unlockedBy(getHasName(ModItems.RESONANCE_COMPONENT.get()),
                has(ModItems.RESONANCE_COMPONENT.get()))
            .save(recipeOutput);

        // --- Machines T2 : châssis Accordé + blueprint T2 (rendu) --------------

        machine(recipeOutput, ModBlocks.FLUX_PURIFIER.get(), ModBlocks.ATTUNED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 2)
                .requires(Items.REDSTONE_BLOCK)
                .requires(blueprint(2)));

        // Field Emitter : c'est LUI que l'amorçage garanti de l'Avant-poste doit
        // permettre de fabriquer (4 Component + 2 Gold dans le coffre, voir
        // ModChestLootProvider). Il en consomme 2 et 2 : le lot garanti couvre donc la
        // recette avec de la marge, et le châssis ne demande, lui, que des matériaux
        // T1 (pierre, cuivre, fer, cristaux stables) — aucun Component. La dépendance
        // circulaire Component ⇄ champ ne peut donc pas se reformer par ce chemin.
        machine(recipeOutput, ModBlocks.FIELD_EMITTER.get(), ModBlocks.ATTUNED_CHASSIS.get(),
            ModItems.RESONANCE_COMPONENT.get(), b -> b
                .requires(ModItems.RESONANCE_COMPONENT.get(), 2)
                .requires(Items.GOLD_INGOT, 2)
                .requires(blueprint(2)));

        // --- Machine T3 : châssis Veskorien ------------------------------------

        machine(recipeOutput, ModBlocks.DAMPING_ARRAY.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.REFINED_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get())
                .requires(Items.REDSTONE_BLOCK)
                .requires(blueprint(2)));

        // Veskorian Alloy Forge (#10) : la porte du T3, et la PREMIÈRE recette à exiger
        // le blueprint T3. Jusqu'ici le plan T2 ouvrait tout ; il s'arrête ici.
        machine(recipeOutput, ModBlocks.VESKORIAN_ALLOY_FORGE.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.REFINED_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
                .requires(ModTags.Items.IRON_SUBSTITUTES)
                .requires(ModTags.Items.IRON_SUBSTITUTES)
                .requires(blueprint(3)));

        // Resonance Relay (#9) : 4 Refined Crystal + 2 Conductive Alloy Ingot + 1 Diamond.
        //
        // L'alliage CONDUCTEUR, et lui seul : c'est ici que la branche du métal choisie à la
        // Forge se paie ou se récompense. Un joueur qui n'a fondu que du fer a de quoi bâtir
        // et rien pour porter son champ ; il lui faut retourner à la Forge avec de l'or. Deux
        // lingots interchangeables auraient fait de ce choix une décoration.
        machine(recipeOutput, ModBlocks.RESONANCE_RELAY.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT.get(), b -> b
                .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 4)
                .requires(ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT.get(), 2)
                .requires(Items.DIAMOND)
                .requires(blueprint(3)));

        // Flux Compressor (#23) : le condensateur du palier.
        machine(recipeOutput, ModBlocks.FLUX_COMPRESSOR.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.REFINED_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
                .requires(ModItems.VESKORIAN_ALLOY_INGOT.get(), 2)
                .requires(blueprint(3)));

        // Advanced Assembler (T3) : il compose la Matrice de Résonance. Il coûte de
        // l'alliage CONDUCTEUR, comme ce qu'il fabriquera — la machine et sa production
        // tirent sur la même branche de métal, donc le choix fait à la Forge se paie une
        // fois de plus, dès l'achat de l'outil.
        machine(recipeOutput, ModBlocks.ADVANCED_ASSEMBLER.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT.get(), b -> b
                .requires(ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT.get(), 2)
                .requires(ModItems.RESONANCE_COMPONENT.get(), 2)
                .requires(blueprint(3)));

        // Reclaimer (T3) : il ferme la boucle économique. Il se fabrique en CUIVRE — le
        // métal des récupérateurs — plutôt qu'en alliage neuf : la machine qui rend les
        // déchets ne doit pas coûter cher dans le matériau qu'elle sert à ne plus gâcher.
        machine(recipeOutput, ModBlocks.RECLAIMER.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.VESKORIAN_ALLOY_INGOT.get(), b -> b
                .requires(net.minecraft.world.item.Items.COPPER_BLOCK, 2)
                .requires(ModItems.VESKORIAN_ALLOY_INGOT.get(), 2)
                .requires(blueprint(3)));

        // Structural Synthesizer (#11) : ce qui rend l'alliage bâtissable.
        machine(recipeOutput, ModBlocks.STRUCTURAL_SYNTHESIZER.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.VESKORIAN_ALLOY_INGOT.get(), b -> b
                .requires(ModItems.VESKORIAN_ALLOY_INGOT.get(), 4)
                .requires(Items.SMOOTH_STONE)
                .requires(blueprint(3)));

        // Deep Crystal Driller (#12). Le dossier annonce 6 Component + 2 lingots ; c'est
        // 4 + 2 ici, et la raison n'est pas un arbitrage d'équilibrage.
        //
        // UNE RECETTE SANS FORME NE TIENT QUE NEUF INGRÉDIENTS — c'est la taille de la
        // grille. Avec le châssis et le blueprint, 6 + 2 en faisait DIX : le JSON se
        // générait sans broncher, et c'est le chargement du monde qui rejetait la recette
        // (« Too many ingredients »). La machine existait, se posait, fonctionnait — et
        // n'était fabricable par personne. Trouvé en jeu, pas par la génération.
        //
        // Le chiffre du dossier a été écrit avant l'existence des châssis, qui absorbent
        // justement les quantités de « boîtier » (voir la note en tête des machines T1) :
        // le Veskorien contient déjà 2 fers et 2 cristaux raffinés. Retirer deux Component
        // rend donc la recette conforme à cette grammaire, en plus de la rendre chargeable.
        //
        // Un GameTest vérifie désormais que CHAQUE machine du mod a une recette réellement
        // chargée (voir MachineGameTests#everyMachineIsActuallyCraftable) : cette classe
        // d'erreur ne peut plus atteindre une partie.
        machine(recipeOutput, ModBlocks.DEEP_CRYSTAL_DRILLER.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.VESKORIAN_ALLOY_INGOT.get(), b -> b
                .requires(ModItems.RESONANCE_COMPONENT.get(), 4)
                .requires(ModItems.VESKORIAN_ALLOY_INGOT.get(), 2)
                .requires(blueprint(3)));

        // Slag Vent (#13) : volontairement BON MARCHÉ. Il ne produit rien ; le rendre
        // cher n'ajouterait aucune décision, ça repousserait juste le moment où le joueur
        // cesse de vider un slot à la main.
        machine(recipeOutput, ModBlocks.SLAG_VENT.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.FLUX_SLAG.get(), b -> b
                .requires(ModItems.VESKORIAN_ALLOY_INGOT.get())
                .requires(Items.IRON_BARS)
                .requires(blueprint(3)));

        // --- Machines T4 : châssis Veskorien + blueprint T4 ---------------------
        //
        // Pas de quatrième châssis : le dossier n'en prévoit que trois, et le T4 n'est pas
        // un changement de matière, c'est un changement d'ÉCHELLE — mêmes boîtiers, pièces
        // maîtresses en Treillis et en Hyper Refined.

        // Deep Synthesis Chamber (#15). Le Hyper Refined de la recette n'est pas un
        // ingrédient comme un autre : c'est le CATALYSEUR PERMANENT de la Chambre
        // (05-Machines.md, « Bootstrap du T4 »). Il ne reparaît jamais comme entrée de
        // cycle — la machine, une fois posée, tourne sur du cristal raffiné. C'est ce
        // troisième cristal qui rend le choix du palier réel : les deux autres partent
        // dans le Treillis du premier Amplificateur, et on ne peut pas faire les deux.
        machine(recipeOutput, ModBlocks.DEEP_SYNTHESIS_CHAMBER.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.HYPER_REFINED_CRYSTAL.get(), b -> b
                .requires(ModItems.HYPER_REFINED_CRYSTAL.get())
                .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
                .requires(ModItems.VESKORIAN_ALLOY_INGOT.get(), 2)
                .requires(ModItems.RESONANCE_MATRIX.get())
                .requires(blueprint(4)));

        // Harmonic Amplifier (#14) : Treillis + 2 Refined Crystal (05-Machines.md).
        machine(recipeOutput, ModBlocks.HARMONIC_AMPLIFIER.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.HARMONIC_LATTICE.get(), b -> b
                .requires(ModItems.HARMONIC_LATTICE.get())
                .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
                .requires(ModItems.RESONANCE_MATRIX.get())
                .requires(blueprint(4)));

        // Rift Core Extractor (#20). Le dossier ne fixe pas sa recette (« placé dans une
        // Faille ancrée ») : proposé au niveau de l'Ancre, dont il est le compagnon direct.
        machine(recipeOutput, ModBlocks.RIFT_CORE_EXTRACTOR.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.HYPER_REFINED_CRYSTAL.get(), b -> b
                .requires(ModItems.HYPER_REFINED_CRYSTAL.get(), 2)
                .requires(ModItems.HARMONIC_LATTICE.get())
                .requires(ModBlocks.VESKORIAN_ALLOY_BLOCK.get(), 2)
                .requires(blueprint(4)));

        // Rift Ward Emitter (#21) : 2 Rift Essence + 4 Alloy Block (05-Machines.md).
        // Il se paie donc EN ESSENCE — un tiers du butin d'une Faille part à rendre cette
        // Faille exploitable. C'est le seul objet du mod dont le coût est prélevé sur ce
        // qu'il permet d'obtenir, et c'est ce qui fait de la première Faille un pari.
        machine(recipeOutput, ModBlocks.RIFT_WARD_EMITTER.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.RIFT_ESSENCE.get(), b -> b
                .requires(ModItems.RIFT_ESSENCE.get(), 2)
                .requires(ModBlocks.VESKORIAN_ALLOY_BLOCK.get(), 4)
                .requires(blueprint(4)));

        // Rift Anchor (#19) : 4 Hyper Refined + 4 Alloy Block (05-Machines.md). Neuf
        // entrées pile avec le châssis et le blueprint — le maximum, et c'est cohérent :
        // c'est la dernière machine du mod, elle doit coûter tout ce qu'une grille peut
        // porter.
        machine(recipeOutput, ModBlocks.RIFT_ANCHOR.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.HYPER_REFINED_CRYSTAL.get(), b -> b
                .requires(ModItems.HYPER_REFINED_CRYSTAL.get(), 4)
                .requires(ModBlocks.VESKORIAN_ALLOY_BLOCK.get(), 3)
                .requires(blueprint(4)));

        // Convergence Core (#18). Le dossier annonce 12 Alloy Block + 6 Treillis +
        // 4 Hyper Refined + 4 Flux Concentré — VINGT-SIX objets, soit près de trois fois ce
        // qu'une grille peut tenir. C'est la même impossibilité que la Foreuse, en plus
        // franche, et elle ne se voit qu'au chargement du monde.
        //
        // Le chiffre du dossier décrit le coût du CHANTIER, pas celui du bloc : l'anneau
        // réclame huit relais ou amplificateurs, et c'est là que partent les autres
        // Treillis et Hyper Refined. Le bloc central en garde le quart, et reste de loin
        // la recette la plus chère du jeu. 05-Machines.md porte la révision.
        //
        // TROIS blocs d'alliage et non quatre : à quatre, la recette faisait dix entrées
        // avec le châssis et le blueprint — un de trop, et donc une recette écartée en
        // silence au chargement du monde. C'est le troisième objet du mod à buter sur les
        // neuf cases ; le test everyMachineIsActuallyCraftable est né du deuxième.
        //
        // Le Flux Concentré reste, lui, non négociable : le Core est son SEUL consommateur
        // dans tout le jeu. Le retirer aurait laissé le Flux Compressor sans débouché et
        // vidé une machine entière de sa raison d'être, pour économiser une case.
        machine(recipeOutput, ModBlocks.CONVERGENCE_CORE.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.HARMONIC_LATTICE.get(), b -> b
                .requires(ModBlocks.VESKORIAN_ALLOY_BLOCK.get(), 3)
                .requires(ModItems.HARMONIC_LATTICE.get(), 2)
                .requires(ModItems.HYPER_REFINED_CRYSTAL.get())
                .requires(ModItems.CONCENTRATED_FLUX.get())
                .requires(blueprint(4)));

        // Automated Extraction Array (#16) : 4 lingots d'alliage (05-Machines.md).
        machine(recipeOutput, ModBlocks.AUTOMATED_EXTRACTION_ARRAY.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.VESKORIAN_ALLOY_INGOT.get(), b -> b
                .requires(ModItems.VESKORIAN_ALLOY_INGOT.get(), 4)
                .requires(ModItems.RESONANCE_MATRIX.get(), 2)
                .requires(blueprint(4)));

        // Resonance Network Hub (#17) : 4 Component + 2 Treillis (05-Machines.md).
        // Deux Treillis, c'est-à-dire quatre Hyper Refined : il coûte deux fois le premier
        // Amplificateur. C'est voulu — on n'arbitre pas un réseau qu'on n'a pas encore.
        machine(recipeOutput, ModBlocks.RESONANCE_NETWORK_HUB.get(), ModBlocks.VESKORIAN_CHASSIS.get(),
            ModItems.HARMONIC_LATTICE.get(), b -> b
                .requires(ModItems.RESONANCE_MATRIX.get(), 2)
                .requires(ModItems.HARMONIC_LATTICE.get(), 2)
                .requires(blueprint(4)));

        // --- Outils et armure en alliage (04-Materials.md) ---------------------
        //
        // Formes vanilla, délibérément : une épée se craft comme une épée. Le mod a déjà
        // sa grammaire propre pour les machines (châssis + pièce distinctive) ; l'imposer
        // aussi à l'équipement obligerait à réapprendre ce que tout le monde sait déjà.
        // Le blueprint T3 suffit à en faire un équipement de palier.
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VESKORIAN_ALLOY_SWORD.get())
            .pattern("A").pattern("A").pattern("S")
            .define('A', ModItems.VESKORIAN_ALLOY_INGOT.get())
            .define('S', Items.STICK)
            .unlockedBy(getHasName(ModItems.VESKORIAN_ALLOY_INGOT.get()),
                has(ModItems.VESKORIAN_ALLOY_INGOT.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.VESKORIAN_ALLOY_PICKAXE.get())
            .pattern("AAA").pattern(" S ").pattern(" S ")
            .define('A', ModItems.VESKORIAN_ALLOY_INGOT.get())
            .define('S', Items.STICK)
            .unlockedBy(getHasName(ModItems.VESKORIAN_ALLOY_INGOT.get()),
                has(ModItems.VESKORIAN_ALLOY_INGOT.get()))
            .save(recipeOutput);

        armorPiece(recipeOutput, ModItems.VESKORIAN_ALLOY_HELMET.get(), "AAA", "A A", "   ");
        armorPiece(recipeOutput, ModItems.VESKORIAN_ALLOY_CHESTPLATE.get(), "A A", "AAA", "AAA");
        armorPiece(recipeOutput, ModItems.VESKORIAN_ALLOY_LEGGINGS.get(), "AAA", "A A", "A A");
        armorPiece(recipeOutput, ModItems.VESKORIAN_ALLOY_BOOTS.get(), "   ", "A A", "A A");

        // Rift-Ward Plate. Le dossier demande aussi 2 Meteoric Resonance Shard — un objet
        // qui N'EXISTE PAS dans le mod, et qui aurait rendu la dernière pièce
        // d'équipement du jeu infabricable. Son propre raisonnement dit pourtant que la
        // pièce est « calibrée pour être atteignable avec le seul drop garanti d'une
        // Faille », soit les 3 lingots corrompus du Gardien : l'éclat est l'intrus, et il
        // est retiré. 04-Materials.md porte la révision.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.COMBAT, ModItems.RIFT_WARD_PLATE.get())
            .requires(ModItems.VESKORIAN_ALLOY_CHESTPLATE.get())
            .requires(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT.get(), 3)
            .requires(ModItems.RIFT_ESSENCE.get())
            .unlockedBy(getHasName(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT.get()),
                has(ModItems.CORRUPTED_VESKORIAN_ALLOY_INGOT.get()))
            .save(recipeOutput);

        // --- Matériau T4 : le Treillis Harmonique -------------------------------
        //
        // 4 lingots CONDUCTEURS + 2 Hyper Refined (04-Materials.md). Première recette à
        // exiger le blueprint T4, donc la première chose que l'Archive débloque.
        //
        // Ce n'est pas un objet parmi d'autres : Harmonic Amplifier et Convergence Core
        // sont les deux seuls consommateurs du Treillis, et rien d'autre du mod n'en veut.
        // Il EST le palier — et il coûte deux des trois Hyper Refined que l'Archive donne,
        // ce qui rend le choix « Amplificateur ou Chambre » immédiatement concret.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModItems.HARMONIC_LATTICE.get())
            .requires(ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT.get(), 4)
            .requires(ModItems.HYPER_REFINED_CRYSTAL.get(), 2)
            .requires(blueprint(4))
            .unlockedBy(getHasName(ModItems.HYPER_REFINED_CRYSTAL.get()),
                has(ModItems.HYPER_REFINED_CRYSTAL.get()))
            .save(recipeOutput);

        // Émetteur Accordable : un Field Emitter + 2 Refined Crystal (l'accord demande
        // du cristal raffiné) + blueprint T2 rendu. Upgrade, pas une machine de plus.
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, ModBlocks.TUNABLE_FIELD_EMITTER.get())
            .requires(ModBlocks.FIELD_EMITTER.get())
            .requires(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
            .requires(blueprint(2))
            .unlockedBy(getHasName(ModItems.REFINED_RESONANCE_CRYSTAL.get()),
                has(ModItems.REFINED_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // Crystal Roost : le nichoir se monte sur un châssis T2, garni de planches et
        // de foin (05-Machines.md #8) + blueprint T2 rendu.
        machine(recipeOutput, ModBlocks.CRYSTAL_ROOST.get(), ModBlocks.ATTUNED_CHASSIS.get(),
            ModItems.STABLE_RESONANCE_CRYSTAL.get(), b -> b
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(net.minecraft.tags.ItemTags.PLANKS)
                .requires(Items.HAY_BLOCK)
                .requires(blueprint(2)));

        // Resonance Codex : recette de secours (Livre + Cristal Brut). Le Codex est
        // donné à la première connexion (15-Codex-Guidebook.md) ; ce craft ne sert qu'à
        // en refaire un s'il est perdu — un exemplaire neuf se re-remplit tout seul des
        // objets possédés et des paliers atteints (CodexUnlocks, scan périodique).
        net.minecraft.data.recipes.ShapelessRecipeBuilder
            .shapeless(RecipeCategory.TOOLS, ModItems.RESONANCE_CODEX.get())
            .requires(Items.BOOK)
            .requires(ModItems.RAW_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.RAW_RESONANCE_CRYSTAL.get()),
                has(ModItems.RAW_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        machine(recipeOutput, ModBlocks.CRYSTAL_CRUSHER.get(), ModBlocks.FRACTURED_CHASSIS.get(),
            ModItems.RAW_RESONANCE_CRYSTAL.get(), b -> b
                .requires(ModTags.Items.IRON_SUBSTITUTES));
    }

    /**
     * Les trois châssis de palier (05-Machines.md, « Châssis par palier »). Chacun
     * <b>contient</b> le précédent : le T2 est un T1 restauré, le T3 un T2 réarmé. La
     * progression est donc littérale — on ne jette rien, on améliore, ce qui colle au
     * pilier « restaurer plutôt que conquérir » (01-Vision-Pillars.md).
     */
    private void buildChassisRecipes(RecipeOutput recipeOutput) {
        // T1 « Fracturé » : de la pierre et du cuivre, rien d'autre. Craftable dès la
        // première minute — c'est le point d'entrée de toute la chaîne.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FRACTURED_CHASSIS.get())
            .pattern("CUC")
            .pattern("C C")
            .pattern("CUC")
            .define('C', Items.COBBLESTONE)
            .define('U', Items.COPPER_INGOT)
            .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
            .save(recipeOutput);

        // T2 « Accordé » : le châssis T1 renforcé de fer et serti de cristaux stables.
        // Volontairement SANS Resonance Component ni blueprint — il doit rester
        // fabricable avant d'avoir un champ, sinon la boucle d'amorçage T2 se referme.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ATTUNED_CHASSIS.get())
            .pattern(" I ")
            .pattern("SFS")
            .pattern(" I ")
            .define('I', ModTags.Items.IRON_SUBSTITUTES)
            .define('S', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .define('F', ModBlocks.FRACTURED_CHASSIS.get())
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);

        // T3 « Veskorien » : cristal raffiné, donc Flux Purifier, donc un champ. Le
        // palier se paie en infrastructure, pas en minage.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VESKORIAN_CHASSIS.get())
            .pattern(" R ")
            .pattern("IAI")
            .pattern(" R ")
            .define('R', ModItems.REFINED_RESONANCE_CRYSTAL.get())
            .define('I', ModTags.Items.IRON_SUBSTITUTES)
            .define('A', ModBlocks.ATTUNED_CHASSIS.get())
            .unlockedBy(getHasName(ModItems.REFINED_RESONANCE_CRYSTAL.get()),
                has(ModItems.REFINED_RESONANCE_CRYSTAL.get()))
            .save(recipeOutput);
    }

    /**
     * Maçonnerie et éclairage veskoriens (17-Dungeons.md §4).
     *
     * <p>Tout part du {@code resonance_veined_stone}, qu'on mine autour des poches : la
     * matière du donjon est celle qu'on trouve dans le sol, appareillée. Aucune de ces
     * recettes ne demande de blueprint ni de Component — bâtir n'est pas un palier, et
     * une brique verrouillée derrière un tier serait une brique qu'on n'utilise jamais.
     *
     * <p>Chaque forme est aussi taillable à la scie de pierre : c'est l'attente vanilla
     * pour une famille de blocs de construction, et ça divise par deux le coût en pierre
     * — donc ça rend la maçonnerie réellement utilisable pour un vrai bâtiment.
     */
    private void buildArchitectureRecipes(RecipeOutput output) {
        var stone = ModBlocks.RESONANCE_VEINED_STONE.get();
        var bricks = ModBlocks.VEINED_STONE_BRICKS.get();

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4)
            .pattern("##").pattern("##")
            .define('#', stone)
            .unlockedBy(getHasName(stone), has(stone))
            .save(output);

        // Fissurées : la brique passée au four. Le raccourci vanilla pour « vieilli »,
        // et le seul moyen d'en obtenir sans démonter une ruine.
        net.minecraft.data.recipes.SimpleCookingRecipeBuilder
            .smelting(net.minecraft.world.item.crafting.Ingredient.of(bricks),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.CRACKED_VEINED_STONE_BRICKS.get(), 0.1F, 200)
            .unlockedBy(getHasName(bricks), has(bricks))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHISELED_VEINED_STONE.get())
            .pattern("#").pattern("#")
            .define('#', ModBlocks.VEINED_STONE_BRICK_SLAB.get())
            .unlockedBy(getHasName(bricks), has(bricks))
            .save(output);

        stairBuilder(ModBlocks.VEINED_STONE_BRICK_STAIRS.get(),
            net.minecraft.world.item.crafting.Ingredient.of(bricks))
            .unlockedBy(getHasName(bricks), has(bricks)).save(output);
        slabBuilder(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VEINED_STONE_BRICK_SLAB.get(),
            net.minecraft.world.item.crafting.Ingredient.of(bricks))
            .unlockedBy(getHasName(bricks), has(bricks)).save(output);
        wallBuilder(RecipeCategory.DECORATIONS, ModBlocks.VEINED_STONE_BRICK_WALL.get(),
            net.minecraft.world.item.crafting.Ingredient.of(bricks))
            .unlockedBy(getHasName(bricks), has(bricks)).save(output);

        for (var target : java.util.List.of(bricks, ModBlocks.CHISELED_VEINED_STONE.get(),
            ModBlocks.VEINED_STONE_BRICK_STAIRS.get(), ModBlocks.VEINED_STONE_BRICK_WALL.get())) {
            stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, target, stone);
            if (target != bricks) {
                stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS, target, bricks);
            }
        }
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS,
            ModBlocks.VEINED_STONE_BRICK_SLAB.get(), stone, 2);
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS,
            ModBlocks.VEINED_STONE_BRICK_SLAB.get(), bricks, 2);
        stonecutterResultFromBase(output, RecipeCategory.BUILDING_BLOCKS,
            ModBlocks.VEINED_STONE_COLUMN.get(), bricks);

        // Colonne : deux briques l'une sur l'autre, comme le pilier de quartz vanilla.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VEINED_STONE_COLUMN.get(), 2)
            .pattern("#").pattern("#")
            .define('#', bricks)
            .unlockedBy(getHasName(bricks), has(bricks))
            .save(output);

        // Lampe : la brique + un Stable Crystal. Fabricable dès le T1 (le Stabilizer est
        // autonome), même si elle ne s'allumera qu'une fois un champ posé — c'est
        // volontaire : découvrir qu'une lampe déjà bâtie s'allume quand l'émetteur
        // démarre est un bien meilleur professeur qu'un tooltip.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.RESONANCE_LAMP.get(), 2)
            .pattern(" # ").pattern("#c#").pattern(" # ")
            .define('#', bricks)
            .define('c', ModItems.STABLE_RESONANCE_CRYSTAL.get())
            .unlockedBy(getHasName(ModItems.STABLE_RESONANCE_CRYSTAL.get()),
                has(ModItems.STABLE_RESONANCE_CRYSTAL.get()))
            .save(output);

        // Conduit : la poussière du Crusher, l'autre voie T1 — les deux branches de
        // départ mènent donc chacune à un bloc d'architecture, et aucune n'est requise.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CONDUIT_LINE.get(), 4)
            .pattern("###").pattern("ddd").pattern("###")
            .define('#', bricks)
            .define('d', ModItems.RESONANCE_DUST.get())
            .unlockedBy(getHasName(ModItems.RESONANCE_DUST.get()), has(ModItems.RESONANCE_DUST.get()))
            .save(output);
    }

    /** Une pièce d'armure, forme vanilla. Trois lignes, l'alliage en 'A'. */
    private void armorPiece(RecipeOutput output, net.minecraft.world.item.Item result,
                            String top, String middle, String bottom) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
            .pattern(top).pattern(middle).pattern(bottom)
            .define('A', ModItems.VESKORIAN_ALLOY_INGOT.get())
            .unlockedBy(getHasName(ModItems.VESKORIAN_ALLOY_INGOT.get()),
                has(ModItems.VESKORIAN_ALLOY_INGOT.get()))
            .save(output);
    }

    /**
     * <b>La clé de craft d'un palier, et de CE palier seulement.</b>
     *
     * <p>Le gatekeeping physique de `03-Progression.md` reposait sur une demi-mesure : les
     * recettes exigeaient bien un {@code resonance_blueprint}, mais
     * {@code ShapelessRecipeBuilder.requires(ItemLike)} <b>ignore les data components</b> —
     * donc le tier n'était jamais vérifié. Le plan T2 débloquait tout, y compris ce que le
     * Sigma et l'Archive sont censés garder derrière leurs énigmes.
     *
     * <p>Le contrôle existait pourtant ({@code ResonanceBlueprintItem.tierOf}), mais seules
     * la console (pour éviter les doublons) et l'infobulle l'appelaient : <b>la valeur
     * s'affichait, elle ne gardait rien</b>. C'est la classe de bug la plus coûteuse du
     * projet — une garantie qu'on croit tenue parce qu'on la voit écrite.
     *
     * <p>{@code strict = false} : on n'exige que le tier, pas une carte de composants
     * exacte — un blueprint qui gagnerait plus tard un autre composant resterait valide.
     */
    private static net.minecraft.world.item.crafting.Ingredient blueprint(int tier) {
        return net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(
            false, com.veskorius.item.ModDataComponents.BLUEPRINT_TIER.get(), tier,
            ModItems.RESONANCE_BLUEPRINT.get());
    }

    /**
     * Une machine = le châssis de son palier + ce qui la distingue. Sans forme imposée :
     * la disposition n'apprend plus rien au joueur une fois la grammaire comprise, et
     * une recette informe se retient mieux qu'un motif de plus.
     */
    private void machine(RecipeOutput output, net.minecraft.world.level.block.Block result,
                         net.minecraft.world.level.block.Block chassis,
                         net.minecraft.world.level.ItemLike trigger,
                         java.util.function.UnaryOperator<net.minecraft.data.recipes.ShapelessRecipeBuilder> parts) {
        net.minecraft.data.recipes.ShapelessRecipeBuilder builder =
            net.minecraft.data.recipes.ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, result)
                .requires(chassis);
        parts.apply(builder)
            .unlockedBy(getHasName(trigger), has(trigger))
            .save(output);
    }

    /**
     * Recettes de FONCTIONNEMENT des machines (ce qu'elles transforment une fois
     * posées), désormais data-driven. Ces valeurs reproduisent celles qui étaient
     * en dur ; un datapack peut les changer ou en ajouter sans recompiler.
     */
    private void buildMachineRecipes(RecipeOutput recipeOutput) {

        // Veskorian Alloy Forge (#10) : 2 Refined Crystal + 2 lingots → 1 alliage, 20 s,
        // 4 Osc/tick. La SCORIE n'est pas dans la recette : elle sort à chaque cycle,
        // quelle que soit la branche (voir VeskorianAlloyForgeBlockEntity). Un datapack
        // qui ajoute un alliage produira donc sa scorie comme les autres.
        //
        // Le métal d'entrée décide de la branche, et c'est un vrai choix : le FER donne
        // le structurel, l'OR le conducteur — seul admis par le Resonance Relay.
        MachineRecipeBuilder.forging(ModItems.VESKORIAN_ALLOY_INGOT.get(), 1)
            .input(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
            .input(ModTags.Items.IRON_SUBSTITUTES, 2)
            .time(400).osc(4).byproduct(ModItems.FLUX_SLAG.get(), 1)
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "forging/veskorian_alloy_ingot"));
        MachineRecipeBuilder.forging(ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT.get(), 1)
            .input(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
            .input(net.minecraft.world.item.Items.GOLD_INGOT, 2)
            .time(400).osc(4).byproduct(ModItems.FLUX_SLAG.get(), 1)
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "forging/veskorian_conductive_alloy_ingot"));
        // Deep Synthesis Chamber (#15) : 2 Refined → 1 Hyper Refined, 90 s, 8 Osc/tick.
        // Le catalyseur a été payé à la construction, il n'apparaît donc pas ici.
        MachineRecipeBuilder.synthesis(ModItems.HYPER_REFINED_CRYSTAL.get(), 1)
            .input(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 2)
            .time(90 * 20).osc(8)
            .save(recipeOutput, machineRecipe("synthesis/hyper_refined_crystal"));

        // Flux Compressor (#23) : 4 Refined → 1 Flux Concentré, 30 s, 6 Osc/tick.
        MachineRecipeBuilder.compressing(ModItems.CONCENTRATED_FLUX.get(), 1)
            .input(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 4)
            .time(30 * 20).osc(6)
            .save(recipeOutput, machineRecipe("compressing/concentrated_flux"));

        // Reclaimer (T3) : les deux conversions que 16-Revision-and-Expansion.md dicte
        // mot pour mot — « slag → un peu de pierre/gravier, sludge → poussière ».
        //
        // QUATRE POUR UN, ET LE TAUX EST MAUVAIS EXPRÈS. Recycler doit rester moins
        // rentable que miner : un gravier vaut une pelletée, une poussière vaut un tiers
        // de cristal brut. Si la boucle payait mieux, elle remplacerait l'exploration au
        // lieu de la prolonger, et le mod enseignerait à rester chez soi. Ce qu'on achète
        // ici n'est pas du rendement, c'est de ne plus avoir à jeter.
        MachineRecipeBuilder.reclaiming(net.minecraft.world.item.Items.GRAVEL, 1)
            .input(ModItems.FLUX_SLAG.get(), 4)
            .time(20 * 20).osc(4)
            .save(recipeOutput, machineRecipe("reclaiming/gravel_from_slag"));

        // La boue n'avait, elle, STRICTEMENT aucun destinataire : le Damping Array en
        // produisait à chaque purge et rien ne la consommait. Elle rend de la poussière —
        // donc elle rentre dans la chaîne T1, ce qui referme la boucle jusqu'en bas.
        MachineRecipeBuilder.reclaiming(ModItems.RESONANCE_DUST.get(), 1)
            .input(ModItems.RESONANCE_SLUDGE.get(), 4)
            .time(20 * 20).osc(4)
            .save(recipeOutput, machineRecipe("reclaiming/dust_from_sludge"));

        // Advanced Assembler (T3) : 4 Composants + 2 lingots CONDUCTEURS → 1 Matrice,
        // 30 s, 5 Osc/tick.
        //
        // Le conducteur, pas le structurel. C'est la TROISIÈME fois que la branche de
        // métal choisie à la Forge se fait payer — après le Relais et le Treillis
        // Harmonique — et c'est ce qui transforme une décision de début de palier en
        // vraie planification : elle continue de coûter jusqu'au T4.
        MachineRecipeBuilder.advancedAssembling(ModItems.RESONANCE_MATRIX.get(), 1)
            .input(ModItems.RESONANCE_COMPONENT.get(), 4)
            .input(ModItems.VESKORIAN_CONDUCTIVE_ALLOY_INGOT.get(), 2)
            .time(30 * 20).osc(5)
            .save(recipeOutput, machineRecipe("advanced_assembling/resonance_matrix"));

        // Structural Synthesizer (#11) : 4 lingots + 8 pierres → 4 blocs, 60 s. Le RÉSIDU
        // n'est pas ici : comme la scorie de la Forge, il est une propriété de la machine
        // (voir StructuralSynthesizerBlockEntity), donc un datapack qui ajoute un moulage
        // produira son résidu comme les autres.
        MachineRecipeBuilder.synthesizing(ModBlocks.VESKORIAN_ALLOY_BLOCK.get(), 4)
            .input(ModItems.VESKORIAN_ALLOY_INGOT.get(), 4)
            .input(net.minecraft.tags.ItemTags.STONE_CRAFTING_MATERIALS, 8)
            .time(60 * 20).osc(6).byproduct(ModItems.SYNTHESIS_RESIDUE.get(), 1)
            .save(recipeOutput, machineRecipe("synthesizing/veskorian_alloy_block"));

        // Stabilizer : Raw Crystal + flux (Quartz via tag) → Stable Crystal, 30 s,
        // autonome (05-Machines.md #1).
        MachineRecipeBuilder.stabilizing(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .input(ModItems.RAW_RESONANCE_CRYSTAL.get(), 1)
            .input(ModTags.Items.STABILIZER_FLUX, 1)
            .time(30 * 20)
            .stable()
            .save(recipeOutput, machineRecipe("stabilizing/stable_crystal"));

        // Assembler : 1 Stable Crystal + 2 Iron → 2 Component, 5 s, 3 Osc/tick
        // (05-Machines.md #2).
        MachineRecipeBuilder.assembling(ModItems.RESONANCE_COMPONENT.get(), 2)
            .input(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .input(ModTags.Items.IRON_SUBSTITUTES, 2)
            .time(5 * 20)
            .osc(3)
            .stable()
            .save(recipeOutput, machineRecipe("assembling/component"));

        // Purifier : 1 Stable Crystal + 1 Redstone → 1 Refined Crystal, 45 s,
        // 2 Osc/tick (05-Machines.md #5).
        MachineRecipeBuilder.purifying(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 1)
            .input(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .input(Items.REDSTONE, 1)
            .time(45 * 20)
            .osc(2)
            .save(recipeOutput, machineRecipe("purifying/refined_crystal"));

        // Crusher : 1 Raw Crystal → 3 Resonance Dust, 10 s, autonome
        // (05-Machines.md #22). Plus rapide que le Stabilizer (10 s contre 30 s)
        // mais ne produit pas de Stable Crystal (04-Materials.md).
        MachineRecipeBuilder.crushing(ModItems.RESONANCE_DUST.get(), 3)
            .input(ModItems.RAW_RESONANCE_CRYSTAL.get(), 1)
            .time(10 * 20)
            .stable()
            .save(recipeOutput, machineRecipe("crushing/resonance_dust"));

        // Assembler, branche alternative : 3 Resonance Dust + 2 Iron → 2 Component,
        // 5 s, 3 Osc/tick (04-Materials.md + note tâche 2 de 11-Development-Plan.md).
        // Désormais possible sans une ligne de code machine : la poussière existe,
        // le slot d'entrée 0 de l'Assembler l'accepte du seul fait de cette recette
        // (isItemValid piloté par les recettes). Mêmes sortie/temps/Osc que la voie
        // au Stable Crystal — c'est une entrée alternative, pas un meilleur chemin.
        MachineRecipeBuilder.assembling(ModItems.RESONANCE_COMPONENT.get(), 2)
            .input(ModItems.RESONANCE_DUST.get(), 3)
            .input(ModTags.Items.IRON_SUBSTITUTES, 2)
            .time(5 * 20)
            .osc(3)
            .stable()
            .save(recipeOutput, machineRecipe("assembling/component_from_dust"));

        // Roost : 2 Quartz → 1 Raw Crystal, 600 s, autonome (05-Machines.md #8). La
        // condition « un Fileur à proximité » n'est pas dans la recette : elle vit
        // dans la machine (CrystalRoostBlockEntity.canRunCycle). 2 Quartz par cycle,
        // 2 cycles par jour MC = 4 Quartz/jour, cohérent avec le design.
        MachineRecipeBuilder.roosting(ModItems.RAW_RESONANCE_CRYSTAL.get(), 1)
            .input(Items.QUARTZ, 2)
            .time(600 * 20)
            .stable()
            .save(recipeOutput, machineRecipe("roosting/raw_crystal"));

        // Whetstone : outil endommagé + 1 Stable Crystal → outil réparé de 25 %,
        // 8 s, autonome (05-Machines.md #3). Type dédié (réparation, pas input→output).
        WhetstoneRecipeBuilder.sharpening()
            .catalyst(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 1)
            .repairPercent(25)
            .time(8 * 20)
            .save(recipeOutput, machineRecipe("sharpening/whetstone"));

        // Field Emitter : carburants data-driven (14-Configuration.md). Par défaut, un
        // seul carburant, le Stable Crystal à 4000 Osc (06-Energy.md, source primaire).
        // Un datapack en ajoute (ex. Refined Crystal à 9000 Osc, capacité à augmenter
        // en conséquence), en retire ou change les valeurs — sans une ligne de code.
        EmitterFuelRecipeBuilder.fuel(ModItems.STABLE_RESONANCE_CRYSTAL.get(), 4000)
            .save(recipeOutput, machineRecipe("fueling/stable_crystal"));

        // Agents de damping, data-driven (06-Energy.md). Le Refined Crystal ouvre la
        // voie dès le T2 ; le Concentrated Flux (T3) prendra le relais avec une valeur
        // bien supérieure — un simple JSON de plus, aucune ligne de code.
        DampingAgentRecipeBuilder.agent(ModItems.REFINED_RESONANCE_CRYSTAL.get(), 500)
            .save(recipeOutput, machineRecipe("damping/refined_crystal"));
    }

    private static ResourceLocation machineRecipe(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }
}
