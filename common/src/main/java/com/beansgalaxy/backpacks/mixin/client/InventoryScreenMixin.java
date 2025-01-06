package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.container.ShorthandSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

      public InventoryScreenMixin(InventoryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
            super(pMenu, pPlayerInventory, pTitle);
      }

      @Inject(method = "renderBg", at = @At("TAIL"))
      private void renderShortHandSlots(GuiGraphics graphics, float f, int $$2, int $$3, CallbackInfo ci) {
            int[] UV = {};
            CommonClient.renderShorthandSlots(graphics, leftPos, topPos, imageWidth, imageHeight, minecraft.player);
      }

      @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
      private void hasClickedShorthand(double mouseX, double mouseY, int leftPos, int topPos, int in4, CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot instanceof ShorthandSlot)
                  cir.setReturnValue(false);
      }
}
