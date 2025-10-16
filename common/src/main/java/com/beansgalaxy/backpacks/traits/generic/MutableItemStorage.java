package com.beansgalaxy.backpacks.traits.generic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface MutableItemStorage extends MutableTraits {

      ItemStack removeItem(int i);

      int getMaxAmountToAdd(ItemStack stack);

      ItemStack addItem(ItemStack inserted);

      void moveItemsTo(MutableItemStorage to, Player player, boolean fullStack);
      
}
