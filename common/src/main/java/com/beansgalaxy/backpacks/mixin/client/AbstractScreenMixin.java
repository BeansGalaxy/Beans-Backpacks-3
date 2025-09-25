package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
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

      @Shadow @Final protected Set<Slot> quickCraftSlots;

      @Shadow protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);

      @Shadow private boolean skipNextRelease;

      @Shadow @Nullable private Slot lastClickSlot;

      @Shadow protected abstract List<Component> getTooltipFromContainerItem(ItemStack pStack);

      protected AbstractScreenMixin(Component pTitle) {
            super(pTitle);
      }

      @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
      protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (this.hoveredSlot != null) {
                  ItemStack itemstack = this.hoveredSlot.getItem();
                  Traits.get(itemstack).ifPresentOrElse(trait -> {
                        trait.client().renderTooltip(trait, itemstack, PatchedComponentHolder.of(itemstack), gui, mouseX, mouseY, ci);
                  }, () -> EnderTraits.get(itemstack).ifPresent(enderTraits -> enderTraits.getTrait().ifPresent(trait ->
                        trait.client().renderTooltip(trait, itemstack, enderTraits, gui, mouseX, mouseY, ci)
                  )));
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

            if (slot != lastClickSlot && slot != drag.backpackDraggedSlot) {
                  IDraggingTrait.runIfPresent(backpack, minecraft.level, ((trait, holder) -> {
                        beans_Backpacks_3$dragTrait(pButton, trait, slot, cir, holder);
                  }));
            }
      }

      @Unique
      private void beans_Backpacks_3$dragTrait(int pButton, IDraggingTrait traits, Slot slot, CallbackInfoReturnable<Boolean> cir, PatchedComponentHolder holder) {
            isQuickCrafting = false;
            skipNextRelease = true;
            if (drag.backpackDraggedSlots.isEmpty()) {
                  drag.backpackDragType = pButton;

                  if (drag.backpackDraggedSlot != null)
                        traits.clickSlot(drag, minecraft.player, holder);
                  else if (lastClickSlot != null) {
                        drag.backpackDraggedSlot = lastClickSlot;
                        traits.clickSlot(drag, minecraft.player, holder);
                  }
            }
            else if (drag.backpackDraggedSlot != null)
                  traits.clickSlot(drag, minecraft.player, holder);

            drag.backpackDraggedSlot = slot;
            cir.setReturnValue(true);
      }

      @Inject(method = "mouseReleased", at = @At("HEAD"))
      public void backpackReleased(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
            if (!drag.backpackDraggedSlots.isEmpty()) {
                  if (drag.backpackDraggedSlot != null) {
                        ItemStack backpack = menu.getCarried();

                        IDraggingTrait.runIfPresent(backpack, minecraft.level, (trait, holder) -> {
                              trait.clickSlot(drag, minecraft.player, PatchedComponentHolder.of(backpack));
                        });

                        drag.backpackDraggedSlot = null;
                  }
                  drag.backpackDraggedSlots.clear();
            }
      }

      @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
      public void renderBackpackDraggedSlot(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
            ItemStack pair = findMatchingBackpackDraggedPair(pSlot);
            if (pair != null) {
                  int i = pSlot.x;
                  int j = pSlot.y;
                  boolean isPickup = drag.backpackDragType != 0;
                  if (isPickup) {
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

                  pGuiGraphics.fill(i, j, i + 16, j + 16, isPickup ? 200 : 0,-2130706433);
            }
      }

      @Unique @Nullable
      private ItemStack findMatchingBackpackDraggedPair(Slot pSlot) {
            for (Map.Entry<Slot, ItemStack> slotPair : drag.backpackDraggedSlots.entrySet()) {
                  if (slotPair.getKey() == pSlot) {
                        return slotPair.getValue();
                  }
            }
            return null;
      }
}
