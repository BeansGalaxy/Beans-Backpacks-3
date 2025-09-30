package com.beansgalaxy.backpacks.traits.chest.screen;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.screen.TinyTraitScreen;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.Optional;

public abstract class TinyChestScreen extends TinyTraitScreen {
      private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.parse(Constants.MOD_ID + ":textures/gui/generic_scalable.png");
      final ChestTraits traits;

      public TinyChestScreen(ChestTraits traits) {
            super(Component.literal("Chest Trait Screen"));
            this.traits = traits;
      }

      @Override
      protected void init() {
            super.init();
            Window window = minecraft.getWindow();
            int scaledHeight = window.getGuiScaledHeight();
            int scaledWidth = window.getGuiScaledWidth();
            this.leftPos = scaledWidth / 2 - traits.columns * 7;
            this.setTopPos(scaledHeight / 2 - traits.rows * 7);

            for(int y = 0; y < traits.rows; ++y) {
                  for (int x = 0; x < traits.columns; ++x) {
                        int index = y * traits.columns + x;
                        TinyChestSlot chestTraitSlot = new TinyChestSlot(x * 18 + leftPos, y * 18 + getTopPos(), index);
                        addSlot(chestTraitSlot);
                        addRenderableWidget(chestTraitSlot);
                  }
            }
      }

      public abstract ItemStack getStack();

      public abstract ComponentHolder getHolder();

      public abstract boolean isFocused();

      @Override
      public void render(GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            for(int y = 0; y < traits.rows; ++y) {
                  for(int x = 0; x < traits.columns; ++x) {
                        gui.blit(CONTAINER_BACKGROUND, leftPos + x * 18, getTopPos() + y * 18, 1, 0, 0, 18, 18, 256, 256);
                  }
            }

            super.render(gui, pMouseX, pMouseY, pPartialTick);
            List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, getStack());

            int tooltipWidth = 0;
            for (Component line : tooltipFromItem) {
                  int j = font.width(line);
                  if (j > tooltipWidth)
                        tooltipWidth = j;
            }

            int x = leftPos - tooltipWidth - 20;
            int y;

            if (x < 5) {
                  x = leftPos;
                  y = getTopPos() - tooltipFromItem.size() * 12 + 6;
            }
            else {
                  y = getTopPos() + 18 + traits.rows;
            }

            gui.renderTooltip(font, tooltipFromItem, Optional.empty(), x, y);
      }

      public class TinyChestSlot extends TinyTraitSlot {
            public TinyChestSlot(int pX, int pY, int index) {
                  super(pX, pY, index);
            }

            @Override
            public ItemStack getItem() {
                  ItemContainerContents contents = getHolder().get(ITraitData.CHEST);
                  if (contents == null)
                        return ItemStack.EMPTY;

                  NonNullList<ItemStack> pList = NonNullList.withSize(traits.size(), ItemStack.EMPTY);
                  contents.copyInto(pList);
                  return index < pList.size() ? pList.get(index) : ItemStack.EMPTY;
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float v) {
                  ItemStack stack = getItem();
                  boolean focused = TinyChestScreen.this.isFocused();
                  boolean hovered = isHovered() && focused;
                  int x = getX() + 9;
                  int y = getY() + 9;

                  if (!stack.isEmpty()) {
                        Minecraft minecraft = Minecraft.getInstance();
                        CommonClient.renderItem(minecraft, gui, stack, x, y, 50, false);
                        CommonClient.renderItemDecorations(gui, font, stack, x, y, 50);

                        if (hovered && getCarried().isEmpty()) {
                              List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, stack);
                              Optional<TooltipComponent> tooltipImage = stack.getTooltipImage();
                              gui.renderTooltip(font, tooltipFromItem, tooltipImage, mouseX, mouseY);
                        }
                  }

                  if (hovered)
                        gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x88FFFFFF);

            }
      }
}
