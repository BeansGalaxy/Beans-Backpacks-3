package com.beansgalaxy.backpacks.mixin;

import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Blocks.class)
public class BlocksMixin {
      @ModifyConstant(method="<clinit>", constant = @Constant(floatValue = 0.0F, ordinal = 0))
      private static float changePotDestroyTime(float constant) {
            return 0.3F;
      }
}
