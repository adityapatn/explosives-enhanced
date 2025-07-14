package pancake.explosives.registry;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import pancake.explosives.ExplosivesEnhanced;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> GRENADE_DAMAGE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ExplosivesEnhanced.MOD_ID, "grenade_entity"));

   /*
    public static final RegistryKey<DamageType> GRENADE_DAMAGE_KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("explosives-enhanced", "grenade_entity"));

    public static RegistryEntry<DamageType> getGrenadeDamageType(ServerWorld world) {
        RegistryWrapper<DamageType> damageTypes = world.getRegistryManager()
                .getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE);

        return damageTypes.getOptional(GRENADE_DAMAGE_KEY)
                .orElseThrow(() -> new IllegalStateException("Grenade DamageType not found"));
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
