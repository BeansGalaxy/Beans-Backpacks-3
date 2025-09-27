package com.beansgalaxy.backpacks.util;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public abstract class DraggingContainer {
      public boolean isPickup = false;
      public Slot firstSlot = null;
      public final HashMap<Slot, ItemStack> allSlots = new HashMap<>();

      public abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);
}
