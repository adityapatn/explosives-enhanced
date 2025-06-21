package pancake.explosives.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
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

    @Override
    protected void onEntityHit(EntityHitResult hitResult) {
        super.onEntityHit(hitResult);
        Entity entity = hitResult.getEntity();
        if (!this.getWorld().isClient()) {
            DamageSource source = this.getDamageSources().thrown(this, this.getOwner());
            entity.damage(source, 1.0F);
            this.getWorld().createExplosion(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    4.0f,
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
                    this,
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
