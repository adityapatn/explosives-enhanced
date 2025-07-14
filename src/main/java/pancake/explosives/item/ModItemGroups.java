package pancake.explosives.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pancake.explosives.ExplosivesEnhanced;
import pancake.explosives.block.ModBlocks;

public class ModItemGroups {
    public static final ItemGroup EnhancedExplosivesItemGroup = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(ExplosivesEnhanced.MOD_ID, "explosives_enhanced_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.GrenadeItem))
                    .displayName(Text.translatable("itemgroup.explosives-enhanced.explosives_enhanced_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.GrenadeItem);
                        entries.add(ModBlocks.DynamiteBlock);
                        entries.add(ModBlocks.LandscaperBlock);
                    })
                    .build());


    public static void registerItemGroups() {
        ExplosivesEnhanced.LOGGER.info("Registering Item Groups for " + ExplosivesEnhanced.MOD_ID);
    }
}
