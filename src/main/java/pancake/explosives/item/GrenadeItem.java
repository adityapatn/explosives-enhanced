package pancake.explosives.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import pancake.explosives.entity.GrenadeEntity;

public class GrenadeItem extends Item implements ProjectileItem {
    public GrenadeItem(Item.Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            GrenadeEntity grenade = new GrenadeEntity(world, player);
            grenade.setItem(itemStack);
            grenade.setOwner(player);
            grenade.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 0.5F, 1.0F);
            world.spawnEntity(grenade);
        }

        player.incrementStat(Stats.USED.getOrCreateStat(this));
        player.getItemCooldownManager().set(this, 10);
        itemStack.decrementUnlessCreative(1, player);
        return TypedActionResult.success(itemStack, world.isClient());
    }

    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        SnowballEntity snowballEntity = new SnowballEntity(world, pos.getX(), pos.getY(), pos.getZ());
        snowballEntity.setItem(stack);
        return snowballEntity;
    }
}
