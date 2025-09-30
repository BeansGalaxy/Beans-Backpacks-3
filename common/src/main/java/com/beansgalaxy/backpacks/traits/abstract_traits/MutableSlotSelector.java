package com.beansgalaxy.backpacks.traits.abstract_traits;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MutableSlotSelector<T extends BundleLikeTraits> extends MutableBundleLike<T> {
      private final SlotSelection selection;
      private final boolean dirtySelection;

      public MutableSlotSelector(T traits, ComponentHolder holder) {
            super(traits, holder);
            SlotSelection selection = holder.get(ITraitData.SLOT_SELECTION);
            if (selection == null) {
                  this.selection = new SlotSelection();
                  dirtySelection = true;
            }
            else {
                  this.selection = selection;
                  dirtySelection = false;
            }
      }

      @Override
      public void push() {
            super.push();


            if (stacks.isEmpty())
                  selection.clear();
            else {
                  int size = stacks.get().size();
                  selection.ceil(size - 1);

                  if (dirtySelection)
                        holder.set(ITraitData.SLOT_SELECTION, selection);
            }
      }

      public void setSelectedSlot(Player player, int slot) {
            selection.setSelectedSlot(player, slot);
      }

      @Override
      public int getSelectedSlot(Player player) {
            return selection.getSelectedSlot(player);
      }

      public void limitSelectedSlot(int slot) {
            selection.limit(slot, getItemStacks().size());
      }

      @Override
      public void growSelectedSlot(int slot) {
            selection.grow(slot);
      }

      public int stepScrollTo(int selection, int scrolled) {
            if (scrolled == 0)
                  return selection;

            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            int availableSlots = stacks == null || stacks.isEmpty() ? 0 : stacks.size() - 1;
            if (availableSlots == 0)
                  return 0;

            int next = selection - scrolled;
            int direction = scrolled > 0 ? 1 : -1;

            while (next != selection) {
                  while (next < 0)
                        next += stacks.size();
                  while (next > availableSlots)
                        next -= stacks.size();

                  ItemStack stack = stacks.get(next);
                  if (isAcceptableSelection(stack))
                        break;

                  if (next == selection)
                        break;

                  next -= direction;
            }

            return next;
      }

      protected boolean isAcceptableSelection(ItemStack stack) {
            return true;
      }
}
