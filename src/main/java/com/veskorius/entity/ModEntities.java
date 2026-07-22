package com.veskorius.entity;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entités du mod (09-Entities.md). Première entité : le Fileur de Cristal, faune
 * neutre des poches de cristal.
 */
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(Registries.ENTITY_TYPE, Veskorius.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CrystalStriderEntity>> CRYSTAL_STRIDER =
        ENTITIES.register("crystal_strider",
            () -> EntityType.Builder.of(CrystalStriderEntity::new, MobCategory.CREATURE)
                .sized(0.6f, 0.7f)
                .clientTrackingRange(10)
                .build("crystal_strider"));
}
