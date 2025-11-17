package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.network.serverbound.TraitMenuClick;
import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BundleMenu<T extends BundleLikeTraits> extends TraitMenu<T> {
      int rows, columns, size;

      public BundleMenu(Minecraft minecraft, int screenLeft, int screenTop, int screenHeight, int screenWidth, Slot slot, ComponentHolder holder, T traits) {
            super(minecraft, screenLeft, screenTop, screenHeight, screenWidth, slot, holder, traits);
            updateSize();
            
            double offset = slot.y * 0.1 - 2;
            int drift = (int) ((rows - 1) * offset);
            topPos -= drift + 2;
      }
      
      protected boolean hasSpace() {
            return traits.fullness(holder).compareTo(Fraction.ONE) != 0;
      }

      private void updateSize() {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);

            this.size = stacks == null ? 0 : stacks.size();
            int sudoSize = size + (hasSpace() ? 1 : 0);

            int forRow = 1;
            int columns = Math.min(sudoSize, 4);
            int rows = 1;
            for (int i = columns; i <= sudoSize; i++) {
                  if (i > columns * rows) {
                        if (forRow == 2) {
                              columns++;
                              forRow = 0;
                        }
                        else {
                              rows++;
                              forRow++;
                        }
                  }
            }

            this.columns = columns;
            this.rows = rows;
      }

      @Override
      protected int getWidth() {
            return columns * 16 + 16;
      }

      @Override
      protected int getHeight() {
            return rows * 16 + 8;
      }
      
      private final class DragInstance {
            final double startX;
            final double startY;
            final int button;
            boolean hasMoved = false;
            double currentX;
            double currentY;
            
            private DragInstance(double startX, double startY, int button) {
                  this.startX = startX;
                  this.startY = startY;
                  this.button = button;
                  currentX = startX;
                  currentY = startY;
            }
            
            void move(double mouseX, double mouseY) {
                  double oX = mouseX - currentX;
                  double oY = mouseY - currentY;
                  
                  currentX += oX;
                  currentY += oY;
                  leftPos += oX;
                  topPos += oY;
            }
      }
      @Nullable private DragInstance dragInstance = null;
      
      @Override
      public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
            super.mouseDragged(mouseX, mouseY, button, dragX, dragY, cir);
            
            if (dragInstance != null) {
                  if (!dragInstance.hasMoved) {
                        dragInstance.hasMoved = true;
                  }
                  else {
                        dragInstance.move(mouseX, mouseY);
                  }
            }
            else if (hoveredSlot == null || hoveredSlot.isEmpty) {
                  LocalPlayer player = minecraft.player;
                  AbstractContainerMenu containerMenu = player.containerMenu;
                  if (containerMenu.getCarried().isEmpty()) {
                        dragInstance = new DragInstance(mouseX, mouseY, button);
                        cir.setReturnValue(true);
                  }
            }
      }
      
      @Override
      public void menuClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
            dragInstance = null;
            
            if (hoveredSlot != null) {
                  LocalPlayer player = minecraft.player;
                  AbstractContainerMenu containerMenu = player.containerMenu;
                  TraitMenuClick.Kind clickType = getClickType(button);

                  TraitMenuClick.send(containerMenu.containerId, slot, hoveredSlot.index, clickType);
                  SlotAccess access = SlotAccess.of(containerMenu::getCarried, containerMenu::setCarried);
                  traits.menuClick(holder, hoveredSlot.index, clickType, access, player);
                  cir.setReturnValue(true);
            }

            updateSize();
      }

      private TraitMenuClick.Kind getClickType(int button) {
            boolean eitherShiftDown = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 340)
                        || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 344);

            if (eitherShiftDown)
                  return TraitMenuClick.Kind.SHIFT;

            if (button == 1)
                  return TraitMenuClick.Kind.RIGHT;

            return TraitMenuClick.Kind.LEFT;
      }
      
      @Override
      public void dropHoveredItem(CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot == null)
                  return;
            
            LocalPlayer player = minecraft.player;
            AbstractContainerMenu containerMenu = player.containerMenu;
            TraitMenuClick.Kind clickType = TraitMenuClick.Kind.DROP;
            
            TraitMenuClick.send(containerMenu.containerId, slot, hoveredSlot.index, clickType);
            SlotAccess access = SlotAccess.of(containerMenu::getCarried, containerMenu::setCarried);
            traits.menuClick(holder, hoveredSlot.index, clickType, access, player);
            updateSize();
      }

      @Override
      protected void menuRender(AbstractContainerScreen<?> screen, GuiGraphics gui, int mouseX, int mouseY) {
            if (isFocused())
                  renderTooltipBackground(gui, 8, 4, getWidth() - 16, getHeight() - 8, 0);
            else
                  TooltipRenderUtil.renderTooltipBackground(gui, 8, 4, getWidth() - 16, getHeight() - 8, 0);
            
            renderItems(gui, mouseX, mouseY);
      }
      
      public static void renderTooltipBackground(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ) {
            int i = pX - 3;
            int j = pY - 3;
            int k = pWidth + 3 + 3;
            int l = pHeight + 3 + 3;
            renderHorizontalLine(pGuiGraphics, i, j - 1, k, pZ, -267386864);
            renderHorizontalLine(pGuiGraphics, i, j + l, k, pZ, -267386864);
            renderRectangle(pGuiGraphics, i, j, k, l, pZ, -267386864);
            renderVerticalLine(pGuiGraphics, i - 1, j, l, pZ, -267386864);
            renderVerticalLine(pGuiGraphics, i + k, j, l, pZ, -267386864);
            renderFrameGradient(pGuiGraphics, i, j + 1, k, l, pZ, 0xA0FFAA00, 0x50EB7114);
      }
      
      private static void renderFrameGradient(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ, int pTopColor, int pBottomColor) {
            renderVerticalLineGradient(pGuiGraphics, pX, pY, pHeight - 2, pZ, pTopColor, pBottomColor);
            renderVerticalLineGradient(pGuiGraphics, pX + pWidth - 1, pY, pHeight - 2, pZ, pTopColor, pBottomColor);
            renderHorizontalLine(pGuiGraphics, pX, pY - 1, pWidth, pZ, pTopColor);
            renderHorizontalLine(pGuiGraphics, pX, pY - 1 + pHeight - 1, pWidth, pZ, pBottomColor);
      }
      
      private static void renderVerticalLine(GuiGraphics pGuiGraphics, int pX, int pY, int pLength, int pZ, int pColor) {
            pGuiGraphics.fill(pX, pY, pX + 1, pY + pLength, pZ, pColor);
      }
      
      private static void renderVerticalLineGradient(GuiGraphics pGuiGraphics, int pX, int pY, int pLength, int pZ, int pTopColor, int pBottomColor) {
            pGuiGraphics.fillGradient(pX, pY, pX + 1, pY + pLength, pZ, pTopColor, pBottomColor);
      }
      
      private static void renderHorizontalLine(GuiGraphics pGuiGraphics, int pX, int pY, int pLength, int pZ, int pColor) {
            pGuiGraphics.fill(pX, pY, pX + pLength, pY + 1, pZ, pColor);
      }
      
      private static void renderRectangle(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ, int pColor) {
            pGuiGraphics.fill(pX, pY, pX + pWidth, pY + pHeight, pZ, pColor);
      }

      @Nullable
      private tSlot hoveredSlot = null;

      private void renderItems(GuiGraphics gui, int mouseX, int mouseY) {
            updateSize();

            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            hoveredSlot = null;
            tSlot lastSlot = null;

            for (int y = 0; y < rows; y++) {
                  for (int x = 0; x < columns; x++) {
                        int i = y * columns + x;

                        int x1 = x * 16 + 8;
                        int y1 = y * 16 + 4;
                        int x2 = x1 + 16;
                        int y2 = y1 + 16;
                        
                        boolean isHovered = isFocused && mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2;
                        
                        if (i == size) {
                              lastSlot = new tSlot(i, x1, x2, y1, y2, true);
                        }
                        
                        if (i >= size) {
                              if (isHovered)
                                    hoveredSlot = lastSlot;
                              continue;
                        }
                        
                        if (isHovered)
                              hoveredSlot = new tSlot(i, x1, x2, y1, y2, false);
                        
                        if (stacks == null)
                              break;
                        
                        Font font = minecraft.font;
                        ItemStack stack = stacks.get(i);
                        PoseStack pose = gui.pose();
                        pose.pushPose();
                        CommonClient.renderItem(minecraft, gui, stack, x1 + 8, y1 + 8, 50, false);
                        CommonClient.renderItemDecorations(gui, font, stack, x1 + 8, y1 + 8, 50);
                        pose.popPose();
                        
                        if (isHovered) {
                              List<Component> lines = Screen.getTooltipFromItem(this.minecraft, stack);
                              Optional<TooltipComponent> image = stack.getTooltipImage();
                              
                              Integer imageHeight = image.map(ClientTooltipComponent::create).map(ClientTooltipComponent::getHeight).orElse(0);
                              int linesSize = lines.size();
                              int linesHeight = linesSize < 2 ? 12 : 14 + (linesSize - 1) * 10;
                              
                              int height = imageHeight + linesHeight;
                              gui.renderTooltip(font, lines, image, -4, -height + 10);
                        }
                  }
            }

            if (hoveredSlot != null)
                  gui.fill(hoveredSlot.x1, hoveredSlot.y1, hoveredSlot.x2, hoveredSlot.y2, 100, 0x60FFFFFF);
      }

      record tSlot(int index, int x1, int x2, int y1, int y2, boolean isEmpty) {

      }
}
