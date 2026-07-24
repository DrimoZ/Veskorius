package com.veskorius.energy;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/**
 * Types de dégâts propres au mod (registre datapack {@code minecraft:damage_type}),
 * construits par datagen comme le reste des registres datapack (voir
 * {@code ModDataGenerators}).
 *
 * <p>Pour l'instant un seul : la <b>décharge de résonance</b> (06-Energy.md) — l'écho
 * local de l'Effondrement qu'un champ saturé de dissonance libère. Un type dédié plutôt
 * qu'un {@code magic()} générique, pour que le message de mort raconte ce qui s'est
 * passé (clé {@code death.attack.veskorius.resonance_discharge}) : la mort par
 * négligence du réseau est un moment de lore, pas un « killed by magic ».
 */
public final class ModDamageTypes {

    public static final ResourceKey<DamageType> RESONANCE_DISCHARGE =
        ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "resonance_discharge"));

    private ModDamageTypes() {
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        // exhaustion 0.1 = comme la plupart des dégâts d'environnement ; DamageScaling
        // NEVER : l'intensité ne dépend pas de la difficulté (elle dépend déjà de la
        // dissonance laissée monter, ce qui est le vrai curseur).
        context.register(RESONANCE_DISCHARGE,
            new DamageType("veskorius.resonance_discharge", DamageScaling.NEVER, 0.1F));
    }
}
