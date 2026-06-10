package com.beansgalaxy.backpacks.mixin.client.render.allay;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AllayRenderState.class)
public class AllayRenderStateMixin implements BackpackRenderState {
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
