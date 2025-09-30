package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.data.HopperTraitContainer;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BundleHopper extends HopperTraitContainer<MutableBundleLike<? extends BundleLikeTraits>> {

      public BundleHopper(BackpackEntity backpack, BundleLikeTraits traits) {
            super(backpack, traits.mutable(backpack));
      }

      @Override
      public int getContainerSize() {
            return mutable.getItemStacks().size();
      }

      @Override
      public ItemStack getItem(int i) {
            if (i >= getContainerSize())
                  return ItemStack.EMPTY;

            List<ItemStack> stacks = mutable.getItemStacks();
            return stacks.get(i);
      }

      @Override
      public ItemStack removeItem(int i, int amount) {
            if (i >= getContainerSize() || amount == 0)
                  return ItemStack.EMPTY;

            ItemStack stack = getItem(i);
            ItemStack itemStack = stack.copyWithCount(amount);
            stack.shrink(amount);
            if (stack.isEmpty())
                  mutable.getItemStacks().remove(i);

            setChanged();
            return itemStack;
      }

      @Override
      public ItemStack removeItemNoUpdate(int i) {
            if (i >= getContainerSize())
                  return ItemStack.EMPTY;

            ItemStack removed = mutable.getItemStacks().remove(i);
            setChanged();
            return removed;
      }

      @Override
      public void setItem(int i, @NotNull ItemStack itemStack) {
            ItemStack stack = mutable.addItem(itemStack, i);
            if (stack != null)
                  setChanged();
      }

      @Override
      public void clearContent() {

      }
}
