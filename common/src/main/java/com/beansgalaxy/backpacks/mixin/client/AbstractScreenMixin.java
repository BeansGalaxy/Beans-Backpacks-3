package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.access.TraitMenuAccessor;
import com.beansgalaxy.backpacks.data.config.ClientConfig;
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
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
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

      @Shadow protected boolean isQuickCrafting;

      @Shadow protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);

      @Shadow private boolean skipNextRelease;

      @Shadow @Nullable private Slot lastClickSlot;

      @Shadow protected int leftPos;

      @Shadow protected int topPos;
      
      @Shadow protected int imageHeight;
      
      @Shadow protected int imageWidth;
      
      @Shadow public abstract T getMenu();
      
      @Shadow @Final private List<ItemSlotMouseAction> itemSlotMouseActions;
      
      @Shadow @Final private static ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE;
      
      @Shadow @Final private static ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE;
      
      protected AbstractScreenMixin(Component pTitle) {
            super(pTitle);
      }
      
      @Unique private List<TraitMenu<?>> traitMenus = new ArrayList<>();

      @Override
      public void clickTraitMenu(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
            if (!traitMenus.isEmpty()) {
                  Iterator<TraitMenu<?>> iterator = traitMenus.iterator();
                  while (iterator.hasNext() && !cir.isCancelled()) {
                        TraitMenu<?> traitMenu = iterator.next();
                        
                        if (traitMenu.isHoveringSlot((int) pMouseX, (int) pMouseY)) { // Try and close the menu
                              long i = Util.getMillis();
                              boolean doubleClick = i - traitMenu.timeOpened < 250L;
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
                        return minecraft.player.isCreative() || !hoveredSlot.mayPickup(minecraft.player) || !hoveredSlot.mayPlace(menu.getCarried());
            }
            
            return false;
      }
      
      @Inject(method = "mouseClicked", cancellable = true, at = @At("HEAD"))
      private void mouseClicked(MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot != null) {
                  if (shouldMakeNewMenu(event.button())) {
                        TraitMenu<?> menu = TraitMenu.create(minecraft, leftPos, topPos, imageHeight, imageWidth, hoveredSlot);
                        if (menu != null) {
                              traitMenus.addFirst(menu);
                              ClientConfig config = CommonClass.CLIENT_CONFIG;
                              if (!config.hide_bundle_tutorial.get()) {
                                    config.hide_bundle_tutorial.set(true);
                                    config.write();
                              }
                        }
                  }
                  else if (menu.getCarried().isEmpty() && hoveredSlot.hasItem() && minecraft.options.keyPickItem.matchesMouse(event)) {
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
      
      @Inject(method = "keyPressed", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"))
      private void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
            boolean dropButtonWasPressed = minecraft.options.keyDrop.matches(event);
            
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

      @Inject(method = "renderCarriedItem", at =@At("HEAD"))
      protected void renderTooltip(GuiGraphics gui, int pMouseX, int pMouseY, CallbackInfo ci) {
            Matrix3x2fStack pose = gui.pose();
            pose.pushMatrix();
            pose.translate(leftPos, topPos);
            
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
            
            int i = traitMenus.size();
            if (i > 0) {
                  gui.nextStratum();
            }
            
            while (i > 0) {
                  i--;
                  
                  TraitMenu<?> traitMenu = traitMenus.get(i);
                  traitMenu.render((AbstractContainerScreen<?>) (Object) this, gui, pMouseX, pMouseY);
                  gui.nextStratum();
            }
            
            if (hoveredSlot != null) {
                  ItemStack stack = hoveredSlot.getItem();
                  if (!stack.isEmpty() && !CommonClass.CLIENT_CONFIG.hide_bundle_tutorial.get()) {
                        ComponentHolder holder = ComponentHolder.of(stack);
                        Optional<BundleLikeTraits> optional = BundleLikeTraits.get(holder);
                        optional.ifPresent(traits -> {
                              if (traits instanceof BackpackTraits) {
                                    if (hoveredSlot instanceof EquipmentSlotAccess) {}
                                    else return;
                              }
                              
                              CommonClient.renderInfoTooltip(gui, pMouseX - leftPos, pMouseY - topPos, holder);
                        });
                  }
            }
            
            pose.popMatrix();
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
      public void backpackDragged(MouseButtonEvent event, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
            if (!traitMenus.isEmpty()) {
                  TraitMenu<?> traitMenu = traitMenus.getFirst();
                  if (traitMenu.isFocused()) {
                        traitMenu.mouseDragged(event.x(), event.y(), event.button(), mouseX, mouseY, cir);
                        if (cir.isCancelled())
                              return;
                  }
            }
            
            ItemStack backpack = menu.getCarried();
            if (hoveredSlot == null)
                  return;
            
            if (backpack.isEmpty())
                  return;

            if (hoveredSlot != lastClickSlot && hoveredSlot != drag.firstSlot) {
                  IDraggingTrait.runIfPresent(backpack, minecraft.level, ((trait, holder) -> {
                        beans_Backpacks_3$dragTrait(trait, event.button(), hoveredSlot, cir, holder);
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
      public void backpackReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
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

      @Inject(method = "renderSlot", at = @At("TAIL"))
      public void renderBackpackDraggedSlot(GuiGraphics gui, Slot slot, CallbackInfo ci) {
            ItemStack pair = drag.allSlots.get(slot);
            if (pair != null) {
                  int i = slot.x;
                  int j = slot.y;
                  if (drag.isPickup) {
                        gui.renderFakeItem(pair, i, j);

                        if (slot.getItem().getCount() == 1) {
                              gui.nextStratum();
                              String pText = String.valueOf(1);
                              gui.drawString(font, pText, i + 19 - 2 - font.width(pText), j + 6 + 3, 16777215, true);
                        }
                        }
                  
                  if (slot.isHighlightable()) {
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
                  }
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
