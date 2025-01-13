package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.network.serverbound.TinyMenuInteract;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleScreen;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LunchBoxScreen extends BundleScreen {
      boolean hoverNonEdible = false;

      public static void openScreen(ViewableBackpack backpack, BundleLikeTraits traits, Player player) {
            LunchBoxScreen screen = new LunchBoxScreen(backpack, traits, player);
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(screen);
            backpack.onOpen(minecraft.player);
            TinyMenuInteract.send(backpack.getId(), true);
      }

      protected LunchBoxScreen(ViewableBackpack backpack, BundleLikeTraits traits, Player player) {
            super(player, backpack, traits);
      }

      @Override
      protected void repopulateSlots(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            ItemStack carried = getCarried();
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            List<ItemStack> nonEdibles = backpack.get(ITraitData.NON_EDIBLES);

            int size;
            int stacksSize;
            boolean hasSpace;
            boolean carriedEmpty = carried.isEmpty();
            if (stacks == null) {
                  hasSpace = true;
                  stacksSize = 1;
                  size = 1;
            }
            else {
                  hasSpace = traits.fullness(stacks).compareTo(Fraction.ONE) != 0;
                  stacksSize = stacks.size();
                  size = stacksSize + (hasSpace ? 1 : 0) + (carriedEmpty ? 0 : 1);
            }


            int guiScaledWidth = minecraft.getWindow().getGuiScaledWidth();
            int maxWidth = guiScaledWidth / 2 - 7 * 18;
            int nonEdibleSize = nonEdibles == null ? 0 : nonEdibles.size();

            boolean forCol = false;
            int columns = Math.min(size + nonEdibleSize, 4);
            int rows = 1;
            for (int i = columns; i <= size + nonEdibleSize; i++) {
                  if (i > columns * rows) {
                        if (forCol && maxWidth > 0) {
                              columns++;
                              maxWidth -= 18;
                        } else
                              rows++;
                        forCol = !forCol;
                  }
            }

            clearSlots();
            hoverNonEdible = false;
            boolean hoverOverAny = false;
            int width = columns * 18;
            int left = leftPos - 18 - width;
            int top = traitTop() - 11 - ((rows - 1) / 2) * 10;
            int i = hasSpace ? -1 : 0;

            boolean shouldHoverNonEdibles = nonEdibles != null && carriedEmpty;
            Component title = Component.empty();
            for(int y = 0; y < rows; ++y) {
                  for(int x = 0; x < columns; ++x) {
                        BundleTraitSlot slot;
                        int nonEdiblesStart = size - 1;
                        if (i < nonEdiblesStart) {
                              slot = new LunchBoxSlot(left + x * 18, top + y * 18, i, shouldHoverNonEdibles);
                              if (i == stacksSize)
                                    lastSlot = slot;
                        } else {
                              int index = i - nonEdiblesStart;
                              slot = index == 0
                                     ? new StartNonEdibles(left + x * 18, top + y * 18, index, shouldHoverNonEdibles, carriedEmpty)
                                     : new NonEdiblesSlot(left + x * 18, top + y * 18, index);
                        }
                        boolean mouseOver = slot.isMouseOver(pMouseX, pMouseY);
                        if (mouseOver) {
                              hoverOverAny = true;
                        }
                        if (!shouldHoverNonEdibles && mouseOver) {
                              List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, slot.getItem());
                              title = tooltipFromItem.getFirst();
                        }
                        addSlot(slot);
                        i++;
                  }
            }

            if (hoverOverAny && shouldHoverNonEdibles) {
                  List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, nonEdibles.get(0));
                  title = tooltipFromItem.getFirst();
            }

            int fontWidth = font.width(title);
            int fontHeight = 9;
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.translate(0, 0, 100);
            if (fontWidth > width) {
                  List<FormattedCharSequence> split = font.split(title, width);
                  int height = split.size() * 9;
                  int splitRow = height;
                  for (FormattedCharSequence sequence : split) {
                        gui.drawString(minecraft.font, sequence, left + 1, top - splitRow, 0xFFFFFFFF);
                        splitRow = splitRow - 9;
                  }
                  fontHeight = height;
            }
            else gui.drawString(minecraft.font, title, left + 1, top - 9, 0xFFFFFFFF);

            pose.popPose();

            this.traitX = left + width;
            this.traitY = top - fontHeight;
            this.traitW = width;
            this.traitH = rows * 18 + fontHeight;

            TooltipRenderUtil.renderTooltipBackground(gui, left, top - 1 - fontHeight, width, rows * 18 + 2 + fontHeight, 1);

            for (TraitSlot slot : slots) {
                  slot.render(gui, pMouseX, pMouseY, pPartialTick);
            }
      }

      private class NonEdiblesSlot extends BundleTraitSlot {
            public NonEdiblesSlot(int pX, int pY, int index) {
                  super(pX, pY, index);
            }

            @Override public ItemStack getItem() {
                  List<ItemStack> stacks = backpack.get(ITraitData.NON_EDIBLES);
                  if (stacks == null || index == -1)
                        return ItemStack.EMPTY;

                  return index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
            }
      }

      private class StartNonEdibles extends NonEdiblesSlot {
            private final boolean shouldHoverNonEdibles;
            private final boolean carriedEmpty;

            public StartNonEdibles(int pX, int pY, int index, boolean shouldHoverNonEdibles, boolean carriedEmpty) {
                  super(pX, pY, index);
                  this.shouldHoverNonEdibles = shouldHoverNonEdibles;
                  this.carriedEmpty = carriedEmpty;
            }

            @Override
            public boolean isHovered() {
                  return shouldHoverNonEdibles
                         ? hoverNonEdible || super.isHovered()
                         : carriedEmpty && super.isHovered();
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int i, int i1, float v) {
                  if (isHovered && !shouldHoverNonEdibles && !carriedEmpty && lastSlot != null) {
                        int x1 = lastSlot.getX() + 9;
                        int y1 = lastSlot.getY() + 9;
                        gui.fill(x1 - 8, y1 - 8, x1 + 8, y1 + 8, 100, 0x88FFFFFF);
                  }
                  super.renderWidget(gui, i, i1, v);
            }
      }

      private class LunchBoxSlot extends BundleTraitSlot {
            private final boolean shouldHoverNonEdibles;

            public LunchBoxSlot(int pX, int pY, int index, boolean shouldHoverNonEdibles) {
                  super(pX, pY, index);
                  this.shouldHoverNonEdibles = shouldHoverNonEdibles;
            }

            @Override public boolean isHovered() {
                  boolean hovered = super.isHovered();
                  if (shouldHoverNonEdibles && hovered) {
                        hoverNonEdible = true;
                  }

                  return hovered && !shouldHoverNonEdibles;
            }
      }
}
