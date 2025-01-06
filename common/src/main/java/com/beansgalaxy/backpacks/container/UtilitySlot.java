package com.beansgalaxy.backpacks.container;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UtilitySlot extends Slot {
      public UtilitySlot(UtilityContainer container, int index) {
            super(container, index, BackSlot.getX(), BackSlot.getY() - 18 - 18 * index);
      }

      @Override
      public boolean mayPlace(ItemStack pStack) {
            return super.mayPlace(pStack);
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
