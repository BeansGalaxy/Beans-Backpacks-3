package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.access.PlayerAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
      @Inject(method = "getFieldOfViewModifier", at = @At("TAIL"), cancellable = true)
      private void backpacks_fovModifier(CallbackInfoReturnable<Float> cir) {
            PlayerAccessor access = (PlayerAccessor) this;
            if (access.isUtilityScoped()) {
                  CameraType type = Minecraft.getInstance().options.getCameraType();
                  float fov;
                  if (!type.isFirstPerson()) {
                        if (!type.isMirrored())
                              return;

                        fov = 0.5f;
                  }
                  else fov = 0.1f;

                  cir.setReturnValue(fov);
            }
      }
}
