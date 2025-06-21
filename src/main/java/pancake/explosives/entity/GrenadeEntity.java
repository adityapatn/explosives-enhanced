package pancake.explosives.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import pancake.explosives.ExplosivesEnhanced;
import pancake.explosives.item.ModItems;

public class GrenadeEntity extends ThrownItemEntity {

    public GrenadeEntity(EntityType<? extends GrenadeEntity> entityType, World world) {
        super(entityType, world);
    }

    public GrenadeEntity(World world, LivingEntity owner) {
        super(EntityType.SNOWBALL, owner, world);
    }

    protected Item getDefaultItem() {
        return ModItems.GrenadeItem;
    }

    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getStack();
        return (ParticleEffect)(!itemStack.isEmpty() && !itemStack.isOf(this.getDefaultItem()) ? new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack) : ParticleTypes.ITEM_SNOWBALL);
    }

    public void handleStatus(byte status) {
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();

            for(int i = 0; i < 8; ++i) {
                this.getWorld().addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hitResult) {
        super.onEntityHit(hitResult);
        Entity entity = hitResult.getEntity();
        if (!this.getWorld().isClient()) {
            /*
            DamageSource source = this.getDamageSources().thrown(this, this.getOwner());
            entity.damage(source, 1.0F);
             */
            this.getWorld().sendEntityStatus(this, (byte) 3); //summon projectile hit particles
            this.getWorld().createExplosion(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    3.0f,
                    World.ExplosionSourceType.MOB
            );
            this.discard();
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        ExplosivesEnhanced.LOGGER.info("Grenade hit!");
        super.onCollision(hitResult);
        if (!this.getWorld().isClient()) {
            this.getWorld().createExplosion(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    4.0f,
                    World.ExplosionSourceType.MOB
            );
            this.discard();
        }
    }

}
