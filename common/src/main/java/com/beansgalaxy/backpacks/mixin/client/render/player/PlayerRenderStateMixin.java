package com.beansgalaxy.backpacks.mixin.client.render.player;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AvatarRenderState.class)
public class PlayerRenderStateMixin implements BackpackRenderState {
      private Context backpackRenderStateContext;
      
      @Override
      public Context getBackpackRenderState() {
            return backpackRenderStateContext;
      }
      
      @Override
      public void setBackpackRenderState(Context context) {
            backpackRenderStateContext = context;
      }
}
