package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class BundleTooltip implements ClientTooltipComponent {
      private static final int SPACING = 17;
      protected final ArrayList<ItemStack> itemStacks;
      protected final Minecraft minecraft;
      private final int size;
      private final boolean hasSpace;
      private final int columns;
      private final int rows;
      private final ItemStack itemstack;
      private final Component title;
      protected final int selectedSlot;
      protected final boolean carriedEmpty;

      public BundleTooltip(BundleLikeTraits traits, ItemStack itemStack, ArrayList<ItemStack> stacks, PatchedComponentHolder holder, Component title) {
            this.itemstack = itemStack;
            this.title = title;
            this.itemStacks = stacks;
            this.minecraft = Minecraft.getInstance();
            this.size = itemStacks.size();

            this.hasSpace = traits.fullness(holder).compareTo(Fraction.ONE) != 0;
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

            LocalPlayer player = minecraft.player;
            long window = Minecraft.getInstance().getWindow().getWindow();
            boolean isQuickMove =  BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(window, 340) || InputConstants.isKeyDown(window, 344);
            this.carriedEmpty = minecraft.player.containerMenu.getCarried().isEmpty();
            boolean hideEmptySlot = carriedEmpty || isQuickMove || !hasSpace;
            int slot1 = traits.getSelectedSlot(holder, player);
            this.selectedSlot = hideEmptySlot && slot1 == 0
                        ? 0
                        : slot1 -1;

      }


      @Override
      public int getHeight() {
            return rows * SPACING + 2;
      }

      @Override
      public int getWidth(Font font) {
            return columns * SPACING + 1;
      }

      @Override
      public void renderImage(Font font, int cursorX, int cursorY, GuiGraphics gui) {
            int tooltipY = cursorY + rows * 2 - 2;
            int firstWidth = font.width(title);
            int tooltipWidth = Math.max(firstWidth, getWidth(font));
            renderHoveredItemTooltip(minecraft, gui, font, cursorX, tooltipY, tooltipWidth, itemstack);

            Iterator<ItemStack> stacks = itemStacks.iterator();

            int topPos = cursorY + 7;
            int leftPos = cursorX + 9;

            if (selectedSlot == -1) {
                  int fillX = leftPos - 9;
                  int fillY = topPos - 9;
                  gui.fill(fillX + 1, fillY + 1, fillX + 17, fillY + 17, 500, 0x60FFFFFF);
            }

            int i = 0;
            int x = hasSpace ? 1 : 0;
            int y = 0;
            while (stacks.hasNext()) {
                  while (x < columns) {
                        int x1 = x * SPACING + leftPos;
                        int y1 = y * SPACING + topPos;
                        drawSlot(font, gui, stacks, x1, y1, i);
                        i++;
                        x++;
                  }
                  x = 0;
                  y++;
            }
      }

      protected void drawSlot(Font font, GuiGraphics gui, Iterator<ItemStack> stacks, int x, int y, int index) {
            if (stacks.hasNext()) {
                  ItemStack stack = stacks.next();
                  renderItem(minecraft, gui, stack, x, y, 300, false);
                  renderItemDecorations(gui, font, stack, x, y, 300);
            }

            if (index == selectedSlot) {
                  int fillX = x - 9;
                  int fillY = y - 9;
                  gui.fill(fillX + 1, fillY + 1, fillX + 17, fillY + 17, 500, 0x60FFFFFF);
            }
      }

      public static boolean renderHoveredItemTooltip(Minecraft minecraft, GuiGraphics gui, Font font, int cursorX, int cursorY, int tooltipWidth, ItemStack stack) {
            boolean carriedEmpty = minecraft.player.containerMenu.getCarried().isEmpty();
            if (carriedEmpty) {
                  List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, stack);
                  int width = 0;
                  for (Component line : tooltipFromItem) {
                        int i = font.width(line);
                        if (i > width)
                              width = i;
                  }

                  width += 35;
                  boolean offsetItemTooltip = width - 14 > cursorX;

                  int pMouseX = offsetItemTooltip
                              ? cursorX + tooltipWidth
                              : cursorX - width;
                  gui.renderTooltip(font, tooltipFromItem, Optional.empty(), pMouseX, cursorY);
            }
            return carriedEmpty;
      }

      public static void renderItemDecorations(GuiGraphics gui, Font font, ItemStack $$1, int x, int y, int z) {
            if (!$$1.isEmpty()) {
                  PoseStack pose = gui.pose();
                  pose.pushPose();
                  pose.translate(0.0F, 0.0F, z + 10);
                  int count = $$1.getCount();
                  if (count != 1) {
                        String string = String.valueOf(count);
                        if (count > 999) {
                              float k = count / 1000f;
                              String s = (int) k + "k";
                              gui.drawString(font, s, x + 9 - font.width(s), y + 1, 0xFFFFFFFF, true);
                        }
                        else if (count > 99) {
                              char[] chars = string.toCharArray();

                              for (int i1 = 0; i1 < chars.length; i1++) {
                                    char c = chars[i1];
                                    String s = CommonClass.getTinyNumberFromDigitChar(c);
                                    chars[i1] = s.toCharArray()[0];
                              }

                              String s = String.valueOf(chars);
                              gui.drawString(font, s, x + 10 - font.width(s), y - 1, 0xFFFFFFFF, true);
                        }
                        else {
                              gui.drawString(font, string, x + 9 - font.width(string), y + 1, 0xFFFFFFFF, true);
                        }
                  }
                  else if ($$1.isBarVisible()) {
                        int barColor = $$1.getBarColor();
                        int barX = x - 6;
                        int barY = y + 5;
                        gui.fill(barX, barY, barX + 13, barY + 2, 0xFF000000);
                        gui.fill(barX, barY, barX + $$1.getBarWidth(), barY + 1, barColor | -16777216);
                  }
                  pose.popPose();
            }
      }

      public static void renderItem(Minecraft minecraft, GuiGraphics gui, ItemStack stack, int x, int y, int z, boolean drawShadows) {
            PoseStack pose = gui.pose();
            pose.pushPose();
            BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, minecraft.player, 0);
            pose.translate(x, y, z);

            try {
                  pose.mulPose((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
                  pose.scale(16.0F, 16.0F, 16.0F);
                  boolean $$8 = !model.usesBlockLight();
                  if ($$8) {
                        Lighting.setupForFlatItems();
                  }

                  minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, gui.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, model);
                  if (drawShadows && !model.isGui3d()) {
                        pose.translate(1/16f, -1/16f, -1/16f);
                        minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, gui.bufferSource(), 0, OverlayTexture.NO_OVERLAY, model);
                  }

                  gui.flush();
                  if ($$8) {
                        Lighting.setupFor3DItems();
                  }
            } catch (Throwable var12) {
                  CrashReport $$10 = CrashReport.forThrowable(var12, "Rendering item");
                  CrashReportCategory $$11 = $$10.addCategory("Item being rendered");
                  $$11.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                  $$11.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                  $$11.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                  throw new ReportedException($$10);
            }

            pose.popPose();
      }
}
