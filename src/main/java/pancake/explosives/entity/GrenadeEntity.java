package pancake.explosives.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;
import pancake.explosives.ExplosivesEnhanced;
import pancake.explosives.item.ModItems;
import net.minecraft.world.explosion.Explosion;
import pancake.explosives.registry.ModDamageTypes;

/*
* Damage Source Checklist:
* Damages owner when thrown
* Kills owner with default death message
* Kills others with player death message */

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
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getWorld();
        if (!world.isClient()) {
            float explosionPower = 2.0F;
            Entity owner = this.getOwner();
            ExplosivesEnhanced.LOGGER.info("Owner: " + owner);
            ExplosivesEnhanced.LOGGER.info("Is owner living: " + (owner instanceof LivingEntity));

            DamageSource grenadeDamageSource = ModDamageTypes.createGrenadeEntityDamage(world, this, owner);
            //(this, null) never causes .player message, (this, owner) doesn't either

            world.createExplosion(this, grenadeDamageSource, new ExplosionBehavior(), getX(), getY(), getZ(), explosionPower, false, World.ExplosionSourceType.TNT);
            //damaging the owner (not included in explosion damage)
            ExplosivesEnhanced.LOGGER.info("NOT Manually damaging owner.");
            /*
            float damagePower = 4.0F;

            if (owner instanceof LivingEntity livingOwner) {
                double dx = livingOwner.getX() - getX();
                double dy = livingOwner.getEyeY() - getY();
                double dz = livingOwner.getZ() - getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                float radius = damagePower * 2.0f;
                if (distance <= radius) {
                    float exposure = Explosion.getExposure(getPos(), livingOwner);
                    if (exposure > 0) {
                        double normDist = distance / radius;
                        double impact = (1.0 - normDist) * exposure;
                        float damage = (float)((impact * impact + impact) * 3.5 * damagePower);


                        livingOwner.damage(grenadeDamageSource, damage); //damage source must not be explosion to affect owner
                        ExplosivesEnhanced.LOGGER.info("Owner damaged for " + damage + "hearts.");
                    }
                }
            }
            */
            this.discard();
        }
    }
}
