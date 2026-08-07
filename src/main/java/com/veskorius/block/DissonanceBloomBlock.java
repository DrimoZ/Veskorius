package com.veskorius.block;

import com.mojang.serialization.MapCodec;
import com.veskorius.energy.ModDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <b>Efflorescence de dissonance</b> — la moisissure de l'Effondrement
 * (17-Dungeons.md, règle R3 : « le danger principal est la dissonance, pas le mob »).
 *
 * <p>Une croûte qui a poussé là où un champ a dérivé pendant des siècles
 * (`02-Lore.md`, Âge 4 : la sur-résonance). Elle <b>n'a pas de collision</b> : on la
 * traverse, et la traverser blesse légèrement. C'est ce qui en fait un obstacle de
 * donjon et non un décor — un couloir tapissé de blooms se franchit en encaissant, se
 * nettoie à la pioche, ou se contourne.
 *
 * <p><b>Pourquoi pas un bloc solide qui blesse au minage.</b> Un bloc solide n'aurait
 * aucun effet sur la circulation : le joueur passerait à côté sans le voir. Sans
 * collision, la croûte occupe réellement l'espace jouable et pose une question
 * (traverser ? creuser ? faire le tour ?), ce qui est la définition d'un obstacle.
 *
 * <p>Les dégâts passent par {@link ModDamageTypes#RESONANCE_DISCHARGE} : c'est la même
 * substance et la même cause que la décharge d'un champ saturé (`06-Energy.md`), donc
 * le même message de mort. Un type de dégâts de plus ne dirait rien de neuf.
 */
public class DissonanceBloomBlock extends Block {

    public static final MapCodec<DissonanceBloomBlock> CODEC = simpleCodec(DissonanceBloomBlock::new);

    /**
     * Dégâts par contact, appliqués une fois par {@link #CONTACT_INTERVAL} ticks.
     * Calibré pour être une <b>taxe de passage</b>, pas un piège mortel : traverser
     * trois blocs de croûte coûte moins d'un cœur, camper dedans finit par tuer.
     */
    private static final float CONTACT_DAMAGE = 1.0F;
    private static final int CONTACT_INTERVAL = 20;

    public DissonanceBloomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DissonanceBloomBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity)) {
            return;
        }
        // Cadencé sur l'horloge du monde plutôt que sur un compteur par entité : le
        // bloc reste sans état, et la note de dégât ne dépend pas du nombre de blocs
        // traversés simultanément (sinon un couloir étroit ferait quatre fois mal).
        if (level.getGameTime() % CONTACT_INTERVAL != 0) {
            return;
        }
        entity.hurt(ModDamageTypes.discharge(level), CONTACT_DAMAGE);
    }
}
