package com.veskorius.entity;

import com.veskorius.Veskorius;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;

/**
 * Enregistrements d'entités sur le bus MOD : attributs et règles de placement de
 * spawn. Séparé de {@link ModEntities} (le registre) et de la partie client (le
 * rendu, dans {@code client/}).
 */
@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CRYSTAL_STRIDER.get(), CrystalStriderEntity.createAttributes().build());
        event.put(ModEntities.CUSTODE.get(), CustodeEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Souterrain, près des poches (Y 0 à -40, 09-Entities.md). Le ciblage exact
        // « près des poches » n'est pas exprimable en règle de spawn vanilla : on
        // borne la strate Y et le sol solide, densité à valider en playtest (comme
        // pour le worldgen des poches).
        event.register(ModEntities.CRYSTAL_STRIDER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ModEntityEvents::checkCrystalStriderSpawn,
            RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean checkCrystalStriderSpawn(EntityType<CrystalStriderEntity> type,
                                                    ServerLevelAccessor level, MobSpawnType spawnType,
                                                    BlockPos pos, RandomSource random) {
        if (MobSpawnType.isSpawner(spawnType)) {
            return true;
        }
        return pos.getY() <= 0 && pos.getY() >= -40
            && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }
}
