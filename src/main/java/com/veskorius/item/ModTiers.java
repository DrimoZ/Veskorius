package com.veskorius.item;

import com.veskorius.Veskorius;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Matériau des outils et de l'armure en alliage veskorien (04-Materials.md).
 *
 * <p><b>Un seul palier pour les deux outils, et c'est une lecture du dossier plutôt qu'un
 * raccourci.</b> Celui-ci demande une épée « aux dégâts du diamant, durabilité +20 % » et
 * une pioche « de niveau netherite ». Or un {@code Tier} porte à la fois le niveau de
 * minage et le bonus de dégâts : un seul suffit donc à satisfaire les deux, en prenant le
 * niveau de la pioche et le bonus du diamant. Deux paliers distincts auraient dupliqué la
 * durabilité sans rien changer au résultat.
 */
public final class ModTiers {

    /** Durabilité du diamant (1561) majorée de 20 %, comme annoncé. */
    private static final int USES = 1873;

    private ModTiers() {
    }

    /**
     * <b>Niveau netherite, dégâts du diamant.</b> Le niveau vient de la pioche, le bonus de
     * l'épée — voir la note de classe.
     */
    public static final Tier VESKORIAN_ALLOY = new SimpleTier(
        // SON PROPRE TAG, et pas celui de la netherite. Un palier d'outil se définit par
        // ce qu'il rate : tant qu'il partageait le tag de la netherite, l'alliage était
        // son égal exact et ne pouvait rien miner qu'elle ne mine. Le dossier lui réserve
        // pourtant une cible propre — l'ancient_conduit_stone, que rien d'autre n'extrait.
        com.veskorius.tag.ModTags.Blocks.INCORRECT_FOR_VESKORIAN_TOOL,
        USES,
        8.0f,
        3.0f,
        14,
        () -> Ingredient.of(ModItems.VESKORIAN_ALLOY_INGOT.get()));

    // --- Armure ---------------------------------------------------------------

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
        DeferredRegister.create(Registries.ARMOR_MATERIAL, Veskorius.MOD_ID);

    /**
     * Protection du diamant. Le dossier ne demande pas mieux : ce qui distingue cette
     * armure n'est pas son chiffre, c'est qu'elle <b>divise par deux les dégâts de
     * déphasage</b> près d'une Faille non ancrée — voir {@link VeskoriusArmor}. Une armure
     * de fin de jeu qui ne ferait que monter les nombres n'apprendrait rien au joueur.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALLOY =
        ARMOR_MATERIALS.register("veskorian_alloy", () -> new ArmorMaterial(
            java.util.Map.of(
                ArmorItem.Type.BOOTS, 3,
                ArmorItem.Type.LEGGINGS, 6,
                ArmorItem.Type.CHESTPLATE, 8,
                ArmorItem.Type.HELMET, 3,
                ArmorItem.Type.BODY, 11),
            10,
            net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(ModItems.VESKORIAN_ALLOY_INGOT.get()),
            List.of(new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "veskorian_alloy"))),
            2.0f,
            0.0f));

    /**
     * Multiplicateur de durabilité de l'armure. La valeur du diamant (33) : contrairement
     * aux outils, le dossier ne promet pas de bonus de durabilité sur l'armure — seulement
     * sur l'épée.
     */
    public static final int ARMOR_DURABILITY = 33;
}
