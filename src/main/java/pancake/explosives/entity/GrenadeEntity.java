package pancake.explosives.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import pancake.explosives.CustomExplosion;
import pancake.explosives.ExplosivesEnhanced;
import pancake.explosives.item.ModItems;
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
            ExplosivesEnhanced.LOGGER.info("Is owner a player: " + (owner instanceof PlayerEntity));

            DamageSource grenadeDamageSource = ModDamageTypes.createGrenadeEntityDamage(world, this, owner);
            ExplosivesEnhanced.LOGGER.info("DamageSource attacker: " + grenadeDamageSource.getAttacker());
            ExplosivesEnhanced.LOGGER.info("DamageSource source: " + grenadeDamageSource.getSource());
            //(this, null) does not cause .player message, (this, owner) doesn't either: for default explosion
            
            //maybe issue is sourceType.TNT: tried .TNT, .MOB, .NONE, none send .player death message
            CustomExplosion grenadeExplosion = new CustomExplosion(world, this, grenadeDamageSource, null, prevX, prevY, prevZ, explosionPower, false, World.ExplosionSourceType.TNT, null, null, null);
            grenadeExplosion.explode();
            ExplosivesEnhanced.LOGGER.info("Causing explosion.");

            this.discard();
        }
    }
}