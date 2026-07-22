package com.veskorius.event;

import com.veskorius.Veskorius;
import com.veskorius.block.AbstractMachineBlock;
import com.veskorius.block.ModBlocks;
import com.veskorius.config.VeskoriusConfig;
import com.veskorius.entity.CustodeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Défense de site (09-Entities.md) : casser une machine Veskorius (ou la console)
 * dans un rayon alerte les Custodes proches, qui prennent le casseur pour cible —
 * même s'il est hors du rayon de détection passif (6 blocs). C'est l'autre condition
 * d'agression réactive prévue par le design (« ou endommage une machine du site »),
 * en plus de la proximité gérée par l'IA du Custode.
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID)
public final class CustodeAlertHandler {

    private CustodeAlertHandler() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && isGuardedBlock(event.getState())) {
            alertNearbyCustodes(level, event.getPos(), event.getPlayer());
        }
    }

    private static boolean isGuardedBlock(BlockState state) {
        return state.getBlock() instanceof AbstractMachineBlock
            || state.is(ModBlocks.ATTUNEMENT_CONSOLE.get());
    }

    /**
     * Fait cibler {@code attacker} par tous les Custodes dans le rayon d'alerte
     * autour de {@code pos}. Statique et sans événement pour être testable.
     */
    public static void alertNearbyCustodes(ServerLevel level, BlockPos pos, @Nullable Player attacker) {
        if (attacker == null || attacker.isCreative() || attacker.isSpectator()) {
            return;
        }
        AABB area = new AABB(pos).inflate(VeskoriusConfig.custodeAlertRange());
        for (CustodeEntity custode : level.getEntitiesOfClass(CustodeEntity.class, area)) {
            custode.setTarget(attacker);
        }
    }
}
