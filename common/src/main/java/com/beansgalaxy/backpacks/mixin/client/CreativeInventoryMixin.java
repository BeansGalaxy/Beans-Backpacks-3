package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.container.*;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

      private static final ResourceLocation LARGE_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "slots/creative_large");
      private static final ResourceLocation SMALL_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "slots/creative_small");

      @Override
      protected void renderSlot(GuiGraphics gui, Slot slot) {
            if (slot instanceof BackSlot || slot instanceof ShorthandSlot.WeaponSlot) {
                  gui.blitSprite(LARGE_SLOT, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
            }
            else if (slot instanceof ShorthandSlot.ToolSlot || slot instanceof UtilitySlot) {
                  gui.blitSprite(SMALL_SLOT, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
            }
            super.renderSlot(gui, slot);
      }

      @Inject(method = "renderBg", at = @At("TAIL"))
      private void backpacks_renderBg(GuiGraphics gui, float pPartialTick, int pMouseX, int pMouseY, CallbackInfo ci) {
            if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
                  gui.blitSprite(LARGE_SLOT, leftPos + 127 - 1, topPos + 20 - 1, 18, 18);
                  UtilityContainer utilities = UtilityContainer.get(minecraft.player);
                  for (byte b = 0; b < utilities.size; b++) {
                        gui.blitSprite(SMALL_SLOT, leftPos - 58 - 1, topPos + (b * 18) - 1, 18, 18);
                  }

                  Shorthand shorthand = Shorthand.get(minecraft.player);
                  int weaponsSize = shorthand.weapons.getContainerSize();
                  for (int i = 0; i < weaponsSize; i++) {
                        gui.blitSprite(LARGE_SLOT, leftPos - 22 - 1, topPos + (i * 18) - 1, 18, 18);
                  }

                  int toolsSize = shorthand.tools.getContainerSize();
                  for (int i = 0; i < toolsSize; i++) {
                        gui.blitSprite(SMALL_SLOT, leftPos - 40 - 1, topPos + (i * 18) - 1, 18, 18);
                  }
            }
      }

      @Inject(method = "selectTab", at = @At(value = "FIELD", shift = At.Shift.BEFORE, ordinal = 0,
                  target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;destroyItemSlot:Lnet/minecraft/world/inventory/Slot;"))
      private void addBackSlot(CreativeModeTab pTab, CallbackInfo ci) {
            AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
            NonNullList<Slot> slots = abstractcontainermenu.slots;
            for (int i = 0; i < slots.size(); i++) {
                  Slot slot = slots.get(i);
                  if (slot instanceof BackSlot backSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(backSlot, backSlot.index, 127, 20);
                        backpacks_setOrAdd(i, wrapped);
                        continue;
                  }
                  if (slot instanceof UtilitySlot utilSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(utilSlot, utilSlot.index, -58, utilSlot.getContainerSlot() * 18);
                        backpacks_setOrAdd(i, wrapped);
                  }
                  if (slot instanceof ShorthandSlot.WeaponSlot shortSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(shortSlot, shortSlot.index, -22, shortSlot.getContainerSlot() * 18);
                        backpacks_setOrAdd(i, wrapped);
                        continue;
                  }
                  if (slot instanceof ShorthandSlot.ToolSlot toolSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(toolSlot, toolSlot.index, -40, toolSlot.getContainerSlot() * 18);
                        backpacks_setOrAdd(i, wrapped);
                        continue;
                  }
            }
      }

      @Unique
      private void backpacks_setOrAdd(int i, CreativeModeInventoryScreen.SlotWrapper wrapped) {
            if (i < menu.slots.size())
                  menu.slots.set(i, wrapped);
            else menu.slots.add(wrapped);
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
