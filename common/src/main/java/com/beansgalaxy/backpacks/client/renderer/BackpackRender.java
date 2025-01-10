package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.util.Tint;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public interface BackpackRender {

      ModelLayerLocation BACKPACK_MODEL =
                  new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack_model"), "main");
      ModelLayerLocation PACK_CAPE_MODEL
                  = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack_cape_model"), "main");

      BackpackModel<?> model();

      ItemRenderer itemRenderer();

      BlockRenderDispatcher blockRenderer();

      default void renderTexture(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ResourceLocation texture, ItemStack itemStack, ViewableBackpack viewable) {
            tryRenderUtilities(pose, pBufferSource, pCombinedLight, itemStack, viewable);

            if (texture.equals(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "leather"))) {
                  builtInLeatherModel(pose, pBufferSource, pCombinedLight, itemStack);
                  return;
            }

            ResourceLocation location = texture.withPath(path -> "textures/backpack/" + path + ".png");
            VertexConsumer outer = pBufferSource.getBuffer(RenderType.entityCutout(location));
            model().renderBody(pose, outer, pCombinedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            model().renderMask(pose, outer, pCombinedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

            ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
            if (armorTrim != null) {
                  ResourceLocation pattern = armorTrim.pattern().value().assetId();
                  String material = armorTrim.material().value().assetName();
                  ResourceLocation trimTexture = ResourceLocation.fromNamespaceAndPath(pattern.getNamespace(),
                              "trims/backpacks/" + pattern.getPath() + '_' + material
                  );

                  TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
                  VertexConsumer trimVC = pBufferSource.getBuffer(RenderType.entityDecal(Sheets.ARMOR_TRIMS_SHEET));
                  VertexConsumer wrapped = atlas.getSprite(trimTexture).wrap(trimVC);

                  model().renderBody(pose, wrapped, pCombinedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

                  VertexConsumer buttonVertex = pBufferSource.getBuffer(RenderType.entityCutout(Sheets.ARMOR_TRIMS_SHEET));
                  VertexConsumer buttonWrap = atlas.getSprite(trimTexture).wrap(buttonVertex);
                  model().renderButton(pose, buttonWrap, pCombinedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            } else {
                  model().renderButton(pose, outer, pCombinedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
      }

      private void tryRenderUtilities(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ItemStack itemStack, ViewableBackpack viewable) {
            UtilityComponent utilities = itemStack.get(ITraitData.UTILITIES);
            if (utilities != null && !utilities.isBlank()) {
                  ItemStack first = utilities.get(0);
                  ItemStack second = utilities.get(1);

                  renderUtilities(pose, pBufferSource, pCombinedLight, first, viewable, true);
                  renderUtilities(pose, pBufferSource, pCombinedLight, second, viewable, false);

            }
      }

      private void renderConduit(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ItemStack first, ClientLevel level) {
            Matrix4f posed = pose.last().pose();
            pose.pushPose();
            itemRenderer().renderStatic(null, first, ItemDisplayContext.HEAD, false, pose, pBufferSource, level, pCombinedLight, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
      }

      private void renderUtilities(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ItemStack stack, ViewableBackpack viewable, boolean rightSide) {
            if (stack.isEmpty())
                  return;

            BakedModel model = resolveUtilitiesModel(stack);
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

      default BakedModel resolveUtilitiesModel(ItemStack stack) {
            ItemModelShaper itemModelShaper = this.itemRenderer().getItemModelShaper();
            if (stack.is(Items.WHITE_BANNER)) {
                  ModelManager modelmanager = itemModelShaper.getModelManager();
                  ModelResourceLocation modelLocation = Services.PLATFORM.getModelVariant(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack/ominous_banner"));
                  return modelmanager.getModel(modelLocation);
            }
            if (stack.is(Items.CONDUIT)) {
                  ModelManager modelmanager = itemModelShaper.getModelManager();
                  ModelResourceLocation modelLocation = Services.PLATFORM.getModelVariant(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack/conduit"));
                  return modelmanager.getModel(modelLocation);
            }

            BakedModel itemModel = itemModelShaper.getItemModel(stack);
            BakedModel resolve = itemModel.getOverrides().resolve(itemModel, CommonClient.UTILITY_DISPLAY_STAND_IN, null, null, 0);
            if (resolve == null)
                  return null;

            Minecraft minecraft = Minecraft.getInstance();
            return resolve.getOverrides().resolve(resolve, stack, minecraft.level, minecraft.player, minecraft.player == null ? 0 : minecraft.player.getId());
      }

      default void builtInLeatherModel(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ItemStack pItemStack) {
            DyedItemColor dyedItemColor = pItemStack.get(DataComponents.DYED_COLOR);
            int color = dyedItemColor == null ? Constants.DEFAULT_LEATHER_COLOR : dyedItemColor.rgb();

            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/backpack/leather/base.png");
            ResourceLocation detail = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/backpack/leather/detail.png");

            VertexConsumer outer = pBufferSource.getBuffer(RenderType.entityCutout(location));
            model().renderBody(pose, outer, pCombinedLight, OverlayTexture.NO_OVERLAY, color);
            model().renderMask(pose, outer, pCombinedLight, OverlayTexture.NO_OVERLAY, color);

            VertexConsumer buttonVC = pBufferSource.getBuffer(RenderType.entityCutout(detail));
            model().renderButton(pose, buttonVC, pCombinedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            model().renderMask(pose, buttonVC, pCombinedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

            Tint tint = new Tint(color);
            double brightness = tint.brightness();
            Tint.HSL hsl = tint.HSL();
            double lum = hsl.getLum();
            hsl.setLum((Math.cbrt(lum + 0.2) + lum) / 2).rotate(5).setSat(Math.sqrt((hsl.getSat() + brightness) / 2));
            int highColor = hsl.rgb();

            VertexConsumer detailVC = pBufferSource.getBuffer(RenderType.entityTranslucentCull(detail));
            model().renderBody(pose, detailVC, pCombinedLight, OverlayTexture.NO_OVERLAY, highColor);
      }

      default void renderBackpack(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ResourceLocation location, ItemStack itemStack, @Nullable LivingEntity entity, ClientLevel level, int seed) {
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.translate(-8 / 16f, -12 / 16f, -8 / 16f - 0.001f);

            ModelManager modelmanager = this.itemRenderer().getItemModelShaper().getModelManager();
            ModelResourceLocation modelLocation = Services.PLATFORM.getModelVariant(location);
            BakedModel bakedModel = modelmanager.getModel(modelLocation);
            BakedModel backpackModel = bakedModel.getOverrides().resolve(bakedModel, itemStack, level, entity, seed);

            if (backpackModel != null) {
                  VertexConsumer buffer = pBufferSource.getBuffer(Sheets.cutoutBlockSheet());
                  this.blockRenderer().getModelRenderer().renderModel(pose.last(), buffer, null, backpackModel, 1.0F, 1.0F, 1.0F, pCombinedLight, OverlayTexture.NO_OVERLAY);
            }
      }
}
