package com.veskorius.codex;

import com.veskorius.block.ModBlocks;
import com.veskorius.item.ModItems;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Catégories du Codex de Résonance (15-Codex-Guidebook.md). L'ordre de déclaration
 * est l'ordre d'affichage. Chaque catégorie a une icône (objet représentatif, résolue
 * paresseusement pour ne pas toucher les registres au chargement de la classe) et une
 * clé de langue {@code codex.category.<id>}.
 */
public enum CodexCategory {
    INTRO("intro", () -> Items.BOOK),
    CRYSTALS("crystals", ModItems.RAW_RESONANCE_CRYSTAL),
    FIELDS("fields", ModBlocks.FIELD_EMITTER),
    MACHINES("machines", ModBlocks.RESONANCE_STABILIZER),
    WORLD("world", ModBlocks.RESONANCE_VEINED_STONE),
    FAUNA("fauna", ModItems.RESONANCE_SPORE),
    LORE("lore", ModItems.CODEX_FRAGMENT),
    PROGRESSION("progression", ModItems.RESONANCE_BLUEPRINT);

    private final String id;
    private final Supplier<? extends ItemLike> icon;

    CodexCategory(String id, Supplier<? extends ItemLike> icon) {
        this.id = id;
        this.icon = icon;
    }

    public String id() {
        return id;
    }

    public ItemStack icon() {
        return new ItemStack(icon.get());
    }

    public String titleKey() {
        return "codex.category." + id;
    }
}
