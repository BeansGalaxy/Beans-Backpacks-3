package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.screen.BackSlot;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import com.beansgalaxy.backpacks.shorthand.ShorthandSlot;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
      @Shadow private static CreativeModeTab selectedTab;

      public CreativeInventoryMixin(CreativeModeInventoryScreen.ItemPickerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
            super(pMenu, pPlayerInventory, pTitle);
      }

      @Inject(method = "hasClickedOutside", cancellable = true, at = @At("HEAD"))
      private void hasClickedShortSlot(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot != null)
                  cir.setReturnValue(false);
      }

      @Inject(method = "selectTab", at = @At(value = "FIELD", shift = At.Shift.BEFORE, ordinal = 0,
                  target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;destroyItemSlot:Lnet/minecraft/world/inventory/Slot;"))
      private void addBackSlot(CreativeModeTab pTab, CallbackInfo ci) {
            AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
            for (Slot slot : abstractcontainermenu.slots) {
                  if (slot instanceof BackSlot backSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(backSlot, 41, 127, 20);
                        menu.slots.set(backSlot.index, wrapped);
                  }
                  if (slot instanceof ShorthandSlot.WeaponSlot shortSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(shortSlot, 41, -22, shortSlot.getContainerSlot() * 18);
                        menu.slots.set(shortSlot.index, wrapped);
                  }
                  if (slot instanceof ShorthandSlot.ToolSlot shortSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(shortSlot, 41, -40, shortSlot.getContainerSlot() * 18);
                        menu.slots.set(shortSlot.index, wrapped);
                  }
            }
      }

      @Inject(method = "slotClicked", at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/inventory/InventoryMenu;getItems()Lnet/minecraft/core/NonNullList;"))
      private void backpackQuickMoveDestroyItemSlot(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType, CallbackInfo ci) {
            Shorthand.get(minecraft.player).clearContent();
      }

      @ModifyExpressionValue(method = "slotClicked", at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/inventory/Slot;mayPickup(Lnet/minecraft/world/entity/player/Player;)Z"))
      private boolean backpacks_passMayPickup(boolean original) {
            if (hoveredSlot == null)
                  return original;

            ItemStack backpack = hoveredSlot.getItem();
            if (Traits.get(backpack).isEmpty())
                  return original;

            return true;
      }

      @Inject(method = "mouseScrolled", cancellable = true, at = @At(value = "HEAD"))
      private void backpacks_creativeMouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY, CallbackInfoReturnable<Boolean> cir) {
            if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY && hoveredSlot != null) {
                  ItemStack stack = hoveredSlot.getItem();
                  int containerId = menu.containerId;
                  ClientLevel level = minecraft.level;
                  if (
                        ItemStorageTraits.testIfPresent(stack, traits ->
                                    traits.client().mouseScrolled(traits, PatchedComponentHolder.of(stack), level, hoveredSlot, containerId, Mth.floor(pScrollY + 0.5)))
                        ||
                        EnderTraits.get(stack).flatMap(enderTraits -> enderTraits.getTrait().map(traits -> {
                              if (traits instanceof ItemStorageTraits storageTraits)
                                    return traits.client().mouseScrolled(storageTraits, enderTraits, level, hoveredSlot, containerId, Mth.floor(pScrollY + 0.5));
                              return false;
                        })).orElse(false)
                  ) {
                        cir.setReturnValue(true);
                  }
            }
      }
}
