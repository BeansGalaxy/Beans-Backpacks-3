package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.container.Shorthand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

      @Shadow @Final private Minecraft minecraft;

      @Inject(method = "onScroll", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
      public void shorthandCancelMouseScroll(long pWindowPointer, double pXOffset, double pYOffset, CallbackInfo ci, boolean flag, double d0, double d1, double d2, int j, int i, int k) {
            LocalPlayer player = minecraft.player;
            Inventory inventory = player.getInventory();
            int itemsSize = inventory.items.size();
            int slot = inventory.selected - itemsSize;
            if (slot < 0)
                  return;

            Shorthand shorthand = Shorthand.get(player);
            shorthand.resetSelected(inventory);
            ci.cancel();
      }
}
