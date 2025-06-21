package pancake.explosives.entity;

import net.minecraft.entity.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pancake.explosives.ExplosivesEnhanced;

public class ModEntities {

    public static final EntityType<GrenadeEntity> Grenade = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(ExplosivesEnhanced.MOD_ID, "grenade"),
            EntityType.Builder.<GrenadeEntity>create(GrenadeEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f) // Size like a snowball
                    .maxTrackingRange(64)
                    .trackingTickInterval(10)
                    .build()
    );

    public static void registerModEntities() {
        System.out.println("Registering Mod Entities for " + ExplosivesEnhanced.MOD_ID);
        // Just calling this ensures static init runs
    }
}
