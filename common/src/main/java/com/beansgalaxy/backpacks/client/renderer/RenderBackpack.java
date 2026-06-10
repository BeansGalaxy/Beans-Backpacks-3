package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.beansgalaxy.backpacks.util.Tint;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public interface RenderBackpack<T extends EntityRenderState> extends RenderUtilities {

      ModelLayerLocation BACKPACK_MODEL =
                  new ModelLayerLocation(Constants.defaultLocation("backpack_model"), "main");
      ModelLayerLocation PACK_CAPE_MODEL
                  = new ModelLayerLocation(Constants.defaultLocation("backpack_cape_model"), "main");

      BackpackModel<T> model();
      
      TextureAtlas trimAtlas();
      
      default void renderTexture(PoseStack pose, SubmitNodeCollector collector, int light, BackpackRenderState.Context backpackState) {
            BackpackModel<T> model = model();
            
            if (backpackState.tint != null) {
                  // TRY RENDER LEATHER
                  builtInLeatherModel(pose, collector, light, backpackState.tint);
                  return;
            }
            
            // OR RENDER METAL
            ResourceLocation location = backpackState.texture.withPath(path -> "textures/backpack/" + path + ".png");
            RenderType renderType_texture = RenderType.armorCutoutNoCull(location);
            collector.submitModelPart(model.body, pose, renderType_texture, light, OverlayTexture.NO_OVERLAY, null);
            
            RenderType renderType_cull = RenderType.entityCutout(location);
            collector.submitModelPart(model.head_mask, pose, renderType_cull, light, OverlayTexture.NO_OVERLAY, null);
            collector.submitModelPart(model.body_mask, pose, renderType_cull, light, OverlayTexture.NO_OVERLAY, null);
            
            if (backpackState.trim != null) {
                  TextureAtlasSprite trim_sprite = trimAtlas().getSprite(backpackState.trim);
                  collector.submitModelPart(model.body, pose, RenderType.createArmorDecalCutoutNoCull(Sheets.ARMOR_TRIMS_SHEET), light, OverlayTexture.NO_OVERLAY, trim_sprite);
                  collector.submitModelPart(model.button, pose, RenderType.entityCutout(Sheets.ARMOR_TRIMS_SHEET), light, OverlayTexture.NO_OVERLAY, trim_sprite);
            } else {
                  collector.submitModelPart(model.button, pose, renderType_texture, light, OverlayTexture.NO_OVERLAY, null);
            }
      }
      
      default void builtInLeatherModel(PoseStack pose, SubmitNodeCollector collector, int light, Tint tint) {
            int color = tint.getRGBA();
            ResourceLocation location = Constants.defaultLocation("textures/backpack/leather/base.png");
            ResourceLocation detail = Constants.defaultLocation("textures/backpack/leather/detail.png");
            
            RenderType render_type_texture = RenderType.itemEntityTranslucentCull(location);
            collector.submitModelPart(model().body, pose, render_type_texture, light, OverlayTexture.NO_OVERLAY, null,  color, null);
            collector.submitModelPart(model().body_mask, pose, render_type_texture, light, OverlayTexture.NO_OVERLAY, null,  color, null);
            
            RenderType render_type_untinted = RenderType.itemEntityTranslucentCull(detail);
            collector.submitModelPart(model().button, pose, render_type_untinted, light, OverlayTexture.NO_OVERLAY, null);
            collector.submitModelPart(model().head_mask, pose, render_type_untinted, light, OverlayTexture.NO_OVERLAY, null);
            collector.submitModelPart(model().body_mask, pose, render_type_untinted, light, OverlayTexture.NO_OVERLAY, null);
            
            pose.pushPose();
            double brightness = tint.brightness();
            Tint.HSL hsl = tint.HSL();
            double lum = hsl.getLum();
            hsl.setLum((Math.cbrt(lum + 0.2) + lum) / 2).rotate(5).setSat(Math.sqrt((hsl.getSat() + brightness) / 2));
            int highlight_color = hsl.rgb();
            
            Matrix4f matrix = new Matrix4f();
            RenderSystem.getProjectionType().applyLayeringTransform(matrix, -1.0F);
            pose.mulPose(matrix);
            RenderType render_type_highlights = RenderType.itemEntityTranslucentCull(detail);
            collector.submitModelPart(model().body, pose, render_type_highlights, light, OverlayTexture.NO_OVERLAY, null, false, false, highlight_color, null, 0);
            pose.popPose();
      }
}
