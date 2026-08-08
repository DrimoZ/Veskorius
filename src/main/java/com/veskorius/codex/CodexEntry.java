package com.veskorius.codex;

import com.veskorius.item.CodexFragmentItem;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Une entrée (page) du Codex de Résonance (15-Codex-Guidebook.md). Le texte n'est pas
 * ici : il vit dans les fichiers de langue, sous les clés {@code codex.<ns>.<path>.title}
 * et {@code .text} — la même convention que les fragments ({@link CodexFragmentItem}),
 * pour que les entrées de lore réutilisent le texte des fragments sans le réécrire.
 */
public final class CodexEntry {

    private final ResourceLocation id;
    private final CodexCategory category;
    private final Supplier<? extends ItemLike> icon;
    private final CodexUnlock unlock;

    /**
     * Palier auquel cette entrée appartient : 0 pour l'introduction, 1 à 5 pour les
     * paliers, −1 pour le lore, qui n'en a pas.
     *
     * <p>C'est ce qui permet de <b>dessiner</b> la progression au lieu de l'énumérer. Une
     * liste de soixante entrées ne dit rien de l'ordre dans lequel on les rencontre ; des
     * colonnes par palier et des flèches entre elles le disent d'un coup d'œil.
     */
    private final int tier;

    public CodexEntry(ResourceLocation id, CodexCategory category,
                      Supplier<? extends ItemLike> icon, CodexUnlock unlock, int tier) {
        this.id = id;
        this.category = category;
        this.icon = icon;
        this.unlock = unlock;
        this.tier = tier;
    }

    /** Voir {@link #tier}. −1 = hors progression (le lore). */
    public int tier() {
        return tier;
    }

    /**
     * <b>Le texte est-il caché tant que l'entrée n'est pas découverte ?</b> Seul le lore
     * l'est. Tout le reste — machines, matériaux, mécaniques — se lit <b>dès le départ</b>.
     *
     * <p>C'était l'inverse, et c'était une erreur de conception, pas un réglage : un manuel
     * qui n'explique une machine <i>qu'une fois qu'on l'a fabriquée</i> n'apprend rien à
     * personne. On ne peut pas deviner qu'il faut de l'or plutôt que du fer à la Forge si
     * la page qui le dit n'apparaît qu'après avoir forgé. Le Codex sert à décider quoi
     * faire ensuite ; le verrouiller derrière le fait de l'avoir déjà fait le vide de sa
     * fonction.
     *
     * <p>Les fragments de lore, eux, restent scellés — ce sont des objets à trouver, et
     * leur intérêt EST d'être trouvés. La condition de déblocage garde donc tout son rôle
     * ailleurs : elle marque ce que le joueur a <b>rencontré</b> (compteur de progression,
     * bulle de notification), sans jamais barrer la lecture.
     */
    public boolean hidesTextUntilFound() {
        return unlock.type() == CodexUnlock.Type.FRAGMENT;
    }

    public ResourceLocation id() {
        return id;
    }

    public CodexCategory category() {
        return category;
    }

    public ItemStack icon() {
        return new ItemStack(icon.get());
    }

    public CodexUnlock unlock() {
        return unlock;
    }

    public String titleKey() {
        return CodexFragmentItem.titleKey(id);
    }

    public String textKey() {
        return CodexFragmentItem.textKey(id);
    }
}
