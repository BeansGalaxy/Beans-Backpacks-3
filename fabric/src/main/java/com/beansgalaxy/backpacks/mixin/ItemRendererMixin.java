package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
      @Shadow @Final private ItemModelShaper itemModelShaper;

      @Shadow @Final private Minecraft minecraft;

      @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
                  target = "Lnet/minecraft/client/resources/model/BakedModel;getTransforms()Lnet/minecraft/client/renderer/block/model/ItemTransforms;"))
      private void backpacks_item_render(
                  ItemStack itemStack,
                  ItemDisplayContext displayContext,
                  boolean leftHand,
                  PoseStack poseStack,
                  MultiBufferSource bufferSource,
                  int combinedLight,
                  int combinedOverlay,
                  BakedModel ignored,
                  CallbackInfo ci,
                  @Local(ordinal = 0, argsOnly = true) LocalRef<BakedModel> model,
                  @Local(ordinal = 1) boolean flag
      ) {
            Optional<DisplayComponent> optional = DisplayComponent.get(itemStack);
            if (optional.isPresent()) {
                  ModelResourceLocation modelLocation = optional.get().getModel();
                  model.set(itemModelShaper.getModelManager().getModel(modelLocation));
            }
            else {
                  ReferenceTrait reference = itemStack.get(Traits.REFERENCE);
                  if (reference != null) {
                        Optional<DisplayComponent> display = reference.getDisplay();
                        if (display.isPresent()) {
                              ModelResourceLocation modelLocation = display.get().getModel();
                              model.set(itemModelShaper.getModelManager().getModel(modelLocation));
                        }
                  }
            }

            if (flag)
                  return;

            BakedModel itemModel = model.get();
            BakedModel resolve = itemModel.getOverrides().resolve(itemModel, CommonClient.NO_GUI_STAND_IN, null, null, 0);
            if (resolve == null)
                  return;

            BakedModel resolve1 = resolve.getOverrides().resolve(resolve, itemStack, minecraft.level, null, 0);
            model.set(resolve1);
      }
}
