package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;

public class MutableChestLike<T extends ChestLikeTraits> extends MutableBundleLike<T> {

      public MutableChestLike(T traits, ComponentHolder holder) {
            super(traits, holder);
      }

      public List<ItemStack> getItemStacks() {
            return stacks.get();
      }

      @Override
      public ModSound sound() {
            return traits.sound();
      }

      public void push() {
            stacks.push();
            holder.setChanged();
      }

      @Override
      public boolean isEmpty() {
            return stacks.isEmpty();
      }

      @Override
      public ItemStack removeItem(int slot) {
            if (slot < 0)
                  return ItemStack.EMPTY;

            ItemStack returned = ItemStack.EMPTY;
            List<ItemStack> stacks = getItemStacks();
            if (stacks.size() > slot) {
                  ItemStack stack = stacks.get(slot);
                  int maxCount = stack.getMaxStackSize();
                  if (stack.getCount() > maxCount) {
                        stack.shrink(maxCount);
                        returned = stack.copyWithCount(maxCount);
                  } else {
                        returned = stacks.remove(slot);
                        limitSelectedSlot(slot);
                  }
            }
            return returned;
      }

      @Override
      public void moveItemsTo(MutableItemStorage to, Player player, boolean fullStack) {
            int selectedSlot = getSelectedSlot(player);
            ItemStack stack = getItemStacks().get(selectedSlot);
            int toAdd = to.getMaxAmountToAdd(stack);
            if (toAdd > 0) {
                  ItemStack moved;
                  if (!fullStack) {
                        moved = stack.copyWithCount(1);
                        stack.shrink(1);
                  }
                  else {
                        int count = stack.getCount();
                        if (count < toAdd) {
                              moved = stack.copyWithCount(count);
                              stack.shrink(count);
                        }
                        else moved = removeItem(selectedSlot);
                  }

                  to.addItem(moved);
                  this.push();
                  to.push();

                  sound().at(player, ModSound.Type.REMOVE);
            }
      }

      @Override
      public ItemStack addItem(ItemStack inserted) {
            if (!traits.canItemFit(holder, inserted))
                  return null;

            int spaceLeft = this.getMaxAmountToAdd(inserted);
            int toInsert = Math.min(inserted.getCount(), spaceLeft);
            if (toInsert == 0)
                  return null;

            List<ItemStack> stacks = getItemStacks();
            int i = stacks.size();
            while (i > 0) {
                  i--;

                  ItemStack stored = stacks.get(i);
                  if (inserted.isEmpty() || toInsert < 1)
                        return ItemStack.EMPTY;

                  if (ItemStack.isSameItemSameComponents(stored, inserted)) {
                        int count = Math.min(toInsert, stored.getMaxStackSize() - stored.getCount());
                        stored.grow(count);
                        inserted.shrink(count);
                        toInsert -= count;
                  }
            }

            if (!inserted.isEmpty()) {
                  ItemStack split = inserted.split(toInsert);
                  getItemStacks().addFirst(split);
                  growSelectedSlot(0);
            }

            return inserted;
      }

      public ItemStack addItem(ItemStack inserted, int slot) {
            if (!traits.canItemFit(holder, inserted))
                  return null;

            int spaceLeft = this.getMaxAmountToAdd(inserted);
            int toInsert = Math.min(inserted.getCount(), spaceLeft);
            if (toInsert == 0)
                  return null;

            List<ItemStack> stacks = getItemStacks();
            int i = stacks.size();
            while (i > 0) {
                  i--;

                  ItemStack stored = stacks.get(i);
                  if (inserted.isEmpty() || toInsert < 1)
                        return ItemStack.EMPTY;

                  if (ItemStack.isSameItemSameComponents(stored, inserted)) {
                        int totalCount = stored.getCount() + toInsert;
                        int maxStackSize = stored.getMaxStackSize();
                        if (totalCount > maxStackSize) {
                              int count = totalCount - maxStackSize;
                              ItemStack removed = stored.copyWithCount(count);
                              stored.setCount(maxStackSize);
                              int selectedSlot = Math.min(slot, stacks.size());
                              stacks.add(selectedSlot, removed);
                              inserted.shrink(toInsert);
                              growSelectedSlot(selectedSlot);
                        }
                        else {
                              ItemStack removed = stacks.remove(i);
                              int size = stacks.size();
                              stacks.add(Math.min(slot, size), removed);
                              
                              removed.grow(toInsert);
                              inserted.shrink(toInsert);
                        }
                  }
            }

            if (!inserted.isEmpty()) {
                  int selectedSlot = Math.min(slot, getItemStacks().size());
                  ItemStack split = inserted.split(toInsert);
                  getItemStacks().add(selectedSlot, split);
                  growSelectedSlot(selectedSlot);
            }

            return inserted;
      }

      @Override
      public int getMaxAmountToAdd(ItemStack inserted) {
            List<ItemStack> stacks = getItemStacks();
            int remainingStacks = traits.size() - stacks.size();
            if (remainingStacks > 0) {
                  return inserted.getMaxStackSize() * remainingStacks;
            }
            
            int toAdd = 0;
            for (ItemStack stored : stacks) {
                  if (ItemStack.isSameItemSameComponents(stored, inserted))
                        toAdd += inserted.getMaxStackSize() - inserted.getCount();
            }
            
            return toAdd;
      }

      public ItemStack removeItem(ItemStack other, Player player) {
            return removeItem(0);
      }

      public int getSelectedSlot(Player player) {
            return 0;
      }

      public void growSelectedSlot(int slot) {

      }

      @Override
      public Fraction fullness() {
            List<ItemStack> stacks = this.stacks.get();
            if (stacks == null) {
                  return Fraction.ZERO;
            }

            return Fraction.getFraction(stacks.size(), traits.size());
      }
      
}
