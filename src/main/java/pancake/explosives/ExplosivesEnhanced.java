package pancake.explosives;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pancake.explosives.block.ModBlocks;
import pancake.explosives.entity.ModEntities;
import pancake.explosives.item.ModItemGroups;
import pancake.explosives.item.ModItems;
import pancake.explosives.registry.ModDamageTypes;

/* TODO
 * Add Landscaper textures
 * Create Landscaper up and down textures
 * Add Landscaper BlockState behavior
 */

public class ExplosivesEnhanced implements ModInitializer {
	public static final String MOD_ID = "explosives-enhanced";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.

		LOGGER.info("It works?");

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModEntities.registerModEntities();
		ModDamageTypes.registerModDamageTypes();
	}
}