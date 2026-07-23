package com.veskorius.tag;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Tags du mod.
 *
 * Deux entrees de recette sont volontairement definies par tag plutot qu'en dur,
 * parce que le dossier de conception annonce deja un second membre pour chacune :
 *
 * - {@link Items#STABILIZER_FLUX} : le Quartz aujourd'hui, plus le
 *   {@code raw_flux_deposit} en 1:1 (04-Materials.md, groupe 2 — Phase 1, tache 14).
 * - {@link Items#MACHINE_AUGMENTS} : vide aujourd'hui, recevra le
 *   {@code resonance_catalyst_core} (05-Machines.md, augment transversal —
 *   Phase 1, tache 15).
 *
 * Consequence : ces deux taches deviennent un ajout de datagen d'une ligne, sans
 * retoucher au code des machines deja ecrites.
 */
public final class ModTags {

    private ModTags() {
    }

    public static final class Items {

        /**
         * Objets acceptes dans le slot d'augment que possede chaque machine active
         * (05-Machines.md). Un seul par machine, jamais consomme.
         */
        public static final TagKey<Item> MACHINE_AUGMENTS = tag("machine_augments");

        /**
         * Source de flux acceptee en seconde entree du Resonance Stabilizer
         * (05-Machines.md #1 : "Raw Crystal + Quartz").
         */
        public static final TagKey<Item> STABILIZER_FLUX = tag("stabilizer_flux");

        /**
         * Substituts du lingot de fer dans les recettes Veskorius : le fer, plus le
         * {@code custode_alloy_fragment} (04-Materials.md, drop du Custode). Toutes
         * les recettes Veskorius qui demandaient du fer passent par ce tag, pour que
         * le fragment y soit accepte 1:1 sans toucher les recettes vanilla.
         */
        public static final TagKey<Item> IRON_SUBSTITUTES = tag("iron_substitutes");

        private Items() {
        }

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name));
        }
    }

    public static final class Structures {

        /**
         * Structures détectables par le mode Structures du Resonance Locator
         * (16-Revision-and-Expansion.md §1). Vide tant que les structures sont des
         * <i>features</i> ; se remplira à la migration vers de vraies {@code Structure}
         * (Avant-poste, Sigma Laboratory…). Le Locator lit ce tag via l'API vanilla
         * {@code findNearestMapStructure} — aucun scan de blocs.
         */
        public static final TagKey<Structure> LOCATABLE =
            TagKey.create(Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "locatable"));

        private Structures() {
        }
    }
}
