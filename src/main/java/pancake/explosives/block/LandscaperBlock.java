package pancake.explosives.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import pancake.explosives.CustomExplosion;
import pancake.explosives.registry.ModDamageTypes;

public class LandscaperBlock extends DynamiteBlock {

    public LandscaperBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public void explode(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (!world.isClient()) {
            world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos); //game event for listeners like sculk sensors
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);

            //non-null attacker AND ENTITY SOURCE triggers .player death message – block source is environmental (must summon LandscaperEntity to cause explosion) - confirmed this is not issue with GrenadeEntity
            
            DamageSource landscaperDamageSource = ModDamageTypes.createLandscaperEntityDamage(world, igniter, igniter);
            //Landscaper explodes
            //world.createExplosion(igniter, landscaperDamageSource, new ExplosionBehavior(), pos.getX(), pos.getY(), pos.getZ(), 4.0F, false, World.ExplosionSourceType.TNT);
            CustomExplosion explosion = new CustomExplosion(world, igniter, landscaperDamageSource, new ExplosionBehavior(), pos.getX(), pos.getY(), pos.getZ(), 4.0F, false, ExplosionSourceType.TNT, null, null, null);
            explosion.explode(pos.getY() - 1, true);
            //ExplosivesEnhanced.LOGGER.info("Landscaper exploded!");
        }
    }
}