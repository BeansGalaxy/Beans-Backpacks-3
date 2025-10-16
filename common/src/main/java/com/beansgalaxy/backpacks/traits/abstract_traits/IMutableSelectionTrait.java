package com.beansgalaxy.backpacks.traits.abstract_traits;

import com.beansgalaxy.backpacks.components.SlotSelection;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMutableSelectionTrait {
      
      SlotSelection selection();
      
      List<ItemStack> getItemStacks();
      
      default int stepScrollTo(int selection, int scrolled) {
            if (scrolled == 0)
                  return selection;
            
            List<ItemStack> stacks = getItemStacks();
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
      
      default boolean isAcceptableSelection(ItemStack stack) {
            return true;
      }
}
