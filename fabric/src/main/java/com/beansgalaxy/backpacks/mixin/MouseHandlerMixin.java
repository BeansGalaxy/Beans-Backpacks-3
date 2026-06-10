package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.access.TraitMenuAccessor;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
      
      @Shadow protected long lastClickTime;
      
      @Shadow protected int lastClickButton;
      
      @Inject(method="onButton", cancellable = true, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
      private void onScreenPress(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci, @Local Screen screen, @Local MouseButtonEvent event, @Local(ordinal = 1) long lastClickTime) {
            if (screen instanceof TraitMenuAccessor access) {
                  CallbackInfoReturnable<Boolean> cir = new CallbackInfoReturnable<>("click_trait_menu", true);
                  access.clickTraitMenu(event.x(), event.y(), event.button(), cir);
                  if (cir.isCancelled()) {
                        ci.cancel();
                        
                        this.lastClickTime = lastClickTime;
                        this.lastClickButton = buttonInfo.button();
                  }
            }
      }
}
