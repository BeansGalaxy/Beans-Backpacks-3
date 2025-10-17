package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class SlotSelectorData {
      private final ComponentHolder holder;
      private final Supplier<List<ItemStack>> getItemStacks;
      private SlotSelection value;
      private boolean isDirty;
      
      public SlotSelectorData(ComponentHolder holder, Supplier<List<ItemStack>> getItemStacks) {
            this.holder = holder;
            this.value = holder.get(ITraitData.SLOT_SELECTION);
            this.getItemStacks = getItemStacks;
      }
      
      public int getSelectedSlot(Player player) {
            return get().get(player);
      }
      
      public void growSelectedSlot(int slot) {
            markDirty();
            get().grow(slot);
      }
      
      public void limitSelectedSlot(int index, int size) {
            markDirty();
            get().limit(index, size);
      }
      
      public void setSelectedSlot(Player player, int slot) {
            markDirty();
            get().set(player, slot);
      }
      
      public boolean isEmpty(SlotSelection data) {
            List<ItemStack> stacks = getItemStacks.get();
            return stacks == null || stacks.isEmpty();
      }
      
      public SlotSelection get() {
            if (value == null) {
                  markDirty();
                  value = new SlotSelection();
            }
            return value;
      }
      
      private void markDirty() {
            isDirty = true;
      }
      
      public void push() {
            List<ItemStack> stacks = getItemStacks.get();
            if (stacks == null || stacks.size() <= 1) {
                  holder.remove(ITraitData.SLOT_SELECTION);
            }
            else if (isDirty) {
                  int size = stacks.size();
                  if (value != null)
                        value.ceil(size - 1);
                  
                  holder.set(ITraitData.SLOT_SELECTION, value);
            }
      }
}
