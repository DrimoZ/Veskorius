package com.veskorius.datagen;

import com.veskorius.Veskorius;
import com.veskorius.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Modèles d'objet — <b>énumérés depuis le registre, jamais recopiés à la main</b>.
 *
 * <p>Ce fichier tenait une liste de {@code basicItem(...)}, une ligne par objet. Cette
 * liste est la source d'un bug qui s'est produit <b>deux fois</b> et qui ne se voit
 * jamais en relecture : un objet sans modèle est un objet <b>parfaitement fonctionnel</b>
 * — il se craft, se stacke, s'insère dans les machines, apparaît dans l'onglet créatif —
 * qui s'affiche simplement en cube violet dans l'inventaire et dans la main. Aucune
 * exception, aucun avertissement au chargement, rien dans les tests : le jeu ne considère
 * pas qu'il manque quelque chose, il affiche le modèle « manquant », qui est un modèle
 * valide. Les cinq matériaux du T3 sont partis ainsi, et la Veskorian Alloy Forge avec eux.
 *
 * <p>La liste a donc disparu. On parcourt le registre : tout objet du mod reçoit son
 * modèle, et ajouter un objet sans y penser <b>devient impossible</b> plutôt que
 * seulement risqué. Deux exceptions, toutes deux justifiées par ce qui produit le modèle
 * ailleurs :
 * <ul>
 *   <li>les {@link BlockItem} — leur modèle vient du modèle de bloc, produit par
 *       {@code ModBlockStateProvider} (même correction, même raison) ;</li>
 *   <li>les œufs d'apparition — modèle vanilla teinté, pas une texture à nous.</li>
 * </ul>
 *
 * <p>Le filet de sécurité est réel : {@code basicItem} passe par l'{@code
 * ExistingFileHelper}, qui <b>échoue la génération</b> si la texture correspondante
 * n'existe pas. Un objet ajouté sans texture casse donc {@code runData} au lieu de
 * s'afficher en violet trois heures plus tard en jeu.
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Veskorius.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            String name = holder.getId().getPath();

            // UN ItemNameBlockItem N'EST PAS UN BlockItem ORDINAIRE, et la nuance coûte cher.
            //
            // Il en hérite, mais il porte son PROPRE nom et sa propre texture : la Graine
            // Ancienne pose un buisson de floraison, sans s'appeler comme lui ni lui
            // ressembler. Le modèle du bloc ne peut donc pas lui servir — et comme le
            // buisson est un bloc à croissance, il ne produit même pas de modèle d'objet.
            //
            // Résultat sans ce cas : une graine parfaitement fonctionnelle, plantable,
            // récoltable, et affichée en cube violet. Exactement le bug que ce fichier
            // existe pour empêcher, revenu par la porte de la sous-classe.
            if (item instanceof BlockItem && !(item instanceof net.minecraft.world.item.ItemNameBlockItem)) {
                // Le modèle d'objet d'un bloc est le modèle du bloc lui-même.
                continue;
            }
            if (item instanceof DeferredSpawnEggItem) {
                // Modèle vanilla : les deux couleurs viennent de l'objet, pas d'une texture.
                withExistingParent(name, mcLoc("item/template_spawn_egg"));
                continue;
            }
            if (item instanceof net.minecraft.world.item.TieredItem) {
                // handheld et non generated : c'est ce parent qui incline l'objet dans la
                // main. Une épée en `generated` se tient à plat devant le personnage, ce
                // qui ne saute aux yeux qu'une fois l'objet équipé.
                singleTexture(name, mcLoc("item/handheld"), "layer0",
                    modLoc("item/" + name));
                continue;
            }
            basicItem(item);
        }
    }
}
