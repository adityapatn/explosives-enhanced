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

    public CustomExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, @Nullable World.ExplosionSourceType sourceType, @Nullable ParticleEffect particle, @Nullable ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {
        //this line does literally nothing because all the Explosion properties are private and the constructor does nothing else but set them
        super(world, entity, damageSource, behavior, x, y, z, power, createFire, null, particle, emitterParticle, soundEvent);
        Explosion.DestructionType defaultDestructionType = world.getGameRules().getBoolean(GameRules.BLOCK_EXPLOSION_DROP_DECAY) ? DestructionType.DESTROY_WITH_DECAY : DestructionType.DESTROY;
        SimpleParticleType defaultParticle = ParticleTypes.EXPLOSION;
        SimpleParticleType defaultEmitterParticle = ParticleTypes.EXPLOSION_EMITTER;

        this.random = Random.create();
        this.affectedBlocks = new ObjectArrayList<BlockPos>();
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

    private void sendPacket() {
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
    }

    public Explosion explode() {
        filterBlocks(null, null);
        damageEntities();
        affectWorld();
        sendPacket();
        return this;
    }

    public Explosion explode(Integer yCutoff, Boolean above) {
        filterBlocks(yCutoff, above);
        damageEntities();
        affectWorld();
        sendPacket();
        return this;
    }

    private void filterBlocks(@Nullable Integer yCutoff, @Nullable Boolean above) {
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();
        
        for(int rayIndexX = 0; rayIndexX < 16; ++rayIndexX) {
            for(int rayIndexY = 0; rayIndexY < 16; ++rayIndexY) {
                for(int rayIndexZ = 0; rayIndexZ < 16; ++rayIndexZ) {
                    if (rayIndexX == 0 || rayIndexX == 15 || rayIndexY == 0 || rayIndexY == 15 || rayIndexZ == 0 || rayIndexZ == 15) { //ensures one of the ray direction componenents is at maximum strength (touching the surface of the cube)
                        double rayDirX = (double)((float)rayIndexX / 15.0F * 2.0F - 1.0F);
                        double rayDirY = (double)((float)rayIndexY / 15.0F * 2.0F - 1.0F);
                        double rayDirZ = (double)((float)rayIndexZ / 15.0F * 2.0F - 1.0F);
                        double rayMagnitude = Math.sqrt(rayDirX * rayDirX + rayDirY * rayDirY + rayDirZ * rayDirZ);
                        rayDirX /= rayMagnitude;
                        rayDirY /= rayMagnitude;
                        rayDirZ /= rayMagnitude;
                        double rayPosX = this.x;
                        double rayPosY = this.y;
                        double rayPosZ = this.z;

                        for(float rayPower = this.power * (0.7F + this.world.random.nextFloat() * 0.6F); rayPower > 0.0F; rayPower -= 0.225F) {
                            BlockPos blockPos = BlockPos.ofFloored(rayPosX, rayPosY, rayPosZ);
                            BlockState blockState = this.world.getBlockState(blockPos);
                            FluidState fluidState = this.world.getFluidState(blockPos);
                            if (!this.world.isInBuildLimit(blockPos)) {
                                break;
                            }

                            Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, blockPos, blockState, fluidState);
                            if (optional.isPresent()) {
                                rayPower -= ((Float)optional.get() + 0.3F) * 0.3F;
                            }

                            if (rayPower > 0.0F && this.behavior.canDestroyBlock(this, this.world, blockPos, blockState, rayPower)) {
                                if (yCutoff != null && above != null) {  //we want to check the block y-level
                                    if (above) { //check whether its above the Y cutoff
                                        if (blockPos.getY() > yCutoff) {
                                            set.add(blockPos);
                                        }
                                    } else { //check whether its below the Y cutoff
                                        if (blockPos.getY() < yCutoff) {
                                            set.add(blockPos);
                                        }
                                    }
                                }
                                else { //no check for Y level
                                    set.add(blockPos);
                                }
                                 //all explodable blocks are added to the set, which are added to affectedBlocks
                            }

                            rayPosX += rayDirX * 0.3;
                            rayPosY += rayDirY * 0.3;
                            rayPosZ += rayDirZ * 0.3;
                        }
                    }
                }
            }
        }
        this.affectedBlocks.addAll(set);
    }

    private void damageEntities() {
        float explosionRadius = this.power * 2.0F;
        int minX = MathHelper.floor(this.x - (double)explosionRadius - 1.0);
        int maxX = MathHelper.floor(this.x + (double)explosionRadius + 1.0);
        int minY = MathHelper.floor(this.y - (double)explosionRadius - 1.0);
        int maxY = MathHelper.floor(this.y + (double)explosionRadius + 1.0);
        int minZ = MathHelper.floor(this.z - (double)explosionRadius - 1.0);
        int maxZ = MathHelper.floor(this.z + (double)explosionRadius + 1.0);
        List<Entity> nearbyEntities = this.world.getOtherEntities(this.entity, new Box((double)minX, (double)minY, (double)minZ, (double)maxX, (double)maxY, (double)maxZ));
        Vec3d explosionCenter = new Vec3d(this.x, this.y, this.z);
        Iterator<Entity> nearbyEntitiesIterator = nearbyEntities.iterator();

        while(true) {
            Entity selectedEntity;
            double xComponent;
            double yComponent;
            double zComponent;
            double normalizedDistance; //Entity distance as fraction of explosion radius (0.0-1.0)
            double distance;
            do {
                do {
                    do {
                        if (!nearbyEntitiesIterator.hasNext()) {
                            return;
                        }

                        selectedEntity = (Entity)nearbyEntitiesIterator.next();
                    } while(selectedEntity.isImmuneToExplosion(this));

                    normalizedDistance = Math.sqrt(selectedEntity.squaredDistanceTo(explosionCenter)) / (double)explosionRadius;
                } while(!(normalizedDistance <= 1.0));

                xComponent = selectedEntity.getX() - this.x;
                yComponent = (selectedEntity instanceof TntEntity ? selectedEntity.getY() : selectedEntity.getEyeY()) - this.y;
                zComponent = selectedEntity.getZ() - this.z;
                distance = Math.sqrt(xComponent * xComponent + yComponent * yComponent + zComponent * zComponent);
            } while(distance == 0.0);

            xComponent /= distance; //normalize to unit vector
            yComponent /= distance;
            zComponent /= distance;
            if (this.behavior.shouldDamage(this, selectedEntity)) {
                //ExplosivesEnhanced.LOGGER.info("Damaging entity: " + selectedEntity);
                //ExplosivesEnhanced.LOGGER.info("DamageSource attacker at damage time: " + this.damageSource.getAttacker());
                //ExplosivesEnhanced.LOGGER.info("DamageSource source at damage time: " + this.damageSource.getSource());
                //confirmed works but does not send the .player death message (I give up)
                selectedEntity.damage(this.damageSource, this.behavior.calculateDamage(this, selectedEntity));
            }

            double baseKnockback = (1.0 - normalizedDistance) * (double)getExposure(explosionCenter, selectedEntity) * (double)this.behavior.getKnockbackModifier(selectedEntity);
            double finalKnockback;
            if (selectedEntity instanceof LivingEntity livingEntity) {
                finalKnockback = baseKnockback * (1.0 - livingEntity.getAttributeValue(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE));
            } else {
                finalKnockback = baseKnockback;
            }

            xComponent *= finalKnockback;
            yComponent *= finalKnockback;
            zComponent *= finalKnockback;
            Vec3d knockbackVector = new Vec3d(xComponent, yComponent, zComponent);
            selectedEntity.setVelocity(selectedEntity.getVelocity().add(knockbackVector));
            if (selectedEntity instanceof PlayerEntity playerEntity) {
                if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                    this.affectedPlayers.put(playerEntity, knockbackVector);
                }
            }

            selectedEntity.onExplodedBy(this.entity);
        }
    }

    //not an @Override because the superclass method has a boolean parameter, I just used this.doParticles
    public void affectWorld() {
        if (this.world.isClient) {
            this.world.playSound(this.x, this.y, this.z, (SoundEvent)this.soundEvent.value(), SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean shouldDestroy = this.shouldDestroy();
        if (this.doParticles) {
            ParticleEffect particleEffect;
            if (!(this.power < 2.0F) && shouldDestroy) {
                particleEffect = this.emitterParticle;
            } else {
                particleEffect = this.particle;
            }

            this.world.addParticle(particleEffect, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }

        if (shouldDestroy) {
            this.world.getProfiler().push("explosion_blocks");
            List<Pair<ItemStack, BlockPos>> blockDrops = new ArrayList<>();
            Util.shuffle(affectedBlocks, world.random);

            ObjectListIterator<BlockPos> affectedBlocksIterator = affectedBlocks.iterator();
            while(affectedBlocksIterator.hasNext()) {
                BlockPos nextBlockPos = affectedBlocksIterator.next();
                world.getBlockState(nextBlockPos).onExploded(this.world, nextBlockPos, this, (stack, pos) -> {
                    tryMergeStack(blockDrops, stack, pos);
                });

                //create fire logic in same iteration
                if (createFire && this.random.nextInt(3) == 0 && this.world.getBlockState(nextBlockPos).isAir() && this.world.getBlockState(nextBlockPos.down()).isOpaqueFullCube(this.world, nextBlockPos.down())) {
                    this.world.setBlockState(nextBlockPos, AbstractFireBlock.getState(this.world, nextBlockPos));
                }
            }

            Iterator<Pair<ItemStack, BlockPos>> blockDropsIterator = blockDrops.iterator();
            while(blockDropsIterator.hasNext()) {
                Pair<ItemStack, BlockPos> pair = blockDropsIterator.next();
                Block.dropStack(this.world, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
            }

            this.world.getProfiler().pop();
        }
    }

    //necessary private methods copied from Explosion class
    private static void tryMergeStack(List<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        for(int i = 0; i < stacks.size(); ++i) {
            Pair<ItemStack, BlockPos> pair = stacks.get(i);
            ItemStack itemStack = pair.getFirst();
            if (ItemEntity.canMerge(itemStack, stack)) {
                stacks.set(i, Pair.of(ItemEntity.merge(itemStack, stack, 16), pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        stacks.add(Pair.of(stack, pos));
    }

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
}
