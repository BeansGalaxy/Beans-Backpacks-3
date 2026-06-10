package com.beansgalaxy.backpacks.mixin.client.render.allay;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.beansgalaxy.backpacks.client.renderer.AllayFeature;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayRenderer.class)
public abstract class AllayRendererMixin extends MobRenderer<Allay, AllayRenderState, AllayModel> {
      @Unique public final AllayRenderer instance = (AllayRenderer) (Object) this;
      
      public AllayRendererMixin(EntityRendererProvider.Context pContext, AllayModel pModel, float pShadowRadius) {
            super(pContext, pModel, pShadowRadius);
      }

      @Inject(method = "<init>", at = @At("RETURN"))
      public void registerBackRenderer(EntityRendererProvider.Context context, CallbackInfo ci) {
            EntityModelSet modelSet = context.getModelSet();
            TextureAtlas atlas = context.getAtlas(AtlasIds.ARMOR_TRIMS);
            this.addLayer(new AllayFeature(instance, modelSet, atlas));
      }
      
      @Inject(method="extractRenderState(Lnet/minecraft/world/entity/animal/allay/Allay;Lnet/minecraft/client/renderer/entity/state/AllayRenderState;F)V", at=@At("TAIL"))
      private void extractRenderState(Allay allay, AllayRenderState state, float delta, CallbackInfo ci) {
            ItemStack backpack = allay.getItemBySlot(EquipmentSlot.BODY);
            ViewableBackpack viewable = ViewableBackpack.get(allay);
            BackpackRenderState.set(state, backpack, viewable, delta);
      }
}
