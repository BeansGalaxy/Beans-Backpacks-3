package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.components.equipable.EquipmentModel;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

public class ArmorStandFeature extends RenderLayer<ArmorStand, ArmorStandArmorModel> implements RenderBackpack {
      private final BackpackModel<ArmorStand> backpackModel;
      private final ItemRenderer itemRenderer;
      private final BlockRenderDispatcher blockDispatcher;

      public ArmorStandFeature(RenderLayerParent<ArmorStand, ArmorStandArmorModel> pRenderer, ItemRenderer itemRenderer, EntityModelSet modelSet, BlockRenderDispatcher blockDispatcher) {
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
      public void render(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ArmorStand armorStand, float limbAngle, float limbDistance, float tick, float animationProgress, float playerHeadYaw, float playerHeadPitch) {
            ItemStack itemStack = armorStand.getItemBySlot(EquipmentSlot.BODY);
            BackpackTraits traits = BackpackTraits.get(itemStack);
            if (traits == null)
                  return;
            
            pose.pushPose();
            this.getParentModel().body.translateAndRotate(pose);

            pose.translate(0.0F, (armorStand.isCrouching() ? 1 / 16f : 0), -1/32f);
            if (!armorStand.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                  pose.translate(0.0F, -1 / 16f, 3 / 32f);

            ViewableBackpack viewable = ViewableBackpack.get(armorStand);
            if (viewable.lastDelta > tick)
                  viewable.updateOpen();

            float headPitch = Mth.lerp(tick, viewable.lastPitch, viewable.headPitch) * 0.25f;
            model().setOpenAngle(headPitch);
            viewable.lastDelta = tick;

            pose.translate(0, 13 / 16f, 0);
            ResourceLocation texture = traits.getTexture();
            renderTexture(pose, pBufferSource, pCombinedLight, texture, itemStack, viewable);
            pose.popPose();
      }

      private void renderModel(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, LivingEntity player, EquipmentModel model, ItemStack itemStack) {
            model.attachments().forEach((attachment, location) -> {
                  pose.pushPose();
                  switch (attachment) {
                        case HEAD -> {
                              this.getParentModel().head.translateAndRotate(pose);
                              pose.translate(0, -12/16f, 0);
                        }
                        case BODY ->
                              this.getParentModel().body.translateAndRotate(pose);
                        case BACK -> {
                              this.getParentModel().body.translateAndRotate(pose);
                              if (player.isCrouching())
                                    pose.translate(0.0F, 1 / 16f, 0);
                              if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                                    pose.translate(0.0F, -1 / 16f, 1 / 16f);
                        }
                        case L_ARM -> {
                              this.getParentModel().leftArm.translateAndRotate(pose);
                              pose.translate(1/16f, -2/16f, 0);
                        }
                        case R_ARM -> {
                              this.getParentModel().rightArm.translateAndRotate(pose);
                              pose.translate(-1/16f, -2/16f, 0);
                        }
                        case L_LEG -> this.getParentModel().leftLeg.translateAndRotate(pose);
                        case R_LEG -> this.getParentModel().rightLeg.translateAndRotate(pose);
                  }

                  renderBackpack(pose, pBufferSource, pCombinedLight, location, itemStack, player, Minecraft.getInstance().level, player.getId());

                  pose.popPose();
            });
      }

}
