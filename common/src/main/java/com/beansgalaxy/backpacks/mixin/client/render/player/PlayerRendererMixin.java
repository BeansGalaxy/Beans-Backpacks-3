package com.beansgalaxy.backpacks.mixin.client.render.player;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.beansgalaxy.backpacks.client.renderer.AvatarFeature;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> {
      public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
            super(context, model, shadowRadius);
      }
      
      @Inject(method = "<init>", at = @At("RETURN"))
      public void registerBackRenderer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
            EntityModelSet modelSet = context.getModelSet();
            TextureAtlas atlas = context.getAtlas(AtlasIds.ARMOR_TRIMS);
            this.addLayer(new AvatarFeature(this, modelSet, atlas));
      }
      
      @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at=@At("TAIL"))
      private void extractRenderState(AvatarlikeEntity entity, AvatarRenderState reusedState, float partialTick, CallbackInfo ci) {
            ItemStack backpack = entity.getItemBySlot(EquipmentSlot.BODY);
            ViewableBackpack viewable = ViewableBackpack.get(entity);
            BackpackRenderState.setForAvatar(entity, reusedState, backpack, viewable, partialTick);
      }
      
      @Inject(method="getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at=@At("HEAD"))
      private static void getArmPose(
                  Avatar avatar,
                  ItemStack ignored,
                  InteractionHand hand,
                  CallbackInfoReturnable<HumanoidModel.ArmPose> cir,
                  @Local(ordinal=0, argsOnly=true) LocalRef<ItemStack> handItem
      ) {
            LunchBoxTraits.selectionIsPresent(handItem.get(), avatar, handItem::set);
      }
}
