package com.veskorius.entity;

import com.veskorius.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Fileur de Cristal (09-Entities.md) : petite faune neutre des poches de cristal.
 *
 * Ne combat jamais (dégâts 0, fuit quand blessé). Deux usages :
 * <ul>
 *   <li><b>Traite</b> : clic droit à main nue, cooldown 5 minutes, rend 1 Raw
 *       Resonance Crystal — source alternative au minage, volontairement plus lente
 *       (09-Entities.md) ;</li>
 *   <li><b>Reproduction</b> : nourri au {@code resonance_spore} (mécanisme d'élevage
 *       vanilla), pour établir un cheptel plutôt que dépendre des rencontres.</li>
 * </ul>
 *
 * La proximité d'un Crystal Roost (tâche 12) est gérée côté Roost, pas ici.
 */
public class CrystalStriderEntity extends Animal {

    /** Cooldown de traite : 5 minutes (09-Entities.md). */
    public static final int MILK_COOLDOWN_TICKS = 5 * 60 * 20;

    private int milkCooldown;

    public CrystalStriderEntity(EntityType<? extends CrystalStriderEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        // Aucun goal de cible/attaque : neutre par conception (pilier 4, 09-Entities.md).
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(3, new TemptGoal(this, 1.1, Ingredient.of(ModItems.RESONANCE_SPORE.get()), false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.RESONANCE_SPORE.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.CRYSTAL_STRIDER.get().create(level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && milkCooldown > 0) {
            milkCooldown--;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Nourriture : laisse le vanilla gérer reproduction / croissance du bébé.
        if (isFood(held)) {
            return super.mobInteract(player, hand);
        }

        // Traite à main nue (adulte, hors cooldown) : 1 Raw Resonance Crystal.
        if (held.isEmpty() && !isBaby()) {
            if (level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (milkCooldown <= 0) {
                ItemStack crystal = new ItemStack(ModItems.RAW_RESONANCE_CRYSTAL.get());
                if (!player.addItem(crystal)) {
                    player.drop(crystal, false);
                }
                milkCooldown = MILK_COOLDOWN_TICKS;
                return InteractionResult.CONSUME;
            }
            // Encore en cooldown : rien à récolter.
            return InteractionResult.PASS;
        }

        return super.mobInteract(player, hand);
    }

    /** Exposé pour les GameTest : temps restant avant la prochaine traite. */
    public int getMilkCooldown() {
        return milkCooldown;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MilkCooldown", milkCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        milkCooldown = tag.getInt("MilkCooldown");
    }
}
