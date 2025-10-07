package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.items.ModItems;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemModelShaper;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
      @Shadow @Final private ItemModelShaper itemModelShaper;

      @Shadow @Final private Minecraft minecraft;

      @Shadow public abstract BakedModel getModel(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int seed);

      @ModifyArg(method = "render", at = @At(value = "INVOKE",
                  target = "Lnet/neoforged/neoforge/client/ClientHooks;handleCameraTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemDisplayContext;Z)Lnet/minecraft/client/resources/model/BakedModel;"))
      private BakedModel backpacks_item_displays(
                  BakedModel pModel,
                  @Local(ordinal = 0, argsOnly = true) ItemStack itemStack,
                  @Local(ordinal = 0, argsOnly = true) ItemDisplayContext displayContext,
                  @Local(ordinal = 1) boolean flag) {
            if (!flag && itemStack.is(ModItems.QUIVER.get())) {
                  ModelResourceLocation location = Services.PLATFORM.getModelVariant(
                              ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack/quiver")
                  );

                  pModel = itemModelShaper.getModelManager().getModel(location);
            }
            else if (displayContext.firstPerson()) {
                  LocalPlayer player = minecraft.player;
                  ItemStack food = ISlotSelectorTrait.getFoodstuffsSelection(itemStack, player);
                  if (food != null) {
                        BakedModel foodModel = getModel(food, minecraft.level, player, player.getId());
                        return foodModel;
                  }
            }
            Optional<DisplayComponent> optional = DisplayComponent.get(itemStack);
            if (optional.isPresent()) {
                  DisplayComponent display = optional.get();
                  ModelResourceLocation modelLocation = display.getModel();
                  pModel = itemModelShaper.getModelManager().getModel(modelLocation);
            }
            else {
                  ReferenceTrait reference = itemStack.get(Traits.REFERENCE);
                  if (reference != null) {
                        Optional<DisplayComponent> display = reference.getDisplay();
                        if (display.isPresent()) {
                              ModelResourceLocation modelLocation = display.get().getModel();
                              pModel = itemModelShaper.getModelManager().getModel(modelLocation);
                        }
                  }
            }

            return pModel;
      }
}
