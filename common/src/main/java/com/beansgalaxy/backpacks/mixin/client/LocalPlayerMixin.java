package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.network.serverbound.UtilitiesUse;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
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
      @Shadow public Input input;

      protected LocalPlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Inject(method = "tick", at = @At("TAIL"))
      public void tick(CallbackInfo ci) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            KeyPress.INSTANCE.tick(minecraft, player);
            Shorthand.get(player).tickTimer(player.getInventory());
      }

      @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V", shift = At.Shift.AFTER))
      private void backpacks_aiStep(CallbackInfo ci, @Local(ordinal = 0) boolean wasJumping) {
            if (input.jumping && !wasJumping && isFallFlying()) {
                  UtilitiesUse.sendRocket();
            }
      }
}
