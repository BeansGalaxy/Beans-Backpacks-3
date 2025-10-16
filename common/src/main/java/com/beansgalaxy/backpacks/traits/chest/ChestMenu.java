package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleMenu;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ChestMenu<T extends ChestLikeTraits> extends BundleMenu<T> {
      public ChestMenu(Minecraft minecraft, int screenLeft, int screenTop, Slot slot, ComponentHolder holder, T traits) {
            super(minecraft, screenLeft, screenTop, slot, holder, traits);
      }
      
      @Override protected boolean hasSpace() {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks == null)
                  return true;
            
            return stacks.size() < traits.size();
      }
}
