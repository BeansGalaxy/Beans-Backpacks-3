package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandFeature extends RenderLayer<ArmorStandRenderState, ArmorStandModel> implements RenderBackpack<ArmorStandRenderState> {
      private final BackpackModel<ArmorStandRenderState> backpackModel;
      private final TextureAtlas trimAtlas;
      
      public ArmorStandFeature(LivingEntityRenderer<ArmorStand, ArmorStandRenderState, ArmorStandModel> pRenderer, EntityModelSet modelSet, TextureAtlas trimAtlas) {
            super(pRenderer);
            this.backpackModel = new BackpackModel<>(modelSet.bakeLayer(BACKPACK_MODEL));
            this.trimAtlas = trimAtlas;
      }
      
      @Override
      public BackpackModel<ArmorStandRenderState> model() {
            return backpackModel;
      }
      
      @Override public TextureAtlas trimAtlas() {
            return trimAtlas;
      }
      
      @Override
      public void submit(PoseStack pose, SubmitNodeCollector collector, int i, ArmorStandRenderState state, float v, float v1) {
            BackpackRenderState.Context context = BackpackRenderState.get(state);
            if (context == null)
                  return;
            
            backpackModel.setupAnim(state);
            
            pose.pushPose();
            this.getParentModel().body.translateAndRotate(pose);
            
            pose.translate(0.0F, 0.0F, -1/32f);
            if (!state.chestEquipment.isEmpty())
                  pose.translate(0.0F, -1 / 16f, 3 / 32f);
            
            pose.translate(0, 13 / 16f, 0);
            renderTexture(pose, collector, i, context);
            pose.popPose();
      }
      
}
