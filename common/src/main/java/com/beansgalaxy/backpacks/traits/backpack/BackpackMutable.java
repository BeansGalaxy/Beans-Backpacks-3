package com.beansgalaxy.backpacks.traits.backpack;

import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BackpackMutable extends MutableBundleLike<BackpackTraits> {
      public BackpackMutable(BackpackTraits traits, ComponentHolder holder) {
            super(traits, holder);
      }

      public void pickItem(int index, int amount, SlotAccess access) {
            List<ItemStack> stacks = getItemStacks();

            ItemStack stack;
            if (amount == -1) {
                  stack = stacks.remove(index);
            }
            else {
                  ItemStack item = stacks.get(index);
                  stack = item.copyWithCount(amount);
                  item.shrink(amount);
            }

            access.set(stack);
            push();
      }

      public void pickBlock(Player player, int index, int amount, int freeSlot) {
            List<ItemStack> stacks = getItemStacks();

            ItemStack stack;
            if (amount == -1) {
                  stack = stacks.remove(index);
            }
            else {
                  ItemStack item = stacks.get(index);
                  stack = item.copyWithCount(amount);
                  item.shrink(amount);
            }

            Inventory inventory = player.getInventory();
            int selected = inventory.selected;
            ItemStack inHand = inventory.items.get(selected);
            if (!inHand.isEmpty()) {
                  if (freeSlot < 9)
                        inventory.selected = freeSlot;
                  else
                        inventory.items.set(freeSlot, inHand);
            }

            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            push();
      }
}
