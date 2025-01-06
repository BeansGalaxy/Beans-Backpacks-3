package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.beansgalaxy.backpacks.CommonClient;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public class GuiMixin {
      @Shadow @Final private Minecraft minecraft;

      @Inject(method = "renderItemHotbar", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"))
      public void render(GuiGraphics drawContext, DeltaTracker tickCounter, CallbackInfo callbackInfo, Player player) {
            if (minecraft.hitResult instanceof EntityHitResult hitResult && hitResult.getEntity() instanceof BackpackEntity backpack) {
                  backpack.getTraits().ifPresent(trait -> {
                        trait.client().renderEntityOverlay(minecraft, backpack, trait, drawContext, tickCounter);
                  });
            }

            CommonClient.renderShorthandHUD(minecraft, drawContext, tickCounter, player);
      }

      @ModifyArg(method = "renderCameraOverlays", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
      private float backpacks_spyglassUtility(float pScopeScale) {
            return pScopeScale * pScopeScale * 1.5f;
      }
}
