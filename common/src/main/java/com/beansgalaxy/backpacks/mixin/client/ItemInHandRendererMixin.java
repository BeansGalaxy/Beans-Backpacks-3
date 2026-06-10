package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
      @Inject(method="renderArmWithItem", at=@At("HEAD"))
      private void renderArmWithItem(
            AbstractClientPlayer player,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack ignored,
            float equippedProgress,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            CallbackInfo ci,
            @Local(argsOnly = true, ordinal = 0) LocalRef<ItemStack> item
      ) {
            ItemStack selection = ISlotSelectorTrait.getFoodstuffsSelection(item.get(), player);
            if (selection != null)
                  item.set(selection);
      }
}
