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

    public CodexEntry(ResourceLocation id, CodexCategory category,
                      Supplier<? extends ItemLike> icon, CodexUnlock unlock) {
        this.id = id;
        this.category = category;
        this.icon = icon;
        this.unlock = unlock;
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
