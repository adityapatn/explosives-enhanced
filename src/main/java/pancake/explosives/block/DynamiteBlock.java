package pancake.explosives.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import pancake.explosives.CustomExplosion;
import pancake.explosives.ExplosivesEnhanced;
import pancake.explosives.registry.ModDamageTypes;

public class DynamiteBlock extends Block {

    public DynamiteBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            if (world.isReceivingRedstonePower(pos)) {
                explode(world, pos, null);
                world.removeBlock(pos, false);
            }
        }
    }

    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            explode(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
            explode(world, pos, explosion.getCausingEntity());
    }

    public void explode(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (!world.isClient()) {
            world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos); //game event for listeners like sculk sensors
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);

            //ExplosivesEnhanced.LOGGER.info("Igniter: " + igniter);
            //ExplosivesEnhanced.LOGGER.info("Is igniter living: " + (igniter instanceof LivingEntity));
            //non-null attacker AND ENTITY SOURCE triggers .player death message – block source is environmental (must summon DynamiteEntity to cause explosion) - confirmed this is not issue with GrenadeEntity
            
            DamageSource dynamiteDamageSource = ModDamageTypes.createDynamiteEntityDamage(world, igniter, igniter);
            //Dynamite explodes
            //world.createExplosion(igniter, dynamiteDamageSource, new ExplosionBehavior(), pos.getX(), pos.getY(), pos.getZ(), 4.0F, false, World.ExplosionSourceType.TNT);
            CustomExplosion explosion = new CustomExplosion(world, igniter, dynamiteDamageSource, new ExplosionBehavior(), pos.getX(), pos.getY(), pos.getZ(), 4.0F, false, ExplosionSourceType.TNT, null, null, null);
            explosion.explode();
            ExplosivesEnhanced.LOGGER.info("Dynamite exploded!");
        }
    }

    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE)) {
            return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
        } else {
            explode(world, pos, player);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            Item item = stack.getItem();
            if (stack.isOf(Items.FLINT_AND_STEEL)) {
                stack.damage(1, player, LivingEntity.getSlotForHand(hand));
            } else {
                stack.decrementUnlessCreative(1, player);
            }

            player.incrementStat(Stats.USED.getOrCreateStat(item));
            return ItemActionResult.success(world.isClient);
        }
    }

    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient) {
            BlockPos blockPos = hit.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.canModifyAt(world, blockPos)) {
                explode(world, blockPos, entity instanceof LivingEntity ? (LivingEntity)entity : null);
                world.removeBlock(blockPos, false);
            }
        }
    }

    public boolean shouldDropItemsOnExplosion(Explosion explosion) {
        return false;
    }
}
