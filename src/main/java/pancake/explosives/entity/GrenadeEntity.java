package pancake.explosives.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import pancake.explosives.item.ModItems;
import net.minecraft.world.explosion.Explosion;
import pancake.explosives.registry.ModDamageTypes;

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

    //code for registering the damage type
    RegistryEntry<DamageType> grenadeDamageEntry = ModDamageTypes.getGrenadeDamageType((ServerWorld) this.getWorld());
    DamageSource grenadeDamageSource = new DamageSource(grenadeDamageEntry, this, this.getOwner());

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient()) {
            float explosionPower = 2.0F;
            this.getWorld().createExplosion(this, getX(), getY(), getZ(), explosionPower, false, World.ExplosionSourceType.TNT);

            //damaging the owner (not included in explosion damage)
            float damagePower = 4.0F;
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                //ExplosivesEnhanced.LOGGER.info("Damaging owner!");
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
                        //ExplosivesEnhanced.LOGGER.info("Owner damaged for " + damage + " hearts.");
                    }
                }
            }
            this.discard();
        }
    }
}
