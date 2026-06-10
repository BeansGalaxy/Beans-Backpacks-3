package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.access.TraitMenuAccessor;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
      @Inject(method="onButton", at=@At(value = "JUMP", opcode = Opcodes.IFNE, ordinal = 2))
      private void onScreenClick(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci, @Local(ordinal = 2) LocalBooleanRef screenHandled, @Local Screen screen, @Local MouseButtonEvent event) {
            if (screenHandled.get())
                  return;
            
            if (screen instanceof TraitMenuAccessor access) {
                  CallbackInfoReturnable<Boolean> cir = new CallbackInfoReturnable<>("click_trait_menu", true);
                  access.clickTraitMenu(event.x(), event.y(), event.button(), cir);
                  if (cir.isCancelled())
                        screenHandled.set(true);
            }
      }
}
