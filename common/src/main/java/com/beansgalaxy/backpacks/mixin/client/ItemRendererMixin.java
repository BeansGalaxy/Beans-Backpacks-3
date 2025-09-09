package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

      @Shadow @Final private ItemModelShaper itemModelShaper;

      @Shadow @Final private Minecraft minecraft;

      @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
      private BakedModel backpackRenderInGUI(BakedModel pModel, ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pCombinedLight, int pCombinedOverlay) {
            if (!pItemStack.isEmpty()) {
                  boolean flag = pDisplayContext == ItemDisplayContext.GUI || pDisplayContext == ItemDisplayContext.GROUND || pDisplayContext == ItemDisplayContext.FIXED;
                  if (!flag) {
                        BakedModel itemModel = itemModelShaper.getItemModel(pItemStack);
                        BakedModel resolve = itemModel.getOverrides().resolve(itemModel, CommonClient.NO_GUI_STAND_IN, null, null, 0);
                        if (resolve == null)
                              return pModel;

                        BakedModel resolve1 = resolve.getOverrides().resolve(resolve, pItemStack, minecraft.level, null, 0);
                        if (itemModel == resolve1)
                              return pModel;

                        return resolve1;
                  }
            }
            return pModel;
      }

      @ModifyVariable(method = "render", argsOnly = true, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/client/resources/model/BakedModel;getTransforms()Lnet/minecraft/client/renderer/block/model/ItemTransforms;"))
      private BakedModel backpacks_render(BakedModel value,
                                          ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand,
                                          PoseStack pPoseStack, MultiBufferSource pBufferSource, int pCombinedLight, int pCombinedOverlay,
                                          @Local(ordinal = 0, argsOnly = true) boolean flag
      ) {
            Optional<DisplayComponent> optional = DisplayComponent.get(pItemStack);
            if (optional.isEmpty()) {
                  ReferenceTrait reference = pItemStack.get(Traits.REFERENCE);
                  if (reference == null)
                        return value;

                  Optional<DisplayComponent> display = reference.getDisplay();
                  if (display.isEmpty())
                        return value;

                  optional = display;
            }

            DisplayComponent display = optional.get();
            ModelResourceLocation modelLocation = display.getModel();
            return itemModelShaper.getModelManager().getModel(modelLocation);

      }
}
