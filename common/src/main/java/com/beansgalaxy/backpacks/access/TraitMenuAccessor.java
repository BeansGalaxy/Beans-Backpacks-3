package com.beansgalaxy.backpacks.access;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface TraitMenuAccessor {
      void clickTraitMenu(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir);
}
