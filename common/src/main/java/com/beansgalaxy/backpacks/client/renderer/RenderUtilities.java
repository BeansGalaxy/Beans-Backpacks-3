package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public interface RenderUtilities {

      ItemRenderer itemRenderer();

      BlockRenderDispatcher blockRenderer();

      default void tryRenderUtilities(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ItemStack itemStack, ViewableBackpack viewable) {
            UtilityComponent utilities = itemStack.get(ITraitData.UTILITIES);
            if (utilities != null && !utilities.isBlank()) {
                  ItemStack first = utilities.get(0);
                  ItemStack second = utilities.get(1);

                  renderUtilities(pose, pBufferSource, pCombinedLight, first, viewable, true);
                  renderUtilities(pose, pBufferSource, pCombinedLight, second, viewable, false);

            }
      }

      default void renderUtilities(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ItemStack stack, ViewableBackpack viewable, boolean rightSide) {
            if (stack.isEmpty())
                  return;

            UtilityComponent.Type type = UtilityComponent.getType(stack);

            switch (type) {
                  case OMINOUS -> {
                        String path = "ominous_banner";
                        BakedModel model = getCustomModel(path);
                        renderItemModel(pose, pBufferSource, pCombinedLight, viewable, rightSide, model);
                  }
                  case CAULDRON -> {
                        String path = "cauldron";
                        BakedModel model = getCustomModel(path);
                        renderItemModel(pose, pBufferSource, pCombinedLight, viewable, rightSide, model);
                        blockRenderer().renderSingleBlock(Blocks.WATER.defaultBlockState(), pose, pBufferSource, pCombinedLight, OverlayTexture.NO_OVERLAY);
                  }
                  case NONE -> {

                  }
                  default -> {
                        BakedModel model = getCustomModel(stack, viewable);
                        renderItemModel(pose, pBufferSource, pCombinedLight, viewable, rightSide, model);
                  }
            }
      }

      private BakedModel getCustomModel(ItemStack stack, ViewableBackpack viewable) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            ModelManager modelmanager = itemRenderer().getItemModelShaper().getModelManager();
            ModelResourceLocation modelLocation = Services.PLATFORM.getModelVariant(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "utilities/" + key.getPath()));
            BakedModel resolve = modelmanager.getModel(modelLocation);

            Entity entity = viewable.entity();
            stack.setEntityRepresentation(entity);
            ClientLevel level = (ClientLevel) entity.level();
            return resolve.getOverrides().resolve(resolve, stack, level, null, viewable.getId());
      }

      private BakedModel getCustomModel(String path) {
            ModelManager modelmanager = itemRenderer().getItemModelShaper().getModelManager();
            ModelResourceLocation modelLocation = Services.PLATFORM.getModelVariant(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "utilities/" + path));
            return modelmanager.getModel(modelLocation);
      }

      private void renderItemModel(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ViewableBackpack viewable, boolean rightSide, BakedModel model) {
            if (model == null)
                  return;

            pose.pushPose();
            ItemDisplayContext displayContext = ItemDisplayContext.FIXED;
            model.getTransforms().getTransform(displayContext).apply(!rightSide, pose);
            pose.scale(.5f, .5f, .5f);
            if (rightSide) {
                  pose.translate(33/32f, -19/16f, -1f);
                  pose.mulPose(Axis.YN.rotationDegrees(90));
            }
            else {
                  pose.translate(-33/32f, -19/16f, 0f);
                  pose.mulPose(Axis.YP.rotationDegrees(90));
            }

            float fallDistance = viewable.fallDistance();
            boolean isFallFlying = false;
            float fallPitch = isFallFlying ? 0 : (float) -Math.log(fallDistance * 2 + 1);
            double y = fallPitch * 0.02;
            pose.translate(0, y, -fallPitch * 0.004);
            pose.mulPose(Axis.XP.rotationDegrees(fallPitch * 2));

            VertexConsumer buffer = pBufferSource.getBuffer(Sheets.cutoutBlockSheet());
            this.blockRenderer().getModelRenderer().renderModel(pose.last(), buffer, null, model, 1.0F, 1.0F, 1.0F, pCombinedLight, OverlayTexture.NO_OVERLAY);


            pose.popPose();
      }

}
