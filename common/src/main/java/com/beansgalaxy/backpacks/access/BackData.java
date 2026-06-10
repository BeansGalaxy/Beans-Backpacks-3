package com.beansgalaxy.backpacks.access;

import com.beansgalaxy.backpacks.container.UtilityContainer;
import com.beansgalaxy.backpacks.util.PlaceProgress;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface BackData {
      ItemStack getBackEquipped();
      
      static BackData get(Player player) {
            return (BackData) player.getInventory();
      }

      boolean isActionKeyDown();

      void setActionKey(boolean actionKeyIsDown);

      boolean isMenuKeyDown();

      void setMenuKey(boolean menuKeyIsDown);
      
      void setTinySlot(int tinySlot);

      int getTinySlot();

      UtilityContainer getUtility();

      PlaceProgress getPlaceProgress();
}
