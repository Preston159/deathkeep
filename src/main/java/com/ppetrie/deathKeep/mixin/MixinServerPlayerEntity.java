package com.ppetrie.deathKeep.mixin;

import com.mojang.authlib.GameProfile;
import com.ppetrie.deathKeep.DeathKeep;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(at = @At("RETURN"), method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V")
    private void mixinOnDeath(DamageSource damageSource, CallbackInfo callbackInfo)
    {
        DeathKeep.LOGGER.info("Resetting player XP");
        experienceLevel = 0;
        experienceProgress = 0.0f;
        totalExperience = 0;
    }
}
