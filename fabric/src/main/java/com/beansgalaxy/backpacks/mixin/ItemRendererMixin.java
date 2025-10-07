package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.items.ModItems;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
      @Shadow @Final private ItemModelShaper itemModelShaper;

      @Shadow @Final private Minecraft minecraft;

      @Shadow public abstract BakedModel getModel(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int seed);

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
            if (!flag && itemStack.is(ModItems.QUIVER.get())) {
                  ModelResourceLocation location = Services.PLATFORM.getModelVariant(
                              ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack/quiver")
                  );

                  BakedModel quiverModel = itemModelShaper.getModelManager().getModel(location);
                  BakedModel resolve = quiverModel.getOverrides().resolve(quiverModel, itemStack, minecraft.level, null, 1);
                  model.set(resolve);
            }
            else if (displayContext.firstPerson()) {
                  LocalPlayer player = minecraft.player;
                  ItemStack food = ISlotSelectorTrait.getFoodstuffsSelection(itemStack, player);
                  if (food != null) {
                        BakedModel foodModel = getModel(food, minecraft.level, player, player.getId());
                        model.set(foodModel);
                        return;
                  }
            }

            Optional<DisplayComponent> optional = DisplayComponent.get(itemStack);
            if (optional.isPresent()) {
                  ModelResourceLocation modelLocation = optional.get().getModel();
                  model.set(itemModelShaper.getModelManager().getModel(modelLocation));
                  return;
            }
            else {
                  ReferenceTrait reference = itemStack.get(Traits.REFERENCE);
                  if (reference != null) {
                        Optional<DisplayComponent> display = reference.getDisplay();
                        if (display.isPresent()) {
                              ModelResourceLocation modelLocation = display.get().getModel();
                              model.set(itemModelShaper.getModelManager().getModel(modelLocation));
                              return;
                        }
                  }
            }
      }

}
