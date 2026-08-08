package com.veskorius.gametest;

import com.veskorius.Veskorius;
import com.veskorius.block.FieldSensitiveBlock;
import com.veskorius.block.ModBlocks;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.entity.ModEntities;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tests des pièces de structure jigsaw (08-Structures.md pour le contenu, 17-Dungeons.md
 * pour la forme). On valide le <b>contenu</b> des pièces NBT générées et les trois
 * mécaniques qui font du donjon autre chose qu'une enfilade de salles : le sas, les
 * conduits, l'émetteur ancien.
 *
 * <p>Le câblage jigsaw lui-même (pools / structure / structure_set) est validé par les
 * codecs au datagen et par le chargement du serveur de test, qui échouerait si un
 * registre datapack était invalide.
 */
/**
 * <b>Namespace séparé, et c'est le point.</b> Le runner headless ne sait filtrer que par
 * NAMESPACE — pas par test, pas par classe. Ces vingt-deux tests posent des donjons
 * entiers (l'Avant-poste fait 39×31×39, l'Archive 23×16×52) : ce sont eux qui coûtent
 * la mémoire et l'essentiel du temps. Les isoler ici permet de lancer les cent trente-six
 * autres seuls, en quelques secondes, et de ne payer le prix fort qu'avant un commit.
 */
@GameTestHolder(WorldGenTests.NAMESPACE)
@PrefixGameTestTemplate(false)
public class StructureGameTests {

    // LES ARÈNES SONT GÉNÉRÉES SOUS LES DEUX NAMESPACES, et il faut savoir pourquoi.
    //
    // @PrefixGameTestTemplate(false) désactive un préfixe de NOM, pas le namespace : le
    // gabarit est TOUJOURS cherché sous celui du @GameTestHolder. Cette classe ayant changé
    // de namespace pour devenir filtrable, le serveur a réclamé « veskorius_world:piece_arena »
    // et s'est écrasé avant le premier tick — pas un test rouge, un crash du serveur.
    //
    // Qualifier le nom à la main ne marche pas non plus : NeoForge concatène, ce qui donne
    // « veskorius_world:veskorius:piece_arena » et une ResourceLocation invalide. La seule
    // issue est d'écrire les arènes sous les deux namespaces — voir
    // ModStructureTemplateProvider, qui les duplique pour cette unique raison.
    private static final String FIELD_ARENA = "field_arena";
    /** Arène élargie : l'Avant-poste fait 39×31×39 et ne tient dans aucune arène standard. */
    private static final String PIECE_ARENA = "piece_arena";

    private static final BlockPos ANCHOR = new BlockPos(1, 1, 1);

    // Repères de la pièce de départ de l'Avant-poste (voir ModStructurePieceProvider) :
    // le chemin critique du T2, celui qui n'a le droit d'être ni tiré au sort ni usé.
    private static final BlockPos CONSOLE = ANCHOR.offset(31, 4, 22);
    private static final BlockPos BULKHEAD = ANCHOR.offset(24, 3, 22);
    private static final BlockPos ANCIENT_EMITTER = ANCHOR.offset(13, 4, 22);
    private static final BlockPos RESERVE_CHEST = ANCHOR.offset(16, 4, 22);
    private static final BlockPos BOOTSTRAP_CHEST = ANCHOR.offset(6, 3, 16);

    // =====================================================================
    // Le chemin critique
    // =====================================================================

