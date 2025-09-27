package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.util.DraggingContainer;
import com.beansgalaxy.backpacks.traits.abstract_traits.IDraggingTrait;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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

import java.util.*;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractScreenMixin<T extends AbstractContainerMenu> extends Screen {

      @Shadow protected Slot hoveredSlot;

      @Shadow @Final protected T menu;

      @Shadow @Nullable protected abstract Slot findSlot(double mouseX, double mouseY);

      @Shadow protected boolean isQuickCrafting;

      @Shadow protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);

      @Shadow private boolean skipNextRelease;

      @Shadow @Nullable private Slot lastClickSlot;

      @Shadow protected int leftPos;

      @Shadow protected int topPos;

      protected AbstractScreenMixin(Component pTitle) {
            super(pTitle);
      }

      @Unique private TraitMenu<?> traitMenu = null;

      @Inject(method = "mouseClicked", cancellable = true, at = @At("HEAD"))
      private void mouseClicked(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
            if (traitMenu != null) {
                  if (traitMenu.isHoveringSlot((int) pMouseX, (int) pMouseY)) {
                        traitMenu = null;
                        skipNextRelease = true;
                        cir.cancel();
                        return;
                  }
                  else {
                        traitMenu.mouseClicked(pMouseX, pMouseY, pButton, cir);
                        if (cir.isCancelled()) {
                              skipNextRelease = true;
                              return;
                        }
                  }
            }

            if (hoveredSlot != null) {
                  if (pButton == 1 || (pButton == 0 && hoveredSlot instanceof EquipmentSlotAccess && !hoveredSlot.mayPickup(minecraft.player))) {
                        TraitMenu<?> menu = TraitMenu.create(minecraft, leftPos, topPos, hoveredSlot);
                        if (menu != null)
                              traitMenu = menu;
                  }
            }
      }

      @Inject(method = "init", at = @At("TAIL"))
      private void init(CallbackInfo ci) {
            traitMenu = null;
      }

      @Inject(method = "keyPressed", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(II)Z"))
      private void keyPressed(int pKeyCode, int pScanCode, int pModifiers, CallbackInfoReturnable<Boolean> cir) {
            if (traitMenu != null) {
                  traitMenu.keyPressed(pKeyCode, pScanCode, pModifiers, cir);
            }
      }

      @Inject(method = "isHovering(IIIIDD)Z", cancellable = true, at = @At("HEAD"))
      private void isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY, CallbackInfoReturnable<Boolean> cir) {
            if (traitMenu != null && traitMenu.isHovering((int) pMouseX, (int) pMouseY))
                  cir.setReturnValue(false);
      }

      @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
      protected void renderTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
            if (traitMenu != null) {
                  traitMenu.render((AbstractContainerScreen<?>) (Object) this, pGuiGraphics, pMouseX, pMouseY);
            }
      }

      @Override
      public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
            if (hoveredSlot != null) {
                  ItemStack stack = hoveredSlot.getItem();
                  int containerId = menu.containerId;
                  ClientLevel level = minecraft.level;

                  int scrolled = Mth.floor(pScrollY + 0.5);
                  if (CommonClient.scrollTraits(stack, level, containerId, scrolled, hoveredSlot))
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
      private void beans_Backpacks_3$dragTrait(IDraggingTrait traits, int pButton, Slot slot, CallbackInfoReturnable<Boolean> cir, PatchedComponentHolder holder) {
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
                              trait.clickSlot(drag, minecraft.player, PatchedComponentHolder.of(backpack));
                        });

                        drag.firstSlot = null;
                  }
                  drag.allSlots.clear();
            }
      }

      @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
      public void renderBackpackDraggedSlot(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
            ItemStack pair = findMatchingBackpackDraggedPair(pSlot);
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

      @Unique @Nullable
      private ItemStack findMatchingBackpackDraggedPair(Slot pSlot) {
            for (Map.Entry<Slot, ItemStack> slotPair : drag.allSlots.entrySet()) {
                  if (slotPair.getKey() == pSlot) {
                        return slotPair.getValue();
                  }
            }
            return null;
      }
}
