package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Quaternionf;

public class AllayFeature extends RenderLayer<AllayRenderState, AllayModel> implements RenderBackpack<AllayRenderState> {
      private final BackpackModel<AllayRenderState> backpackModel;
      private final TextureAtlas trimAtlas;
      
      public AllayFeature(AllayRenderer pRenderer, EntityModelSet modelSet, TextureAtlas atlas) {
            super(pRenderer);
            this.backpackModel = new BackpackModel<>(modelSet.bakeLayer(BACKPACK_MODEL));
            this.trimAtlas = atlas;
      }

      @Override
      public BackpackModel<AllayRenderState> model() {
            return backpackModel;
      }
      
      @Override
      public TextureAtlas trimAtlas() {
            return trimAtlas;
      }
      
      @Override
      public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AllayRenderState entityState, float yRot, float xRot) {
            BackpackRenderState.Context backpackState = BackpackRenderState.get(entityState);
            if (backpackState == null)
                  return;
            
            backpackModel.setupAnim(entityState);
            
            pose.pushPose();
            ModelPart root = getParentModel().root();
            float yOff = root.y / 16f;
            pose.translate(0, -4.5f / 16f + yOff, 3/16f);
            
            
            float scale = 0.875f;
            pose.scale(scale, scale, scale);
            
            ModelPart body = root.getChild("body");
            ModelPart arm = body.getChild("right_arm");
            
            pose.mulPose(Axis.XP.rotation(body.xRot + arm.xRot + (90 * 0.017453292F)));
            pose.mulPose(Axis.YP.rotationDegrees(180));
//            pose.translate(0, 5/16f, 1/16f); // ARMS SLIDE UP BPACk
            pose.translate(0, 5.5/16f, 1.5/16f);
//            pose.translate(0, 10.5f/16f, 2.5f/16f); // ARMS SLIDE DOWN BPACK
            
            renderTexture(pose, collector, light, backpackState);
            pose.popPose();
      }
}
