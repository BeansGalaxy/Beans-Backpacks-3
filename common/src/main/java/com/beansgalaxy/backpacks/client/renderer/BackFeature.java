package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipmentModel;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class BackFeature extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> implements RenderBackpack {
      private final BackpackModel<AbstractClientPlayer> model;
      private final BackpackCapeModel<AbstractClientPlayer> capeModel;
      private final ItemRenderer itemRenderer;
      private final BlockRenderDispatcher blockDispatcher;

      public BackFeature(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer, ItemRenderer itemRenderer, EntityModelSet modelSet, BlockRenderDispatcher blockDispatcher) {
            super(pRenderer);
            this.itemRenderer = itemRenderer;
            this.model = new BackpackModel<>(modelSet.bakeLayer(BACKPACK_MODEL));
            this.capeModel = new BackpackCapeModel<>(modelSet.bakeLayer(PACK_CAPE_MODEL));
            this.blockDispatcher = blockDispatcher;
      }

      @Override
      public BackpackModel<?> model() {
            return model;
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
      public void render(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, AbstractClientPlayer player, float limbAngle, float limbDistance, float tick, float animationProgress, float playerHeadYaw, float playerHeadPitch) {
            renderEquipables(pose, pBufferSource, pCombinedLight, player, tick);
//            renderShorthand(pose, pBufferSource, pCombinedLight, player);
      }

      private void renderShorthand(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, AbstractClientPlayer player) {
//            if (CommonClass.CLIENT_CONFIG.disable_shorthand_render.get())
//                  return;

            Shorthand shorthand = Shorthand.get(player);
            int selection = shorthand.selection;
            ItemStack stack = shorthand.getItem(selection);
            if (stack.isEmpty())
                  return;

            Inventory inventory = player.getInventory();
            int selected = inventory.selected - inventory.items.size();
            if (selection == selected)
                  return;

            pose.pushPose();
            this.getParentModel().body.translateAndRotate(pose);
            pose.translate(0, player.isCrouching() ? 6/16f : 5/16f, 5/32f);
            if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                  pose.translate(0.0F, -1/16f, 1 / 16f);

            pose.mulPose(Axis.ZN.rotationDegrees(90));
            pose.translate(0.001, -0.001, 0);

            BakedModel model = itemRenderer.getModel(stack, player.level(), player, player.getId());
            itemRenderer().render(stack, ItemDisplayContext.FIXED, false, pose, pBufferSource, pCombinedLight, OverlayTexture.NO_OVERLAY, model);
            pose.popPose();

      }

      private void renderEquipables(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, AbstractClientPlayer player, float tick) {
            if (CommonClass.CLIENT_CONFIG.disable_equipable_render.get())
                  return;

            EquipableComponent.runIfPresent(player, (equipable, slot) -> {
                  if (!equipable.slots().test(slot))
                        return;

                  ItemStack itemStack = player.getItemBySlot(slot);
                  ResourceLocation texture = equipable.backpackTexture();
                  EquipmentModel model = equipable.customModel();

                  if (texture != null) {
                        pose.pushPose();
                        this.getParentModel().body.translateAndRotate(pose);

                        ViewableBackpack viewable = ViewableBackpack.get(player);
                        if (viewable.lastDelta > tick)
                              viewable.updateOpen();

                        float headPitch = Mth.lerp(tick, viewable.lastPitch, viewable.headPitch) * 0.25f;
                        model().setOpenAngle(headPitch);
                        viewable.lastDelta = tick;

                        pose.translate(0, 13 / 16f, 0);
                        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                        if (CommonClass.CLIENT_CONFIG.elytra_model_equipment.get().contains(chestStack.getItem())) {
                              float xRot = getParentModel().body.xRot;
                              setUpWithWings(player, xRot, pose);
                        }
                        else {
                              pose.translate(0.0F, (player.isCrouching() ? 1 / 16f : 0), 0.0F);

                              if (!chestStack.isEmpty())
                                    pose.translate(0.0F, -1 / 16f, 1 / 16f);
                              renderCapeAbove(pose, pBufferSource, pCombinedLight, player, headPitch);
                        }

                        renderTexture(pose, pBufferSource, pCombinedLight, texture, itemStack, viewable);
                        pose.popPose();
                  }
                  else if (model != null)
                        renderModel(pose, pBufferSource, pCombinedLight, player, slot, model, itemStack);
            });
      }

      private void renderCapeAbove(PoseStack pose, MultiBufferSource mbs, int light, AbstractClientPlayer player, float headPitch) {
//            ResourceLocation cloakTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/cape_template.png");
            ResourceLocation cloakTexture = player.getSkin().capeTexture();
            if (cloakTexture != null) {
                  float fallDistance = player.fallDistance;
                  float fallPitch = player.isFallFlying() ? 0 : (float) (Math.log(fallDistance * 3 + 1)) * -0.05f;

                  capeModel.cape.yRot = (float) Math.PI * 2;
                  capeModel.cape.xRot = -headPitch;
                  capeModel.cape.y = fallPitch * 6 - 11f;
                  capeModel.cape.z = 2f;
                  RenderType renderType = RenderType.entitySolid(cloakTexture);
                  VertexConsumer vertexConsumer = mbs.getBuffer(renderType);
                  capeModel.cape.render(pose, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
      }

      private void setUpWithWings(AbstractClientPlayer player, float scale, PoseStack poseStack) {
            boolean fallFlying = player.isFallFlying();
            float wingSpread;
            if (fallFlying) {
                  poseStack.translate(0, 1/32f, 0);
                  Vec3 deltaMovement = player.getDeltaMovement();
                  Vec3 norm = deltaMovement.normalize();
                  if (norm.y > 0)
                        wingSpread = 0;
                  else wingSpread = (float) Math.pow(-norm.y, 1.5);
            }
            else wingSpread = 1;

            float xRot = 0.25f * wingSpread;
            float z = Mth.lerp(scale, fallFlying ? xRot : 4/16f, -3/32f);
            float y = Mth.lerp(scale, fallFlying ? -1/16f : 0, 3/16f);
            poseStack.translate(0, y, z);
            poseStack.mulPose(new Quaternionf().rotationXYZ(Mth.lerp(scale, xRot, 0), 0, 0));
      }

      private void renderModel(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, AbstractClientPlayer player, EquipmentSlot slot, EquipmentModel model, ItemStack itemStack) {
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

                  renderBackpack(pose, pBufferSource, pCombinedLight, location, itemStack, player, player.clientLevel, player.getId());

                  pose.popPose();
            });
      }

}
