package com.beansgalaxy.backpacks.mixin.client.render.armor_stand;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.beansgalaxy.backpacks.client.renderer.ArmorStandFeature;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin extends LivingEntityRenderer {
      @Unique public final ArmorStandRenderer instance = (ArmorStandRenderer) (Object) this;
      
      public ArmorStandRendererMixin(EntityRendererProvider.Context pContext, ArmorStandArmorModel pModel, float pShadowRadius) {
            super(pContext, pModel, pShadowRadius);
      }

      @Inject(method = "<init>", at = @At("RETURN"))
      public void registerBackRenderer(EntityRendererProvider.Context context, CallbackInfo ci) {
            EntityModelSet modelSet = context.getModelSet();
            TextureAtlas atlas = context.getAtlas(AtlasIds.ARMOR_TRIMS);
            this.addLayer(new ArmorStandFeature(this, modelSet, atlas));
      }
      
      @Inject(method="extractRenderState(Lnet/minecraft/world/entity/decoration/ArmorStand;Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;F)V", at=@At("TAIL"))
      private void extractRenderState(ArmorStand entity, ArmorStandRenderState reusedState, float partialTick, CallbackInfo ci) {
            ItemStack backpack = entity.getItemBySlot(EquipmentSlot.BODY);
            ViewableBackpack viewable = ViewableBackpack.get(entity);
            BackpackRenderState.set(reusedState, backpack, viewable, partialTick);
      }
}
