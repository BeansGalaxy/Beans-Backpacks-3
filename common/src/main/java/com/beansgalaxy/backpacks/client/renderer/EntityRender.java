package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class EntityRender extends EntityRenderer<BackpackEntity> implements RenderBackpack {
      ModelLayerLocation BACKPACK_MODEL = new ModelLayerLocation(ResourceLocation.parse(Constants.MOD_ID + ":backpack_model"), "main");
      ResourceLocation TEXTURE = ResourceLocation.parse(Constants.MOD_ID + ":textures/entity/backpack/null.png");
      public final BackpackModel<BackpackEntity> model;
      private final ItemRenderer itemRenderer;
      private final BlockRenderDispatcher blockDispatcher;

      public EntityRender(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.model = new BackpackModel<>(ctx.bakeLayer(BACKPACK_MODEL));
            this.itemRenderer = ctx.getItemRenderer();
            this.blockDispatcher = ctx.getBlockRenderDispatcher();
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
      public void render(@NotNull BackpackEntity backpack, float yaw, float tick, @NotNull PoseStack pose, @NotNull MultiBufferSource source, int light) {
            if (backpack.isRemoved())
                  return;

            double breakTime = backpack.viewable.wobble;
            yaw += (float) ((breakTime * 0.80f) * Math.sin(breakTime / Math.PI * 3));

            // ============================================ BACKPACK RENDER ============================================

            ItemStack stack = backpack.getEntityData().get(BackpackEntity.ITEM_STACK);

            pose.pushPose();
            pose.mulPose(Axis.YN.rotationDegrees(yaw + 180));

            IEntityTraits<?> traits = backpack.getTraits();
            ResourceLocation texture = traits.getTexture();

            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180));
            pose.translate(0, -10/16f, -4/16f);

            ViewableBackpack viewable = backpack.viewable;
            if (viewable.lastDelta > tick)
                  viewable.updateOpen();

            float headPitch = Mth.lerp(tick, viewable.lastPitch, viewable.headPitch) * 0.25f;
            model().setOpenAngle(headPitch);
            viewable.lastDelta = tick;

            pose.translate(0, 13 / 16f, 0);
            renderTexture(pose, source, light, texture, stack, viewable);
            pose.popPose();


            pose.popPose();

            renderNameAndHitbox(pose, source, backpack, yaw, light);

            // ============================================= DESTROY DECAL =============================================

//            if (backpack.breakAmount > 0) {
//                  pose.pushPose();
//                  int breakStage = Math.min(Mth.ceil(backpack.breakAmount / 3f), 7);
//                  ResourceLocation location = ResourceLocation.parse(Constants.MOD_ID + ":textures/entity/destroy_stage/" + breakStage + ".png");
//                  VertexConsumer crumble = source.getBuffer(RenderType.crumbling(location));
//                  customModel.renderToBuffer(pose, crumble, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
//                  pose.popPose();
//            }



      }

      @Override
      public ResourceLocation getTextureLocation(BackpackEntity var1) {
            return TEXTURE;
      }

      public void renderNameAndHitbox(PoseStack pose, MultiBufferSource mbs, BackpackEntity entity, float yaw, int light) {
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.options.hideGui && minecraft.hitResult instanceof EntityHitResult hitResult && hitResult.getEntity() == entity) {
                  if (this.shouldShowName(entity)) {
                        Component displayName = entity.getDisplayName();
                        if (!Constants.isEmpty(displayName)) {
                              pose.pushPose();
                              double $$5 = this.entityRenderDispatcher.distanceToSqr(entity);
                              if (!($$5 > 4096.0)) {
                                    Direction direction = entity.getDirection();
                                    double y = entity.getY() + 1;// entity.getNameTagOffsetY();
                                    double yOff = entity.getEyeY() - entityRenderDispatcher.camera.getPosition().y;
                                    y -= yOff / 16.0;
                                    pose.translate(direction.getStepX() * 5/16.0, y, direction.getStepZ() * 5/16.0);
                                    pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
                                    pose.scale(-0.02F, -0.02F, 1F);
                                    Matrix4f $$9 = pose.last().pose();
                                    float $$10 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                                    int $$11 = (int)($$10 * 255.0F) << 24;
                                    Font $$12 = this.getFont();
                                    float $$13 = (float)(-$$12.width(displayName) / 2);
                                    $$12.drawInBatch(displayName, $$13, 0, 553648127, false, $$9, mbs, Font.DisplayMode.SEE_THROUGH, $$11, light);
                                    $$12.drawInBatch(displayName, $$13, 0, -1, false, $$9, mbs, Font.DisplayMode.NORMAL, 0, light);
                              }
                              pose.popPose();
                        }
                  }

                  pose.pushPose();
                  AABB box;
                  if (!entity.getDirection().getAxis().isHorizontal()) {
                        double h = 9D / 16;
                        double w = 8D / 32;
                        double d = 4D / 32;
                        box = new AABB(w, 0, d, -w, h, -d);
                        box.move(-entity.getX(), -entity.getY(), -entity.getZ());
                  } else {
                        box = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
                        float yRot = entity.getDirection().toYRot();
                        yaw += yRot;
                  }

                  pose.mulPose(Axis.YN.rotationDegrees(yaw));
                  VertexConsumer vertices = mbs.getBuffer(RenderType.lines());
                  LevelRenderer.renderLineBox(pose, vertices, box, 0, 0, 0, 0.4f);
                  pose.popPose();
            }
      }
}