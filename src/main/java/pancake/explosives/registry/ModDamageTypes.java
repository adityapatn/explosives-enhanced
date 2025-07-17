package pancake.explosives.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import pancake.explosives.ExplosivesEnhanced;

public class ModDamageTypes {

    public static DamageSource createGrenadeEntityDamage(World world, Entity source, Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ExplosivesEnhanced.MOD_ID, "grenade_entity"))), source, attacker);
    }

    public static DamageSource createDynamiteEntityDamage(World world, Entity source, Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ExplosivesEnhanced.MOD_ID, "dynamite_block"))), source, attacker);
    }

    public static DamageSource createLandscaperEntityDamage(World world, Entity source, Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ExplosivesEnhanced.MOD_ID, "landscaper_block"))), source, attacker);
    }

    public static void registerModDamageTypes() {
        ExplosivesEnhanced.LOGGER.info("Registering Mod Damage Types for " + ExplosivesEnhanced.MOD_ID);
    }
}
