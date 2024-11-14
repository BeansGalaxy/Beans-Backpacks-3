package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.MutableItemStorage;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.apache.commons.lang3.math.Fraction;

public class ChestMutable implements MutableItemStorage {
      private final ChestTraits traits;
      private final NonNullList<ItemStack> stacks;
      private final PatchedComponentHolder holder;

      public ChestMutable(ChestTraits traits, PatchedComponentHolder holder) {
            this.traits = traits;
            this.holder = holder;
            ItemContainerContents contents = holder.get(ITraitData.CHEST);
            this.stacks = NonNullList.withSize(traits.size(), ItemStack.EMPTY);
            if (contents != null)
                  contents.copyInto(stacks);
      }

      public NonNullList<ItemStack> getItemStacks() {
            return stacks;
      }

      @Override
      public ItemStack addItem(ItemStack inserted, Player player) {
            if (!traits.canItemFit(holder, inserted) || inserted.isEmpty())
                  return null;

            NonNullList<ItemStack> items = getItemStacks();

            int i = 0;
            int emptySlot = -1;
            while (!inserted.isEmpty() && i < items.size()) {
                  ItemStack stack = items.get(i);
                  if (emptySlot == -1 && stack.isEmpty())
                        emptySlot = i;

                  if (ItemStack.isSameItemSameComponents(inserted, stack)) {
                        int toAdd = Math.min(inserted.getCount(), stack.getMaxStackSize() - stack.getCount());
                        stack.grow(toAdd);
                        inserted.shrink(toAdd);
                  }
                  i++;
            }

            if (emptySlot != -1 && !inserted.isEmpty()) {
                  items.set(emptySlot, inserted.copy());
                  inserted.setCount(0);
            }

            return ItemStack.EMPTY;
      }

      @Override
      public ItemStack removeItem(int slot) {
            NonNullList<ItemStack> items = getItemStacks();
            ItemStack itemstack = items.get(slot);
            if (itemstack.isEmpty()) {
                  return ItemStack.EMPTY;
            } else {
                  items.set(slot, ItemStack.EMPTY);
                  return itemstack;
            }
      }

      @Override
      public void push() {
            boolean isEmpty = stacks.stream().allMatch(ItemStack::isEmpty);
            if (isEmpty)
                  holder.remove(ITraitData.CHEST);
            else
                  holder.set(ITraitData.CHEST, ItemContainerContents.fromItems(stacks));
      }

      @Override
      public ModSound sound() {
            return traits.sound();
      }

      @Override
      public Fraction fullness() {
            if (stacks.isEmpty())
                  return Fraction.ZERO;

            int fullSlots = stacks.stream().mapToInt(stack -> stack.isEmpty() ? 0 : 1).sum();
            return Fraction.getFraction(fullSlots, traits.size());
      }

      @Override
      public int getMaxAmountToAdd(ItemStack stack) {
            return getItemStacks().stream().mapToInt(stacks ->
                        !stacks.isEmpty() && ItemStack.isSameItemSameComponents(stack, stacks)
                                    ? stacks.getMaxStackSize() - stacks.getCount()
                                    : 0
            ).sum();
      }

      @Override
      public boolean isEmpty() {
            return stacks.isEmpty();
      }

      public ItemStack getItem(int pIndex) {
            NonNullList<ItemStack> items = getItemStacks();
            return pIndex >= 0 && pIndex < items.size() ? items.get(pIndex) : ItemStack.EMPTY;
      }

      public void setItem(int index, ItemStack stack) {
            getItemStacks().set(index, stack);
      }

      @Override
      public InteractionResult interact(BackpackEntity backpack, Player player, InteractionHand hand) {
            return MutableItemStorage.super.interact(backpack, player, hand);
      }
}
