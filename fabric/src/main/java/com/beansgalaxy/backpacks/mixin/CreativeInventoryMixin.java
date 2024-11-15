package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.screen.BackSlot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
      public CreativeInventoryMixin(CreativeModeInventoryScreen.ItemPickerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
            super(pMenu, pPlayerInventory, pTitle);
      }

      @Inject(method = "selectTab", at = @At(value = "FIELD", shift = At.Shift.BEFORE, ordinal = 0,
                  target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;destroyItemSlot:Lnet/minecraft/world/inventory/Slot;"))
      private void addBackSlot(CreativeModeTab pTab, CallbackInfo ci) {
            AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
            BackSlot backSlot = null;
            for (Slot slot : abstractcontainermenu.slots) {
                  if (slot instanceof BackSlot)
                        backSlot = (BackSlot) slot;
            }

            if (backSlot == null)
                  return;

            CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(backSlot, 41, 127, 20);
            menu.slots.set(backSlot.index, wrapped);
      }
}
