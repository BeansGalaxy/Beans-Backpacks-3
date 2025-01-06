package com.beansgalaxy.backpacks.access;

import net.minecraft.world.item.ItemStack;

public interface PlayerAccessor {
      boolean isUtilityScoped();

      void setUtilityScoped(boolean isScoped);

      void setItemUsed(ItemStack stack);
}
