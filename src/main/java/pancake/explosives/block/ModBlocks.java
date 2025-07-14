package pancake.explosives.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.BlockSoundGroup;
import pancake.explosives.ExplosivesEnhanced;

/*
 * Steps for adding a new item:
 * Initialize it in class ModItems as an Item
 * Add it to inventory group
 * Add texture
 * Add model json file - name should be same as item name
 * Add item name translation in en_us.json
 * Add recipe
 * Add loot table
 * */

public class ModBlocks {

    public static final Block DynamiteBlock = registerBlock("dynamite_block", new DynamiteBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.GRASS).strength(1.0f)));
    public static final Block LandscaperBlock = registerBlock("landscaper_block", new Block(AbstractBlock.Settings.create().sounds(BlockSoundGroup.CROP).strength(1.0f)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(ExplosivesEnhanced.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(ExplosivesEnhanced.MOD_ID, name), new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        ExplosivesEnhanced.LOGGER.info("Registering Mod Blocks for " + ExplosivesEnhanced.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(DynamiteBlock);
            entries.add(LandscaperBlock);
        });
    }
}