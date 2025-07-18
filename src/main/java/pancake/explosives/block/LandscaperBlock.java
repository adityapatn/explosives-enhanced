package pancake.explosives.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import pancake.explosives.ExplosivesEnhanced;
import pancake.explosives.registry.ModDamageTypes;

public class LandscaperBlock extends Block {

    public LandscaperBlock(Settings settings) {
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
        if (!world.isClient) {
            explode(world, pos, null);
        }
    }

    public void explode(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (!world.isClient()) {
            world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos); //game event for listeners like sculk sensors
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);

            world.createExplosion(igniter, Explosion.createDamageSource(world, igniter), new ExplosionBehavior(), pos.getX(), pos.getY(), pos.getZ(), 4.0F, false, World.ExplosionSourceType.TNT);
            ExplosivesEnhanced.LOGGER.info("Landscaper exploded!");

            //code for registering the damage type

            //Since owner is excluded from explosion damage calculation, manually apply damage to owner
            float power = 4.0F;
            if (igniter instanceof LivingEntity livingOwner) {
                ExplosivesEnhanced.LOGGER.info("Damaging owner!");
                double dx = livingOwner.getX() - pos.getX();
                double dy = livingOwner.getEyeY() - pos.getY();
                double dz = livingOwner.getZ() - pos.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                float radius = power * 2.0f;
                if (distance <= radius) {
                    float exposure = Explosion.getExposure(Vec3d.ofCenter(pos), livingOwner);
                    if (exposure > 0) {
                        double normDist = distance / radius;
                        double impact = (1.0 - normDist) * exposure;
                        float damage = (float) ((impact * impact + impact) * 3.5 * power);

                        DamageSource landscaperDamageSource = ModDamageTypes.createDynamiteEntityDamage(world, igniter, igniter);
                        livingOwner.damage(landscaperDamageSource, damage); //damage source must not be explosion to affect owner
                    }
                }
            }
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
