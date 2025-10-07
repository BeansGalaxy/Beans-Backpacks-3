package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.client.renderer.BackFeature;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxMutable;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
      public PlayerRendererMixin(EntityRendererProvider.Context pContext, PlayerModel<AbstractClientPlayer> pModel, float pShadowRadius) {
            super(pContext, pModel, pShadowRadius);
      }

      @Inject(method = "<init>", at = @At("RETURN"))
      public void registerBackRenderer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
            ItemRenderer itemRenderer = context.getItemRenderer();
            BlockRenderDispatcher blockDispatcher = context.getBlockRenderDispatcher();
            EntityModelSet modelSet = context.getModelSet();
            this.addLayer(new BackFeature(this, itemRenderer, modelSet, blockDispatcher));
      }

      @Inject(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
      private static void lunchboxArmPose(
                  AbstractClientPlayer pPlayer,
                  InteractionHand pHand,
                  CallbackInfoReturnable<HumanoidModel.ArmPose> cir,
                  @Local LocalRef<ItemStack> itemStack
      ) {
            LunchBoxTraits.selectionIsPresent(itemStack.get(), pPlayer, itemStack::set);
      }
}
