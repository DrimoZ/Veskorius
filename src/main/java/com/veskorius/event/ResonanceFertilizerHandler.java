package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * <b>Deux engrais veskoriens</b> (04-Materials.md) : la <b>Poussière de Résonance</b> et la
 * <b>Boue de Résonance</b> font pousser tout ce qu'un os à moelle fait pousser.
 *
 * <p><b>La boue surtout, et c'est le point.</b> C'est un déchet — la dissonance cristallisée
 * qu'un Damping Array extrait d'un champ pollué. Elle avait, jusqu'au Reclaimer, zéro
 * destinataire ; elle en a maintenant deux, et le second referme la boucle par le bas :
 * nettoyer un réseau malade produit de quoi nourrir un champ. Le dossier le demandait
 * explicitement (« engrais : le sludge accélère ancient_seed/resonance_bloom »).
 *
 * <p><b>Ils marchent sur les cultures VANILLA aussi</b>, pas seulement sur la nôtre. Un
 * engrais qui ne fertiliserait que la plante du mod serait une clé déguisée en engrais :
 * le joueur apprendrait « ceci sert à ça » au lieu d'« ceci est de l'engrais ». On délègue
 * donc à {@link BonemealableBlock}, le contrat vanilla, plutôt que de tester nos blocs.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID)
public final class ResonanceFertilizerHandler {

    private ResonanceFertilizerHandler() {
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        ItemStack held = event.getItemStack();
        if (!isFertilizer(held)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BonemealableBlock growable)
            || !growable.isValidBonemealTarget(level, pos, state)) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (growable.isBonemealSuccess(level, level.random, pos, state)) {
                growable.performBonemeal(serverLevel, level.random, pos, state);
                // La particule d'os à moelle, volontairement : le joueur doit reconnaître
                // le geste avant de reconnaître l'objet.
                // 2005 = la gerbe verte de l'os à moelle. Un identifiant nu, comme le 2001
                // du Driller ailleurs dans ce dépôt : la constante nommée n'est pas exposée
                // en 1.21.1, et inventer un effet propre reviendrait à cacher au joueur que
                // ce geste EST un engraissage.
                level.levelEvent(2005, pos, 15);
            }
            net.minecraft.world.entity.player.Player player = event.getPlayer();
            if (player == null || !player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        event.cancelWithResult(net.minecraft.world.ItemInteractionResult.SUCCESS);
    }

    private static boolean isFertilizer(ItemStack stack) {
        return stack.is(ModItems.RESONANCE_DUST.get()) || stack.is(ModItems.RESONANCE_SLUDGE.get());
    }
}
