package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LunchBoxMutable extends MutableBundleLike<LunchBoxTraits> {
      private final ITraitData<List<ItemStack>> nonEdibles;

      public LunchBoxMutable(LunchBoxTraits traits, ComponentHolder holder) {
            super(traits, holder);
            this.nonEdibles = ITraitData.NON_EDIBLES.get(holder);
      }

      @Override
      public boolean isEmpty() {
            return super.isEmpty() && nonEdibles.isEmpty();
      }

      @Override
      public ItemStack removeItem(int slot) {
            if (!nonEdibles.isEmpty())
                  return nonEdibles.get().removeFirst();

            return super.removeItem(slot);
      }

      public void addNonEdible(ItemStack consumedStack) {
            if (!consumedStack.isEmpty()) {
                  List<ItemStack> itemStacks = nonEdibles.get();
                  for (ItemStack nonEdible : itemStacks) {
                        if (ItemStack.isSameItemSameComponents(nonEdible, consumedStack)) {
                              int insert = Math.min(nonEdible.getMaxStackSize() - nonEdible.getCount(), consumedStack.getCount());
                              nonEdible.grow(insert);
                              consumedStack.shrink(insert);
                        }
                  }

                  if (!consumedStack.isEmpty()) {
                        itemStacks.add(consumedStack);
                  }
            }
      }

      @Override
      public void push() {
            nonEdibles.push();
            super.push();
      }
}
