package pancake.explosives.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import pancake.explosives.ExplosivesEnhanced;

/*
* Steps for adding a new item:
* Initialize it in class ModItems as an Item
* Add it to inventory group
* Add texture
* Add model json file - name should be same as item name
* Add item name translation in en_us.json
* Add recipe
* */

public class ModItems {

    public static final Item GrenadeItem = registerItem("grenade_item", new GrenadeItem(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(ExplosivesEnhanced.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ExplosivesEnhanced.LOGGER.info("Registering Mod Items for " + ExplosivesEnhanced.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
           entries.add(GrenadeItem);
        });
    }
}
