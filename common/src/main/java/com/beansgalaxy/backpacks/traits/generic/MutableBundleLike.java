package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;

public class MutableBundleLike<T extends BundleLikeTraits> implements MutableItemStorage {
      public final ITraitData<List<ItemStack>> stacks;
      protected final ComponentHolder holder;
      protected final T traits;

      public MutableBundleLike(T traits, ComponentHolder holder) {
            this.holder = holder;
            this.traits = traits;
            stacks = ITraitData.ITEM_STACKS.get(holder);
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
                              int min = Math.min(count, toAdd);
                              moved = stack.copyWithCount(min);
                              stack.shrink(min);
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
                        stored.grow(toInsert);
                        inserted.shrink(toInsert);
                        return ItemStack.EMPTY;
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
                        ItemStack removed = stacks.remove(i);
                        int size = stacks.size();
                        stacks.add(Math.min(slot, size), removed);

                        removed.grow(toInsert);
                        inserted.shrink(toInsert);
                        return ItemStack.EMPTY;
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
      public int getMaxAmountToAdd(ItemStack stack) {
            Fraction size = Fraction.getFraction(traits.size(), 1);
            Fraction weight = Traits.getWeight(getItemStacks());
            Fraction weightLeft = size.subtract(weight);
            return Math.max(weightLeft.divideBy(Traits.getItemWeight(stack)).intValue(), 0);
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

            return Traits.getWeight(stacks, traits.size());
      }

      public int toAdd(ItemStack carried) {
            if (!traits.canItemFit(holder, carried))
                  return 0;

            int spaceLeft = getMaxAmountToAdd(carried);
            return Math.min(carried.getCount(), spaceLeft);
      }
}
