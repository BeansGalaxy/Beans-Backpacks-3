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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
      @Shadow @Final private ItemModelShaper itemModelShaper;

      @Shadow @Final private Minecraft minecraft;

      @ModifyArg(method = "render", at = @At(value = "INVOKE",
                  target = "Lnet/neoforged/neoforge/client/ClientHooks;handleCameraTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemDisplayContext;Z)Lnet/minecraft/client/resources/model/BakedModel;"))
      private BakedModel backpacks_item_displays(BakedModel pModel, @Local(ordinal = 0, argsOnly = true) ItemStack pItemStack, @Local(ordinal = 1) boolean flag) {
            Optional<DisplayComponent> optional = DisplayComponent.get(pItemStack);
            if (optional.isPresent()) {
                  DisplayComponent display = optional.get();
                  ModelResourceLocation modelLocation = display.getModel();
                  pModel = itemModelShaper.getModelManager().getModel(modelLocation);
            }
            else {
                  ReferenceTrait reference = pItemStack.get(Traits.REFERENCE);
                  if (reference != null) {
                        Optional<DisplayComponent> display = reference.getDisplay();
                        if (display.isPresent()) {
                              ModelResourceLocation modelLocation = display.get().getModel();
                              pModel = itemModelShaper.getModelManager().getModel(modelLocation);
                        }
                  }
            }

            if (flag)
                  return pModel;

            BakedModel resolve = pModel.getOverrides().resolve(pModel, CommonClient.NO_GUI_STAND_IN, null, null, 0);
            if (resolve == null)
                  return pModel;

            BakedModel resolve1 = resolve.getOverrides().resolve(resolve, pItemStack, minecraft.level, null, 0);
            return resolve1;
      }
}
