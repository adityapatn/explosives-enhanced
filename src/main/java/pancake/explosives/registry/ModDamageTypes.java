package pancake.explosives.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import pancake.explosives.ExplosivesEnhanced;

public class ModDamageTypes {

    public static DamageSource createGrenadeEntityDamage(World world, Entity source, Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ExplosivesEnhanced.MOD_ID, "grenade_entity"))), source, attacker);
    }

    /*
    public static final RegistryKey<DamageType> GRENADE_DAMAGE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ExplosivesEnhanced.MOD_ID, "grenade_entity"));
    public static DamageSource createGrenadeDamage(ServerWorld world, Entity attacker, Entity source) {
        RegistryEntry<DamageType> damageTypeEntry =
                world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(GRENADE_DAMAGE_KEY);
        return new DamageSource(damageTypeEntry, attacker, source);
    }


    public static final RegistryKey<DamageType> DYNAMITE_DAMAGE_KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("explosives-enhanced", "dynamite_block"));

    public static RegistryEntry<DamageType> getDynamiteDamageType(ServerWorld world) {
        RegistryWrapper<DamageType> damageTypes = world.getRegistryManager()
                .getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE);

        return damageTypes.getOptional(DYNAMITE_DAMAGE_KEY)
                .orElseThrow(() -> new IllegalStateException("Dynamite DamageType not found"));
    }

    public static final RegistryKey<DamageType> LANDSCAPER_DAMAGE_KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("explosives-enhanced", "landscaper_block"));

    public static RegistryEntry<DamageType> getLandscaperDamageType(ServerWorld world) {
        RegistryWrapper<DamageType> damageTypes = world.getRegistryManager()
                .getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE);

        return damageTypes.getOptional(LANDSCAPER_DAMAGE_KEY)
                .orElseThrow(() -> new IllegalStateException("Dynamite DamageType not found"));
    }
    */

    public static void registerModDamageTypes() {
        ExplosivesEnhanced.LOGGER.info("Registering Mod Damage Types for " + ExplosivesEnhanced.MOD_ID);
    }
}
