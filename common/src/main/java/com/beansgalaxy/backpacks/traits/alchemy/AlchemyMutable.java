package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.traits.abstract_traits.IMutableSelectionTrait;
import com.beansgalaxy.backpacks.traits.generic.MutableChestLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelectorData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AlchemyMutable extends MutableChestLike<AlchemyTraits> implements IMutableSelectionTrait {
      private final SlotSelectorData selection;
      
      public AlchemyMutable(AlchemyTraits traits, ComponentHolder holder) {
            super(traits, holder);
            selection = new SlotSelectorData(holder, this::getItemStacks);
      }
      
      @Override
      public void push() {
            super.push();
            selection.push();
      }
      
      @Override
      public boolean isAcceptableSelection(ItemStack stack) {
            return traits.canItemFit(holder, stack);
      }
      
      @Override
      public int getSelectedSlot(Player player) {
            return selection.getSelectedSlot(player);
      }
      
      @Override
      public void growSelectedSlot(int slot) {
            selection.growSelectedSlot(slot);
      }
      
      @Override
      public void limitSelectedSlot(int index) {
            selection.limitSelectedSlot(index, getItemStacks().size());
      }
      
      @Override
      public void setSelectedSlot(Player player, int slot) {
            selection.setSelectedSlot(player, slot);
      }
}
