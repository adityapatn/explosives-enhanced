package pancake.explosives.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.BlockSoundGroup;
import pancake.explosives.ExplosivesEnhanced;
import net.minecraft.sound.SoundEvents;

public class ModBlocks {

    public static final Block DynamiteBlock = registerBlock("dynamite_block", new Block(AbstractBlock.Settings.create().strength(1f)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(ExplosivesEnhanced.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(ExplosivesEnhanced.MOD_ID, name), new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        ExplosivesEnhanced.LOGGER.info("Registering Mod Blocks for " + ExplosivesEnhanced.MOD_ID);
    }
}
