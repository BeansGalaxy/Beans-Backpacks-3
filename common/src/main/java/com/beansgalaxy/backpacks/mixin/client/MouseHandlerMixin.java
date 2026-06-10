package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
      @Shadow @Final private Minecraft minecraft;
      private long lastFailedTraitScroll = 0;
      
      @Inject(method="onScroll", cancellable = true, at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;"))
      private void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci, @Local int i) {
            long millis = Util.getMillis();
            LocalPlayer player = minecraft.player;
            if (!BackData.get(player).isActionKeyDown()) {
                  lastFailedTraitScroll = millis;
                  return;
            }
            
            if (millis - lastFailedTraitScroll < 550L) {
                  lastFailedTraitScroll = millis;
                  return;
            }
            
            ItemStack inHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            ISlotSelectorTrait trait = ISlotSelectorTrait.get(inHand);
            if (trait == null) {
                  lastFailedTraitScroll = millis;
                  return;
            }
            
            trait.mouseScrolled(player, ComponentHolder.of(inHand), -1, -1, i);
            ci.cancel();
      }
}
