package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipmentModel;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class AllayFeature extends RenderLayer<Allay, AllayModel> implements BackpackRender {
      private final BackpackModel<Allay> backpackModel;
      private final ItemRenderer itemRenderer;
      private final BlockRenderDispatcher blockDispatcher;

      public AllayFeature(RenderLayerParent<Allay, AllayModel> pRenderer, ItemRenderer itemRenderer, EntityModelSet modelSet, BlockRenderDispatcher blockDispatcher) {
            super(pRenderer);
            this.itemRenderer = itemRenderer;
            this.backpackModel = new BackpackModel<>(modelSet.bakeLayer(BACKPACK_MODEL));
            this.blockDispatcher = blockDispatcher;
      }

      @Override
      public BackpackModel<?> model() {
            return backpackModel;
      }

      @Override
      public ItemRenderer itemRenderer() {
            return itemRenderer;
      }

      @Override
      public BlockRenderDispatcher blockRenderer() {
            return blockDispatcher;
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, Allay allay, float limbAngle, float limbDistance, float tick, float animationProgress, float playerHeadYaw, float playerHeadPitch) {
            ItemStack itemStack = allay.getItemBySlot(EquipmentSlot.BODY);
            Optional<EquipableComponent> optional = EquipableComponent.get(itemStack);
            if (optional.isEmpty())
                  return;

            EquipableComponent equipable = optional.get();
            ResourceLocation texture = equipable.backpackTexture();
            EquipmentModel model = equipable.customModel();

            pose.last().pose().translate(0, 0, 0);

            float pAgeInTicks = allay.tickCount + tick;
            float f3 = pAgeInTicks * 9.0F * 0.017453292F;

            float bobY = (float)Math.cos(f3) * 0.015F;

            if (texture != null) {
                  pose.pushPose();
                  ViewableBackpack viewable = ViewableBackpack.get(allay);
                  if (viewable.lastDelta > tick)
                        viewable.updateOpen();

                  float headPitch = Mth.lerp(tick, viewable.lastPitch, viewable.headPitch) * 0.25f;
                  model().setOpenAngle(headPitch);
                  viewable.lastDelta = tick;

                  pose.translate(0, 25.5f / 16f + bobY, 0.15f + 3/32f);
                  pose.mulPose(Axis.YP.rotationDegrees(180));
                  pose.mulPose(Axis.XN.rotationDegrees(16));
                  float scale = 0.875f;
                  pose.scale(scale, scale, scale);
                  renderTexture(pose, pBufferSource, pCombinedLight, texture, itemStack, viewable);
                  pose.popPose();
            }
            else if (model != null)
                  renderModel(pose, pBufferSource, pCombinedLight, allay, model, itemStack);
      }

      private void renderModel(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, LivingEntity player, EquipmentModel model, ItemStack itemStack) {
            ResourceLocation bodyLocation = model.attachments().get(EquipmentModel.Attachment.BODY);
            if (bodyLocation != null) {
                  pose.pushPose();
                  renderBackpack(pose, pBufferSource, pCombinedLight, bodyLocation, itemStack, player, Minecraft.getInstance().level, player.getId());
                  pose.popPose();
            }
            ResourceLocation backLocation = model.attachments().get(EquipmentModel.Attachment.BACK);
            if (backLocation != null) {
                  pose.pushPose();
                  renderBackpack(pose, pBufferSource, pCombinedLight, backLocation, itemStack, player, Minecraft.getInstance().level, player.getId());
                  pose.popPose();
            }
      }

}
