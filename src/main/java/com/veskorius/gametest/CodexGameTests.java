package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.codex.CodexCategory;
import com.veskorius.codex.CodexEntry;
import com.veskorius.codex.CodexRegistry;
import com.veskorius.codex.CodexUnlock;
import com.veskorius.codex.CodexUnlocks;
import com.veskorius.item.CodexEntries;
import com.veskorius.item.ModItems;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tests de la logique de déblocage du Codex (15-Codex-Guidebook.md). L'état vit sur le
 * joueur (attachment), donc testable via un joueur factice — sans dépendre du rendu du
 * {@code CodexScreen} (visuel, vérifié en {@code runClient}).
 */
@GameTestHolder(Veskorius.MOD_ID)
@PrefixGameTestTemplate(false)
public class CodexGameTests {

    private static final String EMPTY = "empty";

    private static ResourceLocation entry(String path) {
        return ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path);
    }

    /**
     * <b>Les zones du Codex ne se croisent jamais, et le cadre ne sort jamais de l'écran.</b>
     *
     * <p>Ce test existe parce que le défaut est revenu <b>deux fois</b> : du texte
     * superposé, à chaque fois parce que des coordonnées étaient calculées à la main au
     * moment de dessiner. Rien ne pouvait le signaler — il fallait ouvrir le jeu, regarder,
     * et tomber sur le bon écran. La dernière fois, le lien de retour, l'icône et le titre
     * tombaient à quatre pixels les uns des autres.
     *
     * <p>On fait donc calculer la mise en page pour une cinquantaine de tailles de fenêtre,
     * des plus petites que Minecraft accepte aux plus grandes, et on vérifie deux choses
     * qu'aucun coup d'œil ne garantit : que les quatre bandes sont <b>disjointes</b>, et
     * que le cadre <b>tient dans l'écran</b>. Le second point est la seconde plainte : sur
     * une fenêtre réduite, le livre débordait.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void codexLayoutNeverOverlaps(GameTestHelper helper) {
        int tabs = com.veskorius.codex.CodexCategory.values().length + 1;
        for (int w = 320; w <= 1920; w += 100) {
            for (int h = 240; h <= 1080; h += 120) {
                var layout = com.veskorius.client.screen.CodexLayout.of(w, h, tabs);
                helper.assertTrue(layout.fitsIn(w, h),
                    "Fenêtre " + w + "x" + h + " : le cadre déborde de l'écran — "
                        + layout.frame());
                var zones = layout.disjointZones();
                for (int i = 0; i < zones.length; i++) {
                    for (int j = i + 1; j < zones.length; j++) {
                        helper.assertFalse(zones[i].overlaps(zones[j]),
                            "Fenêtre " + w + "x" + h + " : deux zones se croisent — "
                                + zones[i] + " et " + zones[j]
                                + ". C'est exactement ainsi que le texte se superpose.");
                    }
                }
                helper.assertTrue(layout.pageLeft().right() < layout.pageRight().x(),
                    "Fenêtre " + w + "x" + h + " : les deux pages se touchent, "
                        + "le texte de gauche mordrait sur celui de droite");
            }
        }
        helper.succeed();
    }

    /**
     * <b>Chaque entrée du Codex a un texte, et il n'est pas vide.</b>
     *
     * <p>Sans texte, le manuel affiche sa <b>clé de traduction brute</b> —
     * {@code codex.veskorius.machines.forge.text} en pleine page. Rien ne plante, rien
     * n'avertit : on ajoute une entrée au registre, on oublie sa langue, et le joueur
     * ouvre une page qui lui parle en code. C'est arrivé à l'échelle d'un palier entier —
     * le Codex s'arrêtait au T2 pendant que le mod allait au T5.
     *
     * <p>On vérifie sur le <b>gestionnaire de langue chargé</b>, donc sur ce que le joueur
     * verra, et pas sur un fichier de génération.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void everyCodexEntryHasRealText(GameTestHelper helper) {
        for (com.veskorius.codex.CodexEntry entry : com.veskorius.codex.CodexRegistry.all()) {
            for (String key : new String[] {entry.titleKey(), entry.textKey()}) {
                String rendered = net.minecraft.network.chat.Component.translatable(key).getString();
                helper.assertFalse(rendered.equals(key),
                    "Entrée " + entry.id() + " : la clé « " + key + " » n'a aucune "
                        + "traduction, la page afficherait la clé elle-même");
            }
            // La longueur minimale ne vaut que pour le CORPS. Le premier jet l'appliquait
            // aussi aux titres et refusait « Custode » — sept caractères, et le bon titre.
            // Un seuil qui rejette la réponse juste est un seuil mal placé.
            String body = net.minecraft.network.chat.Component
                .translatable(entry.textKey()).getString();

            // DEUX SEUILS, parce que les entrées n'ont pas le même métier. Un fragment de
            // lore est un billet trouvé dans une ruine : trois lignes, et c'est très bien.
            // Une entrée de machine doit permettre de décider quoi construire SANS avoir
            // l'objet — donc dire ce qu'elle fait, ce qui ne se devine pas, et quoi faire
            // ensuite. Trente et une d'entre elles tenaient en une phrase de cent
            // caractères ; on ne pouvait pas progresser en les lisant.
            int floor = entry.hidesTextUntilFound() ? 40 : 200;
            helper.assertTrue(body.length() >= floor,
                "Entrée " + entry.id() + " : le corps fait " + body.length()
                    + " caractères, minimum " + floor + " — trop court pour apprendre "
                    + "quoi que ce soit");
        }
        helper.succeed();
    }

    /** Un joueur neuf : les entrées ALWAYS comptent débloquées, les ITEM non. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void freshPlayerUnlocksAlwaysOnly(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(CodexUnlocks.isUnlocked(player, entry("intro/welcome")),
            "L'intro (ALWAYS) doit être débloquée d'office");
        helper.assertFalse(CodexUnlocks.isUnlocked(player, entry("crystals/raw")),
            "Une entrée ITEM ne doit pas être débloquée sur un joueur neuf");
        helper.assertTrue(CodexUnlocks.unlocked(player).isEmpty(),
            "Aucune entrée ne doit être stockée au départ (ALWAYS n'est pas stockée)");
        helper.succeed();
    }

    /** {@code unlock} est idempotent : nouveau la première fois seulement, sans doublon. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void unlockIsIdempotent(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ResourceLocation raw = entry("crystals/raw");
        helper.assertTrue(CodexUnlocks.unlock(player, raw),
            "Le premier déblocage doit être signalé comme nouveau");
        helper.assertFalse(CodexUnlocks.unlock(player, raw),
            "Un second déblocage de la même entrée n'est pas nouveau");
        helper.assertTrue(CodexUnlocks.isUnlocked(player, raw),
            "L'entrée doit désormais être débloquée");
        helper.assertTrue(CodexUnlocks.unlocked(player).size() == 1,
            "L'entrée ne doit être stockée qu'une fois, taille : " + CodexUnlocks.unlocked(player).size());
        helper.succeed();
    }

    /**
     * Cœur de la demande : la connaissance s'accumule même SANS Codex dans l'inventaire.
     * Le joueur possède un Cristal Brut mais aucun Codex ; le scan débloque quand même
     * son entrée.
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void knowledgeAccumulatesWithoutHoldingCodex(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get()));

        helper.assertTrue(CodexUnlocks.findCodex(player).isEmpty(),
            "Ce test suppose que le joueur ne porte PAS de Codex");
        CodexUnlocks.grantForItem(player);

        helper.assertTrue(CodexUnlocks.isUnlocked(player, entry("crystals/raw")),
            "Posséder le Cristal Brut débloque son entrée, même sans Codex en main");
        helper.assertFalse(CodexUnlocks.isUnlocked(player, entry("crystals/refined")),
            "Une entrée dont l'objet n'est pas possédé reste verrouillée");
        helper.succeed();
    }

    /** Gagner un advancement débloque la page de palier correspondante, pas les autres. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void grantForAdvancementUnlocksTierEntry(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CodexUnlocks.grantForAdvancement(player, entry("tier1_awakening"));

        helper.assertTrue(CodexUnlocks.isUnlocked(player, entry("progression/tier1")),
            "L'advancement tier1_awakening doit débloquer la page Palier 1");
        helper.assertFalse(CodexUnlocks.isUnlocked(player, entry("progression/tier2")),
            "La page Palier 2 ne doit pas être débloquée par un autre advancement");
        helper.succeed();
    }

    /** Lire un fragment débloque sa page de lore ; le chemin FRAGMENT ne touche que ces pages. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void grantForFragmentUnlocksLoreOnly(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CodexUnlocks.grantForFragment(player, CodexEntries.DAILY_LIFE_LAMPS);
        helper.assertTrue(CodexUnlocks.isUnlocked(player, CodexEntries.DAILY_LIFE_LAMPS),
            "Lire le fragment doit débloquer sa page de lore");

        // Le chemin FRAGMENT ne doit pas débloquer une entrée d'un autre type.
        CodexUnlocks.grantForFragment(player, entry("crystals/raw"));
        helper.assertFalse(CodexUnlocks.isUnlocked(player, entry("crystals/raw")),
            "grantForFragment ne doit pas débloquer une entrée ITEM");
        helper.succeed();
    }

    /**
     * Intégrité du catalogue : ids uniques, catégorie non nulle, et cible cohérente
     * avec le type de déblocage. Empêche qu'un ajout futur casse silencieusement le
     * système (ex. une entrée ITEM sans objet, jamais débloquable).
     */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void catalogIntegrity(GameTestHelper helper) {
        Set<ResourceLocation> seen = new HashSet<>();
        for (CodexEntry e : CodexRegistry.all()) {
            helper.assertTrue(seen.add(e.id()), "Id d'entrée dupliqué : " + e.id());
            helper.assertTrue(e.category() != null, "Catégorie nulle sur : " + e.id());
            helper.assertTrue(e.unlock() != null, "Déblocage nul sur : " + e.id());
            CodexUnlock.Type type = e.unlock().type();
            if (type == CodexUnlock.Type.ITEM) {
                helper.assertTrue(e.unlock().item() != null,
                    "Entrée ITEM sans objet (jamais débloquable) : " + e.id());
            }
            if (type == CodexUnlock.Type.ADVANCEMENT) {
                helper.assertTrue(e.unlock().advancement() != null,
                    "Entrée ADVANCEMENT sans advancement : " + e.id());
            }
        }
        // Chaque catégorie a au moins une entrée (une catégorie vide serait un oubli).
        for (CodexCategory category : CodexCategory.values()) {
            helper.assertFalse(CodexRegistry.byCategory(category).isEmpty(),
                "Catégorie sans aucune entrée : " + category.id());
        }
        helper.succeed();
    }

    /** Le compteur de catégorie inclut les ALWAYS et suit les déblocages. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void categoryCountTracksUnlocks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        int introTotal = CodexRegistry.byCategory(CodexCategory.INTRO).size();
        // Toutes les entrées d'intro sont ALWAYS : comptées débloquées d'office.
        helper.assertTrue(CodexUnlocks.unlockedCount(player, CodexCategory.INTRO) == introTotal,
            "Les entrées d'intro (ALWAYS) doivent toutes compter débloquées");

        int before = CodexUnlocks.unlockedCount(player, CodexCategory.CRYSTALS);
        CodexUnlocks.unlock(player, entry("crystals/raw"));
        int after = CodexUnlocks.unlockedCount(player, CodexCategory.CRYSTALS);
        helper.assertTrue(after == before + 1,
            "Débloquer une entrée doit incrémenter le compteur de sa catégorie");
        helper.succeed();
    }
}
