package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class EntityRender extends EntityRenderer<BackpackEntity, BackpackEntityRenderState> implements RenderBackpack {
      ModelLayerLocation BACKPACK_MODEL = new ModelLayerLocation(Constants.defaultLocation("backpack_model"), "main");
      ResourceLocation TEXTURE = Constants.defaultLocation("textures/entity/backpack/null.png");
      public final BackpackModel<BackpackEntityRenderState> model;
      private final TextureAtlas atlas;

      public EntityRender(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.model = new BackpackModel<>(ctx.bakeLayer(BACKPACK_MODEL));
            this.atlas = ctx.getAtlas(AtlasIds.ARMOR_TRIMS);
      }
      
      @Override 
      public BackpackEntityRenderState createRenderState() {
            return new BackpackEntityRenderState();
      }
      
      @Override 
      public void extractRenderState(BackpackEntity entity, BackpackEntityRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            
            ViewableBackpack viewable = ViewableBackpack.get(entity);
            BackpackRenderState.set(state, ComponentHolder.of(entity), viewable, partialTick);
            
            double bobble = entity.getEntityData().get(BackpackEntity.WOBBLE);
            float wobble = (float) ((bobble * 0.80) * Math.sin(bobble / Math.PI * 3));
            state.yaw = wobble + entity.getYRot();
            state.breakStage = Math.min(Mth.ceil(entity.getBreaking() / 3f), 7);
            
            Direction direction = entity.getDirection();
            if (direction.getAxis().isHorizontal()) {
                  Vector3f step = direction.step();
                  state.nameTagAttachment = new Vec3(step.x * 0.4, 0.8, step.z * 0.4);
            }
            
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.options.hideGui && minecraft.hitResult instanceof EntityHitResult hitResult && hitResult.getEntity() == entity) {
                  
                  double h = 9D / 16;
                  double w = 8D / 32;
                  double d = 4D / 32;
                  AABB box = new AABB(w, 0, d, -w, h, -d);
//                  box.move(-state.x, -state.y, -state.z);
                  
                  state.hitBox = box;
            }
      }
      
      @Override 
      public TextureAtlas trimAtlas() {
            return atlas;
      }
      
      @Override
      public BackpackModel<BackpackEntityRenderState> model() {
            return model;
      }
      
      @Override 
      public void submit(BackpackEntityRenderState state, PoseStack pose, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
            model().setupAnim(state);
            
            super.submit(state, pose, nodeCollector, cameraRenderState);
            BackpackRenderState.Context context = state.getBackpackRenderState();
            
//    ================================================================================================================== BACKPACK RENDER
            pose.pushPose();
            pose.mulPose(Axis.YN.rotationDegrees(state.yaw + 180));
            
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180));
            pose.translate(0, -10/16f, -4/16f);
            
            pose.translate(0, 13 / 16f, 0);
            
            int light = state.lightCoords;
            renderTexture(pose, nodeCollector, light, context);
            
//    ================================================================================================================== DESTROY DECAL
            if (state.breakStage > 0) {
                  pose.pushPose();
                  ResourceLocation location = Constants.defaultLocation("textures/entity/destroy_stage/" + state.breakStage + ".png");
                  RenderType renderType_crumble = RenderType.crumbling(location);
                  nodeCollector.submitModelPart(model().body, pose, renderType_crumble, light, OverlayTexture.NO_OVERLAY, null);
                  pose.popPose();
            }
            
            pose.popPose();
            pose.popPose();
            
//    ================================================================================================================== HITBOX RENDER
            if (state.hitBox != null) {
                  nodeCollector.submitCustomGeometry(pose, RenderType.lines(), (pose1, consumer) -> {
                        pose1.rotate(Axis.YN.rotationDegrees(state.yaw));
                        ShapeRenderer.renderLineBox(pose1, consumer, state.hitBox, 0, 0, 0, 0.4f);
                  });
            }
      }
      
}