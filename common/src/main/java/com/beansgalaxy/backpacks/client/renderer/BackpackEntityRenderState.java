package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.access.BackpackRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class BackpackEntityRenderState extends EntityRenderState implements BackpackRenderState {
      public float yaw;
      public int breakStage;
      public Direction direction;
      private Context backpackRenderStateContext;
      public AABB hitBox = null;
      
      @Override
      public Context getBackpackRenderState() {
            return backpackRenderStateContext;
      }
      
      @Override
      public void setBackpackRenderState(Context context) {
            backpackRenderStateContext = context;
      }
}
