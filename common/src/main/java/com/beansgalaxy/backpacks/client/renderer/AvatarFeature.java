package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class AvatarFeature extends RenderLayer<AvatarRenderState, PlayerModel> implements RenderBackpack<AvatarRenderState> {
      private final BackpackModel<AvatarRenderState> model;
      private final BackpackCapeModel<AvatarRenderState> capeModel;
      private final TextureAtlas trimAtlas;
      
      public AvatarFeature(RenderLayerParent<AvatarRenderState, PlayerModel> pRenderer, EntityModelSet modelSet, TextureAtlas atlas) {
            super(pRenderer);
            this.model = new BackpackModel<>(modelSet.bakeLayer(BACKPACK_MODEL));
            this.capeModel = new BackpackCapeModel<>(modelSet.bakeLayer(PACK_CAPE_MODEL));
            this.trimAtlas = atlas;
      }

      @Override
      public BackpackModel<AvatarRenderState> model() {
            return model;
      }
      
      @Override
      public TextureAtlas trimAtlas() {
            return trimAtlas;
      }
      
      @Override
      public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float xRot, float yRot) {
            if (CommonClass.CLIENT_CONFIG.disable_equipable_render.get())
                  return;
            
            BackpackRenderState.Context context = BackpackRenderState.get(state);
            if (context == null)
                  return;
            
            model.setupAnim(state);
            
            pose.pushPose();
            this.getParentModel().body.translateAndRotate(pose);
            
            pose.translate(0, 13 / 16f, 0);
            ItemStack chestStack = state.chestEquipment;
            if (CommonClass.CLIENT_CONFIG.elytra_model_equipment.get().contains(chestStack.getItem())) {
                  pose.translate(0, context.wingY, context.wingZ);
                  pose.mulPose(new Quaternionf().rotationXYZ(context.wingXRot, 0, 0));
            }
            else {
                  pose.translate(0.0F, (Pose.CROUCHING.equals(state.pose) ? 1 / 16f : 0), 0.0F);
                  
                  if (!chestStack.isEmpty())
                        pose.translate(0.0F, -1 / 16f, 1 / 16f);
                  
                  renderCapeAbove(pose, collector, light, state, context);
            }
            
            renderTexture(pose, collector, light, context);
            pose.popPose();
      }
      
      private void renderCapeAbove(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, BackpackRenderState.Context context) {
            if (!state.showCape)
                  return;
            
//            ResourceLocation cloakTexture = Constants.defaultLocation("textures/cape_template.png");
            ClientAsset.Texture cape = state.skin.cape();
            if (cape != null) {
                  pose.pushPose();
                  
                  capeModel.cape.yRot = (float) Math.PI * 2;
                  capeModel.cape.xRot = -context.headPitch;
                  capeModel.cape.y = context.fallPitch - 11f;
                  capeModel.cape.z = 2f;
                  
                  RenderType renderType_cape = RenderType.entitySolid(cape.texturePath());
                  collector.submitModelPart(capeModel.cape, pose, renderType_cape, light, OverlayTexture.NO_OVERLAY, null);
                  pose.popPose();
            }
      }
      
}
