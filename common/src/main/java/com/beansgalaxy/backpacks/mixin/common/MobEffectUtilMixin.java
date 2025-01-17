package com.beansgalaxy.backpacks.mixin.common;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectUtil.class)
public class MobEffectUtilMixin {
      @Inject(method = "getDigSpeedAmplification", cancellable = true, at = @At("RETURN"))
      private static void backpacks_digSpeedAmplification(LivingEntity pEntity, CallbackInfoReturnable<Integer> cir,
                                                          @Local(ordinal = 0) int haste, @Local(ordinal = 1) int conduit
      ) {
            if (pEntity.hasEffect(MobEffects.DIG_SPEED) && pEntity.hasEffect(MobEffects.CONDUIT_POWER)) {
                  cir.setReturnValue(haste + conduit + 1);
            }
      }
}
