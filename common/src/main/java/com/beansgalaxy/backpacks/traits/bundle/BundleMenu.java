package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.serverbound.TraitMenuClick;
import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class BundleMenu<T extends BundleLikeTraits> extends TraitMenu<T> {
      int rows, columns, size;

      public BundleMenu(Minecraft minecraft, int screenLeft, int screenTop, Slot slot, ComponentHolder holder, T traits) {
            super(minecraft, screenLeft, screenTop, slot, holder, traits);
            updateSize();
            topPos -= rows * 6;
      }

      private void updateSize() {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);

            this.size = stacks == null ? 0 : stacks.size();
            boolean hasSpace = traits.fullness(holder).compareTo(Fraction.ONE) != 0;
            int sudoSize = size + (hasSpace ? 1 : 0);

            boolean forCol = false;
            int columns = Math.min(sudoSize, 4);
            int rows = 1;
            for (int i = columns; i <= sudoSize; i++) {
                  if (i > columns * rows) {
                        if (forCol)
                              columns++;
                        else
                              rows++;
                        forCol = !forCol;
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

      @Override
      public void menuClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
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
      public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot == null)
                  return;

            if (keyCode == GLFW.GLFW_KEY_Q) {
                  LocalPlayer player = minecraft.player;
                  AbstractContainerMenu containerMenu = player.containerMenu;
                  TraitMenuClick.Kind clickType = TraitMenuClick.Kind.DROP;

                  TraitMenuClick.send(containerMenu.containerId, slot, hoveredSlot.index, clickType);
                  SlotAccess access = SlotAccess.of(containerMenu::getCarried, containerMenu::setCarried);
                  traits.menuClick(holder, hoveredSlot.index, clickType, access, player);
                  updateSize();
            }
      }

      @Override
      protected void menuRender(AbstractContainerScreen<?> screen, GuiGraphics gui, int mouseX, int mouseY) {
            TooltipRenderUtil.renderTooltipBackground(gui, 8, 4, getWidth() - 16, getHeight() - 8, 0);
            renderItems(gui, mouseX, mouseY);
      }

      @Nullable
      private tSlot hoveredSlot = null;

      private void renderItems(GuiGraphics gui, int mouseX, int mouseY) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks != null && stacks.size() != size)
                  updateSize();

            hoveredSlot = null;

            for (int y = 0; y < rows; y++) {
                  for (int x = 0; x < columns; x++) {
                        int i = y * columns + x;

                        if (i > size)
                              break;

                        int x1 = x * 16 + 8;
                        int y1 = y * 16 + 4;
                        int x2 = x1 + 16;
                        int y2 = y1 + 16;

                        if (mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2) {
                              hoveredSlot = new tSlot(i, x1, x2, y1, y2);
                        }

                        if (i == size)
                              break;

                        ItemStack stack = stacks.get(i);
                        CommonClient.renderItem(minecraft, gui, stack, x1 + 8, y1 + 8, 15, false);
                        CommonClient.renderItemDecorations(gui, minecraft.font, stack, x1 + 8, y1 + 8, 15);
                  }
            }

            if (hoveredSlot != null) {
                  gui.fill(hoveredSlot.x1, hoveredSlot.y1, hoveredSlot.x2, hoveredSlot.y2, 30, 0x60FFFFFF);
            }
      }

      record tSlot(int index, int x1, int x2, int y1, int y2) {

      }
}
