package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
      @Shadow @Final private Minecraft minecraft;

      @Shadow @Final private ItemRenderer itemRenderer;

      @Inject(method = "renderItem", cancellable = true, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V"))
      private void backpacks_renderItem(LivingEntity pEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pSeed, CallbackInfo ci) {
            BundleLikeTraits.get(pItemStack).ifPresentOrElse(traits -> {
                  traits.client().renderItemInHand(itemRenderer, traits, pEntity, ComponentHolder.of(pItemStack), pDisplayContext, pLeftHand, pPoseStack, pBuffer, pSeed, ci);
            }, () -> EnderTraits.get(pItemStack).ifPresent(enderTraits -> {
                  GenericTraits traits = enderTraits.getTrait(minecraft.level);
                  traits.client().renderItemInHand(itemRenderer, traits, pEntity, enderTraits, pDisplayContext, pLeftHand, pPoseStack, pBuffer, pSeed, ci);
            }));
      }
}
