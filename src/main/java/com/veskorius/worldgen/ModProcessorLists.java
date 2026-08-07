package com.veskorius.worldgen;

import com.veskorius.Veskorius;
import com.veskorius.block.ModBlocks;
import com.veskorius.tag.ModTags;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

/**
 * Listes de <b>processors</b> appliquées aux pièces de structure (17-Dungeons.md §2.4).
 *
 * <p><b>Le problème qu'elles résolvent.</b> Les pièces sont des NBT figés : sans
 * processor, chaque Avant-poste du monde est <i>identique au bloc près</i>, effondrement
 * compris (l'ancien {@code collapse()} tirait sur une graine constante). Le deuxième
 * qu'on visite n'apprend donc plus rien. Un processor, lui, retire et remplace des blocs
 * <b>à la pose</b>, avec l'aléa du monde : deux ruines du même plan ne s'abîment jamais
 * pareil.
 *
 * <p><b>Deux listes, et une règle de sécurité.</b> {@link #WORN} vieillit la maçonnerie
 * ; {@link #RUINED} y ajoute des trous. Les deux ne peuvent toucher que les blocs du tag
 * {@link ModTags.Blocks#STRUCTURE_ROTTABLE} — une <b>liste blanche</b>. C'est le point
 * qui compte : le réflexe serait de protéger les blocs critiques (liste noire), mais
 * alors le premier bloc critique ajouté et oublié devient effaçable, et un Avant-poste
 * dont la console a « pourri » est une progression bloquée. Ici l'oubli est inoffensif.
 */
public final class ModProcessorLists {

    /** Vieillissement seul : la maçonnerie se fissure, rien ne disparaît. */
    public static final ResourceKey<StructureProcessorList> WORN = key("worn");

    /**
     * Vieillissement + trous. Réservé aux <b>ailes facultatives</b> : une pièce du
     * chemin critique ne doit jamais avoir de trou dans un mur porteur, sous peine de
     * laisser voir (ou atteindre) ce qu'elle est censée fermer.
     */
    public static final ResourceKey<StructureProcessorList> RUINED = key("ruined");

    /**
     * Part de blocs conservés par {@link BlockRotProcessor}. 0.94 = 6 % de disparitions,
     * assez pour qu'un mur ait des manques visibles, assez peu pour qu'une salle reste
     * une salle. Au-delà, les pièces deviennent des dentelles et le donjon perd sa
     * lisibilité — c'est un réglage esthétique, pas un curseur de difficulté.
     */
    private static final float INTEGRITY = 0.94F;

    private ModProcessorLists() {
    }

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);

        List<StructureProcessor> worn = List.of(new RuleProcessor(List.of(
            // La brique se fend avant tout le reste : c'est l'usure la plus visible et
            // la moins destructrice, donc celle qu'on peut se permettre en quantité.
            rule(ModBlocks.VEINED_STONE_BRICKS.get(), 0.14F,
                ModBlocks.CRACKED_VEINED_STONE_BRICKS.get().defaultBlockState()),
            // Un conduit rompu redevient de la brique : la ligne de lumière s'interrompt
            // par endroits, ce qui rend le chemin plus lisible, pas moins — on voit où
            // le réseau a lâché.
            rule(ModBlocks.CONDUIT_LINE.get(), 0.10F,
                ModBlocks.CRACKED_VEINED_STONE_BRICKS.get().defaultBlockState()),
            // La roche brute, elle, s'éboule.
            rule(ModBlocks.RESONANCE_VEINED_STONE.get(), 0.07F,
                Blocks.COBBLED_DEEPSLATE.defaultBlockState()))));

        context.register(WORN, new StructureProcessorList(worn));
        context.register(RUINED, new StructureProcessorList(
            java.util.stream.Stream.concat(worn.stream(),
                java.util.stream.Stream.of((StructureProcessor) new BlockRotProcessor(
                    blocks.getOrThrow(ModTags.Blocks.STRUCTURE_ROTTABLE), INTEGRITY))).toList()));
    }

    private static ProcessorRule rule(Block input, float probability, BlockState output) {
        return new ProcessorRule(
            new RandomBlockMatchTest(input, probability), AlwaysTrueTest.INSTANCE, output);
    }

    private static ResourceKey<StructureProcessorList> key(String path) {
        return ResourceKey.create(Registries.PROCESSOR_LIST,
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, path));
    }
}
