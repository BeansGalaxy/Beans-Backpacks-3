package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

      @Inject(method = "renderArmWithItem", at = @At("HEAD"))
      private void backpacks_renderArmWithItem(
                  AbstractClientPlayer pPlayer,
                  float pPartialTicks,
                  float pPitch,
                  InteractionHand pHand,
                  float pSwingProgress,
                  ItemStack ignored,
                  float pEquippedProgress,
                  PoseStack pPoseStack,
                  MultiBufferSource pBuffer,
                  int pCombinedLight,
                  CallbackInfo ci,
                  @Local(argsOnly = true, ordinal = 0) LocalRef<ItemStack> pStack
      ) {
            ItemStack selection = ISlotSelectorTrait.getFoodstuffsSelection(pStack.get(), pPlayer);
            if (selection != null)
                  pStack.set(selection);
      }
}
