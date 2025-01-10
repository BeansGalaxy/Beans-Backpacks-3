package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.data.config.options.Orientation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UtilitySlot extends Slot {
      public UtilitySlot(UtilityContainer container, int index) {
            super(container, index, getX(index), getY(index));
      }

      public static int getX(int index) {
            int x = BackSlot.getX();
            Orientation orientation = CommonClass.CLIENT_CONFIG.back_and_utility_direction.get();
            if (orientation.isVertical())
                  return x;

            int mod = 18 * (index + 1);
            return orientation.isPositive()
                   ? x + mod
                   : x - mod;
      }

      public static int getY(int index) {
            int y = BackSlot.getY();
            Orientation orientation = CommonClass.CLIENT_CONFIG.back_and_utility_direction.get();
            if (!orientation.isVertical())
                  return y;

            int mod = 18 * (index + 1);
            return orientation.isPositive()
                   ? y + mod
                   : y - mod;
      }

      @Override
      public boolean mayPlace(ItemStack pStack) {
            return UtilityComponent.getType(pStack) != UtilityComponent.Type.NONE;
      }

      @Override
      public void setByPlayer(ItemStack pStack) {
            super.setByPlayer(pStack);
      }

      @Override
      public boolean isActive() {
            UtilityContainer container = (UtilityContainer) this.container;
            return container.size > getContainerSlot();
      }
}
