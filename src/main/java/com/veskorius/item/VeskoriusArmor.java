package com.veskorius.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Ce que l'armure veskorienne fait <b>de plus</b> que protéger.
 *
 * <p>Sa protection est celle du diamant, ni plus ni moins : une armure de fin de jeu qui
 * ne ferait que monter les nombres n'apprendrait rien. Ce qui la distingue est qu'elle
 * répond au <b>déphasage</b> — le seul dégât du mod qu'aucune armure vanilla n'atténue,
 * puisqu'il ne frappe pas, il désaccorde.
 *
 * <p>Deux crans, et ils correspondent aux deux moments du dernier palier :
 * <ul>
 *   <li><b>La panoplie complète</b> divise le déphasage par deux. Elle permet de
 *       <i>visiter</i> une Faille sauvage plus longtemps que les trois secondes de grâce —
 *       assez pour repérer où poser l'Ancre sans mourir en chemin.</li>
 *   <li><b>Le Rift-Ward Plate</b> l'annule entièrement, sur tout le corps, à lui seul.</li>
 * </ul>
 *
 * <p><b>Pourquoi la panoplie entière et non chaque pièce.</b> Un quart d'atténuation par
 * pièce aurait rendu le calcul invisible : on ne sait pas qu'on a « 50 % » en portant deux
 * pièces, on constate qu'on meurt un peu moins vite. Un seuil franc se lit — on l'a, ou on
 * ne l'a pas — et il donne une raison de terminer la panoplie plutôt que de s'arrêter au
 * plastron.
 */
public final class VeskoriusArmor {

    private VeskoriusArmor() {
    }

    /**
     * Facteur appliqué aux dégâts de déphasage : 0 avec le Plate, 0,5 avec la panoplie
     * complète, 1 sinon. Fonction pure d'un équipement — testable sans monde.
     */
    public static float phaseDamageFactor(LivingEntity target) {
        if (wearsWardPlate(target)) {
            return 0.0f;
        }
        return wearsFullAlloySet(target) ? 0.5f : 1.0f;
    }

    /** Vrai si le plastron porté est le Rift-Ward Plate. */
    public static boolean wearsWardPlate(LivingEntity target) {
        return target.getItemBySlot(EquipmentSlot.CHEST)
            .is(ModItems.RIFT_WARD_PLATE.get());
    }

    /** Vrai si les quatre pièces d'alliage sont portées. */
    public static boolean wearsFullAlloySet(LivingEntity target) {
        for (EquipmentSlot slot : new EquipmentSlot[] {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack worn = target.getItemBySlot(slot);
            if (!(worn.getItem() instanceof ArmorItem armor)
                || !armor.getMaterial().equals(ModTiers.ALLOY)) {
                return false;
            }
        }
        return true;
    }
}
