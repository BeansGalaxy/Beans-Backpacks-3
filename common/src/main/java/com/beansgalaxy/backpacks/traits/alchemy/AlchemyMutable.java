package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.traits.abstract_traits.MutableSlotSelector;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.item.ItemStack;

public class AlchemyMutable extends MutableSlotSelector<AlchemyTraits> {
      public AlchemyMutable(AlchemyTraits traits, ComponentHolder holder) {
            super(traits, holder);
      }

      @Override
      protected boolean isAcceptableSelection(ItemStack stack) {
            return traits.canItemFit(holder, stack);
      }
}
