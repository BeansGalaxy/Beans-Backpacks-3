package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.access.TraitMenuAccessor;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
      @Inject(method="lambda$onPress$0", cancellable = true, at=@At("HEAD"))
      private static void onScreenPress(boolean[] aboolean, Screen screen, double d0, double d1, int i, CallbackInfo ci) {
            if (screen instanceof TraitMenuAccessor access) {
                  CallbackInfoReturnable<Boolean> cir = new CallbackInfoReturnable<>("click_trait_menu", true);
                  access.clickTraitMenu(d0, d1, i, cir);
                  if (cir.isCancelled()) {
                        aboolean[0] = cir.getReturnValue();
                        ci.cancel();
                  }
            }
      }
}
