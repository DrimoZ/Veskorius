package com.veskorius.codex;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

/**
 * Condition de déblocage d'une entrée de Codex (15-Codex-Guidebook.md). Quatre types,
 * chacun détecté par un mécanisme distinct (voir {@code CodexEventHandler} et
 * {@code CodexUnlocks}) :
 * <ul>
 *   <li>{@link Type#ALWAYS} — débloquée dès le départ.</li>
 *   <li>{@link Type#ITEM} — le joueur possède l'objet (scan d'inventaire throttlé).</li>
 *   <li>{@link Type#ADVANCEMENT} — le joueur gagne l'advancement (événement).</li>
 *   <li>{@link Type#FRAGMENT} — le joueur lit le {@code codex_fragment} d'id égal.</li>
 * </ul>
 */
public final class CodexUnlock {

    public enum Type { ALWAYS, ITEM, ADVANCEMENT, FRAGMENT }

    private final Type type;
    @Nullable
    private final Supplier<? extends ItemLike> item;
    @Nullable
    private final ResourceLocation advancement;

    private CodexUnlock(Type type, @Nullable Supplier<? extends ItemLike> item,
                        @Nullable ResourceLocation advancement) {
        this.type = type;
        this.item = item;
        this.advancement = advancement;
    }

    public static CodexUnlock always() {
        return new CodexUnlock(Type.ALWAYS, null, null);
    }

    public static CodexUnlock item(Supplier<? extends ItemLike> item) {
        return new CodexUnlock(Type.ITEM, item, null);
    }

    public static CodexUnlock advancement(ResourceLocation advancement) {
        return new CodexUnlock(Type.ADVANCEMENT, null, advancement);
    }

    /** Débloquée en lisant le fragment de même id (lore). */
    public static CodexUnlock fragment() {
        return new CodexUnlock(Type.FRAGMENT, null, null);
    }

    public Type type() {
        return type;
    }

    @Nullable
    public ItemLike item() {
        return item == null ? null : item.get();
    }

    @Nullable
    public ResourceLocation advancement() {
        return advancement;
    }
}