    /**
     * <b>Tout le chemin critique du T2 est dans la pièce de départ.</b>
     *
     * <p>C'est l'invariant de 17-Dungeons.md §3, et il n'existait pas tant que la
     * profondeur d'assemblage valait 1 : il n'y avait qu'une pièce, donc rien à égarer.
     * Depuis que le jigsaw assemble réellement, déplacer par mégarde la console ou le sas
     * dans une aile facultative rendrait une partie des Avant-postes infranchissables —
     * exactement la classe de bug déjà trouvée deux fois sur le loot d'amorçage.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void outpostStartPieceCarriesTheWholeT2Gate(GameTestHelper helper) {
        place(helper, "outpost");

        helper.assertBlockPresent(ModBlocks.ATTUNEMENT_CONSOLE.get(), CONSOLE);
        helper.assertBlockPresent(ModBlocks.RESONANCE_BULKHEAD.get(), BULKHEAD);
        helper.assertBlockPresent(ModBlocks.ANCIENT_EMITTER.get(), ANCIENT_EMITTER);
        helper.assertBlockPresent(Blocks.CHEST, RESERVE_CHEST);
        helper.assertBlockPresent(Blocks.CHEST, BOOTSTRAP_CHEST);
        // Maçonnerie veskorienne, pas du deepslate vanilla : la coquille elle-même doit
        // dire de quelle civilisation on visite les ruines.
        helper.assertBlockPresent(ModBlocks.VEINED_STONE_BRICKS.get(), ANCHOR.offset(3, 20, 17));
        helper.assertEntityPresent(ModEntities.CUSTODE.get());
        assertChestHasLootTable(helper, BOOTSTRAP_CHEST);
        helper.succeed();
    }

    /**
     * <b>Le coffre-réserve contient de quoi ouvrir le sas, toujours.</b>
     *
     * <p>Le sas ne s'ouvre que dans un champ, et le seul champ disponible sur place vient
     * de l'émetteur ancien, qui est à sec. Un joueur arrivé sans Stable Crystal serait
     * donc coincé devant la porte du T2. Contenu <b>fixe</b> et non table de loot : une
     * table rendrait la porte du T2 aléatoire, ce que le pilier 2 interdit.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void outpostReserveChestHoldsTheFuelToOpenTheGate(GameTestHelper helper) {
        place(helper, "outpost");

        net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(RESERVE_CHEST);
        helper.assertTrue(be instanceof net.minecraft.world.Container,
            "Coffre-réserve attendu en " + RESERVE_CHEST + ", vaut : " + be);
        ItemStack stack = ((net.minecraft.world.Container) be).getItem(0);
        helper.assertTrue(stack.is(ModItems.STABLE_RESONANCE_CRYSTAL.get()) && stack.getCount() >= 1,
            "Le coffre-réserve doit contenir au moins un Stable Resonance Crystal (le "
                + "carburant qui réveille l'émetteur ancien), vaut : " + stack);
        helper.succeed();
    }

    /**
     * <b>Aucune aile facultative ne porte de chemin critique.</b> Le pendant du test
     * ci-dessus : on vérifie non seulement que la pièce de départ a tout, mais que les
     * pièces tirées au sort n'ont rien. Sans ça, l'invariant §3 pourrait se rompre en
     * ajoutant une aile « qui contient aussi une console, ce sera plus pratique ».
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 60)
    public static void optionalWingsNeverCarryTheCriticalPath(GameTestHelper helper) {
        for (String wing : new String[] {
            "outpost_wing_store", "outpost_wing_quarters", "outpost_wing_collapsed",
            "hamlet", "hamlet_dwelling", "hamlet_workshop", "hamlet_cistern",
            "hamlet_collapsed", "ruin_marker", "ruin_marker_pillar", "sunken_chamber",
            "drill_shaft"}) {
            place(helper, wing);
            for (BlockPos pos : allPositions(helper, 25, 17, 25)) {
                BlockState state = helper.getBlockState(pos);
                helper.assertTrue(!state.is(ModBlocks.ATTUNEMENT_CONSOLE.get())
                        && !state.is(ModBlocks.RESONANCE_BULKHEAD.get())
                        && !state.is(ModBlocks.ANCIENT_EMITTER.get()),
                    "La pièce facultative « " + wing + " » porte un élément du chemin "
                        + "critique en " + pos + " (" + state + ") — il doit vivre dans la "
                        + "pièce de départ, jamais dans un pool");
            }
        }
        helper.succeed();
    }

    /**
     * <b>Le donjon se traverse réellement, de l'entrée à la console.</b>
     *
     * <p>C'est le test le plus important du fichier, et celui qui manquait. Un donjon
     * écrit par code peut être <b>parfaitement valide et infranchissable</b> : une galerie
     * qui ne perce pas le mur qu'elle est censée relier, un escalier auquel il manque un
     * bloc de dégagement au sommet, une salle murée par le pan de voûte qu'on vient d'y
     * faire tomber. Rien ne casse, rien ne lève d'exception — la structure se génère
     * magnifiquement, et le joueur se retrouve devant de la roche. Ces trois défauts-là
     * étaient présents, et aucune relecture ne les avait vus ; c'est un parcours
     * automatique qui les a trouvés.
     *
     * <p>On parcourt donc les cases <b>où un joueur tient debout</b> (un bloc de sol, deux
     * d'air), depuis le vestibule, et on exige d'atteindre la console. Le sas est traité
     * comme franchissable : il l'est, une fois l'émetteur réveillé, et ce n'est pas ce que
     * ce test mesure.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void outpostIsWalkableFromEntranceToConsole(GameTestHelper helper) {
        place(helper, "outpost");
        assertWalkable(helper, "outpost",
            ANCHOR.offset(22, 18, 11),   // vestibule, hors du cône d'éboulis
            ANCHOR.offset(30, 4, 22),    // devant la console, sur l'estrade
            39, 31, 39);
    }

    /**
     * Parcourt les cases <b>où un joueur tient debout</b> depuis {@code start}, et exige
     * d'atteindre {@code goal}. Tolérances d'un pas réel : monter d'un bloc, descendre de
     * trois.
     */
    private static void assertWalkable(GameTestHelper helper, String piece,
                                       BlockPos start, BlockPos goal, int w, int h, int d) {
        helper.assertTrue(standable(helper, start),
            "Le point de départ de « " + piece + " » doit être praticable");
        java.util.Set<BlockPos> seen = new java.util.HashSet<>();
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos at = queue.poll();
            if (at.equals(goal)) {
                helper.succeed();
                return;
            }
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                for (int dy = 1; dy >= -3; dy--) {
                    BlockPos next = at.relative(dir).offset(0, dy, 0);
                    if (seen.contains(next) || !inBox(next, w, h, d) || !standable(helper, next)) {
                        continue;
                    }
                    seen.add(next);
                    queue.add(next);
                    break;
                }
            }
        }
        helper.fail("Dans « " + piece + " », " + goal.subtract(ANCHOR) + " n'est pas atteignable "
            + "depuis " + start.subtract(ANCHOR) + " : la structure est murée quelque part ("
            + seen.size() + " cases explorées)");
    }

    /**
     * <b>Le Poste de Garde se descend, de la meurtrière à l'arsenal.</b>
     *
     * <p>Une tour à quatre paliers desservis par une seule vis est le cas où le défaut
     * « valide mais infranchissable » est le plus probable : chaque ouverture de palier
     * doit tomber <b>exactement</b> sur la marche de sa hauteur, sinon elle débouche à
     * côté — c'est-à-dire dans le vide du puits. C'est pour ça que les sorties sont
     * <i>calculées</i> ({@code Masonry.spiralExit}) et non placées à l'estime ; ce test
     * vérifie que le calcul est juste.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void guardPostIsWalkableFromSlitToArsenal(GameTestHelper helper) {
        place(helper, "guard_post");
        assertWalkable(helper, "guard_post",
            ANCHOR.offset(13, 28, 6), ANCHOR.offset(13, 5, 20), 27, 32, 27);
    }

    /**
     * <b>Le Puits de Forage se descend sans se tuer.</b>
     *
     * <p>Il n'a pas d'escalier — c'est un chantier abandonné, on descend de plateforme en
     * plateforme — et c'est justement pour ça qu'il a besoin de ce test : une plateforme
     * de trop retirée, ou décalée d'un bloc, et la « descente » devient une chute de
     * quinze mètres. Le parcours n'autorise que ce qu'un joueur encaisse sans y penser
     * (trois blocs), donc il échoue avant le joueur.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void drillShaftIsDescendableToTheCrystal(GameTestHelper helper) {
        place(helper, "drill_shaft");
        assertWalkable(helper, "drill_shaft",
            ANCHOR.offset(6, 24, 7), ANCHOR.offset(8, 5, 9), 15, 28, 15);
    }

    /**
     * <b>Le Sigma se traverse, des serres à la console.</b> Le sas y est traité comme
     * franchissable — c'est le puzzle des relais qui l'ouvre, et ce test-là mesure la
     * CIRCULATION, pas la serrure. Il garde en revanche le déambulatoire : une salle
     * centrale scellée n'a de sens que si l'on peut en faire le tour, et le réflexe de
     * percer le sanctuaire pour relier deux ailes est exactement ce qu'il interdit.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void sigmaIsWalkableFromGreenhousesToConsole(GameTestHelper helper) {
        place(helper, "sigma_laboratory");
        assertWalkable(helper, "sigma_laboratory",
            ANCHOR.offset(13, 26, 4), ANCHOR.offset(18, 5, 32), 39, 36, 39);
    }

    /**
     * <b>La chaîne du Sigma tient géométriquement.</b>
     *
     * <p>Tout le puzzle du T3 repose sur <b>trois distances</b>, et sur rien d'autre :
     * l'émetteur encore vivant atteint le relais A, A atteint le relais B, B atteint le
     * sas — et <b>A n'atteint PAS le sas</b>. C'est cette dernière inégalité qui fait
     * exister le puzzle : sans elle, un seul relais ouvre la porte et les quatre-vingt-dix
     * secondes ne servent plus à rien.
     *
     * <p>Elle se joue à un bloc près (A→sas vaut 27 pour une portée de 20), et rien dans le
     * code ne la protège : déplacer une salle de deux mètres pour la faire mieux tomber
     * suffit à la rompre, sans qu'aucune erreur n'apparaisse. D'où ce test, qui lit les
     * positions <b>dans la pièce générée</b> plutôt que dans des constantes — c'est la
     * géométrie réelle qu'on vérifie.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void sigmaRelayChainIsGeometricallySound(GameTestHelper helper) {
        place(helper, "sigma_laboratory");
        int range = 20;

        java.util.List<BlockPos> relays = new java.util.ArrayList<>();
        BlockPos bulkhead = null;
        BlockPos emitter = null;
        for (BlockPos pos : BlockPos.betweenClosed(ANCHOR, ANCHOR.offset(38, 35, 38))) {
            BlockState state = helper.getBlockState(pos);
            if (state.is(ModBlocks.DAMAGED_RELAY.get())) {
                relays.add(pos.immutable());
            } else if (state.is(ModBlocks.RESONANCE_BULKHEAD.get()) && bulkhead == null) {
                bulkhead = pos.immutable();
            } else if (state.is(ModBlocks.ANCIENT_EMITTER.get()) && emitter == null) {
                emitter = pos.immutable();
            }
        }
        helper.assertTrue(relays.size() == 2,
            "Le Sigma doit porter exactement deux relais, trouvé : " + relays.size());
        helper.assertTrue(bulkhead != null && emitter != null,
            "Le Sigma doit porter un sas et un émetteur ancien");

        // Le relais des serres est celui que l'émetteur atteint ; l'autre est celui du pont.
        BlockPos a = relays.get(0).distSqr(emitter) < relays.get(1).distSqr(emitter)
            ? relays.get(0) : relays.get(1);
        BlockPos c = a.equals(relays.get(0)) ? relays.get(1) : relays.get(0);

        assertWithin(helper, "l'émetteur encore vivant", emitter, a, 8, true);
        assertWithin(helper, "le relais des serres", a, c, range, true);
        assertWithin(helper, "le relais du pont", c, bulkhead, range, true);
        // L'INÉGALITÉ QUI FAIT LE PUZZLE.
        assertWithin(helper, "le relais des serres", a, bulkhead, range, false);
        helper.succeed();
    }

    private static void assertWithin(GameTestHelper helper, String who, BlockPos from,
                                     BlockPos to, int range, boolean expected) {
        double d = Math.sqrt(from.distSqr(to));
        helper.assertTrue(d <= range == expected,
            who + " est à " + String.format("%.1f", d) + " blocs de " + to.subtract(ANCHOR)
                + " pour une portée de " + range + " — attendu : "
                + (expected ? "à portée" : "HORS de portée, sinon un seul relais ouvre le sas"));
    }

    /**
     * <b>L'Archive se traverse, du vestibule au cadran.</b> Une enfilade est le plan le
     * plus simple qui soit — et c'est exactement pour ça qu'on la teste : la simplicité
     * du plan n'empêche pas une galerie de rater son mur.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void archiveIsWalkableFromVestibuleToDial(GameTestHelper helper) {
        place(helper, "regional_archive");
        assertWalkable(helper, "regional_archive",
            ANCHOR.offset(7, 3, 3), ANCHOR.offset(11, 3, 33), 23, 16, 52);
    }

    /**
     * <b>La salle profonde est atteignable depuis la salle de lecture.</b>
     *
     * <p>C'est la quatrième fois qu'une pièce ajoutée à un bâtiment risquait de se
     * retrouver murée : une galerie qui ne perce pas son mur produit une salle
     * parfaitement construite et parfaitement inaccessible, sans qu'aucune erreur ne soit
     * levée. Ici le risque est double, puisque le seuil traverse le mur du fond d'une
     * salle qui, elle, était déjà terminée.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void archiveDeepRoomIsReachable(GameTestHelper helper) {
        place(helper, "regional_archive");
        assertWalkable(helper, "regional_archive",
            // On part de x=7 et non du centre : la terrasse de la salle de lecture
            // occupe x 9-13 au niveau du sol, donc le centre n'est PAS praticable.
            ANCHOR.offset(7, 3, 37), ANCHOR.offset(11, 3, 45), 23, 16, 52);
    }

    /**
     * <b>L'énigme de l'Archive est résoluble, et seulement dans le bon ordre.</b>
     *
     * <p>Trois choses doivent tenir ensemble, et aucune n'est vérifiable à l'œil : les
     * <b>quatre cotes</b> sont réellement présentes dans le bâtiment (une cote oubliée dans
     * un coffre déplacé rend la salle de lecture inatteignable — et rien ne le signale) ;
     * les <b>quatre socles</b> sont là ; et l'ordre attendu est bien celui des positions,
     * pas celui de la pose.
     *
     * <p>On vérifie aussi qu'un <b>ordre faux ne résout pas</b>. Sans ce second cas, un
     * comparateur inversé passerait le test en donnant toujours « résolu ».
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void archivePuzzleNeedsTheRightOrder(GameTestHelper helper) {
        place(helper, "regional_archive");

        java.util.List<BlockPos> pedestals = new java.util.ArrayList<>();
        java.util.Set<ResourceLocation> found = new java.util.HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(ANCHOR, ANCHOR.offset(22, 15, 39))) {
            if (helper.getBlockState(pos).is(ModBlocks.ARCHIVE_PEDESTAL.get())) {
                pedestals.add(pos.immutable());
            } else if (helper.getLevel().getBlockEntity(helper.absolutePos(pos))
                instanceof net.minecraft.world.Container box) {
                for (int i = 0; i < box.getContainerSize(); i++) {
                    ItemStack stack = box.getItem(i);
                    if (stack.is(ModItems.CODEX_FRAGMENT.get())) {
                        found.add(com.veskorius.item.CodexFragmentItem.entryOf(stack));
                    }
                }
            }
        }
        helper.assertTrue(pedestals.size() == 4,
            "L'Archive doit porter quatre socles, trouvé : " + pedestals.size());
        for (ResourceLocation entry : com.veskorius.item.CodexEntries.ARCHIVE_LOG) {
            helper.assertTrue(found.contains(entry),
                "La cote " + entry + " doit être posée quelque part dans l'Archive — sans elle, "
                    + "la salle de lecture est inatteignable et rien ne le signale");
        }

        pedestals.sort(java.util.Comparator
            .comparingInt((BlockPos p) -> p.getX())
            .thenComparingInt(BlockPos::getZ));
        // Ordre FAUX : le premier et le dernier échangés.
        fill(helper, pedestals, new int[] {3, 1, 2, 0});
        helper.assertTrue(!solved(helper, pedestals.get(0)),
            "Un ordre faux ne doit pas résoudre l'énigme");
        // Ordre juste.
        fill(helper, pedestals, new int[] {0, 1, 2, 3});
        helper.assertTrue(solved(helper, pedestals.get(0)),
            "Les quatre cotes dans l'ordre doivent résoudre l'énigme");
        helper.succeed();
    }

    private static void fill(GameTestHelper helper, java.util.List<BlockPos> pedestals, int[] order) {
        for (int i = 0; i < pedestals.size(); i++) {
            var be = (com.veskorius.block.entity.ArchivePedestalBlockEntity)
                helper.getBlockEntity(pedestals.get(i));
            be.take();
            be.place(com.veskorius.item.CodexFragmentItem.of(
                com.veskorius.item.CodexEntries.ARCHIVE_LOG[order[i]]));
        }
    }

    private static boolean solved(GameTestHelper helper, BlockPos any) {
        return com.veskorius.block.entity.ArchivePedestalBlockEntity.solved(
            helper.getLevel(), helper.absolutePos(any));
    }

    private static boolean inBox(BlockPos p, int w, int h, int d) {
        return p.getX() >= 1 && p.getX() <= w && p.getY() >= 1 && p.getY() <= h
            && p.getZ() >= 1 && p.getZ() <= d;
    }

    /**
     * Une case tenable : un appui sous les pieds, et deux blocs libres au-dessus.
     *
     * <p>« Un appui » se mesure sur la <b>forme de collision</b>, pas sur « est-ce un cube
     * plein » : un escalier, une dalle, un gradin portent parfaitement un joueur. La
     * première version exigeait un cube plein et déclarait donc infranchissable une
     * estrade bordée de nez de marche — le test se trompait, pas le donjon.
     */
    private static boolean standable(GameTestHelper helper, BlockPos p) {
        BlockPos below = p.below();
        if (helper.getBlockState(below)
            .getCollisionShape(helper.getLevel(), helper.absolutePos(below)).isEmpty()) {
            return false;
        }
        return passable(helper, p) && passable(helper, p.above());
    }

    private static boolean passable(GameTestHelper helper, BlockPos p) {
        BlockState state = helper.getBlockState(p);
        // Le sas compte comme franchissable : il l'est une fois l'émetteur réveillé, et ce
        // n'est pas ce que ce test mesure.
        return state.getCollisionShape(helper.getLevel(), helper.absolutePos(p)).isEmpty()
            || state.is(ModBlocks.RESONANCE_BULKHEAD.get());
    }

    /**
     * <b>Rien ne flotte.</b> Aucun bloc d'aucune pièce n'a ses six voisins en l'air.
     *
     * <p>Défaut le plus visible et le plus bête de la version précédente : lampes et
     * conduits étaient posés sur la case <b>intérieure</b> adjacente au mur — donc dans le
     * vide — et formaient des rangées de blocs en lévitation le long de chaque paroi. Une
     * relecture ne le voit pas (le code dit « pose un conduit le long du mur ouest », ce
     * qui est exactement ce qu'il fait) ; on ne le voit qu'en jeu, ou avec ce test.
     *
     * <p>La règle qui en découle, et qui vaut pour toute décoration future : elle
     * <b>remplace</b> un bloc de mur, elle ne s'y accole pas. Ce qui doit éclairer le
     * centre d'une salle se suspend à la clé de voûte par une chaîne.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 200)
    public static void nothingFloatsInAnyPiece(GameTestHelper helper) {
        for (String[] piece : PIECES) {
            place(helper, piece[0]);
            int w = Integer.parseInt(piece[1]);
            int h = Integer.parseInt(piece[2]);
            int d = Integer.parseInt(piece[3]);
            for (BlockPos pos : BlockPos.betweenClosed(ANCHOR, ANCHOR.offset(w - 1, h - 1, d - 1))) {
                if (helper.getBlockState(pos).isAir() || !isolated(helper, pos)) {
                    continue;
                }
                helper.fail("Bloc en lévitation dans « " + piece[0] + " » en "
                    + pos.subtract(ANCHOR) + " (" + helper.getBlockState(pos)
                    + ") — une décoration murale doit REMPLACER un bloc de mur, pas s'y accoler");
            }
            clear(helper, w, h, d);
        }
        helper.succeed();
    }

    /** Les pièces et leur gabarit, pour les tests qui balaient un volume entier. */
    private static final String[][] PIECES = {
        {"outpost", "39", "31", "39"},
        {"sigma_laboratory", "39", "36", "39"},
        {"regional_archive", "23", "16", "40"},
        {"guard_post", "27", "32", "27"},
        {"drill_shaft", "15", "28", "15"},
        {"outpost_wing_store", "15", "11", "15"},
        {"outpost_wing_quarters", "15", "11", "15"},
        {"outpost_wing_collapsed", "15", "11", "15"},
        {"hamlet", "25", "17", "25"},
        {"hamlet_dwelling", "15", "12", "15"},
        {"hamlet_workshop", "15", "12", "15"},
        {"hamlet_cistern", "15", "12", "15"},
        {"hamlet_collapsed", "15", "12", "15"},
        {"ruin_marker", "11", "9", "11"},
        {"ruin_marker_pillar", "7", "9", "7"},
        {"sunken_chamber", "17", "13", "17"},
    };

    private static boolean isolated(GameTestHelper helper, BlockPos pos) {
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            if (!helper.getBlockState(pos.relative(dir)).isAir()) {
                return false;
            }
        }
        return true;
    }

    /** Vide l'arène entre deux pièces : sans ça, la précédente fausse la suivante. */
    private static void clear(GameTestHelper helper, int w, int h, int d) {
        for (BlockPos pos : BlockPos.betweenClosed(ANCHOR, ANCHOR.offset(w - 1, h - 1, d - 1))) {
            helper.setBlock(pos, Blocks.AIR);
        }
    }

    /**
     * <b>Les pièces portent de vrais connecteurs jigsaw.</b>
     *
     * <p>Le mod a longtemps annoncé des « structures en jigsaw » alors qu'aucune pièce ne
     * contenait un seul bloc jigsaw et que la profondeur d'assemblage valait 1 : ajouter
     * une pièce au pool <i>remplaçait</i> le bâtiment au lieu de l'agrandir. Rien ne
     * signalait l'écart — la structure se générait parfaitement, simplement toujours
     * seule. Ce test rend la promesse vérifiable.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void piecesExposeRealJigsawConnectors(GameTestHelper helper) {
        assertConnectors(helper, "outpost", 1);        // une aile facultative
        assertConnectors(helper, "outpost_wing_store", 2); // entrée + chaînage
        assertConnectors(helper, "hamlet", 4);         // quatre directions
        assertConnectors(helper, "hamlet_dwelling", 2);
        assertConnectors(helper, "ruin_marker_pillar", 0); // une miette ne s'assemble à rien
        assertConnectors(helper, "outpost_cap", 1);    // le bouchon en a un, et un seul
        helper.succeed();
    }

    private static void assertConnectors(GameTestHelper helper, String name, int expected) {
        StructureTemplate template = template(helper, name);
        int found = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.JIGSAW).size();
        helper.assertTrue(found == expected,
            "La pièce « " + name + " » doit porter " + expected + " connecteur(s) jigsaw, "
                + "trouvé : " + found);
    }

    /**
     * <b>Le journal de l'Avant-poste est complet et dans l'ordre.</b>
     *
     * <p>Les quatre fragments racontent une descente — routine, dérive, la nuit où le
     * réseau a chanté, l'abandon. Un fragment manquant ou interverti casse le seul arc
     * narratif suivi du mod, et c'est le genre de régression qu'on ne voit pas : la
     * structure se génère, les coffres sont là, seul le texte ne raconte plus rien.
     */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void outpostCarriesOperatorLogInOrder(GameTestHelper helper) {
        place(helper, "outpost");

        ResourceLocation[] expected = {
            com.veskorius.item.CodexEntries.OUTPOST_LOG_1,
            com.veskorius.item.CodexEntries.OUTPOST_LOG_2,
            com.veskorius.item.CodexEntries.OUTPOST_LOG_3,
            com.veskorius.item.CodexEntries.OUTPOST_LOG_4,
        };
        for (int i = 0; i < expected.length; i++) {
            BlockPos at = ANCHOR.offset(10 + i * 6, 18, 16);
            net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(at);
            helper.assertTrue(be instanceof net.minecraft.world.Container,
                "Coffre d'archives attendu en " + at + ", vaut : " + be);
            ItemStack stack = ((net.minecraft.world.Container) be).getItem(0);
            helper.assertTrue(stack.is(ModItems.CODEX_FRAGMENT.get()),
                "Le coffre d'archives " + (i + 1) + " doit contenir un fragment, vaut : " + stack);
            ResourceLocation entry = com.veskorius.item.CodexFragmentItem.entryOf(stack);
            helper.assertTrue(expected[i].equals(entry),
                "Fragment " + (i + 1) + " : attendu " + expected[i] + ", trouvé " + entry
                    + " — le journal doit se lire dans l'ordre en traversant la salle");
        }
        helper.succeed();
    }

    // =====================================================================
    // Les trois mécaniques du donjon (17-Dungeons.md R1 et R2)
    // =====================================================================

    private static final BlockPos GATE = new BlockPos(2, 1, 2);
    private static final BlockPos LAMP = new BlockPos(2, 1, 4);
    private static final BlockPos CONDUIT = new BlockPos(4, 1, 2);
    private static final BlockPos EMITTER = new BlockPos(5, 1, 5);

    /**
     * <b>Le sas est fermé sans champ, ouvert avec.</b> La règle R1 du donjon veskorien :
     * la clé n'est jamais un objet, c'est de la Résonance qu'il faut amener sur place.
     * Si ce test tombe, tout le contenu derrière un sas devient soit inaccessible, soit
     * gratuit — et le pilier 3 redevient une mécanique d'usine qu'on laisse à la base.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 120)
    public static void bulkheadOpensOnlyInsideAField(GameTestHelper helper) {
        helper.setBlock(GATE, ModBlocks.RESONANCE_BULKHEAD.get());
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!helper.getBlockState(GATE).getValue(FieldSensitiveBlock.POWERED),
                "Sans champ, le sas doit rester fermé");

            fuelledEmitter(helper);
            // Les blocs sensibles au champ se vérifient sur un tick programmé (≈ 2 à 4 s)
            // plutôt qu'à chaque tick : un donjon en compte des dizaines, et l'information
            // ne change qu'une fois par partie.
            helper.runAfterDelay(90, () -> {
                helper.assertTrue(helper.getBlockState(GATE).getValue(FieldSensitiveBlock.POWERED),
                    "Dans un champ actif, le sas doit s'ouvrir");
                helper.succeed();
            });
        });
    }

    /**
     * <b>Les conduits et les lampes s'allument avec le champ.</b> La règle R2 : c'est ce
     * qui rend l'état du réseau lisible sans carte ni GUI, et c'est le retour visuel qui
     * récompense le geste de rallumer l'émetteur ancien.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 120)
    public static void conduitsAndLampsFollowTheField(GameTestHelper helper) {
        helper.setBlock(LAMP, ModBlocks.RESONANCE_LAMP.get());
        helper.setBlock(CONDUIT, ModBlocks.CONDUIT_LINE.get());
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!helper.getBlockState(LAMP).getValue(FieldSensitiveBlock.POWERED)
                    && !helper.getBlockState(CONDUIT).getValue(FieldSensitiveBlock.POWERED),
                "Sur une branche morte, lampe et conduit restent éteints");

            fuelledEmitter(helper);
            helper.runAfterDelay(90, () -> {
                helper.assertTrue(helper.getBlockState(LAMP).getValue(FieldSensitiveBlock.POWERED),
                    "La lampe doit s'allumer dans un champ actif");
                helper.assertTrue(helper.getBlockState(CONDUIT).getValue(FieldSensitiveBlock.POWERED),
                    "Le conduit doit s'allumer dans un champ actif");
                helper.succeed();
            });
        });
    }

    /**
     * <b>L'émetteur ancien est un vrai Field Emitter.</b> Il partage son bloc, sa block
     * entity et son carburant avec la machine que le joueur fabriquera au T2 — c'est ce
     * qui fait qu'il ne peut pas diverger d'elle, et que le joueur apprend la vraie
     * machine dans la salle où il l'a rencontrée (pilier 2). Il est en revanche
     * <b>indestructible</b> : sinon on le rapporterait chez soi, et l'Avant-poste
     * offrirait une machine T2 avant même le blueprint.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void ancientEmitterIsARealEmitterButUnminable(GameTestHelper helper) {
        helper.setBlock(EMITTER, ModBlocks.ANCIENT_EMITTER.get());
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
        helper.assertTrue(emitter != null,
            "L'émetteur ancien doit porter la block entity du Field Emitter");
        helper.assertTrue(!emitter.isActive(),
            "Il doit être À SEC à la génération : le réveiller est le geste du joueur");

        emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(helper.getBlockEntity(EMITTER) instanceof FieldEmitterBlockEntity be
                    && be.isActive(),
                "Nourri d'un Stable Crystal, il doit fournir un champ comme n'importe quel émetteur");
            BlockState state = helper.getBlockState(EMITTER);
            helper.assertTrue(state.getDestroySpeed(helper.getLevel(),
                    helper.absolutePos(EMITTER)) < 0.0F,
                "L'émetteur ancien ne doit pas être minable");
            helper.succeed();
        });
    }

    // =====================================================================
    // Coquilles
    // =====================================================================

    /** Le Hameau est une place commune : ni console, ni gardien, ni butin garanti. */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void hamletCentreIsACommonPlace(GameTestHelper helper) {
        place(helper, "hamlet");
        helper.assertBlockNotPresent(Blocks.CHEST, ANCHOR.offset(12, 4, 12));
        helper.assertEntityNotPresent(ModEntities.CUSTODE.get());
        helper.succeed();
    }

    /** Le logis porte le loot quotidien de 08-Structures.md, et rien de technique. */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void hamletDwellingIsALootRoomOnly(GameTestHelper helper) {
        place(helper, "hamlet_dwelling");
        helper.assertBlockPresent(Blocks.CHEST, ANCHOR.offset(7, 2, 11));
        assertChestHasLootTable(helper, ANCHOR.offset(7, 2, 11));
        helper.assertEntityNotPresent(ModEntities.CUSTODE.get());
        helper.succeed();
    }

    /** L'intérieur est bien creusé (air), pas un bloc plein : on peut y entrer. */
    @GameTest(template = PIECE_ARENA, timeoutTicks = 40)
    public static void pieceInteriorIsHollow(GameTestHelper helper) {
        place(helper, "hamlet_dwelling");
        BlockState interior = helper.getBlockState(ANCHOR.offset(7, 4, 7));
        helper.assertTrue(interior.isAir(), "L'intérieur de la pièce doit être creux, vaut : "
            + interior);
        helper.succeed();
    }

    /**
     * Nombre de tirages de la table de loot vérifiés. « Garanti » veut dire « à
     * <b>chaque</b> tirage » : une vérification à un seul tirage ne distingue pas un
     * pool certain d'un pool à 50 % de chances, et laisse donc passer précisément le
     * bug qu'elle est censée interdire.
     */
    private static final int LOOT_ROLLS = 30;

    /**
     * <b>Anti-régression de progression.</b> La recette du Field Emitter exige des
     * Resonance Component + du Gold, or les Component ne s'obtiennent qu'au Component
     * Assembler — qui a besoin d'un champ, que seul le Field Emitter fournit.
     * L'Avant-poste doit donc <b>garantir</b> l'amorçage, sinon un joueur neuf ne peut
     * jamais atteindre le T2.
     */
    @GameTest(template = FIELD_ARENA, timeoutTicks = 40)
    public static void outpostLootGuaranteesBootstrapComponents(GameTestHelper helper) {
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        net.minecraft.world.level.storage.loot.LootTable table = level.getServer()
            .reloadableRegistries()
            .getLootTable(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.LOOT_TABLE,
                com.veskorius.worldgen.ModWorldGen.OUTPOST_LOOT));
        net.minecraft.world.level.storage.loot.LootParams params =
            new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                    net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(ANCHOR)))
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);

        for (int roll = 1; roll <= LOOT_ROLLS; roll++) {
            int components = 0;
            int gold = 0;
            for (ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(ModItems.RESONANCE_COMPONENT.get())) {
                    components += stack.getCount();
                } else if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT)) {
                    gold += stack.getCount();
                }
            }
            helper.assertTrue(components >= 4 && gold >= 2,
                "L'Avant-poste doit garantir 4 Resonance Component + 2 Gold (amorçage du 1er "
                    + "Field Emitter) à CHAQUE tirage ; au tirage " + roll + "/" + LOOT_ROLLS
                    + " : " + components + " Component, " + gold + " Gold");
        }
        helper.succeed();
    }

    // --- Utilitaires ---------------------------------------------------------

    /** Un Field Emitter alimenté, à portée des blocs sensibles au champ testés ici. */
    private static void fuelledEmitter(GameTestHelper helper) {
        helper.setBlock(EMITTER, ModBlocks.FIELD_EMITTER.get());
        FieldEmitterBlockEntity emitter = helper.getBlockEntity(EMITTER);
        emitter.getFuelHandler().insertItem(FieldEmitterBlockEntity.SLOT_FUEL,
            new ItemStack(ModItems.STABLE_RESONANCE_CRYSTAL.get()), false);
    }

    private static Iterable<BlockPos> allPositions(GameTestHelper helper, int w, int h, int d) {
        return BlockPos.betweenClosed(ANCHOR, ANCHOR.offset(w, h, d));
    }

    /**
     * Vérifie que le coffre placé porte encore une table de loot. Le NBT de block entity
     * du template stocke la clé {@code LootTable} ; si elle ne survivait pas à la pose, le
     * coffre serait vide en jeu — un défaut invisible au simple « le coffre est là ».
     */
    private static void assertChestHasLootTable(GameTestHelper helper, BlockPos relative) {
        net.minecraft.world.level.block.entity.BlockEntity be = helper.getBlockEntity(relative);
        net.minecraft.nbt.CompoundTag saved =
            be.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(saved.contains("LootTable"),
            "Le coffre de structure doit conserver sa table de loot");
    }

    private static StructureTemplate template(GameTestHelper helper, String name) {
        return helper.getLevel().getServer().getStructureManager()
            .get(ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, name))
            .orElseThrow(() -> new AssertionError("Pièce de structure introuvable : " + name));
    }

    /** Pose la pièce NBT du mod à {@link #ANCHOR} (coordonnées relatives au test). */
    private static void place(GameTestHelper helper, String name) {
        StructureTemplate template = template(helper, name);
        BlockPos worldAnchor = helper.absolutePos(ANCHOR);
        template.placeInWorld(helper.getLevel(), worldAnchor, worldAnchor,
            new StructurePlaceSettings(), helper.getLevel().getRandom(), 2);
    }
}
