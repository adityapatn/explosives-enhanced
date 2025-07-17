package pancake.explosives.entity;

import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;
import pancake.explosives.CustomExplosion;
import pancake.explosives.ExplosivesEnhanced;
import pancake.explosives.item.ModItems;
import net.minecraft.world.explosion.Explosion;
import pancake.explosives.registry.ModDamageTypes;

/*
* Damage Source Checklist:
* Does not crash on explode
* Damages owner when thrown
* Kills owner with default death message
* Kills others with player death message
* Damages blocks and drops them
* */

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
    public void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getWorld();
        if (!world.isClient()) {
            float explosionPower = 2.0F;
            Entity owner = this.getOwner();
            ExplosivesEnhanced.LOGGER.info("Owner: " + owner);
            ExplosivesEnhanced.LOGGER.info("Is owner living: " + (owner instanceof LivingEntity));

            DamageSource grenadeDamageSource = ModDamageTypes.createGrenadeEntityDamage(world, this, owner);
            //(this, null) does not cause .player message, (this, owner) doesn't either: for default explosion

            //world.createExplosion(this, grenadeDamageSource, new ExplosionBehavior(), getX(), getY(), getZ(), explosionPower, false, World.ExplosionSourceType.TNT);

            CustomExplosion grenadeExplosion = new CustomExplosion(world, this, grenadeDamageSource, null, prevX, prevY, prevZ, 4.0F, false, World.ExplosionSourceType.TNT, null, null, SoundEvents.ENTITY_GENERIC_EXPLODE);
            grenadeExplosion.explode();
        ExplosivesEnhanced.LOGGER.info("Causing explosion.");

            this.discard();
        }
    }
}