package pancake.explosives;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExplosionLogicUpdate {

    public Explosion createCustomExplosion(World world, @Nullable Entity entity, DamageSource damageSource, double x, double y, double z, float power) {

        //always uses DESTROY destruction type
        Explosion.DestructionType destructionType = world.getGameRules().getBoolean(GameRules.BLOCK_EXPLOSION_DROP_DECAY) ? DestructionType.DESTROY_WITH_DECAY : DestructionType.DESTROY;
        //collectBlocksAndDamageEntities(world);
    }

    @Override
    public void collectBlocksAndDamageEntities(World world, @Nullable Entity entity, double x, double y, double z, float power, ExplosionBehavior behavior, ) {
        world.emitGameEvent(entity, GameEvent.EXPLODE, new Vec3d(x, y, z));
        Set<BlockPos> set = Sets.newHashSet();
        int k;
        int l;
        for(int j = 0; j < 16; ++j) {
            for(k = 0; k < 16; ++k) {
                for(l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d = (double)((float)j / 15.0F * 2.0F - 1.0F);
                        double e = (double)((float)k / 15.0F * 2.0F - 1.0F);
                        double f = (double)((float)l / 15.0F * 2.0F - 1.0F);
                        double g = Math.sqrt(d * d + e * e + f * f);
                        d /= g;
                        e /= g;
                        f /= g;
                        float h = power * (0.7F + world.random.nextFloat() * 0.6F);
                        double m = x;
                        double n = y;
                        double o = z;

                        for(float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                            BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                            BlockState blockState = world.getBlockState(blockPos);
                            FluidState fluidState = world.getFluidState(blockPos);
                            if (!world.isInBuildLimit(blockPos)) {
                                break;
                            }

                            Optional<Float> optional = behavior.getBlastResistance(this, world, blockPos, blockState, fluidState);
                            if (optional.isPresent()) {
                                h -= ((Float)optional.get() + 0.3F) * 0.3F;
                            }

                            if (h > 0.0F && behavior.canDestroyBlock(this, world, blockPos, blockState, h)) {
                                set.add(blockPos);
                            }

                            m += d * 0.30000001192092896;
                            n += e * 0.30000001192092896;
                            o += f * 0.30000001192092896;
                        }
                    }
                }
            }
        }

        this.affectedBlocks.addAll(set);
        float q = this.power * 2.0F;
        k = MathHelper.floor(this.x - (double)q - 1.0);
        l = MathHelper.floor(this.x + (double)q + 1.0);
        int r = MathHelper.floor(this.y - (double)q - 1.0);
        int s = MathHelper.floor(this.y + (double)q + 1.0);
        int t = MathHelper.floor(this.z - (double)q - 1.0);
        int u = MathHelper.floor(this.z + (double)q + 1.0);
        List<Entity> list = this.world.getOtherEntities(this.entity, new Box((double)k, (double)r, (double)t, (double)l, (double)s, (double)u));
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);
        Iterator var34 = list.iterator();

        while(true) {
            Entity entity;
            double w;
            double x;
            double y;
            double v;
            double z;
            do {
                do {
                    do {
                        if (!var34.hasNext()) {
                            return;
                        }

                        entity = (Entity)var34.next();
                    } while(entity.isImmuneToExplosion(this));

                    v = Math.sqrt(entity.squaredDistanceTo(vec3d)) / (double)q;
                } while(!(v <= 1.0));

                w = entity.getX() - this.x;
                x = (entity instanceof TntEntity ? entity.getY() : entity.getEyeY()) - this.y;
                y = entity.getZ() - this.z;
                z = Math.sqrt(w * w + x * x + y * y);
            } while(z == 0.0);

            w /= z;
            x /= z;
            y /= z;
            if (this.behavior.shouldDamage(this, entity)) {
                entity.damage(this.damageSource, this.behavior.calculateDamage(this, entity));
            }

            double aa = (1.0 - v) * (double)getExposure(vec3d, entity) * (double)this.behavior.getKnockbackModifier(entity);
            double ab;
            if (entity instanceof LivingEntity livingEntity) {
                ab = aa * (1.0 - livingEntity.getAttributeValue(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE));
            } else {
                ab = aa;
            }

            w *= ab;
            x *= ab;
            y *= ab;
            Vec3d vec3d2 = new Vec3d(w, x, y);
            entity.setVelocity(entity.getVelocity().add(vec3d2));
            if (entity instanceof PlayerEntity playerEntity) {
                if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                    this.affectedPlayers.put(playerEntity, vec3d2);
                }
            }

            entity.onExplodedBy(this.entity);
        }
    }

    private static void tryMergeStack(List<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        for(int i = 0; i < stacks.size(); ++i) {
            Pair<ItemStack, BlockPos> pair = (Pair)stacks.get(i);
            ItemStack itemStack = (ItemStack)pair.getFirst();
            if (ItemEntity.canMerge(itemStack, stack)) {
                stacks.set(i, Pair.of(ItemEntity.merge(itemStack, stack, 16), (BlockPos)pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        stacks.add(Pair.of(stack, pos));
    }


    public void affectWorld() {
        if (this.world.isClient) {
            this.world.playSound(this.x, this.y, this.z, (SoundEvent)this.soundEvent.value(), SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        this.world.getProfiler().push("explosion_blocks");
        List<Pair<ItemStack, BlockPos>> list = new ArrayList();
        Util.shuffle(this.affectedBlocks, this.world.random);
        ObjectListIterator var4 = this.affectedBlocks.iterator();

        while(var4.hasNext()) {
            BlockPos blockPos = (BlockPos)var4.next();
            this.world.getBlockState(blockPos).onExploded(this.world, blockPos, this, (stack, pos) -> {
                tryMergeStack(list, stack, pos);
            });
        }

        Iterator var8 = list.iterator();

        while(var8.hasNext()) {
            Pair<ItemStack, BlockPos> pair = (Pair)var8.next();
            Block.dropStack(this.world, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
        }

        this.world.getProfiler().pop();

        if (this.createFire) {
            ObjectListIterator var7 = this.affectedBlocks.iterator();

            while(var7.hasNext()) {
                BlockPos blockPos2 = (BlockPos)var7.next();
                if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockPos2).isAir() && this.world.getBlockState(blockPos2.down()).isOpaqueFullCube(this.world, blockPos2.down())) {
                    this.world.setBlockState(blockPos2, AbstractFireBlock.getState(this.world, blockPos2));
                }
            }
        }

    }
}
