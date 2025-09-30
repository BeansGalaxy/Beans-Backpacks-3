package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.traits.abstract_traits.MutableSlotSelector;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.item.ItemStack;

public class LunchBoxMutable extends MutableSlotSelector<LunchBoxTraits> {
      public LunchBoxMutable(LunchBoxTraits traits, ComponentHolder holder) {
            super(traits, holder);
      }

      @Override
      protected boolean isAcceptableSelection(ItemStack stack) {
            return traits.canItemFit(holder, stack);
      }
}
