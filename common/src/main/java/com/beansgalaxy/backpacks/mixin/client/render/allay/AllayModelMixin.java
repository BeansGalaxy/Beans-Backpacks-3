package com.beansgalaxy.backpacks.mixin.client.render.allay;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayModel.class)
public abstract class AllayModelMixin extends EntityModel<AllayRenderState> {
      protected AllayModelMixin(ModelPart root) {
            super(root);
      }
      
      @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AllayRenderState;)V", at = @At("TAIL"))
      private void backpackSetupAnim(AllayRenderState renderState, CallbackInfo ci) {
            if (BackpackRenderState.get(renderState) != null) {
                  root.z += 3;
                  root.y -= 1;
            }
      }
}
