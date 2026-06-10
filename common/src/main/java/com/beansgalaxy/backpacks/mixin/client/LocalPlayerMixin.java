package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.network.serverbound.UtilitiesUse;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntity {
      @Shadow @Final protected Minecraft minecraft;
      
      @Shadow public ClientInput input;
      
      protected LocalPlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }
      
      @Inject(method="aiStep", at=@At(value="INVOKE", target="Lnet/minecraft/client/player/ClientInput;tick()V"))
      private void aiStep(CallbackInfo ci, @Local(ordinal = 0) boolean wasJumping) {
            if (input.keyPresses.jump() && !wasJumping && isFallFlying()) {
                  UtilitiesUse.sendRocket();
            }
      }
}
