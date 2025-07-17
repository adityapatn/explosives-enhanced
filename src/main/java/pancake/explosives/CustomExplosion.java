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
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
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
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.*;


//the way this class should be used is that a CustomExplosion object is created with the proper arguments, then the explode method is called to cause an explosion
public class CustomExplosion extends Explosion {
    private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
    private final boolean createFire;
    private final boolean doParticles;
    private final DestructionType destructionType;
    private final Random random;
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity entity;
    private final float power;
    private final DamageSource damageSource;
    private final ExplosionBehavior behavior;
    private final ParticleEffect particle;
    private final ParticleEffect emitterParticle;
    private final RegistryEntry<SoundEvent> soundEvent;
    private final ObjectArrayList<BlockPos> affectedBlocks;
    private final Map<PlayerEntity, Vec3d> affectedPlayers;

    /*
    * Data flow for destructionType:
    * World.createExplosion(explosionSourceType)
    * using switch of explosionSourceType.ordinal(), set var10000 and then destructionType
    * call Explosion constructor, passing destructionType as argument
    * */

    private Explosion.DestructionType getDestructionTypeFromRule(GameRules.Key<GameRules.BooleanRule> gameRuleKey) {
        return world.getGameRules().getBoolean(gameRuleKey) ? DestructionType.DESTROY_WITH_DECAY : DestructionType.DESTROY;
    }

    private DestructionType getDestructionFromSource(World.ExplosionSourceType sourceType) {
        Explosion.DestructionType tempDestructionType;
        switch (sourceType.ordinal()) {
            case 0 -> tempDestructionType = DestructionType.KEEP;
            case 1 -> tempDestructionType = this.getDestructionTypeFromRule(GameRules.BLOCK_EXPLOSION_DROP_DECAY);
            case 2 -> tempDestructionType = world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? this.getDestructionTypeFromRule(GameRules.MOB_EXPLOSION_DROP_DECAY) : DestructionType.KEEP;
            case 3 -> tempDestructionType = this.getDestructionTypeFromRule(GameRules.TNT_EXPLOSION_DROP_DECAY);
            case 4 -> tempDestructionType = DestructionType.TRIGGER_BLOCK;
            default -> throw new MatchException((String)null, (Throwable)null);
        }
        return tempDestructionType;
    }

    private ExplosionBehavior chooseBehavior(@Nullable Entity entity) {
        return (ExplosionBehavior)(entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity));
    }

    public CustomExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, @Nullable World.ExplosionSourceType sourceType, @Nullable ParticleEffect particle, @Nullable ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {
        //this line does literally nothing because all the Explosion properties are private and the constructor does nothing else but set them
        super(world, entity, damageSource, behavior, x, y, z, power, createFire, null, particle, emitterParticle, soundEvent);
        Explosion.DestructionType defaultDestructionType = world.getGameRules().getBoolean(GameRules.BLOCK_EXPLOSION_DROP_DECAY) ? DestructionType.DESTROY_WITH_DECAY : DestructionType.DESTROY;
        SimpleParticleType defaultParticle = ParticleTypes.EXPLOSION;
        SimpleParticleType defaultEmitterParticle = ParticleTypes.EXPLOSION_EMITTER;

        this.random = Random.create();
        this.affectedBlocks = new ObjectArrayList();
        this.affectedPlayers = Maps.newHashMap();
        this.world = world;
        this.entity = entity;
        this.power = power;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createFire = createFire;
        this.destructionType = sourceType == null ? defaultDestructionType : getDestructionFromSource(sourceType);
        this.damageSource = damageSource == null ? world.getDamageSources().explosion(this) : damageSource;
        this.behavior = behavior == null ? this.chooseBehavior(entity) : behavior;
        this.particle = particle == null ? defaultParticle : particle;
        this.emitterParticle = emitterParticle == null ? defaultEmitterParticle : emitterParticle;
        this.doParticles = this.particle != null && this.emitterParticle != null;
        this.soundEvent = soundEvent;
    }

    public Explosion explode() {
        collectBlocksAndDamageEntities();
        affectWorld();
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().sendToNearbyPlayers(
                    entity,
                    new ExplosionS2CPacket(
                            x,
                            y,
                            z,
                            power,
                            affectedBlocks,   // your affectedBlocks list
                            Vec3d.ZERO,
                            destructionType,
                            particle,
                            emitterParticle,
                            soundEvent
                    )
            );
        }

        return this;
    }

    @Override
    public void collectBlocksAndDamageEntities() {
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
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
                        float h = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
                        double m = this.x;
                        double n = this.y;
                        double o = this.z;

                        for(float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                            BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                            BlockState blockState = this.world.getBlockState(blockPos);
                            FluidState fluidState = this.world.getFluidState(blockPos);
                            if (!this.world.isInBuildLimit(blockPos)) {
                                break;
                            }

                            Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, blockPos, blockState, fluidState);
                            if (optional.isPresent()) {
                                h -= ((Float)optional.get() + 0.3F) * 0.3F;
                            }

                            if (h > 0.0F && this.behavior.canDestroyBlock(this, this.world, blockPos, blockState, h)) {
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

    //not an @Override because the superclass method has a boolean parameter, I just used this.doParticles
    public void affectWorld() {
        if (this.world.isClient) {
            this.world.playSound(this.x, this.y, this.z, (SoundEvent)this.soundEvent.value(), SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean bl = this.shouldDestroy();
        if (this.doParticles) {
            ParticleEffect particleEffect;
            if (!(this.power < 2.0F) && bl) {
                particleEffect = this.emitterParticle;
            } else {
                particleEffect = this.particle;
            }

            this.world.addParticle(particleEffect, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }

        if (bl) {
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
        }

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
