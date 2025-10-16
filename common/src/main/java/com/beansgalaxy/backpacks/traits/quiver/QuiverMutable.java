package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.abstract_traits.IMutableSelectionTrait;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableChestLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class QuiverMutable extends MutableChestLike<QuiverTraits> implements IMutableSelectionTrait {
      
      private final ITraitData<SlotSelection> selection;
      
      public QuiverMutable(QuiverTraits traits, ComponentHolder holder) {
            super(traits, holder);
            selection = ITraitData.NEW_SLOT_SELECTION.get(holder);
      }
      
      @Override
      public SlotSelection selection() {
            return selection.get();
      }
      
      @Override public void push() {
            selection.push();
            super.push();
      }
      
      @Override
      public boolean isAcceptableSelection(ItemStack stack) {
            return traits.canItemFit(holder, stack);
      }
      
      @Override
      public int getSelectedSlot(Player player) {
            return selection().get(player);
      }
      
      @Override
      public void growSelectedSlot(int slot) {
            selection().grow(slot);
      }
      
      @Override
      public void limitSelectedSlot(int index) {
            selection().limit(index, getItemStacks().size());
      }
}
