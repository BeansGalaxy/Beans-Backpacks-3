package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.access.TraitMenuAccessor;
import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.abstract_traits.DraggingContainer;
import com.beansgalaxy.backpacks.traits.abstract_traits.IDraggingTrait;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractScreenMixin<T extends AbstractContainerMenu> extends Screen implements TraitMenuAccessor {

      @Shadow protected Slot hoveredSlot;

      @Shadow @Final protected T menu;

      @Shadow @Nullable protected abstract Slot findSlot(double mouseX, double mouseY);

      @Shadow protected boolean isQuickCrafting;

      @Shadow protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);

      @Shadow private boolean skipNextRelease;

      @Shadow @Nullable private Slot lastClickSlot;

      @Shadow protected int leftPos;

      @Shadow protected int topPos;

      @Shadow private int lastClickButton;
      
      @Shadow protected int imageHeight;
      
      @Shadow protected int imageWidth;
      
      @Shadow public abstract T getMenu();
      
      protected AbstractScreenMixin(Component pTitle) {
            super(pTitle);
      }
      
      @Unique private List<TraitMenu<?>> traitMenus = new ArrayList<>();
      
      /*
      TODO:
       ~The literal slot that holds the bundle always lights up whether the menu is focused or not;~
       ~Make that slot light up once the menu is focused;~
       ~fix the Z ordering;~
       ~When focused, the menu should jump to the front of the list;~
       ~Add somewhere to click and drag the menu (dedicated bar / click and drag on items or anywhere);~
       Titles are incorrectly ordered; moving the inject after and increasing the z offset will solve;
       Change the focused menu border from the standard purple gradient to a orange/gold which matches the attribute/fullness bar
       ~?Add a sticky start to ignore SMALL accidental clicks~
       New quicker/intuitive way to close a menu; ESC key closes all menus and cancels closing inv; ?double click empty slot;
       */

      @Override
      public void clickTraitMenu(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
            if (!traitMenus.isEmpty()) {
                  Iterator<TraitMenu<?>> iterator = traitMenus.iterator();
                  while (iterator.hasNext() && !cir.isCancelled()) {
                        TraitMenu<?> traitMenu = iterator.next();
                        
                        if (traitMenu.isHoveringSlot((int) pMouseX, (int) pMouseY)) { // Try and close the menu
                              long i = Util.getMillis();
                              boolean doubleClick = i - traitMenu.timeOpened < 250L && this.lastClickButton == pButton;
                              if (doubleClick) {
                                    cir.setReturnValue(true);
                                    return;
                              }
                              
                              iterator.remove();
                              if (pButton == 1) {
                                    skipNextRelease = true;
                                    cir.setReturnValue(true);
                                    return;
                              }
                        }
                        else { // Try click the menu
                              traitMenu.mouseClicked(pMouseX, pMouseY, pButton, cir);
                              if (cir.isCancelled()) {
                                    iterator.remove();
                                    traitMenus.addFirst(traitMenu);
                                    skipNextRelease = true;
                                    return;
                              }
                        }
                  }
            }
      }
      
      @Unique
      private boolean shouldMakeNewMenu(int mButton) {
            if (mButton == 1)
                  return true;
            
            if (mButton == 0) {
                  if (BackData.get(minecraft.player).isMenuKeyDown())
                        return true;
                  
                  if (hoveredSlot instanceof EquipmentSlotAccess)
                        return !hoveredSlot.mayPickup(minecraft.player) || !hoveredSlot.mayPlace(menu.getCarried());
            }
            
            return false;
      }
      
      @Inject(method = "mouseClicked", cancellable = true, at = @At("HEAD"))
      private void mouseClicked(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot != null) {
                  if (shouldMakeNewMenu(pButton)) {
                        TraitMenu<?> menu = TraitMenu.create(minecraft, leftPos, topPos, imageHeight, imageWidth, hoveredSlot);
                        if (menu != null)
                              traitMenus.addFirst(menu);
                  }
                  else if (menu.getCarried().isEmpty() && hoveredSlot.hasItem() && minecraft.options.keyPickItem.matchesMouse(pButton)) {
                        BackpackTraits.runIfEquipped(minecraft.player, ((traits, slot) -> {
                              Player player = minecraft.player;
                              SlotAccess access = SlotAccess.of(menu::getCarried, menu::setCarried);
                              return traits.pickItemClient(player, slot, access, menu, hoveredSlot.getItem(), cir);
                        }));
                  }
            }
      }

      @Inject(method = "init", at = @At("TAIL"))
      private void init(CallbackInfo ci) {
            traitMenus = new ArrayList<>();
      }
      
      @Override
      public boolean shouldCloseOnEsc() {
            if (traitMenus.isEmpty())
                  return super.shouldCloseOnEsc();
            
            traitMenus = new ArrayList<>();
            return false;
      }
      
      @Inject(method = "keyPressed", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(II)Z"))
      private void keyPressed(int pKeyCode, int pScanCode, int pModifiers, CallbackInfoReturnable<Boolean> cir) {
            boolean dropButtonWasPressed = minecraft.options.keyDrop.matches(pKeyCode, pScanCode);
            
            if (dropButtonWasPressed) {
                  Iterator<TraitMenu<?>> iterator = traitMenus.iterator();
                  while (iterator.hasNext() && !cir.isCancelled()) {
                        TraitMenu<?> menu = iterator.next();
                        menu.dropHoveredItem(cir);
                  }
            }
      }

      @Inject(method = "isHovering(IIIIDD)Z", cancellable = true, at = @At("HEAD"))
      private void isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY, CallbackInfoReturnable<Boolean> cir) {
            for (TraitMenu<?> traitMenu : traitMenus) {
                  if (traitMenu.isHovering((int) pMouseX, (int) pMouseY)) {
                        cir.setReturnValue(false);
                        return;
                  }
            }
      }

      @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
      protected void renderTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
            TraitMenu<?> focusedMenu = null;
            for (TraitMenu<?> traitMenu : traitMenus) {
                  if (focusedMenu == null && traitMenu.isHovering(pMouseX, pMouseY)) {
                        focusedMenu = traitMenu;
                        focusedMenu.setFocus(true);
                  }
                  else traitMenu.setFocus(false);
            }
            
            if (focusedMenu != null) {
                  traitMenus.remove(focusedMenu);
                  traitMenus.addFirst(focusedMenu);
            }
            
            PoseStack pose = pGuiGraphics.pose();
            int i = traitMenus.size();
            while (i > 0) {
                  i--;
                  
                  TraitMenu<?> traitMenu = traitMenus.get(i);
                  traitMenu.render((AbstractContainerScreen<?>) (Object) this, pGuiGraphics, pMouseX, pMouseY);
            }
            
            
            if (hoveredSlot != null) {
                  ItemStack stack = hoveredSlot.getItem();
                  if (!stack.isEmpty()) {
                        Optional<BundleLikeTraits> optional = BundleLikeTraits.get(ComponentHolder.of(stack));
                        optional.ifPresent(traits ->
                              CommonClient.renderInfoTooltip(pGuiGraphics, pMouseX - leftPos, pMouseY - topPos, hoveredSlot, traits)
                        );
                  }
            }
            
            pose.translate(0, 0, 600);
      }

      @Override
      public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
            if (hoveredSlot != null) {
                  ItemStack stack = hoveredSlot.getItem();
                  int containerId = menu.containerId;
                  ClientLevel level = minecraft.level;

                  int scrolled = Mth.floor(pScrollY + 0.5);
                  if (CommonClient.scrollTraits(minecraft.player, stack, level, containerId, scrolled, hoveredSlot))
                        return true;
            }
            return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
      }

      private final DraggingContainer drag = new DraggingContainer() {
            @Override public void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
                  AbstractScreenMixin.this.slotClicked(slot, slotId, mouseButton, type);
            }
      };

      @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
      public void backpackDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY, CallbackInfoReturnable<Boolean> cir) {
            if (!traitMenus.isEmpty()) {
                  TraitMenu<?> traitMenu = traitMenus.getFirst();
                  if (traitMenu.isFocused()) {
                        traitMenu.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY, cir);
                        if (cir.isCancelled())
                              return;
                  }
            }
            
            ItemStack backpack = menu.getCarried();
            Slot slot = this.findSlot(pMouseX, pMouseY);
            if (backpack.isEmpty())
                  return;

            if (slot != lastClickSlot && slot != drag.firstSlot) {
                  IDraggingTrait.runIfPresent(backpack, minecraft.level, ((trait, holder) -> {
                        beans_Backpacks_3$dragTrait(trait, pButton, slot, cir, holder);
                  }));
            }
      }

      @Unique
      private void beans_Backpacks_3$dragTrait(IDraggingTrait traits, int pButton, Slot slot, CallbackInfoReturnable<Boolean> cir, ComponentHolder holder) {
            isQuickCrafting = false;
            skipNextRelease = true;
            if (drag.allSlots.isEmpty()) {
                  drag.isPickup = pButton == 0;

                  if (drag.firstSlot != null)
                        traits.clickSlot(drag, minecraft.player, holder);
                  else if (lastClickSlot != null) {
                        drag.firstSlot = lastClickSlot;
                        traits.clickSlot(drag, minecraft.player, holder);
                  }
            }
            else if (drag.firstSlot != null)
                  traits.clickSlot(drag, minecraft.player, holder);

            drag.firstSlot = slot;
            cir.setReturnValue(true);
      }

      @Inject(method = "mouseReleased", at = @At("HEAD"))
      public void backpackReleased(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
            if (!drag.allSlots.isEmpty()) {
                  if (drag.firstSlot != null) {
                        ItemStack backpack = menu.getCarried();

                        IDraggingTrait.runIfPresent(backpack, minecraft.level, (trait, holder) -> {
                              trait.clickSlot(drag, minecraft.player, ComponentHolder.of(backpack));
                        });

                        drag.firstSlot = null;
                  }
                  drag.allSlots.clear();
            }
      }

      @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
      public void renderBackpackDraggedSlot(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
            ItemStack pair = drag.allSlots.get(pSlot);
            if (pair != null) {
                  int i = pSlot.x;
                  int j = pSlot.y;
                  if (drag.isPickup) {
                        pGuiGraphics.renderFakeItem(pair, i, j);

                        if (pSlot.getItem().getCount() == 1) {
                              PoseStack pose = pGuiGraphics.pose();
                              pose.pushPose();
                              pose.translate(0, 0, 200);
                              String pText = String.valueOf(1);
                              pGuiGraphics.drawString(font, pText, i + 19 - 2 - font.width(pText), j + 6 + 3, 16777215, true);
                              pose.popPose();
                        }
                  }

                  pGuiGraphics.fill(i, j, i + 16, j + 16, drag.isPickup ? 200 : 0,-2130706433);
            }
      }

      @Inject(method = "slotClicked", cancellable = true, at = @At("HEAD"))
      private void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType, CallbackInfo ci) {
            if (pSlot == null || pType != ClickType.PICKUP_ALL)
                  return;

            Optional<GenericTraits> traits = Traits.get(menu.getCarried());
            if (traits.isEmpty())
                  return;

            this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, pSlot.index, pMouseButton, ClickType.PICKUP, this.minecraft.player);

            TraitMenu<?> menu = TraitMenu.create(minecraft, leftPos, topPos, imageHeight, imageWidth, hoveredSlot);
            
            if (menu != null)
                  traitMenus.addFirst(menu);

            ci.cancel();
      }
}
