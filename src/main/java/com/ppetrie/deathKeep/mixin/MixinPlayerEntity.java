package com.ppetrie.deathKeep.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ppetrie.deathKeep.DeathKeep;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

    private static final List<Item> itemsToSave = Arrays.asList(
            Items.SHULKER_BOX,
            Items.WHITE_SHULKER_BOX,
            Items.ORANGE_SHULKER_BOX,
            Items.MAGENTA_SHULKER_BOX,
            Items.LIGHT_BLUE_SHULKER_BOX,
            Items.YELLOW_SHULKER_BOX,
            Items.LIME_SHULKER_BOX,
            Items.PINK_SHULKER_BOX,
            Items.GRAY_SHULKER_BOX,
            Items.LIGHT_GRAY_SHULKER_BOX,
            Items.CYAN_SHULKER_BOX,
            Items.PURPLE_SHULKER_BOX,
            Items.BLUE_SHULKER_BOX,
            Items.BROWN_SHULKER_BOX,
            Items.GREEN_SHULKER_BOX,
            Items.RED_SHULKER_BOX,
            Items.BLACK_SHULKER_BOX);
    
    @Shadow
    private final PlayerInventory inventory;

    @Shadow
    public int experienceLevel;
    
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        inventory = null;
    }

    @Override
    public void dropInventory() {
        super.dropInventory();
        vanishCursedItems();
        if (getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            DeathKeep.LOGGER.info("keepInventory is enabled; using custom drop logic");
            ArrayList<ItemStack> toDrop = new ArrayList<>();
            inventory.main.forEach((ItemStack is) -> {
                if (!shouldSave(is)) {
                    toDrop.add(is);
                }
            });
            inventory.offHand.forEach((ItemStack is) -> {
                if (!itemsToSave.contains(is.getItem())) {
                    toDrop.add(is);
                }
            });
            DeathKeep.LOGGER.info("Dropping {} items", toDrop.size());
            toDrop.forEach((ItemStack is) -> {
                double dropY = getEyeY() - (double)0.3f;
                ItemEntity itemEntity = new ItemEntity(getWorld(), getX(), dropY, getZ(), is.copy());
                itemEntity.setPickupDelay(40);
                float force = random.nextFloat() * 0.5f;
                float angle = random.nextFloat() * ((float)Math.PI * 2);
                itemEntity.setVelocity(-MathHelper.sin(angle) * force, 0.2f, MathHelper.cos(angle) * force);
                getWorld().spawnEntity(itemEntity);
            });
            inventory.remove((ItemStack is) -> toDrop.contains(is), 64, inventory);
        } else {
            inventory.dropAll();
        }
    }

    @Override
    public int getXpToDrop() {
        return experienceLevel * 7;
    }

    @Override
    protected void dropXp() {
        if (this.getWorld() instanceof ServerWorld) {
            int xpCount = getXpToDrop();
            DeathKeep.LOGGER.info("Dropping {} XP", xpCount);
            ExperienceOrbEntity.spawn((ServerWorld)this.getWorld(), this.getPos(), xpCount);
            ExperienceOrbEntity orbEntity = new ExperienceOrbEntity(getWorld(), getX(), getY(), getZ(), getXpToDrop());
            getWorld().spawnEntity(orbEntity);
        }
    }
    
    @Shadow
    protected void vanishCursedItems() {}
    
    @Shadow
    @Nullable
    public abstract ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

    private static boolean shouldSave(ItemStack stack) {
        return stack.hasEnchantments() || itemsToSave.contains(stack.getItem());
    }
}
