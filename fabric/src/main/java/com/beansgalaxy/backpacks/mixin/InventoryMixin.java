package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.container.UtilityContainer;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements BackData {
      @Shadow @Final private EntityEquipment equipment;
      
      @Override
      public ItemStack getBackEquipped() {
            return equipment.get(EquipmentSlot.BODY);
      }
      
      @Unique private boolean beans_Backpacks_3$actionKeyIsDown = false;
      @Unique private boolean beans_Backpacks_3$menuKeyIsDown = false;
      @Unique private int beans_Backpacks_3$tinySlot = -1;

      @Override
      public boolean isActionKeyDown() {
            return beans_Backpacks_3$actionKeyIsDown;
      }

      @Override
      public void setActionKey(boolean actionKeyIsDown) {
            this.beans_Backpacks_3$actionKeyIsDown = actionKeyIsDown;
      }

      @Override
      public boolean isMenuKeyDown() {
            return beans_Backpacks_3$menuKeyIsDown;
      }

      @Override
      public void setMenuKey(boolean menuKeyIsDown) {
            this.beans_Backpacks_3$menuKeyIsDown = menuKeyIsDown;
      }

      @Override
      public int getTinySlot() {
            return beans_Backpacks_3$tinySlot;
      }

      @Override
      public void setTinySlot(int tinySlot) {
            beans_Backpacks_3$tinySlot = tinySlot;
      }

      @Unique private UtilityContainer utility;

      @Override @Unique
      public UtilityContainer getUtility() {
            if (utility == null)
                  utility = new UtilityContainer(this);
            return utility;
      }
}
