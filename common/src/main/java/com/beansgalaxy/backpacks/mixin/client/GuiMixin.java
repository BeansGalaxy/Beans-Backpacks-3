package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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

            CommonClient.renderCompassClockHUD(minecraft, drawContext, player);
      }

      @ModifyArg(method = "renderCameraOverlays", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
      private float backpacks_spyglassUtility(float pScopeScale) {
            return pScopeScale * pScopeScale * 1.5f;
      }

      private static final ResourceLocation CROSSHAIR_PLACE_INDICATOR = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "place_indicator");

      @Inject(method = "renderCrosshair", at = @At(value = "FIELD", ordinal = 0,
                  target = "Lnet/minecraft/client/Minecraft;crosshairPickEntity:Lnet/minecraft/world/entity/Entity;"))
      private void backpacks_renderPlaceProgress(GuiGraphics gui, DeltaTracker tick, CallbackInfo ci, @Local LocalFloatRef f, @Local LocalBooleanRef field) {

            int j = gui.guiHeight() / 2 - 7 + 15;
            int k = gui.guiWidth() / 2 - 8;
            gui.blitSprite(CROSSHAIR_PLACE_INDICATOR, k, j, 16, 16);
      }
}
