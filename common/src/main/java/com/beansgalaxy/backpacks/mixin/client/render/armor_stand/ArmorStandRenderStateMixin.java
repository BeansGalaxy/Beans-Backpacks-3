package com.beansgalaxy.backpacks.mixin.client.render.armor_stand;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorStandRenderState.class)
public class ArmorStandRenderStateMixin implements BackpackRenderState {
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
